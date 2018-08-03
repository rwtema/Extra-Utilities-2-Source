package com.rwtema.extrautils2.achievements;

import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AchievementHelper {
	static final List<AchievementEntry> entryList = new ArrayList<>();

	public static void checkForPotentialAwards(EntityPlayer playerIn, ItemStack stack) {

	}

	public static void addAchievement(String name, String description, @Nonnull IItemStackMaker entry, @Nullable IItemStackMaker parent) {
//		entryList.add(new AchievementEntry(name, description, entry, parent));
	}

	public static void bake() {

	}

	public static class AchievementEntry {
		final String name, description;
		final IItemStackMaker entry, parent;

		public AchievementEntry(String name, String description, IItemStackMaker entry, IItemStackMaker parent) {
			this.name = name;
			this.description = description;
			this.entry = entry;
			this.parent = parent;
		}
	}
}
