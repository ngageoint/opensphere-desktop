package io.opensphere.mantle.data.impl;

import java.util.Set;
import java.util.stream.Collectors;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.StreamingSupport;

/**
 * Data group categorization utilities.
 */
public final class GroupCategorizationUtilities
{
    /**
     * Gets the one or more group categories for the data group info.
     *
     * @param dataGroup the {@link DataGroupInfo}
     * @param activeGroupsOnly whether to show only active groups
     * @return the {@link Set} of categories or (empty set for no category).
     */
    public static Set<String> getGroupCategories(DataGroupInfo dataGroup, boolean activeGroupsOnly)
    {
        Set<String> cats = New.set();
        addCategories(cats, dataGroup, activeGroupsOnly);
        addUnknownCats(cats, dataGroup, activeGroupsOnly);
        return cats;
    }

    /**
     * Gets the category string for the data group info, suitable for display purposes.
     *
     * @param dataGroup the {@link DataGroupInfo}
     * @param activeGroupsOnly whether to show only active groups
     * @return the category string.
     */
    public static String getCategoryString(DataGroupInfo dataGroup, boolean activeGroupsOnly)
    {
        return getGroupCategories(dataGroup, activeGroupsOnly).stream().map(input -> StringUtilities.trim(input, 's'))
                .collect(Collectors.joining(", "));
    }

    /**
     * Assign category.
     *
     * @param cats the categories
     * @param dataGroup the data group
     * @param activeGroupsOnly whether to show only active groups
     */
    private static void addCategories(Set<String> cats, DataGroupInfo dataGroup, boolean activeGroupsOnly)
    {
        Set<MapVisualizationType> types = dataGroup.getMembers(false).stream().filter(t -> t.getMapVisualizationInfo() != null)
                .map(t -> t.getMapVisualizationInfo().getVisualizationType()).collect(Collectors.toSet());

        boolean tileLayer = types.contains(MapVisualizationType.IMAGE_TILE);
        boolean featureLayer = types.stream().anyMatch(t -> t != null && t.isMapDataElementType())
                || dataGroup.hasMember(dt -> dt.getBasicVisualizationInfo().usesDataElements(), false);
        if (types.stream().anyMatch(MapVisualizationType.IS_ANNOTATION_LAYER_PREDICATE))
        {
            cats.add(LayerType.MYPLACES.getLabel());
        }
        else if (types.contains(MapVisualizationType.TRACK_ELEMENTS))
        {
            cats.add(LayerType.TRACKS.getLabel());
        }
        else if (tileLayer && featureLayer)
        {
            cats.add(LayerType.TILE_AND_FEATURE.getLabel());
        }
        else if (tileLayer)
        {
            cats.add(LayerType.TILE_LAYERS.getLabel());
        }
        else if (featureLayer)
        {
            cats.add(LayerType.FEATURE_LAYERS.getLabel());
        }
        else if (types.contains(MapVisualizationType.PLACE_NAME_ELEMENTS))
        {
            cats.add(LayerType.PLACE_NAMES.getLabel());
        }
        else if (types.contains(MapVisualizationType.PROCESS_RESULT_ELEMENTS))
        {
            cats.add(LayerType.ANALYTICS.getLabel());
        }
        else
        {
            cats.add(dataGroup.getDisplayName());
        }

        if (types.contains(MapVisualizationType.IMAGE))
        {
            cats.add(LayerType.IMAGERY.getLabel());
        }
        if (types.contains(MapVisualizationType.TERRAIN_TILE))
        {
            cats.add(LayerType.TERRAIN.getLabel());
        }
        if (types.contains(MapVisualizationType.MOTION_IMAGERY) || types.contains(MapVisualizationType.MOTION_IMAGERY_DATA))
        {
            cats.add(LayerType.MOTION_IMAGERY.getLabel());
        }
        if (types.contains(MapVisualizationType.INTERPOLATED_IMAGE_TILES))
        {
            cats.add(LayerType.HEATMAP.getLabel());
        }
        if (!activeGroupsOnly && !dataGroup.findMembers(StreamingSupport.IS_STREAMING_ENABLED, false, true).isEmpty())
        {
            cats.add(LayerType.STREAMING.getLabel());
        }
    }

    /**
     * Adds the unknown categories.
     *
     * @param cats the categories
     * @param dataGroup the data group
     * @param activeGroupsOnly whether to show only active groups
     */
    private static void addUnknownCats(Set<String> cats, DataGroupInfo dataGroup, boolean activeGroupsOnly)
    {
        if (cats.isEmpty() && !dataGroup.isFlattenable())
        {
            for (DataGroupInfo child : dataGroup.getChildren())
            {
                Set<String> childCats = getGroupCategories(child, activeGroupsOnly);
                if (!childCats.isEmpty())
                {
                    cats.addAll(childCats);
                }
            }
        }

        if (cats.isEmpty())
        {
            cats.add(LayerType.UNKNOWN.getLabel());
        }
    }

    /** Disallow instantiation. */
    private GroupCategorizationUtilities()
    {
    }

    /**
     * The Enum LayerType.
     */
    public enum LayerType
    {
        /** The analytics type. */
        ANALYTICS("Analytics"),

        /** The feature layers type. */
        FEATURE_LAYERS("Feature Layers"),

        /** The imagery type. */
        IMAGERY("Imagery"),

        /** The motion imagery type. */
        MOTION_IMAGERY("Motion Imagery"),

        /** The my places type. */
        MYPLACES("My Places"),

        /** The place names type. */
        PLACE_NAMES("Place Names"),

        /** The streaming type. */
        STREAMING("Streaming"),

        /** The terrain type. */
        TERRAIN("Terrain"),

        /** The tile and feature groups type. */
        TILE_AND_FEATURE("Tile and Feature Groups"),

        /** The tile layers type. */
        TILE_LAYERS("Tile Layers"),

        /** The tracks type. */
        TRACKS("Tracks"),

        /** The heatmaps type. */
        HEATMAP("Heatmaps"),

        /** The unknown type. */
        UNKNOWN("Unknown"),

        ;

        /** The Label. */
        private final String myLabel;

        /**
         * Instantiates a new layer type.
         *
         * @param label the label
         */
        LayerType(String label)
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
}
