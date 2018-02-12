package com.rwtema.extrautils2.utils.datastructures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class InstancePool<T> {
    @Nullable
    private volatile WeakReference<Queue<T>> queue;

    @Nonnull
    public T take() {
        T t = getQueue().poll();
        return t == null ? this.create() : t;
    }

    public void recycle(T t) {
        clear(t);
        getQueue().offer(t);
    }

    protected void clear(T t) {

    }

    @Nonnull
    private Queue<T> getQueue() {
        WeakReference<Queue<T>> currentQueueRef = this.queue;
        Queue<T> queue;
        if (currentQueueRef != null) {
            queue = currentQueueRef.get();
            if (queue != null) {
                return queue;
            }
        }

        queue = new ConcurrentLinkedQueue<>();
        this.queue = new WeakReference<>(queue);
        return queue;
    }

    @Nonnull
    protected abstract T create();
}
