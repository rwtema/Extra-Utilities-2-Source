package com.rwtema.extrautils2.backend.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.EnumMap;

@SuppressWarnings("deprecation")
public class Transforms {
	public static EnumMap<ItemCameraTransforms.TransformType, Matrix4f> itemTransforms;
	public static EnumMap<ItemCameraTransforms.TransformType, Matrix4f> itemToolsTransforms;
	public static EnumMap<ItemCameraTransforms.TransformType, Matrix4f> blockTransforms;
	public static EnumMap<ItemCameraTransforms.TransformType, Matrix4f> zeroTransforms;
	public static EnumMap<ItemCameraTransforms.TransformType, Matrix4f> itemBlockTransforms;
	static Matrix4f identity;

	static {
		blockTransforms = new EnumMap<>(ItemCameraTransforms.TransformType.class);
		itemTransforms = new EnumMap<>(ItemCameraTransforms.TransformType.class);
		itemBlockTransforms = new EnumMap<>(ItemCameraTransforms.TransformType.class);
		itemToolsTransforms = new EnumMap<>(ItemCameraTransforms.TransformType.class);
		zeroTransforms = new EnumMap<>(ItemCameraTransforms.TransformType.class);

		blockTransforms.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, makeMatrix(
				0.26513672, 0.0, 0.26513672, 0.0,
				0.25610352, 0.09716797, -0.25610352, 0.15625,
				-0.068603516, 0.3623047, 0.068603516, 0.0,
				0.0, 0.0, 0.0, 1.0));

