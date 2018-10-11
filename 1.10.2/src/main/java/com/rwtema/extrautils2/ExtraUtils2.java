package com.rwtema.extrautils2;

import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.asm.CoreXU2;
import com.rwtema.extrautils2.backend.*;
import com.rwtema.extrautils2.backend.entries.ConfigHelper;
import com.rwtema.extrautils2.backend.entries.EntryHandler;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.entries.XU2EntriesDev;
import com.rwtema.extrautils2.backend.model.ModelHandler;
import com.rwtema.extrautils2.backend.save.SaveManager;
import com.rwtema.extrautils2.banner.Banner;
import com.rwtema.extrautils2.blocks.BlockCreativeChest;
import com.rwtema.extrautils2.book.BookHandler;
import com.rwtema.extrautils2.chunkloading.XUChunkLoaderManager;
import com.rwtema.extrautils2.commands.CommandDebug;
import com.rwtema.extrautils2.commands.CommandPowerSharing;
import com.rwtema.extrautils2.compatibility.CompatFinalHelper;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.MyCreativeTabs;
import com.rwtema.extrautils2.entity.XUEntityManager;
import com.rwtema.extrautils2.gui.ContainerPlayerAlliances;
import com.rwtema.extrautils2.gui.backend.GuiHandler;
import com.rwtema.extrautils2.interblock.FlatTransferNodeHandler;
import com.rwtema.extrautils2.keyhandler.KeyAlt;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.potion.PotionsHelper;
import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.PowerManager;

import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.errors.LegalException;
import com.rwtema.extrautils2.utils.errors.LegalException.LawLevel;
import com.rwtema.extrautils2.utils.helpers.OreDicHelper;
import com.rwtema.extrautils2.worldgen.SingleChunkWorldGenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.net.URL;
import java.util.Random;

@Mod(modid = ExtraUtils2.MODID, version = ExtraUtils2.VERSION,
		dependencies = CompatFinalHelper.DEPENDENCIES,
		acceptedMinecraftVersions = CompatFinalHelper.MC_VERSIONS
)
public class ExtraUtils2 {
	public static final String MODID = "extrautils2";
	public static final String RESOURCE_FOLDER = "extrautils2";
	public static final String VERSION = "1.0";
	public static final boolean deobf;
	public static final boolean deobf_folder;
	public static final Random RANDOM = new Random();
	public static Configuration config;
	public static CreativeTabs creativeTabExtraUtils;
	@SidedProxy(serverSide = "com.rwtema.extrautils2.XUProxyServer", clientSide = "com.rwtema.extrautils2.XUProxyClient")
	public static XUProxy proxy;
	@Mod.Instance(value = ExtraUtils2.MODID)
	public static ExtraUtils2 instance;
	public static String version;

	public static RuntimeException toThrow = null;
	public static boolean allowNonCreativeHarvest = false;
	public static boolean allowNonCreativeConfig = false;

	static {
		boolean d;
		try {
			net.minecraft.world.World.class.getMethod("getBlockState", BlockPos.class);
			d = true;
			LogHelper.info("Dev Enviroment detected. Releasing hounds...");
		} catch (NoSuchMethodException | SecurityException e) {
			d = false;
		}
		deobf = d;


		if (deobf) {
			URL resource = ExtraUtils2.class.getClassLoader().getResource(ExtraUtils2.class.getName().replace('.', '/').concat(".class"));
			deobf_folder = resource != null && "file".equals(resource.getProtocol());
		} else
			deobf_folder = false;

		if (deobf_folder && !CoreXU2.loaded) {
			String message = "ExtraUtilities2 Dev CoreMod Failed To Load";
			message = message + ": Add to VM Options:  \"-Dfml.coreMods.load=" + CoreXU2.class.getName() + "\"";

//			throw new LoaderException(message);
		}

		creativeTabExtraUtils = new MyCreativeTabs();
	}

	static {
		if (System.getProperty("os.name").equals("GovtOS")) {
			throw new LegalException(
					LawLevel.CONSTITUTIONAL,
					"Unconstitutional search method detected. Taking ethical stand.");
		}
	}

