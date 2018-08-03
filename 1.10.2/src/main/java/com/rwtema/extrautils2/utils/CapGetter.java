package com.rwtema.extrautils2.utils;


import com.google.common.collect.Lists;
import com.rwtema.extrautils2.transfernodes.IPipeConnect;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.IdentityHashMap;

public class CapGetter<T> {
	public static CapGetter<IItemHandler> ItemHandler = new CapGetter<>(IItemHandler.class);
	public static CapGetter<IPipeConnect> PipeConnect = new CapGetter<IPipeConnect>(IPipeConnect.class) {
		@Override
		public boolean hasInterface(ICapabilityProvider provider, EnumFacing side) {
			return provider instanceof IPipeConnect && ((IPipeConnect) provider).forceConnect(side);
		}

		@Override
		public IPipeConnect getInterface(ICapabilityProvider provider, EnumFacing side) {
			return null;
		}
	};
	public static CapGetter<IFluidHandler> FluidHandler = new CapGetter<>(IFluidHandler.class);
	private static IdentityHashMap<String, Capability<?>> providers = ObfuscationReflectionHelper.getPrivateValue(CapabilityManager.class, CapabilityManager.INSTANCE, "providers");
	public static CapGetter<IEnergyStorage> energyReceiver = new CapGetter<IEnergyStorage>(IEnergyStorage.class
	) {
		@Override
		protected boolean hasCapability(ICapabilityProvider provider, EnumFacing side) {
			if (!provider.hasCapability(capability, side)) return false;
			IEnergyStorage capability = provider.getCapability(this.capability, side);
			return capability != null && capability.canReceive();
		}

		@Override
		public IEnergyStorage getInterface(ICapabilityProvider provider, EnumFacing side) {
			IEnergyStorage anInterface = super.getInterface(provider, side);
			return anInterface != null && anInterface.canReceive() ? anInterface : null;
		}
	};
	public static CapGetter<IEnergyStorage> energyExtractor = new CapGetter<IEnergyStorage>(IEnergyStorage.class
	) {
		@Override
		protected boolean hasCapability(ICapabilityProvider provider, EnumFacing side) {
			if (!provider.hasCapability(capability, side)) return false;
			IEnergyStorage capability = provider.getCapability(this.capability, side);
			return capability != null && capability.canExtract();
		}

		@Override
		public IEnergyStorage getInterface(ICapabilityProvider provider, EnumFacing side) {
			IEnergyStorage anInterface = super.getInterface(provider, side);
			return anInterface != null && anInterface.canReceive() ? anInterface : null;
		}
	};
	public static ArrayList<CapGetter<?>> caps = Lists.newArrayList(ItemHandler, PipeConnect, FluidHandler, energyReceiver);
	final Class<T> clazz;
	final Converter<?, T>[] converters;
	boolean init;
	Capability<T> capability;

	@SafeVarargs
	public CapGetter(Class<T> clazz, Converter<?, T>... converters) {
		this.clazz = clazz;
		this.converters = converters;
	}

	@SuppressWarnings("unchecked")
	static <T> Capability<T> getCapability(Class<T> clazz) {
		String intern = clazz.getName().intern();
		return (Capability<T>) providers.get(intern);
	}

	public <R> R cast(T instance) {
		return capability.cast(instance);
	}

	public boolean hasInterface(ICapabilityProvider provider, EnumFacing side) {
		if (!init) {
			capability = getCapability(clazz);
			init = capability != null || Loader.instance().hasReachedState(LoaderState.AVAILABLE);
		}

		if (provider == null) return false;

		if (capability != null && hasCapability(provider, side)) return true;

		if (clazz.isInstance(provider)) return true;

		for (Converter<?, T> converter : converters) {
			if (converter.canHandle(provider, side)) return true;
		}

		return false;
	}

	protected boolean hasCapability(ICapabilityProvider provider, EnumFacing side) {
		return provider.hasCapability(capability, side);
	}

	@SuppressWarnings("unchecked")
	public T getInterface(ICapabilityProvider provider, EnumFacing side) {
		if (!init) {
			capability = getCapability(clazz);
			init = capability != null || Loader.instance().hasReachedState(LoaderState.AVAILABLE);

		}

		if (provider == null) return null;

		if (capability != null && hasCapability(provider, side))
			return provider.getCapability(capability, side);

		if (clazz.isInstance(provider)) {
			return (T) provider;
		}

		for (Converter<?, T> converter : converters) {
			T convert = converter.convert(provider, side);
			if (convert != null) return convert;
		}

		return null;
	}

	public static abstract class Converter<S, T> {
		final Class<S> toConvert;

		protected Converter(Class<S> toConvert) {
			this.toConvert = toConvert;
		}

		@SuppressWarnings("unchecked")
		public T convert(Object type, EnumFacing side) {
			return canHandle(type, side) ? convertInstance((S) type, side) : null;
		}

		public boolean canHandle(Object type, EnumFacing side) {
			return toConvert.isInstance(type);
		}

		protected abstract T convertInstance(S type, EnumFacing side);
	}


}
