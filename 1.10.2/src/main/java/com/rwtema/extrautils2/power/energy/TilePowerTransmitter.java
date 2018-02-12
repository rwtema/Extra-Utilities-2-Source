package com.rwtema.extrautils2.power.energy;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.SpecialChat;
import com.rwtema.extrautils2.particles.PacketParticleSplineCurve;
import com.rwtema.extrautils2.power.IPowerSubType;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.rwtema.extrautils2.utils.helpers.VecHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class TilePowerTransmitter extends TilePower implements IPowerSubType {
	public static final int VELOCITY = 4;
	public static final int RANGE = 4;
	List<BlockPos> nearbyEnergyTiles;

	public List<BlockPos> getTargets(boolean rebuild) {
		if (rebuild || nearbyEnergyTiles == null)
			return refreshTiles();
		else
			return nearbyEnergyTiles;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			List<BlockPos> list = refreshTiles();
			ITextComponent chat;
			if (list == null || list.isEmpty()) {
				chat = Lang.chat("No nearby tiles");
			} else {
				chat = Lang.chat("Serving %s tiles", list.size());

				EnumFacing s = getBlockState().getValue(XUBlockStateCreator.ROTATION_ALL);

				Vec3d start = VecHelper.addSide(new Vec3d(pos).addVector(0.5, 0.5, 0.5), s, 0.375f);
				Vec3d startVel = VecHelper.addSide(Vec3d.ZERO, s, -VELOCITY);

				for (BlockPos blockPos : list) {
					TileEntity tileEntity = worldIn.getTileEntity(blockPos);
					if (tileEntity != null && EnergyTransfer.isValidOutput(tileEntity)) {
						TreeSet<EnumFacing> faces = new TreeSet<>(new Comparator<EnumFacing>() {
							@Override
							public int compare(EnumFacing o1, EnumFacing o2) {
								if (o1 == o2) return 0;
								int i = -Double.compare(getVal(o1), getVal(o2));
								if (i != 0)
									return i;

								return -o1.compareTo(o2);
							}

							public double getVal(EnumFacing side) {
								Vec3d vec3d = new Vec3d(tileEntity.getPos());
								vec3d = VecHelper.addSide(vec3d, side, 0.1);
								return vec3d.distanceTo(new Vec3d(getPos()));
							}
						});

						Collections.addAll(faces, EnumFacing.values());

						for (EnumFacing facing : faces) {

							if (CapGetter.energyReceiver.hasInterface(tileEntity, facing)) {
								NetworkHandler.sendPacketToPlayer(
										new PacketParticleSplineCurve(
												start,
												new Vec3d(tileEntity.getPos()).addVector(0.5, 0.5, 0.5),
												startVel,
												VecHelper.addSide(Vec3d.ZERO, facing, VELOCITY),
												0xffff0000
										), playerIn);
								break;
							}
						}
					}
				}

			}

			SpecialChat.sendChat(playerIn, chat);

		}
		return true;
	}

	public List<BlockPos> refreshTiles() {
		if (!isLoaded()) return null;

		ImmutableList.Builder<BlockPos> builder = ImmutableList.builder();

		IChunkProvider chunkProvider = world.getChunkProvider();

		int minY = pos.getY() - RANGE;
		int maxY = pos.getY() + RANGE;
		int minX = pos.getX() - RANGE;
		int maxX = pos.getX() + RANGE;
		int minZ = pos.getZ() - RANGE;
		int maxZ = pos.getZ() + RANGE;

		for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
			for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
				Chunk chunk = chunkProvider.getLoadedChunk(chunkX, chunkZ);
				if (chunk == null) continue;
				for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
					BlockPos pos = entry.getKey();
					int y = pos.getY();
					if (y >= minY && y <= maxY && pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ) {
						TileEntity tile = entry.getValue();
						if (EnergyTransfer.isValidOutput(tile)) {
							builder.add(pos);
						}
					}
				}
			}
		}

		return nearbyEnergyTiles = builder.build();
	}


	@Override
	public float getPower() {
		return 1;
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public Collection<ResourceLocation> getTypes() {
		return EnergyTransfer.ENERGY_SYSTEM_TRANSMITTERS;
	}
}
