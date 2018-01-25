package io.opensphere.kml.mantle.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Location;
import de.micromata.opengis.kml.v_2_2_0.Model;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import io.opensphere.core.model.DoubleRange;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLFeatureUtils;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * A bean that represents a KML feature to be put in Mantle.
 */
public class KMLMantleFeature
{
    /** NAME constant. */
    private static final String NAME = "Name";

//    /** DESCRIPTION constant. */
//    private static final String DESCRIPTION = "Description";

    /** LAT constant. */
    private static final String LAT = "Latitude";

    /** LON constant. */
    private static final String LON = "Longitude";

    /** ALT constant. */
    private static final String ALT = "Alt (m)";

    /** TIME constant. */
    private static final String TIME = "Time";

    /** GEOM_TYPE constant. */
    private static final String GEOM_TYPE = "Geometry Type";

    /** The KMLFeature. */
    private final KMLFeature myFeature;

    /** The time span. */
    private final TimeSpan myTimeSpan;

    /**
     * Create an object MetaDataInfo and return it.
     *
     * @return An object MetaDataInfo
     */
    public static DefaultMetaDataInfo newMetaDataInfo()
    {
        DefaultMetaDataInfo metaDataInfo = new DefaultMetaDataInfo();
        metaDataInfo.addKey(NAME, String.class, null);
//        metaDataInfo.addKey(DESCRIPTION, String.class, null);
        metaDataInfo.addKey(LAT, DoubleRange.class, null);
        metaDataInfo.addKey(LON, DoubleRange.class, null);
        metaDataInfo.addKey(ALT, DoubleRange.class, null);
        metaDataInfo.addKey(TIME, TimeSpan.class, null);
        metaDataInfo.addKey(GEOM_TYPE, String.class, null);
        metaDataInfo.setSpecialKey(LAT, LatitudeKey.DEFAULT, null);
        metaDataInfo.setSpecialKey(LON, LongitudeKey.DEFAULT, null);
        metaDataInfo.setSpecialKey(ALT, AltitudeKey.DEFAULT, null);
        metaDataInfo.setSpecialKey(TIME, TimeKey.DEFAULT, null);
        return metaDataInfo;
    }

    /**
     * Gets the altitude range for the coordinates.
     *
     * @param coordinates The coordinates
     * @return The altitude range
     */
    private static DoubleRange getAltitudeRange(List<Coordinate> coordinates)
    {
        DoubleRange range = null;
        if (coordinates.size() == 1)
        {
            double value = coordinates.get(0).getAltitude();
            range = new DoubleRange(value, value);
        }
        else if (coordinates.size() > 1)
        {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (Coordinate coordinate : coordinates)
            {
                min = Math.min(min, coordinate.getAltitude());
                max = Math.max(max, coordinate.getAltitude());
            }
            range = new DoubleRange(min, max);
        }
        return range;
    }

    /**
     * Gets a list of all coordinates in the geometry.
     *
     * @param geometry The geometry
     * @return A list of all coordinates
     */
    private static List<Coordinate> getCoordinates(Geometry geometry)
    {
        List<Coordinate> coordinates;
        if (geometry instanceof Point)
        {
            coordinates = ((Point)geometry).getCoordinates();
        }
        else if (geometry instanceof LineString)
        {
            coordinates = ((LineString)geometry).getCoordinates();
        }
        else if (geometry instanceof LinearRing)
        {
            coordinates = ((LinearRing)geometry).getCoordinates();
        }
        else if (geometry instanceof Polygon)
        {
            coordinates = New.list();
            coordinates.addAll(((Polygon)geometry).getOuterBoundaryIs().getLinearRing().getCoordinates());
            for (Boundary innerBoundary : ((Polygon)geometry).getInnerBoundaryIs())
            {
                coordinates.addAll(innerBoundary.getLinearRing().getCoordinates());
            }
        }
        else if (geometry instanceof MultiGeometry)
        {
            coordinates = New.list();
            for (Geometry childGeometry : ((MultiGeometry)geometry).getGeometry())
            {
                coordinates.addAll(getCoordinates(childGeometry));
            }
        }
        else
        {
            coordinates = Collections.emptyList();
        }
        return coordinates;
    }

