package com.rwtema.extrautils2.backend.multiblockstate;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;

public class XUBlockStateMulti extends XUBlockState {
	public final XUBlock mainBlock;

	public XUBlockStateMulti(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn, XUBlock mainBlock) {
		super(blockIn, propertiesIn);
		this.mainBlock = mainBlock;
	}
}
