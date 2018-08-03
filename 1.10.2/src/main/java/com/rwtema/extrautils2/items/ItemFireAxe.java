package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.PositionPool;
import com.rwtema.extrautils2.utils.helpers.WorldHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedList;

public class ItemFireAxe extends ItemAxe implements IXUItem {
	static final int MAX_DESTROY = 2048;
	static final Vec3i[] ORDERED_SIDES = new Vec3i[]{
			// UP-DOWN
			new Vec3i(0, 1, 0),
			new Vec3i(0, -1, 0),

			// SIDES
			new Vec3i(0, 0, 1),
			new Vec3i(0, 0, -1),
			new Vec3i(1, 0, 0),
			new Vec3i(-1, 0, 0),

			// UP-SIDES
			new Vec3i(0, 1, 1),
			new Vec3i(0, 1, -1),
			new Vec3i(1, 1, 0),
			new Vec3i(-1, 1, 0),

			// DOWN-SIDES
			new Vec3i(0, 1, 1),
			new Vec3i(0, 1, -1),
			new Vec3i(1, 1, 0),
			new Vec3i(-1, 1, 0),

			// CORNERS
			new Vec3i(1, 0, 1),
			new Vec3i(1, 0, -1),
			new Vec3i(-1, 0, 1),
			new Vec3i(-1, 0, -1),

			// UP-CORNERS
			new Vec3i(1, 1, 1),
			new Vec3i(1, 1, -1),
			new Vec3i(-1, 1, 1),
			new Vec3i(-1, 1, -1),

			// DOWN-CORNERS
			new Vec3i(1, 0, 1),
			new Vec3i(1, 0, -1),
			new Vec3i(-1, 0, 1),
			new Vec3i(-1, 0, -1),
	};
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite sprite;

	public ItemFireAxe() {
		super(ItemLawSword.material, 10, -3);
		setMaxStackSize(1);
		this.setMaxDamage(0);
		setUnlocalizedName(ExtraUtils2.MODID + ":fireaxe");
		MinecraftForge.EVENT_BUS.register(new ItemLawSword.OPAnvilHandler(this));
	}

//	@Override
//	@SideOnly(Side.CLIENT)
//	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
//		super.addInformation(stack, playerIn, tooltip, advanced);
//		tooltip.add(Lang.translate("\"Truly, a weapon for madmen. Who else would attack fire with a blade?\""));
//	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register("fire_axe");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createModel(int metadata) {
		return new PassthruModelItem(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {
		return sprite;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack, World world, EntityLivingBase entity) {
		model.addSprite(sprite);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void postTextureRegister() {
		sprite = Textures.getSprite("fire_axe");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsTool() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clearCaches() {
		sprite = null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean allowOverride() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		if (state.getMaterial() == Material.LEAVES) {
			return efficiencyOnProperMaterial;
		}
		return super.getStrVsBlock(stack, state);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, @Nonnull EntityLivingBase attacker) {
//		if (target != null && !target.isImmuneToFire()) {
//			target.setFire(5);
//		}
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		if (player == null || player.world.isRemote) {
			return false;
		}
		World world = player.world;
		IBlockState initState = world.getBlockState(pos);
		if (initState.getBlock().isWood(world, pos)) {

			PositionPool positionPool = new PositionPool();
			pos = positionPool.intern(pos);
			BlockPos topPoint = pos;

			HashSet<BlockPos> checkedPositions = new HashSet<>();
			HashSet<BlockPos> toDestroy = new HashSet<>();
			LinkedList<BlockPos> toCheck = new LinkedList<>();

			IBlockState blockState;
			do {
				checkedPositions.add(topPoint);
				for (EnumFacing horizontalSide : EnumFacing.HORIZONTALS) {
					toCheck.add(positionPool.offset(topPoint, horizontalSide));
				}
				toDestroy.add(topPoint);
				topPoint = positionPool.offset(topPoint, EnumFacing.UP);
				blockState = world.getBlockState(topPoint);
			} while (blockState.getBlock().isWood(world, topPoint));

			if (!blockState.getBlock().isLeaves(blockState, world, topPoint)) {
				return false;
			}

			BlockPos nextPos;
			while (toDestroy.size() < MAX_DESTROY && (nextPos = toCheck.poll()) != null) {
				for (Vec3i orderedSide : ORDERED_SIDES) {
					BlockPos offsetPos = positionPool.add(nextPos, orderedSide);
					if (checkedPositions.add(offsetPos)) {
						IBlockState state = world.getBlockState(offsetPos);
						if (state.getBlock().isWood(world, offsetPos)) {
							toDestroy.add(offsetPos);
							toCheck.add(offsetPos);
						} else if (state.getBlock().isLeaves(state, world, offsetPos)) {
							world.scheduleBlockUpdate(nextPos, blockState.getBlock(), world.rand.nextInt(40), 0);
						}
					}
				}
			}

			for (BlockPos blockPos : toDestroy) {
				if (!player.capabilities.isCreativeMode) {
					IBlockState state = world.getBlockState(blockPos);
					state.getBlock().harvestBlock(world, player, blockPos, state, world.getTileEntity(blockPos), itemstack);
				}

				world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
				WorldHelper.markBlockForUpdate(world, blockPos);
			}

			return false;
		}
//		else if (initState.getBlock().isLeaves(initState, world, pos)) {
//				PositionPool positionPool = new PositionPool();
//				pos = positionPool.intern(pos);
//				BlockPos topPoint = pos;
//
//				HashSet<BlockPos> checkedPositions = new HashSet<>();
//
//				LinkedList<BlockPos> toCheck = new LinkedList<>();
//				toCheck.add(pos);
//				checkedPositions.add(pos);
//				BlockPos nextPos;
//				while (checkedPositions.size() < 512 && (nextPos = toCheck.poll()) != null) {
//					IBlockState blockState = world.getBlockState(nextPos);
//					if (blockState.getBlock().isLeaves(blockState, world, nextPos)) {
//						world.scheduleBlockUpdate(nextPos, blockState.getBlock(), world.rand.nextInt(40), 0);
//
//						for (Vec3i side : ORDERED_SIDES) {
//							BlockPos newPos = positionPool.add(nextPos, side);
//							if (checkedPositions.add(newPos)) {
//								toCheck.add(newPos);
//							}
//						}
//					}
//				}
//
//				return false;
//		}
		else {
			return false;
		}
	}
}
