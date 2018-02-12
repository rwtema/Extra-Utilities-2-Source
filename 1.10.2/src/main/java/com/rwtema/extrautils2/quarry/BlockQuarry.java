package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.particles.ParticleSplineDot;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.helpers.VecHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class BlockQuarry extends XUBlockStatic {
	public BlockQuarry() {
		setLightLevel(1);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		return BoxModel.newStandardBlock("quantum_quarry");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(Lang.translate("Mine blocks from a hypothetical dimension that might have existed"));
		tooltip.add(Lang.translate("Requires Actuators to be attached to all sides"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof TileQuarry) {
			if (((TileQuarry) tileEntity).hasNearbyBlocks()) {
				if (rand.nextBoolean()) {
					EnumFacing facing1 = XURandom.getRandomElement(EnumFacing.values());
					EnumFacing facing2;

					do {
						facing2 = XURandom.getRandomElement(EnumFacing.values());
					} while (facing1.getAxis() == facing2.getAxis());


					float c = rand.nextFloat();

					Vec3d p = new Vec3d(pos).addVector(0.5, 0.5, 0.5);

					double speed = 5;
					double speed2 = 2;
					Minecraft.getMinecraft().effectRenderer.addEffect(
							new ParticleSplineDot(
									worldIn,
									VecHelper.addSide(p, facing1, 1.5),
									VecHelper.addSide(p, facing2, 1.5),
									VecHelper.addSide(Vec3d.ZERO, facing1, speed).addVector(rand.nextGaussian() * speed2, rand.nextGaussian() * speed2, rand.nextGaussian() * speed2),
									VecHelper.addSide(Vec3d.ZERO, facing2, -speed).addVector(rand.nextGaussian() * speed2, rand.nextGaussian() * speed2, rand.nextGaussian() * speed2),
									c * (48 / 255.0F),
									1,
									251.0F / 255.0F - c * ((48.0f) / 255.0F),
									100
							)
					);
				}
			}

		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	@Nonnull
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileQuarry();
	}

}
