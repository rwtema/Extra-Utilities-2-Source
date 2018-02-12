package com.rwtema.extrautils2.keyhandler;

import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConstantRightClickHandler {
	static BlockPos dest;
	static IBlockState state;
	static boolean rightClickReleased;

	static {
		MinecraftForge.EVENT_BUS.register(new ConstantRightClickHandler());
	}

	public static void setPlayerRightClicking(World world, BlockPos pos) {
		if (!world.isRemote) return;
		state = world.getBlockState(pos);
		dest = pos;
		rightClickReleased = false;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void constantRightClick(TickEvent.ClientTickEvent event) {
		if (dest == null || state == null) return;
		if (event.phase != TickEvent.Phase.START)
			return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP thePlayer = mc.player;
		if (mc.world == null || thePlayer == null
				|| thePlayer.isSneaking()
				|| thePlayer.isHandActive()
				|| mc.gameSettings.keyBindAttack.isKeyDown()
//				|| mc.gameSettings.keyBindUseItem.isKeyDown()
				|| mc.gameSettings.keyBindPickBlock.isKeyDown()
				|| mc.playerController.getIsHittingBlock()
				|| mc.objectMouseOver == null
				|| mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK
				|| !dest.equals(mc.objectMouseOver.getBlockPos())
				|| mc.world.getBlockState(dest) != state
				|| thePlayer.isRowingBoat()
				) {
			dest = null;
			state = null;
			return;
		}

		if (mc.rightClickDelayTimer > 1)
			return;

		mc.rightClickDelayTimer = 5;

		BlockPos blockpos = mc.objectMouseOver.getBlockPos();

		if (mc.world.isAirBlock(blockpos)) return;

		for (EnumHand enumhand : EnumHand.values()) {
			ItemStack itemstack = thePlayer.getHeldItem(enumhand);

			int i = StackHelper.isNonNull(itemstack) ? StackHelper.getStacksize(itemstack) : 0;
			EnumActionResult result = CompatClientHelper.processRightClick(mc, thePlayer, blockpos, enumhand, itemstack);

			if (result == EnumActionResult.SUCCESS) {
				thePlayer.swingArm(enumhand);

				if (StackHelper.isNonNull(itemstack)) {
					if (StackHelper.isEmpty(itemstack)) {
						thePlayer.setHeldItem(enumhand, StackHelper.empty());
					} else if (StackHelper.getStacksize(itemstack) != i || mc.playerController.isInCreativeMode()) {
						mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
					}
				}

				return;
			}

		}
	}

}
