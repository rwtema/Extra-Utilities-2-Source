package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.gui.GuiEditTileScreen;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.network.XUPacketClientToServer;
import com.rwtema.extrautils2.utils.helpers.SideHelper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class TileScreen extends TilePower implements IGuiHandler {
	public static final Pattern illegalPatternControlCode = Pattern.compile("[^0-9A-Za-z]");

	@Nonnull
	public String id = "";

	@Override
	public void onPowerChanged() {
		markForUpdate();
	}

	@Override
	public float getPower() {
		return 1;
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setString("image_id", id);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		id = compound.getString("image_id");
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		packet.writeString(id);
		packet.writeInt(frequency);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		id = packet.readString();
		frequency = packet.readInt();
		markForUpdate();
	}

	@Override
	public boolean canRenderBreaking() {
		return false;
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return super.shouldRenderInPass(pass);
	}

	public boolean canJoinWith(BlockPos pos) {
		TileEntity tileEntity = getWorld().getTileEntity(pos);
		if (!(tileEntity instanceof TileScreen)) return false;
		TileScreen te = (TileScreen) tileEntity;
		return getBlockState() == te.getBlockState() && id.equals(te.id) && (world.isRemote && frequency == te.frequency);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			openGUI(playerIn);
		return true;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GuiEditTileScreen(this);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);
		if (world.isRemote) return;
		if (placer instanceof EntityPlayerMP && ExtraUtils2.proxy.isAltSneaking((EntityPlayerMP) placer)) return;
		EnumFacing side = state.getValue(XUBlockStateCreator.ROTATION_HORIZONTAL);

		TileScreen toCopy = null;

		for (EnumFacing enumFacing : SideHelper.perp_sides[side.ordinal()]) {
			TileEntity te = worldIn.getTileEntity(pos.offset(enumFacing));
			if (!(te instanceof TileScreen)) continue;
			TileScreen otherScreen = (TileScreen) te;
			if (otherScreen.getBlockState() == state && otherScreen.frequency == frequency && !StringUtils.isNullOrEmpty(otherScreen.id)) {
				if (toCopy != null) {
					if (toCopy.id.equals(otherScreen.id)) continue;
					toCopy = null;
					break;
				}
				toCopy = otherScreen;
			}
		}
		if (toCopy != null) {
			id = toCopy.id;
			markForUpdate();
		}

	}

	@NetworkHandler.XUPacket
	public static class PacketEditScreen extends XUPacketClientToServer {
		private String id;
		private BlockPos pos;
		private EntityPlayer player;

		public PacketEditScreen(){

		}

		public PacketEditScreen(BlockPos pos, String id) {
			this.pos = pos;
			this.id = id;
		}

		@Override
		public void writeData() throws Exception {
			writeString(id);
			writeBlockPos(pos);
			if (illegalPatternControlCode.matcher(id).find()) {
				throw new RuntimeException("Illegal ID");
			}
		}

		@Override
		public void readData(EntityPlayer player) {
			this.player = player;
			id = readString();
			pos = readBlockPos();
			if (illegalPatternControlCode.matcher(id).find()) {
				throw new RuntimeException("Illegal ID");
			}
		}

		@Override
		public Runnable doStuffServer() {
			return new Runnable() {
				@Override
				public void run() {
					if ("error".equals(id))
						throw new RuntimeException("Artificial error");

					World worldObj1 = player.world;
					TileEntity t = worldObj1.getTileEntity(pos);
					if (!(t instanceof TileScreen)) return;
					TileScreen screen = (TileScreen) t;
					if (!screen.isValidPlayer(player)) return;

					String oldID = screen.id;
					screen.id = id;
					screen.markDirty();
					screen.markForUpdate();

					ArrayList<TileScreen> screens = new ArrayList<>();
					LinkedHashSet<BlockPos> checkedPositions = new LinkedHashSet<>();
					LinkedList<BlockPos> toCheck = new LinkedList<>();
					checkedPositions.add(pos);
					EnumFacing[] perp_sides = SideHelper.perp_sides[screen.getBlockState().getValue(XUBlockStateCreator.ROTATION_HORIZONTAL).ordinal()];
					for (EnumFacing perp_side : perp_sides) {
						toCheck.add(pos.offset(perp_side));
					}
					BlockPos next;
					while ((next = toCheck.poll()) != null && checkedPositions.size() < 100) {
						checkedPositions.add(next);
						TileEntity tileEntity = worldObj1.getTileEntity(next);
						if (!(tileEntity instanceof TileScreen)) continue;
						TileScreen te = (TileScreen) tileEntity;
						if (te.frequency != screen.frequency || te.getBlockState() != screen.getBlockState()) continue;
						if (!oldID.equals(te.id)) continue;

						screens.add(te);
						for (EnumFacing side : perp_sides) {
							BlockPos offset = next.offset(side);
							if (!checkedPositions.contains(offset)) {
								toCheck.offer(offset);
							}
						}
					}

					for (TileScreen tileScreen : screens) {
						tileScreen.id = PacketEditScreen.this.id;
						tileScreen.markDirty();
						tileScreen.markForUpdate();
					}
				}
			};
		}
	}


}
