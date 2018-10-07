package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.compatibility.XUShapedRecipe;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.particles.PacketParticleSplineCurve;
import com.rwtema.extrautils2.recipes.GenericMachineRecipe;
import com.rwtema.extrautils2.recipes.SingleInputStackToStackRecipeCached;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import com.rwtema.extrautils2.utils.helpers.VecHelper;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

public class MachineInit {

	static HashSet<String> registeredDusts = Sets.newHashSet(
			"dustIron",
			"dustGold",
			"dustCopper",
			"dustTin",
			"dustLead",
			"dustSilver",
			"dustNickel",
			"dustPlatinum"
	);
	private static MachineSlotItem SLOT_SLIME_SECONDARY;


	@SubscribeEvent
	public static void onOreRegister(OreDictionary.OreRegisterEvent event) {
		String name = event.getName();
		if (name.startsWith("ingot") || name.startsWith("ore") || name.startsWith("dust")) {
			Set<String> oreNames = Sets.newHashSet(OreDictionary.getOreNames());
			for (String dust : oreNames) {
				if (!dust.startsWith("dust") || registeredDusts.contains(dust))
					continue;

				String ore = Pattern.compile("dust", Pattern.LITERAL).matcher(dust).replaceFirst("ore");
				String ingot = Pattern.compile("dust", Pattern.LITERAL).matcher(dust).replaceFirst("ingot");

				if (oreNames.contains(ore) && oreNames.contains(ingot)) {
					addCrusherOreRecipe(ore, dust, 2);
					addCrusherOreRecipe(ingot, dust, 1);
					registeredDusts.add(dust);
				}
			}
		}
	}


	public static void init() {
		XUMachineEnchanter.INSTANCE = new Machine("extrautils2:enchanter", 100000, 1000, ImmutableList.of(XUMachineEnchanter.INPUT, XUMachineEnchanter.INPUT_LAPIS), ImmutableList.of(), ImmutableList.of(XUMachineEnchanter.OUTPUT), ImmutableList.of(), "extrautils2:machine/enchanter_off", "extrautils2:machine/enchanter_on", Machine.EnergyMode.USES_ENERGY, 0xffffff, null, null, null,
//				"extrautils2:machine/enchanter_top"
				null
		) {
			@Override
			public float getSpeed(World world, BlockPos pos, TileMachine tileMachine) {
				float power = 0;

				for (int dx = -1; dx <= 1; dx++) {
					for (int dz = -1; dz <= 1; dz++) {
						if (dx == 0 && dz == 0) continue;
						if (isPassable(world, pos.add(dz, 0, dx)) && isPassable(world, pos.add(dz, 1, dx))) {
							power += ForgeHooks.getEnchantPower(world, pos.add(dz * 2, 0, dx * 2));
							power += ForgeHooks.getEnchantPower(world, pos.add(dz * 2, 1, dx * 2));
							if (dz != 0 && dx != 0) {
								power += ForgeHooks.getEnchantPower(world, pos.add(dz * 2, 0, dx));
								power += ForgeHooks.getEnchantPower(world, pos.add(dz * 2, 1, dx));
								power += ForgeHooks.getEnchantPower(world, pos.add(dz, 0, dx * 2));
								power += ForgeHooks.getEnchantPower(world, pos.add(dz, 1, dx * 2));
							}
							if (power >= 15)
								return 1;
						}
					}
				}
				if (power < 15)
					return 0;

				return 1;
			}

			public boolean isPassable(World world, BlockPos pos) {
				return !world.isBlockFullCube(pos);
			}

			@Nullable
			@Override
			public String getRunError(World world, BlockPos pos, TileMachine tileMachine, float speed) {
				if (speed != 0) return null;
				return Lang.translate("Enchanter requires a full set of nearby bookshelves, or other enchantment boosting blocks");
			}

			@Override
			public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand, TileMachine tileMachine) {
				for (int i = 0; i < 3; i++) {
					Blocks.ENCHANTING_TABLE.randomDisplayTick(
							Blocks.ENCHANTING_TABLE.getDefaultState(),
							worldIn,
							pos,
							rand);
				}
			}
		};

