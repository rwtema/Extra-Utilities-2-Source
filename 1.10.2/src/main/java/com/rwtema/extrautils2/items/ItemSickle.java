package com.rwtema.extrautils2.items;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.XUItem;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.BlockCompat;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ThreadLocalBoolean;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import static net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec;

public class ItemSickle extends ItemTool implements IXUItem {
	public static final String[] TYPE_NAMES = new String[]{"sickle_wood", "sickle_stone", "sickle_iron", "sickle_gold", "sickle_diamond"};
	private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.TALLGRASS, Blocks.LEAVES, Blocks.LEAVES2);
	public static ToolMaterial[] VANILLA_MATERIALS = new ToolMaterial[]{
			ToolMaterial.WOOD,
			ToolMaterial.STONE,
			ToolMaterial.IRON,
			ToolMaterial.GOLD,
			ToolMaterial.DIAMOND,
	};
	static ThreadLocal<Boolean> calling = new ThreadLocalBoolean(false);
	private final String name;
	public HashMap<String, Integer> toolRanges = CollectionHelper.populateMap(
			new HashMap<>(),
			ToolMaterial.WOOD.toString(), 1,
			ToolMaterial.STONE.toString(), 2,
			ToolMaterial.IRON.toString(), 3,
			ToolMaterial.GOLD.toString(), 1,
			ToolMaterial.DIAMOND.toString(), 4
	);
	@SideOnly(Side.CLIENT)
	TextureAtlasSprite sprite;

	public ItemSickle(int i) {
		super(3.0F, -3.0F, VANILLA_MATERIALS[i], EFFECTIVE_ON);
		name = TYPE_NAMES[i];
		XUItem.items.add(this);
		setUnlocalizedName(ExtraUtils2.MODID + ":" + name.toLowerCase(Locale.ENGLISH));
		setCreativeTab(ExtraUtils2.creativeTabExtraUtils);
		this.setMaxDamage(toolMaterial.getMaxUses() * 9);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register("tools/" + name);
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
		sprite = Textures.sprites.get("tools/" + name);
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

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		return Lang.translate(this.getUnlocalizedNameInefficiently(stack) + ".name", StringHelper.sepWords(Item.REGISTRY.getNameForObject(this).getResourcePath().replace("Item", "")));
	}

	@Override
	public boolean canHarvestBlock(IBlockState state) {
		Block block = state.getBlock();
		return block instanceof IPlantable || block instanceof IShearable;
	}

	@Override
	public float getStrVsBlock(@Nonnull ItemStack stack, IBlockState state) {
		return state.getBlock() instanceof IPlantable ? this.efficiencyOnProperMaterial : super.getStrVsBlock(stack, state);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		if (calling.get()) return false;
		World worldObj = player.world;

		IBlockState blockState1 = worldObj.getBlockState(pos);
		Block block = blockState1.getBlock();
		if (worldObj.isRemote || !canHarvestBlock(blockState1)) return false;

		String toolMaterial = getToolMaterialName();
		if (!toolRanges.containsKey(toolMaterial)) return false;

		int range = toolRanges.get(toolMaterial);


		try {
			calling.set(true);
			for (BlockPos.MutableBlockPos blockPos : BlockPos.getAllInBoxMutable(pos.add(-range, -range, -range), pos.add(range, range, range))) {
				IBlockState blockState = worldObj.getBlockState(blockPos);
				if (block == blockState.getBlock()) {
					ExtraUtils2.proxy.onBlockStartBreak(worldObj, itemstack, blockPos, player, true);
				}
			}

			calling.set(false);
		} catch (Throwable throwable) {
			calling.set(false);
			throw Throwables.propagate(throwable);
		}

		return true;
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void rightClickHandler(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		if (StackHelper.isNull(stack) || stack.getItem() != this) return;

		if (calling.get()) return;

		World world = event.getWorld();

		BlockPos pos = event.getPos();
		IBlockState state = world.getBlockState(pos);

		if (!canHarvestBlock(state)) {
			return;
		}

		event.setCanceled(true);
		event.setUseBlock(Event.Result.DENY);
		event.setUseItem(Event.Result.DENY);

		if (world.isRemote) return;

		try {
			calling.set(true);

			String toolMaterial = getToolMaterialName();
			if (!toolRanges.containsKey(toolMaterial)) return;

			int range = toolRanges.get(toolMaterial);
			EntityPlayer player = event.getEntityPlayer();


			EnumFacing side = event.getFace();
			Vec3d hitVec = event.getHitVec();
			float hitX = (float) hitVec.x;
			float hitY = (float) hitVec.y;
			float hitZ = (float) hitVec.z;
			EnumHand hand = event.getHand();

			for (BlockPos.MutableBlockPos blockPos : BlockPos.getAllInBoxMutable(pos.add(-range, -range, -range), pos.add(range, range, range))) {
				IBlockState blockState = world.getBlockState(blockPos);
				if (blockState.getBlock() == state.getBlock()) {
					PlayerInteractEvent.RightClickBlock subevent = BlockCompat.onRightClickBlock(player, hand, stack, pos, side, rayTraceEyeHitVec(player, 2));
					if (!subevent.isCanceled() && subevent.getUseBlock() != Event.Result.DENY) {
						CompatHelper.activateBlock(blockState.getBlock(), world, pos, blockState, player, hand, stack, side,
								hitX, hitY, hitZ);
					}
				}
			}

		} finally {
			calling.set(false);
		}
	}
}
