package com.rwtema.extrautils2.potion;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PotionsHelper {


	public final static List<ItemStack> splash_potions = new ArrayList<>();
	public final static List<ItemStack> regular_potions = new ArrayList<>();
	public final static List<ItemStack> lingering_potions = new ArrayList<>();
	public final static List<ItemStack> potions = new ArrayList<>();

	static {

	}

	public static void addSimplePotionKeys(String key, String name) {
		Lang.translate("effect." + key, "" + name);
		Lang.translate("potion.effect." + key, "Potion of " + name);
		Lang.translate("lingering_potion.effect." + key, "Lingering Potion of " + name);
		Lang.translate("splash_potion.effect." + key, "Splash Potion of " + name);
		Lang.translate("tipped_arrow.effect." + key, "Arrow of " + name);
	}

	public static void addExplicitDerivedRecipes(PotionType type) {
		BrewingRecipeRegistry.addRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), type),
				new ItemStack(Items.GUNPOWDER),
				PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), type)
		);
		BrewingRecipeRegistry.addRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), type),
				new ItemStack(Items.DRAGON_BREATH),
				PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), type)
		);
	}

	public static PotionType newGenericPotion(String s) {
		String key = Lang.stripText(s);
		Lang.translate("potion.effect." + key, s + " Potion");
		Lang.translate("lingering_potion.effect." + key, s + " Lingering Potion");
		Lang.translate("splash_potion.effect." + key, s + " Splash Potion");
		Lang.translate("tipped_arrow.effect." + key, "Tipped Arrow");
		PotionType type = new PotionType();
		type.setRegistryName(new ResourceLocation(ExtraUtils2.MODID, key));
		CompatHelper112.register(type);
		return type;
	}

	public static PotionType registerPotionType(PotionEffect potionEffect) {
		Potion potion = potionEffect.getPotion();
		String name = potion.getName().substring("effect.".length());
		addSimplePotionKeys(name, ((XUPotion) potion).xuName);
		return registerPotionType(potionEffect, name);
	}

	public static void registerPotion(XUPotion potion, String tooltip) {
		Lang.translate(potion.getName(), potion.xuName);
		potion.setRegistryName(new ResourceLocation(ExtraUtils2.MODID, potion.getName()));
		CompatHelper112.register(potion);
		TooltipHandler.tooltips.put(potion, tooltip);
	}

	public static PotionType registerPotionType(PotionEffect potionEffect, String resourcePathIn) {
		PotionType type = new PotionType(potionEffect);
		type.setRegistryName(new ResourceLocation(ExtraUtils2.MODID,
				resourcePathIn
		));
		CompatHelper112.register(type);
		addExplicitDerivedRecipes(type);
		return type;
	}

	public static PotionType getVanillaType(String name) {
		return PotionType.REGISTRY.getObject(new ResourceLocation(name));
	}

	public static PotionType getAwkwardPotionType() {
		return getVanillaType("awkward");
	}

	public static void serverStart() {
		potions.clear();
		regular_potions.clear();
		splash_potions.clear();
		lingering_potions.clear();

		for (ResourceLocation resourceLocation : PotionType.REGISTRY.getKeys()) {
			PotionType type = PotionType.REGISTRY.getObject(resourceLocation);
			regular_potions.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), type));
			splash_potions.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), type));
			lingering_potions.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), type));
		}

		potions.addAll(regular_potions);
		potions.addAll(splash_potions);
		potions.addAll(lingering_potions);

	}

	public static class TooltipHandler {
		static {
			MinecraftForge.EVENT_BUS.register(new TooltipHandler());
		}

		public static HashMap<Potion, String> tooltips = new HashMap<>();

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public void addPotionTooltips(ItemTooltipEvent event) {
			ItemStack stack = event.getItemStack();
			if (!stack.hasTagCompound())
				return;

			List<PotionEffect> list = PotionUtils.getEffectsFromStack(stack);
			if (list.size() == 1) {
				PotionEffect effect = list.get(0);
				String s = tooltips.get(effect.getPotion());
				if (s != null) {
					for (String s1 : s.split("\\n")) {
						event.getToolTip().add(s1);
					}
				}
			}

		}
	}
}
