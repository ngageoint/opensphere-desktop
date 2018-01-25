package io.opensphere.mantle.data.geom.util.jts;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolylineGeometrySupport;

/**
 * A factory for creating converting JTS {@link Geometry} to
 * {@link MapGeometrySupport}.
 */
public final class JTSGeometryToGeometrySupportFactory
{
    /** The Constant ourLogger. */
    private static final Logger LOGGER = Logger.getLogger(JTSGeometryToGeometrySupportFactory.class);

    /**
     * Creates the MapGeometrySupport from JTS LineString.
     *
     * @param featureColor the feature color
     * @param simple true for a {@link SimpleMapPolylineGeometrySupport} false
     *            for a default support capable of supporting children.
     * @param ls the {@link LineString}
     * @return the {@link MapGeometrySupport}
     */
    public static MapGeometrySupport createFromLineString(Color featureColor, boolean simple,
            com.vividsolutions.jts.geom.LineString ls)
    {
        List<LatLonAlt> locList = JTSUtilities.convertToLatLonAlt(ls.getCoordinates(), ReferenceLevel.TERRAIN);
        if (!locList.isEmpty())
        {
            MapGeometrySupport geomSupport;
            if (locList.size() == 1)
            {
                geomSupport = new SimpleMapPointGeometrySupport(locList.get(0));
            }
            else
            {
                geomSupport = simple ? new SimpleMapPolylineGeometrySupport(locList)
                        : new DefaultMapPolylineGeometrySupport(locList);
            }
            geomSupport.setColor(featureColor, null);
            return geomSupport;
        }
        return null;
    }

    /**
     * Creates a new MapGeometrySupport from JTS MultiLineString.
     *
     * @param featureColor the feature {@link Color}
     * @param mls the {@link MultiLineString}
     * @return the map geometry support
     */
    public static MapGeometrySupport createFromMultiLineString(Color featureColor, MultiLineString mls)
    {
        MapGeometrySupport geomSupport = null;
        for (int i = 0; i < mls.getNumGeometries(); i++)
        {
            Geometry g = mls.getGeometryN(i);
            if (g instanceof com.vividsolutions.jts.geom.LineString)
            {
                com.vividsolutions.jts.geom.LineString ls = (com.vividsolutions.jts.geom.LineString)g;
                if (geomSupport == null)
                {
                    geomSupport = createFromLineString(featureColor, false, ls);
                }
                else
                {
                    if (geomSupport instanceof AbstractMapGeometrySupport)
                    {
                        ((AbstractMapGeometrySupport)geomSupport).addChild(createFromLineString(featureColor, true, ls));
                    }
                }
            }
        }
        return geomSupport;
    }

    /**
     * Creates a new MapGeometrySupport from JTS MultiPoint.
     *
     * @param featureColor the feature {@link Color}
     * @param mp the {@link MultiPoint}
     * @return the {@link MapGeometrySupport}
     */
    public static MapGeometrySupport createFromMultiPoint(Color featureColor, com.vividsolutions.jts.geom.MultiPoint mp)
    {
        AbstractMapGeometrySupport geomSupport = null;
        for (int i = 0; i < mp.getNumGeometries(); i++)
        {
            Geometry g = mp.getGeometryN(i);
            if (g instanceof com.vividsolutions.jts.geom.Point)
            {
                com.vividsolutions.jts.geom.Point pt = (com.vividsolutions.jts.geom.Point)g;
                LatLonAlt lla = LatLonAlt.createFromDegrees(pt.getY(), pt.getX(), ReferenceLevel.TERRAIN);
                if (geomSupport == null)
                {
                    geomSupport = new DefaultMapPointGeometrySupport(lla);
                    geomSupport.setColor(featureColor, null);
                }
                else
                {
                    SimpleMapPointGeometrySupport child = new SimpleMapPointGeometrySupport(lla);
                    child.setColor(featureColor, null);
                    geomSupport.addChild(child);
                }
            }
        }
        return geomSupport;
    }

    /**
     * Creates a new MapGeometrySupport from JTS MultiLineString.
     *
     * @param featureColor the feature {@link Color}
     * @param mpg the {@link MultiPolygon}
     * @return the map geometry support
     */
    public static MapGeometrySupport createFromMultiPolygon(Color featureColor, MultiPolygon mpg)
    {
        MapGeometrySupport geomSupport = null;
        for (int i = 0; i < mpg.getNumGeometries(); i++)
        {
            Geometry g = mpg.getGeometryN(i);
            if (g instanceof com.vividsolutions.jts.geom.Polygon)
            {
                com.vividsolutions.jts.geom.Polygon pg = (com.vividsolutions.jts.geom.Polygon)g;
                if (geomSupport == null)
                {
                    geomSupport = createFromPolygon(featureColor, false, pg);
                }
                else
                {
                    if (geomSupport instanceof AbstractMapGeometrySupport)
                    {
                        ((AbstractMapGeometrySupport)geomSupport).addChild(createFromPolygon(featureColor, true, pg));
                    }
                }
            }
        }
        return geomSupport;
    }

    /**
     * Creates a new JTSToMGS object.
     *
     * @param featureColor the feature color
     * @param simpleType true to create simple type, false to create default
     *            type {@link MapGeometrySupport}
     * @param pt the {@link Point}
     * @return the map geometry support
     */
    public static MapGeometrySupport createFromPoint(Color featureColor, boolean simpleType, com.vividsolutions.jts.geom.Point pt)
    {
        MapGeometrySupport geomSupport = null;
        LatLonAlt lla = LatLonAlt.createFromDegreesMeters(pt.getY(), pt.getX(), pt.getCoordinate().z, ReferenceLevel.TERRAIN);
        geomSupport = simpleType ? new SimpleMapPointGeometrySupport(lla) : new DefaultMapPointGeometrySupport(lla);
        geomSupport.setColor(featureColor, null);
        return geomSupport;
    }

