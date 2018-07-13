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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.potion.PotionType;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class CompatHelper {
	public static <E> List<E> toNonNullList(List<E> list) {
		return list;
	}

	public static void setSlot(Slot slot, EntityPlayer player, ItemStack stack) {
		slot.func_82870_a(player, stack);
	}

	public static boolean canPlaceBlockHere(World world, Block block, BlockPos loc, boolean p_175716_3_, EnumFacing side, Entity entity, ItemStack pickBlock1) {
		return world.func_175716_a(block, loc, p_175716_3_, side, entity, pickBlock1);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	public static boolean activateBlock(Block block, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
		return block.onBlockActivated(worldIn, pos, state, playerIn, hand, stack, side, hitX, hitY, hitZ);
	}

	public static BlockPos getCenterBlock(ChunkPos chunkPos, int y) {
		return chunkPos.func_180619_a(y);
	}

	public static EnumEnchantmentType addEnchantmentType(String name) {
		return EnumHelper.addEnchantmentType(name);
	}

	public static void notifyNeighborsOfStateChange(World worldIn, BlockPos pos, Block block) {
		worldIn.notifyNeighborsOfStateChange(pos, block);
	}

	public static <E> ArrayAccess<E> getArray10List11(E[] list) {
		return new ArrayAccess.WrapArray<E>(list);
	}

	public static BiomeDictionary.Type[] getTypesForBiome(Biome biome) {
		return BiomeDictionary.getTypesForBiome(biome);
	}

	public static void registerEntity(Class<? extends Entity> clazz, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, String name, int id) {
		EntityRegistry.registerModEntity(clazz, name, id, ExtraUtils2.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
	}

	public static void interactOn(EntityPlayer player, Entity entity, EnumHand hand, ItemStack copy) {
		player.func_184822_a(entity, copy, hand);
	}

	public static void addPotionEffect(World world, BlockPos pos, PotionType type) {
		world.playBroadcastSound(2002, pos, PotionType.func_185171_a(type));
	}

	public static Class<? extends Enum<?>> getBannerEnumClass() {
		return TileEntityBanner.EnumBannerPattern.class;
	}

	public static boolean isBiomeOfType(Biome targetBiome, BiomeDictionary.Type end) {
		return BiomeDictionary.isBiomeOfType(targetBiome, end);
	}

	public static ChunkProviderEnd getChunkProviderEnd(WorldServer world) {
		return new ChunkProviderEnd(world, true, world.getSeed()) {
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

	public static float getBrightness(Entity player) {
		return player.getBrightness(1);
	}


	public static ItemStack[] getRemainingItems(XUCrafter crafter, World world) {
		return CraftingManager.func_77594_a().getRemainingItems(crafter, world);
	}

	public static void removeIncompatibleEnchantments(List<MechEnchantmentRecipe.EnchantmentEntry> list1, Enchantment enchantment) {
		list1.removeIf(t -> !(enchantment.canApplyTogether(t.enchantment) && t.enchantment.canApplyTogether(enchantment)));
	}

	public static boolean hasSky(World world) {
		return !world.provider.isNether();
	}


	public static String getName(String id) {
		return I18n.translateToLocal("entity." + id + ".name");
	}

	public static IItemHandlerCompat wrapItemHandlerCompat(IItemHandler handler) {
		if(handler instanceof IItemHandlerCompat)return (IItemHandlerCompat) handler;
		return new IItemHandlerCompat(){
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
}
