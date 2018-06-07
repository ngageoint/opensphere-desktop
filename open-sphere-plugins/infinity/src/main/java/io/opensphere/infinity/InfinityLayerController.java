package io.opensphere.infinity;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.infinity.envoy.InfinityEnvoy;
import io.opensphere.infinity.json.SearchResponse;
import io.opensphere.infinity.util.InfinityUtilities;
import io.opensphere.mantle.controller.event.impl.DataTypeAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;
import io.opensphere.mantle.data.event.DataTypePropertyChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfoAssistant;

/** Manages infinity layer count and icon. */
public class InfinityLayerController extends EventListenerService
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(InfinityLayerController.class);

    /** The number format. */
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();
    {
        NUMBER_FORMAT.setGroupingUsed(true);
    }

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** Procrastinating executor. */
    private final ProcrastinatingExecutor myProcrastinatingExecutor = new ProcrastinatingExecutor(getClass().getSimpleName(),
            1000);

    /** The infinity-enabled data types. */
    private final Collection<DataTypeInfo> myInfinityDataTypes = Collections.synchronizedSet(New.set());

    /** The last active time span. */
    @ThreadConfined("InfinityLayerCountController-0")
    private TimeSpan myLastActiveTime;

    /** The last visible bounding box. */
    @ThreadConfined("InfinityLayerCountController-0")
    private GeographicBoundingBox myLastBoundingBox;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public InfinityLayerController(Toolbox toolbox)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;
        bindEvent(DataTypeAddedEvent.class, this::handleDataTypeAdded);
        bindEvent(DataTypeRemovedEvent.class, this::handleDataTypeRemoved);
        addService(toolbox.getMapManager().getViewChangeSupport().getViewChangeListenerService(this::handleViewChanged));
        addService(toolbox.getTimeManager().getPrimaryTimeSpanListenerService(new PrimaryTimeSpanChangeListener()
        {
            @Override
            public void primaryTimeSpansChanged(TimeSpanList spans)
            {
                handleTimeChanged(spans);
            }

            @Override
            public void primaryTimeSpansCleared()
            {
            }
        }));
    }

    /**
     * Handles a DataTypeAddedEvent.
     *
     * @param event the event
     */
    private void handleDataTypeAdded(DataTypeAddedEvent event)
    {
        DataTypeInfo dataType = event.getDataType();
        if (InfinityUtilities.isInfinityEnabled(dataType))
        {
            setInfinityIcon(dataType);
            myInfinityDataTypes.add(dataType);
            myProcrastinatingExecutor.execute(() -> updateLayerCount(null));
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
        if (InfinityUtilities.isInfinityEnabled(dataType))
        {
            myInfinityDataTypes.remove(dataType);
        }
    }

    /**
     * Handle time changed.
     *
     * @param spans the spans
     */
    void handleTimeChanged(TimeSpanList spans)
    {
        myProcrastinatingExecutor.execute(() ->
        {
            myLastActiveTime = null;
            updateLayerCount(spans.get(0));
        });
    }

    /**
     * Handle view changed.
     *
     * @param viewer the viewer
     * @param type the change type
     */
    private void handleViewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type)
    {
        myProcrastinatingExecutor.execute(() ->
        {
            myLastBoundingBox = null;
            updateLayerCount(null);
        });
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

        GenericFontIcon icon = new GenericFontIcon(AwesomeIconSolid.INFINITY, Color.WHITE, 12);
        icon.setYPos(12);
        assistant.getLayerIcons().add(icon);
    }

    /**
     * Updates the layer count for infinity-enabled layers.
     *
     * @param activeSpan the optional active span
     */
    private void updateLayerCount(TimeSpan activeSpan)
    {
        if (myLastActiveTime == null)
        {
            myLastActiveTime = activeSpan != null ? activeSpan : myToolbox.getTimeManager().getPrimaryActiveTimeSpans().get(0);
        }
        if (myLastBoundingBox == null)
        {
            myLastBoundingBox = myToolbox.getMapManager().getVisibleBoundingBox();
        }

        Collection<DataTypeInfo> infinityDataTypes;
        synchronized (myInfinityDataTypes)
        {
            infinityDataTypes = New.list(myInfinityDataTypes);
        }

        if (!infinityDataTypes.isEmpty())
        {
            Polygon polygon = JTSUtilities.createJTSPolygon(myLastBoundingBox.getVertices(), null);
            for (DataTypeInfo dataType : infinityDataTypes)
            {
                try
                {
                    SearchResponse response = InfinityEnvoy.query(myToolbox.getDataRegistry(), dataType, polygon,
                            myLastActiveTime, null);
                    setLayerCount(dataType, response.getHits().getTotal());
                }
                catch (QueryException e)
                {
                    LOGGER.error(e, e);
                }
            }
        }
    }

    /**
     * Sets the layer count.
     *
     * @param dataType the data type
     * @param count the layer count
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
                labels.add(" (" + formatNumber(count) + " in view)");
                changed = true;
            }
        }

        // Let layer tree know that the tree needs to be refreshed
        if (changed)
        {
            DataTypePropertyChangeEvent event = new DataTypePropertyChangeEvent(null, "labels", null, this);
            myToolbox.getEventManager().publishEvent(event);
        }
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
