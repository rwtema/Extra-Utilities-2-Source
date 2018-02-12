package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.model.Textures;
import javax.annotation.Nullable;

import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEnderShard extends XUItemFlat {
	@SideOnly(Side.CLIENT)
	String[] tex;

	public ItemEnderShard() {
		setMaxStackSize(8);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		tex = new String[8];
		for (int i = 1; i <= 8; i++) {
			tex[i - 1] = "endershards/endershard_" + i;
			Textures.register(tex[i - 1]);
		}
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		int size;
		if (StackHelper.isNull(itemStack))
			size = 1;
		else {
			size = StackHelper.getStacksize(itemStack);
			if (size < 1 || size > 8) size = 1;
		}

		return tex[size - 1];
	}
}
