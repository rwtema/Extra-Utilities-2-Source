package com.rwtema.extrautils2.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.items.IItemFilter;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerBase;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.List;


public class ItemFilterItems extends XUItemFlatMetadata implements IItemFilter, IDynamicHandler {
	public static final int NUM_SLOTS = 16;
	public static final String NBT_KEY_FLAGS = "Flags";

	public ItemFilterItems() {
		super("filter_item");
	}

	public static boolean getFlag(ItemStack filter, FLAG flag) {
		NBTTagCompound nbt = filter.getTagCompound();
		int i = nbt == null ? 0 : nbt.getInteger(NBT_KEY_FLAGS);
		return (i & flag.meta) != 0;
	}

	public static void setFlag(ItemStack filter, FLAG flag, boolean value) {
		if (!value && !filter.hasTagCompound()) return;
		NBTTagCompound nbt = NBTHelper.getOrInitTagCompound(filter);
		int flags = nbt.getInteger(NBT_KEY_FLAGS);
		if (value) {
			flags ^= flag.meta;
		} else {
			flags &= ~flag.meta;
		}
		if (flags == 0) {
			nbt.removeTag(NBT_KEY_FLAGS);
			if (nbt.hasNoTags()) {
				filter.setTagCompound(null);
			}
		} else {
			if (flags == (byte) flags) {
				nbt.setByte(NBT_KEY_FLAGS, (byte) flags);
			} else {
				nbt.setInteger(NBT_KEY_FLAGS, (byte) flags);
			}
		}
	}

	public static ItemStack getGhostStack(ItemStack filter, int i) {
		NBTTagCompound nbt = filter.getTagCompound();
		String stringDigit = CollectionHelper.STRING_DIGITS[i];
		if (nbt == null || !nbt.hasKey(stringDigit, Constants.NBT.TAG_COMPOUND)) return StackHelper.empty();
		return StackHelper.loadFromNBT(nbt.getCompoundTag(stringDigit));
	}

