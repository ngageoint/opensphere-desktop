package io.opensphere.core.util.collections;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * A {@link BlockingQueue} that will generate objects using its {@link Factory}
 * if it is empty when {@link #poll()}, {@link #poll(long, TimeUnit)}, or
 * {@link #take()} is called, until its maximum child count has been reached.
 *
 * @param <E> The type of the values in the queue.
 */
public class LimitedFertilityBlockingQueue<E> extends LinkedBlockingQueue<E>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The count of the number of children produced. */
    private int myCount;

    /** The factory that creates the children. */
    private final Factory<? extends E> myFactory;

    /** Lock used to synchronize calls to {@link #checkSpawn(Factory)}. */
    private final Lock myLock = new ReentrantLock();

    /** The maximum number of children to be created. */
    private final int myMaxChildren;

    /**
     * Construct the queue.
     *
     * @param maxChildren The maximum number of children to be created.
     * @param factory The factory that creates the children.
     */
    public LimitedFertilityBlockingQueue(int maxChildren, Factory<? extends E> factory)
    {
        myMaxChildren = maxChildren;
        myFactory = factory;
    }

    /**
     * Construct the queue.
     *
     * @param capacity The maximum number of objects in the queue.
     * @param maxChildren The maximum number of children to be created.
     * @param factory The factory that creates the children.
     */
    public LimitedFertilityBlockingQueue(int capacity, int maxChildren, Factory<? extends E> factory)
    {
        super(capacity);
        myMaxChildren = maxChildren;
        myFactory = factory;
    }

    /**
     * Get the number of children that have been produced.
     *
     * @return The count of children.
     */
    public int getCount()
    {
        return myCount;
    }

    /**
     * Get the maximum number of children to be created.
     *
     * @return The max.
     */
    public int getMaxChildren()
    {
        return myMaxChildren;
    }

    @Override
    public E poll()
    {
        return poll(myFactory);
    }

    /**
     * Retrieves and removes the head of this queue, creating an element using
     * the given factory if the fertility limit has not been reached, or returns
     * <tt>null</tt> if this queue is empty.
     *
     * @param factory The factory to use to create an element if the maximum has
     *            not been reached.
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    public E poll(Factory<? extends E> factory)
    {
        return poll(factory, (Consumer<E>)null);
    }

    /**
     * Retrieves and removes the head of this queue, creating an element using
     * the given factory if the fertility limit has not been reached, or returns
     * <tt>null</tt> if this queue is empty.
     *
     * @param factory The factory to use to create an element if the maximum has
     *            not been reached.
     * @param adapter Optional adapter to be applied to objects returned from
     *            the queue that are not newly created.
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    public E poll(Factory<? extends E> factory, @Nullable Consumer<? super E> adapter)
    {
        E result;
        boolean spawned;
        myLock.lock();
        try
        {
            spawned = checkSpawn(factory);
            result = super.poll();
        }
        finally
        {
            myLock.unlock();
        }
        if (!spawned && adapter != null && result != null)
        {
            adapter.accept(result);
        }
        return result;
    }

    /**
     * Retrieves and removes the head of this queue, waiting up to the specified
     * wait time if necessary for an element to become available.
     *
     * @param factory The factory to use to create an element if the maximum has
     *            not been reached.
     * @param timeout how long to wait before giving up, in units of
     *            <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     *            <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the specified waiting
     *         time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     */
    public E poll(Factory<? extends E> factory, long timeout, TimeUnit unit) throws InterruptedException
    {
        return poll(factory, (Consumer<E>)null, timeout, unit);
    }

    /**
     * Retrieves and removes the head of this queue, waiting up to the specified
     * wait time if necessary for an element to become available.
     *
     * @param factory The factory to use to create an element if the maximum has
     *            not been reached.
     * @param adapter Optional adapter to be applied to objects returned from
     *            the queue that are not newly created.
     * @param timeout how long to wait before giving up, in units of
     *            <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     *            <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the specified waiting
     *         time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     */
    public E poll(Factory<? extends E> factory, @Nullable Consumer<? super E> adapter, long timeout, TimeUnit unit)
        throws InterruptedException
    {
        E result;
        boolean spawned;
        myLock.lock();
        try
        {
            spawned = checkSpawn(factory);
            result = super.poll();
        }
        finally
        {
            myLock.unlock();
        }
        if (result == null)
        {
            result = super.poll(timeout, unit);
        }
        if (!spawned && adapter != null && result != null)
        {
            adapter.accept(result);
        }
        return result;
    }

    /**
     * Retrieves and removes the head of this queue, waiting up to the specified
     * wait time if necessary for an element to become available.
     *
     * @param timeout how long to wait before giving up, in units of
     *            <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     *            <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the specified waiting
     *         time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException
    {
        return poll(myFactory, timeout, unit);
    }

    @Override
    public E take() throws InterruptedException
    {
        return take(myFactory);
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until
     * an element becomes available.
     *
     * @param factory The factory to use to create an element if the maximum has
     *            not been reached.
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    public E take(Factory<? extends E> factory) throws InterruptedException
    {
        return take(factory, (Consumer<E>)null);
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until
     * an element becomes available.
     *
     * @param factory The factory to use to create an element if the maximum has
     *            not been reached.
     * @param adapter Optional adapter to be applied to objects returned from
     *            the queue that are not newly created.
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    public E take(Factory<? extends E> factory, @Nullable Consumer<? super E> adapter) throws InterruptedException
    {
        E result;
        boolean spawned;
        myLock.lock();
        try
        {
            spawned = checkSpawn(factory);
            result = super.poll();
        }
        finally
        {
            myLock.unlock();
        }
        if (result == null)
        {
            result = super.take();
        }
        if (!spawned && adapter != null && result != null)
        {
            adapter.accept(result);
        }
        return result;
    }

    /**
     * Create a new object and add it to the queue if the queue is empty and the
     * maximum child count has not been reached.
     *
     * @param factory The factory to use to create the new object.
     * @return {@code true} if a new object was created.
     */
    private boolean checkSpawn(Factory<? extends E> factory)
    {
        if (isEmpty() && myCount < myMaxChildren && factory != null)
        {
            E obj = factory.create();
            if (obj != null)
            {
                ++myCount;
                offer(obj);
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Interface for the factory that provides the values in the queue.
     *
     * @param <E> The type of the values in the queue.
     */
    @FunctionalInterface
    public interface Factory<E> extends Serializable
    {
        /**
         * Create a new value for the map.
         *
         * @return The value.
         */
        E create();
    }
}
