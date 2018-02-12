package com.rwtema.extrautils2.fairies;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class Fairy implements INBTSerializable<NBTTagCompound> {
	public static int currentID;
	public final int id = currentID++;
	private final HashMap<String, INBTSerializable> nbtHandlers = new HashMap<>();
	public NBTSerializable.Vec pos = registerNBT("pos", new NBTSerializable.Vec());
	public NBTSerializable.Vec dest = registerNBT("dest", new NBTSerializable.Vec());
	public double speed;
	public boolean joinedWorld;
	public boolean dirty = true;
	public boolean dead = false;

	public void moveToDest(Vec3d vec, double speed) {
		this.speed = speed;
		dest.set(vec);
		dirty = true;
	}

	public void moveTick() {
		if (speed == 0) return;

		double dx = dest.x - pos.x;
		double dy = dest.y - pos.y;
		double dz = dest.z - pos.z;
		double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (d < speed) {
			pos.x = dest.x;
			pos.y = dest.y;
			pos.z = dest.z;
			speed = 0;
		} else {
			pos.x += dx / d * speed;
			pos.y += dy / d * speed;
			pos.z += dz / d * speed;
		}
	}

	public boolean atDestination() {
		return pos.x == dest.x && pos.y == dest.y && pos.z == dest.z;
	}

	@OverridingMethodsMustInvokeSuper
	public void addToPacket(XUPacketBuffer buffer) {
		pos.writeToPacket(buffer);
		dest.writeToPacket(buffer);
		buffer.writeDouble(speed);
	}

	@OverridingMethodsMustInvokeSuper
	public void getFromPacket(XUPacketBuffer buffer) {
		pos.readFromPacket(buffer);
		dest.readFromPacket(buffer);
		speed = buffer.readDouble();
	}

	protected <T extends INBTSerializable> T registerNBT(String key, T t) {
		nbtHandlers.put(key, t);
		return t;
	}

	@OverridingMethodsMustInvokeSuper
	public void writeToNBT(NBTTagCompound nbt) {
		for (Map.Entry<String, INBTSerializable> entry : nbtHandlers.entrySet()) {
			String key = entry.getKey();
			NBTBase value = entry.getValue().serializeNBT();
			nbt.setTag(key, value);
		}
		nbt.setDouble("speed", speed);
	}

	@SuppressWarnings("unchecked")
	@OverridingMethodsMustInvokeSuper
	public void readFromNBT(NBTTagCompound compound) {
		for (Map.Entry<String, INBTSerializable> entry : nbtHandlers.entrySet()) {
			NBTBase tag = compound.getTag(entry.getKey());
			entry.getValue().deserializeNBT(tag);
		}
		speed = compound.getDouble("speed");
	}


	public void joinWorld(World world, Vec3d position) {
		pos.set(position);
		dest.set(position);
		dirty = true;

		if (!joinedWorld) {
			Fairies.register(world, this);
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}
}
