package io.opensphere.mantle.data.geom.style;

import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.VisualizationSupport;

/**
 * The Interface VisualizationStyle.
 */
public interface VisualizationStyle extends Cloneable
{
    /** The Constant NO_EVENT_SOURCE. */
    Object NO_EVENT_SOURCE = new Object();

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    Toolbox getToolbox();

    /**
     * Reverts the style to the default parameters.
     *
     * @param source the source making the change.
     */
    void revertToDefaultParameters(Object source);

    /**
     * Sets the parameter.
     *
     * @param paramKey the param key
     * @param newValue the new value
     * @param source the source (if the NO_EVENT_SOURCE) no change event will be
     *            sent.
     * @return true, if parameter changed as result of set.
     * @throws IllegalArgumentException the illegal argument exception if the
     *             type of value is not valid for the specified parameter.
     */
    boolean setParameter(String paramKey, Object newValue, Object source) throws IllegalArgumentException;

    /**
     * Sets the parameters if they are valid parameters for this style. Only
     * parameters with changes will be set and be included in the events.
     *
     * @param parameters the parameters to change.
     * @param source the source making the change.
     * @return the set of parameters that were changed as a result of this call.
     */
    Set<VisualizationStyleParameter> setParameters(Set<VisualizationStyleParameter> parameters, Object source);

    /**
     * Adds the style parameter change listener.
     *
     * @param listener the listener
     */
    void addStyleParameterChangeListener(VisualizationStyleParameterChangeListener listener);

    /**
     * Returns a clone of this VisualizationStyle.
     *
     * @return the cloned style.
     */
    VisualizationStyle clone();

    /**
     * Derives a clone of the style, but for a specific data type.
     *
     * @param dtiKey the dti key
     * @return the visualization style
     */
    VisualizationStyle deriveForType(String dtiKey);

    /**
     * Gets the set of {@link VisualizationStyleParameter}s that should always
     * be saved regardless of whether or not they are the same as the default
     * values.
     *
     * This is in here to handle the case where the default can change session
     * to session and we want to avoid not being able to reproduce the user's
     * values.
     *
     * @return the always save parameters
     */
    Set<VisualizationStyleParameter> getAlwaysSaveParameters();

    /**
     * Get the set of parameters of like {@link VisualizationStyle} types that
     * are different between this type and the provided type.
     *
     * @param other the {@link VisualizationStyle}
     * @return the {@link Set} of {@link VisualizationStyleParameter}s with
     *         changes.
     */
    Set<VisualizationStyleParameter> getChangedParameters(VisualizationStyle other);

    /**
     * Gets the converted VisualizationSupport class.
     *
     * @return the converted VisualizationSupport class
     */
    Class<? extends VisualizationSupport> getConvertedClassType();

    /**
     * Gets the DataTypeInfo key.
     *
     * @return the Data TypeInfo key or null if not associated with a data type.
     */
    String getDTIKey();

    /**
     * Sets the dTI key.
     *
     * @param dtiKey the new dTI key
     */
    void setDTIKey(String dtiKey);

    /**
     * Gets a smaller version of a clean User interface panel for this
     * VisualizationStyle where changes to the UI will alter the underlying
     * parameters.
     *
     * @return the uI panel
     */
    FeatureVisualizationControlPanel getMiniUIPanel();

    /**
     * Gets the style category.
     *
     * @return the style category
     */
    MapVisualizationStyleCategory getStyleCategory();

    /**
     * Gets the style description.
     *
     * @return the style description
     */
    String getStyleDescription();

    /**
     * Gets the style name.
     *
     * @return the style name
     */
    String getStyleName();

    /**
     * Gets the style parameter by parameter key.
     *
     * @param paramKey the parameter key
     * @return the style parameter or null if no parameter with that value.
     */
    VisualizationStyleParameter getStyleParameter(String paramKey);

    /**
     * Gets the set of all parameter keys for this style.
     *
     * @return the style parameter keys
     */
    Set<String> getStyleParameterKeys();

    /**
     * Gets the style parameter set.
     *
     * @return the unmodifiable style parameter set
     */
    Set<VisualizationStyleParameter> getStyleParameterSet();

    /**
     * Gets the style parameter by parameter key.
     *
     * @param paramKey the parameter key
     * @return the style parameter value ( could be null if parameter does not
     *         exist, or if the value is null ).
     */
    Object getStyleParameterValue(String paramKey);

    /**
     * Gets a clean User interface panel for this VisualizationStyle where
     * changes to the UI will alter the underlying parameters.
     *
     * @return the uI panel
     */
    FeatureVisualizationControlPanel getUIPanel();

    /**
     * Initialize the parameter set for a style to its default. (Can also be
     * used to reset the parameter set to default values on a style).
     */
    void initialize();

    /**
     * Initialize the style with the given set of parameters. Each style will
     * select the parameters that are relevant to that style from the set, all
     * others will be ignored. The values in this set will override any
     * defaults. Guaranteed that initialize() will be called first.
     *
     * @param paramSet the parameter set
     */
    void initialize(Set<VisualizationStyleParameter> paramSet);

    /**
     * Initialize the style with values from the data type. Called after
     * initialize() but before initialize(Set paramSet) only when this type is
     * for a specific data type.
     */
    void initializeFromDataType();

    /**
     * Creates a totally new instance of this style in its initialized state.
     *
     * @param tb the {@link Toolbox}
     * @return the visualization style
     */
    VisualizationStyle newInstance(Toolbox tb);

    /**
     * Removes the style parameter change listener.
     *
     * @param listener the listener
     */
    void removeStyleParameterChangeListener(VisualizationStyleParameterChangeListener listener);

    /**
     * Requires shaders.
     *
     * @return true if this style requires shaders.
     */
    boolean requiresShaders();
}
