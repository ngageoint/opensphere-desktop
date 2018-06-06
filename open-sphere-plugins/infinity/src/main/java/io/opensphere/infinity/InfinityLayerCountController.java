package io.opensphere.infinity;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
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
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfoAssistant;
import io.opensphere.server.services.OGCServiceStateEvent;
import io.opensphere.server.source.OGCServerSource;

/** Infinity layer count controller. */
public class InfinityLayerCountController extends EventListenerService
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(InfinityLayerCountController.class);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** Procrastinating executor. */
    private final ProcrastinatingExecutor myProcrastinatingExecutor = new ProcrastinatingExecutor(getClass().getSimpleName(),
            500);

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
    public InfinityLayerCountController(Toolbox toolbox)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;
        bindEvent(OGCServiceStateEvent.class, this::handleOgcEvent);
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
        addService(toolbox.getMapManager().getViewChangeSupport().getViewChangeListenerService(this::handleViewChanged));
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
            List<DataTypeInfo> infinityLayers = event.getLayerList().stream().filter(InfinityUtilities::isInfinityEnabled)
                    .collect(Collectors.toList());
            if (!infinityLayers.isEmpty())
            {
                for (DataTypeInfo dataType : infinityLayers)
                {
                    setInfinityIcon(dataType);
                }
                myInfinityDataTypes.addAll(infinityLayers);
                myProcrastinatingExecutor.execute(() -> updateLayerCount(null));
            }
        }
    }

    /**
     * Handle time changed.
     *
     * @param spans the spans
     */
    private void handleTimeChanged(TimeSpanList spans)
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
            ((DefaultDataTypeInfoAssistant)assistant).setLayerIcons(List.of(icon));
        }
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

        for (DataTypeInfo dataType : myInfinityDataTypes)
        {
            System.out.println("Update stuff " + dataType.getDisplayName() + " " + myLastActiveTime + " " + myLastBoundingBox);

            // TODO polygon, fields
            List<LatLonAlt> points = New.list();
            points.add(LatLonAlt.createFromDegrees(0, 0));
            points.add(LatLonAlt.createFromDegrees(1, 0));
            points.add(LatLonAlt.createFromDegrees(1, 1));
            points.add(LatLonAlt.createFromDegrees(0, 1));
            points.add(LatLonAlt.createFromDegrees(0, 0));
            Polygon polygon = JTSUtilities.createJTSPolygonFromLatLonAlt(points, null);
            try
            {
                SearchResponse response = InfinityEnvoy.query(myToolbox.getDataRegistry(), dataType, polygon, myLastActiveTime,
                        "geom", "time", null);
                LOGGER.info(dataType.getDisplayName() + " hits: " + response.getHits().getTotal());
                if (response.getAggregations() != null)
                {
                    LOGGER.info("Aggs: " + Arrays.toString(response.getAggregations().getBins().getBuckets()));
                }
            }
            catch (QueryException e)
            {
                LOGGER.error(e, e);
            }
        }
    }
}
