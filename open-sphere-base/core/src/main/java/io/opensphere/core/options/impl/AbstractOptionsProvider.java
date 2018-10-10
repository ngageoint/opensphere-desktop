package io.opensphere.core.options.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import net.jcip.annotations.GuardedBy;

/**
 * The Class AbstractOptionsProvider.
 */
public abstract class AbstractOptionsProvider implements OptionsProvider
{
    /** The Constant DEFAULT_BACKGROUND_COLOR. */
    public static final Color DEFAULT_BACKGROUND_COLOR = ColorUtilities.opacitizeColor(Colors.LF_SECONDARY3, 180);

    /** The Constant TRANSPARENT_COLOR. */
    public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

    /** The Constant ourEventExecutor. */
    private static final ThreadPoolExecutor ourEventExecutor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("OptionsProvider:Dispatch"));

    /** The Change support. */
    private final WeakChangeSupport<OptionsProviderChangeListener> myChangeSupport;

    /** The Sub topic change listener. */
    private final OptionsProviderChangeListener mySubTopicChangeListener;

    /** The Sub topics. */
    @GuardedBy("mySubTopics")
    private final Set<OptionsProvider> mySubTopics;

    /** The Topic. */
    private final String myTopic;

    static
    {
        ourEventExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Instantiates a new abstract options provider.
     *
     * @param topic the topic
     */
    public AbstractOptionsProvider(String topic)
    {
        myChangeSupport = new WeakChangeSupport<>();
        myTopic = topic;
        mySubTopics = New.set();
        mySubTopicChangeListener = createSubTopicChangeListener();
    }

    @Override
    public boolean addSubTopic(OptionsProvider provider)
    {
        boolean added = false;
        synchronized (mySubTopics)
        {
            added = mySubTopics.add(provider);
            if (added)
            {
                provider.addSubTopicChangeListener(mySubTopicChangeListener);
            }
        }
        if (added)
        {
            notifyListeners(provider);
        }
        return added;
    }

    @Override
    public void addSubTopicChangeListener(OptionsProviderChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public JPanel getOptionsHeaderPanel()
    {
        return null;
    }

    @Override
    public Set<OptionsProvider> getSubTopics()
    {
        return getSubTopics(null);
    }

    @Override
    public Set<OptionsProvider> getSubTopics(Predicate<OptionsProvider> filter)
    {
        Set<OptionsProvider> result;
        synchronized (mySubTopics)
        {
            if (mySubTopics.isEmpty())
            {
                result = Collections.<OptionsProvider>emptySet();
            }
            else if (filter == null)
            {
                result = Collections.unmodifiableSet(New.set(mySubTopics));
            }
            else
            {
                result = Collections.unmodifiableSet(mySubTopics.stream().filter(filter).collect(Collectors.toSet()));
            }
        }
        return result;
    }

    @Override
    public String getTopic()
    {
        return myTopic;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#removeSubTopic(io.opensphere.core.options.OptionsProvider)
     */
    @Override
    public boolean removeSubTopic(OptionsProvider provider)
    {
        boolean removed = false;
        synchronized (mySubTopics)
        {
            removed = mySubTopics.remove(provider);
            if (removed)
            {
                provider.removeSubTopicChangeListener(mySubTopicChangeListener);
            }
        }
        if (removed)
        {
            notifyListeners(provider);
        }
        return removed;
    }

    @Override
    public void removeSubTopicChangeListener(OptionsProviderChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public boolean usesApply()
    {
        return true;
    }

    @Override
    public boolean usesRestore()
    {
        return true;
    }

    /**
     * Creates the sub topic change listener.
     *
     * @return the sub topic change listener
     */
    private OptionsProviderChangeListener createSubTopicChangeListener()
    {
        return (from, originator) -> notifyListeners(originator);
    }

    /**
     * Notifies listeners.
     *
     * @param provider the provider
     */
    private void notifyListeners(final OptionsProvider provider)
    {
        myChangeSupport.notifyListeners(listener -> listener.optionsProviderChanged(this, provider), ourEventExecutor);
    }
}
