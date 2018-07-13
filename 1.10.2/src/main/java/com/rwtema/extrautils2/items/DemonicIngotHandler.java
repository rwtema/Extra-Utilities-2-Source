package com.rwtema.extrautils2.items;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

public class DemonicIngotHandler {
	static DataParameter<Object> ITEM = ObfuscationReflectionHelper.getPrivateValue(EntityItem.class, null, "field_184525_c", "ITEM", "field_184533_c", "field_184545_d", "field_176599_b");

	ImmutableSet<Block> netherBlocks = ImmutableSet.of(Blocks.NETHER_BRICK, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_FENCE);
	WeakLinkedSet<EntityItem> goldIngotsServer = new WeakLinkedSet<>();
	WeakLinkedSet<EntityItem> goldIngotsClient = new WeakLinkedSet<>();

	public static ItemStack getRawStack(EntityItem entityItem) {
		Object itemStackOptional = entityItem.getDataManager().get(ITEM);
		if (itemStackOptional instanceof ItemStack) {
			return ((ItemStack) itemStackOptional);
		} else if (itemStackOptional instanceof Optional) {
			return ((Optional<ItemStack>) itemStackOptional).or(Optional.fromNullable(StackHelper.empty())).orNull();
		} else return StackHelper.empty();
	}

	@SubscribeEvent
	public void onJoin(EntityJoinWorldEvent event) {

		Entity entity = event.getEntity();
		if (entity instanceof EntityItem) {
			ItemStack entityItem = getRawStack((EntityItem) entity);
			if (StackHelper.isNull(entityItem) || entityItem.getItem() == Items.GOLD_INGOT) {
				WeakLinkedSet<EntityItem> entityItems;
				if (event.getWorld().isRemote)
					entityItems = goldIngotsClient;
				else
					entityItems = goldIngotsServer;
				entityItems.add((EntityItem) entity);
			}
		}
	}

	@SubscribeEvent
	public void run(TickEvent.ClientTickEvent event) {
		handleIngots(goldIngotsClient);
	}

	@SubscribeEvent
	public void run(TickEvent.ServerTickEvent event) {
		handleIngots(goldIngotsServer);
	}

	@SuppressWarnings("ConstantConditions")
	private void handleIngots(WeakLinkedSet<EntityItem> goldIngots) {
		for (Iterator<EntityItem> iterator = goldIngots.iterator(); iterator.hasNext(); ) {
			EntityItem goldIngotItem = iterator.next();
			if (goldIngotItem.isDead) {
				iterator.remove();
				continue;
			}

			ItemStack rawStack = getRawStack(goldIngotItem);

			if (StackHelper.isNull(rawStack)) {
				continue;
			}

			if (rawStack.getItem() != Items.GOLD_INGOT) {
				iterator.remove();
				continue;
			}

			World world = goldIngotItem.world;
			AxisAlignedBB bb = goldIngotItem.getEntityBoundingBox().grow(0.1, 0.1, 0.1);

			int x1 = MathHelper.floor(bb.minX);
			int x2 = MathHelper.ceil(bb.maxX);
			int y1 = MathHelper.floor(bb.minY);
			int y2 = MathHelper.ceil(bb.maxY);
			int z1 = MathHelper.floor(bb.minZ);
			int z2 = MathHelper.ceil(bb.maxZ);
			BlockPos.PooledMutableBlockPos mutPos = BlockPos.PooledMutableBlockPos.retain();

			boolean found = false;
			mainLoop:
			for (int x = x1; x < x2; ++x) {
				for (int y = y1; y < y2; ++y) {
					for (int z = z1; z < z2; ++z) {
						if (world.getBlockState(mutPos.setPos(x, y, z)).getMaterial() == Material.LAVA) {
							found = true;
							for (EnumFacing dir : EnumFacing.HORIZONTALS) {
								if (!netherBlocks.contains(world.getBlockState(mutPos.offset(dir)).getBlock())) {
									found = false;
									break;
								}
							}
							if (found) {
								break mainLoop;
							}

						}
					}
				}
			}

			if (!found) {
				continue;
			}

			if (!world.isRemote && world instanceof WorldServer) {
				WorldServer worldServer = (WorldServer) world;
//				for (int i = 0; i < 100; i++) {
				worldServer.spawnParticle(EnumParticleTypes.LAVA, false, goldIngotItem.posX, goldIngotItem.posY, goldIngotItem.posZ, 100, 0.0, 0D, 0D, 0.0);
//				}
				goldIngotItem.setDead();
				EntityItem demonicIngotItem = new EntityItem(world, goldIngotItem.posX, goldIngotItem.posY, goldIngotItem.posZ, ItemIngredients.Type.DEMON_INGOT.newStack(StackHelper.getStacksize(rawStack)));
				world.spawnEntity(demonicIngotItem);
				iterator.remove();
			}

		}
	}
}
