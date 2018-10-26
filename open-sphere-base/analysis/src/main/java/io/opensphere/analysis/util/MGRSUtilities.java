package io.opensphere.analysis.util;

import io.opensphere.core.mgrs.MGRSCalcUtils;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;

/** Class that holds utility methods to add MGRS values to data elements. */
public class MGRSUtilities
{
    /** The MGRS derived column identifier. */
    public static final String MGRS_DERIVED = "MGRS Derived";

    /** The default MGRS derived precision. */
    public static final int DEFAULT_MGRS_PRECISION = 10;

    /**
     * Gets a new {@link DefaultMapDataElement} which is like the original
     * element, but with the additional field 'MGRS Derived'.
     *
     * @param element the original element to create the MGRS value for
     * @param precision the precision to use in calculating the MGRS value
     * @param source the calling object
     * @return a data element which includes the MGRS value now
     */
    public static DataElement getMGRSDataElement(MapDataElement element, int precision, Object source)
    {
        DataTypeInfo dataTypeInfo = element.getDataTypeInfo();
        MapGeometrySupport mapSupport = element.getMapGeometrySupport();
        // capture the color from the visualization state, otherwise, user
        // changed colors will be lost:
        MetaDataProvider mgrsProvider = getMGRSMetaDataProvider(element.getMetaData(), dataTypeInfo, mapSupport, precision,
                source);

        DefaultMapDataElement returnValue = new DefaultMapDataElement(element.getId(), element.getTimeSpan(), dataTypeInfo,
                mgrsProvider, mapSupport);

        VisualizationState visualizationState = returnValue.getVisualizationState();
        visualizationState.setColor(element.getVisualizationState().getColor());
        visualizationState.setAltitudeAdjust(element.getVisualizationState().getAltitudeAdjust());
        visualizationState.setHasAlternateGeometrySupport(element.getVisualizationState().hasAlternateGeometrySupport());
        visualizationState.setLobVisible(element.getVisualizationState().isLobVisible());
        visualizationState.setSelected(element.getVisualizationState().isSelected());
        visualizationState.setVisible(element.getVisualizationState().isVisible());

        return returnValue;
    }

    /**
     * Gets a {@link MetaDataProvider} with the additional field 'MGRS Derived'.
     *
     * @param provider the original {@link MetaDataProvider}
     * @param dataTypeInfo the {@link DataTypeInfo}
     * @param mapSupport the {@link MapGeometrySupport} for calculating the MGRS
     *            value
     * @param precision the precision to use in calculating the MGRS value
     * @param source the calling object
     * @return a new {@link MetaDataProvider} with the additional field 'MGRS
     *         Derived' or the original provider if a new one could not be
     *         created
     */
    public static MetaDataProvider getMGRSMetaDataProvider(MetaDataProvider provider, DataTypeInfo dataTypeInfo,
            MapGeometrySupport mapSupport, int precision, Object source)
    {
        MetaDataInfo metaInfo;
        if (provider != null && dataTypeInfo != null && (metaInfo = dataTypeInfo.getMetaDataInfo()) != null)
        {
            metaInfo.addKey(MGRS_DERIVED, String.class, source);
            MetaDataProvider newProvider = new MDILinkedMetaDataProvider(metaInfo, provider.getValues());
            newProvider.setValue(MGRS_DERIVED, getMGRSValue(mapSupport, precision));
            return newProvider;
        }
        return provider;
    }

    /**
     * Gets the calculated MGRS value.
     *
     * @param mapSupport the {@link MapGeometrySupport} for calculating the MGRS
     *            value
     * @param precision the precision to use in calculating the MGRS value
     * @return the MGRS value
     */
    public static String getMGRSValue(MapGeometrySupport mapSupport, int precision)
    {
        String value = null;
        if (mapSupport instanceof MapLocationGeometrySupport)
        {
            UTM utmCoords = new UTM(new GeographicPosition(((MapLocationGeometrySupport)mapSupport).getLocation()));
            value = MGRSCalcUtils.reducePrecision(new MGRSConverter().createString(utmCoords), precision);
        }
        return value;
    }
}
