package com.rwtema.extrautils2.tile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.eventhandlers.ItemCaptureHandler;
import com.rwtema.extrautils2.fakeplayer.XUFakePlayer;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.DynamicGui;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.itemhandler.*;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TileMine extends TileAdvInteractor {
	private final static ItemStack genericDigger = new ItemStack(Items.DIAMOND_PICKAXE, 1);
	private final ItemStackHandler contents = registerNBT("contents", new XUTileItemStackHandler(9, this));
	public final IItemHandler publicHandler = new PublicWrapper.Extract(contents);
	private final StackDump extraStacks = registerNBT("extrastacks", new StackDump());
	private ItemStack diggerTool;
	private final SingleStackHandler enchants = registerNBT("enchants", new SingleStackHandler() {
		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			if (stack.getItem() != Items.ENCHANTED_BOOK) {
				return 0;
			}

			Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
			if (map.isEmpty())
				return 0;
			for (Enchantment enchantment : map.keySet()) {
				if (enchantment.canApply(genericDigger))
					return 1;
			}

			return 0;
		}

		@Override
		protected void onContentsChanged() {
			markDirty();
			diggerTool = null;
		}
	});
	private XUFakePlayer fakePlayer;

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return Iterables.concat(
				InventoryHelper.getItemHandlerIterator(contents),
				extraStacks,
				InventoryHelper.getItemHandlerIterator(enchants),
				InventoryHelper.getItemHandlerIterator(upgrades));
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return publicHandler;
	}

	@Override
	public boolean operate() {
		if (!extraStacks.stacks.isEmpty()) {
			extraStacks.attemptDump(contents);
			return false;
		}

		boolean success = false;
		XUBlockState state = getBlockState();

		EnumFacing side = state.getValue(XUBlockStateCreator.ROTATION_ALL);
		BlockPos offset = getPos().offset(side);

		World world = this.world;
		TileEntity tileEntity = world.getTileEntity(offset);
		if (tileEntity instanceof IRemoteTarget) {
			if (world.isRemote) return true;

			Optional<Pair<World, BlockPos>> targetPos = ((IRemoteTarget) tileEntity).getTargetPos();
			if (!targetPos.isPresent()) {
				return false;
			} else {
				Pair<World, BlockPos> pair = targetPos.get();
				world = pair.getLeft();
				offset = pair.getRight();
			}
		}

		if (world.isAirBlock(offset)) return false;

		if (fakePlayer == null) {
			fakePlayer = new XUFakePlayer((WorldServer) world, owner, Lang.getItemName(getXUBlock()));
		}

		if (StackHelper.isNull(diggerTool)) {
			diggerTool = genericDigger.copy();
			ItemStack enchantsStack = enchants.getStack();
			if (StackHelper.isNonNull(enchantsStack)) {
				EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(enchantsStack), diggerTool);
			}
		}

		IBlockState blockState = world.getBlockState(offset);
		if (blockState.getMaterial().isLiquid()) return false;

		float hardness = blockState.getPlayerRelativeBlockHardness(fakePlayer, world, offset);
		if (hardness == 0) return false;

		fakePlayer.setHeldItem(EnumHand.MAIN_HAND, diggerTool.copy());
		fakePlayer.setLocationEdge(offset, side);
		ItemCaptureHandler.startCapturing();
		success = fakePlayer.interactionManager.tryHarvestBlock(offset);
		LinkedList<ItemStack> stacks = ItemCaptureHandler.stopCapturing();
		for (ItemStack stack : stacks) {
			InventoryHelper.insertWithRunoff(contents, stack, extraStacks);
		}

		fakePlayer.setHeldItem(EnumHand.MAIN_HAND, StackHelper.empty());

		InventoryPlayer inventory = fakePlayer.inventory;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (StackHelper.isNonNull(stack) && StackHelper.getStacksize(stack) > 0)
				InventoryHelper.insertWithRunoff(contents, stack, extraStacks);
		}

		fakePlayer.clearInventory();
		if(tileEntity instanceof IRemoteTarget){
			((IRemoteTarget) tileEntity).onSuccessfulInteract(world, pos, side, success );
		}
		return true;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMiner(player);
	}

	private class ContainerMiner extends DynamicContainerTile {
		public ContainerMiner(EntityPlayer player) {
			super(TileMine.this);
			addTitle("Miner");
			crop();
			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					addWidget(new WidgetSlotItemHandler(contents, x + y * 3, centerSlotX - 18 + 18 * x, height + 4 + 18 * y));
				}
			}

			addWidget(new WidgetSlotItemHandler(enchants, 0, height + 4 + 18, 4) {
				@Override
				@SideOnly(Side.CLIENT)
				public List<String> getToolTip() {
					if (!getHasStack()) {
						return ImmutableList.of(Lang.translate("Enchanted Book"));
					}
					return null;
				}

				@Override
				@SideOnly(Side.CLIENT)
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					super.renderBackground(manager, gui, guiLeft, guiTop);
					if (!getHasStack()) {
						ItemStack stack = ItemIngredients.Type.ENCHANTED_BOOK_SKELETON.newStack();
						gui.renderStack(stack, guiLeft + getX() + 1, guiTop + getY() + 1, "");
					}
				}
			});
			addWidget(getRSWidget(playerInvWidth - 18 - 4, height + 4 + 18, redstone_state, pulses));
			addWidget(upgrades.getSpeedUpgradeSlot(playerInvWidth - 18 - 4, height + 4 + 18 * 2));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}

}
