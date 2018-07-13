package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.backend.model.UV;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class QuadHelper {

	public static BakedQuad buildBoxQuad(VertexFormat format,
										 float x0, float y0, float z0, float u0, float v0,
										 float x1, float y1, float z1, float u1, float v1,
										 float x2, float y2, float z2, float u2, float v2,
										 float x3, float y3, float z3, float u3, float v3, TextureAtlasSprite texture) {
		Vec3d c = new Vec3d((x2 + x3) / 2, (y2 + y3) / 2, (z2 + z3) / 2);
		Vec3d a = new Vec3d(x0, y0, z0).subtract(c);
		Vec3d b = new Vec3d(x1, y1, z1).subtract(c);
		Vec3d normal = a.crossProduct(b);

		EnumFacing side = EnumFacing.getFacingFromVector((float) normal.x, (float) normal.y, (float) normal.z);
		return buildQuad(format, TRSRTransformation.identity(), side, -1,
				x0, y0, z0, u0, v0,
				x1, y1, z1, u1, v1,
				x2, y2, z2, u2, v2,
				x3, y3, z3, u3, v3, texture);
	}

	public static BakedQuad buildQuad(
			VertexFormat format, TRSRTransformation transform, EnumFacing side, int tint,
			UV uv0, UV uv1, UV uv2, UV uv3,
			int c0, int c1, int c2, int c3,
			TextureAtlasSprite texture) {
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);

		putQuad(format, transform, side, tint,
				uv0.x, uv0.y, uv0.z, texture.getInterpolatedU(16 * uv0.u), texture.getInterpolatedV(16 * uv0.v), c0,
				uv1.x, uv1.y, uv1.z, texture.getInterpolatedU(16 * uv1.u), texture.getInterpolatedV(16 * uv1.v), c1,
				uv2.x, uv2.y, uv2.z, texture.getInterpolatedU(16 * uv2.u), texture.getInterpolatedV(16 * uv2.v), c2,
				uv3.x, uv3.y, uv3.z, texture.getInterpolatedU(16 * uv3.u), texture.getInterpolatedV(16 * uv3.v), c3,
				builder, texture);
		return builder.build();
	}


	public static BakedQuad buildQuad(
			VertexFormat format, TRSRTransformation transform, EnumFacing side, int tint,
			float x0, float y0, float z0, float u0, float v0, int c0,
			float x1, float y1, float z1, float u1, float v1, int c1,
			float x2, float y2, float z2, float u2, float v2, int c2,
			float x3, float y3, float z3, float u3, float v3, int c3, TextureAtlasSprite texture) {
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		putQuad(format, transform, side, tint, x0, y0, z0, u0, v0, c0, x1, y1, z1, u1, v1, c1, x2, y2, z2, u2, v2, c2, x3, y3, z3, u3, v3, c3, builder, texture);
		return builder.build();
	}

	private static void putQuad(VertexFormat format, TRSRTransformation transform, EnumFacing side, int tint,
								float x0, float y0, float z0, float u0, float v0, int c0,
								float x1, float y1, float z1, float u1, float v1, int c1,
								float x2, float y2, float z2, float u2, float v2, int c2,
								float x3, float y3, float z3, float u3, float v3, int c3,
								UnpackedBakedQuad.Builder builder, TextureAtlasSprite texture) {
		builder.setTexture(texture);
		builder.setQuadTint(tint);
		builder.setQuadOrientation(side);
		putVertex(builder, format, transform, side, x0, y0, z0, u0, v0, c0);
		putVertex(builder, format, transform, side, x1, y1, z1, u1, v1, c1);
		putVertex(builder, format, transform, side, x2, y2, z2, u2, v2, c2);
		putVertex(builder, format, transform, side, x3, y3, z3, u3, v3, c3);
	}

	public static BakedQuad applyMatrixTransform(BakedQuad input, Matrix4f rotation) {
		int[] vertexData = Arrays.copyOf(input.getVertexData(), 28);
		Vector4f vec = new Vector4f();
		for (int i = 0; i < 4; i++) {
			vec.x = Float.intBitsToFloat(vertexData[i * 7 + 0]);
			vec.y = Float.intBitsToFloat(vertexData[i * 7 + 1]);
			vec.z = Float.intBitsToFloat(vertexData[i * 7 + 2]);
			vec.w = 1;
			rotation.transform(vec);
			vertexData[i * 7 + 0] = Float.floatToRawIntBits(vec.x);
			vertexData[i * 7 + 1] = Float.floatToRawIntBits(vec.y);
			vertexData[i * 7 + 2] = Float.floatToRawIntBits(vec.z);
		}
		return new BakedQuad(vertexData, input.getTintIndex(), input.getFace(), input.getSprite(), input.shouldApplyDiffuseLighting(), input.getFormat());
	}

	public static void putVertex(UnpackedBakedQuad.Builder builder, VertexFormat format, TRSRTransformation transform, EnumFacing side, float x, float y, float z, float u, float v, int c) {
		Vector4f vec = new Vector4f();
		for (int e = 0; e < format.getElementCount(); e++) {
			switch (format.getElement(e).getUsage()) {
				case POSITION:
					vec.x = x;
					vec.y = y;
					vec.z = z;
					vec.w = 1;
					transform.getMatrix().transform(vec);
					builder.put(e, vec.x, vec.y, vec.z, vec.w);
					break;
				case COLOR:
					builder.put(e, ColorHelper.getR(c) / 255F, ColorHelper.getG(c) / 255F, ColorHelper.getB(c) / 255F, ColorHelper.getA(c) / 255F);
					break;
				case UV:
					if (format.getElement(e).getIndex() == 0) {
						builder.put(e, u, v, 0f, 1f);
						break;
					}
				case NORMAL:
					builder.put(e, (float) side.getFrontOffsetX(), (float) side.getFrontOffsetY(), (float) side.getFrontOffsetZ(), 0f);
					break;
				default:
					builder.put(e);
					break;
			}
		}
	}

	public static TRSRTransformation rotate(float angle, Vector3f axis, TRSRTransformation transform) {
		Matrix4f matrix = transform.getMatrix();
		return new TRSRTransformation(rotate(angle, axis, matrix, matrix));
	}


	public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f src, Matrix4f dest) {
		return rotate(angle, axis.x, axis.y, axis.z, src, dest);
	}

	public static Matrix4f translate(Matrix4f m, float x, float y, float z) {
		m.m03 += x;
		m.m13 += y;
		m.m23 += z;
		return m;
	}

	public static Matrix4f scale(Matrix4f m, float scale) {
		m.m00 *= scale;
		m.m01 *= scale;
		m.m02 *= scale;
		m.m10 *= scale;
		m.m11 *= scale;
		m.m12 *= scale;
		m.m20 *= scale;
		m.m21 *= scale;
		m.m22 *= scale;
		m.m03 *= scale;
		m.m13 *= scale;
		m.m23 *= scale;
		return m;
	}

	public static Matrix4f rotate(float angle, float x_axis, float y_axis, float z_axis, Matrix4f src, Matrix4f dest) {

		if (dest == null)
			dest = new Matrix4f();
		float c = (float) Math.cos(angle);
		float s = (float) Math.sin(angle);
		float oneminusc = 1.0f - c;

		float xy = x_axis * y_axis;
		float yz = y_axis * z_axis;
		float xz = x_axis * z_axis;
		float xs = x_axis * s;
		float ys = y_axis * s;
		float zs = z_axis * s;

		float f00 = x_axis * x_axis * oneminusc + c;
		float f01 = xy * oneminusc + zs;
		float f02 = xz * oneminusc - ys;
		// n[3] not used
		float f10 = xy * oneminusc - zs;
		float f11 = y_axis * y_axis * oneminusc + c;
		float f12 = yz * oneminusc + xs;
		// n[7] not used
		float f20 = xz * oneminusc + ys;
		float f21 = yz * oneminusc - xs;
		float f22 = z_axis * z_axis * oneminusc + c;

		float t00 = src.m00 * f00 + src.m10 * f01 + src.m20 * f02;
		float t01 = src.m01 * f00 + src.m11 * f01 + src.m21 * f02;
		float t02 = src.m02 * f00 + src.m12 * f01 + src.m22 * f02;
		float t03 = src.m03 * f00 + src.m13 * f01 + src.m23 * f02;
		float t10 = src.m00 * f10 + src.m10 * f11 + src.m20 * f12;
		float t11 = src.m01 * f10 + src.m11 * f11 + src.m21 * f12;
		float t12 = src.m02 * f10 + src.m12 * f11 + src.m22 * f12;
		float t13 = src.m03 * f10 + src.m13 * f11 + src.m23 * f12;
		float t20 = src.m00 * f20 + src.m10 * f21 + src.m20 * f22;
		float t21 = src.m01 * f20 + src.m11 * f21 + src.m21 * f22;
		float t22 = src.m02 * f20 + src.m12 * f21 + src.m22 * f22;
		float t23 = src.m03 * f20 + src.m13 * f21 + src.m23 * f22;

		dest.m00 = t00;
		dest.m01 = t01;
		dest.m02 = t02;
		dest.m03 = t03;
		dest.m10 = t10;
		dest.m11 = t11;
		dest.m12 = t12;
		dest.m13 = t13;
		dest.m20 = t20;
		dest.m21 = t21;
		dest.m22 = t22;
		dest.m23 = t23;
		return dest;
	}

	public static BakedQuad buildQuad(VertexFormat format, TRSRTransformation transform, EnumFacing face, int tint,
									  float x0, float y0, float z0, float u0, float v0,
									  float x1, float y1, float z1, float u1, float v1,
									  float x2, float y2, float z2, float u2, float v2,
									  float x3, float y3, float z3, float u3, float v3, TextureAtlasSprite texture) {
		return buildQuad(format, transform, face, tint, x0, y0, z0, u0, v0, x1, y1, z1, u1, v1, x2, y2, z2, u2, v2, x3, y3, z3, u3, v3, -1, texture);
	}

	public static BakedQuad buildQuad(VertexFormat format, TRSRTransformation transform, EnumFacing face, int tint,
									  float x0, float y0, float z0, float u0, float v0,
									  float x1, float y1, float z1, float u1, float v1,
									  float x2, float y2, float z2, float u2, float v2,
									  float x3, float y3, float z3, float u3, float v3, int color, TextureAtlasSprite texture) {
		return buildQuad(format, transform, face, tint, x0, y0, z0, u0, v0, color, x1, y1, z1, u1, v1, color, x2, y2, z2, u2, v2, color, x3, y3, z3, u3, v3, color, texture);
	}

	public static BakedQuad reverse(BakedQuad input) {
		int[] vertexData = input.getVertexData();
		int[] v = new int[28];

		int col;
		if (input.getFace() == EnumFacing.UP)
			col = -8355712;
		else if (input.getFace() == EnumFacing.DOWN)
			col = -1;
		else
			col = 0;

		for (int i = 0; i < 4; i++) {
			System.arraycopy(vertexData, (3 - i) * 7, v, i * 7, 7);
			if (col != 0)
				v[i * 7 + 3] = col;
		}

		return new BakedQuad(v, input.getTintIndex(), input.getFace(), input.getSprite(), input.shouldApplyDiffuseLighting(), input.getFormat());
	}

	public static float getFaceBrightness(float x, float y, float z) {
		float[] norm = LightMathHelper.norm(x, y, z);
		return getFaceBrightnessNorm(norm[0], norm[1], norm[2]);
	}

	public static float getFaceBrightnessNorm(float a, float b, float c) {
		return a * a * 0.6F + b * b * 0.75F + 0.25F * b + c * c * 0.8F;
	}

	public static int getFaceShadeColor(float x, float y, float z) {
		float f = getFaceBrightness(x, y, z);
		int i = MathHelper.clamp((int) (f * 255.0F), 0, 255);
		return -16777216 | i << 16 | i << 8 | i;
	}

	@SideOnly(Side.CLIENT)
	public static BakedQuad createBakedQuad(UV[] vecs, String texture, boolean addShading, int tint) {
		Vector3f a = new Vector3f(), b = new Vector3f(), c = new Vector3f();
		Vector3f.sub(vecs[1].toVector3f(), vecs[0].toVector3f(), a);
		Vector3f.sub(vecs[2].toVector3f(), vecs[0].toVector3f(), b);
		Vector3f.cross(a, b, c);
		EnumFacing facing = EnumFacing.getFacingFromVector(c.x, c.y, c.z);

		TextureAtlasSprite sprite = Textures.sprites.get(texture);
		if (sprite == null) sprite = Textures.MISSING_SPRITE;

		int col = addShading ? getFaceShadeColor(c.x, c.y, c.z) : 0xffffffff;

		int vertex[] = new int[28];

		for (int i = 0; i < 4; i++) {
			vertex[(i * 7)] = Float.floatToRawIntBits(vecs[i].x);
			vertex[i * 7 + 1] = Float.floatToRawIntBits(vecs[i].y);
			vertex[i * 7 + 2] = Float.floatToRawIntBits(vecs[i].z);
			vertex[i * 7 + 3] = col;
			vertex[i * 7 + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(vecs[i].u * 16));
			vertex[i * 7 + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(16 - vecs[i].v * 16));
		}

		ForgeHooksClient.fillNormal(vertex, facing);

		return new BakedQuad(vertex, tint, facing, sprite, false, DefaultVertexFormats.ITEM);
	}


	public static void rotate(float ang, javax.vecmath.Vector3f orbit_axis, Matrix4f mat) {
		rotate(ang, orbit_axis.x, orbit_axis.y, orbit_axis.z, mat, mat);
	}

	public static Collection<? extends BakedQuad> trySplitQuad(BakedQuad quad, TextureAtlasSprite newsprite) {
		TextureAtlasSprite sprite = quad.getSprite();
		if (NullHelper.nullable(sprite) == null) return ImmutableList.of(new BakedQuadRetextured(quad, newsprite));
		int uMax = sprite.getIconWidth();
		int vMax = sprite.getIconHeight();

		VertexFormat format = quad.getFormat();
		int[] pixels = sprite.getFrameTextureData(0)[0];
//		boolean anyTransparent = false;
//		for (int color : pixels) {
//			int alpha = color >> 24 & 0xFF;
//			if (alpha <= 4) {
//				anyTransparent = true;
//				break;
//			}
//		}
//
//		if (!anyTransparent)
//			return ImmutableList.of(new BakedQuadRetextured(quad, newsprite));

		int offset_position = format.getElements().indexOf(DefaultVertexFormats.POSITION_3F);
		int offset_color = format.getElements().indexOf(DefaultVertexFormats.COLOR_4UB);
		int offset_tex = format.getElements().indexOf(DefaultVertexFormats.TEX_2F);
		if (offset_position == -1 || offset_color == -1 || offset_tex == -1) {
			return ImmutableList.of(new BakedQuadRetextured(quad, newsprite));
		}

		offset_position = format.getOffset(offset_position) / 4;
		offset_color = format.getOffset(offset_color) / 4;
		offset_tex = format.getOffset(offset_tex) / 4;

		int[] vertexData = quad.getVertexData();


		UV[] vecs = new UV[4];
		float uh = 1 / (sprite.getMaxU() - sprite.getMinU());
		float vh = 1 / (sprite.getMaxV() - sprite.getMinV());
		for (int i = 0; i < 4; i++) {
			int nextOffset = format.getIntegerSize() * i;
			vecs[i] = new UV(
					Float.intBitsToFloat(vertexData[offset_position + nextOffset]),
					Float.intBitsToFloat(vertexData[offset_position + 1 + nextOffset]),
					Float.intBitsToFloat(vertexData[offset_position + 2 + nextOffset]),
					MathHelper.clamp((Float.intBitsToFloat(vertexData[offset_tex + nextOffset]) - sprite.getMinU()) * uh, 0, 1),
					MathHelper.clamp((Float.intBitsToFloat(vertexData[offset_tex + 1 + nextOffset]) - sprite.getMinV()) * vh, 0, 1)
			);
		}

		float umin = Float.POSITIVE_INFINITY;
		float vmin = Float.POSITIVE_INFINITY;
		float umax = Float.NEGATIVE_INFINITY;
		float vmax = Float.NEGATIVE_INFINITY;
		for (UV vec : vecs) {
			umin = Math.min(umin, vec.u);
			vmin = Math.min(vmin, vec.v);
			umax = Math.max(umax, vec.u);
			vmax = Math.max(vmax, vec.v);
		}

		int u_lower = MathHelper.floor(umin * 16);
		int u_upper = MathHelper.ceil(umax * 16);
		int v_lower = MathHelper.floor(vmin * 16);
		int v_upper = MathHelper.ceil(vmax * 16);

		if ((u_upper - (umax * 16)) < 0.001) umax = u_upper / 16F;
		if ((v_upper - (vmax * 16)) < 0.001) vmax = v_upper / 16F;

		if (u_lower == u_upper || v_lower == v_upper) return ImmutableList.of(new BakedQuadRetextured(quad, newsprite));

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		int i = Math.max(u_upper - u_lower, v_upper - v_lower);

		int u_m = MathHelper.ceil(Math.max(Math.abs(vecs[1].u - vecs[0].u), Math.abs(vecs[1].v - vecs[0].v)) * 16);
		int v_m = MathHelper.ceil(Math.max(Math.abs(vecs[2].u - vecs[3].u), Math.abs(vecs[2].v - vecs[3].v)) * 16);

		for (int du = 0; du < u_m; du++) {
			for (int dv = 0; dv < v_m; dv++) {
				float u0 = (float) du / (float) u_m;
				float v0 = (float) dv / (float) i;
				float u1 = (float) (du + 1) / (float) u_m;
				float v1 = (float) (dv + 1) / (float) v_m;

				UV[] uvs = {
						UV.interpolateQuad(vecs, u0, v0),
						UV.interpolateQuad(vecs, u1, v0),
						UV.interpolateQuad(vecs, u1, v1),
						UV.interpolateQuad(vecs, u0, v1),
				};

				UV center_uv = UV.interpolateQuad(uvs, 0.5F, 0.5F);

				int center_u = MathHelper.clamp(MathHelper.clamp(Math.round(center_uv.u * 16 - 0.05F), u_lower, u_upper), 0, uMax - 1);
				int center_v = MathHelper.clamp(MathHelper.clamp(Math.round(center_uv.v * 16 - 0.05F), v_lower, v_upper), 0, vMax - 1);

				int i1 = center_u + (center_v) * uMax;
				if (i1 < 0 || i1 >= pixels.length) {
					pixels = pixels;
					continue;
				}
				int color = pixels[i1];

				int alpha = color >> 24 & 0xFF;
				if (alpha <= 4) continue;

				int b = (ColorHelper.brightness(color) * 120) / 256 + (256 - 120);
				int new_color = ColorHelper.color(b, b, b, alpha);
//				new_color = 0xffffffff;

				builder.add(
						buildQuad(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), quad.getFace(), quad.getTintIndex(),
								uvs[0], uvs[1], uvs[2], uvs[3],
								new_color, new_color, new_color, new_color,
								newsprite));


			}
		}

		return builder.build();
	}

	public static List<BakedQuad> buildFrontQuads(TextureAtlasSprite sprite, TextureAtlasSprite override) {
		int uMax = sprite.getIconWidth();
		int vMax = sprite.getIconHeight();


		VertexFormat format = DefaultVertexFormats.ITEM;

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		int[] pixels = sprite.getFrameTextureData(0)[0];
		for (int v = 0; v < vMax; v++) {
			for (int u = 0; u < uMax; u++) {
				int color = pixels[u + (vMax - 1 - v) * uMax];
				int alpha = color >> 24 & 0xFF;
				if (alpha == 0) continue;

				float u0 = ((float) u) / uMax;
				float u1 = ((float) u + 1) / uMax;
				float v0 = ((float) v) / vMax;
				float v1 = ((float) v + 1) / vMax;


				float ou0 = override.getInterpolatedU(16 * u0);
				float ou1 = override.getInterpolatedU(16 * u1);
				float ov0 = override.getInterpolatedV(16 * v0);
				float ov1 = override.getInterpolatedV(16 * v1);

				int b = 255;
				if (u != 0 && v != 0 && u != (uMax - 1) && v != (vMax - 1)) {
					lookForTransparent:
					for (int du = -1; du <= 1; du++) {
						for (int dv = -1; dv <= 1; dv++) {
							if (((pixels[(u + du) + (vMax - 1 - v + dv) * uMax] >> 24) & 0xFF) == 0) {
								if (du == 0 || dv == 0) {
									b = 120;
									break lookForTransparent;
								} else {
									b = 200;
								}
							}
						}
					}
				}

				b = (ColorHelper.brightness(color) * 40 + b * 216) / 256;
				color = ColorHelper.color(b, b, b, alpha);

				builder.add(buildQuad(format, TRSRTransformation.identity(), EnumFacing.NORTH, -1,
						u0, v0, 7.5f / 16f, ou0, ov1,
						u1, v0, 7.5f / 16f, ou1, ov1,
						u1, v1, 7.5f / 16f, ou1, ov0,
						u0, v1, 7.5f / 16f, ou0, ov0, color, sprite
				));

				builder.add(buildQuad(format, TRSRTransformation.identity(), EnumFacing.SOUTH, -1,
						u0, v0, 8.5f / 16f, ou0, ov1,
						u0, v1, 8.5f / 16f, ou0, ov0,
						u1, v1, 8.5f / 16f, ou1, ov0,
						u1, v0, 8.5f / 16f, ou1, ov1, color, sprite
				));
			}

		}

		return builder.build();
	}
}
