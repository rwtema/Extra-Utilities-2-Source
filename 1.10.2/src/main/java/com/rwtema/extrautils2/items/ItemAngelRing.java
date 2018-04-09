package com.rwtema.extrautils2.items;


import com.rwtema.extrautils2.RunnableClient;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.power.ClientPower;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.power.player.IPlayerPowerCreator;
import com.rwtema.extrautils2.power.player.PlayerPower;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class ItemAngelRing extends XUItemFlatMetadata implements IPlayerPowerCreator {
	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		return ItemStackHelper.getBaubleProvider("RING");
	}

	public final static String[] textures = new String[]{
			"angelring_base",
			"angelring_feather",
			"angelring_butterfly",
			"angelring_demon",
			"angelring_golden",
			"angelring_bat",
	};
	public final static int POWER = 32;
	public static TObjectIntHashMap<String> serverFlyingPlayers = new TObjectIntHashMap<>(5, 0.5F, -1);
	public static TObjectIntHashMap<String> clientFlyingPlayers = new TObjectIntHashMap<>(5, 0.5F, -1);

	public ItemAngelRing() {
		super(textures);
		setMaxStackSize(1);
		setHasSubtypes(true);
	}

	public static void updateWingsDisplay(String name, int type, boolean selected) {
		if (type == 0) {
			if (serverFlyingPlayers.containsKey(name)) {
				serverFlyingPlayers.remove(name);
				NetworkHandler.sendToAllPlayers(new PacketAngelRingNotifier(name, 0));
			}
		} else {
			if (!serverFlyingPlayers.containsKey(name) || (selected && serverFlyingPlayers.get(name) != type)) {
				serverFlyingPlayers.put(name, type);
				NetworkHandler.sendToAllPlayers(new PacketAngelRingNotifier(name, type));
			}
		}
	}

	@Override
	public void getSubItemsBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < 6; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return 1073741822;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		tooltip.add(Lang.translateArgs("Uses %s GP", POWER));
		tooltip.add(ClientPower.powerStatusString());
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Nonnull
	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack) {
		NBTTagCompound tag = new NBTTagCompound();
		location.writeToNBT(tag);
		tag.setBoolean("Invulnerable", true);
		location.readFromNBT(tag);
		return null;
	}

	@Override
	public PlayerPower createPower(EntityPlayer player, ItemStack params) {
		return new PlayerPowerAngelRing(player, params.getItemDamage());
	}

	public static class PlayerPowerAngelRing extends PlayerPower {
		int type;
		boolean wasFlying = false;

		String name;
		int power;

		public PlayerPowerAngelRing(EntityPlayer referent, int type) {
			super(referent);

			name = referent.getGameProfile().getName();
			this.type = type;
		}


		@Override
		public float power(EntityPlayer playerMP) {
			return power;
		}

		@Override
		public void powerChanged(boolean powered) {
			if (invalid) return;

			EntityPlayerMP entityPlayerMP = getPlayerMP();
			if (entityPlayerMP.isSpectator()) return;
			if (entityPlayerMP.capabilities.isCreativeMode) powered = true;
			if (entityPlayerMP.capabilities.allowFlying != powered) {
				entityPlayerMP.capabilities.allowFlying = powered;
				if (!powered) entityPlayerMP.capabilities.isFlying = false;
				entityPlayerMP.sendPlayerAbilities();
			}
		}

		@Override
		@Nonnull
		public String getName() {
			return XU2Entries.angelRing.value.getUnlocalizedName() + ".name";
		}

		@Override
		public void onAdd() {
			if (!getPlayer().isSpectator()) {
				if (PowerManager.instance.isPowered(getPlayerMP()))
					getPlayer().capabilities.allowFlying = true;
				getPlayer().capabilities.isFlying = false;
				getPlayer().sendPlayerAbilities();
			}

			updateWingsDisplay(name, type, true);
		}

		@Override
		public void onRemove() {
			EntityPlayer player = getPlayer();
			if (!getPlayer().isSpectator()) {
				if (!player.capabilities.isCreativeMode) {
					player.capabilities.allowFlying = false;
					player.capabilities.isFlying = false;
					player.sendPlayerAbilities();
				}
			}

			updateWingsDisplay(name, 0, true);
		}

		@Override
		public void update(boolean selected, ItemStack params) {
			if (invalid) return;

			int t = params.getItemDamage();

			updateWingsDisplay(name, type, selected);

			EntityPlayerMP player = getPlayerMP();

			if (getPlayer().isSpectator()) return;

			if (!player.capabilities.allowFlying) {
				if (PowerManager.instance.isPowered(player)) {
					player.capabilities.allowFlying = true;
					player.sendPlayerAbilities();
				}
			}

			if (player.capabilities.isFlying) {
				wasFlying = true;
				power = 32;
			}

			if (wasFlying) {
				if (player.onGround) {
					wasFlying = false;
					power = 0;
				}
				power = 32;
			} else
				power = 0;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void tickClient() {

		}
	}

	@NetworkHandler.XUPacket
	public static class PacketAngelRingNotifier extends XUPacketServerToClient {
		String username;
		int wingType;

		public PacketAngelRingNotifier() {

		}

		public PacketAngelRingNotifier(String player, int wing) {
			this.username = player;
			this.wingType = wing;
		}

		@Override
		public void writeData() throws Exception {
			writeString(username);
			data.writeByte(wingType);
		}

		@Override
		public void readData(EntityPlayer player) {
			username = readString();
			wingType = data.readByte();
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			return new RunnableClient() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					if (wingType > 0) {
						clientFlyingPlayers.put(username, wingType);
					} else {
						clientFlyingPlayers.remove(username);
					}
				}
			};
		}
	}
}
