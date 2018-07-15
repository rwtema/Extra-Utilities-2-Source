package com.rwtema.extrautils2.backend.entries;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.blocks.*;
import com.rwtema.extrautils2.machine.TileGrouper;
import com.rwtema.extrautils2.machine.TileItemWrapper;
import com.rwtema.extrautils2.structure.PatternRecipe;
import net.minecraft.init.Blocks;

public class XU2EntriesDev {

	public static final BlockClassEntry<BlockItemWrapper> itemWrapper = new BlockClassEntry<BlockItemWrapper>(BlockItemWrapper.class, TileItemWrapper.class) {
		@Override
		public void addRecipes() {

		}
	};

	public static final BlockClassEntry<BlockGrouper> grouper = new BlockClassEntry<BlockGrouper>(BlockGrouper.class, TileGrouper.class) {
		@Override
		public void addRecipes() {

		}
	};

	public static BlockClassEntry<BlockOneWay> oneWay = new BlockClassEntry<BlockOneWay>(BlockOneWay.class) {

	};

	static {
		PatternRecipe.register(new String[][]{
						{
								"sssss",
								"sgggs",
								"sgggs",
								"sgggs",
								"sssss",
						},
						{
								"sssss",
								"s   s",
								"s   s",
								"s   s",
								"sgggs",
						},
						{
								"sssss",
								"s   s",
								"s   s",
								"s   s",
								"sgggs",
						},
						{
								"sssss",
								"sfffs",
								"sfffs",
								"sfffs",
								"sgggs",
						},
						{
								"sssss",
								"snnns",
								"snnns",
								"snnns",
								"sssss",
						}

				}, ImmutableMap.<Character, Object>builder()
						.put('s', Blocks.STONE.getDefaultState())
						.put(' ', Blocks.AIR.getDefaultState())
						.put('n', Blocks.NETHERRACK.getDefaultState())
						.put('g', Blocks.GLASS.getDefaultState())
						.put('f', Blocks.FIRE.getDefaultState())
						.build()
		);
	}

	public static void init() {

	}

}
