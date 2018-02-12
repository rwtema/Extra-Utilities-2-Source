package com.rwtema.extrautils2.items;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.backend.ModifyingBakedModel;
import com.rwtema.extrautils2.backend.XUItem;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ThreadLocalBoolean;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class ItemFakeCopy extends XUItem {
	static WeakHashMap<ItemStack, Long> lastTooltipTime = new WeakHashMap<>();

	public ItemFakeCopy() {
		MinecraftForge.EVENT_BUS.register(this);
		setHasSubtypes(true);
	}

	public static boolean showAllItems() {
		return false;
	}

	public static ItemStack getOriginalStack(int itemDamage) {
		switch (itemDamage) {
			case 8:
				return new ItemStack(Items.NETHER_STAR);
			case 1:
				return new ItemStack(Items.DIAMOND);
			case 2:
				ItemStack itemStack = new ItemStack(Items.DIAMOND_PICKAXE);
				itemStack.addEnchantment(Enchantments.FORTUNE, 6);
				itemStack.addEnchantment(Enchantments.EFFICIENCY, 10);
				return itemStack;
			case 3:
				return XU2Entries.lawSword.newStack();
			case 4:
				return new ItemStack(Blocks.GOLD_BLOCK);
			case 5:
				return new ItemStack(Blocks.DRAGON_EGG);
			case 6:
				return new ItemStack(Blocks.EMERALD_BLOCK);
			case 7:
				return new ItemStack(Blocks.DIAMOND_BLOCK);
			default:
				return new ItemStack(Items.STICK);
		}
	}

	@Override
	public void registerTextures() {
		Textures.register("cardboard");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {
		return Textures.MISSING_SPRITE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createModel(int metadata) {
		return new PassthruModelItem(this) {
			private ModifyingBakedModel.IQuadReDesigner carboardifier = new ModifyingBakedModel.IQuadReDesigner() {
//				public Class<?> itemLayerModelClass;
//				{
//					try {
//						itemLayerModelClass = Class.forName("net.minecraftforge.client.model.ItemLayerModel$BakedItemModel");
//					} catch (ClassNotFoundException e) {
//						throw Throwables.propagate(e);
//					}
//				}

				@Nonnull
				@Override
				public List<BakedQuad> redesign(@Nonnull List<BakedQuad> original, IBakedModel base, IBlockState state, EnumFacing side, long rand) {
					ArrayList<BakedQuad> list = Lists.newArrayListWithExpectedSize(original.size());
					TextureAtlasSprite sprite = Textures.getSprite("cardboard");
					for (BakedQuad quad : original) {
						list.addAll(PassthruModelItem.trySplitQuad(quad, sprite));
					}
					return list;
				}
			};

			private LoadingCache<ItemStack, ModifyingBakedModel> cache = CacheBuilder.newBuilder().expireAfterWrite(4, TimeUnit.SECONDS).build(new CacheLoader<ItemStack, ModifyingBakedModel>() {
				@Override
				public ModifyingBakedModel load(@Nonnull ItemStack key) throws Exception {
					ItemStack duplicate = getDuplicate(key);
					IBakedModel duplicateModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(duplicate);
					IBakedModel finalModel = duplicateModel.getOverrides().handleItemState(duplicateModel, duplicate, null, null);
					return ModifyingBakedModel.create(finalModel, carboardifier);
				}
			});

			ItemOverrideList list = new ItemOverrideList(ImmutableList.of()) {
				ThreadLocalBoolean avoidRecursion = new ThreadLocalBoolean(false);

				@Nonnull
				@Override
				public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase entity) {
					if (stack.getItemDamage() == 0 && (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Item"))) {
						return overrideList.handleItemState(originalModel, stack, world, entity);
					}

					if (avoidRecursion.get()) return originalModel;
					avoidRecursion.set(true);
					ItemStack duplicate = getDuplicate(stack);
					IBakedModel duplicateModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(duplicate);
					IBakedModel finalModel = duplicateModel.getOverrides().handleItemState(duplicateModel, duplicate, null, null);
					avoidRecursion.set(false);

					if (isInsideInventory(stack) && hasTooltippedRecently(stack))
						return cache.getUnchecked(stack);

					return finalModel;
				}
			};

			@Nonnull
			@Override
			public ItemOverrideList getOverrides() {
				return list;
			}
		};
	}

	public boolean hasTooltippedRecently(ItemStack stack) {
		Long aLong = lastTooltipTime.get(stack);
		if (aLong != null) {
			long l = System.currentTimeMillis() - aLong;
			if (l <= 1000) {
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack) {
		model.addTintedSprite(Textures.getSprite("cardboard"), true, 0);
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return getDuplicate(stack).hasEffect();
	}

	@Nonnull
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return getDuplicate(stack).getRarity();
	}

	public ItemStack getDuplicate(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag.hasKey("Item", Constants.NBT.TAG_COMPOUND)) {
				ItemStack item = StackHelper.loadFromNBT(tag.getCompoundTag("Item"));
				if (StackHelper.isNonNull(item)) {
					return item;
				}
			}
		}

		int itemDamage = stack.getItemDamage();
		return getOriginalStack(itemDamage);

	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		String displayName = getDuplicate(stack).getDisplayName();
		if (isInsideInventory(stack) && hasTooltippedRecently(stack)) {
			return Lang.translateArgs("%s (Fake)", ChatFormatting.STRIKETHROUGH + displayName + ChatFormatting.RESET);
		}
		return displayName;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		ItemStack duplicate = getDuplicate(stack);
		tooltip.add(Lang.translateArgs("On close inspection, this %s", duplicate.getDisplayName()));
		if (duplicate.hasEffect()) {
			tooltip.add(Lang.translate("is a fake made out of enchanted cardboard."));
		} else {
			tooltip.add(Lang.translate("is a fake made out of cardboard."));
		}

		tooltip.add(Lang.translate("Hope you got a receipt."));
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void changeToolTip(ItemTooltipEvent event) {
		ItemStack itemStack = event.getItemStack();
		if (StackHelper.isNull(itemStack) || itemStack.getItem() != this) return;

		if (isInsideInventory(itemStack)) {
			lastTooltipTime.put(itemStack, System.currentTimeMillis());
			return;
		}

		ItemStack duplicate = getDuplicate(itemStack);
		List<String> toolTip = event.getToolTip();
		toolTip.clear();
		toolTip.addAll(CompatHelper112.getTooltip(duplicate, event));
	}

	@SideOnly(Side.CLIENT)
	private boolean isInsideInventory(ItemStack itemStack) {
		EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;
		if (thePlayer == null) return false;
		if (thePlayer.openContainer instanceof ContainerWorkbench) {
			return true;
		}

		if (thePlayer.openContainer instanceof ContainerMerchant) {
			return false;
		}

		InventoryPlayer inv = thePlayer.inventory;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i) == itemStack) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void getSubItemsBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		if (showAllItems()) {
//			Streams.stream(Item.REGISTRY.iterator())
//					.filter(item -> item != this)
//					.flatMap(item -> ExtraUtils2.proxy.getSubItems(item).stream())
//					.forEach(s -> {
//								ItemStack copy = new ItemStack(itemIn);
//								copy.setTagInfo("Item", StackHelper.serializeSafe(s));
//								subItems.add(copy);
//							}
//
//					);
		}
	}
}
