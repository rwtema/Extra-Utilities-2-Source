package com.rwtema.extrautils2.backend;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.achievements.AchievementHelper;
import com.rwtema.extrautils2.backend.entries.ItemEntry;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.compatibility.ItemCompat;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class XUItem extends ItemCompat implements IXUItem {
	public static final List<IXUItem> items = Lists.newArrayList();
	public ItemEntry<?> entry;

	public XUItem() {
		this.setCreativeTab(ExtraUtils2.creativeTabExtraUtils);
		items.add(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createModel(int metadata) {
		return new PassthruModelItem(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void postTextureRegister() {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsTool() {
		return false;
	}

	@Override
	public void clearCaches() {

	}

	public final void register(String... textures) {
		ExtraUtils2.proxy.registerTexture(textures);
	}

	@Override
	public boolean allowOverride() {
		return true;
	}

	public void openItemGui(EntityPlayer playerIn) {
		playerIn.openGui(ExtraUtils2.instance, -1, playerIn.world, 0, 0, 0);
	}

	public void openItemGui(EntityPlayer playerIn, int x, int y, int z) {
		playerIn.openGui(ExtraUtils2.instance, -1, playerIn.world, x, y, z);
	}

	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		AchievementHelper.checkForPotentialAwards(playerIn, stack);
	}


	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		if (!ExtraUtils2.deobf_folder)
			return super.getItemStackDisplayName(stack);

		String key = this.getUnlocalizedNameInefficiently(stack) + ".name";
		String item = StringHelper.sepWords(Item.REGISTRY.getNameForObject(this).getResourcePath().replace("Item", ""));

		if (!hasSubtypes || getUnlocalizedName().equals(getUnlocalizedName(stack))) {
			return Lang.translate(key, item);
		} else {
			return Lang.translate(key, item + " " + stack.getItemDamage());
		}
	}

	@Override
	public final String getUnlocalizedNameInefficiently(@Nonnull ItemStack stack) {
		return super.getUnlocalizedNameInefficiently(stack);
	}

	public void getSubItemsBase(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		super.getSubItemsBase(itemIn, tab, subItems);
	}


}
