package com.rwtema.extrautils2.dimensions;


import com.rwtema.extrautils2.backend.entries.Entry;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;

public class DimensionEntry extends Entry<DimensionType> {
	final String name;
	final String suffix;
	final Class<? extends WorldProvider> provider;
	final boolean keepLoaded;
	public int id;

	public DimensionEntry(String name, int id, Class<? extends WorldProvider> provider, boolean keepLoaded) {
		this(name, "_" + Lang.stripText(name), id, provider, keepLoaded);
	}

	public DimensionEntry(String name, String suffix, int id, Class<? extends WorldProvider> provider, boolean keepLoaded) {
		super(name);
		this.name = name;
		this.suffix = suffix;
		this.id = id;
		this.provider = provider;
		this.keepLoaded = keepLoaded;
	}

	@Override
	protected DimensionType initValue() {
		return DimensionType.register(name, suffix, id, provider, keepLoaded);
	}

	@Override
	public void loadAdditionalConfig(Configuration config) {
		id = config.get("Dimension IDs", name, id).getInt();
		if (id == 0) {
			enabled = false;
		}
	}

	@Override
	public void init() {
		DimensionManager.registerDimension(id, value);
	}

	public Teleporter createTeleporter(WorldServer destWorld, int dest, int curDim) {
		return new TeleporterBase(destWorld, dest, curDim);
	}
}
