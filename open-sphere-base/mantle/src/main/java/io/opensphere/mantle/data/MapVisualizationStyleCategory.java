package io.opensphere.mantle.data;

/**
 * The Enum MapVisualizationStyleCategory.
 */
public enum MapVisualizationStyleCategory
{
    /** The FEATURE. */
    FEATURE("Feature"),

    /** The IMAGE. */
    IMAGE("Image"),

    /** The IMAGE tile. */
    IMAGE_TILE("Tile"),

    /** LOCATION_FEATURE. */
    LOCATION_FEATURE("Location"),

    /** PATH_FEATURE. */
    PATH_FEATURE("Path"),

    /** TERRAIN TILE. */
    TERRAIN_TILE("Terrain Tile"),

    /**
     * Annotation feature.
     */
    ANNOTATION_FEATURE("Annotation"),

    /** The UNKNOWN. */
    UNKNOWN("Unknown");

    /** The Label. */
    private final String myLabel;

    /**
     * Instantiates a new map visualization style type.
     *
     * @param label the label
     */
    MapVisualizationStyleCategory(String label)
    {
        myLabel = label;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }
}
