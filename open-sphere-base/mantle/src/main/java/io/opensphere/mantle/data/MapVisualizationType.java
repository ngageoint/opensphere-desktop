package io.opensphere.mantle.data;

import java.util.function.Predicate;

/**
 * MapVisualizationType.
 */
public enum MapVisualizationType
{
    /** Type for user annotations. */
    ANNOTATION_POINTS(MapVisualizationStyleCategory.ANNOTATION_FEATURE),

    /** Type for user regions of interest. */
    ANNOTATION_REGIONS(MapVisualizationStyleCategory.ANNOTATION_FEATURE),

    /** Circle types. */
    CIRCLE_ELEMENTS(MapVisualizationStyleCategory.LOCATION_FEATURE),

    /** The COMPOUND_ELEMENTS. */
    COMPOUND_FEATURE_ELEMENTS(MapVisualizationStyleCategory.FEATURE),

    /** Ellipse types. */
    ELLIPSE_ELEMENTS(MapVisualizationStyleCategory.LOCATION_FEATURE),

    /** Icon elements. */
    ICON_ELEMENTS(MapVisualizationStyleCategory.LOCATION_FEATURE),

    /** The type used for images. */
    IMAGE(MapVisualizationStyleCategory.IMAGE),

    /** The type used for tiles composed of images. */
    IMAGE_TILE(MapVisualizationStyleCategory.IMAGE_TILE),

    /** LOB types. */
    LOB_ELEMENTS(MapVisualizationStyleCategory.LOCATION_FEATURE),

    /** The type used for items composed of several different types. */
    MIXED_ELEMENTS(MapVisualizationStyleCategory.FEATURE),

    /** The motion imagery type. */
    MOTION_IMAGERY(MapVisualizationStyleCategory.IMAGE),

    /** The motion imagery meta data type. */
    MOTION_IMAGERY_DATA(MapVisualizationStyleCategory.FEATURE),

    /** The type used for place names. */
    PLACE_NAME_ELEMENTS(MapVisualizationStyleCategory.LOCATION_FEATURE),

    /** The type used for feature types. */
    POINT_ELEMENTS(MapVisualizationStyleCategory.LOCATION_FEATURE),

    /** The type used for polygons. */
    POLYGON_ELEMENTS(MapVisualizationStyleCategory.PATH_FEATURE),

    /** The POLYLINE. */
    POLYLINE_ELEMENTS(MapVisualizationStyleCategory.PATH_FEATURE),

    /** The type used for terrain data. */
    TERRAIN_TILE(MapVisualizationStyleCategory.TERRAIN_TILE),

    /** The type used for track data. */
    TRACK_ELEMENTS(MapVisualizationStyleCategory.PATH_FEATURE),

    /** The type used for unknown data. */
    UNKNOWN(MapVisualizationStyleCategory.UNKNOWN),

    /** Type for user-created tracks. */
    USER_TRACK_ELEMENTS(MapVisualizationStyleCategory.PATH_FEATURE),

    /** The type used for the results of server-side processes. */
    PROCESS_RESULT_ELEMENTS(MapVisualizationStyleCategory.LOCATION_FEATURE),

    /** The type used for visualizing tiles composed of interpolated data. */
    INTERPOLATED_IMAGE_TILES(MapVisualizationStyleCategory.IMAGE);

    /** Predicate for testing if this is a map data element type. */
    public static final Predicate<MapVisualizationType> IS_MAP_DATA_ELEMENT_TYPE_PREDICATE = type -> type != null
            && type.isMapDataElementType();

    /** Predicate for testing if this is an annotation layer type. */
    public static final Predicate<MapVisualizationType> IS_ANNOTATION_LAYER_PREDICATE = type -> type == MapVisualizationType.ANNOTATION_POINTS
            || type == MapVisualizationType.ANNOTATION_REGIONS || type == MapVisualizationType.USER_TRACK_ELEMENTS;

    /** The Style category. */
    private final MapVisualizationStyleCategory myStyleCategory;

    /**
     * Instantiates a new map visualization type.
     *
     * @param category the category
     */
    private MapVisualizationType(MapVisualizationStyleCategory category)
    {
        myStyleCategory = category;
    }

    /**
     * Gets the style category.
     *
     * @return the style category
     */
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return myStyleCategory;
    }

    /**
     * Checks if is compound type.
     *
     * @return true, if is compound type
     */
    public boolean isCompoundType()
    {
        return this == COMPOUND_FEATURE_ELEMENTS;
    }

    /**
     * Checks if is image tile type.
     *
     * @return true, if is image tile type
     */
    public boolean isImageTileType()
    {
        return this == IMAGE_TILE;
    }

    /**
     * Checks if is image type.
     *
     * @return true, if is image type
     */
    public boolean isImageType()
    {
        return this == IMAGE;
    }

    /**
     * Checks if this is a heatmap type.
     *
     * @return true if this is a heatmap type.
     */
    public boolean isHeatmapType()
    {
        return this == INTERPOLATED_IMAGE_TILES;
    }

    /**
     * Checks if is MapDataElement type.
     *
     * @return true, if is data element type
     */
    public boolean isMapDataElementType()
    {
        boolean isDEType = false;
        switch (this)
        {
            case COMPOUND_FEATURE_ELEMENTS:
            case CIRCLE_ELEMENTS:
            case ELLIPSE_ELEMENTS:
            case ICON_ELEMENTS:
            case LOB_ELEMENTS:
            case MIXED_ELEMENTS:
            case POINT_ELEMENTS:
            case POLYGON_ELEMENTS:
            case POLYLINE_ELEMENTS:
            case TRACK_ELEMENTS:
            case USER_TRACK_ELEMENTS:
            case PROCESS_RESULT_ELEMENTS:
            case MOTION_IMAGERY:
            case MOTION_IMAGERY_DATA:
                isDEType = true;
                break;
            default:
                break;
        }
        return isDEType;
    }

    /**
     * Checks if is path type.
     *
     * @return true, if is path type
     */
    public boolean isPathType()
    {
        boolean isPathType = false;
        switch (this)
        {
            case POLYGON_ELEMENTS:
            case POLYLINE_ELEMENTS:
            case TRACK_ELEMENTS:
            case USER_TRACK_ELEMENTS:
                isPathType = true;
                break;
            default:
                break;
        }
        return isPathType;
    }

    /**
     * Checks if is single location type.
     *
     * @return true, if is single location type
     */
    public boolean isSingleLocationType()
    {
        boolean isDEType = false;
        switch (this)
        {
            case ELLIPSE_ELEMENTS:
            case CIRCLE_ELEMENTS:
            case LOB_ELEMENTS:
            case POINT_ELEMENTS:
            case ICON_ELEMENTS:
            case MIXED_ELEMENTS:
                isDEType = true;
                break;
            default:
                break;
        }
        return isDEType;
    }

    /**
     * Checks if is terrain tile type.
     *
     * @return true, if is terrain tile type
     */
    public boolean isTerrainTileType()
    {
        return this == TERRAIN_TILE;
    }
}