		XUMachineEnchanter.INSTANCE.recipes_registry.addRecipe(new MechEnchantmentRecipe(5 * 60 * 20, OreDictionary.getOres("gemLapis"), MechEnchantmentRecipe.EnchantType.LOWEST));
		XUMachineEnchanter.INSTANCE.recipes_registry.addRecipe(new MechEnchantmentRecipe(30 * 60 * 20, OreDictionary.getOres("netherStar"), MechEnchantmentRecipe.EnchantType.HIGHEST));

		MinecraftForge.EVENT_BUS.register(MachineInit.class);
		RecipeBuilder.Builder.builder = GenericMachineRecipe.Builder::new;
		XUMachineGenerators.FURNACE_GENERATOR = new Machine("extrautils2:generator", 10000, 1000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0xffffff, null, null, null, null);
		XUMachineGenerators.SURVIVALIST_GENERATOR = new Machine(
				"extrautils2:generator_survival", 10000,
				1000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM),
				ImmutableList.of(),
				ImmutableList.of(),
				ImmutableList.of(),
				"extrautils2:machine/generator_off",
				"extrautils2:machine/generator_on",
				Machine.EnergyMode.GENERATES_ENERGY,
				0xffffff,
				"minecraft:blocks/furnace_top",
				"minecraft:blocks/furnace_side",
				"minecraft:blocks/furnace_top",
				null
		);
		XUMachineGenerators.CULINARY_GENERATOR = new Machine("extrautils2:generator_culinary", 100000, 8000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0xffffff, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_culinary");
		XUMachineGenerators.POTION_GENERATOR = new Machine("extrautils2:generator_potion", 100000, 1000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0x5411b1, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_potion");
		XUMachineGenerators.TNT_GENERATOR = new Machine("extrautils2:generator_tnt", 100000, 1000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0xdb441a, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_tnt") {
			@Override
			public void processingTick(TileEntity tileMachine, IMachineRecipe curRecipe, float processTime, int n) {
				World world = tileMachine.getWorld();
				Random rand = world.rand;
				for (int j = 0; j < n; j++) {
					if (rand.nextInt(1 + 40) == 0) {
						BlockPos pos = tileMachine.getPos();
						for (int i = 0; i < 10; i++) {
							double v = 1.5;
							double x = pos.getX() + 0.5 + v * rand.nextGaussian();
							double y = pos.getY() + 0.5 + v * rand.nextGaussian();
							double z = pos.getZ() + 0.5 + v * rand.nextGaussian();
							BlockPos otherPos = new BlockPos(x, y, z);
							if (i == 9 || !pos.equals(otherPos)) {
								IBlockState state = world.getBlockState(otherPos);
								if (i >= 5 || (state.getBlock().getExplosionResistance(null) == 0)) {
									world.newExplosion(null,
											x,
											y,
											z,
											2, false, false);
									return;
								}
							}
						}
					}
				}
			}
		};
		XUMachineGenerators.LAVA_GENERATOR = new Machine("extrautils2:generator_lava", 100000, 1000, ImmutableList.of(), ImmutableList.of(XUMachineGenerators.INPUT_FLUID), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0x991522, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", null);
		XUMachineGenerators.PINK_GENERATOR = new Machine("extrautils2:generator_pink", 100000, 100, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0xff4550, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_pink");
		XUMachineGenerators.NETHERSTAR_GENERATOR = new Machine(
				"extrautils2:generator_netherstar", 400000,
				400000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM),
				ImmutableList.of(),
				ImmutableList.of(),
				ImmutableList.of(),
				"extrautils2:machine/generator_off",
				"extrautils2:machine/generator_on",
				Machine.EnergyMode.GENERATES_ENERGY,
				0xffffff,
				"extrautils2:machine/generator/machine_base_netherstar",
				"extrautils2:machine/generator/machine_base_netherstar_side",
				"extrautils2:machine/generator/machine_base_netherstar_bottom",
				"extrautils2:machine/generator/generator_netherstar"
		) {
			@Override
			public void processingTick(TileEntity tileMachine, IMachineRecipe curRecipe, float processTime, int n) {
				World world = tileMachine.getWorld();
				BlockPos pos = tileMachine.getPos();
				for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, getRadius(pos))) {
					if (!(entity instanceof EntityEndermite))
						entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 20 * 20 + 10, (int) Math.floor(Math.sqrt(5 + n)) - 1));
				}
			}

			@Nonnull
			private AxisAlignedBB getRadius(BlockPos pos) {
				return new AxisAlignedBB(pos).grow(5, 5, 5);
			}

			@Override
			public void clientTick(TileEntity tileMachine, boolean active) {
				if (active) {
					BlockPos pos = tileMachine.getPos();
					AxisAlignedBB radius = getRadius(pos);
					for (int i = 0; i < 2; i++) {
						spawnParticlesNearby(tileMachine.getWorld(), radius, 0.1F, 0.1F, 0.1F);
					}
				}
			}
		};
		XUMachineGenerators.ENDER_GENERATOR = new Machine("extrautils2:generator_ender", 100000, 4000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0x258474, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_ender");
		XUMachineGenerators.REDSTONE_GENERATOR = new Machine("extrautils2:generator_redstone", 100000, 1600, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(XUMachineGenerators.INPUT_FLUID), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0xaa4e03, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_redstone");
		XUMachineGenerators.OVERCLOCK_GENERATOR = new Machine("extrautils2:generator_overclock", 1000000, 1000000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0x1b0fb0, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_overclock") {
			@Override
			public void processingTick(TileEntity tileMachine, IMachineRecipe curRecipe, float processTime, int n) {
//				World world = tileMachine.getWorld();
//				if (world.rand.nextInt(30) == 0) {
//					BlockPos pos = tileMachine.getPos();
//					for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).expand(1, 1, 1))) {
//						entity.setFire(1);
//					}
//				}
			}
		};
		XUMachineGenerators.DRAGON_GENERATOR = new Machine("extrautils2:generator_dragonsbreath", 1000000, 8000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0xa77aa7, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", null) {
			@Override
			public void processingTick(TileEntity tileMachine, IMachineRecipe curRecipe, float processTime, int n) {
				World world = tileMachine.getWorld();
				if (world.getDifficulty() == EnumDifficulty.PEACEFUL) {
					return;
				}
				if (world.rand.nextInt(60 * 20) == 0) {
					BlockPos pos = tileMachine.getPos();
					double x = tileMachine.getPos().getX() + 0.5 + world.rand.nextGaussian() * 3;
					double y = tileMachine.getPos().getY() + 0.5 + world.rand.nextGaussian() * 3;
					double z = tileMachine.getPos().getZ() + 0.5 + world.rand.nextGaussian() * 3;

					EntityEndermite entityEndermite = new EntityEndermite(world);
					entityEndermite.setLocationAndAngles(
							x,
							y,
							z,
							MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);

					entityEndermite.rotationYawHead = entityEndermite.rotationYaw;
					entityEndermite.renderYawOffset = entityEndermite.rotationYaw;
					entityEndermite.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityEndermite)), null);
					entityEndermite.experienceValue = 0;
					if (entityEndermite.isNotColliding()) {
						world.spawnEntity(entityEndermite);
						entityEndermite.playLivingSound();

						NetworkHandler.sendToAllAround(
								new PacketParticleSplineCurve(
										new Vec3d(pos).addVector(0.5, 0.5, 0.5),
										new Vec3d(entityEndermite.posX, entityEndermite.posY, entityEndermite.posZ),
										VecHelper.randUnitVec(world.rand),
										VecHelper.randUnitVec(world.rand),
										0xffff00ff
								), world.provider.getDimension(),
								pos.getX(),
								pos.getY(),
								pos.getZ(),
								32);

					} else {
						entityEndermite.setDead();
					}

				}
			}
		};
		XUMachineGenerators.ICE_GENERATOR = new Machine("extrautils2:generator_ice", 100000, 1000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator/generator_on_ice", Machine.EnergyMode.GENERATES_ENERGY, 0x4e6c9f, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", null) {
			@Override
			public void processingTick(TileEntity tileMachine, IMachineRecipe curRecipe, float processTime, int n) {
				World world = tileMachine.getWorld();
				BlockPos pos = tileMachine.getPos();
				BlockPos.MutableBlockPos newPos = new BlockPos.MutableBlockPos();
				final int dist = 9;
				for (int i = 0; i < n; i++) {
					if (world.rand.nextInt(1024) == 0) {
						for (int i1 = 0; i1 < 40; i1++) {
							double d;
							do {
								newPos.setPos(
										pos.getX() + world.rand.nextInt(dist * 2 + 1) - dist,
										pos.getY() + world.rand.nextInt(dist * 2 + 1) - dist,
										pos.getZ() + world.rand.nextInt(dist * 2 + 1) - dist);
							} while ((d = newPos.distanceSq(pos)) > dist * dist);

							IBlockState blockState = world.getBlockState(newPos);
							Block block = blockState.getBlock();
							if (block.isAir(blockState, world, newPos)) {
								if (Blocks.SNOW_LAYER.canPlaceBlockAt(world, newPos)) {
									world.setBlockState(newPos, Blocks.SNOW_LAYER.getDefaultState());
									break;
								}
							} else if (blockState.getMaterial() == Material.WATER && (block == Blocks.WATER || block == Blocks.FLOWING_WATER)) {
								if (blockState.getValue(BlockLiquid.LEVEL) == 0) {
									world.setBlockState(newPos, Blocks.ICE.getDefaultState());
									break;
								}
							} else if (block == Blocks.SNOW_LAYER) {
								int value = blockState.getValue(BlockSnow.LAYERS);

								if (value < 8 && ((dist - Math.sqrt(d)) * 1.25 > value)) {
									world.setBlockState(newPos, blockState.withProperty(BlockSnow.LAYERS, value + 1));
									break;
								}
							}
						}
					}
				}
			}
		};
		XUMachineGenerators.DEATH_GENERATOR = new Machine("extrautils2:generator_death", 100000, 1000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0xd8cd9c, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_death") {
			@Override
			public void processingTick(TileEntity tileMachine, IMachineRecipe curRecipe, float processTime, int n) {
				if (!XU2Entries.potionDoom.isActive()) return;
				World world = tileMachine.getWorld();
				BlockPos pos = tileMachine.getPos();
				for (EntityPlayer entity : world.getEntitiesWithinAABB(EntityPlayer.class, getRadius(pos))) {
					PotionEffect activePotionEffect = entity.getActivePotionEffect(XU2Entries.potionDoom.value);
					if (activePotionEffect == null)
						entity.addPotionEffect(new PotionEffect(XU2Entries.potionDoom.value, 60 * 20, 0));
				}
			}

			@Nonnull
			private AxisAlignedBB getRadius(BlockPos pos) {
				return new AxisAlignedBB(pos).grow(1, 1, 1);
			}

			@Override
			public void clientTick(TileEntity tileMachine, boolean active) {
				if (active) {
					BlockPos pos = tileMachine.getPos();
					AxisAlignedBB radius = getRadius(pos);
					for (int i = 0; i < 4; i++) {
						spawnParticlesNearby(tileMachine.getWorld(), radius, 0.7F, 0.1F, 0.1F);
					}
				}
			}
		};
		XUMachineGenerators.ENCHANT_GENERATOR = new Machine("extrautils2:generator_enchant", 100000, 1000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), "extrautils2:machine/generator_off", "extrautils2:machine/generator_on", Machine.EnergyMode.GENERATES_ENERGY, 0x3c3056, "extrautils2:machine/machine_base_white", "extrautils2:machine/machine_base_white_side", "extrautils2:machine/machine_base_white_bottom", "extrautils2:machine/generator/generator_enchant");

		SLOT_SLIME_SECONDARY = new MachineSlotItem("input2");
		XUMachineGenerators.SLIME_GENERATOR = new Machine(
				"extrautils2:generator_slime", 100000,
				1000, ImmutableList.of(XUMachineGenerators.INPUT_ITEM, SLOT_SLIME_SECONDARY),
				ImmutableList.of(),
				ImmutableList.of(),
				ImmutableList.of(),
				"extrautils2:machine/generator_off",
				"extrautils2:machine/generator_on",
				Machine.EnergyMode.GENERATES_ENERGY,
				0xffffff,
				"extrautils2:machine/generator/machine_base_slime",
				"extrautils2:machine/generator/machine_base_slime_side",
				"extrautils2:machine/generator/machine_base_slime_bottom",
				null
		) {
			@Override
			public void processingTick(TileEntity tileMachine, IMachineRecipe curRecipe, float processTime, int n) {

			}
		};

		MachineRegistry.register(XUMachineFurnace.INSTANCE);
		MachineRegistry.register(XUMachineCrusher.INSTANCE);
		MachineRegistry.register(XUMachineEnchanter.INSTANCE);
		MachineRegistry.register(XUMachineGenerators.SURVIVALIST_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.FURNACE_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.CULINARY_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.LAVA_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.REDSTONE_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.ENDER_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.POTION_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.PINK_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.OVERCLOCK_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.TNT_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.NETHERSTAR_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.DRAGON_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.ICE_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.DEATH_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.ENCHANT_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.SLIME_GENERATOR);
	}

	private static void spawnParticlesNearby(World world, AxisAlignedBB radius, float r, float g, float b) {
		world.spawnParticle(EnumParticleTypes.SPELL_MOB,
				radius.minX + (radius.maxX - radius.minX) * world.rand.nextFloat(),
				radius.minY + (radius.maxY - radius.minY) * world.rand.nextFloat(),
				radius.minZ + (radius.maxZ - radius.minZ) * world.rand.nextFloat(),
				r, g, b
		);
	}

	public static void addMachineRecipes() {

		XUMachineFurnace.INSTANCE.recipes_registry.addRecipe(
				new SingleInputStackToStackRecipeCached(XUMachineFurnace.INPUT, XUMachineFurnace.OUTPUT) {
					@Override
					@Nonnull
					public Collection<ItemStack> getInputValues() {
						return FurnaceRecipes.instance().getSmeltingList().keySet();
					}

					@Override
					public ItemStack getResult(@Nonnull ItemStack stack) {
						return FurnaceRecipes.instance().getSmeltingResult(stack);
					}

					@Nullable
					@Override
					public ItemStack getContainer(ItemStack stack) {
						return StackHelper.empty();
					}

					@Override
					public int getEnergyOutput(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
						return 2000;
					}

					@Override
					public int getProcessingTime(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
						return 100;
					}
				}
		);

		XUMachineGenerators.FURNACE_GENERATOR.recipes_registry.addRecipe(new FurnaceRecipe() {
			@Override
			protected float getEnergyRate(@Nonnull ItemStack itemStack) {
				return 40;
			}
		});

		XUMachineGenerators.SURVIVALIST_GENERATOR.recipes_registry.addRecipe(new FurnaceRecipe() {
			@Override
			protected float getEnergyRate(@Nonnull ItemStack itemStack) {
				return 5;
			}

			@Override
			public int getEnergyOutput(@Nonnull ItemStack itemStack) {
				return super.getEnergyOutput(itemStack) * 5;
			}
		});

		XUMachineGenerators.OVERCLOCK_GENERATOR.recipes_registry.addRecipe(new FurnaceRecipe() {
			@Override
			protected float getEnergyRate(@Nonnull ItemStack itemStack) {
				return 4000;
			}

			@Override
			public int getEnergyOutput(@Nonnull ItemStack itemStack) {
				return super.getEnergyOutput(itemStack) / 10;
			}
		});

		XUMachineGenerators.NETHERSTAR_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Items.NETHER_STAR), 20 * 120 * 4000, 4000));

		XUMachineGenerators.ENDER_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Items.ENDER_PEARL), 64000, 40));
		XUMachineGenerators.ENDER_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Items.ENDER_EYE), 64000 * 4, 80));

		XUMachineGenerators.TNT_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Blocks.TNT), 4 * 128000, 4 * 40));
		XUMachineGenerators.TNT_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Items.GUNPOWDER), 4 * 16000, 4 * 40));

		XUMachineGenerators.PINK_GENERATOR.recipes_registry.addRecipe(new PinkRecipe());
		XUMachineGenerators.PINK_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta())), 400, 40));

		XUMachineGenerators.DRAGON_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Items.DRAGON_BREATH), 480000, 40));

		XUMachineGenerators.ICE_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Blocks.ICE), 1600, 40));
		XUMachineGenerators.ICE_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Blocks.PACKED_ICE), 1600, 40));
		XUMachineGenerators.ICE_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Items.SNOWBALL), 200, 40));
		XUMachineGenerators.ICE_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Blocks.SNOW), 800, 40));
		XUMachineGenerators.ICE_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Blocks.SNOW_LAYER), 100, 40));

		XUMachineGenerators.DEATH_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Items.BONE), 16000));
		XUMachineGenerators.DEATH_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Blocks.BONE_BLOCK), 16000 * 3));
		XUMachineGenerators.DEATH_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(Items.ROTTEN_FLESH), 8000));
		XUMachineGenerators.DEATH_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(ItemRef.wrap(new ItemStack(Items.SKULL, 1, 1)), 60000));

		XUMachineGenerators.LAVA_GENERATOR.recipes_registry.addRecipe(RecipeBuilder.newbuilder(XUMachineGenerators.LAVA_GENERATOR).setRFRate(5000, 40).setFluidInputFluidStack(XUMachineGenerators.INPUT_FLUID, new FluidStack(FluidRegistry.LAVA, 50)).build());
		XUMachineGenerators.REDSTONE_GENERATOR.recipes_registry.addRecipe(RecipeBuilder.newbuilder(XUMachineGenerators.REDSTONE_GENERATOR).setRFRate(20000, 4 * 40).setItemInput(XUMachineGenerators.INPUT_ITEM, "dustRedstone", 1).setFluidInputFluidStack(XUMachineGenerators.INPUT_FLUID, new FluidStack(FluidRegistry.LAVA, 50)).build());

		XUMachineGenerators.SLIME_GENERATOR.recipes_registry.addRecipe(
				RecipeBuilder.newbuilder(XUMachineGenerators.SLIME_GENERATOR)
						.setRFRate(12 * 16000, 20 * 20)
						.setItemInput(XUMachineGenerators.INPUT_ITEM, "slimeball", 4)
						.setItemInput(SLOT_SLIME_SECONDARY, new ItemStack(Items.MILK_BUCKET, 1))
						.build()
		);

