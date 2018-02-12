package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.api.tools.IWrench;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemStackHelper {
	final static List<Class<?>> validClazzes = new ArrayList<>();

	public static Function<String, Object> getBauble = null;
	public static Supplier<Capability> capGetter = null;

	static {
		for (String clazzName : ImmutableList.of(
				"com.rwtema.extrautils2.api.tools.IWrench",
				"crazypants.enderio.api.tool.ITool",
				"buildcraft.api.tools.IToolWrench",
				"cofh.api.item.IToolHammer",
				"com.brandon3055.draconicevolution.api.ICrystalBinder"
		)) {
			try {
				Class<?> aClass = Class.forName(clazzName);
				validClazzes.add(aClass);
			} catch (ClassNotFoundException ignore) {

			}
		}
	}

	public static ItemStack addLore(ItemStack a, String... lore) {
		NBTTagCompound tag = a.getTagCompound();
		if (tag == null)
			tag = new NBTTagCompound();

		if (!tag.hasKey("display", 10)) {
			tag.setTag("display", new NBTTagCompound());
		}

		NBTTagList l = new NBTTagList();
		for (String s : lore) {
			l.appendTag(new NBTTagString(s));
		}

		tag.getCompoundTag("display").setTag("Lore", l);

		a.setTagCompound(tag);

		return a;
	}

	public static void addBlockStates(String oreDictName, HashSet<IBlockState> stateList) {
		List<ItemStack> ores = OreDictionary.getOres(oreDictName);
		for (ItemStack ore : ores) {
			addBlockStates(ore, stateList);
		}
	}

	public static void addBlockStates(ItemStack stack, HashSet<IBlockState> stateList) {
		Item item = stack.getItem();
		if (!(item instanceof ItemBlock)) return;
		Block block = ((ItemBlock) item).getBlock();
		for (IBlockState iBlockState : block.getBlockState().getValidStates()) {
			if (block.hasTileEntity(iBlockState)) continue;
			int itemDamage = stack.getItemDamage();
			if (itemDamage == OreDictionary.WILDCARD_VALUE || itemDamage == block.damageDropped(iBlockState)) {
				stateList.add(iBlockState);
			}
		}
	}

	public static boolean holdingWrench(EntityPlayer player) {
		return isWrench(player.getHeldItemMainhand());
	}

	public static boolean isWrench(ItemStack wrench) {
		if (StackHelper.isNull(wrench)) return false;
		if (wrench.hasCapability(IWrench.CAPABILITY, null)) return true;
		Item item = wrench.getItem();
		Class<? extends Item> aClass = item.getClass();
		for (Class<?> clazz : validClazzes) {
			if (clazz.isAssignableFrom(aClass))
				return true;
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	public static void addInfoWidth(List<String> list, ItemStack stack, String data) {
		addInfoWidth(list, stack, data, 9 * 20);
	}

	@SideOnly(Side.CLIENT)
	public static void addInfoWidth(List<String> list, ItemStack stack, String data, int weight) {
		FontRenderer fontRenderer = StackHelper.isNull(stack) || stack.getItem() == StackHelper.nullItem() ? null : stack.getItem().getFontRenderer(stack);
		if (fontRenderer == null) fontRenderer = Minecraft.getMinecraft().fontRenderer;
		list.addAll(fontRenderer.listFormattedStringToWidth(data, weight));
	}

	public static ItemStack addEnchants(ItemStack input, Map<Enchantment, Integer> map) {
		for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
			input.addEnchantment(entry.getKey(), entry.getValue());
		}
		return input;
	}

	public static ICapabilityProvider getBaubleProvider(String name) {
		return getBaubleProvider(name, null);
	}

	public static ICapabilityProvider getBaubleProvider(String name, @Nullable ICapabilityProvider base) {
		return new ICapabilityProvider() {
			@Override
			public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
				return (capGetter != null && getBauble != null && capability == capGetter.get()) || (base != null && base.hasCapability(capability, facing));
			}

			@Nullable
			@Override
			public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
				if (capGetter != null && getBauble != null && capability == capGetter.get())
					return (T) getBauble.apply(name);
				return base != null ? base.getCapability(capability, facing) : null;
			}
		};

	}

}