	public static void putGhostStack(ItemStack filter, int i, ItemStack result) {
		if (StackHelper.isNull(result) && !filter.hasTagCompound()) return;

		NBTTagCompound nbt = NBTHelper.getOrInitTagCompound(filter);
		String stringDigit = CollectionHelper.STRING_DIGITS[i];
		if (StackHelper.isNull(result)) {
			nbt.removeTag(stringDigit);
			if (nbt.hasNoTags()) {
				filter.setTagCompound(null);
			}
		} else {
			NBTTagCompound nbtTagCompound = result.serializeNBT();
			nbt.setTag(stringDigit, nbtTagCompound);
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
		return StackHelper.isNull(oldStack) || StackHelper.isNull(newStack) || oldStack.getItem() != this || newStack.getItem() != this;
	}

	@Override
	public boolean isItemFilter(@Nonnull ItemStack filterStack) {
		return true;
	}

	@Override
	public boolean matches(@Nonnull ItemStack filterStack, ItemStack target) {
		if (!filterStack.hasTagCompound()) return false;

		boolean inverted = getFlag(filterStack, FLAG.INVERTED);

		if (StackHelper.isNull(target)) return inverted;
		boolean useOreDic = getFlag(filterStack, FLAG.USE_ORE_DICTIONARY);
		boolean useMeta = !getFlag(filterStack, FLAG.IGNORE_METADATA);
		boolean useNBT = !getFlag(filterStack, FLAG.IGNORE_NBT);

		for (int i = 0; i < NUM_SLOTS; i++) {
			ItemStack ghostStack = getGhostStack(filterStack, i);
			if (StackHelper.isNull(ghostStack)) continue;

			Item item = ghostStack.getItem();
			if (item instanceof IItemFilter) {
				IItemFilter filter = (IItemFilter) item;
				if (filter.matches(ghostStack, target)) {
					return !inverted;
				}
			}

			if (useOreDic) {
				int[] ghostOreIDs = OreDictionary.getOreIDs(ghostStack);
				int[] targetOreIDs = OreDictionary.getOreIDs(ghostStack);
				for (int a : ghostOreIDs) {
					for (int b : targetOreIDs) {
						if (a == b) return !inverted;
					}
				}
			}

			if (item != target.getItem())
				continue;

			if (useMeta && ghostStack.getMetadata() != target.getMetadata())
				continue;

			if (useNBT && !ItemStack.areItemStackTagsEqual(ghostStack, target))
				continue;

			return !inverted;
		}

		return inverted;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClickBase(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!worldIn.isRemote) {
			openItemGui(playerIn);
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (player == null)
			return null;

		InventoryPlayer inventory = player.inventory;
		int slot = inventory.currentItem;

		ItemStack heldItem = player.getHeldItemMainhand();
		if (StackHelper.isNull(heldItem) || heldItem.getItem() != this)
			return null;

		return new FilterConfigContainer(player, slot, heldItem);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (!stack.hasTagCompound()) return;


		for (int i = 0; i < NUM_SLOTS; i++) {
			ItemStack ghostStack = getGhostStack(stack, i);
			if (StackHelper.isNull(ghostStack)) {
				continue;
			}

			String resourceName = "" + Item.REGISTRY.getNameForObject(ghostStack.getItem());

			List<String> list = CompatHelper112.getTooltip(ghostStack, playerIn, false);
			for (int j = 0; j < list.size(); j++) {
				String s = list.get(j);
				String noFormatting = ChatFormatting.stripFormatting(s).trim();
				if (noFormatting.isEmpty() || noFormatting.equals(resourceName)) continue;
				if (j == 0)
					tooltip.add(ChatFormatting.GRAY + StringHelper.format(1 + i) + " -- " + ChatFormatting.WHITE + s);
				else
					tooltip.add(ChatFormatting.GRAY + "      " + s);
			}
		}


		tooltip.add("");

		for (FLAG flag : FLAG.values()) {
			boolean b = getFlag(stack, flag);
			if (b) {
				tooltip.add(ChatFormatting.BLUE + Lang.translate(flag.name()));
			}
		}
	}

	enum FLAG {
		INVERTED,
		IGNORE_NBT,
		IGNORE_METADATA,
		USE_ORE_DICTIONARY;

		public final int meta = 1 << ordinal();

		public final String ON_KEY = name() + " ON";
		public final String OFF_KEY = name() + " OFF";
	}

	public class FilterConfigContainer extends DynamicContainer {

		private final EntityPlayer player;
		private final int slot;

		WidgetSlotGhost[] ghostSlots = new WidgetSlotGhost[NUM_SLOTS];


		public FilterConfigContainer(EntityPlayer player, int slot, final ItemStack heldItem) {
			this.player = player;
			this.slot = slot;

			final int NUM_COLUMNS = (int) Math.sqrt(NUM_SLOTS);

			int inner_filter_w = NUM_COLUMNS * 18;
			int filter_w = (inner_filter_w * 14) / 8;
			int filter_h_offset = (filter_w * 3) / 14 - 5;

			WidgetBase filter_item = new WidgetBase(centerX - filter_w / 2, 10 - 5, filter_w, filter_w) {
				@Override
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					TextureAtlasSprite sprite = Textures.getSprite("filter_item");
					if (sprite == null) return;
					manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(770, 771);
					GlStateManager.color(1, 1, 1, 0.5F);
					gui.drawTexturedModalRect(guiLeft + x, guiTop + y, w, h, sprite.getInterpolatedU(1), sprite.getInterpolatedV(1), sprite.getInterpolatedU(15), sprite.getInterpolatedV(15));
					GlStateManager.color(1, 1, 1, 1);
				}
			};
			addWidget(filter_item);

			addTitle(Lang.getItemName(ItemFilterItems.this), false);

			for (int i = 0; i < NUM_SLOTS; i++) {
				int slotX = i % NUM_COLUMNS;
				int slotY = i / NUM_COLUMNS;
				final int dSlot = i;

				addWidget(ghostSlots[i] = new WidgetSlotGhost(new SingleStackHandlerBase() {
					@ItemStackNonNull
					@Override
					public ItemStack getStack() {
						return getGhostStack(heldItem, dSlot);
					}

					@Override
					public void setStack(@ItemStackNonNull ItemStack stack) {
						putGhostStack(heldItem, dSlot, stack);
					}
				}, dSlot, centerX - NUM_COLUMNS * 9 + slotX * 18, 10 + filter_h_offset + slotY * 18));
			}
			crop();


			FLAG[] flagValues = FLAG.values();
			for (final FLAG flag : flagValues) {
				int i = flag.ordinal();
				int dx = i % 2;
				int dy = i / 2;
				int x;
				x = 5 + (dx * DynamicContainer.playerInvWidth) / 2;
				if (dx == 0) {
					x = 5;
				} else {
					int w = 8 + Math.max(
							ExtraUtils2.proxy.apply(DynamicContainer.STRING_WIDTH_FUNCTION, Lang.translate(flag.OFF_KEY)),
							ExtraUtils2.proxy.apply(DynamicContainer.STRING_WIDTH_FUNCTION, Lang.translate(flag.ON_KEY)));
					x = DynamicContainer.playerInvWidth - w;
				}
				int y = 10 + filter_h_offset + (NUM_COLUMNS) * 18 + 4 + dy * 18;

				addWidget(new WidgetClickMCButtonChoices<Boolean>(x, y) {
					@Override
					protected void onSelectedServer(Boolean marker) {
						setFlag(heldItem, flag, marker);
					}

					@Override
					public Boolean getSelectedValue() {
						return getFlag(heldItem, flag);
					}
				}.addChoice(false, Lang.translate(flag.OFF_KEY), flag.OFF_KEY)
						.addChoice(true, Lang.translate(flag.ON_KEY), flag.ON_KEY));

//				getJEIWidget(new WidgetText(x + 20, y + (18 - 9) / 2, Lang.translate(flag.name())));
			}

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}

		@Override
		public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
			ItemStack heldItem;
			return playerIn == player && player.inventory.currentItem == slot &&
					StackHelper.isNonNull(heldItem = playerIn.getHeldItemMainhand()) &&
					heldItem.getItem() == ItemFilterItems.this;
		}

		@ItemStackNonNull
		@Override
		public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
			Slot slot = this.inventorySlots.get(par2);
			if (slot instanceof WidgetSlotGhost) {
				slot.putStack(StackHelper.empty());
			} else {
				ItemStack slotStack = slot.getStack();
				if (StackHelper.isNonNull(slotStack)) {
					for (WidgetSlotGhost ghostSlot : ghostSlots) {
						ItemStack stack = ghostSlot.getStack();
						if (StackHelper.isNonNull(stack) && ItemHandlerHelper.canItemStacksStack(stack, slotStack)) {
							return StackHelper.empty();
						}
					}
					for (WidgetSlotGhost ghostSlot : ghostSlots) {
						ItemStack stack = ghostSlot.getStack();
						if (StackHelper.isNull(stack)) {
							ghostSlot.putStack(slotStack.copy());
							break;
						}
					}
				}
			}

			return StackHelper.empty();
		}
	}
}
