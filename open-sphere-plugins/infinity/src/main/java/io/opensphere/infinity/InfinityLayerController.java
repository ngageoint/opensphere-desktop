package io.opensphere.infinity;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.infinity.model.InfinitySettingsModel;
import io.opensphere.mantle.controller.event.impl.DataTypeAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;
import io.opensphere.mantle.data.event.DataTypePropertyChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfoAssistant;
import io.opensphere.mantle.infinity.AbstractViewTimeController;
import io.opensphere.mantle.infinity.InfinityQuerier;
import io.opensphere.mantle.infinity.InfinityUtilities;
import io.opensphere.mantle.infinity.QueryResults;
import io.opensphere.server.services.AbstractServerDataTypeInfo;
import io.opensphere.server.services.OGCServiceStateEvent;
import io.opensphere.server.source.OGCServerSource;

/** Manages infinity layer count and icon. */
public class InfinityLayerController extends AbstractViewTimeController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(InfinityLayerController.class);

    /** The number format. */
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();
    {
        NUMBER_FORMAT.setGroupingUsed(true);
    }

    /** The settings model. */
    private final InfinitySettingsModel mySettingsModel;

    /** The infinity-enabled data types. */
    private final Collection<DataTypeInfo> myInfinityDataTypes = Collections.synchronizedSet(New.set());

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param settingsModel the settings model
     */
    public InfinityLayerController(Toolbox toolbox, InfinitySettingsModel settingsModel)
    {
        super(toolbox);
        mySettingsModel = settingsModel;
        bindEvent(OGCServiceStateEvent.class, this::handleOGCServiceStateEvent);
        bindEvent(DataTypeAddedEvent.class, this::handleDataTypeAdded);
        bindEvent(DataTypeRemovedEvent.class, this::handleDataTypeRemoved);
        bindModelFX(mySettingsModel.enabledProperty(), (obs, o, n) -> handleEnabledChange(n.booleanValue()));
    }

    @Override
    protected void handleTimeChanged(TimeSpanList spans)
    {
        if (mySettingsModel.enabledProperty().get())
        {
            super.handleTimeChanged(spans);
        }
    }

    @Override
    protected void handleViewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type)
    {
        if (mySettingsModel.enabledProperty().get())
        {
            super.handleViewChanged(viewer, type);
        }
    }

    @Override
    protected void handleChange(TimeSpan activeSpan, GeographicBoundingBox boundingBox)
    {
        Collection<DataTypeInfo> infinityDataTypes = getInfinityTypes();
        if (!infinityDataTypes.isEmpty())
        {
            InfinityQuerier querier = new InfinityQuerier(getToolbox().getDataRegistry());
            Polygon polygon = JTSUtilities.createJTSPolygon(boundingBox.getVertices(), null);
            for (DataTypeInfo dataType : infinityDataTypes)
            {
                try
                {
                    QueryResults result = querier.query(dataType, polygon, activeSpan, null, null);
                    if (result != null)
                    {
                        setLayerCount(dataType, result.getCount());
                    }
                }
                catch (QueryException e)
                {
                    LOGGER.error(e);
                }
            }
        }
    }

    /**
     * Handles a OGCServiceStateEvent.
     *
     * @param event the event
     */
    private void handleOGCServiceStateEvent(OGCServiceStateEvent event)
    {
        if (OGCServerSource.WFS_SERVICE.equals(event.getService()))
        {
            event.getLayerList().stream().filter(InfinityLayerController::isInfinityEnabled).forEach(this::setInfinityIcon);
        }
    }

    /**
     * Handles a DataTypeAddedEvent.
     *
     * @param event the event
     */
    private void handleDataTypeAdded(DataTypeAddedEvent event)
    {
        DataTypeInfo dataType = event.getDataType();
        if (isInfinityEnabled(dataType))
        {
            myInfinityDataTypes.add(dataType);

            if (mySettingsModel.enabledProperty().get())
            {
                triggerChange();
            }
        }
    }

    /**
     * Handles a DataTypeRemovedEvent.
     *
     * @param event the event
     */
    private void handleDataTypeRemoved(DataTypeRemovedEvent event)
    {
        DataTypeInfo dataType = event.getDataType();
        if (isInfinityEnabled(dataType))
        {
            myInfinityDataTypes.remove(dataType);
        }
    }

    /**
     * Handles a change to the enabled state.
     *
     * @param enabled the enabled state
     */
    private void handleEnabledChange(boolean enabled)
    {
        if (enabled)
        {
            triggerChange();
        }
        else
        {
            for (DataTypeInfo dataType : getInfinityTypes())
            {
                setLayerCount(dataType, -1);
            }
        }
    }

    /**
     * Gets the infinity data types.
     *
     * @return the infinity data types
     */
    private Collection<DataTypeInfo> getInfinityTypes()
    {
        Collection<DataTypeInfo> infinityDataTypes;
        synchronized (myInfinityDataTypes)
        {
            infinityDataTypes = New.list(myInfinityDataTypes);
        }
        return infinityDataTypes;
    }

    /**
     * Sets the layer count.
     *
     * @param dataType the data type
     * @param count the layer count, or -1 to clear the count
     */
    private void setLayerCount(DataTypeInfo dataType, long count)
    {
        Set<DataTypeInfo> tileTypes = dataType.getParent().findMembers(t -> t.getMapVisualizationInfo().isImageTileType(), false,
                true);
        boolean changed = false;
        for (DataTypeInfo tileType : tileTypes)
        {
            DataTypeInfoAssistant assistant = tileType.getAssistant();
            if (assistant != null)
            {
                List<String> labels = assistant.getLayerLabels();
                labels.clear();
                if (count != -1)
                {
                    labels.add(" (" + formatNumber(count) + " in view)");
                }
                changed = true;
            }
        }

        // Let layer tree know that the tree needs to be refreshed
        if (changed)
        {
            DataTypePropertyChangeEvent event = new DataTypePropertyChangeEvent(null, "labels", null, this);
            getToolbox().getEventManager().publishEvent(event);
        }
    }

    /**
     * Sets the infinity icon in the data type.
     *
     * @param dataType the data type
     */
    private void setInfinityIcon(DataTypeInfo dataType)
    {
        DataTypeInfoAssistant assistant = dataType.getAssistant();
        if (assistant == null)
        {
            assistant = new DefaultDataTypeInfoAssistant();
            dataType.setAssistant(assistant);
        }

        boolean contains = assistant.getLayerIcons().stream()
                .anyMatch(i -> i instanceof GenericFontIcon && ((GenericFontIcon)i).getIcon() == AwesomeIconSolid.INFINITY);
        if (!contains)
        {
            GenericFontIcon icon = new GenericFontIcon(AwesomeIconSolid.INFINITY, Color.WHITE, 12);
            icon.setXPos(3);
            assistant.getLayerIcons().add(icon);
        }
    }

    /**
     * Determines if the data type is infinity-enabled.
     *
     * @param dataType the data type
     * @return whether it's infinity-enabled
     */
    private static boolean isInfinityEnabled(DataTypeInfo dataType)
    {
        return dataType instanceof AbstractServerDataTypeInfo && InfinityUtilities.isInfinityEnabled(dataType);
    }

    /**
     * Formats the number.
     *
     * @param number the number
     * @return the formatted text
     */
    private static String formatNumber(long number)
    {
        return number > 9999 ? NUMBER_FORMAT.format(number) : String.valueOf(number);
    }
}
