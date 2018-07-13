package json_gen;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JsonBoxModelConvertor {
	private static final Gson GSON;

	static {
		GsonBuilder gsonbuilder = new GsonBuilder();
		gsonbuilder.registerTypeHierarchyAdapter(BoxModel.class, new BoxModelCon());
		GSON = gsonbuilder.create();
	}

	public static void test() {
		for (XUBlock block : XUBlock.blocks) {
			if (block instanceof XUBlockStatic) {
				for (IBlockState state : block.getBlockState().getValidStates()) {
					BoxModel model = ((XUBlockStatic) block).cachedModels.get(state);
					try {
						String s = GSON.toJson(model);
						LogHelper.info(state + "_" + s);
					} catch (Throwable err) {
						err.printStackTrace();
					}
				}
			}
		}
	}

	public static class BoxModelCon implements JsonSerializer<BoxModel> {
		@Override
		public JsonElement serialize(BoxModel src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject model = new JsonObject();
			model.addProperty("parent", "block/block");
			HashMap<String, String> texturesMap = new HashMap<>();
			JsonArray elements = new JsonArray();

			for (Box b : src) {
				if (b.getClass() != Box.class) continue;
				JsonObject element = new JsonObject();
				element.add("from", toVector(b.minX, b.minY, b.minZ));
				element.add("to", toVector(b.maxX, b.maxY, b.maxZ));

				boolean flag = false;
				JsonObject faces = new JsonObject();
				for (EnumFacing facing : EnumFacing.values()) {
					int index = facing.ordinal();
					if (!b.invisible[index]) {
						JsonObject face = new JsonObject();
						String tex = b.textureSide[index];
						if (tex == null) tex = b.texture;
						if (tex == null) continue;

						String regTex = texturesMap.computeIfAbsent(tex, s -> s.substring(Math.max(0, Math.max(1 + s.lastIndexOf('/'), 1 + s.lastIndexOf(':')))).replace("_", ""));

						face.addProperty("texture", "#" + regTex);

						float[] uv = null;
						if (b.textureBounds != null) {
							uv = b.textureBounds[index];
						}
						if (uv == null) {
							int[] ints = Box.uv[index][0];
							uv = new float[]{
									toMC(Math.min(b.getPos(ints[0]), b.getPos(ints[0] ^ 1))),
									toMC(Math.min(b.getPos(ints[1]), b.getPos(ints[1] ^ 1))),
									toMC(Math.max(b.getPos(ints[0]), b.getPos(ints[0] ^ 1))),
									toMC(Math.max(b.getPos(ints[1]), b.getPos(ints[1] ^ 1)))
							};
						}

						if (b.flipU != null && b.flipU[index]) {
							float t = uv[0];
							uv[0] = uv[2];
							uv[2] = t;
						}
						if (b.flipV != null && b.flipV[index]) {
							float t = uv[1];
							uv[1] = uv[3];
							uv[3] = t;
						}

						face.add("uv", toArray(uv));

						if (b.isFlush(facing)) {
							face.addProperty("cullface", facing.getName());
						}
						int i = b.rotate[index];
						if (i != 0) {
							face.addProperty("rotation", i);
						}

						if (b.tint != -1) {
							face.addProperty("tint", i);
						}
						faces.add(facing.getName(), face);
						flag = true;
					}
				}
				if (!flag) continue;
				element.add("faces", faces);
				elements.add(element);
			}
			model.add("elements", elements);

			JsonObject textures = new JsonObject();

			TextureAtlasSprite tex = src.getTex();
			Optional<String> s = Textures.textureNames.entrySet().stream().filter(stringTextureAtlasSpriteEntry -> stringTextureAtlasSpriteEntry.getValue() == tex).findFirst().map(e -> e.getKey());
			if (s.isPresent()) {
				textures.addProperty("particle", s.get());
			}

			for (Map.Entry<String, String> entry : texturesMap.entrySet()) {
				String texture = entry.getKey();
				String name = texture.indexOf(':') == -1 ? ExtraUtils2.RESOURCE_FOLDER + ":" + texture : texture;
				textures.addProperty(entry.getValue(), name);
			}
			model.add("textures", textures);

			return model;
		}

		public float toMC(float coordinate) {
			return coordinate * 16;
		}

		public JsonArray toArray(float... f) {
			JsonArray array = new JsonArray();
			for (float v : f) {
				array.add(new JsonPrimitive(v));
			}
			return array;
		}

		public JsonArray toVector(float x, float y, float z) {
			JsonArray array = new JsonArray();
			array.add(new JsonPrimitive(toMC(x)));
			array.add(new JsonPrimitive(toMC(y)));
			array.add(new JsonPrimitive(toMC(z)));
			return array;
		}
	}
}
