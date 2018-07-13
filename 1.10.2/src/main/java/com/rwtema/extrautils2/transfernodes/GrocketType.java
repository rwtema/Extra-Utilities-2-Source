package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public enum GrocketType {
	TRANSFER_NODE_ITEMS {
		@Override
		public Grocket create() {
			return new TransferNodeItem();
		}

		@Override
		public BoxModel createBaseModel() {
			return createBasicBox("transfernodes/transfernode_front");
		}
	},
	FILTER_ITEMS {
		@Override
		public Grocket create() {
			return new GrocketTransferFilter();
		}

		@Override
		public BoxModel createBaseModel() {
			BoxModel model = new BoxModel();
			model.addBoxI(4, 0, 4, 12, 3, 12, "transfernodes/filter_items");
			return model;
		}
	},
	TRANSFER_NODE_FLUIDS {
		@Override
		public Grocket create() {
			return new TransferNodeFluid();
		}

		@Override
		public BoxModel createBaseModel() {
			return createBasicBox("transfernodes/transfernode_front_blue");
		}
	},
	TRANSFER_NODE_ITEMS_RETRIEVE {
		@Override
		public Grocket create() {
			return new TransferNodeItem.Retrieve();
		}

		@Override
		public BoxModel createBaseModel() {
			return createBasicBox("transfernodes/transfernode_front_green");
		}
	},
	TRANSFER_NODE_FLUIDS_RETRIEVE {
		@Override
		public Grocket create() {
			return new TransferNodeFluid.Retrieve();
		}

		@Override
		public BoxModel createBaseModel() {
			return createBasicBox("transfernodes/transfernode_front_cyan");
		}
	}, FILTER_PIPE {
		@Override
		public Grocket create() {
			return new GrocketPipeFilter();
		}

		@Override
		public BoxModel createBaseModel() {
			BoxModel model = new BoxModel();
			model.addBoxI(5, 1, 5, 11, 5, 11, "transfernodes/filter_pipe");
			return model;
		}
	}, TRANSFER_NODE_ENERGY {
		@Override
		public Grocket create() {
			return new TransferNodeEnergy();
		}

		@Override
		public BoxModel createBaseModel() {
			return createBasicBox("transfernodes/transfernode_front_yellow");
		}
	};

	BoxModel[] cache = new BoxModel[6];


	;

	@Nonnull
	public static BoxModel createBasicBox(String s3) {
		BoxModel model = new BoxModel();
		model.addBoxI(1, 0, 1, 15, 1, 15);
		model.addBoxI(2, 1, 2, 14, 2, 14).setInvisible(0);
		model.addBoxI(4, 2, 4, 12, 4, 12).setTextureSides().setInvisible(0);
		model.setTextures("transfernodes/transfernode_side", 0, "transfernodes/transfernode_back", 1, s3);
		return model;
	}

	public abstract Grocket create();

	public abstract BoxModel createBaseModel();

	@Nonnull
	public BoxModel createBaseModel(EnumFacing facing) {
		BoxModel model;
		model = createBaseModel();
		model.rotateToSide(facing);
		for (Box box : model) {
			box.tint = 6 + facing.ordinal();
		}
		return model;
	}

	public ItemStack createStack() {
		return XU2Entries.grocket.newStack(ordinal());
	}
}
