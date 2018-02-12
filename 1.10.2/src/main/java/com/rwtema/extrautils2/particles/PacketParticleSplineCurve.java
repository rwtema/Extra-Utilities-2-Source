package com.rwtema.extrautils2.particles;

import com.rwtema.extrautils2.RunnableClient;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.SplineHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NetworkHandler.XUPacket
public class PacketParticleSplineCurve extends XUPacketServerToClient {
	Vec3d startPos;
	Vec3d endPos;
	Vec3d startVel;
	Vec3d endVel;

	int color;

	public PacketParticleSplineCurve() {

	}

	public PacketParticleSplineCurve(Vec3d startPos, Vec3d endPos, Vec3d startVel, Vec3d endVel, int color) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.startVel = startVel;
		this.endVel = endVel;
		this.color = color;
	}

	public static Vec3d matchSpeed(EntityItem item, Vec3d v) {
		double s = 5;
		return new Vec3d(v.x * s, v.y * s, v.z * s);
	}

	@Override
	public void writeData() throws Exception {
		writeVec(startPos);
		writeVec(endPos);
		writeVec(startVel);
		writeVec(endVel);
		writeInt(color);
	}

	@Override
	public void readData(EntityPlayer player) {
		startPos = readVec();
		endPos = readVec();
		startVel = readVec();
		endVel = readVec();
		color = readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Runnable doStuffClient() {
		return new RunnableClient() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				double v = 0.05 / endPos.subtract(startPos).lengthVector();

				double[] xParam = SplineHelper.splineParams(startPos.x, endPos.x, startVel.x, endVel.x);
				double[] yParam = SplineHelper.splineParams(startPos.y, endPos.y, startVel.y, endVel.y);
				double[] zParam = SplineHelper.splineParams(startPos.z, endPos.z, startVel.z, endVel.z);

				float f = XURandom.rand.nextFloat() * 0.6F + 0.4F;

				for (double h = v; h <= 1; h += v) {

					double x = SplineHelper.evalSpline(h, xParam);
					double y = SplineHelper.evalSpline(h, yParam);
					double z = SplineHelper.evalSpline(h, zParam);

					Minecraft.getMinecraft().effectRenderer.addEffect(
							new ParticleDot(
									Minecraft.getMinecraft().world,
									x, y, z,
									(0.8F + XURandom.rand.nextFloat() * 0.2F) * ColorHelper.getRF(color),
									(0.8F + XURandom.rand.nextFloat() * 0.2F) * ColorHelper.getGF(color),
									(0.8F + XURandom.rand.nextFloat() * 0.2F) * ColorHelper.getBF(color)
							)
					);
				}
			}


		};
	}
}
