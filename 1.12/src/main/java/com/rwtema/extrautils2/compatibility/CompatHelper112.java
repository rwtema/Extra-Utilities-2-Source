package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.XU2Entries112;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.machine.MechEnchantmentRecipe;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;

public class CompatHelper112 {

	@SideOnly(Side.CLIENT)
	public static List<String> getTooltip(ItemStack itemStack, EntityPlayer player, boolean advancedItemTooltips) {
		return itemStack.getTooltip(player, advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
	}

	public static Collection<? extends String> getTooltip(ItemStack itemStack, ItemTooltipEvent event) {
		return itemStack.getTooltip(event.getEntityPlayer(), event.getFlags());
	}

	public static List<String> getTooltip(ItemTooltipEvent event) {
		return event.getItemStack().getTooltip(event.getEntityPlayer(), event.getFlags());
	}

	public static <T extends XUBlock> boolean isChunkProviderEnd(Object provider) {
		return provider instanceof ChunkGeneratorEnd;
	}

	public static <T extends IForgeRegistryEntry<T>> void register(T value) {
		GameRegistry.findRegistry(value.getRegistryType()).register(value);
	}

	public static <T> Optional<T> optionalOf(T value) {
		return Optional.of(value);
	}

	public static VillagerRegistry.VillagerProfession getVillagerProfession(String name1, String texture1) {
		return new VillagerRegistry.VillagerProfession(ExtraUtils2.MODID + ":" + name1, ExtraUtils2.MODID + ":textures/villagers/" + texture1 + ".png", "minecraft:textures/entity/zombie_villager/zombie_farmer.png");
	}

	static HashSet<ResourceLocation> registeredRecipes = new HashSet<>();

	public static void addRecipe(IRecipe recipe) {
		if (!registeredRecipes.add(recipe.getRegistryName())) {
			throw  new IllegalArgumentException(recipe.getRegistryName() + " was added twice");
		}
		register(recipe);
	}

	public static void damage(ItemStack itemStackIn, int amount, Random rand) {
		itemStackIn.attemptDamageItem(amount, rand, null);
	}


	public static void drainExperience(EntityPlayer player, int amount, ItemStack recipeOutput) {
		player.onEnchant(recipeOutput, amount);
	}


	public static void loadVersionSpecificEntries() {
		XU2Entries112.init();
	}
}
