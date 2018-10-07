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
	private final static LinkedHashMap<String, Machine> machines = new LinkedHashMap<>();
	private static Logger logger = LogManager.getLogger("ExtraMachinaAPI");

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

	@Nullable
	public static Machine getMachine(String machine) {
		return machines.get(machine);
	}

	public static Set<Machine> getMachineValues() {
		return ImmutableSet.copyOf(machines.values());
	}
}
