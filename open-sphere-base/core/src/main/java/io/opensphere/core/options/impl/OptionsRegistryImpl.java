package io.opensphere.core.options.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.options.OptionsProvider.OptionsProviderChangeListener;
import io.opensphere.core.options.OptionsProviderUserObject;
import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * The implementation for the {@link OptionsRegistry}.
 */
@SuppressWarnings("PMD.GodClass")
public class OptionsRegistryImpl implements OptionsRegistry
{
    /** The Constant ourEventExecutor. */
    protected static final ThreadPoolExecutor ourEventExecutor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("OptionsRegistryImpl"));

    static
    {
        ourEventExecutor.allowCoreThreadTimeOut(true);
    }

    /** The Change support. */
    private final WeakChangeSupport<OptionsRegistryListener> myChangeSupport;

    /** The Providers lock. */
    private final ReentrantLock myProvidersLock;

    /** The Providers. */
    private final Set<OptionsProvider> myProviders;

    /** The Options provider change listener. */
    private final OptionsProviderChangeListener myOptionsProviderChangeListener;

    /**
     * Constructor.
     */
    public OptionsRegistryImpl()
    {
        myChangeSupport = new WeakChangeSupport<>();
        myProvidersLock = new ReentrantLock();
        myProviders = New.set();
        myOptionsProviderChangeListener = createOptionsProviderChangeListener();
    }

    @Override
    public boolean addOptionsProvider(final OptionsProvider provider)
    {
        boolean added = false;
        myProvidersLock.lock();
        try
        {
            added = myProviders.add(provider);
            if (added)
            {
                provider.addSubTopicChangeListener(myOptionsProviderChangeListener);
            }
        }
        finally
        {
            myProvidersLock.unlock();
        }
        if (added)
        {
            myChangeSupport.notifyListeners(listener -> listener.optionsProviderAdded(provider), ourEventExecutor);
        }
        return false;
    }

    @Override
    public void addOptionsRegistryListener(OptionsRegistryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public Set<OptionsProvider> getOptionProviders()
    {
        Set<OptionsProvider> result = null;
        myProvidersLock.lock();
        try
        {
            if (myProviders != null && !myProviders.isEmpty())
            {
                result = New.set(myProviders);
            }
        }
        finally
        {
            myProvidersLock.unlock();
        }
        return result == null ? Collections.<OptionsProvider>emptySet() : Collections.unmodifiableSet(result);
    }

    @Override
    public Set<OptionsProvider> getOptionProviders(Predicate<OptionsProvider> filter)
    {
        Set<OptionsProvider> result = null;
        myProvidersLock.lock();
        try
        {
            if (myProviders != null && !myProviders.isEmpty())
            {
                result = New.set();
                for (OptionsProvider provider : myProviders)
                {
                    if (filter == null || filter.test(provider))
                    {
                        result.add(provider);
                    }
                }
            }
        }
        finally
        {
            myProvidersLock.unlock();
        }
        return result == null || result.isEmpty() ? Collections.<OptionsProvider>emptySet() : Collections.unmodifiableSet(result);
    }

    @Override
    public MutableTreeNode getOptionProviderTree(Predicate<OptionsProvider> filter)
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Set<OptionsProvider> rootSet = getOptionProviders();
        addSetToNode(root, rootSet, filter);
        return root;
    }

    @Override
    public OptionsProvider getProviderByTopic(String topic, boolean recursive)
    {
        OptionsProvider result = null;
        myProvidersLock.lock();
        try
        {
            if (myProviders != null && !myProviders.isEmpty())
            {
                for (OptionsProvider provider : myProviders)
                {
                    result = getProviderByTopicInternal(provider, topic, recursive);
                    if (result != null)
                    {
                        break;
                    }
                }
            }
        }
        finally
        {
            myProvidersLock.unlock();
        }
        return result;
    }

    @Override
    public OptionsProvider getRootProviderByTopic(String topic)
    {
        return getProviderByTopic(topic, false);
    }

    @Override
    public OptionsProvider removeOptionsProvider(final OptionsProvider provider)
    {
        boolean removed = false;
        myProvidersLock.lock();
        try
        {
            removed = myProviders.remove(provider);
            if (removed)
            {
                provider.removeSubTopicChangeListener(myOptionsProviderChangeListener);
            }
        }
        finally
        {
            myProvidersLock.unlock();
        }
        if (removed)
        {
            myChangeSupport.notifyListeners(listener -> listener.optionsProviderRemoved(provider), ourEventExecutor);
        }
        return removed ? provider : null;
    }

    @Override
    public void removeOptionsRegistryListener(OptionsRegistryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Request the display make itself visible and show the requested provider.
     *
     * @param provider the provider
     */
    @Override
    public void requestShowProvider(final OptionsProvider provider)
    {
        myChangeSupport.notifyListeners(listener -> listener.showProvider(provider), ourEventExecutor);
    }

    /**
     * Request the display make itself visible and show the requested topic.
     *
     * @param topic the topic
     */
    @Override
    public void requestShowTopic(final String topic)
    {
        OptionsProvider provider = getProviderByTopic(topic, true);
        if (provider != null)
        {
            requestShowProvider(provider);
        }
    }

    /**
     * Adds the set to node, recursively adds sub-topic nodes.
     *
     * @param parent the {@link DefaultMutableTreeNode} to which to add the
     *            nodes.
     * @param setToAdd the set to add ( will be filtered by filter inside method
     *            )
     * @param filter the {@link Predicate} of {@link OptionsProvider} to be used
     *            to select the providers.
     */
    private void addSetToNode(DefaultMutableTreeNode parent, Set<OptionsProvider> setToAdd, Predicate<OptionsProvider> filter)
    {
        if (setToAdd != null && !setToAdd.isEmpty())
        {
            Map<String, OptionsProvider> nodeNameToProviderMap = New.map();
            for (OptionsProvider p : setToAdd)
            {
                if (filter == null || filter.test(p))
                {
                    String nodeName = p.getTopic();
                    int counter = 0;
                    while (nodeNameToProviderMap.containsKey(nodeName))
                    {
                        counter++;
                        nodeName = p.getTopic() + " (" + Integer.toString(counter) + ")";
                    }
                    nodeNameToProviderMap.put(nodeName, p);
                }
            }

            if (!nodeNameToProviderMap.isEmpty())
            {
                List<String> nodeNameList = New.list(nodeNameToProviderMap.keySet());
                Collections.sort(nodeNameList);
                for (String nodeName : nodeNameList)
                {
                    OptionsProvider provider = nodeNameToProviderMap.get(nodeName);
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(new OptionsProviderUserObject(nodeName, provider),
                            true);

                    Set<OptionsProvider> subTopicSet = provider.getSubTopics();
                    addSetToNode(node, subTopicSet, filter);

                    parent.add(node);
                }
            }
        }
    }

    /**
     * Creates the options provider change listener.
     *
     * @return the options provider change listener
     */
    private OptionsProviderChangeListener createOptionsProviderChangeListener()
    {
        OptionsProviderChangeListener listener = (from, originator) -> myChangeSupport.notifyListeners(listener1 -> listener1.optionsProviderChanged(from), ourEventExecutor);
        return listener;
    }

    /**
     * Internal implementation of getProviderByTopic that does the recursive
     * search.
     *
     * @param root the root provider to search both itself and its sub-topics if
     *            recursive.
     * @param topic the topic to search for
     * @param recursive true to recursively search sub-topics.
     * @return the provider if found or null if not found
     */
    private OptionsProvider getProviderByTopicInternal(OptionsProvider root, String topic, boolean recursive)
    {
        OptionsProvider result = null;
        if (root.getTopic() != null && root.getTopic().equals(topic))
        {
            result = root;
        }
        if (recursive && result == null)
        {
            Set<OptionsProvider> subTopics = root.getSubTopics();
            if (subTopics != null && !subTopics.isEmpty())
            {
                for (OptionsProvider sub : subTopics)
                {
                    result = getProviderByTopicInternal(sub, topic, recursive);
                    if (result != null)
                    {
                        break;
                    }
                }
            }
        }
        return result;
    }
}
