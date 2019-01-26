import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.api.machine.Machine;
import com.rwtema.extrautils2.api.machine.MachineRegistry;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.datastructures.FieldSetter;
import com.rwtema.extrautils2.utils.datastructures.ObfuscatedKeyMap;
import com.rwtema.extrautils2.utils.helpers.CIELabHelper;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

@Mod(modid = "Test")
public class Test {
	public static List<NeverLoad> t;
	public static Class<?> name;

	static {
		ImmutableMap<String, String> map = ImmutableMap.<String, String>builder()
				.put("t", "hello")
				.put("ta", "hello")
				.put("t2", "hello")
				.put("tab", "hello")
				.build();
		ObfuscatedKeyMap<String> obfuscatedKeyMap = ObfuscatedKeyMap.compile(
				map
				, 1222);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String s = obfuscatedKeyMap.get(entry.getKey());
			if (!entry.getValue().equals(s)) {
				LogHelper.debug("Fail - " + entry.getKey() + " - " + entry.getValue() + " - " + s);
			}
		}

//		int[] test = new int[16 * 16];
//		for (int x = 1; x < 15; x++) {
//			for (int y = 1; y < 15; y++) {
//				test[y * 16 + x] = 0xffffffff;
//			}
//		}
//		int[] edges = XUTConTextureBase.getEdgeDist(test, 16, 16);
		boolean b1 = testFinally();

		int time = 1;
		for (int i1 = 0; i1 < 256; i1++) {
			LogHelper.debug(time + " - " + StringHelper.formatDurationSeconds(time * 20, true));
			time = time * 2;
		}


		Random rand = new Random();
		float d = 0;

