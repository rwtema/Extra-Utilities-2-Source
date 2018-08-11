package com.rwtema.extrautils2.items;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.backend.XUItem;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.transfernodes.FacingHelper;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.blockaccess.CompatBlockAccess;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraftforge.common.BiomeDictionary.Type.*;

public class ItemSnowglobe extends XUItem {
	public static final int REQUIRED_BIOMES = 7;

	@CapabilityInject(BiomeMarker.class)
	public static Capability<BiomeMarker> markerCapability;

	static {
		CapabilityManager.INSTANCE.register(BiomeMarker.class, new Capability.IStorage<BiomeMarker>() {
			@Nullable
			@Override
			public NBTBase writeNBT(Capability<BiomeMarker> capability, BiomeMarker instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<BiomeMarker> capability, BiomeMarker instance, EnumFacing side, NBTBase nbt) {

			}
		}, () -> {
			throw new RuntimeException();
		});
	}

	public Map<BiomeDictionary.Type, String> nbt_keys;
	public Map<BiomeDictionary.Type, String> translate_keys;
	List<BiomeDictionary.Type> types = ImmutableList.of(
			END,
			NETHER,
			OCEAN,
			PLAINS,
			MOUNTAIN,
			FOREST,
			SWAMP,
			SANDY,
			SNOWY,
			MAGICAL,
			JUNGLE,
			HILLS
	);

	{
		translate_keys = types.stream().collect(Collectors.toMap(k -> k, k -> "extrautils2.text.globe.biome." + k.toString().toLowerCase(Locale.ENGLISH)));
		nbt_keys = types.stream().collect(Collectors.toMap(k -> k, k -> k.toString().toLowerCase(Locale.ENGLISH)));
		translate_keys.forEach((k, v) -> Lang.translate(v, StringHelper.capFirst(k.toString().toLowerCase(Locale.ENGLISH))));
	}

