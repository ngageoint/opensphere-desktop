package io.opensphere.mantle.data.geom.style;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Interface VisualizationStyleController.
 */
public interface VisualizationStyleController
{
    /**
     * Checks the configuration for the dtnKey and feature class and trys to get
     * the selected class name, if not found ( i.e. not in config ). Then tries
     * to get the selected type from the registry, data type specific if the
     * type is specific, or default if not.
     *
     * @param featureClass the {@link MapGeometrySupport} feature class
     * @param dgi the {@link DataGroupInfo} for the type
     * @param dti the {@link DataTypeInfo} for the type
     * @return the VisualizationStyle class or null if not found.
     */
    Class<? extends VisualizationStyle> getSelectedVisualizationStyleClass(Class<? extends VisualizationSupport> featureClass,
            DataGroupInfo dgi, DataTypeInfo dti);

    /**
     * Gets the style for editor with config values from the configuration if it
     * is not currently the style installed and activated for the data type in
     * the registry.
     *
     * First tries to pull the style for the dti key and feature class from the
     * registry, if not retrieves the default instance from the registry and
     * derives a new style instance for the data type updated with the config
     * values.
     *
     * Note: Always returns a new style instance, not the same object that is in
     * the registry.
     *
     * @param vsClass the {@link VisualizationStyle} class
     * @param featureClass the {@link MapGeometrySupport} feature class
     * @param dgi the {@link DataGroupInfo} for the type
     * @param dti the {@link DataTypeInfo} for the type
     * @return the style for editor with config values
     */
    VisualizationStyle getStyleForEditorWithConfigValues(Class<? extends VisualizationStyle> vsClass,
            Class<? extends VisualizationSupport> featureClass, DataGroupInfo dgi, DataTypeInfo dti);

    /**
     * Checks if is type using custom.
     *
     * @param dgi the {@link DataGroupInfo} for the type
     * @param dti the {@link DataTypeInfo} for the type
     * @return true, if is type using custom
     */
    boolean isTypeUsingCustom(DataGroupInfo dgi, DataTypeInfo dti);

    /**
     * Removes the style for a given data group's data type.
     *
     * @param dgi The data group info to remove styles for.
     * @param dti The data type info to remove styles for.
     */
    void removeStyle(DataGroupInfo dgi, DataTypeInfo dti);

    /**
     * Reset all style settings, clears all style settings for all types and
     * sets all types back to default.
     *
     * @param source the source of the change request
     */
    void resetAllStyleSettings(Object source);

    /**
     * Reset style for a style/feature/dgi/dti combo in the config and if in
     * use, in the registry.
     *
     * @param aStyle the {@link VisualizationStyle} style to be reset .
     * @param featureClass the {@link MapGeometrySupport} feature class
     * @param dgi the {@link DataGroupInfo} for the type
     * @param dti the {@link DataTypeInfo} for the type
     */
    void resetStyle(VisualizationStyle aStyle, Class<? extends MapGeometrySupport> featureClass, DataGroupInfo dgi,
            DataTypeInfo dti);

    /**
     * Sets the selected style class for a data type.
     *
     * First alters the configuration with the selected style class, and also
     * preserves the parameter set from the passed in style.
     *
     * Then if the style is for a specific data type or a default type and the
     * registry needs to be updated updates the registry.
     *
     * @param aStyle the {@link VisualizationStyle} to switch to as current.
     * @param featureClass the {@link MapGeometrySupport} feature class to apply
     *            to
     * @param dgi the {@link DataGroupInfo} for the type
     * @param dti the {@link DataTypeInfo} for the type
     * @param source the source of the change request
     */
    void setSelectedStyleClass(VisualizationStyle aStyle, Class<? extends VisualizationSupport> featureClass, DataGroupInfo dgi,
            DataTypeInfo dti, Object source);

    /**
     * Updates the configuration to reflect that a data type is to use custom or
     * default, then either deactivates the custom types in the registry, or
     * installs and activates the custom types for a data type in the registry.
     *
     * @param dgi the {@link DataGroupInfo} for the type
     * @param dti the {@link DataTypeInfo} for the type
     * @param useCustom the use custom style, false to revert to the default
     *            style.
     * @param source the source of the change request
     */
    void setUseCustomStyleForDataType(DataGroupInfo dgi, DataTypeInfo dti, boolean useCustom, Object source);

    /**
     * Updates the style in the config to reflect changes from the editor, if
     * active updates/replaces the style in the registry.
     *
     * @param aStyle the {@link VisualizationStyle} class that is being updated.
     * @param featureClass the {@link MapGeometrySupport} feature class.
     * @param dgi the {@link DataGroupInfo} for the type
     * @param dti the {@link DataTypeInfo} for the type
     * @param source the source of the change request
     */
    void updateStyle(VisualizationStyle aStyle, Class<? extends VisualizationSupport> featureClass, DataGroupInfo dgi,
            DataTypeInfo dti, Object source);
}
