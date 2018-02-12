package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ItemDarkCrystal extends XUItemFlatMetadata {
	WeakHashMap<World, List<BlockPos>> positions = new WeakHashMap<>();

	public ItemDarkCrystal() {
		super("dark_crystal");
		setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTick(TickEvent.WorldTickEvent event) {
		if ((event.world.getTotalWorldTime() % 512) == 0) {
			ArrayList<BlockPos> tickers = new ArrayList<>();

			for (TileEntity tileEntity : event.world.loadedTileEntityList) {
				IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				if (itemHandler != null) {
					for (int i = 0; i < itemHandler.getSlots(); i++) {
						ItemStack stackInSlot = itemHandler.getStackInSlot(i);
						if (stackInSlot.getItem() == this) {
							tickers.add(tileEntity.getPos());
							break;
						}
					}
				}
			}
			if (!tickers.isEmpty()) {
				positions.put(event.world, tickers);
			} else {
				positions.remove(event.world);
			}
		}

	}
}
