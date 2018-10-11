package com.rwtema.extrautils2.tweaker;

import com.rwtema.extrautils2.RunnableClient;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.backend.model.Textures;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@ZenRegister
@ZenClass(XUTweaker.PACKAGE_NAME_BASE + "IMachineRegistry")
public class IMachineRegistry {


	@ZenMethod
	public static IMachine createNewMachine(@Nonnull String name,
											int energyBufferSize,
											int energyTransferLimit,
											List<IMachineSlot> inputSlots,
											List<IMachineSlot> outputSlots,
											@Nonnull String frontTexture,
											@Nonnull String frontTextureActive) {
		return createNewMachine(name, energyBufferSize, energyTransferLimit, inputSlots, outputSlots, frontTexture, frontTextureActive, 0xffffff);
	}

	@ZenMethod
	public static IMachine createNewMachine(@Nonnull String name,
											int energyBufferSize,
											int energyTransferLimit,
											List<IMachineSlot> inputSlots,
											List<IMachineSlot> outputSlots,
											@Nonnull String frontTexture,
											@Nonnull String frontTextureActive,
											int color) {
		return createNewMachine(name, energyBufferSize, energyTransferLimit, inputSlots, outputSlots, frontTexture, frontTextureActive, color, null, null, null, null, Machine.EnergyMode.USES_ENERGY);
	}

	@ZenMethod
	public static IMachine createNewGenerator(@Nonnull String name,
											  int energyBufferSize,
											  int energyTransferLimit,
											  List<IMachineSlot> inputSlots,
											  List<IMachineSlot> outputSlots,
											  @Nonnull String frontTexture,
											  @Nonnull String frontTextureActive) {
		return createNewGenerator(name, energyBufferSize, energyTransferLimit, inputSlots, outputSlots, frontTexture, frontTextureActive, 0xffffff);
	}

	@ZenMethod
	public static IMachine createNewGenerator(@Nonnull String name,
											  int energyBufferSize,
											  int energyTransferLimit,
											  List<IMachineSlot> inputSlots,
											  List<IMachineSlot> outputSlots,
											  @Nonnull String frontTexture,
											  @Nonnull String frontTextureActive,
											  int color) {
		return createNewMachine(name, energyBufferSize, energyTransferLimit, inputSlots, outputSlots, frontTexture, frontTextureActive, color, null, null, null, null, Machine.EnergyMode.GENERATES_ENERGY);
	}

