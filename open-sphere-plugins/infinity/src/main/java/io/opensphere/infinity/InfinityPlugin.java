package io.opensphere.infinity;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.event.EventManagerListenerHandle;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.infinity.util.InfinityUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfoAssistant;
import io.opensphere.server.services.OGCServiceStateEvent;
import io.opensphere.server.source.OGCServerSource;

/** Infinity (Elasticsearch) plugin. */
public class InfinityPlugin extends AbstractServicePlugin
{
    @Override
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
        return New.list(
                new EventManagerListenerHandle<>(toolbox.getEventManager(), OGCServiceStateEvent.class, this::handleOgcEvent));
    }

    /**
     * Handles a OGCServiceStateEvent.
     *
     * @param event the event
     */
    private void handleOgcEvent(OGCServiceStateEvent event)
    {
        if (OGCServerSource.WFS_SERVICE.equals(event.getService()))
        {
            for (DataTypeInfo dataType : event.getLayerList())
            {
                if (InfinityUtilities.isInfinityEnabled(dataType))
                {
                    setInfinityIcon(dataType);
                }
            }
        }
    }

    /**
     * Sets the infinity icon in the data type.
     *
     * @param dataType
     */
    private void setInfinityIcon(DataTypeInfo dataType)
    {
        DataTypeInfoAssistant assistant = dataType.getAssistant();
        if (assistant == null)
        {
            assistant = new DefaultDataTypeInfoAssistant();
            dataType.setAssistant(assistant);
        }

        if (assistant instanceof DefaultDataTypeInfoAssistant)
        {
            GenericFontIcon icon = new GenericFontIcon(AwesomeIconSolid.INFINITY, Color.WHITE, 12);
            icon.setYPos(12);
            ((DefaultDataTypeInfoAssistant)assistant).setLayerIcons(Collections.singletonList(icon));
        }
    }
}
