package com.rwtema.extrautils2;

import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.ISidedFunction;
import com.rwtema.extrautils2.backend.SidedCallable;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.model.IClientClearCache;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.commands.CommandDumpTextureSheet;
import com.rwtema.extrautils2.compatibility.ItemCompat;
import com.rwtema.extrautils2.crafting.Radar;
import com.rwtema.extrautils2.hud.HUDHandler;
import com.rwtema.extrautils2.keyhandler.KeyHandler;
import com.rwtema.extrautils2.network.PacketHandler;
import com.rwtema.extrautils2.network.PacketHandlerClient;
import com.rwtema.extrautils2.power.ClientPower;
import com.rwtema.extrautils2.render.LayersHandler;
import com.rwtema.extrautils2.render.TileScreenRenderer;
import com.rwtema.extrautils2.tile.TileScreen;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.tile.tesr.ITESRHookSimple;
import com.rwtema.extrautils2.tile.tesr.XUTESRHook;
import com.rwtema.extrautils2.tile.tesr.XUTESRHookSimple;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public class XUProxyClient extends XUProxy {
	@Override
	public Collection<ItemStack> getSubItems(Item item) {
		ArrayList<ItemStack> list = new ArrayList<>();
		ItemCompat.invokeGetSubItems(item, item.getCreativeTab() != null ? item.getCreativeTab() : CreativeTabs.SEARCH, list);
		return list;
	}

	@Override
	public void run(ClientRunnable callable) {
		callable.run();
	}

	@Override
	public void clearClientCache(IClientClearCache box) {
		box.clientClear();
	}

	@Override
	public boolean isClientSide() {
		return true;
	}

	@Override
	public void registerTexture(String... texture) {
		Textures.register(texture);
	}

	@Override
	public EntityPlayer getPlayerFromNetHandler(INetHandler handler) {
		EntityPlayer player = super.getPlayerFromNetHandler(handler);
		if (player == null) return getClientPlayer();
		return player;
	}


	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
	}

	@Override
	public PacketHandler getNewPacketHandler() {
		LogHelper.oneTimeInfo("CreatePacketHandler Client");
		return new PacketHandlerClient();
	}


	@Override
	public boolean isAltSneaking(EntityPlayer player) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (player == minecraft.player) {
			return KeyHandler.getIsKeyPressed(minecraft.gameSettings.keyBindSprint);
		} else
			return super.isAltSneaking(player);
	}

	@Override
	public void registerBlock(XUBlock xuBlock) {

	}

	@Override
	public void sendUsePacket(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		Minecraft.getMinecraft().getConnection().sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
	}

	@Override
	public void registerHandlers() {
		super.registerHandlers();
		HUDHandler.init();
		ClientPower.init();
		LayersHandler.init();
		ClientRegistry.bindTileEntitySpecialRenderer(TileScreen.class, TileScreenRenderer.instance);
		Radar.init();
	}

	@Override
	public <F, T> T apply(ISidedFunction<F, T> func, F input) {
		return func.applyClient(input);
	}

	@Override
	public void registerClientCommand() {
		ClientCommandHandler.instance.registerCommand(new CommandDumpTextureSheet());
	}

	@Override
	public boolean onBlockStartBreak(World world, ItemStack itemstack, BlockPos pos, EntityPlayer player, boolean reduceParticles) {
		if (!world.isRemote) return super.onBlockStartBreak(world, itemstack, pos, player, reduceParticles);
		Minecraft mc = Minecraft.getMinecraft();
		if (reduceParticles) {
			int particleSetting = mc.gameSettings.particleSetting;
			mc.gameSettings.particleSetting = 2;
			boolean flag = mc.playerController.onPlayerDestroyBlock(pos);
			mc.gameSettings.particleSetting = particleSetting;
			return flag;
		} else
			return mc.playerController.onPlayerDestroyBlock(pos);
	}

	@Override
	public <T> T nullifyOnServer(T object) {
		return object;
	}

	@Override
	public <T extends XUTile & ITESRHook> void registerTESR(Class<T> clazz) {
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, new XUTESRHook<>());
	}

	@Override
	public <T extends XUTile & ITESRHookSimple> void registerTESROther(Class<T> clazz) {
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, new XUTESRHookSimple<>());
	}

	@Override
	public <V> V call(SidedCallable<V> sidedCallable) {
		return sidedCallable.callClient();
	}
}
