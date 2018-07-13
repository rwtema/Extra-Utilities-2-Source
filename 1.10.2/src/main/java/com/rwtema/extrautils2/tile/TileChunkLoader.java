package com.rwtema.extrautils2.tile;

import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.chunkloading.XUChunkLoaderManager;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TileChunkLoader extends TilePower {
	public final int CHUNK_RANGE = 1;
	private GameProfile profile;

	@Override
	public void onPowerChanged() {
		XUChunkLoaderManager.dirty = true;
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (profile != null)
			compound.setTag("profile", NBTHelper.proifleToNBT(profile));
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		profile = NBTHelper.profileFromNBT(compound.getCompoundTag("profile"));
	}

	@Override
	public float getPower() {
		return 8;
	}

	public GameProfile getProfile() {
		return profile;
	}

	public Collection<ChunkPos> getChunkCoords() {
		List<ChunkPos> list = new ArrayList<>(10);
		int x = getPos().getX() >> 4;
		int z = getPos().getZ() >> 4;
		for (int dx = -CHUNK_RANGE; dx <= CHUNK_RANGE; dx++) {
			for (int dz = -CHUNK_RANGE; dz <= CHUNK_RANGE; dz++) {
				list.add(new ChunkPos(x + dx, z + dz));
			}
		}

		return list;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		if (placer instanceof EntityPlayerMP) {
			GameProfile gameProfile = ((EntityPlayerMP) placer).getGameProfile();
			profile = new GameProfile(gameProfile.getId(), gameProfile.getName());
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (!world.isRemote) {
			XUChunkLoaderManager.unregister(this);
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (!world.isRemote) {
			XUChunkLoaderManager.unregister(this);
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!world.isRemote) {
			XUChunkLoaderManager.register(this);
		}
	}
}
