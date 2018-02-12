package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleRedstoneCompat extends ParticleRedstone {
	protected ParticleRedstoneCompat(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, float p_i46349_8_, float p_i46349_9_, float p_i46349_10_) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, p_i46349_8_, p_i46349_9_, p_i46349_10_);
	}

	protected ParticleRedstoneCompat(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, float p_i46350_8_, float p_i46350_9_, float p_i46350_10_, float p_i46350_11_) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, p_i46350_8_, p_i46350_9_, p_i46350_10_, p_i46350_11_);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		render(CompatClientHelper.wrap(buffer), entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	public void render(IVertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		super.renderParticle(CompatClientHelper.unwrap(buffer), entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}
}
