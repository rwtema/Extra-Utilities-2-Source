package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;

public class BlockPeacefulTable extends XUBlockStatic {
	public static final PropertyEnumSimple<Type> TYPE = new PropertyEnumSimple<>(Type.class);
	public static final PropertyBool ACTIVE = PropertyBool.create("activated");

	@Override
	public BoxModel getModel(IBlockState state) {

		return null;
	}

	private enum Type {
		LEG,
		TOP,
		CONTROLLER
	}
}
