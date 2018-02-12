package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.*;
import com.rwtema.extrautils2.textures.ConnectedTexturesHelper;
import com.rwtema.extrautils2.textures.TextureComponent;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class Textures {
	public static final HashMap<String, TextureAtlasSprite> textureNames = Maps.newHashMap();
	public static final HashMap<TextureAtlasSprite, List<TextureComponent>> simpleCompositeTextureCache = new HashMap<>();
	public static final Function<TextureAtlasSprite, List<TextureComponent>> simpleCompostiteFunction = textureAtlasSprite -> ImmutableList.of(new TextureComponent(textureAtlasSprite, 0, 0, 16, 16));
	private static final HashMap<String, Supplier<TextureAtlasSprite>> texCreators = new HashMap<>();

	public static HashBiMap<String, TextureAtlasSprite> sprites;
	public static BiMap<TextureAtlasSprite, String> spritesInverse;
	public static TextureAtlasSprite MISSING_SPRITE;

	static {
		MinecraftForge.EVENT_BUS.register(new Textures());
	}

	ArrayList<ModelResourceLocation> modelLocations = null;

	public static ModelResourceLocation getModelResourceLocation(Item item) {
		return new ModelResourceLocation(
				Validate.notNull(Item.REGISTRY.getNameForObject(item))
				, "inventory");
	}

	public static ModelResourceLocation getModelResourceLocation(Item item, int metadata, IXUItem xuItem) {
		if (metadata == OreDictionary.WILDCARD_VALUE) return getModelResourceLocation(item);
		return new ModelResourceLocation(Item.REGISTRY.getNameForObject(item) + "_" + xuItem.getModelSubName(metadata), "inventory");
	}

	public static void register(String... textures) {
		for (String texture : textures) {
			if (texture != null)
				textureNames.put(texture, null);
		}
	}

	public static ResourceLocation completeTextureResourceLocation(ResourceLocation location) {
		return new ResourceLocation(location.getResourceDomain(), String.format("%s/%s%s", "textures", location.getResourcePath(), ".png"));
	}

	public static TextureAtlasSprite getSprite(String textureName) {
		TextureAtlasSprite sprite = sprites.get(textureName);
		return sprite != null ? sprite : MISSING_SPRITE;
	}

	public static void registerSupplier(String s, Supplier<TextureAtlasSprite> creator) {
		texCreators.put(s, creator);
	}

	@SubscribeEvent
	public void loadTextures(TextureStitchEvent.Pre event) {
		ConnectedTexturesHelper.init();
		MISSING_SPRITE = event.getMap().getMissingSprite();

		simpleCompositeTextureCache.clear();

		for (IXUItem item : XUItem.items) {
			item.clearCaches();
			item.registerTextures();
		}
		for (XUItemBlock itemBlock : XUItemBlock.itemBlocks) {
			itemBlock.clearCaches();
			itemBlock.registerTextures();
		}
		for (XUBlock block : XUBlock.blocks) {
			block.clearCaches();
			block.registerTextures();
		}

		texCreators.forEach((s, t) -> textureNames.put(s, t.get()));

		sprites = HashBiMap.create();
		spritesInverse = sprites.inverse();
		TextureMap map = event.getMap();
		sprites.put(Box.MISSING_TEXTURE, MISSING_SPRITE);
		for (Map.Entry<String, TextureAtlasSprite> entry : textureNames.entrySet()) {
			String texture = entry.getKey();
			String name = texture.indexOf(':') == -1 ? ExtraUtils2.RESOURCE_FOLDER + ":" + texture : texture;
			TextureAtlasSprite value = entry.getValue();
			if (value != null) {
				map.setTextureEntry(value);
				sprites.put(texture, value);
			} else
				sprites.put(texture, map.registerSprite(new ResourceLocation(name)));
		}


		for (IXUItem item : XUItem.items) {
			item.postTextureRegister();
		}
		for (XUItemBlock itemBlock : XUItemBlock.itemBlocks) {
			itemBlock.postTextureRegister();
		}
		for (XUBlock block : XUBlock.blocks) {
			block.postTextureRegister();
		}
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void registerModels(ModelBakeEvent event) {

		IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		IBakedModel missingModel = event.getModelManager().getMissingModel();

		if (ExtraUtils2.deobf_folder) {
			modelLocations = new ArrayList<>();
		}

		for (IXUItem xuItems : XUItem.items) {
			Item item = (Item) xuItems;
			int maxMetadata = xuItems.getMaxMetadata();
			if (maxMetadata == 0) {
				ModelLoader.setCustomMeshDefinition(item, ItemMesher.INSTANCE);
				ModelResourceLocation modelResourceLocation = Textures.getModelResourceLocation(item);
				ModelLoader.setCustomModelResourceLocation(item, 0, modelResourceLocation);
				if (!xuItems.allowOverride() || checkMissingModelLocation(modelRegistry, missingModel, modelResourceLocation))
					modelRegistry.putObject(modelResourceLocation, xuItems.createModel(0));
			} else {
				ModelLoader.setCustomMeshDefinition(item, ItemMesher.INSTANCE);
				ModelResourceLocation genericLocation = Textures.getModelResourceLocation(item);
				if (!xuItems.allowOverride() || checkMissingModelLocation(modelRegistry, missingModel, genericLocation))
					modelRegistry.putObject(genericLocation, xuItems.createModel(0));


				for (int i = 0; i <= maxMetadata; i++) {
					ModelLoader.setCustomMeshDefinition(item, new ItemMesherItem(xuItems));
					ModelResourceLocation modelResourceLocation = Textures.getModelResourceLocation(item, i, xuItems);
					ModelLoader.setCustomModelResourceLocation(item, i, modelResourceLocation);

					if (!xuItems.allowOverride() || checkMissingModelLocation(modelRegistry, missingModel, modelResourceLocation))
						modelRegistry.putObject(modelResourceLocation, xuItems.createModel(i));
				}
			}
		}

		for (XUItemBlock item : XUItemBlock.itemBlocks) {
			ModelLoader.setCustomMeshDefinition(item, ItemMesher.INSTANCE);
			final ModelResourceLocation location = Textures.getModelResourceLocation(item);
			ModelLoader.setCustomStateMapper(item.block, new StateMapperBase() {
				@Nonnull
				@Override
				protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
					return location;
				}
			});

			if (item.block.allowOverride() && !checkMissingModelLocation(modelRegistry, missingModel, location))
				continue;
			modelRegistry.putObject(location, item.block.createItemBlockPassThruModel( item));
		}


		for (XUBlock block : XUBlock.blocks) {
//			ModelLoader.setCustomStateMapper(block, createStateMapper());
			event.getModelManager().getBlockModelShapes().registerBlockWithStateMapper(block, createStateMapper());
//			event.getModelManager().getBlockModelShapes().getBlockStateMapper().registerBlockStateMapper(block, XUStateMapper.INSTANCE);
			for (Map.Entry<IBlockState, ModelResourceLocation> entry : createStateMapper().putStateModelLocations(block).entrySet()) {
				ModelResourceLocation location = entry.getValue();
				if (block.allowOverride() && !checkMissingModelLocation(modelRegistry, missingModel, location))
					continue;
				PassthruModelBlock passthruModelBlock = block.createPassthruModel(entry.getKey(), location);
				modelRegistry.putObject(location, passthruModelBlock);
			}
		}

		if (ExtraUtils2.deobf_folder) {
			modelLocations.sort(Comparator.comparing(ModelResourceLocation::toString));

			File file = new File(new File(new File("."), "debug_text"), "debug_model_locations.txt");
			if (file.getParentFile() != null) {
				if (file.getParentFile().mkdirs())
					LogHelper.fine("Making Debug Text Directory");
			}

			try {
				try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
					out.println("# Extra Utilities uses a built-in model system. However if you create json models for the following, they will be used instead.");
					out.println();
					out.println("## Blocks");
					HashSet<XUItemBlock> notedItemBlocks = new HashSet<>(XUItemBlock.itemBlocks);
					notedItemBlocks.removeIf(item -> !(item.block.allowOverride()));
					for (XUBlock block : XUBlock.blocks) {
						if (block.allowOverride()) {
							ResourceLocation nameForObject = Block.REGISTRY.getNameForObject(block);
							out.println("# " + new ItemStack(block).getDisplayName());
							out.println("blockstates/" + nameForObject.getResourcePath() + ".json");
							out.println("variants={");
							for (String s : createStateMapper().putStateModelLocations(block).values().stream().map(ModelResourceLocation::getVariant).sorted().collect(Collectors.toSet())) {
								out.println("\t" + s);
							}
							out.println("}");
							out.println("");
							Item itemFromBlock = Item.getItemFromBlock(block);
							if (itemFromBlock instanceof XUItemBlock) {
								out.println("extrautils2/models/item/" + nameForObject.getResourcePath() + ".json");
								notedItemBlocks.remove(itemFromBlock);
							}

							out.println("");
						}
					}


					if (!notedItemBlocks.isEmpty()) {
						out.println();
						out.println("## Additional Item Blocks");
						for (XUItemBlock item : notedItemBlocks) {
							if (item.block.allowOverride()) {
								ResourceLocation nameForObject = Block.REGISTRY.getNameForObject(item.block);
								out.println("# " + new ItemStack(item.block).getDisplayName());
								out.println("models/item/" + nameForObject.getResourcePath() + ".json");
								out.println("");
							}
						}
					}

					out.println();
					out.println("## Items");
					for (IXUItem xuItems : XUItem.items) {
						if (!xuItems.allowOverride()) continue;

						Item item = (Item) xuItems;
						int maxMetadata = xuItems.getMaxMetadata();
						if (maxMetadata == 0) {
							ModelResourceLocation modelResourceLocation = Textures.getModelResourceLocation(item);
							out.println("# " + new ItemStack(item).getDisplayName());
							out.println("models/item/" + modelResourceLocation.getResourcePath() + ".json");
							out.println("");
						} else {
							for (int i = 0; i <= maxMetadata; i++) {
								ModelResourceLocation modelResourceLocation = Textures.getModelResourceLocation(item, i, xuItems);
								out.println("# " + new ItemStack(item, 1, i).getDisplayName());
								out.println("models/item/" + modelResourceLocation.getResourcePath() + ".json");
								out.println("");
							}
						}
					}


				}
			} catch (IOException err) {
				err.printStackTrace();
			}


		}
	}

	@Nonnull
	private DefaultStateMapper createStateMapper() {
		return new DefaultStateMapper()
		{
			@Nonnull
			@Override
			public String getPropertyString(Map<IProperty<?>, Comparable<?>> values) {
				StringBuilder stringbuilder = new StringBuilder();

				for (Map.Entry<IProperty<?>, Comparable<?>> entry : values.entrySet()) {
					IProperty<?> iproperty = entry.getKey();

					if(iproperty instanceof IMetaProperty) continue;

					if (stringbuilder.length() != 0) {
						stringbuilder.append(",");
					}

					stringbuilder.append(iproperty.getName());
					stringbuilder.append("=");
					stringbuilder.append(this.subGetPropName(iproperty, entry.getValue()));
				}

				if (stringbuilder.length() == 0) {
					stringbuilder.append("normal");
				}

				return stringbuilder.toString();
			}

			private <T extends Comparable<T>> String subGetPropName(IProperty<T> property, Comparable<?> value) {
				return property.getName((T) value);
			}
		};
	}

	public boolean checkMissingModelLocation(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, IBakedModel missingModel, ModelResourceLocation modelResourceLocation) {
		if (ExtraUtils2.deobf_folder) {
			if (!modelLocations.contains(modelResourceLocation)) {
				modelLocations.add(modelResourceLocation);
			} else {
//				LogHelper.info(modelResourceLocation);
			}
		}
		return isMissingModel(modelRegistry.getObject(modelResourceLocation), missingModel);
	}

	public boolean isMissingModel(IBakedModel model, IBakedModel missingModel) {
		if (model == null) return true;
		if (model == missingModel) return true;
		if (missingModel.equals(model)) return true;
		if (model instanceof PassthruModelBlock || model instanceof PassthruModelItem || model instanceof PassthruModelItemBlock)
			return true;
		if ("net.minecraftforge.client.model.FancyMissingModel$BakedModel".equals(model.getClass().getName())) {
			return true;
		}
		return false;
	}

	public static class XUStateMapper extends DefaultStateMapper {
		static final XUStateMapper INSTANCE = new XUStateMapper();

		@Nonnull
		@Override
		public String getPropertyString(Map<IProperty<?>, Comparable<?>> values) {
			HashMap<IProperty<?>, Comparable<?>> map = new HashMap<>(values);
			for (Iterator<Map.Entry<IProperty<?>, Comparable<?>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
				Map.Entry<IProperty<?>, Comparable<?>> entry = iterator.next();
				IProperty<?> key = entry.getKey();
				if (key instanceof IMetaProperty && !((IMetaProperty) key).isVisible()) {
					iterator.remove();
				}
			}


			return super.getPropertyString(map);
		}
	}

	public static class ItemMesher implements ItemMeshDefinition {
		public final static ItemMesher INSTANCE = new ItemMesher();

		@Nonnull
		@Override
		public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack) {
			return getModelResourceLocation(stack.getItem());
		}
	}

	public static class ItemMesherItem implements ItemMeshDefinition {
		final IXUItem item;

		public ItemMesherItem(IXUItem item) {
			this.item = item;
		}

		@Nonnull
		@Override
		public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack) {
			int meta = stack.getItemDamage();
			if (meta < 0)
				meta = 0;
			else if (meta > item.getMaxMetadata())
				meta = item.getMaxMetadata();
			return getModelResourceLocation(stack.getItem(), meta, item);
		}
	}
}
