package com.rwtema.extrautils2.backend.model;

import com.google.common.base.Throwables;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.utils.blockaccess.BlockAccessEmpty;
import com.rwtema.extrautils2.utils.blockaccess.BlockAccessMimic;
import com.rwtema.extrautils2.utils.PositionPool;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoxMimic extends Box {
	public static ThreadLocal<BlockAccessMimic> blockAccessMimicThreadLocal = new ThreadLocal<BlockAccessMimic>() {
		@Override
		protected BlockAccessMimic initialValue() {
			return new BlockAccessMimic();
		}
	};


	IBlockAccess world;
	BlockPos pos;
	IBlockState mimicState;

	public BoxMimic(IBlockState mimicState) {
		this(BlockAccessEmpty.INSTANCE, PositionPool.MID_HEIGHT, mimicState);
	}

	public BoxMimic(IBlockAccess world, BlockPos pos, IBlockState mimicState) {
		super(0, 0, 0, 1, 1, 1);
		this.world = world;
		this.pos = pos;
		this.mimicState = mimicState;
		ExtraUtils2.proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				layer = BoxMimic.this.mimicState.getBlock().getBlockLayer();
			}
		});
	}

	@Override
	public Box copy() {
		return new BoxMimic(world, pos, mimicState);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> makeQuads(@Nullable EnumFacing side) {
		BlockAccessMimic blockAccessMimic = blockAccessMimicThreadLocal.get();
		try {
			blockAccessMimic.setBase(world);
			blockAccessMimic.state = mimicState;
			blockAccessMimic.myPos = pos;
			IBakedModel modelFromBlockState = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(mimicState);
			IBlockState extendedState = mimicState.getBlock().getExtendedState(mimicState, blockAccessMimic, pos);
			List<BakedQuad> array = modelFromBlockState.getQuads(extendedState, side, 0 );
			blockAccessMimic.setBase(null);
			return array;
		} catch (Throwable throwable) {
			blockAccessMimic.setBase(null);
			throw Throwables.propagate(throwable);
		}
	}
}
