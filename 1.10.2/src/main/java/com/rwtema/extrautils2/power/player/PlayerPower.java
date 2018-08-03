package com.rwtema.extrautils2.power.player;

import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.IPower;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PlayerPower implements IPower, IWorldPowerMultiplier {
	public final int freq;
	public final int dimension;
	@Nonnull
	private final EntityPlayer player;
	public int cooldown = 20;
	public boolean invalid;

	public PlayerPower(@Nonnull EntityPlayer player) {
		this.player = player;
		if (player instanceof EntityPlayerMP) {
			freq = Freq.getBasePlayerFreq((EntityPlayerMP) player);
		} else
			freq = 0;
		dimension = player.world.provider.getDimension();
	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	@Override
	public IWorldPowerMultiplier getMultiplier() {
		return this;
	}

	@Override
	public int frequency() {
		return freq;
	}

	@Override
	public World world() {
		return null;
	}

	@Override
	public float multiplier(World world) {
		EntityPlayerMP playerMP = getPlayerMP();
		if (invalid || !FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().contains(playerMP)) {
			return 0;
		}

		return power(playerMP);
	}

	@Nonnull
	public EntityPlayer getPlayer() {
		return player;
	}

	@Nonnull
	public EntityPlayerMP getPlayerMP() {
		return (EntityPlayerMP) player;
	}

	public abstract float power(EntityPlayer playerMP);

	@Override
	public final float getPower() {
		return 1;
	}

	public void onAdd() {

	}

	public void onRemove() {

	}

	public void update(boolean selected, ItemStack params) {

	}

	public boolean shouldOveride(EntityPlayer player, PlayerPower other) {
		return false;
	}

	public void tick() {

	}

	public void onAddClient() {

	}

	public void onRemoveClient() {

	}

	public void tickClient() {

	}

	@Nullable
	@Override
	public BlockPos getLocation() {
		return null;
	}

	public boolean shouldSustain(ItemStack stack) {
		return true;
	}
}
