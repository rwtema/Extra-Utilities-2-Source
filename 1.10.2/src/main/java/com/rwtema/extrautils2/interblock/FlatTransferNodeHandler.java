package com.rwtema.extrautils2.interblock;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.MutableModel;
import com.rwtema.extrautils2.backend.model.Transforms;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.entity.chunkdata.ChunkDataModuleManager;
import com.rwtema.extrautils2.entity.chunkdata.EntityChunkData;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.GuiHandler;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.items.ItemFlatTransferNode;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.blockaccess.BlockAccessEmptyGlowing;
import com.rwtema.extrautils2.utils.client.GLStateAttributes;
import com.rwtema.extrautils2.utils.datastructures.nbt.NBTSerializer;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FlatTransferNodeHandler extends ChunkDataModuleManager<Multimap<BlockPos, FlatTransferNodeHandler.FlatTransferNode>> {

	public static final FlatTransferNodeHandler INSTANCE = new FlatTransferNodeHandler();
	public static int[] guiIdSides = new int[6];
	static Processer<IItemHandler> itemHandlerProcesser = new Processer<IItemHandler>(CapGetter.ItemHandler) {
		@Override
		public void transfer(World world, TileEntity input, TileEntity output, IItemHandler inputCap, IItemHandler outputCap, SingleStackHandlerFilter.EitherFilter filter) {
			for (int i = 0; i < inputCap.getSlots(); i++) {
				ItemStack extractItem = inputCap.extractItem(i, 1, true);
				if (!StackHelper.isNonNull(extractItem) || !filter.matches(extractItem)) {
					continue;
				}
				ItemStack insert = InventoryHelper.insert(outputCap, extractItem, true);
				if (StackHelper.isNonNull(insert)) {
					continue;
				}
				extractItem = inputCap.extractItem(i, 1, false);
				if (StackHelper.isNull(extractItem)) continue;
				insert = InventoryHelper.insert(outputCap, extractItem, false);

				if (StackHelper.isNonNull(insert)) {
					insert = inputCap.insertItem(i, insert, false);
					if (StackHelper.isNonNull(insert)) {
						BlockPos pos = input.getPos();
						InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), insert);
					}
				}
				return;
			}
		}
	};
	static Processer<IFluidHandler> fluidHandlerProcesser = new Processer<IFluidHandler>(CapGetter.FluidHandler) {
		@Override
		public void transfer(World world, TileEntity input, TileEntity output, IFluidHandler inputCap, IFluidHandler outputCap, SingleStackHandlerFilter.EitherFilter filter) {
			FluidStack drain;
			if (filter.hasFilter()) {
				drain = null;
				for (IFluidTankProperties properties : inputCap.getTankProperties()) {
					if (properties.canDrain()) {
						FluidStack contents = properties.getContents();
						if (contents != null && contents.amount > 0 && filter.matches(contents)) {
							FluidStack copy = contents.copy();
							copy.amount = 200;
							drain = inputCap.drain(copy, false);
						}
					}
				}
			} else {
				drain = inputCap.drain(200, false);
			}
			if (drain != null && drain.amount > 0) {
				int amount = outputCap.fill(drain, false);
				if (amount > 0) {
					outputCap.fill(inputCap.drain(amount, true), true);
				}
			}
		}
	};

	static {
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	public void init() {
		for (EnumFacing facing : EnumFacing.values()) {
			guiIdSides[facing.ordinal()] = GuiHandler.register("flatTransferNodes_" + facing, (int ID, EntityPlayer player, World world, int x, int y, int z) -> {
				BlockPos pos = new BlockPos(x, y, z);

				Multimap<BlockPos, FlatTransferNodeHandler.FlatTransferNode> flatTransferNodes = EntityChunkData.getChunkData(world.getChunkFromBlockCoords(pos), FlatTransferNodeHandler.this, false);

				for (FlatTransferNode flatTransferNode : flatTransferNodes.get(pos)) {
					if (flatTransferNode.side == facing && pos.equals(flatTransferNode.pos)) {
						return new DynamicContainer() {
							{
								addTitle(Lang.getItemName(flatTransferNode.getDrop()));
								addWidget(flatTransferNode.filter.newSlot(centerSlotX, 9 + 4));
								cropAndAddPlayerSlots(player.inventory);
								validate();
							}

							@Override
							public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
								return !flatTransferNode.isDead;
							}
						};
					}
				}
				return null;
			});
		}

	}

	@Override
	public Multimap<BlockPos, FlatTransferNodeHandler.FlatTransferNode> getCachedBlank() {
		return ImmutableMultimap.of();
	}

	@Override
	public Multimap<BlockPos, FlatTransferNodeHandler.FlatTransferNode> createBlank() {
		return LinkedListMultimap.create();
	}

	@Override
	public boolean onUpdate(Chunk chunk, Multimap<BlockPos, FlatTransferNode> entries) {
		if (entries.isEmpty()) return true;

		ArrayList<FlatTransferNode> nodes = new ArrayList<>(entries.values());
		Collections.shuffle(nodes);
		for (FlatTransferNode node : nodes) {
			try {
				if (!node.isDead && node.process(chunk.getWorld())) {
					node.dropItemStack(chunk.getWorld());
					EntityChunkData.markChunkDirty(chunk);
					node.isDead = true;
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				node.isDead = true;
			}
		}
		for (Iterator<FlatTransferNode> iterator = entries.values().iterator(); iterator.hasNext(); ) {
			FlatTransferNode node = iterator.next();
			if (node.isDead) {
				iterator.remove();
			}
		}
		return entries.isEmpty();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientTick(Chunk chunk, Multimap<BlockPos, FlatTransferNode> data) {
//		World world = chunk.getWorld();
//		Random rand = world.rand;
//		for (FlatTransferNode flatTransferNode : data.values()) {
//			AxisAlignedBB bounds = flatTransferNode.getBounds();
//			double x = bounds.minX + (bounds.maxX - bounds.minX) * rand.nextFloat();
//			double y = bounds.minY + (bounds.maxY - bounds.minY) * rand.nextFloat();
//			double z = bounds.minZ + (bounds.maxZ - bounds.minZ) * rand.nextFloat();
//
//			world.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 0, 0, 0);
//		}
	}

	@Override
	public void writeToNBT(NBTTagCompound base, Multimap<BlockPos, FlatTransferNodeHandler.FlatTransferNode> entries) {
		base.setTag("entries", NBTHelper.createList(entries.values(), FlatTransferNode::serializeNBT));
	}

	@Override
	public Multimap<BlockPos, FlatTransferNodeHandler.FlatTransferNode> readFromNBT(NBTTagCompound tag) {
		Multimap<BlockPos, FlatTransferNode> multimap = createBlank();
		for (FlatTransferNode node : NBTHelper.processList(tag.getTagList("entries", Constants.NBT.TAG_COMPOUND), FlatTransferNode::new)) {
			multimap.put(node.pos, node);
		}
		return multimap;
	}

	@Override
	public void writeData(Multimap<BlockPos, FlatTransferNode> data, XUPacketBuffer buffer) {
		Collection<FlatTransferNode> values = data.values();
		buffer.writeInt(values.size());
		for (FlatTransferNode node : values) {
			buffer.writeBlockPos(node.pos);
			buffer.writeByte(node.side.ordinal());
			buffer.writeByte(node.type.ordinal());
			buffer.writeBoolean(node.extract);
		}
	}

	@Override
	public void readData(Multimap<BlockPos, FlatTransferNode> data, XUPacketBuffer buffer) {
		int n = buffer.readInt();
		for (int i = 0; i < n; i++) {
			BlockPos pos = buffer.readBlockPos();
			EnumFacing facing = EnumFacing.values()[buffer.readByte()];
			FlatTransferNode.Type type = FlatTransferNode.Type.values()[buffer.readByte()];
			boolean extract = buffer.readBoolean();
			data.put(pos, new FlatTransferNode(pos, facing, type, extract));
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void render(RenderWorldLastEvent event) {
		WorldClient world = Minecraft.getMinecraft().world;
		List<EntityChunkData> entities = world.getEntities(EntityChunkData.class, t -> t != null && t.hasData(this));
		if (entities.isEmpty()) return;

		Entity entityIn = Minecraft.getMinecraft().getRenderViewEntity();
		if (entityIn == null) return;

		FlatTransferNode selectedNode = null;
		if (entityIn instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entityIn;

			boolean holding = false;
			for (ItemStack stack : new ItemStack[]{player.getHeldItemMainhand(), player.getHeldItemOffhand()}) {
				if (StackHelper.isNonNull(stack)) {
					Item item = stack.getItem();
					if (item == XU2Entries.flatTransferNode.value || (XU2Entries.wrench.isActive() && item == XU2Entries.wrench.value)) {
						holding = true;
						break;
					}
				}
			}
			if (holding) {
				selectedNode = ItemFlatTransferNode.getCurrentFlatTransferNode(player);
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		IVertexBuffer vertexbuffer = IVertexBuffer.getVertexBuffer(tessellator);


		float partialTicks = event.getPartialTicks();

		double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
		double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
		double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;

		GlStateManager.color(1, 1, 1, 1);

		GLStateAttributes attributes = GLStateAttributes.loadStates();

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		} else {
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		GlStateManager.color(1, 1, 1, 1);

		GlStateManager.depthMask(false);

		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		Minecraft.getMinecraft().entityRenderer.enableLightmap();

		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

		MutableModel model = new MutableModel(Transforms.blockTransforms);

		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		for (EntityChunkData data : entities) {
			Multimap<BlockPos, FlatTransferNode> data1 = data.getData(this);
			for (FlatTransferNode node : data1.values()) {
				IBlockAccess access;


				boolean isSelected = selectedNode == node;
				if (isSelected) {
					GlStateManager.disableDepth();
//					GlStateManager.disableLighting();
					Minecraft.getMinecraft().entityRenderer.disableLightmap();
					access = BlockAccessEmptyGlowing.INSTANCE;
				} else
					access = world;

				int i = access.getCombinedLight(node.pos, 0);
				int j = i % 65536;
				int k = i / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);


				EnumFacing texDirection = node.extract ? EnumFacing.UP : EnumFacing.DOWN;

				vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
				vertexbuffer.setTranslation(-d0, -d1, -d2);

				BoxModel boxes = new BoxModel();

				boxes.addBox(0, -1 / 1024F, 0, 1, 1 / 1024F, 1,
						node.type == FlatTransferNode.Type.ITEM ? ItemFlatTransferNode.ITEM_TEXTURE : ItemFlatTransferNode.FLUID_TEXTURE).setTextureSides(texDirection, ItemFlatTransferNode.BACK_TEXTURE);

				if (isSelected) {
					Box box = boxes.addBox(0, -1 / 1024F, 0, 1, 1 / 1024F, 1, ItemFlatTransferNode.SELECTION_TEXTURE);
					box.setInvisible(texDirection);
					box.color = 0xff00ff00;
					box = boxes.addBox(0, -1 / 1024F, 0, 1, 1 / 1024F, 1, ItemFlatTransferNode.SELECTION_TEXTURE);
					box.setInvisible(texDirection.getOpposite());
					box.color = 0xff0000ff;
				}
				boxes.rotateToSide(node.side);

				boxes.loadIntoMutable(model, null);

				dispatcher.getBlockModelRenderer().renderModel(access, model, Blocks.AIR.getDefaultState(), node.pos, CompatClientHelper.unwrap(vertexbuffer), false);

				tessellator.draw();
				vertexbuffer.setTranslation(0.0D, 0.0D, 0.0D);
				if (isSelected) {
					Minecraft.getMinecraft().entityRenderer.enableLightmap();
					GlStateManager.enableDepth();
//					GlStateManager.enableLighting();
					GlStateManager.color(1, 1, 1, 1);
				}

			}
		}

		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		GlStateManager.depthMask(true);
		GlStateManager.shadeModel(7424);
//		RenderHelper.disableStandardItemLighting();
		attributes.restore();
	}

	public static class FlatTransferNode implements INBTSerializable<NBTTagCompound> {
		static NBTSerializer<FlatTransferNode> serializer = new NBTSerializer<FlatTransferNode>(null)
				.registerBlockPos("pos", t -> t.pos, (t, e) -> t.pos = e)
				.registerEnum("side", EnumFacing.class, t -> t.side, (t, e) -> t.side = e)
				.registerEnum("type", Type.class, t -> t.type, (t, e) -> t.type = e)
				.registerNBTSerializable("filter", t -> t.filter)
				.registerBoolean("extract", t -> t.extract, (t, e) -> t.extract = e);

		public BlockPos pos;
		public EnumFacing side;
		public boolean extract;

		public SingleStackHandlerFilter.EitherFilter filter = new SingleStackHandlerFilter.EitherFilter();
		public Type type;
		public boolean isDead;
		@Nullable
		AxisAlignedBB bb;

		private FlatTransferNode() {
		}

		public FlatTransferNode(BlockPos pos, EnumFacing side, Type type, boolean extract) {
			this.pos = pos;
			this.side = side;
			this.type = type;
			this.extract = extract;
		}

		public void dropItemStack(World world) {
			InventoryHelper.dropItemStackAtPosition(
					world,
					getDrop(),
					this.pos.getX() + 0.5 * (1 + this.side.getFrontOffsetX()),
					this.pos.getY() + 0.5 * (1 + this.side.getFrontOffsetY()),
					this.pos.getZ() + 0.5 * (1 + this.side.getFrontOffsetZ())
			);
			ItemStack filterStack = filter.getStack();
			if (StackHelper.isNonNull(filterStack)) {
				InventoryHelper.dropItemStackAtPosition(
						world,
						filterStack,
						this.pos.getX() + 0.5 * (1 + this.side.getFrontOffsetX()),
						this.pos.getY() + 0.5 * (1 + this.side.getFrontOffsetY()),
						this.pos.getZ() + 0.5 * (1 + this.side.getFrontOffsetZ())
				);
			}
		}

		public AxisAlignedBB getBounds() {
			if (bb == null) {
				float x0 = 0;
				float x1 = 1;
				float y0 = 0;
				float y1 = 1;
				float z0 = 0;
				float z1 = 1;
				switch (side) {
					case DOWN:
						y1 = 0;
						break;
					case UP:
						y0 = 1;
						break;
					case NORTH:
						z1 = 0;
						break;
					case SOUTH:
						z0 = 1;
						break;
					case WEST:
						x1 = 0;
						break;
					case EAST:
						x0 = 1;
						break;
				}
				x0 += pos.getX();
				x1 += pos.getX();
				y0 += pos.getY();
				y1 += pos.getY();
				z0 += pos.getZ();
				z1 += pos.getZ();
				bb = new AxisAlignedBB(x0, y0, z0, x1, y1, z1);
			}
			return bb;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return serializer.serialize(this);
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			serializer.deserialize(this, nbt);
		}

		public boolean process(World world) {
			long totalWorldTime = world.getTotalWorldTime();

			if ((totalWorldTime % 4) != 0) return false;
			TileEntity owner = world.getTileEntity(pos);
			if (owner == null) return true;
			if ((totalWorldTime % 10) != 0) return false;
			TileEntity neighbour = world.getTileEntity(pos.offset(side));
			if (neighbour == null) return false;

			TileEntity input, output;
			EnumFacing dir;
			if (extract) {
				input = owner;
				output = neighbour;
				dir = side;
			} else {
				input = neighbour;
				output = owner;
				dir = side.getOpposite();
			}


			switch (type) {
				case ITEM:
					itemHandlerProcesser.process(world, input, output, dir, filter);
					break;
				case FLUIDS:
					fluidHandlerProcesser.process(world, input, output, dir, filter);
					break;
			}
			return false;
		}

		public ItemStack getDrop() {
			return XU2Entries.flatTransferNode.newStack(1, type.ordinal());
		}

		public enum Type {
			ITEM,
			FLUIDS
		}
	}

	public static abstract class Processer<T> {
		final CapGetter<T> capGetter;

		public Processer(CapGetter<T> capGetter) {
			this.capGetter = capGetter;
		}

		public void process(World world, TileEntity input, TileEntity output, EnumFacing direction, SingleStackHandlerFilter.EitherFilter filter) {
			if (!capGetter.hasInterface(input, direction)
					|| !capGetter.hasInterface(output, direction.getOpposite())
			) {
				return;
			}
			T inputCap = capGetter.getInterface(input, direction);
			T outputCap = capGetter.getInterface(output, direction.getOpposite());
			if (inputCap == outputCap || inputCap.equals(outputCap)) return;
			transfer(world, input, output, inputCap, outputCap, filter);
		}

		public abstract void transfer(World world, TileEntity input, TileEntity output, T inputCap, T outputCap, SingleStackHandlerFilter.EitherFilter filter);
	}
}
