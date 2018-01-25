package io.opensphere.core.common.geospatial.model;

import java.util.Date;

import io.opensphere.core.common.geospatial.model.interfaces.TimeRange;

/**
 * A simple concrete implementation of {@link TimeRange}
 */
public class SimpleTimeRange implements TimeRange
{
    Date startDate;

    Date endDate;

    public SimpleTimeRange(Date beginDate, Date endDate)
    {
        super();
        startDate = beginDate;
        this.endDate = endDate;
    }

    @Override
    public Date getEndDate()
    {
        return endDate;
    }

    @Override
    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    @Override
    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date in)
    {
        startDate = in;
    }

    @Override
    public Date getDate()
    {
        return startDate;
    }

    @Override
    public String toString()
    {
        return "SimpleTimeRange [startDate=" + startDate + ", endDate=" + endDate + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (endDate == null ? 0 : endDate.hashCode());
        result = prime * result + (startDate == null ? 0 : startDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        SimpleTimeRange other = (SimpleTimeRange)obj;
        if (endDate == null)
        {
            if (other.endDate != null)
            {
                return false;
            }
        }
        else if (!endDate.equals(other.endDate))
        {
            return false;
        }
        if (startDate == null)
        {
            if (other.startDate != null)
            {
                return false;
            }
        }
        else if (!startDate.equals(other.startDate))
        {
            return false;
        }
        return true;
    }

}
