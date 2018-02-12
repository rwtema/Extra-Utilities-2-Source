package com.rwtema.extrautils2;

import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.recipes.GenericMachineRecipe;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nonnull;
import java.util.List;

public class IMCHandler {

	public static void handle(FMLInterModComms.IMCMessage message) {
		try {
			ModContainer modContainer = Loader.instance().getIndexedModList().get(message.getSender());
			switch (message.key) {
				case "addMachineRecipe":
					addMachineRecipe(message.getNBTValue(), modContainer);
					break;
				case "addMachine":
					addMachine(message.getNBTValue(), modContainer);
					break;
			}
		} catch (Throwable err) {
			CrashReport report = new CrashReport("Error while processing IMC message from sender " + message.getSender(), err);
			report.makeCategory("Sender").addCrashSection("ModID", message.getSender());
			CrashReportCategory category = report.makeCategory("IMC Message");
			category.addCrashSection("Message Key", message.key);
			category.addCrashSection("Message Type", message.getMessageType().toString());
			category.addDetail("Message", () -> {
				if (message.isStringMessage()) {
					return message.getStringValue();
				}
				if (message.isItemStackMessage()) {
					return message.getItemStackValue().toString();
				}
				if (message.isNBTMessage()) {
					return message.getNBTValue().toString();
				}
				if (message.isResourceLocationMessage()) {
					return message.getResourceLocationValue().toString();
				}
				if (message.isFunctionMessage()) {
					return "Function:[" + message.getMessageType() + "]";
				}

				throw new IllegalArgumentException(message + " is not supported");
			});
			throw new ReportedException(report);
		}
	}

	public static void addMachine(NBTTagCompound tag, ModContainer modContainer) {
		String machine = tag.getString("machine");

		String energy_mode = tag.getString(tag.getString("energy_mode")).toUpperCase();
		Machine.EnergyMode energyMode = Machine.EnergyMode.valueOf(energy_mode);

		int energyBufferSize = tag.getInteger("energy_buffer");
		int energyTransferLimit = tag.getInteger("energy_transfer");
		int color = tag.hasKey("color") ? tag.getInteger("color") : 0xffffff;
		String textureTop = NBTHelper.getStringOrNull(tag, "texture_top");
		String textureTopOverlay = NBTHelper.getStringOrNull(tag, "texture_top_overlay");
		String textureBase = NBTHelper.getStringOrNull(tag, "texture_base");
		String textureBottom = NBTHelper.getStringOrNull(tag, "texture_bottom");
		String frontTexture = tag.getString("texture_front_off");
		String frontTextureActive = tag.getString("texture_front_on");

		List<MachineSlotItem> itemInputs = NBTHelper.processList(NBTHelper.getTagListAnyType(tag, "item_inputs"), IMCHandler::getItemSlot);
		List<MachineSlotItem> itemOutputs = NBTHelper.processList(NBTHelper.getTagListAnyType(tag, "item_outputs"), IMCHandler::getItemSlot);
		List<MachineSlotFluid> fluidInputs = NBTHelper.processList(NBTHelper.getTagListAnyType(tag, "fluid_inputs"), IMCHandler::getFluidSlot);
		List<MachineSlotFluid> fluidOutputs = NBTHelper.processList(NBTHelper.getTagListAnyType(tag, "fluid_outputs"), IMCHandler::getFluidSlot);


		Machine actualMachine = new Machine(machine, energyBufferSize, energyTransferLimit, itemInputs, fluidInputs, itemOutputs, fluidOutputs, frontTexture, frontTextureActive, energyMode, color, textureTop, textureBase, textureBottom, textureTopOverlay);

		if (tag.hasKey("default_energy", Constants.NBT.TAG_ANY_NUMERIC)) {
			actualMachine.defaultEnergy = tag.getInteger("default_energy");
		}
		if (tag.hasKey("default_time", Constants.NBT.TAG_ANY_NUMERIC)) {
			actualMachine.defaultProcessingTime = tag.getInteger("default_time");
		}
		MachineRegistry.register(actualMachine, modContainer);
	}


