package io.opensphere.mantle.data.geom.style;

import java.util.Set;

import io.opensphere.mantle.data.VisualizationSupport;

/**
 * The Interface DataTypeStyleConfiguration.
 *
 * Holds the style information for a specific data type that over-rides the
 * default styles in the {@link VisualizationStyleRegistry}
 */
public interface DataTypeStyleConfiguration
{
    /**
     * Clears all styles from this data type specific override.
     */
    void clear();

    /**
     * Gets the Data type key.
     *
     * @return the data type key.
     */
    String getDTIKey();

    /**
     * Gets the set of all the feature type classes overridden by this
     * configuration.
     *
     * @return the feature types.
     */
    Set<Class<? extends VisualizationSupport>> getFeatureTypes();

    /**
     * Gets the set of all {@link VisualizationSupport} classes that were
     * overridden in this style config.
     *
     * @return the MGS classes
     */
    Set<Class<? extends VisualizationSupport>> getMGSClasses();

    /**
     * Gets the style for a specific {@link VisualizationSupport} class. Uses
     * the same search methodology as the VisulaizationStyleRegistry to find the
     * applicable style type.
     *
     *
     * @param mgsClass the {@link VisualizationSupport} class to search for.
     * @return the style or null if none found.
     */
    VisualizationStyle getStyle(Class<? extends VisualizationSupport> mgsClass);

    /**
     * Gets the set of all styles overridden by this configuration.
     *
     * @return the styles
     */
    Set<VisualizationStyle> getStyles();

    /**
     * Checks to see if the configuration has a style for a specific
     * {@link VisualizationSupport} class. Uses the same search methodology as
     * the VisulaizationStyleRegistry to find the applicable style type.
     *
     * @param mgsClass the {@link VisualizationSupport} class to search for.
     * @return true, if there is an applicable style false if not.
     */
    boolean hasStyle(Class<? extends VisualizationSupport> mgsClass);

    /**
     * Removes the style from the type specific configuration. No search is
     * performed only removes for the directly referenced class.
     *
     * @param mgsClass the mgs class
     * @return the visualization style that is removed or null if none removed.
     */
    VisualizationStyle removeStyle(Class<? extends VisualizationSupport> mgsClass);

    /**
     * Removes the specific {@link VisualizationStyle} from the configuration
     * regardless of which {@link VisualizationSupport} class is using it.
     *
     * @param style the style to remove
     * @return the visualization style that was removed or null if none was
     *         removed.
     */
    VisualizationStyle removeStyle(VisualizationStyle style);

    /**
     * Sets the style for a specific {@link VisualizationSupport} class ( and
     * its descendants ).
     *
     * @param mgsClass the {@link VisualizationSupport} class
     * @param style the style
     */
    void setStyle(Class<? extends VisualizationSupport> mgsClass, VisualizationStyle style);
}
