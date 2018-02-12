package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.api.machine.XUMachineEnchanter;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockConnectedTextureBase;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.compatibility.XUShapelessRecipe;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.crafting.EnchantRecipe;
import com.rwtema.extrautils2.textures.ConnectedTexture;
import com.rwtema.extrautils2.textures.ISolidWorldTexture;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;
import com.rwtema.extrautils2.utils.helpers.OreDicHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockDecorativeSolidWood extends XUBlockConnectedTextureBase {
	public static final PropertyEnumSimple<DecorStates> decor = new PropertyEnumSimple<>(DecorStates.class);

	public BlockDecorativeSolidWood() {
		super(Material.WOOD);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		for (DecorStates decorState : DecorStates.values()) {
			decorState.tex = decorState.createTexture(this);
		}
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator(this, false, decor);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}

	@Override
	public ISolidWorldTexture getConnectedTexture(IBlockState state, EnumFacing side) {
		return state.getValue(decor).tex;
	}

	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) return 0;
		return state.getValue(decor).enchantBonus;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		xuBlockState.getStateFromItemStack(stack).getValue(decor).addInformation(stack, playerIn, tooltip, advanced);
	}

	public enum DecorStates implements IItemStackMaker {
		magical_planks {
			{
				enchantBonus = 0.625f;
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("magic_wood_planks", newStack(4), magical_wood.newStack(1));
			}
		},
		magical_wood {
			{

				enchantBonus = 2.5F;
			}

			@Override
			public void addRecipes() {
				OreDictionary.registerOre("blockMagicalWood", newStack());
				OreDicHelper.extendVanillaOre("bookshelf", Blocks.BOOKSHELF);
				ResourceLocation resourceLocation = CraftingHelper.createLocation("magic_wood");
				CraftingHelper.addRecipe(new EnchantRecipe(resourceLocation, new XUShapelessRecipe(resourceLocation, newStack(1), "bookshelf", "ingotGold"), 4,
						ImmutableList.of(
								OreDictionary.getOres("bookshelf"),
								OreDictionary.getOres("ingotGold")
						), null
				));

				if (XU2Entries.machineEntry.isActive()) {
					XUMachineEnchanter.addRecipe(OreDictionary.getOres("bookshelf"), 1, newStack(), 1, 64000, "gemLapis");
				}
			}

			@Override
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				ItemStackHelper.addInfoWidth(tooltip, stack, Lang.translate("Boosts max level of enchanting tables by 2.5 levels"));
			}
		},
		diagonalwood {
			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("diagonal_wood", newStack(5), "SR", "RS", 'S', "stairWood", 'R', "plankWood");
			}
		};


		@SideOnly(Side.CLIENT)
		public ISolidWorldTexture tex;
		public float enchantBonus = 0;

		public abstract void addRecipes();

		public ItemStack newStack(int amount) {
			return XU2Entries.decorativeSolidWood.newStack(amount, decor, this);
		}

		public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

		}


		@Nonnull
		@SideOnly(Side.CLIENT)
		public ISolidWorldTexture createTexture(XUBlockConnectedTextureBase block) {
			return new ConnectedTexture(toString(), block.xuBlockState.defaultState.withProperty(decor, this), block);
		}

		@Override
		public ItemStack newStack() {
			return newStack(1);
		}
	}
}
