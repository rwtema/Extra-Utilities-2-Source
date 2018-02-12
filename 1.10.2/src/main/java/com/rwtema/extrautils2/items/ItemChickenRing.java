package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.power.ClientPower;
import com.rwtema.extrautils2.power.player.IPlayerPowerCreator;
import com.rwtema.extrautils2.power.player.PlayerPower;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemChickenRing extends XUItemFlatMetadata implements IPlayerPowerCreator {
	public ItemChickenRing() {
		super("chickenring", "flyingsquidring");
		setMaxStackSize(1);
		setHasSubtypes(true);
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		return ItemStackHelper.getBaubleProvider("RING");
	}

	@Override
	public boolean shouldOverride(PlayerPower playerPower, EntityPlayer player, ItemStack stack, boolean isSelected) {
		return playerPower instanceof PlayerPowerChicken && stack.getMetadata() == 1;
	}

	@Override
	public PlayerPower createPower(EntityPlayer player, ItemStack params) {
		int metadata = params.getMetadata();
		if (metadata == 1) return new PlayerPowerSquid(player, params);
		return new PlayerPowerChicken(player, params);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		if (stack.getMetadata() == 1) {
			tooltip.add(Lang.translate("Jet propulsion at your fingertips!"));
		} else {
			tooltip.add(Lang.translate("Flight of the majestic beast!"));
		}
		tooltip.add(Lang.translateArgs("Uses %s GP", stack.getMetadata() == 1 ? PlayerPowerSquid.POWER : PlayerPowerChicken.POWER));
		tooltip.add(ClientPower.powerStatusString());
	}


	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getMetadata() == 1) {
			return super.getUnlocalizedName(stack) + ".squid";
		}
		return super.getUnlocalizedName(stack);
	}

	public static class PlayerPowerSquid extends PlayerPowerFlightBase {
		public final static int POWER = 16;
		boolean hasHadBreakTime = false;

		public PlayerPowerSquid(EntityPlayer player, @Nonnull ItemStack stack) {
			super(player, stack);
		}

		@Override
		public int getMaxFlightTime() {
			return 100 * 2;
		}

		@Override
		public float power(EntityPlayer playerMP) {

			return POWER;
		}

		@Override
		public void tickClient() {
			EntityPlayer player = getPlayer();

			if (ClientPower.getClient(XU2Entries.angelRing.value) != null) {
				tickFlight = 0;
				return;
			}

			boolean isJumping = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();

			if (ClientPower.isPowered()) {
				boolean onGround = player.onGround;
				if (onGround && !isJumping) {
					hasHadBreakTime = false;
				}

				if (!onGround && !isJumping) {
					hasHadBreakTime = true;
				}

				if (!onGround && isJumping && tickFlight < getMaxFlightTime()) {
					tickFlight++;
					player.motionY = player.motionY * 0.9 + 0.1;
					NetworkHandler.sendPacketToServer(new PacketFlightData(player));
				}
				if (!isJumping) {
					if (tickFlight > 0) {
						tickFlight--;
					}
				}
			}
		}

		@Override
		public void tick() {
			tickFlight--;
		}

		public boolean isValid(double y, double motion_y) {
			return tickFlight >= (getMaxFlightTime() + 20);
		}
	}


	public static class PlayerPowerChicken extends PlayerPowerFlightBase {
		public final static int POWER = 1;
		boolean hasHadBreakTime = false;

		public PlayerPowerChicken(@Nonnull EntityPlayer player, @Nonnull ItemStack stack) {
			super(player, stack);
		}

		@Override
		public int getMaxFlightTime() {
			return 100;
		}

		@Override
		public boolean isValid(double y, double motion_y) {
			return tickFlight >= (getMaxFlightTime() + 20) && motion_y < 0;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void tickClient() {
			EntityPlayer player = getPlayer();

			if (ClientPower.getClient(XU2Entries.angelRing.value) != null) {
				tickFlight = 0;
				return;
			}

			boolean isJumping = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();

			if (!player.onGround) {

				if (!isJumping) {
					hasHadBreakTime = true;
				}

				if (!hasHadBreakTime) {
					if (tickFlight > 0) {
						tickFlight = MathHelper.clamp(tickFlight - 4, 0, getMaxFlightTime());
					}
				}

				if (ClientPower.isPowered()
						&& isJumping
						&& hasHadBreakTime
						&& player.motionY < 0
						&& tickFlight < getMaxFlightTime()) {
					tickFlight++;
					player.motionY = Math.min(player.motionY + 0.1, player.motionY * 0.5);
					player.fallDistance = 0;
					NetworkHandler.sendPacketToServer(new PacketFlightData(player));
				}

			} else {
				if (!isJumping) {
					if (tickFlight > 0) {
						tickFlight = MathHelper.clamp(tickFlight - 4, 0, getMaxFlightTime());
					}
					hasHadBreakTime = false;
				}
			}
		}

		@Override
		public void tick() {
			if (getPlayer().onGround && tickFlight > 0)
				tickFlight = MathHelper.clamp(tickFlight - 4, 0, getMaxFlightTime());
			ItemAngelRing.updateWingsDisplay(getPlayer().getGameProfile().getName(), 6, false);
		}

		@Override
		public float power(EntityPlayer playerMP) {
			return POWER;
		}

		@Override
		public void onAdd() {
			ItemAngelRing.updateWingsDisplay(getPlayer().getGameProfile().getName(), 6, false);
		}

		@Override
		public void onRemove() {
			ItemAngelRing.updateWingsDisplay(getPlayer().getGameProfile().getName(), 0, true);
		}
	}

}
