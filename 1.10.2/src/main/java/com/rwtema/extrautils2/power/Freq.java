package com.rwtema.extrautils2.power;

import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import java.util.Random;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ObjectUtils;


public class Freq {
	private static final String XU_TAG = "XU2";
	private static final String XU_FREQ_TAG = "Frequency";
	public static Freq INSTANCE;
	private static Random rand = new Random();

	static {
		INSTANCE = new Freq();
	}

	public static int getBasePlayerFreq(EntityPlayerMP player) {

		NBTTagCompound entityData = NBTHelper.getPersistentTag(player);
		NBTTagCompound xu2Tag = NBTHelper.getOrInitTagCompound(entityData, XU_TAG);
		int i = xu2Tag.getInteger(XU_FREQ_TAG);
		PowerSettings.instance.markDirty();

		GameProfile gameProfile = player.getGameProfile();
		if (i != 0) {
			PowerManager.instance.frequncies.putIfAbsent(i, gameProfile);
			return i;
		}
		synchronized (PowerManager.MUTEX) {
			UUID uuid = EntityPlayer.getUUID(gameProfile);

			rand.setSeed(uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits() ^ ObjectUtils.hashCode(gameProfile.getName()));

			do {
				i = rand.nextInt();
			} while (i == 0 || PowerManager.instance.frequncies.containsKey(i));
			xu2Tag.setInteger(XU_FREQ_TAG, i);
			PowerManager.instance.frequncies.put(i, gameProfile);
			PowerManager.instance.reassignValues();
		}
		return i;
	}

	@SubscribeEvent
	public void load(PlayerEvent.LoadFromFile event) {
		getBasePlayerFreq((EntityPlayerMP) event.getEntityPlayer());
	}

}
