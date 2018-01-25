package io.opensphere.core.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.util.Utilities;

/**
 * An unlimited blocking queue with optimized latency for taking objects.
 *
 * @param <E> The type of object in the queue.
 */
public class UnlimitedLinkedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>
{
    /** A condition for notifying when something is added to the queue. */
    private final Condition myCondition;

    /** The head node. */
    private Node<E> myHeadNode;

    /** A lock for synchronizing enqueue and dequeue. */
    private final Lock myLock;

    /** The tail node. */
    private Node<E> myTailNode;

    /**
     * Constructor.
     */
    public UnlimitedLinkedBlockingQueue()
    {
        super();
        myLock = new ReentrantLock();
        myCondition = myLock.newCondition();
    }

    @Override
    public int drainTo(Collection<? super E> c)
    {
        Utilities.checkNull(c, "c");
        if (c == this)
        {
            throw new IllegalArgumentException();
        }
        Node<E> node;
        myLock.lock();
        try
        {
            node = myHeadNode;
            myHeadNode = myTailNode = null;
        }
        finally
        {
            myLock.unlock();
        }

        int count = 0;
        while (node != null)
        {
            ++count;
            c.add(node.getObject());
            node = node.getNext();
        }

        return count;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements)
    {
        Utilities.checkNull(c, "c");
        if (c == this)
        {
            throw new IllegalArgumentException();
        }
        int count = 0;
        myLock.lock();
        try
        {
            while (count < maxElements && myHeadNode != null)
            {
                ++count;
                c.add(myHeadNode.getObject());
                myHeadNode = myHeadNode.getNext();
            }
            if (myHeadNode == null)
            {
                myTailNode = null;
            }
        }
        finally
        {
            myLock.unlock();
        }

        return count;
    }

    @Override
    public boolean isEmpty()
    {
        myLock.lock();
        try
        {
            return myHeadNode == null;
        }
        finally
        {
            myLock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator()
    {
        return new Itr();
    }

    @Override
    public boolean offer(E e)
    {
        myLock.lock();
        try
        {
            if (myTailNode == null)
            {
                myHeadNode = myTailNode = new Node<>(e);
            }
            else
            {
                myTailNode = myTailNode.setNext(new Node<E>(e));
            }
            myCondition.signal();
        }
        finally
        {
            myLock.unlock();
        }
        return true;
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException
    {
        return offer(e);
    }

    @Override
    public E peek()
    {
        myLock.lock();
        try
        {
            return myHeadNode == null ? null : myHeadNode.getObject();
        }
        finally
        {
            myLock.unlock();
        }
    }

    @Override
    public E poll()
    {
        myLock.lock();
        try
        {
            if (myHeadNode == null)
            {
                return null;
            }
            else
            {
                Node<E> node = myHeadNode;
                myHeadNode = myHeadNode.getNext();
                if (myHeadNode == null)
                {
                    myTailNode = null;
                }
                return node.getObject();
            }
        }
        finally
        {
            myLock.unlock();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException
    {
        long nanos = unit.toNanos(timeout);
        while (nanos > 0)
        {
            myLock.lock();
            try
            {
                if (myHeadNode == null)
                {
                    nanos = myCondition.awaitNanos(nanos);
                }
                if (myHeadNode != null)
                {
                    Node<E> node = myHeadNode;
                    myHeadNode = myHeadNode.getNext();
                    if (myHeadNode == null)
                    {
                        myTailNode = null;
                    }
                    return node.getObject();
                }
            }
            finally
            {
                myLock.unlock();
            }
        }

        return null;
    }

    @Override
    public void put(E e) throws InterruptedException
    {
        offer(e);
    }

    @Override
    public int remainingCapacity()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean remove(Object o)
    {
        myLock.lock();
        try
        {
            Node<E> prev = null;
            Node<E> node = myHeadNode;
            while (node != null)
            {
                if (node.getObject().equals(o))
                {
                    if (prev == null)
                    {
                        myHeadNode = myHeadNode.getNext();
                    }
                    else
                    {
                        prev.setNext(node.getNext());
                    }
                    if (Utilities.sameInstance(node, myTailNode))
                    {
                        myTailNode = prev;
                    }
                    return true;
                }
                prev = node;
                node = node.getNext();
            }

            return false;
        }
        finally
        {
            myLock.unlock();
        }
    }

    @Override
    public int size()
    {
        int count = 0;
        myLock.lock();
        try
        {
            Node<E> node = myHeadNode;
            while (node != null)
            {
                ++count;
                node = node.getNext();
            }
        }
        finally
        {
            myLock.unlock();
        }

        return count;
    }

    @Override
    public E take() throws InterruptedException
    {
        for (;;)
        {
            myLock.lock();
            try
            {
                if (myHeadNode == null)
                {
                    myCondition.await();
                }
                if (myHeadNode != null)
                {
                    Node<E> node = myHeadNode;
                    myHeadNode = myHeadNode.getNext();
                    if (myHeadNode == null)
                    {
                        myTailNode = null;
                    }
                    return node.getObject();
                }
            }
            finally
            {
                myLock.unlock();
            }
        }
    }

    /** Iterator. */
    private final class Itr implements Iterator<E>
    {
        /** A lock for changing myNextNode. */
        private final Lock myIterLock = new ReentrantLock();

        /** The next node. */
        private Node<E> myNextNode;

        /** Constructor. */
        public Itr()
        {
            myLock.lock();
            try
            {
                myNextNode = myHeadNode;
            }
            finally
            {
                myLock.unlock();
            }
        }

        @Override
        public boolean hasNext()
        {
            myIterLock.lock();
            try
            {
                return myNextNode != null;
            }
            finally
            {
                myIterLock.unlock();
            }
        }

        @Override
        public E next()
        {
            Node<E> node;
            myIterLock.lock();
            try
            {
                node = myNextNode;
            }
            finally
            {
                myIterLock.unlock();
            }
            if (node == null)
            {
                throw new NoSuchElementException();
            }
            Node<E> nextNode;
            myLock.lock();
            try
            {
                nextNode = node.getNext();
            }
            finally
            {
                myLock.unlock();
            }
            myIterLock.lock();
            try
            {
                myNextNode = nextNode;
            }
            finally
            {
                myIterLock.unlock();
            }
            return node.getObject();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A node in the optimized blocking queue.
     *
     * @param <E> The type of object wrapped by the node.
     */
    private static class Node<E>
    {
        /** The next node. */
        private Node<E> myNext;

        /** The task. */
        private final E myObject;

        /**
         * Constructor.
         *
         * @param r The wrapped object.
         */
        public Node(E r)
        {
            myObject = r;
        }

        /**
         * Get the next node, which may be {@code null}.
         *
         * @return The next node.
         */
        public Node<E> getNext()
        {
            return myNext;
        }

        /**
         * Get the wrapped object.
         *
         * @return The wrapped object.
         */
        public E getObject()
        {
            return myObject;
        }

        /**
         * Set the next node.
         *
         * @param next The next node.
         * @return The next node.
         */
        public Node<E> setNext(Node<E> next)
        {
            return myNext = next;
        }
    }
}
