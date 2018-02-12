package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.hud.HUDHandler;
import com.rwtema.extrautils2.hud.IHudHandler;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketClientToServer;
import com.rwtema.extrautils2.power.ClientPower;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.power.player.PlayerPower;
import com.rwtema.extrautils2.power.player.PlayerPowerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public abstract class PlayerPowerFlightBase extends PlayerPower {

	static {
		ExtraUtils2.proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				HUDHandler.register(new IHudHandler() {
					@Override
					@SideOnly(Side.CLIENT)
					public void render(GuiIngameForge hud, ScaledResolution resolution, float partialTicks) {
						PlayerPowerFlightBase client = (PlayerPowerFlightBase) ClientPower.getClient(XU2Entries.chickenRing.value);
						if (client == null || client.tickFlight == 0) return;
						EntityPlayerSP player = Minecraft.getMinecraft().player;
						if (player == null || player.isRiding()) return;

						Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);

						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
						GlStateManager.disableBlend();

						float charge = MathHelper.clamp(1 - (((float) client.tickFlight) / client.getMaxFlightTime()), 0, 1);

						final int barWidth = 182;

						int width = resolution.getScaledWidth();
						int height = resolution.getScaledHeight();

						int x = (width / 2) - (barWidth / 2);
						int filled = (int) (charge * (float) (barWidth + 1));
						int top = height - 32 + 3 - 18;

						hud.drawTexturedModalRect(x, top, 0, 84, barWidth, 5);

						if (filled > 0) {
							hud.drawTexturedModalRect(x, top, 0, 89, filled, 5);
						}

						GlStateManager.enableBlend();

						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

					}
				});
			}
		});
	}

	protected ItemStack stack;
	int tickFlight = 0;
	public PlayerPowerFlightBase(EntityPlayer player, @Nonnull ItemStack stack) {
		super(player);
		this.stack = stack;
	}

	public abstract int getMaxFlightTime();

	@Override
	public void powerChanged(boolean powered) {

	}

	@Override
	public String getName() {
		return stack.getDisplayName();
	}

	@Override
	public boolean shouldSustain(ItemStack stack) {
		return stack.isItemEqual(this.stack);
	}

	public void apply(double y, double motion_y) {
		if (tickFlight >= (getMaxFlightTime() + 20)) return;

		tickFlight++;

		EntityPlayer player = getPlayer();

		if (motion_y > 0) {
			player.fallDistance = 0;
		} else if (motion_y > -3.92) {
			double altFallDistance = 49 * motion_y - (194 + 1 / 30.0) * Math.log(1 + motion_y / 3.92);
			player.fallDistance = Math.min(player.fallDistance, (float) altFallDistance);
		}

		player.posY = y;
		player.motionY = motion_y;
	}

	public abstract boolean isValid(double y, double motion_y);

	@NetworkHandler.XUPacket
	public static class PacketFlightData extends XUPacketClientToServer {

		double y;
		double motion_y;
		private EntityPlayer player;

		public PacketFlightData() {
			super();
		}

		public PacketFlightData(EntityPlayer player) {
			y = player.posY;
			motion_y = player.motionY;
		}

		@Override
		public void writeData() throws Exception {
			writeDouble(y);
			writeDouble(motion_y);
		}

		@Override
		public void readData(EntityPlayer player) {
			this.player = player;
			y = readDouble();
			motion_y = readDouble();
		}

		@Override
		public Runnable doStuffServer() {
			return () -> {
				if (!PowerManager.instance.isPowered((EntityPlayerMP) player)) return;

				PlayerPower playerPower = PlayerPowerManager.get(player, XU2Entries.chickenRing.value);
				if (!(playerPower instanceof PlayerPowerFlightBase)) return;

				PlayerPowerFlightBase playerPower1 = (PlayerPowerFlightBase) playerPower;
				playerPower1.apply(this.y, this.motion_y);
			};
		}

	}
}
