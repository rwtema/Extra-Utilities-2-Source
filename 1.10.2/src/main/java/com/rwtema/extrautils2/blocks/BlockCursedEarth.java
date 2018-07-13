package com.rwtema.extrautils2.blocks;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.backend.XUBlockConnectedTextureBase;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.textures.ConnectedTexture;
import com.rwtema.extrautils2.textures.ISolidWorldTexture;
import com.rwtema.extrautils2.textures.SimpleWorldTexture;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;

public class BlockCursedEarth extends XUBlockConnectedTextureBase {

	public static final int MAX_DECAY = 15;
	public static final PropertyInteger DECAY = PropertyInteger.create("decay", 0, MAX_DECAY);
	public static final WeakLinkedSet<Entity> cursedClient = new WeakLinkedSet<>();
	static final UUID uuid = UUID.fromString("E53E0344-EA5E-4F71-98F6-40791198D8FE");
	public static Set<String> entity_blacklist = new HashSet<>();
	ISolidWorldTexture tex;
	ISolidWorldTexture side;
	ISolidWorldTexture bottom;

	public BlockCursedEarth() {
		super(Material.GROUND);
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		setTickRandomly(true);
	}

	public static void startFastSpread(World worldIn, BlockPos pos) {
		BlockCursedEarth earth = XU2Entries.cursedEarth.value;
		IBlockState cursedEarthState = earth.getDefaultState();
		worldIn.setBlockState(pos, cursedEarthState);
		worldIn.scheduleUpdate(pos.toImmutable(), earth, 1);
		worldIn.playEvent(1027, pos, 0);
	}

	private static void trySpawnMob(WorldServer world, BlockPos pos, EntityLiving mob) {
		if (mob == null) return;

		boolean shouldCenter = world.rand.nextBoolean();
		float x = pos.getX() + (shouldCenter ? 0.5F : world.rand.nextFloat());
		float y = pos.getY() + 1;
		float z = pos.getZ() + (shouldCenter ? 0.5F : world.rand.nextFloat());
		mob.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);

