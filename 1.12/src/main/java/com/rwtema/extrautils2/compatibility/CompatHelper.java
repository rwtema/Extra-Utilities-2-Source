package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.itemhandler.IItemHandlerCompat;
import com.rwtema.extrautils2.itemhandler.XUCrafter;
import com.rwtema.extrautils2.machine.MechEnchantmentRecipe;
import com.rwtema.extrautils2.utils.datastructures.ArrayAccess;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class CompatHelper {
	public static void setSlot(Slot slot, EntityPlayer par1EntityPlayer, ItemStack itemstack1) {
		slot.onTake(par1EntityPlayer, itemstack1);
	}

	public static boolean canPlaceBlockHere(World world, Block block, BlockPos loc, boolean p_175716_3_, EnumFacing side, Entity entity, ItemStack pickBlock1) {
		return world.mayPlace(block, loc, p_175716_3_, side, entity);
	}

	public static boolean activateBlock(Block block, World world, BlockPos newPos, IBlockState blockState, EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return block.onBlockActivated(world, newPos, blockState, player, hand, facing, hitX, hitY, hitZ);
	}

	public static BlockPos getCenterBlock(ChunkPos p, int y) {
		return p.getBlock(8, y, 8);
	}

	public static EnumEnchantmentType addEnchantmentType(String name) {
		return EnumHelper.addEnchantmentType(name, item -> false);
	}

	public static void notifyNeighborsOfStateChange(World worldIn, BlockPos pos, Block blockType) {
		worldIn.notifyNeighborsOfStateChange(pos, blockType, true);
	}

	public static void registerEntity(Class<? extends Entity> clazz, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, String name, int id) {
		EntityRegistry.registerModEntity(new ResourceLocation(ExtraUtils2.MODID, name), clazz, name, id, ExtraUtils2.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
	}

	public static <E> ArrayAccess<E> getArray10List11(List<E> list) {
		return new ArrayAccess.WrapList<E>(list);
	}

	public static Set<BiomeDictionary.Type> getTypesForBiome(Biome biome) {
		return BiomeDictionary.getTypes(biome);
	}

	public static EnumActionResult interactOn(EntityPlayer player, Entity p_190775_1_, EnumHand p_190775_2_, ItemStack stack) {
		return player.interactOn(p_190775_1_, p_190775_2_);
	}

	public static void addPotionEffect(World world, BlockPos blockPos, PotionType invisibility) {
		world.playEvent(2002, blockPos, PotionUtils.getPotionColor(invisibility));
	}

	public static Class<? extends Enum<?>> getBannerEnumClass() {
		return BannerPattern.class;
	}

	@Nonnull
	public static ChunkGeneratorEnd getChunkProviderEnd(WorldServer world) {
		return new ChunkGeneratorEnd(world, true, world.getSeed(), BlockPos.ORIGIN) {
			@Override
			public Chunk generateChunk(int x, int z) {
				Chunk chunk = super.generateChunk(x, z);
				for (ExtendedBlockStorage extendedBlockStorage : chunk.getBlockStorageArray()) {
					if (extendedBlockStorage != null) {
						if (extendedBlockStorage.getBlockLight() == null) {
							extendedBlockStorage.setBlockLight(new NibbleArray());
						}
					}
				}
				return chunk;
			}
		};
	}

	public static boolean BiomeHasType(Biome biome, BiomeDictionary.Type type) {
		return BiomeDictionary.hasType(biome, type);
	}

	public static Event.Result canSpawnEvent(WorldServer worldServerIn, EntityLiving entityliving, float x, float y, float z) {
		return ForgeEventFactory.canEntitySpawn(entityliving, worldServerIn, x, y, z, false);
	}

	public static boolean isBiomeOfType(Biome biome, BiomeDictionary.Type type) {
		return BiomeDictionary.hasType(biome, type);
	}

	public static float getBrightness(Entity entity) {
		return entity.getBrightness();
	}

	public static List<ItemStack> getRemainingItems(XUCrafter crafter, World world) {
		return CraftingManager.getRemainingItems(crafter, world);
	}

	public static void removeIncompatibleEnchantments(List<MechEnchantmentRecipe.EnchantmentEntry> list1, Enchantment finalEnchantment) {
		list1.removeIf(enchantmentEntry -> !finalEnchantment.isCompatibleWith(enchantmentEntry.enchantment));
	}

	public static boolean hasSky(World world) {
		return world.provider.hasSkyLight();
	}

	public static String getName(String id) {
		EntityEntry value = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
		return I18n.translateToLocal(value != null ? value.getName() : "entity.generic.name");
	}

	public static IItemHandlerCompat wrapItemHandlerCompat(IItemHandler handler) {
		if (handler instanceof IItemHandlerCompat) return (IItemHandlerCompat) handler;
		return new IItemHandlerCompat() {
			public int getSlotLimit(int slot) {
				return handler.getSlotLimit(slot);
			}

			public int getSlots() {
				return handler.getSlots();
			}

			public ItemStack getStackInSlot(int slot) {
				return handler.getStackInSlot(slot);
			}

			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				return handler.insertItem(slot, stack, simulate);
			}

			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				return handler.extractItem(slot, amount, simulate);
			}
		};
	}

	public static void notifyNeighbours(World world, BlockPos pos, Block block) {
		world.notifyNeighborsOfStateChange(pos, block, true);
	}

	public <E> NonNullList<E> toNonNullList(List<E> list) {
		if (list instanceof NonNullList) return (NonNullList<E>) list;
		NonNullList<E> list1 = NonNullList.create();
		list1.addAll(list);
		return list1;
	}


}
