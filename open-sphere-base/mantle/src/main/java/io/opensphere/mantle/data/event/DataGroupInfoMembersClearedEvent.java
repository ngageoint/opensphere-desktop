package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The Class DataGroupInfoMembersClearedEvent.
 */
public class DataGroupInfoMembersClearedEvent extends AbstractDataGroupInfoChangeEvent
{
    /**
     * Instantiates a new data group info members cleared event.
     *
     * @param dgi the dgi
     * @param source the source
     */
    public DataGroupInfoMembersClearedEvent(DataGroupInfo dgi, Object source)
    {
        super(dgi, source);
    }

    @Override
    public String getDescription()
    {
        return "DataGroupInfoMembersClearedEvent";
    }
}
