package io.opensphere.core.common.util;

/**
 * A tuple is a loosely structured bit of data, used for creating lightweight,
 * immutable "scratch" data structures.
 *
 * @param <T1> the type of the first element in the tuple.
 * @param <T2> the type of the second element in the tuple.
 */
public class Tuple2<T1, T2>
{
    /** First tuple element. */
    private final T1 t1;

    /** Second tuple element. */
    private final T2 t2;

    /**
     * Construct a new Tuple2 instance.
     *
     * @param t1 the value for the first element in the tuple.
     * @param t2 the value for the second element in the tuple.
     */
    public Tuple2(final T1 t1, final T2 t2)
    {
        this.t1 = t1;
        this.t2 = t2;
    }

    /**
     * @return value of the first tuple element.
     */
    public T1 getT1()
    {
        return t1;
    }

    /**
     * @return value of the second tuple element.
     */
    public T2 getT2()
    {
        return t2;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof Tuple2<?, ?>))
        {
            return false;
        }

        Tuple2<?, ?> that = (Tuple2<?, ?>)obj;

        if (this.getT1() == null ^ that.getT1() == null)
        {
            return false;
        }
        if (this.getT2() == null ^ that.getT2() == null)
        {
            return false;
        }

        if (this.getT1() != null && that.getT1() != null)
        {
            if (!this.getT1().equals(that.getT1()))
            {
                return false;
            }
        }
        if (this.getT2() != null && that.getT2() != null)
        {
            if (!this.getT2().equals(that.getT2()))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 17;

        hash = 31 * hash + (null == this.getT1() ? 0 : this.getT1().hashCode());
        hash = 31 * hash + (null == this.getT2() ? 0 : this.getT2().hashCode());

        return hash;
    }

    @Override
    public String toString()
    {
        return "{" + getT1() + "," + getT2() + "}";
    }
}