		blockTransforms.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, makeMatrix(
				0.26513672, 0.0, 0.26513672, 0.0,
				0.25610352, 0.09716797, -0.25610352, 0.15625,
				-0.068603516, 0.3623047, 0.068603516, 0.0,
				0.0, 0.0, 0.0, 1.0));

		blockTransforms.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, makeMatrix(
				-0.28295898, 0.0, -0.28295898, 0.0,
				0.0, 0.39990234, 0.0, 0.0,
				0.28295898, 0.0, -0.28295898, 0.0,
				0.0, 0.0, 0.0, 1.0));

		blockTransforms.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, makeMatrix(
				0.28295898, 0.0, 0.28295898, 0.0,
				0.0, 0.39990234, 0.0, 0.0,
				-0.28295898, 0.0, 0.28295898, 0.0,
				0.0, 0.0, 0.0, 1.0));

		blockTransforms.put(ItemCameraTransforms.TransformType.GUI, makeMatrix(
				-0.44189453, 0.0, -0.44189453, 0.0,
				-0.22094727, 0.54125977, 0.22094727, 0.0,
				0.3828125, 0.3125, -0.3828125, 0.0,
				0.0, 0.0, 0.0, 1.0));

		blockTransforms.put(ItemCameraTransforms.TransformType.GROUND, makeMatrix(
				0.25, 0.0, 0.0, 0.0,
				0.0, 0.25, 0.0, 0.1875,
				0.0, 0.0, 0.25, 0.0,
				0.0, 0.0, 0.0, 1.0));

		blockTransforms.put(ItemCameraTransforms.TransformType.FIXED, makeMatrix(
				0.5, 0.0, 0.0, 0.0,
				0.0, 0.5, 0.0, 0.0,
				0.0, 0.0, 0.5, 0.0,
				0.0, 0.0, 0.0, 1.0));

		itemTransforms.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, makeMatrix(
				0.5500488, 0.0, 0.0, 0.0,
				0.0, 0.5500488, 0.0, 0.1875,
				0.0, 0.0, 0.5500488, 0.0625,
				0.0, 0.0, 0.0, 1.0));

		itemTransforms.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, makeMatrix(
				0.5500488, 0.0, 0.0, 0.0,
				0.0, 0.5500488, 0.0, 0.1875,
				0.0, 0.0, 0.5500488, 0.0625,
				0.0, 0.0, 0.0, 1.0));

		itemTransforms.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, makeMatrix(
				0.0, 0.0, -0.67993164, 0.07055664,
				0.28735352, 0.61621094, 0.0, 0.19995117,
				0.61621094, -0.28735352, 0.0, 0.07055664,
				0.0, 0.0, 0.0, 1.0));

		itemTransforms.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, makeMatrix(
				0.0, 0.0, -0.67993164, 0.07055664,
				0.28735352, 0.61621094, 0.0, 0.19995117,
				0.61621094, -0.28735352, 0.0, 0.07055664,
				0.0, 0.0, 0.0, 1.0));

		itemTransforms.put(ItemCameraTransforms.TransformType.HEAD, makeMatrix(
				-1.0, 0.0, 0.0, 0.0,
				0.0, 1.0, 0.0, 0.8125,
				0.0, 0.0, -1.0, 0.4375,
				0.0, 0.0, 0.0, 1.0));

		itemTransforms.put(ItemCameraTransforms.TransformType.GROUND, makeMatrix(
				0.5, 0.0, 0.0, 0.0,
				0.0, 0.5, 0.0, 0.125,
				0.0, 0.0, 0.5, 0.0,
				0.0, 0.0, 0.0, 1.0));

		itemToolsTransforms.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, makeMatrix(
				0.0, 0.0, 0.85009766, 0.0,
				-0.69628906, 0.48754883, 0.0, 0.25,
				-0.48754883, -0.69628906, 0.0, 0.03125,
				0.0, 0.0, 0.0, 1.0));

		itemToolsTransforms.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, makeMatrix(
				0.0, 0.0, -0.85009766, 0.0,
				0.69628906, 0.48754883, 0.0, 0.25,
				0.48754883, -0.69628906, 0.0, 0.03125,
				0.0, 0.0, 0.0, 1.0));

		itemToolsTransforms.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, makeMatrix(
				0.0, 0.0, 0.67993164, 0.07055664,
				-0.28735352, 0.61621094, 0.0, 0.19995117,
				-0.61621094, -0.28735352, 0.0, 0.07055664,
				0.0, 0.0, 0.0, 1.0));

		itemToolsTransforms.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, makeMatrix(
				0.0, 0.0, -0.67993164, 0.07055664,
				0.28735352, 0.61621094, 0.0, 0.19995117,
				0.61621094, -0.28735352, 0.0, 0.07055664,
				0.0, 0.0, 0.0, 1.0));

		itemToolsTransforms.put(ItemCameraTransforms.TransformType.HEAD, makeMatrix(
				-1.0, 0.0, 0.0, 0.0,
				0.0, 1.0, 0.0, 0.8125,
				0.0, 0.0, -1.0, 0.4375,
				0.0, 0.0, 0.0, 1.0));

		itemToolsTransforms.put(ItemCameraTransforms.TransformType.GROUND, makeMatrix(
				0.5, 0.0, 0.0, 0.0,
				0.0, 0.5, 0.0, 0.125,
				0.0, 0.0, 0.5, 0.0,
				0.0, 0.0, 0.0, 1.0));

		identity = new Matrix4f();
		identity.setIdentity();
	}


	public static EnumMap<ItemCameraTransforms.TransformType, Pair<? extends IBakedModel, Matrix4f>> createMap(IBakedModel model, EnumMap<ItemCameraTransforms.TransformType, Matrix4f> type) {
		Pair<? extends IBakedModel, Matrix4f> base = Pair.of(model, null);
		EnumMap<ItemCameraTransforms.TransformType, Pair<? extends IBakedModel, Matrix4f>> map = new EnumMap<>(ItemCameraTransforms.TransformType.class);
		for (ItemCameraTransforms.TransformType transformType : ItemCameraTransforms.TransformType.values()) {
			Matrix4f matrix4f = type.get(transformType);
			if (matrix4f != null) {
				map.put(transformType, Pair.of(model, matrix4f));
			} else {
				map.put(transformType, base);
			}
		}
		return map;
	}

	static Matrix4f makeMatrix(double... values) {
		float[] v = new float[values.length];
		for (int i = 0; i < v.length; i++) {
			v[i] = (float) values[i];
		}
		return new Matrix4f(v);
	}
}
