package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.IPower;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.power.PowerManager;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class GrocketPower extends Grocket implements IPower {
	public final static String NBT_FREQUENCY = "Frequency";
	public final static String NBT_ACTIVE = "Active";

	public int frequency;
	public boolean active;


	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger(NBT_FREQUENCY, frequency);
		compound.setBoolean(NBT_ACTIVE, active);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		frequency = tag.getInteger(NBT_FREQUENCY);
		active = tag.getBoolean(NBT_ACTIVE);
	}

	@Nonnull
	@Override
	public String getName() {
		return getType().createStack().getDisplayName();
	}

	@Nullable
	@Override
	public World world() {
		return holder.getWorld();
	}

	@Override
	public int frequency() {
		return frequency;
	}


	@Override
	public void powerChanged(boolean powered) {
		if (active != powered) {
			active = powered;
			holder.markDirty();
			onPowerChanged();
		}
	}


	@Override
	public IWorldPowerMultiplier getMultiplier() {
		return IWorldPowerMultiplier.CONSTANT;
	}


	public void onPowerChanged() {

	}

	@Override
	public void validate() {
		super.validate();
		if (!holder.getWorld().isRemote)
			PowerManager.instance.addPowerHandler(this);

	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (!holder.getWorld().isRemote)
			PowerManager.instance.removePowerHandler(this);
	}

	@Override
	public void onPlaced(EntityPlayer placer) {
		if (!holder.getWorld().isRemote && placer instanceof EntityPlayerMP)
			frequency = Freq.getBasePlayerFreq((EntityPlayerMP) placer);
	}

	public boolean isValidPlayer(EntityPlayer playerIn) {
		return PowerManager.canUse(playerIn, this);
	}
}
