package com.rwtema.extrautils2.utils.datastructures;

import gnu.trove.list.TLinkable;
import gnu.trove.list.linked.TLinkedList;
import gnu.trove.set.hash.THashSet;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class WeakLinkedSet<E> extends AbstractSet<E> {

	private final ReferenceQueue<E> queue = new ReferenceQueue<>();
	private WeakLinkTHashSet set = new WeakLinkTHashSet();
	private TLinkedList<WeakLink<E>> order = new TLinkedList<>();

	@Nonnull
	@Override
	public Iterator<E> iterator() {
		expungeStaleEntries();
		return new Iter();
	}

	@Override
	public int size() {
		expungeStaleEntries();
		return set.size();
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public void clear() {
		while (queue.poll() != null) ;
		set.clear();
		order.clear();
		while (queue.poll() != null) ;
	}

	@Override
	public boolean contains(Object o) {
		expungeStaleEntries();
		return set.contains(o);
	}

	@Override
	public boolean add(E e) {
		expungeStaleEntries();
		if (set.contains(e))
			return false;

		WeakLink<E> link = new WeakLink<>(e, queue);
		if (set.add(link)) {
			order.add(link);
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Object o) {
		expungeStaleEntries();
		WeakLink weakLink = set.removeSpecial(o);
		if (weakLink != null) {
			order.remove(weakLink);
			return true;
		}
		return false;
	}

	private void expungeStaleEntries() {
		Reference poll;
		while ((poll = queue.poll()) != null) {
			synchronized (queue) {
				@SuppressWarnings("unchecked")
				WeakLink entry = (WeakLink) poll;
				entry.fallbackNext = entry.next;
				set.remove(entry);
				order.remove(entry);
			}
		}
	}

	public static class WeakLink<E> extends WeakReference<E> implements TLinkable<WeakLink<E>> {
		final int hash;
		WeakLink<E> next;
		WeakLink<E> previous;
		WeakLink<E> fallbackNext;

		public WeakLink(E referent, ReferenceQueue<E> queue) {
			super(referent, queue);
			hash = System.identityHashCode(referent);
		}

		@Override
		public WeakLink<E> getNext() {
			return next;
		}

		@Override
		public void setNext(WeakLink<E> next) {
			this.next = next;
		}

		@Override
		public WeakLink<E> getPrevious() {
			return previous;
		}

		@Override
		public void setPrevious(WeakLink<E> previous) {
			this.previous = previous;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			E e1 = get();
			if (e1 == null) return false;
			if (e1 == obj) return true;

			if (obj.getClass() == WeakLink.class) {
				WeakLink<E> other = (WeakLink) obj;
				E e2 = other.get();
				return e2 != null && (e1 == e2);
			} else {
				return false;
			}
		}
	}

	private class WeakLinkTHashSet extends THashSet<WeakLink<E>> {
		public WeakLinkTHashSet() {
		}

		@SuppressWarnings("unchecked")
		public WeakLink removeSpecial(Object obj) {
			int index = index(obj);
			if (index >= 0) {
				Object o = _set[index];
				removeAt(index);
				return (WeakLink) o;
			}
			return null;
		}


		@Override
		protected int hash(Object notnull) {
			if (notnull instanceof WeakLink) {
				return notnull.hashCode();
			}
			return System.identityHashCode(notnull);
		}

		@Override
		protected boolean equals(Object notnull, Object two) {
			return !(two == null || two == REMOVED) && two.equals(notnull);

		}
	}

	private class Iter implements Iterator<E> {
		private WeakLink<E> entry = order.getFirst();
		private WeakLink<E> lastReturned = null;
		private E nextKey;
		private E curKey;

		@Override
		public boolean hasNext() {
			while (nextKey == null) {
				if (entry == null) {
					curKey = null;
					return false;
				}

				nextKey = entry.get();
				if (nextKey != null)
					return true;

				WeakLink<E> next = entry.next;
				if (next == null)
					entry = entry.fallbackNext;
				else
					entry = next;
			}
			return true;
		}

		@Override
		public E next() {
			if (nextKey == null && !hasNext()) throw new NoSuchElementException();

			lastReturned = entry;
			entry = entry.next;
			curKey = nextKey;
			nextKey = null;
			return curKey;
		}

		@Override
		public void remove() {
			if (lastReturned == null)
				throw new IllegalStateException();

			order.remove(lastReturned);
			set.remove(lastReturned);
			lastReturned = null;
			curKey = null;
		}
	}
}
