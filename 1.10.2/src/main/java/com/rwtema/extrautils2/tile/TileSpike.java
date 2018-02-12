package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.tile.tesr.ITESREnchantment;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class TileSpike extends XUTile implements ITESREnchantment<TileSpike> {
	TObjectIntHashMap<Enchantment> enchantments = new TObjectIntHashMap<>();

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		enchantments.clear();
		NBTTagList list = compound.getTagList("enchants", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound compoundTagAt = list.getCompoundTagAt(i);
			Enchantment enchant = Enchantment.REGISTRY.getObject(new ResourceLocation(compoundTagAt.getString("enchant")));
			if (enchant != null) {
				enchantments.put(enchant, compoundTagAt.getInteger("level"));
			}
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
		NBTTagList list = new NBTTagList();
		enchantments.forEachEntry((enchantment, level) -> {
			ResourceLocation nameForObject = Enchantment.REGISTRY.getNameForObject(enchantment);
			if (nameForObject != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString("enchant", nameForObject.toString());
				tag.setInteger("level", level);
				list.appendTag(tag);
			}
			return true;
		});
		nbtTagCompound.setTag("enchants", list);
		return nbtTagCompound;
	}
}
