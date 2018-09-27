package io.opensphere.analysis.util;

import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.MGRSCoreUtil;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;

public class MGRSUtilities
{
    public static final int DEFAULT_MGRS_PRECISION = 10;

    public static DataElement getMGRSDataElement(MapDataElement element, int precision, Object source)
    {
        MetaDataProvider mgrsProvider = MGRSUtilities.getMetaDataProviderWithMGRS(element.getMetaData(),
                element.getDataTypeInfo().getMetaDataInfo(), element.getMapGeometrySupport(), precision, source);

        return new DefaultMapDataElement(element.getId(), element.getTimeSpan(), element.getDataTypeInfo(), mgrsProvider,
                element.getMapGeometrySupport());
    }

    public static MetaDataProvider getMetaDataProviderWithMGRS(MetaDataProvider provider, MetaDataInfo info,
            MapGeometrySupport mapSupport, int precision, Object source)
    {
        if (provider != null && info != null && mapSupport != null)
        {
            info.addKey(MGRSCoreUtil.MGRS_DERIVED, String.class, source);
            provider.setValue(MGRSCoreUtil.MGRS_DERIVED, getMGRSValue(mapSupport, precision));
        }
        return provider;
    }

    private static String getMGRSValue(MapGeometrySupport mapSupport, int precision)
    {
        String value = null;
        if (mapSupport instanceof MapLocationGeometrySupport)
        {
            UTM utmCoords = new UTM(new GeographicPosition(((MapLocationGeometrySupport)mapSupport).getLocation()));
            value = MGRSCoreUtil.reducePrecision(new MGRSConverter().createString(utmCoords), precision);
        }
        return value;
    }
}
