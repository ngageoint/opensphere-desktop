package io.opensphere.core.util.swing;

import java.util.Date;

/**
 * The Class SlavedDateTimeEvent.
 */
public class SlavedDateTimeEvent
{
    /** The Begin date. */
    private final Date myBeginDate;

    /** The End date. */
    private final Date myEndDate;

    /** The Owner. */
    private final Object myOwner;

    /**
     * Instantiates a new slaved date time event.
     *
     * @param owner the owner
     * @param begin the begin
     * @param end the end
     */
    public SlavedDateTimeEvent(Object owner, Date begin, Date end)
    {
        myOwner = owner;
        myBeginDate = new Date(begin.getTime());
        myEndDate = new Date(end.getTime());
    }

    /**
     * Gets the begin date.
     *
     * @return the begin date
     */
    public Date getBeginDate()
    {
        return new Date(myBeginDate.getTime());
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public Date getEndDate()
    {
        return new Date(myEndDate.getTime());
    }

    /**
     * Gets the owner.
     *
     * @return the owner
     */
    public Object getOwner()
    {
        return myOwner;
    }
}
