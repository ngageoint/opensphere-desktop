package io.opensphere.core.util.gdal;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;

import io.opensphere.core.model.GeographicBoundingBox;

/** GDAL I/O Utilities. */
public final class GdalIOUtilities
{
    //@formatter:off
    /*
    GEOGCS[
      "WGS 84",
      DATUM[
        "WGS_1984",
        SPHEROID[
          "WGS 84",
          6378137,
          298.257223563,
          AUTHORITY["EPSG","7030"]
        ],
        AUTHORITY["EPSG","6326"]
      ],
      PRIMEM[
        "Greenwich",
        0,
        AUTHORITY["EPSG","8901"]
      ],
      UNIT[
        "degree",
        0.01745329251994328,
        AUTHORITY["EPSG","9122"]
      ],
      AUTHORITY["EPSG","4326"]
    ]
    */
    //@formatter:on
    /** "Well Known Text" String for the projection in standard use. */
    private static final String WGS_84_WKT = "GEOGCS[\"WGS 84\","
            + "DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],"
            + "AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],"
            + "UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";

    /**
     * Exports the image as a GeoTIFF.
     *
     * @param image the image
     * @param bounds the bounding box
     * @param outFile the file to write to
     */
    public static void exportTiff(BufferedImage image, GeographicBoundingBox bounds, File outFile)
    {
        int pixelW = image.getWidth();
        int pixelH = image.getHeight();
        double lat0 = bounds.getMinLatD();
        double dLat = bounds.getMaxLatD() - lat0;
        double lon0 = bounds.getMinLonD();
        double dLon = bounds.getMaxLonD() - lon0;

        // if this method returns false, then supposedly GDAL is offline
        GDALGenericUtilities.loadGDAL();

        Driver dr = gdal.GetDriverByName("GTiff");
        Dataset data = dr.Create(outFile.getAbsolutePath(), pixelW, pixelH, 4);
        data.SetProjection(WGS_84_WKT);
        data.SetGeoTransform(new double[] { lon0, dLon / pixelW, 0.0, lat0 + dLat, 0.0, -dLat / pixelH });

        copyToTiff(image, data);

        // supposedly, this method will close the file--hope it doesn't really
        // do what its name suggests
        data.delete();
    }

    /**
     * Utility method to transcribe the contents of a BufferedImage into a TIFF
     * controlled by a GDAL Dataset.
     *
     * @param img the BufferedImage to transcribe
     * @param data the GDAL Dataset
     */
    private static void copyToTiff(BufferedImage img, Dataset data)
    {
        int w = img.getWidth();
        int h = img.getHeight();
        WritableRaster rast = img.getRaster();
        int numBands = rast.getNumBands();
        int[] vals = new int[w * h];
        // Note: GetRasterBand method uses 1-based indexing, hence the "+1"
        for (int b = 0; b < numBands; b++)
        {
            copyToBand(rast.getSamples(0, 0, w, h, b, vals), data.GetRasterBand(b + 1));
        }
    }

    /**
     * Utility to transcribe one band of data values. Mainly, this is used by by
     * copyToTiff (q.v.).
     *
     * @param vals image sample values
     * @param bnd the GDAL Band into which the data are written
     */
    private static void copyToBand(int[] vals, Band bnd)
    {
        bnd.WriteRaster(0, 0, bnd.getXSize(), bnd.getYSize(), vals);
        bnd.FlushCache();
    }

    /** Disallow instantiation. */
    private GdalIOUtilities()
    {
    }
}
