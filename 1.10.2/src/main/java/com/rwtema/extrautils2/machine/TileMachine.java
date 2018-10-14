package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.fluids.FluidTankBase;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.*;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.IPowerSubType;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.power.energy.XUEnergyStorage;
import com.rwtema.extrautils2.tile.RedstoneState;
import com.rwtema.extrautils2.tile.TileAdvInteractor;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.transfernodes.Upgrade;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.TObjectIntMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.rwtema.extrautils2.machine.BlockMachine.ACTIVE;

public abstract class TileMachine extends TilePower implements ITickable, IDynamicHandler, IPowerSubType {
	public static final int SHAPE = 0;
	static final int ROLLBACK_INTERVAL = 20;
	public Map<MachineSlotFluid, FluidStack> fluidInputMap;
	public Map<MachineSlotItem, ItemStack> itemInputMap;
	public IMachineRecipe curRecipe;
	public int totalTime;
	public int energyOutput;
	public NBTSerializable.NBTEnum<RedstoneState> redstone_state = registerNBT("redstone", new NBTSerializable.NBTEnum<>(RedstoneState.OPERATE_ALWAYS));
	public NBTSerializable.NBTBoolean powered = registerNBT("powered", new NBTSerializable.NBTBoolean());
	public NBTSerializable.Int pulses = registerNBT("pulses", new NBTSerializable.Int());
	int trackEnergy;
	protected XUEnergyStorage storage = new XUEnergyStorage(Integer.MAX_VALUE) {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (simulate) {
				return super.receiveEnergy(maxReceive, true);
			} else {
				int energy = super.receiveEnergy(maxReceive, false);
				trackEnergy += energy;
				return energy;
			}
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (simulate) {
				return super.extractEnergy(maxExtract, true);
			} else {
				int energy = super.extractEnergy(maxExtract, false);
				trackEnergy += energy;
				return energy;
			}
		}
	};
	String type;
	ComponentHandler<ItemStack> savedItems = registerNBT("items", new ComponentHandler<ItemStack>() {
		@Override
		protected NBTTagCompound serialize(ItemStack value) {
			return value.serializeNBT();
		}

		@Override
		protected ItemStack deserialize(NBTTagCompound tag) {
			return StackHelper.loadFromNBT(tag);
		}
	});
	ComponentHandler<FluidStack> savedFluids = registerNBT("fluids", new ComponentHandler<FluidStack>() {
		@Override
		protected NBTTagCompound serialize(FluidStack value) {
			return value.writeToNBT(new NBTTagCompound());
		}

		@Override
		protected FluidStack deserialize(NBTTagCompound tag) {
			return FluidStack.loadFluidStackFromNBT(tag);
		}
	});
	Machine machine;
	float processTime;
	float speed = -1;
	private boolean recalc = true;
	public SingleStackHandlerUpgrades upgrades = registerNBT("upgrades", new SingleStackHandlerUpgrades(EnumSet.of(Upgrade.SPEED)) {
		@Override
		protected void onContentsChanged() {
			TileMachine.this.recalc = true;
			TileMachine.this.markDirty();
			PowerManager.instance.markDirty(TileMachine.this);
		}
	});
	IFluidHandler fluidHandlerOutputs = new IFluidHandler() {
		FluidTankBase[] tanks;
		FluidHandlerConcatenate tankConcatenate;
		IFluidTankProperties[] properties;

		@Nonnull
		public IFluidHandler getTankConcatenate() {
			if (machine == null) return EmptyFluidHandler.INSTANCE;
			if (tankConcatenate == null) {
				tankConcatenate = new FluidHandlerConcatenate(getTanks());
			}
			return tankConcatenate;
		}

		@Nonnull
		public FluidTankBase[] getTanks() {
			if (machine == null) return new FluidTankBase[0];
			if (tanks == null) {
				tanks = new FluidTankBase[machine.fluidOutputs.size()];
				List<MachineSlotFluid> fluidOutputs = machine.fluidOutputs;
				for (int i = 0; i < fluidOutputs.size(); i++) {
					MachineSlotFluid fluidOutput = fluidOutputs.get(i);
					tanks[i] = new FluidTankBase() {
						@Nullable
						@Override
						public FluidStack getFluid() {
							return savedFluids.contents.get(fluidOutput.name);
						}

						@Override
						public void setFluid(@Nullable FluidStack fluid) {
							if (fluid != null) {
								savedFluids.contents.put(fluidOutput.name, fluid);
							} else {
								savedFluids.contents.remove(fluidOutput.name);
							}
							recalc = true;
						}

						@Override
						public int getCapacity() {
							return fluidOutput.stackCapacity;
						}

						@Override
						public boolean canFillFluidType(FluidStack fluid) {
							return false;
						}

						@Override
						public boolean canDrainFluidType(FluidStack fluid) {
							return true;
						}
					};
				}
			}
			return tanks;
		}


		@Override
		@Nonnull
		public IFluidTankProperties[] getTankProperties() {
			if (machine == null) return new IFluidTankProperties[0];
			if (properties == null) {
				properties = new IFluidTankProperties[machine.fluidOutputs.size()];

				FluidTankBase[] tanks = getTanks();
				for (int i = 0; i < tanks.length; i++) {
					properties[i] = tanks[i].getTankProperties()[0];
				}
			}
			return properties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			return 0;
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			return getTankConcatenate().drain(resource, doDrain);
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			return getTankConcatenate().drain(maxDrain, doDrain);
		}
	};
	IFluidHandler fluidHandlerInputs = new IFluidHandler() {
		FluidTankBase[] tanks;
		FluidHandlerConcatenate tankConcatenate;
		IFluidTankProperties[] properties;

		public IFluidHandler getTankConcatenate() {
			if (machine == null) return EmptyFluidHandler.INSTANCE;
			if (tankConcatenate == null) {
				tankConcatenate = new FluidHandlerConcatenate(getTanks());
			}
			return tankConcatenate;
		}

		public FluidTankBase[] getTanks() {
			if (machine == null) return new FluidTankBase[0];
			if (tanks == null) {
				tanks = new FluidTankBase[machine.fluidInputs.size()];
				List<MachineSlotFluid> fluidInputs = machine.fluidInputs;
				for (int i = 0; i < fluidInputs.size(); i++) {
					MachineSlotFluid fluidInput = fluidInputs.get(i);
					int finalI = i;
					tanks[i] = new FluidTankBase() {
						@Nullable
						@Override
						public FluidStack getFluid() {
							return savedFluids.contents.get(fluidInput.name);
						}

						@Override
						public void setFluid(@Nullable FluidStack fluid) {
							if (fluid != null) {
								savedFluids.contents.put(fluidInput.name, fluid);
							} else {
								savedFluids.contents.remove(fluidInput.name);
							}
							recalc = true;
						}

						@Override
						public int getCapacity() {
							return fluidInput.stackCapacity;
						}

						@Override
						public boolean canFillFluidType(FluidStack fluid) {
							return fluidInput.matchesFluidInput(fluid) && isValidFluidInput(finalI, fluid);
						}

						@Override
						public boolean canDrainFluidType(FluidStack fluid) {
							return !(fluidInput.matchesFluidInput(fluid) && isValidFluidInput(finalI, fluid));
						}
					};
				}
			}
			return tanks;
		}

		@Override
		@Nonnull
		public IFluidTankProperties[] getTankProperties() {
			if (machine == null) return new IFluidTankProperties[0];
			if (properties == null) {
				properties = new IFluidTankProperties[machine.fluidInputs.size()];
				List<MachineSlotFluid> fluidInputs = machine.fluidInputs;
				FluidTankBase[] tanks = getTanks();
				for (int i = 0; i < fluidInputs.size(); i++) {
					properties[i] = tanks[i].getTankProperties()[0];
				}
			}
			return properties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			return getTankConcatenate().fill(resource, doFill);
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			return getTankConcatenate().drain(resource, doDrain);
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			return getTankConcatenate().drain(maxDrain, doDrain);
		}
	};
	IItemHandlerModifiable itemHandlerOutputs = new ItemStackHandlerBase() {
		@Override
		@ItemStackNonNull
		public ItemStack getStack(int slot) {
			if (slot < 0) return StackHelper.empty();
			Machine machine = TileMachine.this.machine;
			if (machine == null || slot >= machine.itemOutputs.size())
				return StackHelper.empty();

			MachineSlotItem slotItem = machine.itemOutputs.get(slot);
			String name = slotItem.name;
			return StackHelper.safeCopy(savedItems.contents.getOrDefault(name, StackHelper.empty()));
		}

		@Override
		public void setStack(int slot, ItemStack stack) {
			if (slot < 0) return;
			Machine machine = TileMachine.this.machine;
			if (machine == null || slot >= machine.itemOutputs.size())
				return;

			recalc = true;

			MachineSlotItem slotItem = machine.itemOutputs.get(slot);
			String name = slotItem.name;
			if (StackHelper.isNull(stack))
				savedItems.contents.remove(name);
			else
				savedItems.contents.put(name, stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			TileMachine.this.markDirty();
			recalc = true;
		}

		@ItemStackNonNull
		@Override
		public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
			return stack;
		}

		@Override
		public int getSlots() {
			Machine machine = TileMachine.this.machine;
			if (machine == null)
				return 0;
			return machine.itemOutputs.size();
		}
	};
	IItemHandlerModifiable itemHandlerInputs = new ItemStackHandlerBase() {
		@Override
		@ItemStackNonNull
		public ItemStack getStack(int slot) {
			if (slot < 0) return StackHelper.empty();
			Machine machine = TileMachine.this.machine;
			if (machine == null || slot >= machine.itemInputs.size())
				return StackHelper.empty();

			MachineSlotItem slotItem = machine.itemInputs.get(slot);
			String name = slotItem.name;
			return StackHelper.safeCopy(savedItems.contents.getOrDefault(name, StackHelper.empty()));
		}

		@Override
		public void setStack(int slot, ItemStack stack) {
			if (slot < 0) return;
			Machine machine = TileMachine.this.machine;
			if (machine == null || slot >= machine.itemInputs.size())
				return;

			recalc = true;

			MachineSlotItem slotItem = machine.itemInputs.get(slot);
			String name = slotItem.name;
			if (StackHelper.isNull(stack))
				savedItems.contents.remove(name);
			else
				savedItems.contents.put(name, stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			TileMachine.this.markDirty();
			recalc = true;
		}

		@Override
		public int getSlots() {
			Machine machine = TileMachine.this.machine;
			if (machine == null)
				return 0;
			return machine.itemInputs.size();
		}

		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			if (!isValidItemInput(slot, stack)) {
				return 0;
			}

			return Math.min(machine.itemInputs.get(slot).stackCapacity, stack.getMaxStackSize());
		}
	};
	private IItemHandler itemHandlerPublic = ConcatItemHandler.concatNonNull(
			new IItemHandlerModifiableCompat() {
				@Override
				public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
					itemHandlerInputs.setStackInSlot(slot, stack);
					recalc = true;
				}

				@Override
				public int getSlots() {
					return itemHandlerInputs.getSlots();
				}

				@ItemStackNonNull
				@Override
				public ItemStack getStackInSlot(int slot) {
					return itemHandlerInputs.getStackInSlot(slot);
				}

				@ItemStackNonNull
				@Override
				public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
					if (!simulate)
						recalc = true;
					return itemHandlerInputs.insertItem(slot, stack, simulate);
				}

				@ItemStackNonNull
				@Override
				public ItemStack extractItem(int slot, int amount, boolean simulate) {
					if (amount == 0) return StackHelper.empty();
					if (!simulate)
						recalc = true;
					ItemStack stack = itemHandlerInputs.getStackInSlot(slot);
					if (StackHelper.isNull(stack) || isValidItemInput(slot, stack)) {
						return StackHelper.empty();
					}
					return itemHandlerInputs.extractItem(slot, amount, simulate);
				}
			},
			itemHandlerOutputs

	);
	private IFluidHandler fluidHandlerPublic = new FluidHandlerConcatenate(fluidHandlerInputs, fluidHandlerOutputs);
	private boolean processing;
	private int processingCooldown = 0;

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		boolean wasPowered = powered.value;
		boolean newPower = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;
		if (newPower != wasPowered) {
			powered.value = newPower;
			if (newPower && redstone_state.value == RedstoneState.OPERATE_REDSTONE_PULSE) {
				pulses.value++;
			}
		}

	}

	@Nullable
	@Override
	public IFluidHandler getFluidHandler(EnumFacing facing) {
		return fluidHandlerPublic;
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return itemHandlerPublic;
	}

	public boolean isValidFluidInput(int slot, FluidStack stack) {
		if (machine == null || slot >= machine.fluidInputs.size())
			return false;

		MachineSlotFluid slotFluid = machine.fluidInputs.get(slot);

		buildInputMaps();

		for (IMachineRecipe recipe : machine.recipes_registry) {
			if (recipe.allowInputFluid(slotFluid, stack, itemInputMap, fluidInputMap)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValidItemInput(int slot, ItemStack stack) {
		if (machine == null || slot >= machine.itemInputs.size())
			return false;

		MachineSlotItem slotItem = machine.itemInputs.get(slot);

		buildInputMaps();

		for (IMachineRecipe recipe : machine.recipes_registry) {
			if (recipe.allowInputItem(slotItem, stack, itemInputMap, fluidInputMap)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);
		type = "blank";
		if (StackHelper.isNonNull(stack) && stack.hasTagCompound()) {
			type = Validate.notNull(stack.getTagCompound()).getString("Type");
		}
		machine = MachineRegistry.getMachine(type);
		storage.setCapacity(machine == null ? 0 : machine.energyBufferSize);
		storage.setMaxTransfer(machine == null ? 0 : machine.energyBufferSize);
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		packet.writeString(type);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		String s = packet.readString();
		if (type == null || !type.equals(s)) {
			type = s;
			machine = MachineRegistry.getMachine(type);
			world.markBlockRangeForRenderUpdate(
					pos.getX(), pos.getY(), pos.getZ(),
					pos.getX(), pos.getY(), pos.getZ());
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		if (type != null)
			nbt.setString("Type", type);
		nbt.setInteger("Energy", storage.getEnergyStored());
		nbt.setFloat("ProcessTime", processTime);
		nbt.setBoolean("Processing", processing);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		storage.setCapacity(Integer.MAX_VALUE);
		super.readFromNBT(nbt);
		type = nbt.getString("Type");
		machine = MachineRegistry.getMachine(type);
		storage.setCapacity(machine == null ? 0 : machine.energyBufferSize);
		storage.setCapacity(machine == null ? 0 : machine.energyBufferSize);
		storage.setEnergyStored(nbt.getInteger("Energy"));
		processTime = nbt.getFloat("ProcessTime");
		processing = nbt.getBoolean("Processing");
	}

	public void setActive() {
		processingCooldown = 100;
		if (!processing)
			world.setBlockState(pos, getBlockState().withProperty(ACTIVE, true), 2);
		processing = true;


	}

	public void setInactive() {
		processing = false;
	}

	@Override
	public void update() {
		Machine machine = this.machine;
		if (machine == null) {
			return;
		}

		if (speed == -1 || (world.getTotalWorldTime() % 100) == 0) {
			speed = machine.getSpeed(world, pos, this);
		}


		if (world.isRemote) {
			machine.clientTick(this, getBlockState().getValue(ACTIVE));
			return;
		}

		if (!processing && processingCooldown > 0) {
			processingCooldown--;
			if (processingCooldown == 0)
				world.setBlockState(pos, getBlockState().withProperty(ACTIVE, false), 2);
		}

		process();
	}

	public abstract void process();

	public void processRecipeInput() {
		if (recalc) {
			recalc = false;

			buildInputMaps();

			IMachineRecipe prevRecipe = this.curRecipe;
			curRecipe = null;
			recipeSearch:
			for (IMachineRecipe recipe : machine.recipes_registry) {
				if (recipe.matches(itemInputMap, fluidInputMap)) {
					Map<MachineSlotItem, ItemStack> itemOutputs = recipe.getItemOutputs(itemInputMap, fluidInputMap);
					Map<MachineSlotFluid, FluidStack> fluidOutputs = recipe.getFluidOutputs(itemInputMap, fluidInputMap);
					for (MachineSlotItem slot : machine.itemOutputs) {
						ItemStack stack = itemOutputs.getOrDefault(slot, StackHelper.empty());
						if (StackHelper.isNonNull(stack)) {
							ItemStack curStack = savedItems.contents.get(slot.name);
							if (StackHelper.isNonNull(curStack) && (!ItemHandlerHelper.canItemStacksStack(stack, curStack) || StackHelper.getStacksize(stack) + StackHelper.getStacksize(curStack) > Math.min(slot.stackCapacity, stack.getMaxStackSize()))) {
								continue recipeSearch;
							}
						}
					}
					for (MachineSlotFluid slot : machine.fluidOutputs) {
						FluidStack fluidStack = fluidOutputs.get(slot);
						if (fluidStack != null) {
							FluidStack fluidStack1 = savedFluids.contents.get(slot.name);
							if (fluidStack1 != null && (!fluidStack.isFluidEqual(fluidStack1) || fluidStack.amount + fluidStack1.amount > slot.stackCapacity)) {
								continue recipeSearch;
							}
						}
					}

					this.curRecipe = recipe;
					break;
				}
			}
			if (curRecipe == null || prevRecipe != curRecipe) {
				processTime = 0;
			}
		}
	}

	public void buildInputMaps() {
		ImmutableMap.Builder<MachineSlotItem, ItemStack> itemBuilder = ImmutableMap.builder();
		for (MachineSlotItem itemInput : machine.itemInputs) {
			ItemStack stack = savedItems.contents.get(itemInput.name);

			if (StackHelper.isNonNull(stack)) {
				itemBuilder.put(itemInput, stack);
			} else if (StackHelper.empty() != null) {
				itemBuilder.put(itemInput, StackHelper.empty());
			}
		}
		itemInputMap = itemBuilder.build();

		ImmutableMap.Builder<MachineSlotFluid, FluidStack> fluidBuilder = ImmutableMap.builder();
		for (MachineSlotFluid fluidInput : machine.fluidInputs) {
			FluidStack stack = savedFluids.contents.get(fluidInput.name);

			if (stack != null)
				fluidBuilder.put(fluidInput, stack);
		}
		fluidInputMap = fluidBuilder.build();
	}

	protected void consumeInputs() {
		TObjectIntMap<MachineSlot> amountToConsume = curRecipe.getAmountToConsume(itemInputMap, fluidInputMap);
		Map<MachineSlotItem, ItemStack> itemOutputs = curRecipe.getItemOutputs(itemInputMap, fluidInputMap);
		Map<MachineSlotFluid, FluidStack> fluidOutputs = curRecipe.getFluidOutputs(itemInputMap, fluidInputMap);
		Map<MachineSlotItem, ItemStack> containerItems = curRecipe.getContainerItems(itemInputMap, fluidInputMap);
		TObjectFloatMap<MachineSlot> probabilityModifier = curRecipe.getProbabilityModifier(itemInputMap, fluidInputMap);

		for (MachineSlotItem slot : machine.itemInputs) {
			int i = amountToConsume.get(slot);
			if (i == 0) continue;

			ItemStack stack = savedItems.contents.get(slot.name);
			if (StackHelper.isNull(stack)) continue;
			StackHelper.decrease(stack, i);
			if (StackHelper.getStacksize(stack) <= 0) {
				savedItems.contents.put(slot.name, containerItems.getOrDefault(slot, StackHelper.empty()));
			}
		}

		for (MachineSlotFluid slot : machine.fluidInputs) {
			int i = amountToConsume.get(slot);
			if (i == 0) continue;

			FluidStack stack = savedFluids.contents.get(slot.name);
			if (stack == null) continue;
			stack.amount -= i;
			if (stack.amount <= 0) {
				savedFluids.contents.put(slot.name, null);
			}
		}


		for (MachineSlotItem slot : machine.itemOutputs) {
			ItemStack stack = itemOutputs.getOrDefault(slot, StackHelper.empty());
			if (StackHelper.isNull(stack)) continue;
			if (probabilityModifier != null && probabilityModifier.containsKey(slot)) {
				float v = probabilityModifier.get(slot);
				int n = StackHelper.getStacksize(stack);
				for (int i = 0; i < n; i++) {
					if (world.rand.nextFloat() > v) {
						StackHelper.decrease(stack);
					}
				}
			}

			if (StackHelper.isEmpty(stack)) continue;

			ItemStack itemStack = savedItems.contents.get(slot.name);
			if (StackHelper.isNull(itemStack)) {
				itemStack = stack.copy();
				savedItems.contents.put(slot.name, itemStack);
			} else {
				StackHelper.increase(itemStack, StackHelper.getStacksize(stack));
			}

			StackHelper.setStackSize(itemStack, Math.min(StackHelper.getStacksize(itemStack), Math.min(itemStack.getMaxStackSize(), slot.stackCapacity)));
		}

		for (MachineSlotFluid slot : machine.fluidOutputs) {
			FluidStack stack = fluidOutputs.get(slot);
			if (stack == null) continue;
			FluidStack fluidStack = savedFluids.contents.get(slot.name);
			if (fluidStack == null) {
				fluidStack = stack.copy();
				savedFluids.contents.put(slot.name, fluidStack);
			} else {
				fluidStack.amount += stack.amount;
			}
			fluidStack.amount = Math.min(fluidStack.amount, slot.stackCapacity);
		}

		recalc = true;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (machine == null) return null;
		return new ContainerMachine(this, player);
	}

	@Override
	public Optional<ItemStack> getPickBlock(EntityPlayer player, RayTraceResult target) {
		return Optional.of(XU2Entries.machineEntry.value.createStack(machine));
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return Iterables.concat(
				InventoryHelper.getItemHandlerIterator(itemHandlerPublic),
				InventoryHelper.getItemHandlerIterator(upgrades)
		);
	}

	@Override
	public float getPower() {
		int level = upgrades.getLevel(Upgrade.SPEED);
		if (level == 0) return Float.NaN;
		return Upgrade.SPEED.getPowerUse(level);
	}

	@Nullable
	@Override
	public abstract IEnergyStorage getEnergyHandler(EnumFacing facing);

	@Override
	public Collection<ResourceLocation> getTypes() {
		if (machine == null) return ImmutableList.of();
		return ImmutableList.of(machine.location);
	}

	public boolean isProcessing() {
		return processing || processingCooldown > 90;
	}

	public static class ContainerMachine extends DynamicContainerTile {
		protected final EntityPlayer player;
		public TileMachine machine;

		public ContainerMachine(final TileMachine machine, EntityPlayer player) {
			super(machine);
			this.player = player;

			addWidget(new WidgetBase(0, 16, 50, 50) {
				@Override
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					x = (width - w) / 2;
					manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					GlStateManager.enableBlend();

					Machine machine1 = machine.machine;
					int color = machine1.color;
					GlStateManager.color(ColorHelper.getRF(color), ColorHelper.getGF(color), ColorHelper.getBF(color), 1);
					gui.drawTexturedModalRect(guiLeft + x, guiTop + y, Textures.getSprite(machine1.textureTop != null ? machine1.textureTop : "extrautils2:machine/machine_base"), w, h);
					GlStateManager.color(1, 1, 1, 1);
					String tex = machine.getBlockState().getValue(ACTIVE) ? machine1.frontTextureActive : machine1.frontTexture;
					gui.drawTexturedModalRect(guiLeft + x, guiTop + y, Textures.getSprite(tex), w, h);
					manager.bindTexture(DynamicContainer.texBackground);
					GlStateManager.color(1, 1, 1, 0.9F);
					gui.drawTexturedModalRect(guiLeft + x, guiTop + y, 128 - w / 2, 128 - h / 2, w, h);
				}
			});

			addTitle(BlockMachine.getDisplayName(machine.machine), false);


			int w = (machine.itemHandlerInputs.getSlots() + machine.machine.fluidInputs.size() + machine.itemHandlerOutputs.getSlots() + machine.machine.fluidOutputs.size()) * (4 + 18) + 4 + 22;

			int x = Math.max(4, centerX - (w / 2));
			int y = 32;


			for (MachineSlotFluid slotFluid : machine.machine.fluidInputs) {
				IWidget fluidTank;
				addWidget(fluidTank = new WidgetFluidBase(x, y + 9 - WidgetFluidBase.uh[SHAPE] / 2, SHAPE) {
					@Override
					protected int getCapacity() {
						return slotFluid.stackCapacity;
					}

					@Override
					protected FluidStack getCurrentFluid() {
						return machine.savedFluids.contents.get(slotFluid.name);
					}
				});
				x += fluidTank.getW() + 4;
			}

			for (int i = 0; i < machine.itemHandlerInputs.getSlots(); i++) {
				addWidget(new WidgetSlotItemHandler(machine.itemHandlerInputs, i, x, y) {
					@Override
					public void onSlotChanged() {
						super.onSlotChanged();
						machine.recalc = true;
					}
				});
				x += 4 + 18;
			}


			WidgetProgressArrowTimer arrowTimer = new WidgetProgressArrowTimer(x, y) {
				@Override
				protected float getTime() {
					return machine.processTime / (float) (1 + machine.upgrades.getLevel(Upgrade.SPEED));
				}

				@Override
				protected float getMaxTime() {
					if (!machine.active) return -1;
					if (machine.speed == 0) return -1;
					return machine.totalTime / (float) (1 + machine.upgrades.getLevel(Upgrade.SPEED));
				}

				@Override
				public List<String> getErrorMessage() {
					String runError = machine.machine.getRunError(machine.world(), machine.getLocation(), machine, machine.speed);
					if (runError != null) {
						return ImmutableList.of(runError);
					}
					return ImmutableList.of(Lang.translate("Grid is overloaded"));
				}
			};
			addWidget(arrowTimer);
			addWidget(arrowTimer.getJEIWidget("xu2_machine_" + machine.machine.name));

			x += 4 + 22;

			for (int i = 0; i < machine.itemHandlerOutputs.getSlots(); i++) {
				addWidget(new WidgetSlotItemHandler(machine.itemHandlerOutputs, i, x, y));
				x += 18;
				x += 4;
			}

			for (MachineSlotFluid slotFluid : machine.machine.fluidOutputs) {
				IWidget fluidTank;
				addWidget(fluidTank = new WidgetFluidBase(x, y + 9 - WidgetFluidBase.uh[SHAPE] / 2, SHAPE) {
					@Override
					protected int getCapacity() {
						return slotFluid.stackCapacity;
					}

					@Override
					protected FluidStack getCurrentFluid() {
						return machine.savedFluids.contents.get(slotFluid.name);
					}
				});
				x += fluidTank.getW() + 4;
			}

			crop(4);
			if (this.width < playerInvWidth + 8) {
				this.width = playerInvWidth + 8;
			}
			addWidget(new WidgetEnergyStorage(width - 24, y - 16, machine.storage));
			crop(4);

			addWidget(machine.upgrades.getSpeedUpgradeSlot(4, y - 16));
			addWidget(TileAdvInteractor.getRSWidget(4, y - 16 + 19, machine.redstone_state, machine.pulses));

			cropAndAddPlayerSlots(player.inventory);
			validate();
			this.machine = machine;
		}

		@Override
		public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
			return super.transferStackInSlot(par1EntityPlayer, par2);
		}

		@Override
		public void onSlotChanged(int index) {
			super.onSlotChanged(index);
		}
	}

	public abstract class ComponentHandler<T> implements INBTSerializable<NBTTagCompound> {
		public HashMap<String, T> contents = new HashMap<>();

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			for (Map.Entry<String, T> entry : contents.entrySet()) {
				T value = entry.getValue();
				if (value != null) {
					tag.setTag(entry.getKey(), serialize(value));
				}
			}

			return tag;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			contents.clear();
			for (String s : nbt.getKeySet()) {
				contents.put(s, deserialize(nbt.getCompoundTag(s)));
			}
		}

		protected abstract NBTTagCompound serialize(T value);

		protected abstract T deserialize(NBTTagCompound tag);
	}

//	@Override
//	public int updateAccelerable() {
//		trackEnergy = 0;
//		process();
//		return trackEnergy;
//	}
}
