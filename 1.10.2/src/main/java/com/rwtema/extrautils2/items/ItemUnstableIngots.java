package com.rwtema.extrautils2.items;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.IRegisterItemColors;
import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.compatibility.XUShapedRecipe;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.crafting.PlayerSpecificCrafting;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.datastructures.GetterSetter;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ItemUnstableIngots extends XUItemFlat implements IRegisterItemColors {
	public final static String[][] textures = {
			{"unstable_ingot_outline", "unstable_ingot_interior", "unstable_ingot_ghost"},
			{"unstable_nugget_outline", "unstable_nugget_interior", "unstable_nugget_ghost"}};

	public static int TIME_OUT = 10 * 20;
	public static HashSet<Class<?>> ALLOWED_CLASSES = Sets.newHashSet(ContainerWorkbench.class);

	private static int cooldown = 0;

	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public ItemUnstableIngots() {
		setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addItemColors(ItemColors itemColors, BlockColors blockColors) {
		itemColors.registerItemColorHandler(this::getColorFromItemstack, this);
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemstack(ItemStack stack, int tintIndex) {
		World world = ExtraUtils2.proxy.getClientWorld();
		if (world != null && stack.getMetadata() == 0 && stack.hasTagCompound()) {
			NBTTagCompound tagCompound = Validate.notNull(stack.getTagCompound());
			long baseTime = tagCompound.getLong("time");
			if (baseTime > 0) {
				float time = (ItemUnstableIngots.TIME_OUT + baseTime - world.getTotalWorldTime() - MCTimer.renderPartialTickTime) + tintIndex * 3;
				if (time < 0) {
					return ColorHelper.colorClamp((float) 1, 0.5F + (world.rand.nextFloat() * 2 - 1) * 0.3F, (float) 0, 1);
				} else if (time > ItemUnstableIngots.TIME_OUT) {
					return 0xffffffff;
				} else {
					float v = time / ItemUnstableIngots.TIME_OUT;
					double v1 = Math.log(v) * -15.49 * 2;
					float g = 0.5F + (float) Math.cos(v1) * 0.3F + 0.2F * v;
					return ColorHelper.colorClamp(1, g, g * v, 1);
				}
			}
		}
		if (tintIndex == 2) return 0;

		return 0xffffffff;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		for (String[] texture : textures) {
			Textures.register(texture);
		}
	}

	@Override
	public void getSubItemsBase(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < 3; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public int getMaxMetadata() {
		return 2;
	}

	@Override
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		if (itemStack == null) return textures[0][0];
		return textures[itemStack.getMetadata() % 2][renderPass % 2];
	}

	@Override
	public int getRenderLayers(@Nullable ItemStack itemStack) {
		return 2;
	}

	@Override
	public boolean renderLayerIn3D(ItemStack stack, int renderPass) {
		return renderPass == 0;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		if (stack.getMetadata() == 0) return 1;
		return 64;
	}

	@Override
	public int getTint(ItemStack stack, int i) {
		return i;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (stack.getMetadata() == 0) {
			cooldown = TIME_OUT * 2;
		}
	}

	public void addRecipes() {
//		CraftingHelper.addShaped("unstable_ingot", ;
		CraftingHelper.addRecipe(new UnstableIngotRecipe(CraftingHelper.createLocation("unstable_ingot")));

		CraftingHelper.addShaped("unstable_nugget", XU2Entries.unstableIngots.newStack(1, 1), "i", "d", "s", 'i', "nuggetIron", 'd', "stickWood", 's', "gemDiamond");
		CraftingHelper.addShaped("stable_unstable_ingot", XU2Entries.unstableIngots.newStack(1, 2), "nnn", "nnn", "nnn", 'n', XU2Entries.unstableIngots.newStack(1, 1));
		CraftingHelper.addShapeless("unstable_unpack", XU2Entries.unstableIngots.newStack(9, 1), XU2Entries.unstableIngots.newStack(1, 2));
	}

	@SubscribeEvent
	public void checkForExplosion(TickEvent.ServerTickEvent event) {
		if (cooldown > 0)
			cooldown--;
	}

	@SubscribeEvent
	public void checkForExplosion(TickEvent.PlayerTickEvent event) {
		if (cooldown == 0 || event.player.world.isRemote) return;
		Container openContainer = event.player.openContainer;


		LinkedList<GetterSetter<ItemStack>> itemStacks = Lists.newLinkedList();

		InventoryPlayer inventory = event.player.inventory;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			itemStacks.add(new GetterSetter.InvSlot(inventory, i));
		}
		itemStacks.add(new GetterSetter.PlayerHand(inventory));

		int windowId;
		if (openContainer == null || openContainer == event.player.inventoryContainer) {
			windowId = -1;
		} else {
			windowId = openContainer.windowId;
			for (Slot slot : openContainer.inventorySlots) {
				itemStacks.add(new GetterSetter.ContainerSlot(slot));
			}
		}

		LinkedList<GetterSetter<ItemStack>> stacks = Lists.newLinkedList();

		for (GetterSetter<ItemStack> getterSetter : itemStacks) {
			ItemStack stack = getterSetter.get();

			if (StackHelper.isNull(stack) || stack.getItem() != this) {
				continue;
			}
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null || !tag.hasKey("time", Constants.NBT.TAG_ANY_NUMERIC)) {
				continue;
			}

			cooldown = TIME_OUT * 2;

			if (tag.getLong("time") + ItemUnstableIngots.TIME_OUT > event.player.world.getTotalWorldTime() && tag.getInteger("dim") == event.player.world.provider.getDimension() && tag.getInteger("container") == windowId) {
				continue;
			}

			stacks.add(getterSetter);
		}

		if (stacks.isEmpty()) return;

		for (GetterSetter<ItemStack> stackGetterSetter : stacks) {
			ItemStack stack = stackGetterSetter.get();
			StackHelper.setStackSize(stack, 0);
			stackGetterSetter.accept(StackHelper.empty());
		}

		explodePlayer(event.player);

		if (windowId != -1) {
			event.player.closeScreen();
		}

		event.player.inventoryContainer.detectAndSendChanges();
	}

	@SubscribeEvent
	public void onPlayerEvent(PlayerEvent event) {
		cooldown = TIME_OUT * 20;
	}

	@SubscribeEvent
	public void onToss(ItemTossEvent event) {

		ItemStack stack = event.getEntityItem().getItem();
		if (stack.getItem() != this) {
			return;
		}
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null || !tag.hasKey("time", Constants.NBT.TAG_ANY_NUMERIC)) {
			return;
		}
		if (!event.getEntityItem().world.isRemote) {
			explodePlayer(event.getPlayer());
		}

		event.getEntityItem().setItem(StackHelper.empty());
		event.getEntityItem().setDead();
	}

	private void explodePlayer(EntityPlayer player) {
		player.attackEntityFrom(DamageSource.causeExplosionDamage(player).setDamageBypassesArmor().setDamageIsAbsolute(), 1000);
		player.world.newExplosion(player, player.posX, player.posY, player.posZ, 6, true, false);
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		return super.onDroppedByPlayer(item, player);
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Nullable
	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack) {
		return super.createEntity(world, location, itemstack);
	}

	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		if (stack.getMetadata() == 0 && playerIn != null && playerIn.openContainer != null && playerIn.openContainer != playerIn.inventoryContainer) {
			NBTTagCompound tag = NBTHelper.getOrInitTagCompound(stack);
			NBTTagCompound profile = NBTHelper.proifleToNBT(playerIn.getGameProfile());
			tag.setTag("owner", profile);
			tag.setInteger("container", playerIn.openContainer.windowId);
			tag.setInteger("dim", playerIn.world.provider.getDimension());
			tag.setLong("time", playerIn.world.getTotalWorldTime());
			stack.setTagCompound(tag);
			if (!worldIn.isRemote) {
				cooldown = TIME_OUT * 2;
			}
		}
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + stack.getMetadata();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (stack.getMetadata() == 0) {
			World world = ExtraUtils2.proxy.getClientWorld();
			if (world != null && stack.hasTagCompound()) {
				NBTTagCompound tagCompound = Validate.notNull(stack.getTagCompound());
				float time = (ItemUnstableIngots.TIME_OUT + tagCompound.getLong("time") - world.getTotalWorldTime()) / 20F;
				if (time > 0) {
					tooltip.add(Lang.translateArgs("Explosion in %s", StringHelper.format(time)));
				}
			} else {
				tooltip.add(Lang.translate("ERROR: Divide by diamond"));
				tooltip.add(Lang.translate("This ingot is highly unstable and will explode after 10 seconds."));
				tooltip.add(Lang.translate("Will also explode if the crafting window is closed or the ingot is thrown on the ground."));
				tooltip.add(Lang.translate("Additionally these ingots do not stack"));
				tooltip.add(Lang.translate(" - Do not craft unless ready -"));
				tooltip.add("");
				tooltip.add(Lang.translate("Must be crafted in a vanilla crafting table."));
			}
		}
	}

	public static class UnstableIngotRecipe extends PlayerSpecificCrafting {

		public UnstableIngotRecipe(ResourceLocation location) {
			super(location, new XUShapedRecipe(location, XU2Entries.unstableIngots.newStack(1, 0), "i", "d", "s", 'i', "ingotIron", 'd', "stickWood", 's', "gemDiamond"));
		}

		@Nullable
		@Override
		public int[] getDimensions() {
			return new int[]{1, 3};
		}

		@Override
		public List<List<ItemStack>> getInputList() {
			ImmutableList.Builder<List<ItemStack>> builder = ImmutableList.builder();
			builder.add(OreDictionary.getOres("ingotIron"));
			builder.add(OreDictionary.getOres("stickWood"));
			builder.add(OreDictionary.getOres("gemDiamond"));
			return builder.build();
		}

		@Override
		public ItemStack getRecipeOutput() {
			return super.getRecipeOutput();
		}

		@Override
		protected void updatePlayer(EntityPlayerMP foundPlayer) {

		}

		@Override
		protected boolean isValidForCrafting(EntityPlayer foundPlayer) {
			return foundPlayer.openContainer != null && (ALLOWED_CLASSES.contains(foundPlayer.openContainer.getClass()));
		}

		@Override
		public String info() {
			return null;
		}

		@Override
		protected void finishedCrafting(EntityPlayer player, ItemStack recipeOutput) {

		}

		@Override
		protected void addTooltip(ItemTooltipEvent event, ItemStack itemStack) {

		}
	}

}
