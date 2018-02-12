package com.rwtema.extrautils2.compatibility;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class BlockStateContainerCompat extends BlockStateContainer {
	public BlockStateContainerCompat(Block blockIn, IProperty<?>[] properties) {
		super(blockIn, properties);
	}

	protected BlockStateContainerCompat(Block blockIn, IProperty<?>[] properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
		super(blockIn, properties, unlistedProperties);
	}

	@Nonnull
	@Override
	protected StateImplementation createState(@Nonnull Block block, @Nonnull ImmutableMap<IProperty<?>, Comparable<?>> properties, @Nullable ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
		Validate.isTrue(unlistedProperties == null || unlistedProperties.isEmpty());
		return createState(block, properties);
	}


	protected StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties) {
		return super.createState(block, properties, null);
	}
}
