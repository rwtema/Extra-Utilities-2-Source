package com.rwtema.extrautils2.power;

import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.entries.BlockEntry;
import com.rwtema.extrautils2.backend.entries.Entry;
import com.rwtema.extrautils2.backend.entries.EntryHandler;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.hud.HUDHandler;
import com.rwtema.extrautils2.hud.IHudHandler;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.power.player.IPlayerPowerCreator;
import com.rwtema.extrautils2.power.player.PlayerPower;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientPower implements IHudHandler {
	private final static HashSet<Item> powerItems = new HashSet<>();
	public static HashMap<IPlayerPowerCreator, PlayerPower> powerClient = new HashMap<>();
	static float powerCreated;
	static float powerDrained;
	public static final PowerManager.IPowerReport POWER_REPORT = new PowerManager.IPowerReport() {
		@Override
		public boolean isPowered() {
			return ClientPower.isPowered();
		}

		@Override
		public float getPowerDrain() {
			return ClientPower.powerDrained;
		}

		@Override
		public float getPowerCreated() {
			return ClientPower.powerCreated;
		}
	};
	static BlockPos currentPosition;
	static float currentPositionEnergy;
	static float currentPositionEfficiency = 1;

	static {
		for (Entry entry : EntryHandler.entries) {
			if (entry instanceof BlockEntry && entry.enabled) {
				for (Class clazz : (((BlockEntry) entry).teClazzes)) {
					if (IPower.class.isAssignableFrom(clazz)) {
						powerItems.add(((XUBlock) entry.value).itemBlock);
					}
				}
			}
		}

		ClientPower handler = new ClientPower();
		MinecraftForge.EVENT_BUS.register(handler);
		HUDHandler.register(handler);
	}

	public static void init() {

	}

	public static String powerStatusString() {
		return Lang.translatePrefix("Grid Power:") + " " + StringHelper.niceFormat(powerDrained) + " / " + StringHelper.niceFormat(powerCreated);
	}

	public static boolean isPowered() {
		return powerDrained <= powerCreated;
	}

	public static boolean hasNoPower() {
		return powerDrained == 0 && powerCreated == 0;
	}

	@Nullable
	public static <T extends Item & IPlayerPowerCreator> PlayerPower getClient(T item) {
		return powerClient.get(item);
	}


	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void playerPowerManagerTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) return;

		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null) return;
		for (ItemStack stack : PlayerHelper.getAllPlayerItems(player)) {
			if (StackHelper.isNonNull(stack) && stack.getItem() instanceof IPlayerPowerCreator) {
				IPlayerPowerCreator creator = (IPlayerPowerCreator) stack.getItem();
				PlayerPower playerPower = ClientPower.powerClient.get(creator);
				if (playerPower == null || creator.shouldOverride(playerPower, player, stack, stack == player.inventory.getCurrentItem())) {
					playerPower = creator.createPower(player, stack);
					ClientPower.powerClient.put(creator, playerPower);
				} else if (!playerPower.shouldSustain(stack)) {
					continue;
				}

				playerPower.cooldown = 2;
			}
		}

		for (Iterator<Map.Entry<IPlayerPowerCreator, PlayerPower>> iterator = ClientPower.powerClient.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<IPlayerPowerCreator, PlayerPower> entry = iterator.next();
			PlayerPower playerPower = entry.getValue();
			playerPower.cooldown--;
			if (playerPower.cooldown < 0) {
				iterator.remove();
			} else {
				playerPower.tickClient();
			}
		}
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) return;
		RayTraceResult mop = Minecraft.getMinecraft().objectMouseOver;

		if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
			currentPosition = null;
			currentPositionEnergy = Float.NaN;
		} else {
			if (currentPosition != null && !currentPosition.equals(mop.getBlockPos())) {
				currentPositionEnergy = Float.NaN;
			}

			currentPosition = mop.getBlockPos();
			WorldClient theWorld = Minecraft.getMinecraft().world;
			if (theWorld != null) {
				TileEntity tileEntity = theWorld.getTileEntity(mop.getBlockPos());
				if (tileEntity instanceof IPower) {
					NetworkHandler.sendPacketToServer(new PacketPowerInfo(currentPositionEnergy, currentPositionEfficiency, currentPosition));
				} else {
					currentPositionEnergy = Float.NaN;
					currentPositionEfficiency = 1;
					currentPosition = null;
				}
			}
		}
	}

	@Override
	public void render(GuiIngameForge hud, ScaledResolution resolution, float partialTicks) {
		boolean flag = currentPosition != null;
		EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;

		if (!flag && thePlayer != null && StackHelper.isNonNull(thePlayer.getHeldItemMainhand()) && powerItems.contains(thePlayer.getHeldItemMainhand().getItem())) {
			flag = true;
		}

		if (!flag) return;

		int y = resolution.getScaledHeight() * 7 / 10;
		hud.drawCenteredString(hud.getFontRenderer(), powerStatusString(), resolution.getScaledWidth() / 2, y, 0xffffffff);
		if (!Float.isNaN(currentPositionEnergy)) {
			y += hud.getFontRenderer().FONT_HEIGHT + 1;
			String text;
			if (currentPositionEnergy == 0) {
				text = Lang.translatePrefix("No Power Used/Generated");
				hud.drawCenteredString(hud.getFontRenderer(), text, resolution.getScaledWidth() / 2, y, 0xffffffff);
			} else {
				if (currentPositionEnergy < 0) {
					text = Lang.translatePrefix("Power Generating:") + " " + StringHelper.niceFormat(-currentPositionEnergy);
				} else {
					text = Lang.translatePrefix("Power Drain:") + " " + StringHelper.niceFormat(currentPositionEnergy);
				}

				hud.drawCenteredString(hud.getFontRenderer(), text, resolution.getScaledWidth() / 2, y, 0xffffffff);
				if (currentPositionEfficiency != 1) {
					y += hud.getFontRenderer().FONT_HEIGHT + 1;
					text = Lang.translate("Effective Rate:") + " " + StringHelper.niceFormat(Math.abs(-currentPositionEnergy * currentPositionEfficiency))
							+ " (" + StringHelper.formatPercent(1 - currentPositionEfficiency) + " " + Lang.translate("Power Loss") + ")";
					hud.drawCenteredString(hud.getFontRenderer(), text, resolution.getScaledWidth() / 2, y, 0xffffffff);
				}
			}

		}
	}
}