package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.api.recipes.ICustomRecipeMatching;
import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.EntityCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.block.BlockFence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemGoldenLasso extends XUItemFlat implements ICustomRecipeMatching {
	public final static String NBT_ANIMAL = "Animal";

	public final static String NBT_ANIMAL_DISPLAY = "Animal_Metadata";
	public final static String NBT_GOLDEN_LASSO_PREVENT = "[GoldenLassoPrevent]";
	public static final List<ItemStack> genericNiceRecipeItemList = new ArrayList<>();
	public static final List<ItemStack> genericEvilRecipeItemList = new ArrayList<>();
	public final static String NBT_ANIMAL_ALREADYPICKEDUP = "CursedLassoPickedUp";
	public static final String NBT_ANIMAL_NOPLACE = "No_Place";
	public static final String NBT_ANIMAL_NOPLACE_OLD = "NoPlace";


	public ItemGoldenLasso() {
		setHasSubtypes(true);
		setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static ItemStack newCraftingVillagerStack(boolean needsContract, VanillaProfessions profession) {
		ItemStack itemStack = newCraftingStack(EntityVillager.class);
		NBTTagCompound tags = NBTHelper.getOrInitTagCompound(itemStack.getTagCompound(), NBT_ANIMAL);

		if (needsContract) {
			NBTHelper.getOrInitTagCompound(tags, "ForgeData").setBoolean(ItemContract.TAG_UNDER_CONTRACT, true);
		}

		if (profession != null) {
			tags.setInteger("Profession", profession.profession);
			tags.setInteger("Career", profession.career);
		}

		return itemStack;
	}

	public static ItemStack newCraftingStack(Class<? extends Entity> entity) {
		int meta = IMob.class.isAssignableFrom(entity) ? 1 : 0;
		ItemStack itemStack = XU2Entries.goldenLasso.newStack(1, meta);
		NBTTagCompound animalTag = NBTHelper.getOrInitTagCompound(NBTHelper.getOrInitTagCompound(itemStack), NBT_ANIMAL);
		animalTag.setString("id", EntityCompat.getKey(entity));
		setNoPlace(itemStack);
		return itemStack;
	}

	public static void setNoPlace(ItemStack cursedLasso) {
		cursedLasso.getTagCompound().setBoolean(NBT_ANIMAL_NOPLACE, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsTool() {
		return true;
	}

	@Override
	public int getMaxMetadata() {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register("dark_lasso", "golden_lasso", "lasso_internal_1", "lasso_internal_2");
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return hasAnimal(stack);
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack) {
		ItemStack copy = stack.copy();
		NBTTagCompound tagCompound = copy.getTagCompound();
		tagCompound.removeTag(NBT_ANIMAL);
		tagCompound.removeTag("display");
		return copy;
	}

	@Override
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		switch (renderPass) {
			case 1:
				return "lasso_internal_1";
			case 2:
				return "lasso_internal_2";
			default:
				if (StackHelper.isNonNull(itemStack) && itemStack.getItemDamage() == 1)
					return "dark_lasso";

				return "golden_lasso";
		}
	}

	@Override
	public int getRenderLayers(@Nullable ItemStack itemStack) {
		return hasAnimal(itemStack) ? 3 : 1;
	}

	@Override
	public boolean renderLayerIn3D(ItemStack stack, int renderPass) {
		return renderPass == 0;
	}

	@Override
	public int getTint(ItemStack stack, int i) {
		switch (i) {
			case 1:
				return 0;
			case 2:
				return 1;
			default:
				return -1;
		}
	}
//
//	@Override
//	@SideOnly(Side.CLIENT)
//	public int getColorFromItemStack(ItemStack stack, int renderPass) {
//		NBTTagCompound tags = stack.getTagCompound();
//		if (!hasAnimal(tags))
//			return 16777215;
//
//		EntityList.EntityEggInfo egg = getEgg(tags.getCompoundTag(NBT_ANIMAL));
//		if (egg == null) return 16777215;
//		else {
////			if ((ColorHelper.brightness(egg.primaryColor) > ColorHelper.brightness(egg.secondaryColor)) == (renderPass == 0)) {
//			if (renderPass == 0) {
//				return egg.primaryColor;
//			} else
//				return egg.secondaryColor;
//		}
//	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return false;
	}

	public boolean hasAnimal(ItemStack itemStack) {
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		return hasAnimal(tagCompound);
	}

	private boolean hasAnimal(NBTTagCompound tagCompound) {
		return tagCompound != null && tagCompound.hasKey(NBT_ANIMAL, Constants.NBT.TAG_COMPOUND);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		if (hasAnimal(stack)) return false;

		if (target.world.isRemote) return true;

		if (!target.isEntityAlive()) return false;

		if (stack.getItemDamage() == 1) {
			if (!(target instanceof IMob)) {
				playerIn.sendMessage(Lang.chat("%s is not a hostile mob.", target.getDisplayName()));
				return false;
			}

			if (!playerIn.capabilities.isCreativeMode) {
				if (!target.isNonBoss()) {
					playerIn.sendMessage(Lang.chat("%s is too powerful.", target.getDisplayName()));
					return false;
				}

				NBTTagCompound data = target.getEntityData();
				if (data.getBoolean(NBT_ANIMAL_ALREADYPICKEDUP)) {
					return true;
				}

				float health = target.getHealth();
				float maxHealth = target.getMaxHealth();

				float threshold = MathHelper.clamp(maxHealth / 4, 4, 10);
				if (health > threshold) {
					playerIn.sendMessage(Lang.chat("%s has too much health (%s hearts). Reduce to %s hearts.", target.getDisplayName(), (int) Math.floor(health / 2F), (int) Math.floor(threshold / 2F)));
					return false;
				}
			}
		} else {
			if (target instanceof IMob) {
				playerIn.sendMessage(Lang.chat("%s is a hostile mob.", target.getDisplayName()));
				return false;
			}

			if (!(target instanceof EntityCreature || target instanceof EntityAmbientCreature || target instanceof EntityWaterMob)) {
				return false;
			}

			if (((EntityLiving) target).getAttackTarget() != null) {
				playerIn.sendMessage(Lang.chat("%s is too busy attacking someone.", target.getDisplayName()));
				return false;
			}
		}

		return addTargetToLasso(stack, target);
	}

	public boolean addTargetToLasso(ItemStack stack, EntityLivingBase target) {
		if (target instanceof IMerchant) {
			target.getDisplayName();
		}

		float health = target.getHealth();
		float maxHealth = target.getMaxHealth();

		NBTTagCompound entityTags = new NBTTagCompound();
		entityTags.setBoolean(NBT_GOLDEN_LASSO_PREVENT, false);

		if (!target.writeToNBTAtomically(entityTags)) {
			return false;
		}

		if (!entityTags.hasKey(NBT_GOLDEN_LASSO_PREVENT) || entityTags.getBoolean(NBT_GOLDEN_LASSO_PREVENT)) {
			return false;
		}

		entityTags.removeTag(NBT_GOLDEN_LASSO_PREVENT);

		String name = "";

		if (target.hasCustomName()) {
			name = target.getCustomNameTag();
		}

		target.setDead();

		NBTTagCompound nbt = NBTHelper.getOrInitTagCompound(stack);
		nbt.setBoolean(NBT_ANIMAL_NOPLACE, false);
		nbt.setTag(NBT_ANIMAL, entityTags);
		NBTTagCompound display = NBTHelper.getOrInitTagCompound(nbt, NBT_ANIMAL_DISPLAY);
		display.setFloat("Health", health);
		display.setFloat("MaxHealth", maxHealth);


		if (!name.equals("")) {
			stack.setStackDisplayName(name);
		}

		return true;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseBase(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!hasAnimal(stack)) return EnumActionResult.FAIL;
		NBTTagCompound stackTags = stack.getTagCompound();
		NBTTagCompound entityTags = stackTags.getCompoundTag(NBT_ANIMAL);
		if (entityTags.hasNoTags() || !entityTags.hasKey("id", Constants.NBT.TAG_STRING)) {
			stackTags.removeTag(NBT_ANIMAL);
			return EnumActionResult.FAIL;
		}

		if (worldIn.isRemote) return EnumActionResult.SUCCESS;

		if (!playerIn.canPlayerEdit(pos.offset(side), side, stack)) {
			return EnumActionResult.FAIL;
		}

		if (stack.getItemDamage() == 1 && worldIn.getDifficulty() == EnumDifficulty.PEACEFUL) {
			playerIn.sendMessage(Lang.chat("Difficulty set to peaceful."));
			return EnumActionResult.FAIL;
		}

		if (stackTags.getBoolean(NBT_ANIMAL_NOPLACE)) {
			if (playerIn.isSneaking()) {
				stackTags.removeTag(NBT_ANIMAL);
				stackTags.removeTag(NBT_ANIMAL_DISPLAY);
				stackTags.removeTag(NBT_ANIMAL_NOPLACE);
				playerIn.sendMessage(Lang.chat("Mob soul released."));
				stack.clearCustomName();
				return EnumActionResult.SUCCESS;
			} else {
				float health = stackTags.getCompoundTag(NBT_ANIMAL_DISPLAY).getFloat("Health");
				playerIn.sendMessage(Lang.chat("Unable to place mob."));
				if (health <= 1e-10) {
					playerIn.sendMessage(Lang.chat("Mob's body is dead."));
				}
				playerIn.sendMessage(Lang.chat("Sneak-right-click to release soul."));
				return EnumActionResult.SUCCESS;
			}
		}


		IBlockState iblockstate = worldIn.getBlockState(pos);

		pos = pos.offset(side);
		double d0 = 0.0D;

		if (side == EnumFacing.UP && iblockstate.getBlock() instanceof BlockFence) {
			d0 = 0.5D;
		}

		entityTags.setTag("Pos", this.newDoubleNBTList(pos.getX() + 0.5D, pos.getY() + d0, pos.getZ() + 0.5D));
		entityTags.setTag("Motion", this.newDoubleNBTList(0, 0, 0));
		entityTags.setFloat("FallDistance", 0);
		entityTags.setInteger("Dimension", worldIn.provider.getDimension());

		Entity entity = EntityList.createEntityFromNBT(entityTags, worldIn);

		if (entity != null) {
			if (entity instanceof EntityLiving && stack.hasDisplayName()) {
				entity.setCustomNameTag(stack.getDisplayName());
			}

			if (entity instanceof IMob) {
				entity.getEntityData().setBoolean(NBT_ANIMAL_ALREADYPICKEDUP, true);
			}

			worldIn.spawnEntity(entity);
		}

		stackTags.removeTag(NBT_ANIMAL);
		stackTags.removeTag(NBT_ANIMAL_DISPLAY);
		stackTags.removeTag(NBT_ANIMAL_NOPLACE);
		stack.clearCustomName();

		if (playerIn.capabilities.isCreativeMode) {
			playerIn.setHeldItem(hand, stack.copy());
		}
		playerIn.inventory.markDirty();

		return EnumActionResult.SUCCESS;
	}

	private NBTTagList newDoubleNBTList(double... numbers) {
		NBTTagList nbttaglist = new NBTTagList();

		for (double d0 : numbers) {
			nbttaglist.appendTag(new NBTTagDouble(d0));
		}

		return nbttaglist;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		NBTTagCompound itemTags = stack.getTagCompound();
		if (hasAnimal(itemTags)) {
			NBTTagCompound entityTags = itemTags.getCompoundTag(NBT_ANIMAL);
			if (entityTags.hasKey("id")) {
				if (!tooltip.isEmpty())
					tooltip.set(0, tooltip.get(0).replaceFirst(TextFormatting.ITALIC + stack.getDisplayName() + TextFormatting.RESET, this.getItemStackDisplayName(stack)));
				String id = entityTags.getString("id");

				String animal_name = CompatHelper.getName(id);
				tooltip.add(animal_name);

				if ("Villager".equals(id)) {
					if (entityTags.hasKey("Profession")) {
						int career = entityTags.getInteger("Career");
						int profession = entityTags.getInteger("Profession");

						int t = MCTimer.clientTimer >> 4;

						String s1 = null;

						switch (profession) {
							case 0:
								if (career == 0) career = (t % 4) + 1;

								if (career == 1) {
									s1 = "farmer";
								} else if (career == 2) {
									s1 = "fisherman";
								} else if (career == 3) {
									s1 = "shepherd";
								} else if (career == 4) {
									s1 = "fletcher";
								}

								break;
							case 1:
								s1 = "librarian";
								break;
							case 2:
								s1 = "cleric";
								break;
							case 3:
								if (career == 0) career = (t % 3) + 1;
								if (career == 1) {
									s1 = "armor";
								} else if (career == 2) {
									s1 = "weapon";
								} else if (career == 3) {
									s1 = "tool";
								}

								break;
							case 4:
								if (career == 0) career = (t % 2) + 1;
								if (career == 1) {
									s1 = "butcher";
								} else if (career == 2) {
									s1 = "leather";
								}
								break;
						}

						if (s1 != null) {
							tooltip.add(I18n.translateToLocal("entity.Villager." + s1));
						}
					}


					if (entityTags.getCompoundTag("ForgeData").getBoolean(ItemContract.TAG_UNDER_CONTRACT)) {
						tooltip.add(Lang.translate("*Under Contract*"));
					}
				}

				float health = itemTags.getCompoundTag(NBT_ANIMAL_DISPLAY).getFloat("Health");
				float maxHealth = itemTags.getCompoundTag(NBT_ANIMAL_DISPLAY).getFloat("MaxHealth");
				tooltip.add(Lang.translateArgs("Health: %s/%s", health, maxHealth));

				if (stack.hasDisplayName()) {
					tooltip.add(stack.getDisplayName());
				}
			}
		}
	}
//
//	@SubscribeEvent(priority = EventPriority.HIGHEST)
//	public void cursedLassoAbsorb(LivingDeathEvent event) {
//		if (!(event.getEntityLiving() instanceof IMob) || (!event.getEntityLiving().isNonBoss())) return;
//		Entity source = event.getSource().getEntity();
//		if (!(source instanceof EntityPlayerMP)) return;
//		EntityPlayerMP player = (EntityPlayerMP) source;
//		ItemStack cursedLasso = check(player.getHeldItem(EnumHand.OFF_HAND));
//
//		if (cursedLasso == null)
//			for (int i = 0; cursedLasso == null && i < player.inventory.getSizeInventory(); i++) {
//				cursedLasso = check(player.inventory.getStackInSlot(i));
//			}
//
//		if (cursedLasso == null) return;
//
//		if (!addTargetToLasso(cursedLasso, event.getEntityLiving())) return;
//
//		setNoPlace(cursedLasso);
//
////		event.setCanceled(true);
//
//		player.sendMessage(Lang.chat("%s absorbed into cursed lasso", event.getEntityLiving().getDisplayName()));
//	}

	@Override
	public void getSubItemsBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		subItems.add(new ItemStack(itemIn, 1, 1));

//		try {
//			WorldClient theWorld = Minecraft.getMinecraft().world;
//			if (theWorld != null)
//				for (Map.Entry<String, Class<? extends Entity>> entry : EntityList.stringToClassMapping.entrySet()) {
//					Entity entity = EntityList.createEntityByName(entry.getKey(), theWorld);
//					if (entity instanceof EntityLivingBase) {
//						int meta = IMob.class.isAssignableFrom(entry.getValue()) ? 1 : 0;
//						ItemStack stack = new ItemStack(itemIn, 1, meta);
//						NBTTagCompound tag = NBTHelper.getOrInitTagCompound(NBTHelper.getOrInitTagCompound(stack), NBT_ANIMAL);
//						if (!entity.writeMountToNBT(tag)) {
//							entity.setDead();
//							continue;
//						}
//						entity.setDead();
//						subItems.add(stack);
//					}
//				}
//		} catch (Throwable err) {
//			err.printStackTrace();
//		}
	}

	public ItemStack check(ItemStack stack) {
		return StackHelper.isNull(stack) || stack.getItem() != this || stack.getItemDamage() != 1 || hasAnimal(stack) ? null : stack;

	}

	@SubscribeEvent
	public void goldenLassoActivate(PlayerInteractEvent.EntityInteract event) {
		EntityPlayer entityPlayer = event.getEntityPlayer();
		ItemStack itemstack = entityPlayer.getHeldItem(event.getHand());
		if (StackHelper.isNonNull(itemstack) && itemstack.getItem() == this) {
			Entity entity = event.getTarget();

			if (entity instanceof EntityLivingBase) {
				itemstack.interactWithEntity(entityPlayer, (EntityLivingBase) entity, event.getHand());
				if (StackHelper.getStacksize(itemstack) <= 0) {
					entityPlayer.setHeldItem(event.getHand(), StackHelper.empty());
				}

				event.setCanceled(true);
			}
		}
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + stack.getItemDamage();
	}

	@Override
	public boolean itemsMatch(ItemStack slot, @Nonnull ItemStack target) {
		if (!OreDictionary.itemMatches(target, slot, false)) return false;

		boolean tAnim = hasAnimal(target);
		boolean sAnim = hasAnimal(slot);
		if (tAnim != sAnim) return false;
		if (!sAnim) return true;
		NBTTagCompound targetTags = getAnimalTags(target);
		String targetID = targetTags.getString("id");
		NBTTagCompound slotTags = getAnimalTags(slot);
		String slotID = slotTags.getString("id");
		return targetID.equals(slotID) && ICustomRecipeMatching.satisfies(targetTags, slotTags);

	}

	public NBTTagCompound getAnimalTags(ItemStack target) {
		return target.getTagCompound().getCompoundTag(NBT_ANIMAL);
	}

	@Override
	public boolean updateItemStackNBT(NBTTagCompound nbt) {
		nbt.removeTag(NBT_ANIMAL_NOPLACE_OLD);
		return super.updateItemStackNBT(nbt);
	}
}
