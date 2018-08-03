package com.rwtema.extrautils2.quarry;

import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.eventhandlers.ItemCaptureHandler;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.StackDump;
import com.rwtema.extrautils2.itemhandler.XUTileItemStackHandler;
import com.rwtema.extrautils2.power.energy.XUEnergyStorage;
import com.rwtema.extrautils2.tile.TilePower;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.items.ItemStackHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static net.minecraft.world.WorldEntitySpawner.canCreatureTypeSpawnAtLocation;

public class TilePeacefulTable extends TilePower implements ITickable {
	public static final int ENERGY_PER_OPERATION = 32000;
	public static final int TICK_TIME = 20;
	private final ItemStackHandler contents = registerNBT("contents", new XUTileItemStackHandler(9, this));
	private final StackDump extraStacks = registerNBT("extrastacks", new StackDump());
	XUEnergyStorage energy = registerNBT("energy", new XUEnergyStorage(ENERGY_PER_OPERATION * 10, ENERGY_PER_OPERATION / TICK_TIME, ENERGY_PER_OPERATION));
	private Biome[] listToReuse = new Biome[1];

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return Iterables.concat(
				InventoryHelper.getItemHandlerIterator(contents),
				extraStacks);
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public float getPower() {
		return Float.NaN;
	}

	@Override
	public void update() {
		if (!world.isRemote && energy.extractEnergy(ENERGY_PER_OPERATION, true) == ENERGY_PER_OPERATION) {
			Random rand = world.rand;

			listToReuse = world.getBiomeProvider().getBiomes(listToReuse, rand.nextInt(65536), rand.nextInt(65536), 1, 1, false);
			Biome biome = listToReuse[0];
			if (biome == null) return;
			List<Biome.SpawnListEntry> spawnableList = biome.getSpawnableList(EnumCreatureType.MONSTER);
			if (spawnableList.isEmpty()) {
				return;
			}

			Biome.SpawnListEntry biome$spawnlistentry = WeightedRandom.getRandomItem(rand, spawnableList);

			energy.extractEnergy(ENERGY_PER_OPERATION, false);

			WorldServer worldServerIn = (WorldServer) world;

			if (worldServerIn.canCreatureTypeSpawnHere(EnumCreatureType.MONSTER, biome$spawnlistentry, pos) &&
					canCreatureTypeSpawnAtLocation(
							EntitySpawnPlacementRegistry.getPlacementForEntity(biome$spawnlistentry.entityClass),
							worldServerIn, pos)) {
				EntityLiving entityliving;

				try {
					entityliving = biome$spawnlistentry.newInstance(worldServerIn);
				} catch (Exception exception) {
					exception.printStackTrace();
					return;
				}

				float x = pos.getX() + 0.5F, y = pos.getY() + 1F, z = pos.getZ() + 0.5F;
				entityliving.setLocationAndAngles(
						x, y, z,
						worldServerIn.rand.nextFloat() * 360.0F, 0.0F);

				Result canSpawn = CompatHelper.canSpawnEvent(worldServerIn, entityliving, x, y, z);
				if (canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && (entityliving.getCanSpawnHere() && entityliving.isNotColliding()))) {

					if (!ForgeEventFactory.doSpecialSpawn(entityliving, worldServerIn, x, y, z)) {
						DifficultyInstance difficultyInstance = new DifficultyInstance(EnumDifficulty.HARD, world.getWorldTime(), rand.nextInt(100), rand.nextFloat());
					}

					entityliving.onInitialSpawn(worldServerIn.getDifficultyForLocation(new BlockPos(entityliving)), null);


					worldServerIn.spawnEntity(entityliving);
					ItemCaptureHandler.startCapturing();
					LinkedList<ItemStack> stacks;
					try {
						entityliving.onDeath(DamageSource.GENERIC);
					} finally {
						stacks = ItemCaptureHandler.stopCapturing();
					}
					for (ItemStack stack : stacks) {
						InventoryHelper.insertWithRunoff(contents, stack, extraStacks);
					}
					entityliving.setDead();
				}

			}
		}


	}

}

