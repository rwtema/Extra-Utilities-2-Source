package com.rwtema.extrautils2.transfernodes;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.utils.CapGetter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class Grocket implements ITickable {
	public TileTransferHolder holder;
	public EnumFacing side;
	@Nullable
	private HashMap<String, INBTSerializable> nbtHandlers;

	@Override
	public void update() {

	}

	@Nonnull
	public BoxModel getWorldModel(EnumFacing facing) {
		GrocketType type = getType();
		BoxModel model = type.cache[facing.ordinal()];
		if (model == null) {
			model = type.createBaseModel(facing);
			type.cache[facing.ordinal()] = model;
		}
		return model;
	}

	public abstract GrocketType getType();

	public boolean onActivated(EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(this instanceof IDynamicHandler){
			if (!holder.getWorld().isRemote)
				holder.openGui(playerIn, this);
			return true;
		}
		return false;
	}

	public void validate() {

	}

	public void invalidate() {

	}

	public List<ItemStack> getDrops() {
		return ImmutableList.of(getBaseDrop());
	}

	public <T> T getInterface(TileEntity tileEntity, CapGetter<T> capability) {
		return capability.getInterface(tileEntity, side.getOpposite());
	}

	public <T> boolean hasInterface(TileEntity tileEntity, CapGetter<T> capability) {
		return tileEntity != null && capability.hasInterface(tileEntity, side.getOpposite());
	}

	public void writeToNBT(NBTTagCompound tag) {
		if (nbtHandlers != null)
			for (Map.Entry<String, INBTSerializable> entry : nbtHandlers.entrySet()) {
				tag.setTag(entry.getKey(), entry.getValue().serializeNBT());
			}
	}

	@SuppressWarnings("unchecked")
	public void readFromNBT(NBTTagCompound tag) {
		if (nbtHandlers != null)
			for (Map.Entry<String, INBTSerializable> entry : nbtHandlers.entrySet()) {
				NBTBase subTag = tag.getTag(entry.getKey());
				if (subTag != null)
					entry.getValue().deserializeNBT(subTag);
			}

	}

	public void onPlaced(EntityPlayer player) {

	}

	protected <T extends INBTSerializable> T registerNBT(String key, T t) {
		if (nbtHandlers == null) {
			nbtHandlers = new HashMap<>();
		}
		nbtHandlers.put(key, t);
		return t;
	}

	public void markDirty() {
		if (holder != null)
			holder.markDirty();
	}

	public abstract float getPower();

	public boolean shouldPipeHaveNozzle(EnumFacing facing) {
		return false;
	}

	public ItemStack getBaseDrop() {
		return new ItemStack(ItemGrocket.instance, 1, getType().ordinal());
	}

	public boolean shouldBlock(IBuffer buffer) {
		return false;
	}

	public GrocketPipeFilter.Priority getPriority() {
		return GrocketPipeFilter.Priority.NORMAL;
	}

	public boolean blockPipeConnection(){
		return true;
	}

	public boolean blockTileConnection(){
		return false;
	}

	@Nullable
	public <T> T getCapability(Capability<T> capability) {
		return null;
	}
}
