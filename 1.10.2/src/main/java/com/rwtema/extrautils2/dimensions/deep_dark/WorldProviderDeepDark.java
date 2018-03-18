package com.rwtema.extrautils2.dimensions.deep_dark;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.dimensions.XUWorldProvider;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldProviderDeepDark extends XUWorldProvider {
	static {
		MinecraftForge.EVENT_BUS.register(WorldProviderDeepDark.class);
	}

	ChunkProviderDeepDark chunkProviderDeepDark;


	public boolean hasSkyLight() {
		return false;
	}

	public WorldProviderDeepDark() {
		super(XU2Entries.deep_dark);
		this.doesWaterVaporize = false;
		this.nether = false;
		this.nether = true;
	}

	@SubscribeEvent
	public static void tickStart(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.START)
			return;

		World worldObj = event.player.world;
		if (!worldObj.isRemote
				&& worldObj.getTotalWorldTime() % 10 == 0
				&& worldObj.provider instanceof WorldProviderDeepDark
				&& event.player instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) event.player;
			int time = 0;

			NBTTagCompound entityData = player.getEntityData();
			if (entityData.hasKey("XU2|DarkTimer")) {
				time = entityData.getInteger("XU2|DarkTimer");
			}

			if (CompatHelper.getBrightness(player) < 0.03) {
				if (time > 100) {
					player.attackEntityFrom(DamageSourceDarkness.darkness, 1);
				} else {
					time += 1;
				}
			} else if (time > 0) {
				time -= 1;
			}

			if (time > 0) {
				entityData.setInteger("XU2|DarkTimer", time);
			} else {
				entityData.removeTag("XU2|DarkTimer");
			}
		}
	}

	@SubscribeEvent
	public static void noMobs(LivingSpawnEvent.CheckSpawn event) {
		if (event.getResult() == Event.Result.DEFAULT) {
			if (event.getWorld().provider instanceof WorldProviderDeepDark) {
				EntityLivingBase eventEntity = event.getEntityLiving();
				if (eventEntity instanceof EntityMob) {
					if (event.getWorld().rand.nextDouble() < Math.min(0.95, eventEntity.posY / 80)) {
						event.setResult(Event.Result.DENY);
					} else {
						EntityMob entity = (EntityMob) eventEntity;
						IAttributeInstance t = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
						t.setBaseValue(t.getBaseValue() * 2);
						entity.heal((float) t.getAttributeValue());
						t = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
						t.setBaseValue(t.getBaseValue() * 2);

					}
				}
			}
		}
	}

	@Override
	public long getSeed() {
		long seed = super.getSeed();
		if (chunkProviderDeepDark != null) {
			int i = chunkProviderDeepDark.seedOffset;
			if (i != 0) {
				return seed * 31 + i;
			}
		}
		return seed;
	}

	@Override
	public int getAverageGroundLevel() {
		return 81;
	}

	@Override
	public boolean doesXZShowFog(int x, int z) {
		return true;
	}

	@Override
	public boolean isSkyColored() {
		return false;
	}

	@Override
	public boolean canRespawnHere() {
		return false;
	}

	@Override
	public boolean isSurfaceWorld() {
		return false;
	}

	@Override
	public boolean canCoordinateBeSpawn(int x, int z) {
		return false;
	}

	@Nullable
	@Override
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		return null;
	}

	@Override
	@Nonnull
	public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
		return new Vec3d(0.00001, 0.00001, 0.00001);
	}

	@Override
	public float calculateCelestialAngle(long par1, float par3) {
		int j = 18000;
		float f1 = (j + par3) / 24000.0F - 0.25F;

		if (f1 < 0.0F) {
			f1 += 1.0F;
		}

		if (f1 > 1.0F) {
			f1 -= 1.0F;
		}

		float f2 = f1;
		f1 = 1.0F - (float) ((Math.cos(f1 * Math.PI) + 1.0D) / 2.0D);
		f1 = f2 + (f1 - f2) / 3.0F;
		return f1;
	}

	@Override
	@Nonnull
	public String getWelcomeMessage() {
		return Lang.translate("Entering the Deep Dark!");
	}

	@Nonnull
	@Override
	public ChunkProviderDeepDark createChunkGenerator() {
		chunkProviderDeepDark = new ChunkProviderDeepDark(world, getSeed());
		return chunkProviderDeepDark;
	}

	@Override
	protected void generateLightBrightnessTable() {
		float f = 0.0F;

		for (int i = 0; i <= 15; ++i) {
			float p = (i) / (15.0F);
			float f1 = 1.0F - p;
			this.lightBrightnessTable[i] = (p) / (f1 * 3.0F + 1.0F);

			if (this.lightBrightnessTable[i] < 0.2F) {
				this.lightBrightnessTable[i] *= this.lightBrightnessTable[i] / 0.2F;
			}
		}
	}

	public static class DamageSourceDarkness extends DamageSource {
		public static DamageSourceDarkness darkness = new DamageSourceDarkness();

		protected DamageSourceDarkness() {
			super("darkness");
			this.setDamageBypassesArmor();
		}
	}
}