	public ItemSnowglobe() {
		setMaxStackSize(1);
		setHasSubtypes(true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register("globe_bottom", "globe_side", "globe_top");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {
		return Textures.getSprite("globe_side");
	}

	@Override
	public void getSubItemsBase(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		subItems.add(new ItemStack(itemIn, 1, 1));
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		tooltip.add(TextFormatting.AQUA + Lang.translate("An entire world contained within a small glass globe. Perhaps our universe is not so different?") + TextFormatting.GRAY);
		if (stack.getMetadata() == 1) {
			tooltip.add(Lang.translate("The snow globe has gathered enough information about our world and is now active."));
		} else {
			NBTTagCompound tagCompound = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();

			tooltip.add(Lang.translateArgs("To activate this globe, it must travel to any %s of the following biome types:", REQUIRED_BIOMES));
			translate_keys.entrySet().stream()
					.map(e -> Pair.of(e.getKey(), I18n.translateToLocal(e.getValue())))
					.sorted(Comparator.comparing(Pair::getValue))
					.forEach(e -> {
						String right = e.getRight();
						if (tagCompound.getBoolean(nbt_keys.get(e.getKey()))) {
							tooltip.add(" - "
									+ TextFormatting.DARK_BLUE.toString() + TextFormatting.STRIKETHROUGH.toString() + right + TextFormatting.RESET.toString() +
									TextFormatting.BLUE.toString() + " " + Lang.translate("(visited)") + TextFormatting.RESET.toString());
						} else {
							tooltip.add(" - " + TextFormatting.BLUE.toString() + right + TextFormatting.RESET.toString());
						}
					});
			tooltip.add(Lang.translate("You can either hold the globe in your inventory or throw it on the ground."));
		}
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		BiomeMarker marker = new BiomeMarker();
		return new ICapabilityProvider() {
			@Override
			public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
				return capability == markerCapability;
			}

			@Nullable
			@Override
			public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
				return capability == markerCapability ? markerCapability.cast(marker) : null;
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addItemColors(ItemColors itemColors, BlockColors blockColors) {
		itemColors.registerItemColorHandler((stack, tintIndex) -> tintIndex, this);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return stack.getMetadata() == 1;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		itemUpdate(worldIn, new BlockPos(entityIn), stack);
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		ItemStack itemStack = itemUpdate(entityItem.getEntityWorld(), new BlockPos(entityItem), entityItem.getItem());
		if (itemStack != null)
			entityItem.setItem(itemStack);
		return false;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	/**
	 * returns the action that specifies what animation to play when the items is being used
	 */
	@Nonnull
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.NONE;
	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		playerIn.setActiveHand(handIn);
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

	public ItemStack itemUpdate(World world, BlockPos pos, ItemStack input) {
		if (world.isRemote) {
			BiomeMarker marker = input.getCapability(markerCapability, null);
			if (marker != null) {
				marker.pos = pos;
				marker.b = world.getBiome(pos);
			}
		}

		if (input.getMetadata() == 1) return null;

		NBTTagCompound tagCompound = input.getTagCompound();
		boolean needsUpdate = false;
		if (tagCompound == null)
			tagCompound = new NBTTagCompound();

		Biome biome = world.getBiome(pos);
		for (BiomeDictionary.Type type : types) {
			if (CompatHelper.BiomeHasType(biome, type)) {
				String key = nbt_keys.get(type);
				if (!tagCompound.getBoolean(key)) {
					needsUpdate = true;
					tagCompound.setBoolean(key, true);
				}
			}
		}


		if (needsUpdate) {
			input.setTagCompound(tagCompound);
			int numBiomes = 0;
			for (String s : nbt_keys.values()) {
				if (tagCompound.getBoolean(s)) {
					numBiomes++;
					if (numBiomes >= REQUIRED_BIOMES) {
						break;
					}
				}
			}
			if (numBiomes >= REQUIRED_BIOMES) {
				input.setItemDamage(1);
				input.setTagCompound(null);
			}


			return input;
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createModel(int metadata) {
		return new PassthruModelItem(this, Transforms.blockTransforms);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderSpecificHandEvent event) {
		@Nullable
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player != null && player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == event.getHand() && StackHelper.isNonNull(event.getItemStack()) && event.getItemStack().getItem() == this ) {
			GlStateManager.pushMatrix();
			GlStateManager.enableCull();
			boolean flag1 = (event.getHand() == EnumHand.MAIN_HAND) == (player.getPrimaryHand() == EnumHandSide.RIGHT);
			int j = flag1 ? 1 : -1;
			GlStateManager.translate((float) j * -0.2785682F, 0.39344387F, 0.15731531F);
			Minecraft.getMinecraft().getItemRenderer().renderItemInFirstPerson(player, event.getPartialTicks(), event.getInterpolatedPitch(), event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress());
			GlStateManager.popMatrix();
			event.setCanceled(true);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack, World world, EntityLivingBase entity) {
		model.clear();
		BoxModel boxes = new BoxModel();
		boxes.add(new BoxDoubleSided(2, 0, 2, 14, 12, 14, false));
		boxes.setTexture("globe_side").setTextures(EnumFacing.UP, "globe_top", EnumFacing.DOWN, "globe_bottom");
//		BoxModel boxes = BoxModel.newStandardBlock("globe_side").setTextures(EnumFacing.UP, "globe_top", EnumFacing.DOWN, "globe_bottom");
		model.addBoxModel(boxes);


		Biome biome = Biomes.PLAINS;
		BiomeMarker marker = stack.getCapability(markerCapability, null);
		if (marker != null) {
			biome = marker.b;
		}
		if (biome == null) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (player != null && player.world != null) {
				biome = player.world.getBiome(new BlockPos(player));
			}
		}

		GlobeAccess blockStateAccess = new GlobeAccess(biome);

		BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();

		for (BlockPos.MutableBlockPos pos : BlockPos.MutableBlockPos.getAllInBoxMutable(GlobeAccess.CENTER, GlobeAccess.CENTER2)) {
			IBlockState state = blockStateAccess.getBlockState(pos);
			if (state.getRenderType() != EnumBlockRenderType.MODEL) {
				continue;
			}
			state = state.getActualState(blockStateAccess, pos);
			IBakedModel displayModel = blockRendererDispatcher.getModelForState(state);
			state = state.getBlock().getExtendedState(state, blockStateAccess, pos);
			long rand = MathHelper.getPositionRandom(pos);
			BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
			for (BlockRenderLayer layer : BlockRenderLayer.values()) {
				if (state.getBlock().canRenderInLayer(state, layer)) {
					ForgeHooksClient.setRenderLayer(layer);
					for (EnumFacing facing : FacingHelper.facingPlusNull) {
						if (facing == null || state.shouldSideBeRendered(blockStateAccess, pos, facing)) {
							List<BakedQuad> quads = displayModel.getQuads(state, facing, rand);

							for (BakedQuad quad : quads) {
								VertexFormat format = quad.getFormat();
								int[] vertex = Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length);
								for (int i = 0; i < 4; i++) {
									final float v = 3 / 16F;
									vertex[i * 7] = Float.floatToRawIntBits(v + (1 - v * 2) * (Float.intBitsToFloat(vertex[i * 7]) + pos.getX() - GlobeAccess.CENTER.getX()) / GlobeAccess.MAX_X);
									vertex[i * 7 + 1] = Float.floatToRawIntBits(0.01F + (1 - v * 2) * (Float.intBitsToFloat(vertex[i * 7 + 1]) + pos.getY() - GlobeAccess.CENTER.getY()) / GlobeAccess.MAX_Y);
									vertex[i * 7 + 2] = Float.floatToRawIntBits(v + (1 - v * 2) * (Float.intBitsToFloat(vertex[i * 7 + 2]) + pos.getZ() - GlobeAccess.CENTER.getZ()) / GlobeAccess.MAX_Z);
								}

								ForgeHooksClient.fillNormal(vertex, quad.getFace());

								int tint;
								if (quad.hasTintIndex()) {
									tint = blockColors.colorMultiplier(state, blockStateAccess, pos, quad.getTintIndex());
								} else {
									tint = -1;
								}

								BakedQuad bakedQuad = new BakedQuad(vertex, tint, quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), format);

								model.addQuad(bakedQuad);

							}
						}

					}
				}
			}
			ForgeHooksClient.setRenderLayer(renderLayer);

		}
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	public static class BiomeMarker {
		@Nullable
		BlockPos pos;
		@Nullable
		Biome b;
	}

	public static class GlobeAccess extends CompatBlockAccess {
		public final static BlockPos CENTER = new BlockPos(0, 128, 0);
		public static final String[][] globe_data = {
				{//0
						"gggggggggg",
						"gggggggggg",
						"gggggggggg",
						"gggggggggg",
						"gggggggggg",
						"gggggggggg",
						"gggggggggg",
						"gggggggggg",
						"gggggggggg",
				},
				{//1
						"ssssssssss",
						"ssssssssss",
						"ssWsssssss",
						"ssssssssss",
						"ssssswppws",
						"sssssd  ps",
						"sssssp  ps",
						"ssssswppws",
						"ssssssssss",
				},
				{//2
						"          ",
						"          ",
						"  W       ",
						"          ",
						"     wppw ",
						"     D  p ",
						"     p  p ",
						"     wppw ",
						"          ",
				},
				{//3
						"          ",
						" lll      ",
						" lWl      ",
						" lll      ",
						"     wppw ",
						"     p  p ",
						"     p  p ",
						"     wppw ",
						"          ",
				},
				{//4
						"          ",
						" lll      ",
						" lWl      ",
						" lll      ",
						"     swws ",
						"     wppw ",
						"     wppw ",
						"     swws ",
						"          ",
				},

				{//5
						"          ",
						" sls      ",
						" lll      ",
						" sls      ",
						"      ss  ",
						"     ssss ",
						"     ssss ",
						"      ss  ",
						"          ",
				},
				{//6
						"          ",
						"  s       ",
						" sls      ",
						"  s       ",
						"          ",
						"          ",
						"          ",
						"          ",
						"          ",
				},
				{//7
						"          ",
						"          ",
						"  s       ",
						"          ",
						"          ",
						"          ",
						"          ",
						"          ",
						"          ",
				}, {//8
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
		}, {//9
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
				"          ",
		},
		};
		public static final int MAX_X = globe_data[0].length;
		public static final int MAX_Y = globe_data.length;
		public static final int MAX_Z = globe_data[0][0].length();
		public static final BlockPos CENTER2 = CENTER.add(GlobeAccess.MAX_X - 1, GlobeAccess.MAX_Y - 1, GlobeAccess.MAX_Z - 1);
		public static List<Pair<List<BiomeDictionary.Type>, Map<Block, Function<IBlockState, IBlockState>>>> BUILDING_REPLACEMENTS = ImmutableList.of(
				Pair.of(ImmutableList.of(BiomeDictionary.Type.DRY, HOT, SANDY),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.SANDSTONE.getDefaultState())
								.put(Blocks.LOG2, s -> Blocks.SANDSTONE.getDefaultState())
								.put(Blocks.COBBLESTONE, s -> Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT))
								.put(Blocks.PLANKS, s -> Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH))
								.put(Blocks.OAK_STAIRS, s -> Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, s.getValue(BlockStairs.FACING)))
								.put(Blocks.STONE_STAIRS, s -> Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, s.getValue(BlockStairs.FACING)))
								.put(Blocks.GRASS, s -> Blocks.SAND.getDefaultState())
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.CONIFEROUS),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LOG2, s -> Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.PLANKS, s -> Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE))
								.put(Blocks.OAK_STAIRS, s -> Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, s.getValue(BlockStairs.FACING)))
								.put(Blocks.OAK_FENCE, s -> Blocks.SPRUCE_FENCE.getDefaultState())
								.put(Blocks.OAK_DOOR, s -> getEquivalent(s, Blocks.SPRUCE_DOOR.getDefaultState()))
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.SAVANNA),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LOG2, s -> Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.PLANKS, s -> Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA))
								.put(Blocks.OAK_STAIRS, s -> Blocks.ACACIA_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, s.getValue(BlockStairs.FACING)))
								.put(Blocks.OAK_FENCE, s -> Blocks.ACACIA_FENCE.getDefaultState())
								.put(Blocks.OAK_DOOR, s -> getEquivalent(s, Blocks.ACACIA_DOOR.getDefaultState()))
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.JUNGLE),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LOG2, s -> Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.PLANKS, s -> Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.JUNGLE))
								.put(Blocks.OAK_STAIRS, s -> Blocks.JUNGLE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, s.getValue(BlockStairs.FACING)))
								.put(Blocks.OAK_FENCE, s -> Blocks.JUNGLE_FENCE.getDefaultState())
								.put(Blocks.OAK_DOOR, s -> getEquivalent(s, Blocks.JUNGLE_DOOR.getDefaultState()))
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.END),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.PURPUR_PILLAR.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y))
								.put(Blocks.LOG2, s -> Blocks.PURPUR_PILLAR.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y))
								.put(Blocks.PLANKS, s -> Blocks.END_BRICKS.getDefaultState())
								.put(Blocks.OAK_STAIRS, s -> Blocks.PURPUR_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, s.getValue(BlockStairs.FACING)))
								.put(Blocks.OAK_FENCE, s -> Blocks.PURPUR_SLAB.getDefaultState())
								.put(Blocks.WATER, s -> Blocks.OBSIDIAN.getDefaultState())
								.put(Blocks.FLOWING_WATER, s -> Blocks.OBSIDIAN.getDefaultState())
								.put(Blocks.GRASS, s -> Blocks.END_STONE.getDefaultState())
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.NETHER),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.NETHER_BRICK.getDefaultState())
								.put(Blocks.LOG2, s -> Blocks.NETHER_BRICK.getDefaultState())
								.put(Blocks.PLANKS, s -> Blocks.SOUL_SAND.getDefaultState())
								.put(Blocks.OAK_STAIRS, s -> Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, s.getValue(BlockStairs.FACING)))
								.put(Blocks.OAK_FENCE, s -> Blocks.NETHER_BRICK_FENCE.getDefaultState())
								.put(Blocks.OAK_DOOR, s -> Blocks.AIR.getDefaultState())
								.put(Blocks.WATER, s -> getEquivalent(s, Blocks.LAVA.getDefaultState()))
								.put(Blocks.FLOWING_WATER, s -> getEquivalent(s, Blocks.FLOWING_LAVA.getDefaultState()))
								.put(Blocks.GRASS, s -> Blocks.NETHERRACK.getDefaultState())
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.MUSHROOM),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.GRASS, s -> Blocks.MYCELIUM.getDefaultState())
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.OCEAN),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.GRASS, s -> Blocks.WATER.getDefaultState())
								.build()
				)
		);
		public static List<Pair<List<BiomeDictionary.Type>, Map<Block, Function<IBlockState, IBlockState>>>> TREE_REPLACEMENTS = ImmutableList.of(
				Pair.of(ImmutableList.of(BiomeDictionary.Type.DRY, HOT, SANDY),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.CACTUS.getDefaultState())
								.put(Blocks.LOG2, s -> Blocks.CACTUS.getDefaultState())
								.put(Blocks.LEAVES, s -> Blocks.AIR.getDefaultState())
								.put(Blocks.LEAVES2, s -> Blocks.AIR.getDefaultState())
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.CONIFEROUS),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LOG2, s -> Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LEAVES, s -> Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE))
								.put(Blocks.LEAVES2, s -> Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE))
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.SAVANNA),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LOG2, s -> Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LEAVES, s -> Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA))
								.put(Blocks.LEAVES2, s -> Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA))
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.JUNGLE),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LOG2, s -> Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLog.LOG_AXIS, s.getValue(BlockLog.LOG_AXIS)))
								.put(Blocks.LEAVES, s -> Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE))
								.put(Blocks.LEAVES2, s -> Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE))
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.END),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.CHORUS_PLANT.getDefaultState())
								.put(Blocks.LOG2, s -> Blocks.CHORUS_PLANT.getDefaultState())
								.put(Blocks.LEAVES, s -> Blocks.AIR.getDefaultState())
								.put(Blocks.LEAVES2, s -> Blocks.AIR.getDefaultState())
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.NETHER),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.QUARTZ_BLOCK.getDefaultState())
								.put(Blocks.LOG2, s -> Blocks.QUARTZ_BLOCK.getDefaultState())
								.put(Blocks.LEAVES, s -> Blocks.QUARTZ_ORE.getDefaultState())
								.put(Blocks.LEAVES2, s -> Blocks.QUARTZ_ORE.getDefaultState())
								.build()
				),
				Pair.of(ImmutableList.of(BiomeDictionary.Type.MUSHROOM),
						ImmutableMap.<Block, Function<IBlockState, IBlockState>>builder()
								.put(Blocks.LOG, s -> Blocks.RED_MUSHROOM_BLOCK.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM))
								.put(Blocks.LOG2, s -> Blocks.RED_MUSHROOM_BLOCK.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM))
								.put(Blocks.LEAVES, s -> Blocks.RED_MUSHROOM_BLOCK.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.ALL_OUTSIDE))
								.put(Blocks.LEAVES2, s -> Blocks.RED_MUSHROOM_BLOCK.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.ALL_OUTSIDE))
								.build()
				)
		);

		static {
			for (int i = 0; i < globe_data.length; i++) {
				String[] globe_datum = globe_data[i];
				if (globe_datum.length != MAX_X) throw new RuntimeException("X[" + i + "]");
				for (int j = 0; j < globe_datum.length; j++) {
					if (globe_datum[j].length() != MAX_Z)
						throw new RuntimeException("X[" + i + "],Z[" + j + "] - " + globe_datum[j]);
				}
			}
		}

		private final Biome biome;

		public GlobeAccess(Biome biome) {
			this.biome = biome;
		}

		public static IBlockState getEquivalent(IBlockState from, IBlockState to) {
			for (IProperty property : to.getPropertyKeys()) {
				if (from.getPropertyKeys().contains(property)) {
					//noinspection unchecked
					to = to.withProperty(property, from.getValue(property));
				}
			}
			return to;
		}


		public static IBlockState getBuildingReplacement(IBlockState state, Biome biome) {
			net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID event = new net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID(biome, state);
			net.minecraftforge.common.MinecraftForge.TERRAIN_GEN_BUS.post(event);
			if (event.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
				return event.getReplacement();

			return getReplacementFromMap(state, biome, BUILDING_REPLACEMENTS);
		}

		private static IBlockState getReplacementFromMap(IBlockState state, Biome biome, List<Pair<List<BiomeDictionary.Type>, Map<Block, Function<IBlockState, IBlockState>>>> replacementsMap) {
			mainLoop:
			for (Pair<List<BiomeDictionary.Type>, Map<Block, Function<IBlockState, IBlockState>>> pair : replacementsMap) {
				for (BiomeDictionary.Type type : pair.getKey()) {
					if (!CompatHelper.BiomeHasType(biome, type))
						continue mainLoop;
				}

				Function<IBlockState, IBlockState> func = pair.getRight().get(state.getBlock());
				if (func != null) {
					return func.apply(state);
				}
			}

			return state;
		}

		@Nonnull
		@Override
		public IBlockState getBlockState(@Nonnull BlockPos pos) {
			int dx = pos.getX() - CENTER.getX();
			int dy = pos.getY() - CENTER.getY();
			int dz = pos.getZ() - CENTER.getZ();
			if (dy < 0) return Blocks.BEDROCK.getDefaultState();
			if (dy >= MAX_Y || dx < 0 || dx >= MAX_X || dz < 0 || dz >= MAX_Z)
				return Blocks.AIR.getDefaultState();
			dx = MAX_X - 1 - dx;
			dz = MAX_Z - 1 - dz;
			String[] datum = globe_data[dy];
			String s = datum[dx];
			char c = s.charAt(dz);
			switch (c) {
				case 'g':
					return getBuildingReplacement(Blocks.GRASS.getDefaultState(), biome);
				case 'w':
					return getBuildingReplacement(Blocks.LOG.getDefaultState().withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y), biome);
				case 'p':
					return getBuildingReplacement(Blocks.PLANKS.getDefaultState(), biome);
				case 's':
					return biome.getEnableSnow() || biome.canRain() ? Blocks.SNOW_LAYER.getDefaultState() : Blocks.AIR.getDefaultState();
				case 'd':
					return getBuildingReplacement(Blocks.OAK_DOOR.getDefaultState().withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER).withProperty(BlockDoor.FACING, EnumFacing.NORTH).withProperty(BlockDoor.OPEN, false), biome);
				case 'D':
					return getBuildingReplacement(Blocks.OAK_DOOR.getDefaultState().withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER).withProperty(BlockDoor.FACING, EnumFacing.NORTH).withProperty(BlockDoor.OPEN, false), biome);
				case 'W':
					return getReplacementFromMap(Blocks.LOG.getDefaultState().withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y), biome, TREE_REPLACEMENTS);
				case 'l':
					return getReplacementFromMap(Blocks.LEAVES.getDefaultState(), biome, TREE_REPLACEMENTS);
				default:
					return Blocks.AIR.getDefaultState();
			}
		}

		@Nullable
		@Override
		public TileEntity getTileEntity(@Nonnull BlockPos pos) {
			return null;
		}

		@Override
		public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
			return 15 << 20 | 15 << 4;
		}


		@Override
		public boolean isAirBlock(@Nonnull BlockPos pos) {
			IBlockState state = getBlockState(pos);
			return state.getBlock().isAir(state, this, pos);
		}

		@Nonnull
		@Override
		public Biome getBiome(@Nonnull BlockPos pos) {
			return biome;
		}

		@Override
		public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
			return 0;
		}

		@Nonnull
		@Override
		public WorldType getWorldType() {
			return WorldType.DEFAULT;
		}

		@Override
		public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
			IBlockState state = getBlockState(pos);
			return state.isSideSolid(this, pos, side);
		}
	}

}
