package com.rwtema.extrautils2.backend;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.backend.multiblockstate.XUBlockStateMulti;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.MapPopulator;
import net.minecraft.util.math.Cartesian;
import net.minecraftforge.common.property.IUnlistedProperty;

public class MultiBlockStateBuilder<T extends XUBlock> {
	public final Class<T> clazz;
	private final List<IProperty<? extends Comparable>> worldProperties = Lists.newArrayList();
	private final List<IProperty<? extends Comparable>> dropProperties = Lists.newArrayList();
	private final List<IProperty<? extends Comparable>> metaProperties = Lists.newArrayList();
	private final HashMap<IProperty, Comparable> defaultValues = Maps.newHashMap();
	private final Constructor<T> constructor;
	private final Object[] parameters;
	public IBlockState[] meta2states;
	public TObjectIntHashMap<IBlockState> states2meta;
	public HashSet<IBlockState> genericPipeStates = new HashSet<>();
	public HashMap<Map<IProperty<?>, Comparable<?>>, XUBlockState> propertyStateBlockStatesMap;
	public boolean initialized = false;
	public XUBlockStateMulti defaultState;
	public T mainBlock;

	public MultiBlockStateBuilder(Class<T> clazz, Object... parameters) {
		this.clazz = clazz;
		this.parameters = parameters;
		Class<?>[] parameClazzes = new Class<?>[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] == null) {
				parameClazzes[i] = int.class;
			} else {
				parameClazzes[i] = parameters[i].getClass();
			}
		}
		try {
			constructor = clazz.getConstructor(parameClazzes);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public MultiBlockStateBuilder<T> addMetaProperties(Collection<IProperty<?>> properties) {
		metaProperties.addAll(properties);
		return this;
	}

	public <K extends Comparable<K>> MultiBlockStateBuilder<T> addWorldProperties(Collection<IProperty<K>> properties) {
		worldProperties.addAll(properties);
		return this;
	}

	public <K extends Comparable<K>> MultiBlockStateBuilder<T> addDropProperties(Collection<IProperty<K>> properties) {
		dropProperties.addAll(properties);
		return this;
	}

	public MultiBlockStateBuilder<T> addWorldProperties(IProperty<?>... properties) {
		Collections.addAll(worldProperties, properties);
		return this;
	}

	public MultiBlockStateBuilder<T> addDropProperties(IProperty<?>... properties) {
		Collections.addAll(dropProperties, properties);
		return this;
	}

	public <K extends Comparable<K>> MultiBlockStateBuilder<T> setDefaultValue(IProperty<K> property, K value) {
		defaultValues.put(property, value);
		return this;
	}

	public List<T> createBlocks(T firstBlock) {
		IProperty<?>[] worldProperties = this.worldProperties.toArray(new IProperty[this.worldProperties.size()]);
		IProperty<?>[] dropProperties = this.dropProperties.toArray(new IProperty[this.dropProperties.size()]);
		IProperty<?>[] properties = XUBlockStateCreator.joinProperties(worldProperties, dropProperties);
		Arrays.sort(properties, XUBlockStateCreator.property_sorter);

		IMetaProperty<?>[] metaProperties = this.metaProperties.toArray(new IMetaProperty[this.metaProperties.size()]);

		List<Collection<? extends Comparable<?>>> allowedValues = Lists.newArrayList();

		for (IProperty<?> property : properties) {
			allowedValues.add(property.getAllowedValues());
		}

		List<List<Comparable<?>>> propertyStates = Lists.newArrayList(Cartesian.cartesianProduct(allowedValues));

		if (propertyStates.size() < 16)
			throw new RuntimeException("No of iblockstates = " + propertyStates.size() + " - Dont be silly");

		int numBlocks = (int) Math.ceil(propertyStates.size() / 16.0);

		List<T> blocks = Lists.newArrayListWithCapacity(numBlocks);

		if (firstBlock != null) blocks.add(firstBlock);

		while (blocks.size() < numBlocks) {
			try {
				Object[] par = new Object[parameters.length];
				for (int i = 0; i < par.length; i++) {
					if (parameters[i] == null)
						par[i] = blocks.size();
					else
						par[i] = parameters[i];
				}

				blocks.add(constructor.newInstance(par));
			} catch (Exception err) {
				throw new RuntimeException(err);
			}
		}

		mainBlock = blocks.get(0);

		final HashMap<Map<IProperty<?>, Comparable<?>>, T> propertyStateBlockMap = new HashMap<>();
		propertyStateBlockStatesMap = new HashMap<>();
		for (int i = 0; i < propertyStates.size(); i++) {
			List<Comparable<?>> propertState = propertyStates.get(i);
			ImmutableMap<IProperty<?>, Comparable<?>> map1 = ImmutableMap.copyOf(MapPopulator.createMap(
					Arrays.asList(properties),
					propertState
			));
			T t = blocks.get(i / 16);
			propertyStateBlockMap.put(map1, t);
		}

		meta2states = new IBlockState[propertyStates.size()];

		HashMap<T, XUBlockStateCreator> map = new HashMap<>();

		for (int i = 0; i < blocks.size(); i++) {
			final T curBlock = blocks.get(i);
			final List<IBlockState> myStates = Lists.newArrayList();
			XUBlockStateCreator creator = new XUBlockStateCreator(curBlock, worldProperties, dropProperties, metaProperties, defaultValues) {
				@Nonnull
				@Override
				protected StateImplementation createState(@Nonnull Block block, @Nonnull ImmutableMap<IProperty<?>, Comparable<?>> properties) {
					if (propertyStateBlockStatesMap.containsKey(properties)) {
						XUBlockState xuBlockState = propertyStateBlockStatesMap.get(properties);
						xuBlockState.clearPropertyTable();
						if (xuBlockState.getBlock() == curBlock) {
							myStates.add(xuBlockState);
						}
						return xuBlockState;
					}

					Map<IProperty<?>, Comparable<?>> cleanProps = new HashMap<>();
					cleanProps.putAll(properties);
					for (IMetaProperty metaProperty : metaProperties) {
						cleanProps.remove(metaProperty);
					}

					T blockIn = propertyStateBlockMap.get(cleanProps);
					XUBlockState state = createXUBlockStateMulti(blockIn, properties, MultiBlockStateBuilder.this.mainBlock);
					if (blockIn == curBlock) {
						myStates.add(state);
					}
					propertyStateBlockStatesMap.put(properties, state);
					return state;
				}


				@Nonnull
				@Override
				public ImmutableList<IBlockState> getValidStates() {
					return ImmutableList.copyOf(myStates);
				}

				@Override
				protected Collection<IBlockState> getMyStates(ImmutableList<IBlockState> validStates) {
					return myStates;
				}
			};

			creator.mainBlock = mainBlock;
			System.arraycopy(creator.meta2state, 0, meta2states, i * 16, creator.meta2state.length);
			genericPipeStates.addAll(myStates);
			map.put(curBlock, creator);
		}

		states2meta = new TObjectIntHashMap<>();
		for (int i = 0; i < meta2states.length; i++) {
			states2meta.put(meta2states[i], i);
		}

		for (XUBlockState state : propertyStateBlockStatesMap.values()) {
			XUBlockState curState = state;
			for (IMetaProperty property : metaProperties) {
				Comparable obj;
				if (defaultValues.containsKey(property)) {
					obj = defaultValues.get(property);
				} else {
					obj = (Comparable) Collections.min(property.getAllowedValues());
				}
				state = (XUBlockState) state.withProperty(property, obj);
			}
			states2meta.putIfAbsent(curState, states2meta.get(state));
		}

		defaultState = (XUBlockStateMulti) map.get(mainBlock).defaultState;

		initialized = true;

		for (Map.Entry<T, XUBlockStateCreator> entry : map.entrySet()) {
			entry.getKey().setBlockState(entry.getValue());
		}

		return blocks;
	}

	@Nonnull
	protected XUBlockStateMulti createXUBlockStateMulti(T blockIn, ImmutableMap<IProperty<?>, Comparable<?>> properties, T mainBlock) {
		return new XUBlockStateMulti(blockIn, properties, mainBlock);
	}
}
