package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.entries.BlockEntry;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.BoxSingleQuad;
import com.rwtema.extrautils2.backend.model.UV;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.particles.PacketParticleSplosion;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.WeakLinkedSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

public class BlockSpike extends XUBlockStatic {
	public static final DamageSource spike_creative = new DamageSource("spike_creative").setDamageBypassesArmor();

	static final float bounds = 1 / 32F;
	private static final String spikeDamageName = "spike";
	public static final DamageSource spike = new DamageSource(spikeDamageName);

	static {
		Lang.translate("death.attack.spike", "%1$s walked on a pointy spike (ouchies)");
		Lang.translate("death.attack.spike.item", "%1$s walked on a pointy spike (ouchies)");
		Lang.translate("death.attack.spike_creative", "%1$s failed to become the guy");
		Lang.translate("death.attack.spike_creative.item", "%1$s failed to become the guy");
	}

	final SpikeType type;

	public BlockSpike(SpikeType type) {
		super(type.material);
		this.type = type;
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addWorldProperties(XUBlockStateCreator.ROTATION_ALL).build();
	}

	@Override
	public void onBlockExploded(World world, @Nonnull BlockPos pos, @Nonnull Explosion explosion) {
		if (blockHardness >= 0)
			super.onBlockExploded(world, pos, explosion);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		final float base_height = 1 / 16F;
		BoxModel model = new BoxModel();
		model.renderAsNormalBlock = false;
		model.overrideBounds = new Box(bounds, bounds, bounds, 1 - bounds, 1 - bounds, 1 - bounds);

		model.addBox(0, 0, 0, 1, base_height, 1).setTexture("spikes/spike_" + type.name() + "_base");
		for (int i = 0; i < 4; i++) {
			model.add(new BoxSingleQuad(
					new UV(1, base_height, 0, 0, base_height),
					new UV(0, base_height, 0, 1, base_height),
					new UV(0.5F - 1e-10F, 1, 0.5F, 0.5F, 1),
					new UV(0.5F + 1e-10F, 1, 0.5F, 0.5F, 1)
			).setDoubleSided(false).setTexture("spikes/spike_" + type.name() + "_side").rotateY(i));
		}
		model = model.rotateToSide(state.getValue(XUBlockStateCreator.ROTATION_ALL));

		return model;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxBase(IBlockState state, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos) {
		return new AxisAlignedBB(bounds, bounds, bounds, 1 - bounds, 1 - bounds, 1 - bounds);
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return blockHardness >= 0 && super.canEntityDestroy(state, world, pos, entity);
	}

	@Override
	public void addCollisionBoxToListBase(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
		AxisAlignedBB collisionBoundingBox;
		if (isIgnored(entityIn))
			collisionBoundingBox = Block.FULL_BLOCK_AABB;
		else {
			switch (state.getValue(XUBlockStateCreator.ROTATION_ALL)) {
				case DOWN:
					collisionBoundingBox = new AxisAlignedBB(bounds, 0, bounds, 1 - bounds, 1 - bounds, 1 - bounds);
					break;

				case UP:
					collisionBoundingBox = new AxisAlignedBB(bounds, bounds, bounds, 1 - bounds, 1, 1 - bounds);
					break;
				case NORTH:
					collisionBoundingBox = new AxisAlignedBB(bounds, bounds, 0, 1 - bounds, 1 - bounds, 1 - bounds);
					break;
				case SOUTH:
					collisionBoundingBox = new AxisAlignedBB(bounds, bounds, bounds, 1 - bounds, 1 - bounds, 1);
					break;
				case WEST:
					collisionBoundingBox = new AxisAlignedBB(0, bounds, bounds, 1 - bounds, 1 - bounds, 1 - bounds);
					break;
				case EAST:
					collisionBoundingBox = new AxisAlignedBB(bounds, bounds, bounds, 1, 1 - bounds, 1 - bounds);
					break;
				default:
					throw new IllegalStateException("Invalid side");
			}
		}
		addCollisionBoxToList(pos, entityBox, collidingBoxes, collisionBoundingBox);
	}

	private boolean isIgnored(Entity entityIn) {
		return type.isIgnored(entityIn);
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return xuBlockState.getStateFromDropMeta(meta).withProperty(XUBlockStateCreator.ROTATION_ALL, facing.getOpposite());
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (worldIn.isRemote) return;
		if (isIgnored(entityIn)) {
			return;
		}
		if (entityIn instanceof EntityLivingBase) {
			type.hurtEntity(worldIn, pos, state, (EntityLivingBase) entityIn);
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		type.addInformation(stack, playerIn, tooltip, advanced);
	}

	@Override
	public boolean rotateBlock(World world, @Nonnull BlockPos pos, EnumFacing axis) {
		if (blockHardness < 0) return false;
		return super.rotateBlock(world, pos, axis);
	}

	@Override
	public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
		if (blockHardness < 0) return state;
		return super.withRotation(state, rot);
	}

	@Nonnull
	@Override
	public EnumFacing[] getValidRotations(World world, @Nonnull BlockPos pos) {
		if (blockHardness < 0) return null;
		return super.getValidRotations(world, pos);
	}

//	@Override
//	public boolean hasTileEntity(IBlockState state) {
//		return state.getValue(enchanted);
//	}
//
//	@Nonnull
//	@Override
//	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
//		return new TileSpike();
//	}


	public enum SpikeType {
		wood(Material.WOOD, 1, Items.WOODEN_SWORD, new ItemStack(Blocks.PLANKS), new ItemStack(Blocks.LOG)) {
			@Override
			public void hurtEntity(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entityIn) {
				if (entityIn.getHealth() <= amount) return;
				super.hurtEntity(worldIn, pos, state, entityIn);
			}

			@Override
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Reduces health to half a heart, but doesn't kill"));
			}
		},
		stone(Material.ROCK, 2, Items.STONE_SWORD, "cobblestone", "compressed1xCobblestone"),
		iron(Material.IRON, 4, Items.IRON_SWORD, "ingotIron", "blockIron"),
		gold(Material.IRON, 2, Items.GOLDEN_SWORD, "ingotGold", "blockGold") {
			@Override
			public void hurtEntity(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entityIn) {
				entityIn.attackEntityFrom(spike, amount);
				entityIn.recentlyHit = 100;
			}

			@Override
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Mobs drop experience"));
			}
		},
		diamond(Material.IRON, 8, Items.DIAMOND_SWORD, "gemDiamond", "blockDiamond") {
			@Override
			public void hurtEntity(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entityIn) {
				float min = Math.min(amount, entityIn.getHealth() - 0.0001f);
				entityIn.attackEntityFrom(spike, min);

				if (entityIn.getHealth() <= 0.001f) {
					if (worldIn instanceof WorldServer) {
						FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) worldIn);
						entityIn.attackEntityFrom(new EntityDamageSource(spikeDamageName, fakePlayer), amount * 1000);
						if (entityIn instanceof EntityLiving) {
							((EntityLiving) entityIn).experienceValue = 0;
						}
					} else {
						super.hurtEntity(worldIn, pos, state, entityIn);
					}
				}
			}

			@Override
			public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
				tooltip.add(Lang.translate("Mobs drop 'Player-kill only' items"));
			}
		},
		creative(Material.ROCK, 8000, null, null, null) {
			{
				MinecraftForge.EVENT_BUS.register(EventHandler.class);
			}

			@Override
			public void hurtEntity(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entityIn) {
//				boolean weTemporarilySetKeepInventoryToTrue;
//
//				if (entityIn instanceof EntityPlayer &&
//						!worldIn.getGameRules().getBoolean("keepInventory")) {
//					weTemporarilySetKeepInventoryToTrue = true;
//					worldIn.getGameRules().setOrCreateGameRule("keepInventory", "true");
//
//				} else weTemporarilySetKeepInventoryToTrue = false;

				EventHandler.entitiesToKillDrops.add(entityIn);
				for (int i = 0; i < 100; i++) {
					if (!entityIn.attackEntityFrom(spike_creative, amount))
						break;
				}
				EventHandler.entitiesToKillDrops.remove(entityIn);
//				if (weTemporarilySetKeepInventoryToTrue) {
//					worldIn.getGameRules().setOrCreateGameRule("keepInventory", "false");
//				}

				if (entityIn.getHealth() <= 0 && entityIn instanceof EntityLiving) {

					EntityLiving entityLiving = (EntityLiving) entityIn;
					NetworkHandler.sendToAllAround(
							new PacketParticleSplosion(entityIn.getEntityId()),
							entityLiving.world.provider.getDimension(),
							entityLiving.posX,
							entityLiving.posY,
							entityLiving.posZ, 64);

					entityLiving.experienceValue = 0;
				}
			}

			@Override
			public boolean isIgnored(Entity entityIn) {
				return false;
			}
		};

		public final Material material;
		public final float amount;

		public final Item sword;
		public final Object itemIngot;
		public final Object itemBlock;

		SpikeType(Material material, float amount, Item sword, Object itemIngot, Object itemBlock) {
			this.material = material;
			this.amount = amount;

			this.sword = sword;
			this.itemIngot = itemIngot;
			this.itemBlock = itemBlock;
		}

		public static void addRecipes(BlockEntry<BlockSpike> entry) {
			SpikeType type = entry.value.type;
			CraftingHelper.addShaped("spike_" + type.name().toLowerCase(Locale.ENGLISH), entry.newStack(4), " S ", "SIS", "IBI", 'S', type.sword, 'I', type.itemIngot, 'B', type.itemBlock);
		}

		public void hurtEntity(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entityIn) {
			entityIn.attackEntityFrom(spike, amount);
			if (entityIn.getHealth() <= 0 && entityIn instanceof EntityLiving) {
				((EntityLiving) entityIn).experienceValue = 0;
			}
		}

		public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

		}

		public boolean isIgnored(Entity entityIn) {
			return entityIn instanceof EntityItem || entityIn instanceof EntityXPOrb;
		}
	}

	public static class Creative extends BlockSpike {
		public Creative() {
			super(SpikeType.creative);
			if(!ExtraUtils2.allowNonCreativeHarvest)
				setBlockUnbreakable();
			setResistance(6000000.0F);
		}

		@Override
		public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
			if (pos.getY() != 1) {
				return;
			}

			if (state.getBlock() != this) {
				return;
			}
			EnumFacing value = state.getValue(XUBlockStateCreator.ROTATION_ALL);
			if (value != EnumFacing.DOWN) return;

			if (worldIn.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
				BlockPos up = pos.up();
				IBlockState blockState = worldIn.getBlockState(up);
				Block block = blockState.getBlock();
				if (block.isAir(state, worldIn, up)) {
					return;
				}
				if (block instanceof BlockLiquid || block instanceof IFluidBlock) {
					worldIn.setBlockState(up, Blocks.AIR.getDefaultState(), 2);
				} else {
					worldIn.destroyBlock(up, true);
				}


			}

		}
	}

	public final static class EventHandler {
		final static WeakLinkedSet<EntityLivingBase> entitiesToKillDrops = new WeakLinkedSet<>();

		@SubscribeEvent
		public static void killDrops(LivingDropsEvent event) {
			EntityLivingBase base = event.getEntityLiving();
			if (entitiesToKillDrops.contains(base)) {
				event.setCanceled(true);
			}
		}
	}


}
