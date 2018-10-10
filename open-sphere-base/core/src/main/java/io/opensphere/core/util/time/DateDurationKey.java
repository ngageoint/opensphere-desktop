package io.opensphere.core.util.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Months;

/**
 * This is a simple DateDurationKey object.
 *
 * Do NOT add layer metadata to this object Instead, use TiledDateDurationKey
 */

/**
 * The Class DateDurationKey.
 */
//TODO What is this class for? Is this a key to look up something else?
public class DateDurationKey implements Comparable<DateDurationKey>
{
    /** The start date. */
    private Date myStartDate;

    /** The end date. */
    private Date myEndDate;

    /** The duration. */
    private Duration myDuration;

    /** The children. */
    private List<DateDurationKey> myChildren;

    /** The has data. */
    private boolean myHasData;

    /** The has children. */
    private boolean myHasChildren;

    /** The my name. */
    private String myName;

    // Do lazy caching of this object's hash value, since a large
    // number of these objects appear in hash sets.
    /** The hash calculated. */
    private boolean myHashCalculated;

    /** The my hash. */
    private int myHash;

    /** The my sort index. */
    private int mySortIndex;

    /** The Is expanded. */
    private boolean myExpanded;

    /**
     * Instantiates a new date duration key.
     */
    public DateDurationKey()
    {
        super();
        myChildren = new ArrayList<>();
        myStartDate = null;
        myEndDate = null;
        myDuration = Months.ONE;
    }

    /**
     * Instantiates a new date duration key.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param duration the duration
     */
    public DateDurationKey(Date startDate, Date endDate, Duration duration)
    {
        myChildren = new ArrayList<>();
        // Don't want to store or pass back mutable references to these Dates
        // clone() is the Java SDK's recommended way to get date copies.
        // Enums are pass by value anyway.
        myStartDate = (Date)startDate.clone();
        myEndDate = (Date)endDate.clone();

        myDuration = duration;
    }

    /**
     * Instantiates a new date duration key.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param duration the duration
     * @param children the children
     */
    public DateDurationKey(Date startDate, Date endDate, Duration duration, Collection<? extends DateDurationKey> children)
    {
        myChildren = new ArrayList<>();

        myChildren.addAll(children);
        Collections.sort(myChildren);

        // Don't want to store or pass back mutable references to these Dates
        // clone() is the Java SDK's recommended way to get date copies.
        // Enums are pass by value anyway.
        myStartDate = (Date)startDate.clone();
        myEndDate = (Date)endDate.clone();

        myDuration = duration;
    }

    @Override
    public int compareTo(DateDurationKey ddk)
    {
        final int equal = 0;
        int toReturn = 0;

        // Double thisIndex = new Double(mySortIndex);
        // Double inIndex = new Double(ddk.getMySortIndex());

        if (this == ddk)
        {
            toReturn = equal;
        }
        else
        {
            toReturn = myStartDate.compareTo(ddk.getStartDate());
            if (toReturn == 0)
            {
                toReturn = myEndDate.compareTo(ddk.getEndDate());
            }
            if (toReturn == 0)
            {
                toReturn = myDuration.compareTo(ddk.getDuration());
            }
        }
        return toReturn;
    }

    //    /**
    //     * Instantiates a new date duration key.
    //     *
    //     * @param startDate the start date
    //     * @param endDate the end date
    //     * @param aDurationInSeconds the a duration in seconds
    //     */
    //    public DateDurationKey(Date startDate, Date endDate, int aDurationInSeconds)
    //    {
    //        // Don't want to store or pass back mutable references to these Dates
    //        // clone() is the Java SDK's recommended way to get date copies.
    //        // Enums are pass by value anyway.
    //        myStartDate = (Date)startDate.clone();
    //        myEndDate = (Date)endDate.clone();
    //
    //        myDuration = Duration.CUSTOM;
    //        myCustomDurationInSeconds = aDurationInSeconds;
    //    }

    /**
     * Contains date.
     *
     * @param in the in
     * @return true, if successful
     */
    public boolean containsDate(Date in)
    {
        return in.getTime() < myEndDate.getTime() && in.getTime() >= myStartDate.getTime();
    }

