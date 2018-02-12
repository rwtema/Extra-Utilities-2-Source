package com.rwtema.extrautils2.tile;

import com.mojang.authlib.GameProfile;
import com.rwtema.extrautils2.backend.IMetaProperty;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.commands.CommandPowerSharing;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.network.SpecialChat;
import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.IPower;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TilePower extends XUTile implements IPower {
	public final static String NBT_FREQUENCY = "Frequency";
	public final static String NBT_OWNER = "Owner";
	public final static String NBT_ACTIVE = "Active";
	public final static int FREQUENCY_CREATIVE = 1337;
	public static final IMetaProperty<Boolean> ENABLED_STATE = new IMetaProperty.WrapTile<Boolean, TilePower>(TilePower.class, PropertyBool.create("active")) {
		@Override
		public Boolean getValue(TilePower tile) {
			return tile.active;
		}
	};
	public int frequency;
	public boolean active;

	protected GameProfile owner;

	@Override
	public World world() {
		return getWorld();
	}

	@Override
	public int frequency() {
		return frequency;
	}

	@Override
	public void powerChanged(boolean powered) {
		if (active != powered) {
			active = powered;
			markDirty();
			onPowerChanged();
		}
	}

	public abstract void onPowerChanged();

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger(NBT_FREQUENCY, frequency);
		compound.setBoolean(NBT_ACTIVE, active);
		if (owner != null)
			compound.setTag(NBT_OWNER, NBTHelper.proifleToNBT(owner));
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		frequency = compound.getInteger(NBT_FREQUENCY);
		active = compound.getBoolean(NBT_ACTIVE);
		owner = NBTHelper.profileFromNBT(compound.getCompoundTag(NBT_OWNER));
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);
		if (!world.isRemote && placer instanceof EntityPlayerMP)
			frequency = Freq.getBasePlayerFreq((EntityPlayerMP) placer);
	}

	@Override
	public IWorldPowerMultiplier getMultiplier() {
		return IWorldPowerMultiplier.CONSTANT;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (!world.isRemote)
			PowerManager.instance.removePowerHandler(this);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (!world.isRemote)
			PowerManager.instance.removePowerHandler(this);
	}

	@Override
	public void onLoad() {
		if (!world.isRemote)
			PowerManager.instance.addPowerHandler(this);
	}

	public boolean isValidPlayer(EntityPlayer playerIn) {
		return PowerManager.canUse(playerIn, this);
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		packet.writeBoolean(active);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		active = packet.readBoolean();
	}

	@Nonnull
	@Override
	public String getName() {
		return getBlockState().getUnlocalizedName();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (handleFluids(worldIn, playerIn, hand, heldItem, side))
			return true;

		if (this instanceof IDynamicHandler) {
			if (!worldIn.isRemote) {
				if (PowerManager.canUse(playerIn, this))
					openGUI(playerIn);
				else {
					SpecialChat.sendChat(playerIn, Lang.chat("Access Denied. Use command /%s to try to ally with the owner.", CommandPowerSharing.COMMAND_NAME));
				}
			}
			return true;
		}
		return false;
	}

	@Nullable
	public BlockPos getLocation() {
		return getPos();
	}
}
