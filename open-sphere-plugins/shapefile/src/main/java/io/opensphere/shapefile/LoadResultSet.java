package io.opensphere.shapefile;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;
import io.opensphere.mantle.data.util.MantleJTSGeometryUtils;

/**
 * Class for a load result set.
 */
public class LoadResultSet
{
    /** The bounding geometry. */
    private Geometry myBoundingGeometry;

    /** The MapDataElements. */
    private final List<MapDataElement> myDataElements = New.linkedList();

    /** The {@link DataTypeInfo}. */
    private ShapeFileDataTypeInfo myDataTypeInfo;

    /** The overall time span. */
    private TimeSpan myOverallTimespan;

    /**
     * Examines the results in this Result set and calculates the time and
     * geometry bounds.
     */
    public void determineTimeAndGeometryBounds()
    {
        if (myDataElements != null && !myDataElements.isEmpty())
        {
            // Determine the overall timespan and geometry bounds.
            ExtentAccumulator extentAccumulator = new ExtentAccumulator();
            double minLat = Double.POSITIVE_INFINITY;
            double maxLat = Double.NEGATIVE_INFINITY;
            double minLon = Double.POSITIVE_INFINITY;
            double maxLon = Double.NEGATIVE_INFINITY;

            for (MapDataElement mde : myDataElements)
            {
                // Do the earliest latest time stuff first.
                if (!mde.getTimeSpan().isTimeless())
                {
                    extentAccumulator.add(mde.getTimeSpan());
                }

                // Do the geometry stuff second.
                MapGeometrySupport mgs = mde.getMapGeometrySupport();

                mgs.setTimeSpan(mde.getTimeSpan());
                if (mgs instanceof MapLocationGeometrySupport)
                {
                    MapLocationGeometrySupport mlgs = (MapLocationGeometrySupport)mde.getMapGeometrySupport();
                    double lat = mlgs.getLocation().getNormalizedLatD();
                    double lon = mlgs.getLocation().getNormalizedLonD();

                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);
                    minLon = Math.min(minLon, lon);
                    maxLon = Math.max(maxLon, lon);
                }
                else if (mgs instanceof MapPathGeometrySupport)
                {
                    MapPathGeometrySupport mpgs = (MapPathGeometrySupport)mgs;
                    for (LatLonAlt lla : mpgs.getLocations())
                    {
                        double lat = lla.getNormalizedLatD();
                        double lon = lla.getNormalizedLonD();

                        minLat = Math.min(minLat, lat);
                        maxLat = Math.max(maxLat, lat);
                        minLon = Math.min(minLon, lon);
                        maxLon = Math.max(maxLon, lon);
                    }
                }
            }

            myOverallTimespan = extentAccumulator.getExtent();
            if (myOverallTimespan.isZero())
            {
                myOverallTimespan = TimeSpan.TIMELESS;
            }

            myBoundingGeometry = MantleJTSGeometryUtils.createPolygon(minLon, maxLon, minLat, maxLat, new GeometryFactory());

            LatLonAlt lowerLeftCorner = LatLonAlt.createFromDegrees(minLat, minLon);
            LatLonAlt upperRightCorner = LatLonAlt.createFromDegrees(maxLat, maxLon);
            myDataTypeInfo.addBoundingBox(new GeographicBoundingBox(lowerLeftCorner, upperRightCorner));
            myDataTypeInfo.setTimeExtents(new DefaultTimeExtents(myOverallTimespan), this);
        }
    }

    /**
     * Gets the bounding geometry.
     *
     * @return the bounding geometry
     */
    public Geometry getBoundingGeometry()
    {
        return myBoundingGeometry;
    }

    /**
     * Gets the {@link List} of {@link MapDataElement}.
     *
     * @return the list of map data elements.
     */
    public List<MapDataElement> getDataElements()
    {
        return myDataElements;
    }

    /**
     * Gets the {@link DataTypeInfo}.
     *
     * @return the data type info.
     */
    public ShapeFileDataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    /**
     * Gets the overall timespan.
     *
     * @return the overall timespan
     */
    public TimeSpan getOverallTimespan()
    {
        return myOverallTimespan;
    }

    /**
     * Sets the bounding geometry.
     *
     * @param boundingGeometry the new bounding geometry
     */
    public void setBoundingGeometry(Geometry boundingGeometry)
    {
        myBoundingGeometry = boundingGeometry;
    }

    /**
     * Sets the {@link DataTypeInfo}.
     *
     * @param dti the new data type info
     */
    public void setDataTypeInfo(ShapeFileDataTypeInfo dti)
    {
        myDataTypeInfo = dti;
    }

    /**
     * Sets the overall timespan.
     *
     * @param overallTimespan the new overall timespan
     */
    public void setOverallTimespan(TimeSpan overallTimespan)
    {
        myOverallTimespan = overallTimespan;
    }
}
