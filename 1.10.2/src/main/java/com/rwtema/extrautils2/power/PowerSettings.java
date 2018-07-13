package com.rwtema.extrautils2.power;

import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.backend.save.SaveModule;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class PowerSettings extends SaveModule {
	public static PowerSettings instance = new PowerSettings();

	public PowerSettings() {
		super("PowerAlliances");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		LogHelper.oneTimeInfo("Loaded Power Settings");
		for (NBTTagCompound freqEntry : NBTHelper.iterateNBTTagList(nbt.getTagList("PlayerFreq", 10))) {
			int a = freqEntry.getInteger("Freq");
			GameProfile profile = NBTHelper.profileFromNBT(freqEntry.getCompoundTag("Profile"));
			if (a != 0 && profile != null) {
				int[] b = freqEntry.getIntArray("Allies");
				PowerManager.instance.frequncies.put(a, profile);
				if (b != null && b.length > 0) {
					PowerManager.instance.alliances.put(a, new TIntHashSet(b));
				}
			}
		}

		try {
			PowerManager.instance.unloadedChunkManager.deserializeNBT(nbt.getTagList("UnloadedChunkData", Constants.NBT.TAG_COMPOUND));
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			PowerManager.instance.unloadedChunkManager.freqs.clear();
		}

		PowerManager.instance.lockedFrequencies.addAll(nbt.getIntArray("LockedFreqs"));

		PowerManager.instance.reassignValues();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		final NBTTagList playerFreq = new NBTTagList();
		PowerManager.instance.frequncies.forEachEntry((a, b) -> {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("Freq", a);
			tag.setTag("Profile", NBTHelper.proifleToNBT(b));

			TIntHashSet set = PowerManager.instance.alliances.get(a);
			if (set != null && !set.isEmpty())
				tag.setIntArray("Allies", set.toArray());
			playerFreq.appendTag(tag);
			return true;
		});

		nbt.setTag("PlayerFreq", playerFreq);

		try {
			nbt.setTag("UnloadedChunkData", PowerManager.instance.unloadedChunkManager.serializeNBT());
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			nbt.removeTag("UnloadedChunkData");
		}

		nbt.setIntArray("LockedFreqs", PowerManager.instance.lockedFrequencies.toArray());
	}

	@Override
	public void reset() {
		PowerManager.instance.frequncies.clear();
		PowerManager.instance.alliances.clear();
		PowerManager.instance.unloadedChunkManager.freqs.clear();
		PowerManager.instance.lockedFrequencies.clear();
	}
}
