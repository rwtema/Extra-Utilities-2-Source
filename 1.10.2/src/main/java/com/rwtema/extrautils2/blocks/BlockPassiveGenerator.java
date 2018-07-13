package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.achievements.AchievementHelper;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.power.PowerMultipliers;
import com.rwtema.extrautils2.structure.PatternRecipe;
import com.rwtema.extrautils2.tile.TilePassiveGenerator;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.tile.TilePowerHandCrank;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BlockPassiveGenerator extends XUBlockStatic {
	public static final PropertyEnumSimple<GeneratorType> GENERATOR_TYPE = new PropertyEnumSimple<>(GeneratorType.class);
	public static final int BASE_SOLAR_VALUE = 2;
	private static final float LOSS_1 = 0.6666666F;
	private static final float LOSS_2 = 0.5F;
	private static final float LOSS_3 = 0.2F;
	private static final float LOSS_4 = 0.1F;

	public BlockPassiveGenerator() {
		super(Material.ROCK);
	}

	private static int getBaseSolarPower(World world) {
		return world.isDaytime() ? BASE_SOLAR_VALUE : 0;
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this)
				.addDropProperties(GENERATOR_TYPE)
				.addMetaProperty(TilePower.ENABLED_STATE)
				.build();
	}

	@Override
	public BoxModel getModel(IBlockState baseState) {
		GeneratorType value = baseState.getValue(GENERATOR_TYPE);
		BoxModel model = new BoxModel();
		value.createModel(model, false);
		return model;
	}

	public BoxModel getModelInv(IBlockState baseState) {
		GeneratorType value = baseState.getValue(GENERATOR_TYPE);
		BoxModel model = new BoxModel();
		value.createModel(model, true);
		return model;
	}


	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return state.getValue(GENERATOR_TYPE).createTileEntity(world);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public BlockRenderLayer getBlockLayer() {
		return super.getBlockLayer();
	}

	@Override
	public boolean canReplaceBase(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side, ItemStack stack) {
		if (!super.canReplaceBase(worldIn, pos, side, stack))
			return false;
		IBlockState state = xuBlockState.getStateFromItemStack(stack);

		return state.getValue(GENERATOR_TYPE).validPos(worldIn, pos);
	}

	@Override
	public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
		if (!state.getValue(GENERATOR_TYPE).validPos(worldIn, pos)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			return;
		}

		super.neighborChangedBase(state, worldIn, pos, neighborBlock);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		xuBlockState.getStateFromItemStack(stack).getValue(GENERATOR_TYPE).addInformation(stack, playerIn, tooltip, advanced);
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		if (blockState == getDefaultState().withProperty(GENERATOR_TYPE, GeneratorType.CREATIVE))
			return -1;
		return super.getBlockHardness(blockState, worldIn, pos);
	}

	public enum GeneratorType implements IItemStackMaker, IWorldPowerMultiplier {
		SOLAR(PowerMultipliers.SOLAR, 80, LOSS_1, 160, LOSS_2, 320, LOSS_3) {
			@Override
			public void registerAchievements() {
				AchievementHelper.addAchievement("Solar Panel", "The sun gives power to all!", this, ItemIngredients.Type.REDSTONE_CRYSTAL);
			}

			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				if (!world.canSeeSky(generator.getPos().up())) return 0;
				return 1;
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 4, 16).setTextureSides("panel_side", 0, "panel_bottom", 1, "panel_solar");
			}

			@Override
			public boolean validPos(World world, BlockPos pos) {
				return world.isSideSolid(pos.down(), EnumFacing.UP);
			}

			@Override
			public void addRecipe() {
				CraftingHelper.addShaped("mill_solar", newStack(3), "LLL", "BRB", 'L', "gemLapis", 'R', ItemIngredients.Type.REDSTONE_CRYSTAL.newStack(1), 'B', BlockDecorativeSolid.DecorStates.stoneslab.newStack(1));
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Gives power during daylight hours."));
				tooltip.add(Lang.translate("Must have clear line of sight to sky."));
				tooltip.add(Lang.translate("Power reduced by rain."));
				super.addInformation(stack, playerIn, tooltip, advanced);
			}
		},
		LUNAR(PowerMultipliers.LUNAR, 80, LOSS_1, 160, LOSS_2, 320, LOSS_3) {
			@Override
			public void registerAchievements() {
				AchievementHelper.addAchievement("Lunar Panel", "The moon gives power to all!", this, ItemIngredients.Type.DYE_POWDER_LUNAR);
			}

			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				if (!world.canSeeSky(generator.getPos().up())) return 0;
				return 1;
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 4, 16).setTexture("panel_side").setTextureSides(0, "panel_bottom", 1, "panel_lunar");
			}

			@Override
			public boolean validPos(World worldIn, BlockPos pos) {
				return worldIn.isSideSolid(pos.down(), EnumFacing.UP);
			}

			@Override
			public void addRecipe() {
				CraftingHelper.addShaped("mill_lunar", newStack(3), "LLL", "BrB", 'L', ItemIngredients.Type.DYE_POWDER_LUNAR.newStack(1), 'r', ItemIngredients.Type.REDSTONE_CRYSTAL.newStack(1), 'B', BlockDecorativeSolid.DecorStates.stoneslab.newStack(1));
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Gives power during night hours."));
				tooltip.add(Lang.translate("Must have clear line of sight to sky."));
				tooltip.add(Lang.translate("Power boosted by full moon."));
				tooltip.add(Lang.translate("Power reduced by rain."));
				super.addInformation(stack, playerIn, tooltip, advanced);
			}
		},
		LAVA(IWorldPowerMultiplier.CONSTANT, 200, LOSS_1, 400, LOSS_2, 800, LOSS_3) {
			@Override
			public void registerAchievements() {
				AchievementHelper.addAchievement("Lava XUMachineGenerators", "Adjacent heat! It burns!", this, BlockDecorativeSolid.DecorStates.stoneburnt);
			}

			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				float h = 0;
				for (EnumFacing enumFacing : EnumFacing.HORIZONTALS) {
					BlockPos offset = generator.getPos().offset(enumFacing);
					IBlockState blockState = generator.getWorld().getBlockState(offset);
					Block block = blockState.getBlock();
					if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
						h = Math.max(h, 8 - blockState.getValue(BlockLiquid.LEVEL));
						if (h == 8)
							return 2;
					}
				}

				return h / 2;
			}

			@Override
			public float basePowerGen() {
				return 4;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Operates when adjacent to lava."));
				super.addInformation(stack, playerIn, tooltip, advanced);
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 16, 16, "panel_base").setTextureSides(0, "panel_lava", 1, "panel_lava");
			}

			@Override
			public void addRecipe() {
				CraftingHelper.addShaped("mill_lava", newStack(1), "BBB", "BRB", "BgB", 'g', "ingotGold", 'R', ItemIngredients.Type.REDSTONE_CRYSTAL.newStack(1), 'B', BlockDecorativeSolid.DecorStates.stoneburnt.newStack(1));
				PatternRecipe.register(
						new String[][]{{"bbb", "bbb", "bbb"}, {" g ", "glg", " g "}},
						ImmutableMap.<Character, Object>builder().put('l', BlockStates.LAVA_LEVEL_0).put('g', getMyBlockState()).put('b', Blocks.BARRIER).build(),
						newStack()
				);
			}
		},
		WATER(IWorldPowerMultiplier.CONSTANT, 64, LOSS_1, 128, LOSS_2, 512, LOSS_4) {
			@Override
			public float basePowerGen() {
				return 4;
			}

			@Override
			public void registerAchievements() {
				AchievementHelper.addAchievement("Water Mill", "The Flow of Water", this, BlockDecorativeSolid.DecorStates.stoneburnt);
			}

			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				float v = 0;
				for (EnumFacing enumFacing : EnumFacing.HORIZONTALS) {
					BlockPos offset = generator.getPos().offset(enumFacing);
					IBlockState blockState = world.getBlockState(offset);
					Block block = blockState.getBlock();
					if (isWater(block)) {
						if (isWater(world.getBlockState(offset.up()).getBlock()))
							continue;
						int value = 8 - blockState.getValue(BlockLiquid.LEVEL);
						if (value >= 8) continue;
						v += (value + 1) / 2;
					}
				}

				return v;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Gives GP for adjacent flowing water blocks."));
				tooltip.add(Lang.translate("The higher the level, the more GP."));
				tooltip.add(Lang.translate("Source/full water blocks do not count."));
				super.addInformation(stack, playerIn, tooltip, advanced);
//				tooltip.add(Lang.translateArgs("Max Power: %s GP", 4));
			}

			private boolean isWater(Block block) {
				return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 16, 16, "panel_base").setTextureSides(1, "panel_water");
				model.addBoxOverlay(0).setTexture("fan_spinning_small").setInvisible(1 | 2).setLayer(BlockRenderLayer.CUTOUT);
			}

			@Override
			public void addRecipe() {
				CraftingHelper.addShaped("mill_water", newStack(1), "BBB", "gRg", "BBB", 'g', ItemIngredients.Type.REDSTONE_GEAR.newStack(1), 'R', ItemIngredients.Type.REDSTONE_CRYSTAL.newStack(1), 'B', BlockDecorativeSolid.DecorStates.stoneburnt.newStack(1));
				PatternRecipe.register(new String[][]{{"bbbbb", "bbbbb", "bbbbb", "bbbbb", "bbbbb"}, {"bbbbb", "bWwbb", "bwgwb", "bbwWb", "bbbbb"}},
						ImmutableMap.<Character, Object>builder().put('W', BlockStates.WATER_LEVEL_0).put('w', BlockStates.WATER_LEVEL_1).put('g', getMyBlockState()).put('b', Blocks.BARRIER).build(),
						newStack());
			}
		},
		WIND(PowerMultipliers.WIND, 512, LOSS_2, 1024, LOSS_3) {
			@Override
			public void registerAchievements() {
				AchievementHelper.addAchievement("Wind Mill", "Winds light to variable", this, BlockDecorativeSolid.DecorStates.stoneburnt);
			}

			EnumFacing[] sides = new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.NORTH};

			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				for (EnumFacing enumFacing : sides) {
					if (!world.isAirBlock(generator.getPos().offset(enumFacing))) {
						return 0;
					}
				}

				return basePowerGen();
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 1, 16, "panel_base").setTextureSides(0, "panel_air");
				model.addBoxI(0, 15, 0, 16, 16, 16, "panel_base").setTextureSides(1, "panel_air");
				model.addBoxI(0, 1, 0, 1, 15, 16, "panel_base");
				model.addBoxI(15, 1, 0, 16, 15, 16, "panel_base");

				model.addBoxI(1, 1, 8, 15, 15, 8, "fan_spinning").setInvisible(true, true, false, false, true, true).setLayer(BlockRenderLayer.CUTOUT);
			}

			@Override
			public void addRecipe() {
				CraftingHelper.addShaped("mill_wind", newStack(1), true, "BBB", " gR", "BBB", 'g', ItemIngredients.Type.REDSTONE_GEAR.newStack(1), 'R', ItemIngredients.Type.REDSTONE_CRYSTAL.newStack(1), 'B', BlockDecorativeSolid.DecorStates.stoneburnt.newStack(1));
			}

			@Override
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Generates power from wind."));
				tooltip.add(Lang.translate("Power boosted by rain."));
				tooltip.add(Lang.translate("North/south blocks must be clear"));
				super.addInformation(stack, playerIn, tooltip, advanced);
			}
		},
		FIRE(IWorldPowerMultiplier.CONSTANT, 40, LOSS_1, 320, LOSS_2, 640, LOSS_3) {
			@Override
			public void registerAchievements() {
				AchievementHelper.addAchievement("Fire Mill", "Rising Heat!", this, BlockDecorativeSolid.DecorStates.stoneburnt);
			}

			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				IBlockState blockState = world.getBlockState(generator.getPos().down());
				if (blockState.getBlock() != Blocks.FIRE) return 0;
				return 4;
			}

			@Override
			public float basePowerGen() {
				return 4;
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 16, 1, "panel_base").setTextureSides(2, "panel_fire");
				model.addBoxI(0, 0, 15, 16, 16, 16, "panel_base").setTextureSides(3, "panel_fire");
				model.addBoxI(0, 0, 1, 1, 16, 15, "panel_base").setTextureSides(4, "panel_fire");

				model.addBoxI(15, 0, 1, 16, 16, 15, "panel_base").setTextureSides(5, "panel_fire");
				model.addBoxI(1, 8, 1, 15, 8, 15, "fan_spinning").setInvisible(false, false, true, true, true, true).setLayer(BlockRenderLayer.CUTOUT);

			}

			@Override
			public void addRecipe() {
				CraftingHelper.addShaped("mill_fire", newStack(1), true, "BRB", "BgB", "BNB", 'g', ItemIngredients.Type.REDSTONE_GEAR.newStack(1), 'R', ItemIngredients.Type.REDSTONE_CRYSTAL.newStack(1), 'B', BlockDecorativeSolid.DecorStates.stoneburnt.newStack(1), 'N', Blocks.NETHER_BRICK_FENCE);
				PatternRecipe.register(new String[][]{{"n"}, {"f"}, {"g"}}, ImmutableMap.<Character, Object>builder().put('n', Blocks.NETHERRACK).put('f', Blocks.FIRE).put('g', getMyBlockState()).put('b', Blocks.BARRIER).build());
			}

			@Override
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Operates when placed over fire."));
				super.addInformation(stack, playerIn, tooltip, advanced);
			}
		},
		CREATIVE(IWorldPowerMultiplier.CONSTANT) {
			@Override
			public void registerAchievements() {

			}

			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				return 10000;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Creative-only item."));
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 16, 16, "panel_creative").setTextureSides(0, "panel_creative_top", 1, "panel_creative_top");
			}

			@Override
			public void addRecipe() {

			}
		}, PLAYER_WIND_UP(IWorldPowerMultiplier.CONSTANT) {
			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				return 1;
			}

			@Override
			public float basePowerGen() {
				return 15;
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 6, 16, "panel_stone_side").setTextureSides(0, "panel_stone_base", 1, "panel_stone_base");
				if (isInv) {
					model.addBoxI(6, 6, 6, 10, 10, 10, "redstone_gear");
					model.addBoxI(2, 7, 7, 14, 9, 9, "redstone_gear");
					model.addBoxI(7, 7, 2, 9, 9, 14, "redstone_gear");
					model.addBoxI(1, 7, 1, 15, 9, 15, "redstone_gear").setInvisible(~(1 | 2));
					model.addBoxI(1, 6, 7, 15, 10, 9, "redstone_gear").setInvisible(~(4 | 8));
					model.addBoxI(7, 6, 1, 9, 10, 15, "redstone_gear").setInvisible(~(16 | 32));
				} else {
					model.addBoxI(1, 6, 1, 15, 10, 15, "redstone_gear").setInvisible(-1);
				}
			}

			@Override
			public void addRecipe() {
				CraftingHelper.addShaped("mill_manual", newStack(), " R ", "BCB", 'R', ItemIngredients.Type.REDSTONE_GEAR, 'B', BlockDecorativeSolid.DecorStates.stoneslab, 'C', ItemIngredients.Type.REDSTONE_CRYSTAL);
			}

			@Override
			public void registerAchievements() {
				AchievementHelper.addAchievement("Manual Mill", "Hold right click to temporarily generate power.", this, ItemIngredients.Type.REDSTONE_CRYSTAL);
			}

			@Override
			public TilePassiveGenerator createTileEntity(World world) {
				return new TilePowerHandCrank();
			}
		}, DRAGON_EGG(IWorldPowerMultiplier.CONSTANT, 500, 0.5F, 1000, 0.25F, 1500, 0.05F) {
			@Override
			public float getPowerLevel(TilePassiveGenerator generator, World world) {
				IBlockState blockState = world.getBlockState(generator.getPos().up());
				if (blockState.getBlock() != Blocks.DRAGON_EGG) return 0;
				return 500;
			}

			//			@Override
//			public float getCap() {
//				return 500;
//			}
			@Override
			public float basePowerGen() {
				return 500;
			}

			@Override
			public void createModel(BoxModel model, boolean isInv) {
				model.addBoxI(0, 0, 0, 16, 16, 16, "panel_egg_side").setTextureSides(0, "panel_egg", 1, "panel_egg");
			}

			@Override
			public void addRecipe() {
				CraftingHelper.addShaped("mill_dragon", newStack(1), true, "BEB", "NgN", "BRB", 'g', ItemIngredients.Type.REDSTONE_GEAR.newStack(1), 'R', ItemIngredients.Type.EYE_REDSTONE.newStack(1), 'B', BlockDecorativeSolid.DecorStates.stoneburnt.newStack(1), 'N', Items.NETHER_STAR, 'E', Items.ENDER_PEARL);
				PatternRecipe.register(new String[][]{{"g"}, {"d"}},
						ImmutableMap.<Character, Object>builder().put('d', Blocks.DRAGON_EGG).put('g', getMyBlockState()).put('b', Blocks.BARRIER).build(),
						newStack());
			}

			@Override
			public void registerAchievements() {

			}
		};

		public final IWorldPowerMultiplier powerMultiplier;
		public final Collection<ResourceLocation> types;
		final ResourceLocation key;
		@Nullable
		private final TreeMap<Float, Pair<Float, Float>> caps;

		GeneratorType(IWorldPowerMultiplier powerMultiplier, float... capsInput) {
			this.powerMultiplier = powerMultiplier;
			key = new ResourceLocation("generators", name());
			types = ImmutableList.of(new ResourceLocation("generators", "any"), key);

			float[][] caps = new float[capsInput.length / 2][2];
			for (int i = 0; i < capsInput.length; i += 2) {
				caps[i / 2][0] = capsInput[i];
				caps[i / 2][1] = capsInput[i + 1];
			}
			this.caps = IWorldPowerMultiplier.createCapsTree(caps);
		}


		public ItemStack newStack(int i) {
			return XU2Entries.passiveGenerator.newStack(i, GENERATOR_TYPE, this);
		}

		public abstract float getPowerLevel(TilePassiveGenerator generator, World world);

		public abstract void createModel(BoxModel model, boolean isInv);

		public boolean validPos(World worldIn, BlockPos pos) {
			return true;
		}

		public abstract void addRecipe();

		public IBlockState getMyBlockState() {
			return XU2Entries.passiveGenerator.value.getDefaultState().withProperty(GENERATOR_TYPE, this);
		}

		public float basePowerGen() {
			return 1;
		}

		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
			if (powerMultiplier != IWorldPowerMultiplier.CONSTANT) {
				tooltip.add(Lang.translateArgs("Base Power Given: %s GP", basePowerGen()));
				WorldClient theWorld = Minecraft.getMinecraft().world;
				if (theWorld != null)
					tooltip.add(Lang.translateArgs("Cur Power Given: %s GP", StringHelper.niceFormat(basePowerGen() * powerMultiplier.multiplier(theWorld))));
			} else {
				tooltip.add(Lang.translateArgs("Power Given: %s GP", basePowerGen()));
			}
			if (caps != null) {
				String reset = ChatFormatting.RESET.toString() + ChatFormatting.GRAY.toString();
				String tab = "   ";
				if (!(Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54))) {
					ChatFormatting col = ChatFormatting.DARK_GRAY;
					tooltip.add(col + Lang.translate("Progressive Efficiency Loss:"));
					tooltip.add(col.toString() + ChatFormatting.ITALIC + tab + Lang.translate("<Press Shift for more details>") + reset);
				} else {
					ChatFormatting col = ChatFormatting.GRAY;
					tooltip.add(col + Lang.translate("Progressive Efficiency Loss:") + reset);
					for (Map.Entry<Float, Pair<Float, Float>> entry : caps.entrySet()) {
						Float lower = entry.getKey();
						Float upper = caps.higherKey(lower);
						String lowerFormat = StringHelper.format(lower);
						String efficiencyFormat = StringHelper.formatPercent(1 - entry.getValue().getValue());
						if (upper == null) {
							tooltip.add(col + tab + Lang.translateArgs("Above %s GP: %s", lowerFormat, efficiencyFormat) + reset);
						} else {
							tooltip.add(col + tab + Lang.translateArgs("%s GP to %s GP: %s", lowerFormat, StringHelper.format(upper), efficiencyFormat) + reset);
						}
					}
				}
			}
		}

		@Override
		public ItemStack newStack() {
			return newStack(1);
		}


		public abstract void registerAchievements();

		public TilePassiveGenerator createTileEntity(World world) {
			return new TilePassiveGenerator();
		}


		@Override
		public float multiplier(World world) {
			return powerMultiplier.multiplier(world);
		}

		@Override
		public IWorldPowerMultiplier getStaticVariation() {
			return this;
		}

		@Override
		public float alterTotal(float value) {
			return IWorldPowerMultiplier.capPower(value, caps);
		}

		@Override
		public boolean hasInefficiencies() {
			return true;
		}
	}
}
