package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PlayerHelper {
	public static final ArrayList<Function<EntityPlayer, Stream<ItemStack>>> getPlayerInventories = Lists.newArrayList(
			(Function<EntityPlayer, Stream<ItemStack>>) player -> IntStream.range(0, player.inventory.getSizeInventory()).mapToObj(player.inventory::getStackInSlot)
	);
	private static final UUID temaID = UUID.fromString("72ddaa05-7bbe-4ae2-9892-2c8d90ea0ad8");

	public static boolean isPlayerReal(EntityPlayer player) {
		return player != null &&
				player.world != null &&
				!player.world.isRemote &&
				(player.getClass() == EntityPlayerMP.class ||
						FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().contains(player));

	}

	public static boolean isTema(GameProfile gameProfile) {
		return isTema(gameProfile.getName(), gameProfile.getId());
	}

	private static boolean isTema(String name, UUID id) {
		return "RWTema".equals(name) && id.equals(temaID);
	}

	public static boolean isThisPlayerACheatyBastardOfCheatBastardness(EntityPlayer player) {
		return isPlayerReal(player) && isTema(player.getGameProfile());
	}

	public static void syncInventory(EntityPlayerMP player) {
		player.inventory.markDirty();
		player.mcServer.getPlayerList().syncPlayerInventory(player);
	}

	public static RayTraceResult rayTrace(EntityPlayer player) {
		if (player == null) return null;
		float pitch = player.rotationPitch;
		float yaw = player.rotationYaw;
		double dx = player.posX, dy = player.posY + (double) player.getEyeHeight(), dz = player.posZ;
		Vec3d vec3 = new Vec3d(dx, dy, dz);
		float f2 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-pitch * 0.017453292F);
		float f5 = MathHelper.sin(-pitch * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = 5.0D;
		if (player instanceof EntityPlayerMP) {
			d3 = ((EntityPlayerMP) player).interactionManager.getBlockReachDistance();
		}
		Vec3d vec31 = vec3.addVector((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
		return player.world.rayTraceBlocks(vec3, vec31, false, false, true);
	}

	public static List<ItemStack> getAllPlayerItems(EntityPlayer player) {
		return getPlayerInventories.stream().flatMap(t -> t.apply(player)).filter(Objects::nonNull).collect(Collectors.toList());
	}


}
