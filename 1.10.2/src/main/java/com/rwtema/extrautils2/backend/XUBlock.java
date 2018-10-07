package com.rwtema.extrautils2.backend;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.achievements.AchievementHelper;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.compatibility.BlockCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.utils.blockaccess.BlockAccessSingle;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings("deprecation")
public abstract class XUBlock extends BlockCompat implements IRegisterItemColors {
	public final static List<XUBlock> blocks = Lists.newArrayList();

	public XUBlockStateCreator xuBlockState;
	public XUItemBlock itemBlock;
	protected ThreadLocal<XUTile> droppingTileEntity = new ThreadLocal<>();
	private boolean hasTile;
	private boolean finishedCreating;

	public XUBlock() {
		this(Material.ROCK);
	}

	public XUBlock(Material materialIn) {
		this(materialIn, materialIn.getMaterialMapColor());
	}

	public XUBlock(Material materialIn, MapColor color) {
		super(materialIn, color);
		finishedCreating = true;
		this.setCreativeTab(ExtraUtils2.creativeTabExtraUtils);
		this.setHardness(1);
		this.setSoundType(SoundType.STONE);
		setBlockState((XUBlockStateCreator) this.blockState);
		ExtraUtils2.proxy.registerBlock(this);
		blocks.add(this);
	}

	public static <T extends Comparable<T>> T getPropertySafe(World world, BlockPos pos, IProperty<T> property, T _default) {
		IBlockState blockState = world.getBlockState(pos);
		if (blockState.getProperties().containsKey(property)) {
			return blockState.getValue(property);
		} else
			return _default;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return super.isSideSolid(base_state, world, pos, side);
	}

