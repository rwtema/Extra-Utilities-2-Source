package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.entries.BlockClassEntry;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.BoxModel;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockDecorativeBedrock extends XUBlockStatic {
	public static final PropertyEnumSimple<Type> TYPE = new PropertyEnumSimple<>(Type.class);

	public BlockDecorativeBedrock() {
		super(Material.ROCK);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setSoundType(SoundType.STONE);
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return false;
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addDropProperties(TYPE).build();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		return BoxModel.newStandardBlock(state.getValue(TYPE).texture);
	}

	public enum Type implements IItemStackMaker {
		BEDROCK_BRICKS("bedrock_bricks"),
		BEDROCK_SLABS("bedrock_slab"),
		BEDROCK_COBBLESTONE("bedrock_cobblestone");
		public final String texture;

		Type(String texture) {
			this.texture = texture;
		}

		@Override
		public ItemStack newStack() {
			BlockClassEntry<BlockSimpleDecorative> simpleDecorative = XU2Entries.simpleDecorative;
			return simpleDecorative.newStack(1, simpleDecorative.value.getDefaultState().withProperty(BlockDecorativeBedrock.TYPE, this));
		}
	}
}
