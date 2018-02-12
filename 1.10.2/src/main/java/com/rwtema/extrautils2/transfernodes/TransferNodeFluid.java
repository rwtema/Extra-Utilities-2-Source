package com.rwtema.extrautils2.transfernodes;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.fluids.FluidTankSerial;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TransferNodeFluid extends TransferNodeBase<IFluidHandler> implements IDynamicHandler {
	SingleStackHandlerFilter.FluidFilter filter = registerNBT("Filter", new SingleStackHandlerFilter.FluidFilter() {
		@Override
		protected void onContentsChanged() {
			holder.markDirty();
		}
	});
	protected FluidTankSerial tank = registerNBT("Buffer", new FluidTankSerial(1000) {
		@Override
		protected void onChange() {
			holder.markDirty();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (resource == null || !filter.matches(resource))
				return 0;
			return super.fill(resource, doFill);
		}
	});

	@Override
	public List<ItemStack> getDrops() {
		ArrayList<ItemStack> drops = Lists.newArrayList();
		drops.add(getBaseDrop());
		for (ItemStack itemStack : InventoryHelper.getItemHandlerIterator(upgradeHandler)) {
			if (StackHelper.isNonNull(itemStack)) {
				drops.add(itemStack);
			}
		}
		if (StackHelper.isNonNull(filter.getStack())) drops.add(filter.getStack());
		return drops;
	}

	@Override
	protected boolean shouldAdvance() {
		return !tank.isEmpty();
	}

	public int getDrainAmount() {
		return getUpgradeLevel(Upgrade.STACK_SIZE) > 0 ? 12800 : 200;
	}

	@Override
	protected void processBuffer(@Nullable IFluidHandler attached) {
		if (attached != null) {
			FluidStack drain = attached.drain(getDrainAmount(), false);
			if (drain != null) {
				int fill = tank.fill(drain, false);
				if (fill > 0) {
					tank.fill(attached.drain(fill, true), true);
				}
			}
		} else {
			int upgradeLevel = getUpgradeLevel(Upgrade.MINING);
			if (upgradeLevel > 0) {
				FluidStack fluid = tank.getFluid();
				if (fluid != null && (fluid.getFluid() != FluidRegistry.WATER || fluid.amount == tank.getCapacity())) {
					return;
				}

				World world = holder.getWorld();
				BlockPos offset = holder.getPos().offset(side);
				IBlockState state = world.getBlockState(offset);
				if (state == BlockStates.WATER_LEVEL_0) {
					byte numWater = 0;


					EnumSet<EnumFacing> enumFacings = FacingHelper.horizontalOrthogonal.get(side);
					for (EnumFacing facing : enumFacings) {
						IBlockState blockState = world.getBlockState(offset.offset(facing));
						if (blockState == BlockStates.WATER_LEVEL_0) {
							numWater++;
							if (numWater == 2)
								break;
						}
					}

					if (numWater >= 2 && ForgeEventFactory.canCreateFluidSource(world, offset, state, true)) {
						tank.fill(new FluidStack(FluidRegistry.WATER, 200), true);
					}
				}
			}
		}
	}

	@Override
	protected boolean processPosition(BlockPos pingPos, IFluidHandler attached, IPipe pipe) {
		if (pipe == null) {
			return true;
		}

		int maxTransfer = tank.getFluidAmount();

		if (maxTransfer >= 0) {
			for (EnumFacing facing : FacingHelper.getRandomFaceOrder()) {
				IFluidHandler capability = pipe.getCapability(holder.getWorld(), pingPos, facing, CapGetter.FluidHandler);
				if (capability == null) continue;

				int fill = capability.fill(tank.getFluid(), false);
				if (fill > 0) {
					fill = Math.min(fill, maxTransfer);
					maxTransfer -= capability.fill(tank.drain(fill, true), true);
					if (maxTransfer == 0) break;
				}

			}
		}

		if (tank.isEmpty()) {
			ping.resetPosition();
			return false;
		}


		return true;
	}

	@Override
	public IFluidHandler getHandler(TileEntity tile) {
		return CapGetter.FluidHandler.getInterface(tile, side.getOpposite());
	}


	@Override
	public GrocketType getType() {
		return GrocketType.TRANSFER_NODE_FLUIDS;
	}

	@Override
	public boolean onActivated(EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!holder.getWorld().isRemote)
			holder.openGui(playerIn, this);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getInterface(TileEntity tileEntity, CapGetter<T> capability) {
		if (capability == CapGetter.FluidHandler) {
			final IFluidHandler handler = CapGetter.FluidHandler.getInterface(tileEntity, side.getOpposite());
			if (handler != null) {
				return (T) new IFluidHandler() {

					@Override
					public IFluidTankProperties[] getTankProperties() {
						return handler.getTankProperties();
					}

					@Override
					public int fill(FluidStack resource, boolean doFill) {
						return 0;
					}

					@Nullable
					@Override
					public FluidStack drain(FluidStack resource, boolean doDrain) {
						if (resource == null || !filter.matches(resource))
							return null;
						return handler.drain(resource, doDrain);
					}

					@Nullable
					@Override
					public FluidStack drain(int maxDrain, boolean doDrain) {
						FluidStack drain = handler.drain(maxDrain, false);
						if (drain == null || !filter.matches(drain))
							return null;
						return doDrain ? handler.drain(maxDrain, true) : drain;
					}
				};
			}
		}
		return super.getInterface(tileEntity, capability);
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new TransferNodeFluidContainer(player);
	}

	@ItemStackNonNull
	@Override
	public ItemStack getItem() {
		return StackHelper.empty();
	}

	@Override
	public FluidStack getFluid() {
		return tank.getFluid();
	}

	@Override
	public Type getBufferType() {
		return Type.FLUID;
	}

	public static class Retrieve extends TransferNodeFluid {
		@Override
		protected boolean shouldAdvance() {
			return !tank.isFull();
		}

		@Override
		protected void processBuffer(@Nullable IFluidHandler attached) {
			if (attached != null) {
				FluidStack drain = tank.getFluid();
				if (drain != null) {
					int fill = attached.fill(drain, false);
					if (fill > 0) {
						attached.fill(tank.drain(fill, true), true);
					}
				}
			}
		}

		@Override
		protected boolean processPosition(BlockPos pingPos, IFluidHandler attached, IPipe pipe) {
			if (pipe == null || tank.isFull()) {
				return true;
			}

			int maxTransfer = getDrainAmount();


			for (EnumFacing facing : FacingHelper.getRandomFaceOrder()) {
				IFluidHandler capability = pipe.getCapability(holder.getWorld(), pingPos, facing, CapGetter.FluidHandler);
				if (capability == null) continue;

				int fill = tank.fill(capability.drain(maxTransfer, false), false);
				if (fill > 0) {
					fill = Math.min(fill, maxTransfer);
					maxTransfer -= tank.fill(capability.drain(fill, true), true);
					if (maxTransfer == 0) break;
				}
			}


			return true;
		}

		@Override
		public GrocketType getType() {
			return GrocketType.TRANSFER_NODE_FLUIDS_RETRIEVE;
		}
	}

	public class TransferNodeFluidContainer extends DynamicContainerTile {
		public TransferNodeFluidContainer(EntityPlayer player) {
			super(holder, 9, 64);
			addTitle("Transfer Node");

			int numUpgradeSlots = upgradeHandler.getSlots();
			for (int i = 0; i < numUpgradeSlots; i++) {
				addWidget(new WidgetSlotItemHandler(upgradeHandler, i, centerX + i * 18 - 9 * numUpgradeSlots, 80));
			}

			addWidget(filter.newSlot(4, 80));

			addWidget(new WidgetFluidIndicator(centerSlotX, 31) {
				@Override
				public FluidStack getFluid() {
					return tank.getFluid();
				}

				@Override
				public float getFillPercent() {
					return 1;
				}
			});


			addWidget(new WidgetTextData(4, 54, DynamicContainerTile.playerInvWidth - 8) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeFluidStack(tank.getFluid());
					packet.writeInt(tank.getFluidAmount());
					packet.writeInt(tank.getCapacity());
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					FluidStack stack = packet.readFluidStack();
					int i = packet.readInt();
					int n = packet.readInt();
					if (stack == null) return null;
					return String.format("%s: %s/%s mB", stack.getLocalizedName(), i, n);

				}
			}.setAlign(0));

			addWidget(new WidgetPingPosition(4, 68));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
