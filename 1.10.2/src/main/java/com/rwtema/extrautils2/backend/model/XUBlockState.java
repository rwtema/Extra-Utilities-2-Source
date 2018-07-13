package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.backend.IMetaProperty;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public class XUBlockState extends BlockStateContainer.StateImplementation implements IExtendedBlockState {
	@SideOnly(Side.CLIENT)
	public ThreadLocal<MutableModel> result;

	public int metadata;
	public int dropMeta;
	public String dropName;
	public String unlocalizedName;
	@Nullable
	private IBlockState clean;

	public XUBlockState(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn) {
		super(blockIn, propertiesIn);
	}

	private static <T extends Comparable<T>> IBlockState withDefaultMetaProperty(IBlockState base, IMetaProperty<T> property) {
		return base.withProperty(property, property.getDefaultValue());
	}

	@Override
	public void buildPropertyValueTable(Map<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation> map) {
		super.buildPropertyValueTable(map);
		clean = null;

	}

	public String getUnlocalizedName() {
		if (unlocalizedName == null) {
			if (StringUtils.isNullOrEmpty(dropName))
				unlocalizedName = getBlock().getUnlocalizedName() + ".name";
			else
				unlocalizedName = getBlock().getUnlocalizedName() + "." + dropName + ".name";
		}
		return unlocalizedName;
	}

	@SideOnly(Side.CLIENT)
	public void load(BoxModel model) {
		if (result == null) {
			result = ThreadLocal.withInitial(() -> new MutableModel(Transforms.blockTransforms));
		}

		model.loadIntoMutable(result.get(), MinecraftForgeClient.getRenderLayer());
	}

	public void clearPropertyTable() {
		propertyValueTable = null;
	}

	@Override
	public Collection<IUnlistedProperty<?>> getUnlistedNames() {
		return ImmutableList.of();
	}

	@Override
	public <V> V getValue(IUnlistedProperty<V> property) {
		throw new IllegalArgumentException("Cannot get unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
	}

	@Override
	public <V> IExtendedBlockState withProperty(IUnlistedProperty<V> property, V value) {
		throw new IllegalArgumentException("Cannot set unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
	}

	@Override
	public ImmutableMap getUnlistedProperties() {
		return ImmutableMap.of();
	}

	@Override
	@Nonnull
	public IBlockState getClean() {
		IBlockState clean = this.clean;
		if (clean == null) {
			clean = this;
			for (Map.Entry<IProperty<?>, Comparable<?>> entry : getProperties().entrySet()) {
				if (entry.getKey() instanceof IMetaProperty<?>) {
					IMetaProperty<?> metaProperty = (IMetaProperty<?>) entry.getKey();
					clean = withDefaultMetaProperty(clean, metaProperty);
				}
			}
			this.clean = clean;
		}
		return clean;
	}

}
