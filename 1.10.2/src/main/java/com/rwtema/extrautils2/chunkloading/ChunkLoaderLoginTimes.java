package com.rwtema.extrautils2.chunkloading;

import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.backend.save.SaveModule;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectLongProcedure;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class ChunkLoaderLoginTimes extends SaveModule {
	private static final long MAX_WAIT_TIME = 1000 * 60 * 60 * 24 * 7;
	public static ChunkLoaderLoginTimes instance;
	static TObjectLongHashMap<GameProfile> loginTimes = new TObjectLongHashMap<>();

	static {
		instance = new ChunkLoaderLoginTimes();
		MinecraftForge.EVENT_BUS.register(instance);
	}

	boolean loaded = false;

	public ChunkLoaderLoginTimes() {
		super("ChunkLoaderData");
	}

	public boolean isValid(GameProfile profile) {
		if (!loaded) return true;
		for (EntityPlayerMP playerMP : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
			if (playerMP.getGameProfile().equals(profile)) {
				loginTimes.put(profile, System.currentTimeMillis());
				return true;
			}
		}

		long l = System.currentTimeMillis() - loginTimes.get(profile);
		return l < MAX_WAIT_TIME;
	}

	@SubscribeEvent
	public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		loginTimes.put(event.player.getGameProfile(), System.currentTimeMillis());
		XUChunkLoaderManager.dirty = true;
		markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		loginTimes.clear();
		NBTTagList loginTimes = nbt.getTagList("LoginTimes", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < loginTimes.tagCount(); i++) {
			NBTTagCompound loginTime = loginTimes.getCompoundTagAt(i);
			GameProfile profile = NBTHelper.profileFromNBT(loginTime);
			if (profile != null) {
				ChunkLoaderLoginTimes.loginTimes.put(profile, loginTime.getLong("LoginTime"));
			}
		}

		loaded = true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		final NBTTagList tagList = new NBTTagList();
		loginTimes.forEachEntry(new TObjectLongProcedure<GameProfile>() {
			@Override
			public boolean execute(GameProfile a, long b) {
				NBTTagCompound t = NBTHelper.proifleToNBT(a);
				t.setLong("LoginTime", b);
				tagList.appendTag(t);
				return true;
			}
		});
		nbt.setTag("LoginTimes", tagList);
	}

	@Override
	public void reset() {
		loginTimes.clear();
	}
}
