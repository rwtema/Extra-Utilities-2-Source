package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.gui.backend.WidgetClickMCButtonChoices;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public enum RedstoneState {
	OPERATE_ALWAYS {
		@Override
		public boolean acceptableValue(boolean state) {
			return true;
		}
	},
	OPERATE_REDSTONE_ON {
		@Override
		public boolean acceptableValue(boolean state) {
			return state;
		}
	},
	OPERATE_REDSTONE_OFF {
		@Override
		public boolean acceptableValue(boolean state) {
			return !state;
		}
	},
	OPERATE_REDSTONE_PULSE {
		@Override
		public boolean acceptableValue(boolean state) {
			throw new UnsupportedOperationException();
		}
	};

	public static WidgetClickMCButtonChoices<RedstoneState> getRSWidget(int x, int y, NBTSerializable.NBTEnum<RedstoneState> redstone_state) {
		return new WidgetClickMCButtonChoices<RedstoneState>(x, y) {
			@Override
			protected void onSelectedServer(RedstoneState marker) {
				if (marker != RedstoneState.OPERATE_REDSTONE_PULSE)
					redstone_state.value = marker;
			}

			@Override
			public RedstoneState getSelectedValue() {
				return redstone_state.value;
			}
		}.addChoice(RedstoneState.OPERATE_ALWAYS, new ItemStack(Items.GUNPOWDER), "Always On")
				.addChoice(RedstoneState.OPERATE_REDSTONE_ON, new ItemStack(Items.REDSTONE), "Redstone On")
				.addChoice(RedstoneState.OPERATE_REDSTONE_OFF, new ItemStack(Blocks.REDSTONE_TORCH), "Redstone Off");
	}

	public static WidgetClickMCButtonChoices<RedstoneState> getRSWidgetPulses(int x, int y, NBTSerializable.NBTEnum<RedstoneState> redstone_state, NBTSerializable.Int pulses) {
		return new WidgetClickMCButtonChoices<RedstoneState>(x, y) {
			@Override
			protected void onSelectedServer(RedstoneState marker) {
				redstone_state.value = marker;
				pulses.value = 0;
			}

			@Override
			public RedstoneState getSelectedValue() {
				return redstone_state.value;
			}
		}.addChoice(RedstoneState.OPERATE_ALWAYS, new ItemStack(Items.GUNPOWDER), "Always On")
				.addChoice(RedstoneState.OPERATE_REDSTONE_ON, new ItemStack(Items.REDSTONE), "Redstone On")
				.addChoice(RedstoneState.OPERATE_REDSTONE_OFF, new ItemStack(Blocks.REDSTONE_TORCH), "Redstone Off")
				.addChoice(RedstoneState.OPERATE_REDSTONE_PULSE, new ItemStack(Items.REPEATER), "Redstone Pulse");
	}

	public abstract boolean acceptableValue(boolean state);
}
