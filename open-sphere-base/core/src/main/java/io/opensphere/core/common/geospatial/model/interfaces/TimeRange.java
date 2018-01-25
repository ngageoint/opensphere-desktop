package io.opensphere.core.common.geospatial.model.interfaces;

import java.util.Date;

public interface TimeRange
{
    /**
     *
     * @return
     */
    public Date getStartDate();

    /**
     * Convience methods if point just has one date to it Same as getStartDate
     *
     * @return startDate
     */
    public Date getDate();

    /**
     *
     * @return
     */
    public Date getEndDate();

    /**
     *
     * @param endDate
     */
    public void setEndDate(Date endDate);

}
