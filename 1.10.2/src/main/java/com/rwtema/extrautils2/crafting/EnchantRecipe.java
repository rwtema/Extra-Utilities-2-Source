package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import javax.annotation.Nullable;
import java.util.List;

public class EnchantRecipe extends PlayerSpecificCrafting {
	private final int enchantLevel;
	@Nullable
	private final int[] dimensions;
	private List<List<ItemStack>> inputs;

	public EnchantRecipe(ResourceLocation location, IRecipe recipe, int enchantLevel, List<List<ItemStack>> inputs, @Nullable int[] dimensions) {
		super(location, recipe);
		this.enchantLevel = enchantLevel;
		this.inputs = inputs;
		this.dimensions = dimensions;
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}

	@Override
	protected void updatePlayer(EntityPlayerMP foundPlayer) {
		foundPlayer.connection.sendPacket(new SPacketSetExperience(foundPlayer.experience, foundPlayer.experienceTotal, foundPlayer.experienceLevel));
	}

	@Override
	protected boolean isValidForCrafting(EntityPlayer foundPlayer) {
		return foundPlayer.experienceLevel >= enchantLevel;
	}

	@Override
	public String info() {
		return Lang.translateArgs("%s XP", enchantLevel);
	}

	@Override
	protected void finishedCrafting(EntityPlayer player, ItemStack recipeOutput) {
		CompatHelper112.drainExperience(player, enchantLevel, recipeOutput);
	}

	@Override
	protected void addTooltip(ItemTooltipEvent event, ItemStack itemStack) {
		if (enchantLevel == 1) {
			ItemStackHelper.addInfoWidth(event.getToolTip(), itemStack, Lang.translate("Requires 1 enchantment level to craft"));
		} else
			ItemStackHelper.addInfoWidth(event.getToolTip(), itemStack, Lang.translateArgs("Requires %s enchantment levels to craft", enchantLevel));
	}

	@Override
	public List<List<ItemStack>> getInputList() {
		return inputs;
	}

	@Nullable
	@Override
	public int[] getDimensions() {
		return dimensions;
	}
}
