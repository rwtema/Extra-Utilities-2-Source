package com.rwtema.extrautils2.compatibility;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.api.recipes.ICustomRecipeMatching;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.crafting.IItemMatcher;
import com.rwtema.extrautils2.utils.datastructures.ConcatList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class XUShapedRecipe extends ShapedOreCompat implements IItemMatcher {
	public BiFunction<ItemStack, InventoryCrafting, ItemStack> finalOutputTransform = null;

	public XUShapedRecipe(ResourceLocation location, Block result, Object... recipe) {
		this(location, new ItemStack(result), recipe);
	}

	public XUShapedRecipe(ResourceLocation location, Item result, Object... recipe) {
		this(location, new ItemStack(result), recipe);
	}

	public XUShapedRecipe(ResourceLocation location, ItemStack result, Object... recipe) {
		super(result, CraftingHelper112.getShapedIngredientRecipes(recipe));
		output = result.copy();

		CraftingHelper112.processRecipe(this, result, recipe);
	}

	public static void handleListInput(List<ItemStack> list, Object in) {
		if (in instanceof ItemStack) {
			list.add(((ItemStack) in).copy());
		} else if (in instanceof Item) {
			list.add(new ItemStack((Item) in));
		} else if (in instanceof Block) {
			list.add(new ItemStack((Block) in, 1, OreDictionary.WILDCARD_VALUE));
		} else if (in instanceof String) {
			list.addAll(OreDictionary.getOres((String) in));
		} else if (in instanceof IItemStackMaker) {
			list.add(((IItemStackMaker) in).newStack());
		} else if (in instanceof IBlockState) {
			IBlockState state = (IBlockState) in;
			Block block = (state).getBlock();
			Item itemDropped = block.getItemDropped(state, null, 0);
			if (itemDropped != StackHelper.nullItem())
				list.add(new ItemStack(itemDropped, 1, block.damageDropped(state)));
		} else if (in instanceof Collection) {
			if (in instanceof List) {
				boolean allStacks = true;
				for (Object o : ((List) in)) {
					if (!(o instanceof ItemStack)) {
						allStacks = false;
						break;
					}
				}

				if (allStacks) {
					list.addAll((List) in);
					return;
				}
			}

			for (Object o : ((Collection) in)) {
				handleListInput(list, o);
			}
		} else {
			String ret = "Invalid shaped ore input: ";
			ret += in;
			throw new RuntimeException(ret);
		}
	}

	public static List<ItemStack> getRecipeStackList(Object in) {
		ConcatList<ItemStack> list = new ConcatList<>();
		handleListInput(list, in);
		List<ItemStack> out;
		if (list.subLists.size() == 1) {
			if (list.size() == 1) {
				ItemStack itemStack = list.modifiableList.get(0);
				out = StackHelper.isNonNull(itemStack) ? ImmutableList.of(itemStack) : ImmutableList.of();
			} else {
				out = list.modifiableList;
			}
		} else {
			if (list.subLists.size() == 2 && list.modifiableList.isEmpty()) {
				out = list.subLists.get(1);
			} else
				out = list;
		}
		return out;
	}

	@Override
	protected boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror) {
		for (int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++) {
			for (int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++) {
				int subX = x - startX;
				int subY = y - startY;
				Object target = null;

				if (subX >= 0 && subY >= 0 && subX < this.width && subY < this.height) {
					if (mirror) {
						target = this.input[this.width - subX - 1 + subY * this.width];
					} else {
						target = this.input[subX + subY * this.width];
					}
				}

				ItemStack slot = inv.getStackInRowAndColumn(x, y);

				if (target instanceof ItemStack) {
					if (!itemsMatch(slot, (ItemStack) target)) {
						return false;
					}
				} else if (target instanceof List) {
					boolean matched = false;

					Iterator<ItemStack> itr = ((List<ItemStack>) target).iterator();
					while (itr.hasNext() && !matched) {
						matched = itemsMatch(slot, itr.next());
					}

					if (!matched) {
						return false;
					}
				} else if (target == null && StackHelper.isNonNull(slot)) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean itemsMatch(ItemStack slot, @Nonnull ItemStack target) {
		Item item = target.getItem();
		if (item instanceof ICustomRecipeMatching)
			return ((ICustomRecipeMatching) item).itemsMatch(slot, target);

		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).block;
			if (block instanceof ICustomRecipeMatching)
				return ((ICustomRecipeMatching) block).itemsMatch(slot, target);
		}

		return OreDictionary.itemMatches(target, slot, false);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		ItemStack result = super.getCraftingResult(var1);
		if (finalOutputTransform != null) {
			return finalOutputTransform.apply(result, var1);
		}
		return result;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setInput(Object[] input) {
		this.input = input;
	}
}