package com.rwtema.extrautils2.transfernodes;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.PositionPool;
import com.rwtema.extrautils2.utils.blockaccess.ThreadSafeBlockAccess;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;

public class TransferNodeEnergy extends Grocket {
	public static final int MAX_SEND = 10000;
	static boolean sending = false;
	public ListenableFuture<Runnable> submit;
	boolean needsRecheck = true;
	@Nullable
	ArrayList<TileIndexer.SidedPos> dests;
	int sentPerTick = 0;
	public IEnergyStorage storage = new IEnergyStorage() {
		@Override
		public int receiveEnergy(int energy, boolean simulate) {
			if (sending) return 0;
			try {
				sending = true;
				ArrayList<TileIndexer.SidedPos> dests = TransferNodeEnergy.this.dests;
				if (dests == null) {
					return 0;
				}
				energy = Math.min(MAX_SEND - sentPerTick, energy);
				if (energy <= 0) {
					return 0;
				}

				HashSet<IEnergyStorage> storages = new HashSet<>();
				World world = holder.world();
				for (TileIndexer.SidedPos pos : dests) {
					IPipe pipe = TransferHelper.getPipe(world, pos.pos);
					if (pipe != null) {
						IEnergyStorage capability = pipe.getCapability(world, pos.pos, pos.side, CapGetter.energyReceiver);
						if (capability == null) {
							needsRecheck = true;
						} else {
							storages.add(capability);
						}
					}
				}

				if (storages.isEmpty()) return 0;

				int sent = 0;
				if (!simulate) {
					for (int i = 0; i < 3; i++) {
						int limit = energy / storages.size();
						if (limit > 0) {
							for (IEnergyStorage storage : storages) {
								sent += storage.receiveEnergy(Math.min(energy - sent, limit), false);
								if (sent >= energy) break;
							}
						} else {
							break;
						}
					}
				}

				if (sent < energy) {
					for (IEnergyStorage storage : storages) {
						sent += storage.receiveEnergy(energy - sent, simulate);
						if (sent >= energy) break;
					}
				}

				sentPerTick += sent;
				return sent;
			} finally {
				sending = false;
			}
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return MAX_SEND;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	};

	@javax.annotation.Nullable
	@Override
	public <T> T getCapability(Capability<T> capability) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(storage);
		}
		return super.getCapability(capability);
	}

	@Override
	public boolean onActivated(EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		needsRecheck = true;
		return true;
	}

	@Override
	public void update() {
		sentPerTick = 0;

		if (holder.getWorld().isRemote) return;
		if (submit != null) return;

		if (!needsRecheck && holder.getWorld().getTotalWorldTime() % 200 != 0) return;

		final PositionPool pool = new PositionPool();
		BlockPos intern = pool.intern(holder.getPos());
		final HashSet<BlockPos> alreadyChecked = new HashSet<>();
		final LinkedList<BlockPos> toCheck = new LinkedList<>();
		toCheck.add(intern);

		final IBlockAccess world = new ThreadSafeBlockAccess((WorldServer) holder.getWorld());

		submit = HttpUtil.DOWNLOADER_EXECUTOR.submit(
				new Callable<Runnable>() {
					@Override
					public Runnable call() throws Exception {
						final ArrayList<TileIndexer.SidedPos> sides = new ArrayList<>();

						BlockPos pos1;
						while ((pos1 = toCheck.poll()) != null) {
							if (TransferNodeEnergy.this.holder.isInvalid()) return null;

							IPipe pipe = TransferHelper.getPipe(world, pos1);
							if (pipe == null) continue;

							for (EnumFacing facing : EnumFacing.values()) {
								if (pipe.hasCapability(world, pos1, facing, CapGetter.energyReceiver)) {
									sides.add(new TileIndexer.SidedPos(pos1, facing));
								}

								BlockPos offset = pool.offset(pos1, facing);
								if (alreadyChecked.contains(offset))
									continue;

								IPipe otherPipe = TransferHelper.getPipe(world, offset);

								if (otherPipe != null &&
										(pipe.canOutput(world, pos1, facing, null) && otherPipe.canInput(world, offset, facing.getOpposite()) ||
												(pipe.canInput(world, pos1, facing) && otherPipe.canOutput(world, offset, facing.getOpposite(), null)))) {
									alreadyChecked.add(offset);
									toCheck.add(offset);
								}
							}
						}

						return () -> TransferNodeEnergy.this.process(sides);
					}
				});

		Futures.addCallback(submit, new FutureCallback<Runnable>() {
			@Override
			public void onSuccess(Runnable result) {
				if (result != null)
					FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(result);
				TransferNodeEnergy.this.submit = null;
			}

			@Override
			public void onFailure(@Nonnull final Throwable t) {
				FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
					throw Throwables.propagate(t);
				});
				TransferNodeEnergy.this.submit = null;
			}
		});
	}

	public void process(ArrayList<TileIndexer.SidedPos> sides) {
		dests = sides;
	}

	@Override
	public GrocketType getType() {
		return GrocketType.TRANSFER_NODE_ENERGY;
	}

	@Override
	public float getPower() {
		return 0;
	}

}
