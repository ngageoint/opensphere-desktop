package io.opensphere.csv.parse;

import java.awt.Color;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.csvcommon.parse.GeomSupportFactory;
import io.opensphere.csvcommon.parse.PointExtract;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;

/** Factory that creates {@link MapLocationGeometrySupport}s for CSV rows. */
public class CSVMapLocationGeoemtrySupportFactory implements GeomSupportFactory
{
    /** Default Lob Length for LOB Geometries. */
    private static final float DEFAULT_LOB_LENGTH_KM = 1000f;

    /**
     * Double or nothing. If the {@link Number} object is null return 0.0 else
     * returns the .doubleValue() for the {@link Number}
     *
     * @param val the val
     * @return the double
     */
    private static double doubleOrNothing(Number val)
    {
        return val == null ? 0.0 : val.doubleValue();
    }

    /**
     * Float or nothing. If the {@link Number} object is null return 0.0f else
     * returns the .floatValue() for the {@link Number}
     *
     * @param val the val
     * @return the float
     */
    private static float floatOrNothing(Number val)
    {
        return val == null ? 0.0f : val.floatValue();
    }

    /**
     * Creates the geometry support.
     *
     * @param mapVisInfo the visualization info
     * @param ptData the pt data
     * @param dotColor the dot color
     * @return the abstract location geometry support
     */
    @Override
    public MapLocationGeometrySupport createGeometrySupport(MapVisualizationInfo mapVisInfo, PointExtract ptData, Color dotColor)
    {
        MapLocationGeometrySupport geomSupport = null;
        switch (mapVisInfo.getVisualizationType())
        {
            case CIRCLE_ELEMENTS:
                geomSupport = new SimpleMapCircleGeometrySupport(createLatLonAlt(ptData), floatOrNothing(ptData.getRadius()));
                break;
            case ELLIPSE_ELEMENTS:
                geomSupport = new SimpleMapEllipseGeometrySupport(createLatLonAlt(ptData), floatOrNothing(ptData.getSma()),
                        floatOrNothing(ptData.getSmi()), floatOrNothing(ptData.getOrientation()));
                break;
            case LOB_ELEMENTS:
                geomSupport = new SimpleMapLineOfBearingGeometrySupport(createLatLonAlt(ptData), floatOrNothing(ptData.getLob()),
                        DEFAULT_LOB_LENGTH_KM);
                break;
            case POINT_ELEMENTS:
                geomSupport = new SimpleMapPointGeometrySupport(createLatLonAlt(ptData));
                break;
            default:
                break;
        }
        if (geomSupport != null)
        {
            geomSupport.setColor(dotColor, null);
        }
        return geomSupport;
    }

    /**
     * Creates a LatLonAlt from a PointExtract.
     *
     * @param ptData the PointExtract
     * @return the LatLonAlt
     */
    private LatLonAlt createLatLonAlt(PointExtract ptData)
    {
        return LatLonAlt.createFromDegreesMeters(doubleOrNothing(ptData.getLat()), doubleOrNothing(ptData.getLon()),
                doubleOrNothing(ptData.getAlt()), Altitude.ReferenceLevel.TERRAIN);
    }
}
