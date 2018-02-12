package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.WidgetTextData;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ArrayAccess;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public abstract class TransferNodeBase<T> extends Grocket implements ITickable, IBuffer {
	protected final Ping ping = registerNBT("ping", new Ping());
	protected int cooldown = 0;
	protected float power = Float.NaN;
	TObjectIntHashMap<Upgrade> upgrades;
	public ItemStackHandler upgradeHandler = registerNBT("upgrades", new ItemStackHandler(6) {

		public final TObjectIntProcedure<Upgrade> loadUpgradePower = (a, b) -> {
			float powerUse = a.getPowerUse(b);
			if (Float.isNaN(TransferNodeBase.this.power))
				TransferNodeBase.this.power = powerUse;
			else
				TransferNodeBase.this.power += powerUse;
			return true;
		};


		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			if (!(stack.getItem() instanceof IUpgradeProvider)) {
				return 0;
			}

			Upgrade upgrade = ((IUpgradeProvider) stack.getItem()).getUpgrade(stack);
			if (upgrade == null) return 0;

			ArrayAccess<ItemStack> arrayAccess = CompatHelper.getArray10List11(stacks);
			for (int i = 0; i < arrayAccess.length(); i++) {
				if (i == slot) continue;
				ItemStack otherStack = arrayAccess.get(i);
				if (StackHelper.isNonNull(otherStack) && otherStack != null) {
					if (StackHelper.isNonNull(otherStack) && ItemHandlerHelper.canItemStacksStack(stack, otherStack))
						return 0;

					Item item;

					if ((item = otherStack.getItem()) instanceof IUpgradeProvider && ((IUpgradeProvider) item).getUpgrade(otherStack) == upgrade) {
						return 0;
					}
				}
			}


			return upgrade.maxLevel;
		}

		@Override
		protected void onContentsChanged(int slot) {
			markDirty();
			float prevPower = power;
			loadUpgrades();
			if (Float.floatToIntBits(prevPower) != Float.floatToIntBits(power)) {
				if (holder != null) {
					PowerManager.instance.markDirty(holder);
				}
			}
		}

		@Override
		protected void onLoad() {
			loadUpgrades();
		}

		public void loadUpgrades() {
			float p = Float.NaN;

			TObjectIntHashMap<Upgrade> upgradeMap = new TObjectIntHashMap<>(10, 0.5F, 0);
			for (ItemStack stack : stacks) {
				if (StackHelper.isNonNull(stack) && stack.getItem() instanceof IUpgradeProvider) {
					upgradeMap.adjustOrPutValue(((IUpgradeProvider) stack.getItem()).getUpgrade(stack), StackHelper.getStacksize(stack), StackHelper.getStacksize(stack));
				}
			}

			TransferNodeBase.this.power = Float.NaN;

			upgradeMap.forEachEntry(loadUpgradePower);

			TransferNodeBase.this.upgrades = upgradeMap;

		}
	});
	private byte rareTickOnce;

	public int getUpgradeLevel(Upgrade upgrade) {
		TObjectIntHashMap<Upgrade> upgrades = this.upgrades;
		if (upgrades == null) return upgrade.getModifierLevel(0);
		int level = upgrades.get(upgrade);
		if (level < 0) level = 0;
		if (level > upgrade.maxLevel) level = upgrade.maxLevel;
		return upgrade.getModifierLevel(level);
	}

	@Override
	public void update() {
		if (holder == null) return;

		World world = holder.getWorld();

		if (world.getTotalWorldTime() % 16 == 0) {
			if (rareTickOnce == 0) {
				rareTickOnce = 1;
			} else {
				rareTickOnce = 2;
			}
		} else if (rareTickOnce != 0) {
			rareTickOnce = 0;
		}

		if (ping.needsInit()) {
			ping.init(world, holder.getPos(), side.getOpposite(), this);
			return;
		}

		if (cooldown > 0) cooldown -= stepCooldown();

		if (checkRedstone()) {
			cooldown = 20;
			return;
		}

		T attached = getAttached();
		while (cooldown <= 0) {
			cooldown += 20;


			processBuffer(attached);

			if (shouldAdvance()) {
				BlockPos pingPos = ping.getPos();
				if (pingPos == null)
					return;

				if (!world.isBlockLoaded(pingPos)) {
					ping.resetPosition();
					return;
				} else {
					IPipe pipe = TransferHelper.getPipe(world, pingPos);

					if (processPosition(pingPos, attached, pipe)) {
						ping.advanceSearch(pipe);
					}
				}
			} else {
				ping.resetPosition();
			}


		}
	}

	protected abstract boolean shouldAdvance();

	protected abstract void processBuffer(@Nullable T attached);


	protected abstract boolean processPosition(BlockPos pingPos, T attached, IPipe pipe);

	private int stepCooldown() {
		return 1 + getUpgradeLevel(Upgrade.SPEED);
	}

	public boolean checkRedstone() {
		return holder.getWorld().isBlockPowered(holder.getPos());
	}

	public T getAttached() {
		TileEntity tile = holder.getWorld().getTileEntity(holder.getPos().offset(side));
		if (tile == null) return null;
		T capability1 = getHandler(tile);
		if (capability1 != null) return capability1;

		return null;
	}

	public abstract T getHandler(TileEntity tile);

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("Cooldown", cooldown);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		cooldown = tag.getInteger("Cooldown");
	}

	@Override
	public <S> boolean hasInterface(TileEntity tileEntity, CapGetter<S> capability) {
		return capability == CapGetter.PipeConnect || super.hasInterface(tileEntity, capability);
	}

	@Override
	public float getPower() {
		return power;
	}

	public class WidgetPingPosition extends WidgetTextData {
		public WidgetPingPosition(int x, int y) {
			super(x, y, DynamicContainerTile.playerInvWidth - 8);
			setAlign(0);
		}

		@Override
		public void addToDescription(XUPacketBuffer packet) {
			packet.writeBlockPos(ping.getPos());
		}

		@Override
		protected String constructText(XUPacketBuffer packet) {
			BlockPos pos = packet.readBlockPos();
			pos = pos.subtract(holder.getPos());
			return Lang.translateArgs("x = %s, y = %s, z = %s", pos.getX(), pos.getY(), pos.getZ());
		}
	}
}
