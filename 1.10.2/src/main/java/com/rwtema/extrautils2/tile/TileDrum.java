package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.fluids.FluidTankSerial;
import com.rwtema.extrautils2.network.SpecialChat;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class TileDrum extends XUTile {
	public static int numTicksTilDisplayEmpty = 20 * 2;
	public FluidStack prevFluid = null;
	boolean recentlyFilled = false;
	boolean recentlyDrained = false;
	public FluidTankSerial tanks = registerNBT("tank", new FluidTankSerial(getCapacity()) {
		@Override
		protected void onChange() {
			TileDrum.this.markDirty();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			boolean t2 = getFluid() == null;
			int t = super.fill(resource, doFill);

			if (doFill) {
				if (t2 && getFluid() != null)
					if (!getFluid().isFluidEqual(prevFluid)) {
						prevFluid = getFluid().copy();
						markForUpdate();
					}

				if (t != 0) {
					recentlyFilled = true;
				}
			}

			return t;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			if (resource == null || !resource.isFluidEqual(getFluid())) {
				return null;
			}

			return this.drain(resource.amount, doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			FluidStack t = super.drain(maxDrain, doDrain);

			if (doDrain && t != null) {
				if (getFluidAmount() == 0) {
					recentlyDrained = true;
					scheduleUpdate();
				}
			}
			return t;
		}
	});

	public static FluidStack getFluidFromItemStack(ItemStack stack) {
		if (StackHelper.isNull(stack)) return null;
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound == null || !tagCompound.hasKey("Fluid")) return null;
		NBTTagCompound fluidNBT = tagCompound.getCompoundTag("Fluid");
		return FluidStack.loadFluidStackFromNBT(fluidNBT);
	}

	protected abstract int getCapacity();


	public void ticked() {
		if (recentlyDrained) {
			recentlyDrained = false;

			if (recentlyFilled) {
				recentlyFilled = false;
				scheduleUpdate();
			} else {
				markDirty();
				markForUpdate();
			}
		}
	}

	public void scheduleUpdate() {
		world.scheduleBlockUpdate(getPos(), this.getBlockType(), numTicksTilDisplayEmpty, 0);
	}


	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return true;

		if (handleFluids(worldIn, playerIn, hand, heldItem, side))
			return true;

		FluidStack fluidStack = tanks.getFluid();
		if (fluidStack == null) {
			SpecialChat.sendChat(playerIn, Lang.chat("Drum: Empty"));
		} else {
			Fluid fluid = fluidStack.getFluid();
			String unlocalizedName;
			if (fluid == FluidRegistry.WATER) {
				unlocalizedName = "tile.water.name";
			} else if (fluid == FluidRegistry.LAVA) {
				unlocalizedName = "tile.lava.name";
			} else
				unlocalizedName = fluid.getUnlocalizedName(fluidStack);
			SpecialChat.sendChat(playerIn, Lang.chat("Drum: %s (%s / %s)", new TextComponentTranslation(unlocalizedName), StringHelper.format(tanks.getFluidAmount()), StringHelper.format(getCapacity())));
		}

		return true;
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		FluidStack fluid = packet.readFluidStack();
		FluidStack oldFluid = tanks.getFluid();
		if (fluid != oldFluid && (fluid == null || !fluid.isFluidEqual(oldFluid))) {
			markForUpdate();
		}
		tanks.setFluid(fluid);
	}

	@Override
	public boolean harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, XUBlock xuBlock, IBlockState state) {
		if (tanks.isEmpty())
			return false;

		ItemStack itemStack = createDropStack((XUBlockState) state);
		Block.spawnAsEntity(worldIn, pos, itemStack);
		return true;
	}

	@Override
	public Optional<ItemStack> getPickBlock(EntityPlayer player, RayTraceResult target) {
		return Optional.of(createDropStack());
	}

	public ItemStack createDropStack() {
		return createDropStack(getBlockState());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		FluidStack fluidStack = getFluidFromItemStack(stack);
		if (fluidStack != null && fluidStack.amount > getCapacity()) {
			fluidStack.amount = getCapacity();
		}
		tanks.setFluid(fluidStack);
	}

	@Override
	public boolean restrictNBTCopy() {
		return true;
	}

	public ItemStack createDropStack(XUBlockState blockState) {
		int meta = blockState.dropMeta;
		ItemStack itemStack = new ItemStack(blockState.getBlock(), 1, meta);
		NBTTagCompound nbt = tanks.serializeNBT();
		itemStack.setTagInfo("Fluid", nbt);
		return itemStack;
	}


	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		FluidStack fluid = tanks.getFluid();
		if (fluid != null) {
			fluid = fluid.copy();
			fluid.amount = 1;
		}
		packet.writeFluidStack(fluid);
	}

	@Nullable
	@Override
	public IFluidHandler getFluidHandler(EnumFacing facing) {
		return tanks;
	}


	@Override
	public boolean hasComparatorLevels() {
		return true;
	}

	public int getComparatorLevel() {
		int amount = tanks.getFluidAmount();
		if (amount == 0) return 0;
		return 1 + (amount * 14) / tanks.getCapacity();
	}

	public static class Tank16 extends TileDrum {
		@Override
		protected int getCapacity() {
			return 16 * 1000;
		}
	}

	public static class Tank256 extends TileDrum {
		@Override
		protected int getCapacity() {
			return 256 * 1000;
		}
	}

	public static class Tank4096 extends TileDrum {
		@Override
		protected int getCapacity() {
			return 4096 * 1000;
		}
	}

	public static class Tank65536 extends TileDrum {
		@Override
		protected int getCapacity() {
			return 65536 * 1000;
		}
	}

	public static class TankInf extends TileDrum {
		@Override
		protected int getCapacity() {
			return 10000 * 1000;
		}


		@Override
		public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
			if (worldIn.isRemote) return true;

			IFluidHandler container;
			if (StackHelper.isNull(heldItem) || (container = FluidUtil.getFluidHandler(heldItem)) == null) {

				FluidStack fluidStack = tanks.getFluid();
				if (fluidStack == null) {
					SpecialChat.sendChat(playerIn, Lang.chat("Drum: Empty"));
				} else {
					Fluid fluid = fluidStack.getFluid();
					String unlocalizedName;
					if (fluid == FluidRegistry.WATER) {
						unlocalizedName = "tile.water.name";
					} else if (fluid == FluidRegistry.LAVA) {
						unlocalizedName = "tile.lava.name";
					} else
						unlocalizedName = fluid.getUnlocalizedName(fluidStack);
					SpecialChat.sendChat(playerIn, Lang.chat("Drum: %s (%s / %s)", new TextComponentTranslation(unlocalizedName), StringHelper.format(getCapacity()), StringHelper.format(getCapacity())));
				}
				return true;
			}

			if (tanks.getFluidAmount() == 0) {
				if (playerIn.capabilities.isCreativeMode) {
					FluidStack fluid = container.drain(Integer.MAX_VALUE, false);
					if (fluid == null) return true;
					tanks.setFluid(fluid.copy());
					markForUpdate();
					markDirty();
				}
				return true;
			} else if (attemptFill(playerIn, hand, heldItem, container)) {
				return true;
			}

			return true;
		}

		@Nullable
		@Override
		public IFluidHandler getFluidHandler(EnumFacing facing) {
			return new IFluidHandler() {
				@Override
				public IFluidTankProperties[] getTankProperties() {
					return tanks.getTankProperties();
				}

				@Override
				public int fill(FluidStack resource, boolean doFill) {
					return 0;
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {
					if (resource == null) return null;
					return drain(resource.amount, doDrain);
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {
					FluidStack fluid = tanks.getFluid();
					if (fluid == null)
						return null;
					FluidStack copy = fluid.copy();
					copy.amount = maxDrain;
					return copy;

				}
			};
		}
	}
}
