package com.rwtema.extrautils2.entity;

import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.DataWatcherItemStack;
import com.rwtema.extrautils2.compatibility.EntityCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.enchants.BoomerangEnchantment;
import com.rwtema.extrautils2.items.ItemBoomerang;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import com.rwtema.extrautils2.utils.helpers.WorldHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

public class EntityBoomerang extends Entity implements IEntityAdditionalSpawnData, IProjectile {
	public final static WeakHashMap<Object, WeakReference<EntityBoomerang>> boomerangOwners = new WeakHashMap<>();
	public final static WeakHashMap<Object, WeakReference<EntityBoomerang>> boomerangOwnersClient = ExtraUtils2.proxy.nullifyOnServer(new WeakHashMap<Object, WeakReference<EntityBoomerang>>());

	private static final DataParameter<Byte> DATAWATCHER_OUT_FLAG = EntityDataManager.createKey(EntityBoomerang.class, DataSerializers.BYTE);
	private static final DataParameter<Rotations> DATAWATCHER_HOME = EntityDataManager.createKey(EntityBoomerang.class, DataSerializers.ROTATIONS);
	private static final DataParameter<Integer> DATAWATCHER_OWNER_ID = EntityDataManager.createKey(EntityBoomerang.class, DataSerializers.VARINT);
	private static final DataWatcherItemStack.Wrapper DATAWATCHER_STACK = new DataWatcherItemStack.Wrapper(EntityDataManager.createKey(EntityBoomerang.class, DataSerializers.ITEM_STACK));


	int flyTime;
	int potionColor = 0;
	UUID owner = null;


	public EntityBoomerang(World worldIn) {
		super(worldIn);
		this.setSize(0.5F, 0.5F);
		noClip = true;
	}

	public EntityBoomerang(World worldIn, double x, double y, double z, ItemStack stack, Object owner) {
		this(worldIn);
		setLocationAndAngles(x, y, z, 0, 0);
		setHome((float) posX, (float) posY, (float) posZ);
		DataWatcherItemStack.setStack(dataManager, stack, DATAWATCHER_STACK);
		if (owner != null) getBoomerangOwners(worldIn).put(owner, new WeakReference<>(this));
	}

	public EntityBoomerang(World worldIn, EntityLivingBase shooter, ItemStack stack) {
		this(worldIn);

		DataWatcherItemStack.setStack(dataManager, stack, DATAWATCHER_STACK);


		if (!(shooter instanceof EntityPlayer) || PlayerHelper.isPlayerReal((EntityPlayer) shooter)) {
			this.owner = shooter.getUniqueID();
		}

		if (worldIn.isRemote)
			boomerangOwnersClient.put(shooter, new WeakReference<>(this));
		else
			boomerangOwners.put(shooter, new WeakReference<>(this));


		Vec3d eyeVec = getEyeVec(shooter);
		this.setLocationAndAngles(eyeVec.x, eyeVec.y, eyeVec.z, shooter.rotationYaw, shooter.rotationPitch);
		this.motionX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
		this.motionZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
		this.motionY = (double) (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 1.25F * (1 + 0.3F * getEnchantmentLevel(ItemBoomerang.SPEED)), 0.0F);

		setHome((float) eyeVec.x, (float) eyeVec.y, (float) eyeVec.z);


	}

