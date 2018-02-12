package com.rwtema.extrautils2.items.itemmatching;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.api.recipes.ICustomRecipeMatching;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class Matchers {


	public static Predicate<ItemStack> createMatcher(Object object, boolean crafting) {
		if (object == null) return ItemRef.NULL;
		if (object instanceof ItemStack) {
			return matchItemStackIS((ItemStack) object, crafting);
		}

		if (object instanceof IMatcher) return ((IMatcher) object);

		if (object instanceof Predicate) {
			return (Predicate<ItemStack>) object;
		}
		if (object instanceof Block) {
			return matchItemStackIS(new ItemStack((Block) object), crafting);
		}

		if (object instanceof Item) {
			return matchItemStackIS(new ItemStack((Item) object), crafting);
		}

		if (object instanceof IItemStackMaker) {
			return matchItemStackIS(((IItemStackMaker) object).newStack(), crafting);
		}

		if (object instanceof String) {
			List<ItemStack> ores = OreDictionary.getOres(((String) object));
			ImmutableList.Builder<IMatcher> builder = ImmutableList.builder();
			for (ItemStack ore : ores) {
				builder.add(matchItemStackIS(ore, crafting));
			}
			return new MultiMatcher(builder.build());
		}

		if (object instanceof List) {
			List list = (List) object;
			if (list.size() == 1) return createMatcher(list.get(0), crafting);
			ImmutableList.Builder<Predicate<ItemStack>> builder = ImmutableList.builder();
			for (Object o : list) {
				builder.add(createMatcher(o, crafting));
			}
			return new MultiMatcher(builder.build());
		}

		throw new IllegalArgumentException("Unable to process " + object);
	}

	private static IMatcher matchItemStackIS(ItemStack stack, boolean crafting) {
		if (crafting)
			return matchCrafting(stack);
		else
			return ItemRef.wrap(stack);
	}

	public static IMatcher matchCrafting(final ItemStack stack) {
		if (StackHelper.isNull(stack)) return ItemRef.NULL;
		Item item = stack.getItem();
		if (item == StackHelper.nullItem()) return ItemRef.NULL;
		if (item instanceof ICustomRecipeMatching) {
			final ICustomRecipeMatching matching = (ICustomRecipeMatching) item;
			return new IMatcher() {
				@Override
				public boolean matchesItemStack(@Nullable ItemStack t) {
					return matching.itemsMatch(t, stack);
				}
			};
		}

		return new IMatcher() {
			@Override
			public boolean matchesItemStack(@Nullable ItemStack t) {
				return t != null && OreDictionary.itemMatches(stack, t, false);
			}
		};

	}
}
