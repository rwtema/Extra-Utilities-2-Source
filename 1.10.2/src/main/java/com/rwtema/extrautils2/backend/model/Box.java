package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Box implements IClientClearCache {
	public static final String MISSING_TEXTURE = "[Missing]";
	public static final Box fullBox = new Box(0, 0, 0, 1, 1, 1);
	public static final int[][][] uv = {
			{{1, 5}, {1, 4}, {0, 4}, {0, 5},},
			{{0, 5}, {0, 4}, {1, 4}, {1, 5},},
			{{1, 2}, {1, 3}, {0, 3}, {0, 2},},
			{{1, 2}, {1, 3}, {0, 3}, {0, 2},},
			{{4, 2}, {5, 2}, {5, 3}, {4, 3},},
			{{5, 3}, {4, 3}, {4, 2}, {5, 2},},
	};
	private static final int BOTTOM = 0;
	private static final int TOP = 1;
	private static final int NORTH = 2;
	private static final int SOUTH = 3;
	private static final int WEST = 4;
	private static final int EAST = 5;
	private static final int[][][] sidesVertex = {
			{{1, 2, 4}, {1, 2, 5}, {0, 2, 5}, {0, 2, 4}},
			{{0, 3, 4}, {0, 3, 5}, {1, 3, 5}, {1, 3, 4}},
			{{0, 2, 4}, {0, 3, 4}, {1, 3, 4}, {1, 2, 4}},
			{{1, 2, 5}, {1, 3, 5}, {0, 3, 5}, {0, 2, 5}},
			{{0, 2, 4}, {0, 2, 5}, {0, 3, 5}, {0, 3, 4}},
			{{1, 3, 4}, {1, 3, 5}, {1, 2, 5}, {1, 2, 4}},
	};
	private static final int[] colShade = {0xff7f7f7f, 0xffffffff, 0xffcccccc, 0xffcccccc, 0xff999999, 0xff999999};
	private static final float[] colShadeMult = {0.5f, 1.0f, 0.8f, 0.8f, 0.6f, 0.6f};
	public static int[] rotAdd = new int[]{0, 1, 2, 3};
	public final int[] rotate = new int[6];
	public final String[] textureSide = new String[6];
	public final boolean[] invisible = new boolean[6];
	public float[][] textureBounds = null;
	public float minX;
	public float minY;
	public float minZ;
	public float maxX;
	public float maxY;
	public float maxZ;
	public String texture;
	public int color = 0xffffffff;
	public boolean noCollide;
	@SideOnly(Side.CLIENT)
	public List<BakedQuad>[] cachedQuads;
	public BlockRenderLayer layer = BlockRenderLayer.SOLID;
	public boolean[] flipU = null;
	public boolean[] flipV = null;
	public int tint = -1;
	public float[] renderOffset = null;
	@SideOnly(Side.CLIENT)
	TextureAtlasSprite sprite;


	public Box(float x0, float y0, float z0, float x1, float y1, float z1) {
		setBounds(x0, y0, z0, x1, y1, z1);
	}

	public Box(int x0, int y0, int z0, int x1, int y1, int z1, boolean dummy) {
		setBounds(x0 / 16F, y0 / 16F, z0 / 16F, x1 / 16F, y1 / 16F, z1 / 16F);
	}

	public Box(Box box) {
		this(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}

	public Box(AxisAlignedBB bounds) {
		this((float) bounds.minX,
				(float) bounds.minY,
				(float) bounds.minZ,
				(float) bounds.maxX,
				(float) bounds.maxY,
				(float) bounds.maxZ);
	}


	@SideOnly(Side.CLIENT)
	private static List<BakedQuad> makeFlatSideBakedQuad(Box box, @Nonnull EnumFacing side, TextureAtlasSprite textureAtlasSprite, int tint, int color) {
		int index = side.getIndex();
		int vertex[] = new int[28];

		int rot = box.rotate[side.ordinal()] & 3;
		for (int i = 0; i < 4; i++) {
			//noinspection PointlessArithmeticExpression
			vertex[i * 7 + 0] = Float.floatToRawIntBits(box.getPos(sidesVertex[index][i][0]));
			vertex[i * 7 + 1] = Float.floatToRawIntBits(box.getPos(sidesVertex[index][i][1]));
			vertex[i * 7 + 2] = Float.floatToRawIntBits(box.getPos(sidesVertex[index][i][2]));
		}

		if (color == -1) {
			for (int i = 0; i < 4; i++) {
				vertex[i * 7 + 3] = colShade[index];
			}
		} else {
			for (int i = 0; i < 4; i++) {
				vertex[i * 7 + 3] = ColorHelper.multShade(color, colShadeMult[index]);
			}
		}

		loadTextureUV(box, textureAtlasSprite, index, vertex, rot, box.textureBounds, box.flipU, box.flipV);

		int x = ((byte) (side.getFrontOffsetX() * 127)) & 0xFF;
		int y = ((byte) (side.getFrontOffsetY() * 127)) & 0xFF;
		int z = ((byte) (side.getFrontOffsetZ() * 127)) & 0xFF;
		for (int i = 0; i < 4; i++) {
			vertex[i * 7 + 6] = x | (y << 0x08) | (z << 0x10);
		}

		BakedQuad quad = new BakedQuad(vertex, tint, side, textureAtlasSprite, false, DefaultVertexFormats.ITEM);
		return Lists.newArrayList(quad);
	}

	@SideOnly(Side.CLIENT)
	public static float[][] getUVArray(Box box, int index, int rot, @Nullable float[][] textureBounds, @Nullable boolean[] flipU, @Nullable boolean[] flipV) {
		float[][] uv_result = new float[2][4];
		for (int i = 0; i < 4; i++) {
			float u, v;
			float du, dv;
			float[] textureBound = null;
			if (textureBounds != null && (textureBound = textureBounds[index]) != null) {
				du = 16 * fullBox.getPos(uv[index][i][0]);
				dv = 16 * fullBox.getPos(uv[index][i][1]);
			} else {
				du = 16 * box.getPos(uv[index][i][0]);
				dv = 16 - 16 * box.getPos(uv[index][i][1]);
			}

			if (flipU != null && flipU[index]) {
				du = 16 - du;
			}

			if (flipV != null && flipV[index]) {
				dv = 16 - dv;
			}

			if (textureBound != null) {
				du = textureBound[0] + (textureBound[2] - textureBound[0]) * du / 16F;
				dv = textureBound[1] + (textureBound[3] - textureBound[1]) * dv / 16F;
			}

			if (rot == 0) {
				u = du;
				v = dv;
			} else if (rot == 1) {
				u = 16 - dv;
				v = du;
			} else if (rot == 2) {
				u = 16 - du;
				v = 16 - dv;
			} else if (rot == 3) {
				u = dv;
				v = 16 - du;
			} else {
				throw new RuntimeException("invalid rotation - " + rot);
			}

			u = clamp(u);
			v = clamp(v);

			uv_result[0][i] = u;
			uv_result[1][i] = v;
		}
		return uv_result;
	}

	@SideOnly(Side.CLIENT)
	public static void loadTextureUV(Box box, TextureAtlasSprite sprite, int index, int[] vertex, int rot, @Nullable float[][] textureBounds, @Nullable boolean[] flipU, @Nullable boolean[] flipV) {
		float[][] verts = getUVArray(box, index, rot, textureBounds, flipU, flipV);
		for (int i = 0; i < 4; i++) {
			float u = verts[0][i];
			float v = verts[1][i];
			vertex[i * 7 + 4] = Float.floatToRawIntBits(MathHelper.clamp(sprite.getInterpolatedU(u), sprite.getMinU(), sprite.getMaxU()));
			vertex[i * 7 + 5] = Float.floatToRawIntBits(MathHelper.clamp(sprite.getInterpolatedV(v), sprite.getMinV(), sprite.getMaxV()));
		}
	}

	public static float clamp(float f) {
		return f;//MathHelper.clamp(Math.round(f * 0x1.0p6f) * (0x1.0p-6f), 0, 1);
	}

	public static List<BakedQuad> offsetQuadList(List<BakedQuad> quads, float[] offsets) {
		if (offsets == null || quads == null) return quads;
		for (BakedQuad quad : quads) {
			offsetQuad(quad, offsets);
		}
		return quads;
	}

	public static void offsetQuad(BakedQuad quad, float[] offsets) {
		int[] vertex = quad.getVertexData();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				vertex[i * 7 + j] = Float.floatToRawIntBits(Float.intBitsToFloat(vertex[i * 7 + j]) + offsets[j]);
			}
		}
	}

	public Box setTint(int tint) {
		this.tint = tint;
		return this;
	}

	public Box setLayer(BlockRenderLayer layer) {
		this.layer = layer;
		return this;
	}

	public Box setBounds(float x0, float y0, float z0, float x1, float y1, float z1) {
		this.minX = Math.min(x0, x1);
		this.minY = Math.min(y0, y1);
		this.minZ = Math.min(z0, z1);
		this.maxX = Math.max(x0, x1);
		this.maxY = Math.max(y0, y1);
		this.maxZ = Math.max(z0, z1);
		return this;
	}

	public Box increaseBounds(Box other) {
		if (other.minX < minX) minX = other.minX;
		if (other.minY < minY) minY = other.minY;
		if (other.minZ < minZ) minZ = other.minZ;
		if (other.maxX > maxX) maxX = other.maxX;
		if (other.maxY > maxY) maxY = other.maxY;
		if (other.maxZ > maxZ) maxZ = other.maxZ;
		return this;
	}

	public Box copy() {
		Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
		copyBaseProperties(box);
		return box;
	}

	protected void copyBaseProperties(Box box) {
		box.color = color;
		System.arraycopy(rotate, 0, box.rotate, 0, 6);
		System.arraycopy(invisible, 0, box.invisible, 0, 6);
		System.arraycopy(textureSide, 0, box.textureSide, 0, 6);
		box.texture = texture;
		box.noCollide = noCollide;
		box.layer = layer;
		if (flipU != null) box.flipU = flipU.clone();
		if (flipV != null) box.flipV = flipV.clone();
		box.tint = tint;
		if (renderOffset != null)
			box.renderOffset = renderOffset.clone();
	}

	public Box rotateY(int numRotations) {
		if (numRotations == 0) {
			return this;
		}

		if (numRotations < 0) {
			numRotations += 4;
		}

		numRotations = numRotations & 3;

		for (int i = 0; i < numRotations; i++) {
			Box prev = this.copy();
			this.minZ = prev.minX;
			this.maxZ = prev.maxX;
			this.minX = 1 - prev.maxZ;
			this.maxX = 1 - prev.minZ;

			String temp = this.textureSide[2];
			this.textureSide[2] = this.textureSide[4];
			this.textureSide[4] = this.textureSide[3];
			this.textureSide[3] = this.textureSide[5];
			this.textureSide[5] = temp;

			boolean t = invisible[2];
			invisible[2] = invisible[4];
			invisible[4] = invisible[3];
			invisible[3] = invisible[5];
			invisible[5] = t;

			if (flipU != null) {
				t = flipU[2];
				flipU[2] = flipU[4];
				flipU[4] = flipU[3];
				flipU[3] = flipU[5];
				flipU[5] = t;
			}

			if (flipV != null) {
				t = flipV[2];
				flipV[2] = flipV[4];
				flipV[4] = flipV[3];
				flipV[3] = flipV[5];
				flipV[5] = t;
			}
		}

		rotate[TOP] = (rotate[TOP] + rotAdd[numRotations]) & 3;
		rotate[BOTTOM] = (rotate[BOTTOM] + rotAdd[numRotations]) & 3;
		clearCache();
		return this;
	}

	public Box swapIcons(int a, int b) {
		boolean t2 = invisible[a];
		invisible[a] = invisible[b];
		invisible[b] = t2;

		String temp = textureSide[a];
		textureSide[a] = textureSide[b];
		textureSide[b] = temp;

		if (flipU != null) {
			boolean t = flipU[b];
			flipU[b] = flipU[a];
			flipU[a] = t;
		}

		if (flipV != null) {
			boolean t = flipV[b];
			flipV[b] = flipV[a];
			flipV[a] = t;
		}

		if (textureBounds != null) {
			float[] t = textureBounds[b];
			textureBounds[b] = textureBounds[a];
			textureBounds[a] = t;
		}

		return this;
	}

	public Box rotateToSideTex(EnumFacing dir) {
		switch (dir) {
			case DOWN:
				break;
			case UP:
				swapIcons(0, 1);
				rotate[EAST] += 2;
				rotate[WEST] += 2;
				rotate[SOUTH] += 2;
				rotate[NORTH] += 2;
				break;
			case NORTH:
				swapIcons(1, 3);
				swapIcons(0, 2);
				rotate[EAST] += 1;
				rotate[WEST] += 3;
				rotate[TOP] += 2;
				rotate[BOTTOM] += 2;
				break;
			case SOUTH:
				swapIcons(1, 2);
				swapIcons(0, 3);
				rotate[EAST] += 3;
				rotate[WEST] += 1;
				break;
			case WEST:
				swapIcons(1, 5);
				swapIcons(0, 4);
				rotate[SOUTH] += 3;
				rotate[NORTH] += 1;
				rotate[TOP] += 3;
				rotate[BOTTOM] += 3;
				break;
			case EAST:
				swapIcons(1, 4);
				swapIcons(0, 5);
				rotate[SOUTH] += 1;
				rotate[NORTH] += 3;
				rotate[TOP] += 1;
				rotate[BOTTOM] += 1;
				break;
			default:
				break;
		}
		return this;
	}

	@SuppressWarnings("SuspiciousNameCombination")
	public Box rotateToSide(EnumFacing dir) {
		Box prev = this.copy();
		clearCache();
		rotateToSideTex(dir);
		switch (dir) {
			case DOWN:// (0, -1, 0),
				break;

			case UP:// (0, 1, 0)
				minY = 1 - prev.maxY;
				maxY = 1 - prev.minY;
				break;

			case NORTH:// (0, 0, -1),
				minZ = prev.minY;
				maxZ = prev.maxY;
				minY = prev.minX;
				maxY = prev.maxX;
				minX = prev.minZ;
				maxX = prev.maxZ;
				break;

			case SOUTH:// (0, 0, 1),
				minZ = 1 - prev.maxY;
				maxZ = 1 - prev.minY;
				minY = prev.minX;
				maxY = prev.maxX;
				minX = 1 - prev.maxZ;
				maxX = 1 - prev.minZ;
				break;

			case WEST:// (-1, 0, 0),
				minX = prev.minY;
				maxX = prev.maxY;
				minY = prev.minX;
				maxY = prev.maxX;
				minZ = 1 - prev.maxZ;
				maxZ = 1 - prev.minZ;
				break;

			case EAST:// (1, 0, 0),
				minX = 1 - prev.maxY;
				maxX = 1 - prev.minY;
				minY = prev.minX;
				maxY = prev.maxX;
				break;

			default:
				break;
		}

		return this;
	}

	public Box setTexture(String tex) {
		texture = tex;
		return this;
	}

	public Box setTextureSides(Object... tex) {
		int s = -1;

		for (Object aTex : tex) {
			if (aTex instanceof Integer) {
				s = (Integer) aTex;
			}
			if (aTex instanceof EnumFacing) {
				s = ((EnumFacing) aTex).getIndex();
			} else if (aTex instanceof String) {
				if (s == -1) {
					texture = (String) aTex;
				} else if (s >= 0 && s < 6) {
					textureSide[s] = (String) aTex;
					s++;
				}
			}
		}

		return this;
	}

	public boolean isFlush(EnumFacing side) {
		switch (side) {
			case DOWN:
				return minY <= 0;
			case UP:
				return maxY >= 1;
			case NORTH:
				return minZ <= 0;
			case SOUTH:
				return maxZ >= 1;
			case WEST:
				return minX <= 0;
			case EAST:
				return maxX >= 1;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTex() {
		if (sprite != null) return sprite;

		if (texture != null && (sprite = Textures.sprites.get(texture)) != null) {
			CachedRenderers.register(this);
			return sprite;
		}
		for (int i = 0; i < 6; i++) {
			if ((sprite = getTextureSide(i)) != null) {
				CachedRenderers.register(this);
				return sprite;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getQuads(@Nullable EnumFacing side) {
		if (side != null && invisible[side.ordinal()]) return null;

		List<BakedQuad>[] cache = this.cachedQuads;
		if (cache == null) {
			cache = new List[7];
			for (EnumFacing face : EnumFacing.values()) {
				cache[face.getIndex()] = offsetQuadList(makeQuads(face), renderOffset);
			}
			cache[6] = offsetQuadList(makeQuads(null), renderOffset);
			if (!ExtraUtils2.deobf_folder)
				this.cachedQuads = cache;
		}
		int i = side == null ? 6 : side.getIndex();
		return cache[i];
	}

	@SideOnly(Side.CLIENT)
	public List<BakedQuad> makeQuads(@Nullable EnumFacing side) {
		if (side == null) return null;
		int index = side.getIndex();

		switch (index) {
			case 0:
			case 1:
				if (minX == maxX) return null;
				if (minZ == maxZ) return null;
				break;
			case 2:
			case 3:
				if (minX == maxX) return null;
				if (minY == maxY) return null;
				break;
			case 4:
			case 5:
				if (minY == maxY) return null;
				if (minZ == maxZ) return null;
				break;
		}

		TextureAtlasSprite textureAtlasSprite = getTextureSide(index);
		if (textureAtlasSprite == null) textureAtlasSprite = Textures.MISSING_SPRITE;

		return makeFlatSideBakedQuad(this, side, textureAtlasSprite, tint, color);
	}

	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTextureSide(int index) {
		String tex = textureSide[index];
		if (tex == null) tex = texture;
		if (tex == null) return null;

		TextureAtlasSprite textureAtlasSprite = Textures.sprites.get(tex);
		if (textureAtlasSprite == null) return null;
		return textureAtlasSprite;
	}

	@SideOnly(Side.CLIENT)
	public float getPos(int i) {
		switch (i) {
			case 0:
				return minX;
			case 1:
				return maxX;
			case 2:
				return minY;
			case 3:
				return maxY;
			case 4:
				return minZ;
			case 5:
				return maxZ;
		}
		throw new RuntimeException(i + " is not valid side");
	}

	public void clearCache() {
		ExtraUtils2.proxy.clearClientCache(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientClear() {
		sprite = null;
		cachedQuads = null;
	}

	public Box setInvisible(boolean... sides) {
		System.arraycopy(sides, 0, invisible, 0, sides.length);
		return this;
	}

	public Box setInvisible(int mask) {
		for (int i = 0; i < invisible.length; i++) {
			invisible[i] = (mask & (1 << i)) != 0;
		}
		return this;
	}

	public List<BakedQuad> addReverseQuads(List<BakedQuad> bakedQuads) {
		if (bakedQuads == null || bakedQuads.isEmpty()) return bakedQuads;

		List<BakedQuad> rev = new ArrayList<>(bakedQuads.size() * 2);
		rev.addAll(bakedQuads);
		for (BakedQuad bakedQuad : bakedQuads) {
			rev.add(QuadHelper.reverse(bakedQuad));
		}

		return rev;
	}

	public Box setTextureBounds(float[][] floats) {
		textureBounds = floats;
		return this;
	}

	public Box setFlipU(int... sides) {
		flipU = new boolean[6];
		for (int side : sides) {
			flipU[side] = true;
		}
		return this;
	}

	public Box setFlipV(int... sides) {
		flipV = new boolean[6];
		for (int side : sides) {
			flipV[side] = true;
		}
		return this;
	}

	public void setRenderOffset(float dx, float dy, float dz) {
		renderOffset = new float[]{dx, dy, dz};
	}

	public void setInvisible(EnumFacing... facing) {
		for (EnumFacing enumFacing : facing) {
			invisible[enumFacing.ordinal()] = true;
		}
	}

	public AxisAlignedBB toAABB() {
		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