    /**
     * Destroy.
     */
    public void destroy()
    {
        myStartDate = null;
        myEndDate = null;

        myDuration = null;
        if (myChildren != null)
        {
            for (DateDurationKey iter : myChildren)
            {
                iter.destroy();
                iter = null;
            }
            myChildren.clear();
            myChildren = null;
        }

        myHashCalculated = false;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof DateDurationKey)
        {
            DateDurationKey other = (DateDurationKey)o;
            return getDuration().equals(other.getDuration()) && getStartDate().getTime() == other.getStartDate().getTime();
        }
        return false;
    }

    /**
     * Gets the as time span.
     *
     * @return the as time span
     */
    public TimeSpan getAsTimeSpan()
    {
        return TimeSpan.get(myStartDate, myEndDate);
    }

    /**
     * Gets the my children.
     *
     * @return the my children
     */
    public List<DateDurationKey> getChildren()
    {
        return myChildren;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public Duration getDuration()
    {
        return myDuration;
    }

    /**
     * Gets the end date.
     *
     * @return the endDate
     */
    public Date getEndDate()
    {
        return (Date)myEndDate.clone();
    }

    /**
     * Gets the my name.
     *
     * @return the my name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the my sort index.
     *
     * @return the my sort index
     */
    public double getSortIndex()
    {
        return mySortIndex;
    }

    /**
     * Gets the span.
     *
     * @return the span
     */
    public TimeSpan getSpan()
    {
        return TimeSpan.get((Date)myStartDate.clone(), (Date)myEndDate.clone());
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public Date getStartDate()
    {
        Date toReturn = null;
        if (myStartDate != null)
        {
            toReturn = (Date)myStartDate.clone();
        }
        return toReturn;
    }

    @Override
    public int hashCode()
    {
        if (!myHashCalculated)
        {
            final int prime = 31;
            myHash = 1;
            myHash = prime * myHash + (myDuration == null ? 0 : myDuration.hashCode());
            myHash = prime * myHash + (myEndDate == null ? 0 : myEndDate.hashCode());
            myHash = prime * myHash + (myStartDate == null ? 0 : myStartDate.hashCode());
            myHashCalculated = true;
        }
        return myHash;
    }

    /**
     * Checks if is destroyed.
     *
     * @return true, if is destroyed
     */
    public boolean isDestroyed()
    {
        return null == myEndDate || null == myStartDate;
    }

    /**
     * Get if this DDK has been expanded.
     *
     * @return The expanded flag.
     */
    public boolean isExpanded()
    {
        return myExpanded;
    }

    /**
     * Checks if is checks for children.
     *
     * @return true, if is checks for children
     */
    public boolean isHasChildren()
    {
        return myHasChildren;
    }

    /**
     * Checks if is checks for data.
     *
     * @return true, if is checks for data
     */
    public boolean isHasData()
    {
        return myHasData;
    }

    /**
     * Sets the duration.
     *
     * @param duration the new duration
     */
    public void setDuration(Duration duration)
    {
        myDuration = duration;
        myHashCalculated = false;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate)
    {
        myEndDate = (Date)endDate.clone();
        myHashCalculated = false;
    }

    /**
     * Set if this DDK has been expanded.
     *
     * @param expanded Flag indicating if the DDK has been expanded.
     */
    public void setExpanded(boolean expanded)
    {
        myExpanded = expanded;
    }

    /**
     * Sets the checks for children.
     *
     * @param hasChildren the new checks for children
     */
    public void setHasChildren(boolean hasChildren)
    {
        myHasChildren = hasChildren;
    }

    /**
     * Sets the checks for data.
     *
     * @param hasData the new checks for data
     */
    public void setHasData(boolean hasData)
    {
        myHasData = hasData;
    }

    /**
     * Sets the my name.
     *
     * @param s the new my name
     */
    public void setName(String s)
    {
        myName = s;
    }

    /**
     * Sets the my sort index.
     *
     * @param sortIndex the new my sort index
     */
    public void setSortIndex(int sortIndex)
    {
        mySortIndex = sortIndex;
    }

    /**
     * Sets the start date.
     *
     * @param date the new start date
     */
    public void setStartDate(Date date)
    {
        myStartDate = (Date)date.clone();
        myHashCalculated = false;
    }

    /**
     * To string.
     *
     * @return the string
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(myStartDate);
        sb.append(" - ");
        sb.append(myEndDate);
        sb.append(':');
        sb.append(myDuration);
        if (!myHasData)
        {
            sb.append("no ");
        }
        sb.append("data]  ");

        return sb.toString();
    }

    /**
     * To string little.
     *
     * @return the string
     */
    public String toStringLittle()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(myStartDate);
        sb.append(" - ");
        sb.append(myEndDate);
        sb.append(':');
        sb.append(myDuration);
        sb.append("]  ");

        return sb.toString();
    }
}
