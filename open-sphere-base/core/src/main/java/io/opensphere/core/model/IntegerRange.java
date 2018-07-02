package io.opensphere.core.model;

import net.jcip.annotations.Immutable;

/**
 * Integer Range.
 */
@Immutable
public class IntegerRange extends Number implements Range<Integer>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The maximum. */
    private final int myMax;

    /** The minimum. */
    private final int myMin;

    /**
     * Constructor.
     *
     * @param val The value
     */
    public IntegerRange(int val)
    {
        super();
        myMin = val;
        myMax = val;
    }

    /**
     * Constructor.
     *
     * @param min The minimum
     * @param max The maximum
     */
    public IntegerRange(int min, int max)
    {
        super();
        myMin = min;
        myMax = max;
    }

    /**
     * Compare this range with another range, favoring the max values.
     *
     * @param o The other range.
     * @return -1, 0, or 1
     * @see Comparable#compareTo(Object)
     */
    public int compareMaxThenMin(Range<Integer> o)
    {
        int val = Integer.compare(myMax, o.getMax().intValue());
        if (val == 0)
        {
            val = Integer.compare(myMin, o.getMin().intValue());
        }
        return val;
    }

    /**
     * Compare this range with another range, favoring the min values.
     *
     * @param o The other range.
     * @return -1, 0, or 1
     * @see Comparable#compareTo(Object)
     */
    public int compareMinThenMax(Range<Integer> o)
    {
        int val = Integer.compare(myMin, o.getMin().intValue());
        if (val == 0)
        {
            val = Integer.compare(myMax, o.getMax().intValue());
        }
        return val;
    }

    /**
     * Returns whether the value is contained in the range.
     *
     * @param value the value
     * @return whether it's in the range
     */
    public boolean contains(int value)
    {
        return myMin <= value && value <= myMax;
    }

    @Override
    public double doubleValue()
    {
        return (myMax + myMin) / 2.;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        IntegerRange other = (IntegerRange)obj;
        return myMax == other.myMax && myMin == other.myMin;
    }

    @Override
    public float floatValue()
    {
        return (float)doubleValue();
    }

    @Override
    public Integer getMax()
    {
        return Integer.valueOf(myMax);
    }

    @Override
    public Integer getMin()
    {
        return Integer.valueOf(myMin);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myMax;
        result = prime * result + myMin;
        return result;
    }

    @Override
    public int intValue()
    {
        return (int)doubleValue();
    }

    @Override
    public long longValue()
    {
        return (long)doubleValue();
    }

    @Override
    public String toString()
    {
        String s;
        if (myMin == myMax)
        {
            s = String.valueOf(myMin);
        }
        else
        {
            StringBuilder sb = new StringBuilder(32);
            sb.append(myMin);
            sb.append(" to ");
            sb.append(myMax);
            s = sb.toString();
        }
        return s;
    }

    /**
     * Gets the max value.
     *
     * @return the max value
     */
    public int getMaxValue()
    {
        return myMax;
    }

    /**
     * Gets the min value.
     *
     * @return the min value
     */
    public int getMinValue()
    {
        return myMin;
    }
}
