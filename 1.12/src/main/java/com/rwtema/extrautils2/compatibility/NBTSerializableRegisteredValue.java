package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class NBTSerializableRegisteredValue<V extends IForgeRegistryEntry<V>> implements INBTSerializable<NBTTagString> {
	private static final ResourceLocation emptyNeverToBeFound = new ResourceLocation("EmptyNeverToBeFound");
	final IForgeRegistry<V> registry;
	public V value;

	public NBTSerializableRegisteredValue(IForgeRegistry<V> registry) {
		this.registry = registry;
		value = registry.getValue(emptyNeverToBeFound);
	}

	@Override
	public NBTTagString serializeNBT() {
		return NBTSerializable.NBTResourceLocationSerializable.serialize(registry.getKey(value));
	}

	@Override
	public void deserializeNBT(NBTTagString nbt) {
		ResourceLocation location = NBTSerializable.NBTResourceLocationSerializable.deserialize(nbt);
		if (location == null) {

			value = registry.getValue(emptyNeverToBeFound);
		} else {
			value = registry.getValue(location);
		}
	}
}
