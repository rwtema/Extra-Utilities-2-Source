package com.rwtema.extrautils2.backend.model;

import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public class BoxFullCross extends Box {
	private static final UV[][] stdUVs = new UV[][]{
			{
					new UV(0, 0, 0, 0, 0),
					new UV(1, 0, 1, 1, 0),
					new UV(1, 1, 1, 1, 1),
					new UV(0, 1, 0, 0, 1),
			},
			{
					new UV(0, 0, 1, 0, 0),
					new UV(1, 0, 0, 1, 0),
					new UV(1, 1, 0, 1, 1),
					new UV(0, 1, 1, 0, 1),
			},
			{
					new UV(0, 1, 0, 0, 1),
					new UV(1, 1, 1, 1, 1),
					new UV(1, 0, 1, 1, 0),
					new UV(0, 0, 0, 0, 0),
			},
			{
					new UV(0, 1, 1, 0, 1),
					new UV(1, 1, 0, 1, 1),
					new UV(1, 0, 0, 1, 0),
					new UV(0, 0, 1, 0, 0),
			},
	};

	public BoxFullCross() {
		super(0, 0, 0, 1, 1, 1);
	}

	@Override
	public List<BakedQuad> makeQuads(@Nullable EnumFacing side) {
		if (side != null) return null;
		List<BakedQuad> list = new ArrayList<>(4);
		for (UV[] stdUV : stdUVs) {
			list.add(QuadHelper.createBakedQuad(stdUV, texture, true, tint));
		}
		return list;
	}


	@Override
	public Box copy() {
		Box box = new BoxFullCross();
		copyBaseProperties(box);
		return box;
	}
}
