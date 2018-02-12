package com.rwtema.extrautils2.api.machine;

import com.google.common.collect.Iterators;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class MachineRecipeRegistry implements Iterable<IMachineRecipe> {
	private final IdentityHashMap<IMachineRecipe, ModContainer> toBlameMap = new IdentityHashMap<>();
	private final LinkedList<IMachineRecipe> recipes = new LinkedList<>();

	public void addRecipe(IMachineRecipe recipe) {
		addRecipe(recipe, Loader.instance().activeModContainer());
	}

	public void addRecipe(IMachineRecipe recipe, ModContainer value) {
		recipes.add(recipe);
		toBlameMap.put(recipe, value);
	}

	public void addPriorityRecipe(IMachineRecipe recipe) {
		addPriorityRecipe(recipe, Loader.instance().activeModContainer());
	}

	public void addPriorityRecipe(IMachineRecipe recipe, ModContainer value) {
		recipes.addFirst(recipe);
		toBlameMap.put(recipe, value);
	}

	public boolean removeRecipe(IMachineRecipe recipe) {
		if (recipes.remove(recipe)) {
			toBlameMap.remove(recipe);
			return true;
		}
		return false;
	}

	public ModContainer getOwner(IMachineRecipe recipe) {
		return toBlameMap.get(recipe);
	}

	@Override
	public Iterator<IMachineRecipe> iterator() {
		return Iterators.unmodifiableIterator(recipes.iterator());
	}

	public boolean contains(IMachineRecipe recipe) {
		return toBlameMap.containsKey(recipe);
	}
}