	@Override
	public boolean causesDownwardCurrent(IBlockAccess worldIn, @Nonnull BlockPos pos, EnumFacing side) {
		return getWorldModel(worldIn, pos, null).isFullCube();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return finishedCreating && getGenericWorldModel(state).isFullCube();
	}

//	@Override
//	public boolean isVisuallyOpaque() {
//		return super.isVisuallyOpaque();
//	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator(this);
	}

	public void setBlockName(String s) {
		this.setUnlocalizedName(s);
	}

	@Nonnull
	public abstract BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state);

	public BoxModel getGenericWorldModel(IBlockState state) {
		BlockAccessSingle blockAccessSingle = BlockAccessSingle.cache.getUnchecked(state);
		return getWorldModel(blockAccessSingle, BlockAccessSingle.CENTER, state);
	}

	@SideOnly(Side.CLIENT)
	public BoxModel getRenderModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		return getWorldModel(world, pos, state);
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		return super.canRenderInLayer(state, layer);
	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return getGenericWorldModel(state).isFullCube();
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return getGenericWorldModel(state).isFullCube();
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return getGenericWorldModel(state).isFullCube();
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return getGenericWorldModel(state).isFullCube();
	}

	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		if (xuBlockState.mainBlock == this) {
			for (int i = 0; i < xuBlockState.dropmeta2state.length; i++) {
				list.add(new ItemStack(itemIn, 1, i));
			}
		}
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return false;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof XUTile) {
			return ((XUTile) te).getComparatorLevel();
		}
		return 0;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public abstract BoxModel getInventoryModel(@Nullable ItemStack item);

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
		BoxModel worldModel = getRenderModel(world, pos, state);
		((XUBlockState) state).load(worldModel);
		return state;
	}

	@Nonnull
	@Override
	public BlockStateContainer getBlockState() {
		return xuBlockState;
	}

	public void setBlockState(XUBlockStateCreator creator) {
		this.xuBlockState = creator;
		boolean hasTile = false;
		for (IBlockState iBlockState : xuBlockState.getValidStates()) {
			if (hasTileEntity(iBlockState)) {
				hasTile = true;
				break;
			}
		}
		this.hasTile = hasTile;

		if (itemBlock != null) {
			itemBlock.setHasSubtypes(xuBlockState.dropmeta2state.length > 1);
		}

		setDefaultState(creator.defaultState);
	}

	@Override
	public void addCollisionBoxToListBase(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
		BoxModel models = this.getWorldModel(worldIn, pos, state);

		if (models.isEmpty()) return;

		for (Box b : models) {
			if (b.noCollide) continue;

			AxisAlignedBB aabb = new AxisAlignedBB(pos.getX() + b.minX, pos.getY() + b.minY, pos.getZ() + b.minZ, pos.getX() + b.maxX, pos.getY() + b.maxY, pos.getZ() + b.maxZ);

			if (entityBox.intersects(aabb)) {
				collidingBoxes.add(aabb);
			}
		}
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		AxisAlignedBB aabb = getWorldModel(source, pos, state).getAABB(false);
		return aabb != null ? aabb : FULL_BLOCK_AABB;
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
		BoxModel worldModel = this.getWorldModel(worldIn, pos, blockState);
		if (worldModel.overrideBounds != null)
			return super.collisionRayTrace(blockState, worldIn, pos, start, end);

		RayTraceResult result = null;

		for (Box box : worldModel) {
			RayTraceResult r = this.rayTrace(pos, start, end, box.toAABB());
			if (r != null && (result == null || start.distanceTo(r.hitVec) < start.distanceTo(result.hitVec))) {
				result = r;
				result.subHit = box.tint;
			}
		}

		return result;
	}

	@Nonnull
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return xuOnBlockPlacedBase(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
	}


	@Nonnull
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return xuBlockState.getStateFromDropMeta(meta);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return xuBlockState.getDropMetaFromState(state);
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return xuBlockState.getStateFromMeta(meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return xuBlockState.getMetaFromState(state);
	}

	@Override
	protected ItemStack getSilkTouchDrop(@Nonnull IBlockState state) {
		Item item = Item.getItemFromBlock(xuBlockState.mainBlock);

		if (item == StackHelper.nullItem()) return StackHelper.empty();

		if (item.getHasSubtypes()) {
			return new ItemStack(item, 1, xuBlockState.getDropMetaFromState(state));
		} else
			return new ItemStack(item);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(xuBlockState.mainBlock);
	}

	@SideOnly(Side.CLIENT)
	public void registerTextures() {

	}

	public void postTextureRegister() {

	}

	public XUTile getTile(IBlockAccess world, BlockPos pos) {
		if (hasTile) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof XUTile) {
				return (XUTile) tile;
			} else if (tile != null && world.getBlockState(pos).getBlock() == this) {
				tile.invalidate();
			}
		}
		return null;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		XUTile checkTile;
		if (hasTile && (checkTile = getTile(worldIn, pos)) != null) {
			checkTile.onBlockPlacedBy(worldIn, pos, state, placer, stack, this);
		}
	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		if (hasTile) {
			TileEntity te = world.getTileEntity(pos);
			if (te != null) {
				Optional<ItemStack> stack = ((XUTile) te).getPickBlock(player, target);
				if (stack != null)
					return stack.orElse(StackHelper.empty());
			}
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public void harvestBlock(@Nonnull World worldIn, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, TileEntity te, ItemStack stack) {
		if (hasTile && te instanceof XUTile) {
			XUTile xuTile = (XUTile) te;
			if (xuTile.harvestBlock(worldIn, player, pos, this, state))
				return;

			droppingTileEntity.set(xuTile);
			super.harvestBlock(worldIn, player, pos, state, te, stack);
			droppingTileEntity.set(null);
		} else
			super.harvestBlock(worldIn, player, pos, state, te, stack);
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		if (hasTile && hasTileEntity(state)) {
			XUTile xuTile = droppingTileEntity.get();
			if (xuTile == null) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof XUTile) {
					xuTile = (XUTile) te;
				}
			}
			if (xuTile != null) {
				NBTTagCompound saveInfo = xuTile.getSaveInfo();
				if (saveInfo != null) {
					Item item = Item.getItemFromBlock(xuBlockState.mainBlock);
					if (item != null) {
						ItemStack stack = new ItemStack(item, 1, this.damageDropped(state));
						stack.setTagCompound(saveInfo);
						return Lists.newArrayList(stack);
					}
				}
			}
		}

		return super.getDrops(world, pos, state, fortune);
	}

	public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
		XUTile checkTile;
		if (hasTile && hasTileEntity(state) && (checkTile = getTile(worldIn, pos)) != null)
			checkTile.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
	}

	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		XUTile checkTile;
		if (hasTile && (checkTile = getTile(worldIn, pos)) != null)
			checkTile.breakBlock(worldIn, pos, state);
	}

	public void clearCaches() {

	}

	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

	}

	@SideOnly(Side.CLIENT)
	public void addInventoryQuads(MutableModel result, ItemStack stack) {
		BoxModel inventoryModel = getInventoryModel(stack);
		inventoryModel.loadIntoMutable(result, null);
	}

	@SideOnly(Side.CLIENT)
	public MutableModel createInventoryMutableModel() {
		return new MutableModel(Transforms.blockTransforms);
	}

	@Nonnull
	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
		return getWorldModel(world, pos, state).getAABB(pos, false);
	}


	public <T extends Comparable<T>> T getMyPropertySafe(World world, BlockPos pos, IProperty<T> property) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == this) {
			return state.getValue(property);
		} else {
			return getDefaultState().getValue(property);
		}
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		IBlockState newState = state;
		for (IMetaProperty hiddenProperty : xuBlockState.hiddenProperties) {
			newState = newState.withProperty(hiddenProperty, hiddenProperty.calculateValue(worldIn, pos, state));
		}
		return newState;
	}

	@Nonnull
	@Override
	public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
		for (IProperty prop : state.getProperties().keySet()) {
			if (prop.getName().equals("facing") || prop.getName().equals("rotation")) {
				@SuppressWarnings("unchecked")
				IProperty<EnumFacing> propFacing = (IProperty<EnumFacing>) prop;
				EnumFacing value = state.getValue(propFacing);
				EnumFacing facing = rot.rotate(value);
				return state.withProperty(propFacing, facing);
			}
		}
		return state;
	}

	@Override
	public boolean onBlockActivatedBase(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		XUTile checkTile;
		return hasTile && hasTileEntity(state) && (checkTile = getTile(worldIn, pos)) != null && checkTile.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return super.isPassable(worldIn, pos) || getWorldModel(worldIn, pos, null).getPassable();
	}

	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		AchievementHelper.checkForPotentialAwards(playerIn, stack);
	}

	public boolean getHasSubtypes() {
		return xuBlockState.dropmeta2state.length > 1;
	}

	@SideOnly(Side.CLIENT)
	public PassthruModelBlock createPassthruModel(IBlockState state, ModelResourceLocation location) {
		return new PassthruModelBlock(this, state, location);
	}


	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack) {
		return null;
	}

	public EnumRarity getRarity(ItemStack stack) {
		return null;
	}

	public EnumActionResult hasEffect(ItemStack stack) {
		return EnumActionResult.PASS;
	}

	public XUBlockState getStateFromItemStack(@Nullable ItemStack item) {
		if (StackHelper.isNull(item)) return xuBlockState.defaultState;
		return xuBlockState.getStateFromDropMeta(item.getItemDamage());
	}

	public boolean canReplaceBase(World p_176193_1_, BlockPos p_176193_2_, EnumFacing p_176193_3_, ItemStack p_176193_4_) {
		return this.canPlaceBlockOnSide(p_176193_1_, p_176193_2_, p_176193_3_);
	}

	public String getSuffix(ItemStack stack) {
		return xuBlockState.getStateFromItemStack(stack).dropName;
	}

	public boolean allowOverride() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public IBakedModel createItemBlockPassThruModel(XUItemBlock item) {
		return new PassthruModelItemBlock(item);
	}

	@Nonnull
	public String getOverrideStackDisplayName(ItemStack stack, String name) {
		return name;
	}

	@SideOnly(Side.CLIENT)
	public MutableModel recreateNewInstance(MutableModel result) {
		return createInventoryMutableModel();
	}
}
