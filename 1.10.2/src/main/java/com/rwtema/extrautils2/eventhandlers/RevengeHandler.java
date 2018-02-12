package com.rwtema.extrautils2.eventhandlers;

import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RevengeHandler {
	public static void init() {
		MinecraftForge.EVENT_BUS.register(new RevengeHandler());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDeath(LivingDeathEvent event) {
		if (event.isCanceled()) return;
		Entity victim = event.getEntity();
		World worldObj = victim.world;
		if (worldObj == null || worldObj.isRemote) return;

		if (!worldObj.getGameRules().getBoolean("showDeathMessages")) return;

		Entity attacker = event.getSource().getTrueSource();
		if (!(attacker instanceof EntityLivingBase)) {
			return;
		}

		boolean attackerIsPlayer = attacker instanceof EntityPlayerMP;
		boolean victimIsPlayer = victim instanceof EntityPlayerMP;

		if (victimIsPlayer || attackerIsPlayer) {
			if (!attackerIsPlayer) { // killed by mob
				NBTTagCompound killList = NBTHelper.getOrInitTagCompound(attacker.getEntityData(), "entityKillList");
				EntityPlayerMP victimPlayer = (EntityPlayerMP) victim;
				String tagKey = getTagKey(victimPlayer);
				int kills = killList.getInteger(tagKey);
				kills++;
				if (kills == 4) {
					sendToPlayersFriends(victimPlayer, getDominationMessage(attacker, victim));
				}
				killList.setInteger(tagKey, kills);
			} else if (!victimIsPlayer) { // killed by player
				NBTTagCompound entityData = victim.getEntityData();
				if (!entityData.hasKey("entityKillList", Constants.NBT.TAG_COMPOUND)) return;
				NBTTagCompound killList = entityData.getCompoundTag("entityKillList");
				EntityPlayerMP attackerPlayer = (EntityPlayerMP) attacker;
				String tagKey = getTagKey(attackerPlayer);
				int kills = killList.getInteger(tagKey);
				if (kills > 0) {
					sendToPlayersFriends(attackerPlayer, getRevengeMessage(attacker, victim, kills >= 4));
				}
				killList.setInteger(tagKey, 0);
			} else {
				EntityPlayerMP victimPlayer = (EntityPlayerMP) victim;
				NBTTagCompound victimPersistentTag = NBTHelper.getPersistentTag(victimPlayer);
				if (victimPersistentTag.hasKey("playerKillList", Constants.NBT.TAG_COMPOUND)) {
					NBTTagCompound killList = victimPersistentTag.getCompoundTag("playerKillList");
					String tagKey = getTagKey((EntityPlayerMP) attacker);
					int kills = killList.getInteger(tagKey);
					if (kills >= 4) {
						sendToPlayersFriends(victimPlayer, getRevengeMessage(attacker, victim, true));
					}
					killList.setInteger(tagKey, 0);
				}


				NBTTagCompound killList = NBTHelper.getOrInitTagCompound(NBTHelper.getPersistentTag((EntityPlayerMP) attacker), "playerKillList");
				String tagKey = getTagKey((EntityPlayerMP) victim);
				int kills = killList.getInteger(tagKey);
				kills++;
				if (kills == 4) {
					sendToPlayersFriends(victimPlayer, getDominationMessage(attacker, victim));
				}
				killList.setInteger(tagKey, kills);
			}
		}
	}


	private void sendToPlayersFriends(final EntityPlayerMP playerMP, final ITextComponent chatComponent) {
		Team team = playerMP.getTeam();
		if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {

			if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
				playerMP.mcServer.getPlayerList().sendMessageToAllTeamMembers(playerMP, chatComponent);
			} else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
				playerMP.mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(playerMP, chatComponent);
			}
		} else {
			sendToAllPlayers(playerMP.mcServer, chatComponent);
		}
	}

	private void sendToAllPlayers(final MinecraftServer mcServer, final ITextComponent chatComponent) {
		mcServer.getPlayerList().sendMessage(chatComponent);
	}


	private ITextComponent getRevengeMessage(Entity attacker, Entity victim, boolean endDomination) {
		if (endDomination) {
			return Lang.chat("%s got revenge on nemesis %s", attacker.getDisplayName(), victim.getDisplayName());
		} else
			return Lang.chat("%s got revenge on %s", attacker.getDisplayName(), victim.getDisplayName());
	}

	private ITextComponent getDominationMessage(Entity attacker, Entity victim) {
		return Lang.chat("%s is dominating %s", attacker.getDisplayName(), victim.getDisplayName());
	}


	public String getTagKey(EntityPlayerMP player) {
		return player.getName();
	}
}
