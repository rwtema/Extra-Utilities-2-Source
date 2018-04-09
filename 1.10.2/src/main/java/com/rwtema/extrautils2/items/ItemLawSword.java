package com.rwtema.extrautils2.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import gnu.trove.set.hash.THashSet;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.*;

public class ItemLawSword extends ItemSword implements IXUItem {
	public static final UUID soulDamageUUID = UUID.fromString("2CCDC290-A885-473A-973F-CDC5C918773B");
	public static final UUID myModifier = UUID.fromString("3D0B4C2D-58EA-439E-83E0-26CFC61D1124");
	public static final ToolMaterial material = EnumHelper.addToolMaterial("Ti-Tema-ian", 6, 2048, 10, 10, 22);
	public static BaseAttribute godSlayingDamage = new RangedAttribute(null, "extrautils2.armorpiercingattackdamage", 0.0D, 0.0D, Double.MAX_VALUE);
	public static BaseAttribute armorPiercingDamage = new RangedAttribute(null, "extrautils2.godslayingattackdamage", 0.0D, 0.0D, Double.MAX_VALUE);
	public static BaseAttribute soulDamage = new RangedAttribute(null, "extrautils2.souldamage", 0.0D, 0.0D, Double.MAX_VALUE);
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite sprite;
	private BoxModel sword;

	public ItemLawSword() {
		super(material);
		EventHandlerSword handler = new EventHandlerSword(this);
		MinecraftForge.EVENT_BUS.register(handler);
		MinecraftForge.EVENT_BUS.register(new OPAnvilHandler(this));
		setMaxStackSize(1);
		this.setMaxDamage(0);
	}

	@Nonnull
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(armorPiercingDamage.getName(), new AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 3, 0));
			multimap.put(godSlayingDamage.getName(), new AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 2, 0));
			multimap.put(soulDamage.getName(), new AttributeModifier(myModifier, "Weapon modifier", 10 / 39.0, 0));
		}
		return multimap;
	}
