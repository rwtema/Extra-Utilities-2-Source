package com.rwtema.extrautils2.modcompat;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.particles.ParticleWithering;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.tools.ranged.IAmmo;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;

import javax.annotation.Nullable;
import java.util.*;

public class XUTrait extends AbstractTrait {
	public XUTrait(String identifier, TextFormatting color, String name, @Nullable String tooltip) {
		super(identifier, color);
		register(name, tooltip);
	}

	public XUTrait(String identifier, int color, String name, @Nullable String tooltip) {
		super(identifier, color);
		register(name, tooltip);
	}

	public static boolean hasTrait(Entity entity, ITrait trait) {
		for (ItemStack stack : entity.getHeldEquipment()) {
			if (hasTrait(stack, trait)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasTrait(ItemStack tool, ITrait trait) {

		if (tool.getItem() instanceof ToolCore && !ToolHelper.isBroken(tool)) {
			NBTTagList list = TagUtil.getTraitsTagList(tool);
			for (int i = 0; i < list.tagCount(); i++) {
				ITrait toolTrait = TinkerRegistry.getTrait(list.getStringTagAt(i));
				if (toolTrait == trait) {
					return true;
				}
			}
		}
		return false;
	}

	public void register(String name, @Nullable String tooltip) {
		Lang.translate(String.format(LOC_Name, getIdentifier()), name);
		if (tooltip != null) {
			Lang.translate(String.format(LOC_Desc, getIdentifier()), tooltip);
		}
	}

	public static class TraitMagicalModifiers extends XUTrait {

		public TraitMagicalModifiers() {
			super("magical_modifier", 0xffafafaf, "Magically Modifiable", "Adds 3 extra modifiers to a tool");
		}

		@Override
		public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
			super.applyEffect(rootCompound, modifierTag);

			NBTTagList tagList = TagUtil.getModifiersTagList(rootCompound);
			int index = TinkerUtil.getIndexInCompoundList(tagList, getModifierIdentifier());

			NBTTagCompound tag = new NBTTagCompound();
			if (index > -1) {
				tag = tagList.getCompoundTagAt(index);
			} else {
				index = tagList.tagCount();
				tagList.appendTag(tag);
			}

			if (!tag.getBoolean(identifier)) {
				tag.setBoolean(identifier, true);
				tagList.set(index, tag);

				NBTTagCompound toolTag = TagUtil.getToolTag(rootCompound);
				int modifiers = toolTag.getInteger(Tags.FREE_MODIFIERS) + 3;
				toolTag.setInteger(Tags.FREE_MODIFIERS, Math.max(0, modifiers));
				TagUtil.setToolTag(rootCompound, toolTag);
			}
		}
	}

	public static class TraitBrittle extends XUTrait {

		public TraitBrittle() {
			super("brittle", 0xFF695433, "Brittle", "Has a random chance of breaking when it takes damage.");
		}

		@Override
		public int onToolDamage(ItemStack tool, int damage, int newDamage, EntityLivingBase entity) {
			int maxDurability = ToolHelper.getMaxDurability(tool);
			boolean flag = maxDurability <= 1;

			if (!flag) {
				Random rand = entity.world.rand;
				for (int i = 0; i < Math.max(1, damage); i++) {
					if (rand.nextInt(Math.max(100, maxDurability * maxDurability)) == 0) {
						flag = true;
						break;
					}
				}
			}

			if (flag) {
				ToolHelper.breakTool(tool, entity);
			}

			return super.onToolDamage(tool, damage, newDamage, entity);
		}
	}
//
//	public static class TraitGP extends XUTrait {
//
//		public TraitGP(String identifier, int color) {
//			super(identifier, color);
//		}
//
//		@Override
//		public void miningSpeed(ItemStack tool, PlayerEvent.BreakSpeed event) {
//			super.miningSpeed(tool, event);
//		}
//	}

	public static class TraitWithering extends XUTrait {
		static final int INNER_RANGE = 8 * 8;
		static final int OUTER_RANGE = 30 * 30;
		WeakHashMap<EntityPlayerMP, Integer> players = new WeakHashMap<>();

		public TraitWithering() {
			super("xu_withering", 0xFF343434, "Evil Aura", "When held, creates an aura of evil that brings misfortune to the wielder.");
			MinecraftForge.EVENT_BUS.register(this);
		}


		@SubscribeEvent
		public void spawnOverride(LivingSpawnEvent.CheckSpawn event) {
			if (players.isEmpty()) return;

			World world = event.getWorld();

			if (world.getDifficulty() == EnumDifficulty.PEACEFUL)
				return;
			if (world.rand.nextInt(40) != 0) return;

			EntityLiving entityLiving = (EntityLiving) event.getEntityLiving();

			if (!(entityLiving instanceof IMob)) return;

			for (EntityPlayerMP player : players.keySet()) {

				double distanceSqToEntity = entityLiving.getDistanceSqToEntity(player);
				if (distanceSqToEntity < OUTER_RANGE && distanceSqToEntity > INNER_RANGE) {
					if (!entityLiving.isNotColliding()) {
						return;
					}

					if (world.rand.nextInt(20) != 0) return;

					IBlockState iblockstate = world.getBlockState((new BlockPos(entityLiving)).down());
					if (!iblockstate.canEntitySpawn(entityLiving)) {
						return;
					}

					entityLiving.setAttackTarget(player);

					event.setResult(Event.Result.ALLOW);
					return;
				}
			}
		}

		@Override
		public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
			if (!isSelected && !(tool.getItem() instanceof IAmmo))
				return;

			if (!world.isRemote && entity instanceof EntityPlayerMP && !(entity instanceof FakePlayer)) {
				if (world.rand.nextInt(4) == 0) {
					BlockPos down = new BlockPos(entity).down();
					IBlockState blockState = world.getBlockState(down);
					if (blockState != Blocks.DIRT.getDefaultState())
						if (blockState.getMaterial() == Material.GRASS || blockState.getMaterial() == Material.GROUND) {
							Item itemDropped = blockState.getBlock().getItemDropped(blockState, random, 0);
							if (itemDropped == Item.getItemFromBlock(Blocks.DIRT)) {
								world.setBlockState(down, Blocks.DIRT.getDefaultState());
							}
						}
				}

				players.put((EntityPlayerMP) entity, 10);
			} else if (world.isRemote) {
				ExtraUtils2.proxy.run(new ClientRunnable() {
					@Override
					@SideOnly(Side.CLIENT)
					public void run() {
						AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
						Random rand = entity.world.rand;

						Minecraft.getMinecraft().effectRenderer.addEffect(
								new ParticleWithering(
										entity.world,
										entityBoundingBox.minX + (entityBoundingBox.maxX - entityBoundingBox.minX) * rand.nextFloat(),
										entityBoundingBox.minY + (entityBoundingBox.maxY - entityBoundingBox.minY) * rand.nextFloat() * 0.2F,
										entityBoundingBox.minZ + (entityBoundingBox.maxZ - entityBoundingBox.minZ) * rand.nextFloat()
								)
						);
					}
				});
			}
		}

		@SubscribeEvent
		public void onTick(TickEvent.ServerTickEvent event) {
			if (players.isEmpty()) return;
			Iterator<Map.Entry<EntityPlayerMP, Integer>> iterator = players.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<EntityPlayerMP, Integer> next = iterator.next();
				int value = next.getValue() - 1;
				if (value <= 0) {
					iterator.remove();
				} else {
					next.setValue(value);
				}
			}
		}

		@Override
		public int onToolDamage(ItemStack tool, int damage, int newDamage, EntityLivingBase entity) {
			if (entity instanceof FakePlayer) {
				ToolHelper.breakTool(tool, null);
			}
			return super.onToolDamage(tool, damage, newDamage, entity);
		}
	}

