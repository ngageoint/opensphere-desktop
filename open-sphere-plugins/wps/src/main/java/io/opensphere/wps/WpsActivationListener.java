package io.opensphere.wps;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;

/**
 * An activation listener used to react to WPS servers being added.
 */
public class WpsActivationListener implements EventListener<ActiveDataGroupsChangedEvent>
{
    /**
     * The toolbox through which application interaction occurs.
     */
    private final Toolbox myToolbox;

    /**
     * Creates a new data type activation listener, configured with the supplied toolbox.
     *
     * @param pToolbox The toolbox through which application interaction occurs.
     */
    public WpsActivationListener(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
        myToolbox.getEventManager().subscribe(ActiveDataGroupsChangedEvent.class, this);
    }

    /**
     * Prepares the activation listener for termination, un-subscribing it from event propagation.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(ActiveDataGroupsChangedEvent.class, this);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.event.EventListener#notify(java.lang.Object)
     */
    @Override
    public void notify(ActiveDataGroupsChangedEvent pEvent)
    {
        // intentionally commented out until a later release.
//        for (DataGroupInfo dataGroupInfo : pEvent.getActivatedGroups())
//        {
//            for (DataTypeInfo dataTypeInfo : dataGroupInfo.getMembers(false))
//            {
//                if (dataTypeInfo instanceof WpsDataTypeInfo)
//                {
//                    WpsDataTypeInfo data = (WpsDataTypeInfo)dataTypeInfo;
//                    WpsProcessConfiguration processConfiguration = data.getProcessConfiguration();
//
//                    // TODO execute the process configuration:
//                }
//            }
//        }
//
//        Set<DataGroupInfo> deactivatedGroups = pEvent.getDeactivatedGroups();
//        for (DataGroupInfo dataGroupInfo : deactivatedGroups)
//        {
//            // TODO finish working this out.
//        }
    }
}