	public static MachineSlotItem getItemSlot(NBTBase base) {
		if (base.getId() == Constants.NBT.TAG_STRING) {
			return new MachineSlotItem(((NBTTagString) base).getString());
		}
		NBTTagCompound nbt = (NBTTagCompound) base;
		String name = nbt.getString("name");
		boolean optional = nbt.getBoolean("optional");
		int capacity = nbt.hasKey("capacity") ? nbt.getInteger("capacity") : 64;
		int col = nbt.hasKey("color") ? nbt.getInteger("color") : 0xffffffff;
		String texture = NBTHelper.getStringOrNull(nbt, "background_texture");

		return new MachineSlotItem(name, col, optional, texture, capacity);
	}


	public static MachineSlotFluid getFluidSlot(NBTBase base) {
		if (base.getId() == Constants.NBT.TAG_STRING) {
			return new MachineSlotFluid(((NBTTagString) base).getString());
		}
		NBTTagCompound nbt = (NBTTagCompound) base;
		String name = nbt.getString("name");
		boolean optional = nbt.getBoolean("optional");
		int capacity = nbt.hasKey("capacity") ? nbt.getInteger("capacity") : 64;
		int col = nbt.hasKey("color") ? nbt.getInteger("color") : 0xffffffff;
		FluidStack filterstack = nbt.hasKey("fluid_filter") ? FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fluid_filter")) : null;

		return new MachineSlotFluid(name, capacity, col, optional, filterstack);
	}


