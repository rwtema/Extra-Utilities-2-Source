package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.XUItemBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;


public class PassthruModelItemBlock extends NullModel {
	private final XUItemBlock item;
	MutableModel result;
	protected ItemOverrideList overrideList = new ItemOverrideList(ImmutableList.of()) {
		@Nonnull
		@Override
		public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase entity) {
			try {
				result = item.block.recreateNewInstance(result);
				result.clear();
				item.block.addInventoryQuads(result, stack);
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting model for itemstack");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being processed");
				crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(stack.getItem()));
				crashreportcategory.addCrashSection("Item data", stack.getMetadata());
				crashreportcategory.addDetail("Item name", stack::getDisplayName);
				throw new ReportedException(crashreport);
			}
			return result;
		}
	};

	public PassthruModelItemBlock(XUItemBlock item) {
		this.item = item;
		result = item.block.createInventoryMutableModel();
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		return result.getQuads(state, side, rand);
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		return overrideList;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return item.block.getInventoryModel(null).getTex();
	}
}
