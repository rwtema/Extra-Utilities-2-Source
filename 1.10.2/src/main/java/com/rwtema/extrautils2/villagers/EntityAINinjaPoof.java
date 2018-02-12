package com.rwtema.extrautils2.villagers;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import java.util.List;
import javax.annotation.Nullable;

import com.rwtema.extrautils2.compatibility.CompatHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public class EntityAINinjaPoof extends EntityAIAvoidEntity<EntityPlayer> {
	private final EntityVillager villager;
	private final float avoidDistance;
	private Predicate<Entity> canBeSeenSelector;

	public EntityAINinjaPoof(EntityVillager villager, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
		super(villager, EntityPlayer.class, avoidDistanceIn, farSpeedIn, nearSpeedIn);
		this.villager = villager;
		this.avoidDistance = avoidDistanceIn;
		this.canBeSeenSelector = new Predicate<Entity>() {
			@Override
			public boolean apply(@Nullable Entity p_apply_1_) {
				return p_apply_1_ != null && p_apply_1_.isEntityAlive() && EntityAINinjaPoof.this.villager.getEntitySenses().canSee(p_apply_1_);
			}
		};
	}

	@Override
	public void updateTask() {
		super.updateTask();
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		if (!villager.isPotionActive(MobEffects.INVISIBILITY)) {
			CompatHelper.addPotionEffect(villager.world, new BlockPos(villager), PotionTypes.INVISIBILITY);

			for (PotionEffect potionEffect : PotionTypes.INVISIBILITY.getEffects()) {
				villager.addPotionEffect(new PotionEffect(potionEffect));
			}
		}
	}

	@Override
	public void resetTask() {
		super.resetTask();

		World worldObj = this.entity.world;
		if (worldObj == null) return;

		List<EntityPlayer> list = worldObj.getEntitiesWithinAABB(EntityPlayer.class,
				this.entity.getEntityBoundingBox().grow(this.avoidDistance, 3.0D, this.avoidDistance),
				Predicates.and(EntitySelectors.CAN_AI_TARGET, this.canBeSeenSelector));
		if (list.isEmpty()) {
			if (villager.wealth > 0)
				villager.wealth--;
		}
	}

	@Override
	public boolean shouldExecute() {
		return villager.wealth != 0 &&
				!villager.isTrading() &&
				villager.getProfessionForge() == XU2Entries.shadyMerchant.value &&
				super.shouldExecute();
	}

	public static class Handler {

		@SubscribeEvent
		public void a(EntityJoinWorldEvent event) {
			World world = event.getWorld();
			if (world == null || world.isRemote) return;

			Entity base = event.getEntity();
			if (base.getClass() == EntityVillager.class) {
				EntityVillager villager = (EntityVillager) base;
				villager.tasks.addTask(1, new EntityAINinjaPoof(villager, 16, 0.7F, 0.7F));
			}
		}

		@SubscribeEvent(priority = EventPriority.HIGH)
		public void getDrops(LivingDropsEvent event) {
			EntityLivingBase living = event.getEntityLiving();
			if (!(living instanceof EntityVillager)) return;

			if (!living.world.getGameRules().getBoolean("doMobLoot")) return;

			EntityVillager villager = (EntityVillager) living;
			VillagerRegistry.VillagerProfession professionForge = villager.getProfessionForge();
			if (villager.wealth > 0 && professionForge == XU2Entries.shadyMerchant.value) {
				while (villager.wealth > 0) {
					int i = villager.world.rand.nextInt(5) + 4;

					if (i > villager.wealth) {
						i = villager.wealth;
					}

					villager.wealth -= i;
					EntityItem entityitem = new EntityItem(villager.world, villager.posX, villager.posY, villager.posZ, new ItemStack(Items.EMERALD, i));

					entityitem.setDefaultPickupDelay();
					event.getDrops().add(entityitem);
				}
			}

		}
	}
}