	public static IMachine createNewMachine(@Nonnull String name,
											int energyBufferSize,
											int energyTransferLimit,
											List<IMachineSlot> inputSlots,
											List<IMachineSlot> outputSlots,
											@Nonnull String frontTexture,
											@Nonnull String frontTextureActive,
											int color,
											@Nullable String textureTop,
											@Nullable String textureBase,
											@Nullable String textureBottom,
											@Nullable String textureTopOverlay, Machine.EnergyMode energyMode) {
		if (name.indexOf(':') == -1) {
			name = "crafttweaker:" + name;
		}

		List<MachineSlotItem> itemInputs = inputSlots.stream().map(ObjWrapper::getInternal).filter(s -> s instanceof MachineSlotItem).map(s -> (MachineSlotItem) s).collect(Collectors.toList());
		@Nonnull List<MachineSlotFluid> fluidInputs = inputSlots.stream().map(ObjWrapper::getInternal).filter(s -> s instanceof MachineSlotFluid).map(s -> (MachineSlotFluid) s).collect(Collectors.toList());
		;
		@Nonnull List<MachineSlotItem> itemOutputs = outputSlots.stream().map(ObjWrapper::getInternal).filter(s -> s instanceof MachineSlotItem).map(s -> (MachineSlotItem) s).collect(Collectors.toList());
		@Nonnull List<MachineSlotFluid> fluidOutputs = outputSlots.stream().map(ObjWrapper::getInternal).filter(s -> s instanceof MachineSlotFluid).map(s -> (MachineSlotFluid) s).collect(Collectors.toList());
		;
		Machine machine = new Machine(name, energyBufferSize, energyTransferLimit, itemInputs, fluidInputs, itemOutputs, fluidOutputs, frontTexture, frontTextureActive, energyMode, color, textureTop, textureBase, textureBottom, textureTopOverlay);
		GenericAction.run(() -> MachineRegistry.register(machine), "Creating new machine: " + machine.name);
		new RunnableClient() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				Textures.register(frontTexture, frontTextureActive, textureBase, textureBottom, textureTopOverlay, textureTop);
			}
		}.run();

		return new IMachine(machine);
	}

	@ZenMethod
	public static IMachine getMachine(String name) {
		if (name.indexOf(':') == -1) {
			name = "crafttweaker:" + name;
		}
		Machine machine = MachineRegistry.getMachine(name);
		if (machine == null) return null;
		return new IMachine(machine);
	}

	@ZenMethod
	public static List<IMachine> getRegisteredMachineNames() {
		return MachineRegistry.getMachineValues().stream().map(IMachine::new).collect(Collectors.toList());
	}

	@ZenGetter("crusher")
	public static IMachine getCrusher() {
		return new IMachine(XUMachineCrusher.INSTANCE);
	}

	@ZenGetter("enchanter")
	public static IMachine getEnchanter() {
		return new IMachine(XUMachineEnchanter.INSTANCE);
	}

	@ZenGetter("generator_furnace")
	public static IMachine getFurnaceGenerator() {
		return new IMachine(XUMachineGenerators.FURNACE_GENERATOR);
	}


	@ZenGetter("generator_survivalist")
	public static IMachine getSurvivalistGenerator() {
		return new IMachine(XUMachineGenerators.SURVIVALIST_GENERATOR);
	}


	@ZenGetter("generator_culinary")
	public static IMachine getCulinaryGenerator() {
		return new IMachine(XUMachineGenerators.CULINARY_GENERATOR);
	}


	@ZenGetter("generator_potion")
	public static IMachine getPotionGenerator() {
		return new IMachine(XUMachineGenerators.POTION_GENERATOR);
	}


	@ZenGetter("generator_tnt")
	public static IMachine getTntGenerator() {
		return new IMachine(XUMachineGenerators.TNT_GENERATOR);
	}


	@ZenGetter("generator_lava")
	public static IMachine getLavaGenerator() {
		return new IMachine(XUMachineGenerators.LAVA_GENERATOR);
	}


	@ZenGetter("generator_pink")
	public static IMachine getPinkGenerator() {
		return new IMachine(XUMachineGenerators.PINK_GENERATOR);
	}


	@ZenGetter("generator_netherstar")
	public static IMachine getNetherstarGenerator() {
		return new IMachine(XUMachineGenerators.NETHERSTAR_GENERATOR);
	}


	@ZenGetter("generator_ender")
	public static IMachine getEnderGenerator() {
		return new IMachine(XUMachineGenerators.ENDER_GENERATOR);
	}


	@ZenGetter("generator_redstone")
	public static IMachine getRedstoneGenerator() {
		return new IMachine(XUMachineGenerators.REDSTONE_GENERATOR);
	}


	@ZenGetter("generator_overclock")
	public static IMachine getOverclockGenerator() {
		return new IMachine(XUMachineGenerators.OVERCLOCK_GENERATOR);
	}


	@ZenGetter("generator_dragon")
	public static IMachine getDragonGenerator() {
		return new IMachine(XUMachineGenerators.DRAGON_GENERATOR);
	}


	@ZenGetter("generator_ice")
	public static IMachine getIceGenerator() {
		return new IMachine(XUMachineGenerators.ICE_GENERATOR);
	}


	@ZenGetter("generator_death")
	public static IMachine getDeathGenerator() {
		return new IMachine(XUMachineGenerators.DEATH_GENERATOR);
	}


	@ZenGetter("generator_enchant")
	public static IMachine getEnchantGenerator() {
		return new IMachine(XUMachineGenerators.ENCHANT_GENERATOR);
	}


	@ZenGetter("generator_slime")
	public static IMachine getSlimeGenerator() {
		return new IMachine(XUMachineGenerators.SLIME_GENERATOR);
	}


}
