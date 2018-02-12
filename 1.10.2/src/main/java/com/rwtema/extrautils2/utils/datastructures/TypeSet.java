package com.rwtema.extrautils2.utils.datastructures;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.*;

public class TypeSet<V> extends ForwardingSet<V> {
	private final ISetMaker supplier;
	private final Set<V> mainCollection;
	private final Class<V> baseClazz;
	private final HashMap<Class<? extends V>, Set<V>> subCollections = new HashMap<>();
	private final HashMap<Class<? extends V>, List<Class<? extends V>>> parentage = new HashMap<>();

	public TypeSet(Class<V> baseClazz) {
		this(HashSet::new, baseClazz);
	}

	public TypeSet(ISetMaker supplier, Class<V> baseClazz) {
		this.supplier = supplier;
		mainCollection = supplier.get();
		this.baseClazz = baseClazz;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(@Nonnull V element) {
		if (element == null) throw new NullPointerException();
		boolean add = super.add(element);
		if (add) {
			List<Class<? extends V>> classList = parentage.computeIfAbsent((Class<? extends V>) element.getClass(), clazz -> {
				LinkedList<Class<? extends V>> classList1 = new LinkedList<>();
				LinkedList<Class> toProcess = new LinkedList<>();
				toProcess.add(clazz);
				Class processing;
				while ((processing = toProcess.poll()) != null) {
					if (baseClazz.isAssignableFrom(processing)) {
						classList1.add(processing);
						if (clazz.getSuperclass() != null)
							toProcess.add(clazz.getSuperclass());
						Collections.addAll(toProcess, clazz.getInterfaces());
					}
				}
				classList1.remove(this.baseClazz);
				return ImmutableList.copyOf(classList1);
			});
			for (Class<? extends V> aClass : classList) {
				subCollections.computeIfAbsent(aClass, supplier::get).add(element);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(@Nonnull Collection<? extends V> collection) {
		return standardAddAll(collection);
	}

	public <K extends V> Set<K> getSubTypes(Class<K> clazz) {
		@SuppressWarnings("unchecked")
		Set<K> vs = (Set<K>) subCollections.get(clazz);
		if (vs == null) return ImmutableSet.of();
		return ImmutableSet.copyOf(vs);
	}


	@Override
	protected Set<V> delegate() {
		return mainCollection;
	}

	public interface ISetMaker {
		<E> Set<E> get();

		default <E> Set<E> get(Class<? extends E> clazz) {
			return get();
		}
	}
}
