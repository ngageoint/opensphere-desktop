package io.opensphere.controlpanels.animation.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModel;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModels;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent;
import io.opensphere.core.timeline.TimelineChangeEvent;
import io.opensphere.core.timeline.TimelineDatum;
import io.opensphere.core.timeline.TimelineRegistry;
import io.opensphere.core.util.ObservableValueService;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Gets feature data from the timeline and order registries and gives it to the
 * chart layer.
 */
public class ChartDataController extends ObservableValueService
{
    /** The timeline registry. */
    private final TimelineRegistry myTimelineRegistry;

    /** The order manager registry. */
    private final OrderManagerRegistry myOrderManagerRegistry;

    /** The timeline UI model. */
    private final TimelineUIModel myUIModel;

    /** The chart layer models. */
    @ThreadConfined("EDT")
    private final ChartLayerModels myChartLayerModels;

    /** The map of order key to chart layer. */
    @ThreadConfined("EDT")
    private final Map<OrderParticipantKey, ChartLayerModel> myChartLayerMap = New.map();

    /** The map of order key to order change listener. */
    @ThreadConfined("EDT")
    private final Map<OrderParticipantKey, OrderChangeListener> myOrderListenerMap = New.map();

    /** The procrastinating executor for regenerating chart data. */
    private final ProcrastinatingExecutor myDataExecutor = new ProcrastinatingExecutor("ChartDataController", 500, 1000);

    /**
     * Constructor.
     *
     * @param timelineRegistry The timeline registry
     * @param orderManagerRegistry The order manager registry
     * @param uiModel The timeline UI model
     * @param chartLayerModels The chart layer models
     */
    public ChartDataController(TimelineRegistry timelineRegistry, OrderManagerRegistry orderManagerRegistry,
            TimelineUIModel uiModel, ChartLayerModels chartLayerModels)
    {
        super();
        myTimelineRegistry = timelineRegistry;
        myOrderManagerRegistry = orderManagerRegistry;
        myUIModel = uiModel;
        myChartLayerModels = chartLayerModels;

        addService(new Service()
        {
            /** The listener. */
            private final Consumer<TimelineChangeEvent> myListener = ChartDataController.this::handleTimelineChangeEvent;

            @Override
            public void open()
            {
                myTimelineRegistry.addListener(myListener);
            }

            @Override
            public void close()
            {
                myTimelineRegistry.removeListener(myListener);
            }
        });
        bindModel(myUIModel.getUISpan(), (observable, oldValue, newValue) -> updateChartData());
    }

    /**
     * Handles a timeline change event.
     *
     * @param event the event
     */
    private void handleTimelineChangeEvent(final TimelineChangeEvent event)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                OrderParticipantKey key = event.getKey();
                switch (event.getChangeType())
                {
                    case LAYER_ADDED:
                        handleLayerAdded(key);
                        break;
                    case LAYER_REMOVED:
                        handleLayerRemoved(key);
                        break;
                    case TIME_SPANS:
                        updateChartData();
                        break;
                    case COLOR:
                        handleColorChange(key);
                        break;
                    case VISIBILITY:
                        handleVisibilityChange(key);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * Handles an order change event.
     *
     * @param event the event
     */
    private void handleOrderChangeEvent(ParticipantOrderChangeEvent event)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                for (OrderParticipantKey key : event.getChangedParticipants().keySet())
                {
                    int order = event.getChangedParticipants().get(key);
                    ChartLayerModel layer = myChartLayerMap.get(key);
                    if (layer != null)
                    {
                        layer.setOrder(order);
                    }
                }

                myChartLayerModels.reorder();
                myUIModel.repaint();
            }
        });
    }

    /**
     * Handles a layer being added.
     *
     * @param key the key
     */
    private void handleLayerAdded(OrderParticipantKey key)
    {
        myOrderListenerMap.put(key, addOrderListener(key));
        myChartLayerMap.put(key, createLayer(key));
    }

    /**
     * Handles a layer being removed.
     *
     * @param key the key
     */
    private void handleLayerRemoved(OrderParticipantKey key)
    {
        myOrderListenerMap.remove(key);
        myChartLayerMap.remove(key);
        updateChartData();
    }

    /**
     * Handles a color change.
     *
     * @param key the key
     */
    private void handleColorChange(OrderParticipantKey key)
    {
        ChartLayerModel layer = myChartLayerMap.get(key);
        if (layer != null)
        {
            layer.setColor(myTimelineRegistry.getColor(key));
            myUIModel.repaint();
        }
    }

    /**
     * Handles a visibility change.
     *
     * @param key the key
     */
    private void handleVisibilityChange(OrderParticipantKey key)
    {
        ChartLayerModel layer = myChartLayerMap.get(key);
        if (layer != null)
        {
            layer.setVisible(myTimelineRegistry.isVisible(key));
            myUIModel.repaint();
        }
    }

    /**
     * Creates a chart layer model from the key.
     *
     * @param key the order participant key
     * @return the chart layer model, or null if there are no time spans for the
     *         key
     */
    private ChartLayerModel createLayer(OrderParticipantKey key)
    {
        ChartLayerModel layer = new ChartLayerModel(myTimelineRegistry.getName(key), myTimelineRegistry.getColor(key), myUIModel);
        layer.setOrder(myOrderManagerRegistry.getOrderManager(key).getOrder(key));
        layer.setVisible(myTimelineRegistry.isVisible(key));
        return layer;
    }

    /**
     * Adds an order listener for the key.
     *
     * @param key the key
     * @return the order listener
     */
    private OrderChangeListener addOrderListener(OrderParticipantKey key)
    {
        OrderManager orderManager = myOrderManagerRegistry.getOrderManager(key);
        OrderChangeListener listener = orderEvent -> handleOrderChangeEvent(orderEvent);
        orderManager.addParticipantChangeListener(listener);
        return listener;
    }

    /**
     * Updates the chart data.
     */
    private void updateChartData()
    {
        myDataExecutor.execute(getSwingRunnable(this::updateChartDataNow));
    }

    /**
     * Updates the chart data now.
     */
    private void updateChartDataNow()
    {
        List<ChartLayerModel> chartLayers = New.list();

        // Update the layer times from the times in the registry
        for (Map.Entry<OrderParticipantKey, ChartLayerModel> entry : myChartLayerMap.entrySet())
        {
            OrderParticipantKey key = entry.getKey();
            ChartLayerModel layer = entry.getValue();

            Collection<TimelineDatum> timeSpans = myTimelineRegistry.getSpans(key,
                span -> span != null && !span.getTimeSpan().isTimeless());
            layer.setData(timeSpans);

            if (!timeSpans.isEmpty())
            {
                chartLayers.add(layer);
            }
        }

        // Update the chart model
        myChartLayerModels.clear();
        for (ChartLayerModel layer : chartLayers)
        {
            myChartLayerModels.addLayer(layer);
        }
        myChartLayerModels.reorder();
        myUIModel.repaint();
    }

    /**
     * Wraps a runnable in a EventQueueUtilities.invokeLater call.
     *
     * @param r the runnable to wrap
     * @return the swing runnable
     */
    private Runnable getSwingRunnable(Runnable r)
    {
        return () -> EventQueueUtilities.invokeLater(r);
    }
}
