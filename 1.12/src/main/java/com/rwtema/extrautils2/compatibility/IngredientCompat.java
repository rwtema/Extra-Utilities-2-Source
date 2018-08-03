package com.rwtema.extrautils2.compatibility;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public abstract class IngredientCompat extends Ingredient implements Predicate<ItemStack>, com.google.common.base.Predicate<ItemStack> {

	protected IngredientCompat(int size) {
		super(size);
	}

	protected IngredientCompat(ItemStack... p_i47503_1_) {
		super(p_i47503_1_);
	}

	public Ingredient toCompat() {
		return this;
	}

	public abstract Object getStandby();

	@Override
	public boolean apply(@Nullable ItemStack p_apply_1_) {
		return test(p_apply_1_);
	}

	public static abstract class Factory<T extends Ingredient> implements IIngredientFactory {
		@Nonnull
		@Override
		public Ingredient parse(JsonContext context, JsonObject json) {
			return parse(new JsonContextCompat(context), json);
		}

		public abstract T parse(JsonContextCompat context, JsonObject json);
	}

	public static class JsonContextCompat {
		final JsonContext context;

		public JsonContextCompat(JsonContext context) {
			this.context = context;
		}

		public String getModId() {
			return context.getModId();
		}

		public String appendModId(String data) {
			return context.appendModId(data);
		}

		@Nullable
		public Ingredient getConstant(String name) {
			return context.getConstant(name);
		}
	}
}
