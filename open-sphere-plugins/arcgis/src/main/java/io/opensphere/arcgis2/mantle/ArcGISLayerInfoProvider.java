package io.opensphere.arcgis2.mantle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.arcgis2.esri.EsriClassBreaksRenderer;
import io.opensphere.arcgis2.esri.EsriClassBreaksRenderer.EsriClassBreakInfo;
import io.opensphere.arcgis2.esri.EsriField;
import io.opensphere.arcgis2.esri.EsriField.EsriFieldType;
import io.opensphere.arcgis2.esri.EsriFullLayer;
import io.opensphere.arcgis2.esri.EsriFullLayer.EsriGeometryType;
import io.opensphere.arcgis2.esri.EsriPictureMarkerSymbol;
import io.opensphere.arcgis2.esri.EsriRenderer;
import io.opensphere.arcgis2.esri.EsriSimpleRenderer;
import io.opensphere.arcgis2.esri.EsriUniqueValueRenderer;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.analysis.ColumnAnalysis;
import io.opensphere.mantle.data.analysis.ColumnAnalysis.Determination;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.EndTimeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;

/** Queries and generates metadata and other info for ArcGIS feature layers. */
@SuppressWarnings("PMD.GodClass")
public class ArcGISLayerInfoProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ArcGISLayerInfoProvider.class);

    /**
     * Supported ESRI geometry types and which MapVisualizationType they map to.
     */
    private static Map<EsriGeometryType, MapVisualizationType> ourRestPrimitives = New.map();

    /** Set the ESRI primitives. */
    static
    {
        ourRestPrimitives.put(EsriGeometryType.esriGeometryPoint, MapVisualizationType.POINT_ELEMENTS);
        ourRestPrimitives.put(EsriGeometryType.esriGeometryMultipoint, MapVisualizationType.POINT_ELEMENTS);
        ourRestPrimitives.put(EsriGeometryType.esriGeometryPolyline, MapVisualizationType.POLYLINE_ELEMENTS);
        ourRestPrimitives.put(EsriGeometryType.esriGeometryPolygon, MapVisualizationType.POLYGON_ELEMENTS);
    }

    /** The system toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public ArcGISLayerInfoProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Updates the data type with layer details.
     *
     * @param dataType the data type
     * @param layer the layer details
     */
    public void updateDataType(DefaultDataTypeInfo dataType, EsriFullLayer layer)
    {
        if (StringUtils.isNotEmpty(layer.getDescription()))
        {
            dataType.setDescription(layer.getDescription());
        }
        setFeatureProperties(dataType, layer);
        dataType.getMapVisualizationInfo().setVisualizationType(getFeatureDataType(layer));
        dataType.getBasicVisualizationInfo().setTypeColor(getColorFromLayer(layer), this);
    }

    /**
     * Get the properties of each feature in the layer.
     *
     * @param dataType the data type
     * @param layer The layer definition with property names.
     */
    private void setFeatureProperties(DefaultDataTypeInfo dataType, EsriFullLayer layer)
    {
        if (CollectionUtilities.hasContent(layer.getFields()))
        {
            String startTimeField = null;
            String endTimeField = null;
            if (layer.getTimeInfo() != null)
            {
                if (layer.getTimeInfo().getStartTimeField() != null)
                {
                    startTimeField = layer.getTimeInfo().getStartTimeField();
                }
                if (layer.getTimeInfo().getEndTimeField() != null)
                {
                    endTimeField = layer.getTimeInfo().getEndTimeField();
                }
            }

            DefaultMetaDataInfo metaDataInfo = (DefaultMetaDataInfo)dataType.getMetaDataInfo();

            for (EsriField field : layer.getFields())
            {
                String name = field.getName();
                Class<?> javaClass = getJavaTypeFromEsri(field.getType());
                javaClass = applyColumnAnalysisToDetermineColumnClass(dataType, name, javaClass);
                metaDataInfo.addKey(name, javaClass, this);
                if (name.equals(startTimeField))
                {
                    metaDataInfo.setSpecialKey(name, TimeKey.DEFAULT, this);

                    dataType.getBasicVisualizationInfo()
                            .setSupportedLoadsToTypes(DefaultBasicVisualizationInfo.LOADS_TO_STATIC_AND_TIMELINE);
                    dataType.getBasicVisualizationInfo().setLoadsTo(LoadsTo.TIMELINE, this);
                }
                if (name.equals(endTimeField))
                {
                    metaDataInfo.setSpecialKey(name, EndTimeKey.DEFAULT, this);
                }
            }
            metaDataInfo.copyKeysToOriginalKeys();
        }
    }

    /**
     * Apply column analysis to metadata.
     *
     * @param dataType the WFSDataType
     * @param name the Node
     * @param pType the type
     * @return the class
     */
    private Class<?> applyColumnAnalysisToDetermineColumnClass(DataTypeInfo dataType, String name, Class<?> pType)
    {
        Class<?> aType = pType;
        if (aType == String.class)
        {
            MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(myToolbox);
            if (mtb.getDataAnalysisReporter().isColumnDataAnalysisEnabled())
            {
                ColumnAnalysis analysis = mtb.getDataAnalysisReporter().getColumnAnalysis(dataType.getTypeKey(), name);
                if (analysis != null && analysis.getDetermination() == Determination.DETERMINED)
                {
                    if (analysis.getDeterminedClass() != String.class)
                    {
                        aType = analysis.getDeterminedClass();
                    }
                    if (analysis.isEnumCandidate())
                    {
                        mtb.getDynamicEnumerationRegistry().createEnumeration(dataType.getTypeKey(), name, aType);
                        aType = DynamicEnumerationKey.class;
                    }
                }
            }
        }
        return aType;
    }

    /**
     * Get a Java type from an ESRI Field type.
     *
     * @param base the base field type to translate.
     * @return the Java type (default = String.class)
     */
    private static Class<?> getJavaTypeFromEsri(EsriFieldType base)
    {
        Class<?> type;

        switch (base)
        {
            case esriFieldTypeSmallInteger:
                type = Integer.class;
                break;
            case esriFieldTypeInteger:
                type = Integer.class;
                break;
            case esriFieldTypeDouble:
                type = Double.class;
                break;
            case esriFieldTypeSingle:
                type = Float.class;
                break;
            case esriFieldTypeOID:
            case esriFieldTypeRaster:
                type = Long.class;
                break;
            case esriFieldTypeDate:
            case esriFieldTypeGlobalID:
            case esriFieldTypeGUID:
            case esriFieldTypeString:
            case esriFieldTypeXml:
            default:
                type = String.class;
                break;
        }

        return type;
    }

    /**
     * Gets the default layer color from an ESRI layer definition.
     *
     * @param layer the layer to extract the color from
     * @return the color from layer
     */
    private static Color getColorFromLayer(EsriFullLayer layer)
    {
        Color color = Color.WHITE;
        EsriRenderer renderer = layer == null ? null
                : layer.getDrawingInfo() == null ? null : layer.getDrawingInfo().getRenderer();

        if (renderer instanceof EsriSimpleRenderer)
        {
            EsriSimpleRenderer simpleRenderer = (EsriSimpleRenderer)renderer;
            if (simpleRenderer.getSymbol() != null && simpleRenderer.getSymbol().getColor() != null)
            {
                Color renderColor = simpleRenderer.getSymbol().getColor().getColor();
                color = renderColor != null ? renderColor : color;
            }
            else if (simpleRenderer.getSymbol() instanceof EsriPictureMarkerSymbol)
            {
                EsriPictureMarkerSymbol symbol = (EsriPictureMarkerSymbol)simpleRenderer.getSymbol();
                BufferedImage sample = symbol.getImage();
                if (sample != null)
                {
                    int centerPixel = sample.getRGB(sample.getWidth() / 2, sample.getHeight() / 2);
                    color = new Color(centerPixel).brighter();
                }
            }
        }
        else if (renderer instanceof EsriUniqueValueRenderer)
        {
            EsriUniqueValueRenderer uniqueRenderer = (EsriUniqueValueRenderer)renderer;
            if (uniqueRenderer.getDefaultSymbol() != null && uniqueRenderer.getDefaultSymbol().getColor() != null)
            {
                Color renderColor = uniqueRenderer.getDefaultSymbol().getColor().getColor();
                color = renderColor != null ? renderColor : color;
            }
        }
        else if (renderer instanceof EsriClassBreaksRenderer)
        {
            EsriClassBreaksRenderer breakRenderer = (EsriClassBreaksRenderer)renderer;
            if (CollectionUtilities.hasContent(breakRenderer.getClassBreakInfos()))
            {
                EsriClassBreakInfo breakInfo = breakRenderer.getClassBreakInfos().get(0);
                if (breakInfo != null && breakInfo.getSymbol() != null && breakInfo.getSymbol().getColor() != null)
                {
                    Color renderColor = breakInfo.getSymbol().getColor().getColor();
                    color = renderColor != null ? renderColor : color;
                }
            }
        }

        return color;
    }

    /**
     * Function to get the type of data returned by feature requests from the
     * passed-in layer.
     *
     * @param layer The full layer description provided by the REST interface
     * @return the features' data type, UNKNOWN if not found
     */
    private static MapVisualizationType getFeatureDataType(EsriFullLayer layer)
    {
        if (layer.getGeometryType() != null && ourRestPrimitives.get(layer.getGeometryType()) != null)
        {
            return ourRestPrimitives.get(layer.getGeometryType());
        }
        return MapVisualizationType.UNKNOWN;
    }
}
