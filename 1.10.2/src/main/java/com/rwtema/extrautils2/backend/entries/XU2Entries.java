package com.rwtema.extrautils2.backend.entries;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.achievements.AchievementHelper;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.blocks.*;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.XUShapedRecipe;
import com.rwtema.extrautils2.crafting.*;
import com.rwtema.extrautils2.dimensions.DimensionEntry;
import com.rwtema.extrautils2.dimensions.WorldWall;
import com.rwtema.extrautils2.dimensions.deep_dark.TeleporterDeepDark;
import com.rwtema.extrautils2.dimensions.deep_dark.WorldProviderDeepDark;
import com.rwtema.extrautils2.dimensions.workhousedim.WorldProviderSpecialDim;
import com.rwtema.extrautils2.entity.EntityBoomerang;
import com.rwtema.extrautils2.eventhandlers.ProddingStickHandler;
import com.rwtema.extrautils2.eventhandlers.RareSeedHandler;
import com.rwtema.extrautils2.eventhandlers.SquidSpawnRestrictions;
import com.rwtema.extrautils2.items.*;
import com.rwtema.extrautils2.machine.*;
import com.rwtema.extrautils2.potion.*;
import com.rwtema.extrautils2.power.energy.BlockPowerBattery;
import com.rwtema.extrautils2.power.energy.BlockPowerTransmitter;
import com.rwtema.extrautils2.power.energy.TilePowerBattery;
import com.rwtema.extrautils2.power.energy.TilePowerTransmitter;
import com.rwtema.extrautils2.quarry.BlockQuarry;
import com.rwtema.extrautils2.quarry.BlockQuarryProxy;
import com.rwtema.extrautils2.quarry.TileQuarry;
import com.rwtema.extrautils2.quarry.TileQuarryProxy;
import com.rwtema.extrautils2.structure.PatternRecipe;
import com.rwtema.extrautils2.tile.*;
import com.rwtema.extrautils2.transfernodes.*;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.villagers.EmeraldForPotions;
import com.rwtema.extrautils2.villagers.EntityAINinjaPoof;
import com.rwtema.extrautils2.villagers.GenericTrade;
import com.rwtema.extrautils2.villagers.XUVillagerCareer;
import com.rwtema.extrautils2.worldgen.SingleChunkGen;
import com.rwtema.extrautils2.worldgen.SingleChunkWorldGenManager;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingOreRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

import static com.rwtema.extrautils2.transfernodes.GrocketType.TRANSFER_NODE_ITEMS;

@SuppressWarnings({"unchecked", "unused"})
public class XU2Entries {
	public static final IItemStackMaker magical_wood = BlockDecorativeSolidWood.DecorStates.magical_wood;
	public static final IItemStackMaker redstoneCrystal = ItemIngredients.Type.REDSTONE_CRYSTAL;

