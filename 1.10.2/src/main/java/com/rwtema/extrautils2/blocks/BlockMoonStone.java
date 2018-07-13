package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxMimic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.WorldHelper;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockMoonStone extends XUBlock {
	public static IBlockState mimicState = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE);

	public BlockMoonStone() {
		super(Material.ROCK);
	}

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		return getModel();
	}

//	@Override
//	public int getMixedBrightnessForBlock(IBlockAccess worldIn, BlockPos pos) {
//		BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
//		if (renderLayer == BlockRenderLayer.TRANSLUCENT) {
//			return 15 << 4 | 15 << 20;
//		}
//
//		return super.getMixedBrightnessForBlock(worldIn, pos);
//	}

	@Override
	public void registerTextures() {
		Textures.register("moon_stone_cutout");
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BoxModel getRenderModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		BoxModel model = BoxModel.newStandardBlock(false);
		model.add(new BoxMimic(world, pos, mimicState));

		WorldClient theWorld = world instanceof WorldClient ? (WorldClient) world : Minecraft.getMinecraft().world;

		if (theWorld != null && WorldHelper.isFullMoon(theWorld)) {
			Box box = model.addBox(-1 / 1024F, -1 / 1024F, -1 / 1024F, 1 + 1 / 1024F, 1 + 1 / 1024F, 1 + 1 / 1024F).setTexture("moon_stone_cutout");
			box.layer = BlockRenderLayer.TRANSLUCENT;
			int combinedLight = world.getCombinedLight(pos, 0);
			int b = (combinedLight >> 4) & 0xf;
			b *= WorldHelper.getMoonBrightness(theWorld);
			int alpha = (255 - 2 * b * 12);
			box.color = ColorHelper.makeAlphaWhite(alpha);
		}
		return model;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		BoxModel boxes = BoxModel.newStandardBlock();
		boxes.sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(mimicState);
		return boxes;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Blocks.STONE.getItemDropped(mimicState, rand, fortune);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return Blocks.STONE.damageDropped(mimicState);
	}

	@Override
	public int quantityDropped(Random random) {
		return 1;
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		List<ItemStack> drops = Blocks.STONE.getDrops(world, pos, mimicState, fortune);
		if (world instanceof World) {
			World world1 = (World) world;
			if (WorldHelper.isFullMoon(world1)) {
				drops.add(ItemIngredients.Type.MOON_STONE.newStack(1));
			}
		}
		return drops;
	}

	public BoxModel getModel() {
		return BoxModel.newStandardBlock();
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		return Blocks.STONE.canRenderInLayer(mimicState, layer) || layer == BlockRenderLayer.TRANSLUCENT;
	}
}
