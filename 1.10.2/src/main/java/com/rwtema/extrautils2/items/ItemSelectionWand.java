package com.rwtema.extrautils2.items;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import com.rwtema.extrautils2.utils.PositionPool;
import com.rwtema.extrautils2.utils.helpers.SideHelper;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public abstract class ItemSelectionWand extends XUItemFlat {
	static double[][][] edgeLines;

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	static {
		float offset = 0.50097656f;

		edgeLines = new double[12][2][3];
		for (int i = 0; i < SideHelper.edges.length; i++) {
			EnumFacing[] edge = SideHelper.edges[i];

			EnumFacing a = edge[0];
			EnumFacing b = edge[1];
			EnumFacing c = edge[2];

			edgeLines[i] = new double[][]{
					{
							0.5F + (a.getFrontOffsetX() + b.getFrontOffsetX() + c.getFrontOffsetX()) * offset,
							0.5F + (a.getFrontOffsetY() + b.getFrontOffsetY() + c.getFrontOffsetY()) * offset,
							0.5F + (a.getFrontOffsetZ() + b.getFrontOffsetZ() + c.getFrontOffsetZ()) * offset,
					},
					{
							0.5F + (a.getFrontOffsetX() + b.getFrontOffsetX() - c.getFrontOffsetX()) * offset,
							0.5F + (a.getFrontOffsetY() + b.getFrontOffsetY() - c.getFrontOffsetY()) * offset,
							0.5F + (a.getFrontOffsetZ() + b.getFrontOffsetZ() - c.getFrontOffsetZ()) * offset,
					}
			};
		}
	}

	public final String texture;
	public final String name;
	public final int range;
	public final float[] col;


	public ItemSelectionWand(String texture, String name, float[] col, int range) {
		this.texture = texture;
		this.name = name;
		this.col = col;
		this.range = range;
		this.setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public List<BlockPos> getPotentialBlocks(EntityPlayer player, World world, BlockPos pos, EnumFacing side, int maxBlocks, ItemStack pickBlock1, IBlockState blockState, Block block) {
		if (pos.getY() >= 255 || pos.getY() < 0 || world.isAirBlock(pos) || (!world.isRemote  && !PlayerHelper.isPlayerReal(player)))
			return ImmutableList.of();

		if (StackHelper.isNull(pickBlock1))
			return ImmutableList.of();

		if (initialCheck(world, pos, side, pickBlock1, blockState)) return ImmutableList.of();

		int data = block.damageDropped(blockState);

		boolean grassBlock = block == Blocks.GRASS || block == Blocks.MYCELIUM;

		int numBlocks = getNumBlocks(player, maxBlocks, pickBlock1, grassBlock);

		if (numBlocks == 0) return ImmutableList.of();

		EnumSet<EnumFacing> dirs2Search = EnumSet.allOf(EnumFacing.class);

		dirs2Search.remove(side);
		dirs2Search.remove(side.getOpposite());

		boolean sneaking = player.isSneaking();
		if (ExtraUtils2.proxy.isAltSneaking(player)) {
			if (side.getFrontOffsetY() != 0) {
				EnumFacing horizontalFacing = player.getHorizontalFacing().rotateY();
				dirs2Search.remove(horizontalFacing);
				dirs2Search.remove(horizontalFacing.getOpposite());
			} else {
				dirs2Search.remove(EnumFacing.WEST);
				dirs2Search.remove(EnumFacing.EAST);
				dirs2Search.remove(EnumFacing.SOUTH);
				dirs2Search.remove(EnumFacing.NORTH);
			}
		} else if (sneaking) {
			if (side.getFrontOffsetY() != 0) {
				EnumFacing horizontalFacing = player.getHorizontalFacing();
				dirs2Search.remove(horizontalFacing);
				dirs2Search.remove(horizontalFacing.getOpposite());
			} else {
				dirs2Search.remove(EnumFacing.DOWN);
				dirs2Search.remove(EnumFacing.UP);
			}
		}

		if (dirs2Search.isEmpty()) return ImmutableList.of();

		LinkedHashSet<Vec3i> vecs = new LinkedHashSet<>();
		for (EnumFacing enumFacing : dirs2Search) {
			vecs.add(BlockPos.ORIGIN.offset(enumFacing));
		}

		for (EnumFacing enumFacing : dirs2Search) {
			for (EnumFacing otherEnumFacing : dirs2Search) {
				BlockPos offset = BlockPos.ORIGIN.offset(enumFacing).offset(otherEnumFacing);
				if (Math.abs(offset.getX()) < 2 && Math.abs(offset.getY()) < 2 && Math.abs(offset.getZ()) < 2)
					vecs.add(offset);

			}
		}


//		for (EnumFacing enumFacing : dirs2Search) {
//			for (EnumFacing enumFacing2 : dirs2Search) {
//				vecs.add(BlockPos.ORIGIN.offset(enumFacing).offset(enumFacing2));
//			}
//		}

		HashSet<IBlockState> states = new HashSet<>();
		states.add(blockState);
		for (IBlockState otherState : block.getBlockState().getValidStates()) {
			if (block.damageDropped(otherState) == data) {
				states.add(otherState);
			}
		}

		if (grassBlock) {
			states.addAll(Blocks.DIRT.getBlockState().getValidStates());
		} else if (block == Blocks.DIRT) {
			states.addAll(Blocks.GRASS.getBlockState().getValidStates());
			states.addAll(Blocks.MYCELIUM.getBlockState().getValidStates());
		}

		LinkedList<BlockPos> queue = new LinkedList<>();
		Set<BlockPos> checkedBlocks = new HashSet<>(numBlocks);

		queue.add(pos);
		checkedBlocks.add(pos);

		PositionPool pool = new PositionPool();

		BlockPos p;

		List<BlockPos> blocks = new ArrayList<>();
		while ((p = queue.poll()) != null && blocks.size() < numBlocks) {
			if (!states.contains(world.getBlockState(p)))
				continue;

			if (!checkAndAddBlocks(player, world, side, pickBlock1, block, blockState, pool, p, blocks)) continue;

			dirLoop:
			for (Vec3i offset : vecs) {
				BlockPos p2 = pool.add(p, offset);
				if (!checkedBlocks.contains(p2)) {
					checkedBlocks.add(p2);

					int d = Math.max(Math.max(
									Math.abs(p2.getX() - pos.getX()),
									Math.abs(p2.getY() - pos.getY())),
							Math.abs(p2.getZ() - pos.getZ()));

					ListIterator<BlockPos> listIterator = queue.listIterator();
					while (listIterator.hasNext()) {
						BlockPos next = listIterator.next();

						int d2 = Math.max(Math.max(
										Math.abs(next.getX() - pos.getX()),
										Math.abs(next.getY() - pos.getY())),
								Math.abs(next.getZ() - pos.getZ()));

						if (d2 >= d) {
							listIterator.add(p2);
							continue dirLoop;
						}
					}
					queue.add(p2);
				}
			}
		}

		return blocks;
	}

	protected abstract boolean initialCheck(World world, BlockPos pos, EnumFacing side, ItemStack pickBlock1, IBlockState state);

	protected abstract int getNumBlocks(EntityPlayer player, int maxBlocks, ItemStack pickBlock1, boolean grassBlock);

	protected abstract boolean checkAndAddBlocks(EntityPlayer player, World world, EnumFacing side, ItemStack pickBlock1, Block block, IBlockState state, PositionPool pool, BlockPos p, List<BlockPos> blocks);

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderLayers(@Nullable ItemStack itemStack) {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderLayerIn3D(ItemStack stack, int renderPass) {
		return renderPass == 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register(texture + "0");
		Textures.register(texture + "1");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		return texture + renderPass;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsTool() {
		return true;
	}

	public ItemStack getStack(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		Item item = Item.getItemFromBlock(block);
		if (item == StackHelper.nullItem()) return StackHelper.empty();

		return new ItemStack(item, 1, block.damageDropped(state));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void drawSelection(DrawBlockHighlightEvent event) {
		ItemStack currentItem = event.getPlayer().getHeldItemMainhand();
		RayTraceResult target = event.getTarget();
		if (StackHelper.isNull(currentItem) || currentItem.getItem() != this || target.typeOfHit != RayTraceResult.Type.BLOCK)
			return;

		event.setCanceled(true);

		BlockPos blockPos = target.getBlockPos();
		EntityPlayer player = event.getPlayer();
		IBlockState blockState = player.world.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block == Blocks.AIR) return;

		ItemStack pickBlock = getStack(player.world, blockPos);

		List<BlockPos> potentialBlocks = getPotentialBlocks(player, player.world, blockPos, target.sideHit, range, pickBlock, blockState, block);

			if (potentialBlocks.isEmpty()) return;

		LinkedHashSet<BlockPos> posSet = new LinkedHashSet<>(potentialBlocks);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.disableDepth();

		Tessellator tessellator = Tessellator.getInstance();
		IVertexBuffer worldrenderer = IVertexBuffer.getVertexBuffer(tessellator);

		float scale = 0.52f;

		float partialTicks = event.getPartialTicks();
		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

		PositionPool pool = new PositionPool();

		int prevType = -1;

		EnumFacing[][] edges1 = SideHelper.edges;

		for (int i = 0; i < edges1.length; i++) {
			EnumFacing[] edges = edges1[i];
			double[][] line = edgeLines[i];

			for (BlockPos pos : posSet) {
				int type;
				BlockPos o1 = pool.offset(pos, edges[0]);
				boolean a = posSet.contains(o1);
				boolean b = posSet.contains(pool.offset(pos, edges[1]));
				if (a && b) {
					if (posSet.contains(pool.offset(o1, edges[1]))) {
						continue;
					} else
						type = 1;
				} else if (!a && !b) {
					type = 1;
				} else {
					type = 2;
				}

				if (type != prevType) {
					if (prevType != -1) tessellator.draw();
					worldrenderer.begin(1, DefaultVertexFormats.POSITION);

					if (type == 1) {
						GlStateManager.color(col[0], col[1], col[2], 0.8F);
						GL11.glLineWidth(4.0F);
					} else {
						GlStateManager.color(col[0], col[1], col[2], 0.1F);
						GL11.glLineWidth(2.0F);
					}

					prevType = type;
				}

				for (double[] vec : line) {
					worldrenderer.pos(
							pos.getX() - d0 + vec[0],
							pos.getY() - d1 + vec[1],
							pos.getZ() - d2 + vec[2]
					).endVertex();
				}
			}
		}
		if (prevType != -1) {
			tessellator.draw();
		}

		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
}
