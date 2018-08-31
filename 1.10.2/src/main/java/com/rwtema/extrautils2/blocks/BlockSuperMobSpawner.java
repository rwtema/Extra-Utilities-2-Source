package com.rwtema.extrautils2.blocks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.tile.TileSuperMobSpawner;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class BlockSuperMobSpawner extends XUBlockStatic {
	public BlockSuperMobSpawner() {
		super(Material.ROCK);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		return new BoxModel(new Box(0, 0, 0, 1, 1, 1).setTexture("mob_spawner")).setLayer(BlockRenderLayer.TRANSLUCENT);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileSuperMobSpawner();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		NBTTagCompound nbt = stack.getTagCompound();
		if(nbt == null) return;

		LinkedList<WeightedSpawnerEntity> list = Lists.newLinkedList();
		if (nbt.hasKey("SpawnPotentials", 9)) {
			NBTTagList nbttagcompound = nbt.getTagList("SpawnPotentials", 10);

			for (int i = 0; i < nbttagcompound.tagCount(); ++i) {
				list.add(new WeightedSpawnerEntity(nbttagcompound.getCompoundTagAt(i)));
			}
		}

		NBTTagCompound var4 = nbt.getCompoundTag("SpawnData");
		if (!var4.hasKey("id", 8)) {
			var4.setString("id", "Pig");
		}

		list.addFirst(new WeightedSpawnerEntity(1, var4));

		HashSet<String> names = Sets.newHashSet();
		for (WeightedSpawnerEntity weightedSpawnerEntity : list) {
			NBTTagCompound entityTags = weightedSpawnerEntity.getNbt();
			if (entityTags.hasKey("id")) {
				String id = entityTags.getString("id");
				String animal_name = CompatHelper.getName(id);
				names.add(animal_name);
			}
		}
		tooltip.addAll(names);
	}
}
