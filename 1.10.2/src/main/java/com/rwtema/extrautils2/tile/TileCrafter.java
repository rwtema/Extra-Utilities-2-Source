package com.rwtema.extrautils2.tile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.backend.ModifyingBakedModel;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.CraftingHelper112;
import com.rwtema.extrautils2.compatibility.ICompatPerspectiveAwareModel;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.NullRecipe;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.*;
import com.rwtema.extrautils2.items.itemmatching.Matchers;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.MCTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;

public class TileCrafter extends TileAdvInteractor implements ITickable, IDynamicHandler, ITESRHook {
	public static final LoadingCache<ShapedOreRecipe, Integer> SOR_WIDTH = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<ShapedOreRecipe, Integer>() {
		@Override
		public Integer load(@Nonnull ShapedOreRecipe key) throws Exception {
			return ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, key, "width");
		}
	});
	public static final LoadingCache<ShapedOreRecipe, Integer> SOR_HEIGHT = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<ShapedOreRecipe, Integer>() {
		@Override
		public Integer load(@Nonnull ShapedOreRecipe key) throws Exception {
			return ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, key, "height");
		}
	});
	private final ItemStackHandler recipeSlots = registerNBT("recipe", new XUTileItemStackHandler(9, this));
	private final ItemStackHandler contents = registerNBT("contents", new XUTileItemStackHandler(27, this));

	private final SingleStackHandler ghostOutput = new SingleStackHandler();

	IRecipe curRecipe;
	Predicate<ItemStack>[] recipeMatchers = null;
	ItemStack[] genericStacks;
	XUCrafter crafter = new XUCrafter();
	IItemHandler publicOutputSlot = ConcatItemHandler.concatNonNull(contents);

	public static String errLog(IRecipe recipe) {
		if (recipe == null) return "[null recipe]";
		try {
			return "[" + recipe.getClass() + "=" +
					recipe.getRecipeOutput() +
					"]";
		} catch (Exception err) {
			return "[" + recipe.getClass() + "=ERROR_GETTING_RECIPE_OUTPUT" + "]";
		}
	}

	@Override
	protected void operate() {
		if (world.isRemote) return;
		if (curRecipe == NullRecipe.INSTANCE) return;

		if (genericStacks == null) return;

		List<IItemHandler> adjHandlers = getAdjacentHandlers();

		crafter.loadStacks(genericStacks);

		if (!curRecipe.matches(crafter, world))
			return;

		BitSet matched = new BitSet(9);

		for (int i = 0; i < genericStacks.length; i++) {
			if (StackHelper.isNull(genericStacks[i])) {
				matched.set(i);
			}
		}

		if (matched.cardinality() == 9) {
			return;
		}

		IItemHandler copy = InventoryHelper.copyHandler(contents);

		for (int i = 0; i < 9; i++) {
			if (matched.get(i)) continue;

			ItemStack take = takeItem(i, recipeMatchers[i], copy, false, crafter);
			if (StackHelper.isNonNull(take)) {
				matched.set(i);
			}
		}

		if (matched.cardinality() < 9 && !adjHandlers.isEmpty()) {
			BitSet clone = (BitSet) matched.clone();

			ArrayList<IItemHandler> adjHandlersCopy = new ArrayList<>(adjHandlers.size());
			for (IItemHandler adjHandler : adjHandlers) {
				adjHandlersCopy.add(InventoryHelper.copyHandler(adjHandler));
			}

			XUCrafter crafterCopy = new XUCrafter();
			crafterCopy.loadStacks(crafter);

			IItemHandler copy2 = InventoryHelper.copyHandler(contents);

			for (int i = 0; i < 9; i++) {
				if (clone.get(i)) continue;

				for (IItemHandler adjHandler : adjHandlersCopy) {
					ItemStack take = takeItem(i, recipeMatchers[i], adjHandler, true, crafterCopy);
					if (StackHelper.isNonNull(take)) {
						if (StackHelper.isNull(InventoryHelper.insert(copy2, take, true))) {
							take = takeItem(i, recipeMatchers[i], adjHandler, false, crafter);
							InventoryHelper.insert(copy2, take, false);
							clone.set(i);
							break;
						}
					}
				}
			}

			if (clone.cardinality() < 9)
				return;


			for (int i = 0; i < 9; i++) {
				if (matched.get(i)) continue;

				for (IItemHandler adjHandler : adjHandlers) {
					ItemStack take = takeItem(i, recipeMatchers[i], adjHandler, true, crafter);
					if (StackHelper.isNonNull(take)) {
						if (StackHelper.isNull(InventoryHelper.insert(contents, take, true))) {
							take = takeItem(i, recipeMatchers[i], adjHandler, false, crafter);
							InventoryHelper.insert(contents, take, false);
							matched.set(i);
							break;
						}
					}
				}
			}
		}

		if (matched.cardinality() < 9) {
			return;
		}

		if (!curRecipe.matches(crafter, world)) {
			return;
		}

		ItemStack output = curRecipe.getCraftingResult(crafter);

		if (StackHelper.isNull(output)) {
			return;
		}

		if (StackHelper.isNonNull(InventoryHelper.insert(copy, output, false))) {
			return;
		}

		for (ItemStack stack : curRecipe.getRemainingItems(crafter)) {
			if (StackHelper.isNonNull(stack)) {
				if (StackHelper.isNonNull(InventoryHelper.insert(copy, stack, false))) {
					return;
				}
			}
		}

		for (int i = 0; i < 9; i++) {
			if (recipeMatchers[i] == null) continue;
			takeItem(i, recipeMatchers[i], contents, false, crafter);
		}


		output = curRecipe.getCraftingResult(crafter);
		InventoryHelper.insert(contents, output, false);

		for (ItemStack stack : curRecipe.getRemainingItems(crafter)) {
			if (StackHelper.isNonNull(stack))
				InventoryHelper.insert(contents, stack, false);
		}
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		packet.writeItemStack(ghostOutput.getStack());
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		ghostOutput.setStack(packet.readItemStack());
	}

	@Override
	public boolean preOperate() {
		if (curRecipe == null) {
			crafter.loadStacks(recipeSlots);
			List<IRecipe> recipes = CraftingHelper112.getRecipeList();
			for (IRecipe recipe : recipes) {
				try {
					if (recipe.matches(crafter, world)
							&& StackHelper.isNonNull(recipe.getCraftingResult(crafter))
							) {
						curRecipe = recipe;
						break;
					}
				} catch (Exception err) {
					throw new RuntimeException("Caught exception while querying recipe " + errLog(recipe), err);
				}
			}
			if (curRecipe == null) {
				curRecipe = NullRecipe.INSTANCE;
				if (StackHelper.isNonNull(ghostOutput.getStack()))
					markForUpdate();
				ghostOutput.setStack(StackHelper.empty());
			}

			ghostOutput.setStack(curRecipe.getCraftingResult(crafter));
			markForUpdate();

			genericStacks = new ItemStack[9];
			for (int i = 0; i < genericStacks.length; i++) {
				genericStacks[i] = StackHelper.empty();
			}
			recipeMatchers = new Predicate[9];
			for (int i = 0; i < recipeMatchers.length; i++) {
				recipeMatchers[i] = StackHelper::isNull;
			}

			List<Object> input = CraftingHelper112.getRecipeInputs(curRecipe);

			if(input.isEmpty()){
				curRecipe = NullRecipe.INSTANCE;
				if (StackHelper.isNonNull(ghostOutput.getStack()))
					markForUpdate();
				ghostOutput.setStack(StackHelper.empty());
			}
//			mainLoop:
//			for (int i = 0; i < recipeSlots.getSlots(); i++) {
//				ItemStack stackInSlot = recipeSlots.getStackInSlot(i);
//				if (StackHelper.isNull(stackInSlot)) continue;
//				ItemStack itemStack = ItemHandlerHelper.copyStackWithSize(stackInSlot, 1);
//				for (Object o : input) {
//					if (o instanceof ItemStack && ItemHandlerHelper.canItemStacksStack(itemStack, (ItemStack) o)) {
//						continue mainLoop;
//					}
//				}
//
//				if (input.size() == 9) {
//					Object o = input.get(i);
//					if (o == null || (o instanceof ItemStack && StackHelper.isNull((ItemStack) o)) || (o instanceof List && ((List) o).isEmpty())) {
//						input.set(i, stackInSlot.copy());
//					}
//				} else {
//					if(input.size() < 9) {
//						input.add(itemStack);
//					}
//				}
//			}

			for (int i = 0; i < input.size(); i++) {
				genericStacks[i] = StackHelper.empty();

				Object o = input.get(i);

				o = CraftingHelper112.unwrapIngredients(o);

				if (o instanceof List) {
					List o1 = (List) o;
					if (!o1.isEmpty()) {
						lookForExistStacks:
						for (Object o2 : o1) {
							ItemStack o21 = (ItemStack) o2;
							for (int j = 0; j < recipeSlots.getSlots(); j++) {
								ItemStack a = recipeSlots.getStackInSlot(j);
								if (OreDictionary.itemMatches(a, o21, false)) {
									genericStacks[i] = a;
									break lookForExistStacks;
								}
							}
						}

						if (StackHelper.isNull(genericStacks[i]))
							genericStacks[i] = (ItemStack) o1.iterator().next();
					}
				} else if (o instanceof ItemStack) {
					genericStacks[i] = (ItemStack) o;
				}

				recipeMatchers[i] = Matchers.createMatcher(o, true);
			}

		}
		return curRecipe != NullRecipe.INSTANCE;
	}

	private List<IItemHandler> getAdjacentHandlers() {
		List<IItemHandler> adjHandlers = new ArrayList<>();

		for (EnumFacing facing : EnumFacing.values()) {
			TileEntity tile = world.getTileEntity(pos.offset(facing));
			if (tile != null) {
				IItemHandler handler = CapGetter.ItemHandler.getInterface(tile, facing.getOpposite());
				if (handler != null) {
					adjHandlers.add(handler);
				}
			}
		}
		return adjHandlers;
	}

	public ItemStack takeItem(int slot, Predicate<ItemStack> matcher, IItemHandler handler, boolean simulate, XUCrafter crafter) {
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (StackHelper.isNonNull(stack) && matcher.test(stack)) {
				ItemStack take = handler.extractItem(i, 1, simulate);
				if (StackHelper.isNonNull(take)) {
					crafter.setInventorySlotContents(slot, take);
					if (curRecipe.matches(crafter, world)
							&& StackHelper.isNonNull(curRecipe.getCraftingResult(crafter))) {
						return take;
					}
					crafter.setInventorySlotContents(slot, genericStacks[slot]);
				}
			}
		}
		return StackHelper.empty();
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return InventoryHelper.getItemHandlerIterator(contents, upgrades);
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return publicOutputSlot;
	}


	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new CrafterContainer(this, player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(IBlockAccess world, BlockPos pos, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer, BlockRendererDispatcher blockRenderer) {
		ItemStack stack = ghostOutput.getStack();
		if (StackHelper.isNull(stack)) return;
		IBakedModel duplicateModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
		IBakedModel finalModel = duplicateModel.getOverrides().handleItemState(duplicateModel, stack, this.world, null);
		if (finalModel instanceof ICompatPerspectiveAwareModel) {
			Pair<? extends IBakedModel, Matrix4f> pair = ((ICompatPerspectiveAwareModel) finalModel).handlePerspective(ItemCameraTransforms.TransformType.GROUND);
			if (pair.getLeft() != null) {
				finalModel = pair.getLeft();
			}
		}

		finalModel = ModifyingBakedModel.create(finalModel, (original, base, state1, side, rand) -> {
			float t = MCTimer.renderTimer / 64;
			float c = MathHelper.cos(t);
			float s = MathHelper.sin(t);

			ArrayList<BakedQuad> list = Lists.newArrayListWithExpectedSize(original.size());
			for (BakedQuad bakedQuad : original) {
				int[] data = Arrays.copyOf(bakedQuad.getVertexData(), 28);
				for (int i = 0; i < 28; i += 7) {
					float ax = Float.intBitsToFloat(data[i]) - 0.5F;
					float ay = Float.intBitsToFloat(data[i + 1]);
					float az = Float.intBitsToFloat(data[i + 2]) - 0.5F;

					data[i] = Float.floatToRawIntBits(0.5F + (ax * c - az * s) * 0.25F);
					data[i + 1] = Float.floatToRawIntBits(0.05F + ay * 0.25F);
					data[i + 2] = Float.floatToRawIntBits(0.5F + (ax * s + az * c) * 0.25F);
//						int col = data[i + 3];
//						int alpha = ((col & 0xff000000) >> 17) << 16;
//						data[i + 3] = alpha | (col & 0x00ffffff);
				}

				list.add(new BakedQuad(data, bakedQuad.getTintIndex(), bakedQuad.getFace(), bakedQuad.getSprite(), bakedQuad.shouldApplyDiffuseLighting(), bakedQuad.getFormat()));
			}
			return list;
		});

		blockRenderer.getBlockModelRenderer().renderModel(world, finalModel, Blocks.AIR.getDefaultState(), getPos().up(), CompatClientHelper.unwrap(renderer), false);
	}

	@Override
	public void preRender(int destroyStage) {
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.color(1, 1, 1, 0.4F);
//		GlStateManager.enableCull();
	}

	@Override
	public void postRender(int destroyStage) {
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public static class CrafterContainer extends DynamicContainerTile {
		public WidgetSlotGhost[] ghostSlots = new WidgetSlotGhost[9];

		public CrafterContainer(final TileCrafter tileCrafter, EntityPlayer player) {
			super(tileCrafter);

			tileCrafter.curRecipe = null;

			addTitle("AutoCrafter");

			crop();

			int u = height;

			int l = playerInvWidth - 4 - 18;

			int l2 = (l - (4 * 18 + 8 + WidgetProgressArrowNetworkBase.ARROW_WIDTH)) / 2;


			addWidget(new WidgetProgressArrowBase(l2 + 3 * 18 + 4, u + 18));

			addWidget(getRSWidget(l, u, tileCrafter.redstone_state, tileCrafter.pulses));
			addWidget(tileCrafter.upgrades.getSpeedUpgradeSlot(l, u + 2 * 18));

			addWidget(new WidgetSlotReadOnly(tileCrafter.ghostOutput, 0, l2 + 3 * 18 + WidgetProgressArrowNetworkBase.ARROW_WIDTH + 8, u + 18) {
				@Override
				@SideOnly(Side.CLIENT)
				public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

				}
			});

			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					int i = x + y * 3;
					addWidget(ghostSlots[i] = new WidgetSlotGhost(tileCrafter.recipeSlots, i, l2 + x * 18, u + y * 18) {
						@Override
						public void putStack(ItemStack stack) {
							tileCrafter.curRecipe = null;
							super.putStack(stack);
						}
					});
				}
			}


			crop();

			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					addWidget(new WidgetSlotItemHandler(tileCrafter.contents, x + y * 9, 4 + x * 18, height + 4 + y * 18));
				}
			}

			cropAndAddPlayerSlots(player.inventory);


			validate();
		}
	}


}
