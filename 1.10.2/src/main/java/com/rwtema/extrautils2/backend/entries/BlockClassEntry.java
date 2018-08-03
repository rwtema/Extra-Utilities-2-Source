package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.tileentity.TileEntity;

public class BlockClassEntry<T extends XUBlock> extends BlockEntry<T> {
	Class<T> clazz;


	@SafeVarargs
	public BlockClassEntry(String name, Class<T> clazz, Class<? extends TileEntity>... teClazzes) {
		super(name, teClazzes);
		this.clazz = clazz;
	}


	@SafeVarargs
	public BlockClassEntry(Class<T> clazz, Class<? extends TileEntity>... teClazzes) {
		this(StringHelper.erasePrefix(clazz.getSimpleName(), "Block"), clazz, teClazzes);
	}

	@Override
	public T initValue() {
		try {
			return clazz.newInstance();
		} catch (Throwable throwable) {
			throw new RuntimeException("Could not init " + clazz, throwable);
		}
	}
}
