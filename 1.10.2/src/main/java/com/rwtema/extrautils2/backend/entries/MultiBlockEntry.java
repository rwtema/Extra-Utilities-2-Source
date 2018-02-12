package com.rwtema.extrautils2.backend.entries;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.MultiBlockStateBuilder;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUItemBlock;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Locale;

public class MultiBlockEntry<T extends XUBlock> extends Entry<List<T>> {
	public Class<? extends XUItemBlock> xuItemBlockClass = XUItemBlock.class;
	MultiBlockStateBuilder<T> builder;
	T mainBlock;
	XUItemBlock mainItem;

	public MultiBlockEntry(MultiBlockStateBuilder<T> builder, String name) {
		super(name);
		this.builder = builder;
	}

	public <S extends MultiBlockEntry<T>> S setItemBlockClass(Class<? extends XUItemBlock> xuItemBlockClass) {
		this.xuItemBlockClass = xuItemBlockClass;
		return (S) this;
	}

	@Override
	public List<T> initValue() {
		return builder.createBlocks(null);
	}

	@Override
	public void preInitRegister() {
		for (int i = 0; i < value.size(); i++) {
			T t = value.get(i);
			if (i == 0) {
				t.setBlockName(ExtraUtils2.MODID + ":" + name.toLowerCase(Locale.ENGLISH));
				Pair<T, ItemBlock> tItemBlockPair = BlockEntry.registerBlockItemCombo(t, xuItemBlockClass, name);
				mainBlock = tItemBlockPair.getLeft();
				mainItem = (XUItemBlock) tItemBlockPair.getRight();
			} else {
				t.setBlockName(ExtraUtils2.MODID + ":" + name.toLowerCase(Locale.ENGLISH) + "." + i);
				xuItemBlockClass = XUItemBlock.class;
				BlockEntry.registerBlockItemCombo(t, null, name + "_" + i);
			}
		}
	}

	@Override
	public ItemStack newStack(int amount, int meta) {
		return new ItemStack(mainItem, amount, meta);
	}
}
