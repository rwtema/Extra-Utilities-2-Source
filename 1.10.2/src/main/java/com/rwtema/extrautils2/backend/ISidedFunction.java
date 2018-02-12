package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.ExtraUtils2;
import java.util.concurrent.Callable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISidedFunction<F, T> {

	@SideOnly(Side.SERVER)
	T applyServer(F input);

	@SideOnly(Side.CLIENT)
	T applyClient(F input);


}
