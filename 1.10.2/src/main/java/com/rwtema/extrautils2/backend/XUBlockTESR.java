package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.backend.entries.BlockEntry;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.MutableModel;
import com.rwtema.extrautils2.backend.model.Transforms;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class XUBlockTESR extends XUBlock {
	static {
		BlockEntry.registerTile(XUTESRTile.class);
	}

	public XUBlockTESR() {
		BlockEntry.tileToBlocksMap.put(XUTESRTile.class, this);
	}

	public XUBlockTESR(Material materialIn) {
		super(materialIn);
		BlockEntry.tileToBlocksMap.put(XUTESRTile.class, this);
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	public abstract BoxModel getWorldModel(@Nullable ItemStack stack, IBlockState state, float timer);

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		if (state == null) {
			state = world.getBlockState(pos);
		}
		if (world == null) return getWorldModel(null, state, 0);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof XUTESRTile)) return getWorldModel(null, state, 0);
		XUTESRTile tile = (XUTESRTile) tileEntity;
		return getWorldModel(tile.NBTStack.getRaw(), state, 0);
	}

	@Override
	public BoxModel getRenderModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		if (state == null) {
			state = world.getBlockState(pos);
		}
		if (world == null) return getWorldModel(null, state, 0);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof XUTESRTile)) return getWorldModel(null, state, 0);
		XUTESRTile tile = (XUTESRTile) tileEntity;
		ItemStack raw = tile.NBTStack.getRaw();
		if (StackHelper.isNull(raw)) return getWorldModel(null, state, 0);

		return getRenderModel(raw, state, 0);
	}

	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	@Nonnull
	public XUTESRTile createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new XUTESRTile();
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		return getRenderModel(item, xuBlockState.getStateFromItemStack(item), MCTimer.renderTimer);
	}

	@SideOnly(Side.CLIENT)
	public abstract BoxModel getRenderModel(ItemStack raw, IBlockState state, float renderTimer);

	public static class XUTESRTile extends XUTile implements ITESRHook {

		public NBTSerializable.NBTStack NBTStack = registerNBT("stack", new NBTSerializable.NBTStack());

		@Override
		public void handleDescriptionPacket(XUPacketBuffer packet) {
			NBTStack.setStackRaw(packet.readItemStack());
		}

		@Override
		public void addToDescriptionPacket(XUPacketBuffer packet) {
			packet.writeItemStack(NBTStack.getRaw());
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void render(IBlockAccess world, BlockPos pos, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer, BlockRendererDispatcher blockRenderer) {
			MutableModel model = new MutableModel(Transforms.blockTransforms);
			XUBlockState state = getBlockState();
			if (state == null) {
				state = state;
				return;
			}
			if (!(state.getBlock() instanceof XUBlockTESR)) return;

			BoxModel boxes = ((XUBlockTESR) state.getBlock()).getRenderModel(NBTStack.getRaw(), state, MCTimer.renderTimer + (hashCode() % 1024) / 1024.0F);
			boxes.loadIntoMutable(model, null);
			renderBakedModel(world, renderer, blockRenderer, model);
		}

		@Override
		public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
			super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);
			if (StackHelper.isNonNull(stack)) {
				this.NBTStack.setStackCopy(ItemHandlerHelper.copyStackWithSize(stack, 1));
			}
		}

		@Override
		public Optional<ItemStack> getPickBlock(EntityPlayer player, RayTraceResult target) {
			return Optional.ofNullable(NBTStack.getCopy());
		}

		@Override
		public boolean harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, XUBlock xuBlock, IBlockState state) {
			ItemStack copy = NBTStack.getCopy();
			if (StackHelper.isNonNull(copy)) {
				Block.spawnAsEntity(worldIn, pos, copy);
			}
			return true;
		}
	}
}
