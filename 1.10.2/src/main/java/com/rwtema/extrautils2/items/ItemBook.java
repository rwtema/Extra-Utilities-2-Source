package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.book.BookHandler;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemBook extends XUItemFlatMetadata {
	public ItemBook() {
		super("book");
		setMaxStackSize(1);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClickBase(@Nonnull ItemStack itemStackIn, World worldIn, final EntityPlayer playerIn, EnumHand hand) {
		if (worldIn.isRemote) {
			ExtraUtils2.proxy.run(new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					ItemStack bookStack = BookHandler.newStack();
					if (StackHelper.isNull(bookStack)) return;
					Minecraft.getMinecraft().displayGuiScreen(new GuiScreenBook(playerIn, bookStack, false));
				}
			});
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
	}
}
