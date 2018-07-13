package com.rwtema.extrautils2.backend.entries;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUItemBlock;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.tile.tesr.ITESRHookSimple;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;

public abstract class BlockEntry<T extends XUBlock> extends Entry<T> implements IItemStackMaker {
	public static final HashMap<Class<? extends TileEntity>, XUBlock> tileToBlockMap = new HashMap<>();
	public static final HashMultimap<Class<? extends TileEntity>, Block> tileToBlocksMap = HashMultimap.create();
	public static HashMap<ResourceLocation, Block> blockMap = new HashMap<>();
	public Class<? extends TileEntity>[] teClazzes;
	public Class<? extends ItemBlock> itemClass = XUItemBlock.class;

	@SafeVarargs
	public BlockEntry(String name, Class<? extends TileEntity>... teClazzes) {
		super(name);
		this.teClazzes = teClazzes;
	}

	@SuppressWarnings("unchecked")
	public static void registerTile(Class<? extends TileEntity> teClazz) {
		if (ITESRHookSimple.class.isAssignableFrom(teClazz)) {
			ExtraUtils2.proxy.registerTESROther((Class) teClazz);
		} else if (ITESRHook.class.isAssignableFrom(teClazz)) {
			ExtraUtils2.proxy.registerTESR((Class) teClazz);
		}

		GameRegistry.registerTileEntity(teClazz, "XU2" + ":" + teClazz.getSimpleName());
	}

	public static <T extends XUBlock> Pair<T, ItemBlock> registerBlockItemCombo(T value, Class<? extends ItemBlock> itemClass, String name) {
		ResourceLocation location = new ResourceLocation(ExtraUtils2.MODID + ":" + name.toLowerCase(Locale.ENGLISH));
		value.setRegistryName(location);
		CompatHelper112.register(value);
		blockMap.put(location, value);
		if (itemClass != null) {
			try {
				ItemBlock itemBlock = itemClass.getConstructor(Block.class).newInstance(value);
				itemBlock.setRegistryName(location);
				CompatHelper112.register(itemBlock);
				ItemEntry.itemMap.put(location, itemBlock);
				return Pair.of(value, itemBlock);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw Throwables.propagate(e);
			}
		}
		return Pair.of(value, null);
	}

	public <K extends BlockEntry<T>> K setItemClass(Class<? extends ItemBlock> clazz) {
		itemClass = clazz;
		return (K) this;
	}

	@Override
	public void preInitRegister() {
		value.setBlockName(ExtraUtils2.MODID + ":" + name.toLowerCase(Locale.ENGLISH));

		registerBlockItemCombo(value, itemClass, name);

		for (Class<? extends TileEntity> teClazz : teClazzes) {
			tileToBlockMap.putIfAbsent(teClazz, value);
			tileToBlocksMap.put(teClazz, value);
			registerTile(teClazz);
		}
	}

	@Override
	public ItemStack newStack(int amount, int meta) {
		if (value == null || itemClass == null) return StackHelper.empty();

		return new ItemStack(value, amount, meta);
	}

	public ItemStack newStack(int amount, IBlockState state) {
		if (value == null || itemClass == null) return StackHelper.empty();
		return newStack(amount, value.xuBlockState.getDropMetaFromState(state));
	}

	public ItemStack newStack(int amount, Object... properties) {
		if (value == null || itemClass == null) return StackHelper.empty();
		IBlockState state = value.getDefaultState();
		for (int i = 0; i < properties.length; i += 2) {
			state = state.withProperty((IProperty) properties[i], (Comparable) properties[i + 1]);
		}
		return newStack(amount, state);
	}
}
