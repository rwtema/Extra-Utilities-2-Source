package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.transfernodes.FacingHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class BoxQuadList extends Box {

	Map<EnumFacing, List<BakedQuad>> sidedQuads = new HashMap<>();

	public BoxQuadList(Map<EnumFacing, List<BakedQuad>> sidedQuads) {
		super(getFullBounds(Iterables.concat(sidedQuads.values())));
		this.sidedQuads = sidedQuads;
		for (EnumFacing facing : FacingHelper.facingPlusNull) {
			sidedQuads.computeIfAbsent(facing, t -> ImmutableList.of());
		}

	}

	public BoxQuadList(Iterable<BakedQuad> quads) {
		super(getFullBounds(quads));

		HashMap<EnumFacing, ImmutableList.Builder<BakedQuad>> builderHashMap = new HashMap<>();
		for (EnumFacing facing : FacingHelper.facingPlusNull) {
			builderHashMap.put(facing, new ImmutableList.Builder<>());
		}

		for (BakedQuad quad : quads) {
			AxisAlignedBB bounds = getBounds(quad);
			EnumFacing side;
			if (bounds.minX == 1 && bounds.maxX == 1) {
				side = EnumFacing.EAST;
			} else if (bounds.minX == 0 && bounds.maxX == 0) {
				side = EnumFacing.WEST;
			} else if (bounds.minY == 1 && bounds.maxY == 1) {
				side = EnumFacing.UP;
			} else if (bounds.minY == 0 && bounds.maxY == 0) {
				side = EnumFacing.DOWN;
			} else if (bounds.minZ == 1 && bounds.maxZ == 1) {
				side = EnumFacing.SOUTH;
			} else if (bounds.minZ == 0 && bounds.maxZ == 0) {
				side = EnumFacing.NORTH;
			} else {
				side = null;
			}
			builderHashMap.get(side).add(quad);
		}

		for (EnumFacing facing : FacingHelper.facingPlusNull) {
			sidedQuads.put(facing, builderHashMap.get(facing).build());
		}
	}

	public static Box getFullBounds(Iterable<BakedQuad> quads) {
		AxisAlignedBB bound = null;
		for (BakedQuad quad : quads) {
			if (bound == null) {
				bound = getBounds(quad);
			} else {
				bound = bound.union(getBounds(quad));
			}
		}
		return new Box(bound);
	}

	public static AxisAlignedBB getBounds(BakedQuad quads) {
		int[] vertex = quads.getVertexData();
		AxisAlignedBB bb = null;
		for (int i = 0; i < 4; i++) {
			float x = Float.intBitsToFloat(vertex[i * 7]);
			float y = Float.intBitsToFloat(vertex[i * 7 + 1]);
			float z = Float.intBitsToFloat(vertex[i * 7 + 2]);
			AxisAlignedBB t = new AxisAlignedBB(x, y, z, x, y, z);

			if (bb == null) {
				bb = t;
			} else {
				bb = bb.union(t);
			}
		}
		return bb;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable EnumFacing side) {
		return sidedQuads.get(side);
	}
}