    /**
     * Creates the MapGeometrySupport from JTS Polygon.
     *
     * @param featureColor the feature color
     * @param simple true for a {@link SimpleMapPolygonGeometrySupport} false
     *            for a default support capable of supporting children.
     * @param poly the {@link com.vividsolutions.jts.geom.Polygon}
     * @return the {@link MapGeometrySupport}
     */
    public static MapGeometrySupport createFromPolygon(Color featureColor, boolean simple,
            com.vividsolutions.jts.geom.Polygon poly)
    {
        Pair<List<LatLonAlt>, Collection<List<LatLonAlt>>> rings = JTSUtilities.convertToLatLonAlt(poly, ReferenceLevel.TERRAIN);

        MapGeometrySupport geomSupport = simple
                ? new SimpleMapPolygonGeometrySupport(rings.getFirstObject(), rings.getSecondObject())
                : new DefaultMapPolygonGeometrySupport(rings.getFirstObject(), rings.getSecondObject());
        geomSupport.setColor(featureColor, null);
        return geomSupport;
    }

    /**
     * Creates the {@link MapGeometrySupport} from WKT geometry.
     *
     * @param jtsGeometry the JTS {@link Geometry}
     * @param featureColor the feature {@link Color}
     * @return the {@link MapGeometrySupport}
     */
    public static MapGeometrySupport createGeometrySupportFromWKTGeometry(Geometry jtsGeometry, Color featureColor)
    {
        return createGeometrySupportFromWKTGeometryInternal(jtsGeometry, true, featureColor);
    }

    /**
     * Creates the {@link MapGeometrySupport} from WKT geometry.
     *
     * @param jtsGeometry the jts {@link Geometry}
     * @param simpleType create simple types when possible.
     * @param featureColor the feature {@link Color}
     * @return the {@link MapGeometrySupport}
     */
    private static MapGeometrySupport createGeometrySupportFromWKTGeometryInternal(Geometry jtsGeometry, boolean simpleType,
            Color featureColor)
    {
        MapGeometrySupport geomSupport = null;
        if (jtsGeometry != null)
        {
            if (jtsGeometry instanceof com.vividsolutions.jts.geom.Point)
            {
                com.vividsolutions.jts.geom.Point pt = (com.vividsolutions.jts.geom.Point)jtsGeometry;
                geomSupport = createFromPoint(featureColor, simpleType, pt);
            }
            else if (jtsGeometry instanceof com.vividsolutions.jts.geom.LineString)
            {
                com.vividsolutions.jts.geom.LineString ls = (com.vividsolutions.jts.geom.LineString)jtsGeometry;
                geomSupport = createFromLineString(featureColor, simpleType, ls);
            }
            else if (jtsGeometry instanceof com.vividsolutions.jts.geom.Polygon)
            {
                com.vividsolutions.jts.geom.Polygon poly = (com.vividsolutions.jts.geom.Polygon)jtsGeometry;
                geomSupport = createFromPolygon(featureColor, simpleType, poly);
            }
            else if (jtsGeometry instanceof com.vividsolutions.jts.geom.MultiPoint)
            {
                com.vividsolutions.jts.geom.MultiPoint mp = (com.vividsolutions.jts.geom.MultiPoint)jtsGeometry;
                geomSupport = createFromMultiPoint(featureColor, mp);
            }
            else if (jtsGeometry instanceof com.vividsolutions.jts.geom.MultiLineString)
            {
                com.vividsolutions.jts.geom.MultiLineString mls = (com.vividsolutions.jts.geom.MultiLineString)jtsGeometry;
                geomSupport = createFromMultiLineString(featureColor, mls);
            }
            else if (jtsGeometry instanceof com.vividsolutions.jts.geom.MultiPolygon)
            {
                com.vividsolutions.jts.geom.MultiPolygon mpg = (com.vividsolutions.jts.geom.MultiPolygon)jtsGeometry;
                geomSupport = createFromMultiPolygon(featureColor, mpg);
            }
            else if (jtsGeometry instanceof com.vividsolutions.jts.geom.GeometryCollection)
            {
                com.vividsolutions.jts.geom.GeometryCollection gc = (com.vividsolutions.jts.geom.GeometryCollection)jtsGeometry;
                for (int i = 0; i < gc.getNumGeometries(); i++)
                {
                    Geometry g = gc.getGeometryN(i);
                    if (geomSupport == null)
                    {
                        geomSupport = createGeometrySupportFromWKTGeometryInternal(g, false, featureColor);
                    }
                    else
                    {
                        if (geomSupport instanceof AbstractMapGeometrySupport)
                        {
                            ((AbstractMapGeometrySupport)geomSupport)
                                    .addChild(createGeometrySupportFromWKTGeometryInternal(g, false, featureColor));
                        }
                        else
                        {
                            LOGGER.error("Failed to add children to MapGeometrySupport during JTS GeometryCollection Conversion");
                        }
                    }
                }
            }
            else
            {
                LOGGER.error("Encountered unknown JTS Geometry Type : " + jtsGeometry.getClass().getName()
                        + " unable to ingest geometry data");
            }
        }
        if (geomSupport != null)
        {
            geomSupport.setColor(featureColor, null);
        }

        return geomSupport;
    }

    /**
     * Instantiates a new jTS to {@link MapGeometrySupport} factory.
     */
    private JTSGeometryToGeometrySupportFactory()
    {
        // Don't allow instantiation.
    }
}
