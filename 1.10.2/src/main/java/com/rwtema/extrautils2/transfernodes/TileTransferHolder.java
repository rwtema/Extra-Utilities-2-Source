package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TileTransferHolder extends TilePower implements ITickable, IPipe, IDynamicHandler {
	final Grocket[] grockets = new Grocket[6];
	public BoxModel worldModel;
	IBlockState centerPipe = null;

	@Nullable
	public static IBlockState getCenterPipeState(byte b) {
		return b == -1 ? null : BlockTransferPipe.stateBuilder.meta2states[b];
	}

	public void addGrocket(EntityPlayer player, Grocket grocket, EnumFacing facing) {
		loadGrocket(grocket, facing.ordinal());
		if (player != null) {
			grocket.onPlaced(player);
		}
		grocket.validate();
		markForUpdate();
	}

	public void loadGrocket(Grocket grocket, int ordinal) {
		grockets[ordinal] = grocket;
		grocket.holder = this;
		grocket.side = EnumFacing.values()[ordinal];
	}

	@Override
	public void update() {
		if (this.world == null || this.world.isRemote)
			return;

		boolean shouldStay = centerPipe != null;
		for (Grocket grocket : grockets) {
			if (grocket != null) {
				grocket.update();
				shouldStay = true;
			}
		}

		if (!shouldStay) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
//		List<ItemStack> drops = new ArrayList<>();
//		for (Grocket grocket : grockets) {
//			if (grocket != null) drops.addOverrides(grocket.getDrops());
//		}
//		return drops;
		return null;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		RayTraceResult rayTrace = PlayerHelper.rayTrace(playerIn);
		if (rayTrace == null) return false;

		int subHit = rayTrace.subHit;
		if (subHit < 6) {

			if (!ItemStackHelper.holdingWrench(playerIn)) return false;
			if (subHit < 0) subHit = side.ordinal();
			EnumFacing facing = EnumFacing.values()[subHit];
			BlockPos offset = pos.offset(facing);
			if (!TransferHelper.isInputtingPipe(worldIn, offset, facing.getOpposite()) && !TransferHelper.hasValidCapability(worldIn, offset, facing.getOpposite()))
				return false;
			centerPipe = centerPipe.cycleProperty(BlockTransferPipe.SIDE_BLOCKED.get(facing));
			markForUpdate();
			return true;
		}

		subHit = subHit % 6;

		Grocket grocket = grockets[subHit];
		if (grocket == null) return false;

		if (ItemStackHelper.holdingWrench(playerIn) && playerIn.isSneaking()) {

			grocket.invalidate();
			for (ItemStack itemStack : grocket.getDrops()) {
				if (StackHelper.isNonNull(itemStack))
					Block.spawnAsEntity(worldIn, pos, itemStack);
			}
			grockets[subHit] = null;

			for (Grocket g : grockets) {
				if (g != null) {
					markForUpdate();
					return true;
				}
			}

			IBlockState centerPipe = this.centerPipe;
			if (centerPipe == null) centerPipe = Blocks.AIR.getDefaultState();
			worldIn.setBlockState(pos, centerPipe);
			return true;
		}

		return grocket.onActivated(playerIn, side, hitX, hitY, hitZ);
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public Optional<ItemStack> getPickBlock(EntityPlayer player, RayTraceResult target) {
		if (player == null) return null;
		RayTraceResult rayTrace = PlayerHelper.rayTrace(player);
		if (rayTrace == null) return Optional.empty();

		int subHit = rayTrace.subHit;
		if (subHit < 6) {
			return Optional.of(XU2Entries.pipe.newStack());
		}

		subHit = subHit % 6;

		Grocket grocket = grockets[subHit];
		if (grocket == null) return Optional.empty();

		return Optional.ofNullable(grocket.getBaseDrop());
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("CenterPipeState", getCenterPipeIndex());

		for (int i = 0; i < 6; i++) {
			Grocket grocket = grockets[i];
			NBTTagCompound subTag = new NBTTagCompound();
			if (grocket != null) {
				grocket.writeToNBT(subTag);
				nbt.setInteger("Type_" + i, grocket.getType().ordinal());
				nbt.setTag("Grocket_" + i, subTag);
			}

		}
		return nbt;
	}

	public byte getCenterPipeIndex() {
		return this.centerPipe == null ? (byte) -1 : (byte) BlockTransferPipe.stateBuilder.states2meta.get(this.centerPipe);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		byte b = nbt.getByte("CenterPipeState");
		centerPipe = getCenterPipeState(b);

		for (int i = 0; i < 6; i++) {
			String key = "Grocket_" + i;
			if (nbt.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
				String typeKey = "Type_" + i;
				int type = nbt.hasKey(typeKey, Constants.NBT.TAG_INT) ? nbt.getInteger(typeKey) : nbt.getInteger("Type");
				if (type < 0 || type >= GrocketType.values().length) {
					grockets[i] = null;
				} else {
					NBTTagCompound tag = nbt.getCompoundTag(key);
					Grocket grocket = GrocketType.values()[type].create();
					loadGrocket(grocket, i);
					grocket.readFromNBT(tag);
				}

			} else
				grockets[i] = null;

		}
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		packet.writeByte(getCenterPipeIndex());

		int mask = 0;
		for (int i = 0; i < 6; i++) {
			if (grockets[i] != null)
				mask |= (1 << i);
		}

		packet.writeByte(mask);

		for (Grocket grocket : grockets) {
			if (grocket != null) {
				packet.writeInt(grocket.getType().ordinal());
			}
		}
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		if (facing == null) return false;
		Grocket grocket = grockets[facing.ordinal()];
		return grocket != null && grocket.getCapability(capability) != null;
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (facing == null) return null;
		Grocket grocket = grockets[facing.ordinal()];
		return grocket != null ? grocket.getCapability(capability) : null;
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		if (!world.isRemote)
			return;
		super.handleDescriptionPacket(packet);
		centerPipe = getCenterPipeState(packet.readByte());
		int mask = packet.readUnsignedByte();
		for (int i = 0; i < 6; i++) {
			if ((mask & (1 << i)) != 0) {
				loadGrocket(GrocketType.values()[packet.readInt()].create(), i);
			} else
				grockets[i] = null;
		}

		markForUpdate();
	}

	@Override
	public boolean canInput(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		return centerPipe != null;
	}

	@Override
	public boolean canOutput(IBlockAccess world, BlockPos pos, EnumFacing dir, IBuffer buffer) {
		if (centerPipe != null && BlockTransferPipe.isUnblocked(centerPipe, dir)) {
			Grocket grocket = grockets[dir.ordinal()];
			if (grocket != null) {
				if (grocket.blockPipeConnection()) return false;
				if (buffer != null && grocket.shouldBlock(buffer)) {
					return false;
				}
			}

			return TransferHelper.isInputtingPipe(world, pos.offset(dir), dir.getOpposite());
		} else return false;
	}

	@Override
	public boolean canOutputTile(IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (centerPipe == null || !BlockTransferPipe.isUnblocked(centerPipe, side)) {
			return false;
		}

		TileEntity tileEntity = world.getTileEntity(pos.offset(side));

		Grocket grocket = grockets[side.ordinal()];

		if (grocket != null && grocket.blockTileConnection()) return false;

		for (CapGetter<?> cap : CapGetter.caps) {
			if (grocket != null && grocket.hasInterface(tileEntity, cap)) {
				return true;
			}
		}

		if (tileEntity == null) return false;
		for (CapGetter<?> cap : CapGetter.caps) {
			if (cap.hasInterface(tileEntity, side.getOpposite())) return true;
		}
		return false;
	}

	@Override
	public <T> boolean hasCapability(IBlockAccess world, BlockPos pos, EnumFacing side, CapGetter<T> capability) {
		if (centerPipe == null || !BlockTransferPipe.isUnblocked(centerPipe, side)) return false;

		TileEntity tileEntity = world.getTileEntity(pos.offset(side));


		Grocket grocket = grockets[side.ordinal()];
		if (grocket != null) {
			return grocket.hasInterface(tileEntity, capability);
		}

		return tileEntity != null && capability.hasInterface(tileEntity, side.getOpposite());
	}

	@Override
	public <T> T getCapability(IBlockAccess world, BlockPos pos, EnumFacing side, CapGetter<T> capability) {
		if (centerPipe == null || !BlockTransferPipe.isUnblocked(centerPipe, side)) return null;
		TileEntity tileEntity = world.getTileEntity(pos.offset(side));
		if (tileEntity == null) return null;

		Grocket grocket = grockets[side.ordinal()];
		if (grocket != null) {
			if (grocket.blockTileConnection()) return null;
			return grocket.getInterface(tileEntity, capability);
		}

		return capability.getInterface(tileEntity, side.getOpposite());
	}

	@Override
	public boolean shouldTileConnectionShowNozzle(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		Grocket grocket = grockets[facing.ordinal()];
		return grocket == null || grocket.shouldPipeHaveNozzle(facing);
	}

	@Override
	public boolean mayHavePriorities() {
		for (Grocket grocket : grockets) {
			if (grocket != null) {
				if (grocket.getPriority() != GrocketPipeFilter.Priority.NORMAL) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public GrocketPipeFilter.Priority getPriority(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		Grocket grocket = grockets[facing.ordinal()];
		if (grocket == null) return GrocketPipeFilter.Priority.NORMAL;
		return grocket.getPriority();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (Grocket grocket : grockets) {
			if (grocket != null) grocket.invalidate();
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		for (Grocket grocket : grockets) {
			if (grocket != null) grocket.invalidate();
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		for (Grocket grocket : grockets) {
			if (grocket != null) {
				grocket.validate();
			}
		}
	}

	public void openGui(EntityPlayer player, Grocket grocket) {
		openGui(player, grocket.side);
	}

	public void openGui(EntityPlayer player, EnumFacing side) {
		openGUI(player, side.ordinal());
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID < 0 || ID >= 6)
			return null;
		Grocket grocket = grockets[ID];
		if (grocket instanceof IDynamicHandler) {
			return ((IDynamicHandler) grocket).getDynamicContainer(ID, player, world, x, y, z);
		}
		return null;
	}

	@Override
	public float getPower() {
		float t = Float.NaN;
		for (Grocket grocket : grockets) {
			if (grocket != null) {
				float power = grocket.getPower();
				if (!Float.isNaN(power)) {
					if (Float.isNaN(t))
						t = 0;
					t += power;
				}
			}
		}

		return t;
	}
}
