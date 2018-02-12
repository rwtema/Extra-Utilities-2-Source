package com.rwtema.extrautils2.api.machine;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Set;

public class MachineRegistry {
	private final static LinkedHashMap<String, Machine> machines;
	private static Logger logger = LogManager.getLogger("ExtraMachinaAPI");

	static {
		machines = new LinkedHashMap<>();
		registerInternal(XUMachineFurnace.INSTANCE);
		registerInternal(XUMachineCrusher.INSTANCE);
		registerInternal(XUMachineEnchanter.INSTANCE);
		registerInternal(XUMachineGenerators.SURVIVALIST_GENERATOR);
		registerInternal(XUMachineGenerators.FURNACE_GENERATOR);
		registerInternal(XUMachineGenerators.CULINARY_GENERATOR);
		registerInternal(XUMachineGenerators.LAVA_GENERATOR);
		registerInternal(XUMachineGenerators.REDSTONE_GENERATOR);
		registerInternal(XUMachineGenerators.ENDER_GENERATOR);
		registerInternal(XUMachineGenerators.POTION_GENERATOR);
		registerInternal(XUMachineGenerators.PINK_GENERATOR);
		registerInternal(XUMachineGenerators.OVERCLOCK_GENERATOR);
		registerInternal(XUMachineGenerators.TNT_GENERATOR);
		registerInternal(XUMachineGenerators.NETHERSTAR_GENERATOR);
		registerInternal(XUMachineGenerators.DRAGON_GENERATOR);
		registerInternal(XUMachineGenerators.ICE_GENERATOR);
		registerInternal(XUMachineGenerators.DEATH_GENERATOR);
		registerInternal(XUMachineGenerators.ENCHANT_GENERATOR);
		registerInternal(XUMachineGenerators.SLIME_GENERATOR);
	}

	public static Machine register(@Nonnull Machine machine) {
		return register(machine, Loader.instance().activeModContainer());
	}

	public static Machine register(@Nonnull Machine machine, ModContainer modContainer) {
		machine.container = modContainer;
		logger.trace("Registering " + machine.name + " from " + machine.container);
		registerMachineDo(machine);
		return machine;
	}

	private static void registerMachineDo(@Nonnull Machine machine) {
		if (machines.containsKey(machine.name))
			throw new RuntimeException(machine.name + " already registered.");
		machines.put(machine.name, machine);
	}

	public static boolean deregister(Machine machine) {
		logger.trace(machine.name + " was deregistered by " + machine.container);
		return machines.remove(machine.name) == machine;
	}

	private static Machine registerInternal(@Nonnull Machine machine) {
		logger.trace("Registering internal machine " + machine.name);
		registerMachineDo(machine);
		return machine;
	}

	@Nullable
	public static Machine getMachine(String machine) {
		return machines.get(machine);
	}

	public static Set<Machine> getMachineValues() {
		return ImmutableSet.copyOf(machines.values());
	}
}
