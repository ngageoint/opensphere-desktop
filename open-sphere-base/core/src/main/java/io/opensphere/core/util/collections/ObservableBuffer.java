package io.opensphere.core.util.collections;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.collections.ObservableListBase;

/**
 * A fixed sized container in which data is stored in a first-in / first-out
 * manner (FIFO). This is a self-evicting queue such that when the queue reaches
 * this capacity, the oldest element in the queue is automatically removed to
 * make room for a new entry upon calling offer or add.
 *
 * @param <E> The type of the elements contained in the buffer.
 */
public class ObservableBuffer<E> extends ObservableListBase<E> implements Queue<E>
{
    /** The queue backing this collection. */
    private final LinkedBlockingQueue<E> myQueue;

    /**
     * Creates a new buffer storing elements in a fixed sized container. This is
     * a self-evicting queue such that when the queue reaches this capacity, the
     * oldest element in the queue is automatically removed to make room for a
     * new entry.
     *
     * @param capacity the number of elements that can be stored in the buffer
     *            before the oldest item is pushed out.
     */
    public ObservableBuffer(int capacity)
    {
        myQueue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Queue#offer(java.lang.Object)
     */
    @Override
    public boolean offer(E element)
    {
        boolean changed = false;
        synchronized (myQueue)
        {
            beginChange();
            while (myQueue.remainingCapacity() < 1)
            {
                try
                {
                    E removedElement = myQueue.take();
                    if (removedElement != null)
                    {
                        nextRemove(0, removedElement);
                    }
                }
                catch (InterruptedException e)
                {
                    /* safe to ignore */
                }
            }
            changed = myQueue.offer(element);
            if (changed)
            {
                nextAdd(myQueue.size() - 1, myQueue.size());
            }
            endChange();
        }
        return changed;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractList#add(java.lang.Object)
     */
    @Override
    public boolean add(E e)
    {
        boolean changed = false;
        synchronized (myQueue)
        {
            beginChange();
            try
            {
                if (myQueue.add(e))
                {
                    changed = true;
                    nextAdd(myQueue.size() - 1, myQueue.size());
                }
            }
            finally
            {
                endChange();
            }
        }
        return changed;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Queue#remove()
     */
    @Override
    public E remove()
    {
        E removedItem = null;
        synchronized (myQueue)
        {
            beginChange();
            try
            {
                removedItem = myQueue.remove();
                nextRemove(0, removedItem);
            }
            finally
            {
                endChange();
            }
        }
        return removedItem;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Queue#poll()
     */
    @Override
    public E poll()
    {
        E removedItem = null;
        synchronized (myQueue)
        {
            beginChange();
            try
            {
                removedItem = myQueue.poll();
                nextRemove(0, removedItem);
            }
            finally
            {
                endChange();
            }
        }
        return removedItem;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Queue#element()
     */
    @Override
    public E element()
    {
        return myQueue.element();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Queue#peek()
     */
    @Override
    public E peek()
    {
        return myQueue.peek();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public E get(int index)
    {
        E item = null;
        synchronized (myQueue)
        {
            Iterator<E> iterator = myQueue.iterator();
            for (int i = 0; i < index; i++)
            {
                iterator.next();
            }
            item = iterator.next();
        }
        return item;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size()
    {
        return myQueue.size();
    }
}