	public static class TraitChatty extends XUTrait {


		private final int TIME_BETWEEN_MESSAGES = 30 * 20;
		int timeSinceLastMessage = XURandom.rand.nextInt(TIME_BETWEEN_MESSAGES);

		public TraitChatty() {
			super("xu_whispering", TextFormatting.DARK_RED, "Whispering", "Will occasionally whisper it's will to you");
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
			if (!world.isRemote) return;
			if (!(entity instanceof EntityPlayer)) {
				return;
			}
			if (Minecraft.getMinecraft().player != entity) return;

			EntityPlayer player = (EntityPlayer) entity;

			timeSinceLastMessage--;

			if (timeSinceLastMessage < 0) {
				timeSinceLastMessage = TIME_BETWEEN_MESSAGES << 2 + player.world.rand.nextInt(TIME_BETWEEN_MESSAGES << 2);
				if (!isSelected) return;

				RayTraceResult objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
				if (objectMouseOver != null && objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY && objectMouseOver.entityHit instanceof EntityPlayer) {
					sendMessage(tool, player, Lang.chat("Kill!"));
				} else {

					sendMessage(tool, player, CollectionHelper.getRandomElementMulti(
							Lang.chat("Time for death and destruction?"),
							Lang.chat("I'm hungry, feed me."),
							Lang.chat("Hey you, let's go kill everything!"),
							Lang.chat("Murder! Death! Kill!"),
							Lang.chat("Hack n' slash! Hack n' slash! Hack n' slash! Hack n' slash! Hack n' slash!"),
							Lang.chat("Feast on their blood."),
							Lang.chat("I feel... sharp."),
							Lang.chat("I'm ready and willing."),
							Lang.chat("Stabby stabby stab!."),
							Lang.chat("Let the essence of life and death flow freely."),
							Lang.chat("This world is filled with such life and beauty. Let's go destroy it all.")
					));


				}
			}
		}

