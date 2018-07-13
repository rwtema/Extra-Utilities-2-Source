package com.rwtema.extrautils2.tile;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.IMetaProperty;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.entries.BlockEntry;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.IItemFluidHandlerCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.utils.helpers.NullHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.*;

public abstract class XUTile extends TileEntity {

	XUBlock xuBlock;
	XUBlockState state;

	@Nullable
	private HashMap<String, INBTSerializable> nbtHandlers;

	public static boolean isLoaded(TileEntity tile) {
		World world;

		if (!(tile.isInvalid() || (world = tile.getWorld()) == null || tile.getPos() == null)) {
			if (DimensionManager.getWorld(world.provider.getDimension()) != world) {
				return false;
			}

			if (world.isBlockLoaded(tile.getPos())) {
				Chunk chunk = world.getChunkFromBlockCoords(tile.getPos());
				if (chunk == null) return false;
				if (chunk.getTileEntity(tile.getPos(), Chunk.EnumCreateEntityType.CHECK) == tile) return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T extends TileEntity> List<T> searchAABBForTiles(World world, AxisAlignedBB area, Class<T> tileClazz, boolean firstOnly, List<T> list) {
		int x0 = ((int) Math.floor(area.minX) >> 4);
		int x1 = ((int) Math.ceil(area.maxX) >> 4);
		int z0 = ((int) Math.floor(area.minZ) >> 4);
		int z1 = ((int) Math.ceil(area.maxZ) >> 4);

		if (list == null) list = Lists.newArrayList();

		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				Chunk chunk = world.getChunkFromChunkCoords(x, z);
				for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
					BlockPos pos = entry.getKey();
					if (tileClazz == entry.getValue().getClass() && area.contains(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5))) {
						list.add((T) entry.getValue());
						if (firstOnly) return list;
					}
				}
			}
		}

