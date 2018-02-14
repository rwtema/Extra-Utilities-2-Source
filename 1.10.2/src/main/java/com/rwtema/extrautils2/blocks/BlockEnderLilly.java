package com.rwtema.extrautils2.blocks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.IMetaProperty;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class BlockEnderLilly extends XUBlockStatic implements IPlantable, IGrowable {
	public static final EnumPlantType ender = EnumPlantType.getPlantType("ender");
	public static final PropertyInteger GROWTH_STATE = PropertyInteger.create("growth", 0, 7);
	public static final HashSet<IBlockState> end_stone_states = Sets.newHashSet(Blocks.END_STONE.getBlockState().getValidStates());
	public static final IMetaProperty<Boolean> READY_TO_GROW = new IMetaProperty.Wrap<Boolean>(PropertyBool.create("ready_to_grow"), false) {
		@Override
		public Boolean calculateValue(IBlockAccess worldIn, BlockPos pos, IBlockState originalState) {
			int i = originalState.getValue(GROWTH_STATE);
			World world;
			if (worldIn instanceof World) {
				world = (World) worldIn;
			} else {
				if (ExtraUtils2.proxy.isClientSide()) {
					world = ExtraUtils2.proxy.getClientWorld();
				} else {
					return false;
				}
			}

			return isReadyToGrow(world, pos, i);
		}
	};
	static float[] size_w = {2 / 16F, 3 / 16F, 4 / 16F, 6 / 16F, 8 / 16F, 8 / 16F, 8 / 16F, 8 / 16F};
	static float[] size_h = {4 / 16F, 7 / 16F, 7 / 16F, 8 / 16F, 11 / 16F, 12 / 16F, 12 / 16F, 14 / 16F};
	public final IBlockState FULLY_GROWN_STATE;

	public BlockEnderLilly() {
		super(Material.PLANTS);
		setTickRandomly(true);
		setHardness(0);
		FULLY_GROWN_STATE = getDefaultState().withProperty(GROWTH_STATE, 7);
	}

	public static boolean isReadyToGrow(World worldIn, BlockPos pos, int metadata) {
		if (metadata >= 7) return false;
		long period = isEndStoneBlock(worldIn, pos) ? 17000L : 57000L;

		long midPeriod = worldIn.getWorldTime() % (2 * period);
		return ((metadata % 2) == 0) == (midPeriod <= period);
	}

	public static boolean isEndStoneBlock(IBlockAccess worldIn, BlockPos pos) {
		return end_stone_states.contains(worldIn.getBlockState(pos.down()));
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		int i = state.getValue(GROWTH_STATE);

		if (!isReadyToGrow(worldIn, pos, i)) return;

		if (rand.nextInt(isEndStoneBlock(worldIn, pos) ? 2 : 40) != 0)
			return;

		worldIn.setBlockState(pos, state.withProperty(GROWTH_STATE, i + 1), 2);
	}

	@Override
	public void grow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		int i = state.getValue(GROWTH_STATE);
		i += MathHelper.getInt(worldIn.rand, 2, 5);
		if (i >= 7) {
			i = 7;
			if (worldIn.getBlockState(pos.down()).getBlock() == Blocks.REDSTONE_ORE) {
				worldIn.setBlockState(pos.down(), Blocks.LIT_REDSTONE_ORE.getDefaultState());
			}
		}

		worldIn.setBlockState(pos, state.withProperty(GROWTH_STATE, i), 2);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addWorldProperties(GROWTH_STATE).addMetaProperty(READY_TO_GROW).build();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		int value = state.getValue(GROWTH_STATE);
		BoxModel boxes = BoxModel.crossBoxModel().setTexture("plants/ender_lilly_stage_" + value);
		for (Box box : boxes) {
			box.noCollide = true;
			box.setLayer(BlockRenderLayer.CUTOUT);
		}

		boxes.overrideBounds = new Box(0.5F - size_w[value], 0, .5F - size_w[value], 0.5F + size_w[value], size_h[value], .5F + size_w[value]);

		return boxes;
	}

	@Override
	public boolean canGrow(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean isClient) {
		return state.getValue(GROWTH_STATE) < 7;
	}

	@Override
	public boolean canUseBonemeal(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		return false;
	}

	@Override
	public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
		if (!validLocation(worldIn, pos)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			return;
		}

		super.neighborChangedBase(state, worldIn, pos, neighborBlock);
	}

	@Override
	public boolean canReplaceBase(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side, ItemStack stack) {
		return validLocation(worldIn, pos) && super.canReplaceBase(worldIn, pos, side, stack);
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
		if (state == FULLY_GROWN_STATE)
			addAdditionalDrops(world, pos, fortune, drops, world instanceof World ? ((World) world).rand : RANDOM);
		return drops;
	}

	protected List<ItemStack> addAdditionalDrops(IBlockAccess world, BlockPos pos, int fortune, List<ItemStack> drops, Random random) {
		drops.add(new ItemStack(Items.ENDER_PEARL, 1));

		if (isEndStoneBlock(world, pos) && random.nextInt(20) <= (1 + fortune))
			drops.add(new ItemStack(this, 1));

		return drops;
	}

	public boolean validLocation(World world, BlockPos pos) {
		Block block = world.getBlockState(pos.down()).getBlock();
		return isEndStoneBlock(world, pos) || block == Blocks.DIRT || block == Blocks.GRASS;
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return ender;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		if (world == null || pos == null) return getDefaultState();
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) return getDefaultState();
		return state;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public MutableModel createInventoryMutableModel() {
		return new PassthruModelItem.ModelLayer(Transforms.itemTransforms);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		super.registerTextures();
		Textures.register("plants/ender_lilly_seed");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInventoryQuads(MutableModel result, ItemStack stack) {
		PassthruModelItem.ModelLayer layer = (PassthruModelItem.ModelLayer) result;
		layer.clear();
		layer.addSprite(Textures.sprites.get("plants/ender_lilly_seed"));
//		layer.addTintedSprite(Textures.sprites.get("plants/ender_lilly_seed"), true, -1);
	}

	@Override
	public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand) {
		if (state.getValue(GROWTH_STATE) != 7 || rand.nextInt(5) != 0) return;


		int ddx = rand.nextInt(2) * 2 - 1;
		int ddz = rand.nextInt(2) * 2 - 1;
		double dx = rand.nextFloat() * 1.0F * ddx;
		double dy = (rand.nextFloat() - 0.5D) * 0.125D;
		double dz = rand.nextFloat() * 1.0F * ddz;
		double x = pos.getX() + 0.5D + 0.25D * ddx;
		double y = pos.getY() + rand.nextFloat();
		double z = pos.getZ() + 0.5D + 0.25D * ddz;

		worldIn.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, dx, dy, dz);
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (state.getValue(GROWTH_STATE) >= 3) {
			if (entityIn instanceof EntityItem) {
				ItemStack item = ((EntityItem) entityIn).getItem();

				if (StackHelper.isNonNull(item) && (item.getItem() == Items.ENDER_PEARL || item.getItem() == Item.getItemFromBlock(this))) {
					return;
				}

				if (worldIn.isRemote) {
					worldIn.spawnParticle(EnumParticleTypes.CRIT, entityIn.posX, entityIn.posY, entityIn.posZ, 0, 0, 0);
				}
			}

			if (entityIn instanceof EntityEnderman) {
				return;
			}

			entityIn.attackEntityFrom(DamageSource.CACTUS, 0.1F);
		}
	}

	@Override
	public boolean onBlockActivatedBase(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (state != FULLY_GROWN_STATE) {
			return false;
		}

		if (worldIn.isRemote)
			return true;

		worldIn.setBlockState(pos, getDefaultState());

		List<ItemStack> drops = addAdditionalDrops(worldIn, pos, EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FORTUNE, playerIn), Lists.newArrayList(), worldIn.rand);

		for (ItemStack drop : drops) {
			spawnAsEntity(worldIn, pos, drop);
		}
		return true;
	}

}
