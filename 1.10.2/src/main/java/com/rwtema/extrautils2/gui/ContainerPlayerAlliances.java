package com.rwtema.extrautils2.gui;

import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.utils.Lang;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class ContainerPlayerAlliances extends DynamicContainer {
	public static IDynamicHandler handler = (ID1, player, world, x, y, z) -> new ContainerPlayerAlliances(player);

	public static int ID;
	final WidgetScrollBar scrollBar;
	final int NUM_VERTICAL_ENTRIES = 10;
	final int BUTTON_HEIGHT = 20;
	final int BUTTON_WIDTH = 180;
	public ArrayList<PlayerEntry> entryList = new ArrayList<>();

	public ContainerPlayerAlliances(final EntityPlayer player) {
		EntityPlayerMP playerMP = player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null;
		final int freq = playerMP != null ? Freq.getBasePlayerFreq(playerMP) : 0;


		final PowerManager manager = PowerManager.instance;

		if (playerMP != null && ExtraUtils2.deobf_folder) {
			synchronized (PowerManager.MUTEX) {
				manager.frequncies.put(343, new GameProfile(UUID.randomUUID(), "Frank"));
				manager.frequncies.put(432132, new GameProfile(UUID.randomUUID(), "Tim"));
				manager.frequncies.put(432133, new GameProfile(UUID.randomUUID(), "Tom"));
				manager.frequncies.put(65396, new GameProfile(UUID.randomUUID(), "Katey"));
				manager.frequncies.put(322, new GameProfile(UUID.randomUUID(), "Sandy"));
				manager.frequncies.put(544, new GameProfile(UUID.randomUUID(), "Oscar"));
				manager.frequncies.put(5441, new GameProfile(UUID.randomUUID(), "Roger"));
				manager.frequncies.put(3223, new GameProfile(UUID.randomUUID(), "Olgoth the Destroyer"));

				TIntHashSet set = new TIntHashSet();
				set.add(freq);
				set.add(322);
				manager.alliances.put(343, set);

				set = new TIntHashSet();
				set.add(343);
				manager.alliances.put(322, set);

				manager.reassignValues();
			}
		}

		addWidget(new WidgetRawData() {
			@Override
			public void addToDescription(final XUPacketBuffer packet) {
				final ArrayList<PlayerEntry> entryList = new ArrayList<>();
				synchronized (PowerManager.MUTEX) {
					manager.frequncies.forEachEntry(new TIntObjectProcedure<GameProfile>() {
						@Override
						public boolean execute(int a, GameProfile b) {
							if (b.equals(player.getGameProfile()))
								return true;

							entryList.add(new PlayerEntry(b, a,
									manager.doesAWishToAllyWithB(a, freq),
									manager.doesAWishToAllyWithB(freq, a),
									manager.sameTeam(a, freq)
							));
							return true;
						}
					});
				}
				packet.writeShort(entryList.size());
				for (PlayerEntry entry : entryList) {
					packet.writeProfile(entry.profile);
					packet.writeInt(entry.freq);
					packet.writeBoolean(entry.themAlliedWithYou);
					packet.writeBoolean(entry.youAlliedWithThem);
					packet.writeBoolean(entry.sameTeam);
				}
			}

			@Override
			public void handleDescriptionPacket(XUPacketBuffer packet) {
				ArrayList<PlayerEntry> list = new ArrayList<>();
				list.clear();
				int n = packet.readUnsignedShort();
				for (int i = 0; i < n; i++) {
					GameProfile profile = packet.readProfile();
					int freq = packet.readInt();
					boolean them = packet.readBoolean();
					boolean you = packet.readBoolean();
					boolean sameTeam = packet.readBoolean();
					list.add(new PlayerEntry(profile, freq, them, you, sameTeam));
				}
				Collections.sort(list);
				entryList = list;
				scrollBar.setValues(0, Math.max(0, n - NUM_VERTICAL_ENTRIES + 1));
				scrollBar.reScroll();
			}
		});

		addWidget(scrollBar = new WidgetScrollBar(BUTTON_WIDTH + 8, 17, NUM_VERTICAL_ENTRIES * BUTTON_HEIGHT, 0, 0));

		addWidget(new WidgetText(4, 4, Lang.translate("Select players to share power with."), BUTTON_WIDTH + 4 + 14));

		for (int i = 0; i < NUM_VERTICAL_ENTRIES; i++) {
			final int k = i;
			addWidget(new WidgetClickMCButtonText("", 4, 17 + i * BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT) {
				@Override
				public XUPacketBuffer getPacketToSend(int mouseButton) {
					XUPacketBuffer pkt = new XUPacketBuffer();
					PlayerEntry ref = getEntry();
					if (ref == null) return null;
					pkt.writeInt(ref.freq);
					return pkt;
				}

				@Override
				public void receiveClientPacket(XUPacketBuffer buffer) {
					int otherFreq = buffer.readInt();
					if (!manager.frequncies.containsKey(otherFreq))
						return;

					synchronized (PowerManager.MUTEX) {
						TIntHashSet set = manager.alliances.get(freq);
						if (set == null) {
							set = new TIntHashSet();
							manager.alliances.put(freq, set);
						}
						if (set.contains(otherFreq))
							set.remove(otherFreq);
						else
							set.add(otherFreq);
						manager.reassignValues();
					}

				}

				public PlayerEntry getEntry() {
					int j = k + scrollBar.scrollValue;
					if (j < 0) {
						visible = false;
						return null;
					}
					ArrayList<PlayerEntry> list = ContainerPlayerAlliances.this.entryList;
					if (j < list.size()) {
						visible = true;
						return list.get(j);
					}
					visible = false;
					return null;
				}

				@Override
				@SideOnly(Side.CLIENT)
				public void renderButtonText(DynamicGui gui, FontRenderer fontrenderer, int xPosition, int yPosition, int color) {
					PlayerEntry entry = getEntry();
					if (entry == null) return;
					StringBuilder builder = new StringBuilder();
					if (entry.youAlliedWithThem) {
						builder.append("[");
						builder.append(entry.getName());
						builder.append("]");
					} else
						builder.append(entry.getName());
					builder.append(" ");
					builder.append(entry.sameTeam ? Lang.translate("[Sharing]") : "");
					String text = builder.toString();
					gui.drawCenteredString(fontrenderer, text, xPosition + getW() / 2, yPosition + (getH() - 8) / 2, color);
				}
			});
		}
		crop();

		addWidget(new WidgetClickMCButtonChoices<Boolean>(4, height) {
					@Override
					protected void onSelectedServer(Boolean marker) {
						if (marker) {
							PowerManager.instance.lockedFrequencies.add(freq);
						} else {
							PowerManager.instance.lockedFrequencies.remove(freq);
						}
					}

					@Override
					public Boolean getSelectedValue() {
						return PowerManager.instance.lockedFrequencies.contains(freq);
					}
				}
						.addChoice(Boolean.FALSE, Lang.translate("Secure Inventories"), ItemIngredients.Type.SYMBOL_CROSS.newStack(), Lang.translate("All players can currently access your inventories"))
						.addChoice(Boolean.TRUE, Lang.translate("Secure Inventories"), ItemIngredients.Type.SYMBOL_TICK.newStack(), Lang.translate("Only players on your team can access your inventories"))
		);

		crop();
		validate();
	}

	public static void init() {
		ID = GuiHandler.register("Alliances", handler);
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
		return true;
	}

	public static class PlayerEntry implements Comparable<PlayerEntry> {
		private final int freq;
		GameProfile profile;
		boolean themAlliedWithYou;
		boolean youAlliedWithThem;
		boolean sameTeam;

		public PlayerEntry(GameProfile profile, int freq, boolean themAlliedWithYou, boolean youAlliedWithThem, boolean sameTeam) {
			this.profile = profile;
			this.freq = freq;
			this.themAlliedWithYou = themAlliedWithYou;
			this.youAlliedWithThem = youAlliedWithThem;
			this.sameTeam = sameTeam;
		}

		@Override
		public int compareTo(@Nonnull PlayerEntry o) {
			String nameA = getName();
			String nameB = o.getName();
			return nameA.compareTo(nameB);
		}

		@Nonnull
		public String getName() {
			String name = profile.getName();
			return name != null ? name : "";
		}
	}
}
