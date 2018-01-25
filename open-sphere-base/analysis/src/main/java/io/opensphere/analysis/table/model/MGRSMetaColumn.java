package io.opensphere.analysis.table.model;

import java.util.function.Predicate;

import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.MGRSUtil;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;

/**
 * MGRS meta table column.
 */
public class MGRSMetaColumn extends MetaColumn<String>
{
    /** The MGRS predicate. */
    public static final Predicate<MetaColumn<?>> MGRS_PREDICATE = metaColumn -> metaColumn instanceof MGRSMetaColumn;

    /** The MGRS converter. */
    private final transient MGRSConverter myConverter = new MGRSConverter();

    /** The MGRS precision. */
    private int myMGRSPrecision = 10;

    /**
     * Constructor.
     */
    public MGRSMetaColumn()
    {
        super(MetaColumn.MGRS_DERIVED, String.class, false);
    }

    @Override
    public String getValue(int rowIndex, DataElement dataElement)
    {
        String value = null;
        if (dataElement instanceof MapDataElement)
        {
            MapGeometrySupport mgs = ((MapDataElement)dataElement).getMapGeometrySupport();
            if (mgs instanceof MapLocationGeometrySupport)
            {
                MapLocationGeometrySupport mlgs = (MapLocationGeometrySupport)mgs;
                LatLonAlt location = mlgs.getLocation();
                UTM utmCoords = new UTM(new GeographicPosition(location));
                value = myConverter.createString(utmCoords);
                if (myMGRSPrecision != 10)
                {
                    value = MGRSUtil.reducePrecision(value, myMGRSPrecision);
                }
            }
        }
        return value;
    }

    /**
     * Sets the MGRS precision.
     *
     * @param mgrsPrecision the MGRS precision
     */
    public void setMGRSPrecision(int mgrsPrecision)
    {
        myMGRSPrecision = mgrsPrecision;
        getObservable().set(Integer.valueOf(myMGRSPrecision));
    }
}
