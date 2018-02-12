package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemWateringCan extends XUItemFlat {

	public ItemWateringCan() {
		this.setMaxDamage(1000);
		this.setMaxStackSize(1);
	}

	@Override
	public void getSubItemsBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		subItems.add(new ItemStack(itemIn, 1, getMaxDamage()));
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
		return 72000;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseBase(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote && !PlayerHelper.isPlayerReal(playerIn)) {
			int i = stack.getItemDamage();

			if (worldIn.getBlockState(pos).getMaterial() == Material.WATER) {
				i -= 20;
				if (i < 0) i = 0;
				stack.setItemDamage(i);
			} else {
				if (i != stack.getMaxDamage()) {
					waterLocation(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, EnumFacing.DOWN, stack, playerIn, 1, 4);
					if (i < stack.getMaxDamage()) {
						stack.setItemDamage(Math.min(i + 20, stack.getMaxDamage()));
					}
				}
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClickBase(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {


		if (!worldIn.isRemote && !PlayerHelper.isPlayerReal(playerIn)) { //FakePlayer
			return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);


		} else {
			RayTraceResult movingobjectposition = this.rayTrace(worldIn, playerIn, true);

			//noinspection ConstantConditions
			if (movingobjectposition == null)
				return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);

			if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos pos = movingobjectposition.getBlockPos();

				if (worldIn.getBlockState(pos).getMaterial() == Material.WATER) {
					playerIn.setActiveHand(hand);
					return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
				}
			}

			if (itemStackIn.getItemDamage() == getMaxDamage(itemStackIn) && !playerIn.capabilities.isCreativeMode)
				return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);

			playerIn.setActiveHand(hand);
			return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem();
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
		if (!(entity instanceof EntityPlayer)) {
			entity.stopActiveHand();
			return;
		}
		int i = stack.getItemDamage();
		World worldObj = entity.world;

//		if ((worldObj.getTotalWorldTime() % 5) != 0) return;

		EntityPlayer player = (EntityPlayer) entity;
		RayTraceResult movingobjectposition = this.rayTrace(worldObj, player, true);
		if (movingobjectposition == null) return;
		//		if (i > 0) {
		if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = movingobjectposition.getBlockPos();

			if (worldObj.getBlockState(pos).getMaterial() == Material.WATER) {
				if (!worldObj.isRemote && !player.capabilities.isCreativeMode) {
					i -= 5;
					if (i < 0) i = 0;
					stack.setItemDamage(i - 1);
				}
				return;
			}
		}
//		}

		if (i == stack.getMaxDamage()) {
			player.stopActiveHand();
			return;
		}

		waterLocation(worldObj,
				movingobjectposition.hitVec.x,
				movingobjectposition.hitVec.y,
				movingobjectposition.hitVec.z, movingobjectposition.sideHit, stack, player,
				player.capabilities.isCreativeMode ? 3 : 1, (1.0 / (player.capabilities.isCreativeMode ? 3 : 1))
		);

		if (!player.capabilities.isCreativeMode) {
			if (i < stack.getMaxDamage()) {
				stack.setItemDamage(i + 1);
			}
		}
	}

	private void waterLocation(World worldObj, double hitX, double hitY, double hitZ, EnumFacing side, ItemStack stack, EntityPlayer play, int range, double timer_modifier) {
		List<EntityEnderman> enderman = worldObj.getEntitiesWithinAABB(EntityEnderman.class,
				new AxisAlignedBB(hitX - range, hitY - range, hitZ - range, hitX + range, hitY + 6, hitZ + range));

		for (EntityEnderman anEnderman : enderman) {
			anEnderman.attackEntityFrom(DamageSource.DROWN, 1);
		}

//		boolean cheat = stack.getItemDamage() == 4 && (XUHelper.isThisPlayerACheatyBastardOfCheatBastardness(play) || LogHelper.isDeObf || XUHelper.isPlayerFake(play));
		boolean cheat = false;

		if (worldObj.isRemote) {
			double dx = side.getFrontOffsetX(), dy = side.getFrontOffsetY(), dz = side.getFrontOffsetZ();
			double x2 = hitX + dx * 0.1, y2 = hitY + dy * 0.1, z2 = hitZ + dz * 0.1;

			for (int i = 0; i < (4 * range); i++) {
				worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, x2 + worldObj.rand.nextGaussian() * 0.6 * range, y2, z2 + worldObj.rand.nextGaussian() * 0.6 * range, 0, 0, 0);
			}
		} else {
			List<Entity> ents = worldObj.getEntitiesWithinAABB(Entity.class,
					new AxisAlignedBB(hitX - range, hitY - range, hitZ - range, hitX + range, hitY + range + 6, hitZ + range));

			for (Entity ent : ents) {
				if (ent.isBurning()) {
					float p = 0.01F;

					if (ent instanceof EntityPlayer) {
						p = 0.333F;
					}

					ent.extinguish();

					if (worldObj.rand.nextDouble() < p) {
						if (stack.getItemDamage() < 3)
							stack.setItemDamage(1);
						if (play != null)
							play.stopActiveHand();
						return;
					}
				}
			}

			int blockX = (int) Math.floor(hitX);
			int blockY = (int) Math.floor(hitY);
			int blockZ = (int) Math.floor(hitZ);

			for (int x = blockX - range; x <= blockX + range; x++) {
				for (int y = blockY - range; y <= blockY + range; y++)
					for (int z = blockZ - range; z <= blockZ + range; z++) {
						BlockPos pos = new BlockPos(x, y, z);
						IBlockState state = worldObj.getBlockState(pos);
						Block id = state.getBlock();

						if (!worldObj.isAirBlock(pos)) {
							if (id == Blocks.FIRE) {
								worldObj.setBlockToAir(pos);
							}


							int timer = -1;
							if (id == Blocks.FARMLAND) {
								worldObj.setBlockState(pos, id.getDefaultState().withProperty(BlockFarmland.MOISTURE, 7), 2);
							} else if (id == Blocks.GRASS) {
								timer = 20;

								if (!cheat && worldObj.rand.nextInt(9 * 5 * 5 * 20) == 0) {
									if (worldObj.isAirBlock(pos.up())) {
//										if (flowers.size() > 0 && worldObj.rand.nextInt(5) == 0) {
//											ItemStack flower = flowers.get(worldObj.rand.nextInt(flowers.size()));
//
//											if (flower.getItem() instanceof ItemBlock) {
//												if (play != null)
//													((ItemBlock) flower.getItem()).placeBlockAt(flower, play, worldObj, x, y + 1, z, 1, 0.5F, 1, 0.5F,
//															flower.getItem().getMetadata(flower.getItemDamage()));
//											}
//										} else {
//											worldObj.getBiomeGenForCoords(x, z).plantFlower(worldObj, rand, x, y + 1, z);
//										}
									}
								}
							} else if (id == Blocks.MYCELIUM) {
								timer = 20;
							} else if (id == Blocks.WHEAT) {
								timer = 40;
							} else if (id instanceof BlockSapling) {
								timer = 50;
							} else if (id instanceof IPlantable || id instanceof IGrowable) {
								timer = 40;
							} else if (state.getMaterial() == Material.GRASS || state.getMaterial() == Material.GROUND) {
								timer = 20;
							}

							timer *= timer_modifier;

							if (timer > 0) {
								if (id.getTickRandomly()) {
									worldObj.scheduleUpdate(pos, id, worldObj.rand.nextInt(timer));
								}
							}
						}
					}
			}

//			if (cheat)
//				for (int i = 0; i < 100; i++)
//					for (int x = blockX - range; x <= blockX + range; x++)
//						for (int y = blockY - range; y <= blockY + range; y++)
//							for (int z = blockZ - range; z <= blockZ + range; z++) {
//								Block block = worldObj.getBlock(x, y, z);
//
//								block.updateTick(worldObj, x, y, z, worldObj.rand);
//
//								TileEntity tile = worldObj.getTileEntity(x, y, z);
//								if (tile != null && tile.canUpdate() && !tile.isInvalid()) {
//									tile.updateEntity();
//								}
//							}
		}
	}

	@Nonnull
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.NONE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register("watering_can");
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		return "watering_can";
	}

}
