package io.opensphere.mantle.data.element.factory;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.generic.GenericRecord;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;

/**
 * Factory that creates DataElements from types in common. DataObject,
 * DataPoint, DataEllipse, DataTrack, DataPolygon etc.
 */
@SuppressWarnings("PMD.GodClass")
public final class DataElementFactory
{
    /**
     * Create the MapGeometrySupport for an Avro record.
     * @param rec record
     * @param help the layer, wrapped with a time span helper
     * @return MapGeometrySupport
     */
    public static MapGeometrySupport avroObjectToGeom(GenericRecord rec, AvroTimeHelper help)
    {
        DataTypeInfo dti = help.getType();
        MetaDataInfo meta = dti.getMetaDataInfo();
        TimeSpan span = help.span(rec);
        Color typeColor = avroElementColor(rec, dti);
        try
        {
            Geometry geom = new WKTReader().read(getStr(rec, meta.getGeometryColumn()));
            if (geom instanceof LineString || geom instanceof Polygon)
            {
                return pathGeom(geom, span, typeColor);
            }
            if (geom instanceof Point)
            {
                Point p = (Point)geom;
                return pointGeom(p.getY(), p.getX(), span, typeColor);
            }
        }
        catch (ClassCastException | com.vividsolutions.jts.io.ParseException eek)
        {
            // if this happens, just fall through
        }
        throw new UnsupportedOperationException("Can not create MapGeometrySupport for avro record.");
    }

    /**
     * Determine the color for an Avro record.
     * @param rec record
     * @param t layer
     * @return Color
     */
    public static Color avroElementColor(GenericRecord rec, DataTypeInfo t)
    {
        Color c = colorOf(t);
        if (c == null)
        {
            c = Color.WHITE;
        }
        return c;
    }

    /**
     * Convert an Object to String, with nulls remaining as such.
     * @param obj an Object or null
     * @return a String or null
     */
    private static String stringOf(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        return obj.toString();
    }

    /**
     * Get a field from an Avro record as a String (or null).
     * @param rec record
     * @param key field name
     * @return associated field value, if any, as a String
     */
    private static String getStr(GenericRecord rec, String key)
    {
        return stringOf(rec.get(key));
    }

    /**
     * Create the MapGeometrySupport for a feature in the form of a polygon or
     * a polyline.  Vertices of the Geometry are included as children.
     * @param geom the basic Geometry
     * @param span the associated TimeSpan
     * @param c the Color of the feature
     * @return MapGeometrySupport
     */
    private static MapGeometrySupport pathGeom(Geometry geom, TimeSpan span, Color c)
    {
        List<LatLonAlt> llaList = new LinkedList<>();
        Coordinate[] coords = geom.getCoordinates();
        for (int i = 0; i < coords.length; i++)
        {
            llaList.add(LatLonAlt.createFromDegrees(
                    coords[i].y, coords[i].x, Altitude.ReferenceLevel.ELLIPSOID));
        }

        AbstractMapGeometrySupport pgs = null;
        if (geom instanceof LineString)
        {
            pgs = new DefaultMapPolylineGeometrySupport(llaList);
        }
        else
        {
            pgs = new DefaultMapPolygonGeometrySupport(llaList, null);
        }

        if (!span.isTimeless())
        {
            for (LatLonAlt lla :  llaList)
            {
                pgs.addChild(defPointGeom(lla, span, c));
            }
        }

        pgs.setColor(c, null);
        pgs.setTimeSpan(span);
        return pgs;
    }

    /**
     * Create the MapGeometrySupport for a geographical ellipse defined by the
     * provided parameters.
     * @param lat center latitude (degrees)
     * @param lon center longitude (degrees)
     * @param semiMaj semi-major axis length in km
     * @param semiMin semi-minor axis length in km
     * @param orient orientation angle in "degrees clockwise from north"
     * @param span TimeSpan of the feature
     * @param c Color of the feature
     * @return MapGeometrySupport
     */
    public static MapGeometrySupport ellipseGeom(double lat, double lon, float semiMaj, float semiMin, float orient,
            TimeSpan span, Color c)
    {
        LatLonAlt lla = LatLonAlt.createFromDegrees(lat, lon, Altitude.ReferenceLevel.TERRAIN);
        MapGeometrySupport mgs = new SimpleMapEllipseGeometrySupport(lla, semiMaj, semiMin, orient);
        mgs.setColor(c, null);
        mgs.setTimeSpan(span);
        return mgs;
    }

    /**
     * Create a MapGeometrySupport for the specified point parameters.  The
     * return value is an instance of SimpleMapPointGeometrySupport.
     * @param lat latitude (degrees)
     * @param lon longitude (degrees)
     * @param t TimeSpan
     * @param c Color
     * @return MapGeometrySupport
     */
    public static MapGeometrySupport pointGeom(double lat, double lon, TimeSpan t, Color c)
    {
        MapGeometrySupport mgs = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(
                lat, lon, Altitude.ReferenceLevel.TERRAIN));
        mgs.setColor(c, null);
        mgs.setTimeSpan(t);
        return mgs;
    }

    /**
     * Create a "default" MapGeometrySupport for the specified parameters.  The
     * return value is an instance of DefaultMapPointGeometrySupport (cf.
     * pointGeom(double, double, TimeSpan, Color)).  This author is at a loss
     * to explain why we need two kinds of these and why both are used here.
     * @param lla LatLonAlt position of the point
     * @param span TimeSpan
     * @param c Color
     * @return MapGeometrySupport
     */
    public static MapGeometrySupport defPointGeom(LatLonAlt lla, TimeSpan span, Color c)
    {
        DefaultMapPointGeometrySupport pt = new DefaultMapPointGeometrySupport(lla);
        pt.setTimeSpan(span);
        pt.setColor(c, null);
        return pt;
    }

    /**
     * Extract a Color from the arcane API of DataTypeInfo, if possible.
     * @param t DataTypeInfo
     * @return the layer's Color, if any, or null
     */
    public static Color colorOf(DataTypeInfo t)
    {
        BasicVisualizationInfo vi = t.getBasicVisualizationInfo();
        if (vi != null)
        {
            return vi.getTypeColor();
        }
        return null;
    }

    /**
     * Private CTOR.
     */
    private DataElementFactory()
    {
    }
}
