package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.entries.BlockClassEntry;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.BoxModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockSimpleDecorative extends XUBlockStatic {
	public static final PropertyEnumSimple<Type> TYPE = new PropertyEnumSimple<Type>(Type.class);

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addDropProperties(TYPE).build();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		return BoxModel.newStandardBlock(state.getValue(TYPE).texture);
	}


	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) return 0;
		Type value = state.getValue(TYPE);
		return value.getEnchantBonus();
	}

	public enum Type implements IItemStackMaker {
		BLOCK_ENCHANTED("blockEnchantedMetal", "enchanted_block") {
			@Override
			public void addRecipes() {

			}

			@Override
			public float getEnchantBonus() {
				return 5F;
			}
		},
		BLOCK_DEMONIC("blockDemonicMetal", "demon_block") {
			@Override
			public void addRecipes() {

			}
		},
		BLOCK_EVIL("blockEvilMetal", "evil_infused_ingot_block") {
			@Override
			public void addRecipes() {

			}
		};

		public final String oreName;
		public final String texture;

		Type(String oreName, String texture) {
			this.oreName = oreName;
			this.texture = texture;
		}

		public abstract void addRecipes();

		@Override
		public ItemStack newStack() {
			return newStack(1);
		}

		public ItemStack newStack(int num) {
			BlockClassEntry<BlockSimpleDecorative> simpleDecorative = XU2Entries.simpleDecorative;
			return simpleDecorative.newStack(num, simpleDecorative.value.getDefaultState().withProperty(BlockSimpleDecorative.TYPE, this));
		}


		public float getEnchantBonus() {
			return 0;
		}
	}

}
