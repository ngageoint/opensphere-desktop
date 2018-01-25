package io.opensphere.core.model;

import java.text.DecimalFormat;

import io.opensphere.core.util.Utilities;

/**
 * Double Range.
 */
public class DoubleRange extends Number implements Range<Double>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Format for display. */
    private static final DecimalFormat FORMAT = new DecimalFormat("0.0#####");

    /** The minimum. */
    private final double myMin;

    /** The maximum. */
    private final double myMax;

    /**
     * Constructor.
     *
     * @param val The value
     */
    public DoubleRange(double val)
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
    public DoubleRange(double min, double max)
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
    public int compareMaxThenMin(Range<Double> o)
    {
        int val = Double.compare(myMax, o.getMax().doubleValue());
        if (val == 0)
        {
            val = Double.compare(myMin, o.getMin().doubleValue());
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
    public int compareMinThenMax(Range<Double> o)
    {
        int val = Double.compare(myMin, o.getMin().doubleValue());
        if (val == 0)
        {
            val = Double.compare(myMax, o.getMax().doubleValue());
        }
        return val;
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
        DoubleRange other = (DoubleRange)obj;
        return Utilities.equalsOrBothNaN(myMax, other.myMax) && Utilities.equalsOrBothNaN(myMin, other.myMin);
    }

    @Override
    public float floatValue()
    {
        return (float)doubleValue();
    }

    @Override
    public Double getMax()
    {
        return Double.valueOf(myMax);
    }

    @Override
    public Double getMin()
    {
        return Double.valueOf(myMin);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myMax);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myMin);
        result = prime * result + (int)(temp ^ temp >>> 32);
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
        if (Utilities.equalsOrBothNaN(myMin, myMax))
        {
            s = String.valueOf(myMin);
        }
        else
        {
            StringBuilder sb = new StringBuilder(32);
            sb.append(FORMAT.format(myMin));
            sb.append(" to ");
            sb.append(FORMAT.format(myMax));
            s = sb.toString();
        }
        return s;
    }
}
