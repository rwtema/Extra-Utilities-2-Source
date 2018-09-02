package com.rwtema.extrautils2.gui.backend;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class WidgetClickMCButtonChoices<T> extends WidgetClickMCButtonBase implements IWidgetServerNetwork {
	public final ArrayList<Choice<T>> choices = new ArrayList<>();
	int selected = 0;
	int networkState = 0;

	public WidgetClickMCButtonChoices(int x, int y) {
		super(x, y, 18, 18);
	}

	@SuppressWarnings("unchecked")
	public <V extends WidgetClickMCButtonChoices<T>> V addChoice(T marker, ItemStack stack, String tooltip) {
		int ordinal = choices.size();
		Choice<T> choice = new Choice<>(ordinal, marker, stack, tooltip);
		choices.add(choice);
		return (V) this;
	}

	@SuppressWarnings("unchecked")
	public <V extends WidgetClickMCButtonChoices<T>> V addChoice(T marker, String displayText, ItemStack stack, String tooltip) {
		int ordinal = choices.size();
		Choice<T> choice = new Choice<>(ordinal, marker, displayText, stack, tooltip);
		choices.add(choice);
		w = Math.max(w, 18 + 8 + ExtraUtils2.proxy.apply(DynamicContainer.STRING_WIDTH_FUNCTION, displayText));
		return (V) this;
	}

	@SuppressWarnings("unchecked")
	public <V extends WidgetClickMCButtonChoices<T>> V addChoice(T marker, String displayText, String tooltip) {
		int ordinal = choices.size();
		Choice<T> choice = new Choice<>(ordinal, marker, displayText, tooltip);
		choices.add(choice);
		w = Math.max(w, 8 + ExtraUtils2.proxy.apply(DynamicContainer.STRING_WIDTH_FUNCTION, displayText));
		return (V) this;
	}

	@Override
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		super.renderBackground(manager, gui, guiLeft, guiTop);
		String displayText = choices.get(selected).displayText;
		if (displayText == null) return;

		int x;
		if (!StackHelper.isNull(choices.get(selected).stack)) {
			x = getX() + 18 + (((getW() - 18) - gui.getFontRenderer().getStringWidth(displayText))) / 2;
		} else {
			x = getX() + ((getW() - gui.getFontRenderer().getStringWidth(displayText))) / 2;
		}

		GlStateManager.color(1, 1, 1, 1);


		int col = 14737632;
		if (!this.enabled) {
			col = 10526880;
		} else if (this.hover) {
			col = 16777120;
		}

		gui.getFontRenderer().drawString(displayText, guiLeft + x, guiTop + getY() + (18 - 9) / 2, col);
		manager.bindTexture(gui.getWidgetTexture());
		GlStateManager.color(1, 1, 1, 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		gui.renderStack(choices.get(selected).stack, guiLeft + getX() + 1, guiTop + getY() + 1, "");
	}

	@Override
	public List<String> getToolTip() {
		String tooltip = choices.get(selected).tooltip;
		return tooltip != null ? ImmutableList.of(Lang.translate(tooltip)) : null;
	}

	@Nullable
	@Override
	public XUPacketBuffer getPacketToSend(int mouseButton) {
		if (mouseButton == 1) {
			selected--;
			if (selected < 0) selected = choices.size() - 1;
		} else if (mouseButton == 0) {
			selected++;
			if (selected >= choices.size()) selected = 0;
		} else {
			return null;
		}

		networkState++;
		XUPacketBuffer buffer = new XUPacketBuffer();
		buffer.writeVarInt(networkState);
		buffer.writeVarInt(selected);
		return buffer;
	}

	@Override
	public void receiveClientPacket(XUPacketBuffer buffer) {
		int newState = buffer.readVarInt();
		int selection = buffer.readVarInt();
		if (newState > networkState) {
			networkState = newState;
			selected = selection;
			Choice<T> choice = choices.get(selection);
			onSelectedServer(choice.marker);
		}
	}

	protected abstract void onSelectedServer(T marker);

	public abstract T getSelectedValue();

	@Override
	public void addToDescription(XUPacketBuffer packet) {
		T value = getSelectedValue();
		for (Choice<T> choice : choices) {
			if (choice.marker.equals(value)) {
				selected = choice.ordinal;
				break;
			}
		}
		packet.writeVarInt(networkState);
		packet.writeVarInt(selected);

	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		int newNetworkState = packet.readVarInt();
		int newSelected = packet.readVarInt();
		if (newNetworkState < networkState) return;
		networkState = newNetworkState;
		selected = newSelected;
		if (selected < 0 || selected >= choices.size())
			throw new RuntimeException("Invalid packet " + selected);
	}


	static class Choice<T> {
		final int ordinal;
		final T marker;
		final ItemStack stack;
		@Nullable
		final String tooltip;
		final String displayText;

		public Choice(int ordinal, T marker, String displayText, @Nullable String tooltip) {
			this.ordinal = ordinal;
			this.marker = marker;
			this.displayText = displayText;
			this.tooltip = tooltip;
			stack = StackHelper.empty();
		}

		public Choice(int ordinal, T marker, ItemStack stack, String tooltip) {
			this(ordinal, marker, null, stack, tooltip);
		}

		public Choice(int ordinal, T marker, String displayText, ItemStack stack, @Nullable String tooltip) {
			this.ordinal = ordinal;
			this.marker = marker;
			this.stack = stack;
			this.tooltip = tooltip;
			this.displayText = displayText;
		}

	}


}
