package io.opensphere.lidar.util;

import java.io.File;
import java.util.List;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import io.opensphere.core.model.GeographicConvexPolygon;
import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.gdal.GDALGenericUtilities;
import io.opensphere.core.util.gdal.LockingDataSetSampler;

/**
 * Reader for loading and obtaining values from a GeoTIFF file containing LiDAR
 * data.
 */
public class GeotiffFileReader
{
    /**
     * A helper which retrieves values from the {@link Dataset} which reads
     * values from the GeoTIFF.
     */
    private final LockingDataSetSampler myDataSetSampler;

    /** The file location from which this reader reads. */
    private final String myFilePath;

    /** Whether the reader was successfully initialized. */
    private final boolean myInitSuccess;

    /**
     * Get the data set.
     *
     * @param path The path to the data.
     * @param accessType The access type.
     * @return The data set or <code>null</code> if acquisition failed.
     */
    public static synchronized Dataset acquireDataSet(String path, int accessType)
    {
        if (GDALGenericUtilities.loadGDAL())
        {
            return gdal.Open(path, accessType);
        }
        return null;
    }

    /**
     * Constructor.
     *
     * @param file The file which contains the LiDAR data.
     * @param isNorthOriented False if the inverse transform should be used,
     *            True if we should assume the image is north oriented.
     */
    public GeotiffFileReader(File file, boolean isNorthOriented)
    {
        myFilePath = file.getAbsolutePath();
        Dataset dataset = GeotiffFileReader.acquireDataSet(myFilePath, 0);
        myInitSuccess = dataset != null;
        myDataSetSampler = new LockingDataSetSampler(dataset, isNorthOriented);
    }

    /**
     * Gets whether the reader was successfully initialized.
     *
     * @return whether the reader was successfully initialized
     */
    public boolean isInitSuccess()
    {
        return myInitSuccess;
    }

    /** Perform any required cleanup. */
    public synchronized void close()
    {
        myDataSetSampler.close();
    }

    /**
     * Get the geographic corners of the data set. The order of the points is
     * counter-clockwise starting with the image's lower left corner.
     *
     * @return The geographic corners of the data set.
     */
    public List<LatLonAlt> getCorners()
    {
        List<LatLonAlt> corners = New.list();
        int width = myDataSetSampler.getImageWidth();
        int height = myDataSetSampler.getImageHeight();

        corners.add(myDataSetSampler.getGeoPosition(0, 0));
        corners.add(myDataSetSampler.getGeoPosition(width, 0));
        corners.add(myDataSetSampler.getGeoPosition(width, height));
        corners.add(myDataSetSampler.getGeoPosition(0, height));

        return corners;
    }

    /**
     * Get the dataSetSampler.
     *
     * @return the dataSetSampler
     */
    public LockingDataSetSampler getDataSetSampler()
    {
        return myDataSetSampler;
    }

    /**
     * Get the file path.
     *
     * @return the file path to get.
     */
    public String getFilePath()
    {
        return myFilePath;
    }

    /**
     * Get the bounding polygon for the region this reader can provide
     * elevations.
     *
     * @return the bonding polygons.
     */
    public List<GeographicPolygon> getRegionsAsPolygons()
    {
        List<GeographicPosition> corners = New.list(4);
        int width = myDataSetSampler.getImageWidth();
        int height = myDataSetSampler.getImageHeight();

        corners.add(new GeographicPosition(myDataSetSampler.getGeoPosition(0, height)));
        corners.add(new GeographicPosition(myDataSetSampler.getGeoPosition(width, height)));
        corners.add(new GeographicPosition(myDataSetSampler.getGeoPosition(width, 0)));
        corners.add(new GeographicPosition(myDataSetSampler.getGeoPosition(0, 0)));

        List<GeographicPolygon> regions = New.list(1);
        regions.add(new GeographicConvexPolygon(corners));
        return regions;
    }
}
