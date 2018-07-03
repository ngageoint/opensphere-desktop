package io.opensphere.core.quantify.impl;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import io.opensphere.core.quantify.QuantifySender;
import io.opensphere.core.quantify.QuantifyService;
import io.opensphere.core.quantify.model.Metric;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import javafx.beans.property.BooleanProperty;

/**
 *
 */
public class DefaultQuantifyService implements QuantifyService
{
    /** Logger reference. */
    private static final Logger LOG = Logger.getLogger(DefaultQuantifyService.class);

    /**
     * The metrics collected by the service but not yet sent to the remote
     * collector.
     */
    private final Map<String, Metric> myMetrics;

    /** The sender to which to send metrics. */
    private final QuantifySender mySender;

    /** The frequency at which metrics are sent. */
    private final Duration mySendFrequency;

    /** The scheduler service used to maintain periodic transmission. */
    private final ScheduledExecutorService mySendScheduler;

    /**
     * The property in which the enabled state of the quantify plugin is
     * maintained.
     */
    private final BooleanProperty myEnabledProperty = new ConcurrentBooleanProperty(true);

    /**
     * Creates a new service instance bound to the supplied sender.
     *
     * @param sender the sender to which to send the metrics.
     * @param enabledProperty the property to which the enabled property is
     *            bound.
     */
    public DefaultQuantifyService(QuantifySender sender, BooleanProperty enabledProperty)
    {
        mySender = sender;
        myEnabledProperty.bind(enabledProperty);
        myMetrics = New.map();
        mySendFrequency = Duration.ofMinutes(5);
        mySendScheduler = Executors.newSingleThreadScheduledExecutor();
        mySendScheduler.scheduleAtFixedRate(() -> flush(), mySendFrequency.toMillis(), mySendFrequency.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.quantify.QuantifyService#close()
     */
    @Override
    public void close()
    {
        mySendScheduler.shutdown();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.quantify.QuantifyService#collectMetric(java.lang.String)
     */
    @Override
    public void collectMetric(String key)
    {
        if (myEnabledProperty.get())
        {
            synchronized (myMetrics)
            {
                if (!myMetrics.containsKey(key))
                {
                    myMetrics.put(key, new Metric(key));
                }
                myMetrics.get(key).increment();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.quantify.QuantifyService#flush()
     */
    @Override
    public void flush()
    {
        if (myEnabledProperty.get())
        {
            synchronized (myMetrics)
            {
                LOG.info("Sending metrics.");
                mySender.send(New.collection(myMetrics.values()));
                myMetrics.clear();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.quantify.QuantifyService#enabledProperty()
     */
    @Override
    public BooleanProperty enabledProperty()
    {
        return myEnabledProperty;
    }
}
