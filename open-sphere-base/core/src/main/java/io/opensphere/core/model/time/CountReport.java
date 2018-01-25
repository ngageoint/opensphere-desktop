package io.opensphere.core.model.time;

import java.util.Collections;
import java.util.List;

/**
 * The Class CountReport.
 */
public class CountReport
{
    /** The Total count. */
    private final int myTotalCount;

    /** The Max bin count. */
    private final int myMaxBinCount;

    /** The Min bin count. */
    private final int myMinBinCount;

    /** The Data. */
    private final List<TimeAndCount> myData;

    /**
     * Instantiates a new count report.
     */
    public CountReport()
    {
        myTotalCount = 0;
        myMaxBinCount = 0;
        myMinBinCount = 0;
        myData = Collections.<TimeAndCount>emptyList();
    }

    /**
     * Instantiates a new count report.
     *
     * @param totalCount the total count
     * @param minBinCount the min bin count
     * @param maxBinCount the max bin count
     * @param data the data
     */
    public CountReport(int totalCount, int minBinCount, int maxBinCount, List<TimeAndCount> data)
    {
        myTotalCount = totalCount;
        myMinBinCount = minBinCount;
        myMaxBinCount = maxBinCount;
        myData = data;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public List<TimeAndCount> getData()
    {
        return myData;
    }

    /**
     * Gets the max bin count.
     *
     * @return the max bin count
     */
    public int getMaxBinCount()
    {
        return myMaxBinCount;
    }

    /**
     * Gets the min bin count.
     *
     * @return the min bin count
     */
    public int getMinBinCount()
    {
        return myMinBinCount;
    }

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount()
    {
        return myTotalCount;
    }
}
