package com.rwtema.extrautils2.tile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.particles.PacketParticleSplineCurve;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import com.rwtema.extrautils2.utils.helpers.VecHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TileInteractionProxy extends TilePower implements ITickable, IRemoteTarget, IDynamicHandler {
	public static final Function<BlockPos, Integer> GET_X = BlockPos::getX;
	public static final Function<BlockPos, Integer> GET_Y = BlockPos::getY;
	public static final Function<BlockPos, Integer> GET_Z = BlockPos::getZ;
	public static final BiConsumer<NBTSerializable.NBTMutableBlockPos, Integer> SET_POS_X = NBTSerializable.NBTMutableBlockPos::setPosX;
	public static final BiConsumer<NBTSerializable.NBTMutableBlockPos, Integer> SET_POS_Y = NBTSerializable.NBTMutableBlockPos::setPosY;
	public static final BiConsumer<NBTSerializable.NBTMutableBlockPos, Integer> SET_POS_Z = NBTSerializable.NBTMutableBlockPos::setPosZ;
	public static final Set<Capability<?>> VALID_CAPS = ImmutableSet.of(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, CapabilityEnergy.ENERGY);
	public NBTSerializable.NBTMutableBlockPos targetA = registerNBT("target_a", new NBTSerializable.NBTMutableBlockPos());
	public NBTSerializable.NBTMutableBlockPos targetB = registerNBT("target_b", new NBTSerializable.NBTMutableBlockPos());
	public NBTSerializable.NBTMutableBlockPos currentPos = registerNBT("pos", new NBTSerializable.NBTMutableBlockPos());
	public NBTSerializable.Float power = registerNBT("power", new NBTSerializable.Float(0));
	boolean searching;
	long time = -1;

	@Override
	public void onPowerChanged() {

	}

	@Override
	public float getPower() {
		return power.value;
	}

	@Override
	public void update() {
		if (world.isRemote) return;
		long totalWorldTime = world.getTotalWorldTime();
		if (totalWorldTime != time && totalWorldTime % 20 == 0) {
			time = totalWorldTime;
			nextPos();
			markForUpdate();
			world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
			markDirty();
			power.value = Math.abs(currentPos.getX()) + Math.abs(currentPos.getY()) + Math.abs(currentPos.getZ());
//			((WorldServer) world).spawnParticle(EnumParticleTypes.REDSTONE,
//					getPos().getX() + currentPos.getX() + 0.5,
//					getPos().getY() + currentPos.getY() + 0.5,
//					getPos().getZ() + currentPos.getZ() + 0.5,
//					1, 3 / 255.0, 38 / 255.0, 32 / 255.0, 0.0D);
//			onSuccessfulInteract(world,getPos().add( currentPos), EnumFacing.DOWN, true);
		}
	}

	@Override
	@Nullable
	public Optional<Pair<World, BlockPos>> getTargetPos() {
		if (!active) {
			return Optional.empty();
		}

		verifyPos();

		if (currentPos.equals(BlockPos.ORIGIN)) {
			return Optional.empty();
		}

		return Optional.of(Pair.of(world, getPos().add(currentPos)));
	}

	public void verifyPos() {
		currentPos.setPos(
				MathHelper.clamp(currentPos.getX(), Math.min(targetA.getX(), targetB.getX()), Math.max(targetA.getX(), targetB.getX())),
				MathHelper.clamp(currentPos.getY(), Math.min(targetA.getY(), targetB.getY()), Math.max(targetA.getY(), targetB.getY())),
				MathHelper.clamp(currentPos.getZ(), Math.min(targetA.getZ(), targetB.getZ()), Math.max(targetA.getZ(), targetB.getZ()))
		);
	}

	public void nextPos() {
		verifyPos();
		if (tryAdvance(GET_X, SET_POS_X)) {
			if (tryAdvance(GET_Z, SET_POS_Z)) {
				tryAdvance(GET_Y, SET_POS_Y);
			}
		}

	}

	public boolean tryAdvance(Function<BlockPos, Integer> getter, BiConsumer<NBTSerializable.NBTMutableBlockPos, Integer> setter) {
		int t = getter.apply(currentPos);
		t--;

		if (t < getter.apply(targetA) && t < getter.apply(targetB)) {
			t = Math.max(getter.apply(targetA), getter.apply(targetB));
			setter.accept(currentPos, t);
			return true;
		} else {
			setter.accept(currentPos, t);
			return false;
		}
	}

	@Override
	public void onSuccessfulInteract(World world, BlockPos pos, EnumFacing side, boolean success) {
		final int VELOCITY = -8;
		if (success && world == getWorld()) {
			Vec3d start = new Vec3d(getPos()).addVector(0.5, 0.5, 0.5);
			Vec3d startVel = VecHelper.addSide(Vec3d.ZERO, side.getOpposite(), -VELOCITY);
			NetworkHandler.sendToAllAround(
					new PacketParticleSplineCurve(
							start,
							new Vec3d(pos).addVector(0.5, 0.5, 0.5),
							startVel,
							VecHelper.addSide(Vec3d.ZERO, side, VELOCITY / 2F),
							0xff8cf4e2
					), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 40));
		}
	}

	@Nullable
	@Override
	public NBTTagCompound getSaveInfo() {
		return NBTSerializable.saveData(new NBTTagCompound(), ImmutableMap.<String, INBTSerializable>builder()
				.put("targetA", targetA)
				.put("targetB", targetB)
				.build());
	}

	@Override
	public void loadSaveInfo(@Nonnull NBTTagCompound tag) {
		NBTSerializable.loadData(tag, ImmutableMap.<String, INBTSerializable>builder()
				.put("targetA", targetA)
				.put("targetB", targetB)
				.build());
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		packet.writeVarInt(targetA.getX());
		packet.writeVarInt(targetA.getY());
		packet.writeVarInt(targetA.getZ());
		packet.writeVarInt(targetB.getX());
		packet.writeVarInt(targetB.getY());
		packet.writeVarInt(targetB.getZ());
		packet.writeVarInt(currentPos.getX());
		packet.writeVarInt(currentPos.getY());
		packet.writeVarInt(currentPos.getZ());
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		targetA.setPos(packet.readVarInt(), packet.readVarInt(), packet.readVarInt());
		targetB.setPos(packet.readVarInt(), packet.readVarInt(), packet.readVarInt());
		currentPos.setPos(packet.readVarInt(), packet.readVarInt(), packet.readVarInt());
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerInteractionProxy(this);
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (searching || world.isRemote || !VALID_CAPS.contains(capability)) return null;
		BlockPos add = getPos().add(currentPos);

		if (getPos().equals(add) || !world.isBlockLoaded(add)) return null;

		TileEntity tileEntity = world.getTileEntity(add);
		if (tileEntity == null) return null;

		try {
			searching = true;
			return tileEntity.hasCapability(capability, facing) ? tileEntity.getCapability(capability, facing) : null;
		} finally {
			searching = false;
		}
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return getCapability(capability, facing) != null;
	}

	public static class ContainerInteractionProxy extends DynamicContainerTile {

		public ContainerInteractionProxy(TileInteractionProxy tile) {
			super(tile);

			addTitle(tile);
			crop();
			addWidget(new WidgetTextData(5, height, playerInvWidth - 5 * 2) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeBlockPos(tile.getPos());
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					BlockPos blockPos = packet.readBlockPos();
					return Lang.translateArgs("Origin: [%s, %s, %s]", StringHelper.format(blockPos.getX()), StringHelper.format(blockPos.getY()), StringHelper.format(blockPos.getZ()));
				}
			});
			crop();
			addWidget(new WidgetTextData(5, height, playerInvWidth - 5 * 2) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeBlockPos(tile.getPos().add(tile.currentPos));
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					BlockPos blockPos = packet.readBlockPos();
					return Lang.translateArgs("Scanning: [%s, %s, %s]", StringHelper.format(blockPos.getX()), StringHelper.format(blockPos.getY()), StringHelper.format(blockPos.getZ()));
				}
			});
			crop(5);
			int w = 9 * 5;
			int x;
			for (NBTSerializable.NBTMutableBlockPos mutableBlockPos : ImmutableList.of(tile.targetA, tile.targetB)) {
				x = 4;
				addWidget(new WidgetText(x, height,
						tile.targetA == mutableBlockPos ?
								Lang.translate("Block Range Start") : Lang.translate("Block Range End")
				));
				crop();
				for (Pair<Function<BlockPos, Integer>, BiConsumer<NBTSerializable.NBTMutableBlockPos, Integer>> pair :
						ImmutableList
								.<Pair<Function<BlockPos, Integer>, BiConsumer<NBTSerializable.NBTMutableBlockPos, Integer>>>of(
										Pair.of(GET_X, SET_POS_X),
										Pair.of(GET_Y, SET_POS_Y),
										Pair.of(GET_Z, SET_POS_Z)
								)) {
					new UpDownIntSelector(x, height, w) {
						@Override
						public int getValue() {
							return pair.getLeft().apply(mutableBlockPos);
						}

						@Override
						public void setValue(int val) {
							pair.getRight().accept(mutableBlockPos, val);
						}
					}.forEach(this::addWidget);
					x += w + 4;
				}
				crop();
				height += 8;

			}

			crop();
			validate();
		}


	}
}
