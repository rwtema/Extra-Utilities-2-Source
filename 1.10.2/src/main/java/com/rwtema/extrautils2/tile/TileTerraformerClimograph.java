package com.rwtema.extrautils2.tile;

import com.google.common.collect.HashMultimap;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.blocks.BlockTerraformer;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.ItemHandlerFilterInsertion;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import com.rwtema.extrautils2.items.itemmatching.IMatcherMaker;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.energy.XUEnergyStorage;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Consumer;

public class TileTerraformerClimograph extends TilePower implements ITickable, IDynamicHandler {
	public final static HashMultimap<BlockTerraformer.Type, Pair<IMatcherMaker, Integer>> inputTypes = HashMultimap.create();

	public static final int INCREASE_MULTIPLIER = 40;
	final static int POWER_PER_OPERATION = 1000;
	public static Consumer<ItemStack> lookUpRecipes = null;

	static {
		register(BlockTerraformer.Type.COOLER, ItemRef.wrap(Blocks.ICE), 8);
		register(BlockTerraformer.Type.COOLER, ItemRef.wrap(Blocks.PACKED_ICE), 8);
		register(BlockTerraformer.Type.COOLER, ItemRef.wrap(Items.WATER_BUCKET), 1);

		register(BlockTerraformer.Type.HEATER, ItemRef.wrap(Items.LAVA_BUCKET), 4);
		register(BlockTerraformer.Type.HEATER, ItemRef.wrap(Items.BLAZE_ROD), 8);
		registerEntry(BlockTerraformer.Type.HEATER, Pair.of(ItemRef.wrap(Items.BLAZE_POWDER), 1));

		register(BlockTerraformer.Type.HUMIDIFIER, ItemRef.wrap(Items.WATER_BUCKET), 1);
		register(BlockTerraformer.Type.HUMIDIFIER, new IMatcherMaker.MatcherMakerOreDic("sugarcane"), 4);
		register(BlockTerraformer.Type.HUMIDIFIER, ItemRef.wrap(Blocks.WATERLILY), 8);

		register(BlockTerraformer.Type.DEHUMIDIFIER, new IMatcherMaker.MatcherMakerOreDic("sand"), 1);
		register(BlockTerraformer.Type.DEHUMIDIFIER, new IMatcherMaker.MatcherMakerOreDic("blockCactus"), 4);

		register(BlockTerraformer.Type.MAGIC_INFUSER, ItemRef.wrap(Items.EXPERIENCE_BOTTLE), 4);
		register(BlockTerraformer.Type.MAGIC_INFUSER, ItemRef.wrap(Items.ENCHANTED_BOOK), 16);
		register(BlockTerraformer.Type.MAGIC_INFUSER, new IMatcherMaker.MatcherMakerOreDic("gemLapis"), 1);
		register(BlockTerraformer.Type.MAGIC_INFUSER, ItemRef.wrap(XU2Entries.magical_wood.get()), 16);

		register(BlockTerraformer.Type.MAGIC_ABSORBTION, ItemRef.wrap(Items.BOOK), 2);
		register(BlockTerraformer.Type.MAGIC_ABSORBTION, new IMatcherMaker.MatcherMakerOreDic("ingotGold"), 2);

		register(BlockTerraformer.Type.DEHOSTILIFIER, ItemRef.wrap(Items.NETHER_STAR), 16);
	}

