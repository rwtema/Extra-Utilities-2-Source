package com.rwtema.extrautils2.backend.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class BoxModel extends ArrayList<Box> implements IClientClearCache {
	public static final float OVERLAP = 0F;
	public boolean renderAsNormalBlock;
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite sprite;
	public Box overrideBounds;
	private byte passableFlag;


	public BoxModel() {
	}

	public BoxModel(Box newBox) {
		super(1);
		this.add(newBox);
	}

	public BoxModel(float par1, float par3, float par5, float par7, float par9, float par11) {
		super(1);
		this.add(new Box(par1, par3, par5, par7, par9, par11));
	}

	public static BoxModel newStandardBlock() {
		Box t = new Box(0, 0, 0, 1, 1, 1);
		BoxModel boxes = new BoxModel(t);
		boxes.renderAsNormalBlock = true;
		return boxes;
	}

	public static BoxModel newStandardBlock(boolean dummy) {
		BoxModel boxes = new BoxModel();
		boxes.renderAsNormalBlock = true;
		return boxes;
	}

	public static BoxModel newStandardBlock(String texture) {
		Box t = new Box(0, 0, 0, 1, 1, 1);
		t.texture = texture;
		BoxModel boxes = new BoxModel(t);
		boxes.renderAsNormalBlock = true;
		return boxes;
	}

	public static BoxModel hollowBox(float minX, float minY, float minZ, float holeMinX, float holeMinZ, float holeMaxX, float holeMaxZ, float maxX, float maxY, float maxZ) {
		BoxModel t = new BoxModel();
		t.add(new Box(minX, minY, minZ, holeMinX, maxY, maxZ));
		t.add(new Box(holeMinX, minY, minZ, holeMaxX, maxY, holeMinZ));
		t.add(new Box(holeMinX, minY, holeMaxZ, holeMaxX, maxY, maxZ));
		t.add(new Box(holeMaxX, minY, minZ, maxX, maxY, maxZ));
		return t;
	}

	public static Box boundingBox(BoxModel models, boolean collideOnly) {
		if (models == null) {
			return null;
		}

		if (models.overrideBounds != null) {
			if (collideOnly) {
				for (Box box : models) {
					if (!box.noCollide)
						return models.overrideBounds;
				}
				return null;
			} else
				return models.overrideBounds;
		}

		if (models.isEmpty()) {
			return null;
		}


		Box bounds = null;
		for (Box box : models) {
			if (collideOnly && box.noCollide) continue;
			if (bounds != null) {
				bounds.increaseBounds(box);
			} else {
				bounds = new Box(box);
			}
		}

		return bounds;
	}

	public static BoxModel crossBoxModel() {
		float size = 0.2F;
		BoxModel model = new BoxModel();
		BoxRotatable box1 = new BoxRotatable(0.5F, 0, -size, 0.5F, 1, 1 + size);
		box1.setInvisible(1 | 2 | 4 | 8);
		box1.rotate(1, 1, 0.5F, 0.5F, 0.5F, 0, 1, 0);
		box1.setTextureBounds(new float[][]{null, null, null, null, {0, 0, 16, 16}, {0, 0, 16, 16}});
		model.add(box1);

		BoxRotatable box2 = new BoxRotatable(0.5F, 0, -size, 0.5F, 1, 1 + size);
		box2.setInvisible(1 | 2 | 4 | 8);
		box2.setTextureBounds(new float[][]{null, null, null, null, {0, 0, 16, 16}, {0, 0, 16, 16}});
		box2.rotate(1, -1, 0.5F, 0.5F, 0.5F, 0, 1, 0);
		model.add(box2);
		return model;
	}

	public AxisAlignedBB getAABB(boolean collideOnly) {
		Box box = boundingBox(this, collideOnly);
		return box != null ? new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ) : null;
	}

	public Box addBoxOverlay(float h) {
		return addBox(-h, -h, -h, 1 + h, 1 + h, 1 + h);
	}

	public Box addBoxI(int par1, int par3, int par5, int par7, int par9, int par11) {
		return this.addBox(par1 / 16F - OVERLAP, par3 / 16F - OVERLAP, par5 / 16F - OVERLAP, par7 / 16F + OVERLAP, par9 / 16F + OVERLAP, par11 / 16F + OVERLAP);
	}

	public Box addBoxI(int par1, int par3, int par5, int par7, int par9, int par11, String texture) {
		return this.addBox(par1 / 16F - OVERLAP, par3 / 16F - OVERLAP, par5 / 16F - OVERLAP, par7 / 16F + OVERLAP, par9 / 16F + OVERLAP, par11 / 16F + OVERLAP).setTexture(texture);
	}

	public Box addBox(float par1, float par3, float par5, float par7, float par9, float par11, String texture) {
		return this.addBox(par1, par3, par5, par7, par9, par11).setTexture(texture);
	}

	public Box addBox(float par1, float par3, float par5, float par7, float par9, float par11) {
		Box b = new Box(par1, par3, par5, par7, par9, par11);
		this.add(b);
		return b;
	}

	public BoxModel rotateToSide(EnumFacing dir) {
		for (Box box : this) {
			box.rotateToSide(dir);
		}

		return this;
	}

	public BoxModel rotateY(EnumFacing side) {
		switch (side) {
			case EAST:
				rotateY(1);
				break;
			case SOUTH:
				rotateY(2);
				break;
			case WEST:
				rotateY(3);
				break;
		}
		return this;
	}

	public BoxModel rotateY(int numRotations) {
		for (Box box : this) {
			box.rotateY(numRotations);
		}

		return this;
	}

	@SideOnly(Side.CLIENT)
	public MutableModel loadIntoMutable(MutableModel result, BlockRenderLayer layer) {
		result.clear();

		result.ambientOcclusion = true;
		result.isGui3D = true;
		for (Box box : this) {
			if (result.tex == null) {
				result.tex = box.getTex();
			}

			if (layer != null && box.layer != layer) {
				continue;
			}

			for (EnumFacing facing : EnumFacing.values()) {
				List<BakedQuad> bakedQuads = box.getQuads(facing);
				if (bakedQuads != null) {
					if (box.isFlush(facing)) {
						result.sidedQuads.get(facing.getIndex()).addAll(bakedQuads);
					} else
						result.generalQuads.addAll(bakedQuads);
				}
			}
			List<BakedQuad> bakedQuads = box.getQuads(null);
			if (bakedQuads != null)
				result.generalQuads.addAll(bakedQuads);
		}
		return result;
	}

	public BoxModel setTextures(Object... objects) {
		for (Box box : this) {
			box.setTextureSides(objects);
		}
		return this;
	}

	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTex() {
		if (sprite != null) return sprite;

		for (Box box : this) {
			if ((sprite = box.getTex()) != null) {
				CachedRenderers.register(this);
				return sprite;
			}
		}
		return Textures.MISSING_SPRITE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientClear() {
		sprite = null;
	}

	public boolean isFullCube() {
		return renderAsNormalBlock;
	}

	public BoxModel setTexture(String s) {
		for (Box box : this) {
			box.setTexture(s);
		}
		return this;
	}

	public BoxModel setLayer(BlockRenderLayer layer) {
		for (Box box : this) {
			box.setLayer(layer);
		}
		return this;
	}

	public AxisAlignedBB getAABB(BlockPos pos, boolean collideOnly) {
		AxisAlignedBB aabb = getAABB(collideOnly);
		if (aabb == null) return null;
		return aabb.offset(pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean getPassable() {
		if (passableFlag == 0) {
			passableFlag = 2;
			for (Box box : this) {
				if (box.noCollide) continue;
				if (box.maxY > 0.5) {
					passableFlag = 1;
					break;
				}
			}
		}

		return passableFlag == 2;
	}

	public BoxModel copy() {
		BoxModel model = new BoxModel();
		model.renderAsNormalBlock = renderAsNormalBlock;
		model.overrideBounds = overrideBounds;
		model.passableFlag = passableFlag;
		for (Box box : this) {
			model.add(box.copy());
		}
		return model;
	}

	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		for (Box box : this) {
			Textures.register(box.texture);
			Textures.register(box.textureSide);
		}
	}

	public void moveToCenterForInventoryRendering() {
		Box bounds = boundingBox(this, false);
		float dx = 0.5F - (bounds.maxX + bounds.minX) / 2;
		float dy = 0 - bounds.minY;
		float dz = 0.5F - (bounds.maxZ + bounds.minZ) / 2;
		if (dx != 0 || dz != 0 || dy != 0)
			for (Box box : this) {
				box.setRenderOffset(dx, dy, dz);
			}
	}
}
