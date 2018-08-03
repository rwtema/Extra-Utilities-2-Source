package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.XUItemBlock;
import com.rwtema.extrautils2.backend.entries.Entry;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.rwtema.extrautils2.backend.entries.BlockEntry.registerBlockItemCombo;
import static net.minecraft.block.BlockLeaves.CHECK_DECAY;
import static net.minecraft.block.BlockLeaves.DECAYABLE;
import static net.minecraft.block.BlockLog.LOG_AXIS;
import static net.minecraft.block.BlockSapling.STAGE;


public abstract class XUTree extends Entry<XUTree.TreeBlocks> {
	@Nullable
	private static XUTree curXUTreeIniting = null;
	private final Map<Map<IProperty<?>, Comparable<?>>, TreeModel> models;

	protected XUTree(String name, Map<Map<IProperty<?>, Comparable<?>>, TreeModel> models) {
		super(name);
		this.models = models;
	}

	public static Map<IProperty<?>, Comparable<?>> getStripKey(IBlockState state) {

		return state.getProperties().entrySet().stream()
				.filter(e -> {
					IProperty<?> key = e.getKey();
					return key != LOG_AXIS && key != DECAYABLE && key != CHECK_DECAY && key != STAGE;
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Nullable
	public static IBlockState getEquivalentState(IBlockState state, IBlockState blockState) {
		for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
			if (blockState.getProperties().containsKey(entry.getKey())) {
				//noinspection unchecked,RedundantCast
				blockState = blockState.withProperty((IProperty) entry.getKey(), (Comparable) entry.getValue());
			}
		}
		return blockState;
	}

	@SideOnly(Side.CLIENT)
	private void addSaplingInvQuads(PassthruModelItem.ModelLayer layer, XUBlockState stateFromItemStack) {
		getModel(stateFromItemStack).createSaplingInvModel(layer);
	}

	protected abstract XUBlockStateCreator.Builder getBuilder(XUBlock block);

	protected BoxModel createLogModel(IBlockState state) {
		return getModel(state).createLogModel();
	}

	protected BoxModel createBlankLogModel(IBlockState state) {
		return getModel(state).createBlankLogModel();
	}

	protected TreeModel getModel(IBlockState state) {
		return models.get(getStripKey(state));
	}

	private BoxModel getSaplingWorldModel(IBlockState state) {
		return getModel(state).createSaplingWorldModel();
	}

	private BoxModel createLeavesModel(IBlockState state) {
		return getModel(state).createLeavesModel();
	}

	@Override
	protected TreeBlocks initValue() {
		XUTreeLeaves leaves;
		XUTreeLog log;
		XUTreeSapling sapling;
		XUTreePlanks planks;

		curXUTreeIniting = this;
		leaves = getXuTreeLeaves();
		log = getXuTreeLog();
		sapling = getXuTreeSapling();
		planks = getXuTreePlanks();
		curXUTreeIniting = null;

		return new TreeBlocks(leaves, log, sapling, planks);
	}

	public XUTreePlanks getXuTreePlanks() {
		return new XUTreePlanks();
	}

	public XUTreeSapling getXuTreeSapling() {
		return new XUTreeSapling();
	}

	public XUTreeLog getXuTreeLog() {
		return new XUTreeLog();
	}

	public XUTreeLeaves getXuTreeLeaves() {
		return new XUTreeLeaves();
	}

	@Override
	public void preInitRegister() {
		value.getBlocksAndNames().forEach(
				(n, b) -> {
					b.setBlockName(ExtraUtils2.MODID + ":" + name.toLowerCase(Locale.ENGLISH) + "_" + n);
					registerBlockItemCombo(b, XUItemBlock.class, name + "_" + n);
				}
		);


	}

	@Override
	public void addRecipes() {

		XUBlockState[] logDropStates = value.log.xuBlockState.dropmeta2state;
		for (XUBlockState logDropState : logDropStates) {
			StringBuilder suffix = new StringBuilder();
			IBlockState state = value.leaves.getDefaultState();
			for (Map.Entry<IProperty<?>, Comparable<?>> entry : logDropState.getProperties().entrySet()) {
				if (state.getProperties().containsKey(entry.getKey())) {
					IProperty key = entry.getKey();
					Comparable value = entry.getValue();
					//noinspection unchecked
					state = state.withProperty(key, value);
					//noinspection unchecked
					suffix.append("_").append(key.getName(value).toLowerCase(Locale.ENGLISH));
				}
			}
			CraftingHelper.addShapeless(name.toLowerCase(Locale.ENGLISH) + "_log_to_planks" + suffix,
					new ItemStack(value.planks, 4, value.leaves.xuBlockState.getDropMetaFromState(state)),
					new ItemStack(value.log, 1, value.log.xuBlockState.getDropMetaFromState(logDropState)));
		}

		FurnaceRecipes.instance().addSmelting(value.log.itemBlock, new ItemStack(Items.COAL, 1, 1), 0);
//		CraftingHelper.addShapeless(name.toLowerCase(Locale.ENGLISH) + "_log_to_planks", new ItemStack(value.planks, 4),
	}

	@Override
	public void registerOres() {
		OreDictionary.registerOre("treeSapling", new ItemStack(value.sapling, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("treeLeaves", new ItemStack(value.leaves, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("logWood", new ItemStack(value.log, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("plankWood", new ItemStack(value.planks, 1, OreDictionary.WILDCARD_VALUE));
	}

	public abstract int getHeight(World worldIn, Random rand, IBlockState state, BlockPos pos);

	protected int getLeavesColour(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
		return worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos) : ColorizerFoliage.getFoliageColorBasic();
	}

	protected int getLeavesStackColour(ItemStack stack, int tintIndex) {
		return getLeavesColour(value.leaves.xuBlockState.getStateFromItemStack(stack), null, null, tintIndex);
	}

	public static class TreeBlocks {
		public XUTreeLeaves leaves;
		public XUTreeLog log;
		public XUTreeSapling sapling;
		public XUTreePlanks planks;

		public TreeBlocks(XUTreeLeaves leaves, XUTreeLog log, XUTreeSapling sapling, XUTreePlanks planks) {
			this.leaves = leaves;
			this.log = log;
			this.sapling = sapling;
			this.planks = planks;
		}


		public Map<String, XUBlock> getBlocksAndNames() {
			return ImmutableMap.<String, XUBlock>builder()
					.put("leaves", leaves)
					.put("log", log)
					.put("sapling", sapling)
					.put("planks", planks).build();
		}
	}

	public static class TreeModelTex extends TreeModel {
		final String saplingTex;
		final String logSideTex;
		final String logInsideTex;
		final String planksTex;
		final String leavesTex;

		public TreeModelTex(String prefix) {
			this(prefix + "_sapling", prefix + "_log", prefix + "_log_top", prefix + "_planks", prefix + "_leaves");
		}

		public TreeModelTex(String saplingTex, String logSideTex, String logInsideTex, String planksTex, String leavesTex) {
			this.saplingTex = saplingTex;
			this.logSideTex = logSideTex;
			this.logInsideTex = logInsideTex;
			this.planksTex = planksTex;
			this.leavesTex = leavesTex;
		}

		@Override
		public BoxModel createLogModel() {
			return BoxModel.newStandardBlock(logSideTex).setTextures(EnumFacing.UP, logInsideTex).setTextures(EnumFacing.DOWN, logInsideTex);
		}

		@Override
		public BoxModel createBlankLogModel() {
			return BoxModel.newStandardBlock(logSideTex);
		}

		@Override
		public BoxModel createSaplingWorldModel() {
			BoxModel boxes = BoxModel.crossBoxModel().setLayer(BlockRenderLayer.CUTOUT).setTexture(saplingTex);
			for (Box box : boxes) {
				box.noCollide = true;
			}
			return boxes;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void createSaplingInvModel(MutableModel model) {
			model.clear();
			((PassthruModelItem.ModelLayer) model).addSprite(Textures.sprites.get(saplingTex));
		}

		@Override
		public BoxModel createLeavesModel() {
			BoxModel model = BoxModel.newStandardBlock(leavesTex);
			model.get(0).setTint(0);
			model.get(0).setLayer(BlockRenderLayer.CUTOUT);
			return model;
		}

		@Override
		public BoxModel createPlanksModel() {
			return BoxModel.newStandardBlock(planksTex);
		}
	}

	public static abstract class TreeModel {
		public abstract BoxModel createLogModel();

		public abstract BoxModel createBlankLogModel();

		public abstract BoxModel createSaplingWorldModel();

		@SideOnly(Side.CLIENT)
		public abstract void createSaplingInvModel(MutableModel model);

		public abstract BoxModel createLeavesModel();

		public abstract BoxModel createPlanksModel();
	}

	public static class XUTreeSapling extends XUBlockStatic implements IPlantable, IGrowable {
		protected static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.800000011920929D, 0.8999999761581421D);
		@Nonnull
		XUTree xuTree = Validate.notNull(XUTree.curXUTreeIniting);

		public XUTreeSapling() {
			super(Material.PLANTS);
			this.setTickRandomly(true);
			this.setCreativeTab(CreativeTabs.DECORATIONS);
			this.setSoundType(SoundType.PLANT);
			this.setHardness(0);
		}

		@Override
		public AxisAlignedBB getCollisionBoundingBoxBase(IBlockState state, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos) {
			return NULL_AABB;
		}

		@Override
		public BoxModel getModel(IBlockState state) {
			return xuTree.getSaplingWorldModel(state);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void addInventoryQuads(MutableModel result, ItemStack stack) {
			xuTree.addSaplingInvQuads((PassthruModelItem.ModelLayer) result, xuBlockState.getStateFromItemStack(stack));
		}

		@Override
		@SideOnly(Side.CLIENT)
		public MutableModel createInventoryMutableModel() {
			return new PassthruModelItem.ModelLayer(Transforms.itemTransforms);
		}


		public boolean canPlaceBlockAt(World worldIn, @Nonnull BlockPos pos) {
			IBlockState soil = worldIn.getBlockState(pos.down());
			return super.canPlaceBlockAt(worldIn, pos) && soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this);
		}

		/**
		 * Return true if the block can sustain a Bush
		 */
		protected boolean canSustainBush(IBlockState state) {
			return state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.FARMLAND;
		}

		/**
		 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
		 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
		 * block, etc.
		 */
		public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
			super.neighborChangedBase(state, worldIn, pos, neighborBlock);
			this.checkAndDropBlock(worldIn, pos, state);
		}

		protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
			if (!this.canBlockStay(worldIn, pos, state)) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
			}
		}

		public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
			if (state.getBlock() == this) //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
			{
				IBlockState soil = worldIn.getBlockState(pos.down());
				return soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this);
			}
			return this.canSustainBush(worldIn.getBlockState(pos.down()));
		}

		/**
		 * Used to determine ambient occlusion and culling when rebuilding chunks for render
		 */
		public boolean isOpaqueCube(IBlockState state) {
			return false;
		}

		public boolean isFullCube(IBlockState state) {
			return false;
		}

		@Override
		public EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world, BlockPos pos) {
			return EnumPlantType.Plains;
		}

		@Override
		public IBlockState getPlant(net.minecraft.world.IBlockAccess world, BlockPos pos) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() != this) return getDefaultState();
			return state;
		}

