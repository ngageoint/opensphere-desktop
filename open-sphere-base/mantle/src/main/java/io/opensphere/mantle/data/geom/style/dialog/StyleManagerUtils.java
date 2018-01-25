package io.opensphere.mantle.data.geom.style.dialog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import avro.shaded.com.google.common.base.Objects;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.InterpolatedTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.LocationVisualizationStyle;
import io.opensphere.mantle.data.geom.style.TileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.geom.style.config.v1.StyleParameterConfig;
import io.opensphere.mantle.data.geom.style.config.v1.StyleParameterSetConfig;
import io.opensphere.mantle.data.geom.style.config.v1.UnsupportedClassConversionError;
import io.opensphere.mantle.data.geom.style.dialog.DataTypeNodeUserObject.NodeKeyComponent;
import io.opensphere.mantle.data.geom.style.dialog.DataTypeNodeUserObject.NodeType;
import io.opensphere.mantle.data.geom.style.impl.PolygonFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PolylineFeatureVisualizationStyle;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.tile.InterpolatedTileVisualizationSupport;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class StyleManagerUtils.
 */
@SuppressWarnings("PMD.GodClass")
public final class StyleManagerUtils
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(StyleManagerUtils.class);

    /**
     * Creates the style node list that we will use.
     *
     * @param tb the {@link Toolbox}
     * @param baseStyleClass the base style class
     * @param baseMGSClass the base mgs class
     * @param dgi the {@link DataGroupInfo}
     * @param dti the {@link DataTypeInfo}
     * @return the list
     */
    public static List<StyleNodeUserObject> createStyleNodeList(Toolbox tb, Class<? extends VisualizationStyle> baseStyleClass,
            Class<? extends VisualizationSupport> baseMGSClass, DataGroupInfo dgi, DataTypeInfo dti)
    {
        VisualizationStyleRegistry vsr = MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleRegistry();
        VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleController();

        Set<Class<? extends VisualizationStyle>> styleSet = vsr.getStylesForStyleType(baseStyleClass);

        List<StyleNodeUserObject> nodeList = New.list();
        for (Class<? extends VisualizationStyle> styleClass : styleSet)
        {
            VisualizationStyle defaultInstance = vsc.getStyleForEditorWithConfigValues(styleClass, baseMGSClass, dgi, dti);

            boolean addNodeFlag = true;
            if (defaultInstance instanceof FeatureVisualizationStyle)
            {
                FeatureVisualizationStyle fvs = (FeatureVisualizationStyle)defaultInstance;
                MapVisualizationType mvt = null;
                if (dti != null && dti.getMapVisualizationInfo() != null)
                {
                    mvt = dti.getMapVisualizationInfo().getVisualizationType();
                }

                Set<MapVisualizationType> types = fvs.getRequiredMapVisTypes();
                if (mvt != null && !types.isEmpty() && !types.contains(mvt))
                {
                    addNodeFlag = false;
                }
            }

            if (addNodeFlag)
            {
                nodeList.add(new StyleNodeUserObject(styleClass, defaultInstance, baseMGSClass));
            }
        }
        Collections.sort(nodeList, (uo1, uo2) -> uo1.toString().compareTo(uo2.toString()));
        return nodeList;
    }

    /**
     * Gets the base style classes for feature class.
     *
     * @param featureClass the feature class
     * @return the base style classes for feature class
     */
    public static Class<? extends VisualizationStyle> getBaseStyleClassesForFeatureClass(
            Class<? extends VisualizationSupport> featureClass)
    {
        Class<? extends VisualizationStyle> result = null;

        if (EqualsHelper.equals(featureClass, MapLocationGeometrySupport.class))
        {
            result = LocationVisualizationStyle.class;
        }
        else if (EqualsHelper.equals(featureClass, MapPolylineGeometrySupport.class))
        {
            result = PolylineFeatureVisualizationStyle.class;
        }
        else if (EqualsHelper.equals(featureClass, MapPolygonGeometrySupport.class))
        {
            result = PolygonFeatureVisualizationStyle.class;
        }
        else if (EqualsHelper.equals(featureClass, TileVisualizationSupport.class))
        {
            result = TileVisualizationStyle.class;
        }
        else if (EqualsHelper.equals(featureClass, InterpolatedTileVisualizationSupport.class))
        {
            result = InterpolatedTileVisualizationStyle.class;
        }
        return result;
    }

    /**
     * Gets the base style classes for tile class.
     *
     * @param tileClass the tile class
     * @return the base style classes for feature class
     */
    public static Class<? extends VisualizationStyle> getBaseStyleClassesForTileClass(
            Class<? extends TileVisualizationSupport> tileClass)
    {
        return TileVisualizationStyle.class;
    }

    /**
     * Gets the base style classes for tile class.
     *
     * @param tileClass the tile class
     * @return the base style classes for feature class
     */
    public static Class<? extends VisualizationStyle> getBaseStyleClassesForHeatmapClass(
            Class<? extends InterpolatedTileVisualizationSupport> tileClass)
    {
        return InterpolatedTileVisualizationStyle.class;
    }

    /**
     * Gets the classes for data type.
     *
     * @param dti the dti
     * @param list the list
     */
    private static void getClassesForDataType(DataTypeInfo dti, List<Class<? extends VisualizationSupport>> list)
    {
        if (dti != null && dti.getMapVisualizationInfo() != null)
        {
            MapVisualizationType type = dti.getMapVisualizationInfo().getVisualizationType();

            boolean isCompoundOrMixed = type == MapVisualizationType.MIXED_ELEMENTS
                    || type == MapVisualizationType.COMPOUND_FEATURE_ELEMENTS;

            if (type.isSingleLocationType() || type.isCompoundType())
            {
                list.add(MapLocationGeometrySupport.class);
            }
            if (type == MapVisualizationType.POLYGON_ELEMENTS || isCompoundOrMixed)
            {
                list.add(MapPolygonGeometrySupport.class);
            }
            if (type == MapVisualizationType.POLYLINE_ELEMENTS || type == MapVisualizationType.TRACK_ELEMENTS
                    || type == MapVisualizationType.USER_TRACK_ELEMENTS || isCompoundOrMixed)
            {
                list.add(MapPolylineGeometrySupport.class);
            }
            if (type == MapVisualizationType.IMAGE_TILE || type == MapVisualizationType.IMAGE)
            {
                list.add(TileVisualizationSupport.class);
            }
            if (type == MapVisualizationType.INTERPOLATED_IMAGE_TILES)
            {
                list.add(InterpolatedTileVisualizationSupport.class);
            }
        }
    }

    /**
     * Gets the class for name and type.
     *
     * @param <T> the generic type
     * @param type the type
     * @param className the class name
     * @return the class for name and type
     */
    public static <T> Class<? extends T> getClassForNameAndType(Class<? extends T> type, String className)
    {
        Class<? extends T> result = null;
        try
        {
            Class<?> aClass = Class.forName(className);
            result = aClass.asSubclass(type);
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.error("Failed to find class for style manager: " + className);
        }
        return result;
    }

    /**
     * Gets the data type info, if possible.
     *
     * @param tb the {@link Toolbox}
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @return the data type info
     */
    public static DataTypeInfo getDataTypeInfo(Toolbox tb, String dtnKey)
    {
        Map<NodeKeyComponent, String> nodeKeyCompMap = DataTypeNodeUserObject.decomposeNodeKey(dtnKey);
        NodeType nt = NodeType.valueOf(nodeKeyCompMap.get(NodeKeyComponent.NODE_TYPE));
        String dtiKey = nodeKeyCompMap.get(NodeKeyComponent.DATA_TYPE_INFO_ID);
        String groupKey = nodeKeyCompMap.get(NodeKeyComponent.DATA_GROUP_INFO_ID);
        DataTypeInfo dti = null;
        if (dtiKey != null && nt != null && nt.isLeafType())
        {
            MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(tb);
            dti = mtb.getDataTypeController().getDataTypeInfoForType(dtiKey);

            // If we didn't find the type info in the type controller try
            // to retrieve the DataGroupInfo referenced in the node key and see
            // if it is available through the group info.
            if (dti == null && StringUtils.isNotEmpty(groupKey))
            {
                DataGroupInfo dgi = DefaultDataGroupInfo.getKeyMap().getGroupForKey(groupKey);
                if (dgi != null)
                {
                    dti = dgi.getMemberById(dtiKey, true);
                }
            }

            // Have not yet found it, full blown search of the
            // DataGroupController.
            if (dti == null)
            {
                dti = mtb.getDataGroupController().findMemberById(dtiKey);
            }
        }
        return dti;
    }

    /**
     * Gets the feature classes for data type.
     *
     * @param tb the {@link Toolbox}
     * @param nt the nt
     * @param dti the dti
     * @return the feature classes for data type
     */
    public static List<Class<? extends VisualizationSupport>> getDefaultFeatureClassesForDataType(Toolbox tb, NodeType nt,
            DataTypeInfo dti)
    {
        List<Class<? extends VisualizationSupport>> list = New.list();
        if (nt == NodeType.DEFAULT_ROOT_FEATURE)
        {
            list.add(MapLocationGeometrySupport.class);
            list.add(MapPolygonGeometrySupport.class);
            list.add(MapPolylineGeometrySupport.class);
        }
        else if (nt.isLeafType())
        {
            getClassesForDataType(dti, list);
        }
        return list;
    }

    /**
     * Gets the default feature classes for type.
     *
     * @param dti the dti
     * @return the default feature classes for type
     */
    public static List<Class<? extends VisualizationSupport>> getDefaultFeatureClassesForType(DataTypeInfo dti)
    {
        List<Class<? extends VisualizationSupport>> list = New.list();
        getClassesForDataType(dti, list);
        return list;
    }

    /**
     * Gets the tile classes for data type.
     *
     * @param tb the {@link Toolbox}
     * @param nt the nt
     * @param dti the dti
     * @return the tile classes for data type
     */
    public static List<Class<? extends TileVisualizationSupport>> getDefaultTileClassesForDataType(Toolbox tb, NodeType nt,
            DataTypeInfo dti)
    {
        List<Class<? extends TileVisualizationSupport>> list = New.list(1);
        if (nt == NodeType.DEFAULT_ROOT_TILE
                || nt == NodeType.TILE_TYPE_LEAF && dti != null && dti.getMapVisualizationInfo() != null)
        {
            list.add(TileVisualizationSupport.class);
        }
        return list;
    }

    /**
     * Gets the heatmap classes for the supplied datatype.
     *
     * @param nodeType the type of node for which to get the heatmap classes.
     * @param dataType the data type to examine.
     * @return a List of support classes populated for the supplied datatype.
     */
    public static List<Class<? extends InterpolatedTileVisualizationSupport>> getDefaultHeatmapClassesForDataType(
            NodeType nodeType, DataTypeInfo dataType)
    {
        List<Class<? extends InterpolatedTileVisualizationSupport>> list = New.list(1);
        if (nodeType == NodeType.DEFAULT_ROOT_HEATMAP
                || nodeType == NodeType.HEATMAP_TYPE_LEAF && dataType != null && dataType.getMapVisualizationInfo() != null)
        {
            list.add(InterpolatedTileVisualizationSupport.class);
        }
        return list;
    }

    /**
     * Gets the new instance, first tries to use the constructor with a Toolbox,
     * then the default constructor if that fails.
     *
     * @param tb the {@link Toolbox}
     * @param vsClass the {@link VisualizationStyle} class
     * @return the new instance or null if an instance could not be created.
     */
    public static VisualizationStyle getNewInstance(Toolbox tb, Class<? extends VisualizationStyle> vsClass)
    {
        VisualizationStyle instance = null;
        Exception exception = null;

        try
        {
            Constructor<? extends VisualizationStyle> ctor = vsClass.getConstructor(Toolbox.class);
            if (ctor != null)
            {
                try
                {
                    instance = ctor.newInstance(tb);
                }
                catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e)
                {
                    exception = e;
                    instance = null;
                }
            }
        }
        catch (SecurityException | NoSuchMethodException e)
        {
            exception = e;
            instance = null;
        }

        if (instance == null)
        {
            LOGGER.error("Failed to create VisualizationStyle class " + vsClass.getName()
                    + " with Toolbox.  Trying default constructor.", exception);
            exception = null;
            try
            {
                instance = vsClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                exception = e;
            }
            if (instance == null)
            {
                LOGGER.error("Failed to create VisualizationStyle class " + vsClass.getName() + " with default constructor.",
                        exception);
            }
        }

        return instance;
    }

    /**
     * Gets the base style classes for feature class.
     *
     * @param featureClass the feature class
     * @return the base style classes for feature class
     */
    public static String getStyleCategoryNameForFeatureClass(Class<? extends VisualizationSupport> featureClass)
    {
        String result;
        if (EqualsHelper.equals(featureClass, MapLocationGeometrySupport.class))
        {
            result = "Locations";
        }
        else if (EqualsHelper.equals(featureClass, MapPolylineGeometrySupport.class))
        {
            result = "Polylines";
        }
        else if (EqualsHelper.equals(featureClass, MapPolygonGeometrySupport.class))
        {
            result = "Polygons";
        }
        else if (EqualsHelper.equals(featureClass, TileVisualizationSupport.class))
        {
            result = "Tiles";
        }
        else if (Objects.equal(featureClass, InterpolatedTileVisualizationSupport.class))
        {
            result = "Data Tiles";
        }
        else
        {
            return "Unknown";
        }
        return result;
    }

    /**
     * Gets the base style classes for feature class.
     *
     * @param featureClass the feature class
     * @return the base style classes for feature class
     */
    public static String getStyleCategoryNameForTileClass(Class<? extends VisualizationSupport> featureClass)
    {
        return "All";
    }

    /**
     * Gets the base style classes for heatmap class.
     *
     * @param heatmapClass the class for which to get the style category name.
     * @return the name of the style category.
     */
    public static String getStyleCategoryNameForHeatmapClass(Class<? extends InterpolatedTileVisualizationSupport> heatmapClass)
    {
        return "Heatmaps";
    }

    /**
     * Update style with parameters from config.
     *
     * @param defaultStyle the default style
     * @param spsc the spsc
     * @param source the source
     */
    public static void updateStyleWithParametersFromConfig(VisualizationStyle defaultStyle, StyleParameterSetConfig spsc,
            Object source)
    {
        Set<StyleParameterConfig> spSet = spsc.getParameterSet();
        if (spSet != null && !spSet.isEmpty())
        {
            Set<VisualizationStyleParameter> vspSet = New.set();
            for (StyleParameterConfig sp : spSet)
            {
                VisualizationStyleParameter vsp = defaultStyle.getStyleParameter(sp.getParameterKey());
                if (vsp != null)
                {
                    try
                    {
                        vsp = vsp.deriveWithNewValue(sp.getParameterValueAsObject());
                    }
                    catch (UnsupportedClassConversionError | IllegalArgumentException e)
                    {
                        LOGGER.error(e);
                    }
                    vspSet.add(vsp);
                }
            }
            defaultStyle.setParameters(vspSet, VisualizationStyle.NO_EVENT_SOURCE);
        }
    }

    /** Do not allow instantiation. */
    private StyleManagerUtils()
    {
    }
}
