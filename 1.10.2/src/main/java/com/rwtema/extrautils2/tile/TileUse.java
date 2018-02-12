package com.rwtema.extrautils2.tile;

import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.blocks.BlockAdvInteractor;
import com.rwtema.extrautils2.compatibility.BlockCompat;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.ItemCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.fakeplayer.XUFakePlayer;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.StackDump;
import com.rwtema.extrautils2.itemhandler.XUTileItemStackHandler;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.datastructures.ThreadLocalBoolean;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Random;

import static net.minecraftforge.common.ForgeHooks.onLeftClickBlock;
import static net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec;

public class TileUse extends TileAdvInteractor {
	private final ItemStackHandler contents = registerNBT("contents", new XUTileItemStackHandler(9, this));
	private final StackDump extraStacks = registerNBT("extrastacks", new StackDump());
	private final NBTSerializable.NBTBoolean sneak = registerNBT("sneak", new NBTSerializable.NBTBoolean(false));
	NBTSerializable.NBTEnum<Button> button = registerNBT("button", new NBTSerializable.NBTEnum<>(Button.RIGHT_CLICK));
	NBTSerializable.NBTEnum<Mode> mode = registerNBT("mode", new NBTSerializable.NBTEnum<>(Mode.GENERIC_CLICK));
	NBTSerializable.NBTEnum<Select> select = registerNBT("select", new NBTSerializable.NBTEnum<>(Select.RANDOM_SLOT));

