package com.rwtema.extrautils2.backend;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyHelper;

import javax.annotation.Nonnull;
import java.util.*;

public class PropertyEnumSimple<T extends Enum<T>> extends PropertyHelper<T> {
	List<T> allowedValues = new ArrayList<>();
	HashMap<String, T> nameToValue = new HashMap<>();
	EnumMap<T, String> propertyNames;

	public PropertyEnumSimple(Class<T> valueClass) {
		this(valueClass, valueClass.getSimpleName().toLowerCase(Locale.ENGLISH));
	}

	public PropertyEnumSimple(Class<T> valueClass, String name) {
		super(name, valueClass);
		allowedValues = Lists.newArrayList(valueClass.getEnumConstants());
		Collections.sort(allowedValues);
		propertyNames = new EnumMap<>(valueClass);
		for (T t : allowedValues) {
			String key = t.toString().toLowerCase();
			nameToValue.put(key, t);
			propertyNames.put(t, key);
		}
	}

	@Nonnull
	@Override
	public Collection<T> getAllowedValues() {
		return allowedValues;
	}

	@Nonnull
	@Override
	public Optional<T> parseValue(@Nonnull String value) {
		return Optional.fromNullable(this.nameToValue.get(value));
	}

	@Nonnull
	@Override
	public String getName(@Nonnull T value) {
		return propertyNames.get(value);
	}
}
