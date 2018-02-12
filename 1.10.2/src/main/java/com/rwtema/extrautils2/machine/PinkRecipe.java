package com.rwtema.extrautils2.machine;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.blockaccess.BlockAccessEmpty;
import com.rwtema.extrautils2.utils.blockaccess.BlockAccessSingle;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.*;

public class PinkRecipe extends EnergyBaseRecipe {
	static Set<ItemRef> pinkThings;

	static {
		MinecraftForge.EVENT_BUS.register(PinkRecipe.class);
	}

	@Mod.EventHandler
	public static void oreRegister(OreDictionary.OreRegisterEvent event) {
		pinkThings = null;
	}

	@Override
	public int getEnergyOutput(@Nonnull ItemStack stack) {
		ItemRef itemRef = ItemRef.wrapNoNBT(stack);
		if (pinkThings == null) {
			pinkThings = new HashSet<>();
			for (Block block : Block.REGISTRY) {
				for (IBlockState state : block.getBlockState().getValidStates()) {
//					boolean flag = state.getMapColor() == MapColor.PINK;
					boolean flag = false;
					if (!flag) {
						for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
							if (entry.getKey().getValueClass() == EnumDyeColor.class) {
								if (entry.getValue() == EnumDyeColor.PINK) {
									flag = true;
									break;
								}
							}
							if (entry.getKey().getName().contains("color")) {
								if (((IProperty) entry.getKey()).getName(entry.getValue()).contains("pink")) {
									flag = true;
									break;
								}
							}
						}
					}

					if (flag) {
						try {
							for (int i = 0; i < 10; i++) {
								List<ItemStack> drops = block.getDrops(BlockAccessEmpty.INSTANCE, BlockAccessSingle.CENTER, state, 300);
								for (ItemStack drop : drops) {
									if (StackHelper.isNonNull(drop))
										pinkThings.add(ItemRef.wrapCrafting(drop));
								}
							}
						} catch (RuntimeException err) {
							err.printStackTrace();
						}
					}
				}
			}
			for (String s : OreDictionary.getOreNames()) {
				if (s.toLowerCase(Locale.ROOT).contains("pink")) {
					for (ItemStack itemStack : OreDictionary.getOres(s)) {
						pinkThings.add(ItemRef.wrapCrafting(itemStack));
					}
				}
			}
		}

		if (pinkThings.contains(itemRef) || (itemRef.hasMeta() && pinkThings.contains(itemRef.toNoMetaVersion()))) {
			return 400;
		}

		return 0;
	}

	@Override
	protected float getEnergyRate(@Nonnull ItemStack stack) {
		return 40;
	}

	@Nonnull
	@Override
	public Collection<ItemStack> getInputValues() {
		return EnergyBaseRecipe.getCreativeStacks(t -> getEnergyOutput(t) > 0, null);
	}
}
