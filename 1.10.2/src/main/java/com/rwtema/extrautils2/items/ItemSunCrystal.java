package com.rwtema.extrautils2.items;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.XUItem;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.backend.model.UV;
import com.rwtema.extrautils2.lighting.ILight;
import com.rwtema.extrautils2.power.player.IPlayerPowerCreator;
import com.rwtema.extrautils2.power.player.PlayerPower;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemSunCrystal extends XUItem {
	public static IPlayerPowerCreator creator = new IPlayerPowerCreator() {
		@Override
		public PlayerPower createPower(EntityPlayer player, ItemStack params) {
			return new PlayerLight(player);
		}
	};
	@SideOnly(Side.CLIENT)
	TextureAtlasSprite sprite, sprite2, gradient;
	@SideOnly(Side.CLIENT)
	List<BakedQuad> rays;
	float prevTime = -1;
	@SideOnly(Side.CLIENT)
	Vector3f vecs[];

	public ItemSunCrystal() {
		this.setMaxDamage(250);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register("gradient", "sun_crystal", "sun_crystal2");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void postTextureRegister() {
		sprite = Textures.sprites.get("sun_crystal");
		sprite2 = Textures.sprites.get("sun_crystal2");
		gradient = Textures.sprites.get("gradient");
	}

	@Override
	public void getSubItemsBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		subItems.add(new ItemStack(itemIn, 1, itemIn.getMaxDamage()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {
		return sprite;
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		ItemStack item = entityItem.getItem();
		int itemDamage = item.getItemDamage();
		if (itemDamage > 0) {
			World worldObj = entityItem.world;
			if (!worldObj.provider.isNether()) {
				BlockPos pos = new BlockPos(entityItem.posX, entityItem.getEntityBoundingBox().minY, entityItem.posZ);

				if (!worldObj.canSeeSky(pos)) return false;

				int i = worldObj.getLightFor(EnumSkyBlock.SKY, pos) - worldObj.getSkylightSubtracted();
				float f = worldObj.getCelestialAngleRadians(1.0F);
				float f1 = f < (float) Math.PI ? 0.0F : ((float) Math.PI * 2F);
				f = f + (f1 - f) * 0.2F;
				i = Math.round((float) i * MathHelper.cos(f));
				if (i > 0 && (i >= 15 || worldObj.rand.nextInt(15) < i))
					item.setItemDamage(itemDamage - 1);

			}
		}
		return false;
	}

	@Override
	public boolean allowOverride() {
		return false;
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack, World world, EntityLivingBase entity) {
		model.addSprite(sprite);
		TextureAtlasSprite sprite2 = this.sprite2;

//		model.addGLState(new GLState.DepthState(false));

		int progress = 255 - (stack.getItemDamage() * 255) / stack.getMaxDamage();
		int color = ColorHelper.makeAlphaWhite(progress);

		if (rays == null) {
			rays = new ArrayList<>();

			vecs = new Vector3f[]{
					new Vector3f(1, 0, 0),
					new Vector3f(0, 1, 0),
					new Vector3f(0, 0, 1),
					new Vector3f(1, 0, 0),
					new Vector3f(0, 1, 0),
					new Vector3f(0, 0, 1),
			};
		}

		rays.clear();

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		// front
		builder.add(QuadHelper.buildQuad(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), EnumFacing.SOUTH, -1,
				0, 0, 7.5f / 16f, sprite2.getMinU(), sprite2.getMaxV(),
				0, 1, 7.5f / 16f, sprite2.getMinU(), sprite2.getMinV(),
				1, 1, 7.5f / 16f, sprite2.getMaxU(), sprite2.getMinV(),
				1, 0, 7.5f / 16f, sprite2.getMaxU(), sprite2.getMaxV(), color, sprite2
		));
		// back
		builder.add(QuadHelper.buildQuad(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), EnumFacing.NORTH, -1,
				0, 0, 8.5f / 16f, sprite2.getMinU(), sprite2.getMaxV(),
				1, 0, 8.5f / 16f, sprite2.getMaxU(), sprite2.getMaxV(),
				1, 1, 8.5f / 16f, sprite2.getMaxU(), sprite2.getMinV(),
				0, 1, 8.5f / 16f, sprite2.getMinU(), sprite2.getMinV(), color, sprite2
		));
		rays.addAll(builder.build());

		Random rand = new Random(425L + System.identityHashCode(stack));

		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();

		Vector4f b = new Vector4f();
		Vector4f c = new Vector4f();
		Vector4f d = new Vector4f();


		for (int i = 0; i < 16; i++) {
			for (Vector3f vec : vecs) {
				QuadHelper.rotate((float) Math.PI * 2 * rand.nextFloat() + MCTimer.renderTimer / 360, vec, matrix, matrix);
			}

			float r = (1F + rand.nextFloat() * 0.5F) * progress / 255.0F;

			QuadHelper.rotate(MCTimer.renderTimer / 180, new Vector3f(0, 1, 0), matrix, matrix);

			b.set(0F, 0.126F * r, 0.5F * r, 1);
			c.set(0F, -0.126F * r, 0.5F * r, 1);
			d.set(0F, 0, 0.6F * r, 1);

			matrix.transform(b);
			matrix.transform(c);
			matrix.transform(d);

			rays.add(QuadHelper.createBakedQuad(new UV[]{
					new UV(0.5F, 0.5F, 0.5F, 0.5F, 1),
					new UV(0.5F + b.x, 0.5F + b.y, 0.5F + b.z, 1, 0),
					new UV(0.5F + d.x, 0.5F + d.y, 0.5F + d.z, 0.5F, 0),
					new UV(0.5F + c.x, 0.5F + c.y, 0.5F + c.z, 0, 0),
			}, "gradient", false, -1));

			rays.add(QuadHelper.createBakedQuad(new UV[]{
					new UV(0.5F, 0.5F, 0.5F, 0.5F, 1),
					new UV(0.5F + c.x, 0.5F + c.y, 0.5F + c.z, 0, 0),
					new UV(0.5F + d.x, 0.5F + d.y, 0.5F + d.z, 0.5F, 0),
					new UV(0.5F + b.x, 0.5F + b.y, 0.5F + b.z, 1, 0),
			}, "gradient", false, -1));
		}

		model.addAllQuads(rays);
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getItemDamage() == stack.getMaxDamage())
			return super.getUnlocalizedName(stack) + ".empty";
		return super.getUnlocalizedName(stack);
	}

	public static class PlayerLight extends PlayerPower implements ILight {
		final double RADIUS = 5;

		Vec3d center;

		public PlayerLight(@Nonnull EntityPlayer player) {
			super(player);
			loadPlayerCenter(player);
		}

		@Override
		public World getLightWorld() {
			return getPlayer().world;
		}

		@Override
		public float getLightOffset(BlockPos pos, EnumSkyBlock type) {
			double dx = pos.getX() - center.x;
			double dy = pos.getY() - center.y;
			double dz = pos.getZ() - center.z;
			double t = 1 - Math.sqrt(dx * dx + dy * dy + dz * dz) / RADIUS;

			return MathHelper.clamp((int) (t * 16), 0, 15);
		}

		@Override
		public EnumSkyBlock[] getLightType() {
			return new EnumSkyBlock[]{EnumSkyBlock.BLOCK};
		}

		@Override
		public float power(EntityPlayer playerMP) {
			return 0;
		}

		@Override
		public void powerChanged(boolean powered) {

		}

		@Override
		public String getName() {
			return Lang.translate("FlashLight");
		}

		@Override
		public void tick() {
			EntityPlayer player = getPlayer();
			loadPlayerCenter(player);
		}

		public void loadPlayerCenter(EntityPlayer player) {
			this.center = new Vec3d(player.posX, player.posY, player.posZ);
		}

		@Override
		public void tickClient() {
			Vec3d pos = this.center;
			tick();
			if (this.center.distanceTo(pos) > 0.01) {
				World world = getPlayer().world;
				world.markBlockRangeForRenderUpdate(
						(int) (center.x - RADIUS),
						(int) (center.y - RADIUS),
						(int) (center.z - RADIUS),
						(int) (center.x + RADIUS),
						(int) (center.y + RADIUS),
						(int) (center.z + RADIUS)
				);
			} else
				this.center = pos;
		}
	}
}
