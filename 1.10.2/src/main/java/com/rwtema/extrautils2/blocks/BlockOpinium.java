package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.XUBlockTESR;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.BoxQuadListDeferred;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.items.itemmatching.IMatcherMaker;
import com.rwtema.extrautils2.transfernodes.FacingHelper;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class BlockOpinium extends XUBlockTESR {


	public final static int NUM_TIERS;
	final static IMatcherMaker[] tierList;
	final static int NUMBER_ORBITS = 4;
	final static int NUMBER_SUBORBITS = 0;
	final static float ORBIT_REDUCTION = 0.2F * 1.25F;
	final static float CORE_REDUCTION = 0.3f * 1.25F;
	final static float ORBIT_DISTANCE = 0.35f * 1.25F;
	final static float SUB_ORBIT_DISTANCE = 0.2f * 1.25F;
	final static float SUB_ORBIT_REDUCTION = 0.05f * 1.25F;

	static {
		tierList = new IMatcherMaker[]{
				ItemRef.wrap(ItemIngredients.Type.RED_COAL.newStack()),
				new IMatcherMaker.MatcherMakerOreDic("blockIron"),
				new IMatcherMaker.MatcherMakerOreDic("blockGold"),
				new IMatcherMaker.MatcherMakerOreDic("blockDiamond"),
				new IMatcherMaker.MatcherMakerOreDic("blockEmerald"),
				ItemRef.wrap(Blocks.CHORUS_FLOWER),
				ItemRef.wrap(Items.EXPERIENCE_BOTTLE),
				ItemRef.wrap(Items.ELYTRA),
				ItemRef.wrap(Items.NETHER_STAR),
				new IMatcherMaker.MatcherMakerOreDic("ingotIron"),
		};

		NUM_TIERS = tierList.length - 1;
	}

	public static void addRecipes() {
		CraftingHelper.addShaped("opinium_0", new ItemStack(XU2Entries.openium.value, 1, 0), " o ", "oio", " o ", 'o', tierList[0].getCraftingObject(), 'i', tierList[1].getCraftingObject());
		for (int i = 1; i < NUM_TIERS; i++) {
			Object outside = new ItemStack(XU2Entries.openium.value, 1, i - 1);
			Object middle = tierList[i].getCraftingObject();
			Object inside = tierList[i + 1].getCraftingObject();
			CraftingHelper.addShaped("opinium_" + i, new ItemStack(XU2Entries.openium.value, 1, i), " o ", "mim", " o ", 'o', outside, 'i', inside, 'm', middle);
		}
	}

	@Override
	public BoxModel getWorldModel(@Nullable ItemStack stack, IBlockState state, float timer) {
		return new BoxModel(0, 0, 0, 1, 1, 1);
	}

	@Override
	public String getSuffix(ItemStack stack) {
		return String.valueOf(stack.getMetadata());
	}

	@Override
	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < NUM_TIERS; i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BoxModel getRenderModel(ItemStack raw, IBlockState state, float renderTimer) {
		BoxModel model = new BoxModel();
		model.sprite = getModel(new ItemStack(Items.IRON_INGOT)).getParticleTexture();
		if (StackHelper.isEmpty(raw)) return model;
		int tier = raw.getMetadata();
		if (tier < 0 || tier >= NUM_TIERS) return model;

		model.add(new BoxQuadListDeferred(0, 0, 0, 1, 1, 1,
				() -> getModel(tierList[tier + 1].getMainStack()).getParticleTexture(),
				(side) -> (side == null ? createQuads(tier) : ImmutableList.of())
		));
		return model;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> createQuads(int tier) {
		float renderTimer = MCTimer.renderTimer;
		List<BakedQuad> quads = new ArrayList<>();
		renderTimer /= 2;
		renderTimer += tier * tier + tier * 1.21F;

		IBakedModel orbitModel;
		IBakedModel centerModel;
		IBakedModel subOrbitModel;

		centerModel = getModel(tierList[tier + 1].getMainStack());
		orbitModel = getModel(tierList[tier].getMainStack());
		subOrbitModel = tier == 0 ? null : getModel(tierList[tier - 1].getMainStack());

		Vector3f ring_axis_a = new Vector3f(0, 1, 0);
		Vector3f ring_axis_b = new Vector3f(1, 0, 0);
		Vector3f ring_axis_c = new Vector3f(0, 0, 1);
		Matrix4f mat = new Matrix4f();

		mat.setIdentity();
		float r = renderTimer / 5;
		Vector3f[] vecs = {
				new Vector3f(0, 1, 0),
				new Vector3f(0, 0, 1),
				new Vector3f(1, 0, 0),
				new Vector3f(0, 1, 0),
				new Vector3f(0, 0, 1),
		};
		for (Vector3f subAxis : vecs) {
			QuadHelper.rotate(r, subAxis.x, subAxis.y, subAxis.z, mat, mat);
			r = r * 0.546F;
		}

		mat.transform(ring_axis_a);
		mat.transform(ring_axis_b);
		mat.transform(ring_axis_c);

		mat.setIdentity();
		r = renderTimer / 2;
		for (Vector3f subAxis : vecs) {
			QuadHelper.rotate(r, subAxis.x, subAxis.y, subAxis.z, mat, mat);
			r = r / 2;
		}

		Vector3f orbit_axis = new Vector3f(0, 1, 0);
		mat.transform(orbit_axis);

		mat.setIdentity();
		QuadHelper.translate(mat, -0.5F, -0.5F, -0.5F);
		mat.mul(CORE_REDUCTION);
		QuadHelper.rotate(MCTimer.renderTimer / 15, 0, 1, 0, mat, mat);
		QuadHelper.translate(mat, 0.5F, 0.5F, 0.5F);

		if (centerModel != null) {
			for (EnumFacing facing : FacingHelper.facingPlusNull) {
				for (BakedQuad bakedQuad : centerModel.getQuads(null, facing, 0)) {
					quads.add(QuadHelper.applyMatrixTransform(bakedQuad, mat));
				}
			}
		}

		for (int i = 0; i < NUMBER_ORBITS; i++) {
			float ang = (float) (i * Math.PI * 2) / NUMBER_ORBITS;

			mat.setIdentity();
			QuadHelper.translate(mat, -0.5F, -0.5F, -0.5F);
			QuadHelper.scale(mat, ORBIT_REDUCTION);

			QuadHelper.rotate(renderTimer / 6 + ang, orbit_axis, mat);
			float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);

			QuadHelper.translate(mat,
					(ring_axis_a.x * ca + ring_axis_b.x * sa) * ORBIT_DISTANCE,
					(ring_axis_a.y * ca + ring_axis_b.y * sa) * ORBIT_DISTANCE,
					(ring_axis_a.z * ca + ring_axis_b.z * sa) * ORBIT_DISTANCE);

//			QuadHelper.rotate(ang, ring_axis_a, mat, mat);
			QuadHelper.translate(mat, 0.5F, 0.5F, 0.5F);

			if (orbitModel != null) {
				for (EnumFacing facing : FacingHelper.facingPlusNull) {
					for (BakedQuad bakedQuad : orbitModel.getQuads(null, facing, 0)) {
						quads.add(QuadHelper.applyMatrixTransform(bakedQuad, mat));
					}
				}
			}

			if (
//					((i%2)==0) &&
					subOrbitModel != null) {
				for (int j = 0; j < NUMBER_SUBORBITS; j++) {
					float ang2 = (float) (j * Math.PI * 2) / NUMBER_SUBORBITS + renderTimer / 4.6F + ang * 24.434F + i * i;

					mat.setIdentity();
					QuadHelper.translate(mat, -0.5F, -0.5F, -0.5F);
					QuadHelper.scale(mat, SUB_ORBIT_REDUCTION);

					float ca2 = (float) Math.cos(ang2), sa2 = (float) Math.sin(ang2);

					QuadHelper.translate(mat,
							(ring_axis_c.x * ca2 + ring_axis_b.x * sa2) * SUB_ORBIT_DISTANCE,
							(ring_axis_c.y * ca2 + ring_axis_b.y * sa2) * SUB_ORBIT_DISTANCE,
							(ring_axis_c.z * ca2 + ring_axis_b.z * sa2) * SUB_ORBIT_DISTANCE);

					QuadHelper.rotate(renderTimer / 6 + ang2 + ang + i, orbit_axis, mat);

					QuadHelper.translate(mat,
							(ring_axis_a.x * ca + ring_axis_b.x * sa) * ORBIT_DISTANCE,
							(ring_axis_a.y * ca + ring_axis_b.y * sa) * ORBIT_DISTANCE,
							(ring_axis_a.z * ca + ring_axis_b.z * sa) * ORBIT_DISTANCE);


					QuadHelper.translate(mat, 0.5F, 0.5F, 0.5F);

					for (EnumFacing facing : FacingHelper.facingPlusNull) {
						for (BakedQuad bakedQuad : subOrbitModel.getQuads(null, facing, 0)) {
							quads.add(QuadHelper.applyMatrixTransform(bakedQuad, mat));
						}
					}
				}
			}
		}

		for (BakedQuad quad : quads) {
			int[] vertexData = quad.getVertexData();
			EnumFacing enumfacing = FaceBakery.getFacingFromVertexData(vertexData);
			net.minecraftforge.client.ForgeHooksClient.fillNormal(vertexData, enumfacing);
		}
		return quads;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	private IBakedModel getModel(ItemStack stack) {
		IBakedModel duplicateModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
		return duplicateModel.getOverrides().handleItemState(duplicateModel, stack, null, null);
	}
}