    /**
     * Gets the latitude range for the coordinates.
     *
     * @param coordinates The coordinates
     * @return The latitude range
     */
    private static DoubleRange getLatitudeRange(List<Coordinate> coordinates)
    {
        DoubleRange range = null;
        if (coordinates.size() == 1)
        {
            double value = coordinates.get(0).getLatitude();
            range = new DoubleRange(value, value);
        }
        else if (coordinates.size() > 1)
        {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (Coordinate coordinate : coordinates)
            {
                min = Math.min(min, coordinate.getLatitude());
                max = Math.max(max, coordinate.getLatitude());
            }
            range = new DoubleRange(min, max);
        }
        return range;
    }

    /**
     * Gets the longitude range for the coordinates.
     *
     * @param coordinates The coordinates
     * @return The longitude range
     */
    private static DoubleRange getLongitudeRange(List<Coordinate> coordinates)
    {
        DoubleRange range = null;
        if (coordinates.size() == 1)
        {
            double value = coordinates.get(0).getLongitude();
            range = new DoubleRange(value, value);
        }
        else if (coordinates.size() > 1)
        {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (Coordinate coordinate : coordinates)
            {
                min = Math.min(min, coordinate.getLongitude());
                max = Math.max(max, coordinate.getLongitude());
            }
            range = new DoubleRange(min, max);
        }
        return range;
    }

    /**
     * Constructor.
     *
     * @param feature The feature
     * @param timeSpan The time span of the feature
     */
    public KMLMantleFeature(KMLFeature feature, TimeSpan timeSpan)
    {
        myFeature = feature;
        myTimeSpan = timeSpan;
    }

    /**
     * Create a new metadata provider with this feature's metadata values.
     *
     * @param metaDataInfo The MetaDataInfo
     * @param dataSource The data source
     * @return The metadata provider
     */
    public MetaDataProvider newMetaDataProvider(MetaDataInfo metaDataInfo, KMLDataSource dataSource)
    {
        MetaDataProvider metaDataProvider = new MDILinkedMetaDataProvider(metaDataInfo);

        // Set name
        metaDataProvider.setValue(NAME, StringUtilities.safeTrim(StringUtilities.removeHTML(myFeature.getName())));

//        // Set description
//        metaDataProvider.setValue(DESCRIPTION, StringUtilities.safeTrim(StringUtilities.removeHTML(myFeature.getDescription())));

        // Set lat/lon/alt
        Geometry geom = ((Placemark)myFeature.getFeature()).getGeometry();
        if (geom instanceof Model)
        {
            Location modelLocation = ((Model)geom).getLocation();
            metaDataProvider.setValue(LAT, new DoubleRange(modelLocation.getLatitude()));
            metaDataProvider.setValue(LON, new DoubleRange(modelLocation.getLongitude()));
            metaDataProvider.setValue(ALT, new DoubleRange(modelLocation.getAltitude()));
        }
        else
        {
            List<Coordinate> coordinates = getCoordinates(geom);
            metaDataProvider.setValue(LAT, getLatitudeRange(coordinates));
            metaDataProvider.setValue(LON, getLongitudeRange(coordinates));
            metaDataProvider.setValue(ALT, getAltitudeRange(coordinates));
        }

        // Set time
        metaDataProvider.setValue(TIME, myTimeSpan);

        // Set geometry type
        String geometryType = geom != null ? geom.getClass().getSimpleName() : null;
        metaDataProvider.setValue(GEOM_TYPE, geometryType);

        // Add the extended data
        if (myFeature.getExtendedData() != null)
        {
            Map<String, String> extendedDataMap = KMLFeatureUtils.getExtendedDataMap(myFeature.getFeature());
            for (Map.Entry<String, String> entry : extendedDataMap.entrySet())
            {
                metaDataProvider.setValue(StringUtilities.removeHTML(entry.getKey()), entry.getValue());
            }
        }

        return metaDataProvider;
    }

    @Override
    public String toString()
    {
        return myFeature.getName();
    }
}
