package com.rwtema.extrautils2.backend.entries;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class Entry<T> implements IItemStackMaker {

	public final String name;
	public final LoadingCache<Integer, IItemStackMaker> metaCache = CacheBuilder.newBuilder().initialCapacity(1).build(new CacheLoader<Integer, IItemStackMaker>() {
		@Override
		public IItemStackMaker load(@Nonnull final Integer key) throws Exception {
			if (key == 0) return Entry.this;

			return new IItemStackMaker() {
				@Override
				public ItemStack newStack() {
					return Entry.this.newStack(1, key);
				}
			};
		}
	});
	public T value;

	public boolean enabled;
	public List<IRecipe> recipes = new ArrayList<>();

	public Entry(String name) {
		this.name = name;
		EntryHandler.entries.add(this);
	}

	public String getDisplayName(int meta) {
		String title;
		ItemStack itemStack = newStack(1, meta);
		if (StackHelper.isNonNull(itemStack)) title = itemStack.getDisplayName();
		else
			title = this.name;
		return title;
	}

	public IItemStackMaker getMetaMaker(int meta) {
		return metaCache.getUnchecked(meta);
	}

	public IItemStackMaker getOreDicMaker() {
		return metaCache.getUnchecked(OreDictionary.WILDCARD_VALUE);
	}

	protected abstract T initValue();

	public void loadAdditionalConfig(Configuration config) {

	}

	public void preInitLoad() {
		value = initValue();
	}

	public void preInitRegister() {

	}

	public void addRecipes() {

	}

	public boolean isActive() {
		return enabled;
	}

	public void registerOres() {

	}

	public void addShapeless(String location, ItemStack stack, Object... recipe) {
		CraftingHelper.addShapeless(location, stack, recipe);
	}

	public void addShaped(String location, ItemStack stack, Object... recipe) {
		CraftingHelper.addShaped(location, stack, recipe);
	}

	public void addShapeless(String location, Item stack, Object... recipe) {
		CraftingHelper.addShapeless(location, stack, recipe);
	}

	public void addShaped(String location, Item stack, Object... recipe) {
		CraftingHelper.addShaped(location, stack, recipe);
	}

	public void addShapeless(String location, Block stack, Object... recipe) {
		CraftingHelper.addShapeless(location, stack, recipe);
	}

	public void addShaped(String location, Block stack, Object... recipe) {
		CraftingHelper.addShaped(location, stack, recipe);
	}

	public void postInit() {

	}

	public final ItemStack newStack() {
		return newStack(1);
	}

	public final ItemStack newStackMeta(int meta) {
		return newStack(1, meta);
	}

	public final ItemStack newStack(int amount) {
		return newStack(amount, 0);
	}

	public ItemStack newStack(int amount, int meta) {
		return StackHelper.empty();
	}

	public String getConfigLabel() {
		return name;
	}

	public void addAchievements() {

	}

	public Set<Entry<?>> getDependencies() {
		return ImmutableSet.of();
	}

	public void init() {

	}

	public boolean isEnabledByDefault() {
		return true;
	}

	public List<ItemStack> getCreativeStacks() {
		ItemStack element = newStack();
		if (StackHelper.isNull(element)) return ImmutableList.of();

		return ImmutableList.copyOf(ExtraUtils2.proxy.getSubItems(element.getItem()));
	}
}
