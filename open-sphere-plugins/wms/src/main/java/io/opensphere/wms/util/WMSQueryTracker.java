package io.opensphere.wms.util;

import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.metrics.impl.DefaultNumberMetricsProvider;
import io.opensphere.core.util.PhasedChangeArbitrator;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;

/**
 * The Class WMSQueryTracker.
 */
public class WMSQueryTracker extends CancellableTaskActivity
{
    /** The Active queries. */
    private final AtomicInteger myActiveQueryCounter = new AtomicInteger();

    /** The Query count provider. */
    private final DefaultNumberMetricsProvider myActiveQueryCountProvider;

    /** The cancelled queries. */
    private final AtomicInteger myCancelledQueryCounter = new AtomicInteger();

    /** The cancelled query count provider. */
    private final DefaultNumberMetricsProvider myCancelledQueryCountProvider;

    /** The canceller. */
    private final Runnable myCanceller;

    /** The done queries. */
    private final AtomicInteger myDoneQueryCounter = new AtomicInteger();

    /** The Active queries. */
    private final AtomicInteger myFailedQueryCounter = new AtomicInteger();

    /** The Query count provider. */
    private final DefaultNumberMetricsProvider myFailedQueryCountProvider;

    /**
     * While tiles are being downloaded, insist on phased commits for animation
     * changes.
     */
    private final PhasedChangeArbitrator myPhasedChangeArbitrator = new PhasedChangeArbitrator()
    {
        @Override
        public boolean isPhasedCommitRequired()
        {
            return isActive();
        }
    };

    /** The Query counter. */
    private final AtomicInteger myQueryCounter = new AtomicInteger();

    /** The Query count provider. */
    private final DefaultNumberMetricsProvider myTotalQueryCountProvider;

    /**
     * The number of queries that have been started since the last time all the
     * queries were done.
     */
    private final AtomicInteger myTotalSinceLastAllDoneCounter = new AtomicInteger();

    /**
     * Instantiates a new wMS query metrics tracker.
     *
     * @param tb the {@link Toolbox}
     * @param topic The topic for the metrics.
     */
    public WMSQueryTracker(Toolbox tb, String topic)
    {
        Utilities.checkNull(tb, "tb");
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
        // TODO there is no cleanup mechanism for this monitor, so these are
        // never removed cleanly.
        tb.getUIRegistry().getMenuBarRegistry().addTaskActivity(this);
        tb.getAnimationManager().addPhasedChangeArbitrator(myPhasedChangeArbitrator);

        myCanceller = () -> tb.getGeometryRegistry().cancelAllImageRetrievals();
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
        if (myActiveQueryCounter.get() == 0)
        {
            myDoneQueryCounter.set(0);
            myTotalSinceLastAllDoneCounter.set(0);
        }
        else
        {
            myDoneQueryCounter.incrementAndGet();
        }
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
        myTotalSinceLastAllDoneCounter.incrementAndGet();
        if (myActiveQueryCounter.intValue() > 0 && !isActive())
        {
            setActive(true);
        }
        updateTaskActivityLabel();
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        super.setCancelled(cancelled);

        if (cancelled)
        {
            myCanceller.run();
        }
    }

    /**
     * Update task activity label.
     */
    private void updateTaskActivityLabel()
    {
        setLabelValue("Tile Downloads " + myActiveQueryCounter.intValue());
        setProgress(myDoneQueryCounter.doubleValue() / myTotalSinceLastAllDoneCounter.doubleValue());
    }
}
