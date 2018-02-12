package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TileTrashCanFluids extends XUTile implements IDynamicHandler {
	private static final int FAKE_CAPACITY = 1000000;
	static IFluidTankProperties[] properties = new IFluidTankProperties[]{
			new FluidTankProperties(null, FAKE_CAPACITY, true, false),
			new FluidTankProperties(null, FAKE_CAPACITY, true, false),
			new FluidTankProperties(null, FAKE_CAPACITY, true, false),
			new FluidTankProperties(null, FAKE_CAPACITY, true, false),
	};
	SingleStackHandlerFilter.FluidFilter FILTER = registerNBT("filter", new SingleStackHandlerFilter.FluidFilter());
	IFluidHandler ABSORB_HANDLER = new IFluidHandler() {
		@Override
		public IFluidTankProperties[] getTankProperties() {
			return properties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			return resource != null ? resource.amount : 0;
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			return null;
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			return null;
		}
	};

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return InventoryHelper.getItemHandlerIterator(FILTER);
	}

	@Nullable
	@Override
	public IFluidHandler getFluidHandler(EnumFacing facing) {
		return ABSORB_HANDLER;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTrashCan(player);
	}

	public class ContainerTrashCan extends DynamicContainerTile {

		public ContainerTrashCan(EntityPlayer player) {
			super(TileTrashCanFluids.this, 16, 64);
			addTitle(Lang.getItemName(getXUBlock()), false);
			addWidget(FILTER.newSlot(centerSlotX, 40));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
