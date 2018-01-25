package io.opensphere.mantle.data.geom.style;

import java.util.Set;

import javax.swing.JPanel;

import io.opensphere.mantle.data.VisualizationSupport;

/**
 * The Interface FeatureVisualizationControlPanel.
 */
public interface FeatureVisualizationControlPanel
{
    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    void addListener(FeatureVisualizationControlPanelListener listener);

    /**
     * Apply changes.
     */
    void applyChanges();

    /**
     * Cancel changes.
     */
    void cancelChanges();

    /**
     * Gets the changed parameters.
     *
     * @return the changed parameters
     */
    Set<VisualizationStyleParameter> getChangedParameters();

    /**
     * Gets the changed style.
     *
     * @return the changed style
     */
    VisualizationStyle getChangedStyle();

    /**
     * Gets the panel.
     *
     * @return the panel
     */
    JPanel getPanel();

    /**
     * Gets the style.
     *
     * @return the style
     */
    VisualizationStyle getStyle();

    /**
     * Checks for changes.
     *
     * @return true, if successful
     */
    boolean hasChanges();

    /**
     * Checks if is update with previewable.
     *
     * @return true, if is update with previewable
     */
    boolean isUpdateWithPreviewable();

    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    void removeListener(FeatureVisualizationControlPanelListener listener);

    /**
     * Revert to default settings.
     */
    void revertToDefaultSettigns();

    /**
     * A listener that listens for changes in the style being manipulated by the
     * control panel.
     *
     */
    public interface FeatureVisualizationControlPanelListener
    {
        /**
         * Perform live parameter update.
         *
         * @param dtiKey the dti key
         * @param convertedClass the converted class
         * @param vsClass the vs class
         * @param updateSet the update set
         */
        void performLiveParameterUpdate(String dtiKey, Class<? extends VisualizationSupport> convertedClass,
                Class<? extends VisualizationStyle> vsClass, Set<VisualizationStyleParameter> updateSet);

        /**
         * Called when changes are made to the style copy from the style base.
         *
         * @param hasChangesFromBase the has changes from base style.
         */
        void styleChanged(boolean hasChangesFromBase);

        /**
         * Style changes accepted.
         */
        void styleChangesAccepted();

        /**
         * Style changes cancelled.
         */
        void styleChangesCancelled();
    }
}