		if (!ForgeEventFactory.doSpecialSpawn(mob, world, x, y, z))
			mob.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(mob)), null);

		if (!mob.isNotColliding() || !mob.getCanSpawnHere()) {
			mob.setDead();
			return;
		}

		if (spawnMobAsCursed(mob)) {
			mob.playLivingSound();
		}
	}

	public static boolean spawnMobAsCursed(Entity mob) {
		mob.forceSpawn = true;
		if (mob instanceof EntityLivingBase) {
			mob.getEntityData().setInteger("CursedEarth", 60);
			EntityLivingBase living = (EntityLivingBase) mob;
			BlockCursedEarth.applyAttribute(living, SharedMonsterAttributes.ATTACK_DAMAGE, new AttributeModifier(uuid, "CursedEarth", 1.5, 1));
			BlockCursedEarth.applyAttribute(living, SharedMonsterAttributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "CursedEarth", 1.2, 1));

			IAttributeInstance attributeInstanceByName = living.getAttributeMap().getAttributeInstanceByName("zombie.spawnReinforcements");
			if (attributeInstanceByName != null) {
				attributeInstanceByName.setBaseValue(0);
			}
		}
		if (!mob.world.spawnEntity(mob)) {
			return false;
		}

		if (mob.isBeingRidden()) {
			mob.getPassengers().forEach(BlockCursedEarth::spawnMobAsCursed);
		}
		return true;
	}

	@SuppressWarnings("ConstantConditions")
	private static void applyAttribute(EntityLivingBase mob, IAttribute attackDamage, AttributeModifier modifier) {
		IAttributeInstance instance = mob.getEntityAttribute(attackDamage);
		if (instance != null) {
			instance.applyModifier(modifier);
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.DIRT);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, @Nonnull IBlockState state, EntityPlayer player) {
		return state.getValue(DECAY) == 0;
	}

	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		int numParticles = (MAX_DECAY - stateIn.getValue(DECAY));
		for (int i = 0; i < numParticles; i++) {
			worldIn.spawnParticle(
					EnumParticleTypes.SMOKE_NORMAL,
					pos.getX() + rand.nextDouble(),
					pos.getY() + 1.01,
					pos.getZ() + rand.nextDouble(),
					0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public boolean isFireSource(@Nonnull World world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.UP;
	}

	@Override
	public void randomTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
		if (worldIn.isRemote) return;
		performTick(worldIn, pos, random, false);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (worldIn == null || worldIn.isRemote) return;
		performTick(worldIn, pos, rand, true);
	}

	protected void performTick(World worldIn, BlockPos pos, Random rand, boolean fastSpreading) {
		WorldServer world = (WorldServer) worldIn;

		int light = world.getLightFromNeighbors(pos.up());
		if (light >= 9) {
			IBlockState blockState = world.getBlockState(pos.up());
			boolean nearbyFire = blockState.getMaterial() == Material.FIRE;
			if (nearbyFire) {
				if (rand.nextInt(5) == 0) {
					world.setBlockState(pos, Blocks.DIRT.getDefaultState());
				}
			}
			if (!nearbyFire) for (int i = 0; i < 10; ++i) {
				BlockPos add = pos.add(rand.nextInt(9) - 4,
						rand.nextInt(5) - 3,
						rand.nextInt(9) - 4
				);

				if (add.getY() >= 0 && add.getY() < 256 && !worldIn.isBlockLoaded(add)) continue;

				if (world.getBlockState(add).getMaterial() == Material.FIRE) {
					nearbyFire = true;
					break;
				}
			}

			if (nearbyFire) {
				for (int i = 0; i < 40; ++i) {
					BlockPos add = pos.add(rand.nextInt(9) - 4,
							rand.nextInt(5) - 3,
							rand.nextInt(9) - 4
					);
					if (add.getY() >= 0 && add.getY() < 256 && !worldIn.isBlockLoaded(add)) continue;

					if (world.getBlockState(add).getBlock() == this) {
						IBlockState s = world.getBlockState(add.up());
						if (s.getBlock().isReplaceable(worldIn, add.up())) {
							world.setBlockState(add.up(), Blocks.FIRE.getDefaultState());
						} else {
							world.setBlockState(add, Blocks.DIRT.getDefaultState());
						}
					}
				}
				return;
			}
		}
		boolean spread = false;


		if (fastSpreading) {
			ArrayList<BlockPos> list = Lists.newArrayList(BlockPos.getAllInBox(pos.add(-1, -2, -1), pos.add(1, 2, 1)));
			Collections.shuffle(list);
			for (BlockPos blockPos : list) {
				trySpread(worldIn, pos, rand, true, world, false, blockPos);
			}
		} else {
			for (int i = 0; i < 4; ++i) {
				BlockPos blockpos = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
				spread = trySpread(worldIn, pos, rand, false, world, spread, blockpos);
			}

			if (light >= 9 || spread && rand.nextInt(8) != 0) return;

			AxisAlignedBB bb = new AxisAlignedBB(pos).grow(-7, 4, 7);
			int numCreaturesNearby = world.getEntitiesWithinAABB(EntityLiving.class, bb, input -> input != null && input.isCreatureType(EnumCreatureType.MONSTER, false)).size();

			if (numCreaturesNearby < 8)
				trySpawnMob(world, pos);
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(Lang.translate("Aggressively spawns mobs when in darkness."));
		tooltip.add(Lang.translate("Only fire can stop its spread."));
	}

	private boolean trySpread(World worldIn, BlockPos pos, Random rand, boolean fastSpreading, WorldServer world, boolean spread, BlockPos blockpos) {
		if (blockpos.getY() >= 0 && blockpos.getY() < 256 && !worldIn.isBlockLoaded(blockpos)) {
			return spread;
		}

		IBlockState otherBlock = worldIn.getBlockState(blockpos);
		if (otherBlock.getBlock() != Blocks.GRASS && (otherBlock.getBlock() != Blocks.DIRT || otherBlock.getValue(BlockDirt.VARIANT) != BlockDirt.DirtType.DIRT)) {
			return spread;
		}

		IBlockState aboveState = worldIn.getBlockState(blockpos.up());
		if (aboveState.getLightOpacity(worldIn, pos.up()) > 2) {
			return spread;
		}

		int decay = MAX_DECAY + 1;
		for (BlockPos.MutableBlockPos p : BlockPos.getAllInBoxMutable(blockpos.add(-1, -1, -1), blockpos.add(1, 1, 1))) {
			IBlockState blockState = world.getBlockState(p);
			if (blockState.getBlock() == this) {
				decay = Math.min(decay, blockState.getValue(DECAY) + 1);
			}
		}

		if (rand.nextBoolean()) decay++;

		if (decay > MAX_DECAY) return spread;

		worldIn.setBlockState(blockpos, this.getDefaultState().withProperty(DECAY, decay));
		if (fastSpreading) {
			world.playEvent(2001, blockpos, Block.getIdFromBlock(this));
			world.scheduleUpdate(blockpos, this, 2 + rand.nextInt(8));
		}

		return true;
	}

	protected void trySpawnMob(WorldServer world, BlockPos pos) {
		EnumCreatureType type = EnumCreatureType.MONSTER;
		Biome.SpawnListEntry entry = world.getSpawnListEntryForTypeAt(type, pos);

		if (entry == null || !world.canCreatureTypeSpawnHere(type, entry, pos))
			return;

		EntityEntry entityEntry = EntityRegistry.getEntry(entry.entityClass);

		if (entityEntry == null || entity_blacklist.contains(Objects.toString(entityEntry.getRegistryName()))) {
			return;
		}

		EntityLiving mob;

		try {
			mob = (EntityLiving) entityEntry.newInstance(world);
		} catch (Exception exception) {
			exception.printStackTrace();
			return;
		}

		trySpawnMob(world, pos, mob);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this).addWorldPropertyWithDefault(DECAY, 0).build();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}

	@Override
	public ISolidWorldTexture getConnectedTexture(IBlockState state, EnumFacing side) {
		if (side == EnumFacing.UP)
			return this.tex;
		if (side == EnumFacing.DOWN)
			return this.bottom;
		return this.side;
	}

	@Override
	public void registerTextures() {
		tex = new ConnectedTexture("evil_earth", getDefaultState(), this) {
			@Override
			protected boolean matches(IBlockAccess world, BlockPos pos, EnumFacing side, BlockPos originalPos) {

				IBlockState b1 = world.getBlockState(pos);
				boolean matches = b1.getBlock() == XU2Entries.cursedEarth.value && b1.shouldSideBeRendered(world, pos, side);
				if (matches) {
					int d1 = b1.getValue(DECAY);
					int d2 = world.getBlockState(originalPos).getValue(DECAY);
					if (d1 == MAX_DECAY && d2 == MAX_DECAY)
						return true;
					if (d1 == MAX_DECAY || d2 == MAX_DECAY)
						return false;

					long a = MathHelper.getPositionRandom(pos) >> 16;
					long b = MathHelper.getPositionRandom(originalPos) >> 16;
					return (a & 1) == (b & 1);

				}
				return false;

			}
		};
		side = new SimpleWorldTexture("cursedearthside");
		bottom = new SimpleWorldTexture("cursedearthbottom");
	}

	public enum Type {
		CURSED_NORMAL
	}

	public static class EventHandler {

		@SubscribeEvent
		public void cureCurse(PlayerInteractEvent.EntityInteract event) {
			Entity entity = event.getTarget();
			ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
			if (StackHelper.isNull(heldItem) || heldItem.getItem() != Items.MILK_BUCKET) return;
			if (entity instanceof EntityLiving) {
				NBTTagCompound nbt = entity.getEntityData();
				if (nbt.hasKey("CursedEarth", Constants.NBT.TAG_INT)) {
					EntityLiving living = (EntityLiving) entity;
					for (EntityAITasks.EntityAITaskEntry taskEntry : living.tasks.taskEntries) {
						if (taskEntry.action instanceof AICursed) {
							((AICursed) taskEntry.action).cursedEarth = -1;
							living.tasks.removeTask(taskEntry.action);
							break;
						}
					}

					for (IAttributeInstance attributeInstance : living.getAttributeMap().getAllAttributes()) {
						attributeInstance.removeModifier(uuid);
					}

					nbt.removeTag("CursedEarth");
					event.setCanceled(true);

					event.getEntityPlayer().setHeldItem(event.getHand(), heldItem.getItem().getContainerItem(heldItem));
					NetworkHandler.sendToAllAround(new PacketEntityIsEvil(living.getEntityId(), false), new NetworkRegistry.TargetPoint(
							living.dimension, living.posX, living.posY, living.posZ, 64
					));
				}
			}
		}

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public void spawnParticle(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.END || Minecraft.getMinecraft().isGamePaused()) return;
			for (Iterator<Entity> iterator = cursedClient.iterator(); iterator.hasNext(); ) {
				Entity entity = iterator.next();
				if (entity.isDead) {
					iterator.remove();
					continue;
				}
				Random rand = entity.world.rand;
				entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
						entity.posX + (rand.nextDouble() - 0.5D) * (double) entity.width,
						entity.posY + rand.nextDouble() * (double) entity.height,
						entity.posZ + (rand.nextDouble() - 0.5D) * (double) entity.width,
						0, 0, 0);
			}


		}

		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public void render(RenderLivingEvent.Pre event) {
			if (cursedClient.contains(event.getEntity())) {
//				GlStateManager.enableBlend();
//				GlStateManager.alphaFunc(516, 0.003921569F);
//				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//				GlStateManager.enableAlpha();
				float v = 0.1F;
				GlStateManager.color(v, v, v, 1F);
			}
		}

		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public void render(RenderLivingEvent.Post event) {
			if (cursedClient.contains(event.getEntity())) {
				GlStateManager.color(1, 1, 1, 1);
//				GlStateManager.disableBlend();
//				GlStateManager.alphaFunc(516, 0.1F);
//				GlStateManager.depthMask(true);
			}
		}

		@SubscribeEvent
		public void onStartTrack(PlayerEvent.StartTracking event) {
			Entity target = event.getTarget();
			if (!target.isDead && target instanceof EntityLiving) {
				NBTTagCompound nbt = target.getEntityData();
				if (nbt.hasKey("CursedEarth", Constants.NBT.TAG_INT)) {
					NetworkHandler.sendPacketToPlayer(new PacketEntityIsEvil(target.getEntityId(), true), event.getEntityPlayer());
				}
			}
		}

		@SubscribeEvent
		public void spawnInWorld(EntityJoinWorldEvent event) {
			Entity entity = event.getEntity();
			if (entity instanceof EntityLiving) {
				NBTTagCompound nbt = entity.getEntityData();
				if (nbt.hasKey("CursedEarth", Constants.NBT.TAG_INT)) {
					int cursedEarth = nbt.getInteger("CursedEarth");
					if (cursedEarth <= 0) {
						entity.setDead();
						event.setCanceled(true);
					} else {
						EntityLiving living = (EntityLiving) entity;
						living.tasks.addTask(0, new AICursed(living, cursedEarth));
					}
				}
			}
		}
	}

	@NetworkHandler.XUPacket
	public static class PacketEntityIsEvil extends XUPacketServerToClient {
		private int entityId;
		private boolean isEvil;

		@SuppressWarnings("unused")
		public PacketEntityIsEvil() {

		}

		public PacketEntityIsEvil(int entityId, boolean isEvil) {
			this.entityId = entityId;
			this.isEvil = isEvil;
		}

		@Override
		public void writeData() throws Exception {
			writeInt(entityId);
			writeBoolean(isEvil);
		}

		@Override
		public void readData(EntityPlayer player) {
			entityId = readInt();
			isEvil = readBoolean();
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			return new Runnable() {
				@Override
				public void run() {
					Entity entity = Minecraft.getMinecraft().world.getEntityByID(entityId);
					if (entity != null) {
						if (isEvil) {
							cursedClient.add(entity);
						} else {
							cursedClient.remove(entity);
						}
					} else {
						LogHelper.debug("No findy entity");
					}
				}
			};
		}
	}

	public static class AICursed extends EntityAIBase {
		final EntityLiving living;
		int cursedEarth;

		public AICursed(EntityLiving living, int cursedEarth) {
			this.living = living;
			this.cursedEarth = cursedEarth;
		}

		@Override
		public boolean shouldExecute() {
			return true;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return true;
		}

		@Override
		public void updateTask() {
			if ((living.world.getTotalWorldTime() % 20) != 0) {
				return;
			}

			if (cursedEarth < 0) return;

			if (cursedEarth == 0) {
				for (int k = 0; k < 20; ++k) {
					Random rand = living.world.rand;
					double d2 = rand.nextGaussian() * 0.02D;
					double d0 = rand.nextGaussian() * 0.02D;
					double d1 = rand.nextGaussian() * 0.02D;

					living.world.spawnParticle(
							EnumParticleTypes.EXPLOSION_NORMAL,
							living.posX + (double) (rand.nextFloat() * living.width * 2.0F) - (double) living.width,
							living.posY + (double) (rand.nextFloat() * living.height),
							living.posZ + (double) (rand.nextFloat() * living.width * 2.0F) - (double) living.width,
							d2, d0, d1);
				}

				living.setDead();
			} else {
				cursedEarth--;
				living.getEntityData().setInteger("CursedEarth", cursedEarth);
			}
		}
	}
}
