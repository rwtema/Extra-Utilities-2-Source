package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUItemBlock;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.tile.TileCreativeHarvest;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.blockaccess.BlockAccessMimic;
import com.rwtema.extrautils2.utils.datastructures.ThreadLocalBoolean;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class BlockCreativeHarvest extends XUBlock {
	BoxModel base = new BoxModel(new Box(0, 0, 0, 1, 1, 1));

	@Override
	public void registerTextures() {
		Textures.register("creative_harvestable");
		base = new BoxModel(new Box(0, 0, 0, 1, 1, 1).setTexture("creative_harvestable"));
	}


	public IBlockState getMimicState(IBlockAccess world, BlockPos pos, boolean returnNull) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileCreativeHarvest) {
			IBlockState value = ((TileCreativeHarvest) tile).mimicState.value;
			if (value != null) {
				if (value.getBlock() != this)
					return value;
			}
		}
		if (returnNull) return null;
		return getDefaultState();
	}

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		IBlockState mimicState = getMimicState(world, pos, true);
		if (mimicState != null) {
			if (mimicState.getBlock() == this) return base;

			BlockAccessMimic blockAccessMimic = new BlockAccessMimic(world, pos, mimicState);
			AxisAlignedBB bb = mimicState.getBoundingBox(blockAccessMimic, pos);
			if (bb != FULL_BLOCK_AABB) {
				return new BoxModel(new Box((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ));
			}
		}
		return base;
	}


	@Override
	public BoxModel getRenderModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileCreativeHarvest) {
			IBlockState value = ((TileCreativeHarvest) tile).mimicState.value;
			if (value != null && value.getBlock() != this) {
				BoxModel model = new BoxModel();
				model.add(new BoxMimic(world, pos, value));
				return model;
			}
		}
		return super.getRenderModel(world, pos, state);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		return base;
	}

	@Override
	public MutableModel createInventoryMutableModel() {
		return super.createInventoryMutableModel();
	}

	@Override
	public EnumActionResult hasEffect(ItemStack stack) {
		NBTTagCompound tag;
		if (stack.hasTagCompound() && (tag = stack.getTagCompound()) != null && tag.hasKey("creative_block", Constants.NBT.TAG_COMPOUND)) {
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createItemBlockPassThruModel(XUItemBlock item) {
		return new PassthruModelItemBlock(item) {
			ItemOverrideList list = new ItemOverrideList(ImmutableList.of()) {
				ThreadLocalBoolean avoidRecursion = new ThreadLocalBoolean(false);

				@Nonnull
				@Override
				public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
					NBTTagCompound tag;
					if (!stack.hasTagCompound() || (tag = stack.getTagCompound()) == null || !tag.hasKey("display_stack", Constants.NBT.TAG_COMPOUND)) {
						return overrideList.handleItemState(originalModel, stack, world, entity);
					}

					NBTTagCompound display_stack = tag.getCompoundTag("display_stack");
					ItemStack duplicate = StackHelper.deserializeSafe(display_stack);

					if (StackHelper.isNull(duplicate) || avoidRecursion.get()) {
						return overrideList.handleItemState(originalModel, stack, world, entity);
					}

					avoidRecursion.set(true);

					IBakedModel duplicateModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(duplicate);
					IBakedModel finalModel = duplicateModel.getOverrides().handleItemState(duplicateModel, duplicate, world, entity);
					avoidRecursion.set(false);

					return finalModel;
				}
			};

			@Nonnull
			@Override
			public ItemOverrideList getOverrides() {
				return list;
			}
		};
	}


	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileCreativeHarvest();
	}

	@Override
	public void harvestBlock(@Nonnull World worldIn, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, TileEntity te, ItemStack stack) {
		IBlockState mimicState = getMimicState(worldIn, pos, false);
		mimicState.getBlock().harvestBlock(worldIn, player, pos, mimicState, null, stack);
	}

	@Override
	public float getExplosionResistance(Entity exploder) {
		return 100000000;
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosionIn) {
		return false;
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {

	}

	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {

	}

	@Override
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {

	}

	@Override
	public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
		if (player.capabilities.isCreativeMode) {
			return super.removedByPlayer(state, world, pos, player, willHarvest);
		}
		return true;
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		IBlockState mimicState = getMimicState(worldIn, pos, true);
		if (mimicState != null) {
			return mimicState.getBlockHardness(worldIn, pos);
		}
		return super.getBlockHardness(blockState, worldIn, pos);
	}

	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileCreativeHarvest) {
			IBlockState mimicState = ((TileCreativeHarvest) tile).mimicState.value;

			boolean pickupable = ((TileCreativeHarvest) tile).pickupable.value;
			if (mimicState != null) {
				float v;
				try {
					worldIn.setBlockState(pos, mimicState, 0);
					v = mimicState.getPlayerRelativeBlockHardness(player, worldIn, pos);
					worldIn.setBlockState(pos, getDefaultState(), 0);
					((TileCreativeHarvest) Objects.requireNonNull(worldIn.getTileEntity(pos))).mimicState.value = mimicState;
					((TileCreativeHarvest) Objects.requireNonNull(worldIn.getTileEntity(pos))).pickupable.value = pickupable;
				} catch (RuntimeException err) {
					worldIn.setBlockState(pos, getDefaultState(), 0);
					TileEntity tileEntity = worldIn.getTileEntity(pos);
					if (tileEntity instanceof TileCreativeHarvest) {
						((TileCreativeHarvest) tileEntity).mimicState.value = mimicState;
						((TileCreativeHarvest) tileEntity).pickupable.value = pickupable;
					}
					throw err;
				}
				return v;
			}
		}
		return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
		IBlockState mimicState = getMimicState(world, pos, true);
		if (mimicState == null) return super.canHarvestBlock(world, pos, player);
		BlockAccessMimic blockAccessMimic = new BlockAccessMimic();
		blockAccessMimic.setBase(world);
		blockAccessMimic.myPos = pos;
		blockAccessMimic.state = mimicState;
		return net.minecraftforge.common.ForgeHooks.canHarvestBlock(mimicState.getBlock(), player, blockAccessMimic, pos);
	}

	@Override
	public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
		IBlockState mimicState = getMimicState(worldObj, blockPosition, true);
		if (mimicState != null) {
			worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(mimicState));
			return true;
		}
		return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, numberOfParticles);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
		BlockPos pos = target.getBlockPos();

		IBlockState mimicState = getMimicState(worldObj, pos, true);
		if (mimicState != null) {
			addBlockHitEffects(pos, target.sideHit, mimicState, worldObj);
			return true;
		}
		return super.addHitEffects(state, worldObj, target, manager);
	}

	@SideOnly(Side.CLIENT)
	public void addBlockHitEffects(BlockPos pos, EnumFacing side, IBlockState iblockstate, World world) {
		if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			float f = 0.1F;
			AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, pos);
			double d0 = (double) i + world.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
			double d1 = (double) j + world.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
			double d2 = (double) k + world.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

			if (side == EnumFacing.DOWN) {
				d1 = (double) j + axisalignedbb.minY - 0.10000000149011612D;
			}

			if (side == EnumFacing.UP) {
				d1 = (double) j + axisalignedbb.maxY + 0.10000000149011612D;
			}

			if (side == EnumFacing.NORTH) {
				d2 = (double) k + axisalignedbb.minZ - 0.10000000149011612D;
			}

			if (side == EnumFacing.SOUTH) {
				d2 = (double) k + axisalignedbb.maxZ + 0.10000000149011612D;
			}

			if (side == EnumFacing.WEST) {
				d0 = (double) i + axisalignedbb.minX - 0.10000000149011612D;
			}

			if (side == EnumFacing.EAST) {
				d0 = (double) i + axisalignedbb.maxX + 0.10000000149011612D;
			}

			Minecraft.getMinecraft().effectRenderer.addEffect(createDiggingParticle(pos, iblockstate, world, d0, d1, d2, 0.0D, 0.0D, 0.0D).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
		}
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public ParticleDigging createDiggingParticle(BlockPos pos, IBlockState iblockstate, World world, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
		return ((ParticleDigging) new ParticleDigging.Factory().createParticle(0, world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Block.getStateId(iblockstate))).setBlockPos(pos);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		IBlockState mimicState = getMimicState(world, pos, true);
		if (mimicState != null) {
			addBlockDestroyEffects(pos, mimicState, world);
			return true;
		}
		return super.addDestroyEffects(world, pos, manager);
	}

	@SideOnly(Side.CLIENT)
	public void addBlockDestroyEffects(BlockPos pos, IBlockState state, World world) {

		BlockAccessMimic mimicWorld = new BlockAccessMimic();
		mimicWorld.setBase(world);
		mimicWorld.myPos = pos;
		mimicWorld.state = state;

		state = state.getActualState(mimicWorld, pos);

		for (int j = 0; j < 4; ++j) {
			for (int k = 0; k < 4; ++k) {
				for (int l = 0; l < 4; ++l) {
					double d0 = (double) pos.getX() + ((double) j + 0.5D) / 4.0D;
					double d1 = (double) pos.getY() + ((double) k + 0.5D) / 4.0D;
					double d2 = (double) pos.getZ() + ((double) l + 0.5D) / 4.0D;
					Minecraft.getMinecraft().effectRenderer.addEffect(
							createDiggingParticle(pos, state, world, d0, d1, d2, d0 - (double) pos.getX() - 0.5D, d1 - (double) pos.getY() - 0.5D, d2 - (double) pos.getZ() - 0.5D)
					);
				}
			}
		}

	}

	@Nonnull
	@Override
	public String getOverrideStackDisplayName(ItemStack stack, String name) {
		NBTTagCompound tag;
		if (!stack.hasTagCompound() || (tag = stack.getTagCompound()) == null || !tag.hasKey("display_stack", Constants.NBT.TAG_COMPOUND)) {
			return name;
		}

		NBTTagCompound display_stack = tag.getCompoundTag("display_stack");
		ItemStack duplicate = StackHelper.deserializeSafe(display_stack);

		if (StackHelper.isNull(duplicate)) return name;

		return Lang.translateArgs("%s (Infinite)", duplicate.getDisplayName());
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		NBTTagCompound tag;
		if (!stack.hasTagCompound() || (tag = stack.getTagCompound()) == null || !tag.hasKey("display_stack", Constants.NBT.TAG_COMPOUND)) {
			return;
		}

		NBTTagCompound display_stack = tag.getCompoundTag("display_stack");
		ItemStack duplicate = StackHelper.deserializeSafe(display_stack);

		if (StackHelper.isNull(duplicate)) return;

		tooltip.add(Lang.translate("Can be mined infinitely without being destroyed."));
		tooltip.add(Lang.translate("Shift-right-click with an empty hand to pick up."));
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		return true;
	}
}