	public static final IItemStackMaker stoneburnt = BlockDecorativeSolid.DecorStates.stoneburnt;
	public static final ItemClassEntry<ItemSnowglobe> snowGlobe = new ItemClassEntry<ItemSnowglobe>(ItemSnowglobe.class) {
		@Override
		public void addRecipes() {
			OreDictionary.registerOre("doorWood", Items.OAK_DOOR);
			OreDictionary.registerOre("doorWood", Items.ACACIA_DOOR);
			OreDictionary.registerOre("doorWood", Items.JUNGLE_DOOR);
			OreDictionary.registerOre("doorWood", Items.SPRUCE_DOOR);
			OreDictionary.registerOre("doorWood", Items.DARK_OAK_DOOR);
			OreDictionary.registerOre("doorWood", Items.BIRCH_DOOR);
			OreDictionary.registerOre("doorIron", Items.IRON_DOOR);

			addShapeless("snow_globe", newStack(),
					"blockGlass", "treeSapling", Items.SNOWBALL,
					"doorWood", "logWood", "grass",
					Items.ENDER_PEARL, "netherStar");
		}

		@Override
		public void registerOres() {
			OreDictionary.registerOre("magic_snow_globe", newStack(1, 1));
		}
	};
	public static ItemEntry<ItemWateringCan> wateringCan = new ItemClassEntry<ItemWateringCan>(ItemWateringCan.class) {
		@Override
		public void addRecipes() {
			addShaped("watering_can", newStackLowestDamage(), "S  ", "SBS", " S ", 'S', Blocks.STONE, 'B', Items.BOWL);
		}
	};
	public static ItemEntry<ItemDestructionWand> creativeDestructionWand = new ItemEntry<ItemDestructionWand>("ItemCreativeDestructionWand") {
		@Override
		public ItemDestructionWand initValue() {
			return new ItemDestructionWand(name.toLowerCase().substring(4), name, new float[]{191 / 255F, 75 / 255F, 244 / 255F}, 49);
		}
	};
	public static ItemEntry<ItemUnstableIngots> unstableIngots = new ItemClassEntry<ItemUnstableIngots>(ItemUnstableIngots.class) {
		@Override
		public void loadAdditionalConfig(Configuration config) {
			ItemUnstableIngots.TIME_OUT = config.getInt(ConfigHelper.GAMEPLAY_CATEGORY, "Unstable Ingot: Explosion Time", 10 * 20, 0, 120 * 20, "Choose the time until explosion.");
			for (String clazzName : config.get(ConfigHelper.GAMEPLAY_CATEGORY, "Unstable Ingot: Allowed Classes", new String[]{ContainerWorkbench.class.getName()}, "Choose allowed container classnames.", false, -1, null).getStringList()) {
				Class<?> aClass;
				try {
					aClass = Class.forName(clazzName);
				} catch (ClassNotFoundException ignore) {
					continue;
				}

				ItemUnstableIngots.ALLOWED_CLASSES.add(aClass);
			}
		}

		@Override
		public void registerOres() {
			OreDictionary.registerOre("ingotUnstable", newStack(1, 0));
			OreDictionary.registerOre("ingotUnstable", newStack(1, 2));
			OreDictionary.registerOre("nuggetUnstable", newStack(1, 1));
		}

		@Override
		public void addRecipes() {
			value.addRecipes();
		}
	};
	public static ItemEntry<ItemBuildersWand> buildersWand = new ItemEntry<ItemBuildersWand>("ItemBuildersWand") {
		@Override
		public ItemBuildersWand initValue() {
			return new ItemBuildersWand(name, 9, name.toLowerCase().substring(4), new float[]{244 / 255F, 230 / 255F, 78 / 255F});
		}

		@Override
		public void addRecipes() {
			CraftingHelper.addShaped("builders_wand", newStack(), "  G", " W ", "W  ", 'W', magical_wood, 'G', "ingotGold");
		}

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Builders Wand", "Places blocks on blocks", this, magical_wood);
		}
	};
	public static ItemEntry<ItemDestructionWand> destructionWand = new ItemEntry<ItemDestructionWand>("ItemDestructionWand") {
		@Override
		public ItemDestructionWand initValue() {
			return new ItemDestructionWand(name.toLowerCase().substring(4), name, new float[]{244 / 255F, 230 / 255F, 78 / 255F}, 9);
		}

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Destruction Wand", "Breaks blocks", this, buildersWand);
		}


		@Override
		public void addRecipes() {
			CraftingHelper.addShaped("destruction_wand", newStack(), " GG", " WG", "W  ", 'W', magical_wood, 'G', "ingotGold");
		}
	};
	public static ItemEntry<ItemBuildersWand> creativeBuildersWand = new ItemEntry<ItemBuildersWand>("ItemCreativeBuildersWand") {
		@Override
		public ItemBuildersWand initValue() {
			return new ItemBuildersWand(name, 49, "creativebuilderswand", new float[]{191 / 255F, 75 / 255F, 244 / 255F});
		}
	};
	public static BlockClassEntry<BlockSoundMuffler> soundMuffler = new BlockClassEntry<BlockSoundMuffler>(BlockSoundMuffler.class, TileSoundMuffler.class) {
		@Override
		public void addRecipes() {
			addShapeless("sound_muffler", value, Blocks.WOOL, Blocks.NOTEBLOCK);
		}
	};
	public static BlockEntry<BlockEnderLilly> blockEnderLilly = new BlockClassEntry<BlockEnderLilly>(BlockEnderLilly.class) {
		final IBlockState target = Blocks.END_STONE.getDefaultState();

		@Override
		public void postInit() {

			RareSeedHandler.register(newStack(1), 0.0078125f);
//			DungeonHelper.addDungeonItem(newStack(5), 1, 3, ChestGenHooks.DUNGEON_CHEST, 0.03);
			SingleChunkWorldGenManager.register(new SingleChunkGen("EnderLillies", 0) {
				@Override
				public void genChunk(Chunk chunk, Object provider, Random random) {
					if (!(CompatHelper112.isChunkProviderEnd(provider)))
						return;

					BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

					int dx = random.nextInt(16);
					int dz = random.nextInt(16);
					int dy = chunk.getHeightValue(dx, dz) - 1;
					if (dy <= 0) return;
					pos.setPos(dx, dy, dz);
					IBlockState blockState = chunk.getBlockState(pos);
					if (blockState == target) {
						pos.setPos(dx, dy + 1, dz);
						if (isAir(chunk, pos)) {
							setBlockState(chunk, pos, value.FULLY_GROWN_STATE);
						}
					}

				}
			});
		}
	}.setItemClass(ItemBlockPlantable.class);
	public static BlockEntry<BlockDecorativeSolidWood> decorativeSolidWood = new BlockClassEntry<BlockDecorativeSolidWood>(BlockDecorativeSolidWood.class) {
		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Magical Wood", "*snicker*", magical_wood, null);
		}

		@Override
		public void addRecipes() {
			for (BlockDecorativeSolidWood.DecorStates decorState : BlockDecorativeSolidWood.DecorStates.values()) {
				decorState.addRecipes();
			}
		}
	};
	public static BlockEntry<BlockTrashCan> trashCan = new BlockClassEntry<BlockTrashCan>(BlockTrashCan.class, TileTrashCan.class) {
		@Override
		public void addRecipes() {
			addShaped("trash_can", value, "SSS", "CcC", "CCC", 'S', "stone", 'C', "cobblestone", 'c', Blocks.CHEST);
		}
	};
	public static BlockEntry<BlockTrashCan.Fluid> trashCanFluid = new BlockClassEntry<BlockTrashCan.Fluid>("TrashCanFluid", BlockTrashCan.Fluid.class, TileTrashCanFluids.class) {
		@Override
		public void addRecipes() {
			addShaped("trash_can_fluid", value, "SSS", "CcC", "CCC", 'S', "stone", 'C', "cobblestone", 'c', Items.BUCKET);
		}
	};
	public static BlockEntry<BlockTrashCan.Energy> trashCanEnergy = new BlockClassEntry<BlockTrashCan.Energy>("TrashCanEnergy", BlockTrashCan.Energy.class, TileTrashCanEnergy.class) {
		@Override
		public void addRecipes() {
			addShaped("trash_can_energy", value, "SSS", "CcC", "CCC", 'S', "stone", 'C', "cobblestone", 'c', "blockRedstone");
		}
	};
	public static BlockEntry<BlockAngelBlock> angelBlock = new BlockEntry<BlockAngelBlock>("AngelBlock") {

		@Override
		public BlockAngelBlock initValue() {
			return new BlockAngelBlock();
		}

		@Override
		public void addRecipes() {
			addShaped("angel_block", newStack(1), " G ", "WOW", 'G', "ingotGold", 'W', Items.FEATHER, 'O', Blocks.OBSIDIAN);
		}
	}.setItemClass(ItemAngelBlock.class);
	public static BlockEntry<BlockPassiveGenerator> passiveGenerator = new BlockClassEntry<BlockPassiveGenerator>(BlockPassiveGenerator.class, TilePassiveGenerator.class, TilePowerHandCrank.class) {
		@Override
		public void addRecipes() {
			for (BlockPassiveGenerator.GeneratorType generatorTypes : BlockPassiveGenerator.GeneratorType.values()) {
				generatorTypes.addRecipe();
			}
		}

		@Override
		public void addAchievements() {
			for (BlockPassiveGenerator.GeneratorType generatorTypes : BlockPassiveGenerator.GeneratorType.values()) {
				generatorTypes.registerAchievements();
			}
		}
	};
	public static BlockClassEntry<BlockSuperMobSpawner> mobSpawner = new BlockClassEntry<>(BlockSuperMobSpawner.class, TileSuperMobSpawner.class);
	public static BlockClassEntry<BlockCursedEarth> cursedEarth = new BlockClassEntry<BlockCursedEarth>(BlockCursedEarth.class) {
		@Override
		public void loadAdditionalConfig(Configuration config) {
			Collections.addAll(BlockCursedEarth.entity_blacklist, config.getStringList("Cursed Earth Entity BlackList", ConfigHelper.GAMEPLAY_CATEGORY, new String[0], "Add an entity id (mod:name) to this list to prevent cursed earth from spawning it."));
		}
	};
	public static BlockClassEntry<BlockRedstoneClock> redstoneClock = new BlockClassEntry<BlockRedstoneClock>(BlockRedstoneClock.class) {
		@Override
		public void addRecipes() {
			addShaped("redstone_clock", value, "SRS", "RTR", "SRS", 'S', "stone", 'T', Blocks.REDSTONE_TORCH, 'R', "dustRedstone");
		}
	};
	public static BlockCompressedEntry compressedCobblestone = new BlockCompressedEntry(Blocks.COBBLESTONE, "cobblestone", 8);
	public static BlockCompressedEntry compressedDirt = new BlockCompressedEntry(Blocks.DIRT, "dirt", 4);
	public static BlockCompressedEntry compressedSand = new BlockCompressedEntry(Blocks.SAND, "sand", 2);
	public static BlockCompressedEntry compressedGravel = new BlockCompressedEntry(Blocks.GRAVEL, "gravel", 2);
	public static BlockCompressedEntry compressedNetherack = new BlockCompressedEntry(Blocks.NETHERRACK, "netherrack", 6);
	public static ItemClassEntry<ItemFakeCopy> itemFakeCopy = new ItemClassEntry<>(ItemFakeCopy.class);
	public static ItemClassEntry<ItemGlassCutter> itemGlassCutter = new ItemClassEntry<ItemGlassCutter>(ItemGlassCutter.class) {
		@Override
		public void addRecipes() {
			addShaped("glass_cutter", value, "  i", " si", "i  ", 's', "stickWood", 'i', "ingotIron");
		}
	};
	public static ItemEnderShardEntry itemEnderShard = new ItemEnderShardEntry();
	public static BlockEntry<BlockRedOrchid> blockRedOrchid = new BlockClassEntry<BlockRedOrchid>(BlockRedOrchid.class) {
		@Override
		public void postInit() {
			SingleChunkWorldGenManager.register(new SingleChunkGen("RedOrchid", 0) {
				@Override
				public void genChunk(Chunk chunk, Object provider, Random random) {
					BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

					for (int i = 0; i < (256); i++) {
						int dx = random.nextInt(16);
						int dz = random.nextInt(16);
						int dy = 1 + random.nextInt(32);

						pos.setPos(dx, dy, dz);

						Block block = chunk.getBlockState(pos).getBlock();
						if (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) {
							for (int j = 1; j < 3; j++) {

								pos.setPos(dx, dy + j, dz);
								block = chunk.getBlockState(pos).getBlock();
								if (block == Blocks.AIR) {
									report(chunk, dx, dy, dz);

									setBlockState(chunk, pos, value.FULLY_GROWN_STATE);
									break;
								} else if (block != Blocks.REDSTONE_ORE && block != Blocks.LIT_REDSTONE_ORE) {
									break;
								}
							}
						}
					}
				}
			});
			RareSeedHandler.register(newStack(1), 0.0078125f * 2);
//			DungeonHelper.addDungeonItem(newStack(5), 2, 5, ChestGenHooks.DUNGEON_CHEST, 0.05);
		}
	}.setItemClass(ItemBlockPlantable.class);
	public static VoidEntry additionalVanillaRecipes = new VoidEntry("additionalVanillaRecipes") {
		@Override
		public void addRecipes() {
			CraftingHelper.addRecipe(new AlwaysLast.XUShapedRecipeAlwaysLast(CraftingHelper.createLocation("shortcut_chest"), new ItemStack(Blocks.CHEST, 4), "WWW", "W W", "WWW", 'W', "logWood"));
			CraftingHelper.addRecipe(new AlwaysLast.XUShapedRecipeAlwaysLast(CraftingHelper.createLocation("shortcut_stick"), new ItemStack(Items.STICK, 16), "W", "W", 'W', "logWood"));
			CraftingHelper.addRecipe(new AlwaysLast.XUShapedRecipeAlwaysLast(CraftingHelper.createLocation("shortcut_hopper"), new ItemStack(Blocks.HOPPER, 1), "IWI", "IWI", " I ", 'W', "logWood", 'I', "ingotIron"));
		}
	};
	public static ItemEntrySickle[] sickles = new ItemEntrySickle[]{
			new ItemEntrySickle(0),
			new ItemEntrySickle(1),
			new ItemEntrySickle(2),
			new ItemEntrySickle(3),
			new ItemEntrySickle(4),
	};
	public static BlockClassEntry<BlockPowerOverload> overload = new BlockClassEntry<>(BlockPowerOverload.class, TilePowerOverload.class);
	public static BlockClassEntry<BlockResonator> resonator = new BlockClassEntry<BlockResonator>(BlockResonator.class, TileResonator.class) {
		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Power Resonator", "Imbue energy to objects", this, BlockPassiveGenerator.GeneratorType.SOLAR);
		}

		@Override
		public void addRecipes() {
			addShaped("resonator", newStack(1), "RCR", "IcI", "III", 'I', "ingotIron", 'R', "dustRedstone", 'C', "blockCoal", 'c', redstoneCrystal);
		}
	};
	public static ItemEntry<ItemSunCrystal> sunCrystal = new ItemClassEntry<ItemSunCrystal>(ItemSunCrystal.class) {
		IItemStackMaker EMPTY_GEM = this::newStackLowestDamage;

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Luminescent Potential", "Collect the suns rays", EMPTY_GEM, XU2Entries.resonator);
			AchievementHelper.addAchievement("Sun Crystal", "Light through walls", this, EMPTY_GEM);
		}

		@Override
		public void addRecipes() {
			addShapeless("sun_crystal", newStackLowestDamage(), "gemDiamond", "dustGlowstone", "dustGlowstone", "dustGlowstone", "dustGlowstone");
		}
	};
	public static BlockEntry<BlockSpotlight> blockSpotlight = new BlockClassEntry<BlockSpotlight>(BlockSpotlight.class, TileSpotlight.class) {
		@Override
		public void addAchievements() {
//			AchievementHelper.addAchievement("Spotlight", "Shine light across the world", this, XU2Entries.sunCrystal);
		}

		@Override
		public void addRecipes() {
//			CraftingHelper.addShaped(newStack(8), true, "BBB", "GRB", "BBB", 'G', XU2Entries.sunCrystal.newStack(1), 'R', redstoneCrystal.newStack(), 'B', stoneburnt);
		}
	};
	public static BlockClassEntry<BlockScreen> blockScreen = new BlockClassEntry<BlockScreen>(BlockScreen.class, TileScreen.class) {
		@Override
		public void loadAdditionalConfig(Configuration config) {
			BlockScreen.maxSize = 256 * 1024;
		}

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Screen", "For you viewing pleasure", this, stoneburnt);
		}

		@Override
		public void addRecipes() {

			CraftingHelper.addShaped("screen", newStack(1), true, "BBB", "ERE", 'R', redstoneCrystal, 'B', stoneburnt, 'E', itemEnderShard.newStack());
		}
	};
	public static BlockEntry<BlockDecorativeSolid> decorativeSolid = new BlockClassEntry<BlockDecorativeSolid>(BlockDecorativeSolid.class) {
		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Powered Stone", "Stone that conducts intense amounts of energy", stoneburnt, XU2Entries.resonator);
		}

		@Override
		public void addRecipes() {
			for (BlockDecorativeSolid.DecorStates decorState : BlockDecorativeSolid.DecorStates.values()) {
				decorState.addRecipes();
			}
		}

		@Override
		public void postInit() {
//			if (blockEnderLilly.enabled) {
//				BlockEnderLilly.end_stone_states.add(value.getDefaultState().withProperty(BlockDecorativeSolid.decor, BlockDecorativeSolid.DecorStates.endstone));
//			}
		}
	};
	public static ItemClassEntry<ItemIngredients> itemIngredients = new ItemClassEntry<ItemIngredients>(ItemIngredients.class) {
		@Override
		public void registerOres() {
			for (ItemIngredients.Type type : ItemIngredients.Type.values()) {
				type.registerOres();
			}
		}

		@Override
		public void preInitRegister() {
			super.preInitRegister();
			for (ItemIngredients.Type type : ItemIngredients.Type.values()) {
				type.preInitRegister();
			}
		}

		@Override
		public void addAchievements() {
			for (ItemIngredients.Type type : ItemIngredients.Type.values()) {
				type.addAchievement();
			}
//			AchievementHelper.addAchievement("Tech-Tree Start", "See achievements list for Extra Utilities 2 power tech tree", redstoneCrystal, null);
//			AchievementHelper.addAchievement("Dye of the Moon", "The moons power beckons!", ItemIngredients.Type.DYE_POWDER_LUNAR, stoneburnt);
//			AchievementHelper.addAchievement("Powered Coal", "Boosts coals power by " + ItemIngredients.RED_COAL_MULTIPLIER + "x", ItemIngredients.Type.RED_COAL, XU2Entries.resonator);
		}

		@Override
		public void addRecipes() {
			for (ItemIngredients.Type type : ItemIngredients.Type.values()) {
				type.addRecipes();
			}
		}
	};
	public static ItemClassEntry<ItemGoldenLasso> goldenLasso = new ItemClassEntry<ItemGoldenLasso>(ItemGoldenLasso.class) {
		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Golden Lasso", "Animal Transport", getOreDicMaker(), magical_wood);
			AchievementHelper.addAchievement("Cursed Lasso", "Evil Monster Capture", getMetaMaker(1), getOreDicMaker());
		}

		@Override
		public void addRecipes() {
			ResourceLocation location = CraftingHelper.createLocation("golden_lasso");
			CraftingHelper.addRecipe(new EnchantRecipe(location, new XUShapedRecipe(location, newStack(1, 0), "gsg", "s s", "gsg", 's', Items.STRING, 'g', "nuggetGold"), 8, ImmutableList.of(
					OreDictionary.getOres("nuggetGold"),
					ImmutableList.of(new ItemStack(Items.STRING)),
					OreDictionary.getOres("nuggetGold"),
					ImmutableList.of(new ItemStack(Items.STRING)),
					ImmutableList.of(),
					ImmutableList.of(new ItemStack(Items.STRING)),
					OreDictionary.getOres("nuggetGold"),
					ImmutableList.of(new ItemStack(Items.STRING)),
					OreDictionary.getOres("nuggetGold")
			), new int[]{3, 3}));
			addShapeless("golden_lasso_evil", newStack(1, 1),
					newStack(1, 0),
					XU2Entries.itemIngredients.isActive() ? ItemIngredients.Type.EVIL_DROP.newStack() : new ItemStack(Items.SKULL, 1, 1));
		}
	};
	public static ItemEntry<ItemChickenRing> chickenRing = new ItemClassEntry<ItemChickenRing>(ItemChickenRing.class) {
		@Override
		public void addRecipes() {
			addShaped("chicken_ring", newStack(1, 0),
					"FIF",
					"ICI",
					"rIr",
					'F', "feather",
					'I', "ingotIron",
					'r', redstoneCrystal,
					'C', ItemGoldenLasso.newCraftingStack(EntityChicken.class));

			addShaped("squid_ring", newStack(1, 1),
					"bdb",
					"sre",
					"bdb",
					'b', "dyeBlack",
					'd', "gemDiamond",
					's', ItemGoldenLasso.newCraftingStack(EntitySquid.class),
					'e', Items.ENDER_PEARL,
					'r', newStack(1, 0));
		}
	};
	public static ItemEntry<ItemAngelRing> angelRing = new ItemClassEntry<ItemAngelRing>(ItemAngelRing.class) {
		@Override
		public void addRecipes() {
			final Object[] leftWing = new Object[]{"blockGlass", Items.FEATHER, "dyePurple", Items.LEATHER, "nuggetGold", new ItemStack(Items.COAL, 1, 0)};
			final Object[] rightWing = new Object[]{"blockGlass", Items.FEATHER, "dyePink", Items.LEATHER, "nuggetGold", new ItemStack(Items.COAL, 1, 1)};

			for (int i = 0; i < leftWing.length; i++) {
				addShaped("angel_ring_" + i, newStack(1, i), !Objects.equal(leftWing[i], rightWing[i]),
						"LGR",
						"GrG",
						"BGH",
						'L', leftWing[i],
						'R', rightWing[i],
						'B', ItemGoldenLasso.newCraftingStack(EntityBat.class),
						'G', "ingotGold",
						'r', XU2Entries.chickenRing.isActive() ? XU2Entries.chickenRing.newStack(1, 1) : redstoneCrystal,
						'H', ItemGoldenLasso.newCraftingStack(EntityGhast.class));
				addShapeless("angel_ring_convert_" + i, newStack(1, i), leftWing[i], newWildcardStack(), rightWing[i]);
			}
		}
	};
	public static ItemClassEntry<ItemPowerManager> powerManager = new ItemClassEntry<ItemPowerManager>(ItemPowerManager.class) {
		@Override
		public void addRecipes() {
			addShaped("power_manager", newStack(1), " c", "ss", "ss", 's', "stone", 'c', redstoneCrystal);
		}
	};
	public static ItemClassEntry<ItemBagOfHolding> bagOfHolding = new ItemClassEntry<ItemBagOfHolding>(ItemBagOfHolding.class) {
		@Override
		public void addRecipes() {
			addShaped("bag_of_holding", newStack(), "ggg", "cBc", "ggg", 'g', "ingotGold", 'c', Blocks.CHEST, 'B', magical_wood);
		}
	};
	public static BlockClassEntry<BlockDecorativeGlass> decorativeGlass = new BlockClassEntry<BlockDecorativeGlass>(BlockDecorativeGlass.class) {
		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Powered Glass", "Glass that gives off energy", BlockDecorativeGlass.DecorStates.glass_redstone, XU2Entries.resonator);
		}

		@Override
		public void addRecipes() {
			for (BlockDecorativeGlass.DecorStates decorState : BlockDecorativeGlass.DecorStates.values()) {
				decorState.addRecipes();
			}
		}
	};
	public static BlockClassEntry<BlockIneffableGlass> strangeGlass = new BlockClassEntry<BlockIneffableGlass>(BlockIneffableGlass.class) {
		@Override
		public void addRecipes() {
			for (BlockIneffableGlass.DecorStates decorStates : BlockIneffableGlass.DecorStates.values()) {
				decorStates.addRecipes();
			}
		}
	};
	public static VoidEntry proddingStick = new VoidEntry("WoodenStickPoke") {
		@Override
		public void postInit() {
			ProddingStickHandler.register();
		}
	};
	public static BlockClassEntry<BlockMoonStone> moonStone = new BlockClassEntry<BlockMoonStone>(BlockMoonStone.class) {
		@Override
		public void postInit() {
			SingleChunkWorldGenManager.register(new SingleChunkGen("MoonStone", 0) {
				public IBlockState defaultState = value.getDefaultState();

				@Override
				public void genChunk(Chunk chunk, Object provider, Random random) {
					for (int i = 0; i < 4; i++) {
						int dx = 1 + random.nextInt(14);
						int dy = 1 + random.nextInt(64);
						int dz = 1 + random.nextInt(14);

						BlockPos pos = new BlockPos(dx, dy, dz);

						if (chunk.getBlockState(pos) == BlockMoonStone.mimicState) {
							for (EnumFacing facing : EnumFacing.values()) {
								if (isAir(chunk, pos, facing)) {
									setBlockState(chunk, pos, defaultState);
									return;
								}
							}
						}
					}
				}

			});
		}
	}.setItemClass(null);
	public static MultiBlockEntry<BlockTransferPipe> pipe = new MultiBlockEntry<BlockTransferPipe>(BlockTransferPipe.stateBuilder, "pipe") {
		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Pipes!", "Point to Point Transport", this, resonator);
		}

		@Override
		public void addRecipes() {
			addShaped("transfer_pipes", newStack(64), "SSS", "GRG",
					"SSS", 'S',
					new ItemStack(Blocks.STONE_SLAB), 'G',
					"blockGlass", 'R',
					"dustRedstone");
		}
	}.setItemBlockClass(ItemBlockPipe.class);
	public static BlockEntry<BlockTransferHolder> holder = new BlockClassEntry<BlockTransferHolder>(BlockTransferHolder.class, TileTransferHolder.class) {
		@Override
		public String getConfigLabel() {
			return pipe.getConfigLabel();
		}
	}.setItemClass(null);
	public static BlockClassEntry<BlockPlayerChest> playerChest = new BlockClassEntry<BlockPlayerChest>(BlockPlayerChest.class, TilePlayerChest.class) {
		@Override
		public void addRecipes() {
			addShaped("player_chest", value, "SSS", "SCS",
					"ScS", 'S',
					stoneburnt, 'C',
					Blocks.ENDER_CHEST, 'c',
					redstoneCrystal);
		}
	};
	public static ItemClassEntry<ItemWrench> wrench = new ItemClassEntry<ItemWrench>(ItemWrench.class) {
		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Wrench", "Wrench it!", this, null);
		}

		@Override
		public void addRecipes() {
			addShaped("wrench", newStack(1), " RS", " Sr", "S  ", 'S', "ingotIron", 'R', "dyeRed", 'r', "dustRedstone");
		}
	};
	public static ItemClassEntry<ItemBoomerang> boomerang = new ItemClassEntry<ItemBoomerang>(ItemBoomerang.class) {
		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Boomerang", "It returns", this, magical_wood);
		}

		@Override
		public void init() {

		}

		@Override
		public void addRecipes() {
			addShaped("boomerang", newStack(1), " W ", "W W", 'W', magical_wood);
//			CraftingHelper.addRecipe(new AdvShapelessRecipeBase(newStack()) {
//
//
//				final String boomerang = addMatcher("boomerang", newStack());
//				final String potion = addMatcher("lingering_potion", t -> StackHelper.isNonNull(t) && t.getItem() == Items.LINGERING_POTION && !PotionUtils.getEffectsFromStack(t).isEmpty(),
//						new ItemStack(Items.LINGERING_POTION)
//				);
//
//				@Override
//				protected ItemStack getResult(HashMap<String, ItemStack> map) {
//					ItemStack boomerang_stack = map.get(this.boomerang);
//					ItemStack newStack = boomerang_stack.copy();
//					ItemStack potion = map.get(this.potion);
//					PotionUtils.addPotionToItemStack(newStack, PotionUtils.getPotionFromItem(potion));
//					PotionUtils.appendEffects(newStack, PotionUtils.getFullEffectsFromItem(potion));
//					return newStack;
//				}
//			});
		}

		@Override
		public void postInit() {
			BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(value, new BehaviorDefaultDispenseItem() {
				@Nonnull
				@Override
				public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
					World world = source.getWorld();
					TileEntity tile = source.getBlockTileEntity();

					WeakReference<EntityBoomerang> reference = EntityBoomerang.getBoomerangOwners(world).get(tile);
					EntityBoomerang t;
					if (reference != null && (t = reference.get()) != null && !t.isDead) return stack;

					IPosition position = BlockDispenser.getDispensePosition(source);
					IBlockState iBlockState = source.getBlockState();
					EnumFacing direction = iBlockState.getValue(BlockDispenser.FACING);

					EntityBoomerang boomerang = new EntityBoomerang(world, position.getX(), position.getY(), position.getZ(), stack, tile);
					boomerang.setThrowableHeading(direction.getFrontOffsetX(), direction.getFrontOffsetY(), direction.getFrontOffsetZ(), 0.75F, 4.0F);

					world.spawnEntity(boomerang);

					return stack;
				}

				protected void playDispenseSound(IBlockSource source) {
					source.getWorld().playBroadcastSound(1002, source.getBlockPos(), 0);
				}
			});
		}
	};
	public static ItemClassEntry<ItemBook> book = new ItemClassEntry<ItemBook>(ItemBook.class) {
		@Override
		public void addRecipes() {
			CraftingHelper.addShapeless("manual", newStack(1), ImmutableList.of(new ItemStack(Items.BOOK, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.WRITABLE_BOOK, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.WRITTEN_BOOK, 1, OreDictionary.WILDCARD_VALUE)), "ingotGold", Blocks.OBSIDIAN);
		}
	};
	//	public static VoidEntry throwEnderPearlsInCreative = new VoidEntry("throwEnderPearlsInCreative") {