		private void sendMessage(ItemStack tool, EntityPlayer player, ITextComponent message) {
			TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("commands.message.display.incoming", tool.getDisplayName(), message);
			textcomponenttranslation.getStyle().setColor(TextFormatting.GRAY).setItalic(Boolean.TRUE);
			player.sendMessage(textcomponenttranslation);
		}

	}

	public static class TraitWitherHead extends XUTrait {
		public static final int ATTACK_COST = 10;

		public TraitWitherHead() {
			super("xu_wither_heads", 0xFF343434, "Wither Head Launcher", "Right click to launch Wither Heads at things!");
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void onClick(PlayerInteractEvent.RightClickItem event) {
			ItemStack tool = event.getItemStack();
			if (event.getWorld().isRemote) {
				return;
			}

			if (tool.getItem() instanceof ToolCore && !ToolHelper.isBroken(tool)) {
				int startDurability;
				if (ToolHelper.isBroken(tool) || (startDurability = ToolHelper.getCurrentDurability(tool)) < ATTACK_COST) {
					return;
				}

				NBTTagList list = TagUtil.getTraitsTagList(tool);
				for (int i = 0; i < list.tagCount(); i++) {
					ITrait trait = TinkerRegistry.getTrait(list.getStringTagAt(i));
					if (trait == this) {
						EntityPlayer player = event.getEntityPlayer();
						ToolHelper.damageTool(tool, ATTACK_COST, player);
						int newDurability = ToolHelper.getCurrentDurability(tool);

						World world = event.getWorld();

						int ammountTaken = startDurability - newDurability;
						if (ammountTaken == 0) return;

						event.setCanceled(true);

						if (world.rand.nextInt(ATTACK_COST) >= ammountTaken) {
							return;
						}

						float pitch = player.rotationPitch;
						float yaw = player.rotationYaw;
						float x = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
						float y = -MathHelper.sin(pitch * 0.017453292F);
						float z = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);

						EntityWitherSkull entitywitherskull = new EntityWitherSkull(world, player, x * 10, y * 10, z * 10);
						entitywitherskull.posY = player.posY + (double) player.getEyeHeight() - 0.1;
						entitywitherskull.posX = player.posX;
						entitywitherskull.posZ = player.posZ;
						entitywitherskull.rotationPitch = pitch;
						entitywitherskull.rotationYaw = yaw;
						world.spawnEntity(entitywitherskull);
						return;
					}
				}
			}
		}
	}

	public static class TraitExperience extends XUTrait {

		private final int MULTIPLIER = 4;

		public TraitExperience() {
			super("xu_xp_boost", 0xFFC4FF8F, "Experience Boost", "Gives bonus XP when mining or slaying mobs.");
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void onLivDie(LivingExperienceDropEvent event) {
			if (event.getAttackingPlayer() != null && hasTrait(event.getAttackingPlayer(), this)) {
				event.setDroppedExperience(event.getDroppedExperience() * MULTIPLIER);
			}
		}

		@SubscribeEvent
		public void onMine(BlockEvent.BreakEvent event) {
			if (hasTrait(event.getPlayer(), this)) {
				event.setExpToDrop(event.getExpToDrop() * MULTIPLIER);
			}
		}

		@Override
		public List<String> getExtraInfo(ItemStack tool, NBTTagCompound modifierTag) {
			return ImmutableList.of(Lang.translateArgs(false, String.format(LOC_Extra, getIdentifier()), "Gives %s XP boost", StringHelper.formatPercent(MULTIPLIER)));
		}
	}
}
