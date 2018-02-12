package com.rwtema.extrautils2.power.player;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.utils.datastructures.ID;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class PlayerPowerManager {
	static HashMap<Pair<ID<EntityPlayer>, IPlayerPowerCreator>, PlayerPower> powerServer = new HashMap<>();



	public static void clear() {
		powerServer.clear();
	}

	@Nullable
	public static <T extends Item & IPlayerPowerCreator> PlayerPower get(EntityPlayer player, T item) {
		return powerServer.get(Pair.<ID<EntityPlayer>, IPlayerPowerCreator>of(new ID<>(player), item));
	}


	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) return;

		HashSet<EntityPlayer> players = new HashSet<>(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers());
		HashSet<PlayerPower> loadedPowers = new HashSet<>();
		HashSet<PlayerPower> addedPowers = new HashSet<>();

		for (EntityPlayer player : players) {

			for (ItemStack stack : PlayerHelper.getAllPlayerItems(player)) {
				if (StackHelper.isNonNull(stack) && stack.getItem() instanceof IPlayerPowerCreator) {
					IPlayerPowerCreator creator = (IPlayerPowerCreator) stack.getItem();

					Pair<ID<EntityPlayer>, IPlayerPowerCreator> key = Pair.of(new ID<>(player), creator);
					PlayerPower playerPower = powerServer.get(key);
					if (playerPower == null || creator.shouldOverride(playerPower, player, stack, stack == player.inventory.getCurrentItem())) {
						if (playerPower != null) {
							playerPower.onRemove();
						}
						playerPower = creator.createPower(player, stack);
						if (playerPower == null) continue;
						powerServer.put(key, playerPower);
						addedPowers.add(playerPower);
					} else if (!playerPower.shouldSustain(stack)) {
						continue;
					}

					playerPower.update(player.inventory.getCurrentItem() == stack, stack);
					playerPower.cooldown = 20;
					loadedPowers.add(playerPower);
				}
			}
		}

		for (Iterator<Map.Entry<Pair<ID<EntityPlayer>, IPlayerPowerCreator>, PlayerPower>> iterator = powerServer.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<Pair<ID<EntityPlayer>, IPlayerPowerCreator>, PlayerPower> entry = iterator.next();
			EntityPlayer player = entry.getKey().getLeft().object;
			PlayerPower power = entry.getValue();
			boolean playerIsLoaded = !players.contains(player);
			boolean incorrectDimension = player.world.provider.getDimension() != power.dimension;
			boolean expired = !loadedPowers.contains(power);
			if (playerIsLoaded || incorrectDimension || expired) {
				removePlayer(power);
				iterator.remove();
			} else if (!addedPowers.contains(power))
				power.tick();
		}

		for (PlayerPower playerPower : addedPowers) {
			playerPower.onAdd();
			PowerManager.instance.addPowerHandler(playerPower);
		}
	}

	public void removePlayer(PlayerPower power) {
		power.invalid = true;
		power.onRemove();
		PowerManager.instance.removePowerHandler(power);
	}
}
