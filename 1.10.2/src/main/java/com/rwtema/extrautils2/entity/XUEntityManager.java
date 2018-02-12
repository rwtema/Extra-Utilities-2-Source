package com.rwtema.extrautils2.entity;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.entity.chunkdata.EntityChunkData;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class XUEntityManager {

	public static int id = 0;
	ImmutableMap<DataSerializer<?>, NBT<?, ?>> serializers = ImmutableMap.<DataSerializer<?>, NBT<?, ?>>builder()
			.put(DataSerializers.BYTE, new NBT<Byte, NBTTagByte>() {
				@Override
				public NBTTagByte write(Byte aByte) {
					return new NBTTagByte(aByte);
				}

				@Override
				public Byte read(NBTTagByte t) {
					return t.getByte();
				}
			})
			.put(DataSerializers.BLOCK_POS, new NBT<BlockPos, NBTTagLong>() {

				@Override
				public NBTTagLong write(BlockPos blockPos) {
					return new NBTTagLong(blockPos.toLong());
				}

				@Override
				public BlockPos read(NBTTagLong t) {
					return BlockPos.fromLong(t.getLong());
				}
			})
			.put(DataSerializers.BOOLEAN, new NBT<Boolean, NBTTagByte>() {

				@Override
				public NBTTagByte write(Boolean aBoolean) {
					return new NBTTagByte((byte) (aBoolean ? 1 : 0));
				}

				@Override
				public Boolean read(NBTTagByte t) {
					return t.getByte() != 0;
				}
			})
			.put(DataSerializers.FACING, new NBT<EnumFacing, NBTTagByte>() {
				@Override
				public NBTTagByte write(EnumFacing facing) {
					return new NBTTagByte((byte) facing.ordinal());
				}

				@Override
				public EnumFacing read(NBTTagByte t) {
					return EnumFacing.values()[t.getByte()];
				}
			})
			.build();

	public static void init() {
		registerEntity(EntityBoomerang.class, 64, 5, true);
		registerEntity(EntityChunkData.class, 64, Integer.MAX_VALUE, false );


		ExtraUtils2.proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				RenderingRegistry.registerEntityRenderingHandler(EntityBoomerang.class, new IRenderFactory<EntityBoomerang>() {
					@Override
					public Render<? super EntityBoomerang> createRenderFor(RenderManager renderManager) {
						return new RenderEntityBoomerang(renderManager);
					}
				});
			}
		});
	}

	private static void registerEntity(Class<? extends Entity> clazz, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		String name = clazz.getSimpleName().toLowerCase();
		if (name.startsWith("entity")) name = name.replace("entity", "");
		CompatHelper.registerEntity(clazz, trackingRange, updateFrequency, sendsVelocityUpdates, name, id);
		id++;
	}

	public static void readEntityDataManagersFromNBT(EntityDataManager watcher, NBTTagCompound tags) {

	}

	public static void writeEntityDataManagersToNBT(EntityDataManager watcher, NBTTagCompound tags) {

	}

	private interface NBT<T, K extends NBTBase> {
		K write(T t);

		T read(K t);
	}
}
