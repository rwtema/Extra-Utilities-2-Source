package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.blocks.BlockCompressed;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class BlockCompressedEntry extends BlockEntry<BlockCompressed> {
	private final IBlockState blockState;
	private final String texture;
	private final int max;

	public BlockCompressedEntry(Block block, String texture, int max) {
		this(block.getDefaultState(), texture, max);
	}

	public BlockCompressedEntry(IBlockState blockState, String texture, int max) {
		super("Compressed" + StringHelper.capFirst(texture, true));
		this.blockState = blockState;
		this.texture = texture;
		this.max = max;
	}

	@Override
	public BlockCompressed initValue() {
		return new BlockCompressed(blockState, texture, max);
	}

	@Override
	public void addRecipes() {
		ItemStack base = new ItemStack(blockState.getBlock());
		CraftingHelper.addShaped("compressed_" + texture, newStack(1, 0), "BBB", "BBB", "BBB", 'B', base);
		CraftingHelper.addShapeless("compressed_" + texture + "_uncompress", new ItemStack(blockState.getBlock(), 9), newStack(1, 0));
		for (int i = 0; i < (max - 1); i++) {
			CraftingHelper.addShaped("compressed_" + texture + "_" + (i + 1), newStack(1, i + 1), "BBB", "BBB", "BBB", 'B', newStack(1, i));
			CraftingHelper.addShapeless("compressed_" + texture + "_" + (i + 1) + "_uncompress", newStack(9, i), newStack(1, i + 1));
		}
	}

	@Override
	public void registerOres() {
		for (int i = 0; i < max; i++) {
			OreDictionary.registerOre("compressed" + (i + 1) + "x" + StringHelper.capFirst(texture, true), newStack(1, i));
		}
	}
}
