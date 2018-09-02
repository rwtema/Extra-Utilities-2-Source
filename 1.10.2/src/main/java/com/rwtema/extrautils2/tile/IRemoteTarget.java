package com.rwtema.extrautils2.tile;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Optional;

public interface IRemoteTarget {

	Optional<Pair<World, BlockPos>> getTargetPos();

	void onSuccessfulInteract(World world, BlockPos target, @Nullable EnumFacing side, boolean success);

}
