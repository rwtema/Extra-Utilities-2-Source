package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.machine.Machine;
import com.rwtema.extrautils2.api.machine.MachineRegistry;
import com.rwtema.extrautils2.api.recipes.ICustomRecipeMatching;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BlockMachine extends XUBlock implements ICustomRecipeMatching {
	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	public static final PropertyEnumSimple<Machine.EnergyMode> TYPE = new PropertyEnumSimple<>(Machine.EnergyMode.class, "energy");
	public HashMap<IBlockState, HashMap<String, BoxModel>> modelCache = new HashMap<>();

	public BlockMachine() {

	}

	public static String getDisplayName(Machine machine) {
		if (machine == null)
			return Lang.translate("machine.blank", "Machine Block");

		return Lang.translate("machine." + machine.name, StringHelper.capFirst(new ResourceLocation(machine.name).getResourcePath()));
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this).addWorldProperties(TYPE).addWorldProperties(ACTIVE).addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_HORIZONTAL, EnumFacing.SOUTH).build();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register("extrautils2:machine/machine_base_side"
				, "extrautils2:machine/machine_base"
				, "extrautils2:machine/machine_base_bottom"
				, "extrautils2:machine/machine_err");
		for (Machine machine : MachineRegistry.getMachineValues()) {
			Textures.register(machine.frontTexture);
			Textures.register(machine.frontTextureActive);
			Textures.register(machine.textureBase, machine.textureBottom, machine.textureTop, machine.textureTopOverlay);
		}
	}

	@Override
	public void clearCaches() {
		modelCache.clear();
	}

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		String type = "blank";
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileMachine) {
			type = ((TileMachine) tileEntity).type;
		}

		IBlockState foundState = state;

		return modelCache.computeIfAbsent(foundState, t -> new HashMap<>()).computeIfAbsent(type, machine -> buildBoxModel(foundState, machine));
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		Machine.EnergyMode value = state.getValue(TYPE);
		switch (value) {
			case USES_ENERGY:
				return new TileMachineReceiver();
			case GENERATES_ENERGY:
				return new TileMachineProvider();
			default:
				throw new IllegalStateException(value + " not supported");
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void addItemColors(ItemColors itemColors, BlockColors blockColors) {
		itemColors.registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(@Nonnull ItemStack stack, int tintIndex) {
				if (tintIndex == 1) {
					String type = "blank";
					if (StackHelper.isNonNull(stack) && stack.hasTagCompound()) {
						type = Validate.notNull(stack.getTagCompound()).getString("Type");
					}
					Machine machine = MachineRegistry.getMachine(type);
					if (machine != null)
						return machine.color | 0xff000000;
				}
				return -1;
			}
		}, BlockMachine.this);


		blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
			if (tintIndex == 1 && world != null && pos != null) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileMachine) {
					Machine machine = ((TileMachine) te).machine;
					if (machine != null) {
						return machine.color | 0xff000000;
					}
				}
			}

			return -1;
		}, BlockMachine.this);
	}

	private BoxModel buildBoxModel(IBlockState state, String machineKey) {
		BoxModel model = BoxModel.newStandardBlock("extrautils2:machine/machine_base_side");
		model.setLayer(BlockRenderLayer.SOLID);
		model.setTextures(EnumFacing.UP, "extrautils2:machine/machine_base");
		model.setTextures(EnumFacing.DOWN, "extrautils2:machine/machine_base_bottom");

		if (!"blank".equals(machineKey)) {
			Machine machine = MachineRegistry.getMachine(machineKey);
			EnumFacing value = state.getValue(XUBlockStateCreator.ROTATION_HORIZONTAL).getOpposite();

			String tex;
			String tex_top;
			Box baseBox = model.get(0);
			baseBox.setTextureSides(value, "extrautils2:machine/machine_base");
			if (machine != null) {
				tex = state.getValue(ACTIVE) ? machine.frontTextureActive : machine.frontTexture;
				if (machine.textureBase != null) {
					baseBox.setTexture(machine.textureBase);
					baseBox.setTextureSides(value, machine.textureTop);
				} else {
					baseBox.setTextureSides(value, "extrautils2:machine/machine_base");
				}
				if (machine.textureTop != null) baseBox.setTextureSides(EnumFacing.UP, machine.textureTop);
				if (machine.textureBottom != null) baseBox.setTextureSides(EnumFacing.DOWN, machine.textureBottom);
				tex_top = machine.textureTopOverlay;
				baseBox.setTint(1);
			} else {
				tex = "extrautils2:machine/machine_err";
				tex_top = null;
			}

			Box box = model.addBox(0, 0, 0, 1, 1, 1);
			box.layer = BlockRenderLayer.TRANSLUCENT;
			box.setTexture(tex);
			for (EnumFacing facing : EnumFacing.values()) {
				if (facing == EnumFacing.UP && tex_top != null) {
					box.setTextureSides(EnumFacing.UP, tex_top);
					box.rotate[1] = value == EnumFacing.NORTH ? 2 : value == EnumFacing.EAST ? 1 : value == EnumFacing.WEST ? 3 : 0;

				} else if (facing != value) {
					box.setInvisible(facing);
				}
			}
		}

		return model;
	}

	@Override
	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(itemIn));
		for (Machine machine : MachineRegistry.getMachineValues()) {
			ItemStack stack = createStack(machine);
			list.add(stack);
		}
	}

	@Nonnull
	public ItemStack createStack(@Nullable Machine machine) {
		ItemStack stack = new ItemStack(this);
		if (machine != null)
			stack.setTagInfo("Type", new NBTTagString(machine.name));
		return stack;
	}

	@Nonnull
	@Override
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		String type = "blank";
		if (StackHelper.isNonNull(item) && item.hasTagCompound()) {
			type = Validate.notNull(item.getTagCompound()).getString("Type");
		}

		if (ExtraUtils2.deobf_folder)
			modelCache.clear();

		IBlockState defaultState = xuBlockState.defaultState.withProperty(XUBlockStateCreator.ROTATION_HORIZONTAL, EnumFacing.SOUTH);
		return modelCache.computeIfAbsent(defaultState, state -> new HashMap<>()).computeIfAbsent(type, machine -> buildBoxModel(defaultState, machine));
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return xuBlockState.getStateFromDropMeta(meta).withProperty(XUBlockStateCreator.ROTATION_HORIZONTAL, placer.getHorizontalFacing()).withProperty(ACTIVE, false);
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		TileEntity tileEntity = droppingTileEntity.get();
		if (tileEntity instanceof TileMachine) {
			return ImmutableList.of(createStack(((TileMachine) tileEntity).machine));
		}
		return super.getDrops(world, pos, state, fortune);
	}

	@Override
	public boolean itemsMatch(ItemStack slot, @Nonnull ItemStack target) {
		if (!OreDictionary.itemMatches(target, slot, false))
			return false;

		NBTTagCompound nbt = slot.getTagCompound();
		if (target.hasTagCompound()) {
			return ICustomRecipeMatching.satisfies(target.getTagCompound(), nbt);
		} else
			return nbt == null || !nbt.hasKey("Type");

	}

	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof TileMachine) {
			TileMachine tileMachine = (TileMachine) tileEntity;
			Machine machine = tileMachine.machine;
			if (machine != null) {
				machine.randomDisplayTick(stateIn, worldIn, pos, rand, tileMachine);
			}
		}
	}
}
