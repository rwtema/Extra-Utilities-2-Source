package com.rwtema.extrautils2.fakeplayer;

import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class XUFakePlayer extends FakePlayer {
	private static GameProfile XU_PROFILE = new GameProfile(UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"), "[XU2FakePlayer]");

	@Nullable
	final private GameProfile owner;
	@Nullable
	final private String type;
	private final ItemStack[] cachedHandInventory = new ItemStack[2];
	private final ItemStack[] cachedArmorArray = new ItemStack[4];

	public XUFakePlayer(WorldServer world, GameProfile owner, String type) {
		super(world, XU_PROFILE);
		this.owner = owner;
		this.type = type;
		connection = new FakeServerHandler(this);
		setSize(0, 0);
		capabilities.disableDamage = true;
	}

	public void setLocationEdge(BlockPos offset, EnumFacing side) {
		double r = 0.2;
		double x = offset.getX() + 0.5 - side.getFrontOffsetX() * r;
		double y = offset.getY() + 0.5 - side.getFrontOffsetY() * r;
		double z = offset.getZ() + 0.5 - side.getFrontOffsetZ() * r;

		int yaw;
		int pitch;

		switch (side) {
			case DOWN:
				pitch = 90;
				yaw = 0;
				break;
			case UP:
				pitch = -90;
				yaw = 0;
				break;
			case NORTH:
				yaw = 180;
				pitch = 0;
				break;
			case SOUTH:
				yaw = 0;
				pitch = 0;
				break;
			case WEST:
				yaw = 90;
				pitch = 0;
				break;
			case EAST:
				yaw = 270;
				pitch = 0;
				break;
			default:
				throw new RuntimeException("Invalid Side (" + side + ")");
		}

		setLocationAndAngles(x, y, z, yaw, pitch);
	}

	@Override
	public float getEyeHeight() {
		return 0;
	}

	public RayTraceResult trace(double blockReachDistance) {
		Vec3d vec3d = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
		Vec3d vec3d1 = getVectorForRotationPublic(this.rotationPitch, this.rotationYaw);
		Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
		return this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
	}

	@Nonnull
	public Vec3d getVectorForRotationPublic(float rotationPitch, float rotationYaw) {
		return this.getVectorForRotation(rotationPitch, rotationYaw);
	}

	public void clearInventory() {
		inventory.clear();
	}

	@Override
	public void setActiveHand(@Nonnull EnumHand hand) {

	}

	@Override
	@Nonnull
	public Vec3d getPositionEyes(float partialTicks) {
		return getPositionEyes();
	}

	@Nonnull
	public Vec3d getPositionEyes() {
		return new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		ITextComponent itextcomponent = new TextComponentString("[");
		if (type != null) {
			itextcomponent.appendText(type);
		} else {
			itextcomponent.appendText("XUFakePlayer");
		}
		if (owner != null) {
			itextcomponent.appendText(" - ");
			itextcomponent.appendText(owner.getName());
		}
		itextcomponent.appendText("]");
		return itextcomponent;
	}

	public void updateCooldown() {
		this.ticksSinceLastSwing = 20090;
	}

	public void updateAttributes() {
		for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
			ItemStack itemstack;

			switch (entityequipmentslot.getSlotType()) {
				case HAND:
					itemstack = StackHelper.safeCopy(this.cachedHandInventory[entityequipmentslot.getIndex()]);
					break;
				case ARMOR:
					itemstack = StackHelper.safeCopy(this.cachedArmorArray[entityequipmentslot.getIndex()]);
					break;
				default:
					continue;
			}

			ItemStack newStack = this.getItemStackFromSlot(entityequipmentslot);

			if (!ItemStack.areItemStacksEqual(newStack, itemstack)) {
				if (StackHelper.isNonNull(itemstack)) {
					this.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(entityequipmentslot));
				}

				if (StackHelper.isNonNull(newStack)) {
					this.getAttributeMap().applyAttributeModifiers(newStack.getAttributeModifiers(entityequipmentslot));
				}

				switch (entityequipmentslot.getSlotType()) {
					case HAND:
						this.cachedHandInventory[entityequipmentslot.getIndex()] = StackHelper.safeCopy(newStack);
						break;
					case ARMOR:
						this.cachedArmorArray[entityequipmentslot.getIndex()] = StackHelper.safeCopy(newStack);
				}
			}
		}
	}
}
