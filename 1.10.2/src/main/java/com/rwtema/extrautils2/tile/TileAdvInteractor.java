package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetClickMCButtonChoices;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerUpgrades;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.transfernodes.Upgrade;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class TileAdvInteractor extends TilePower implements ITickable, IDynamicHandler {
	public static final EnumSet<Upgrade> SPEED = EnumSet.of(Upgrade.SPEED);

	public NBTSerializable.NBTEnum<RedstoneState> redstone_state = registerNBT("redstone", new NBTSerializable.NBTEnum<>(RedstoneState.OPERATE_ALWAYS));
	public NBTSerializable.Int pulses = registerNBT("pulses", new NBTSerializable.Int());
	public NBTSerializable.Int cooldown = registerNBT("cooldown", new NBTSerializable.Int());
	public SingleStackHandlerUpgrades upgrades = registerNBT("upgrades", new SingleStackHandlerUpgrades(SPEED) {
		@Override
		protected void onContentsChanged() {
			TileAdvInteractor.this.markDirty();
			PowerManager.instance.markDirty(TileAdvInteractor.this);
		}
	});
	public NBTSerializable.NBTBoolean powered = registerNBT("powered", new NBTSerializable.NBTBoolean());

	public static WidgetClickMCButtonChoices<RedstoneState> getRSWidget(int x, int y, @Nonnull final NBTSerializable.NBTEnum<RedstoneState> redstone_state, @Nullable final NBTSerializable.Int pulses) {
		WidgetClickMCButtonChoices<RedstoneState> button = new WidgetClickMCButtonChoices<RedstoneState>(x, y) {
			@Override
			protected void onSelectedServer(RedstoneState marker) {
				redstone_state.value = marker;
				if (pulses != null)
					pulses.value = 0;
			}

			@Override
			public RedstoneState getSelectedValue() {
				return redstone_state.value;
			}
		};
		button.addChoice(RedstoneState.OPERATE_ALWAYS, new ItemStack(Items.GUNPOWDER), Lang.translate("Always On"))
				.addChoice(RedstoneState.OPERATE_REDSTONE_ON, new ItemStack(Items.REDSTONE), Lang.translate("Redstone On"))
				.addChoice(RedstoneState.OPERATE_REDSTONE_OFF, new ItemStack(Blocks.REDSTONE_TORCH), Lang.translate("Redstone Off"));
		if (pulses != null) {
			button.addChoice(RedstoneState.OPERATE_REDSTONE_PULSE, new ItemStack(Items.REPEATER), Lang.translate("Redstone Pulse"));
		}
		return button;
	}

	@Override
	protected abstract Iterable<ItemStack> getDropHandler();

	@Override
	public void update() {
		if (world.isRemote)
			return;

		if (!active) return;

		if (!preOperate()) return;

		if (cooldown.value > 0) {
			cooldown.value -= 1 + upgrades.getLevel(Upgrade.SPEED);
			if (cooldown.value > 0)
				return;
		}

		switch (redstone_state.value) {
			case OPERATE_REDSTONE_ON:
				if (!powered.value) return;
				break;
			case OPERATE_REDSTONE_OFF:
				if (powered.value) return;
				break;
			case OPERATE_REDSTONE_PULSE:
				if (pulses.value == 0) return;
				pulses.value--;
				break;
		}

		while (cooldown.value <= 0) {
			cooldown.value += 20;
			if(!operate()){

			}
		}
	}

	public boolean preOperate() {
		return true;
	}

	protected abstract boolean operate();

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {

		boolean wasPowered = powered.value;
		boolean newPower = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;
		if (newPower != wasPowered) {
			powered.value = newPower;
			if (newPower && redstone_state.value == RedstoneState.OPERATE_REDSTONE_PULSE) {
				pulses.value++;
			}
		}
	}

	@Override
	public float getPower() {
		int level = upgrades.getLevel(Upgrade.SPEED);
		if (level == 0) return Float.NaN;
		return Upgrade.SPEED.getPowerUse(level);
	}

	@Override
	public void onPowerChanged() {

	}

}
