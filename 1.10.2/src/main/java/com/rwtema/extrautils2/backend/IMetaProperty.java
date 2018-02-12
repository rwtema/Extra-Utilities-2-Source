package com.rwtema.extrautils2.backend;

import com.google.common.base.Optional;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public interface IMetaProperty<T extends Comparable<T>> extends IProperty<T> {
	T calculateValue(IBlockAccess worldIn, BlockPos pos, IBlockState originalState);

	default boolean isVisible() {
		return false;
	}

	T getDefaultValue();

	default boolean addLocalization(){return false;}

	abstract class Wrap<T extends Comparable<T>> implements IMetaProperty<T> {
		final IProperty<T> base;
		final T defaultValue;

		public Wrap(IProperty<T> base, T defaultValue) {
			this.base = base;
			this.defaultValue = defaultValue;
		}

		@Nonnull
		@Override
		public String getName() {
			return base.getName();
		}

		@Nonnull
		@Override
		public Collection<T> getAllowedValues() {
			return base.getAllowedValues();
		}

		@Nonnull
		@Override
		public Class<T> getValueClass() {
			return base.getValueClass();
		}

		@Nonnull
		@Override
		@SideOnly(Side.CLIENT)
		public Optional<T> parseValue(@Nonnull String value) {
			return base.parseValue(value);
		}

		@Nonnull
		@Override
		public String getName(@Nonnull T value) {
			return base.getName(value);
		}

		@Override
		public T getDefaultValue() {
			return defaultValue;
		}
	}

	abstract class WrapTile<T extends Comparable<T>, K extends TileEntity> extends Wrap<T> {

		private final Class<K> clazz;

		public WrapTile(Class<K> clazz, IProperty<T> base) {
			this(clazz, base, Collections.min(base.getAllowedValues()));
		}

		public WrapTile(Class<K> clazz, IProperty<T> base, T defaultVal) {
			super(base, defaultVal);
			this.clazz = clazz;
		}

		@Override
		public T calculateValue(IBlockAccess worldIn, BlockPos pos, IBlockState originalState) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile != null && clazz.isAssignableFrom(tile.getClass())) {
				return getValue((K) tile);
			}
			return defaultValue;
		}

		public abstract T getValue(K tile);
	}
}