		return list;
	}

	public static EnumFacing getSafeFacing(int i) {
		if (i == 6) return null;
		return EnumFacing.values()[i];
	}

	public static int getSafeOrdinal(@Nullable EnumFacing facing) {
		return facing == null ? 6 : facing.ordinal();
	}

	@Override
	public final boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
		if (oldState.getBlock() != newState.getBlock())
			return true;
		if (oldState == newState) return false;

		Collection<IProperty<?>> newStatePropertyNames = newState.getPropertyKeys();
		for (IProperty<?> prop : oldState.getPropertyKeys()) {
			if (prop instanceof IMetaProperty)
				continue;
			if (!newStatePropertyNames.contains(prop))
				return true;
			if (!prop.getName().equals("facing") && !prop.getName().equals("rotation")) {
				if (!oldState.getValue(prop).equals(newState.getValue(prop))) {
					return true;
				}
			}
		}
		return false;
	}

	protected <T extends INBTSerializable> T registerNBT(String key, T t) {
		if (nbtHandlers == null) nbtHandlers = new HashMap<>();
		nbtHandlers.put(key, t);
		return t;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (nbtHandlers != null)
			for (Map.Entry<String, INBTSerializable> entry : nbtHandlers.entrySet()) {
				NBTBase tag = compound.getTag(entry.getKey());
				if (tag != null) {
					entry.getValue().deserializeNBT(tag);
				}
			}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (nbtHandlers != null)
			for (Map.Entry<String, INBTSerializable> entry : nbtHandlers.entrySet()) {
				String key = entry.getKey();
				NBTBase value = entry.getValue().serializeNBT();
				compound.setTag(key, value);
			}
		return compound;
	}

	public boolean isLoaded() {
		return isLoaded(this);
	}

	@OverridingMethodsMustInvokeSuper
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		if (StackHelper.isNonNull(stack) && stack.hasTagCompound()) {
			NBTTagCompound tagCompound = Validate.notNull(stack.getTagCompound());
			loadSaveInfo(tagCompound);
		}
	}

	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {

	}

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		Iterable<ItemStack> itemHandler = getDropHandler();
		if (itemHandler != null) {
			InventoryHelper.dropAll(worldIn, pos, itemHandler);
		}
	}

	protected Iterable<ItemStack> getDropHandler() {
		IItemHandler itemHandler = getItemHandler(null);
		if (itemHandler == null)
			return null;
		return InventoryHelper.getItemHandlerIterator(itemHandler);
	}

	public boolean harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, XUBlock xuBlock, IBlockState state) {
		return false;
	}

	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		xuBlock = null;
		state = null;
	}

	public XUBlockState getBlockState() {
		if (state == null) {
			IBlockState blockState = world.getBlockState(pos);
			XUBlock xuBlock = BlockEntry.tileToBlockMap.get(this.getClass());
			Set<Block> xuBlocks = BlockEntry.tileToBlocksMap.get(this.getClass());
			if (blockState instanceof XUBlockState && xuBlocks.contains(blockState.getBlock())) {
				state = (XUBlockState) blockState;
			} else {
				if (xuBlock != null) {
					state = (XUBlockState) xuBlock.getDefaultState();
					world.notifyBlockUpdate(pos, state, state, 0);
				}
				invalidate();
			}
		}
		return state;
	}

	public XUBlock getXUBlock() {
		if (xuBlock == null) {
			xuBlock = (XUBlock) getBlockState().getBlock();
		}
		return xuBlock;
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (handleFluids(worldIn, playerIn, hand, heldItem, side))
			return true;

		if (this instanceof IDynamicHandler) {
			if (!worldIn.isRemote) {
				openGUI(playerIn);
			}
			return true;
		}
		return false;
	}

	public boolean handleFluids(World worldIn, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side) {
		IFluidHandler handler = getFluidHandler(side);
		if (handler == null) {
			return false;
		}
		if (FluidUtil.getFluidHandler(heldItem) != null) {
			if (worldIn.isRemote) return true;
			if (attemptDrain(playerIn, hand, heldItem, handler) || attemptFill(playerIn, hand, heldItem, handler))
				return true;
		}
		return false;

	}

	public boolean attemptFill(EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, IFluidHandler handler) {
		ItemStack copy = heldItem.copy();
		StackHelper.setStackSize(copy, 1);
		IItemFluidHandlerCompat container = IItemFluidHandlerCompat.getFluidHandler(copy);
		if (container == null) return false;

		FluidStack drain = container.drain(1000, false);
		if (drain == null) return false;
		int fill = handler.fill(drain, false);
		if (fill == 0) return false;

		copy = heldItem.splitStack(1);
		container = IItemFluidHandlerCompat.getFluidHandler(copy);
		if (container != null) {
			handler.fill(container.drain(fill, true), true);
			copy = container.getModifiedStack();
		}
		if (StackHelper.getStacksize(copy) != 0) {
			if (StackHelper.isEmpty(heldItem)) {
				heldItem = copy;
				playerIn.setHeldItem(hand, heldItem);
			} else if (ItemHandlerHelper.canItemStacksStack(copy, heldItem)) {
				StackHelper.increase(heldItem);
				playerIn.setHeldItem(hand, heldItem);
			} else {
				playerIn.inventory.addItemStackToInventory(copy);
			}
		}
		return true;
	}

	public boolean attemptDrain(EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, IFluidHandler handler) {
		ItemStack copy = heldItem.copy();
		StackHelper.setStackSize(copy, 1);
		IItemFluidHandlerCompat container = IItemFluidHandlerCompat.getFluidHandler(copy);
		if (container == null) return false;

		FluidStack toDrain = handler.drain(1000, false);
		if (toDrain == null) return false;
		int fill = container.fill(toDrain, false);
		if (fill == 0) return false;

		copy = heldItem.splitStack(1);
		container = IItemFluidHandlerCompat.getFluidHandler(copy);
		if (container != null) {
			container.fill(handler.drain(fill, true), true);
			copy = container.getModifiedStack();
		}
		if (StackHelper.getStacksize(copy) != 0) {
			if (StackHelper.isEmpty(heldItem)) {
				heldItem = copy;
				playerIn.setHeldItem(hand, heldItem);
			} else if (ItemHandlerHelper.canItemStacksStack(copy, heldItem)) {
				StackHelper.increase(heldItem);
				playerIn.setHeldItem(hand, heldItem);
			} else {
				playerIn.inventory.addItemStackToInventory(copy);
			}
		}
		return true;
	}


	public void openGUI(EntityPlayer player) {
		openGUI(player, 0);
	}

	public void openGUI(EntityPlayer player, int modGuiId) {
		player.openGui(ExtraUtils2.instance, modGuiId, world, pos.getX(), pos.getY(), pos.getZ());
	}

	public IItemHandler getItemHandler(EnumFacing facing) {
		return null;
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
			IItemHandler handler = getItemHandler(facing);
			if (handler != null) return (T) handler;
		}
		if (CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY == capability) {
			IFluidHandler handler = getFluidHandler(facing);
			if (handler != null) return (T) handler;
		}
		if (CapabilityEnergy.ENERGY == capability) {
			IEnergyStorage handler = getEnergyHandler(facing);
			if (handler != null) return (T) handler;
		}
		return super.getCapability(capability, facing);
	}

	@Nullable
	public IFluidHandler getFluidHandler(EnumFacing facing) {
		return null;
	}

	@Nullable
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return null;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return getCapability(capability, facing) != null;
	}

	public void addToDescriptionPacket(XUPacketBuffer packet) {

	}

	public void handleDescriptionPacket(XUPacketBuffer packet) {

	}

	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, -1, getUpdateTag());
	}

	@Override
	public final void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		super.readFromNBT(tag);
		if (tag.hasKey("a", Constants.NBT.TAG_BYTE_ARRAY)) {
			this.handleDescriptionPacket(new XUPacketBuffer(Unpooled.wrappedBuffer(tag.getByteArray("a"))));
		}
	}

	@Nonnull
	@Override
	public final NBTTagCompound getUpdateTag() {
		ByteBuf buffer = Unpooled.buffer();
		addToDescriptionPacket(new XUPacketBuffer(buffer));
		int readableBytes = buffer.readableBytes();
		if (readableBytes == 0)
			return super.getUpdateTag();
		byte[] b = new byte[readableBytes];
		buffer.readBytes(b);
		NBTTagCompound tags = super.getUpdateTag();
		tags.setByteArray("a", b);
		return tags;
	}

	@Override
	public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbtCompound = pkt.getNbtCompound();
		handleUpdateTag(nbtCompound);
	}

	@SideOnly(Side.CLIENT)
	public void renderBakedModel(IBlockAccess world, IVertexBuffer renderer, BlockRendererDispatcher blockRenderer, IBakedModel model) {
		blockRenderer.getBlockModelRenderer().renderModel(world, model, getBlockState(), getPos(), CompatClientHelper.unwrap(renderer), false);
	}

	public void markForUpdate() {
		if (world == null || pos == null) return;
		XUBlockState state = getBlockState();
		world.notifyBlockUpdate(pos, state, state, 0);
	}

	public Optional<ItemStack> getPickBlock(EntityPlayer player, @Nullable RayTraceResult target) {
		NBTTagCompound saveInfo = getSaveInfo();
		if (saveInfo != null) {
			Item item = Item.getItemFromBlock(getXUBlock().xuBlockState.mainBlock);
			if (item != null) {
				ItemStack stack = new ItemStack(item, 1, getXUBlock().damageDropped(state));
				stack.setTagCompound(saveInfo);
				return Optional.of(stack);
			}
		}
		return null;
	}


	public boolean hasComparatorLevels() {
		return false;
	}

	public int getComparatorLevel() {
		throw new IllegalStateException();
	}

	public void markDirty() {
		super.markDirty();
	}

	@Nullable
	public NBTTagCompound getSaveInfo() {
		return null;
	}

	public void loadSaveInfo(@Nonnull NBTTagCompound tag) {

	}

	public NBTTagCompound saveRegisteredNBT(NBTTagCompound tag, String... data) {
		if (nbtHandlers == null) throw new IllegalStateException();

		for (String key : data) {
			NBTBase value = nbtHandlers.get(key).serializeNBT();
			tag.setTag(key, value);
		}

		return tag;
	}

	public void loadRegisteredNBT(NBTTagCompound compound, String... data) {
		if (nbtHandlers == null) throw new IllegalStateException();

		for (String key : data) {
			NBTBase tag = compound.getTag(key);
			if (NullHelper.nullable(tag) != null) {
				Validate.notNull(nbtHandlers.get(key)).deserializeNBT(tag);
			}
		}
	}
}
