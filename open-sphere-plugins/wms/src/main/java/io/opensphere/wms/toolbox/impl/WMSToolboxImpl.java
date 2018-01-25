package io.opensphere.wms.toolbox.impl;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.sld.SldRegistry;
import io.opensphere.wms.sld.impl.SldRegistryImpl;
import io.opensphere.wms.toolbox.WMSServerCapabilitiesListener;
import io.opensphere.wms.toolbox.WMSToolbox;

/**
 * The Class WMSToolboxImpl.
 */
public class WMSToolboxImpl implements WMSToolbox
{
    /**
     * The list of listeners interested in any new {@link WMSServerCapabilities}
     * being received from a wms server.
     */
    private final List<WMSServerCapabilitiesListener> myServerCapabilitiesListeners = Collections.synchronizedList(New.list());

    /** The my sld registry. */
    private final SldRegistryImpl mySldRegistry;

    /**
     * Instantiates a new wMS toolbox impl.
     *
     * @param toolbox the toolbox
     */
    public WMSToolboxImpl(Toolbox toolbox)
    {
        mySldRegistry = new SldRegistryImpl(toolbox);
    }

    @Override
    public void addServerCapabiltiesListener(WMSServerCapabilitiesListener listener)
    {
        myServerCapabilitiesListeners.add(listener);
    }

    @Override
    public String getDescription()
    {
        return "WMS Toolbox";
    }

    @Override
    public SldRegistry getSldRegistry()
    {
        return mySldRegistry;
    }

    @Override
    public void notifyServerCapabilitiesListener(ServerConnectionParams serverConfig, WMSServerCapabilities capabilities)
    {
        synchronized (myServerCapabilitiesListeners)
        {
            for (WMSServerCapabilitiesListener listener : myServerCapabilitiesListeners)
            {
                listener.received(serverConfig, capabilities);
            }
        }
    }

    @Override
    public void removeServerCapabilitiesListener(WMSServerCapabilitiesListener listener)
    {
        myServerCapabilitiesListeners.remove(listener);
    }
}