	public static WeakHashMap<Object, WeakReference<EntityBoomerang>> getBoomerangOwners(World worldIn) {
		return worldIn.isRemote ? boomerangOwnersClient : boomerangOwners;
	}

	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / (double) f;
		y = y / (double) f;
		z = z / (double) f;
		x = x + this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) inaccuracy;
		y = y + this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) inaccuracy;
		z = z + this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) inaccuracy;
		x = x * (double) velocity;
		y = y * (double) velocity;
		z = z * (double) velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		this.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (MathHelper.atan2(y, (double) f1) * 180.0D / Math.PI);
	}

	public void setHome(float x, float y, float z) {
		dataManager.set(DATAWATCHER_HOME, new Rotations(x, y, z));
	}

	public Vec3d getHome() {
		Rotations rotations = dataManager.get(DATAWATCHER_HOME);
		return new Vec3d(rotations.getX(), rotations.getY(), rotations.getZ());
	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(x * x + z * z);
			this.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (MathHelper.atan2(y, (double) f) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
		}
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(DATAWATCHER_OUT_FLAG, (byte) 0);
		this.dataManager.register(DATAWATCHER_HOME, new Rotations(0, 0, 0));
		this.dataManager.register(DATAWATCHER_OWNER_ID, -1);
		DataWatcherItemStack.register(dataManager, DATAWATCHER_STACK);
	}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound tag) {
		if (tag.hasKey("Target_UUIDL")) {
			owner = new UUID(tag.getLong("Target_UUIDU"), tag.getLong("Target_UUIDL"));
		} else
			owner = null;

		tag.setByte("OutFlag", dataManager.get(DATAWATCHER_OUT_FLAG));
		tag.setTag("Home", dataManager.get(DATAWATCHER_HOME).writeToNBT());
		tag.setInteger("Owner", dataManager.get(DATAWATCHER_OWNER_ID));
		ItemStack stack = DataWatcherItemStack.getStack(dataManager, DATAWATCHER_STACK);
		if (!StackHelper.isNonNull(stack)) {
			NBTTagCompound t = new NBTTagCompound();
			stack.writeToNBT(t);
			tag.setTag("Stack", t);
		}
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound tag) {
		if (owner != null) {
			tag.setLong("Target_UUIDL", owner.getLeastSignificantBits());
			tag.setLong("Target_UUIDU", owner.getMostSignificantBits());
		}
		dataManager.set(DATAWATCHER_OUT_FLAG, tag.getByte("OutFlag"));
		dataManager.set(DATAWATCHER_HOME, new Rotations(tag.getTagList("Home", Constants.NBT.TAG_INT)));
		dataManager.set(DATAWATCHER_OWNER_ID, tag.getInteger("Owner"));
		DataWatcherItemStack.setStack(dataManager, StackHelper.loadFromNBT(tag.getCompoundTag("Stack")), DATAWATCHER_STACK);
	}

	public int getEnchantmentLevel(BoomerangEnchantment enchantment) {
		ItemStack stack = DataWatcherItemStack.getStack(dataManager, DATAWATCHER_STACK);
		return StackHelper.isNonNull(stack) ? EnchantmentHelper.getEnchantmentLevel(enchantment, stack) : 0;
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		Entity owner = getOwner();
		boolean isRemote = world.isRemote;

		Vec3d dest = calcTargetVec();
		flyTime++;

		boolean returning = dataManager.get(DATAWATCHER_OUT_FLAG) != 0;


		Vec3d destDiff = dest.subtract(posX, posY, posZ);

		float d = MathHelper.sqrt(destDiff.x * destDiff.x + destDiff.y * destDiff.y + destDiff.z * destDiff.z);

		destDiff = destDiff.normalize();


		double acceleration = flyTime * 0.001 + (returning ? 0.05 : 0);

		if (returning)
			acceleration *= (1 + getEnchantmentLevel(ItemBoomerang.SPEED));

		if ((d < 1e-4D && flyTime > 25) || acceleration > 1) {
			setMeDead();
			return;
		}


		motionX *= (1 - acceleration);
		motionY *= (1 - acceleration);
		motionZ *= (1 - acceleration);


		motionX += destDiff.x * acceleration;
		motionY += destDiff.y * acceleration;
		motionZ += destDiff.z * acceleration;

		float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
		this.rotationPitch = (float) (MathHelper.atan2(this.motionY, (double) f) * 180.0D / Math.PI);


		if (flyTime > 5 || returning)
			if (MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ) >= d) {
				setLocationAndAngles(dest.x, dest.y, dest.z, rotationYaw, rotationPitch);
				setMeDead();
				return;
			}

		if (this.world.isRemote)
			EntityCompat.move(this, this.motionX, this.motionY, this.motionZ);
		else {
			HashSet<BlockPos> prevPosSet = new HashSet<>();
			if (flyTime > 1)
				Iterables.addAll(prevPosSet, getNeighbourBlocks());

			EntityCompat.move(this, this.motionX, this.motionY, this.motionZ);

			for (BlockPos newPos : getNeighbourBlocks()) {
				if (prevPosSet.contains(newPos)) continue;
				IBlockState blockState = world.getBlockState(newPos);
				Block block = blockState.getBlock();
				if (block == Blocks.STONE_BUTTON || block == Blocks.WOODEN_BUTTON || block == Blocks.LEVER) {
					CompatHelper.activateBlock(block, world, newPos, blockState, FakePlayerFactory.getMinecraft((WorldServer) world), EnumHand.MAIN_HAND, null, EnumFacing.DOWN, 0, 0, 0);
				}
				if (getEnchantmentLevel(ItemBoomerang.DIGGING) > 0) {
					if (block instanceof IPlantable || block instanceof IShearable) {
						block.dropBlockAsItem(world, newPos, blockState, 0);
						world.setBlockState(newPos, Blocks.AIR.getDefaultState(), 3);
						WorldHelper.markBlockForUpdate(world, newPos);
					}
				}

			}
		}


		ItemStack potionStack = DataWatcherItemStack.getStack(dataManager, DATAWATCHER_STACK);
		if (isRemote) {
			if (!returning) {
				if (potionColor == -1) {
					if (StackHelper.isNonNull(potionStack)) {
						List<PotionEffect> effectsFromStack = PotionUtils.getEffectsFromStack(potionStack);
						if (effectsFromStack.isEmpty()) {
							potionColor = 0;
						} else {
							potionColor = PotionUtils.getPotionColorFromEffectList(effectsFromStack);
						}
					} else {
						potionColor = 0;
					}
				}

				double dx = posX - prevPosX;
				double dy = posY - prevPosY;
				double dz = posZ - prevPosZ;

				for (int k = 0; k < 4; ++k) {
					double t = k / 4.0;

					world.spawnParticle(EnumParticleTypes.CRIT,
							this.posX + dx * t,
							this.posY + dy * t,
							this.posZ + dz * t,
							-dx,
							-dy + 0.2D,
							-dz);
				}

				if (potionColor != 0) {
					double d0 = (potionColor >> 16 & 255) / 255.0D;
					double d1 = (potionColor >> 8 & 255) / 255.0D;
					double d2 = (potionColor & 255) / 255.0D;

					for (int j = 0; j < 3; ++j) {
						double t = j / 3.0D;
						this.world.spawnParticle(EnumParticleTypes.SPELL_MOB,
								this.posX + dx * t,
								this.posY + dy * t,
								this.posZ + dz * t, d0, d1, d2);
					}
				}

			}
		}

		Vec3d startVec = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d endVec = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		RayTraceResult movingobjectposition = this.world.rayTraceBlocks(startVec, endVec, false, true, false);


		if (!isRemote) {
			Entity entity = null;
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().offset(this.motionX, this.motionY, this.motionZ).grow(1.0D, 1.0D, 1.0D));
			double d0 = -1;

			for (Entity e : list) {
				if (e instanceof EntityItem || e instanceof EntityXPOrb) {
					if (e.getRidingEntity() == null) {
						addItem(e);
					}
					continue;
				}

				if (returning) continue;

				if (e.canBeCollidedWith() && !isOwner(e)) {
					if (e instanceof EntityPlayer) {
						EntityPlayer entityplayer = (EntityPlayer) e;
						if (entityplayer.capabilities.disableDamage || owner instanceof EntityPlayer && !(((EntityPlayer) owner).canAttackPlayer(entityplayer))) {
							continue;
						}
					}

					float f1 = 0.3F;
					AxisAlignedBB axisAlignedBB = e.getEntityBoundingBox().grow((double) f1, (double) f1, (double) f1);
					RayTraceResult mop = axisAlignedBB.calculateIntercept(startVec, endVec);

					if (mop != null) {
						double d1 = startVec.squareDistanceTo(mop.hitVec);

						if (d1 < d0 || d0 == -1) {
							entity = e;
							d0 = d1;
						}
					}
				}
			}


			if (!returning && entity != null) {
				if (entity.attackEntityFrom(new DamageSourceBoomerang(this, owner), 4.0F + 4 * getEnchantmentLevel(ItemBoomerang.SHARPNESS))) {
					if (entity instanceof EntityLivingBase && !(entity instanceof EntityEnderman)) {
						motionX = motionY = motionZ = 0;
						dataManager.set(DATAWATCHER_OUT_FLAG, (byte) 1);
						returning = true;

						EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
						if (owner instanceof EntityLivingBase) {
							EnchantmentHelper.applyThornEnchantments(entitylivingbase, owner);
							EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) owner, entitylivingbase);
						}

						if (StackHelper.isNonNull(potionStack)) {
							List<PotionEffect> potionEffects = PotionUtils.getEffectsFromStack(potionStack);
							for (PotionEffect potionEffect : potionEffects) {
								if (potionEffect.getPotion().isInstant()) {
									potionEffect.getPotion().affectEntity(this, owner, entitylivingbase, potionEffect.getAmplifier(), 0.25D);
								} else {
									entitylivingbase.addPotionEffect(
											new PotionEffect(
													potionEffect.getPotion(),
													potionEffect.getDuration() / 8,
													potionEffect.getAmplifier(),
													potionEffect.getIsAmbient(),
													potionEffect.doesShowParticles()));
								}
							}

						}

						int boom = getEnchantmentLevel(ItemBoomerang.EXPLODE);
						if (boom > 0) {
							world.createExplosion(owner, posX, posY, posZ, boom, false);
						}

						int flame = getEnchantmentLevel(ItemBoomerang.FLAMING);
						if (flame > 0) {
							entity.setFire(5);
						}


						if (owner != null && entity != owner && entity instanceof EntityPlayer && owner instanceof EntityPlayerMP) {
							((EntityPlayerMP) owner).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
						}
					}
				}
			}
		}

		if (movingobjectposition != null) {
			motionX = motionY = motionZ = 0;
			dataManager.set(DATAWATCHER_OUT_FLAG, (byte) 1);
			if (!returning && !world.isRemote) {
				int boom = getEnchantmentLevel(ItemBoomerang.EXPLODE);
				if (boom > 0) {
					Random rand = world.rand;
					world.createExplosion(this,
							posX + rand.nextGaussian() * 0.1,
							posY + rand.nextGaussian() * 0.1,
							posZ + rand.nextGaussian() * 0.1,
							boom, false);
				}
			}
		}


	}

	public void addItem(Entity entity) {
		if (isDead || entity == null || entity.isDead)
			return;

		EntityItem entityItem = entity instanceof EntityItem ? (EntityItem) entity : null;
		EntityXPOrb xp = entity instanceof EntityXPOrb ? (EntityXPOrb) entity : null;

		for (Entity checkEntity : getPassengers()) {
			if (entityItem != null && checkEntity instanceof EntityItem) {
				if (combine(entityItem, (EntityItem) checkEntity)) {
					world.removeEntity(entityItem);
					return;
				}
			}
			if (xp != null && checkEntity instanceof EntityXPOrb) {
				((EntityXPOrb) checkEntity).xpValue += xp.xpValue;
				world.removeEntity(xp);
				return;
			}
		}

		entity.startRiding(this);
	}

	public boolean combine(EntityItem adding, EntityItem current) {
		if (adding == current) return true;
		if (!adding.isEntityAlive() || !current.isEntityAlive())
			return true;

		ItemStack addingStack = adding.getItem();
		ItemStack currentStack = current.getItem();
		if (StackHelper.isNull(addingStack) || StackHelper.isNull(currentStack)) return true;

		if (!ItemHandlerHelper.canItemStacksStack(addingStack, currentStack)) {
			return false;
		}

		int allowedAmount = currentStack.getMaxStackSize() - StackHelper.getStacksize(currentStack);
		if (allowedAmount == 0) return false;

		int toAdd = StackHelper.getStacksize(addingStack);
		if (toAdd <= allowedAmount) {
			StackHelper.decrease(addingStack, toAdd);
			adding.setItem(addingStack);
			StackHelper.increase(currentStack, toAdd);
			current.setItem(currentStack);
			return true;
		} else {
			StackHelper.decrease(addingStack, allowedAmount);
			adding.setItem(addingStack);
			StackHelper.increase(currentStack, allowedAmount);
			current.setItem(currentStack);
			return false;
		}
	}


	public void setMeDead() {
		motionX = motionY = motionZ = 0;

		removePassengers();

		if (getRidingEntity() != null) {
			dismountRidingEntity();
		}

		world.removeEntity(this);
	}

	@Nonnull
	public Iterable<BlockPos> getNeighbourBlocks() {
		AxisAlignedBB expand = getEntityBoundingBox();
		return BlockPos.getAllInBox(
				new BlockPos(
						MathHelper.floor(expand.minX),
						MathHelper.floor(expand.minY),
						MathHelper.floor(expand.minZ)),
				new BlockPos(
						MathHelper.ceil(expand.maxX),
						MathHelper.ceil(expand.maxY),
						MathHelper.ceil(expand.maxZ))
		);
	}

	public boolean isOwner(Entity entity) {
		if (world.isRemote) {
			int i = dataManager.get(DATAWATCHER_OWNER_ID);
			return entity.getEntityId() == i;
		} else {
			return owner != null && owner.equals(entity.getUniqueID());
		}
	}


	public Entity getOwner() {
		WeakHashMap<Object, WeakReference<EntityBoomerang>> boomerangOwners;
		Entity entity;
		if (world.isRemote) {
			int i = dataManager.get(DATAWATCHER_OWNER_ID);
			entity = world.getEntityByID(i);
			boomerangOwners = EntityBoomerang.boomerangOwnersClient;
		} else {
			if (owner == null) return null;
			entity = ((WorldServer) world).getEntityFromUuid(owner);
			boomerangOwners = EntityBoomerang.boomerangOwners;
		}

		if (entity != null) {
			WeakReference<EntityBoomerang> reference = boomerangOwners.get(entity);
			if (reference == null || reference.get() == null) {
				boomerangOwners.put(entity, new WeakReference<>(this));
			}
		}
		return entity;
	}

	@Override
	public void setDead() {
		super.setDead();
		if (world.isRemote)
			boomerangOwnersClient.remove(getOwner());
		else
			boomerangOwners.remove(getOwner());
	}

	public Vec3d calcTargetVec() {
		if (world.isRemote) {
			int i = dataManager.get(DATAWATCHER_OWNER_ID);
			if (i != -1) {
				Entity entity = world.getEntityByID(i);
				if (entity != null) {
					return getEyeVec(entity);
				}
			}
		} else {
			if (owner != null) {
				Entity entity = ((WorldServer) world).getEntityFromUuid(owner);
				if (entity != null) {
					dataManager.set(DATAWATCHER_OWNER_ID, entity.getEntityId());
					Vec3d eyeVec = getEyeVec(entity);
					setHome((float) eyeVec.x, (float) eyeVec.y, (float) eyeVec.z);
					return eyeVec;
				}
			}

			dataManager.set(DATAWATCHER_OWNER_ID, -1);
		}

		return getHome();
	}

	@Nonnull
	public Vec3d getEyeVec(Entity entity) {
		return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight() * 0.8, entity.posZ);
	}


	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt(flyTime);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == DATAWATCHER_STACK.wrapper) {
			potionColor = -1;
		}
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		flyTime = additionalData.readInt();
	}

//	private static final DataParameter<String> PROFESSION_STR = EntityDataManager.<String>createKey(EntityVillager.class, DataSerializers.STRING);
//	private net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession prof;
//	public void notifyDataManagerChange(DataParameter<?> key) {
//		if(key == PROFESSION_STR)
//		prof = null;
//	}

	public class DamageSourceBoomerang extends EntityDamageSourceIndirect {

		public DamageSourceBoomerang(EntityBoomerang indirectEntityIn, Entity owner) {
			super("boomerang", indirectEntityIn, owner);
		}
	}
}
