package com.rwtema.extrautils2.items;

import com.google.common.collect.Multimap;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.ILeftClickHandler;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.entity.chunkdata.EntityChunkData;
import com.rwtema.extrautils2.interblock.FlatTransferNodeHandler;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemFlatTransferNode extends XUItemFlatMetadata implements ILeftClickHandler {
	public static final String ITEM_TEXTURE = "transfernodes/flat_transfernode_items";
	public static final String FLUID_TEXTURE = "transfernodes/flat_transfernode_fluids";
	public static final String SELECTION_TEXTURE = "transfernodes/flat_transfernode_selection";
	public static final String BACK_TEXTURE = "transfernodes/flat_transfernode_back";

	public ItemFlatTransferNode() {
		super(ITEM_TEXTURE, FLUID_TEXTURE);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Nullable
	public static FlatTransferNodeHandler.FlatTransferNode getCurrentFlatTransferNode(EntityPlayer player) {
		int length = 5;
		Vec3d startPos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3d endPos = startPos.addVector(player.getLookVec().x * length, player.getLookVec().y * length, player.getLookVec().z * length);

		int cx0 = ((int) Math.floor(Math.min(startPos.x, endPos.x))) >> 4;
		int cz0 = ((int) Math.floor(Math.min(startPos.z, endPos.z))) >> 4;
		int cx1 = ((int) Math.ceil(Math.max(startPos.x, endPos.x))) >> 4;
		int cz1 = ((int) Math.ceil(Math.max(startPos.z, endPos.z))) >> 4;

		double bestDist = -1;
		FlatTransferNodeHandler.FlatTransferNode bestDistNode = null;
		World world = player.getEntityWorld();
		for (int cx = cx0; cx <= cx1; cx++) {
			for (int cz = cz0; cz <= cz1; cz++) {
				Multimap<BlockPos, FlatTransferNodeHandler.FlatTransferNode> chunkData = EntityChunkData.getChunkData(world.getChunkFromChunkCoords(cx, cz), FlatTransferNodeHandler.INSTANCE, false);
				for (FlatTransferNodeHandler.FlatTransferNode node : chunkData.values()) {
					AxisAlignedBB bounds = node.getBounds();
					RayTraceResult ray = bounds.calculateIntercept(startPos, endPos);
					if (ray != null) {
						double d = startPos.squareDistanceTo(ray.hitVec);
						if (d < bestDist || bestDist == -1) {
							bestDist = d;
							bestDistNode = node;
						}
					}
				}
			}
		}


		return bestDistNode;
	}

	@Override
	public boolean renderLayerIn3D(ItemStack stack, int renderPass) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		super.registerTextures();
		Textures.register(SELECTION_TEXTURE);
		Textures.register(BACK_TEXTURE);
	}

	@Override
	public EnumActionResult onItemUseFirstBase(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (world.isRemote) {
			ExtraUtils2.proxy.run(new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					NetHandlerPlayClient connection = Minecraft.getMinecraft().getConnection();
					if (connection != null)
						connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, side, hand, hitX, hitY, hitZ));
				}
			});
			return EnumActionResult.SUCCESS;
		}

		if (!player.isSneaking()) {
			FlatTransferNodeHandler.FlatTransferNode node = getCurrentFlatTransferNode(player);
			if (node != null) {
				player.openGui(ExtraUtils2.instance, FlatTransferNodeHandler.guiIdSides[node.side.ordinal()], world, node.pos.getX(), node.pos.getY(), node.pos.getZ());
				return EnumActionResult.SUCCESS;
			}
		}

		TileEntity te = world.getTileEntity(pos);
		if (te != null) {
			FlatTransferNodeHandler.FlatTransferNode.Type type = FlatTransferNodeHandler.FlatTransferNode.Type.values()[stack.getMetadata() % 2];
			if ((type == FlatTransferNodeHandler.FlatTransferNode.Type.ITEM &&
					CapGetter.ItemHandler.hasInterface(te, side)) ||
					(type == FlatTransferNodeHandler.FlatTransferNode.Type.FLUIDS &&
							CapGetter.FluidHandler.hasInterface(te, side))
			) {
				FlatTransferNodeHandler.FlatTransferNode newNode = new FlatTransferNodeHandler.FlatTransferNode(pos.toImmutable(), side, type, !ExtraUtils2.proxy.isAltSneaking(player));
				Chunk chunk = world.getChunkFromBlockCoords(pos);

				boolean flag = false;
				for (FlatTransferNodeHandler.FlatTransferNode node : EntityChunkData.getChunkData(chunk, FlatTransferNodeHandler.INSTANCE, false).get(pos)) {
					if (node.side == side) {
						flag = true;
						break;
					}
				}

				if (!flag) {
					EntityChunkData.getChunkData(chunk, FlatTransferNodeHandler.INSTANCE, true)
							.put(newNode.pos, newNode);
					EntityChunkData.markChunkDirty(chunk);
					if (!player.capabilities.isCreativeMode)
						StackHelper.decrease(stack);

				}
			}

		}

		return EnumActionResult.SUCCESS;
	}

	@SubscribeEvent
	public void run(PlayerInteractEvent.RightClickBlock event) {
//		if (!event.getWorld().isRemote) {
//			return;
//		}
//		ItemStack itemStack = event.getItemStack();
//		if (StackHelper.isNonNull(itemStack) && itemStack.getItem() == this) {
//			event.setCanceled(true);
//		}
	}

	@Override
	public boolean leftClick(World world, EntityPlayer player, ItemStack stack) {
		FlatTransferNodeHandler.FlatTransferNode node = getCurrentFlatTransferNode(player);
		if (node == null) return false;
		if (!world.isRemote) {
			node.isDead = true;
			Chunk chunk = world.getChunkFromBlockCoords(node.pos);
			Multimap<BlockPos, FlatTransferNodeHandler.FlatTransferNode> chunkData = EntityChunkData.getChunkData(chunk, FlatTransferNodeHandler.INSTANCE, false);
			if (chunkData != null) {
				if (chunkData.remove(node.pos, node)) {
					node.dropItemStack(world);
					EntityChunkData.markChunkDirty(chunk);
				}
			}
		}
		return true;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		switch (stack.getMetadata()) {
			case 0:
				return super.getUnlocalizedName(stack) + ".item";
			case 1:
				return super.getUnlocalizedName(stack) + ".fluid";
			default:
				return super.getUnlocalizedName(stack);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		tooltip.add(Lang.translate("Thinner than the thinnest of pancakes."));
		tooltip.add(Lang.translate("Small enough to fit between blocks."));
		tooltip.add("");
		tooltip.add(Lang.translateArgs("Hold %s when placing to reverse push/pull.", Minecraft.getMinecraft().gameSettings.keyBindSprint.getDisplayName()));
	}
}
