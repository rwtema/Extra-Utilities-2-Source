package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.enchants.BoomerangEnchantment;
import com.rwtema.extrautils2.enchants.XUEnchantment;
import com.rwtema.extrautils2.entity.EntityBoomerang;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

public class ItemBoomerang extends XUItemFlat {
	public final static String TEXTURE_NAME = "boomerang";
	public final static String TEXTURE_NAME_EMPTY = "boomerang_missing";

	public static final BoomerangEnchantment EXPLODE = new BoomerangEnchantment("Kaboomerang", 3, Enchantment.Rarity.COMMON);
	public static final BoomerangEnchantment SPEED = new BoomerangEnchantment("Zoomerang", 3, Enchantment.Rarity.COMMON);
	public static final BoomerangEnchantment FLAMING = new BoomerangEnchantment("Burnerang", 1, Enchantment.Rarity.COMMON);
	public static final BoomerangEnchantment SHARPNESS = new BoomerangEnchantment("Bladerang", 5, Enchantment.Rarity.COMMON);
	public static final BoomerangEnchantment DIGGING = new BoomerangEnchantment("Boomereaperang", 1, Enchantment.Rarity.COMMON);

	static {
		XUEnchantment.makeMutuallyExclusive(DIGGING, EXPLODE);
	}

	public ItemBoomerang() {
		setMaxStackSize(1);
		setMaxDamage(64);
	}

	@Override
	public int getItemEnchantability() {
		return 1;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register(TEXTURE_NAME, TEXTURE_NAME_EMPTY);
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		if (!canThrowBoomerang(Minecraft.getMinecraft().player, EntityBoomerang.boomerangOwnersClient)) {
			InventoryPlayer inventory = Minecraft.getMinecraft().player.inventory;
			if (inventory.getItemStack() == itemStack)
				return TEXTURE_NAME_EMPTY;

			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				if (inventory.getStackInSlot(i) == itemStack)
					return TEXTURE_NAME_EMPTY;
			}
		}

		return TEXTURE_NAME;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClickBase(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!worldIn.isRemote) {
			if (!canThrowBoomerang(playerIn, EntityBoomerang.boomerangOwners))
				return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);

			EntityBoomerang boomerang = new EntityBoomerang(worldIn, playerIn, itemStackIn.copy());
			List<PotionEffect> potionEffects = PotionUtils.getEffectsFromStack(itemStackIn);
			if (!potionEffects.isEmpty()) {
				CompatHelper112.damage(itemStackIn, 1, playerIn.getRNG());
				int damage = itemStackIn.getItemDamage();
				if (damage >= getMaxDamage()) {
					itemStackIn.setItemDamage(0);
					NBTTagCompound nbt = itemStackIn.getTagCompound();
					if (nbt != null) {
						nbt.removeTag("Potion");
						nbt.removeTag("CustomPotionEffects");
						if (nbt.hasNoTags()) {
							//noinspection ConstantConditions
							itemStackIn.setTagCompound(null);
						}
					}
				} else {
					itemStackIn.setItemDamage(damage);
				}
			}

			worldIn.spawnEntity(boomerang);
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}

	public boolean canThrowBoomerang(EntityPlayer playerIn, WeakHashMap<Object, WeakReference<EntityBoomerang>> boomerangOwners) {
		if (playerIn != null && !playerIn.capabilities.isCreativeMode) {
			WeakReference<EntityBoomerang> reference = boomerangOwners.get(playerIn);
			if (reference != null) {
				EntityBoomerang boomerang = reference.get();
				if (boomerang != null && !boomerang.isDead) return false;
			}
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		List<PotionEffect> list = PotionUtils.getEffectsFromStack(stack);
		if (!list.isEmpty()) {
			PotionUtils.addPotionTooltip(stack, tooltip, 0.125F);
		}
	}


}
