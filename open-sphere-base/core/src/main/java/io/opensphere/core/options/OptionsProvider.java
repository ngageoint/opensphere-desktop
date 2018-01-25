package io.opensphere.core.options;

import java.util.Set;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JPanel;

import io.opensphere.core.options.OptionsRegistry.OptionsRegistryListener;

/**
 * The Interface OptionsProvider.
 *
 * Provides a primary topic, and a {@link JPanel} that provides some set of
 * swing controls. The provider must be able to allow UI state changes without
 * impacting the actual settings for the options until "applyChanges()" is
 * called, thus committing the change. A call to "restoreDefaults()" must return
 * the UI panel to its default state.
 */
public interface OptionsProvider
{
    /**
     * Adds the sub topic.
     *
     * @param provider the provider
     * @return true, if successful
     */
    boolean addSubTopic(final OptionsProvider provider);

    /**
     * Removes the supplied subtopic from the options provider.
     *
     * @param provider the provider to remove from the options provider.
     * @return true, if the underlying set of subtopics was modified, false
     *         otherwise.
     */
    boolean removeSubTopic(OptionsProvider provider);

    /**
     * Adds a {@link OptionsRegistryListener} to the provider. Note: Held as a
     * weak reference, submitter must keep a strong reference in the owning
     * class to prevent listener from being garbage collected.
     *
     * @param listener the {@link OptionsProviderChangeListener} to be added.
     */
    void addSubTopicChangeListener(OptionsProviderChangeListener listener);

    /**
     * Apply/persist changes from the UI Panel.
     *
     * The provider may at is discretion notify the user if further actions are
     * required to make the changes active, or if a problem is encountered
     * during the apply/commit process.
     */
    void applyChanges();

    /**
     * Gets the options header panel.
     *
     * @return the options header panel
     */
    JPanel getOptionsHeaderPanel();

    /**
     * Gets UI panel with the controls for this provider. The panel does not
     * need to implement its own Apply/Cancel/RestoreDefaults UI Buttons, that
     * will be handled elsewhere.
     *
     * @return the options panel
     */
    JComponent getOptionsPanel();

    /**
     * Gets an immutable set of any sub topics ( as their own providers ) that
     * fall under this provider in a hierarchy of options. May return null or
     * empty {@link Set} if none are provided.
     *
     * Sub-topic lists or controls may not change based on the controls for this
     * provider, this is purely a method to provide hierarchical controls for
     * the UI.
     *
     * @return the {@link Set} of {@link OptionsProvider} sub-topics.
     */
    Set<OptionsProvider> getSubTopics();

    /**
     * Gets the sub topics filtered using the provided filter.
     *
     * @param filter the {@link Predicate} to be used to select the sub-topics.
     *            (Null implies all).
     * @return the {@link Set} of {@link OptionsProvider} that match the filter.
     */
    Set<OptionsProvider> getSubTopics(Predicate<OptionsProvider> filter);

    /**
     * Gets the topic for this provider.
     *
     * @return the topic
     */
    String getTopic();

    /**
     * Removes the {@link OptionsProviderChangeListener} from the provider.
     *
     * @param listener the {@link OptionsProviderChangeListener} to be removed.
     */
    void removeSubTopicChangeListener(OptionsProviderChangeListener listener);

    /**
     * Restore defaults for this options provider (not any sub-topic options
     * providers).
     */
    void restoreDefaults();

    /**
     * True if this provider uses the apply button, false if it provides no
     * state change options only information or immediate change actions.
     *
     * @return true, if uses Apply.
     */
    boolean usesApply();

    /**
     * True if this provider uses the restore button, false if it provides no
     * state change options only information or immediate change actions.
     *
     * @return true, if uses restore.
     */
    boolean usesRestore();

    /**
     * The interface {@link OptionsProviderChangeListener}, which is used to
     * event changes out to listeners when this OptionsProvider has changes to
     * its provider set.
     */
    @FunctionalInterface
    interface OptionsProviderChangeListener
    {
        /**
         * Options Provider changed.
         *
         * @param from the {@link OptionsProvider} sending the changed notice
         * @param originator the originator {@link OptionsProvider} (could be a
         *            sub topic).
         */
        void optionsProviderChanged(OptionsProvider from, OptionsProvider originator);
    }
}
