package io.opensphere.mantle.data;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;

/**
 * The Class AbstractDataGroupInfoChangeEvent.
 */
public abstract class AbstractDataGroupInfoChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The data group info associated with the event. */
    private final DataGroupInfo myDataGroupInfo;

    /** The source. */
    private final Object mySource;

    /**
     * Instantiates a new abstract data group info change event.
     *
     * @param dgi the dgi
     * @param source - the source of the event
     */
    public AbstractDataGroupInfoChangeEvent(DataGroupInfo dgi, Object source)
    {
        myDataGroupInfo = dgi;
        mySource = source;
    }

    /**
     * Gets the {@link DataGroupInfo} associated with this event.
     *
     * @return the group.
     */
    public DataGroupInfo getGroup()
    {
        return myDataGroupInfo;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append("DGI : ").append(myDataGroupInfo.getDisplayName()).append(" - ").append(myDataGroupInfo.getId())
                .append(" Event: ").append(this.getClass().getName());
        return sb.toString();
    }
}
