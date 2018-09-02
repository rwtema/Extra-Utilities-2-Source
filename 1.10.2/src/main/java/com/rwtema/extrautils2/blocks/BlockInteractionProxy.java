package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileInteractionProxy;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockInteractionProxy extends XUBlockStatic {
	public BlockInteractionProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = new BoxModel();
		model.addBoxI(2, 2, 2, 14, 14, 14);
		model.addBoxI(0, 2, 0, 16, 2, 16);
		model.addBoxI(0, 14, 0, 16, 14, 16);
		model.addBoxI(2, 0, 0, 2, 16, 16);
		model.addBoxI(14, 0, 0, 14, 16, 16);
		model.addBoxI(0, 0, 2, 16, 16, 2);
		model.addBoxI(0, 0, 14, 16, 16, 14);
		model.setTexture("ender_pearl_construct");
		model.setLayer(BlockRenderLayer.CUTOUT);
		return model;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		tooltip.add(Lang.translate("Acts as a portal to a remote block location."));
		tooltip.add(Lang.translate("Use the GUI to specify a range of blocks from which the remote block is chosen."));
		tooltip.add(Lang.translate("The remote block changes once per second."));
		tooltip.add(Lang.translate("'Mechanical Users', 'Mechanical Miners' and 'Scanners' interact with the remote block."));
		tooltip.add(Lang.translate("In addition, items/fluids/energy can travel through the portal"));
		tooltip.add(Lang.translate("Requires GP, proportional to the distance covered by the portal."));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileInteractionProxy();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public  void onSelect(DrawBlockHighlightEvent event){
		RayTraceResult movingObjectPositionIn = event.getTarget();
		if ( movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK)
		{
			BlockPos blockpos = movingObjectPositionIn.getBlockPos();
			EntityPlayer player = event.getPlayer();
			IBlockState iblockstate = player.world.getBlockState(blockpos);
			if(iblockstate.getBlock() == this){
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(2.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				TileEntity tileEntity = player.world.getTileEntity(blockpos);
				if(!(tileEntity instanceof TileInteractionProxy))return;

				TileInteractionProxy proxy = (TileInteractionProxy) tileEntity;

				float partialTicks = event.getPartialTicks();
				double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
					double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
					double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

				AxisAlignedBB union = new AxisAlignedBB(proxy.targetA).union(new AxisAlignedBB(proxy.targetB));
				RenderGlobal.drawSelectionBoundingBox(
							union.grow(0.0020000000949949026D).offset(blockpos).offset(-d3, -d4, -d5),
							140.0F/255F, 244F/255F, 226F/255F, 0.4F);

				union=new AxisAlignedBB(proxy.currentPos);
				RenderGlobal.drawSelectionBoundingBox(
						union.grow(0.0020000000949949026D).offset(blockpos).offset(-d3, -d4, -d5),
						140.0F/255F, 244F/255F, 226F/255F, 0.4F);

				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
			}


		}
	}
}
