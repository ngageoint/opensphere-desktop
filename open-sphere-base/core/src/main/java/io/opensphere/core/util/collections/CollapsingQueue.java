package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A LinkedBlockingQueue that clears itself before any add in order to keep no
 * more than one item in the queue.
 *
 * @param <E> the type which is to be put in the queue.
 */
@javax.annotation.concurrent.ThreadSafe
public class CollapsingQueue<E> extends LinkedBlockingQueue<E>
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public boolean add(E e)
    {
        clear();
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        clear();
        if (c != null)
        {
            Iterator<? extends E> iterator = c.iterator();
            if (iterator.hasNext())
            {
                return super.add(iterator.next());
            }
        }
        return false;
    }

    @Override
    public boolean offer(E e)
    {
        clear();
        return super.offer(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException
    {
        clear();
        return super.offer(e, timeout, unit);
    }

    @Override
    public void put(E e) throws InterruptedException
    {
        clear();
        super.put(e);
    }
}
