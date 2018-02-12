package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.itemhandler.IItemHandlerCompat;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerBase;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.AltStackStorage;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemBagOfHolding extends XUItemFlatMetadata implements IDynamicHandler {
	public ItemBagOfHolding() {
		super("bag_of_holding");
		setMaxStackSize(1);
	}

	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new BagHoldingItemHandler(stack);
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
		if (player == null) return null;

		InventoryPlayer inventory = player.inventory;
		int slot = inventory.currentItem;

		ItemStack heldItem = player.getHeldItemMainhand();
		if (StackHelper.isNull(heldItem) || heldItem.getItem() != this)
			return null;

		IItemHandler handler = heldItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if (handler == null || handler.getClass() != BagHoldingItemHandler.class)
			return null;

		return new ContainerBagHolding(player, slot, (BagHoldingItemHandler) handler, player.world.isRemote ? Side.CLIENT : Side.SERVER);
	}

	@Nullable
	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack) {
		NBTTagCompound compound = stack.getTagCompound();
		if (compound == null) return super.getNBTShareTag(stack);

		NBTTagCompound copy = new NBTTagCompound();
		copy.removeTag("Items");
		return copy;
	}

	public class BagHoldingItemHandler implements ICapabilityProvider, IItemHandlerCompat {
		final ItemStack bagStack;

		public BagHoldingItemHandler(ItemStack bagStack) {
			this.bagStack = bagStack;
		}

		@Override
		public int getSlots() {
			return 27 * 2;
		}

		public IItemHandler getSlotHandler(final int slot) {
			if (slot < 0 || slot >= (27 * 2)) return EmptyHandler.INSTANCE;

			return new SingleStackHandlerBase() {

				@ItemStackNonNull
				@Override
				public ItemStack getStack() {
					NBTTagCompound itemsTag = NBTHelper.getOrInitTagCompound(NBTHelper.getOrInitTagCompound(bagStack), "Items");
					String key = getSlotKey();
					if (itemsTag.hasKey(key, Constants.NBT.TAG_BYTE_ARRAY)) {
						ItemStack itemStack = AltStackStorage.loadData(itemsTag.getByteArray(key));
						if (StackHelper.isNull(itemStack)) {
							itemsTag.removeTag(key);
							itemStack = StackHelper.empty();
						}
						return itemStack;
					} else if (itemsTag.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
						ItemStack itemStack = StackHelper.loadFromNBT(itemsTag.getCompoundTag(key));
						if (StackHelper.isNull(itemStack)) {
							itemsTag.removeTag(key);
						} else {
							itemsTag.setByteArray(key, AltStackStorage.storeData(itemStack));
						}

						return itemStack;
					}
					return StackHelper.empty();
				}


				@Nonnull
				public String getSlotKey() {
					return CollectionHelper.STRING_DIGITS[slot];
				}

				@Override
				protected int getStackLimit(@Nonnull ItemStack stack) {
					if (stack.getItem() == ItemBagOfHolding.this)
						return 0;
					if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
						return 0;

					return super.getStackLimit(stack);
				}

				@Override
				public void setStack(@ItemStackNonNull ItemStack stack) {
					NBTTagCompound itemsTag = NBTHelper.getOrInitTagCompound(NBTHelper.getOrInitTagCompound(bagStack), "Items");
					String key = getSlotKey();
					if (StackHelper.isNull(stack)) {
						itemsTag.removeTag(key);
					} else {
						itemsTag.setByteArray(key, AltStackStorage.storeData(stack));
					}
				}
			};
		}

		@Override
		@ItemStackNonNull
		public ItemStack getStackInSlot(int slot) {
			return getSlotHandler(slot).getStackInSlot(slot);
		}

		@Override
		@ItemStackNonNull
		public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
			return getSlotHandler(slot).insertItem(slot, stack, simulate);
		}

		@Override
		@ItemStackNonNull
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return getSlotHandler(slot).extractItem(slot, amount, simulate);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return facing == null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return hasCapability(capability, facing) ? (T) this : null;
		}
	}

	public class ContainerBagHolding extends DynamicContainer {

		public final InventoryPlayer inventory;
		public final EntityPlayer player;
		public final int slot;


		public ContainerBagHolding(EntityPlayer player, int slot, BagHoldingItemHandler handler, Side side) {
			this.player = player;
			this.inventory = player.inventory;
			this.slot = slot;

			addTitle(Lang.getItemName(ItemBagOfHolding.this), false);

			IItemHandler clientHandler = side == Side.CLIENT ? new ItemStackHandler(handler.getSlots()) : null;

			for (int i = 0; i < handler.getSlots(); i++) {
				int slotX = i % 9;
				int slotY = i / 9;

				if (side == Side.CLIENT) {
					addWidget(new WidgetSlotItemHandler(clientHandler, i, 5 + slotX * 18, 15 + slotY * 18));
				} else {
					final int dSlot = i;
					addWidget(new WidgetSlotItemHandler(handler, dSlot, 5 + slotX * 18, 15 + slotY * 18) {
						ItemStack lastReturned;

						@Override
						public boolean isItemValid(ItemStack stack) {
							if (StackHelper.isNull(stack))
								return false;
							ItemStack remainder = getHandler().insertItem(dSlot, stack, true);
							return StackHelper.isNull(remainder) || StackHelper.getStacksize(remainder) < StackHelper.getStacksize(stack);
						}

						@Override
						public ItemStack getStack() {
							return (lastReturned = getHandler().getStackInSlot(dSlot));
						}

						@Override
						public void putStack(ItemStack stack) {
							IItemHandler handler1 = getHandler();
							if (handler1 instanceof BagHoldingItemHandler) {
								((IItemHandlerModifiable) ((BagHoldingItemHandler) handler1).getSlotHandler(dSlot)).setStackInSlot(dSlot, stack);
								lastReturned = stack;
								this.onSlotChanged();
							}
						}

						@Override
						public void onSlotChanged() {
							ItemStack lastReturned = this.lastReturned;
							ItemStack stack = getHandler().getStackInSlot(dSlot);
							if (StackHelper.isNonNull(lastReturned) && StackHelper.isNonNull(stack) && StackHelper.getStacksize(lastReturned) != StackHelper.getStacksize(stack) && ItemHandlerHelper.canItemStacksStack(stack, lastReturned)) {
								putStack(lastReturned);
								return;
							}

							super.onSlotChanged();
						}

						@Override
						public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
							super.onSlotChange(p_75220_1_, p_75220_2_);
						}

						@Override
						public int getItemStackLimit(ItemStack stack) {
							ItemStack maxAdd = stack.copy();
							StackHelper.setStackSize(maxAdd, maxAdd.getMaxStackSize());
							IItemHandler itemHandler = getHandler();
							ItemStack currentStack = itemHandler.getStackInSlot(dSlot);
							ItemStack remainder = itemHandler.insertItem(dSlot, maxAdd, true);

							int current = StackHelper.isNull(currentStack) ? 0 : StackHelper.getStacksize(currentStack);
							int added = StackHelper.getStacksize(maxAdd) - (StackHelper.isNonNull(remainder) ? StackHelper.getStacksize(remainder) : 0);
							return current + added;
						}

						@Override
						public boolean canTakeStack(EntityPlayer playerIn) {
							return StackHelper.isNonNull(getHandler().extractItem(dSlot, 1, true));
						}

						@Override
						public ItemStack decrStackSize(int amount) {
							ItemStack itemStack = getHandler().extractItem(dSlot, amount, false);
							getStack();
							return itemStack;
						}
					});
				}
			}

			cropAndAddPlayerSlots(inventory);
			validate();
		}

		public IItemHandler getHandler() {
			ItemStack heldItem = player.getHeldItemMainhand();
			if (StackHelper.isNull(heldItem) || heldItem.getItem() != ItemBagOfHolding.this) {
				return EmptyHandler.INSTANCE;
			}
			IItemHandler capability = heldItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (!(capability instanceof BagHoldingItemHandler))
				return EmptyHandler.INSTANCE;

			return capability;
		}

		@Override
		public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
			return playerIn == player && inventory.currentItem == slot && getHandler() != EmptyHandler.INSTANCE;
		}

	}
}