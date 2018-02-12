package com.rwtema.extrautils2.items;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.fluids.FluidColors;
import com.rwtema.extrautils2.gui.backend.DynamicGui;
import com.rwtema.extrautils2.gui.backend.WidgetSlotItemHandler;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBiomeMarker extends XUItemFlat {
	public ItemBiomeMarker() {
		super();
		setMaxStackSize(1);
	}

	@Nullable
	public static Biome getBiome(ItemStack stack) {
		if (StackHelper.isNull(stack)) return null;
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound == null || !tagCompound.hasKey("Biome", Constants.NBT.TAG_STRING)) return null;
		return Biome.REGISTRY.getObject(new ResourceLocation(tagCompound.getString("Biome")));
	}

	@Nonnull
	public static ItemStack setBiome(@Nonnull ItemStack itemStackIn, Biome biome) {
		itemStackIn = itemStackIn.copy();
		ResourceLocation nameForObject = Biome.REGISTRY.getNameForObject(biome);
		if (nameForObject == null) throw new IllegalStateException(biome + " is not registered");
		NBTHelper.getOrInitTagCompound(itemStackIn).setString("Biome", nameForObject.toString());
		return itemStackIn;
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		return super.getItemStackDisplayName(stack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		Biome biome = getBiome(stack);
		if (biome != null) {
			tooltip.add(biome.getBiomeName());

		}
	}

	@Override
	public void getSubItemsBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		ItemStack stack = new ItemStack(itemIn);
		subItems.add(stack);
		for (Biome biome : Biome.REGISTRY) {
			subItems.add(setBiome(stack, biome));
		}
	}

	@Override
	public void registerTextures() {
		Textures.register(
				"biome_marker",
				"biome_marker_active",
				"biome_marker_active_0",
				"biome_marker_active_1",
				"biome_marker_active_2",
				"biome_marker_active_3"
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addItemColors(ItemColors itemColors, BlockColors blockColors) {
		itemColors.registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(@Nonnull ItemStack stack, int tintIndex) {
				Biome biome = getBiome(stack);
				if (biome != null) {
					float d0 = MathHelper.clamp(biome.getTemperature(), 0.0F, 1.0F);
					float d1 = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
					int color = ColorizerGrass.getGrassColor(d0, d1);
					switch (tintIndex) {
						case 0:
							return biome.getModdedBiomeFoliageColor(color);
						case 2:
							return biome.getModdedBiomeGrassColor(color);
						case 1:
							return biome.getSkyColorByTemp(d0);
						case 3:
							int col = FluidColors.FLUID_COLOR.getUnchecked(FluidRegistry.WATER);

							int waterColor = biome.getWaterColor();
							if (waterColor == -1) return col;
							return ColorHelper.colorClamp(
									ColorHelper.getRF(col) * ColorHelper.getRF(waterColor),
									ColorHelper.getGF(col) * ColorHelper.getGF(waterColor),
									ColorHelper.getBF(col) * ColorHelper.getBF(waterColor), 1);

					}
				}
				return -1;
			}
		}, this);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClickBase(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		Biome biome = getBiome(itemStackIn);
		if (biome != null) {
			if (!playerIn.isSneaking()) {
				return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
			}
			NBTTagCompound nbt = itemStackIn.getTagCompound();
			if (nbt != null) {
				nbt.removeTag("Biome");
				if (nbt.hasNoTags()) {
					itemStackIn.setTagCompound(null);
				}
			}
			return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
		}
		Biome biomeForCoordsBody = worldIn.getBiomeForCoordsBody(new BlockPos(playerIn));
		itemStackIn = setBiome(itemStackIn, biomeForCoordsBody);
		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}

	@Override
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		if (StackHelper.isNull(itemStack) || getBiome(itemStack) == null) return "biome_marker";
		switch (renderPass) {
			case 0:
			default:
				return "biome_marker_active";
			case 1:
				return "biome_marker_active_0";
			case 2:
				return "biome_marker_active_1";
			case 3:
				return "biome_marker_active_2";
			case 4:
				return "biome_marker_active_3";
		}
	}

	@Override
	public int getRenderLayers(@Nullable ItemStack itemStack) {
		if (StackHelper.isNull(itemStack) || getBiome(itemStack) == null) return 1;
		return 5;
	}

	@Override
	public int getTint(ItemStack stack, int i) {
		if (i == 0 || StackHelper.isNull(stack) || getBiome(stack) == null) return -1;
		return i - 1;
	}

	@Override
	public TextureAtlasSprite getBaseTexture() {
		return Textures.getSprite("biome_marker");
	}


	@Override
	public int getMaxMetadata() {
		return 0;
	}


	public static class ItemBiomeHandler extends SingleStackHandler {
		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			return stack.getItem() == XU2Entries.biomeMarker.value ? super.getStackLimit(stack) : 0;
		}

		public WidgetSlotItemHandler getSlot(int x, int y) {
			return new WidgetSlotItemHandler(this, 0, x, y) {
				@Override
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					super.renderBackground(manager, gui, guiLeft, guiTop);
					if (!getHasStack()) {
						ItemStack stack = ItemIngredients.Type.BIOME_MARKER_BLANK.newStack();
						gui.renderStack(stack, guiLeft + getX() + 1, guiTop + getY() + 1, "");
					}
				}

				@Override
				public List<String> getToolTip() {
					if (isEmpty()) {
						return ImmutableList.of(new ItemStack(XU2Entries.biomeMarker.value).getDisplayName());
					}
					return null;
				}
			};
		}

		@Nullable
		public Biome getBiome() {
			return ItemBiomeMarker.getBiome(getStack());
		}
	}

}
