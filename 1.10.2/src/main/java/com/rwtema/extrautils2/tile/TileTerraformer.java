package com.rwtema.extrautils2.tile;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.biome.BiomeManip;
import com.rwtema.extrautils2.blocks.BlockTerraformer;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.items.ItemBiomeMarker;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.power.energy.XUEnergyStorage;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.TEHelper;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileTerraformer extends TilePower implements ITickable, IDynamicHandler {
	final static int MAX_CONTAINER_RANGE = 4;
	final static int MAX_TRANSFORMATION_RANGE = 64;
	final static int POWER_PER_TICK = 80;
	private static final int TRANSFORM_TIME = 20;

	final NBTSerializable.NBTMutableBlockPos targetPosition = registerNBT("targetpos", new NBTSerializable.NBTMutableBlockPos());
	public XUEnergyStorage energy = registerNBT("energy", new XUEnergyStorage(POWER_PER_TICK * 20));
	ItemBiomeMarker.ItemBiomeHandler targetBiome = registerNBT("biome", new ItemBiomeMarker.ItemBiomeHandler() {
		@Override
		protected void onContentsChanged() {
			markDirty();
		}
	});
	NBTSerializable.Int range = registerNBT("scantime", new NBTSerializable.Int());
	NBTSerializable.Int transformTime = registerNBT("transformtime", new NBTSerializable.Int());
	int findingCooldown = 0;
	NBTSerializable.NBTEnumIntMap<BlockTerraformer.Type> currentStoredEnergy = registerNBT("loadedEnergy", new NBTSerializable.NBTEnumIntMap<>(BlockTerraformer.Type.class));

	EnumSet<BlockTerraformer.Type> present_set = EnumSet.noneOf(BlockTerraformer.Type.class);

	public static boolean isHostile(Biome biome) {
		List<Biome.SpawnListEntry> spawnableList = biome.getSpawnableList(EnumCreatureType.MONSTER);
		return !spawnableList.isEmpty();
	}

	@Nonnull
	public static String getName(BlockTerraformer.Type type) {
		return getStack(type).getDisplayName();
	}

	@Nonnull
	public static ItemStack getStack(BlockTerraformer.Type type) {
		return XU2Entries.terraformer.newStack(1, XU2Entries.terraformer.value.getDefaultState().withProperty(BlockTerraformer.TYPE, type));
	}

	public static TObjectIntHashMap<BlockTerraformer.Type> getTransformationRequirements(Biome current, Biome target) {
		TObjectIntHashMap<BlockTerraformer.Type> map = new TObjectIntHashMap<>(10, 0.5F, 0);
		float tempDiff = target.getTemperature() - current.getTemperature();
		float rainDiff = target.getRainfall() - current.getRainfall();

		if (tempDiff > 0) {
			int v = (int) Math.floor(tempDiff * 15);
			map.adjustOrPutValue(BlockTerraformer.Type.HEATER, v, v);
		} else if (tempDiff < 0) {
			int v = (int) Math.floor(-tempDiff * 15);
			map.adjustOrPutValue(BlockTerraformer.Type.COOLER, v, v);
		}

		if (rainDiff > 0) {
			int v = (int) Math.floor(rainDiff * 20);
			map.adjustOrPutValue(BlockTerraformer.Type.HUMIDIFIER, v, v);
		} else if (rainDiff < 0) {
			int v = (int) Math.floor(-rainDiff * 20);
			map.adjustOrPutValue(BlockTerraformer.Type.DEHUMIDIFIER, v, v);
		}

		Set<BiomeDictionary.Type> removedTraits = getTraitsDiff(target, current);
		Set<BiomeDictionary.Type> addedTraits = getTraitsDiff(current, target);

		alter(map, addedTraits, removedTraits, BlockTerraformer.Type.HUMIDIFIER, BlockTerraformer.Type.DEHUMIDIFIER, 4, 4, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.WET, BiomeDictionary.Type.LUSH);
		alter(map, addedTraits, removedTraits, BlockTerraformer.Type.DEHUMIDIFIER, BlockTerraformer.Type.HUMIDIFIER, 4, 4, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.DRY, BiomeDictionary.Type.SAVANNA);
		alter(map, addedTraits, removedTraits, BlockTerraformer.Type.MAGIC_INFUSER, BlockTerraformer.Type.MAGIC_INFUSER, 10, 10, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER);

		for (Pair<BlockTerraformer.Type, BlockTerraformer.Type> opposite : BlockTerraformer.OPPOSITES) {
			int l = map.get(opposite.getLeft());
			int r = map.get(opposite.getRight());
			if (l == 0 && r == 0) continue;

			if (l == r) {
				map.remove(opposite.getLeft());
			} else if (l < r) {
				map.remove(opposite.getLeft());
			} else if (r < l) {
				map.remove(opposite.getRight());
			}
		}

		if (!isHostile(target) && isHostile(current)) {
			map.adjustOrPutValue(BlockTerraformer.Type.DEHOSTILIFIER, 10, 10);
		}

		return map;
	}

	public static void alter(TObjectIntHashMap<BlockTerraformer.Type> map, Set<BiomeDictionary.Type> addedTraits, Set<BiomeDictionary.Type> removedTraits, BlockTerraformer.Type plusType, BlockTerraformer.Type removeType, int addAmount, int remove_amount, BiomeDictionary.Type... biomeTypes) {
		for (BiomeDictionary.Type type : biomeTypes) {
			if (addedTraits.contains(type)) {
				map.adjustOrPutValue(plusType, addAmount, addAmount);
			}
			if (removedTraits.contains(type)) {
				map.adjustOrPutValue(removeType, remove_amount, remove_amount);
			}
		}
	}

	public static Set<BiomeDictionary.Type> getTraitsDiff(Biome a, Biome b) {
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for (BiomeDictionary.Type type : CompatHelper.getTypesForBiome(b)) {
			set.add(type);
		}
		for (BiomeDictionary.Type type : CompatHelper.getTypesForBiome(a)) {
			set.remove(type);
		}
		return set;
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return InventoryHelper.getItemHandlerIterator(targetBiome);
	}

	@Override
	public void update() {
		if (world.isRemote) return;

		Biome target = targetBiome.getBiome();

		if (target == null) {
			return;
		}

		if (range.value < 0) return;

		if (this.targetPosition.equals(BlockPos.ORIGIN)) {
			if (findingCooldown > 0) {
				findingCooldown--;
				return;
			}

			BlockPos.MutableBlockPos temp_pos = new BlockPos.MutableBlockPos();

			int range = MathHelper.clamp(this.range.value, 0, MAX_TRANSFORMATION_RANGE);

			Random rand = world.rand;


			if (range == 0) {
				Biome biome1 = world.getBiomeForCoordsBody(pos);

				if (biome1 != target) {
					targetPosition.setPos(pos.getX(), pos.getY(), pos.getZ());
				}
			} else {
				int curBest = Integer.MAX_VALUE;
				for (int i = 0; i < 100; i++) {
					int x = rand.nextInt(range * 2 + 1) - range;
					int z = rand.nextInt(range * 2 + 1) - range;
					temp_pos.setPos(pos.getX() + x, pos.getY(), pos.getZ() + z);

					if (!world.isBlockLoaded(temp_pos)) continue;

					Biome biome1 = world.getBiomeForCoordsBody(temp_pos);

					if (biome1 != target) {
						int d = x * x + z * z;
						if (d < curBest) {
							curBest = d;
							this.targetPosition.setPos(temp_pos);
						}
					}
				}
			}

			if (this.targetPosition.equals(BlockPos.ORIGIN)) {
				findingCooldown = 20;
				return;
			}
		}

		if (this.targetPosition.equals(BlockPos.ORIGIN)) return;

		if (!world.isBlockLoaded(this.targetPosition)) {
			this.targetPosition.setPos(BlockPos.ORIGIN);
			transformTime.value = 0;
			return;
		}

		Biome curBiome = world.getBiomeForCoordsBody(this.targetPosition);

		if (curBiome == target) {
			this.targetPosition.setPos(BlockPos.ORIGIN);
			transformTime.value = 0;
			return;
		}

		TObjectIntHashMap<BlockTerraformer.Type> map = getTransformationRequirements(curBiome, target);


		if (transformTime.value > 0) {
			List<TileTerraformerClimograph> containers = getTileTerraformerContainers();

			present_set.clear();
			for (TileTerraformerClimograph container : containers) {
				BlockTerraformer.Type type = container.getType();
				if (map.containsKey(type)) {
					present_set.add(type);
					if (present_set.size() >= map.size()) {
						break;
					}
				}
			}

			if (present_set.size() < map.size()) {
				return;
			}


			for (TileTerraformerClimograph container : containers) {
				if (map.containsKey(container.getType())) {
					if (container.sprinkerActive == 0) {
						container.markForUpdate();
					}
					container.sprinkerActive = 200;
				}
			}

			if (energy.extractEnergy(POWER_PER_TICK, true) < POWER_PER_TICK)
				return;

			energy.extractEnergy(POWER_PER_TICK, false);


			transformTime.value--;
			if (transformTime.value == 0) {
				BiomeManip.setBiome(world, target, targetPosition.toImmutable());
				this.targetPosition.setPos(BlockPos.ORIGIN);
				transformTime.value = 0;
				currentStoredEnergy.map.clear();
				update();
			}
			return;
		}

		boolean needsEnergy = false;

		for (BlockTerraformer.Type type : map.keySet()) {
			if (currentStoredEnergy.map.get(type) < map.get(type)) {
				needsEnergy = true;
				break;
			}
		}

		if (!needsEnergy) {
			transformTime.value = TRANSFORM_TIME;
		} else {
			List<TileTerraformerClimograph> containers = getTileTerraformerContainers();
			present_set.clear();
			for (TileTerraformerClimograph container : containers) {
				BlockTerraformer.Type type = container.getType();
				present_set.add(type);

				if (container.hasAntenna != Boolean.TRUE || container.level.value <= 0) continue;

				int toAdd = map.get(type) - currentStoredEnergy.map.get(type);
				if (toAdd > 0) {
					container.level.value -= toAdd;
					currentStoredEnergy.map.adjustOrPutValue(type, toAdd, toAdd);
				}
			}
		}
	}

	private List<TileTerraformerClimograph> getTileTerraformerContainers() {
		return TEHelper.get(world, pos, MAX_CONTAINER_RANGE, value -> value instanceof TileTerraformerClimograph && PowerManager.areFreqOnSameGrid(TileTerraformer.this.frequency(), ((TileTerraformerClimograph) value).frequency()));
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTerraformer(this, player);
	}

	@Override
	public float getPower() {
		return Float.NaN;
	}

	@Override
	public void onPowerChanged() {

	}

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return energy;
	}

	public static class ContainerTerraformer extends DynamicContainerTile {

		public ContainerTerraformer(TileTerraformer tile, EntityPlayer player) {
			super(tile);
			addTitle(getName(BlockTerraformer.Type.CONTROLLER));
			addWidget(tile.targetBiome.getSlot(4, 6 + 9));

			addWidget(new WidgetTextData(6 + 18 + 4, 4 + 9 + 5, 90) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					Biome biome = tile.targetBiome.getBiome();
					if (biome == null || tile.targetPosition.equals(BlockPos.ORIGIN)) {
						packet.writeBoolean(false);
						return;
					}
					packet.writeBoolean(true);
					Biome current = tile.world.getBiome(tile.targetPosition);
					packet.writeInt(Biome.REGISTRY.getIDForObject(biome));
					packet.writeInt(Biome.REGISTRY.getIDForObject(current));
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					if (!packet.readBoolean()) {
						return "";
					}

					Biome biome = Biome.getBiome(packet.readInt());
					Biome current = Biome.getBiome(packet.readInt());

					biome = biome == null ? Biomes.PLAINS : biome;
					current = current == null ? Biomes.PLAINS : current;

					return current.getBiomeName() + " -> " + biome.getBiomeName();
				}
			});

			WidgetScrollBarServer scrollBar = new WidgetScrollBarServer(4, 4 + 9 + 18 + 4, 70, 0, MAX_TRANSFORMATION_RANGE) {
				@Override
				public int getValueServer() {
					return tile.range.value;
				}

				@Override
				public void setValueServer(int level) {
					if (tile.range.value > level) {
						BlockPos subtract = tile.targetPosition.subtract(tile.pos);

						if (Math.abs(subtract.getX()) > level || Math.abs(subtract.getZ()) > level) {
							tile.targetPosition.setPos(BlockPos.ORIGIN);
						}

					}
					tile.range.value = level;


				}

				@Override
				public List<String> getToolTip() {
					return ImmutableList.of(Lang.translateArgs("Range: %s", scrollValue));
				}
			};
			addWidget(scrollBar);
			final int widthtemp = 160;
			addWidget(new WidgetBackground(4 + 4 + WidgetScrollBar.BAR_WIDTH, 4 + 9 + 18 + 4, widthtemp - WidgetScrollBar.BAR_WIDTH - 4, 70, DynamicContainer.texBackgroundBorder));
			int border = 2;
			WidgetTextDataScroll textData = new WidgetTextDataScroll(4 + 4 + WidgetScrollBar.BAR_WIDTH + border, 4 + 9 + 18 + 4 + border, widthtemp - WidgetScrollBar.BAR_WIDTH - 4 - border * 2, 70 - border * 2) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeInt(tile.range.value);

					Biome biome = tile.targetBiome.getBiome();
					packet.writeBoolean(biome != null);
					if (biome == null) {
						packet.writeBlockPos(BlockPos.ORIGIN);
						return;
					}
					packet.writeBlockPos(tile.targetPosition);
					if (tile.targetPosition.equals(BlockPos.ORIGIN)) {
						return;
					}

					Biome current = tile.world.getBiome(tile.targetPosition);

					packet.writeInt(Biome.REGISTRY.getIDForObject(biome));
					packet.writeInt(Biome.REGISTRY.getIDForObject(current));

					packet.writeInt(tile.transformTime.value);

					TObjectIntHashMap<BlockTerraformer.Type> transformationRequirements = tile.getTransformationRequirements(current, biome);
					packet.writeShort(transformationRequirements.size());
					HashSet<BlockTerraformer.Type> missingTypes = new HashSet<>();
					for (BlockTerraformer.Type type : transformationRequirements.keySet()) {
						packet.writeByte(type.ordinal());
						packet.writeInt(tile.currentStoredEnergy.map.get(type));
						packet.writeInt(transformationRequirements.get(type));
						if (!tile.present_set.contains(type)) {
							missingTypes.add(type);
						}
					}

					packet.writeShort(missingTypes.size());
					for (BlockTerraformer.Type missingType : missingTypes) {
						packet.writeByte(missingType.ordinal());
					}

				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					int range = packet.readInt();
					packet.readBoolean();
					BlockPos pos = packet.readBlockPos();
					if (pos.equals(BlockPos.ORIGIN)) {
						if (tile.targetBiome.isEmpty()) {
							return Lang.translate("No biome marker.");
						}
//						if (range == 0) {
//							return Lang.translate("Range is set to zero.");
//						}
						return Lang.translate("Searching...");
					}

					StringBuilder builder = new StringBuilder();

					builder.append(Lang.translateArgs("Processing %s, %s, %s", pos.getX(), pos.getY(), pos.getZ())).append('\n');

					Biome biome = Biome.getBiome(packet.readInt());
					Biome current = Biome.getBiome(packet.readInt());

					int time = packet.readInt();

					builder.append(Lang.translateArgs("Transform Time: %s", time)).append("\n\n");

					int n = packet.readShort();
					for (int i = 0; i < n; i++) {
						BlockTerraformer.Type type = BlockTerraformer.Type.values()[packet.readUnsignedByte()];
						builder.append(getName(type));
						builder.append(" (").append(StringHelper.format(packet.readInt())).append("/").append(StringHelper.format(packet.readInt())).append(")\n");

					}
					n = packet.readShort();
					if (n > 0) {
						builder.append("\n").append(ChatFormatting.RED).append(Lang.translate("Missing Climographs:")).append('\n');
						for (int i = 0; i < n; i++) {
							BlockTerraformer.Type type = BlockTerraformer.Type.values()[packet.readUnsignedByte()];
							builder.append("    - ").append(getName(type)).append("\n");
						}
						builder.append(Lang.translateArgs("Place Climographs within %s blocks of Terraformer", MAX_CONTAINER_RANGE));
					}

					return builder.toString();
				}
			};

			addWidget(textData);

			crop();

			addWidget(new WidgetEnergyStorage(width, (height - 54) / 2, tile.energy));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}


}
