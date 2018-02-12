package com.rwtema.extrautils2.backend;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.ItemBlockCompat;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class XUItemBlock extends ItemBlockCompat implements IRegisterItemColors {
	public final static List<XUItemBlock> itemBlocks = Lists.newArrayList();
	public XUBlock block;

	public XUItemBlock(Block block) {
		super(block);
		this.block = (XUBlock) block;
		this.block.itemBlock = this;
		itemBlocks.add(this);
		this.setMaxDamage(0);
		if (this.block.getHasSubtypes()) {
			this.setHasSubtypes(true);
		}
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {

		if (!hasSubtypes) {
			return super.getUnlocalizedName(stack);
		} else {
			String suffix = block.getSuffix(stack);
			if (suffix.length() == 0) {
				return super.getUnlocalizedName(stack);
			}
			return this.block.getUnlocalizedName() + "." + suffix;
		}
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		if (!ExtraUtils2.deobf_folder)
			return super.getItemStackDisplayName(stack);



		String key = this.getUnlocalizedNameInefficiently(stack) + ".name";
		String name;
		if (!hasSubtypes)
			name = Lang.translate(key, StringHelper.sepWords(Block.REGISTRY.getNameForObject(this.block).getResourcePath().replace("Block", "")));
		else
			name = Lang.translate(key, StringHelper.sepWords(StringHelper.capFirst(this.block.xuBlockState.getStateFromItemStack(stack).dropName, false)));

		name = block.getOverrideStackDisplayName(stack, name );
		return name;
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@SideOnly(Side.CLIENT)
	public void registerTextures() {

	}

	public void postTextureRegister() {

	}

	public void clearCaches() {

	}

	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nonnull EntityPlayer playerIn, @Nonnull List<String> tooltip, boolean advanced) {
		block.addInformation(stack, playerIn, tooltip, advanced);
	}

	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		block.onCreated(stack, worldIn, playerIn);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack) {
		return block.getFontRenderer(stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		EnumActionResult t = block.hasEffect(stack);
		if (t != EnumActionResult.PASS) return t == EnumActionResult.SUCCESS;
		return super.hasEffect(stack);
	}

	@Nonnull
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		EnumRarity rarity = block.getRarity(stack);
		if (rarity != null) return rarity;
		return super.getRarity(stack);
	}


}
