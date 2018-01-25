package io.opensphere.imagery.util;

import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.metrics.impl.DefaultNumberMetricsProvider;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * The Class ImageryTileLoadTracker.
 */
public class ImageryTileLoadTracker extends TaskActivity
{
    /** The Active queries. */
    private final AtomicInteger myActiveQueryCounter = new AtomicInteger();

    /** The Query count provider. */
    private final DefaultNumberMetricsProvider myActiveQueryCountProvider;

    /** The cancelled queries. */
    private final AtomicInteger myCancelledQueryCounter = new AtomicInteger();

    /** The cancelled query count provider. */
    private final DefaultNumberMetricsProvider myCancelledQueryCountProvider;

    /** The Active queries. */
    private final AtomicInteger myFailedQueryCounter = new AtomicInteger();

    /** The Query count provider. */
    private final DefaultNumberMetricsProvider myFailedQueryCountProvider;

    /** The Query counter. */
    private final AtomicInteger myQueryCounter = new AtomicInteger();

    /** The Query count provider. */
    private final DefaultNumberMetricsProvider myTotalQueryCountProvider;

    /**
     * Instantiates a new wMS query metrics tracker.
     *
     * @param tb the {@link Toolbox}
     */
    public ImageryTileLoadTracker(Toolbox tb)
    {
        Utilities.checkNull(tb, "tb");
        String topic = "Imagery";
        String subTopic = "Downloads";
        myActiveQueryCountProvider = new DefaultNumberMetricsProvider(1, topic, subTopic, "Active");
        myTotalQueryCountProvider = new DefaultNumberMetricsProvider(2, topic, subTopic, "Total");
        myCancelledQueryCountProvider = new DefaultNumberMetricsProvider(3, topic, subTopic, "Cancelled");
        myFailedQueryCountProvider = new DefaultNumberMetricsProvider(4, topic, subTopic, "Failed");
        tb.getMetricsRegistry().addMetricsProvider(myActiveQueryCountProvider);
        tb.getMetricsRegistry().addMetricsProvider(myCancelledQueryCountProvider);
        tb.getMetricsRegistry().addMetricsProvider(myTotalQueryCountProvider);
        tb.getMetricsRegistry().addMetricsProvider(myFailedQueryCountProvider);
        setActive(false);
        tb.getUIRegistry().getMenuBarRegistry().addTaskActivity(this);
    }

    /**
     * Report a cancelled query.
     */
    public void queryCancelled()
    {
        myCancelledQueryCountProvider.setValue(Integer.valueOf(myCancelledQueryCounter.incrementAndGet()));
    }

    /**
     * Report query end (successful or not).
     */
    public void queryEnded()
    {
        myActiveQueryCountProvider.setValue(Integer.valueOf(myActiveQueryCounter.decrementAndGet()));
        updateTaskActivityLabel();
        if (myActiveQueryCounter.intValue() == 0 && isActive())
        {
            setActive(false);
        }
    }

    /**
     * Report a query error.
     */
    public void queryError()
    {
        myFailedQueryCountProvider.setValue(Integer.valueOf(myFailedQueryCounter.incrementAndGet()));
    }

    /**
     * Query started.
     */
    public void queryStarted()
    {
        myActiveQueryCountProvider.setValue(Integer.valueOf(myActiveQueryCounter.incrementAndGet()));
        myTotalQueryCountProvider.setValue(Integer.valueOf(myQueryCounter.incrementAndGet()));
        if (myActiveQueryCounter.intValue() > 0 && !isActive())
        {
            setActive(true);
        }
        updateTaskActivityLabel();
    }

    /**
     * Update task activity label.
     */
    private void updateTaskActivityLabel()
    {
        setLabelValue("Imagery Retrieves " + myActiveQueryCounter.intValue());
    }
}
