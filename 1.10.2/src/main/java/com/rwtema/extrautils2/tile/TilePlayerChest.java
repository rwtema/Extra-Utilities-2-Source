package com.rwtema.extrautils2.tile;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.ConcatFixedLength;
import com.rwtema.extrautils2.itemhandler.EmptyHandlerModifiable;
import com.rwtema.extrautils2.itemhandler.IItemHandlerModifiableCompat;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerBase;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ArrayAccess;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TilePlayerChest extends TilePower implements IDynamicHandler {
	private final static int[] col = {0x80505050, 0x80FF5050, 0x8050FF50, 0x80FFFFFF};
	public IItemHandlerModifiable PLAYER_HANDLER;
	GameProfile profile;
	TIntHashSet extractable = new TIntHashSet();
	TIntHashSet insertable = new TIntHashSet();
	IItemHandler[] playerInvHandlers;

	{
		playerInvHandlers = new IItemHandler[6];

		playerInvHandlers[0] = new IItemHandlerModifiableCompat() {
			@Override
			public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
				EntityPlayerMP ownerPlayer = getOwnerPlayer();
				if (ownerPlayer != null) {
					ownerPlayer.inventory.setInventorySlotContents(slot, stack);
					PlayerHelper.syncInventory(ownerPlayer);
				}
			}

			@Override
			public int getSlots() {
				return 36;
			}

			@ItemStackNonNull
			@Override
			public ItemStack getStackInSlot(int slot) {
				return getTarget().getStackInSlot(slot);
			}

			@ItemStackNonNull
			@Override
			public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
				if (simulate)
					return getTarget().insertItem(slot, stack, true);
				EntityPlayerMP ownerPlayer = getOwnerPlayer();
				if (ownerPlayer == null) return stack;
				ItemStack itemStack = getTarget().insertItem(slot, stack, false);
				if (itemStack != stack) {
					PlayerHelper.syncInventory(ownerPlayer);
				}
				return itemStack;
			}

			@ItemStackNonNull
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				if (simulate)
					return getTarget().extractItem(slot, amount, true);
				EntityPlayerMP ownerPlayer = getOwnerPlayer();
				if (ownerPlayer == null) return StackHelper.empty();
				ItemStack itemStack = getTarget().extractItem(slot, amount, false);
				if (StackHelper.isNonNull(itemStack)) {
					PlayerHelper.syncInventory(ownerPlayer);
				}
				return itemStack;
			}
		};

		for (int i = 0; i < 4; i++) {
			final int finalI = i;
			playerInvHandlers[i + 1] = new SingleStackHandlerBase() {
				@ItemStackNonNull
				@Override
				public ItemStack getStack() {
					EntityPlayerMP ownerPlayer = getOwnerPlayer();
					if (ownerPlayer == null) return StackHelper.empty();
					return CompatHelper.getArray10List11(ownerPlayer.inventory.armorInventory).get(3 - finalI);
				}

				@Override
				public void setStack(@ItemStackNonNull ItemStack stack) {
					EntityPlayerMP ownerPlayer = getOwnerPlayer();
					if (ownerPlayer == null) return;
					ArrayAccess<ItemStack> arrayAccess = CompatHelper.getArray10List11(ownerPlayer.inventory.armorInventory);
					if (!ItemStack.areItemStacksEqual(stack, arrayAccess.get(3 - finalI))) {
						arrayAccess.set(3 - finalI, stack);
						PlayerHelper.syncInventory(ownerPlayer);
					}

				}

				@Override
				protected int getStackLimit(@Nonnull ItemStack stack) {
					if (StackHelper.isNull(stack)) return 0;
					EntityPlayerMP ownerPlayer = getOwnerPlayer();
					return ownerPlayer != null && stack.getItem().isValidArmor(stack, EntityEquipmentSlot.values()[2 + 3 - finalI], ownerPlayer) ? 1 : 0;
				}
			};
		}

		playerInvHandlers[5] = new IItemHandlerModifiableCompat() {
			@Override
			public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {

			}

			@Override
			public int getSlots() {
				return 1;
			}

			@Nonnull
			@Override
			public ItemStack getStackInSlot(int slot) {
				EntityPlayerMP ownerPlayer = getOwnerPlayer();
				return ownerPlayer != null ? CompatHelper.getArray10List11(ownerPlayer.inventory.offHandInventory).get(0) : StackHelper.empty();
			}

			@ItemStackNonNull
			@Override
			public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
				if (simulate)
					return getTarget().insertItem(slot, stack, true);
				EntityPlayerMP ownerPlayer = getOwnerPlayer();
				if (ownerPlayer == null) return stack;
				ItemStack itemStack = getTarget().insertItem(slot, stack, false);
				if (itemStack != stack) {
					PlayerHelper.syncInventory(ownerPlayer);
				}
				return itemStack;
			}

			@ItemStackNonNull
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				if (simulate)
					return getTarget().extractItem(slot, amount, true);
				EntityPlayerMP ownerPlayer = getOwnerPlayer();
				if (ownerPlayer == null) return StackHelper.empty();
				ItemStack itemStack = getTarget().extractItem(slot, amount, false);
				if (StackHelper.isNonNull(itemStack)) {
					PlayerHelper.syncInventory(ownerPlayer);
				}
				return itemStack;
			}
		};

		PLAYER_HANDLER = new ConcatFixedLength(playerInvHandlers) {
			@ItemStackNonNull
			@Override
			public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
				if (!insertable.contains(slot)) return stack;
				return super.insertItem(slot, stack, simulate);
			}

			@ItemStackNonNull
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				if (!extractable.contains(slot)) return StackHelper.empty();
				return super.extractItem(slot, amount, simulate);
			}
		};
	}

	public IItemHandlerModifiable getTarget() {
		EntityPlayerMP target = getOwnerPlayer();
		if (target == null)
			return EmptyHandlerModifiable.INSTANCE;
		return new InvWrapper(target.inventory);
	}

	@Nullable
	public EntityPlayerMP getOwnerPlayer() {
		if (profile == null || world == null || world.isRemote || !active)
			return null;
		EntityPlayerMP target = null;
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server != null)
			for (EntityPlayerMP playerMP : server.getPlayerList().getPlayers()) {
				if (profile.equals(playerMP.getGameProfile())) {
					if (playerMP.isDead) return null;
					target = playerMP;
				}
			}
		return target;
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return PLAYER_HANDLER;
	}

	@Override
	public void onPowerChanged() {
		markForUpdate();
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		if (profile != null) {
			String name = profile.getName();
			packet.writeString(name);
		} else
			packet.writeShort(0);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		String s = packet.readString();
		if (StringUtils.isNullOrEmpty(s))
			profile = null;
		else
			profile = new GameProfile(null, s);
	}

	@Override
	public float getPower() {
		return 4;
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (profile != null)
			compound.setTag("Owner", NBTHelper.proifleToNBT(profile));

		compound.setIntArray("InsertSlots", insertable.toArray());
		compound.setIntArray("ExtractSlots", extractable.toArray());
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		profile = NBTHelper.profileFromNBT(compound.getCompoundTag("Owner"));
		insertable.addAll(compound.getIntArray("InsertSlots"));
		extractable.addAll(compound.getIntArray("ExtractSlots"));
	}


	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);
		if (placer instanceof EntityPlayer) {
			if (PlayerHelper.isPlayerReal((EntityPlayer) placer)) {
				profile = ((EntityPlayer) placer).getGameProfile();
			} else {
				profile = null;
			}
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			if (ExtraUtils2.deobf_folder) {
				if (ExtraUtils2.proxy.isAltSneaking(playerIn)) {
					openGUI(playerIn, 2);
					return true;
				}
			}

			EntityPlayerMP ownerPlayer = getOwnerPlayer();
			if (ownerPlayer == playerIn)
				openGUI(playerIn, 0);
			else if (ownerPlayer != null)
				openGUI(playerIn, 1);
		}
		return true;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {
			return new DynamicPlayerChestConfig(player);
		}
		if (ID == 1) {
			if (world.isRemote) {
				return new DynamicPlayerChest(player, new ItemStackHandler(40));
			} else {
				return new DynamicPlayerChest(player, PLAYER_HANDLER);
			}
		}

		if (ID == 2) {
			if (world.isRemote) {
				return new DynamicPlayerChest(player, new ItemStackHandler(40));
			} else {
				List<EntityMinecartChest> ents = world.getEntitiesWithinAABB(EntityMinecartChest.class, new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).grow(10, 10, 10));
				if (ents == null || ents.isEmpty()) {
					return new DynamicPlayerChest(player, EmptyHandlerModifiable.INSTANCE);
				}

				EntityMinecartChest entityMinecartChest = ents.get(0);
				final InvWrapper invWrapper = new InvWrapper(entityMinecartChest);
				return new DynamicPlayerChest(player, new IItemHandlerModifiableCompat() {
					@Override
					public void setStackInSlot(int slot, @ItemStackNonNull ItemStack stack) {
						if (slot < invWrapper.getSlots()) invWrapper.setStackInSlot(slot, stack);
					}

					@Override
					public int getSlots() {
						return 40;
					}

					@ItemStackNonNull
					@Override
					public ItemStack getStackInSlot(int slot) {
						if (slot < invWrapper.getSlots()) return invWrapper.getStackInSlot(slot);
						return StackHelper.empty();
					}

					@ItemStackNonNull
					@Override
					public ItemStack insertItem(int slot, @ItemStackNonNull ItemStack stack, boolean simulate) {
						if (slot < invWrapper.getSlots()) return invWrapper.insertItem(slot, stack, simulate);
						return stack;
					}

					@ItemStackNonNull
					@Override
					public ItemStack extractItem(int slot, int amount, boolean simulate) {
						if (slot < invWrapper.getSlots()) return invWrapper.extractItem(slot, amount, simulate);
						return StackHelper.empty();
					}
				});
			}
		}


		return null;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

	}

	public class DynamicPlayerChest extends DynamicContainerTile {
		IItemHandler target;

		public DynamicPlayerChest(EntityPlayer player, IItemHandler target) {
			super(TilePlayerChest.this);
			this.target = target;
			int x = 4, y = 4;
			addWidget(new WidgetTextData(x, y, DynamicContainer.playerInvWidth) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeString(profile == null ? "" : profile.getName());
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					String s = packet.readString();
					if (StringUtils.isNullOrEmpty(s)) return "";
					return Lang.translateArgs("%s's Inventory", s);
				}
			});


			y += 9;

			for (int i = 0; i < 4; i++) {
				final int finalI = i;
				addWidget(new WidgetSlotAccess(target, 36 + finalI, x + finalI * 18, y) {
					@SideOnly(Side.CLIENT)
					public String getSlotTexture() {
						return ItemArmor.EMPTY_SLOT_NAMES[3 - finalI];
					}

					public int getSlotStackLimit() {
						return 1;
					}

					public boolean isItemValid(ItemStack stack) {
						if (!super.isItemValid(stack)) return false;
						EntityPlayerMP ownerPlayer = getOwnerPlayer();
						return ownerPlayer != null && stack.getItem().isValidArmor(stack, EntityEquipmentSlot.values()[2 + finalI], ownerPlayer);
					}
				});
			}

			y += 18;

			for (int j = 0; j < 3; ++j) {
				for (int k = 0; k < 9; ++k) {
					addWidget(new WidgetSlotAccess(target, k + j * 9 + 9, x + k * 18, y + 14 + j * 18));
				}
			}

			for (int j = 0; j < 9; ++j) {
				addWidget(new WidgetSlotAccess(target, j, x + j * 18, y + 14 + 58));
			}

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}

		@Override
		public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
			return super.canInteractWith(playerIn);
		}

		public class WidgetSlotAccess extends WidgetSlotItemHandler implements IWidgetServerNetwork {
			byte accessType = 3;

			public WidgetSlotAccess(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
				super(itemHandler, index, xPosition, yPosition);
			}

			@Override
			public void addToDescription(XUPacketBuffer packet) {
				accessType = (byte) ((insertable.contains(index) ? 1 : 0) | (extractable.contains(index) ? 2 : 0));
				packet.writeByte(accessType);
			}

			@Override
			public void handleDescriptionPacket(XUPacketBuffer packet) {
				accessType = packet.readByte();
			}

			@Override
			public boolean isItemValid(ItemStack stack) {
				return (accessType & 1) != 0 && super.isItemValid(stack);
			}

			@Override
			public boolean canTakeStack(EntityPlayer playerIn) {
				return (accessType & 2) != 0 && super.canTakeStack(playerIn);
			}


			@Override
			@SideOnly(Side.CLIENT)
			public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
				GlStateManager.color(1, 1, 1, 1);
				gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 0, 0, 18, 18);
				int color = col[accessType & 3];
				GlStateManager.color(ColorHelper.getRF(color), ColorHelper.getGF(color), ColorHelper.getBF(color), 1);
				gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1, 1, 1, 16, 16);
			}

		}
	}

	public class DynamicPlayerChestConfig extends DynamicContainerTile {
		int clickType = -1;

		public DynamicPlayerChestConfig(EntityPlayer player) {
			super(TilePlayerChest.this);
			int x = 4, y = 4;
			addWidget(new WidgetTextData(x, y, DynamicContainer.playerInvWidth) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeString(profile == null ? "" : profile.getName());
				}

				@Override
				@SideOnly(Side.CLIENT)
				protected String constructText(XUPacketBuffer packet) {
					String s = packet.readString();
					if (StringUtils.isNullOrEmpty(s)) return "";
					return Lang.translateArgs("%s's Inventory", s);
				}
			});

			y += 9;

			addWidget(new WidgetText(x, y, Lang.translate("Configuration")));

			y += 9;

			for (int i = 0; i < 4; i++) {
				final int finalI = i;
				addWidget(new WidgetClickableSlot(36 + finalI, x + finalI * 18, y) {
					@Override
					@SideOnly(Side.CLIENT)
					public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
						super.renderBackground(manager, gui, guiLeft, guiTop);
						if (StackHelper.isNonNull(displayStack)) return;
						TextureAtlasSprite textureExtry = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(ItemArmor.EMPTY_SLOT_NAMES[3 - finalI]);
						if (textureExtry != null) {
							manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
							gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1, textureExtry, 16, 16);
							manager.bindTexture(gui.getWidgetTexture());
						}

					}
				});
			}

			y += 18;

			for (int j = 0; j < 3; ++j) {
				for (int k = 0; k < 9; ++k) {
					addWidget(new WidgetClickableSlot(k + j * 9 + 9, x + k * 18, y + 14 + j * 18));
				}
			}

			for (int j = 0; j < 9; ++j) {
				addWidget(new WidgetClickableSlot(j, x + j * 18, y + 14 + 58));
			}

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}

		@ItemStackNonNull
		@Override
		public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
			return super.transferStackInSlot(par1EntityPlayer, par2);
		}

		@Override
		public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
			if (!isClient) {
				if (playerIn != getOwnerPlayer()) {
					return false;
				}
			}

			return super.canInteractWith(playerIn) && active;
		}

		public class WidgetClickableSlot extends WidgetClick implements IWidgetServerNetwork {
			private final int slot;
			ItemStack displayStack;
			byte accessType = 0;

			boolean overMe = false;

			public WidgetClickableSlot(int slot, int x, int y) {
				super(x, y, 18, 18);
				this.slot = slot;
			}

			@Override
			public void onClick(byte b) {
				if ((b & 1) != 0)
					insertable.add(slot);
				else
					insertable.remove(slot);

				if ((b & 2) != 0)
					extractable.add(slot);
				else
					extractable.remove(slot);

			}

			@Override
			public void addToDescription(XUPacketBuffer packet) {
				packet.writeByte((insertable.contains(slot) ? 1 : 0) | (extractable.contains(slot) ? 2 : 0));
				packet.writeItemStack(PLAYER_HANDLER.getStackInSlot(slot));
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void handleDescriptionPacket(XUPacketBuffer packet) {
				accessType = packet.readByte();
				displayStack = packet.readItemStack();
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
				if (StackHelper.isNull(displayStack)) return;
				GlStateManager.enableLighting();
				RenderHelper.enableGUIStandardItemLighting();
				gui.renderStack(displayStack, guiLeft + getX() + 1, guiTop + getY() + 1, null);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.disableLighting();


			}

			@Override
			@SideOnly(Side.CLIENT)
			public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

				GlStateManager.color(1, 1, 1, 1);
				gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 0, 0, 18, 18);
				int color = col[accessType & 3];
				GlStateManager.color(ColorHelper.getRF(color), ColorHelper.getGF(color), ColorHelper.getBF(color), 1);
				gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + 1, 1, 1, 16, 16);
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void mouseClicked(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {
				if (mouseOver && (mouseButton == 0 || mouseButton == 1)) {
					clickType = (accessType + (mouseButton * 2 - 1)) & 3;
					overMe = true;
					sendClick(clickType);
				}
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void mouseReleased(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {
				super.mouseReleased(mouseX, mouseY, mouseButton, mouseOver);
				clickType = -1;
				overMe = false;

			}

			@Override
			@SideOnly(Side.CLIENT)
			public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastMove, boolean mouseOver) {
				if (mouseOver && (clickType != -1)) {
					if (!overMe) {
						sendClick(clickType);
						overMe = true;
					}
				}
			}

			@Override
			@SideOnly(Side.CLIENT)
			public List<String> getToolTip() {
				ArrayList<String> strings = Lists.newArrayList();
				switch (accessType) {
					case 0:
						strings.add(ChatFormatting.GRAY + Lang.translate("No Access") + ChatFormatting.RESET);
						break;
					case 1:
						strings.add(ChatFormatting.RED + Lang.translate("Insert Only") + ChatFormatting.RESET);
						break;
					case 2:
						strings.add(ChatFormatting.GREEN + Lang.translate("Extract Only") + ChatFormatting.RESET);
						break;
					case 3:
						strings.add(ChatFormatting.WHITE + Lang.translate("Full Access") + ChatFormatting.RESET);
						break;
				}
				return strings;
			}
		}
	}
}
