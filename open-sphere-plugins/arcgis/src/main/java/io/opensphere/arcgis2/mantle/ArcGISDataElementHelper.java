package io.opensphere.arcgis2.mantle;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.arcgis2.esri.Feature;
import io.opensphere.arcgis2.esri.Geometry;
import io.opensphere.core.common.lobintersect.LatLon;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.model.GeographicUtilities;
import io.opensphere.core.util.model.GeographicUtilities.PolygonWinding;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;

/** Creates data elements for ArcGIS layers. */
public final class ArcGISDataElementHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ArcGISDataElementHelper.class);

    /**
     * Creates a data element for the feature.
     *
     * @param dataType the data type
     * @param feature the feature
     * @return the data element
     */
    public static MapDataElement createDataElement(DataTypeInfo dataType, Feature feature)
    {
        // Use code from ArcRestSaxFeatureRequestResponseHandler to create the
        // data element

        DefaultMapDataElement dataElement = null;
        if (feature.getGeometry() != null)
        {
            MapGeometrySupport geometry;
            if (!isEmpty(feature.getGeometry()))
            {
                geometry = createGeometry(feature.getGeometry());
            }
            else
            {
                geometry = createGeometry(feature.getAttributes());
            }

            TimeSpan timeSpan = getTimeSpan(feature.getAttributes());
            if (timeSpan != TimeSpan.TIMELESS)
            {
                geometry.setTimeSpan(timeSpan);
            }

            MetaDataProvider provider = new MDILinkedMetaDataProvider(dataType.getMetaDataInfo());
            for (Map.Entry<String, Object> entry : feature.getAttributes().entrySet())
            {
                provider.setValue(entry.getKey(), (Serializable)entry.getValue());
            }
            dataElement = new DefaultMapDataElement(1L, timeSpan, dataType, provider, geometry);
            dataElement.getVisualizationState().setColor(dataType.getBasicVisualizationInfo().getTypeColor());
        }
        return dataElement;
    }

    /**
     * Determines if the geometry is empty.
     *
     * @param geometry the geometry
     * @return whether it's empty
     */
    private static boolean isEmpty(Geometry geometry)
    {
        return geometry.getX() == 0. && geometry.getY() == 0. && geometry.getPaths() == null && geometry.getRings() == null;
    }

    /**
     * Does a reasonable attempt at creating a geometry from the attributes.
     *
     * @param attributes the attributes
     * @return the mantle geometry
     */
    private static MapGeometrySupport createGeometry(Map<String, Object> attributes)
    {
        MapGeometrySupport mantleGeom;

        LatLonAlt point;
        Object latValue = getValue(attributes, new String[] { "LATITUDE", "LAT" });
        Object lonValue = getValue(attributes, new String[] { "LONGITUDE", "LONG", "LON" });
        if (latValue instanceof Number && lonValue instanceof Number)
        {
            point = LatLonAlt.createFromDegrees(((Number)latValue).doubleValue(), ((Number)lonValue).doubleValue());
        }
        else
        {
            point = LatLonAlt.createFromDegrees(0, 0);
        }
        mantleGeom = new SimpleMapPointGeometrySupport(point);

        return mantleGeom;
    }

    /**
     * Placeholder for a method to get the timespan from the attributes.
     *
     * @param attributes the attributes
     * @return the TimeSpan
     */
    static TimeSpan getTimeSpan(Map<String, Object> attributes)
    {
        TimeSpan span = TimeSpan.TIMELESS;
        // TODO someday we should either guess the time span or get the field(s) from the user
        return span;
    }

    /**
     * Gets the attribute value for the first key that contains one of the column strings.
     *
     * @param attributes the attributes
     * @param columns the column strings
     * @return the attribute value, or null
     */
    private static Object getValue(Map<String, Object> attributes, String... columns)
    {
        Object value = null;
        for (String column : columns)
        {
            value = attributes.entrySet().stream().filter(e -> e.getKey().contains(column)).map(e -> e.getValue())
                    .filter(Objects::nonNull).findFirst().orElse(null);
            if (value != null)
            {
                break;
            }
        }
        return value;
    }

    /**
     * Creates a mantle geometry from the ArcGIS geometry.
     *
     * @param arcGeom the ArcGIS geometry
     * @return the mantle geometry
     */
    private static MapGeometrySupport createGeometry(Geometry arcGeom)
    {
        MapGeometrySupport mantleGeom = null;
        if (arcGeom.getRings() != null)
        {
            List<List<LatLonAlt>> rings = toLatLonAlts(arcGeom.getRings());
            mantleGeom = createPolygon(rings);
        }
        else if (arcGeom.getPaths() != null)
        {
            List<List<LatLonAlt>> paths = toLatLonAlts(arcGeom.getPaths());
            mantleGeom = createPolyine(paths);
        }
        else
        {
            LatLonAlt point = LatLonAlt.createFromDegrees(arcGeom.getY(), arcGeom.getX());
            mantleGeom = new SimpleMapPointGeometrySupport(point);
        }
        return mantleGeom;
    }

    /**
     * Converts a triple double array to a list of lists of {@link LatLon}.
     *
     * @param coords The coordinates to convert.
     * @return The converted coordinates.
     */
    private static List<List<LatLonAlt>> toLatLonAlts(double[][][] coords)
    {
        List<List<LatLonAlt>> rings = New.list(coords.length);
        for (double[][] arcRing : coords)
        {
            List<LatLonAlt> ring = New.list(arcRing.length);
            for (double[] arcPoint : arcRing)
            {
                ring.add(LatLonAlt.createFromDegrees(arcPoint[1], arcPoint[0]));
            }
            rings.add(ring);
        }

        return rings;
    }

    /**
     * Converts a esriGeometryPolygon into a {@link MapGeometrySupport}.
     *
     * @param pointListList the list of lists of lat/lon pairs for the individual polylines
     * @return a list of the polygons from the conversion.
     */
    private static MapPolygonGeometrySupport createPolygon(List<List<LatLonAlt>> pointListList)
    {
        DefaultMapPolygonGeometrySupport geometry = null;
        try
        {
            Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> polygons = groupInnerRings(pointListList);

            Set<Entry<List<LatLonAlt>, Collection<List<LatLonAlt>>>> entrySet = polygons.entrySet();
            if (!entrySet.isEmpty())
            {
                Entry<List<LatLonAlt>, Collection<List<LatLonAlt>>> entry = entrySet.iterator().next();
                geometry = new DefaultMapPolygonGeometrySupport(entry.getKey(), entry.getValue());
                geometry.setFollowTerrain(true, null);
            }
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Error converting feature to polygon.", e);
        }
        return geometry;
    }

    /**
     * Converts a esriGeometryPolygon into a {@link MapGeometrySupport}.
     *
     * @param pointListList the list of lists of lat/lon pairs for the individual polylines
     * @return a list of the polygons from the conversion.
     */
    private static DefaultMapPolylineGeometrySupport createPolyine(List<List<LatLonAlt>> pointListList)
    {
        DefaultMapPolylineGeometrySupport geometry = null;

        for (List<LatLonAlt> line : pointListList)
        {
            if (geometry == null)
            {
                geometry = new DefaultMapPolylineGeometrySupport(line);
            }
            else
            {
                geometry.addChild(new DefaultMapPolylineGeometrySupport(line));
            }
        }

        return geometry;
    }

    /**
     * Decompose polygon based on closing positions.
     *
     * @param rings the input list of LatLonAlt.
     * @return the decomposed parts list of lists of lat lon alt.
     */
    private static Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> groupInnerRings(List<List<LatLonAlt>> rings)
    {
        Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> polygons = New.map();

        Collection<List<LatLonAlt>> holes = null;
        for (List<LatLonAlt> polygon : rings)
        {
            // Exterior rings are defined in clockwise order and
            // inner rings are defined in counter-clockwise order.
            PolygonWinding winding = GeographicUtilities.getNaturalWinding(polygon);
            if (winding == PolygonWinding.CLOCKWISE)
            {
                holes = New.collection();
                polygons.put(polygon, holes);
            }
            else
            {
                if (holes != null)
                {
                    holes.add(polygon);
                }
                else
                {
                    LOGGER.error("Polygon hole listed before polygon or incorrect winding for polygon.");
                }
            }
        }

        return polygons;
    }

    /** Disallow instantiation. */
    private ArcGISDataElementHelper()
    {
    }
}
