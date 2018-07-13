package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.villagers.XUVillagerCareer;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.List;

public abstract class VillagerEntry<T extends VillagerRegistry.VillagerProfession> extends Entry<T> {
	public List<XUVillagerCareer> careers;

	public VillagerEntry(String name) {
		super(name);
	}

	@Override
	public String getDisplayName(int meta) {
		return I18n.translateToLocal("entity.Villager." + careers.get(meta % careers.size()).getName());
	}

	@Override
	public void postInit() {
		CompatHelper112.register(value);
		careers = getCareers();
	}


	public abstract List<XUVillagerCareer> getCareers();

	public XUVillagerCareer newCareer(String name) {
		return new XUVillagerCareer(value, name);
	}
}
