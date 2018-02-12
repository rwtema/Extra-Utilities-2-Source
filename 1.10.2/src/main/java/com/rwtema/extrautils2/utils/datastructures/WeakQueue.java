package com.rwtema.extrautils2.utils.datastructures;

import gnu.trove.list.TLinkable;
import gnu.trove.list.linked.TLinkedList;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import javax.annotation.Nonnull;

public class WeakQueue<T> extends AbstractQueue<T> {
	final ReferenceQueue<T> q = new ReferenceQueue<>();
	TLinkedList<Node> list = new TLinkedList<Node>();

	@Override
	public int size() {
		expungeStaleEntries();
		return list.size();
	}

	@Override
	public boolean offer(T t) {
		expungeStaleEntries();
		return list.add(new Node(t));
	}

	@Override
	public T poll() {
		expungeStaleEntries();
		return list.isEmpty() ? null : list.remove(0).get();
	}

	@Override
	public T peek() {
		expungeStaleEntries();
		return list.isEmpty() ? null : list.get(0).get();
	}

	@Override
	public void clear() {
		list.clear();
	}

	private void expungeStaleEntries() {
		for (Object x; (x = q.poll()) != null; ) {
			synchronized (q) {
				@SuppressWarnings("unchecked")
				Node e = (Node) x;
				list.remove(e);
			}
		}
	}

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		expungeStaleEntries();
		return new NodeIterator();
	}

	private final class Node extends WeakReference<T> implements TLinkable<Node> {
		private volatile Node next;
		private volatile Node prev;

		public Node(T referent) {
			super(referent, q);
		}

		@Override
		public Node getNext() {
			return next;
		}

		@Override
		public void setNext(Node linkable) {
			next = linkable;
		}

		@Override
		public Node getPrevious() {
			return prev;
		}

		@Override
		public void setPrevious(Node linkable) {
			prev = linkable;
		}
	}

	private class NodeIterator implements Iterator<T> {
		public NodeIterator() {
			this.iterator = new ArrayList<>(list).iterator();
		}

		Iterator<Node> iterator;
		Node curNode;
		T next;

		@Override
		public void remove() {
			list.remove(curNode);
		}

		@Override
		public T next() {
			return next;
		}

		@Override
		public boolean hasNext() {
			do {
				if (!iterator.hasNext()) {
					next = null;
					return false;
				}

				curNode = iterator.next();
				next = curNode.get();
			} while (next == null);
			return true;
		}
	}
}