//		XUMachineGenerators.SLIME_GENERATOR.recipes_registry.addPriorityRecipe(new EnergyBaseRecipe.EnergyBaseItem(new IMatcherMaker.MatcherMakerOreDic("slimeballBlood"), 3 * 16000));
//		XUMachineGenerators.SLIME_GENERATOR.recipes_registry.addPriorityRecipe(new EnergyBaseRecipe.EnergyBaseItem(new IMatcherMaker.MatcherMakerOreDic("slimeballPurple"), 3 * 16000));
//		XUMachineGenerators.SLIME_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(new IMatcherMaker.MatcherMakerOreDic("slimeball").addExceptions("slimeballPurple", "slimeballBlood"), 16000));
//		XUMachineGenerators.SLIME_GENERATOR.recipes_registry.addRecipe(new EnergyBaseRecipe.EnergyBaseItem(new IMatcherMaker.MatcherMakerOreDic("blockSlime"), 9 * 16000));

		XUMachineGenerators.POTION_GENERATOR.recipes_registry.addRecipe(new BrewingEnergyRecipe());
		XUMachineGenerators.ENCHANT_GENERATOR.recipes_registry.addRecipe(new DischantEnergyRecipe());

		XUMachineGenerators.CULINARY_GENERATOR.recipes_registry.addRecipe(new FoodEnergyRecipe());


		XUMachineCrusher.addRecipe(new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.BLAZE_POWDER, 2), new ItemStack(Items.BLAZE_POWDER, 3), 0.4F);
		XUMachineCrusher.addRecipe(new ItemStack(Items.BONE), new ItemStack(Items.DYE, 3, EnumDyeColor.WHITE.getDyeDamage()), new ItemStack(Items.DYE, 3, EnumDyeColor.WHITE.getDyeDamage()), 0.5F);

		for (int i = 0; i < 16; ++i) {
			ItemStack outputSecondary;
			if ((15 - i) != 4) {
				outputSecondary = new ItemStack(Items.DYE, 1, 15 - i);
			} else {
				outputSecondary = ItemIngredients.Type.DYE_POWDER_BLUE.newStack();
			}

			XUMachineCrusher.addRecipe(new ItemStack(Blocks.WOOL, 1, i), new ItemStack(Items.STRING, 3), outputSecondary, 0.05F);
			XUMachineCrusher.addRecipe(new ItemStack(Blocks.CARPET, 1, i), new ItemStack(Items.STRING, 2), outputSecondary, 0.05F);
		}

		XUMachineCrusher.addRecipe(new ItemStack(Blocks.YELLOW_FLOWER, 1, BlockFlower.EnumFlowerType.DANDELION.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.YELLOW.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.POPPY.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.RED.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.LIGHT_BLUE.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.ALLIUM.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.MAGENTA.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.SILVER.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.RED_TULIP.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.RED.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.ORANGE.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.SILVER.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.PINK.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.SILVER.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.SUNFLOWER.getMeta()), new ItemStack(Items.DYE, 4, EnumDyeColor.YELLOW.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.SYRINGA.getMeta()), new ItemStack(Items.DYE, 4, EnumDyeColor.MAGENTA.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.ROSE.getMeta()), new ItemStack(Items.DYE, 4, EnumDyeColor.RED.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.PAEONIA.getMeta()), new ItemStack(Items.DYE, 4, EnumDyeColor.PINK.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Items.BEETROOT, 1), new ItemStack(Items.DYE, 2, EnumDyeColor.RED.getDyeDamage()));


		List<Pair<String, String>> pairedOres = ImmutableList.of(
				Pair.of("Iron", "Gold"),
				Pair.of("Gold", "Iron"),
				Pair.of("Copper", "Tin"),
				Pair.of("Tin", "Copper"),
				Pair.of("Lead", "Silver"),
				Pair.of("Silver", "Lead"),
				Pair.of("Nickel", "Platinum"),
				Pair.of("Platinum", "Nickel")
		);
		for (Pair<String, String> pair : pairedOres) {
			String s = pair.getLeft();
			addCrusherOreRecipe("ore" + s, "dust" + s, 2, "dust" + pair.getRight(), 1, 0.1F);
			addCrusherOreRecipe("ingot" + s, "dust" + s, 1);
		}
		addCrusherOreRecipe("oreDiamond", "gemDiamond", 1, "gemDiamond", 3, 0.2F);
		addCrusherOreRecipe("oreEmerald", "gemEmerald", 1, "gemEmerald", 3, 0.2F);
		addCrusherOreRecipe("cobblestone", "gravel", 1, "sand", 1, 0.1F);
		addCrusherOreRecipe("gravel", "sand", 1);
		addCrusherOreRecipe("oreLapis", "gemLapis", 8);
		addCrusherOreRecipe("oreRedstone", "dustRedstone", 8);
		addCrusherOreRecipe("oreQuartz", "gemQuartz", 1, "gemQuartz", 3, 0.2F);
		addCrusherOreRecipe("glowstone", "dustGlowstone", 4);
		addCrusherOreRecipe("oreCoal", Items.COAL, 4);
	}

	static void addCrusherOreRecipe(@Nonnull Object oreInput, @Nonnull Object dustOutput, int amount) {
		addCrusherOreRecipe(oreInput, dustOutput, amount, null, 0, 0);
	}

	static void addCrusherOreRecipe(@Nonnull Object oreInput, @Nonnull Object dustOutput, int amount, @Nullable Object outputSecondary, int outputSecondaryAmount, float outputSecondaryProbability) {
		RecipeBuilder recipeBuilder = RecipeBuilder.newbuilder(XUMachineCrusher.INSTANCE);
		recipeBuilder.setItemInput(XUMachineCrusher.INPUT, XUShapedRecipe.getRecipeStackList(oreInput), 1);
		recipeBuilder.setItemOutput(XUMachineCrusher.OUTPUT, XUShapedRecipe.getRecipeStackList(dustOutput), amount);
		if (outputSecondary != null && outputSecondaryAmount > 0) {
			recipeBuilder.setItemOutput(XUMachineCrusher.OUTPUT_SECONDARY, XUShapedRecipe.getRecipeStackList(outputSecondary), outputSecondaryAmount);
			recipeBuilder.setProbability(XUMachineCrusher.OUTPUT_SECONDARY, outputSecondaryProbability);
		}
		recipeBuilder.setEnergy(4000);
		recipeBuilder.setProcessingTime(200);
		XUMachineCrusher.INSTANCE.recipes_registry.addRecipe(recipeBuilder.build());
	}


	public static void register() {
		MachineRegistry.register(XUMachineFurnace.INSTANCE);
		MachineRegistry.register(XUMachineCrusher.INSTANCE);
		MachineRegistry.register(XUMachineEnchanter.INSTANCE);
		MachineRegistry.register(XUMachineGenerators.SURVIVALIST_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.FURNACE_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.CULINARY_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.LAVA_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.REDSTONE_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.ENDER_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.POTION_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.PINK_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.OVERCLOCK_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.TNT_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.NETHERSTAR_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.DRAGON_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.ICE_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.DEATH_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.ENCHANT_GENERATOR);
		MachineRegistry.register(XUMachineGenerators.SLIME_GENERATOR);
	}
}
