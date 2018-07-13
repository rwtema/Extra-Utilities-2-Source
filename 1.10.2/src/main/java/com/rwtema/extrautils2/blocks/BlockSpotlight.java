package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.tile.TileSpotlight;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

public class BlockSpotlight extends XUBlock {

	public final static WeakHashMap<TileEntity, BoxModel> worldModelCache = new WeakHashMap<>();
	public final static WeakHashMap<TileEntity, BoxModel> renderModelCache = new WeakHashMap<>(1);

	BoxModel invCache;

	public BlockSpotlight() {
		super(Material.ROCK);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator(this, XUBlockStateCreator.ROTATION_ALL);
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return xuBlockState.getStateFromDropMeta(meta).withProperty(XUBlockStateCreator.ROTATION_ALL, facing);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		super.registerTextures();
		Textures.register("spotlight", "spotlight_stand", "gradient_64");
	}

	public BoxModel getStandModel(EnumFacing facing) {
		BoxModel model;
		model = new BoxModel();
		model.addBoxI(2, 0, 2, 14, 1, 14, "spotlight_stand");
		Box stand = model.addBox(0.46875F, 1 / 16F, 0.46875F, 0.53125F, 0.5F, 0.53125F);
		model.setTexture("spotlight_stand");
		stand.setTextureBounds(new float[][]{
				{15, 15, 16, 16},
				{15, 15, 16, 16},
				{15, 8, 16, 16},
				{15, 8, 16, 16},
				{15, 8, 16, 16},
				{15, 8, 16, 16},
		});
		model.rotateToSide(facing.getOpposite());
		return model;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public BoxModel getRenderModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileSpotlight) {
			BoxModel model = renderModelCache.get(tileEntity);
			if (model != null) return model;
			model = new BoxModel();
			model.addAll(super.getRenderModel(world, pos, state));

			TileSpotlight spotlight = (TileSpotlight) tileEntity;

			BoxRotatable lamp = (BoxRotatable) model.get(model.size() - 1);
			float[] normal = spotlight.getBaseNormal(state).clone();

			if (tileEntity.getWorld().isRemote && spotlight.active) {
				UV[] faceVec = lamp.faceVecs[5];
				float d = -0.1F, d2 = 4;
				Vector3f v = new Vector3f(d * normal[0] + 0.5F, d * normal[1] + 0.5F, d * normal[2] + 0.5F);
				for (int i = 0; i < 4; i++) {
					UV a = faceVec[i];
					UV b = faceVec[(i + 1) % 4];
					BoxSingleQuad glow = new BoxSingleQuad(
							new UV(a.x, a.y, a.z, 1),
							new UV(b.x, b.y, b.z, 2),
							new UV(v.x + (b.x - v.x) * d2, v.y + (b.y - v.y) * d2, v.z + (b.z - v.z) * d2, 3),
							new UV(v.x + (a.x - v.x) * d2, v.y + (a.y - v.y) * d2, v.z + (a.z - v.z) * d2, 0));
					glow.setTexture("gradient_64");
					glow.noCollide = true;
					glow.setDoubleSided(true);
					glow.addShading = false;
					glow.layer = BlockRenderLayer.TRANSLUCENT;
					model.add(glow);
				}
			}
			renderModelCache.put(tileEntity, model);
			return model;
		}
		return invCache;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FULL_BLOCK_AABB;
	}


	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileSpotlight) {
			BoxModel model = worldModelCache.get(tileEntity);
			if (model != null) return model;
			if (state == null) state = world.getBlockState(pos);

			TileSpotlight spotlight = (TileSpotlight) tileEntity;

			model = new BoxModel();
			model.addAll(getStandModel(state.getValue(XUBlockStateCreator.ROTATION_ALL)));
			BoxRotatable lamp = new BoxRotatable(3 / 16F, 5 / 16F, 5 / 16F, 13 / 16F, 11 / 16F, 11 / 16F);
			lamp.setTexture("spotlight");
			lamp.setTextureBounds(new float[][]{
							{10, 10, 0, 16},
							{0, 10, 10, 16},
							{0, 10, 10, 16},
							{10, 10, 0, 16},
							{10, 10, 16, 16},
//							{10, 6, 16, 0},
							{spotlight.active ? 10 : 0, 0, spotlight.active ? 16 : 6, 6},
					}
			);

			float[] normal = spotlight.getBaseNormal(state).clone();
			float x = normal[0];
			float y = normal[1];
			float z = normal[2];
			lamp.rotate(MathHelper.sqrt(1 - y * y), y, 0.5F, 0.5F, 0.5F, 0, 0, 1);
			lamp.rotate(x, -z, 0.5F, 0.5F, 0.5F, 0, 1, 0);
			model.add(lamp);

			worldModelCache.put(tileEntity, model);
			return model;
		}
		return getInventoryModel(null);
	}

	@Override
	public void clearCaches() {
		invCache = null;
		worldModelCache.clear();
		renderModelCache.clear();
	}

	@Nonnull
	@Override
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		BoxModel inv = this.invCache;
		if (inv != null) return this.invCache;
		inv = new BoxModel();
		inv.addAll(getStandModel(EnumFacing.UP));
		inv.addBoxI(3, 5, 5, 13, 11, 11, "spotlight").textureBounds = new float[][]{
				{10, 0, 0, 6},
				{0, 0, 10, 6},
				{10, 0, 0, 6},
				{0, 0, 10, 6},
				{10, 0, 16, 6},
				{0, 10, 6, 16},
		};

		this.invCache = inv;
		return inv;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(Lang.translateArgs("Power Required: %s GP", TileSpotlight.POWER));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileSpotlight();
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
//		TileSpotlight tile = (TileSpotlight) getTile(worldIn, pos);
//		float[] normal = tile.getNormal();
//		for (int i = 0; i < 100; i++) {
//			float r = rand.nextFloat() * TileSpotlight.range;
//
//			worldIn.spawnParticle(EnumParticleTypes.REDSTONE,
//					pos.getX() + 0.5 + normal[0] * r,
//					pos.getY() + 0.5 + normal[1] * r,
//					pos.getZ() + 0.5 + normal[2] * r, 0.0D, 0.0D, 0.0D);
//		}
//
//
//		EntityPlayerSP player = Minecraft.getMinecraft().player;
//		double dx = player.posX - pos.getX() - 0.5;
//		double dy = player.posY - pos.getY() - 0.5;
//		double dz = player.posZ - pos.getZ() - 0.5;
//		double t = dx * normal[0]
//				+ dy * normal[1]
//				+ dz * normal[2];
//
//
//		for (int i = 0; i < 100; i++) {
//			float d = rand.nextFloat();
//			worldIn.spawnParticle(EnumParticleTypes.REDSTONE,
//					player.posX + d * (pos.getX() + 0.5 + normal[0] * t - player.posX),
//					player.posY + d * (pos.getY() + 0.5 + normal[1] * t - player.posY),
//					player.posZ + d * (pos.getZ() + 0.5 + normal[2] * t - player.posZ),
//					0, 0, 0);
//		}
	}
}
