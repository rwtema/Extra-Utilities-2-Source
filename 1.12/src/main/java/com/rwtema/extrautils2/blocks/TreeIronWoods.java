package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.eventhandlers.DropsHandler;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.XURandom;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetMetadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TreeIronWoods extends XUTree {
	public static final PropertyEnumSimple<TreeType> TREE_TYPE = new PropertyEnumSimple<>(TreeType.class);
	public static final int ENCOURAGABILITY = 15;

	public TreeIronWoods() {
		super("ironwood", ImmutableMap.of(
				ImmutableMap.of(TREE_TYPE, TreeType.RAW),
				new TreeModelTex("tree/iron_wood_raw"),
				ImmutableMap.of(TREE_TYPE, TreeType.BURNT),
				new TreeModelTex("tree/iron_wood_burnt"))
		);
	}

	public IBlockState convert(IBlockState base, XUBlock other) {
		IBlockState result = other.getDefaultState();
		for (Map.Entry<IProperty<?>, Comparable<?>> entry : base.getProperties().entrySet()) {
			IProperty key = entry.getKey();
			Comparable value = entry.getValue();
			if (result.getProperties().containsKey(key)) {
				//noinspection unchecked
				result = result.withProperty(key, value);
			}
		}
		return result;
	}

	@Override
	public void addRecipes() {
		super.addRecipes();

		FurnaceRecipes.instance().addSmelting(value.planks.itemBlock, new ItemStack(Items.IRON_NUGGET, 1), 0);

		XUTreeSapling sapling = value.sapling;
		int metaFromState = sapling.getMetaFromState(sapling.getDefaultState().withProperty(TREE_TYPE, TreeType.RAW));
		DropsHandler.lootDrops.put(LootTableList.CHESTS_NETHER_BRIDGE,
				new LootPool(
						new LootEntry[]{
								new LootEntryItem(
										Item.getItemFromBlock(sapling), 1, 0
										, new LootFunction[]{new SetMetadata(new LootCondition[0], new RandomValueRange(metaFromState, metaFromState))}
										, new LootCondition[]{
										new RandomChance(0.1F)
								}, "ferrousjuniperSapling")
						},
						new LootCondition[0],
						new RandomValueRange(0, 3),
						new RandomValueRange(0, 0),
						"xuLootDropOfEvil"
				)
		);
	}

	@Override
	protected XUBlockStateCreator.Builder getBuilder(XUBlock block) {
		return new XUBlockStateCreator.Builder(block).addDropProperties(TREE_TYPE);
	}

	@Override
	public XUTreePlanks getXuTreePlanks() {
		return new XUTreePlanks();
	}

	@Override
	public XUTreeSapling getXuTreeSapling() {
		return new XUTreeSapling() {
			@Override
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				switch (xuBlockState.getStateFromItemStack(stack).getValue(TREE_TYPE)) {
					case RAW:
						tooltip.add(Lang.translate("A strange and weak sapling."));
						break;
					case BURNT:
						tooltip.add(Lang.translate("It's dead."));
						break;
				}

			}

			@Override
			public void generateTree(World worldIn, BlockPos pos, IBlockState state, Random rand) {
				if (state.getValue(TREE_TYPE) == TreeType.BURNT) return;
				super.generateTree(worldIn, pos, state, rand);
			}

			@Override
			protected void tryGrow(World worldIn, BlockPos pos, IBlockState state, Random rand) {
				if (state.getValue(TREE_TYPE) == TreeType.BURNT) return;
				super.tryGrow(worldIn, pos, state, rand);
			}

			@Override
			public void grow(World worldIn, BlockPos pos, IBlockState state, Random rand) {
				if (state.getValue(TREE_TYPE) == TreeType.BURNT) return;
				super.grow(worldIn, pos, state, rand);
			}

			@Override
			public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
				if (state.getValue(TREE_TYPE) == TreeType.BURNT) {
					this.dropBlockAsItem(worldIn, pos, state, 0);
					worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
				} else {
					super.updateTick(worldIn, pos, state, rand);
				}
			}

			@Override
			public void grow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
				if (state.getValue(TREE_TYPE) == TreeType.BURNT) return;
				super.grow(worldIn, rand, pos, state);
			}

			@Override
			public boolean canUseBonemeal(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
				return state.getValue(TREE_TYPE) != TreeType.BURNT && super.canUseBonemeal(worldIn, rand, pos, state);
			}

			@Override
			public boolean canGrow(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean isClient) {
				return state.getValue(TREE_TYPE) != TreeType.BURNT && super.canGrow(worldIn, pos, state, isClient);
			}
		};
	}

	@Override
	public XUTreeLog getXuTreeLog() {
		return new XUTreeLog() {
			{
				setTickRandomly(true);
				setHardness(0.5F);
			}

			@Override
			public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
				super.updateTick(worldIn, pos, state, rand);
				performUpdate(worldIn, pos, state, rand);
			}

			@Nonnull
			@Override
			public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
				if (state.getValue(TREE_TYPE) == TreeType.RAW) {
					Random r = world instanceof World ? ((World) world).rand : XURandom.rand;
					int i = r.nextInt(100);
					if (i == 0) {
						return super.getDrops(world, pos, state, fortune);
					} else {
						return value.planks.getDrops(world, pos, value.planks.getDefaultState().withProperty(TREE_TYPE, TreeType.RAW), fortune);
					}
				} else {
					return super.getDrops(world, pos, state, fortune);
				}
			}

			@Override
			public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
				return 0;
			}

			@Override
			public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
				if (neighborBlock == Blocks.FIRE) {

				}
			}

			@Override
			public boolean isFlammable(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
				return world.getBlockState(pos).getValue(TREE_TYPE) == TreeType.RAW;
			}

			@Override
			public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
				return ENCOURAGABILITY;
			}

			@Override
			public boolean isFireSource(@Nonnull World world, BlockPos pos, EnumFacing side) {
				return world.getBlockState(pos).getValue(TREE_TYPE) == TreeType.RAW;
			}
		};
	}

	public void addIronInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {

	}

	@Override
	public XUTreeLeaves getXuTreeLeaves() {
		return new XUTreeLeaves() {
			{
				setTickRandomly(true);
			}

			@Override
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				addIronInformation(stack, playerIn, tooltip, advanced);
			}

			@Override
			protected ItemStack getAppleDrop(IBlockAccess world, BlockPos pos, IBlockState state, Random rand, int fortune, int chance) {
				if (state.getValue(TREE_TYPE) == TreeType.BURNT) {
					return new ItemStack(Items.BLAZE_POWDER);
				}
				return super.getAppleDrop(world, pos, state, rand, fortune, chance);
			}

			@Override
			public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
				super.updateTick(worldIn, pos, state, rand);
				performUpdate(worldIn, pos, state, rand);
			}


			@Override
			public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
				IBlockState blockState = world.getBlockState(pos);
				return blockState.getValue(TREE_TYPE) == TreeType.BURNT ? 0 : 0;
			}

			@Override
			public boolean isFlammable(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
				IBlockState blockState = world.getBlockState(pos);
				return true;
			}

			@Override
			public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
				IBlockState blockState = world.getBlockState(pos);
				return blockState.getValue(TREE_TYPE) == TreeType.BURNT ? 0 : ENCOURAGABILITY;
			}

			@Override
			public boolean isFireSource(@Nonnull World world, BlockPos pos, EnumFacing side) {
				return true;
			}

			public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
				IBlockState blockState = worldIn.getBlockState(pos);
				if (blockState.getValue(TREE_TYPE) == TreeType.BURNT)
					if (!entityIn.isImmuneToFire() && entityIn instanceof EntityLivingBase && !EnchantmentHelper.hasFrostWalkerEnchantment((EntityLivingBase) entityIn)) {
						entityIn.setFire(1);
						entityIn.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F);
					}

				super.onEntityWalk(worldIn, pos, entityIn);
			}

		};
	}

	@Override
	public int getHeight(World worldIn, Random rand, IBlockState state, BlockPos pos) {
		return 5;
	}

	@Override
	protected int getLeavesColour(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
		if (state.getValue(TREE_TYPE) == TreeType.BURNT) {
			return 0xffffffff;
		}
		return super.getLeavesColour(state, worldIn, pos, tintIndex);
	}

	private void performUpdate(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (state.getValue(TREE_TYPE) == TreeType.RAW) {
			for (EnumFacing facing : EnumFacing.values()) {
				IBlockState fireState = worldIn.getBlockState(pos.offset(facing));
				if (fireState.getBlock() == Blocks.FIRE
						|| fireState.getMaterial() == Material.FIRE
						|| (fireState.getBlock() == value.leaves && fireState.getValue(TREE_TYPE) == TreeType.BURNT)
				) {
					for (EnumFacing enumFacing : EnumFacing.values()) {
						BlockPos offset = pos.offset(enumFacing);
						IBlockState blockState = worldIn.getBlockState(offset);
						if (blockState.getBlock() == Blocks.FIRE || blockState.getBlock().isAir(blockState, worldIn, offset)) {
							worldIn.setBlockState(offset, Blocks.FIRE.getDefaultState().withProperty(BlockFire.AGE, 0), 3);
						} else if (blockState.getProperties().containsKey(TREE_TYPE)) {
							for (EnumFacing enumFacing2 : EnumFacing.values()) {
								if (rand.nextBoolean()) continue;
								BlockPos offset2 = pos.offset(enumFacing2);
								IBlockState blockState2 = worldIn.getBlockState(offset2);
								if (blockState2.getBlock() == Blocks.FIRE || blockState2.getBlock().isAir(blockState2, worldIn, offset2) || blockState2.getBlock().isReplaceable(worldIn, offset2) ) {
									worldIn.setBlockState(offset2, Blocks.FIRE.getDefaultState().withProperty(BlockFire.AGE, 0), 3);
								}
							}
						}
					}

					worldIn.setBlockState(pos, state.withProperty(TREE_TYPE, TreeType.BURNT));

					for (EnumFacing enumFacing : EnumFacing.values()) {
						if (rand.nextBoolean()) continue;
						BlockPos offset = pos.offset(enumFacing);
						IBlockState offsetBlockState = worldIn.getBlockState(offset);
						if (offsetBlockState.getProperties().containsKey(TREE_TYPE) && offsetBlockState.getValue(TREE_TYPE) == TreeType.RAW) {
							worldIn.setBlockState(offset, offsetBlockState.withProperty(TREE_TYPE, TreeType.BURNT));
						}
					}
					return;
				}
			}
		}
	}


	public enum TreeType {
		RAW,
		BURNT
	}
}