//		@Override
//		public void postInit() {
////			CreativeEPHandler.init();
//		}
//
//		@Override
//		public boolean isEnabledByDefault() {
//			return false;
//		}
//	};
	//	public static VoidEntry mobsSpawnInAnyLight = new VoidEntry("mobsSpawnInAnyLight"){
//		@Override
//		public void preInitLoad() {
//			MobSpawnInAnyLightHandler.init();
//		}
//
//		@Override
//		public boolean isEnabledByDefault() {
//			return false;
//		}
//	};
	public static ItemClassEntry<ItemContract> contract = new ItemClassEntry<ItemContract>(ItemContract.class) {
		@Override
		public void addRecipes() {
			CraftingHelper.addShapeless("contract", newStack(1), Items.FEATHER, Items.PAPER, Items.GLASS_BOTTLE, "dyeBlack");
		}

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Villager Contract", "Head of Villager Soul Resources", this, goldenLasso.getOreDicMaker());
		}
	};
	public static BlockClassEntry<BlockWardChunkLoader> ward_chunkloader = new BlockClassEntry<BlockWardChunkLoader>("ChunkLoader", BlockWardChunkLoader.class, TileChunkLoader.class) {
		@Override
		public void addRecipes() {
			CraftingHelper.addShaped("chunk_loader", newStack(1), "SES", "SLS", " S ", 'S', "stickWood", 'E', ItemIngredients.Type.EYE_REDSTONE.newStack(1), 'L', ItemGoldenLasso.newCraftingVillagerStack(true, null));
		}

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Chunk Loader", "Persistence", this, contract);
		}
	};
	public static ItemClassEntry<ItemFilterItems> filterItems = new ItemClassEntry<ItemFilterItems>("Filter", ItemFilterItems.class) {
		@Override
		public void addRecipes() {
			addShaped("filter_items", newStack(), "rsr", "sSs", "rsr", 'r', "dustRedstone", 's', "stickWood", 'S', "string");
		}
	};
	public static ItemClassEntry<ItemFilterFluids> filterFluids = new ItemClassEntry<ItemFilterFluids>(ItemFilterFluids.class) {
		@Override
		public void addRecipes() {
			addShaped("filter_fluids", newStack(), "rsr", "sSs", "rsr", 'r', "gemLapis", 's', "stickWood", 'S', "string");
		}
	};
	//	public static BlockClassEntry<BlockEvil> evilBlock = new BlockClassEntry<BlockEvil>(BlockEvil.class, TileEvil.class) {
