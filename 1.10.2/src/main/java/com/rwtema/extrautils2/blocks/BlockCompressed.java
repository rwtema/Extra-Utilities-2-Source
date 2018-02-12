package com.rwtema.extrautils2.blocks;

import com.google.common.base.Throwables;
import com.rwtema.extrautils2.backend.XUBlockFull;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.textures.SpriteCompressed;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.blockaccess.BlockAccessDelegate;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class BlockCompressed extends XUBlockFull {
	public final PropertyInteger property_compression;
	private final IBlockState baseState;
	private final String texture;
	private final int max_n;
	private final Block baseBlock;


	public BlockCompressed(IBlockState baseState, String texture, int max_n) {
		super(baseState.getMaterial());
		this.baseState = baseState;
		this.baseBlock = baseState.getBlock();
		this.texture = texture;
		this.max_n = max_n;
		this.setHardness(2);
		this.setResistance(10.0F);
		property_compression = PropertyInteger.create("compression_level_" + texture.toLowerCase(), 1, max_n);
		setBlockState(new XUBlockStateCreator(this, false, property_compression));
	}

	@Override
	public void registerTextures() {
		for (int i = 0; i <= max_n; i++) {
			Textures.textureNames.put("compr_" + texture + "_" + i, new SpriteCompressed(texture, i, MathHelper.sqrt(max_n * 8) + 0.5F));
		}
	}

	@Override
	public String getTexture(IBlockState state, EnumFacing side) {
		int i = state.getValue(property_compression);
		return "compr_" + texture + "_" + i;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull EntityPlayer player) {
		return ForgeHooks.canHarvestBlock(baseBlock, player, new BlockAccessDelegate(world) {
			@Nonnull
			@Override
			public IBlockState getBlockState(@Nonnull BlockPos p) {
				return p.equals(pos) ? baseState : super.getBlockState(pos);
			}
		}, pos);
	}

	@Override
	public boolean isFireSource(@Nonnull World worldIn, BlockPos pos, EnumFacing side) {
		IBlockState oldState = worldIn.getBlockState(pos);
		try {
			worldIn.setBlockState(pos, baseState, 4);
			boolean v = baseBlock.isFireSource(worldIn, pos, side);
			worldIn.setBlockState(pos, oldState, 4);
			return v;
		} catch (Throwable err) {
			worldIn.setBlockState(pos, oldState, 4);
			throw Throwables.propagate(err);
		}
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		int value = state.getValue(property_compression);
		return value <= 6;
	}

	@Override
 	public float getExplosionResistance(World world, BlockPos pos, @Nonnull Entity exploder, Explosion explosion) {
		int value = world.getBlockState(pos).getValue(property_compression);
		return baseBlock.getExplosionResistance(exploder) * (int) Math.pow(1.5, value - 1);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		IBlockState state = xuBlockState.getStateFromItemStack(stack);
		int value = state.getValue(property_compression);
		tooltip.add(Lang.translateArgs("%s Blocks", Math.pow(9, value)));
	}

	@Override
	public float getBlockHardness(IBlockState state, World worldIn, BlockPos pos) {
		int value = worldIn.getBlockState(pos).getValue(property_compression);
		IBlockState oldState = worldIn.getBlockState(pos);
		try {
			worldIn.setBlockState(pos, baseState, 4);
			float v = baseState.getBlockHardness(worldIn, pos) * (float) Math.pow(2.25F, value - 1);
			worldIn.setBlockState(pos, oldState, 4);
			return v;
		} catch (Throwable err) {
			worldIn.setBlockState(pos, oldState, 4);
			throw Throwables.propagate(err);
		}
	}

}
