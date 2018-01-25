package io.opensphere.mantle.data.geom.style.dialog;

import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;

/**
 * The Class StyleTypeInfo.
 */
public class StyleTypeInfo
{
    /** The Base mgs class. */
    private final Class<? extends MapGeometrySupport> myBaseMGSClass;

    /** The Data type. */
    private final DataTypeNodeUserObject myDataType;

    /** The Default style instance. */
    private final VisualizationStyle myDefaultStyleInstance;

    /** The Style class. */
    private final Class<? extends VisualizationStyle> myStyleClass;

    /**
     * Instantiates a new style select event.
     *
     * @param dataType the data type
     * @param styleClass the style class
     * @param defaultStyleInstance the default style instance
     * @param baseMGSClass the base mgs class
     */
    public StyleTypeInfo(DataTypeNodeUserObject dataType, Class<? extends VisualizationStyle> styleClass,
            VisualizationStyle defaultStyleInstance, Class<? extends MapGeometrySupport> baseMGSClass)
    {
        myDataType = dataType;
        myStyleClass = styleClass;
        myDefaultStyleInstance = defaultStyleInstance;
        myBaseMGSClass = baseMGSClass;
    }

    /**
     * Gets the base mgs class.
     *
     * @return the base mgs class
     */
    protected Class<? extends MapGeometrySupport> getBaseMGSClass()
    {
        return myBaseMGSClass;
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    protected DataTypeNodeUserObject getDataType()
    {
        return myDataType;
    }

    /**
     * Gets the default style instance.
     *
     * @return the default style instance
     */
    protected VisualizationStyle getDefaultStyleInstance()
    {
        return myDefaultStyleInstance;
    }

    /**
     * Gets the style class.
     *
     * @return the style class
     */
    protected Class<? extends VisualizationStyle> getStyleClass()
    {
        return myStyleClass;
    }
}
