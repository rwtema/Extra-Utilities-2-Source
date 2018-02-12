package com.rwtema.extrautils2.backend.model;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public class BoxDoubleSided extends Box {
	public BoxDoubleSided(int x0, int y0, int z0, int x1, int y1, int z1, boolean dummy) {
		super(x0, y0, z0, x1, y1, z1, dummy);
	}

	public BoxDoubleSided(Box box) {
		super(box);
	}

	public BoxDoubleSided(float x0, float y0, float z0, float x1, float y1, float z1) {
		super(x0, y0, z0, x1, y1, z1);
	}


	@Override
	public List<BakedQuad> makeQuads(@Nullable EnumFacing side) {
		return addReverseQuads(super.makeQuads(side));
	}

	@Override
	public Box copy() {
		Box box = new BoxDoubleSided(this);
		copyBaseProperties(box);
		return box;
	}
}
