package com.rwtema.extrautils2.items.itemmatching;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface IMatcherMaker extends IMatcher {

	ItemStack getMainStack();

	Collection<ItemStack> getSubItems();

	default Object getCraftingObject() {
		return getMainStack();
	}

	class MatcherMakerOreDic implements IMatcherMaker {
		final List<ItemStack> stacks;
		final List<String> exceptions = new ArrayList<>();

		public MatcherMakerOreDic(String oreName) {
			this(OreDictionary.getOres(oreName));
		}

		public MatcherMakerOreDic(List<ItemStack> stacks) {
			this.stacks = stacks;
		}

		public MatcherMakerOreDic addExceptions(String... strings) {
			Collections.addAll(exceptions, strings);
			return this;
		}

		@Override
		public boolean matchesItemStack(@Nullable ItemStack t) {
			if (StackHelper.isNull(t)) return false;
			boolean b = false;
			for (ItemStack input : stacks) {
				if (OreDictionary.itemMatches( input, t, false)) {
					b = true;
					break;
				}
			}

			if (!b) return false;
			if (!exceptions.isEmpty()) {
				int[] oreIDs = OreDictionary.getOreIDs(t);
				for (String exception : exceptions) {
					int oreID1 = OreDictionary.getOreID(exception);
					for (int oreID : oreIDs) {
						if (oreID == oreID1)
							return false;
					}
				}
			}
			return true;
		}

		@Override
		public ItemStack getMainStack() {
			ItemStack fallbackStack = StackHelper.empty();
			for (ItemStack stack : stacks) {
				fallbackStack = stack;
				if ("minecraft".equals(stack.getItem().getRegistryName().getResourceDomain())) {
					return StackHelper.safeCopy(stack);
				}
			}
			return fallbackStack;
		}

		@Override
		public Collection<ItemStack> getSubItems() {
			ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
			for (ItemStack stack : stacks) {
				Collection<ItemStack> subItems = ItemRef.wrapCrafting(stack).getSubItems();
				builder.addAll(subItems.stream().filter(this::matchesItemStack).collect(Collectors.toList()));
			}
			return builder.build();
		}

		@Override
		public Object getCraftingObject() {
			return stacks;
		}
	}
}
