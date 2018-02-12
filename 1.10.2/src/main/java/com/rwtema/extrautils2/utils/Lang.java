package com.rwtema.extrautils2.utils;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

public class Lang {
	private final static TreeMap<String, String> lang = ExtraUtils2.deobf_folder ? new TreeMap<>() : null;
	private final static HashMap<String, String> textKey = new HashMap<>();
	private final static HashMap<String, String> existingMCLangMap;
	private final static HashMap<String, String> injectingMCLangMap;
	private static final int MAX_KEY_LEN = 32;
	private static final TObjectIntHashMap<String> numRandomEntries = new TObjectIntHashMap<>();
	private static int size = 0;

	static {
		if (ExtraUtils2.deobf_folder && FMLLaunchHandler.side() == Side.CLIENT) {
			try {
				FileInputStream fis = null;
				try {
					File file = getMissedEntriesFile();
					fis = new FileInputStream(file);
					readStream(fis, true);
				} finally {
					if (fis != null)
						fis.close();
				}
			} catch (FileNotFoundException ignore) {

			} catch (IOException e) {
				e.printStackTrace();
			}

			ExtraUtils2.proxy.run(new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					ResourceLocation resourceLocation = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "lang/en_US.lang");
					try {
						IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
						InputStream stream = null;
						try {
							stream = resource.getInputStream();
							readStream(stream, false);
						} finally {
							if (stream != null)
								stream.close();
						}
					} catch (IOException e) {
						throw Throwables.propagate(e);
					}

					createMissedFile();
				}
			});

			LanguageMap instance = ObfuscationReflectionHelper.getPrivateValue(LanguageMap.class, null, "instance");
			existingMCLangMap = ObfuscationReflectionHelper.getPrivateValue(LanguageMap.class, instance, "languageList");
			size = existingMCLangMap.size();
			injectingMCLangMap = Maps.newHashMap();
		} else {
			existingMCLangMap = null;
			injectingMCLangMap = Maps.newHashMap();
		}
	}

	public static void init() {

	}


	public static void readStream(InputStream stream, boolean safe) {
		Map<String, String> langMap = LanguageMap.parseLangFile(stream);
		if (safe) {
			for (Map.Entry<String, String> entry : langMap.entrySet()) {
				String key = makeKey(entry.getValue());
				if (!key.equals(entry.getKey()))
					lang.put(entry.getKey().toLowerCase(Locale.ENGLISH), entry.getValue());
			}
		} else {
			for (Map.Entry<String, String> entry : langMap.entrySet()) {
				lang.put(entry.getKey().toLowerCase(Locale.ENGLISH), entry.getValue());
			}
//			lang.putAll(langMap);
		}
	}

	public static String translate(String text) {
		return translatePrefix(text);
	}

	public static String translatePrefix(String text) {
		String key = getKey(text);
		return translate(key, text);
	}

	public static String getKey(String text) {
		String key = textKey.get(text);
		if (key == null) {
			key = makeKey(text);
			textKey.put(text, key);
			if (ExtraUtils2.deobf_folder) {
				translate(key, text);
			}
		}
		return key;
	}

	private static String makeKey(String text) {
		String key;
		String t = stripText(text);
		key = "extrautils2.text." + t;
		return key;
	}

	@Nonnull
	public static String stripText(String text) {
		String t = text.replaceAll("([^A-Za-z\\s])", "").trim();
		t = t.replaceAll("\\s+", ".").toLowerCase();
		if (t.length() > MAX_KEY_LEN) {
			int n = t.indexOf('.', MAX_KEY_LEN);
			if (n != -1)
				t = t.substring(0, n);
		}
		return t;
	}

	public static String translate(String key, String _default) {
		if (ExtraUtils2.deobf_folder && FMLLaunchHandler.side() == Side.CLIENT) {
			if (!key.equals(key.toLowerCase())) {
				LogHelper.oneTimeInfo("Lang: " + key + " is not lowercased");
			}

			if (size != existingMCLangMap.size()) {
				existingMCLangMap.putAll(injectingMCLangMap);
				size = existingMCLangMap.size();
			}
		}

		if (I18n.canTranslate(key))
			return I18n.translateToLocal(key);
		initKey(key, _default);
		return _default;
	}

	public static String initKey(String key, String _default) {
		if (ExtraUtils2.deobf_folder && FMLLaunchHandler.side() == Side.CLIENT) {
			if (!_default.equals(lang.get(key))) {
				lang.put(key, _default);
				createMissedFile();
			}

			if (!existingMCLangMap.containsKey(key)) {
				injectingMCLangMap.put(key, _default);
				existingMCLangMap.put(key, _default);
			}
		}
		return key;
	}

	public static void createMissedFile() {
		PrintWriter out = null;
		try {
			try {
				File file = getMissedEntriesFile();
				if (file.getParentFile() != null) {
					if (file.getParentFile().mkdirs())
						LogHelper.fine("Making Translation Directory");
				}

				out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				String t = null;
				for (Map.Entry<String, String> entry : lang.entrySet()) {
					int i = entry.getKey().indexOf('.');
					if (i < 0) {
						i = 1;
					}

					String s = entry.getKey().substring(0, i);
					if (t != null) {
						if (!t.equals(s)) {
							out.println("");
						}
					}
					t = s;

					out.println(entry.getKey() + "=" + entry.getValue());
				}
			} finally {
				if (out != null)
					out.close();
			}
			out = null;


			try {
				File file = getNonTrivialFile();
				if (file.getParentFile() != null) {
					if (file.getParentFile().mkdirs())
						LogHelper.fine("Making Translation Directory");
				}

				out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				String t = null;
				for (Map.Entry<String, String> entry : lang.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key.equals(makeKey(value))) {
						continue;
					}

					int i = key.indexOf('.');
					if (i < 0) {
						i = 1;
					}

					String s = key.substring(0, i);
					if (t != null) {
						if (!t.equals(s)) {
							out.println("");
						}
					}
					t = s;

					out.println(key.toLowerCase() + "=" + value);
				}
			} finally {
				if (out != null)
					out.close();
			}


		} catch (Exception err) {
			err.printStackTrace();
		}
	}


	private static File getMissedEntriesFile() {
		return new File(new File(new File("."), "debug_text"), "missed_en_US.lang");
	}

	private static File getNonTrivialFile() {
		return new File(new File(new File("."), "debug_text"), "non_trivial_en_US.lang");
	}

	public static TextComponentTranslation chat(String message, Object... args) {
		String key = getKey(message);
		if (I18n.canTranslate(key))
			return new TextComponentTranslation(key, args);

		return new TextComponentTranslation(message, args);
	}

	public static TextComponentTranslation chat(boolean dummy, String key, String _default, Object... args) {
		return new TextComponentTranslation(translate(key, _default), args);
	}


	public static String translateArgs(boolean dummy, String key, String _default, Object... args) {
		String translate = translate(key, _default);
		try {
			return String.format(translate, args);
		} catch (IllegalFormatException err) {
			throw new RuntimeException("Message: \"" + _default + "\" with key : \"" + key + "\" and translation: \"" + translate + "\"", err);
		}
	}

	public static String translateArgs(String message, Object... args) {
		String translate = Lang.translate(message);
		try {
			return String.format(translate, args);
		} catch (IllegalFormatException err) {
			throw new RuntimeException("Message: \"" + message + "\" with key : \"" + getKey(message) + "\" and translation: \"" + translate + "\"", err);
		}
	}

	public static String getItemName(Block block) {
		return getItemName(new ItemStack(block));
	}

	public static String getItemName(Item item) {
		return getItemName(new ItemStack(item));
	}

	public static String getItemName(ItemStack stack) {
		return stack.getDisplayName();
	}

	public static String random(String key) {
		return random(key, XURandom.rand);
	}

	public static String random(String key, Random rand) {
		int n = getNumSelections(key);
		if (n == 0) {
			return I18n.translateToLocal(key);
		} else {
			return I18n.translateToLocal(key + "." + rand.nextInt(n));
		}
	}

	public static String random(String key, int index) {
		int n = getNumSelections(key);
		int i = Math.abs(index) % n;
		return I18n.translateToLocal(key + "." + i);
	}

	private static int getNumSelections(String key) {
		int i;
		if (numRandomEntries.containsKey(key)) {
			i = numRandomEntries.get(key);
		} else {
			i = 0;
			while (I18n.canTranslate(key + "." + i)) {
				i++;
			}
			i++;
			numRandomEntries.put(key, i);
		}
		return i;
	}
}
