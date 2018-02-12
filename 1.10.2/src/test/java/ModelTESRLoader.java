import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import gnu.trove.set.hash.TFloatHashSet;
import java.util.HashMap;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

public class ModelTESRLoader {

	static HashMap<Matrix4f, String> names = new HashMap<>();

	public static void init() {
		StringBuilder builder = new StringBuilder();

		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState crafting_table = Blocks.CRAFTING_TABLE.getDefaultState();
		IBakedModel model = dispatcher.getModelForState(crafting_table);
		logMatrices(builder, (IPerspectiveAwareModel) model, "blockTransforms");

		model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(Items.EMERALD));
		logMatrices(builder, (IPerspectiveAwareModel) model, "itemTransforms");

		model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(Items.IRON_PICKAXE));
		logMatrices(builder, (IPerspectiveAwareModel) model, "itemToolsTransforms");


		LogHelper.info(builder.toString());
		LogHelper.info("Fin");
		LogHelper.info(valueSet.toString());
	}

	static TFloatHashSet valueSet = new TFloatHashSet();

	private static String getName(Matrix4f m) {
		Matrix4f mc = copy(m);
		String s = names.get(mc);
		if (s == null) {
			names.put(m, s = CollectionHelper.STRING_DIGITS[names.size()]);
		}
		return s;
	}

	private static Matrix4f copy(Matrix4f m) {
		return new Matrix4f(
				round(m.m00),
				round(m.m01),
				round(m.m02),
				round(m.m03),
				round(m.m10),
				round(m.m11),
				round(m.m12),
				round(m.m13),
				round(m.m20),
				round(m.m21),
				round(m.m22),
				round(m.m23),
				round(m.m30),
				round(m.m31),
				round(m.m32),
				round(m.m33));
	}

	private static float round(float t) {
		return Math.round(t * 4096) / 4096F;
	}

	private static void logMatrices(StringBuilder builder, IPerspectiveAwareModel model, String fieldName) {
		Matrix4f identMatrix = new Matrix4f();
		identMatrix.setIdentity();

		for (ItemCameraTransforms.TransformType transformType : ItemCameraTransforms.TransformType.values()) {
			Matrix4f value = model.handlePerspective(transformType).getValue();
			if (value == null) continue;
			value = copy(value);
			if (!identMatrix.equals(value)) {
				printMatrix(fieldName, transformType, value, builder);
			}
		}
	}

	private static void printMatrix(String fieldName, ItemCameraTransforms.TransformType type, Matrix4f value, StringBuilder builder) {
		valueSet.add(value.m00);
		valueSet.add(value.m01);
		valueSet.add(value.m02);
		valueSet.add(value.m03);
		valueSet.add(value.m10);
		valueSet.add(value.m11);
		valueSet.add(value.m12);
		valueSet.add(value.m13);
		valueSet.add(value.m20);
		valueSet.add(value.m21);
		valueSet.add(value.m22);
		valueSet.add(value.m23);
		valueSet.add(value.m30);
		valueSet.add(value.m31);
		valueSet.add(value.m32);
		valueSet.add(value.m33);

		builder.append(String.format("\n" +
						"%s.put(ItemCameraTransforms.TransformType.%s, makeMatrix(\n" +
						"%s, %s, %s, %s,\n" +
						"%s, %s, %s, %s,\n" +
						"%s, %s, %s, %s,\n" +
						"%s, %s, %s, %s));\n",
				fieldName, type, value.m00, value.m01, value.m02, value.m03, value.m10, value.m11, value.m12, value.m13, value.m20, value.m21, value.m22, value.m23, value.m30, value.m31, value.m32, value.m33));
	}


}
