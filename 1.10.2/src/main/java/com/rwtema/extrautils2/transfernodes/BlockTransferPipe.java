package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.backend.MultiBlockStateBuilder;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.PassthruModelBlock;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTransferPipe extends XUBlockStatic implements IPipe {
	public static final Map<EnumFacing, IProperty<Boolean>> SIDE_BLOCKED = XUBlockStateCreator.createDirectionBooleanMap("blocked", (name, side) -> PropertyBool.create(name));

	@Override
	public boolean allowOverride() {
		return false;
	}
//	public static final Map<EnumFacing, IMetaProperty<Boolean>> SIDE_PIPE_CONNECTED = XUBlockStateCreator.createDirectionBooleanMap("connected", (name, side) -> {
//		IProperty<Boolean> propertyBool = PropertyBool.create(name);
//		return new IMetaProperty.Wrap<Boolean>(propertyBool) {
//			@Override
//			public Boolean calculateValue(IBlockAccess world, BlockPos pos, IBlockState originalState) {
//				IPipe pipe = TransferHelper.getPipe(world, pos);
//				return shouldConnectPipe(world, pos, side, pipe) || shouldConnectTile(world, pos, side, pipe);
//			}
//		};
//	});

	public static MultiBlockStateBuilder<BlockTransferPipe> stateBuilder = new MultiBlockStateBuilder<>(BlockTransferPipe.class)
			.addWorldProperties(SIDE_BLOCKED.values());
	EnumMap<EnumFacing, HashSet<IBlockState>> canOutputPipeStates;
	IBlockState defaultSimple;
	IBlockState allSides;

	public BlockTransferPipe() {
		super(Material.CLAY, MapColor.STONE);
	}



	public static boolean isUnblocked(@Nonnull IBlockState state, EnumFacing facing) {
		return !state.getValue(SIDE_BLOCKED.get(facing));
	}

	public static boolean shouldConnectPipe(IBlockAccess world, BlockPos pos, EnumFacing facing, IPipe pipe) {
		BlockPos offset = pos.offset(facing);

		IPipe otherPipe = TransferHelper.getPipe(world, offset);
		return otherPipe != null &&
				(pipe.canOutput(world, pos, facing, null) && otherPipe.canInput(world, offset, facing.getOpposite()) ||
						(pipe.canInput(world, pos, facing) && otherPipe.canOutput(world, offset, facing.getOpposite(), null)));
	}

	public static boolean shouldConnectTile(IBlockAccess world, BlockPos pos, EnumFacing facing, IPipe pipe) {
		BlockPos offset = pos.offset(facing);


		if (pipe.canOutputTile(world, pos, facing) &&
				TransferHelper.getPipe(world, offset) == null) {
			for (CapGetter<?> cap : CapGetter.caps) {
				if (pipe.hasCapability(world, pos, facing, cap))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isToolEffective(String type, @Nonnull IBlockState state) {
		return true;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
		return true;
	}

	@Override
	public void setBlockState(XUBlockStateCreator creator) {
		super.setBlockState(creator);

		if (!stateBuilder.initialized) return;

		defaultSimple = allSides = stateBuilder.defaultState;

		for (IProperty<Boolean> propertyBool : SIDE_BLOCKED.values()) {
			allSides = allSides.withProperty(propertyBool, true);
			defaultSimple = defaultSimple.withProperty(propertyBool, false);
		}

		canOutputPipeStates = new EnumMap<>(EnumFacing.class);

		for (EnumFacing a : EnumFacing.values()) {
			HashSet<IBlockState> set = new HashSet<>();
			for (IBlockState genericPipeState : stateBuilder.genericPipeStates) {
				if (isUnblocked(genericPipeState, a.getOpposite())) {
					set.add(genericPipeState);
				}
			}
			canOutputPipeStates.put(a, set);
		}
	}

	@Override
	public boolean onBlockActivatedBase(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!ItemStackHelper.holdingWrench(playerIn)) return false;
		EnumFacing facing = EnumFacing.getFacingFromVector(hitX - 0.5F, hitY - 0.5F, hitZ - 0.5F);
		BlockPos offset = pos.offset(facing);
		if (!TransferHelper.isInputtingPipe(worldIn, offset, facing.getOpposite()) && !TransferHelper.hasValidCapability(worldIn, offset, facing.getOpposite()))
			return false;
		IBlockState newState = state.cycleProperty(SIDE_BLOCKED.get(facing));
		worldIn.setBlockState(pos, newState);
		return true;
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = new BoxModel();
		model.addBoxI(6, 6, 6, 10, 10, 10, "transfernodes/pipes");

		for (EnumFacing facing : EnumFacing.values()) {
			if (isUnblocked(state, facing)) {
				model.addBoxI(6, 0, 6, 10, 6, 10, "transfernodes/pipes").rotateToSide(facing).tint = facing.ordinal();
			}
		}
		return model;
	}

	@Override
	public void registerTextures() {
		super.registerTextures();
		Textures.register("transfernodes/pipes_oneway", "transfernodes/pipes_nozzle");
	}

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		if (state == null) state = world.getBlockState(pos);
		IPipe pipe = TransferHelper.getPipe(world, pos);
		if (pipe == null) return super.getWorldModel(world, pos, state);

		BoxModel model = new BoxModel();
		Box center_box = model.addBoxI(6, 6, 6, 10, 10, 10, "transfernodes/pipes");
		for (EnumFacing facing : EnumFacing.values()) {
			if (shouldConnectPipe(world, pos, facing, pipe)) {
				boolean value = pipe.canOutput(world, pos, facing, null);
				Box box;
				if (value) {
					box = model.addBoxI(6, 0, 6, 10, 6, 10, "transfernodes/pipes");
				} else {
					box = model.addBoxI(6, 0, 6, 10, 6, 10, "transfernodes/pipes_oneway");
				}
				box.setTint(facing.ordinal());
				box.rotateToSide(facing).setInvisible(facing, facing.getOpposite());
				center_box.setInvisible(facing);
			} else if (shouldConnectTile(world, pos, facing, pipe)) {
				model.addBoxI(6, 0, 6, 10, 6, 10, "transfernodes/pipes").rotateToSide(facing).setTint(facing.ordinal()).setInvisible(facing, facing.getOpposite());
				if (pipe.shouldTileConnectionShowNozzle(world, pos, facing))
					model.addBoxI(5, 0, 5, 11, 3, 11, "transfernodes/pipes_nozzle").rotateToSide(facing).setTint(facing.ordinal());
			}
		}
		return model;
	}

	@Override
	public boolean canInput(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		return true;
	}

	@Override
	public boolean canOutput(IBlockAccess world, BlockPos pos, EnumFacing dir, IBuffer buffer) {
		return isUnblocked(world.getBlockState(pos), dir);
	}

	@Override
	public boolean canOutputTile(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		return isUnblocked(world.getBlockState(pos), dir);
	}

	@Override
	public <T> boolean hasCapability(IBlockAccess world, BlockPos pos, EnumFacing side, CapGetter<T> capability) {
		if (!isUnblocked(world.getBlockState(pos), side)) return false;
		TileEntity tileEntity = world.getTileEntity(pos.offset(side));
		return tileEntity != null && capability.hasInterface(tileEntity, side.getOpposite());
	}


	@Override
	public <T> T getCapability(IBlockAccess world, BlockPos pos, EnumFacing side, CapGetter<T> capability) {
		if (!isUnblocked(world.getBlockState(pos), side)) return null;
		TileEntity tileEntity = world.getTileEntity(pos.offset(side));
		if (tileEntity != null) {
			return capability.getInterface(tileEntity, side.getOpposite());
		}
		return null;
	}

	@Override
	public PassthruModelBlock createPassthruModel(IBlockState state, ModelResourceLocation location) {
		return new PassthruModelBlock(this, state, location);
	}

	@Override
	public boolean shouldTileConnectionShowNozzle(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		return true;
	}

	@Override
	public boolean mayHavePriorities() {
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		return XU2Entries.pipe.newStack();
	}

	@Override
	public GrocketPipeFilter.Priority getPriority(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		return GrocketPipeFilter.Priority.NORMAL;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName() {
		return "tile.extrautils2:pipe";
	}
}
