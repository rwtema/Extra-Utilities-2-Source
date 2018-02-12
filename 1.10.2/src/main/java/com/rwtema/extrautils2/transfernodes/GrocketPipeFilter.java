package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.DynamicContainerTile;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.gui.backend.WidgetClickMCButtonChoices;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GrocketPipeFilter extends Grocket implements IDynamicHandler {
	public SingleStackHandlerFilter.EitherFilter filter = registerNBT("filter", new SingleStackHandlerFilter.EitherFilter());

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new TransferPipeContainer(player);
	}

	@Override
	public GrocketType getType() {
		return GrocketType.FILTER_PIPE;
	}

	@Override
	public float getPower() {
		return 0;
	}

	@Override
	public boolean shouldBlock(IBuffer buffer) {
		return !filter.matches(buffer);
	}

	@Override
	public boolean blockPipeConnection() {
		return false;
	}

	@Override
	public boolean blockTileConnection() {
		return true;
	}

	@Override
	public Priority getPriority() {
		return priority.value;
	}

	enum Priority {
		HIGH,
		NORMAL,
		LOW
	}

	public NBTSerializable.NBTEnum<Priority> priority = registerNBT("priority", new NBTSerializable.NBTEnum<>(Priority.HIGH));

	public class TransferPipeContainer extends DynamicContainerTile {
		public TransferPipeContainer(EntityPlayer player) {
			super(holder);
			addTitle("Transfer Filter");
			crop();
			addWidget(filter.newSlot(4, height + 4));
			addWidget(new WidgetClickMCButtonChoices<Priority>(4 + 18 + 4, height + 4) {
						@Override
						protected void onSelectedServer(Priority marker) {
							priority.value = marker;
							markDirty();
						}

						@Override
						public Priority getSelectedValue() {
							return priority.value;
						}
					}
							.addChoice(Priority.HIGH, "High Priority", null)
							.addChoice(Priority.NORMAL, "Neutral Priority", null)
							.addChoice(Priority.LOW, "Low Priority", null)
			);

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
