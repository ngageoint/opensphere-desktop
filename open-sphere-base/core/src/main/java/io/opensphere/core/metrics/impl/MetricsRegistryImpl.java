package io.opensphere.core.metrics.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import io.opensphere.core.metrics.MetricsProvider;
import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * The Class MetricsRegistryImpl.
 */
public class MetricsRegistryImpl implements MetricsRegistry
{
    /** The Constant ourEventExecutor. */
    protected static final ThreadPoolExecutor ourEventExecutor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("MetricsRegistryImpl:Dispatch"));

    /** The Change support. */
    private final WeakChangeSupport<MetricsRegistryListener> myChangeSupport;

    /** The Provider lock. */
    private final ReentrantLock myProviderLock;

    /** The Metrics providers. */
    private final Set<MetricsProvider> myMetricsProviders;

    static
    {
        ourEventExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Instantiates a new metrics registry impl.
     */
    public MetricsRegistryImpl()
    {
        myChangeSupport = new WeakChangeSupport<>();
        myProviderLock = new ReentrantLock();
        myMetricsProviders = new HashSet<>();
    }

    @Override
    public void addMetricsProvider(final MetricsProvider provider)
    {
        myProviderLock.lock();
        try
        {
            if (myMetricsProviders.add(provider))
            {
                myChangeSupport.notifyListeners(listener -> listener.metricsProviderAdded(provider), ourEventExecutor);
            }
        }
        finally
        {
            myProviderLock.unlock();
        }
    }

    @Override
    public void addMetricsRegistryListener(MetricsRegistryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public Set<MetricsProvider> getProviders()
    {
        Set<MetricsProvider> topicSet = null;
        myProviderLock.lock();
        try
        {
            topicSet = New.set(myMetricsProviders);
        }
        finally
        {
            myProviderLock.unlock();
        }
        return topicSet;
    }

    @Override
    public Set<MetricsProvider> getProviders(Predicate<? super MetricsProvider> filter)
    {
        Utilities.checkNull(filter, "filter");
        Set<MetricsProvider> topicSet = New.set();
        myProviderLock.lock();
        try
        {
            for (MetricsProvider p : myMetricsProviders)
            {
                if (filter.test(p))
                {
                    topicSet.add(p);
                }
            }
        }
        finally
        {
            myProviderLock.unlock();
        }
        return topicSet.isEmpty() ? Collections.<MetricsProvider>emptySet() : topicSet;
    }

    @Override
    public Set<MetricsProvider> getProviders(String pTopic)
    {
        final String topic = pTopic == null ? DEFAULT_TOPIC : pTopic;
        return getProviders(value -> value != null && topic.equals(value.getTopic() == null ? DEFAULT_TOPIC : value.getTopic()));
    }

    @Override
    public Set<MetricsProvider> getProviders(String pTopic, String pSubTopic)
    {
        final String topic = pTopic == null ? DEFAULT_TOPIC : pTopic;
        final String subTopic = pSubTopic == null ? DEFAULT_SUB_TOPIC : pSubTopic;
        return getProviders(value -> value != null && topic.equals(value.getTopic() == null ? DEFAULT_TOPIC : value.getTopic())
                && subTopic.equals(value.getSubTopic() == null ? DEFAULT_SUB_TOPIC : value.getSubTopic()));
    }

    @Override
    public Set<String> getSubTopics(String topic)
    {
        Set<MetricsProvider> providersForTopic = getProviders(topic);
        Set<String> subTopics = New.set();
        if (providersForTopic != null && !providersForTopic.isEmpty())
        {
            for (MetricsProvider p : providersForTopic)
            {
                if (p.getSubTopic() == null)
                {
                    subTopics.add(DEFAULT_SUB_TOPIC);
                }
                else
                {
                    subTopics.add(p.getSubTopic());
                }
            }
        }
        return subTopics;
    }

    @Override
    public Set<String> getTopics()
    {
        Set<String> topicSet = New.set();
        myProviderLock.lock();
        try
        {
            for (MetricsProvider p : myMetricsProviders)
            {
                if (p.getTopic() != null)
                {
                    topicSet.add(p.getTopic());
                }
                else
                {
                    topicSet.add(DEFAULT_TOPIC);
                }
            }
        }
        finally
        {
            myProviderLock.unlock();
        }
        return topicSet;
    }

    @Override
    public void removeMetricsProvider(final MetricsProvider provider)
    {
        myProviderLock.lock();
        try
        {
            if (myMetricsProviders.remove(provider))
            {
                myChangeSupport.notifyListeners(listener -> listener.metricsProviderRemoved(provider), ourEventExecutor);
            }
        }
        finally
        {
            myProviderLock.unlock();
        }
    }

    @Override
    public boolean removeMetricsRegistryListener(MetricsRegistryListener listener)
    {
        return myChangeSupport.removeListener(listener);
    }
}
