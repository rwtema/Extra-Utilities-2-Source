package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.FontRainbow;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileRainbowGenerator;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockRainbowGenerator extends XUBlockStatic {
	public final static PropertyEnumSimple<State> PROPERTY_STATE = new PropertyEnumSimple<>(State.class);

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addDropProperties(PROPERTY_STATE).build();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		IBlockState state = xuBlockState.getStateFromItemStack(stack);
		if (state.getValue(PROPERTY_STATE) != State.FULL) return;
		tooltip.add(Lang.translate("When all other generators are creating power, taste the unlimited power of the rainbow"));
	}


	@Override
	public BoxModel getModel(IBlockState state) {
		switch (state.getValue(PROPERTY_STATE)) {
			case FULL:
				return BoxModel.newStandardBlock("rainbow_generator");
			case BOTTOM_HALF:
				return new BoxModel(new Box(0, 0, 0, 1, 0.5F, 1).setTextureSides("rainbow_generator", EnumFacing.UP, "rainbow_generator_center"));
			case TOP_HALF:
				return new BoxModel(new Box(0, 0.5F, 0, 1, 1, 1).setTextureSides("rainbow_generator", EnumFacing.DOWN, "rainbow_generator_center"));
		}
		throw new IllegalStateException();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(PROPERTY_STATE) == State.FULL;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileRainbowGenerator();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack) {
		if (xuBlockState.getStateFromItemStack(stack).getValue(PROPERTY_STATE) == State.FULL)
			return FontRainbow.INSTANCE.init(true);
		return null;
	}

	public enum State {
		FULL, BOTTOM_HALF, TOP_HALF
	}
}
