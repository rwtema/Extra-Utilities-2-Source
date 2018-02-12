package com.rwtema.extrautils2.compatibility;

import com.google.common.base.Optional;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.machine.MechEnchantmentRecipe;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class CompatHelper112 {
	public static List<String> getTooltip(ItemStack itemStack, EntityPlayer player, boolean advancedItemTooltips) {
		return itemStack.getTooltip(player, advancedItemTooltips);
	}

	public static Collection<? extends String> getTooltip(ItemStack itemStack, ItemTooltipEvent event) {
		return itemStack.getTooltip(event.getEntityPlayer(), event.isShowAdvancedItemTooltips());
	}

	public static <T extends XUBlock> boolean isChunkProviderEnd(Object provider) {
		return provider instanceof ChunkProviderEnd;
	}

	public static <T extends IForgeRegistryEntry<T>> void register(T value) {
		GameRegistry.register(value);
	}

	@SuppressWarnings("Guava")
	public static <T> Optional<T> optionalOf(T identity) {
		return Optional.of(identity);
	}

	public static VillagerRegistry.VillagerProfession getVillagerProfession(String name1, String texture1) {
		return new VillagerRegistry.VillagerProfession(ExtraUtils2.MODID + ":" + name1, ExtraUtils2.MODID + ":textures/villagers/" + texture1 + ".png");
	}

	public static void damage(ItemStack itemStackIn, int amount, Random rand) {
		itemStackIn.attemptDamageItem(amount, rand);
	}

	public static List<String> getTooltip(ItemTooltipEvent event) {
		return event.getItemStack().getTooltip(event.getEntityPlayer(), event.isShowAdvancedItemTooltips());
	}

	public static void addRecipe(IRecipe recipe) {
		GameRegistry.addRecipe(recipe);
	}

	public static void drainExperience(EntityPlayer player, int amount, ItemStack result) {
		player.func_71013_b(amount);
	}


}
