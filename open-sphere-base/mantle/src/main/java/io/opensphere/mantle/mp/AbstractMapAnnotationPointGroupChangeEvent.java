package io.opensphere.mantle.mp;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The Class AbstractDataGroupInfoChangeEvent.
 */
public abstract class AbstractMapAnnotationPointGroupChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The MapAnnotationPointGroup associated with the event. */
    private final MutableMapAnnotationPointGroup myGroup;

    /** The source. */
    private final Object mySource;

    /**
     * Instantiates a new abstract map annotation point group change event.
     *
     * @param group the group
     * @param source the source
     */
    public AbstractMapAnnotationPointGroupChangeEvent(MutableMapAnnotationPointGroup group, Object source)
    {
        myGroup = group;
        mySource = source;
    }

    /**
     * Gets the {@link DataGroupInfo} associated with this event.
     *
     * @return the group.
     */
    public MutableMapAnnotationPointGroup getGroup()
    {
        return myGroup;
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
        sb.append("DGI : ").append(myGroup.getName()).append(" -  Event: ").append(this.getClass().getName());
        return sb.toString();
    }
}