	public Boolean hasAntenna;
	public NBTSerializable.Int level = registerNBT("level", new NBTSerializable.Int());
	public XUEnergyStorage energy = registerNBT("energy", new XUEnergyStorage(POWER_PER_OPERATION * 20));
	int sprinkerActive = 0;
	private BlockTerraformer.Type type;
	ItemHandlerFilterInsertion<SingleStackHandler> handler = new ItemHandlerFilterInsertion<SingleStackHandler>(registerNBT("contents", new SingleStackHandler() {
		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			if (stack.getItem().hasContainerItem(stack)) return 1;
			return super.getStackLimit(stack);
		}

		@Override
		protected void onContentsChanged() {
			markDirty();
		}
	})) {
		@Override
		public boolean isValid(@Nonnull ItemStack stack) {
			for (Pair<IMatcherMaker, Integer> pair : inputTypes.get(getType())) {
				if (pair.getKey().matchesItemStack(stack)) {
					return true;
				}
			}
			return false;
		}
	};

	public static void register(BlockTerraformer.Type type, IMatcherMaker matcher, int amount) {
		inputTypes.put(type, Pair.of(matcher, amount));
	}

	private static void registerEntry(BlockTerraformer.Type heater, Pair<IMatcherMaker, Integer> of) {
		inputTypes.put(heater, of);
	}

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return energy;
	}

	public BlockTerraformer.Type getType() {
		if (type == null) {
			type = getBlockState().getValue(BlockTerraformer.TYPE);
		}
		return type;
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return handler;
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		hasAntenna = null;
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		packet.writeInt(sprinkerActive);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		sprinkerActive = packet.readInt();
	}

	@Override
	public void update() {
		if (world.isRemote) {
			if (sprinkerActive > 0) {
				if (hasAntennaAbove()) {
					int[] colors = getType().colors;
					if (colors == null || colors.length == 0) return;
					for (int i = 0; i < 10; i++) {
						Random rand = world.rand;
						EnumFacing facing = CollectionHelper.getRandomElementArray(EnumFacing.HORIZONTALS);
						double x = pos.getX() + 0.5 + (facing.getFrontOffsetX() * 5 + rand.nextFloat() * 2 - 1) / 16.0;
						double y = pos.getY() + 1 + (5 + rand.nextFloat() * 11.5) / 16;
						double z = pos.getZ() + 0.5 + (facing.getFrontOffsetZ() * 5 + rand.nextFloat() * 2 - 1) / 16.0;

						int color = colors[rand.nextInt(colors.length)];
						double r = ColorHelper.getRF(color);
						double g = ColorHelper.getGF(color);
						double b = ColorHelper.getBF(color);

						if (r == 0) r = 0.0001;

						world.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, r, g, b);
					}
				}
			}

			return;
		}

		if (sprinkerActive > 0) {
			sprinkerActive--;
			markForUpdate();
		}

		if (hasAntenna == null) {
			hasAntenna = hasAntennaAbove();
		}
		if (hasAntenna == Boolean.FALSE) {
			return;
		}

		ItemStack stack = handler.original.extractItem(0, 1, true);
		if (StackHelper.isNonNull(stack)) {
			int increase = 0;
			for (Pair<IMatcherMaker, Integer> pair : inputTypes.get(getType())) {
				if (pair.getKey().matchesItemStack(stack)) {
					increase = Math.max(increase, pair.getValue());
				}
			}

			if (increase > 0) {
				increase *= INCREASE_MULTIPLIER;

				if (level.value > (increase / 2)) {
					return;
				}

				if (energy.extractEnergy(POWER_PER_OPERATION, true) != POWER_PER_OPERATION) {
					return;
				}

				energy.extractEnergy(POWER_PER_OPERATION, false);

				if (stack.getItem().hasContainerItem(stack)) {
					handler.original.setStack(stack.getItem().getContainerItem(stack));
				} else {
					handler.original.extractItem(0, 1, false);
				}

				level.value += increase;
			}
		}
	}

	private boolean hasAntennaAbove() {
		return world.getBlockState(pos.up()) == XU2Entries.terraformer.value.getDefaultState().withProperty(BlockTerraformer.TYPE, BlockTerraformer.Type.ANTENNA);
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new Container(this, player);
	}

	@Override
	public float getPower() {
		return Float.NaN;
	}

	@Override
	public void onPowerChanged() {

	}

	public static class Container extends DynamicContainerTile {

		public Container(TileTerraformerClimograph tile, EntityPlayer player) {
			super(tile);
			addTitle(XU2Entries.terraformer.newStack(1, XU2Entries.terraformer.value.getDefaultState().withProperty(BlockTerraformer.TYPE, tile.getType())).getDisplayName());

			final int y = 4 + 9 + 16;

			WidgetProgressArrowBase w = new WidgetProgressArrowBase(DynamicContainer.centerX - WidgetProgressArrowBase.ARROW_WIDTH / 2, y + 1, (byte) 0);
			addWidget(w);

			addWidget(WidgetProgressArrowBase.getJEIWidget(lookUpRecipes != null ? () -> lookUpRecipes.accept(TileTerraformer.getStack(tile.getType())) : null, w));

			addWidget(new WidgetSlotItemHandler(tile.handler.getGUIVariant(), 0, w.getX() - 18 - 4, y));


			addWidget(new WidgetTextData(w.getX() + w.getW() + 6, y + 5, 60, 9, 1, 0x404040) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeInt(tile.level.value);
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					return StringHelper.format(packet.readInt()) + " TF";
				}
			});

			addWidget(new WidgetText(DynamicContainer.centerX - 30, y + 18 + 8, 60, 9, 0, 0x600000, "") {
				@Override
				@SideOnly(Side.CLIENT)
				public String getMsgClient() {
					if (!tile.hasAntennaAbove()) {
						return ChatFormatting.RED + Lang.translate("Missing antenna");
					}
					return "";
				}
			});

			addWidget(new WidgetEnergyStorage(width - 4 - 18, 4 + 9 + 4, tile.energy));

			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