	public  static boolean allowCreativeBlocksToBeBroken = false;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		checkThrow();
		ExtraUtils2.version = event.getModMetadata().version;
		NetworkHandler.init(event.getAsmData());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		ModelHandler.init();
		config = new Configuration(event.getSuggestedConfigurationFile());
		allowNonCreativeHarvest = config.getBoolean(ConfigHelper.GAMEPLAY_CATEGORY, "Creative Blocks: Breakable", false, "Allow Non-Creative players to break/harvest creative blocks.");
		allowNonCreativeConfig = config.getBoolean(ConfigHelper.GAMEPLAY_CATEGORY, "Creative Blocks: Configurable", false, "Allow Non-Creative players to configure creative blocks.");

		XU2Entries.init();
		CompatHelper112.loadVersionSpecificEntries();
		if (deobf_folder) {
			//noinspection TrivialFunctionalExpressionUsage,RedundantCast
			(((Runnable) XU2EntriesDev::init)).run();
		}
		XUEntityManager.init();
		EntryHandler.loadModEntries(event.getAsmData());
		EntryHandler.loadConfig(config);
		KeyAlt.isAltSneaking(null);

		EntryHandler.preInit();

		proxy.registerHandlers();
		proxy.registerClientCommand();

		MinecraftForge.EVENT_BUS.register(Freq.INSTANCE);

		XUChunkLoaderManager.init();
		GameRegistry.registerWorldGenerator(SingleChunkWorldGenManager.INSTANCE, 0);
		checkThrow();
	}

	public <R> R test() {
		return null;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		checkThrow();
		OreDicHelper.extendVanillaOre("bricksStone", Blocks.STONEBRICK);
		OreDicHelper.extendVanillaOre("endstone", Blocks.END_STONE);
		OreDicHelper.extendVanillaOre("obsidian", Blocks.OBSIDIAN);

		Banner.init();
		EntryHandler.init();

		proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				BookHandler.init();
			}
		});
		checkThrow();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		checkThrow();
		EntryHandler.postInit();


		proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				Minecraft minecraft = Minecraft.getMinecraft();
				BlockColors blockColors = minecraft.getBlockColors();
				ItemColors itemColors = minecraft.getItemColors();
				for (IRegisterItemColors iRegisterItemColors : Iterables.concat(XUItem.items, XUItemBlock.itemBlocks, XUBlock.blocks)) {
					iRegisterItemColors.addItemColors(itemColors, blockColors);
				}
			}
		});

		ContainerPlayerAlliances.init();
		FlatTransferNodeHandler.INSTANCE.init();
		proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				ClientCommandHandler.instance.registerCommand(new CommandPowerSharing());
			}
		});
		checkThrow();
	}

	@Mod.EventHandler
	public void complete(FMLLoadCompleteEvent event) {
		checkThrow();
		if (config.hasChanged()) {
			config.save();
		}
	}

	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent event) {
		PowerManager.init();
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {


		event.registerServerCommand(new CommandDebug());
		PotionsHelper.serverStart();
	}

	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
		SaveManager.init();
	}

//	@Mod.EventHandler
//	public void serverStoped(FMLServerStoppedEvent event) {
//		PowerManager.instance.clear();
//		XUChunkLoaderManager.clear();
//	}

	@Mod.EventHandler
	public void getIMC(FMLInterModComms.IMCEvent event) {
		for (FMLInterModComms.IMCMessage message : event.getMessages()) {
			IMCHandler.handle(message);
		}
	}

	public void checkThrow() {
		if (toThrow != null) {
			throw toThrow;
		}
	}

//	@Mod.EventHandler
//	public void missingMappings(FMLMissingMappingsEvent event) {
//		for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
//			ResourceLocation resourceLocation = mapping.resourceLocation;
//			if (!ExtraUtils2.MODID.equals(resourceLocation.getResourceDomain().toLowerCase(Locale.ENGLISH))) {
//				continue;
//			}
//			ResourceLocation properLocation = new ResourceLocation(ExtraUtils2.MODID, resourceLocation.getResourcePath().toLowerCase(Locale.ENGLISH));
//			if (mapping.type == GameRegistry.Type.BLOCK) {
//				Block block = BlockEntry.blockMap.get(properLocation);
//				if (block != null) {
//					mapping.remap(block);
//				}
//			} else {
//				Item item = ItemEntry.itemMap.get(properLocation);
//				if (item != null) {
//					mapping.remap(item);
//				}
//			}
//		}
//	}
}
