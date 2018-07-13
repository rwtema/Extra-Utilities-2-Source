package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.backend.XUItem;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Transforms;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;

public class ItemGrocket extends XUItem {
	public static ItemGrocket instance;
	private EnumMap<GrocketType, BoxModel> models;
	@SideOnly(Side.CLIENT)
	private TextureAtlasSprite sprite;

	public ItemGrocket() {
		setHasSubtypes(true);
		instance = this;
	}

	@Override
	public void getSubItemsBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < GrocketType.values().length; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseBase(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (StackHelper.isNull(stack) || StackHelper.isEmpty(stack))
			return EnumActionResult.FAIL;

		if (!playerIn.canPlayerEdit(pos, facing, stack)) return EnumActionResult.FAIL;

		if (worldIn.isRemote) return EnumActionResult.SUCCESS;

		GrocketType type = getGrocketType(stack);

		RayTraceResult rayTrace = PlayerHelper.rayTrace(playerIn);
		if (rayTrace != null) {
			if (TransferHelper.getPipe(worldIn, rayTrace.getBlockPos()) != null) {
				int subHit = rayTrace.subHit;
				if (subHit >= 0 && subHit < 6) {
					facing = EnumFacing.values()[subHit];
				}
			}
		}

		if (BlockTransferHolder.placeGrocket(playerIn, worldIn, pos, type.create(), facing) || BlockTransferHolder.placeGrocket(playerIn, worldIn, pos.offset(facing), type.create(), facing.getOpposite())) {
			SoundType soundtype = Blocks.STONE.getSoundType();
			worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
			StackHelper.decrease(stack);
			return EnumActionResult.SUCCESS;
		} else return EnumActionResult.FAIL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		sprite = null;
		GrocketType[] values = GrocketType.values();
		models = new EnumMap<>(GrocketType.class);
		for (GrocketType type : values) {
			BoxModel baseModel = type.createBaseModel();
			baseModel.moveToCenterForInventoryRendering();
			baseModel.registerTextures();
			models.put(type, baseModel);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {
		if (sprite == null) {
			for (BoxModel boxes : models.values()) {
				sprite = boxes.getTex();
				if (sprite != null) return sprite;
			}
		}
		return sprite;
	}

	public GrocketType getGrocketType(ItemStack stack) {
		GrocketType[] values = GrocketType.values();
		int i = stack.getItemDamage();
		if (i < 0 || i >= values.length) i = 0;
		return values[i];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createModel(int metadata) {
		return new PassthruModelItem(this, Transforms.blockTransforms);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack) {
		GrocketType type = getGrocketType(stack);
		BoxModel m = models.get(type);
		model.clear();
		model.isGui3D = true;
		model.tex = m.getTex();
		model.addBoxModel(m);
	}

	@Override
	public int getMaxMetadata() {
		return GrocketType.values().length;
	}

	@Override
	public boolean allowOverride() {
		return true;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + stack.getItemDamage();
	}


}
