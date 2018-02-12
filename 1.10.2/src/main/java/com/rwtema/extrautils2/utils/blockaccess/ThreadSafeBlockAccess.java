package com.rwtema.extrautils2.utils.blockaccess;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;


import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ThreadSafeBlockAccess extends CompatBlockAccess implements IBlockAccess {

	public final Long2ObjectMap<Chunk> chunkMap;
	WorldServer world;

	public ThreadSafeBlockAccess(WorldServer world) {
		this.world = world;
		chunkMap = world.getChunkProvider().id2ChunkMap;
	}

	public Chunk getChunk(BlockPos pos) {
		long p_76164_1_ = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
		return chunkMap.get(p_76164_1_);
	}

	@Override
	public TileEntity getTileEntity(@Nonnull BlockPos pos) {
		Chunk chunk = getChunk(pos);
		if(chunk == null) return null;
		Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
		TileEntity tileEntity = map.get(pos);
		if (tileEntity == null || tileEntity.isInvalid() || !pos.equals(tileEntity.getPos())) return null;
		return tileEntity;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
		return 0;
	}

	@Nonnull
	@Override
	public IBlockState getBlockState(@Nonnull BlockPos pos) {
		Chunk chunk = getChunk(pos);
		if (chunk == null) return Blocks.AIR.getDefaultState();
		return chunk.getBlockState(pos);
	}

	@Override
	public boolean isAirBlock(@Nonnull BlockPos pos) {
		IBlockState state = getBlockState(pos);
		return state.getBlock().isAir(state, this, pos);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Biome getBiome(@Nonnull BlockPos pos) {
		return Biomes.FOREST;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean extendedLevelsInChunkCache() {
		return false;
	}

	@Override
	public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
		IBlockState iblockstate = getBlockState(pos);
		return iblockstate.getBlock().getStrongPower(iblockstate, this, pos, direction);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public WorldType getWorldType() {
		return world.getWorldType();
	}

	@Override
	public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
		Chunk chunk = getChunk(pos);
		if (chunk == null || chunk.isEmpty()) return _default;
		return getBlockState(pos).isSideSolid(this, pos, side);
	}

}
