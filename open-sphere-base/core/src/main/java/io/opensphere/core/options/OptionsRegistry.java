package io.opensphere.core.options;

import java.util.Set;
import java.util.function.Predicate;

import javax.swing.tree.MutableTreeNode;

import io.opensphere.core.util.Service;

/**
 * A registry that stores a set of {@link OptionsProvider}, which are used to
 * supply the options editor panels and interface for topics/sub topics to be
 * provided to the user interface.
 */
public interface OptionsRegistry
{
    /**
     * Adds the options provider to the registry.
     *
     * @param provider the {@link OptionsProvider} to add.
     * @return true, if not already in the registry.
     */
    boolean addOptionsProvider(OptionsProvider provider);

    /**
     * Adds a {@link OptionsRegistryListener} to the registry. Note: Held as a
     * weak reference, submitter must keep a strong reference in the owning
     * class to prevent listener from being garbage collected.
     *
     * @param listener the {@link OptionsRegistryListener} to be added.
     */
    void addOptionsRegistryListener(OptionsRegistryListener listener);

    /**
     * Returns an immutable set of the OptionsProviders in this registry.
     *
     * @return the {@link Set} of {@link OptionsProvider} that passed the filter
     *         criteria or an empty set if none.
     */
    Set<OptionsProvider> getOptionProviders();

    /**
     * Returns an immutable set of {@link OptionsProvider}s that match the
     * provided filter criteria.
     *
     * @param filter the {@link Predicate} of {@link OptionsProvider} used to
     *            determine which providers will be returned in the set. Null
     *            filter implies accept all.
     * @return the {@link Set} of {@link OptionsProvider} that passed the filter
     *         criteria or an empty set if none matched.
     */
    Set<OptionsProvider> getOptionProviders(Predicate<OptionsProvider> filter);

    /**
     * Gets the option provider root tree node built from the hierarchy of
     * OptionsProvider in the registry. The UserObject for each of the sub-nodes
     * will be the {@link OptionsProviderUserObject} for that node, the node
     * name will be the topic. If multiple providers use the same topic name in
     * the hierarchy at the same tree path the node name will be enumerated with
     * a "(1)" ... "(N)" label end.
     *
     * @param filter the {@link Predicate} used to control which providers are
     *            accepted into the tree. This will be recursively applied
     *            throughout the entire tree.
     * @return the {@link MutableTreeNode} that represents the root node for the
     *         tree.
     */
    MutableTreeNode getOptionProviderTree(Predicate<OptionsProvider> filter);

    /**
     * Searches the root set of OptionsProvider for one that matches the
     * provided topic and if found returns it, or returns null. If recursive
     * will search recursively through all sub-topics for a match. Returns first
     * found or null if not found.
     *
     * @param topic the topic to search for.
     * @param recursive true to recurse through sub-topics for a match.
     * @return the {@link OptionsProvider} with the topic
     */
    OptionsProvider getProviderByTopic(String topic, boolean recursive);

    /**
     * Searches the root set of OptionsProvider for one that matches the
     * provided topic and if found returns it, or returns null.
     *
     * @param topic the topic to search for.
     * @return the {@link OptionsProvider} with the topic
     */
    OptionsProvider getRootProviderByTopic(String topic);

    /**
     * Removes the {@link OptionsProvider} from the registry if in the
     * registry..
     *
     * @param provider the {@link OptionsProvider} to remove.
     * @return the removed {@link OptionsProvider} or null if not in registry.
     */
    OptionsProvider removeOptionsProvider(OptionsProvider provider);

    /**
     * Removes the {@link OptionsRegistryListener} from the registry.
     *
     * @param listener the {@link OptionsRegistryListener} to be removed.
     */
    void removeOptionsRegistryListener(OptionsRegistryListener listener);

    /**
     * Request the display make itself visible and show the requested provider.
     *
     * @param provider the provider
     */
    void requestShowProvider(OptionsProvider provider);

    /**
     * Request the display make itself visible and show the requested topic.
     *
     * @param string the string
     */
    void requestShowTopic(String string);

    /**
     * Creates a service that can be used to add/remove the given provider.
     *
     * @param provider the provider
     * @return the service
     */
    default Service getOptionsProviderService(final OptionsProvider provider)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                addOptionsProvider(provider);
            }

            @Override
            public void close()
            {
                removeOptionsProvider(provider);
            }
        };
    }

    /**
     * The interface {@link OptionsRegistryListener}, which is used to event
     * changes out to listeners when this OptionsRegistry has changes to its
     * provider set.
     */
    interface OptionsRegistryListener
    {
        /**
         * Options provider added.
         *
         * @param provider the provider
         */
        void optionsProviderAdded(OptionsProvider provider);

        /**
         * Options provider changed.
         *
         * @param provider the provider
         */
        void optionsProviderChanged(OptionsProvider provider);

        /**
         * Options provider removed.
         *
         * @param provider the provider
         */
        void optionsProviderRemoved(OptionsProvider provider);

        /**
         * Show provider.
         *
         * @param provider the provider
         */
        void showProvider(OptionsProvider provider);
    }
}