		@Nonnull
		@SideOnly(Side.CLIENT)
		public BlockRenderLayer getBlockLayer() {
			return BlockRenderLayer.CUTOUT;
		}

		@Nonnull
		public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
			return SAPLING_AABB;
		}

		/**
		 * Gets the localized name of this block. Used for the statistics page.
		 */
		@Nonnull
		public String getLocalizedName() {
			return I18n.translateToLocal(this.getUnlocalizedName() + "." + BlockPlanks.EnumType.OAK.getUnlocalizedName() + ".name");
		}

		public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
			this.checkAndDropBlock(worldIn, pos, state);
			if (!worldIn.isRemote) {
				tryGrow(worldIn, pos, state, rand);
			}
		}

		protected void tryGrow(World worldIn, BlockPos pos, IBlockState state, Random rand) {
			if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
				this.grow(worldIn, pos, state, rand);
			}
		}

		public void grow(World worldIn, BlockPos pos, IBlockState state, Random rand) {
			if (state.getValue(STAGE) == 0) {
				worldIn.setBlockState(pos, state.cycleProperty(STAGE), 4);
			} else {
				this.generateTree(worldIn, pos, state, rand);
			}
		}

		public void generateTree(World worldIn, BlockPos pos, IBlockState state, Random rand) {
			if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(worldIn, rand, pos)) return;
			IBlockState blockState = xuTree.value.leaves.getDefaultState().withProperty(DECAYABLE, true);
			IBlockState logState = xuTree.value.log.getDefaultState().withProperty(LOG_AXIS, BlockLog.EnumAxis.Y);
			blockState = getEquivalentState(state, blockState);
			logState = getEquivalentState(state, logState);

			WorldGenerator worldgenerator = new WorldGenTrees(true, xuTree.getHeight(worldIn, rand, state, pos), logState, blockState, false);

			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);

			if (!worldgenerator.generate(worldIn, rand, pos)) {
				worldIn.setBlockState(pos, state, 4);
			}
		}

		public boolean canGrow(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean isClient) {
			return true;
		}

		public boolean canUseBonemeal(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
			return (double) worldIn.rand.nextFloat() < 0.45D;
		}

		public void grow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
			this.grow(worldIn, pos, state, rand);
		}

		@Nonnull
		@Override
		protected XUBlockStateCreator createBlockState() {
			return Validate.notNull(curXUTreeIniting).getBuilder(this).addWorldProperties(STAGE).build();
		}
	}

	public static class XUTreeLog extends XUBlockStatic {
		@Nonnull
		XUTree xuTree = Validate.notNull(XUTree.curXUTreeIniting);

		public XUTreeLog() {
			super(Material.WOOD);
			this.setHardness(2.0F);
			this.setSoundType(SoundType.WOOD);
		}

		@Override
		public BoxModel getModel(IBlockState state) {
			switch (state.getValue(LOG_AXIS)) {
				case X:
					return xuTree.createLogModel(state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y)).rotateToSide(EnumFacing.WEST);
				case Y:
					return xuTree.createLogModel(state);
				case Z:
					return xuTree.createLogModel(state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y)).rotateToSide(EnumFacing.NORTH);
				case NONE:
					return xuTree.createBlankLogModel(state);
			}

			throw new IllegalArgumentException("" + state);
		}

		@Nonnull
		@Override
		protected XUBlockStateCreator createBlockState() {
			return Validate.notNull(curXUTreeIniting).getBuilder(this).addWorldPropertyWithDefault(LOG_AXIS, BlockLog.EnumAxis.Y).build();
		}


		/**
		 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
		 */
		public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {

			if (worldIn.isAreaLoaded(pos.add(-5, -5, -5), pos.add(5, 5, 5))) {
				for (BlockPos blockpos : BlockPos.getAllInBox(pos.add(-4, -4, -4), pos.add(4, 4, 4))) {
					IBlockState iblockstate = worldIn.getBlockState(blockpos);

					if (iblockstate.getBlock().isLeaves(iblockstate, worldIn, blockpos)) {
						iblockstate.getBlock().beginLeavesDecay(iblockstate, worldIn, blockpos);
					}
				}
			}
		}

		/**
		 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
		 * blockstate.
		 */
		@Nonnull
		public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
			switch (rot) {
				case COUNTERCLOCKWISE_90:
				case CLOCKWISE_90:

					switch (state.getValue(LOG_AXIS)) {
						case X:
							return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
						case Z:
							return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
						default:
							return state;
					}

				default:
					return state;
			}
		}

		@Override
		public boolean canSustainLeaves(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos) {
			return true;
		}

		@Override
		public boolean isWood(net.minecraft.world.IBlockAccess world, BlockPos pos) {
			return true;
		}

	}

	public static class XUTreeLeaves extends XUBlockStatic implements IShearable {
		int[] surroundings;
		@Nonnull
		XUTree xuTree = Validate.notNull(XUTree.curXUTreeIniting);

		public XUTreeLeaves() {
			super(Material.LEAVES);
			this.setTickRandomly(true);
			this.setCreativeTab(CreativeTabs.DECORATIONS);
			this.setHardness(0.2F);
			this.setLightOpacity(1);
			this.setSoundType(SoundType.PLANT);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void addItemColors(ItemColors itemColors, BlockColors blockColors) {
			blockColors.registerBlockColorHandler(xuTree::getLeavesColour, this);
			itemColors.registerItemColorHandler(xuTree::getLeavesStackColour, this);
		}

		public boolean isLeavesFancy() {
			return !Blocks.LEAVES.isOpaqueCube(Blocks.LEAVES.getDefaultState());
		}

		@Override
		public BoxModel getModel(IBlockState state) {
			return xuTree.createLeavesModel(state);
		}

		@Nonnull
		@Override
		protected XUBlockStateCreator createBlockState() {
			return Validate.notNull(curXUTreeIniting).getBuilder(this).addWorldPropertyWithDefault(CHECK_DECAY, false).addWorldPropertyWithDefault(DECAYABLE, false).build();
		}

		/**
		 * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
		 */
		public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
			int k = pos.getX();
			int l = pos.getY();
			int i1 = pos.getZ();

			if (worldIn.isAreaLoaded(new BlockPos(k - 2, l - 2, i1 - 2), new BlockPos(k + 2, l + 2, i1 + 2))) {
				for (int j1 = -1; j1 <= 1; ++j1) {
					for (int k1 = -1; k1 <= 1; ++k1) {
						for (int l1 = -1; l1 <= 1; ++l1) {
							BlockPos blockpos = pos.add(j1, k1, l1);
							IBlockState iblockstate = worldIn.getBlockState(blockpos);

							if (iblockstate.getBlock().isLeaves(iblockstate, worldIn, blockpos)) {
								iblockstate.getBlock().beginLeavesDecay(iblockstate, worldIn, blockpos);
							}
						}
					}
				}
			}
		}

		public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
			if (!worldIn.isRemote) {
				if (state.getValue(CHECK_DECAY) && state.getValue(BlockLeaves.DECAYABLE)) {
					int x = pos.getX();
					int y = pos.getY();
					int z = pos.getZ();

					if (this.surroundings == null) {
						this.surroundings = new int[32768];
					}

					if (worldIn.isAreaLoaded(new BlockPos(x - 5, y - 5, z - 5), new BlockPos(x + 5, y + 5, z + 5))) {
						BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

						for (int dx = -4; dx <= 4; ++dx) {
							for (int dy = -4; dy <= 4; ++dy) {
								for (int dz = -4; dz <= 4; ++dz) {
									IBlockState iblockstate = worldIn.getBlockState(blockpos$mutableblockpos.setPos(x + dx, y + dy, z + dz));
									Block block = iblockstate.getBlock();

									if (!block.canSustainLeaves(iblockstate, worldIn, blockpos$mutableblockpos.setPos(x + dx, y + dy, z + dz))) {
										if (block.isLeaves(iblockstate, worldIn, blockpos$mutableblockpos.setPos(x + dx, y + dy, z + dz))) {
											this.surroundings[(dx + 16) * 1024 + (dy + 16) * 32 + dz + 16] = -2;
										} else {
											this.surroundings[(dx + 16) * 1024 + (dy + 16) * 32 + dz + 16] = -1;
										}
									} else {
										this.surroundings[(dx + 16) * 1024 + (dy + 16) * 32 + dz + 16] = 0;
									}
								}
							}
						}

						for (int level = 1; level <= 4; ++level) {
							for (int dx = -4; dx <= 4; ++dx) {
								for (int dy = -4; dy <= 4; ++dy) {
									for (int dz = -4; dz <= 4; ++dz) {
										if (this.surroundings[(dx + 16) * 1024 + (dy + 16) * 32 + dz + 16] == level - 1) {
											if (this.surroundings[(dx + 16 - 1) * 1024 + (dy + 16) * 32 + dz + 16] == -2) {
												this.surroundings[(dx + 16 - 1) * 1024 + (dy + 16) * 32 + dz + 16] = level;
											}

											if (this.surroundings[(dx + 16 + 1) * 1024 + (dy + 16) * 32 + dz + 16] == -2) {
												this.surroundings[(dx + 16 + 1) * 1024 + (dy + 16) * 32 + dz + 16] = level;
											}

											if (this.surroundings[(dx + 16) * 1024 + (dy + 16 - 1) * 32 + dz + 16] == -2) {
												this.surroundings[(dx + 16) * 1024 + (dy + 16 - 1) * 32 + dz + 16] = level;
											}

											if (this.surroundings[(dx + 16) * 1024 + (dy + 16 + 1) * 32 + dz + 16] == -2) {
												this.surroundings[(dx + 16) * 1024 + (dy + 16 + 1) * 32 + dz + 16] = level;
											}

											if (this.surroundings[(dx + 16) * 1024 + (dy + 16) * 32 + (dz + 16 - 1)] == -2) {
												this.surroundings[(dx + 16) * 1024 + (dy + 16) * 32 + (dz + 16 - 1)] = level;
											}

											if (this.surroundings[(dx + 16) * 1024 + (dy + 16) * 32 + dz + 16 + 1] == -2) {
												this.surroundings[(dx + 16) * 1024 + (dy + 16) * 32 + dz + 16 + 1] = level;
											}
										}
									}
								}
							}
						}
					}

					int l2 = this.surroundings[16912];

					if (l2 >= 0) {
						worldIn.setBlockState(pos, state.withProperty(CHECK_DECAY, Boolean.FALSE), 4);
					} else {
						this.destroy(worldIn, pos);
					}
				}
			}
		}

		private void destroy(World worldIn, BlockPos pos) {
			this.dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
			worldIn.setBlockToAir(pos);
		}

		@SideOnly(Side.CLIENT)
		public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
			if (worldIn.isRainingAt(pos.up()) && !worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos, EnumFacing.UP) && rand.nextInt(15) == 1) {
				double d0 = (double) ((float) pos.getX() + rand.nextFloat());
				double d1 = (double) pos.getY() - 0.05D;
				double d2 = (double) ((float) pos.getZ() + rand.nextFloat());
				worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			}
		}

		/**
		 * Returns the quantity of items to drop on block destruction.
		 */
		public int quantityDropped(Random random) {
			return random.nextInt(20) == 0 ? 1 : 0;
		}

		/**
		 * Get the Item that this Block should drop when harvested.
		 */
		public Item getItemDropped(IBlockState state, Random rand, int fortune) {
			return Item.getItemFromBlock(xuTree.value.sapling);
		}

		@Override
		public int damageDropped(IBlockState state) {
			IBlockState dropState = xuTree.value.sapling.getDefaultState();
			for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
				if (dropState.getProperties().containsKey(entry.getKey())) {
					//noinspection unchecked,RedundantCast
					dropState = dropState.withProperty((IProperty) entry.getKey(), (Comparable) entry.getValue());
				}
			}
			return xuTree.value.sapling.xuBlockState.getDropMetaFromState(dropState);
		}

		protected int getSaplingDropChance(IBlockState state) {
			return 20;
		}

		/**
		 * Used to determine ambient occlusion and culling when rebuilding chunks for render
		 */
		public boolean isOpaqueCube(IBlockState state) {
			return !this.isLeavesFancy();
		}

		@Nonnull
		@SideOnly(Side.CLIENT)
		public BlockRenderLayer getBlockLayer() {
			return this.isLeavesFancy() ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID;
		}

		public boolean causesSuffocation(IBlockState state) {
			return false;
		}

		@Override
		public boolean isShearable(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos) {
			return true;
		}

		@Override
		public List<ItemStack> onSheared(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
			IBlockState blockState = world.getBlockState(pos);
			return Lists.newArrayList(new ItemStack(this, 1, xuBlockState.getDropMetaFromState(blockState)));
		}

		@Override
		public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
			return true;
		}

		@Override
		public void beginLeavesDecay(IBlockState state, World world, BlockPos pos) {
			if (!state.getValue(CHECK_DECAY)) {
				world.setBlockState(pos, state.withProperty(CHECK_DECAY, true), 4);
			}
		}

		@Nonnull
		@Override
		public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
			Random rand = world instanceof World ? ((World) world).rand : new Random();
			List<ItemStack> drops = new ArrayList<>();
			int chance = this.getSaplingDropChance(state);

			if (fortune > 0) {
				chance -= 2 << fortune;
				if (chance < 10) chance = 10;
			}

			if (rand.nextInt(chance) == 0) {
				ItemStack drop = new ItemStack(getItemDropped(state, rand, fortune), 1, damageDropped(state));
				if (!StackHelper.isEmpty(drop))
					drops.add(drop);
			}

			chance = 200;
			if (fortune > 0) {
				chance -= 10 << fortune;
				if (chance < 40) chance = 40;
			}
			return drops;
		}


		@SideOnly(Side.CLIENT)
		public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, EnumFacing side) {
			return !(!this.isLeavesFancy() && blockAccess.getBlockState(pos.offset(side)).getBlock() == this) && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
		}

	}

	public static class XUTreePlanks extends XUBlockStatic {
		XUTree tree = Validate.notNull(XUTree.curXUTreeIniting);


		public XUTreePlanks() {
			super(Material.WOOD);
			this.setHardness(2.0F);
			this.setResistance(5.0F);
			this.setSoundType(SoundType.WOOD);
		}

		@Nonnull
		@Override
		protected XUBlockStateCreator createBlockState() {
			return Validate.notNull(curXUTreeIniting).getBuilder(this).build();
		}

		@Override
		public BoxModel getModel(IBlockState state) {
			return tree.getModel(state).createPlanksModel();
		}
	}
}
