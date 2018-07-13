package com.rwtema.extrautils2.particles;

import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

@NetworkHandler.XUPacket
public class PacketParticleSplosion extends XUPacketServerToClient {
	int entityID;

	public PacketParticleSplosion() {
		super();
	}

	public PacketParticleSplosion(int entityID) {
		this.entityID = entityID;
	}

	@Override
	public void writeData() throws Exception {
		writeInt(entityID);
	}

	@Override
	public void readData(EntityPlayer player) {
		entityID = readInt();
	}

	@Override
	public Runnable doStuffClient() {
		return new Runnable() {
			@Override
			public void run() {
				WorldClient theWorld = Minecraft.getMinecraft().world;
				if (theWorld != null) {
					Entity entity = theWorld.getEntityByID(entityID);
					if (entity != null && !entity.isDead) {
						entity.setDead();
						if (entity instanceof EntityLivingBase) {
							EntityLivingBase livingBase = (EntityLivingBase) entity;
							for (int i = 0; i < 100; i++) {
								Minecraft.getMinecraft().effectRenderer.addEffect(
										new ParticleBlood(livingBase)
								);
							}
						}

					}
				}
			}
		};
	}
}