	public static void addMachineRecipe(NBTTagCompound tag, ModContainer modContainer) {
		String machineKey = tag.getString("machine");
		Machine machine = MachineRegistry.getMachine(machineKey);
		if (machine == null) return;

		GenericMachineRecipe.Builder builder = new GenericMachineRecipe.Builder(machine);

		boolean hasEnergy = tag.hasKey("energy", Constants.NBT.TAG_ANY_NUMERIC);
		boolean hasRF = tag.hasKey("rf_rate", Constants.NBT.TAG_ANY_NUMERIC);
		boolean hasProcessingTime = tag.hasKey("time", Constants.NBT.TAG_ANY_NUMERIC);

		int energy = tag.getInteger("energy");
		float rf = tag.getFloat("rf_rate");
		int time = tag.getInteger("time");

		if (hasEnergy && hasProcessingTime) {
			builder.setEnergy(energy).setProcessingTime(time);
		} else if (hasEnergy && hasRF) {
			builder.setRFRate(energy, rf);
		} else if (hasProcessingTime && hasRF) {
			builder.setEnergy((int) Math.ceil(time * rf));
			builder.setProcessingTime(time);
		} else {
			if (hasEnergy) {
				builder.setEnergy(energy);
			}
			if (hasProcessingTime) {
				builder.setProcessingTime(time);
			}
		}

		Iterable<MachineSlot<?>> allSlots = Iterables.concat(machine.itemInputs, machine.itemOutputs, machine.fluidInputs, machine.fluidOutputs);
		for (MachineSlot<?> slot : allSlots) {
			if (!slot.optional && !tag.hasKey(slot.name)) {
				throw new IllegalArgumentException("Missing slot " + slot.name + " in tag " + tag.toString());
			}
			if (tag.hasKey(slot.name + "_probability")) {
				builder.setProbability(slot, tag.getFloat(slot.name + "_probability"));
			}
		}

		for (MachineSlotItem slot : machine.itemInputs) {
			String name = slot.name;
			if (!tag.hasKey(name)) {
				continue;
			}
			int amount = -1;
			if (tag.hasKey(name + "_amount", Constants.NBT.TAG_ANY_NUMERIC)) {
				amount = tag.getInteger(name + "_amount");
			}

			NBTBase base = tag.getTag(name);
			if (base.getId() == Constants.NBT.TAG_STRING) {
				if (amount == -1) throw new IllegalArgumentException("Unspecified amount");
				builder.setItemInput(slot, ((NBTTagString) base).getString(), amount);
			} else if (base.getId() == Constants.NBT.TAG_COMPOUND) {
				ItemStack stack = StackHelper.loadFromNBT((NBTTagCompound) base);
				if (amount == -1) amount = StackHelper.getStacksize(stack);
				builder.setItemInput(slot, stack, amount);
			} else if (base.getId() == Constants.NBT.TAG_LIST) {
				List<ItemStack> list = NBTHelper.getItemStackRecipeEntry(base);
				if (amount == -1) throw new IllegalArgumentException("Unspecified amount");
				builder.setItemInput(slot, list, amount);
			} else {
				throw new IllegalArgumentException("Unable to process Item Input " + base);
			}
		}


		for (MachineSlotItem slot : machine.itemOutputs) {
			String name = slot.name;
			if (!tag.hasKey(name)) {
				continue;
			}
			int amount = -1;
			if (tag.hasKey(name + "_amount", Constants.NBT.TAG_ANY_NUMERIC)) {
				amount = tag.getInteger(name + "_amount");
			}

			NBTBase base = tag.getTag(name);
			if (base.getId() == Constants.NBT.TAG_STRING) {
				if (amount == -1) throw new IllegalArgumentException("Unspecified amount");
				builder.setItemOutput(slot, ((NBTTagString) base).getString(), amount);
			} else if (base.getId() == Constants.NBT.TAG_COMPOUND) {
				ItemStack stack = StackHelper.loadFromNBT((NBTTagCompound) base);
				if (amount == -1) amount = StackHelper.getStacksize(stack);
				builder.setItemOutput(slot, stack, amount);
			} else if (base.getId() == Constants.NBT.TAG_LIST) {
				List<ItemStack> list = NBTHelper.getItemStackRecipeEntry(base);
				if (amount == -1) throw new IllegalArgumentException("Unspecified amount");
				builder.setItemOutput(slot, list, amount);
			} else {
				throw new IllegalArgumentException("Unable to process Item Output " + base);
			}
		}

		for (MachineSlotFluid slot : machine.fluidInputs) {
			String name = slot.name;
			if (!tag.hasKey(name)) {
				continue;
			}
			int amount = -1;
			if (tag.hasKey(name + "_amount", Constants.NBT.TAG_ANY_NUMERIC)) {
				amount = tag.getInteger(name + "_amount");
			}

			NBTBase base = tag.getTag(name);
			if (base.getId() == Constants.NBT.TAG_STRING) {
				if (amount == -1) throw new IllegalArgumentException("Unspecified amount");
				builder.setFluidInputFluidName(slot, ((NBTTagString) base).getString(), amount);
			} else if (base.getId() == Constants.NBT.TAG_COMPOUND) {
				FluidStack stack = FluidStack.loadFluidStackFromNBT((NBTTagCompound) base);
				if (amount == -1) amount = stack.amount;
				builder.setFluidInputFluidStack(slot, stack, amount);
			} else if (base.getId() == Constants.NBT.TAG_LIST) {
				List<String> list = NBTHelper.getFluidStackRecipeEntry(base);
				if (amount == -1) throw new IllegalArgumentException("Unspecified amount");
				builder.setFluidInputFluidNameList(slot, list, amount);
			} else {
				throw new IllegalArgumentException("Unable to process Fluid Input " + base);
			}
		}

		for (MachineSlotFluid slot : machine.fluidOutputs) {
			String name = slot.name;
			if (!tag.hasKey(name)) {
				continue;
			}
			int amount = -1;
			if (tag.hasKey(name + "_amount", Constants.NBT.TAG_ANY_NUMERIC)) {
				amount = tag.getInteger(name + "_amount");
			}

			NBTBase base = tag.getTag(name);
			if (base.getId() == Constants.NBT.TAG_STRING) {
				if (amount == -1) throw new IllegalArgumentException("Unspecified amount");
				builder.setFluidOutput(slot, ((NBTTagString) base).getString(), amount);
			} else if (base.getId() == Constants.NBT.TAG_COMPOUND) {
				FluidStack stack = FluidStack.loadFluidStackFromNBT((NBTTagCompound) base);
				if (amount == -1) amount = stack.amount;
				builder.setFluidOutput(slot, stack, amount);
			} else {
				throw new IllegalArgumentException("Unable to process Fluid Output " + base);
			}
		}

		machine.recipes_registry.addRecipe(builder.build(), modContainer);

	}

}
