package com.rwtema.extrautils2.tweaker;

import com.rwtema.extrautils2.api.machine.MachineSlot;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass(XUTweaker.PACKAGE_NAME_BASE + "IMachineSlot")
public class IMachineSlot extends ObjWrapper<MachineSlot<?>> {
	public IMachineSlot(MachineSlot<?> object) {
		super(object);
	}

	@ZenMethod
	public static IMachineSlot newItemStackSlot(String name) {
		return new IMachineSlot(new MachineSlotItem(name));
	}

	@ZenMethod
	public static IMachineSlot newItemStackSlot(String name, int stackCapacity) {
		return new IMachineSlot(new MachineSlotItem(name, stackCapacity));
	}


	@ZenMethod
	public static IMachineSlot newItemStackSlot(String name, boolean optional) {
		return new IMachineSlot(new MachineSlotItem(name, optional, 64));
	}

	@ZenMethod
	public static IMachineSlot newItemStackSlot(String name, boolean optional, int stackCapacity) {
		return new IMachineSlot(new MachineSlotItem(name, optional, stackCapacity));
	}

	@ZenMethod
	public static IMachineSlot newItemStackSlot(String name, int color, boolean optional, String backgroundTexture, int stackCapacity) {
		return new IMachineSlot(new MachineSlotItem(name, color, optional, backgroundTexture, stackCapacity));
	}

	@ZenMethod
	public static IMachineSlot newFluidSlot(String name) {
		return new IMachineSlot(new MachineSlotFluid(name));
	}

	@ZenMethod
	public static IMachineSlot newFluidSlot(String name, int stackCapacity, ILiquidStack filterStack) {
		return new IMachineSlot(new MachineSlotFluid(name, stackCapacity, XUTweaker.createFluidStack(filterStack)));
	}

	@ZenMethod
	public static IMachineSlot newFluidSlot(String name, int stackCapacity, boolean optional, ILiquidStack filterStack) {
		return new IMachineSlot(new MachineSlotFluid(name, stackCapacity, optional, XUTweaker.createFluidStack(filterStack)));
	}

	@ZenMethod
	public static IMachineSlot newFluidSlot(String name, int stackCapacity, int color, boolean optional, ILiquidStack filterStack) {
		return new IMachineSlot(new MachineSlotFluid(name, stackCapacity, color, optional, XUTweaker.createFluidStack(filterStack)));
	}

	@ZenMethod
	public static IMachineSlot newFluidSlot(String name, int stackCapacity) {
		return new IMachineSlot(new MachineSlotFluid(name, stackCapacity));
	}

	@ZenGetter("name")
	public String getName() {
		return object.name;
	}

	@ZenGetter("optional")
	public boolean isOptional() {
		return object.optional;
	}

	@ZenGetter("capacity")
	public int getCapacity() {
		return object.stackCapacity;
	}
}
