package com.rwtema.extrautils2.tile;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.blocks.BlockCursedEarth;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IWidget;
import com.rwtema.extrautils2.gui.backend.WidgetTextData;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.StringUtils;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class TileSuperMobSpawner extends TileAdvInteractor implements ITESRHook {
	MyMobSpawnerBaseLogic logic = registerNBT("spawner", new MyMobSpawnerBaseLogic() {


		@Override
		public void broadcastEvent(int id) {
			getWorld().addBlockEvent(getPos(), Blocks.MOB_SPAWNER, id, 0);
		}

		@Override
		public World getSpawnerWorld() {
			return getWorld();
		}

		@Override
		public BlockPos getSpawnerPosition() {
			return getPos();
		}
	});

	public TileSuperMobSpawner() {

	}

	@Override
	public void render(IBlockAccess world, BlockPos pos, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer, BlockRendererDispatcher blockRenderer) {

	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerSpawner(this, player);
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return InventoryHelper.getItemHandlerIterator(upgrades);
	}

	@Override
	protected boolean operate() {
		logic.updateSpawner();
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);
		if (StackHelper.isNull(stack) || !stack.hasTagCompound()) return;
		logic.readFromNBT(stack.getTagCompound());
	}

	public ItemStack createDropStack() {
		ItemStack stack = new ItemStack(XU2Entries.mobSpawner.value);
		stack.setTagCompound(logic.serializeNBT());
		return stack;
	}

	@Override
	public boolean harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, XUBlock xuBlock, IBlockState state) {
		ItemStack itemStack = createDropStack();
		Block.spawnAsEntity(worldIn, pos, itemStack);
		return true;
	}

	@Override
	public Optional<ItemStack> getPickBlock(EntityPlayer player, RayTraceResult target) {
		return Optional.of(createDropStack());
	}

	public void loadFromVanillaSpawner(TileEntityMobSpawner spawner) {
		NBTTagCompound nbt = spawner.writeToNBT(new NBTTagCompound());
		logic.readFromNBT(nbt);
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		NBTTagCompound tag = logic.serializeNBT();
		tag.removeTag("SpawnPotentials");
		packet.writeNBT(tag);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		logic.readFromNBT(packet.readNBT());
	}

	@Override
	public float getPower() {
		return super.getPower();
	}

	public static class ContainerSpawner extends DynamicContainerTile {

		public ContainerSpawner(TileSuperMobSpawner spawner, EntityPlayer player) {
			super(spawner);
			addTitle("Resturbed Spawner");
			crop();
			IWidget widget;
			addWidget(widget = getRSWidget(4, height + 4, spawner.redstone_state, spawner.pulses));
			addWidget(widget = spawner.upgrades.getSpeedUpgradeSlot(4 + widget.getX() + widget.getW(), widget.getY()));
			addWidget(new WidgetTextData(4 + widget.getX() + widget.getW(), widget.getY(), 64) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {

				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					return spawner.logic.getCachedEntity().getName();
				}
			});
			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}

	public abstract static class MyMobSpawnerBaseLogic implements INBTSerializable<NBTTagCompound> {
		private final List<WeightedSpawnerEntity> toSpawnList = Lists.newArrayList();
		private int spawnDelay = 20;
		private WeightedSpawnerEntity randomEntity = new WeightedSpawnerEntity();

		private int minSpawnDelay = 200;
		private int maxSpawnDelay = 800;
		private int spawnCount = 4;
		private Entity cachedEntity;
		private int maxNearbyEntities = 6;

		private int spawnRange = 4;

		public MyMobSpawnerBaseLogic() {
		}

		private String getEntityNameToSpawn() {
			return this.randomEntity.getNbt().getString("id");
		}

		public void updateSpawner() {
			if (this.spawnDelay > 0) {
				this.spawnDelay -= 20;
				return;
			}

			BlockPos blockpos = this.getSpawnerPosition();
			boolean flag = false;
			int i = 0;

			World world = this.getSpawnerWorld();
			Random rand = world.rand;

			while (true) {
				if (i >= this.spawnCount) {
					if (flag) {
						this.resetTimer();
					}
					break;
				}

				NBTTagCompound nbttagcompound = this.randomEntity.getNbt();
				NBTTagList nbttaglist = nbttagcompound.getTagList("Pos", 6);

				int j = nbttaglist.tagCount();

				double d0 = j >= 1 ? nbttaglist.getDoubleAt(0) : (double) blockpos.getX() + (rand.nextDouble() - rand.nextDouble()) * (double) this.spawnRange + 0.5D;
				double d1 = j >= 2 ? nbttaglist.getDoubleAt(1) : (double) (blockpos.getY() + rand.nextInt(3) - 1);
				double d2 = j >= 3 ? nbttaglist.getDoubleAt(2) : (double) blockpos.getZ() + (rand.nextDouble() - rand.nextDouble()) * (double) this.spawnRange + 0.5D;
				Entity entity = AnvilChunkLoader.readWorldEntityPos(nbttagcompound, world, d0, d1, d2, false);
				if (entity == null) {
					return;
				}

				int k = world.getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ(), (double) (blockpos.getX() + 1), (double) (blockpos.getY() + 1), (double) (blockpos.getZ() + 1))).grow((double) this.spawnRange)).size();
				if (k >= this.maxNearbyEntities) {
					this.resetTimer();
					return;
				}

				EntityLiving entityliving = entity instanceof EntityLiving ? (EntityLiving) entity : null;
				if (entityliving == null) continue;

				entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, rand.nextFloat() * 360.0F, 0.0F);
				if (!ForgeEventFactory.canEntitySpawnSpawner(entityliving, world, (float) entity.posX, (float) entity.posY, (float) entity.posZ)) {
					++i;
					continue;
				}

				if (this.randomEntity.getNbt().getSize() == 1 && this.randomEntity.getNbt().hasKey("id", 8) && !ForgeEventFactory.doSpecialSpawn(entityliving, world, (float) entity.posX, (float) entity.posY, (float) entity.posZ)) {
					entityliving.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), null);
				}

				BlockCursedEarth.spawnMobAsCursed(entity);

				world.playEvent(2004, blockpos, 0);
				entityliving.spawnExplosionParticle();

				flag = true;


				++i;
			}

			this.resetTimer();
		}

		private void resetTimer() {
			if (this.maxSpawnDelay <= this.minSpawnDelay) {
				this.spawnDelay = this.minSpawnDelay;
			} else {
				int i = this.maxSpawnDelay - this.minSpawnDelay;
				this.spawnDelay = this.minSpawnDelay + this.getSpawnerWorld().rand.nextInt(i);
			}

			if (!this.toSpawnList.isEmpty()) {
				this.setNextSpawnData(WeightedRandom.getRandomItem(this.getSpawnerWorld().rand, this.toSpawnList));
			}

			this.broadcastEvent(1);
		}

		public void readFromNBT(NBTTagCompound nbt) {
			this.spawnDelay = nbt.getShort("Delay");
			this.toSpawnList.clear();
			if (nbt.hasKey("SpawnPotentials", 9)) {
				NBTTagList nbttagcompound = nbt.getTagList("SpawnPotentials", 10);

				for (int i = 0; i < nbttagcompound.tagCount(); ++i) {
					this.toSpawnList.add(new WeightedSpawnerEntity(nbttagcompound.getCompoundTagAt(i)));
				}
			}

			NBTTagCompound var4 = nbt.getCompoundTag("SpawnData");
			if (!var4.hasKey("id", 8)) {
				var4.setString("id", "Pig");
			}

			loadSpawnData(new WeightedSpawnerEntity(1, var4));
			if (nbt.hasKey("MinSpawnDelay", 99)) {
				this.minSpawnDelay = nbt.getShort("MinSpawnDelay");
				this.maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
				this.spawnCount = nbt.getShort("SpawnCount");
			}

			if (nbt.hasKey("MaxNearbyEntities", 99)) {
				this.maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
			}

			if (nbt.hasKey("SpawnRange", 99)) {
				this.spawnRange = nbt.getShort("SpawnRange");
			}

			if (this.getSpawnerWorld() != null) {
				this.cachedEntity = null;
			}
		}

		protected void loadSpawnData(WeightedSpawnerEntity entry) {
			this.randomEntity = entry;
		}

		public NBTTagCompound writeToNBT(NBTTagCompound p_189530_1_) {
			String s = this.getEntityNameToSpawn();
			if (StringUtils.isNullOrEmpty(s)) {
				return p_189530_1_;
			} else {
				p_189530_1_.setShort("Delay", (short) this.spawnDelay);
				p_189530_1_.setShort("MinSpawnDelay", (short) this.minSpawnDelay);
				p_189530_1_.setShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
				p_189530_1_.setShort("SpawnCount", (short) this.spawnCount);
				p_189530_1_.setShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
				p_189530_1_.setShort("SpawnRange", (short) this.spawnRange);
				p_189530_1_.setTag("SpawnData", this.randomEntity.getNbt().copy());
				NBTTagList nbttaglist = new NBTTagList();
				if (this.toSpawnList.isEmpty()) {
					nbttaglist.appendTag(this.randomEntity.toCompoundTag());
				} else {

					for (WeightedSpawnerEntity weightedspawnerentity : this.toSpawnList) {
						nbttaglist.appendTag(weightedspawnerentity.toCompoundTag());
					}
				}

				p_189530_1_.setTag("SpawnPotentials", nbttaglist);
				return p_189530_1_;
			}
		}


		public Entity getCachedEntity() {
			if (this.cachedEntity == null) {
				this.cachedEntity = AnvilChunkLoader.readWorldEntity(this.randomEntity.getNbt(), this.getSpawnerWorld(), false);
				if (this.randomEntity.getNbt().getSize() == 1 && this.randomEntity.getNbt().hasKey("id", 8) && this.cachedEntity instanceof EntityLiving) {
					((EntityLiving) this.cachedEntity).onInitialSpawn(this.getSpawnerWorld().getDifficultyForLocation(new BlockPos(this.cachedEntity)), null);
				}
			}

			return this.cachedEntity;
		}

		public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_) {
			this.randomEntity = p_184993_1_;
		}

		public abstract void broadcastEvent(int var1);

		public abstract World getSpawnerWorld();

		public abstract BlockPos getSpawnerPosition();

		@Override
		public NBTTagCompound serializeNBT() {
			return writeToNBT(new NBTTagCompound());
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			readFromNBT(nbt);
		}
	}
}
