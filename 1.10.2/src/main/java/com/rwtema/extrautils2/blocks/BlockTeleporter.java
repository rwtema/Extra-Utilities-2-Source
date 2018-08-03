package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.dimensions.DimensionEntry;
import com.rwtema.extrautils2.dimensions.TeleporterBase;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.tile.TileTeleporter;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.helpers.BlockStates;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BlockTeleporter extends XUBlockStatic {
	public static final PropertyEnumSimple<Type> property_type = new PropertyEnumSimple<Type>(Type.class);
	public static final PropertyBool property_unbreakable = PropertyBool.create("unbreakable");
	public static EnumMap<Side, WeakHashMap<Entity, TeleportingEntityEntry>> teleportingEntities = new EnumMap<>(Side.class);

	public BlockTeleporter() {
		MinecraftForge.EVENT_BUS.register(EventHandler.class);
	}

	public static boolean isValid(Entity entityIn) {
		return !entityIn.isRiding() && !entityIn.isBeingRidden() && entityIn.isNonBoss() && !entityIn.isDead && entityIn instanceof EntityPlayer;
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addDropProperties(property_type).addWorldProperties(property_unbreakable).build();
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		if (blockState.getValue(property_unbreakable)) return -1;
		return super.getBlockHardness(blockState, worldIn, pos);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		worldIn.setBlockState(pos.up(), BlockStates.TORCH_UP);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	@Nonnull
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileTeleporter();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		Type value = state.getValue(property_type);
		return BoxModel.newStandardBlock(value.tex);
	}

	@Override
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
		if (isValid(entityIn)) {
			IBlockState state = worldIn.getBlockState(pos);
			if (state.getBlock() == this) {
				Type type = state.getValue(property_type);
				if (type.dimension != null || worldIn.provider.getDimension() != 0) {
					teleportingEntities.computeIfAbsent(worldIn.isRemote ? Side.CLIENT : Side.SERVER, p -> new WeakHashMap<>()).putIfAbsent(
							entityIn, new TeleportingEntityEntry(type, pos)
					);
				}
			}
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {

	}

	@Override
	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < xuBlockState.dropmeta2state.length; i++) {
			if (xuBlockState.dropmeta2state[i].getValue(property_type).debug && !ExtraUtils2.deobf) {
				continue;
			}
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	public enum Type {
		OVERWORLD(null, false),
		DEEP_DARK(XU2Entries.deep_dark, false),
		//		DREAM_WORLD(XU2Entries.dream_world),
		SPECIAL_DIM(XU2Entries.specialdim, true);

		@Nullable
		public final DimensionEntry dimension;
		public final String tex;
		public final boolean debug;

		Type(@Nullable DimensionEntry dimension, boolean debug) {
			this.dimension = dimension;
			this.debug = debug;
			tex = name().toLowerCase(Locale.ENGLISH) + "_portal";

		}

		public int getDestinationID() {
			if (dimension == null) return 0;
			else
				return dimension.id;
		}

	}

	public static class TeleportingEntityEntry {
		@Nonnull
		Type type;
		@Nonnull
		BlockPos pos;
		int time;

		public TeleportingEntityEntry(@Nonnull Type type, @Nonnull BlockPos pos) {
			this.type = type;
			this.pos = pos;
		}
	}

	public static class EventHandler {

		@SubscribeEvent
		public static void clientTick(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.START) {
				processMap(Side.CLIENT);
			}
		}

		@SubscribeEvent
		public static void worldTick(TickEvent.ServerTickEvent event) {
			if (event.phase == TickEvent.Phase.START) {
				processMap(Side.SERVER);
			}
		}

		private static void processMap(Side side) {
			WeakHashMap<Entity, TeleportingEntityEntry> map = teleportingEntities.get(side);
			if (map != null) {
				for (Iterator<Map.Entry<Entity, TeleportingEntityEntry>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
					Map.Entry<Entity, TeleportingEntityEntry> entry = iterator.next();
					Entity key = entry.getKey();
					TeleportingEntityEntry value = entry.getValue();
					World worldObj;
					if (!isValid(key) ||
							key.isDead ||
							(worldObj = key.world) == null ||
							!key.getEntityBoundingBox().grow(0.2, 0.2, 0.2).intersects(new AxisAlignedBB(value.pos))

					) {
						iterator.remove();
						continue;
					}

					IBlockState state = worldObj.getBlockState(value.pos);
					if (state.getBlock() != XU2Entries.teleporter.value || state.getValue(property_type) != value.type) {
						iterator.remove();
						continue;
					}

					if (key.timeUntilPortal > 0) continue;

					value.time++;

					if (worldObj.isRemote) continue;

					if (value.time > (80 + key.getMaxInPortalTime())) {
						key.timeUntilPortal = key.getPortalCooldown() + 80;

						DimensionEntry dimension = value.type.dimension;
						int dest = dimension != null ? dimension.id : 0;

						int curDim = worldObj.provider.getDimension();
						if (dest == curDim) {
							if (curDim != 0) {
								dest = 0;
							} else {
								iterator.remove();
								continue;
							}
						}

						if (key instanceof EntityPlayerMP) {
							MinecraftServer mcServer = ((EntityPlayerMP) key).mcServer;
							Teleporter teleporter;
							if (dest == 0) {
								teleporter = new TeleporterBase(mcServer.getWorld(0), 0, curDim);
							} else
								teleporter = dimension.createTeleporter(mcServer.getWorld(dest), dest, curDim);
							mcServer.getPlayerList().transferPlayerToDimension(((EntityPlayerMP) key), dest, teleporter);
						}

						iterator.remove();
					}
				}
			}
		}


		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void renderOverlay(TickEvent.RenderTickEvent event) {
			Minecraft mc = Minecraft.getMinecraft();
			WeakHashMap<Entity, TeleportingEntityEntry> map = teleportingEntities.get(Side.CLIENT);
			if (map == null) return;
			TeleportingEntityEntry entry = map.get(mc.player);
			if (entry == null) return;

			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			float v = Math.min((entry.time + MCTimer.renderPartialTickTime) / (80 + mc.player.getMaxInPortalTime()), 1) * 0.75F;

			GlStateManager.color(1.0F, 1.0F, 1.0F, v);
			GlStateManager.disableAlpha();

			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			TextureAtlasSprite sprite = Textures.getSprite(entry.type.tex);

			GlStateManager.pushMatrix();

			ScaledResolution scaledRes = new ScaledResolution(mc);
			double w = scaledRes.getScaledWidth();
			double h = scaledRes.getScaledHeight();

			GlStateManager.translate(w / 2, h / 2, 0);
			GlStateManager.rotate(MCTimer.renderTimer * 4, 0, 0, 1);

			Tessellator tessellator = Tessellator.getInstance();
			IVertexBuffer vertexbuffer = IVertexBuffer.getVertexBuffer(tessellator);
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);

			double s = Math.max(w, h) * 0.71;

			vertexbuffer.pos(-s, s, -90.0D)
					.tex(sprite.getMinU(), sprite.getMaxV())
					.endVertex();
			vertexbuffer.pos(s, s, -90.0D)
					.tex(sprite.getMaxU(), sprite.getMaxV())
					.endVertex();
			vertexbuffer.pos(s, -s, -90.0D)
					.tex(sprite.getMaxU(), sprite.getMinV())
					.endVertex();
			vertexbuffer.pos(-s, -s, -90.0D)
					.tex(sprite.getMinU(), sprite.getMinV())
					.endVertex();
			tessellator.draw();

			GlStateManager.popMatrix();
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();
			GlStateManager.enableAlpha();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
