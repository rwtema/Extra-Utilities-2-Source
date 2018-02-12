package com.rwtema.extrautils2.entity.chunkdata;

import com.google.common.collect.HashBiMap;
import com.rwtema.extrautils2.RunnableClient;
import com.rwtema.extrautils2.interblock.FlatTransferNodeHandler;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.network.XUPacketServerToClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntityChunkData extends Entity implements IEntityAdditionalSpawnData {

	public static HashBiMap<String, ChunkDataModuleManager> managers;

	static {
		managers = HashBiMap.create();
		managers.put("flattransfernodes", FlatTransferNodeHandler.INSTANCE);
	}

	public Map<ChunkDataModuleManager, Object> objectHashMap = new HashMap<>();
	ChunkPos pos;
	private boolean dirty;
	public EntityChunkData(World worldIn) {
		super(worldIn);
		noClip = true;
		isImmuneToFire = true;
		setEntityInvulnerable(true);
		setSize(0, 0);
		setInvisible(true);
	}

	public EntityChunkData(World world, ChunkPos chunkPos) {
		this(world);
		this.pos = chunkPos;
		this.prevPosX = this.posX = (pos.x << 4) + 8;
		this.prevPosY = this.posY = 512;
		this.prevPosZ = this.posZ = (pos.z << 4) + 8;
	}

	public static void markChunkDirty(Chunk chunk) {
		EntityChunkData dataEntity = getChunkDataEntity(chunk);
		if (dataEntity != null) {
			dataEntity.dirty = true;
		}
	}

	public static <T> T getChunkData(Chunk chunk, ChunkDataModuleManager<T> manager, boolean create) {
		EntityChunkData dataEntity = getChunkDataEntity(chunk);
		if (dataEntity == null) {
			if (create) {
				World world = chunk.getWorld();
				dataEntity = new EntityChunkData(world, new ChunkPos(chunk.x, chunk.z));
				T blank = manager.createBlank();
				dataEntity.objectHashMap.put(manager, blank);
				world.spawnEntity(dataEntity);
				return blank;
			} else {
				return manager.getCachedBlank();
			}
		}

		Object o = dataEntity.objectHashMap.get(manager);
		if (o == null) {
			if (create) {
				T blank = manager.createBlank();
				dataEntity.objectHashMap.put(manager, blank);
				return blank;
			} else {
				return manager.getCachedBlank();
			}
		}
		return (T) o;
	}

	@Nullable
	public static EntityChunkData getChunkDataEntity(Chunk chunk) {
		ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
		for (int i = entityLists.length - 1; i >= 0; i--) {
			ClassInheritanceMultiMap<Entity> entities = entityLists[i];
			if (!entities.isEmpty()) {
				for (EntityChunkData entityChunkData : entities.getByClass(EntityChunkData.class)) {
					if (!entityChunkData.isDead) {
						return entityChunkData;
					}
				}
			}
		}
		return null;
	}

	public static void writeData(ByteBuf buffer, Map<ChunkDataModuleManager, Object> objectHashMap) {
		XUPacketBuffer packetBuffer = new XUPacketBuffer(buffer);
		packetBuffer.writeInt(objectHashMap.size());
		for (Map.Entry<ChunkDataModuleManager, Object> entry : objectHashMap.entrySet()) {
			ChunkDataModuleManager manager = entry.getKey();
			packetBuffer.writeString(managers.inverse().get(manager));
			manager.writeData(entry.getValue(), packetBuffer);
		}
	}

	public static void readSpawnData(ByteBuf additionalData, Map<ChunkDataModuleManager, Object> objectHashMap) {
		objectHashMap.clear();
		XUPacketBuffer buffer = new XUPacketBuffer(additionalData);
		int n = buffer.readInt();
		for (int i = 0; i < n; i++) {
			ChunkDataModuleManager manager = managers.get(buffer.readString());
			Object blank = Validate.notNull(manager).createBlank();
			manager.readData(blank, buffer);
			objectHashMap.put(manager, blank);
		}
	}

	@Override
	public int getMaxInPortalTime() {
		return Integer.MAX_VALUE;
	}

	public <T> boolean hasData(ChunkDataModuleManager<T> manager) {
		return objectHashMap.containsKey(manager);
	}

	public <T> T getData(ChunkDataModuleManager<T> manager) {
		return (T) objectHashMap.get(manager);
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	protected boolean canBeRidden(Entity entityIn) {
		return false;
	}

	@Override
	protected boolean canFitPassenger(Entity passenger) {
		return false;
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	@Override
	public boolean canRiderInteract() {
		return false;
	}

	@Override
	public boolean canRenderOnFire() {
		return false;
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	@Nullable
	@Override
	public Entity changeDimension(int dimensionIn) {
		return null;
	}

	@Override
	protected void dealFireDamage(int amount) {

	}

	@Override
	protected void entityInit() {

	}

	@Override
	public void onUpdate() {
		if (this.world.isRemote) {
			Chunk chunkFromChunkCoords = world.getChunkFromBlockCoords(new BlockPos(this));
			for (Iterator<Map.Entry<ChunkDataModuleManager, Object>> iterator = objectHashMap.entrySet().iterator(); iterator.hasNext(); ) {
				Map.Entry<ChunkDataModuleManager, Object> entry = iterator.next();
				try {
					entry.getKey().clientTick(chunkFromChunkCoords, entry.getValue());
				} catch (ClassCastException exception) {
					exception.printStackTrace();
					iterator.remove();
				}
			}
		} else {
			this.prevPosX = this.posX = (pos.x << 4) + 8;
			this.prevPosY = this.posY = 512;
			this.prevPosZ = this.posZ = (pos.z << 4) + 8;

			if (objectHashMap.isEmpty()) {
				setDead();
			} else {
				Chunk chunkFromChunkCoords = world.getChunkFromChunkCoords(pos.x, pos.z);
				for (Iterator<Map.Entry<ChunkDataModuleManager, Object>> iterator = objectHashMap.entrySet().iterator(); iterator.hasNext(); ) {
					Map.Entry<ChunkDataModuleManager, Object> entry = iterator.next();
					try {
						if (entry.getKey().onUpdate(chunkFromChunkCoords, entry.getValue())) {
							iterator.remove();
							dirty = true;
						}
					} catch (ClassCastException exception) {
						exception.printStackTrace();
						iterator.remove();
						dirty = true;
					}
				}

				if (dirty) {
					dirty = false;

					if (world instanceof WorldServer) {
						EntityTracker tracker = ((WorldServer) world).getEntityTracker();
						for (EntityPlayer player : tracker.getTrackingPlayers(this)) {
							NetworkHandler.sendPacketToPlayer(new PacketEntityChunkData(getEntityId(), objectHashMap), player);
						}
					}
				}
			}
		}

		super.onUpdate();
	}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
		pos = new ChunkPos(compound.getInteger("chunk_x"), compound.getInteger("chunk_z"));

		objectHashMap.clear();
		NBTTagList data = compound.getTagList("xudata", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < data.tagCount(); i++) {
			NBTTagCompound tagAt = data.getCompoundTagAt(i);
			ChunkDataModuleManager manager = managers.get(tagAt.getString("key"));
			if (manager != null) {
				Object o = manager.readFromNBT(tagAt);
				objectHashMap.put(manager, o);
			}
		}
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
		compound.setInteger("chunk_x", pos.x);
		compound.setInteger("chunk_z", pos.z);

		NBTTagList data = new NBTTagList();

		for (Iterator<Map.Entry<ChunkDataModuleManager, Object>> iterator = objectHashMap.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<ChunkDataModuleManager, Object> entry = iterator.next();
			String s = managers.inverse().get(entry.getKey());
			NBTTagCompound nbtTagCompound = new NBTTagCompound();
			nbtTagCompound.setString("key", s);
			try {
				entry.getKey().writeToNBT(nbtTagCompound, entry.getValue());
			} catch (ClassCastException exception) {
				iterator.remove();
				exception.printStackTrace();
				continue;
			}

			data.appendTag(nbtTagCompound);
		}

		compound.setTag("xudata", data);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		writeData(buffer, objectHashMap);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		readSpawnData(additionalData, objectHashMap);
	}

	@NetworkHandler.XUPacket
	public static class PacketEntityChunkData extends XUPacketServerToClient {

		int entityId;
		Map<ChunkDataModuleManager, Object> objectHashMap;

		public PacketEntityChunkData() {
			super();
		}

		public PacketEntityChunkData(int entityId, Map<ChunkDataModuleManager, Object> objectHashMap) {
			this.entityId = entityId;
			this.objectHashMap = objectHashMap;
		}

		@Override
		public void writeData() throws Exception {
			writeInt(entityId);
			EntityChunkData.writeData(data, objectHashMap);
		}

		@Override
		public void readData(EntityPlayer player) {
			entityId = readInt();
			objectHashMap = new HashMap<>();
			EntityChunkData.readSpawnData(data, objectHashMap);
		}

		@Override
		public Runnable doStuffClient() {
			return new RunnableClient() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					Entity entityByID = Minecraft.getMinecraft().world.getEntityByID(entityId);
					if (entityByID instanceof EntityChunkData) {
						((EntityChunkData) entityByID).objectHashMap = objectHashMap;
					}
				}
			};
		}
	}
}
