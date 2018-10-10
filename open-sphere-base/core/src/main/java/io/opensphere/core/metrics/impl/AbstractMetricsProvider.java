package io.opensphere.core.metrics.impl;

import java.awt.Color;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opensphere.core.metrics.MetricsProvider;
import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * The Class AbstractMetricsProvider.
 */
public abstract class AbstractMetricsProvider implements MetricsProvider
{
    /** The Constant ourEventExecutor. */
    protected static final ThreadPoolExecutor ourEventExecutor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("MetricsRegistryImpl:Dispatch"));

    static
    {
        ourEventExecutor.allowCoreThreadTimeOut(true);
    }

    /** The Weak change support. */
    private final WeakChangeSupport<MetricsProviderListener> myWeakChangeSupport;

    /** The Topic. */
    private final String myTopic;

    /** The Sub topic. */
    private final String mySubTopic;

    /** The Label. */
    private final String myLabel;

    /** The Last updated time. */
    private long myLastUpdatedTime;

    /** The Display priority. */
    private final int myDisplayPriority;

    /** The Postfix. */
    private String myPostfix;

    /** The Color. */
    private Color myColor;

    /** The Event type. */
    private EventStrategy myEventStrategy = EventStrategy.EVENT_ON_CHANGES_ONLY;

    /**
     * Instantiates a new abstract metrics provider.
     *
     * Note: Defaults {@link EventStrategy} to EVENT_ON_CHANGES_ONLY.
     *
     * @param displayPriority the display priority
     * @param topic the topic
     * @param subTopic the sub topic
     * @param label the label
     */
    public AbstractMetricsProvider(int displayPriority, String topic, String subTopic, String label)
    {
        myWeakChangeSupport = new WeakChangeSupport<>();
        myTopic = topic == null ? MetricsRegistry.DEFAULT_TOPIC : topic;
        mySubTopic = subTopic == null ? MetricsRegistry.DEFAULT_SUB_TOPIC : subTopic;
        myLabel = label;
        myLastUpdatedTime = System.nanoTime();
        myDisplayPriority = displayPriority;
    }

    @Override
    public final void addListener(MetricsProviderListener listener)
    {
        myWeakChangeSupport.addListener(listener);
    }

    /**
     * Fire updated.
     */
    public final void fireUpdated()
    {
        myLastUpdatedTime = System.nanoTime();
        myWeakChangeSupport.notifyListeners(listener -> listener.providerUpdated(AbstractMetricsProvider.this), ourEventExecutor);
    }

    @Override
    public Color getColor()
    {
        return myColor;
    }

    @Override
    public int getDisplayPriority()
    {
        return myDisplayPriority;
    }

    /**
     * Gets the {@link EventStrategy}.
     *
     * @return the event strategy
     */
    public EventStrategy getEventType()
    {
        return myEventStrategy;
    }

    @Override
    public final String getLabel()
    {
        return myLabel;
    }

    @Override
    public Date getLastUpdatedTime()
    {
        return new Date(myLastUpdatedTime);
    }

    /**
     * Gets the postfix for the get value method.
     *
     * @return the postfix
     */
    public String getPostfix()
    {
        return myPostfix;
    }

    @Override
    public final String getSubTopic()
    {
        return mySubTopic;
    }

    @Override
    public final String getTopic()
    {
        return myTopic;
    }

    @Override
    public final boolean removeListener(MetricsProviderListener listener)
    {
        return myWeakChangeSupport.removeListener(listener);
    }

    /**
     * Sets the color for the value. Will fire a updated event if the color
     * changes.
     *
     * @param c the {@link Color}
     */
    public void setColor(Color c)
    {
        if (!EqualsHelper.equals(myColor, c))
        {
            myColor = c;
            fireUpdated();
        }
    }

    /**
     * Sets the event strategy.
     *
     * @param et the new {@link EventStrategy} (NULL not allowed)
     */
    public void setEventStrategy(EventStrategy et)
    {
        Utilities.checkNull(et, "et");
        myEventStrategy = et;
    }

    /**
     * Sets the postfix for the get value method.
     *
     * @param postfix the new postfix
     */
    public void setPostfix(String postfix)
    {
        myPostfix = postfix;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append(": TOPIC[").append(getTopic()).append("] SubTopic[").append(getSubTopic())
                .append("] Label[").append(getLabel()).append("] Value[").append(getValue()).append("] Updated[")
                .append(getLastUpdatedTime()).append(']');
        return sb.toString();
    }

    /**
     * The Enum EventStrategy.
     */
    public enum EventStrategy
    {
        /** The EVENT_ON_CHANGES_ONLY. */
        EVENT_ON_CHANGES_ONLY,

        /** The EVENT_ON_ALL_UPDATES. */
        EVENT_ON_ALL_UPDATES
    }
}
