package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.eventhandlers.ItemEntityInteractionOverride;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.UUID;

public class ItemContract extends XUItemFlatMetadata implements IDynamicHandler {
	public static final String TAG_UNDER_CONTRACT = "Contracted";
	static final String TAG_CONTRACT_LEVEL = "ContractLevel";

	public ItemContract() {
		super("contract");
		ItemEntityInteractionOverride.items.add(this);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		if (!(target instanceof EntityVillager)) {
			return false;
		}
		EntityVillager villager = (EntityVillager) target;
		if (villager.world.isRemote) return true;
		if (villager.isChild()) return true;
		if (villager.getCustomer() != null) return true;

		if (!PlayerHelper.isPlayerReal(playerIn)) return true;

		NBTTagCompound data = villager.getEntityData();
		if (data.getBoolean(TAG_UNDER_CONTRACT))
			return false;

		if (!data.hasKey(TAG_CONTRACT_LEVEL, Constants.NBT.TAG_INT)) {
			int value;
			switch (playerIn.world.rand.nextInt(3)) {
				case 0:
					value = 2;
					break;
				default:
					value = 0;
					break;
			}

			data.setInteger(TAG_CONTRACT_LEVEL, value);
		}

		int i = data.getInteger(TAG_CONTRACT_LEVEL);

		int r = 0;
		UUID uuid = villager.getPersistentID();
		long l;
		l = uuid.getLeastSignificantBits();
		r = r ^ (int) ((l >>> 32) ^ (l));
		l = uuid.getMostSignificantBits();
		r = r ^ (int) ((l >>> 32) ^ (l));

		playerIn.openGui(ExtraUtils2.instance, -1, playerIn.world, villager.getEntityId(), r, i);

		return true;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int id, int rng, int responseLevel) {
		Entity entityByID = world.getEntityByID(id);
		if (!(entityByID instanceof EntityVillager)) return null;
		EntityVillager villager = (EntityVillager) entityByID;
		villager.setCustomer(player);
		return new ContainerContract(villager, player, id, rng, responseLevel);
	}

	public static class ContainerContract extends DynamicContainer {
		public static final ResourceLocation texBackground = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/parchment.png");
		private static final BigInteger TWO_64 = BigInteger.ONE.shiftLeft(64);
		static String[] keys = new String[]{
				"villager.response.no_deal",
				"villager.response.trade_first",
				"villager.response.deal"
		};
		public final DynamicWindow sideWindow;
		EntityVillager villager;
		EntityPlayer player;
		InventoryPlayer inventory;

		public ContainerContract(final EntityVillager villager, final EntityPlayer player, int id, int rng, final int responseLevel) {
			this.villager = villager;
			this.player = player;
			this.inventory = player.inventory;

			final WidgetTextSmallText text = new WidgetTextSmallText(9, 9, "", 111);
			addWidget(text);

			addWidget(new WidgetRawData() {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					EntityVillager villager1 = ContainerContract.this.villager;
					UUID uniqueID = villager1.getUniqueID();
					packet.writeLong(uniqueID.getLeastSignificantBits() | uniqueID.getMostSignificantBits());
					packet.writeChatComponent(villager1.getDisplayName());
				}

				@Override
				public void handleDescriptionPacket(XUPacketBuffer packet) {
					long num = packet.readLong();
					ITextComponent iChatComponent = packet.readChatComponent();
					text.msg = Lang.translateArgs(false, "villager.contract",
							"I, \"Villager No. %s\",\nalso known as \"%s\",\n\nbeing of sound mind and body, do agree to allow my physical, spiritual and/or mental essences to be bound to a physical object, and to use any skills that I may currently possess to provide useful service to \"%s\". I agree that my essence shall remain bound to the physical object until either...\n\n1. The object is destroyed\n2. The universe ends\n3. The very concept of time and/or entropy is destroyed or rendered untenable\n4. I get bored of standing around in limbo making weird noises, and float away to the next life (which is unlikely since I like making weird noises).\n\n\nSigned  ______________",
							unsigned(num),
							iChatComponent.getFormattedText(),
							ContainerContract.this.player.getName()
					).replaceAll("\\\\n", "\n");
				}
			});

			width = 130;
			height = 165;

			sideWindow = new DynamicWindow(DynamicWindow.WindowSide.RIGHT);
			IWidget w = new WidgetEntity(villager, 20, 4, 4, 100, 60);
			addWidget(w, sideWindow);

			int i = responseLevel % 3;
			if (i == 2) {
				addWidget(w = new WidgetButton(w.getX(), w.getY() + w.getH() + 4, w.getW(), 20, Lang.translate("Sign")) {
					@Override
					public void onClickServer(XUPacketBuffer buffer) {
						if (villager.isDead) return;

						NBTTagCompound data = villager.getEntityData();
						if (data.getBoolean(TAG_UNDER_CONTRACT))
							return;

						int i = data.getInteger(TAG_CONTRACT_LEVEL);

						if (i == 2) {
							if (player.inventory.clearMatchingItems(XU2Entries.contract.value, -1, 1, null) != 0) {
								data.setBoolean(TAG_UNDER_CONTRACT, true);
							}
							player.closeScreen();
						}
					}
				}, sideWindow);
			}

			addWidget(w = new WidgetTextMultiline(
							w.getX(),
							w.getY() + w.getH() + 4,
							Lang.random(keys[i], rng).replaceAll("\\\\n", "\n"),
							w.getW()),
					sideWindow);


			validate();
		}

		private static String unsigned(long num) {
			BigInteger a = BigInteger.valueOf(num);
			if (a.signum() < 0) a = a.add(TWO_64);
			return a.toString();
		}

		@Override
		public void onContainerClosed(EntityPlayer playerIn) {
			villager.setCustomer(null);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean drawBackgroundOverride(DynamicGui gui) {
			Minecraft.getMinecraft().renderEngine.bindTexture(texBackground);
			gui.drawTexturedModalRect(gui.guiLeft, gui.guiTop, 0, 0, width, height);
			return true;
		}

		@Override
		public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
			return this.villager.getCustomer() == playerIn && !villager.isDead;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void loadGuiDimensions(DynamicGui dynamicGui) {
			dynamicGui.xSize = width;
			dynamicGui.ySize = height;
			dynamicGui.guiLeft = (dynamicGui.width - dynamicGui.xSize - sideWindow.w) / 2;
			dynamicGui.guiTop = (dynamicGui.height - dynamicGui.ySize) / 2;
		}


	}
}
