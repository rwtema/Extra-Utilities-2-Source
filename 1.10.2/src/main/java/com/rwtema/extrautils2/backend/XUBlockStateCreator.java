package com.rwtema.extrautils2.backend;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.compatibility.BlockStateContainerCompat;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MapPopulator;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class XUBlockStateCreator extends BlockStateContainerCompat {
	public static final PropertyDirection ROTATION_HORIZONTAL = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection ROTATION_ALL = PropertyDirection.create("facing", Arrays.asList(EnumFacing.values()));
	public static final PropertyDirection ROTATION_HORIZONTAL_INC_DOWN = PropertyDirection.create("facing", Arrays.asList(EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH));
	public static final Map<EnumFacing, PropertyBool> FACING_BOOLEANS = createDirectionBooleanMap(null, (name, side) -> PropertyBool.create(name));
	public static final Comparator<IProperty> property_sorter = Comparator.comparing(IProperty::getName);
	public final HashMap<IProperty, Comparable> defaultValues;
	public final XUBlockState defaultState;
	public final XUBlockState[] dropmeta2state;


	@Nonnull
	public final IMetaProperty<? extends Comparable>[] hiddenProperties;

	protected final TObjectIntHashMap<IBlockState> state2meta = new TObjectIntHashMap<>();
	protected final TObjectIntHashMap<IBlockState> state2dropMeta = new TObjectIntHashMap<>();
	final IBlockState[] meta2state;
	public XUBlock mainBlock;

	@SuppressWarnings("unchecked")
	protected XUBlockStateCreator(XUBlock block, @Nonnull IProperty<? extends Comparable>[] worldProperties, @Nonnull IProperty<? extends Comparable>[] dropProperties, @Nonnull IMetaProperty<? extends Comparable>[] hiddenProperties, HashMap<IProperty, Comparable> defaultValues) {
		super(block, Validate.noNullElements(joinProperties(joinProperties(worldProperties, dropProperties), hiddenProperties)), ImmutableMap.of());

		mainBlock = block;

		this.hiddenProperties = hiddenProperties;


		Arrays.sort(worldProperties, property_sorter);
		Arrays.sort(dropProperties, property_sorter);
		Arrays.sort(hiddenProperties, property_sorter);

		if (defaultValues == null) this.defaultValues = new HashMap<>();
		else this.defaultValues = defaultValues;

		for (IProperty<?> iProperty : getProperties()) {
			if (defaultValues == null || !this.defaultValues.containsKey(iProperty)) {
				Collection<? extends Comparable> allowedValues = iProperty.getAllowedValues();
				this.defaultValues.put(iProperty, Collections.min(allowedValues));
			}
		}

		ImmutableList<IBlockState> validStates = super.getValidStates();

		IBlockState defaultState = validStates.get(0);
		for (Map.Entry<IProperty, Comparable> entry : this.defaultValues.entrySet()) {
			defaultState = defaultState.withProperty(entry.getKey(), entry.getValue());
		}
		this.defaultState = (XUBlockState) defaultState;


		Collection<IBlockState> myStates = getMyStates(validStates);
		LinkedHashSet<IBlockState> significantStates = new LinkedHashSet<>();

		for (IBlockState validState : myStates) {
			validState = collapseStateToMetaState(worldProperties, dropProperties, hiddenProperties, defaultState, validState);
			significantStates.add(validState);
		}

		meta2state = significantStates.toArray(new IBlockState[0]);

		for (int i = 0; i < meta2state.length; i++)
			state2meta.put(meta2state[i], i);

		for (IBlockState validState : validStates) {
			IBlockState significantState = collapseStateToMetaState(worldProperties, dropProperties, hiddenProperties, defaultState, validState);
			state2meta.putIfAbsent(validState, state2meta.get(significantState));
		}

		Validate.isTrue(meta2state.length <= 16);
		for (int i : state2meta.values()) {
			Validate.isTrue(i >= 0 && i < 16);
		}


		LinkedHashSet<IBlockState> dropMetas = new LinkedHashSet<>();
		for (IBlockState validState : validStates) {
			IBlockState metaState = collapseStateToMetaState(worldProperties, dropProperties, hiddenProperties, defaultState, validState);
			for (IProperty property : worldProperties) {
				metaState = metaState.withProperty(property, collapseToProperDropState(metaState, property));
			}
			dropMetas.add(metaState);
		}

		dropmeta2state = dropMetas.toArray(new XUBlockState[0]);

		for (int i = 0; i < dropmeta2state.length; i++)
			state2dropMeta.put(dropmeta2state[i], i);

		for (IBlockState validState : validStates) {
			if (!state2dropMeta.containsKey(validState)) {
				IBlockState metaState = validState;
				for (IProperty property : worldProperties) {
					metaState = metaState.withProperty(property, collapseToProperDropState(metaState, property));
				}
				for (IProperty hiddenProperty : hiddenProperties) {
					metaState = metaState.withProperty(hiddenProperty, this.defaultValues.get(hiddenProperty));
				}
				state2dropMeta.put(validState, state2dropMeta.get(metaState));
			}
		}

		for (IBlockState validState : validStates) {
			StringBuilder builder = new StringBuilder();
			boolean flag = false;

			for (IProperty<? extends Comparable> dropProperty : dropProperties) {
				if (flag) {
					builder.append(".");
				} else
					flag = true;
				builder.append(validState.getValue(dropProperty).toString().toLowerCase(Locale.ENGLISH));
			}

			for (IMetaProperty<? extends Comparable> metaProperty : hiddenProperties) {
				if (!metaProperty.addLocalization()) continue;

				if (flag) {
					builder.append(".");
				} else
					flag = true;
				builder.append(validState.getValue(metaProperty).toString().toLowerCase(Locale.ENGLISH));
			}


			((XUBlockState) validState).dropName = builder.toString();
		}

		for (IBlockState validState : validStates) {
			XUBlockState xuBlockState = (XUBlockState) validState;
			xuBlockState.metadata = state2meta.get(xuBlockState);
			xuBlockState.dropMeta = state2dropMeta.get(xuBlockState);
		}
	}

	public XUBlockStateCreator(XUBlock xuBlock) {
		this(xuBlock, new IProperty<?>[0]);
	}

	public XUBlockStateCreator(XUBlock xuBlock, IProperty<?>... properties) {
		this(xuBlock, properties, new IProperty[0]);
	}

	public XUBlockStateCreator(XUBlock xuBlock, boolean dummy, IProperty<?>... dropProperties) {
		this(xuBlock, new IProperty[0], dropProperties);
	}


	public XUBlockStateCreator(XUBlock xuBlock, IProperty<?>[] properties, IProperty<?>[] dropProperties) {
		this(xuBlock, properties, dropProperties, new IMetaProperty<?>[0], null);
	}

	@Nonnull
	public static <T extends IProperty<Boolean>> EnumMap<EnumFacing, T> createDirectionBooleanMap(String name, final BiFunction<String, EnumFacing, T> function) {
		return new EnumMap<>(
				MapPopulator.createMap(
						Lists.newArrayList(EnumFacing.values()),
						Lists.newArrayList(EnumFacing.values()).stream().map(input -> {
							String dirName = input.getName().toLowerCase(Locale.ENGLISH);
							if (name != null) dirName = name + "_" + dirName;
							return function.apply(dirName, input);
						}).collect(Collectors.toList())));
	}

	public static IProperty<?>[] joinProperties(IProperty[] a, IProperty[] b) {
		if (b == null || b.length == 0) return a;

		IProperty[] properties = new IProperty[a.length + b.length];

		System.arraycopy(a, 0, properties, 0, a.length);
		System.arraycopy(b, 0, properties, a.length, b.length);

		return properties;
	}

	public static Builder builder(XUBlock block) {
		return new Builder(block);
	}

	private IBlockState collapseStateToMetaState(@Nonnull IProperty<? extends Comparable>[] worldProperties, @Nonnull IProperty<? extends Comparable>[] dropProperties, @Nonnull IMetaProperty<? extends Comparable>[] hiddenProperties, IBlockState defaultState, IBlockState validState) {
		IBlockState significantState = validState;
		for (IProperty hiddenProperty : hiddenProperties) {
			significantState = significantState.withProperty(hiddenProperty, this.defaultValues.get(hiddenProperty));
		}
		for (IProperty<? extends Comparable> iProperty : worldProperties) {
			significantState = collapseToProperMetaState(iProperty, significantState, defaultState);
		}
		for (IProperty<? extends Comparable> iProperty : dropProperties) {
			significantState = collapseToProperMetaState(iProperty, significantState, defaultState);
		}
		return significantState;
	}

	protected IBlockState collapseToProperMetaState(IProperty<? extends Comparable> iProperty, IBlockState validState, IBlockState defaultState) {
		return validState;
	}

	protected Comparable collapseToProperDropState(IBlockState metaState, IProperty property) {
		return this.defaultValues.get(property);
	}

	protected Collection<IBlockState> getMyStates(ImmutableList<IBlockState> validStates) {
		return validStates;
	}

	@Nonnull
	@Override
	protected StateImplementation createState(@Nonnull Block block, @Nonnull ImmutableMap<IProperty<?>, Comparable<?>> properties) {
		return new XUBlockState(block, properties);
	}

	public IBlockState getStateFromMeta(int meta) {
		if (meta < 0 || meta >= meta2state.length)
			return defaultState;
		else
			return meta2state[meta];
	}

	public int getMetaFromState(IBlockState state) {
		int i = state2meta.get(state);
		return i >= 0 ? i : 0;
	}

	public XUBlockState getStateFromDropMeta(int meta) {
		if (meta < 0 || meta >= dropmeta2state.length)
			return defaultState;
		else
			return dropmeta2state[meta];
	}

	public int getDropMetaFromState(IBlockState state) {
		int i = state2dropMeta.get(state);
		return i >= 0 ? i : 0;
	}

	public XUBlockState getStateFromItemStack(ItemStack item) {
		return mainBlock.getStateFromItemStack(item);
	}

	public static class Builder {
		final List<IProperty<? extends Comparable>> worldProperties = Lists.newArrayList();
		final List<IProperty<? extends Comparable>> dropProperties = Lists.newArrayList();
		final List<IMetaProperty<? extends Comparable>> metaProperties = Lists.newArrayList();
		final HashMap<IProperty, Comparable> defaultValues = Maps.newHashMap();
		final XUBlock block;

		public Builder(XUBlock block) {
			this.block = block;
		}

		public <T extends Comparable<T>> Builder addMetaProperty(IMetaProperty<T> property) {
			metaProperties.add(property);
			defaultValues.put(property, property.getDefaultValue());
			return this;
		}

		public <T extends Comparable<T>> Builder addWorldPropertyWithDefault(IProperty<T> property, T value) {
			worldProperties.add(property);
			defaultValues.put(property, value);
			return this;
		}

		public <T extends Comparable<T>> Builder addDropPropertyWithDefault(IProperty<T> property, T value) {
			dropProperties.add(property);
			defaultValues.put(property, value);
			return this;
		}

		public Builder addWorldProperties(IProperty<?>... properties) {
			Collections.addAll(worldProperties, properties);
			return this;
		}

		public Builder addDropProperties(IProperty<?>... properties) {
			Collections.addAll(dropProperties, properties);
			return this;
		}

		public <T extends Comparable<T>> Builder setDefaultValue(IProperty<T> property, T value) {
			defaultValues.put(property, value);
			return this;
		}


		public Builder addWorldProperties(Collection<IProperty<?>> properties) {
			worldProperties.addAll(properties);
			return this;
		}

		public Builder addDropProperties(Collection<IProperty<?>> properties) {
			dropProperties.addAll(properties);
			return this;
		}


		public XUBlockStateCreator build() {
			IProperty<?>[] worldProperties = this.worldProperties.toArray(new IProperty[0]);
			IProperty<?>[] dropProperties = this.dropProperties.toArray(new IProperty[0]);
			IMetaProperty<?>[] hiddenProperties = this.metaProperties.toArray(new IMetaProperty[0]);
			return new XUBlockStateCreator(block, worldProperties, dropProperties, hiddenProperties, defaultValues.isEmpty() ? null : defaultValues);
		}
	}
}
