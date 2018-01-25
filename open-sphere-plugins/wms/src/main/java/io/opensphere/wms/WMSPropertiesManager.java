package io.opensphere.wms;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoZOrderChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.layer.WMSLayerEvent;
import io.opensphere.wms.layer.WMSLayerEvent.WMSLayerEventAction;

/**
 * The Class WMSPropertiesManager.
 */
public class WMSPropertiesManager
{
    /** The Constant OPAQUE_ALPHA. */
    protected static final float OPAQUE_ALPHA = 255.0f;

    /** My Core event manager. */
    private final EventManager myEventManager;

    /** My Opacity change event handler. */
    private final EventListener<DataTypeInfoColorChangeEvent> myOpacityEventHandler = new EventListener<DataTypeInfoColorChangeEvent>()
    {
        @Override
        public void notify(DataTypeInfoColorChangeEvent event)
        {
            DataTypeInfo info = event.getDataTypeInfo();
            if (info instanceof WMSDataTypeInfo && info.getMapVisualizationInfo() != null)
            {
                TileRenderProperties props = info.getMapVisualizationInfo().getTileRenderProperties();
                if (props != null)
                {
                    props.setOpacity(event.getColor().getAlpha() / OPAQUE_ALPHA);
                }
            }
        }
    };

    /** My Visibility change event handler. */
    private final EventListener<DataTypeVisibilityChangeEvent> myVisibilityEventHandler = new EventListener<DataTypeVisibilityChangeEvent>()
    {
        @Override
        public void notify(DataTypeVisibilityChangeEvent event)
        {
            DataTypeInfo info = event.getDataTypeInfo();
            if (info instanceof WMSDataTypeInfo)
            {
                WMSLayerEvent.WMSLayerEventAction action = event.isVisible() ? WMSLayerEvent.WMSLayerEventAction.ACTIVATE
                        : WMSLayerEvent.WMSLayerEventAction.DEACTIVATE;
                WMSLayerEvent sendEvt = new WMSLayerEvent((WMSDataTypeInfo)info, action);
                myEventManager.publishEvent(sendEvt);
            }
        }
    };

    /** My Z-Order change event handler. */
    private final EventListener<DataTypeInfoZOrderChangeEvent> myZOrderEventHandler = new EventListener<DataTypeInfoZOrderChangeEvent>()
    {
        @Override
        public void notify(DataTypeInfoZOrderChangeEvent event)
        {
            DataTypeInfo info = event.getDataTypeInfo();
            if (info instanceof WMSDataTypeInfo && info.isVisible())
            {
                WMSLayerEvent wmsEvent = new WMSLayerEvent((WMSDataTypeInfo)info, WMSLayerEventAction.RESET);
                myEventManager.publishEvent(wmsEvent);
            }
        }
    };

    /**
     * Instantiates a new WMS properties manager.
     *
     * @param eventManager the Manager that receives events from the Core
     */
    public WMSPropertiesManager(EventManager eventManager)
    {
        myEventManager = eventManager;
    }

    /**
     * Cleanup and unsubscribe from Events from Core.
     */
    public void close()
    {
        if (myVisibilityEventHandler != null)
        {
            myEventManager.unsubscribe(DataTypeVisibilityChangeEvent.class, myVisibilityEventHandler);
        }
        if (myOpacityEventHandler != null)
        {
            myEventManager.unsubscribe(DataTypeInfoColorChangeEvent.class, myOpacityEventHandler);
        }
        if (myZOrderEventHandler != null)
        {
            myEventManager.unsubscribe(DataTypeInfoZOrderChangeEvent.class, myZOrderEventHandler);
        }
    }

    /**
     * Setup and subscribe to Events from the Core.
     */
    public void open()
    {
        if (myEventManager != null)
        {
            myEventManager.subscribe(DataTypeVisibilityChangeEvent.class, myVisibilityEventHandler);
            myEventManager.subscribe(DataTypeInfoColorChangeEvent.class, myOpacityEventHandler);
            myEventManager.subscribe(DataTypeInfoZOrderChangeEvent.class, myZOrderEventHandler);
        }
    }
}
