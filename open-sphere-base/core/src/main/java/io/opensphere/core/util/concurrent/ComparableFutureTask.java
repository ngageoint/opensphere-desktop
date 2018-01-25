package io.opensphere.core.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * A {@link FutureTask} that is also {@link Comparable}.
 *
 * @param <V> The result type.
 */
public class ComparableFutureTask<V> extends FutureTask<V> implements Comparable<ComparableFutureTask<V>>
{
    /** The comparable. */
    private final Comparable<Object> myComparable;

    /**
     * Constructor.
     *
     * @param callable The wrapped callable.
     * @see FutureTask#FutureTask(Callable)
     */
    @SuppressWarnings("unchecked")
    public ComparableFutureTask(Callable<V> callable)
    {
        super(callable);
        if (callable instanceof Comparable)
        {
            myComparable = (Comparable<Object>)callable;
        }
        else
        {
            throw new IllegalArgumentException("Callable is not Comparable.");
        }
    }

    /**
     * Constructor.
     *
     * @param runnable The wrapped runnable.
     * @param result The result to return upon successful completion.
     * @see FutureTask#FutureTask(Runnable, Object)
     */
    @SuppressWarnings("unchecked")
    public ComparableFutureTask(Runnable runnable, V result)
    {
        super(runnable, result);
        if (runnable instanceof Comparable)
        {
            myComparable = (Comparable<Object>)runnable;
        }
        else
        {
            throw new IllegalArgumentException("Runnable is not Comparable.");
        }
    }

    @Override
    public int compareTo(ComparableFutureTask<V> o)
    {
        return myComparable.compareTo(o.myComparable);
    }

    @Override
    @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    @Override
    @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
    public int hashCode()
    {
        return super.hashCode();
    }
}
