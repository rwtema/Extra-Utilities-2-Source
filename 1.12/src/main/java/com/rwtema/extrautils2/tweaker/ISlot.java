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
@ZenClass
public class ISlot extends ObjWrapper<MachineSlot<?>> {
	public ISlot(MachineSlot<?> object) {
		super(object);
	}

	@ZenMethod
	public static ISlot newItemStackSlot(String name) {
		return new ISlot(new MachineSlotItem(name));
	}

	@ZenMethod
	public static ISlot newItemStackSlot(String name, int stackCapacity) {
		return new ISlot(new MachineSlotItem(name, stackCapacity));
	}

	@ZenMethod
	public static ISlot newItemStackSlot(String name, boolean optional, int stackCapacity) {
		return new ISlot(new MachineSlotItem(name, optional, stackCapacity));
	}

	@ZenMethod
	public static ISlot newItemStackSlot(String name, int color, boolean optional, String backgroundTexture, int stackCapacity) {
		return new ISlot(new MachineSlotItem(name, color, optional, backgroundTexture, stackCapacity));
	}

	@ZenMethod
	public static ISlot newFluidSlot(String name) {
		return new ISlot(new MachineSlotFluid(name));
	}

	@ZenMethod
	public static ISlot newFluidSlot(String name, int stackCapacity, ILiquidStack filterStack) {
		return new ISlot(new MachineSlotFluid(name, stackCapacity, XUTweaker.createFluidStack(filterStack)));
	}

	@ZenMethod
	public static ISlot newFluidSlot(String name, int stackCapacity, boolean optional, ILiquidStack filterStack) {
		return new ISlot(new MachineSlotFluid(name, stackCapacity, optional, XUTweaker.createFluidStack(filterStack)));
	}

	@ZenMethod
	public static ISlot newFluidSlot(String name, int stackCapacity, int color, boolean optional, ILiquidStack filterStack) {
		return new ISlot(new MachineSlotFluid(name, stackCapacity, color, optional, XUTweaker.createFluidStack(filterStack)));
	}

	@ZenMethod
	public static ISlot newFluidSlot(String name, int stackCapacity) {
		return new ISlot(new MachineSlotFluid(name, stackCapacity));
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
