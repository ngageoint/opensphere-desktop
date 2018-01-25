package io.opensphere.myplaces.specific.tracks;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Contains the data necessary to create a new segment.
 *
 */
public class NewSegmentData
{
    /**
     * The cumulative distance.
     */
    private double myCumulativeDistanceKM;

    /**
     * The data type info.
     */
    private DataTypeInfo myDti;

    /**
     * The end position.
     */
    private LatLonAlt myEndPosition;

    /**
     * The end time.
     */
    private TimeSpan myEndTime;

    /**
     * The segment counter.
     */
    private int mySegmentCounter;

    /**
     * The segment length.
     */
    private double mySegmentLengthKM;

    /**
     * The start position.
     */
    private LatLonAlt myStartPosition;

    /**
     * The start time.
     */
    private TimeSpan myStartTime;

    /**
     * Gets the cumulative distance.
     *
     * @return The distance.
     */
    public double getCumulativeDistanceKM()
    {
        return myCumulativeDistanceKM;
    }

    /**
     * Gets the data type info.
     *
     * @return The data type info.
     */
    public DataTypeInfo getDti()
    {
        return myDti;
    }

    /**
     * Gets the end position.
     *
     * @return The end position.
     */
    public LatLonAlt getEndPosition()
    {
        return myEndPosition;
    }

    /**
     * Gets the end time.
     *
     * @return The end time.
     */
    public TimeSpan getEndTime()
    {
        return myEndTime;
    }

    /**
     * Gets the segment counter.
     *
     * @return The segment counter.
     */
    public int getSegmentCounter()
    {
        return mySegmentCounter;
    }

    /**
     * Gets the segment length.
     *
     * @return The segment length.
     */
    public double getSegmentLengthKM()
    {
        return mySegmentLengthKM;
    }

    /**
     * Gets the start position.
     *
     * @return The start position.
     */
    public LatLonAlt getStartPosition()
    {
        return myStartPosition;
    }

    /**
     * Gets the start time.
     *
     * @return The start time.
     */
    public TimeSpan getStartTime()
    {
        return myStartTime;
    }

    /**
     * Sets the distance.
     *
     * @param cumulativeDistanceKM The distance.
     */
    public void setCumulativeDistanceKM(double cumulativeDistanceKM)
    {
        myCumulativeDistanceKM = cumulativeDistanceKM;
    }

    /**
     * Sets the data type info.
     *
     * @param dti The data type info.
     */
    public void setDti(DataTypeInfo dti)
    {
        myDti = dti;
    }

    /**
     * Sets the end position.
     *
     * @param endPosition The end position.
     */
    public void setEndPosition(LatLonAlt endPosition)
    {
        myEndPosition = endPosition;
    }

    /**
     * Sets the end time.
     *
     * @param endTime The end time.
     */
    public void setEndTime(TimeSpan endTime)
    {
        myEndTime = endTime;
    }

    /**
     * Sets the segment counter.
     *
     * @param segmentCounter The segment counter.
     */
    public void setSegmentCounter(int segmentCounter)
    {
        mySegmentCounter = segmentCounter;
    }

    /**
     * Sets the segment length.
     *
     * @param segmentLengthKM The segment length.
     */
    public void setSegmentLengthKM(double segmentLengthKM)
    {
        mySegmentLengthKM = segmentLengthKM;
    }

    /**
     * Sets the start position.
     *
     * @param startPosition The start position.
     */
    public void setStartPosition(LatLonAlt startPosition)
    {
        myStartPosition = startPosition;
    }

    /**
     * Sets the start time.
     *
     * @param startTime The start time.
     */
    public void setStartTime(TimeSpan startTime)
    {
        myStartTime = startTime;
    }
}