	private XUFakePlayer fakePlayer;

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return Iterables.concat(InventoryHelper.getItemHandlerIterator(contents),
				extraStacks,
				InventoryHelper.getItemHandlerIterator(upgrades));
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return contents;
	}

	@Override
	public void operate() {
		if (!extraStacks.stacks.isEmpty()) {
			extraStacks.attemptDump(contents);
			return;
		}

		XUBlockState state = getBlockState();
		EnumFacing side = state.getValue(XUBlockStateCreator.ROTATION_ALL);
		BlockPos pos = getPos().offset(side);

		int slotNo = 0;
		ItemStack stack = null;

		switch (select.value) {
			case UPPER_LEFT_SLOT_ONLY:
				slotNo = 0;
				stack = contents.getStackInSlot(slotNo);
				break;
			case RANDOM_SLOT:
				int[] ind = new int[9];
				Random rand = world.rand;
				for (int i = 1; i < ind.length; i++) {
					int j = rand.nextInt(1 + i);
					ind[i] = ind[j];
					ind[j] = i;
				}

				for (int i : ind) {
					slotNo = i;
					stack = contents.getStackInSlot(i);
					if (StackHelper.isNonNull(stack))
						break;
				}

				break;
		}


		if (fakePlayer == null) {
			fakePlayer = new XUFakePlayer((WorldServer) world, owner, Lang.getItemName(getXUBlock()));
		}

		ItemStack copy;
		fakePlayer.setLocationEdge(pos, side);
		fakePlayer.clearInventory();

		InventoryPlayer inventory = fakePlayer.inventory;

		for (int i = 0; i < 9; i++) {
			inventory.setInventorySlotContents(i, StackHelper.safeCopy(contents.getStackInSlot(i)));
		}
		inventory.currentItem = slotNo;

		fakePlayer.updateAttributes();
		fakePlayer.setSneaking(sneak.value);

		copy = inventory.getStackInSlot(slotNo);

		IBlockState blockState = world.getBlockState(pos);

		float hitX = (float) (fakePlayer.posX - pos.getX());
		float hitY = (float) (fakePlayer.posY - pos.getY());
		float hitZ = (float) (fakePlayer.posZ - pos.getZ());

		try {
			switch (mode.value) {
				case GENERIC_CLICK:
					if (StackHelper.isNonNull(copy)) {
						switch (button.value) {
							case LEFT_CLICK:
								fakePlayer.interactionManager.onBlockClicked(pos, side);
								for (int i = 0; i < 20; i++) {
									fakePlayer.interactionManager.updateBlockRemoving();
								}
								fakePlayer.interactionManager.blockRemoving(pos);
								fakePlayer.interactionManager.cancelDestroyingBlock();
								break;
							case RIGHT_CLICK:
								fakePlayer.interactionManager.processRightClick(fakePlayer, world, copy, EnumHand.MAIN_HAND);
								break;
						}
					}
					break;
				case PLACE_BLOCK:
					if (button.value == Button.RIGHT_CLICK && StackHelper.isNonNull(copy)) {
						if (blockState.getBlock().isReplaceable(world, pos)) {
							if (copy.getItem() instanceof ItemSeedFood) {
								ItemCompat.invokeOnItemUse(copy, fakePlayer, world, pos.down(), EnumHand.MAIN_HAND, EnumFacing.UP, hitX, hitY, hitZ);
							} else if (copy.getItem() instanceof ItemBlock) {
								ItemBlock itemBlock = (ItemBlock) copy.getItem();


								if (!world.isRemote) {
									int i = itemBlock.getMetadata(copy.getMetadata());
									IBlockState placedState = BlockCompat.invokeGetStateForPlacement(itemBlock.getBlock(), world, pos, side, hitX, hitY, hitZ, i, fakePlayer, EnumHand.MAIN_HAND, stack);
									if (itemBlock.placeBlockAt(copy, fakePlayer, world, pos, side, hitX, hitY, hitZ, placedState)) {
										StackHelper.decrease(copy);
									}
								}

							}
						}
					}
					break;
				case USE_ITEM_ON_BLOCK:

					switch (button.value) {
						case LEFT_CLICK:
							LeftClickBlock event2 = onLeftClickBlock(fakePlayer, pos, side, rayTraceEyeHitVec(fakePlayer, 2));
							if (!event2.isCanceled() && event2.getUseItem() != Result.DENY) {
								EventHandler.cancelClickBlock.set(true);
								fakePlayer.interactionManager.onBlockClicked(pos, side);
								EventHandler.cancelClickBlock.set(false);
								for (int i = 0; i < 20; i++) {
									fakePlayer.interactionManager.updateBlockRemoving();
								}
								fakePlayer.interactionManager.blockRemoving(pos);
								fakePlayer.interactionManager.cancelDestroyingBlock();
							}
							break;
						case RIGHT_CLICK:
							if (StackHelper.isNonNull(copy)) {
								RightClickBlock event = BlockCompat.onRightClickBlock(fakePlayer, EnumHand.MAIN_HAND, copy, pos, side, rayTraceEyeHitVec(fakePlayer, 2));
								if (!event.isCanceled() && event.getUseItem() != Result.DENY) {
									if (ItemCompat.invokeOnItemUseFirst(copy, fakePlayer, world, pos, side, hitX, hitY, hitZ, EnumHand.MAIN_HAND) == EnumActionResult.PASS) {
										copy.onItemUse(fakePlayer, world, pos, EnumHand.MAIN_HAND, side, hitX, hitY, hitZ);
									}
								}
							}
							break;

					}
					break;
				case ACTIVATE_BLOCK_WITH_ITEM:
					switch (button.value) {
						case LEFT_CLICK:
							LeftClickBlock event2 = onLeftClickBlock(fakePlayer, pos, side, rayTraceEyeHitVec(fakePlayer, 2));
							if (!event2.isCanceled() && event2.getUseBlock() != Result.DENY) {
								blockState.getBlock().onBlockClicked(world, pos, fakePlayer);
								world.extinguishFire(null, pos, side);
							}
							break;
						case RIGHT_CLICK:
							RightClickBlock event = BlockCompat.onRightClickBlock(fakePlayer, EnumHand.MAIN_HAND, copy, pos, side, rayTraceEyeHitVec(fakePlayer, 2));
							if (!event.isCanceled() && event.getUseBlock() != Result.DENY) {
								CompatHelper.activateBlock(blockState.getBlock(), world, pos, blockState, fakePlayer, EnumHand.MAIN_HAND, copy, side, hitX, hitY, hitZ);
							}
							break;
					}
					break;

				case USE_ITEM:
					if (StackHelper.isNonNull(copy) && button.value == Button.RIGHT_CLICK)
						fakePlayer.interactionManager.processRightClick(fakePlayer, world, copy, EnumHand.MAIN_HAND);
					break;
				case ENTITY:
					BlockAdvInteractor.Use.rayTraceFlag.set(true);
					Vec3d start = new Vec3d(fakePlayer.posX, fakePlayer.posY + (double) fakePlayer.getEyeHeight(), fakePlayer.posZ);
					Vec3d vec3d1 = fakePlayer.getVectorForRotationPublic(fakePlayer.rotationPitch, fakePlayer.rotationYaw);
					Vec3d end = start.addVector(vec3d1.x * (double) 3, vec3d1.y * (double) 3, vec3d1.z * (double) 3);
					RayTraceResult trace = fakePlayer.world.rayTraceBlocks(start, end, false, false, true);
					BlockAdvInteractor.Use.rayTraceFlag.set(false);

					if (trace != null && trace.hitVec != null) {
						end = trace.hitVec;
					}

					Entity entity = null;
					List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos));
					if (!list.isEmpty()) {
						double d6 = 0.0D;

						Entity fallback = null;

						for (Entity ent : list) {
							if (ent.canBeCollidedWith() && !ent.noClip) {
								if (button.value != Button.LEFT_CLICK || (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb) && !(entity instanceof EntityArrow))) {
									fallback = ent;
									AxisAlignedBB axisalignedbb = ent.getEntityBoundingBox().grow(0.30000001192092896D);
									RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(start, end);

									if (raytraceresult1 != null) {
										double d7 = start.squareDistanceTo(raytraceresult1.hitVec);

										if (d7 < d6 || d6 == 0.0D) {
											entity = ent;
											d6 = d7;
										}
									}
								}
							}
						}
						if (entity == null) {
							entity = fallback;
						}


						if (entity != null)
							switch (button.value) {
								case LEFT_CLICK:
									if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb) && !(entity instanceof EntityArrow)) {
										fakePlayer.updateCooldown();
										fakePlayer.attackTargetEntityWithCurrentItem(entity);
									}
									break;
								case RIGHT_CLICK:
									CompatHelper.interactOn(fakePlayer, entity, EnumHand.MAIN_HAND, copy);
									break;
							}
					}
					break;
			}
		} catch (Exception throwable) {
			fakePlayer.clearInventory();
			fakePlayer.setSneaking(false);
			throw new RuntimeException("Error while interacting with block " + blockState + " with " + copy + ". Method:" + mode.value + "  Button:" + button.value, throwable);
		}

		if (StackHelper.isNonNull(copy)) {
			fakePlayer.getCooldownTracker().setCooldown(copy.getItem(), 0);
			fakePlayer.updateCooldown();
		}

		for (int i = 0; i < 9; i++) {
			ItemStack slot = inventory.getStackInSlot(i);
			if (StackHelper.isNonNull(slot) && StackHelper.isEmpty(slot)) {
				slot = StackHelper.empty();
			}
			inventory.setInventorySlotContents(i, StackHelper.empty());
			contents.setStackInSlot(i, StackHelper.safeCopy(slot));
		}

		for (int i = 9; i < inventory.getSizeInventory(); i++) {
			ItemStack stackInSlot = inventory.getStackInSlot(i);
			if (StackHelper.isNonNull(stackInSlot) && StackHelper.getStacksize(stackInSlot) > 0) {
				stackInSlot = InventoryHelper.insert(contents, stackInSlot, false);
				if (StackHelper.isNonNull(stackInSlot)) {
					extraStacks.addStack(stackInSlot);
				}
			}
		}

		inventory.clear();
		fakePlayer.setSneaking(false);
		fakePlayer.updateAttributes();
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerUser(player);
	}

	enum Select {
		RANDOM_SLOT,
		UPPER_LEFT_SLOT_ONLY,
	}

	enum Button {
		RIGHT_CLICK,
		LEFT_CLICK
	}

	enum Mode {
		GENERIC_CLICK,
		PLACE_BLOCK,
		USE_ITEM_ON_BLOCK,
		ACTIVATE_BLOCK_WITH_ITEM,
		USE_ITEM,
		ENTITY,
	}

	private static class EventHandler {
		static ThreadLocalBoolean cancelClickBlock = new ThreadLocalBoolean(false);

		@SubscribeEvent
		public void appropriateType(LeftClickBlock event) {
			if (cancelClickBlock.get()) {
				event.setUseBlock(Result.DENY);
			}
		}
	}

	private class ContainerUser extends DynamicContainerTile {
		public ContainerUser(EntityPlayer player) {
			super(TileUse.this);
			addTitle("User");
			crop();
			addWidget(upgrades.getSpeedUpgradeSlot(playerInvWidth - 18 - 4, height + 4 + 18 * 2));

			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					addWidget(new WidgetSlotItemHandler(contents, x + y * 3, centerSlotX - 18 + 18 * x, height + 4 + 18 * y));
				}
			}

			addWidget(getRSWidget(playerInvWidth - 18 - 4, height + 4 + 18, redstone_state, pulses));

			crop();

			IWidget w;
			addWidget(w = new WidgetClickMCButtonChoiceEnum<>(4, height, mode));
			crop();
			addWidget(w = new WidgetClickMCButtonChoiceEnum<>(4, height, button));
			crop();
			addWidget(w = new WidgetClickMCButtonChoiceEnum<>(4, height, select));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}

}
