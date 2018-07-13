package com.rwtema.extrautils2.itemhandler;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicGui;
import com.rwtema.extrautils2.gui.backend.ITransferPriority;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.transfernodes.IUpgradeProvider;
import com.rwtema.extrautils2.transfernodes.Upgrade;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;

public class SingleStackHandlerUpgrades extends SingleStackHandler {
	EnumSet<Upgrade> allowedTypes;

	public SingleStackHandlerUpgrades(EnumSet<Upgrade> allowedTypes) {
		this.allowedTypes = allowedTypes;
	}

	public WidgetSlotItemHandler getSpeedUpgradeSlot(int x, int y) {
		return new SlotItemHandlerSpeed(x, y);
	}

	public int getLevel(Upgrade upgrade) {
		ItemStack stack = getStack();
		if (StackHelper.isNull(stack)) return 0;

		Item item = stack.getItem();
		if (!(item instanceof IUpgradeProvider))
			return 0;

		Upgrade stackUpgrade = ((IUpgradeProvider) item).getUpgrade(stack);
		if (stackUpgrade != upgrade) return 0;

		return Math.min(StackHelper.getStacksize(stack), upgrade.maxLevel);
	}

	@Override
	protected int getStackLimit(@Nonnull ItemStack stack) {
		if (!(stack.getItem() instanceof IUpgradeProvider)) {
			return 0;
		}

		Upgrade upgrade = ((IUpgradeProvider) stack.getItem()).getUpgrade(stack);
		if (upgrade == null || !allowedTypes.contains(upgrade)) return 0;
		return upgrade.maxLevel;
	}

	private class SlotItemHandlerSpeed extends WidgetSlotItemHandler implements ITransferPriority {
		public SlotItemHandlerSpeed(int x, int y) {
			super(SingleStackHandlerUpgrades.this, 0, x, y);
		}

		@Override
		public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
			super.renderBackground(manager, gui, guiLeft, guiTop);
			if (!getHasStack()) {
				ItemStack stack = ItemIngredients.Type.UPGRADE_SPEED_SKELETON.newStack();
				gui.renderStack(stack, guiLeft + getX() + 1, guiTop + getY() + 1, "");
			}
		}

		@Override
		public List<String> getToolTip() {
			if (isEmpty()) {
				return ImmutableList.of(Lang.translate("Speed Upgrades"));
			}
			return null;
		}

		@Override
		public int getTransferPriority() {
			return 1;
		}
	}
}
