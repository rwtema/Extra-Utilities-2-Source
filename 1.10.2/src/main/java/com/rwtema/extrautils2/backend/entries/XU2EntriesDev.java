package com.rwtema.extrautils2.backend.entries;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.blocks.BlockOneWay;
import com.rwtema.extrautils2.items.ItemSantaHat;
import com.rwtema.extrautils2.structure.PatternRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class XU2EntriesDev {
	public static ItemClassEntry<ItemSantaHat> santaHat = new ItemClassEntry<ItemSantaHat>(ItemSantaHat.class) {
		@Override
		public void init() {
//			MinecraftForge.EVENT_BUS.register(new Object() {
//				@SubscribeEvent
//				public void addHat(LivingSpawnEvent.SpecialSpawn event) {
//					if (event.getEntityLiving() instanceof EntityEnderman) {
//						EntityEnderman enderman = (EntityEnderman) event.getEntityLiving();
//						NBTTagCompound nbt = new NBTTagCompound();
//						enderman.writeEntityToNBT(nbt);
//						nbt.setShort("carried", (short) Block.getIdFromBlock(Blocks.CHEST));
//						nbt.setShort("carriedData", (short) 0);
//						NBTTagList nbttaglist = new NBTTagList();
//						for (int i = 0; i < 3; i++) {
//							nbttaglist.appendTag(new NBTTagCompound());
//						}
//						nbttaglist.appendTag(newStack().writeToNBT(new NBTTagCompound()));
//						nbt.setTag("ArmorItems", nbttaglist);
//						enderman.readEntityFromNBT(nbt);
//					}
//				}
//			});
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void postInit() {
			RenderEnderman render = (RenderEnderman) Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(EntityEnderman.class);
			render.addLayer(new LayerBipedArmor(render) {
				@Override
				public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
					this.modelArmor = new ModelEnderman(2.0F);
					super.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
				}

				protected void initArmor() {
					this.modelLeggings = new ModelBiped(0.5F, -14.0F * 2, 64, 32);
					this.modelArmor = new ModelBiped(1.0F, -14.0F * 2, 64, 32);

				}
			});
		}
	};
	public static BlockClassEntry<BlockOneWay> oneWay = new BlockClassEntry<BlockOneWay>(BlockOneWay.class) {

	};

	static {
		PatternRecipe.register(new String[][]{
				{
						"sssss",
						"sgggs",
						"sgggs",
						"sgggs",
						"sssss",
				},
				{
						"sssss",
						"s   s",
						"s   s",
						"s   s",
						"sgggs",
				},
				{
						"sssss",
						"s   s",
						"s   s",
						"s   s",
						"sgggs",
				},
				{
						"sssss",
						"sfffs",
						"sfffs",
						"sfffs",
						"sgggs",
				},
				{
						"sssss",
						"snnns",
						"snnns",
						"snnns",
						"sssss",
				}

		}, ImmutableMap.<Character, Object>builder()
				.put('s', Blocks.STONE.getDefaultState())
				.put(' ', Blocks.AIR.getDefaultState())
				.put('n', Blocks.NETHERRACK.getDefaultState())
				.put('g', Blocks.GLASS.getDefaultState())
				.put('f', Blocks.FIRE.getDefaultState())
				.build()
				);
	}

	public static void init() {

	}
}
