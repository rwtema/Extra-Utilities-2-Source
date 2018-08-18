package com.rwtema.extrautils2.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.blocks.LuxColors;
import com.rwtema.extrautils2.transfernodes.FacingHelper;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.client.GLStateAttributes;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.*;

public class ItemLuxSaber extends ItemSword implements IXUItem {

	public static final int ENERGY_PER_HIT = 200;
	public static final int NUM_HITS = 200;
	public static final float OFFSET = 0.05F;
	@Nonnull
	public static final ToolMaterial material = Validate.notNull(EnumHelper.addToolMaterial(
			"LuxSaber",
			6,
			2048,
			5,
			7,
			15));
	public static final UUID UUID = java.util.UUID.fromString("8126703D-7BDC-4E0A-98C6-C8FD511FE3A8");
	final WeakHashMap<ItemStack, Float> stackMap = new WeakHashMap<>();
	final ThreadLocal<Boolean> bladeOnly = ThreadLocal.withInitial(() -> false);
	@SideOnly(Side.CLIENT)
	ItemCameraTransforms.TransformType[] transformTypes;
	@SideOnly(Side.CLIENT)
	private TextureAtlasSprite blank;

	public ItemLuxSaber() {
		super(material);
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for (int i = 0; i < LuxColors.values().length; i++) {
				items.add(new ItemStack(this, 1, i));
				ItemStack stack = new ItemStack(this, 1, i);
				XUItemEnergyStorage xuItemEnergyStorage = (XUItemEnergyStorage)Validate.notNull(stack.getCapability(CapabilityEnergy.ENERGY, null));
				xuItemEnergyStorage.setEnergyStored(xuItemEnergyStorage.getMaxEnergyStored());
				items.add(stack);
			}

		}
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound != null) {
			IEnergyStorage storage = Validate.notNull(stack.getCapability(CapabilityEnergy.ENERGY, null));
			return 1 - storage.getEnergyStored() / (double) storage.getMaxEnergyStored();
		}
		return super.getDurabilityForDisplay(stack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
		return LuxColors.values()[stack.getMetadata() % LuxColors.values().length].color;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		transformTypes = new ItemCameraTransforms.TransformType[]{
				ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
				ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
				ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND,
				ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
		};
		Textures.register("luxsaber", "blank");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createModel(int metadata) {
		EnumMap<ItemCameraTransforms.TransformType, Matrix4f> enumMap = new EnumMap<>(Transforms.blockTransforms);
		for (ItemCameraTransforms.TransformType type : ItemCameraTransforms.TransformType.values()) {
			Matrix4f matrix4f = Transforms.itemTransforms.get(type);
			if (matrix4f != null) {
				enumMap.put(type, matrix4f);
			}
		}

		return new PassthruModelItem(this, () -> new PassthruModelItem.ModelLayer(enumMap) {
			@Override
			public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
				if (bladeOnly.get()) {
					if (metadata == LuxColors.BLACK.ordinal()) {
						GlStateManager.disableTexture2D();
						GlStateManager.tryBlendFuncSeparate(
								GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
								GlStateManager.DestFactor.ZERO,
								GlStateManager.SourceFactor.ONE,
								GlStateManager.DestFactor.ZERO);
					} else {
						GlStateManager.disableLighting();
						GlStateManager.tryBlendFuncSeparate(
								GlStateManager.SourceFactor.ONE,
								GlStateManager.DestFactor.ONE,
								GlStateManager.SourceFactor.ONE,
								GlStateManager.DestFactor.ZERO);
					}
				}
				return super.handlePerspective(cameraTransformType);
			}
		});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void postTextureRegister() {
		blank = Textures.getSprite("blank");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {

		return blank;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsTool() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clearCaches() {
		blank = null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean allowOverride() {
		return false;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, @Nonnull EntityLivingBase attacker) {

		int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);

		if (!EnchantmentDurability.negateDamage(stack, i, target.getRNG())) {
			XUItemEnergyStorage capability = (XUItemEnergyStorage) stack.getCapability(CapabilityEnergy.ENERGY, null);
			if (capability == null) throw new IllegalStateException();
			capability.setEnergyStored(Math.max(0, capability.getEnergyStored() - ENERGY_PER_HIT));
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		IEnergyStorage storage = Validate.notNull(stack.getCapability(CapabilityEnergy.ENERGY, null));
		tooltip.add(String.format("%s RF / %s RF",
				StringHelper.format(storage.getEnergyStored()),
				StringHelper.format(storage.getMaxEnergyStored())));
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		return new XUItemEnergyStorage(stack, ENERGY_PER_HIT * NUM_HITS, ENERGY_PER_HIT, 0);
	}

	@Nonnull
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, ItemStack stack) {
		IEnergyStorage capability = stack.getCapability(CapabilityEnergy.ENERGY, null);
		Multimap<String, AttributeModifier> itemAttributeModifiers;
		if (slot != EntityEquipmentSlot.MAINHAND) {
			return HashMultimap.create();
		}

		if (capability == null || capability.getEnergyStored() <= 0) {
			itemAttributeModifiers = HashMultimap.create();
		} else {
			itemAttributeModifiers = this.getItemAttributeModifiers(slot);
		}
		itemAttributeModifiers.put(EntityPlayer.REACH_DISTANCE.getName(), new AttributeModifier(UUID, "Long Swipe", 3, 0));
		return itemAttributeModifiers;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack, World world, EntityLivingBase entity) {
		float[] saberHiltBottom = {24 / 2F, 27 / 2F, 28 / 2F, 31 / 2F};
		float[] saberHiltTop = {28 / 2F, 27 / 2F, 32 / 2F, 31 / 2F};
		float[] saberHiltA = {24F / 2F, 7F / 2F, 28 / 2F, 15 / 2F};
		float[] saberHiltB = {25F / 2F, 3F / 2F, 28 / 2F, 7 / 2F};
		float[] saberHiltC = {24F / 2F, 0F / 2F, 28 / 2F, 3 / 2F};
		float[] saberHiltC2 = {24F / 2F, 1F / 2F, 28 / 2F, 3 / 2F};

		boolean renderBladeOnly = bladeOnly.get();
		if (!renderBladeOnly) {
			model.clear();
			BoxModel standard = new BoxModel();

			standard.addBox(0.5F - 2 / 16F, 0 / 16F, 0.5F - 2 / 16F, 0.5F + 2 / 16F, 8 / 16F, 0.5F + 2 / 16F)
					.setTextureBounds(new float[][]{
							saberHiltBottom, saberHiltTop,
							saberHiltA, saberHiltA, saberHiltA, saberHiltA
					});
			standard.addBox(0.5F - 1.5F / 16F, 8 / 16F, 0.5F - 1.5F / 16F, 0.5F + 1.5F / 16F, 12 / 16F, 0.5F + 1.5F / 16F)
					.setTextureBounds(new float[][]{
							saberHiltBottom, saberHiltTop,
							saberHiltB, saberHiltB, saberHiltB, saberHiltB
					}).setInvisible(3);
			standard.addBox(0.5F - 2 / 16F, 12 / 16F, 0.5F - 2 / 16F, 0.5F + 2 / 16F, 15 / 16F, 0.5F + 2 / 16F)
					.setTextureBounds(new float[][]{
							saberHiltBottom, saberHiltTop,
							saberHiltC, saberHiltC, saberHiltC, saberHiltC
					});

			standard.setTexture("luxsaber");

			model.addBoxModel(standard);
		}

		float v = MathHelper.clamp(stackMap.getOrDefault(stack, 0F), 0, 1);
		if (v > 0) {
			v = Math.min(1, v + MCTimer.renderPartialTickTime * OFFSET);
			int color = stack.getMetadata();
			float[] saberGlowSide = {(color * 4 + 0F) / 2F, 31F / 2F, (color * 4 + 4F) / 2F, 32F / 2F};
			float[] saberGlowTop = {(color * 4 + 0F) / 2F, 27F / 2F, (color * 4 + 4F) / 2F, 31F / 2F};
			float[] saberSide = {(color * 3 + 0F) / 2F, (1 - v) * 24F / 2F, (color * 3 + 3F) / 2F, 24F / 2F};
			float[] saberTop = {(color * 3 + 0F) / 2F, 24F / 2F, (color * 3 + 3F) / 2F, 27F / 2F};

			BoxModel litSaber = new BoxModel();


			if (!renderBladeOnly) {
				litSaber.addBox(0.5F - 2 / 16F, 0 / 16F, 0.5F - 2 / 16F, 0.5F + 2 / 16F, 8 / 16F, 0.5F + 2 / 16F)
						.setTextureBounds(new float[][]{
								saberHiltBottom, saberHiltTop,
								saberHiltA, saberHiltA, saberHiltA, saberHiltA
						});
				litSaber.addBox(0.5F - 1.5F / 16F, 8 / 16F, 0.5F - 1.5F / 16F, 0.5F + 1.5F / 16F, 12 / 16F, 0.5F + 1.5F / 16F)
						.setTextureBounds(new float[][]{
								saberHiltBottom, saberHiltTop,
								saberHiltB, saberHiltB, saberHiltB, saberHiltB
						}).setInvisible(3);

				litSaber.addBox(0.5F - 2 / 16F, 12 / 16F, 0.5F - 2 / 16F, 0.5F + 2 / 16F, 14 / 16F, 0.5F + 2 / 16F)
						.setTextureBounds(new float[][]{
								saberHiltBottom, saberHiltTop,
								saberHiltC2, saberHiltC2, saberHiltC2, saberHiltC2
						}).setInvisible(2);
				litSaber.addBox(0.5F - 2 / 16F, 14 / 16F, 0.5F - 2 / 16F, 0.5F + 2 / 16F, 15 / 16F, 0.5F + 2 / 16F)
						.setTextureBounds(new float[][]{
								saberGlowTop, saberGlowTop,
								saberGlowSide, saberGlowSide, saberGlowSide, saberGlowSide
						}).setInvisible(2);
			}
			float v2;
			v2 = Math.min(v * 1.2F, 1) * 0.5F;
			float v1 = 3.0F;
//			if (!renderBladeOnly || stack.getMetadata() != LuxColors.BLACK.ordinal())
			litSaber.addBox(0.5F - v2 * 1.5F / 16F, 15 / 16F, 0.5F - v2 * 1.5F / 16F,
					0.5F + v2 * 1.5F / 16F, 15 / 16F + v * v1, 0.5F + v2 * 1.5F / 16F)
					.setTextureBounds(new float[][]{
							saberTop, saberTop,
							saberSide, saberSide, saberSide, saberSide
					}).setInvisible(1);


			v2 = Math.min(v * 1.2F, 1);
			litSaber.addBox(0.5F - v2 * 1.5F / 16F, 15 / 16F, 0.5F - v2 * 1.5F / 16F,
					0.5F + v2 * 1.5F / 16F, 15 / 16F + v * (v1 + 0.1F), 0.5F + v2 * 1.5F / 16F)
					.setTextureBounds(new float[][]{
							saberTop, saberTop,
							saberSide, saberSide, saberSide, saberSide
					}).setInvisible(1);


			litSaber.setTexture("luxsaber");


			ItemCameraTransforms.TransformType[] transformTypes = renderBladeOnly ?
					new ItemCameraTransforms.TransformType[]{
							ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
							ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
							ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND,
							ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
					} :
					new ItemCameraTransforms.TransformType[]{
//							ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
//							ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
							ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND,
							ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
					};
			;
			for (ItemCameraTransforms.TransformType type : transformTypes) {
				Pair<? extends IBakedModel, Matrix4f> pair = model.transformMap.get(type);
				IBakedModel key = pair.getKey();
				if (key == model) {
					MutableModel mutableModel = new MutableModel(Transforms.itemToolsTransforms);
					model.transformMap.put(type, Pair.of(mutableModel, pair.getRight()));
				}
			}
			for (ItemCameraTransforms.TransformType type : transformTypes) {
				MutableModel key = (MutableModel) model.transformMap.get(type).getKey();
				key.clear();
				for (Box box : litSaber) {
					for (EnumFacing facing : FacingHelper.facingPlusNull) {
						List<BakedQuad> quads = box.getQuads(facing);
						if (quads != null)
							key.generalQuads.addAll(quads);
					}
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getMaxMetadata() {
		return LuxColors.values().length - 1;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void overrideRendering(RenderSpecificHandEvent event) {
		ItemStack itemStack = event.getItemStack();
		if (itemStack.getItem() == this) {
			Minecraft.getMinecraft().getItemRenderer().renderItemInFirstPerson(Minecraft.getMinecraft().player, event.getPartialTicks(), event.getInterpolatedPitch(), event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress());
			GLStateAttributes states = GLStateAttributes.loadStates();
			bladeOnly.set(true);
			Minecraft.getMinecraft().getItemRenderer().renderItemInFirstPerson(Minecraft.getMinecraft().player, event.getPartialTicks(), event.getInterpolatedPitch(), event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress());
			bladeOnly.set(false);
			states.restore();
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void run(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.isGamePaused()) {
			return;
		}

		HashMap<ItemStack, Float> prevMap = Maps.newHashMap(stackMap);
		stackMap.clear();
		WorldClient world = mc.world;
		if (world == null) return;
		for (EntityPlayer player : world.getEntities(EntityPlayer.class, s -> true)) {
			for (ItemStack stack : player.getHeldEquipment()) {
				if (stack.getItem() == this) {
					double durabilityForDisplay = 1 - getDurabilityForDisplay(stack);
					if (durabilityForDisplay != 0) {
						stackMap.put(stack, Math.min(prevMap.getOrDefault(stack, 0F) + OFFSET, 1));
					}
				}
			}
		}
	}

	public static class XUItemEnergyStorage implements IEnergyStorage, ICapabilityProvider {
		protected final ItemStack stack;
		protected int capacity;
		protected int maxReceive;
		protected int maxExtract;

		public XUItemEnergyStorage(ItemStack stack, int capacity, int maxReceive, int maxExtract) {
			this.stack = stack;
			this.capacity = capacity;
			this.maxReceive = maxReceive;
			this.maxExtract = maxExtract;
		}


		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (!canReceive())
				return 0;

			int energyReceived = Math.min(capacity - getEnergyStored(), Math.min(this.maxReceive, maxReceive));
			if (!simulate)
				setEnergyStored(getEnergyStored() + energyReceived);
			return energyReceived;
		}


		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (!canExtract())
				return 0;

			int energyExtracted = Math.min(getEnergyStored(), Math.min(this.maxExtract, maxExtract));
			if (!simulate)
				setEnergyStored(getEnergyStored() - energyExtracted);
			return energyExtracted;
		}

		@Override
		public int getEnergyStored() {
			NBTTagCompound tagCompound = stack.getTagCompound();
			return tagCompound == null ? 0 : tagCompound.getInteger("Energy");
		}

		private void setEnergyStored(int energy) {
			stack.setTagInfo("Energy", new NBTTagInt(energy));
		}

		@Override
		public int getMaxEnergyStored() {
			return capacity;
		}

		@Override
		public boolean canExtract() {
			return this.maxExtract > 0;
		}

		@Override
		public boolean canReceive() {
			return this.maxReceive > 0;
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
			return capability == CapabilityEnergy.ENERGY;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
			return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(this) : null;
		}
	}

}
