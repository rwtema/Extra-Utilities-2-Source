package com.rwtema.extrautils2.structure;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.blockaccess.CompatBlockAccess;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PatternRecipe {
	public static ArrayList<PatternRecipe> recipeList = new ArrayList<>();
	public final List<BlockPos> toRender;
	public final HashMap<BlockPos, IBlockState> posMap;
	public final Set<ItemRef> items;
	public final IBlockAccess blockAccess;
	public BlockPos min;
	public BlockPos max;
	public BlockPos mid;
	public IBlockState[][][] states;
	public List<ItemStack> stacks = new ArrayList<>();

	public PatternRecipe(String[][] stateStrings, Map<Character, Object> mapKey, List<ItemStack> stacks) {
		int len_y = stateStrings.length;
		int len_x = stateStrings[0].length;
		int len_z = stateStrings[0][0].length();
		min = null;
		max = null;
		states = new IBlockState[len_y][len_x][len_z];
		items = new HashSet<>();
		toRender = new ArrayList<>();
		posMap = new HashMap<>();
		for (int y = 0; y < stateStrings.length; y++) {
			Validate.isTrue(stateStrings[y].length == len_x);
			String[] stateY = stateStrings[y];
			for (int x = 0; x < stateY.length; x++) {
				String s = stateY[x];
				Validate.isTrue(s.length() == len_z);
				char[] chars = s.toCharArray();
				for (int z = 0; z < chars.length; z++) {
					Object o = mapKey.get(chars[z]);
					if (o == null) continue;
					IBlockState state;
					if (o instanceof IBlockState)
						state = (IBlockState) o;
					else if (o instanceof Block) {
						state = ((Block) o).getDefaultState();
					} else
						throw new RuntimeException("Object not recognized" + o);

					BlockPos pos = new BlockPos(x, 128 + y, z);
					posMap.put(pos, state);

					if (state.getBlock() != Blocks.AIR && state.getBlock() != Blocks.BARRIER) {
						if (min == null) {
							min = pos;
							max = pos;
						} else {
							min = new BlockPos(
									Math.min(min.getX(), pos.getX()),
									Math.min(min.getY(), pos.getY()),
									Math.min(min.getZ(), pos.getZ()));
							max = new BlockPos(
									Math.max(max.getX(), pos.getX()),
									Math.max(max.getY(), pos.getY()),
									Math.max(max.getZ(), pos.getZ()));
						}

						toRender.add(pos);

						Block block = state.getBlock();
						Item itemDropped = Item.getItemFromBlock(block);
						if (itemDropped != StackHelper.nullItem()) {
							items.add(ItemRef.wrap(new ItemStack(itemDropped, 1, block.damageDropped(state))));
						}
					}
				}
			}
		}

		mid = min.add(max);
		mid = new BlockPos(mid.getX() / 2, mid.getY() / 2, mid.getZ() / 2);

		this.stacks = stacks;

		blockAccess = new CompatBlockAccess() {
			@Nullable
			@Override
			public TileEntity getTileEntity(@Nonnull BlockPos pos) {
				return null;
			}

			@Override
			public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
				int j = 15;

				if (j < lightValue) {
					j = lightValue;
				}

				return 15 << 20 | j << 4;
			}

			@Nonnull
			@Override
			public IBlockState getBlockState(@Nonnull BlockPos pos) {
				return posMap.getOrDefault(pos, BlockStates.AIR);
			}

			@Override
			public boolean isAirBlock(@Nonnull BlockPos pos) {
				IBlockState blockState = getBlockState(pos);
				return blockState == BlockStates.AIR || blockState.getBlock().isAir(blockState, this, pos);
			}

			@Nonnull
			@Override
			public Biome getBiome(@Nonnull BlockPos pos) {
				return Biomes.PLAINS;
			}

			@Override
			public boolean extendedLevelsInChunkCache() {
				return false;
			}

			@Override
			public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
				return 0;
			}

			@Nonnull
			@Override
			public WorldType getWorldType() {
				return WorldType.FLAT;
			}

			@Override
			public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
				return getBlockState(pos).isSideSolid(this, pos, side);
			}
		};
	}

	public static void register(String[][] stateStrings, Map<Character, Object> mapKey, ItemStack... stacks) {
		PatternRecipe patternRecipe = new PatternRecipe(stateStrings, mapKey, Lists.newArrayList(stacks));
		recipeList.add(patternRecipe);

//		if(ExtraUtils2.deobf_folder) {
////			PatternDiscrimination patternDiscrimination = new PatternDiscrimination(patternRecipe);
////			LogHelper.info(patternDiscrimination.list.stream().map(Objects::toString).collect(Collectors.joining(" ")));
//
//			PatternDiscrimination2 pd2 = new PatternDiscrimination2(patternRecipe);
//
//		}
	}
}
