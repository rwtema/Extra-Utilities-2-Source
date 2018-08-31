package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.achievements.AchievementHelper;
import com.rwtema.extrautils2.api.machine.XUMachineEnchanter;
import com.rwtema.extrautils2.backend.ISidedFunction;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.blocks.BlockCursedEarth;
import com.rwtema.extrautils2.blocks.BlockSimpleDecorative;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.eventhandlers.DropsHandler;
import com.rwtema.extrautils2.power.ClientPower;
import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.power.energy.EnergyTransfer;
import com.rwtema.extrautils2.power.energy.TilePowerBattery;
import com.rwtema.extrautils2.tile.TileResonator;
import com.rwtema.extrautils2.transfernodes.IUpgradeProvider;
import com.rwtema.extrautils2.transfernodes.Upgrade;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class ItemIngredients extends XUItemFlatMetadata implements IUpgradeProvider {
	public static final int RED_COAL_MULTIPLIER = 8;
	public static TIntObjectHashMap<Type> metaMap = new TIntObjectHashMap<>();

	public ItemIngredients() {
		super(getTextureArray());
		setHasSubtypes(true);
	}

	public static String[] getTextureArray() {
		int maxMeta = 0;
		for (Type type : Type.values()) {
			maxMeta = Math.max(maxMeta, type.meta);
		}

		String[] strings = new String[maxMeta + 1];
		for (Type type : Type.values()) {
			if (type.meta >= 0)
				strings[type.meta] = type.texture;
		}
		return strings;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return getType(stack).getMaxStackSize(stack);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		getType(stack).onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseBase(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return getType(stack).onItemUse(stack, playerIn, worldIn, pos, hand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		super.registerTextures();
		for (Type type : Type.values()) {
			Textures.register(type.texture);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		return getType(itemStack).texture;
	}

	@Override
	public void getSubItemsBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (Type type : Type.values()) {
			if (type.meta >= 0)
				type.addsubitems(subItems);
		}

	}

	public Type getType(ItemStack stack) {
		Type type = metaMap.get(stack.getItemDamage());
		return type == null ? Type.SYMBOL_ERROR : type;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + getType(stack).name().toLowerCase();
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		return getType(entityItem.getItem()).onEntityItemUpdate(entityItem);
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		return Lang.translate(this.getUnlocalizedNameInefficiently(stack) + ".name", getType(stack).defaultName());
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack itemStack) {
		return getType(itemStack).getContainerItem(itemStack);
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return getType(stack).hasContainerItem(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		getType(stack).addInformation(stack, tooltip, advanced);
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return getType(stack).hasCustomEntity(stack);
	}

	@Nonnull
	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack) {
		return getType(itemstack).createEntity(world, location, itemstack);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return getType(stack).hasEffect(stack);
	}

	@Override
	public Upgrade getUpgrade(ItemStack stack) {
		return getType(stack).getUpgrade(stack);
	}

	public enum Type implements IItemStackMaker, IUpgradeProvider {
		BIOME_MARKER_BLANK(-8),
		ENCHANTED_BOOK_SKELETON(-7),
		FILTER_SKELETON(-6),
		SYMBOL_TICK(-5),
		SYMBOL_CROSS(-4),
		UPGRADE_SPEED_SKELETON(-3),
		SYMBOL_NOCRAFT(-2),
		SYMBOL_ERROR(-1),
		REDSTONE_CRYSTAL(0, "gemRedstone") {
			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("redstone_crystal", newStack(1), XU2Entries.itemEnderShard.newWildcardStack(), "dustRedstone", "dustRedstone", "dustRedstone", "dustRedstone");
				DropsHandler.registerDrops(Blocks.REDSTONE_ORE.getDefaultState(), newStack(1), 0.025);
				DropsHandler.registerDrops(Blocks.LIT_REDSTONE_ORE.getDefaultState(), newStack(1), 0.1);
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
				tooltip.add(ClientPower.powerStatusString());
				if (ClientPower.hasNoPower())
					tooltip.add(Lang.translate("See the Extra Utilities 2 Manual for more info"));

			}

			@Override
			public void addAchievement() {
				AchievementHelper.addAchievement("Tech-Tree Start", "See achievements list for Extra Utilities 2 power tech tree", this, null);
			}
		},
		REDSTONE_GEAR(1, "gearRedstone") {
			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("redstone_gear", newStack(1), " T ", "TsT", " T ", 'T', Blocks.REDSTONE_TORCH, 's', "plankWood");
			}
		},
		EYE_REDSTONE(2, "eyeofredstone") {
			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("redstone_eye", newStack(1), Items.ENDER_PEARL, "dustRedstone", REDSTONE_CRYSTAL.newStack(1));
			}

			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
				tooltip.add(ClientPower.powerStatusString());
			}
		},
		DYE_POWDER_LUNAR(3, "dustLunar") {
			@Override
			public void addRecipes() {
				OreDictionary.registerOre("dyeMagenta", newStack(1));
				TileResonator.register(new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), newStack(1), 1600);
			}

			@Override
			public void addAchievement() {
				AchievementHelper.addAchievement("Dye of the Moon", "The moons power beckons!", ItemIngredients.Type.DYE_POWDER_LUNAR, XU2Entries.stoneburnt);
			}
		},
		RED_COAL(4, "coalPowered") {
			@Override
			public void addRecipes() {
				TileResonator.register(new ItemStack(Items.COAL, 1, OreDictionary.WILDCARD_VALUE), newStack(1), 1600, true);
				GameRegistry.registerFuelHandler(new IFuelHandler() {
					ISidedFunction<Integer, PowerManager.IPowerReport> getPowerReport = new ISidedFunction<Integer, PowerManager.IPowerReport>() {
						@Override
						@SideOnly(Side.SERVER)
						public PowerManager.IPowerReport applyServer(Integer input) {
							return isPowered(input);
						}

						@Nonnull
						public PowerManager.IPowerReport isPowered(Integer input) {
							return PowerManager.instance.getPowerFreq(input);
						}

						@Override
						@SideOnly(Side.CLIENT)
						public PowerManager.IPowerReport applyClient(Integer input) {
							MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
							if (server == null) {
								return ClientPower.POWER_REPORT;
							}
							return isPowered(input);
						}
					};

					@Override
					public int getBurnTime(ItemStack fuel) {
						if (StackHelper.isNull(fuel) || fuel.getItemDamage() != Type.RED_COAL.meta || fuel.getItem() != XU2Entries.itemIngredients.value) {
							return 0;
						}

						NBTTagCompound nbt = fuel.getTagCompound();
						if (nbt != null && nbt.hasKey("Freq", Constants.NBT.TAG_INT)) {
							PowerManager.IPowerReport freq = ExtraUtils2.proxy.apply(getPowerReport, nbt.getInteger("Freq"));

							float n = getPowerBonus(freq);

							return (int) Math.floor(1600 * n);
						}

						return 1600;
					}
				});
			}

			public float getPowerBonus(PowerManager.IPowerReport freq) {
				if (!freq.isPowered()) return 1;

				float n = (1.0F + freq.getPowerCreated() / 80F);
				if (n > RED_COAL_MULTIPLIER) {
					n = RED_COAL_MULTIPLIER;
				} else if (n < 1) {
					n = 1;
				}
				return n;
			}

			@Override
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {

				tooltip.add(Lang.translateArgs("Boosts fuel efficiency with additional GP up to %s.", StringHelper.formatPercent(RED_COAL_MULTIPLIER - 1)));
				tooltip.add(Lang.translateArgs("Current efficiency boost: %s", StringHelper.formatPercent(getPowerBonus(ClientPower.POWER_REPORT) - 1)));
			}

			@Override
			public boolean hasContainerItem(ItemStack stack) {
//				NBTTagCompound nbt = stack.getTagCompound();
//				return nbt != null && nbt.hasKey("Freq", Constants.NBT.TAG_INT);
				return false;
			}

			@Override
			public ItemStack getContainerItem(ItemStack itemStack) {
//				NBTTagCompound nbt = itemStack.getTagCompound();
//				if (nbt != null && nbt.hasKey("Freq", Constants.NBT.TAG_INT)) {
//					Side effectiveSide = FMLCommonHandler.instance().getEffectiveSide();
//					if(effectiveSide == Side.SERVER) {
//						PowerManager.addPulseTime(nbt.getInteger("Freq"), 20 * 10);
//					}
//				}

				return StackHelper.empty();
			}

			@Override
			public void addAchievement() {
				AchievementHelper.addAchievement("Powered Coal", "Boosts coals power by " + ItemIngredients.RED_COAL_MULTIPLIER + "x", ItemIngredients.Type.RED_COAL, XU2Entries.resonator);
			}
		},
		MOON_STONE(5, "gemMoon") {
			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("moon_stone", newStack(1), "sss", "sds", "sss", 'd',
						"gemDiamond",
						's', DYE_POWDER_LUNAR);
				if (XU2Entries.unstableIngots.isActive()) {
					CraftingHelper.addShaped("moon_stone_adv", newStack(9), "sss", "sds", "sss", 'd', "ingotUnstable", 's', DYE_POWDER_LUNAR);
				}
			}
		},

		UPGRADE_SPEED(6, "xuUpgradeSpeed") {
			@Override
			public Upgrade getUpgrade(ItemStack stack) {
				return Upgrade.SPEED;
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("upgrade_speed", newStack(1), UPGRADE_BASE, "ingotGold", "blockRedstone");
			}

			@Override
			public void addAchievement() {
				AchievementHelper.addAchievement("Speed Upgrade", "Boosts speed of transfer nodes.", this, UPGRADE_BASE);
			}

			@Override
			public int getMaxStackSize(ItemStack stack) {
				return 4;
			}

			@Override
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Increases speed of operations."));
				Upgrade.addTooltip(tooltip, stack, this, 4);

			}
		},

		UPGRADE_STACK(7, "xuUpgradeStack") {
			@Override
			public Upgrade getUpgrade(ItemStack stack) {
				return Upgrade.STACK_SIZE;
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("upgrade_stack", newStack(1), UPGRADE_BASE, "ingotGold", Items.DIAMOND);
			}

			@Override
			public void addAchievement() {
				AchievementHelper.addAchievement("Stack Upgrade", "Boosts amount pulled from transfer nodes.", this, UPGRADE_BASE);
			}

			@Override
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Increase the number of items pulled to a stack"));
				Upgrade.addTooltip(tooltip, stack, this, -1);
			}
		},

		UPGRADE_MINING(8, "xuUpgradeMining") {
			@Override
			public Upgrade getUpgrade(ItemStack stack) {
				return Upgrade.MINING;
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("upgrade_mining", newStack(1), UPGRADE_BASE, new ItemStack(Items.GOLDEN_PICKAXE));
			}

			@Override
			public void addAchievement() {
				AchievementHelper.addAchievement("Mining Upgrade", "Mines objects and fluids from the world.", this, UPGRADE_BASE);
			}

			@Override
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Allows mining of cobblestone and pumping of water"));
				Upgrade.addTooltip(tooltip, stack, this, -1);
			}
		},
		UPGRADE_BASE(9, "xuUpgradeBlank") {
			@Override
			public void addRecipes() {
				TileResonator.register(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE), newStack(1), 800);
			}

			@Override
			public void addAchievement() {
				AchievementHelper.addAchievement("Transfer Node Upgrades", "Augment the power of nodes", this, XU2Entries.grocket.getMetaMaker(0));
			}
		},
		EVIL_DROP(10, "dropofevil") {
			@Override
			public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand) {
				IBlockState state = worldIn.getBlockState(pos);

				if (XU2Entries.mobSpawner.isActive() && state.getBlock() == Blocks.MOB_SPAWNER) {
					if (!worldIn.isRemote) {
						TileEntity tile = worldIn.getTileEntity(pos);
						if (tile instanceof TileEntityMobSpawner) {
							TileEntityMobSpawner spawner = (TileEntityMobSpawner) tile;
							NBTTagCompound nbt = spawner.getSpawnerBaseLogic().writeToNBT(new NBTTagCompound());
							if (worldIn.setBlockToAir(pos)) {
								ItemStack dropStack = new ItemStack(XU2Entries.mobSpawner.value);
								dropStack.setTagCompound(nbt);
								Block.spawnAsEntity(worldIn, pos, dropStack);
								StackHelper.decrease(stack);
							}
						}
					}
					return EnumActionResult.SUCCESS;
				}


				if (XU2Entries.cursedEarth.isActive() &&
						(state == Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT) ||
								state == Blocks.GRASS.getDefaultState())) {

					if (!worldIn.isRemote) {
						BlockCursedEarth.startFastSpread(worldIn, pos);
						StackHelper.decrease(stack);

					}
					return EnumActionResult.SUCCESS;
				}

				return EnumActionResult.PASS;
			}

			@Override
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
//				tooltip.add(Lang.translate("Right click on grass or dirt to spread it's corruption."));
			}

			@Override
			public void addRecipes() {
				DropsHandler.lootDrops.put(LootTableList.ENTITIES_WITHER_SKELETON,
						new LootPool(new LootEntry[]{
								new LootEntryItem(
										XU2Entries.itemIngredients.value, 1, 0,
										new LootFunction[]{new SetMetadata(new LootCondition[0], new RandomValueRange(meta, meta))},
										new LootCondition[0]
										, "xuLootDropOfEvil")
						}, new LootCondition[]{
								new RandomChanceWithLooting(0.1F, 0.1F)
						}, new RandomValueRange(1, 1), new RandomValueRange(0, 0),
								"xuLootDropOfEvil"
						));
			}
		},
		DEMON_INGOT(11, "ingotDemonicMetal") {

			{
				MinecraftForge.EVENT_BUS.register(new DemonicIngotHandler());
			}

			@Override
			public void addRecipes() {
				if (XU2Entries.simpleDecorative.isActive()) {
					CraftingHelper.addIngotBlockPackingRecipes("demon", newStack(), BlockSimpleDecorative.Type.BLOCK_DEMONIC.newStack());
				}
			}

			@Override
			public boolean hasCustomEntity(ItemStack stack) {
				return true;
			}

			@Override
			public Entity createEntity(World world, Entity location, ItemStack itemstack) {
				location.setEntityInvulnerable(true);
				return null;
			}

			@Override
			public boolean onEntityItemUpdate(EntityItem entityItem) {
				World worldObj = entityItem.world;
				if (worldObj.isRemote && worldObj.getBlockState(new BlockPos(entityItem)).getMaterial() == Material.LAVA) {
					worldObj.spawnParticle(EnumParticleTypes.LAVA, entityItem.posX, entityItem.posY, entityItem.posZ, 0.0D, 0.0D, 0.0D);
				}
				return super.onEntityItemUpdate(entityItem);
			}
		},
		ENCHANTED_INGOT(12, "ingotEnchantedMetal") {
			@Override
			public void addRecipes() {
				if (XU2Entries.simpleDecorative.isActive()) {
					CraftingHelper.addIngotBlockPackingRecipes("enchanted", newStack(), BlockSimpleDecorative.Type.BLOCK_ENCHANTED.newStack());
					if (XU2Entries.machineEntry.isActive()) {
						XUMachineEnchanter.addRecipe(new ItemStack(Blocks.GOLD_BLOCK), BlockSimpleDecorative.Type.BLOCK_ENCHANTED.newStack(), 9, 8000 * 3, "gemLapis");
					}
				}

				if (XU2Entries.machineEntry.isActive()) {
					XUMachineEnchanter.addRecipe(new ItemStack(Items.GOLD_INGOT), newStack(), 1, 8000, "gemLapis");
				}
			}

		},
		REDSTONE_COIL(13, "xuRedstoneCoil") {
			final int NEEDED_FOR_BURN = 2000;
			final int BURN_TIME = 20;

			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
				tooltip.addAll(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(
						Lang.translate("Use the RF energy stored in wireless batteries to create heat") + "\n" + Lang.translateArgs("Uses %s RF to create %s ticks of burn time.", NEEDED_FOR_BURN, BURN_TIME), 200));

			}

			@Override
			public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
				NBTTagCompound nbt = stack.getTagCompound();
				if ((nbt == null || !nbt.hasKey("Freq", Constants.NBT.TAG_INT)) && !worldIn.isRemote && entityIn instanceof EntityPlayerMP) {
					stack.setTagInfo("Freq", new NBTTagInt(Freq.getBasePlayerFreq((EntityPlayerMP) entityIn)));
				}
			}

			@Override
			public void addRecipes() {
				TileResonator.register(new ItemStack(Blocks.IRON_BARS, 1, OreDictionary.WILDCARD_VALUE), newStack(1), 1600, true);
				GameRegistry.registerFuelHandler(fuel -> {
					if (StackHelper.isNull(fuel) || fuel.getItemDamage() != meta || fuel.getItem() != XU2Entries.itemIngredients.value) {
						return 0;
					}

					if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER)
						return BURN_TIME;

					NBTTagCompound nbt = fuel.getTagCompound();
					if (nbt != null && nbt.hasKey("Freq", Constants.NBT.TAG_INT)) {
						int freqNo = nbt.getInteger("Freq");
						PowerManager.PowerFreq freq = PowerManager.instance.getPowerFreq(freqNo);
						Collection<TilePowerBattery> batteries = freq.getSubTypes(EnergyTransfer.ENERGY_SYSTEM_STORAGE_KEY);
						int totalEnergy = 0;
						if (batteries != null) {
							for (TilePowerBattery battery : batteries) {
								totalEnergy += battery.energy.getEnergyStored();
								if (totalEnergy >= NEEDED_FOR_BURN) {
									return BURN_TIME;
								}
							}
						}
					}
					return 0;
				});
			}

			@Override
			public boolean hasContainerItem(ItemStack stack) {
				return true;
			}

			@Override
			public ItemStack getContainerItem(ItemStack itemStack) {
				if (itemStack == null) return null;
				Side effectiveSide = FMLCommonHandler.instance().getEffectiveSide();
				NBTTagCompound nbt = itemStack.getTagCompound();
				if (effectiveSide == Side.SERVER) {
					if (nbt != null && nbt.hasKey("Freq", Constants.NBT.TAG_INT)) {
						int freqNo = nbt.getInteger("Freq");
						PowerManager.PowerFreq freq = PowerManager.instance.getPowerFreq(freqNo);
						Collection<TilePowerBattery> batteries = freq.getSubTypes(EnergyTransfer.ENERGY_SYSTEM_STORAGE_KEY);
						int extractedEnergy = 0;
						if (batteries != null) {
							for (TilePowerBattery battery : batteries) {
								extractedEnergy += battery.energy.extractEnergy(NEEDED_FOR_BURN - extractedEnergy, false);
								if (extractedEnergy >= NEEDED_FOR_BURN) {
									break;
								}
							}
						}
					}
				}
				ItemStack copy = newStack();
				if (nbt != null) {
					copy.setTagCompound(nbt);
				}
				return copy;
			}

			@Override
			public int getMaxStackSize(ItemStack stack) {
				return 1;
			}
		},
		DYE_POWDER_BLUE(14, "dyeBlue") {
			@Override
			public void registerOres() {
				OreDictionary.registerOre("dye", newStack());
				OreDictionary.registerOre("dyeBlue", newStack());
			}
		},
		UPGRADE_SPEED_ENCHANTED(15, "xuUpgradeSpeedEnchanted") {
			@Override
			public Upgrade getUpgrade(ItemStack stack) {
				return Upgrade.SPEED;
			}

			@Override
			public boolean hasEffect(ItemStack stack) {
				return true;
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("upgrade_speed_enchanted", newStack(), "aia", "isi", "aia", 'a', XU2Entries.magicApple.isActive() ? XU2Entries.magicApple : "ingotGold", 'i', ENCHANTED_INGOT, 's', UPGRADE_SPEED);
			}

			@Override
			public int getMaxStackSize(ItemStack stack) {
				return 16;
			}

			@Override
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Increases speed of operations."));
				Upgrade.addTooltip(tooltip, stack, this, 16);
			}
		},
		UPGRADE_SPEED_SUPER(16, "xuUpgradeSpeedEnchanted") {
			@Override
			public Upgrade getUpgrade(ItemStack stack) {
				return Upgrade.SPEED;
			}

			@Override
			public boolean hasEffect(ItemStack stack) {
				return true;
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("upgrade_speed_super", newStack(), "aia", "isi", "aia", 'a', EVIL_DROP, 'i', EVIL_INFUSED_INGOT, 's', UPGRADE_SPEED_ENCHANTED);
			}

			@Override
			public int getMaxStackSize(ItemStack stack) {
				return 64;
			}

			@Override
			public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Increases speed of operations."));
				Upgrade.addTooltip(tooltip, stack, this, 64);
			}
		},
		EVIL_INFUSED_INGOT(17, "ingotEvilMetal") {
			@Override
			public void addRecipes() {
//				IItemStackMaker block_evil = BlockDecorativeSolid.DecorStates.block_evil;
				IItemStackMaker block_evil = BlockSimpleDecorative.Type.BLOCK_EVIL;
				if (XU2Entries.simpleDecorative.isActive()) {
					CraftingHelper.addIngotBlockPackingRecipes("evil", newStack(), block_evil.newStack());
					if (XU2Entries.machineEntry.isActive()) {
						XUMachineEnchanter.addRecipe(new ItemStack(Blocks.IRON_BLOCK, 8), block_evil.newStack(8), 9, 64000 * 3, "netherStar");
					}
				}

				if (XU2Entries.machineEntry.isActive()) {
					XUMachineEnchanter.addRecipe(new ItemStack(Items.IRON_INGOT, 8), newStack(8), 1, 64000, "netherStar");
				}
			}
		};

		public final int meta;
		public final String texture;
		public final String oreName;

		Type(int meta) {
			this(meta, null);
		}

		Type(int meta, String oreName) {
			this.meta = meta;
			this.oreName = oreName;
			texture = name().toLowerCase();
			metaMap.put(meta, this);
			if (meta < 0)
				metaMap.put(meta + Short.MAX_VALUE, this);
		}

		protected void addsubitems(List<ItemStack> subItems) {
			subItems.add(newStack());
		}

		public String defaultName() {
			StringBuilder builder = new StringBuilder();
			String[] split = name().split("_");
			for (int i = 0; i < split.length; i++) {
				if (i > 0) builder.append(" ");
				builder.append(StringHelper.capFirst(split[i], true));
			}
			return builder.toString();
		}

		public void addRecipes() {

		}

		public ItemStack newStack(int amount) {
			int m = this.meta;
			if (m < 0) m += Short.MAX_VALUE;
			return XU2Entries.itemIngredients.newStack(amount, m);
		}


		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, List<String> tooltip, boolean advanced) {

		}

		@Override
		public ItemStack newStack() {
			return newStack(1);
		}

		public int getMaxStackSize(ItemStack stack) {
			return 64;
		}


		@Override
		public Upgrade getUpgrade(ItemStack stack) {
			return null;
		}

		public void addAchievement() {

		}

		public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand) {
			return EnumActionResult.PASS;
		}

		public ItemStack getContainerItem(ItemStack itemStack) {
			return StackHelper.empty();
		}

		public boolean hasContainerItem(ItemStack stack) {
			return false;
		}

		public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

		}

		public void registerOres() {
			if (oreName != null)
				OreDictionary.registerOre(oreName, newStack());
		}

		public boolean hasCustomEntity(ItemStack stack) {
			return false;
		}

		public Entity createEntity(World world, Entity location, ItemStack itemstack) {
			return null;
		}

		public boolean onEntityItemUpdate(EntityItem entityItem) {
			return false;
		}

		public boolean hasEffect(ItemStack stack) {
			return false;
		}

		public void preInitRegister() {

		}
	}

}
