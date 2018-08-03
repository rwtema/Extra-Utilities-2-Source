package com.rwtema.extrautils2.quarry;

import com.google.common.collect.ComparisonChain;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.fairies.Fairy;
import com.rwtema.extrautils2.itemhandler.XUTileItemStackHandler;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.utils.PositionPool;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;

public class TileQuarryConvoluted extends XUTile implements ITickable {
	static final EnumFacing[][] sidePriority =
			new EnumFacing[][]{
					new EnumFacing[]{EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST},
					new EnumFacing[]{EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.NORTH},
					new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.WEST},
					new EnumFacing[]{EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH},
			};
	final PositionPool pool = new PositionPool();
	final Digger[] diggers;
	final Limits limits = registerNBT("limits", new Limits());
	int curLevel = 0;
	boolean initialized;
	ItemStackHandler tools = new XUTileItemStackHandler(9, this) {
		@Override
		protected int getStackLimit(int slot, ItemStack stack) {

			return super.getStackLimit(slot, stack);
		}
	};
	NBTSerializable.NBTCollection<ChunkPos, Set<ChunkPos>, NBTTagLong> deadChunks = registerNBT("captured_chunks",
			new NBTSerializable.NBTCollection<>(new HashSet<>(),
					chunkPos -> new NBTTagLong(((long) chunkPos.x << 32) | (chunkPos.z & 0xFFFFFFFFL)),
					nbtTagLong -> {
						long c = nbtTagLong.getLong();
						return new ChunkPos((int) (c >> 32), (int) c);
					}
			));

	{
		diggers = new Digger[32];
		for (int i = 0; i < diggers.length; i++) {
			diggers[i] = registerNBT("digger_i", new Digger(i));
		}
	}

	public Block getSludgeBlock() {
		return Blocks.COBBLESTONE;
//		return XU2Entries.quarry_sludge.value;
	}

	@Override
	public void update() {
		if (world.isRemote) return;


		if (limits.isBlank()) {
			limits.x_min = Math.min(pos.getX() + 1, pos.getX() + 96);
			limits.x_max = Math.max(pos.getX() + 1, pos.getX() + 96);
			limits.z_min = Math.min(pos.getZ() + 1, pos.getZ() + 96);
			limits.z_max = Math.max(pos.getZ() + 1, pos.getZ() + 96);
			limits.roof = pos.getY();

			for (Digger digger : diggers) {
				digger.active.value = true;
				findGoodChunk(digger);
			}
		}

		if (!initialized) {
			for (Digger digger : diggers) {
				digger.joinWorld(world, new Vec3d(pos).addVector(0.5, 0.5, 0.5));
			}
			initialized = true;
		}

		long l = world.getTotalWorldTime() % 20;
		if (l == 0)
			pool.clear();

		for (int i = 0; i < 100; i++) {
			for (Digger digger : diggers) {
				if (digger.active.value) {
					if (digger.atDestination()) {
						if (dig(digger)) {
							digger.digTime.value = 0;
							findNextDest(digger);

							if (!digger.dead) {
								double curDist = digger.target.distanceSq(digger.pos.x, digger.pos.y, digger.pos.z);
								for (Digger digger1 : diggers) {
									if (!digger.active.value) continue;
									double curDistOther = digger1.target.distanceSq(digger1.pos.x, digger1.pos.y, digger1.pos.z);
									double newDist = digger.target.distanceSq(digger1.pos.x, digger1.pos.y, digger1.pos.z);
									double newDistOther = digger1.target.distanceSq(digger.pos.x, digger.pos.y, digger.pos.z);
									if ((newDist + newDistOther) < (curDist + curDistOther)) {
										BlockPos t = digger1.target.toImmutable();
										digger1.target.setPos(digger.target);
										digger.target.setPos(t);
										curDist = digger.target.distanceSq(digger.pos.x, digger.pos.y, digger.pos.z);
									}
								}
							}
						}
					} else {
						digger.moveTick();
					}
				} else {
					digger.dead = true;
				}
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (Digger digger : diggers) {
			digger.dead = true;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		for (Digger digger : diggers) {
			digger.dead = true;
		}
	}

	private void findNextDest(Digger digger) {
		NBTSerializable.NBTMutableBlockPos target = digger.target;

		int y = target.getY();
		int x = target.getX();
		int z = target.getZ();

		if (!findDiggableBlockInChunk(digger, y, x, z)) {
			deadChunks.collection.add(new ChunkPos(x >> 4, z >> 4));
			findGoodChunk(digger);
		}
	}

	private boolean findDiggableBlockInChunk(Digger digger, int y, int x, int z) {
		HashSet<BlockPos> blacklist = new HashSet<>();
		for (Digger digger1 : diggers) {
			if (digger != digger1 && digger1.active.value) {
				blacklist.add(digger1.target);
			}
		}

		Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
		ExtendedBlockStorage[] array = chunk.getBlockStorageArray();

		while (y >= 0) {
			ExtendedBlockStorage storage = array[y >> 4];
			if (storage != Chunk.NULL_BLOCK_STORAGE && !storage.isEmpty()) {
				if (findDiggableBlockInChunkRow(digger, x, y, z, blacklist)) {
					return true;
				}
				y--;
			} else {
				y = ((y >> 4) << 4) - 1;
			}
		}

		return false;
	}

	private void findGoodChunk(Digger digger) {
		TObjectIntHashMap<ChunkPos> currentlyAssigned = new TObjectIntHashMap<>(10, 0.5F, 0);

		ArrayList<ChunkPos> pos = new ArrayList<>();
		for (int x = (limits.x_min >> 4); x <= (limits.x_max >> 4); x++) {
			for (int z = (limits.z_min >> 4); z <= (limits.z_max >> 4); z++) {
				ChunkPos e = new ChunkPos(x, z);
				if (!deadChunks.collection.contains(e))
					pos.add(e);
			}
		}

		for (Digger digger1 : diggers) {
			if (digger1 != digger && digger1.active.value) {
				currentlyAssigned.adjustOrPutValue(new ChunkPos(digger1.target), 1, 1);
			}
		}

		Collections.shuffle(pos);

		TreeSet<ChunkPos> orderedSet = new TreeSet<>(new Comparator<ChunkPos>() {
			@Override
			public int compare(ChunkPos o1, ChunkPos o2) {

				return ComparisonChain.start()
						.compare(currentlyAssigned.get(o1), currentlyAssigned.get(o2))
						.compare(getSuitability(o1), getSuitability(o2))
						.result();
			}

			private int getSuitability(ChunkPos o1) {
				return Math.abs(o1.x + 8 - limits.centerX()) * 8
						+ Math.abs(o1.z + 8 - limits.centerZ()) * 8
//						+ Math.abs(o1.chunkXPos - (digger.target.getX() >> 4))
//						+ Math.abs(o1.chunkZPos - (digger.target.getZ() >> 4))
						;
			}
		});

		orderedSet.addAll(pos);
		for (ChunkPos p : orderedSet) {
			BlockPos centerBlock = CompatHelper.getCenterBlock(p, limits.roof);
			if (findDiggableBlockInChunk(digger, centerBlock.getY(),
					MathHelper.clamp(centerBlock.getX(), limits.x_min, limits.x_max),
					MathHelper.clamp(centerBlock.getZ(), limits.z_min, limits.z_max))) {
				return;
			} else {
				deadChunks.collection.add(p);
			}
		}

		digger.active.value = false;
		digger.dead = true;
	}

	private boolean findDiggableBlockInChunkRow(Digger digger, int x, int y, int z, Set<BlockPos> blacklist) {
		BlockPos.MutableBlockPos test = new BlockPos.MutableBlockPos();

		for (EnumFacing facing : sidePriority[world.rand.nextInt(4)]) {
			int x2 = x + facing.getFrontOffsetX();
			int z2 = z + facing.getFrontOffsetZ();
			if ((x2 >> 4) != (x >> 4) || (z2 >> 4) != (z >> 4)) {
				continue;
			}

			if (!limits.contains(x2, y, z2))
				continue;

			test.setPos(x2, y, z2);
			if (blacklist.contains(test)) continue;
			IBlockState state = world.getBlockState(test);
			state = state.getActualState(world, pos);
			if (checkIsValid(test, state)) {
				float hardness = state.getBlockHardness(world, pos);
				if (hardness >= 0) {
					digger.setTarget(test);
					return true;
				}
			}
		}


		LinkedList<BlockPos> toTest = new LinkedList<>();
		HashSet<BlockPos> testedPositions = new HashSet<>();
		toTest.add(pool.getPos(x, y, z));
		BlockPos t;
		while ((t = toTest.pollFirst()) != null) {
			testedPositions.add(t);
			for (EnumFacing facing : sidePriority[world.rand.nextInt(4)]) {
				BlockPos offset = pool.offset(t, facing);
				if ((offset.getX() >> 4) != (t.getX() >> 4) || (offset.getZ() >> 4) != (t.getZ() >> 4)) {
					continue;
				}

				if (!limits.contains(offset.getX(), y, offset.getZ()))
					continue;

				if (!blacklist.contains(offset)) {
					IBlockState state = world.getBlockState(offset);
					state = state.getActualState(world, pos);
					if (checkIsValid(offset, state)) {
						float hardness = state.getBlockHardness(world, pos);
						if (hardness >= 0) {
							digger.setTarget(offset);
							return true;
						}
					}
				}

				if (!testedPositions.contains(offset)) {
					testedPositions.add(offset);
					toTest.add(offset);
				}
			}
		}

		return false;
	}

	private boolean checkIsValid(BlockPos offset, IBlockState state) {
		Block block;
		return state != BlockStates.AIR && !state.getBlock().isAir(state, world, offset) && (block = state.getBlock()) != this.getSludgeBlock() && (!state.getMaterial().isLiquid() || (limits.isOnBorder(offset.getX(), offset.getZ()) && isLiquidJustOutside(offset)));
	}

	private boolean isLiquidJustOutside(BlockPos target) {
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos offset = pool.offset(target, facing);
			if (world.getBlockState(offset).getMaterial().isLiquid()) {
				if (limits.isJustOutsideBorder(offset.getX(), offset.getZ()) &&
						!world.getBlockState(offset.up()).getMaterial().isLiquid()) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean dig(Digger digger) {
		NBTSerializable.NBTMutableBlockPos target = digger.target;
		IBlockState state = world.getBlockState(target);
		if (!checkIsValid(target, state))
			return true;

		int digTime = digger.digTime.value;
		float hardness = state.getBlockHardness(world, target);
		if (hardness < -1) {
			return true;
		}

		if (state.getMaterial().isLiquid()) {
			if (limits.isOnBorder(target.getX(), target.getZ())) {
				for (EnumFacing facing : EnumFacing.HORIZONTALS) {
					BlockPos offset = pool.offset(target, facing);
					if (world.getBlockState(offset).getMaterial().isLiquid()) {
						if (limits.isJustOutsideBorder(offset.getX(), offset.getZ()) &&
								!world.getBlockState(offset.up()).getMaterial().isLiquid()) {
							world.setBlockState(offset, Blocks.COBBLESTONE.getDefaultState());
						}
					}
				}

				BlockPos up = pool.offset(target, EnumFacing.UP);
				if (world.getBlockState(up).getMaterial().isLiquid()) {
					for (EnumFacing facing : EnumFacing.HORIZONTALS) {
						BlockPos offset = pool.offset(up, facing);
						if (world.isAirBlock(offset)) {
							world.setBlockState(offset, getSludgeBlock().getDefaultState());
						}
					}
				} else {
					world.setBlockState(target, getSludgeBlock().getDefaultState());
				}
			}
			return true;
		} else {
			if (hardness * 10 > digTime) {
				digger.digTime.value++;
				return false;
			}
		}

		boolean flag = true;

		BlockPos up = pool.offset(target, EnumFacing.UP);
		if (world.getBlockState(up).getMaterial().isLiquid()) {
			flag = false;
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				BlockPos offset = pool.offset(up, facing);
				if (world.isAirBlock(offset)) {
					world.setBlockState(offset, getSludgeBlock().getDefaultState());
				}
			}
		}

		if (flag)
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				BlockPos offset = pool.offset(target, facing);
				if (world.getBlockState(offset).getMaterial().isLiquid()) {
					if (limits.isJustOutsideBorder(offset.getX(), offset.getZ()) &&
							!world.getBlockState(offset.up()).getMaterial().isLiquid()) {
						world.setBlockState(offset, Blocks.COBBLESTONE.getDefaultState());
						continue;
					}
					flag = false;
					break;

				}
			}

		if (flag)
			world.setBlockToAir(target);
		else {
			world.setBlockState(target, getSludgeBlock().getDefaultState());
		}

		digger.digTime.value = 0;
		return true;
	}

	public class Digger extends Fairy {
		final int index;
		NBTSerializable.NBTBoolean active = registerNBT("active", new NBTSerializable.NBTBoolean(false));
		NBTSerializable.NBTMutableBlockPos target = registerNBT("target", new NBTSerializable.NBTMutableBlockPos());
		NBTSerializable.Int digTime = registerNBT("digging_time", new NBTSerializable.Int());

		public Digger(int index) {
			this.index = index;
		}

		public void setTarget(BlockPos targetPos) {
			target.setPos(targetPos);
			Vec3d dest = new Vec3d(targetPos).addVector(0.5, 1, 0.5);
			double dx = this.pos.x - dest.x;
			double dy = this.pos.y - dest.y;
			double dz = this.pos.z - dest.z;
			double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (d > 1.2) {
				double d_inv = 1.2 / d;
				dx *= d_inv;
				dy *= d_inv;
				dz *= d_inv;
			}

			dest.addVector(dx, dy, dz);

			moveToDest(dest.addVector(0.5, 1.5, 0.5), 0.05);
			digTime.value = 0;
		}
	}

	public class Limits implements INBTSerializable<NBTTagIntArray> {

		int x_min, x_max;
		int z_min, z_max;
		int roof;

		public Limits() {

		}

		public void setLimits(int x_min, int x_max, int z_min, int z_max, int roof) {
			this.x_min = Math.min(x_min, x_max);
			this.x_max = Math.max(x_min, x_max);
			this.z_min = Math.min(z_min, z_max);
			this.z_max = Math.max(z_min, z_max);
			this.roof = roof;
		}

		@Override
		public NBTTagIntArray serializeNBT() {
			return new NBTTagIntArray(new int[]{x_min, x_max, z_min, z_max, roof});
		}

		@Override
		public void deserializeNBT(NBTTagIntArray nbt) {
			int[] ints = nbt.getIntArray();
			x_min = ints[0];
			x_max = ints[1];
			z_min = ints[2];
			z_max = ints[3];
			roof = ints[4];
		}

		public boolean isBlank() {
			return x_max == x_min && roof == 0 && z_max == z_min;
		}

		public boolean isJustOutsideBorder(int x, int z) {
			return x == x_min - 1 || z == z_min - 1 || x == x_max + 1 || z == z_max + 1;
		}

		public boolean isOnBorder(int x, int z) {
			return x == x_min || z == z_min || x == x_max || z == z_max;
		}

		public boolean contains(int x, int y, int z) {
//			return true;
			return x >= x_min && x <= x_max
					&& z >= z_min && z <= z_max
					&& y >= 0 && y <= roof;
		}

		public int centerX() {
			return (x_max + x_min) / 2;
		}

		public int centerZ() {
			return (z_max + z_min) / 2;
		}


	}
}
