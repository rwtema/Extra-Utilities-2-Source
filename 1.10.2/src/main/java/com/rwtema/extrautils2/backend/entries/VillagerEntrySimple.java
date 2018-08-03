package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.compatibility.CompatHelper112;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public abstract class VillagerEntrySimple extends VillagerEntry<VillagerRegistry.VillagerProfession> {
	private final String villager_name;
	private String texture;

	public VillagerEntrySimple(String name) {
		this(name, name);
	}

	public VillagerEntrySimple(String name, String texture) {
		super("villager_" + name);
		this.villager_name = name;
		this.texture = texture;
	}

	@Override
	public VillagerRegistry.VillagerProfession initValue() {
		return CompatHelper112.getVillagerProfession(villager_name, texture);
	}

}
