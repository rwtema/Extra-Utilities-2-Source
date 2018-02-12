package com.rwtema.extrautils2.backend.save;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.chunkloading.ChunkLoaderLoginTimes;
import com.rwtema.extrautils2.compatibility.WorldSavedDataCompat;
import com.rwtema.extrautils2.power.FrequencyPulses;
import com.rwtema.extrautils2.power.PowerSettings;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import java.util.List;

public class SaveManager extends WorldSavedDataCompat {

	public static final String SAVE_DATA_NAME = "XU2SaveData";
	public static SaveManager manager;
	public static List<SaveModule> saveModules = Lists.newArrayList(
			PowerSettings.instance,
			ChunkLoaderLoginTimes.instance,
			FrequencyPulses.INSTANCE
	);

	public SaveManager(String name) {
		super(name);
	}

	public static void init() {
		WorldServer worldServer = DimensionManager.getWorld(0);
		manager = (SaveManager) worldServer.loadData(SaveManager.class, SAVE_DATA_NAME);
		if (manager == null) {
			for (SaveModule saveModule : saveModules) {
				saveModule.reset();
			}
			manager = new SaveManager(SAVE_DATA_NAME);
			worldServer.setData(SAVE_DATA_NAME, manager);
			manager.markDirty();
		}
	}


	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbt) {
		LogHelper.oneTimeInfo("Begin Loading Saves");
		for (SaveModule saveModule : saveModules) {
			saveModule.reset();
			if (nbt.hasKey(saveModule.name, 10)) {
				try {
					saveModule.readFromNBT(nbt.getCompoundTag(saveModule.name));
				} catch (Exception e) {
					LogHelper.oneTimeInfo("Error Loading Data");
					e.printStackTrace();
					saveModule.reset();
				}
			}
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
		for (SaveModule saveModule : saveModules) {
			NBTTagCompound tag = new NBTTagCompound();
			saveModule.writeToNBT(tag);
			nbt.setTag(saveModule.name, tag);
		}
		return nbt;
	}
}
