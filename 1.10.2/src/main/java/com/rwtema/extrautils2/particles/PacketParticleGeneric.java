package com.rwtema.extrautils2.particles;

import com.rwtema.extrautils2.RunnableClient;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;


@NetworkHandler.XUPacket
public class PacketParticleGeneric extends XUPacketServerToClient {
	Type type;
	Object[] params;

	public PacketParticleGeneric() {

	}

	private PacketParticleGeneric(Type type, Object[] params) {
		this.type = type;
		this.params = params;
	}

	@Override
	public void writeData() throws Exception {
		for (int i = 0; i < type.paramClasses.length; i++) {
			writeTypeUnchecked(type.paramClasses[i], params[i]);
		}
	}

	@Override
	public void readData(EntityPlayer player) {
		for (int i = 0; i < type.paramClasses.length; i++) {
			params[i] = readTypeUnchecked(type.paramClasses[i]);
		}
	}

	@Override
	public Runnable doStuffClient() {
		return new RunnableClient() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				type.spawnParticle(params);
			}
		};
	}

	public enum Type {
		VASHTANERADA(Integer.class, Integer.class) {
			@Override
			@SideOnly(Side.CLIENT)
			public void spawnParticle(Object[] params) {
				Entity entity = Minecraft.getMinecraft().world.getEntityByID(getParam(Integer.class, params, 0));
				if (entity != null) {

				}
			}
		};

		final Class<?>[] paramClasses;

		Type(Class<?>... paramClasses) {
			this.paramClasses = paramClasses;
		}

		public PacketParticleGeneric createPacket(Object... objects) {
			for (int i = 0; i < objects.length; i++) {
				if (!paramClasses[i].isInstance(objects[i])) {
					throw new IllegalArgumentException(Arrays.toString(objects));
				}
			}

			return new PacketParticleGeneric(this, objects);
		}

		@SideOnly(Side.CLIENT)
		public abstract void spawnParticle(Object[] params);

		public <T> T getParam(Class<T> type, Object[] params, int i) {
			Validate.isTrue(paramClasses[i] == type);
			return (T) params[i];
		}
	}
}
