package com.rwtema.extrautils2.utils.client;

import com.google.common.collect.Lists;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

public class GLStateAttributes {
	private static final TIntObjectHashMap<GlStateManager.FogMode> fogEnum;
	private static final TIntObjectHashMap<GlStateManager.CullFace> cullFaceEnum;
	private static final ArrayList<GlStateManager.BooleanState> booleanStates;
	private static  TIntObjectHashMap<String> glAttrib;

	static {
		booleanStates = Lists.newArrayList(
				GlStateManager.alphaState.alphaTest,
				GlStateManager.colorMaterialState.colorMaterial,
				GlStateManager.blendState.blend,
				GlStateManager.depthState.depthTest,
				GlStateManager.fogState.fog,
				GlStateManager.cullState.cullFace,
				GlStateManager.polygonOffsetState.polygonOffsetFill,
				GlStateManager.polygonOffsetState.polygonOffsetLine,
				GlStateManager.colorLogicState.colorLogicOp,
				GlStateManager.rescaleNormalState,
				GlStateManager.normalizeState,
				GlStateManager.lightingState
		);
		Collections.addAll(booleanStates, GlStateManager.lightState);
	}

	static {
		fogEnum = new TIntObjectHashMap<>();
		for (GlStateManager.FogMode fogMode : GlStateManager.FogMode.values()) {
			fogEnum.put(fogMode.capabilityId, fogMode);
		}
		cullFaceEnum = new TIntObjectHashMap<>();
		for (GlStateManager.CullFace face : GlStateManager.CullFace.values()) {
			cullFaceEnum.put(face.mode, face);
		}
	}

	int alphaState_alphaFunc = GlStateManager.alphaState.func;
	float alphaState_alphaRef = GlStateManager.alphaState.ref;
	boolean textureState_enabled = GlStateManager.textureState[GlStateManager.activeTextureUnit].texture2DState.currentState;
	int textureState_name = GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName;
	int colorMaterialState_face = GlStateManager.colorMaterialState.face;
	int colorMaterialState_mode = GlStateManager.colorMaterialState.mode;
	int blendState_srcFactor = GlStateManager.blendState.srcFactor;
	int blendState_dstFactor = GlStateManager.blendState.dstFactor;
	int blendState_srcFactorAlpha = GlStateManager.blendState.srcFactorAlpha;
	int blendState_dstFactorAlpha = GlStateManager.blendState.dstFactorAlpha;
	boolean depthState_maskEnabled = GlStateManager.depthState.maskEnabled;
	int depthState_depthFunc = GlStateManager.depthState.depthFunc;
	int fogState_mode = GlStateManager.fogState.mode;
	float fogState_density = GlStateManager.fogState.density;
	float fogState_start = GlStateManager.fogState.start;
	float fogState_end = GlStateManager.fogState.end;
	int cullState_field_179053_b = GlStateManager.cullState.mode;
	float polygonOffsetState_factor = GlStateManager.polygonOffsetState.factor;
	float polygonOffsetState_units = GlStateManager.polygonOffsetState.units;
	int colorLogicState_field_179196_b = GlStateManager.colorLogicState.opcode;
	double clearState_field_179205_a = GlStateManager.clearState.depth;
	float clearState_field_179203_b_r = GlStateManager.clearState.color.red;
	float clearState_field_179203_b_g = GlStateManager.clearState.color.green;
	float clearState_field_179203_b_b = GlStateManager.clearState.color.blue;
	float clearState_field_179203_b_a = GlStateManager.clearState.color.alpha;
	int activeTextureUnit = GlStateManager.activeTextureUnit;
	int activeShadeModel = GlStateManager.activeShadeModel;
	boolean colorMaskState_r = GlStateManager.colorMaskState.red;
	boolean colorMaskState_g = GlStateManager.colorMaskState.green;
	boolean colorMaskState_b = GlStateManager.colorMaskState.blue;
	boolean colorMaskState_a = GlStateManager.colorMaskState.alpha;
	float r = GlStateManager.colorState.red;
	float g = GlStateManager.colorState.green;
	float b = GlStateManager.colorState.blue;
	float a = GlStateManager.colorState.alpha;
	boolean[] field_texGenState;
	int[] field_texGenCoord;
	int[] field_texGenParam;
	boolean boolStates[];

	{
		field_texGenState = new boolean[4];
		field_texGenCoord = new int[4];
		field_texGenParam = new int[4];

		GlStateManager.TexGen[] values = GlStateManager.TexGen.values();
		for (int i = 0; i < values.length; i++) {
			GlStateManager.TexGenCoord texGenCoord = texGenCoord(values[i]);
			field_texGenState[i] = texGenCoord.textureGen.currentState;
			field_texGenCoord[i] = texGenCoord.coord;
			field_texGenParam[i] = texGenCoord.param;
		}

		boolStates = new boolean[booleanStates.size()];
		for (int i = 0; i < booleanStates.size(); i++) {
			boolStates[i] = booleanStates.get(i).currentState;
		}
	}

	private GLStateAttributes() {

	}

	private static GlStateManager.TexGenCoord texGenCoord(GlStateManager.TexGen p_179125_0_) {
		switch (p_179125_0_) {
			case S:
				return GlStateManager.texGenState.s;
			case T:
				return GlStateManager.texGenState.t;
			case R:
				return GlStateManager.texGenState.r;
			case Q:
				return GlStateManager.texGenState.q;
			default:
				return GlStateManager.texGenState.s;
		}
	}

