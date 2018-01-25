package io.opensphere.mantle.data.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * The Class MantleJTSGeometryUtils.
 */
public final class MantleJTSGeometryUtils
{
    /**
     * Create a JTS polygon for a four point envelope.
     *
     * @param minLon The west longitude.
     * @param maxLon The east longitude.
     * @param minLat The south latitude.
     * @param maxLat The north latitude.
     * @param geomFactory A geometry factory to use.
     * @return The geometry.
     */
    public static Polygon createPolygon(double minLon, double maxLon, double minLat, double maxLat, GeometryFactory geomFactory)
    {
        Coordinate[] coord;
        double dx = maxLon - minLon;
        if (dx == 360. && maxLat == 90. ^ minLat == -90.)
        {
            double lat = maxLat == 90 ? minLat : maxLat;
            coord = new Coordinate[4];
            for (int i = 0; i < 3; i++)
            {
                coord[i] = new Coordinate(-180. + i * 360. / 3, lat);
            }
            coord[3] = coord[0];
        }
        else if (dx < 0. && dx > -180 || dx > 180.)
        {
            double midX = minLon + dx * .5;
            if (dx < 0.)
            {
                if (midX < 0.)
                {
                    midX += 180.;
                }
                else
                {
                    midX -= 180.;
                }
            }
            coord = new Coordinate[7];
            coord[0] = new Coordinate(minLon, minLat);
            coord[1] = new Coordinate(midX, minLat);
            coord[2] = new Coordinate(maxLon, minLat);
            coord[3] = new Coordinate(maxLon, maxLat);
            coord[4] = new Coordinate(midX, maxLat);
            coord[5] = new Coordinate(minLon, maxLat);
            coord[6] = coord[0];
        }
        else
        {
            coord = new Coordinate[5];
            coord[0] = new Coordinate(minLon, minLat);
            coord[1] = new Coordinate(maxLon, minLat);
            coord[2] = new Coordinate(maxLon, maxLat);
            coord[3] = new Coordinate(minLon, maxLat);
            coord[4] = coord[0];
        }
        Polygon geom = new Polygon(geomFactory.createLinearRing(coord), null, geomFactory);
        return geom;
    }

    /**
     * Instantiates a new mantle jts geometry utils.
     */
    private MantleJTSGeometryUtils()
    {
        // Don't allow construction.
    }
}