//
//	@Override
//	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
//		super.addInformation(stack, playerIn, tooltip, advanced);
//		tooltip.add(Lang.translate("Cursed Sword"));
//	}


	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, @Nonnull EntityLivingBase attacker) {
		if (target == null || !target.canBeAttackedWithItem()) return false;
		if (attacker.world.isRemote) return false;
		if (!(attacker instanceof EntityPlayer)) return false;

		double[] m = new double[]{target.motionX, target.motionY, target.motionZ};

		Multimap<String, AttributeModifier> multimap = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);

		boolean flag = !target.isEntityInvulnerable(DamageSource.ANVIL);
		if (target instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) target;
			if (player.capabilities.isCreativeMode)
				flag = false;
			if (!PlayerHelper.isPlayerReal(player))
				flag = false;
		}
		if (flag)
			drainHealth(target);

		attack(stack, target, multimap, godSlayingDamage, new DamageSourceDivine(attacker, true), attacker);
		attack(stack, target, multimap, armorPiercingDamage, new DamageSourceArmorBypass(attacker), attacker);

		target.motionX = m[0];
		target.motionY = m[1];
		target.motionZ = m[2];
		target.velocityChanged = true;
		return false;
	}

	private void drainHealth(EntityLivingBase target) {
		double l = 0;

		IAttributeInstance a = target.getAttributeMap().getAttributeInstanceByName(SharedMonsterAttributes.MAX_HEALTH.getName());

		AttributeModifier attr = a.getModifier(soulDamageUUID);
		if (attr != null) {
			l = attr.getAmount();
			if (l == -1) return;
		}

		l -= 1 / 39F;

		if (l < -1) l = -1;

		Multimap<String, AttributeModifier> multimap = HashMultimap.create();
		multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier(soulDamageUUID, "Soul Damage", l, 2));
		target.getAttributeMap().applyAttributeModifiers(multimap);
		if (l <= -1) {
			target.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
		}

	}

	public void attack(ItemStack stack, EntityLivingBase target, Multimap<String, AttributeModifier> multimap, BaseAttribute attribute, DamageSource source, EntityLivingBase attacker) {
		float amount = getAmount(stack, target, multimap, attribute);
		if (amount > 0) {
			amount *= ((EntityPlayer) attacker).getCooledAttackStrength(0);
			target.hurtResistantTime = 0;
			target.attackEntityFrom(source, amount);
		}
	}

	public float getAmount(ItemStack stack, EntityLivingBase target, Multimap<String, AttributeModifier> multimap, BaseAttribute attribute) {
		Collection<AttributeModifier> gsd;

		float amount = 0;
		gsd = multimap.get(attribute.getName());

		if (gsd != null)
			for (AttributeModifier t : gsd) {
				float d0 = (float) t.getAmount();
				if (t.getID() == Item.ATTACK_DAMAGE_MODIFIER) {
					d0 += (double) EnchantmentHelper.getModifierForCreature(stack, target.getCreatureAttribute());
				}
				amount += d0;
			}
		return amount;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register("rwtema_blade");
	}

	@Override
	public void clearCaches() {
		sword = null;
		sprite = null;
	}

	@Override
	public boolean allowOverride() {
		return false;
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void postTextureRegister() {
		sprite = Textures.sprites.get("rwtema_blade");
	}

	@Override
	public boolean renderAsTool() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {
		return sprite;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack) {
		if (this.sword == null) {
			float sc = 16F / 32F;
			float[] handleUV = new float[]{0 * 16F, 0 * 16F, 5F * sc, 16 * sc};
			float[] handleBottomUV = new float[]{9 * sc, 21F * sc, 14F * sc, 26F * sc};
			float[] fuzzTopUV = new float[]{5 * sc, 0 * 16F, 18F * sc, 13F * sc};
			float[] fuzzSideUV = new float[]{5 * sc, 13F * sc, 18F * sc, 17F * sc};
			float[] fuzzBottomUV = new float[]{5 * sc, 17F * sc, 18F * sc, 30F * sc};

			float[] swordUV1 = {0F * sc, 16F * sc, 4F * sc, 32F * sc};
			float[] swordUV2 = {18F * sc, 0F * sc, 22F * sc, 32F * sc};
			float[] swordUV3 = {24F * sc, 0F * sc, 28F * sc, 16F * sc};

			float r = 2.5F / 84F;
			float dy = -0.25F;
			BoxModel sword;
			sword = new BoxModel();

			float s = 1 / 512F;

			sword.add(new Box(0.5F - 2.5F * r, dy, 0.5F - 2.5F * r, 0.5F + 2.5F * r, dy + 16 * r, 0.5F + 2.5F * r).setTexture("rwtema_blade").setTextureBounds(new float[][]{
					handleBottomUV, handleBottomUV,
					handleUV, handleUV, handleUV, handleUV
			}).setInvisible(2));

			sword.add(new Box(0.5F - 6.5f * r, dy + 16F * r, 0.5F - 6.5f * r, 0.5F + 6.5f * r, dy + 20F * r, 0.5F + 6.5f * r).setTexture("rwtema_blade").setTextureBounds(new float[][]{
					fuzzBottomUV, fuzzTopUV,
					fuzzSideUV, fuzzSideUV, fuzzSideUV, fuzzSideUV
			}));

			sword.add(new Box(0.5F - s, dy + 20F * r, 0.5F - 2F * r, 0.5F + s, dy + 36F * r, 0.5F + 2F * r).setTexture("rwtema_blade").setTextureBounds(new float[][]{
					null, null,
					swordUV1, swordUV1, swordUV1, swordUV1
			}).setInvisible(15).setFlipU(4));

			sword.add(new Box(0.5F - s, dy + 36F * r, 0.5F - 2F * r, 0.5F + s, dy + 68F * r, 0.5F + 2F * r).setTexture("rwtema_blade").setTextureBounds(new float[][]{
					null, null,
					swordUV2, swordUV2, swordUV2, swordUV2
			}).setInvisible(15).setFlipU(4));

			sword.add(new Box(0.5F - s, dy + 68F * r, 0.5F - 2F * r, 0.5F + s, dy + 84.0F * r, 0.5F + 2F * r).setTexture("rwtema_blade").setTextureBounds(new float[][]{
					null, null,
					swordUV3, swordUV3, swordUV3, swordUV3
			}).setInvisible(15).setFlipU(4));

			this.sword = sword;
		}

		model.clear();
		model.isGui3D = true;
		model.tex = sprite;
		model.addBoxModel(sword);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createModel(int metadata) {
		EnumMap<ItemCameraTransforms.TransformType, javax.vecmath.Matrix4f> enumMap = new EnumMap<>(Transforms.blockTransforms);
		for (ItemCameraTransforms.TransformType type : ItemCameraTransforms.TransformType.values()) {
			Matrix4f matrix4f = Transforms.itemTransforms.get(type);
			if (matrix4f != null) {
				enumMap.put(type, matrix4f);
			}
		}

		return new PassthruModelItem(this, enumMap);
	}

	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int slot, boolean selected) {
		if (world.isRemote)
			return;

		if (!(entity instanceof EntityPlayerMP))
			return;

		NBTTagCompound nbt = NBTHelper.getPersistantNBT(entity);
		nbt.setByte("XU|Sword", (byte) 20);

		EntityPlayerMP entityPlayerMP = (EntityPlayerMP) entity;
		EventHandlerSword.addPlayer(entityPlayerMP, !nbt.hasKey("XU|SwordDim") || nbt.getInteger("XU|SwordDim") != world.provider.getDimension());


		nbt.setInteger("XU|SwordDim", world.provider.getDimension());
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return true;
	}

	public static class DamageSourceArmorBypass extends EntityDamageSource {
		public DamageSourceArmorBypass(Entity entity) {
			super("player", entity);
			this.setDamageBypassesArmor();
			this.setDamageIsAbsolute();
		}
	}

	public static class DamageSourceDivine extends EntityDamageSource {
		public DamageSourceDivine(Entity entity, boolean creative) {
			super("player", entity);
			this.setDamageBypassesArmor();
			this.setDamageIsAbsolute();
			this.setDamageAllowedInCreativeMode();
		}
	}

	public static class EventHandlerSword {
		public static THashSet<String> serverLawSwords = new THashSet<>(5, 0.5F);
		public static THashSet<String> clientLawSwords = new THashSet<>(5, 0.5F);
		private final ItemLawSword itemLawSword;

		public EventHandlerSword(ItemLawSword itemLawSword) {

			this.itemLawSword = itemLawSword;
		}

		public static void addPlayer(EntityPlayer player, boolean b) {
			String name = player.getGameProfile().getName();
			if (!serverLawSwords.contains(name) || b) {
				serverLawSwords.add(name);
				NetworkHandler.sendToAllPlayers(new PacketLawSwordNotifier(name, true));
			}
		}

		public static void removePlayer(EntityPlayer player) {
			String name = player.getGameProfile().getName();
			if (serverLawSwords.contains(name)) {
				serverLawSwords.remove(name);
				NetworkHandler.sendToAllPlayers(new PacketLawSwordNotifier(name, false));
			}
		}

		@SubscribeEvent
		public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
			for (String name : FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames()) {
				NetworkHandler.sendPacketToPlayer(new PacketLawSwordNotifier(name, serverLawSwords.contains(name)), event.player);
			}
		}

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public void onRun(GuiScreenEvent.InitGuiEvent.Post event) {
			GuiScreen gui = event.getGui();
			if (gui instanceof GuiWinGame) {
				try {
					List<String> lines = ObfuscationReflectionHelper.getPrivateValue(GuiWinGame.class, (GuiWinGame) gui, "field_146582_i");
					char[] test = "IlikebigbuttsandIcannotliethismessagevandelisedbyRWTema".toCharArray();
					int char_index = 0;
					main:
					for (int line_index = 0; line_index < lines.size(); line_index++) {
						String line = lines.get(line_index);
						char v = test[char_index];
						for (char c : new char[]{Character.toLowerCase(v), Character.toUpperCase(v)}) {
							int k = line.indexOf(c);
							if (k >= 0) {
								StringBuilder formatting = new StringBuilder(ChatFormatting.RESET.toString());
								for (int i2 = 0; i2 < k; i2++) {
									if (line.charAt(i2) == ChatFormatting.PREFIX_CODE) {
										formatting = formatting.append(ChatFormatting.PREFIX_CODE).append(line.charAt(i2 + 1));
									}
								}

								lines.set(line_index, line.replaceFirst(String.valueOf(c), ChatFormatting.RESET.toString() + ChatFormatting.WHITE.toString() + "" + c + "" + formatting));

								char_index++;

								if (char_index >= test.length) {
									break main;
								}

								break;
							}
						}

					}
				} catch (ReflectionHelper.UnableToFindFieldException ignore) {

				}
			}
		}

		@SubscribeEvent
		public void entTick(LivingEvent.LivingUpdateEvent event) {
			if (event.getEntity().world.isRemote)
				return;

			if (MCTimer.serverTimer % (20 * 10) == 0) {
				IAttributeInstance a = event.getEntityLiving().getAttributeMap().getAttributeInstanceByName(SharedMonsterAttributes.MAX_HEALTH.getName());

				AttributeModifier attr = a.getModifier(soulDamageUUID);
				if (attr != null) {
					double l = attr.getAmount();
					l = (Math.round(l * 39D) + 1D) / 39D;
					a.removeModifier(attr);
					if (l < 0) {
						a.applyModifier(new AttributeModifier(soulDamageUUID, "Soul Damage", l, 2));
					}
				}
			}

			Entity entity = event.getEntity();
			if (!(entity instanceof EntityPlayer) || !NBTHelper.hasPersistantNBT(entity))
				return;

			NBTTagCompound tagCompound = NBTHelper.getPersistantNBT(entity);

			if (!tagCompound.hasKey("XU|Sword", 1))
				return;

			Byte t = tagCompound.getByte("XU|Sword");
			t--;

			if (t == 0) {
				tagCompound.removeTag("XU|Sword");

				if (entity instanceof EntityPlayerMP) {
					EntityPlayerMP entityPlayer = ((EntityPlayerMP) entity);
					removePlayer(entityPlayer);
				}
			} else {
				tagCompound.setByte("XU|Sword", t);
			}
		}

		public void renderPlayer(RenderPlayerEvent.Pre event) {

		}
	}

	@NetworkHandler.XUPacket
	public static class PacketLawSwordNotifier extends XUPacketServerToClient {
		String username;
		boolean swordPresent;

		public PacketLawSwordNotifier() {

		}

		public PacketLawSwordNotifier(String player, boolean swordPresent) {
			this.username = player;
			this.swordPresent = swordPresent;
		}

		@Override
		public void writeData() throws Exception {
			writeString(username);
			data.writeBoolean(swordPresent);
		}

		@Override
		public void readData(EntityPlayer player) {
			username = readString();
			swordPresent = data.readBoolean();
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			return new Runnable() {
				@Override
				public void run() {
					if (swordPresent) {
						EventHandlerSword.clientLawSwords.add(username);
					} else {
						EventHandlerSword.clientLawSwords.remove(username);
					}
				}
			};
		}
	}


	public static class OPAnvilHandler {

		@Nonnull
		private final Item item;

		public OPAnvilHandler(@Nonnull Item item) {
			this.item = item;
		}

		@SubscribeEvent
		public void anvil(AnvilUpdateEvent event) {
			ItemStack left = event.getLeft();
			ItemStack right = event.getRight();
			if (StackHelper.isNull(left) || left.getItem() != item || StackHelper.isNull(right))
				return;

			Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(left);
			Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(right);
			if (map2.isEmpty()) return;

			Map<Enchantment, Integer> map3 = new HashMap<>(map1);

			int cost = 0;

			for (Map.Entry<Enchantment, Integer> entry : map2.entrySet()) {
				Enchantment enchantment = entry.getKey();
				if (enchantment == null) continue;

				Integer curValue = map1.get(entry.getKey());
				Integer addValue = entry.getValue();
				if (curValue == null) {
					cost += addValue;
					map3.put(entry.getKey(), addValue);
				} else {
					int value = Math.min(curValue + addValue, enchantment.getMaxLevel() * 2);
					cost += value - curValue;
					map3.put(entry.getKey(), value);
				}
			}

			event.setCost(cost * 2);

			ItemStack copy = left.copy();
			EnchantmentHelper.setEnchantments(map3, copy);
			event.setOutput(copy);
		}

		@SubscribeEvent
		public void playerLogin(PlayerEvent.PlayerRespawnEvent event) {
			cheatsyTema(event.player);
		}

		@SubscribeEvent
		public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
			cheatsyTema(event.player);
		}

		private void cheatsyTema(EntityPlayer player) {
			if (PlayerHelper.isThisPlayerACheatyBastardOfCheatBastardness(player)) {
				InventoryPlayer inventory = player.inventory;
				int j = -1;
				for (int i = 0; i < inventory.getSizeInventory(); i++) {
					ItemStack stack = inventory.getStackInSlot(i);
					if (StackHelper.isNull(stack)) {
						if (j == -1) j = i;
					} else if (stack.getItem() == item) {
						return;
					}
				}

				if (j != -1) {
					inventory.setInventorySlotContents(j, new ItemStack(item));
				}
			}
		}
	}
}
