package io.opensphere.mantle.data.geom.style;

import java.awt.Color;
import java.util.Set;

import io.opensphere.mantle.data.VisualizationSupport;

/**
 * The Interface VisualizationStyleRegistry.
 */
public interface VisualizationStyleRegistry
{
    /**
     * Adds the visualization style registry change listener.
     *
     * Note: Listeners are held as weak references, submitter should hold a hard
     * reference or the listener may be garbage collected.
     *
     * @param listener the listener
     */
    void addVisualizationStyleRegistryChangeListener(VisualizationStyleRegistryChangeListener listener);

    /**
     * Gets the default {@link VisualizationStyle} for a particular
     * {@link VisualizationSupport}, will search for the first applicable style
     * based on the MGS type, or its super types and interfaces until if finds
     * an applicable type, or eventually fails.
     *
     * Search is performed as follows:
     *
     * Submitted Direct Class A -&gt; Direct interfaces of class A that extend
     * {@link VisualizationSupport} -&gt; Direct super class of class A ( Class
     * A' ) -&gt; Direct interfaces of class A' that extend
     * {@link VisualizationSupport} -&gt; etc.
     *
     *
     * @param mgsClass the Class
     * @return the default style or null if no default style is found.
     */
    VisualizationStyle getDefaultStyle(Class<? extends VisualizationSupport> mgsClass);

    /**
     * Gets the default {@link VisualizationStyle} instance for a concrete
     * {@link VisualizationStyle} class that is in the registry.
     *
     * @param styleClass the {@link VisualizationStyle} class to search for.
     * @return the default {@link VisualizationStyle} instance for style class
     */
    VisualizationStyle getDefaultStyleInstanceForStyleClass(Class<? extends VisualizationStyle> styleClass);

    /**
     * Gets {@link Set} of all default style instances.
     *
     * @return the default styles set.
     */
    Set<VisualizationStyle> getDefaultStyles();

    /**
     * Gets the feature color for data type if there is an active style that
     * matches the input parameters that has a color.
     *
     * @param dtiKey the DataTypeInfo key.
     * @param vsSupportType the VisualizationSupport type class or null for the
     *            first active type.
     * @return the color or null if no active style with a color is found that
     *         matches the parameters.
     */
    Color getFeatureColorForActiveStyle(String dtiKey, Class<? extends VisualizationSupport> vsSupportType);

    /**
     * Gets {@link Set} of all feature classes overridden for a particular data
     * type ( not defaults ).
     *
     * @param dtiKey the data type key
     * @return the unmodifiable {@link Set} of feature classes.
     */
    Set<Class<? extends VisualizationSupport>> getFeatureTypes(String dtiKey);

    /**
     * Gets the {@link VisualizationStyle} for a specific.
     *
     * @param mgsCLass the {@link VisualizationSupport} class
     * @param dtiKey the Data Type key.
     * @param returnDefaultIfNoSpecificStyle the return default if no specific
     *            style
     * @return the style or null if none found. {@link VisualizationSupport}
     *         will search for the first applicable style based on the MGS type,
     *         or its super types and interfaces until if finds an applicable
     *         type, or eventually fails. Will not return the default type if a
     *         specific type is not found.
     *
     *         Uses the same search methodology as getDefaultStyle.
     */
    VisualizationStyle getStyle(Class<? extends VisualizationSupport> mgsCLass, String dtiKey,
            boolean returnDefaultIfNoSpecificStyle);

    /**
     * Gets {@link Set} of all style instances for a particular data type ( not
     * defaults ).
     *
     * @param dtiKey the data type key
     * @return the unmodifiable {@link Set} of styles
     */
    Set<VisualizationStyle> getStyles(String dtiKey);

    /**
     * Gets the all of the VisualizationStyle classes that are sub-classes of
     * the submitted style class.
     *
     * @param styleClass the style class to use to retrieve sub-classes
     * @return the unmodifiable {@link Set} of styles that are sub-types of the
     *         submitted style, may be empty but will never be null.
     */
    Set<Class<? extends VisualizationStyle>> getStylesForStyleType(Class<? extends VisualizationStyle> styleClass);

    /**
     * Install a VisualizationStyle.
     *
     * Provided the style is not already installed, it will be added and a
     * default instance will be created and managed by the registry. Will fire a
     * visualizationStyleInstalled to registry listeners.
     *
     * @param styleClass the style class to install
     * @param source the source installing the style
     * @return true, if successful, false if not installed
     */
    boolean installStyle(Class<? extends VisualizationStyle> styleClass, Object source);

    /**
     * Removes the visualization style registry change listener.
     *
     * @param listener the listener
     */
    void removeVisualizationStyleRegistryChangeListener(VisualizationStyleRegistryChangeListener listener);

    /**
     * Reset the style for a specific {@link VisualizationSupport} class for a
     * specific data type back to the default ( or next capturing type ).
     *
     * @param mgsClass the {@link VisualizationSupport} class
     * @param dtiKey the dti key
     * @param source the source
     */
    void resetStyle(Class<? extends VisualizationSupport> mgsClass, String dtiKey, Object source);

    /**
     * Sets the default style for a particular class of
     * {@link VisualizationSupport}, any VisualizationSupport classes that
     * inherits from the specified {@link VisualizationSupport} will use the
     * installed style unless it or one of its more direct descendants have an
     * installed style.
     *
     * Will fire a defaultStyleChanged if successful.
     *
     * @param mgsClass the {@link VisualizationSupport} class for which this
     *            style is to apply.
     * @param styleClass the style class ( will be installed if not already
     *            installed ).
     * @param source the source setting the default style.
     */
    void setDefaultStyle(Class<? extends VisualizationSupport> mgsClass, Class<? extends VisualizationStyle> styleClass,
            Object source);

    /**
     * Sets the style for a particular {@link VisualizationSupport} class type
     * for a particular DataType that is to override the default style.
     *
     * fires a {@link VisualizationStyleDatatypeChangeEvent} if the style is
     * changed.
     *
     * @param mgsClass the {@link VisualizationSupport} class for which the
     *            style applies.
     * @param dtiKey the Data Type key.
     * @param style the VisualizationStyle for the type
     * @param source the source setting the style
     * @return the previous style for this mgs class and dtiKey ( which could be
     *         the default style ).
     */
    VisualizationStyle setStyle(Class<? extends VisualizationSupport> mgsClass, String dtiKey, VisualizationStyle style,
            Object source);

    /**
     * The listener interface for receiving events and notifications from the
     * visualization style registry.
     */
    interface VisualizationStyleRegistryChangeListener
    {
        /**
         * Default style changed.
         *
         * @param mgsClass the mgs class
         * @param styleClass the style class
         * @param source the source
         */
        void defaultStyleChanged(Class<? extends VisualizationSupport> mgsClass, Class<? extends VisualizationStyle> styleClass,
                Object source);

        /**
         * Visualization style datatype changed.
         *
         * @param evt the evt
         */
        void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt);

        /**
         * Visualization style installed.
         *
         * @param styleClass the style class
         * @param source the source
         */
        void visualizationStyleInstalled(Class<? extends VisualizationStyle> styleClass, Object source);
    }
}
