package com.rwtema.extrautils2.items;

import buildcraft.api.tools.IToolWrench;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.tools.IWrench;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.ILeftClickHandler;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.interblock.FlatTransferNodeHandler;
import crazypants.enderio.api.tool.ITool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@Optional.InterfaceList({
		@Optional.Interface(iface = "crazypants.enderio.api.tool.ITool", modid = "EnderIOAPI|Tools"),
		@Optional.Interface(iface = "crazypants.enderio.api.tool.IHideFacades", modid = "EnderIOAPI|Tools"),
		@Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|tools"),
		@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "cofhapi|item")
})
public class ItemWrench extends XUItemFlatMetadata implements IWrench, ITool, IToolWrench, ILeftClickHandler {
	public ICapabilityProvider wrenchProvider = new ICapabilityProvider() {

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return getCapability(capability, facing) != null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == IWrench.CAPABILITY)
				return (T) ItemWrench.this;
			return null;
		}
	};

	public ItemWrench() {
		super("pipe_wrench");
		setMaxStackSize(1);
	}

	@Override
	public boolean renderAsTool() {
		return true;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return wrenchProvider;
	}

	@Override
	public boolean shouldHideFacades(ItemStack stack, EntityPlayer player) {
		return false;
	}

	@Override
	public boolean canUse(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {
		return true;
	}


	@Override
	public void used(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {

	}

	@Override
	public boolean canWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {

	}

	@Override
	public boolean leftClick(World world, EntityPlayer player, ItemStack stack) {
		return XU2Entries.flatTransferNode.isActive() && XU2Entries.flatTransferNode.value.leftClick(world, player, stack);
	}

	@Override
	public EnumActionResult onItemUseFirstBase(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (XU2Entries.flatTransferNode.isActive()) {
			FlatTransferNodeHandler.FlatTransferNode node = ItemFlatTransferNode.getCurrentFlatTransferNode(player);
			if (node != null) {
				if (world.isRemote) {
					ExtraUtils2.proxy.run(new ClientRunnable() {
						@Override
						@SideOnly(Side.CLIENT)
						public void run() {
							NetHandlerPlayClient connection = Minecraft.getMinecraft().getConnection();
							if (connection != null)
								connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, side, hand, hitX, hitY, hitZ));
						}
					});
				} else {
					if (player.isSneaking()) {
						XU2Entries.flatTransferNode.value.leftClick(world, player, stack);
					} else {
						player.openGui(ExtraUtils2.instance, FlatTransferNodeHandler.guiIdSides[node.side.ordinal()], world, node.pos.getX(), node.pos.getY(), node.pos.getZ());
					}
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUseFirstBase(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
	}


//	@Override
//	public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {
//		return true;
//	}
//
//	@Override
//	public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {
//		return true;
//	}
//
//	@Override
//	public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
//
//	}
//
//	@Override
//	public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {
//
//	}
}
