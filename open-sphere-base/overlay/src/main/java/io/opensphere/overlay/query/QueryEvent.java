package io.opensphere.overlay.query;

import java.util.EventObject;

/**
 * A simple event used to describe a query operation.
 */
public class QueryEvent extends EventObject
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = -4269450808816043941L;

    /**
     * Creates a new query event.
     * 
     * @param source the source that fired the event.
     */
    public QueryEvent(Object source)
    {
        super(source);
    }
}