	public static GLStateAttributes loadStates() {
		return new GLStateAttributes();
	}

	public String getBoolStatesString() {
		if (glAttrib == null) {
			glAttrib = new TIntObjectHashMap<>();
			for (Field field : GL11.class.getFields()) {
				if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
					try {
						int value = field.getInt(null);
						glAttrib.putIfAbsent(value, field.getName());
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		StringBuilder builder = new StringBuilder("{");

		for (int i = 0; i < boolStates.length; i++) {
			if (i > 0) builder.append(", ");
			builder.append(glAttrib.get(booleanStates.get(i).capability));
			builder.append("=");
			builder.append(boolStates[i]);
		}

		return builder.toString();
	}

	public void restore() {
//		if(null == null)
//			return;
		for (int i = 0; i < booleanStates.size(); i++) {
			booleanStates.get(i).setState(boolStates[i]);
		}

		GlStateManager.alphaFunc(alphaState_alphaFunc, alphaState_alphaRef);

		GlStateManager.setActiveTexture(activeTextureUnit + OpenGlHelper.defaultTexUnit);

		if (textureState_enabled)
			GlStateManager.enableTexture2D();
		else
			GlStateManager.disableTexture2D();
		GlStateManager.bindTexture(textureState_name);


		GlStateManager.tryBlendFuncSeparate(blendState_srcFactor, blendState_dstFactor, blendState_srcFactorAlpha, blendState_dstFactorAlpha);


		GlStateManager.depthMask(depthState_maskEnabled);
		GlStateManager.depthFunc(depthState_depthFunc);

		GlStateManager.setFog(fogEnum.get(fogState_mode));
		GlStateManager.setFogDensity(fogState_density);
		GlStateManager.setFogStart(fogState_start);
		GlStateManager.setFogEnd(fogState_end);

		GlStateManager.cullFace(cullFaceEnum.get(cullState_field_179053_b));

		GlStateManager.doPolygonOffset(polygonOffsetState_factor, polygonOffsetState_units);

		GlStateManager.colorLogicOp(colorLogicState_field_179196_b);

		GlStateManager.clearDepth(clearState_field_179205_a);

		GlStateManager.clearColor(clearState_field_179203_b_r, clearState_field_179203_b_g, clearState_field_179203_b_b, clearState_field_179203_b_a);

		GlStateManager.shadeModel(activeShadeModel);

		GlStateManager.colorMask(colorMaskState_r, colorMaskState_g, colorMaskState_b, colorMaskState_a);

		GlStateManager.color(r, g, b, a);

		GlStateManager.colorMaterial(colorMaterialState_face, colorMaterialState_mode);
	}

	@Override
	public String toString() {
		return "GLStateAttributes{" +
				"boolStates=" + getBoolStatesString() +
				", alphaState_alphaFunc=" + alphaState_alphaFunc +
				", alphaState_alphaRef=" + alphaState_alphaRef +
				", textureState_enabled=" + textureState_enabled +
				", textureState_name=" + textureState_name +
				", colorMaterialState_face=" + colorMaterialState_face +
				", colorMaterialState_mode=" + colorMaterialState_mode +
				", blendState_srcFactor=" + blendState_srcFactor +
				", blendState_dstFactor=" + blendState_dstFactor +
				", blendState_srcFactorAlpha=" + blendState_srcFactorAlpha +
				", blendState_dstFactorAlpha=" + blendState_dstFactorAlpha +
				", depthState_maskEnabled=" + depthState_maskEnabled +
				", depthState_depthFunc=" + depthState_depthFunc +
				", fogState_field_179047_b=" + fogState_mode +
				", fogState_field_179048_c=" + fogState_density +
				", fogState_field_179045_d=" + fogState_start +
				", fogState_field_179046_e=" + fogState_end +
				", cullState_field_179053_b=" + cullState_field_179053_b +
				", polygonOffsetState_field_179043_c=" + polygonOffsetState_factor +
				", polygonOffsetState_field_179041_d=" + polygonOffsetState_units +
				", colorLogicState_field_179196_b=" + colorLogicState_field_179196_b +
				", clearState_field_179205_a=" + clearState_field_179205_a +
				", clearState_field_179203_b_r=" + clearState_field_179203_b_r +
				", clearState_field_179203_b_g=" + clearState_field_179203_b_g +
				", clearState_field_179203_b_b=" + clearState_field_179203_b_b +
				", clearState_field_179203_b_a=" + clearState_field_179203_b_a +
				", activeTextureUnit=" + activeTextureUnit +
				", activeShadeModel=" + activeShadeModel +
				", colorMaskState_r=" + colorMaskState_r +
				", colorMaskState_g=" + colorMaskState_g +
				", colorMaskState_b=" + colorMaskState_b +
				", colorMaskState_a=" + colorMaskState_a +
				", r=" + r +
				", g=" + g +
				", y=" + b +
				", x=" + a +
				", field_179067_a=" + Arrays.toString(field_texGenState) +
				", field_179065_b=" + Arrays.toString(field_texGenCoord) +
				", field_179066_c=" + Arrays.toString(field_texGenParam) +

				'}';
	}
}
