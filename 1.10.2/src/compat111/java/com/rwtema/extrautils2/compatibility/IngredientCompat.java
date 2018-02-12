package com.rwtema.extrautils2.compatibility;


import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public abstract class IngredientCompat implements Predicate<ItemStack>, com.google.common.base.Predicate<ItemStack> {

	private List<ItemStack> stacks;

	public IngredientCompat(ItemStack... stacks) {

		this.stacks = Lists.newArrayList(stacks);
	}

	public Object toCompat(){
		return getStandby();
	}

	public Object getStandby(){
		return stacks;
	}
	@Override
	public boolean apply(@Nullable ItemStack p_apply_1_) {
		return test(p_apply_1_);
	}

	public static abstract class Factory<T> {
		public Factory() {
			throw new IllegalStateException("This must never be called on 1.10.2");
		}

		public abstract T parse(JsonContextCompat context, JsonObject json);
	}

	public class JsonContextCompat {
		public String getModId() {
			throw new IllegalStateException();
		}

		public String appendModId(String data) {
			throw new IllegalStateException();
		}

		@Nullable
		public Object getConstant(String name) {
			throw new IllegalStateException();
		}
	}
}
