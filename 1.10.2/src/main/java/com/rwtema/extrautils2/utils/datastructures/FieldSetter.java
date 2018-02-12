package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.base.Throwables;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Level;

import java.lang.reflect.*;

public class FieldSetter<C, V> {
	private static Object reflectionFactory;
	private static Method newConstructorAccessor;
	private static Method newInstance;
	private static Method newFieldAccessor;
	private static Method fieldAccessorSet;

	static {
		try {
			Method getReflectionFactory = Class.forName("sun.reflect.ReflectionFactory").getDeclaredMethod("getReflectionFactory");
			reflectionFactory = getReflectionFactory.invoke(null);
			newConstructorAccessor = Class.forName("sun.reflect.ReflectionFactory").getDeclaredMethod("newConstructorAccessor", Constructor.class);
			newInstance = Class.forName("sun.reflect.ConstructorAccessor").getDeclaredMethod("newInstance", Object[].class);
			newFieldAccessor = Class.forName("sun.reflect.ReflectionFactory").getDeclaredMethod("newFieldAccessor", Field.class, boolean.class);
			fieldAccessorSet = Class.forName("sun.reflect.FieldAccessor").getDeclaredMethod("set", Object.class, Object.class);
		} catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
			e.printStackTrace();
		}

	}

	private Field field;
	private final Object fieldAccessor;

	public FieldSetter(Class<C> clazz, String... fieldNames) {
		field = ReflectionHelper.findField(clazz, fieldNames);
		field.setAccessible(true);
		Field modifiersField;
		try {
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			fieldAccessor = newFieldAccessor.invoke(reflectionFactory, field, false);
		} catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

	}

	public void apply(C owner, V object) {
		try {
			fieldAccessorSet.invoke(fieldAccessor, owner, object);
		} catch (Exception e) {
			FMLLog.getLogger().log(Level.WARN, "Unable to set {} with value {}", this.field, object);
		}
	}
}
