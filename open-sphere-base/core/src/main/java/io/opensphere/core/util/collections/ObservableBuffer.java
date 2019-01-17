package io.opensphere.core.util.collections;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import javafx.collections.ObservableListBase;

/**
 * A fixed sized container in which data is stored in order of insertion, with
 * the newest element at the end of the container, and the oldest element at the
 * beginning of the container. This container is of a fixed capacity, but will
 * continue to accept new elements regardless of whether it is full or not. If
 * the size is equal to the capacity and a new element is offered to the
 * container, the oldest item in the buffer is pushed out before the new item is
 * added.
 *
 * @param <E> The type of the elements contained in the buffer.
 */
public class ObservableBuffer<E> extends ObservableListBase<E> implements Queue<E>
{
    /** The queue backing this collection. */
    private final LinkedList<E> myQueue;

    /** The number of elements that can be stored in the buffer. */
    private final int myCapacity;

    /**
     * Creates a new buffer storing elements in a fixed sized container. This is
     * a self-evicting queue such that when the queue reaches this capacity, the
     * oldest element in the queue is automatically removed to make room for a
     * new entry.
     *
     * @param capacity the number of elements that can be stored in the buffer
     *            before the oldest item is pushed out.
     */
    public ObservableBuffer(final int capacity)
    {
        myCapacity = capacity;
        myQueue = new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Queue#offer(java.lang.Object)
     */
    @Override
    public boolean offer(final E element)
    {
        boolean changed = false;
        synchronized (myQueue)
        {
            beginChange();
            while (remainingCapacity() < 1)
            {
                final E removedElement = myQueue.removeFirst();
                if (removedElement != null)
                {
                    nextRemove(0, removedElement);
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
     * Calculates and returns the amount of space left in the queue.
     *
     * @return the amount of space left in the queue.
     */
    protected int remainingCapacity()
    {
        return myCapacity - myQueue.size();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractList#add(java.lang.Object)
     */
    @Override
    public boolean add(final E e)
    {
        if (remainingCapacity() == 0)
        {
            throw new IllegalStateException("Queue full");
        }

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
                removedItem = myQueue.removeLast();
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
                removedItem = myQueue.pollLast();
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
        if (myQueue.isEmpty())
        {
            throw new NoSuchElementException();
        }
        return myQueue.peekLast();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Queue#peek()
     */
    @Override
    public E peek()
    {
        return myQueue.peekLast();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public E get(final int index)
    {
        if (index < 0 || index >= myQueue.size())
        {
            throw new IndexOutOfBoundsException(0);
        }
        E item = null;
        synchronized (myQueue)
        {
            final Iterator<E> iterator = myQueue.iterator();
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

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractList#clear()
     */
    @Override
    public void clear()
    {
        if (hasListeners())
        {
            beginChange();
            nextRemove(0, this);
        }
        myQueue.clear();
        ++modCount;
        if (hasListeners())
        {
            endChange();
        }
    }
}