		for (int i = 0; i < 1000; i++) {
			float r = rand.nextFloat(), g = rand.nextFloat(), b = rand.nextFloat();
			float[] lab = new float[3], rgb = new float[3];
			CIELabHelper.rgb2lab(r, g, b, lab);
			CIELabHelper.lab2rgb(lab[0], lab[1], lab[2], rgb);

			double sqrt = Math.sqrt((r - rgb[0]) * (r - rgb[0]) + (g - rgb[1]) * (g - rgb[1]) + (b - rgb[2]) * (b - rgb[2]));
			if (sqrt > 0.001) {
				LogHelper.info(t);
				CIELabHelper.rgb2lab(r, g, b, lab);
				CIELabHelper.lab2rgb(lab[0], lab[1], lab[2], rgb);
			}

			d += sqrt;
		}

//		try {
//			name = Class.forName("net.minecraftforge.client.model.ItemLayerModel$BakedItemModel");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}

//		t = new ArrayList<NeverLoad>();
//		for (int i = 0; i < 3000; i++) {
//			String input = "Nineteen";
//			while (input.length() < 31999){
//				input = input.toLowerCase(Locale.ENGLISH) + "#" + new String(salt(StringUtils.getBytesUtf8(input.toUpperCase(Locale.ENGLISH)), new byte[]{1,2,4,8,16,32,64, -128, -1}));
//			}
//
//			byte[] s = StringUtils.getBytesUtf8(input + "#211");
//			s = salt(s, StringUtils.getBytesUtf8("IlikebigbuttsandIcannotlieYouotherbrotherscan'tdenyThatwhenagirlwalksinwithanittybittywaistAndaroundthinginyourfaceYougetsprungWannapulluptough'causeyounoticethatbuttwasstuffedDeepinthejeansshe'swearingI'mhookedandIcan'tstopstaringOh,babyIwannagetwithya"));
//			s = salt(s, StringUtils.getBytesUtf8("iwannaknowwhatloveis"));
//			s = salt(s, new byte[]{0, 1, 1, 8, 9, 9, 9, 8, 8, 1, 9, 9, 9, 1, 1, 9, 7, 2, 5, 3});
//			s = salt(s, new byte[]{
//					-119, 113, 88, 3, -45, 77, -118, -12,
//					28, 101, -78, 6, -71, 56, 87, 96,
//					-46, 100, 72, -99, 117, -101, 114, -32,
//					-16, -21, -68, -74, -31, 63, 88, 119,
//					-98, 79, -88, 121, -30, 93, 53, 42,
//					10, 72, 120, 2, -41, -55, 56, -78,
//					59, 106, 36, 43, -109, -60, -118, -93,
//					121, 117, -13, -44, -115, -35, -96, 48
//			});
//
//			String s1 = new String(s);
//
//			String key = DigestUtils.sha512Hex(s);
//			LogHelper.info(i + " "  + key);
//		}
	}

	private final Object test = null;
	@CapabilityInject(NeverLoad.class)
	public Capability<NeverLoad> cap = null;

	{
		int n = 10000;
		Object k = new Object();
		FieldSetter<Test, Object> fieldSetter = new FieldSetter<>(Test.class, "test");
		for (int i = 0; i < 50; i++) {
			fieldSetter.apply(this, k);
			fieldSetter.apply(this, null);
		}
		long t = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			fieldSetter.apply(this, k);
			fieldSetter.apply(this, null);
		}
		LogHelper.info(System.currentTimeMillis() - t);
		LogHelper.info("");
	}

	static boolean testFinally() {
		try {
			throw new RuntimeException();
		} finally {
			return false;
		}
	}

	static byte[] salt(byte[] key, byte[] salt) {
		int n = key.length;
		byte[] output = new byte[n];
		for (int i = 0; i < n; i++) {
			output[i] = (byte) (key[i] ^ salt[i % salt.length]);
		}
		return output;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
//		LoadBS.loadStates();
		UpdateLang.init();
//
//		float total = 0;
//		for (Machine generator : TileRainbowGenerator.GENERATORS) {
//			float rate = 0;
//			for (IMachineRecipe recipe : generator.recipes_registry) {
//				List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> jeiInputItemExamples = recipe.getJEIInputItemExamples();
//				for (Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>> jeiExample : jeiInputItemExamples) {
//					HashMap<MachineSlotItem, ItemStack> items = new HashMap<>();
//					HashMap<MachineSlotFluid, FluidStack> fluids = new HashMap<>();
//					jeiExample.getLeft().entrySet().forEach(t -> items.put(t.getKey(), t.getValue().get(0)));
//					jeiExample.getRight().entrySet().forEach(t -> fluids.put(t.getKey(), t.getValue().get(0)));
//					if (recipe.matches(items, fluids)) {
//						rate = Math.max(rate, recipe.getEnergyRate(items, fluids));
//					}
//				}
//			}
//			total += rate;
//			LogHelper.info(generator.name + ":" + rate);
//		}
//		LogHelper.info("Total: " + total);
//		LogHelper.info("");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		LanguageMap instance = ObfuscationReflectionHelper.getPrivateValue(LanguageMap.class, null, "instance");
		HashMap<String, String> languageList = ObfuscationReflectionHelper.getPrivateValue(LanguageMap.class, instance, "languageList");

		ObfuscatedKeyMap<String> obfuscatedKeyMap = ObfuscatedKeyMap.compile(languageList, 1222);
		for (Map.Entry<String, String> entry : languageList.entrySet()) {
			String s = obfuscatedKeyMap.get(entry.getKey());
			if (!entry.getValue().equals(s)) {
				LogHelper.debug("Fail - " + entry.getKey() + " - " + entry.getValue() + " - " + s);
			}
		}
//		ModelTESRLoader.init();
//		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(Items.EMERALD));
//		LogHelper.info(model.getClass());
//		LogHelper.info(model.getClass() == name);

	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {


//		JsonBoxModelConvertor.test();
		TreeSet<Machine> machines = new TreeSet<>(new Comparator<Machine>() {
			@Override
			public int compare(Machine o1, Machine o2) {
				return Float.compare(getHue(o1.color), getHue(o2.color));
			}

			private float getHue(int c) {
				return Color.RGBtoHSB(ColorHelper.getR(c), ColorHelper.getG(c), ColorHelper.getB(c), null)[0];
			}
		});
		machines.addAll(MachineRegistry.getMachineValues());
		for (Machine machine : machines) {
			LogHelper.info(machine.name);
		}

	}
}
