package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class ItemCompoundBow extends ItemBow implements IXUItem {
	public static final float DRAW_TIME = 20.0F;
	private static WeakLinkedSet<EntityArrow> blue_arrows = new WeakLinkedSet<>();

	static {
		MinecraftForge.EVENT_BUS.register(ItemCompoundBow.class);
	}

	String[] tex = new String[]{"compound_bow",
			"compound_bow_pull_0",
			"compound_bow_pull_1",
			"compound_bow_pull_2",
			"compound_bow_pull_charged"};

	public ItemCompoundBow() {
		MinecraftForge.EVENT_BUS.register(new ItemLawSword.OPAnvilHandler(this));
		setMaxStackSize(1);
		this.setMaxDamage(0);
	}

	private static float getArrowVelocityCustom(int charge) {
		float f = (float) charge / DRAW_TIME;

		if (f < 0) return 0;

		f = (f + (float) Math.sqrt(f) * 2.0F) / 3.0F;

		if (f > 1.0F) {
			f = 1.0F;
		}

		return f;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void tickClient(TickEvent.ClientTickEvent event) {
		if (Minecraft.getMinecraft().isGamePaused()) return;
		for (Iterator<EntityArrow> iterator = blue_arrows.iterator(); iterator.hasNext(); ) {
			EntityArrow blue_arrow = iterator.next();
			if (blue_arrow.isDead || blue_arrow.inGround) {
				iterator.remove();
			} else
				for (int i = 0; i < 5; i++) {
					for (int k = 0; k < 4; ++k) {
						double r = i / 5.0 * 0.1;
						blue_arrow.world.spawnParticle(EnumParticleTypes.REDSTONE,
								blue_arrow.posX + blue_arrow.motionX * (double) k / 4.0D + blue_arrow.world.rand.nextGaussian() * r,
								blue_arrow.posY + blue_arrow.motionY * (double) k / 4.0D + blue_arrow.world.rand.nextGaussian() * r,
								blue_arrow.posZ + blue_arrow.motionZ * (double) k / 4.0D + blue_arrow.world.rand.nextGaussian() * r,
								92 / 255F, 151 / 255F, 224 / 255F);
					}
				}
		}
	}

	private ItemStack findAmmunition(EntityPlayer player) {
		if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
			return player.getHeldItem(EnumHand.OFF_HAND);
		} else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
			return player.getHeldItem(EnumHand.MAIN_HAND);
		} else {
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (this.isArrow(itemstack)) {
					return itemstack;
				}
			}

			return StackHelper.empty();
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, @Nonnull World worldIn, EntityLivingBase entityLiving, int timeLeft) {
		if (!(entityLiving instanceof EntityPlayer)) return;

		EntityPlayer entityplayer = (EntityPlayer) entityLiving;
		boolean flag = entityplayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
		ItemStack ammunition = this.findAmmunition(entityplayer);

		int i = this.getMaxItemUseDuration(stack) - timeLeft;
		i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, entityplayer, i, StackHelper.isNonNull(ammunition) || flag);
		if (i < 0) return;

		if (!StackHelper.isNonNull(ammunition)) {
			if (flag) {
				ammunition = new ItemStack(Items.ARROW);
			} else {
				return;
			}
		}

		float f = getArrowVelocityCustom(i);

		if (f < 0.1F) {
			return;
		}

		boolean isInfinite = flag || entityplayer.capabilities.isCreativeMode || (ammunition.getItem() instanceof ItemArrow && ((ItemArrow) ammunition.getItem()).isInfinite(ammunition, stack, entityplayer));

		if (!worldIn.isRemote) {
			ItemArrow itemarrow = (ItemArrow) (ammunition.getItem() instanceof ItemArrow ? ammunition.getItem() : Items.ARROW);
			EntityArrow entityarrow = itemarrow.createArrow(worldIn, ammunition, entityplayer);
			entityarrow.setAim(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F, f * 4.0F, 1.0F);

			if (f == 1.0F) {
//				entityarrow.setIsCritical(true);
				entityarrow.setNoGravity(true);
			}

			entityarrow.setDamage(entityarrow.getDamage() + f * 2D);

			int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

			if (j > 0) {
				entityarrow.setDamage(entityarrow.getDamage() + (double) j * 0.5D + 0.5D);
			}

			int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);

			if (k > 0) {
				entityarrow.setKnockbackStrength(k);
			}

			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
				entityarrow.setFire(100);
			}

			stack.damageItem(1, entityplayer);

			if (isInfinite || entityplayer.capabilities.isCreativeMode && (ammunition.getItem() == Items.SPECTRAL_ARROW || ammunition.getItem() == Items.TIPPED_ARROW)) {
				entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
			}

			if (worldIn.spawnEntity(entityarrow)) {
				if (f == 1.0F) {
					PacketBlueArrow packetBlueArrow = new PacketBlueArrow(entityarrow.getEntityId());
					((WorldServer) worldIn).getEntityTracker().sendToTracking(entityarrow, NetworkHandler.channels.get(Side.SERVER).generatePacketFrom(packetBlueArrow));
				}
			}
		}

		worldIn.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

		if (!isInfinite && !entityplayer.capabilities.isCreativeMode) {
			StackHelper.decrease(ammunition);

			if (StackHelper.getStacksize(ammunition) == 0) {
				entityplayer.inventory.deleteStack(ammunition);
			}
		}

		entityplayer.addStat(StatList.getObjectUseStats(this));
	}

	@Override
	public boolean renderAsTool() {
		return true;
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	public void registerTextures() {
		Textures.register(tex);
	}

	@Override
	public IBakedModel createModel(int metadata) {
		return new PassthruModelItem(this);
	}

	@Override
	public TextureAtlasSprite getBaseTexture() {
		return Textures.getSprite(tex[0]);
	}

	@Override
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack, World world, EntityLivingBase entityIn) {
		int val;
		if (entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack) {
			float v = (float) (stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / DRAW_TIME;
			if (v < 0.4) {
				val = 1;
			} else if (v < 0.82) {
				val = 2;
			} else if (v < 1) {
				val = 3;
			} else {
				val = 4;
			}
		} else {
			val = 0;
		}

		model.addSprite(Textures.getSprite(tex[val]));
	}

	@Override
	public void postTextureRegister() {

	}

	@Override
	public void clearCaches() {

	}

	@Override
	public boolean allowOverride() {
		return true;
	}

	@NetworkHandler.XUPacket
	public static class PacketBlueArrow extends XUPacketServerToClient {
		private int entityID;

		public PacketBlueArrow(int entityID) {
			this.entityID = entityID;
		}

		public PacketBlueArrow() {
		}

		@Override
		public void writeData() throws Exception {
			writeInt(entityID);
		}

		@Override
		public void readData(EntityPlayer player) {
			entityID = readInt();
		}

		@Override
		public Runnable doStuffClient() {
			return new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					Entity entityByID = Minecraft.getMinecraft().world.getEntityByID(entityID);
					if (entityByID instanceof EntityArrow) {
						blue_arrows.add((EntityArrow) entityByID);
					}
				}
			};
		}
	}

}
