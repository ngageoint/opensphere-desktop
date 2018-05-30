package io.opensphere.infinity;

import java.util.Collection;
import java.util.Collections;

import javax.swing.Icon;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.event.EventManagerListenerHandle;
import io.opensphere.core.util.AwesomeIcon;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.data.DataTypeInfoAssistant;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfoAssistant;
import io.opensphere.server.services.AbstractServerDataTypeInfo;
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
            for (AbstractServerDataTypeInfo dataType : event.getLayerList())
            {
                if (dataType.hasTag(".es-url"))
                {
                    DataTypeInfoAssistant assistant = dataType.getAssistant();
                    if (assistant == null)
                    {
                        assistant = new DefaultDataTypeInfoAssistant();
                        dataType.setAssistant(assistant);
                    }
                    if (assistant instanceof DefaultDataTypeInfoAssistant)
                    {
                        Icon icon = new GenericFontIcon(AwesomeIcon.ICON_INFINITY, dataType.getBasicVisualizationInfo().getTypeColor());
//                        Icon icon = IconUtil.getColorizedIcon(IconType.BINOCULARS, IconStyle.NORMAL, dataType.getBasicVisualizationInfo().getTypeColor(), 18);
                        ((DefaultDataTypeInfoAssistant)assistant).setLayerIcons(Collections.singletonList(icon));
                    }
                }
            }
        }
    }
}
