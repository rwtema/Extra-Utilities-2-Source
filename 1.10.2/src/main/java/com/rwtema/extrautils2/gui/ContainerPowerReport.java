package com.rwtema.extrautils2.gui;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicGui;
import com.rwtema.extrautils2.gui.backend.WidgetBase;
import com.rwtema.extrautils2.gui.backend.WidgetTextDataScroll;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.IPower;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class ContainerPowerReport extends DynamicContainer {
	public ContainerPowerReport(final EntityPlayer player) {
		addWidget(new WidgetTextDataScroll(4, 24, 176, 176) {
			EntityPlayerMP playerMP = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player) : null;

			@Override
			public void addToDescription(XUPacketBuffer packet) {
				synchronized (PowerManager.MUTEX) {

					TObjectIntHashMap<String> numPowers = new TObjectIntHashMap<>();
					TObjectFloatHashMap<String> powerOutput = new TObjectFloatHashMap<>();
					PowerManager.PowerFreq powerFreq = PowerManager.instance.getPowerFreq(Freq.getBasePlayerFreq(playerMP));
					for (IPower handler : powerFreq.powerHandlers) {
						String name = handler.getName();
						if (name == null) continue;
						float f = PowerManager.getCurrentPower(handler);
						if (Float.isNaN(f)) continue;
						if (handler.getMultiplier().hasInefficiencies()) {
							f *= PowerManager.getEfficiency(handler);
						}
						numPowers.adjustOrPutValue(name, 1, 1);
						powerOutput.adjustOrPutValue(name, f, f);
					}

					Set<String> names = numPowers.keySet();
					packet.writeInt(names.size());
					for (String name : names) {
						packet.writeString(name);
						packet.writeInt(numPowers.get(name));
						packet.writeFloat(powerOutput.get(name));
					}
				}
			}

			@Override
			@SideOnly(Side.CLIENT)
			protected String constructText(XUPacketBuffer packet) {
				int n = packet.readInt();
				if (n == 0)
					return Lang.translate("No Power Handlers");

				ArrayList<String> generators = new ArrayList<>();
				ArrayList<String> drainers = new ArrayList<>();
				ArrayList<String> inactive = new ArrayList<>();
				for (int i = 0; i < n; i++) {
					String formattedText = I18n.translateToLocal(packet.readString());
					int num = packet.readInt();
					float power = packet.readFloat();
					if (power < 0) {
						generators.add(Lang.translateArgs("%s x %s: %s GP", formattedText, num, StringHelper.niceFormat(-power)));
					} else if (power > 0) {
						drainers.add(Lang.translateArgs("%s x %s: %s GP", formattedText, num, StringHelper.niceFormat(power)));
					} else {
						inactive.add(Lang.translateArgs("%s x %s", formattedText, num));
					}
				}

				Collections.sort(generators);
				Collections.sort(drainers);

				StringBuilder builder = new StringBuilder();
				addEntries(builder, generators, "Power Generators");
				addEntries(builder, drainers, "Power Users");
				addEntries(builder, inactive, "Inactive");


				return builder.toString();
			}

			@SideOnly(Side.CLIENT)
			private void addEntries(StringBuilder builder, ArrayList<String> generators, String text) {
				if (!generators.isEmpty()) {
					builder.append(Lang.translate(text)).append('\n');
					for (String s : generators) {
						builder.append(' ').append(s).append('\n');
					}
				}
			}
		});
		crop(4);

		if (XU2Entries.itemIngredients.enabled)
			addWidget(new WidgetBase(width / 2, 0, 0, 0) {
				int size = 40;

				@Override
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					manager.bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
					TextureAtlasSprite sprite = Textures.sprites.get(ItemIngredients.Type.REDSTONE_CRYSTAL.texture);
					if (sprite != null)
						gui.drawTexturedModalRect(guiLeft + getX() - size, guiTop + getY() - size, sprite, size * 2, size * 2);
					manager.bindTexture(gui.getWidgetTexture());
				}
			});


		validate();
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
		return true;
	}
}
