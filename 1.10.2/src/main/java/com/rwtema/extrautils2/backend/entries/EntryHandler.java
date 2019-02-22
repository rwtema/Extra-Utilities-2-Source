package com.rwtema.extrautils2.backend.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rwtema.extrautils2.achievements.AchievementHelper;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.modcompat.ModCompatibility;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class EntryHandler {
	public static List<Entry> entries = Lists.newArrayList();
	public static List<Entry> activeEntries = Lists.newArrayList();
	public static HashMap<String, Entry> entryHashMap = new HashMap<>();
	public static List<IItemStackMaker> stackEntries = Lists.newArrayList();

	public static void loadConfig(Configuration config) {
		HashMap<String, Boolean> configEntries = Maps.newHashMap();
		for (Entry entry : entries) {
			entryHashMap.put(entry.name.toLowerCase(Locale.ENGLISH), entry);
			configEntries.put(entry.name, config.get("Enabled", entry.getConfigLabel(), entry.isEnabledByDefault()).getBoolean());
			entry.loadAdditionalConfig(config);
		}

		for (Entry entry : entries) {
			entry.enabled = configEntries.get(entry.name);
			if (entry.enabled) {
				Set<Entry> dependencies = entry.getDependencies();
				for (Entry dependency : dependencies) {
					if (!configEntries.get(dependency.name)) {
						entry.enabled = false;
						break;
					}
				}
			}

			if (entry.enabled) {
				activeEntries.add(entry);
				if (entry instanceof IItemStackMaker) {
					stackEntries.add(entry);
				}
			}
		}
	}

	public static void preInit() {
		for (Entry activeEntry : activeEntries) {
			activeEntry.preInitLoad();
		}

		for (Entry activeEntry : activeEntries) {
			activeEntry.preInitRegister();
		}

		for (Entry entry : entries) {
			entry.addAchievements();
		}
		for (Entry activeEntry : activeEntries) {
			activeEntry.registerOres();
		}

		for (Entry entry : activeEntries) {
			CraftingHelper.recipeCallback.set(entry.recipes);
			entry.addRecipes();
			CraftingHelper.recipeCallback.set(null);
		}

		AchievementHelper.bake();

	}

	public static void init() {
		for (Entry entry : activeEntries) {
			entry.init();
		}
	}

	public static void postInit() {
		for (Entry entry : activeEntries) {
			entry.postInit();
		}
	}

	public static void loadModEntries(ASMDataTable asmData) {
		for (ASMDataTable.ASMData data : asmData.getAll(ModCompatibility.class.getName())) {
			String mod = (String) data.getAnnotationInfo().get("mod");

			if (Loader.isModLoaded(mod) || ModAPIManager.INSTANCE.hasAPI(mod)) {
				try {
					Class.forName(data.getClassName()).newInstance();
				} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
