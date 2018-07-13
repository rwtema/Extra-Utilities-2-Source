package com.rwtema.extrautils2.crafting.jei;

import com.rwtema.extrautils2.compatibility.ParticleRedstoneCompat;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.utils.XURandom;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class ParticlePing extends ParticleRedstoneCompat {
	public static final Random RANDOM = XURandom.rand;
	public static final float r = 255 / 255.0F;
	public static final float g = 255 / 255.0F;
	public static final float b = 255 / 255.0F;

	public ParticlePing(World world, int x, int y, int z) {
		super(world,
				x + randOffset(),
				y + randOffset(),
				z + randOffset(),
				r, g, b);

		this.particleMaxAge *= 10;
		this.motionX *= 0.1D;
		this.motionY *= 0.1D;
		this.motionZ *= 0.1D;
		this.particleScale *= 2;
	}

	public ParticlePing(WorldClient theWorld, BlockPos p) {
		this(theWorld, p.getX(), p.getY(), p.getZ());
	}

	public static double randOffset() {
		return 0.5 + (RANDOM.nextDouble() - 0.5) * RANDOM.nextDouble();
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
		this.resetPositionToBB();
	}

	@Override
	public void render(IVertexBuffer worldRenderer, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
		if (GlStateManager.depthState.depthTest.currentState) {
			super.render(worldRenderer, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
			Tessellator tessellator = Tessellator.getInstance();
			tessellator.draw();

			GlStateManager.disableDepth();
			worldRenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			super.render(worldRenderer, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
			tessellator.draw();
			GlStateManager.enableDepth();

			worldRenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		} else {
			super.render(worldRenderer, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
		}
	}
}
