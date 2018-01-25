package io.opensphere.analysis.base.model;

import io.opensphere.analysis.binning.criteria.TimeBinType;

/** Bin type enum. */
public enum BinType
{
    /** Range. */
    RANGE("Range"),

    /** Unique. */
    UNIQUE("Unique"),

    /** Periodic, primarily for time data. */
    PERIOD("Period");

    /** The display text. */
    private final String myText;

    /**
     * Gets the bin type for the given TimeBinType.
     *
     * @param timeBinType the TimeBinType
     * @return the bin type
     */
    public static BinType toBinType(TimeBinType timeBinType)
    {
        return timeBinType == TimeBinType.UNIQUE ? UNIQUE : timeBinType.isPeriod() ? PERIOD : RANGE;
    }

    /**
     * Constructor.
     *
     * @param text The display text
     */
    private BinType(String text)
    {
        myText = text;
    }

    @Override
    public String toString()
    {
        return myText;
    }
}
