package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.MutableModel;
import com.rwtema.extrautils2.backend.model.Transforms;
import com.rwtema.extrautils2.keyhandler.ConstantRightClickHandler;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TilePowerHandCrank extends TilePassiveGenerator implements ITickable, ITESRHook, IWorldPowerMultiplier {
	public final double DELTA_OFFSET = Math.PI * 2 / 20;
	public final NBTSerializable.Float TIME = registerNBT("time", new NBTSerializable.Float());
	float renderOffset;

	@Override
	public void update() {
		if (TIME.value > 0) {
			if(world.isRemote){
				renderOffset += TIME.value * DELTA_OFFSET;
			}

			TIME.value -= 0.05;

			if(TIME.value < 0) TIME.value = 0;
		}
	}

	@Override
	public IWorldPowerMultiplier getMultiplier() {
		return this;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			TIME.value = 1;
			ConstantRightClickHandler.setPlayerRightClicking(worldIn, pos);
		} else {
			if (PlayerHelper.isPlayerReal(playerIn)) {
				TIME.value = 1;
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(IBlockAccess world, BlockPos pos, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer, BlockRendererDispatcher blockRenderer) {
		double v = renderOffset  + partialTicks * Math.max(Math.min(TIME.value,0.5F)-0.05 * partialTicks,0)  * DELTA_OFFSET;
		MutableModel model = new MutableModel(Transforms.blockTransforms);

		BoxModel boxes = new BoxModel();
		boxes.addBoxI(6, 6, 6, 6 + 4, 6 + 4, 6 + 4, "redstone_gear");
		boxes.addBoxI(2, 7, 7, 14, 9, 9, "redstone_gear");
		boxes.addBoxI(7, 7, 2, 9, 9, 14, "redstone_gear");
		boxes.addBoxI(1, 7, 1, 15, 9, 15, "redstone_gear").setInvisible(~3);
//		boxes.addBoxI(1, 10, 1, 15, 10, 15, "redstone_gear").setInvisible(~3);

		boxes.addBoxI(1, 6, 7, 15, 10, 9, "redstone_gear").setInvisible(~(4 | 8));
		boxes.addBoxI(7, 6, 1, 9, 10, 15, "redstone_gear").setInvisible(~(16 | 32));

		boxes.loadIntoMutable(model, null);

		model.rotateY(0.5F, 0.5F, (float) v);

		renderBakedModel(world, renderer, blockRenderer, model);
	}

	@Override
	public void preRender(int destroyStage) {

	}

	@Override
	public void postRender(int destroyStage) {

	}

	@Override
	public float multiplier(World world) {
		return Math.min(TIME.value,0.5F) * 30;
	}
}
