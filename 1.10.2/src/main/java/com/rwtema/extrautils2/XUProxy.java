package com.rwtema.extrautils2;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.ISidedFunction;
import com.rwtema.extrautils2.backend.SidedCallable;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.model.IClientClearCache;
import com.rwtema.extrautils2.eventhandlers.RevengeHandler;
import com.rwtema.extrautils2.eventhandlers.SlimeSpawnHandler;
import com.rwtema.extrautils2.keyhandler.KeyAlt;
import com.rwtema.extrautils2.network.PacketHandler;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.tile.tesr.ITESRHookSimple;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.helpers.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

public class XUProxy {
	public static final ClientRunnable sendRightClick = new ClientRunnable() {
		@Override
		@SideOnly(Side.CLIENT)
		public void run() {
			EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;
			thePlayer.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
		}
	};

	public void run(ClientRunnable callable) {

	}


	public void clearClientCache(IClientClearCache box) {

	}

	public void registerTexture(String... texture) {

	}

	public boolean isClientSide() {
		return false;
	}

	public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
		if (handler instanceof NetHandlerPlayServer) {
			return ((NetHandlerPlayServer) handler).player;
		} else {
			return null;
		}
	}


	public EntityPlayer getClientPlayer() {
		throw new RuntimeException("getClientPlayer called on server");
	}

	public World getClientWorld() {
		throw new RuntimeException("getClientWorld called on server");
	}


	public PacketHandler getNewPacketHandler() {
		LogHelper.oneTimeInfo("CreatePacketHandler Server");
		return new PacketHandler();
	}

	public boolean isAltSneaking(EntityPlayer player) {
		return KeyAlt.isAltSneaking(player);
	}

	public void sendUsePacket(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {

	}

	public void registerBlock(XUBlock xuBlock) {

	}

	public void registerClientCommand() {

	}

	public void registerHandlers() {
		SlimeSpawnHandler.init();
		RevengeHandler.init();
	}


	public <F, T> T apply(ISidedFunction<F, T> func, F input) {
		return func.applyServer(input);
	}

	public boolean onBlockStartBreak(World world, ItemStack itemstack, BlockPos pos, EntityPlayer player, boolean reduceParticles) {
		if (world.isRemote) throw new IllegalStateException("Client World on Server");
		PlayerInteractionManager theItemInWorldManager = ((EntityPlayerMP) player).interactionManager;
		WorldHelper.markBlockForUpdate(world, pos);
		return theItemInWorldManager.tryHarvestBlock(pos);
	}

	public <T> T nullifyOnServer(T object) {
		return null;
	}

	public <T extends XUTile & ITESRHook> void registerTESR(Class<T> clazz) {

	}

	public <T extends XUTile & ITESRHookSimple> void registerTESROther(Class<T> clazz) {

	}

	public Collection<ItemStack> getSubItems(Item item) {
		return ImmutableList.of(new ItemStack(item));
	}

	public <V> V call(SidedCallable<V> sidedCallable) {
		return sidedCallable.callServer();
	}
}
