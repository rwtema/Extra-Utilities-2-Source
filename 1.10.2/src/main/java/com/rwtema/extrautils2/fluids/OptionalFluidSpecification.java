package com.rwtema.extrautils2.fluids;

import com.rwtema.extrautils2.utils.helpers.NBTCopyHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class OptionalFluidSpecification implements IOptionalFluid {
	private static HashMap<String, OptionalFluidSpecification> cache = new HashMap<>();

	static {
		MinecraftForge.EVENT_BUS.register(OptionalFluidSpecification.class);
	}

	@Nullable
	Fluid fluid;

	@SubscribeEvent
	public static void registerFluid(FluidRegistry.FluidRegisterEvent event) {
		for (Map.Entry<String, Fluid> entry : FluidRegistry.getRegisteredFluids().entrySet()) {
			OptionalFluidSpecification specification = cache.get(entry.getKey());
			if (specification != null) {
				specification.fluid = entry.getValue();
			}
		}

	}

	public static IOptionalFluid getInstance(Fluid fluid) {
		return doCreate(fluid.getName());
	}

	public static IOptionalFluid getInstance(String fluid) {
		return doCreate(fluid);
	}

	private static IOptionalFluid doCreate(String name) {
		return cache.computeIfAbsent(name, s -> {
			OptionalFluidSpecification specification = new OptionalFluidSpecification();
			specification.fluid = FluidRegistry.getFluid(s);
			return specification;
		});
	}

	public static IOptionalFluid create(FluidStack stack) {
		if (stack.tag == null) {
			return getInstance(stack.getFluid());
		} else {
			return new NBTVariant(getInstance(stack.getFluid()), stack.tag);
		}
	}


	@Override
	public boolean isPresent() {
		return fluid != null;
	}


	@Override
	public FluidStack createStack(int amount) {
		return new FluidStack(Validate.notNull(fluid), amount);
	}


	@Override
	public boolean matches(FluidStack stack) {
		return stack != null && fluid != null && fluid == stack.getFluid();
	}

	public static class NBTVariant extends OptionalFluidSpecification {
		final IOptionalFluid parent;
		final NBTTagCompound tag;

		public NBTVariant(IOptionalFluid parent, NBTTagCompound tag) {
			this.parent = parent;
			this.tag = NBTCopyHelper.copyAndHashNBT(tag).copy;
		}

		@Override
		public boolean isPresent() {
			return parent.isPresent();
		}

		@Override
		public boolean matches(FluidStack stack) {
			return parent.matches(stack) && NBTCopyHelper.equalNBT(tag, stack.tag);
		}

		@Override
		public FluidStack createStack(int amount) {
			FluidStack stack = parent.createStack(amount);
			stack.tag = NBTCopyHelper.copyAndHashNBT(tag).copy;
			return stack;
		}
	}
}
