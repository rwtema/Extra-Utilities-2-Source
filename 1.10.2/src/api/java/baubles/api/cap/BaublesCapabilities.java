package baubles.api.cap;

import baubles.api.IBauble;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class BaublesCapabilities {
	/**
	 * Access to the baubles capability. 
	 */
	@CapabilityInject(IBaublesItemHandler.class)
	public static final Capability<IBaublesItemHandler> CAPABILITY_BAUBLES = null;

	@CapabilityInject(IBauble.class)
	public static final Capability<IBauble> CAPABILITY_ITEM_BAUBLE = null;

}