//		@Override
//		public void addRecipes() {
//
//		}
//
//		@Override
//		public void addAchievements() {
//
//		}
//	};
	public static ItemClassEntry<ItemGrocket> grocket = new ItemClassEntry<ItemGrocket>(ItemGrocket.class) {
		@Override
		public void addRecipes() {
			CraftingHelper.addShaped("transfer_node_items",
					newStack(4, TRANSFER_NODE_ITEMS.ordinal()),
					"RPR",
					"SCS", 'P',
					pipe, 'R',
					"dustRedstone", 'S',
					"stone", 'C', ImmutableList.of(
							new ItemStack(Blocks.CHEST),
							new ItemStack(Blocks.TRAPPED_CHEST)));

			CraftingHelper.addShaped("transfer_filter_items",
					newStack(4, GrocketType.FILTER_ITEMS.ordinal()),
					"RFR",
					"SPS", 'P',
					pipe, 'F',
					filterItems, 'R',
					"dustRedstone", 'S', "stone");

			CraftingHelper.addShapeless("transfer_pipe_filter", newStack(4, GrocketType.FILTER_PIPE.ordinal()),
					pipe,
					filterItems, "dustRedstone");

			CraftingHelper.addShaped("transfer_node_fluids",
					newStack(4, GrocketType.TRANSFER_NODE_FLUIDS.ordinal()),
					"RPR",
					"SCS", 'P',
					pipe, 'R',
					"dustRedstone", 'S',
					"stone", 'C', Items.BUCKET);

			CraftingHelper.addShaped("transfer_node_items_retrieve",
					newStack(2, GrocketType.TRANSFER_NODE_ITEMS_RETRIEVE.ordinal()),
					" E ",
					"NSN",
					" E ", 'E',
					Items.ENDER_PEARL, 'N',
					newStack(1, TRANSFER_NODE_ITEMS.ordinal()), 'S',
					"gemEmerald");

			CraftingHelper.addShaped("transfer_node_fluids_retrieve",
					newStack(2, GrocketType.TRANSFER_NODE_FLUIDS_RETRIEVE.ordinal()),
					" E ",
					"NSN",
					" E ", 'E',
					Items.ENDER_PEARL, 'N',
					newStack(1, GrocketType.TRANSFER_NODE_FLUIDS.ordinal()), 'S',
					"gemDiamond");

			CraftingHelper.addShaped("transfer_node_energy",
					newStack(1, GrocketType.TRANSFER_NODE_ENERGY.ordinal()),
					"RPR",
					"SCS", 'P',
					pipe, 'R',
					"dustRedstone", 'S',
					"ingotGold", 'C', Blocks.REDSTONE_BLOCK);
		}

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Transfer Nodes", "Simple Item Management", metaCache.getUnchecked(0), pipe);
		}

		@Override
		public String getConfigLabel() {
			return pipe.getConfigLabel();
		}
	};
	public static BlockClassEntry<BlockIndexer> indexer = new BlockClassEntry<BlockIndexer>(BlockIndexer.class, TileIndexer.class) {
		@Override
		public void addRecipes() {
			addShaped("indexer", value, "SCS", "SsS",
					"SCS", 'S',
					stoneburnt, 'C',
					redstoneCrystal, 's',
					blockScreen);
		}

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Indexer", "Item Retrieval", this, grocket.getMetaMaker(0));
		}
	};
	public static ItemClassEntry<ItemIndexerRemote> indexerRemote = new ItemClassEntry<ItemIndexerRemote>(ItemIndexerRemote.class) {
		@Override
		public void addRecipes() {
			addShaped("indexer_remote", value, "SCS", "SsS",
					"SCS", 'S',
					"stone", 'C',
					ItemIngredients.Type.EYE_REDSTONE, 's',
					blockScreen.newStack());
		}

		@Override
		public void addAchievements() {
			AchievementHelper.addAchievement("Remote Indexing", "Item Retrieval at distance", this, indexer);
		}
	};
	public static PotionEntry<PotionDoom> potionDoom = new PotionEntry<PotionDoom>("potion_doom", "Kills in 60 seconds.") {
		@Override
		public void registerTypesAndRecipes() {
			PotionType doom = PotionsHelper.registerPotionType(new PotionEffect(value, 60 * 20));
		}

		@Override
		protected PotionDoom initValue() {
			return new PotionDoom();
		}
	};
	public static PotionEntry<PotionGravity> potionGravity = new PotionEntry<PotionGravity>("potion_gravity", "Pulls fliers down to the ground.") {
		@Override
		public PotionGravity initValue() {
			return new PotionGravity();
		}

		@Override
		public void registerTypesAndRecipes() {
			PotionType antiFlying = PotionsHelper.registerPotionType(new PotionEffect(value, 60 * 20));
			BrewingRecipeRegistry.addRecipe(
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionsHelper.getAwkwardPotionType()),
					"obsidian",
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), antiFlying));

			PotionType antiFlying2 = PotionsHelper.registerDerivedPotionType(new PotionEffect(value, 8 * 60 * 20), antiFlying, ".long");
			BrewingRecipeRegistry.addRecipe(
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), antiFlying),
					"dustRedstone",
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), antiFlying2));
		}
	};
	public static PotionEntry<PotionSecondChance> potionAntiDeath = new PotionEntry<PotionSecondChance>("potion_second_chance", "Will save from death but only once.") {
		@Override
		public PotionSecondChance initValue() {
			return new PotionSecondChance();
		}

		@Override
		public void registerTypesAndRecipes() {
			PotionType antiDeath = PotionsHelper.registerPotionType(new PotionEffect(value, 2 * 60 * 20));
			BrewingRecipeRegistry.addRecipe(
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionsHelper.getVanillaType("strong_healing")),
					new ItemStack(Items.GOLDEN_APPLE, 1, 1),
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), antiDeath));
		}
	};
	public static PotionEntry<PotionNapalm> potionGreekFire = new PotionEntry<PotionNapalm>("potion_greek_fire", "Creates a fire that cannot be extenguished") {
		@Override
		public void registerTypesAndRecipes() {
			PotionType greekFire = PotionsHelper.registerPotionType(new PotionEffect(value, 2 * 60 * 20));
			PotionType oily = PotionsHelper.newGenericPotion("Oily");
			BrewingRecipeRegistry.addRecipe(
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionsHelper.getAwkwardPotionType()),
					new ItemStack(Items.BEETROOT),
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), oily));
			BrewingRecipeRegistry.addRecipe(
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), oily),
					new ItemStack(Items.LAVA_BUCKET),
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), greekFire));
		}

		@Override
		public PotionNapalm initValue() {
			return new PotionNapalm();
		}
	};
	public static PotionEntry<PotionFizzyLifting> potionFizzy = new PotionEntry<PotionFizzyLifting>("potion_fizzy_lifting", null) {
		@Override
		public void registerTypesAndRecipes() {
			PotionType greekFire = PotionsHelper.registerPotionType(new PotionEffect(value, 30 * 20));

			BrewingRecipeRegistry.addRecipe(
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.LEAPING),
					new ItemStack(Items.SUGAR),
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), greekFire));
		}

		@Override
		public PotionFizzyLifting initValue() {
			return new PotionFizzyLifting();
		}
	};
	public static PotionEntry<PotionRelapse> potionRelapse = new PotionEntry<PotionRelapse>("potion_relapse", "Negative potion effects resist being cured.") {
		@Override
		public void registerTypesAndRecipes() {
			PotionType relapse = PotionsHelper.registerPotionType(new PotionEffect(value, 8 * 60 * 20));
			BrewingRecipeRegistry.addRecipe(new BrewingOreRecipe(
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionsHelper.getAwkwardPotionType()),
					Lists.newArrayList(
							new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.ROSE.getMeta()),
							new ItemStack(BlockFlower.EnumFlowerType.POPPY.getBlockType().getBlock(), 1, BlockFlower.EnumFlowerType.POPPY.getMeta()),
							new ItemStack(BlockFlower.EnumFlowerType.RED_TULIP.getBlockType().getBlock(), 1, BlockFlower.EnumFlowerType.RED_TULIP.getMeta())
					),
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), relapse)));
		}

		@Override
		public PotionRelapse initValue() {
			return new PotionRelapse();
		}
	};
	public static PotionEntry<PotionLove> potionLove = new PotionEntry<PotionLove>("potion_love", null) {
		@Override
		public PotionLove initValue() {
			return new PotionLove();
		}

		@Override
		public void registerTypesAndRecipes() {
			PotionType love = PotionsHelper.registerPotionType(new PotionEffect(value, 10 * 20));
			BrewingRecipeRegistry.addRecipe(new BrewingOreRecipe(
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionsHelper.getAwkwardPotionType()),
					Lists.newArrayList(
							new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.ROSE.getMeta()),
							new ItemStack(BlockFlower.EnumFlowerType.POPPY.getBlockType().getBlock(), 1, BlockFlower.EnumFlowerType.POPPY.getMeta()),
							new ItemStack(BlockFlower.EnumFlowerType.RED_TULIP.getBlockType().getBlock(), 1, BlockFlower.EnumFlowerType.RED_TULIP.getMeta())
					),
					PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), love)));
		}
	};
	public static VillagerEntrySimple alchemist = new VillagerEntrySimple("alchemist") {
		@Override
		public List<XUVillagerCareer> getCareers() {
			return Lists.newArrayList(
					newCareer("alchemist")
							.addAdditionalTrade(0, new EmeraldForPotions(false))
							.addAdditionalTrade(0, new EmeraldForPotions(false))
							.addAdditionalTrade(0, new EmeraldForPotions(true))
							.addAdditionalTrade(1, new EmeraldForPotions(false))
							.addAdditionalTrade(1, new EmeraldForPotions(false))
							.addAdditionalTrade(1, new EmeraldForPotions(true))

			);
		}
	};
	public static VillagerEntrySimple red_mechanic = new VillagerEntrySimple("red_mechanic") {
		@Override
		public List<XUVillagerCareer> getCareers() {
			return Lists.newArrayList(newCareer("red_mechanic")
							.addRandomTrade(0,
									new EntityVillager.ListItemForEmeralds(redstoneCrystal.newStack(), new EntityVillager.PriceInfo(-4, -8)),
//							new EntityVillager.ListItemForEmeralds(new ItemStack("dustRedstone"), new EntityVillager.PriceInfo(1, 1)),
//							new EntityVillager.ListItemForEmeralds(new ItemStack(Items.repeater), new EntityVillager.PriceInfo(1, 1)),
//							new EntityVillager.ListItemForEmeralds(new ItemStack(Items.comparator), new EntityVillager.PriceInfo(1, 1)),
//							new EntityVillager.ListItemForEmeralds(new ItemStack(Blocks.redstone_torch), new EntityVillager.PriceInfo(1, 1)),
									new EntityVillager.ListItemForEmeralds(wrench.newStack(), new EntityVillager.PriceInfo(1, 1))

							)
			);
		}


	};
	public static BlockClassEntry<BlockCrafter> blockCrafter = new BlockClassEntry<BlockCrafter>(BlockCrafter.class, TileCrafter.class) {
		@Override
		public void addRecipes() {
			addShapeless("crafter", newStack(), Blocks.DROPPER, redstoneCrystal, Blocks.CRAFTING_TABLE);
		}
	};
	public static BlockClassEntry<BlockAdvInteractor.Scanner> blockScanner = new BlockClassEntry<BlockAdvInteractor.Scanner>(BlockAdvInteractor.Scanner.class, TileScanner.class) {
		@Override
		public void addRecipes() {
			addShapeless("scanner", newStack(), Blocks.DROPPER, "dustRedstone", Items.SPIDER_EYE);
		}
	};
	public static BlockClassEntry<BlockAdvInteractor.Mine> blockMiner = new BlockClassEntry<BlockAdvInteractor.Mine>("Miner", BlockAdvInteractor.Mine.class, TileMine.class) {
		@Override
		public void addRecipes() {
			addShapeless("miner", newStack(), Blocks.DROPPER, redstoneCrystal, Items.IRON_PICKAXE);
		}
	};
	public static BlockClassEntry<BlockAdvInteractor.Use> blockUser = new BlockClassEntry<BlockAdvInteractor.Use>("User", BlockAdvInteractor.Use.class, TileUse.class) {
		@Override
		public void addRecipes() {
			addShapeless("user", newStack(), Blocks.DROPPER, redstoneCrystal, Blocks.LEVER);
		}
	};
	public static BlockClassEntry<BlockKlein> kleinFlask = new BlockClassEntry<BlockKlein>(BlockKlein.class) {
		@Override
		public void addRecipes() {
			addShapeless("klein_flash", newStack(), Items.GLASS_BOTTLE, Items.ENDER_PEARL, Items.ENDER_PEARL);
		}
	};
	public static BlockClassEntry<BlockDrum> drum = new BlockClassEntry<BlockDrum>(BlockDrum.class,
			TileDrum.Tank16.class, TileDrum.Tank256.class, TileDrum.Tank4096.class, TileDrum.Tank65536.class, TileDrum.TankInf.class) {
		@Override
		public void addRecipes() {
			addShaped("drum_16", newStackMeta(BlockDrum.Capacity.DRUM_16.ordinal()), "SsS", "SbS",
					"SsS", 'S',
					"cobblestone", 's',
					ImmutableList.of(
							new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.COBBLESTONE.getMetadata()),
							new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.STONE.getMetadata()),
							"slabStone"), 'b',
					Items.BOWL);

			addShaped("drum_256", newStackMeta(BlockDrum.Capacity.DRUM_256.ordinal()), "SsS", "SbS",
					"SsS", 'S',
					"ingotIron", 's',
					Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 'b',
					Items.CAULDRON);

			CraftingHelper.addRecipe(new UpgradeRecipe(CraftingHelper.createLocation("drum_4096"), newStackMeta(BlockDrum.Capacity.DRUM_4096.ordinal()), 1, 1, "SsS", "SbS", "SsS", 'S', "gemDiamond", 's', Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 'b', newStackMeta(BlockDrum.Capacity.DRUM_256.ordinal())));

			CraftingHelper.addRecipe(new UpgradeRecipe(CraftingHelper.createLocation("drum_65536"), newStackMeta(BlockDrum.Capacity.DRUM_65536.ordinal()), 1, 1, "SbS", "SsS", "SbS",
					'S', "ingotDemonicMetal",
					's', newStackMeta(BlockDrum.Capacity.DRUM_4096.ordinal()),
					'b', kleinFlask
			));


			for (BlockDrum.Capacity capacity : BlockDrum.Capacity.values()) {
				CraftingHelper.addRecipe(new XUShapelessRecipeClearNBT(CraftingHelper.createLocation("drum_clear_" + capacity.capacity), newStackMeta(capacity.ordinal()), "Fluid"));
			}

		}
	};
	public static BlockClassEntry<BlockMachine> machineEntry = new BlockClassEntry<BlockMachine>(BlockMachine.class, TileMachineProvider.class, TileMachineReceiver.class) {
		HashMap<String, IItemStackMaker> cache = new HashMap<>();

		{
			try {
				MachineInit.init();
			} catch (Throwable error) {
				error.printStackTrace();
				throw error;
			}
		}

		public IItemStackMaker createMaker(String machine) {
			Machine machine1 = MachineRegistry.getMachine(machine);
			return createMaker(machine1);
		}

		public IItemStackMaker createMaker(Machine machine1) {
			if (machine1 == null)
				return this;
			return cache.computeIfAbsent(machine1.name, m -> () -> value.createStack(machine1));
		}

		@Override
		public void addAchievements() {

		}

		@Override
		public void addRecipes() {
			addShaped("machine_base", newStack(4), "IRI", "RCR",
					"IRI", 'I',
					"ingotIron", 'R',
					"dustRedstone",
					'C', ImmutableList.of(Blocks.CHEST, Blocks.TRAPPED_CHEST));


			addShaped("machine_furnace",
					value.createStack(XUMachineFurnace.INSTANCE),
					"bbb",
					"bcb",
					"bbb", 'b',
					"ingotBrick", 'c', newStack());

			addShaped("machine_enchanter",
					value.createStack(XUMachineEnchanter.INSTANCE),
					" e ",
					"dcd",
					"iii",
					'e', Items.ENCHANTED_BOOK,
					'd', "gemDiamond",
					'i', "ingotIron",
					'c', newStack());

			addShaped("machine_crusher",
					value.createStack(XUMachineCrusher.INSTANCE),
					"ipi",
					"ici",
					"ipi", 'p',
					ImmutableList.of(Blocks.PISTON, Blocks.STICKY_PISTON), 'i',
					"ingotIron", 'c',
					newStack());

			addShaped("generator_furnace",
					value.createStack(XUMachineGenerators.FURNACE_GENERATOR),
					"iii",
					"ici",
					"rpr", 'p',
					Blocks.FURNACE, 'i',
					"ingotIron", 'r',
					"dustRedstone", 'c',
					newStack());

			addShaped("generator_survivalist",
					value.createStack(XUMachineGenerators.SURVIVALIST_GENERATOR),
					"iii",
					"ici",
					"rpr", 'p',
					Blocks.FURNACE, 'i',
					"cobblestone", 'r',
					"dustRedstone", 'c',
					"ingotIron");

			createSubMachineRecipe(XUMachineGenerators.CULINARY_GENERATOR, Lists.newArrayList(Items.COOKED_PORKCHOP, Items.COOKED_BEEF, Items.COOKED_CHICKEN, Items.COOKED_MUTTON, Items.COOKED_FISH, Items.COOKED_RABBIT), Lists.newArrayList("cropWheat", "cropPotato", "cropCarrot"));
			createSubMachineRecipe(XUMachineGenerators.POTION_GENERATOR, Items.BREWING_STAND, Items.BLAZE_ROD);
			createSubMachineRecipe(XUMachineGenerators.TNT_GENERATOR, Blocks.TNT, Items.GUNPOWDER);
			createSubMachineRecipe(XUMachineGenerators.LAVA_GENERATOR, Items.LAVA_BUCKET, "ingotGold");
			createSubMachineRecipe(XUMachineGenerators.PINK_GENERATOR, new ItemStack(Blocks.WOOL, 1, EnumDyeColor.PINK.getMetadata()), new ItemStack(Items.DYE, 1, EnumDyeColor.PINK.getDyeDamage()));
			createSubMachineRecipe(XUMachineGenerators.NETHERSTAR_GENERATOR, Items.NETHER_STAR, new ItemStack(Items.SKULL, 1, 1));
			createSubMachineRecipe(XUMachineGenerators.ENDER_GENERATOR, Blocks.OBSIDIAN, Items.ENDER_PEARL);
			createSubMachineRecipe(XUMachineGenerators.REDSTONE_GENERATOR, Blocks.REDSTONE_BLOCK, Items.REDSTONE, XUMachineGenerators.LAVA_GENERATOR);
			createSubMachineRecipe(XUMachineGenerators.OVERCLOCK_GENERATOR, Blocks.GOLD_BLOCK, new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
			createSubMachineRecipe(XUMachineGenerators.DRAGON_GENERATOR, Blocks.END_ROD, Blocks.PURPUR_BLOCK);
			createSubMachineRecipe(XUMachineGenerators.ICE_GENERATOR, Blocks.ICE, Items.SNOWBALL);
			createSubMachineRecipe(XUMachineGenerators.DEATH_GENERATOR, new ItemStack(Items.SPIDER_EYE), Lists.newArrayList(Items.BONE, Items.ROTTEN_FLESH));
			createSubMachineRecipe(XUMachineGenerators.ENCHANT_GENERATOR, Blocks.ENCHANTING_TABLE, magical_wood);
			createSubMachineRecipe(XUMachineGenerators.SLIME_GENERATOR, Blocks.SLIME_BLOCK, Items.SLIME_BALL);

			MachineInit.addMachineRecipes();

		}

		private void createSubMachineRecipe(Machine generator, Object center, Object outside) {
			createSubMachineRecipe(generator, center, outside, XUMachineGenerators.FURNACE_GENERATOR);
		}

		private void createSubMachineRecipe(Machine generator, Object center, Object outside, Machine furnaceGenerator) {
			addShaped("generator_" + generator.location.getResourcePath().toLowerCase(Locale.ENGLISH),
					value.createStack(generator),
					"iii",
					"iCi",
					"rpr", 'C',
					center, 'i',
					outside, 'r',
					"dustRedstone", 'p',
					value.createStack(furnaceGenerator));
		}

	}.setItemClass(ItemBlockMachine.class);
	public static BlockClassEntry<BlockTeleporter> teleporter = new BlockClassEntry<BlockTeleporter>(BlockTeleporter.class, TileTeleporter.class) {
		@Override
		public void addRecipes() {
			addShaped("teleporter", teleporter.newStack(1, 1), "SSS", "S S", "SSS", 'S', XU2Entries.compressedCobblestone.newStack(1));
		}
	};
	public static DimensionEntry deep_dark = new DimensionEntry("Deep Dark", -11325, WorldProviderDeepDark.class, false) {
		@Override
		public Teleporter createTeleporter(WorldServer destWorld, int dest, int curDim) {
			return new TeleporterDeepDark(destWorld, dest, curDim);
		}
	};
	public static BlockClassEntry<BlockPowerTransmitter> transmitter = new BlockClassEntry<BlockPowerTransmitter>(BlockPowerTransmitter.class, TilePowerTransmitter.class) {
		@Override
		public Set<Entry<?>> getDependencies() {
			return ImmutableSet.of(battery);
		}

		@Override
		public void addRecipes() {
			addShaped("transmitter", newStack(4), "r", "S", 'S', stoneburnt, 'r', redstoneCrystal);
		}
	};
	public static BlockClassEntry<BlockPowerBattery> battery = new BlockClassEntry<BlockPowerBattery>(BlockPowerBattery.class, TilePowerBattery.class) {
		@Override
		public Set<Entry<?>> getDependencies() {
			return ImmutableSet.of(transmitter);
		}

		@Override
		public void addRecipes() {
			addShaped("battery", newStack(), "SSS", "rRr", "SSS", 'S', stoneburnt, 'r', "blockRedstone", 'R', redstoneCrystal);
		}
	};
	public static BlockClassEntry<BlockQuarryProxy> quarry_proxy = new BlockClassEntry<BlockQuarryProxy>(BlockQuarryProxy.class, TileQuarryProxy.class) {
		@Override
		public Set<Entry<?>> getDependencies() {
			return ImmutableSet.of(quarry, specialdim);
		}

		@Override
		public void addRecipes() {
			addShaped("quarry_proxy", newStack(), "eme", "ede", "rrr", 'e', Blocks.END_STONE, 'm', Blocks.END_ROD, 'd', Items.DIAMOND_PICKAXE, 'r', stoneburnt);
		}
	};
	//	public static DimensionEntry dream_world = new DimensionEntry("dream_world", -22322, WorldProviderDreamWorld.class, false);
	public static BlockClassEntry<BlockQuarry> quarry = new BlockClassEntry<BlockQuarry>(BlockQuarry.class, TileQuarry.class) {
		@Override
		public Set<Entry<?>> getDependencies() {
			return ImmutableSet.of(quarry_proxy, specialdim);
		}

		@Override
		public void addRecipes() {

			PatternRecipe.register(
					new String[][]{{"   ", " u ", "   "}, {" e ", "scn", " w "}, {"   ", " d ", "   "}},
					ImmutableMap.<Character, Object>builder()
							.put('c', value.getDefaultState())
							.put('d', getQuarryProxyState(EnumFacing.DOWN))
							.put('u', getQuarryProxyState(EnumFacing.UP))
							.put('n', getQuarryProxyState(EnumFacing.NORTH))
							.put('s', getQuarryProxyState(EnumFacing.SOUTH))
							.put('w', getQuarryProxyState(EnumFacing.WEST))
							.put('e', getQuarryProxyState(EnumFacing.EAST))
							.build(),
					newStack(), quarry_proxy.newStack()
			);

			addShaped("quarry_base", newStack(), "mem", "ede", "mem", 'm', Blocks.END_STONE, 'd', snowGlobe.isActive() ? "magic_snow_globe" : "netherStar", 'e', stoneburnt);
		}

		@Nonnull
		private IBlockState getQuarryProxyState(EnumFacing side) {
			return quarry_proxy.value.getDefaultState().withProperty(XUBlockStateCreator.ROTATION_ALL, side);
		}

		@Override
		public void loadAdditionalConfig(Configuration config) {
			WorldProviderSpecialDim.ALLOW_SPECIAL_DIMS = config.get(ConfigHelper.GAMEPLAY_CATEGORY, "Quantum Quarry: Enable Nether/End biome generation (has been buggy)", false).getBoolean();
			TileQuarry.ENERGY_PER_OPERATION = config.get(ConfigHelper.ENERGY_CATEGORY, "Quantum Quarry: Base Energy Per Operation", 20000).getInt();
		}
	};
	public static DimensionEntry specialdim = new DimensionEntry("ExtraUtils2_Quarry_Dim", -9999, WorldProviderSpecialDim.class, true) {
		@Override
		public Set<Entry<?>> getDependencies() {
			return ImmutableSet.of(quarry_proxy, quarry);
		}
	};
	public static BlockEntry<BlockSpike> spike_wood = new BlockEntry<BlockSpike>("spike_wood") {
		@Override
		protected BlockSpike initValue() {
			return new BlockSpike(BlockSpike.SpikeType.wood);
		}

		@Override
		public void addRecipes() {
			BlockSpike.SpikeType.addRecipes(this);
		}
	};
	public static BlockEntry<BlockSpike> spike_stone = new BlockEntry<BlockSpike>("spike_stone") {
		@Override
		protected BlockSpike initValue() {
			return new BlockSpike(BlockSpike.SpikeType.stone);
		}

		@Override
		public void addRecipes() {
			BlockSpike.SpikeType.addRecipes(this);
		}
	};
	public static BlockEntry<BlockSpike> spike_iron = new BlockEntry<BlockSpike>("spike_iron") {
		@Override
		protected BlockSpike initValue() {
			return new BlockSpike(BlockSpike.SpikeType.iron);
		}

		@Override
		public void addRecipes() {
			BlockSpike.SpikeType.addRecipes(this);
		}
	};
	public static BlockEntry<BlockSpike> spike_gold = new BlockEntry<BlockSpike>("spike_gold") {
		@Override
		protected BlockSpike initValue() {
			return new BlockSpike(BlockSpike.SpikeType.gold);
		}

		@Override
		public void addRecipes() {
			BlockSpike.SpikeType.addRecipes(this);
		}
	};
	public static BlockEntry<BlockSpike> spike_diamond = new BlockEntry<BlockSpike>("spike_diamond") {
		@Override
		protected BlockSpike initValue() {
			return new BlockSpike(BlockSpike.SpikeType.diamond);
		}

		@Override
		public void addRecipes() {
			BlockSpike.SpikeType.addRecipes(this);
		}
	};
	public static BlockEntry<BlockSpike> spike_creative = new BlockEntry<BlockSpike>("spike_creative") {
		@Override
		protected BlockSpike initValue() {
			return new BlockSpike.Creative();
		}
	};
	public static BlockClassEntry<BlockRedstoneLantern> redstoneLantern = new BlockClassEntry<BlockRedstoneLantern>(BlockRedstoneLantern.class) {
		@Override
		public void addRecipes() {
			addShaped("lantern", newStack(), "RSR", "SCS", "RrR", 'R', "dustRedstone", 'S', decorativeSolid.isActive() ? BlockDecorativeSolid.DecorStates.stoneslab : "stone", 'C', Items.COAL, 'r', Items.COMPARATOR);
		}
	};
	public static BlockClassEntry<BlockLargishChest> largistChest = new BlockClassEntry<BlockLargishChest>(BlockLargishChest.class, TileLargishChest.class) {
		@Override
		public void addRecipes() {
			addShaped("largist_chest", newStack(), "sss", "scs", "sss", 'c', Blocks.CHEST, 's', Items.STICK);
		}
	};
	public static BlockClassEntry<BlockMiniChest> miniChest = new BlockClassEntry<BlockMiniChest>(BlockMiniChest.class, TileMinChest.class) {
		@Override
		public void addRecipes() {
			addShapeless("mini_chest", newStack(9), Blocks.CHEST);
			addShaped("mini_chest_to_chest", Blocks.CHEST, "ccc", "ccc", "ccc", 'c', this);
		}
	};
	public static BlockClassEntry<BlockRainbowGenerator> rainbowGenerator = new BlockClassEntry<BlockRainbowGenerator>(BlockRainbowGenerator.class, TileRainbowGenerator.class) {
		@Override
		public Set<Entry<?>> getDependencies() {
			return ImmutableSet.of(machineEntry);
		}

		@Override
		public void addRecipes() {
			Machine[] generators = TileRainbowGenerator.GENERATORS;

			char[] chars = new char[]{'1', '2', '3', '4', '5', '6', '7', '8'};
			ArrayList<Object> top = new ArrayList<>(ImmutableList.of("123", "4c5", "678", 'c', redstoneCrystal));
			ArrayList<Object> bottom = new ArrayList<>(ImmutableList.of("123", "4c5", "678", 'c', redstoneCrystal));
			for (int i = 0; i < 8; i++) {
				top.add(chars[i]);
				top.add(machineEntry.value.createStack(generators[i]));
			}
			for (int i = 8; i < 16; i++) {
				bottom.add(chars[i - 8]);
				bottom.add(machineEntry.value.createStack(generators[i]));
			}
			addShaped("rainbow_gen_bottom", newStack(1, value.getDefaultState().withProperty(BlockRainbowGenerator.PROPERTY_STATE, BlockRainbowGenerator.State.BOTTOM_HALF)), bottom.toArray());
			addShaped("rainbow_gen_top", newStack(1, value.getDefaultState().withProperty(BlockRainbowGenerator.PROPERTY_STATE, BlockRainbowGenerator.State.TOP_HALF)), top.toArray());
			addShapeless("rainbow_gen",
					newStack(1, value.getDefaultState().withProperty(BlockRainbowGenerator.PROPERTY_STATE, BlockRainbowGenerator.State.FULL)),
					newStack(1, value.getDefaultState().withProperty(BlockRainbowGenerator.PROPERTY_STATE, BlockRainbowGenerator.State.BOTTOM_HALF)), newStack(1, value.getDefaultState().withProperty(BlockRainbowGenerator.PROPERTY_STATE, BlockRainbowGenerator.State.TOP_HALF)));

		}
	};
	public static ItemClassEntry<ItemMagicApple> magicApple = new ItemClassEntry<ItemMagicApple>(ItemMagicApple.class) {
		@Override
		public void addRecipes() {
			addShaped("magic_apple", newStack(8), "ccc", "cec", "ccc", 'c', Items.APPLE, 'e', magical_wood);
			if (XU2Entries.machineEntry.isActive()) {
				XUMachineEnchanter.addRecipe(new ItemStack(Items.APPLE, 16), newStack(16), 1, 16000, "gemLapis");
			}
		}
	};
	public static BlockClassEntry<BlockSimpleDecorative> simpleDecorative = new BlockClassEntry<BlockSimpleDecorative>(BlockSimpleDecorative.class) {
		@Override
		public void registerOres() {
			for (BlockSimpleDecorative.Type type : BlockSimpleDecorative.Type.values()) {
				if (type.oreName == null) continue;
				OreDictionary.registerOre(type.oreName, type.newStack());
			}
		}

		@Override
		public void addRecipes() {
			for (BlockSimpleDecorative.Type type : BlockSimpleDecorative.Type.values()) {
				type.addRecipes();
			}
		}
	};
	public static BlockClassEntry<BlockCreativeChest> creativeChest = new BlockClassEntry<>(BlockCreativeChest.class, TileCreativeChest.class);
	public static BlockClassEntry<BlockCreativeEnergy> creativeEnergy = new BlockClassEntry<>(BlockCreativeEnergy.class, TileCreativeEnergy.class);
	public static BlockClassEntry<BlockCreativeHarvest> creativeHarvest = new BlockClassEntry<>(BlockCreativeHarvest.class, TileCreativeHarvest.class);
	public static BlockClassEntry<BlockSynergy> synergyUnit = new BlockClassEntry<BlockSynergy>(BlockSynergy.class, TileSynergyUnit.class) {
		@Override
		public void addRecipes() {

		}

		@Override
		public boolean isEnabledByDefault() {
			return false;
		}
	};
	public static ItemClassEntry<ItemTrowel> trowel = new ItemClassEntry<ItemTrowel>(ItemTrowel.class) {
		@Override
		public void addRecipes() {
			addShaped("trowel", newStack(), "  i", " s ", "t  ", 'i', "ingotIron", 's', Blocks.STONE_BUTTON, 't', "stickWood");
		}
	};
	public static ItemClassEntry<ItemBiomeMarker> biomeMarker = new ItemClassEntry<ItemBiomeMarker>(ItemBiomeMarker.class) {
		@Override
		public void addRecipes() {
			addShaped("biome_marker", newStack(), "pip", "isi", "pip", 'p', ImmutableList.of("dyeMagenta", "dyePurple"), 'i', "ingotIron", 's', "treeSapling");
		}
	};
	public static BlockClassEntry<BlockTerraformer> terraformer = new BlockClassEntry<BlockTerraformer>(BlockTerraformer.class, TileTerraformer.class, TileTerraformerClimograph.class) {
		{
			if (ExtraUtils2.deobf_folder) {
				HashSet<BlockTerraformer.Type> validTypes = new HashSet<>();
				TObjectIntHashMap<BlockTerraformer.Type> sum = new TObjectIntHashMap<>();
				TObjectIntHashMap<BlockTerraformer.Type> num = new TObjectIntHashMap<>();
				TObjectIntHashMap<BlockTerraformer.Type> max = new TObjectIntHashMap<>(10, 0.5F, 0);
				TObjectIntHashMap<BlockTerraformer.Type> min = new TObjectIntHashMap<>(10, 0.5F, Integer.MAX_VALUE);
				HashMap<BlockTerraformer.Type, Pair<Biome, Biome>> curmax = new HashMap<>();
				HashMap<BlockTerraformer.Type, Pair<Biome, Biome>> curmin = new HashMap<>();
				for (Biome a : Biome.REGISTRY) {
					for (Biome b : Biome.REGISTRY) {
						if (a == b) continue;
						TObjectIntHashMap<BlockTerraformer.Type> requirements = TileTerraformer.getTransformationRequirements(a, b);
						for (BlockTerraformer.Type type : requirements.keySet()) {
							validTypes.add(type);
							num.adjustOrPutValue(type, 1, 1);
							int i = requirements.get(type);
							sum.adjustOrPutValue(type, i, i);
							if (i > max.get(type)) {
								max.put(type, i);
								curmax.put(type, Pair.of(a, b));
							}
							if (i < min.get(type)) {
								min.put(type, i);
								curmin.put(type, Pair.of(a, b));
							}
						}
					}
				}
				if (LogHelper.isDeObf)
					ExtraUtils2.proxy.run(new ClientRunnable() {
						@Override
						public void run() {
							StringBuilder builder = new StringBuilder("\n");
							for (BlockTerraformer.Type type : validTypes) {
								builder.append(type);
								builder.append(" - ");
								builder.append(sum.get(type) / num.get(type));
								builder.append(" - ");
								builder.append(num.get(type));
								builder.append(" - ");
								Pair<Biome, Biome> pair;
								pair = curmax.get(type);
								builder.append(max.get(type)).append(" (").append(pair.getLeft().getBiomeName()).append(",").append(pair.getRight().getBiomeName()).append(")");
								builder.append(" - ");
								pair = curmin.get(type);
								builder.append(min.get(type)).append(" (").append(pair.getLeft().getBiomeName()).append(",").append(pair.getRight().getBiomeName()).append(")");
								builder.append("\n");
							}
							LogHelper.debug(builder.toString());
							LogHelper.debug("");
						}
					});

			}

		}

		@Override
		public void addRecipes() {
			addShaped("terraformer_base", getBase(), "idi", "sms", "idi", 'm', XU2Entries.machineEntry.newStack(), 'i', "ingotIron", 'd', "gemDiamond", 's', "treeSapling");

			addShaped("terraformer_controller", getStack(BlockTerraformer.Type.CONTROLLER), "eme", "rcr", 'm', getBase(), 'r', "dustRedstone", 'c', Items.COMPARATOR, 'e', Items.ENDER_PEARL);
			addShaped("terraformer_antenna", getStack(BlockTerraformer.Type.ANTENNA), "e e", "isi", " i ", 'i', "ingotIron", 'e', Blocks.END_ROD, 's', "treeSapling");

			addClimographRecipe(BlockTerraformer.Type.HEATER, Items.LAVA_BUCKET);
			addClimographRecipe(BlockTerraformer.Type.COOLER, Items.SNOWBALL);
			addClimographRecipe(BlockTerraformer.Type.HUMIDIFIER, Items.WATER_BUCKET);
			addClimographRecipe(BlockTerraformer.Type.DEHUMIDIFIER, "sand");
			addClimographRecipe(BlockTerraformer.Type.MAGIC_ABSORBTION, Blocks.ANVIL);
			addClimographRecipe(BlockTerraformer.Type.MAGIC_INFUSER, Blocks.ENCHANTING_TABLE);
			addClimographRecipe(BlockTerraformer.Type.DEHOSTILIFIER, Blocks.MYCELIUM);
		}

		public void addClimographRecipe(BlockTerraformer.Type heater, Object lavaBucket) {
			addShapeless("terraformer_" + heater.toString().toLowerCase(), getStack(heater), getBase(), lavaBucket, lavaBucket);
		}

		@Nonnull
		public ItemStack getStack(BlockTerraformer.Type controller) {
			return TileTerraformer.getStack(controller);
		}

		@Nonnull
		public ItemStack getBase() {
			return getStack(BlockTerraformer.Type.CLIMOGRAPH_BASE);
		}
	};
	public static BlockClassEntry<BlockTrashChest> trashChest = new BlockClassEntry<BlockTrashChest>(BlockTrashChest.class, TileTrashChest.class) {
		@Override
		public void addRecipes() {
			addShapeless("trash_chest", newStack(), Blocks.CHEST, XU2Entries.trashCan, "dustRedstone");
		}
	};
	public static BlockClassEntry<BlockOpinium> openium = new BlockClassEntry<BlockOpinium>(BlockOpinium.class) {
		@Override
		public void addRecipes() {
			BlockOpinium.addRecipes();
		}
	};
	public static ItemClassEntry<ItemLawSword> lawSword = new ItemClassEntry<ItemLawSword>(ItemLawSword.class) {
		@Override
		public void addRecipes() {
			if (openium.isActive()) {
				addShaped("kikoku", newStack(), "i", "i", "s", 'i', openium.newStack(1, BlockOpinium.NUM_TIERS - 1), 's', Items.STICK);
			}
		}
	};
	public static VillagerEntrySimple shadyMerchant = new VillagerEntrySimple("shady_merchant") {
		@Override
		public List<XUVillagerCareer> getCareers() {
			MinecraftForge.EVENT_BUS.register(new EntityAINinjaPoof.Handler());

			return Lists.newArrayList(
					newCareer("shady_merchant")
							.addRandomTrade(0, new GenericTrade(GenericTrade.EMERALDS, 1, getFakeCopyStackToSell(1)).setMaxTrades(1))
							.addRandomTrade(0, new GenericTrade(GenericTrade.EMERALDS, 1, 3, getFakeCopyStackToSell(2)).setMaxTrades(1))
							.addRandomTrade(0, new GenericTrade(GenericTrade.EMERALDS, 5, 8, getFakeCopyStackToSell(3)).setMaxTrades(1))
							.addRandomTrade(0, new GenericTrade(GenericTrade.EMERALDS, 1, 1, getFakeCopyStackToSell(4)).setMaxTrades(1))
							.addRandomTrade(0, new GenericTrade(GenericTrade.EMERALDS, 4, 16, getFakeCopyStackToSell(5)).setMaxTrades(1))
							.addRandomTrade(0, new GenericTrade(GenericTrade.EMERALDS, 8, getFakeCopyStackToSell(6)).setMaxTrades(1))
							.addRandomTrade(0, new GenericTrade(GenericTrade.EMERALDS, 8, getFakeCopyStackToSell(7)).setMaxTrades(1))
							.addRandomTrade(0, new GenericTrade(GenericTrade.EMERALDS, 2, 6, getFakeCopyStackToSell(8)).setMaxTrades(1))
			);
		}

		public ItemStack getFakeCopyStackToSell(int meta) {
			Calendar calendar = Calendar.getInstance();
			if (calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1 && meta != 3 && XURandom.rand.nextBoolean()) {
				ItemFakeCopy.getOriginalStack(meta);
			}
			return itemFakeCopy.newStackMeta(meta);
		}
	};
	public static VoidEntry squidSpawnRestrictions = new VoidEntry("squidSpawnRestrictions") {
		@Override
		public void postInit() {
			MinecraftForge.EVENT_BUS.register(SquidSpawnRestrictions.class);
		}
	};
	public static ItemClassEntry<ItemFlatTransferNode> flatTransferNode = new ItemClassEntry<ItemFlatTransferNode>(ItemFlatTransferNode.class) {
		@Override
		public void addRecipes() {
			CraftingHelper.addRecipe(new AnvilRecipe(CraftingHelper.createLocation("transfer_flatnode_items"), newStack(8, 0), "a", " ", "n", 'a', Blocks.ANVIL, 'n', grocket.newStack(1, GrocketType.TRANSFER_NODE_ITEMS.ordinal())));
			CraftingHelper.addRecipe(new AnvilRecipe(CraftingHelper.createLocation("transfer_flatnode_fluids"), newStack(8, 1), "a", " ", "n", 'a', Blocks.ANVIL, 'n', grocket.newStack(1, GrocketType.TRANSFER_NODE_FLUIDS.ordinal())));
		}
	};
	public static BlockClassEntry<BlockAnalogCrafter> analogCrafter = new BlockClassEntry<BlockAnalogCrafter>(BlockAnalogCrafter.class, TileAnalogCrafter.class) {
		@Override
		public void addRecipes() {
			addShapeless("crafter_analog", newStack(), Blocks.CRAFTING_TABLE, Blocks.CHEST, Blocks.LEVER);
		}
	};
	public static BlockClassEntry<BlockDecorativeBedrock> decorativeBedrock = new BlockClassEntry<BlockDecorativeBedrock>(BlockDecorativeBedrock.class) {
		@Override
		public void addRecipes() {

		}
	};
	public static Entry<WorldWall> wallWorldEntry = new Entry<WorldWall>("wall_world_type") {
		@Override
		protected WorldWall initValue() {
			return new WorldWall();
		}

		@Override
		public void loadAdditionalConfig(Configuration config) {
			WorldWall.giveSpawnItems = config.get("Settings", "WallWorld_GiveSpawnItems", true).getBoolean();
		}
	};
	public static ItemClassEntry<ItemCompoundBow> compoundBow = new ItemClassEntry<ItemCompoundBow>(ItemCompoundBow.class) {
		@Override
		public void addRecipes() {
			if (openium.isActive()) {
				addShaped("compound_bow", newStack(), true, " iS", "s S", " iS", 'i', openium.newStack(1, BlockOpinium.NUM_TIERS - 1), 's', "ingotIron", 'S', "string");
			}
		}
	};
	public static ItemClassEntry<ItemFireAxe> fireAxe = new ItemClassEntry<ItemFireAxe>(ItemFireAxe.class) {
		@Override
		public void addRecipes() {
			if (openium.isActive()) {
				addShaped("fire_axe", newStack(), true, "ii", "is", " s", 'i', openium.newStack(1, BlockOpinium.NUM_TIERS - 1), 's', Items.STICK);
			}
		}
	};

	public static void init() {

	}


}
