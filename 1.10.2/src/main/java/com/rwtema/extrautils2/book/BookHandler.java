package com.rwtema.extrautils2.book;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.entries.Entry;
import com.rwtema.extrautils2.backend.entries.EntryHandler;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.LogHelper;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class BookHandler {
	public static Book book;

	static {
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {
			@Override
			public void onResourceManagerReload(IResourceManager resourceManager) {
				Gson DES = new GsonBuilder().registerTypeAdapter(
						Entry.class, new EntryDeserializer()
				).create();

				Book b;
				try {
					Language currentLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
					IResource resource = null;
					try {
						try {
							resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "lang/book/" + currentLanguage.getLanguageCode().toLowerCase() + ".json"));
						} catch (IOException | NullPointerException err) {
							resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "lang/book/en_us.json"));
						}

						InputStreamReader reader = new InputStreamReader(resource.getInputStream());
						b = DES.fromJson(reader, Book.class);
					} finally {
						if (resource != null)
							resource.close();
					}
				} catch (
						IOException e) {
					e.printStackTrace();
					b = null;
				}

				book = b;

				if (ExtraUtils2.deobf_folder && book != null) {
					HashSet<Entry> entries = Sets.newHashSet(EntryHandler.entries);
					for (Book.Page page : book.pages) {
						entries.remove(page.entry);
					}
					for (Entry entry : entries) {
						LogHelper.info("Missing Book Data: " + entry.name);
					}
					LogHelper.info("Missing Book Count: ", entries.size());
				}
			}
		});
	}

	public static ItemStack newStack() {
		if (book == null) return StackHelper.empty();
		NBTTagCompound tags = new NBTTagCompound();
//		NBTTagList pages = new NBTTagList();
		List<NBTTagString> pages = new ArrayList<>();
		final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		TIntObjectHashMap<String> contents = new TIntObjectHashMap<>();

		for (Book.Page page : book.pages) {

			StringBuilder builder = new StringBuilder();
			String title = null;
			Entry entry = page.entry;
			if (entry != null) {
				title = entry.getDisplayName(page.meta);
			}
			if (page.title != null) {
				title = page.title;
			}

			if (title != null) {
				contents.put(pages.size(), title);
				builder.append(TextFormatting.UNDERLINE).append(title).append(TextFormatting.RESET).append("\n\n");
			}

			builder.append(page.text);
			String string = builder.toString();

			List<String> strings = fontRenderer.listFormattedStringToWidth(string, 116);
			StringBuilder builder1 = new StringBuilder();
			int k = 0;
			for (String s : strings) {
				builder1.append(s).append('\n');
				k++;
				if (k >= 13) {
					pages.add(new NBTTagString(builder1.toString()));
					builder1 = new StringBuilder();
					k = 0;
				}
			}
			if (k != 0)
				pages.add(new NBTTagString(builder1.toString()));
		}

		final NBTTagList pageList = new NBTTagList();
		pageList.appendTag(new NBTTagString("Extra Utilities 2\nManual\n(temp GUI stand in)"));

		final int offset = 3 + (contents.size() / 13);

		int[] keys = contents.keys();
		Arrays.sort(keys);
		StringBuilder builder = new StringBuilder("Table of Contents\n\n");
		int k = 2;
		for (int key : keys) {
			StringBuilder line = new StringBuilder(contents.get(key));
			int a = key + offset;
			while (fontRenderer.listFormattedStringToWidth(line + " " + a, 116).size() > 1) {
				line = new StringBuilder(line.substring(0, line.length() - 1));
			}

			line.append(" ");

			while (fontRenderer.listFormattedStringToWidth(line + " " + a, 116).size() == 1) {
				line.append(" ");
			}

			line.append(a);
			builder.append(line).append('\n');
			k++;
			if (k >= 13) {
				k = 0;
				pageList.appendTag(new NBTTagString(builder.toString()));
				builder = new StringBuilder();
			}
		}

		if (k != 0)
			pageList.appendTag(new NBTTagString(builder.toString()));

		for (NBTTagString page : pages) {
			pageList.appendTag(page);
		}

		tags.setTag("pages", pageList);
		tags.setString("title", book.title);
		tags.setString("author", "RWTema");

		ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
		stack.setTagCompound(tags);
		return stack;
	}

	public static void init() {

	}

	public static class Book {
		public String id;
		public String title;
		public List<Page> pages;

		public static class Page {
			String title;
			@Nullable
			Entry entry;
			String text;
			int meta = 0;
		}
	}

	public static class EntryDeserializer implements JsonDeserializer<Entry> {

		@Override
		public Entry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			String asString = json.getAsString().toLowerCase();
			Entry entry = EntryHandler.entryHashMap.get(asString);
			if (entry != null)
				return entry;

			for (Map.Entry<String, Entry> e : EntryHandler.entryHashMap.entrySet()) {
				String key = e.getKey().toLowerCase();
				if (key.contains(asString) || asString.contains(key)) {
					return e.getValue();
				}
			}

			return null;
		}
	}
}
