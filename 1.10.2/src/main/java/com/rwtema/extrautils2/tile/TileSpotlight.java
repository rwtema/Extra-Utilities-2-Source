package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.asm.Lighting;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.blocks.BlockSpotlight;
import com.rwtema.extrautils2.lighting.ILight;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.helpers.LightMathHelper;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;

public class TileSpotlight extends TilePower implements ILight {

	public static final int range = 78;
	public static final int range_side = 32;
	public static final int range_back = 1;
	public static final float POWER = 4;
	public static HashMap<EnumFacing, float[]> baseNormals;

	static {
		initNormals();
	}

	public float[] normal;

	private static void initNormals() {
		baseNormals = new HashMap<>();
		baseNormals.put(EnumFacing.DOWN, new float[]{0, -1, 0});
		baseNormals.put(EnumFacing.UP, new float[]{0, 1, 0});
		float sqr2 = (float) Math.sqrt(0.5);
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			float[] normal = new float[]{
					facing.getFrontOffsetX() * sqr2,
					-sqr2,
					facing.getFrontOffsetZ() * sqr2,
			};
			baseNormals.put(facing, normal);
		}
	}

	@Override
	public float getPower() {
		return POWER;
	}

	@Override
	public World getLightWorld() {
		return world;
	}

	@Override
	public float getLightOffset(BlockPos p, EnumSkyBlock type) {
		if (!active) return 0;

		if (normal == null) {
			Chunk chunk = world.getChunkFromBlockCoords(pos);
			if (chunk == null) return 0;
			normal = getBaseNormal(chunk.getBlockState(pos));
		}

		float foreDist = 1F + normal[0] * (p.getX() - pos.getX())
				+ normal[1] * (p.getY() - pos.getY())
				+ normal[2] * (p.getZ() - pos.getZ());

		if (foreDist <= 0) return 0;

		float sideDist = dist(p.getX() - pos.getX() - foreDist * normal[0],
				p.getY() - pos.getY() - foreDist * normal[1],
				p.getZ() - pos.getZ() - foreDist * normal[2]);

		float mH = 1 - LightMathHelper.approxSqrt(foreDist * foreDist + sideDist * sideDist, range * range);
		if (mH < 0) return 0;

		mH = mH * (1 - sideDist / (0.5F + foreDist) / 2F);
		if (mH < 0) return 0;
		return Math.min(mH * 16, 16);
	}

	private float dist(float a, float b, float c) {
		return MathHelper.sqrt(a * a + b * b + c * c);
	}

	public float sqr(float a) {
		return a * a;
	}

	@Override
	public EnumSkyBlock[] getLightType() {
		return new EnumSkyBlock[]{EnumSkyBlock.BLOCK};
	}

	@Override
	public void onPowerChanged() {
		markForUpdate();
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		packet.writeBoolean(active);
		if (normal != null) {
			packet.writeBoolean(true);
			for (float v : normal) {
				packet.writeFloat(v);
			}
		} else
			packet.writeBoolean(false);
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		boolean b = packet.readBoolean();

		float[] prevNormal = normal != null ? normal.clone() : null;

		if (packet.readBoolean()) {
			normal = new float[3];
			for (int i = 0; i < normal.length; i++) {
				normal[i] = packet.readFloat();
			}
		} else {
			normal = null;
		}

		if (b != active || !Arrays.equals(prevNormal, normal)) {
			BlockSpotlight.worldModelCache.remove(this);
			BlockSpotlight.renderModelCache.remove(this);
			active = b;
			updateLight();
		}
	}


	private void updateLight() {
		world.markBlockRangeForRenderUpdate(
				pos.getX() - range,
				pos.getY() - range,
				pos.getZ() - range,
				pos.getX() + range,
				pos.getY() + range,
				pos.getZ() + range);
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		writeNormal(compound);
		return compound;
	}

	protected void writeNormal(NBTTagCompound compound) {
		if (normal != null) {
			NBTTagCompound normal = NBTHelper.getOrInitTagCompound(compound, "Normal");
			normal.setFloat("NormalX", this.normal[0]);
			normal.setFloat("NormalY", this.normal[1]);
			normal.setFloat("NormalZ", this.normal[2]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readNormal(compound);
	}

	protected void readNormal(NBTTagCompound compound) {
		if (compound.hasKey("Normal", Constants.NBT.TAG_COMPOUND)) {
			normal = new float[3];
			NBTTagCompound normalTag = compound.getCompoundTag("Normal");
			normal[0] = normalTag.getFloat("NormalX");
			normal[1] = normalTag.getFloat("NormalY");
			normal[2] = normalTag.getFloat("NormalZ");
		} else {
			normal = null;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		Lighting.unregister(this, getLightMap());
		if (world.isRemote) {
			updateLight();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		Lighting.unregister(this, getLightMap());
		if (world.isRemote) {
			updateLight();
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		Lighting.register(this, getLightMap());
	}

	protected WeakHashMap<World, HashMap<EnumSkyBlock, Set<ILight>>> getLightMap() {
		return Lighting.plusLights;
	}

	public float[] getBaseNormal(IBlockState state) {
		if (normal == null) {
			if (state == null) {
				return baseNormals.get(EnumFacing.DOWN).clone();
			} else {
				EnumFacing facing = state.getValue(XUBlockStateCreator.ROTATION_ALL);
				normal = baseNormals.get(facing).clone();
			}
		}
		return normal;
	}


	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, XUBlock xuBlock) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack, xuBlock);
		normal = null;
		updateNormal(state, placer);
	}

	public void updateNormal(IBlockState state, EntityLivingBase placer) {
		EnumFacing facing = state.getValue(XUBlockStateCreator.ROTATION_ALL);
		Vec3d lookVec = placer.getLookVec();
		lookVec = lookVec.normalize();

		if (normal != null) {
			double v = lookVec.dotProduct(new Vec3d(normal[0], normal[1], normal[2]));
			if (v > 0.995) {
				lookVec = new Vec3d(-lookVec.x, -lookVec.y, -lookVec.z);
			}
		}

		double v = lookVec.dotProduct(new Vec3d(facing.getDirectionVec()));
		if (v < 0.75F) {
			normal = new float[]{(float) lookVec.x, (float) lookVec.y, (float) lookVec.z};
		} else {
			normal = TileSpotlight.baseNormals.get(facing);
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return true;
		if (isValidPlayer(playerIn)) return false;
		updateNormal(state, playerIn);
		markForUpdate();
		return true;
	}


	@Override
	public boolean canRenderBreaking() {
		return true;
	}
}
