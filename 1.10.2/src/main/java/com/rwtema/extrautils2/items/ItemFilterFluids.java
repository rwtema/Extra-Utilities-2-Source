package com.rwtema.extrautils2.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.fluids.IFluidFilter;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemFilterFluids extends XUItemFlatMetadata implements IFluidFilter, IDynamicHandler {
	public static final int NUM_SLOTS = 16;
	public static final String NBT_KEY_FLAGS = "Flags";

	public ItemFilterFluids() {
		super("filter_fluid");
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

		return new ItemFilterFluids.FilterConfigContainer(player, slot, heldItem);
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

			if (ghostStack.getItem() instanceof IFluidFilter) {
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

			FluidStack fluidStack = FluidUtil.getFluidContained(ghostStack);
			if (fluidStack != null) {
				String s = fluidStack.getLocalizedName();
				tooltip.add(ChatFormatting.GRAY + StringHelper.format(1 + i) + " -- " + ChatFormatting.WHITE + s);
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

	@Override
	public boolean isFluidFilter(@Nonnull ItemStack filterStack) {
		return true;
	}

	public FluidStack getFluidFromStack(ItemStack stack) {
		if (StackHelper.isNull(stack)) return null;

		Item item = stack.getItem();
		if (item == Items.WATER_BUCKET)
			return new FluidStack(FluidRegistry.WATER, 1000);

		if (item == Items.LAVA_BUCKET)
			return new FluidStack(FluidRegistry.LAVA, 1000);


		return FluidUtil.getFluidContained(stack);
	}

	@Override
	public boolean matches(@Nonnull ItemStack filterStack, FluidStack target) {
		if (!filterStack.hasTagCompound()) return false;

		boolean inverted = getFlag(filterStack, FLAG.INVERTED);

		if (target == null) return inverted;
		boolean useNBT = !getFlag(filterStack, FLAG.IGNORE_NBT);

		for (int i = 0; i < NUM_SLOTS; i++) {
			ItemStack ghostStack = getGhostStack(filterStack, i);
			if (StackHelper.isNull(ghostStack)) continue;

			Item item = ghostStack.getItem();
			if (item instanceof IFluidFilter) {
				IFluidFilter filter = (IFluidFilter) item;
				if (filter.matches(ghostStack, target) == !inverted) {
					return !inverted;
				}
			}

			FluidStack fluid = FluidUtil.getFluidContained(ghostStack);
			if (fluid == null) continue;

			if ((useNBT && !target.isFluidEqual(fluid)) ||
					(!useNBT && target.getFluid() != fluid.getFluid())
			) {
				continue;
			}

			return !inverted;
		}

		return inverted;
	}

	enum FLAG {
		INVERTED,
		IGNORE_NBT;

		public final int meta = 1 << ordinal();

		public final String ON_KEY = name() + " ON";
		public final String OFF_KEY = name() + " OFF";
	}

	public class FilterConfigContainer extends DynamicContainer {

		private final EntityPlayer player;
		private final int slot;
		private final ItemStack heldItem;

		WidgetSlotGhost[] ghostSlots = new WidgetSlotGhost[NUM_SLOTS];

		public FilterConfigContainer(EntityPlayer player, int slot, final ItemStack heldItem) {
			this.player = player;
			this.slot = slot;
			this.heldItem = heldItem;

			final int NUM_COLUMNS = (int) Math.sqrt(NUM_SLOTS);

			int inner_filter_w = NUM_COLUMNS * 18;
			int filter_w = (inner_filter_w * 14) / 8;
			int filter_h_offset = (filter_w * 3) / 14 - 5;

			WidgetBase filter_item = new WidgetBase(centerX - filter_w / 2, 10 - 5, filter_w, filter_w) {
				@Override
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					TextureAtlasSprite sprite = Textures.getSprite("filter_fluid");
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

			addTitle(Lang.getItemName(ItemFilterFluids.this), false);


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
				}, dSlot, centerX - NUM_COLUMNS * 9 + slotX * 18, 10 + filter_h_offset + slotY * 18) {
					@Override
					public void putStack(ItemStack stack) {
						FluidStack fluidContained = FluidUtil.getFluidContained(stack);
						if (fluidContained == null && StackHelper.isNonNull(stack) && !(stack.getItem() instanceof IFluidFilter))
							return;

						super.putStack(stack);
					}

					@Override
					public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
						super.renderBackground(manager, gui, guiLeft, guiTop);
						ItemStack stack = getStack();
						FluidStack fluidStack = FluidUtil.getFluidContained(stack);
						if (fluidStack != null && fluidStack.getFluid() != null) {
							ResourceLocation still = fluidStack.getFluid().getStill(fluidStack);
							if (still == null) return;

							TextureAtlasSprite sprite = null;
							sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(still.toString());
							if (sprite == null)
								return;

							manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
							gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1, 16, 16,
									sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV()
							);
						}
					}
				});
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
			}

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}

		@Override
		public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
			ItemStack heldItem;
			return playerIn == player && player.inventory.currentItem == slot &&
					StackHelper.isNonNull(heldItem = playerIn.getHeldItemMainhand()) &&
					heldItem.getItem() == ItemFilterFluids.this;
		}

		@ItemStackNonNull
		@Override
		public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
			ItemStack itemstack = null;
			Slot slot = this.inventorySlots.get(par2);

			return super.transferStackInSlot(par1EntityPlayer, par2);
		}
	}
}
