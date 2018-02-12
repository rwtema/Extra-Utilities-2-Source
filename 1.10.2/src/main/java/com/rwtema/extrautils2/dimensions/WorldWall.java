package com.rwtema.extrautils2.dimensions;

import com.rwtema.extrautils2.compatibility.ChunkProviderFlatCompat;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class WorldWall extends WorldType {

	public final static String TAG_SPAWN_ITEMS = "xu_wall_spawned";
	public static boolean giveSpawnItems;

	private final AxisAlignedBB BOUNDS = new AxisAlignedBB(
			Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
			0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

	public WorldWall() {
		super("xu_wall");
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public int getSpawnFuzz(@Nonnull WorldServer world, MinecraftServer server) {
		return 0;
	}

	@Nonnull
	@Override
	public ChunkProviderFlatCompat getChunkGenerator(@Nonnull World world, String generatorOptions) {
		FlatGeneratorInfo voidGenerator = new FlatGeneratorInfo();
		voidGenerator.setBiome(Biome.getIdForBiome(Biomes.PLAINS));
		voidGenerator.getFlatLayers().add(new FlatLayerInfo(1, Blocks.AIR));
		voidGenerator.updateLayers();

		FlatGeneratorInfo altflatgeneratorinfo = new FlatGeneratorInfo();
		altflatgeneratorinfo.setBiome(Biome.getIdForBiome(Biomes.PLAINS));
		altflatgeneratorinfo.getFlatLayers().add(new FlatLayerInfo(3, 256, Blocks.BEDROCK, 0));
		altflatgeneratorinfo.updateLayers();

		ChunkProviderFlatCompat wallGenerator = new ChunkProviderFlatCompat(world, world.getSeed(), false, altflatgeneratorinfo.toString());

		return new ChunkProviderFlatCompat(world, world.getSeed(), false, voidGenerator.toString()) {

			@Nonnull
			@Override
			public Chunk generateChunk(int x, int z) {
				Chunk chunk;
				if (x == -1 || x == -2) {
					chunk = wallGenerator.generateChunk(x, z);
					chunk.setTerrainPopulated(true);
				} else {
					chunk = super.generateChunk(x, z);
					if (x <= 0) {
						chunk.setTerrainPopulated(true);
					}
				}
				return chunk;
			}
		};
	}

	@SubscribeEvent
	public void onSpawn(WorldEvent.CreateSpawnPosition event) {
		World world = event.getWorld();
		if (world.getWorldType() != this) return;
		BlockPos spawnPoint = new BlockPos(0, 64, 0);
		world.setBlockState(spawnPoint, Blocks.BEDROCK.getDefaultState());
		for (int i = 1; i <= 3; i++) {
			world.setBlockState(spawnPoint.up(i), Blocks.AIR.getDefaultState());
		}

		world.getWorldInfo().setSpawn(spawnPoint);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		World world = event.player.getEntityWorld();
		if (giveSpawnItems && world.getWorldType() == this) {

			NBTTagCompound data = NBTHelper.getOrCreatePersistentTag(event.player);

			if (!data.getBoolean(TAG_SPAWN_ITEMS)) {
				ItemHandlerHelper.giveItemToPlayer(event.player, new ItemStack(Blocks.GRASS, 16));
				ItemHandlerHelper.giveItemToPlayer(event.player, new ItemStack(Blocks.SAPLING, 1, 0));
				ItemHandlerHelper.giveItemToPlayer(event.player, new ItemStack(Items.DYE, 64, EnumDyeColor.WHITE.getDyeDamage()));
				ItemHandlerHelper.giveItemToPlayer(event.player, new ItemStack(Items.LAVA_BUCKET));
				ItemHandlerHelper.giveItemToPlayer(event.player, new ItemStack(Blocks.ICE));
				data.setBoolean(TAG_SPAWN_ITEMS, true);
			}
		}
	}


	@SubscribeEvent
	public void onPlayerTeleport(EnderTeleportEvent event) {
		if (event.getTargetX() < 0) {
			EntityLivingBase entityLiving = event.getEntityLiving();
			if (entityLiving.world.getWorldType() == this && entityLiving.posX >= event.getTargetX()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerTransport(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.toDim != 0) return;
		World world = event.player.world;
		if (world.getWorldType() != this) return;
		if (event.player.posX >= 0) return;
		if (event.player instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.player;
			player.connection.setPlayerLocation(
					0.5, player.posY, player.posZ, player.rotationYaw, player.rotationPitch
			);
		}
	}

	@SubscribeEvent
	public void onCollision(GetCollisionBoxesEvent event) {
		if (event.getWorld().getWorldType() != this) return;

		if (event.getWorld().provider.getDimension() != 0) return;

		AxisAlignedBB aabb = event.getAabb();
		if (BOUNDS.intersects(aabb)) {
			event.getCollisionBoxesList().add(BOUNDS);
			for (int i = -1; i <= 2; i++) {
				int x2 = (int) Math.ceil(aabb.minX) - i;
				if (x2 < 0) {
					AxisAlignedBB bb = new AxisAlignedBB(
							Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
							x2, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
					if (bb.intersects(aabb)) {
						event.getCollisionBoxesList().add(bb);
					}
				}
			}
		}
	}
}
