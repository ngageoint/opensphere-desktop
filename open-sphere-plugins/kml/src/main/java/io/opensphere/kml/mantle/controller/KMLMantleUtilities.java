package io.opensphere.kml.mantle.controller;

import java.awt.Color;
import java.util.function.Function;

import io.opensphere.core.Toolbox;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.ScalingMethod;
import io.opensphere.kml.common.util.KMLToolbox;
import io.opensphere.kml.mantle.model.KMLMantleFeature;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;

/**
 * Utilities for the Mantle.
 */
public final class KMLMantleUtilities
{
    /** The KML constant. */
    public static final String KML = "KML";

    /** The toolbox. */
    private static Toolbox ourToolbox;

    /** The Mantle toolbox. */
    private static MantleToolbox ourMantleToolbox;

    /** The KML toolbox. */
    private static KMLToolbox ourKMLToolbox;

    /**
     * Creates a data group for a KML data source.
     *
     * @param dataSource The data source
     * @return The data group info
     */
    public static DataGroupInfo createDataGroup(KMLDataSource dataSource)
    {
        KMLDataGroupInfo group = null;

        // Use the specified group
        if (dataSource.getOverrideDataGroupKey() != null)
        {
            group = (KMLDataGroupInfo)DefaultDataGroupInfo.getKeyMap().getGroupForKey(dataSource.getOverrideDataGroupKey());
        }
        // Create our own group
        else
        {
            group = new KMLDataGroupInfo(false, ourToolbox, KML, dataSource.getDataGroupKey(), dataSource.getName());
            group.setKMLDataSource(dataSource);
            ourKMLToolbox.getMasterGroup().addChild(group, null);
            LoadsTo loadsTo = dataSource.isIncludeInTimeline() ? LoadsTo.TIMELINE : LoadsTo.STATIC;
            DataTypeInfo dataType = createDataType(dataSource, loadsTo, Color.WHITE);
            if (dataSource.isTransient())
            {
                dataType.setVisible(dataSource.isVisible(), group);
            }
            group.addMember(dataType, null);
        }

        return group;
    }

    /**
     * Creates a data type for the data source.
     *
     * @param dataSource The data source
     * @param loadsTo The loadsTo value
     * @param defaultColor The default color for the features.
     * @return The DataTypeInfo
     */
    public static DataTypeInfo createDataType(KMLDataSource dataSource, LoadsTo loadsTo, Color defaultColor)
    {
        DefaultDataTypeInfo dataType = new DefaultDataTypeInfo(ourToolbox, KML, dataSource.getDataTypeKey(), KML,
                dataSource.getName(), false);

        // Create metadata info
        DefaultMetaDataInfo metaDataInfo = KMLMantleFeature.newMetaDataInfo();
        metaDataInfo.copyKeysToOriginalKeys();
        dataType.setMetaDataInfo(metaDataInfo);

        // Create basic visualization info
        BasicVisualizationInfo basicVisInfo = new DefaultBasicVisualizationInfo(loadsTo,
                DefaultBasicVisualizationInfo.LOADS_TO_STATIC_AND_TIMELINE, defaultColor, true);
        dataType.setBasicVisualizationInfo(basicVisInfo);

        // Create map visualization info
        MapVisualizationInfo mapVisInfo = new DefaultMapFeatureVisualizationInfo(MapVisualizationType.MIXED_ELEMENTS, false);

        dataType.setMapVisualizationInfo(mapVisInfo);

        dataType.setTimeExtents(new DefaultTimeExtents(), null);

        dataType.setUrl(dataSource.getPath());

        return dataType;
    }

    /**
     * Initializes and creates the master KML group.
     *
     * @param toolbox the toolbox
     */
    public static void init(Toolbox toolbox)
    {
        ourToolbox = toolbox;
        ourMantleToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        ourKMLToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(KMLToolbox.class);

        DefaultDataGroupInfo masterGroup = new DefaultDataGroupInfo(true, toolbox, KML, KML, "KML Files");
        ourKMLToolbox.setMasterGroup(masterGroup);
        ourMantleToolbox.getDataGroupController().addRootDataGroupInfo(masterGroup, KMLMantleUtilities.class);
    }

    /**
     * Determines if a feature really should be visible.
     *
     * @param feature the feature
     * @return Whether the feature should be visible
     */
    public static boolean isFeatureVisible(KMLFeature feature)
    {
        boolean isVisible = feature.isVisibility().booleanValue() && feature.isRegionActive();
        if (isVisible)
        {
            KMLDataSource dataSource = feature.getCreatingDataSource();
            DataTypeInfo dataType = ourMantleToolbox.getDataTypeController().getDataTypeInfoForType(dataSource.getDataTypeKey());
            isVisible = dataType != null && dataType.isVisible();
        }
        return isVisible;
    }

    /**
     * Removes the data group for a KML data source.
     *
     * @param dataSource The data group
     */
    public static void removeDataGroup(KMLDataSource dataSource)
    {
        DataGroupInfo group = dataSource.getDataGroupInfo();
        // Remove our own group
        if (dataSource.getOverrideDataGroupKey() == null)
        {
            ourKMLToolbox.getMasterGroup().removeChild(group, null);
            ourMantleToolbox.getDataGroupController().cleanUpGroup(group);
        }
        for (DataTypeInfo dataType : group.getMembers(false))
        {
            ourMantleToolbox.getDataTypeController().removeDataType(dataType, null);
            group.removeMember(dataType, false, null);
        }
    }

    /**
     * Gets the scale function for the data source.
     *
     * @param dataSource the data source
     * @return the scale function
     */
    public static Function<Kilometers, Float> getScaleFunction(KMLDataSource dataSource)
    {
        ScalingMethod scalingMethod = dataSource.getScalingMethod() == ScalingMethod.DEFAULT
                ? ourKMLToolbox.getSettings().getScalingMethod() : dataSource.getScalingMethod();
        return scalingMethod == ScalingMethod.GOOGLE_EARTH ? GoogleEarthScaleFunction.INSTANCE : null;
    }

    /** Disallow instantiation. */
    private KMLMantleUtilities()
    {
    }
}
