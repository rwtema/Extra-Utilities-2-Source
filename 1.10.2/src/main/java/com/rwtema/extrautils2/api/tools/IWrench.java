package com.rwtema.extrautils2.api.tools;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.items.IItemHandler;

import java.util.concurrent.Callable;

public interface IWrench {
	@CapabilityInject(IWrench.class)
	Capability<IWrench> CAPABILITY = null;

	class InitClass {
		@CapabilityInject(IItemHandler.class)
		public static void initializeMe(Capability capability) {
			CapabilityManager.INSTANCE.register(IWrench.class, new Capability.IStorage<IWrench>() {
				@Override
				public NBTBase writeNBT(Capability<IWrench> capability, IWrench instance, EnumFacing side) {
					return new NBTTagByte((byte) 0);
				}

				@Override
				public void readNBT(Capability<IWrench> capability, IWrench instance, EnumFacing side, NBTBase nbt) {

				}
			}, new Callable<IWrench>() {
				@Override
				public IWrench call() throws Exception {
					return new IWrench() {
					};
				}
			});
		}
	}
}
