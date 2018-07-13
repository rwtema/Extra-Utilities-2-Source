package com.rwtema.extrautils2.textures;

import com.rwtema.extrautils2.utils.datastructures.IntArrKey;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class ConnectedTexturesHelper {
	public static int[] textureFromArrangement;
	public static boolean[] isAdvancedArrangement;
	public static int[] textureIds;
	static int[] sideA;
	static int[] sideB;
	static int[] corner;
	static int[][] cornerTex;
	static int[] trueTextures;
	static int[][][] texBounds;

	public static void init() {
		sideA = new int[]{1, 4, 4, 1};
		sideB = new int[]{2, 2, 8, 8};
		corner = new int[]{16, 32, 64, 128};
		cornerTex = new int[47][4];
		texBounds = new int[47][][];
		textureFromArrangement = new int[256];
		isAdvancedArrangement = new boolean[16];
		textureIds = new int[47];

		int j = 0;

		boolean[] validTexture = new boolean[625];
		int[] revTextureIds = new int[625];
		int[] k = new int[]{1, 5, 25, 125};

		HashMap<IntArrKey, Integer> texToArrangement = new HashMap<>();

		for (int ar = 0; ar < 256; ar++) {
			int texId = 0;

			int[] t = new int[4];

			for (int i = 0; i < 4; i++) {
				boolean sA = (ar & sideA[i]) != 0;
				boolean sB = (ar & sideB[i]) != 0;
				boolean c = (ar & corner[i]) != 0;
				int tex = getTex(sA, sB, c);
				t[i] = tex;
				texId = texId + (tex * k[i]);
				if (!sA && !sB)
					isAdvancedArrangement[ar & 15] = true;
			}

			if (!validTexture[texId]) {
				texToArrangement.put(new IntArrKey(t), ar);
				textureIds[j] = texId;
				cornerTex[j] = t;
				revTextureIds[texId] = j;
				validTexture[texId] = true;
				j++;
			}

			textureFromArrangement[ar] = revTextureIds[texId];
		}

		Set<Integer> definites = new HashSet<>();
		for (int i = 0; i < 5; i++) {
			definites.add(texToArrangement.get(new IntArrKey(i, i, i, i)));
		}

		// half-anti-corners
		definites.add(texToArrangement.get(new IntArrKey(3, 4, 3, 4)));
		definites.add(texToArrangement.get(new IntArrKey(4, 3, 4, 3)));

		// edges
		definites.add(makeArrangementFull(false, true, true, true));
		definites.add(makeArrangementFull(true, false, true, true));
		definites.add(makeArrangementFull(true, true, false, true));
		definites.add(makeArrangementFull(true, true, true, false));

		// ends
		definites.add(makeArrangementEmpty(true, false, false, false));
		definites.add(makeArrangementEmpty(false, true, false, false));
		definites.add(makeArrangementEmpty(false, false, true, false));
		definites.add(makeArrangementEmpty(false, false, false, true));

		// corners
		definites.add(makeArrangementFull(false, true, false, true));
		definites.add(makeArrangementFull(false, true, true, false));
		definites.add(makeArrangementFull(true, false, false, true));
		definites.add(makeArrangementFull(true, false, true, false));
		definites.add(makeArrangementEmpty(false, true, false, true));
		definites.add(makeArrangementEmpty(false, true, true, false));
		definites.add(makeArrangementEmpty(true, false, false, true));
		definites.add(makeArrangementEmpty(true, false, true, false));


		List<Integer> list = definites.stream().map(i -> textureFromArrangement[i]).collect(Collectors.toList());

		trueTextures = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			trueTextures[i] = list.get(i);
		}

		TIntIntHashMap horizUp = new TIntIntHashMap(16, 0.5F, -1, -1);
		TIntIntHashMap horizDown = new TIntIntHashMap(16, 0.5F, -1, -1);
		int ul = 0, dl = 1, dr = 2, ur = 3;
		for (int trueTexture : trueTextures) {
			int[] tex = cornerTex[trueTexture];
			horizUp.putIfAbsent(tex[ul] * 8 + tex[ur], trueTexture);
			horizDown.putIfAbsent(tex[dl] * 8 + tex[dr], trueTexture);
		}


		for (int i = 0; i < 47; i++) {
			if (list.contains(i)) {
				texBounds[i] = new int[][]{{i, 0, 0, 16, 16}};
			} else {
				int[] tex = cornerTex[i];
				int hu = horizUp.get(tex[ul] * 8 + tex[ur]);
				int hd = horizDown.get(tex[dl] * 8 + tex[dr]);
				if (hu >= 0 && hd >= 0) {
					texBounds[i] = new int[][]{
							{hu, 0, 0, 16, 8},
							{hd, 0, 8, 16, 16}
					};
				} else
					throw new IllegalStateException();
			}
		}

	}

	private static int makeArrangementEmpty(boolean l, boolean r, boolean u, boolean d) {
		return makeArrangement(l, r, u, d, false, false, false, false);
	}


	private static int makeArrangementFull(boolean l, boolean r, boolean u, boolean d) {
		return makeArrangement(l, r, u, d, true, true, true, true);
	}

	private static int makeArrangement(boolean l, boolean r, boolean u, boolean d, boolean ul, boolean ur, boolean dl, boolean dr) {
		int t = 0;
		if (l) t |= 1;
		if (u) t |= 2;

		if (r) t |= 4;
		if (d) t |= 8;

		if (ul) t |= 16;
		if (ur) t |= 32;
		if (dr) t |= 64;
		if (dl) t |= 128;
		return t;
	}

	private static int getTex(boolean sideA, boolean sideB, boolean corner) {
		return sideA ? (sideB ? 0 : 1) : (sideB ? 2 : corner ? 3 : 4);
	}

}
