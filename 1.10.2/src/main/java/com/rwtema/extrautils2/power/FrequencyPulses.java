package com.rwtema.extrautils2.power;

import com.rwtema.extrautils2.backend.save.SaveModule;
import gnu.trove.map.hash.TIntLongHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class FrequencyPulses extends SaveModule {
	public static FrequencyPulses INSTANCE = new FrequencyPulses();
	public static TIntLongHashMap pulsesMap = new TIntLongHashMap();

	public static final float COST_PER_TICK = .05F;

	public FrequencyPulses() {
		super("FrequencyPulses");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList pulses = nbt.getTagList("pulses", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < pulses.tagCount(); i++) {
			NBTTagCompound pulse = pulses.getCompoundTagAt(i);
			pulsesMap.put(pulse.getInteger("freq"), pulse.getLong("timeout"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();
		pulsesMap.forEachEntry((a, b) -> {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("freq", a);
			tag.setLong("timeout", b);
			list.appendTag(tag);
			return true;
		});
		nbt.setTag("pulses", list);
	}

	@Override
	public void reset() {
		pulsesMap.clear();
	}
}
