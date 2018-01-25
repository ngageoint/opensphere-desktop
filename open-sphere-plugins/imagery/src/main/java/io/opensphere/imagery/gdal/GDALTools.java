package io.opensphere.imagery.gdal;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.GCP;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.gdal.GDALGenericUtilities;
import io.opensphere.imagery.transform.ImageryTransform;
import io.opensphere.imagery.transform.ImageryTransformFactory;

/**
 * A class to support the interface to GDAL, allowing access to pieces of
 * geo-referenced imagery. Wide range of capabilities supported regarding
 * geo-referenced imagery. A GCP is a ground control point.
 *
 * On 2014/12/05 a lot of unused members were removed from this class - please
 * see the previous version if they become needed.
 */
@SuppressWarnings({ "PMD.ReplaceVectorWithList", "PMD.UseArrayListInsteadOfVector", "PMD.GodClass" })
// GDAL uses Vectors
public final class GDALTools
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(GDALTools.class);

    /** The Constant NINE. */
    private static final double NINE = 9.0;

    /** The our driver name to default extensions map. */
    private static Map<String, String[]> ourDriverNameToDefaultExtensionsMap;

    /** The Constant TEN. */
    private static final double TEN = 10.0;

    /** The Constant TWO_FIFTY_FIVE. */
    private static final double TWO_FIFTY_FIVE = 255.0;

    /** The Backup image bounding box. */
    private GeographicBoundingBox myBackupImageBoundingBox;

    /** The Brightening. */
    private double myBrightening = 1.0;

    /** The Current transform finder. */
    private ImageryTransform myCurrentTransformFinder;

    /** The Current transform finder lock. */
    private final ReentrantLock myCurrentTransformFinderLock = new ReentrantLock();

    /** The File. */
    private File myFile;

    /** The Original transform's min / max sector. */
    private GeographicBoundingBox myOriginalSector;

    /** The raster x size. */
    private int myRasterXSize;

    /** The raster y size. */
    private int myRasterYSize;

    static
    {
        /**
         * Note to Maintainers: This load library sequence is important. Each of
         * these shared libraries has interdependencies on others. If you just
         * load the top level one it may crash the jvm.
         *
         * This is because java.library.path is ignored after the first load,
         * since it's the OS doing the dependency resolve search for the shared
         * library's deps, not the jvm. Obviously, it has no concept of the
         * java.library.path.
         *
         * So, here we are loading the dependencies from bottom to top. Then,
         * when each library is loaded, its required library is already in
         * memory ready to go. This is known to work in the Windows and Linux
         * x86 environments.
         */
        GDALGenericUtilities.loadGDAL();
        populateDefaultExtensions();
    }

    /**
     * Retrieve the GDAL dataset from a file. The data set is a GDAL
     * summary/model of the image.
     *
     * @param f the f
     * @return the data set
     */
    public static Dataset getDataSet(File f)
    {
        Dataset poDataset = null;
        try
        {
            poDataset = gdal.Open(f.getAbsolutePath(), gdalconst.GA_ReadOnly);
            if (poDataset == null)
            {
                LOGGER.error("The image " + f.getAbsolutePath() + " could not be read");
                return null;
            }
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Exception while loading " + f.getAbsolutePath(), e);
        }
        return poDataset;
    }

    /**
     * Predict image histogram.
     *
     * @param file the file
     * @return the histo info
     */
    public static HistoInfo predictImageHistogram(File file)
    {
        long time1 = System.currentTimeMillis();
        HistoInfo histInfoToReturn = new HistoInfo();
        // Each int[] contains the histo for a single band. Planning 3 bands
        // here,
        // but there's no reason there couldn't be more, (or fewer).
        // ArrayList<int[]> imageHistogram = new ArrayList<>(3);
        Dataset dataSet = getDataSet(file);
        // TODO Ultimately this will need to be controllable - ie: which bands need to be profiled specifically? Not just the first 3 in a multiband image.
        int bandCount = Math.min(dataSet.getRasterCount(), 4);

        Band poBand = null;
        // bands are 1-based, THANKS FORTRAN!
        for (int band = 1; band <= bandCount; band++)
        {
            poBand = dataSet.GetRasterBand(band);

            double[] min = new double[1];
            double[] max = new double[1];
            double[] mean = new double[1];
            double[] stddev = new double[1];

            long time4 = System.currentTimeMillis();
            int haveToScanReturn = poBand.GetStatistics(true, false, min, max, mean, stddev);
            long time5 = System.currentTimeMillis();
            boolean haveToScanImage = haveToScanReturn == gdalconst.CE_Warning;
            if (haveToScanImage)
            {
                time4 = System.currentTimeMillis();
                poBand.GetStatistics(true, true, min, max, mean, stddev);
                time5 = System.currentTimeMillis();
            }

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("approx, band " + band + ": " + min[0] + " " + max[0] + " " + mean[0] + " " + stddev[0] + " time:"
                        + (time5 - time4) + " scanned? " + haveToScanImage);
            }

            int[] histo = new int[256];
            poBand.GetHistogram(0, 255, histo, true, true);

            BandHistoBean bandHisto = new BandHistoBean(min[0], max[0], mean[0], stddev[0], histo);
            histInfoToReturn.addBandHisto(bandHisto);
            // imageHistogram.add(histo);
        }
        if (LOGGER.isTraceEnabled())
        {
            long time2 = System.currentTimeMillis();
            LOGGER.trace("Done with histo, total time:" + (time2 - time1));
        }

        // writeHistoToFile(imageHistogram);
        return histInfoToReturn;
    }

    /**
     * Take a MiniSector and a new LatLon and enlarge the MiniSector to contain
     * the LatLon.
     *
     * @param latLon the lat lon
     * @param ms the ms
     */
    private static void adjustMinMax(LatLonAlt latLon, MiniSector ms)
    {
        if (latLon.getLatD() < ms.getMinLat())
        {
            ms.setMinLat(latLon.getLatD());
        }
        if (latLon.getLatD() > ms.getMaxLat())
        {
            ms.setMaxLat(latLon.getLatD());
        }
        if (latLon.getLonD() < ms.getMinLon())
        {
            ms.setMinLon(latLon.getLonD());
        }
        if (latLon.getLonD() > ms.getMaxLon())
        {
            ms.setMaxLon(latLon.getLonD());
        }
    }

    /**
     * Walks the edge of a tile to determine its min/max lat lon. This is only
     * important when the image is not north up. Otherwise computing min/max is
     * alot easier.
     *
     * @param poDataset the po dataset
     * @return the min max sector original
     */
    private static GeographicBoundingBox getMinMaxSectorOriginal(Dataset poDataset)
    {
        return getMinMaxSectorOriginal(poDataset, TEN, 0, poDataset.getRasterXSize(), 0, poDataset.getRasterYSize());
    }

    /**
     * Walks the edge of a tile to determine its min/max lat lon. This is only
     * important when the image is not north up. Otherwise computing min/max is
     * alot easier.
     *
     * @param poDataset the po dataset
     * @param jump - percentage to move each jump around the peremiter
     * @param beginX the begin x
     * @param endX the end x
     * @param beginY the begin y
     * @param endY the end y
     * @return the min max sector original
     */
    private static GeographicBoundingBox getMinMaxSectorOriginal(Dataset poDataset, double jump, int beginX, int endX, int beginY,
            int endY)
    {
        MiniSector ms = new MiniSector();
        // checking the perimeter in divisions for the min/max lat lon,
        // important for non-north projection
        double divX = poDataset.getRasterXSize() / jump;
        double divY = poDataset.getRasterYSize() / jump;

        double xIter = beginX;
        double yIter = beginY;
        for (yIter = beginY; yIter <= poDataset.getRasterYSize(); yIter += divY)
        {
            adjustMinMax(GDALInfo.getGeoCoordinateFromPixelAsLatLon(poDataset, xIter, yIter), ms);
        }

        xIter = poDataset.getRasterXSize();
        for (yIter = beginY; yIter <= poDataset.getRasterYSize(); yIter += divY)
        {
            adjustMinMax(GDALInfo.getGeoCoordinateFromPixelAsLatLon(poDataset, xIter, yIter), ms);
        }

        yIter = beginY;
        for (xIter = beginX; xIter <= poDataset.getRasterXSize(); xIter += divX)
        {
            adjustMinMax(GDALInfo.getGeoCoordinateFromPixelAsLatLon(poDataset, xIter, yIter), ms);
        }

        yIter = poDataset.getRasterYSize();
        for (xIter = beginX; xIter <= poDataset.getRasterXSize(); xIter += divX)
        {
            adjustMinMax(GDALInfo.getGeoCoordinateFromPixelAsLatLon(poDataset, xIter, yIter), ms);
        }

        return ms.toSector();
    }

    /**
     * Helper method to populate ourDriverNameToDefaultExtensionsMap.
     */
    private static void populateDefaultExtensions()
    {
        ourDriverNameToDefaultExtensionsMap = new LinkedHashMap<>(75);
        ourDriverNameToDefaultExtensionsMap.put("GeoTIFF", new String[] { ".tif", ".tiff", "geotiff" });
        // TODO is this right?
        ourDriverNameToDefaultExtensionsMap.put("JPEG", new String[] { ".tif", ".tiff", "geotiff" });
        ourDriverNameToDefaultExtensionsMap.put("JPEG", new String[] { ".tif", ".tiff", "geotiff" });
    }

    /**
     * Default constructor provided for class loading only.
     */
    public GDALTools()
    {
    }

    /**
     * Each imagery file gets associated with a different GDALTools.
     *
     * @param file the file
     * @param backupImageBoundingBox A bounding box for the backup image.
     */
    public GDALTools(File file, GeographicBoundingBox backupImageBoundingBox)
    {
        myFile = file;
        myBackupImageBoundingBox = backupImageBoundingBox;
        myBrightening = 1.0;
        prePrepare();
    }

    /**
     * Summarize a geo-referenced image into World Wind terms.
     *
     * @param f the f
     * @param tileSize , fileSize
     * @return the gDAL image layer data
     */
    public GDALImageLayerData retrieveGDALImageLayerDataOriginal(File f, int tileSize)
    {
        // XXX this uses direct file original transform
        Dataset poDataset = getDataSet(f);

        double[] geoTransform = new double[6];

        poDataset.GetGeoTransform(geoTransform);

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Origin = (" + geoTransform[0] + ", " + geoTransform[3] + ")");
        }

        myOriginalSector = getMinMaxSectorOriginal(poDataset);
        GDALImageLayerData ret = new GDALImageLayerData();
        ret.setSector(myOriginalSector);
        ret.setXWidth(poDataset.getRasterXSize());
        ret.setYWidth(poDataset.getRasterYSize());

        ret.determineSettings(tileSize, f.length());

        return ret;
    }

    /**
     * Find a sub image(tile), using the current transform.
     *
     * @param f the f
     * @param sector the bounding box
     * @param xsize the xsize
     * @param ysize the ysize
     * @param imageHisto the image histo
     * @return the buffered image
     */
    public BufferedImage retrieveGeographicalPiece(File f, GeographicBoundingBox sector, int xsize, int ysize,
            HistoInfo imageHisto)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("retrieveGeographicalPiece" + sector);
        }

        Dataset poDataset = null;

        poDataset = gdal.Open(f.getAbsolutePath(), gdalconst.GA_ReadOnly);
        if (poDataset == null)
        {
            LOGGER.error("The image " + f.getAbsolutePath() + " could not be read");
            return null;
        }

        MiniSector requestBounds = new MiniSector();
        requestBounds.setMinLat(sector.getMinLatD());
        requestBounds.setMaxLat(sector.getMaxLatD());
        requestBounds.setMinLon(sector.getMinLonD());
        requestBounds.setMaxLon(sector.getMaxLonD());

        ImageryTransform aTransformer = cloneCurrentTransform();
        BufferedImage toReturn = retrieveGeographicalPiece(requestBounds, xsize, ysize, imageHisto, aTransformer);
        poDataset.delete();
        return toReturn;
    }

    /**
     * Clone the current transform, so that it could be handed off to foreign
     * code, or modified.
     *
     * @return the clone
     */
    private ImageryTransform cloneCurrentTransform()
    {
        ImageryTransform aTransformer;
        myCurrentTransformFinderLock.lock();
        try
        {
            aTransformer = myCurrentTransformFinder.clone();
        }
        finally
        {
            myCurrentTransformFinderLock.unlock();
        }
        return aTransformer;
    }

    /**
     * Switch from the GDAL data as returned, into a BufferedImage.
     *
     * @param gsmHelper the gsm helper
     * @param poBand the po band
     * @param imgBuffer the img buffer
     * @param sampleModel the sample model
     * @param dataType the data_type
     * @param bufferType the buffer_type
     * @return the buffered image
     */
    private BufferedImage createImageFromRaster(GeoSampleModelHelper gsmHelper, Band poBand, DataBuffer imgBuffer,
            SampleModel sampleModel, int dataType, int bufferType)
    {
        WritableRaster raster = Raster.createWritableRaster(sampleModel, imgBuffer, null);
        ColorModel cm = null;
        BufferedImage img = null;

        if (poBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex)
        {
            // data_type = BufferedImage.TYPE_BYTE_INDEXED;
            cm = poBand.GetRasterColorTable().getIndexColorModel(gdal.GetDataTypeSize(poBand.getDataType()));
            img = new BufferedImage(cm, raster, false, null);
        }
        else
        {
            ColorSpace cs = null;
            if (gsmHelper.getBandCount() == 4)
            {
                cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                cm = new ComponentColorModel(cs, true, false, ColorModel.TRANSLUCENT, bufferType);
                img = new BufferedImage(cm, raster, true, null);
            }
            else if (gsmHelper.getBandCount() > 2)
            {
                cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                cm = new ComponentColorModel(cs, false, false, ColorModel.OPAQUE, bufferType);
                img = new BufferedImage(cm, raster, true, null);
            }
            else
            {
                img = new BufferedImage(gsmHelper.getXsize(), gsmHelper.getYsize(), dataType);
                img.setData(raster);
            }
        }
        return img;
    }

    /**
     * Assists when a piece of an image intersects with a given tile.
     *
     * @param xsize the xsize
     * @param ysize the ysize
     * @return the buffered image
     */
    private BufferedImage createTransparentImage(int xsize, int ysize)
    {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage dst = new BufferedImage(xsize, ysize, type);

        int xp = new Color(0, 0, 0, 0).getRGB();

        for (int y = 0; y < ysize; y++)
        {
            for (int x = 0; x < xsize; x++)
            {
                dst.setRGB(x, y, xp);
            }
        }

        return dst;
    }

    /**
     * Helper method to find the pixel x/y location of curLat/curLon and enlarge
     * the MiniIntSector to contain that.
     *
     * @param curLat the cur lat
     * @param curLon the cur lon
     * @param mis the mis
     * @param aTransformFinder the a transform finder
     */
    private void findNewImageCoordinateMinsAndMaxs(double curLat, double curLon, MiniIntSector mis,
            ImageryTransform aTransformFinder)
    {
        int x1 = (int)Math.round(aTransformFinder.getXPixelBasedOnTransform(curLon, curLat));
        int y1 = (int)Math.round(aTransformFinder.getYPixelBasedOnTransform(curLon, curLat));

        if (mis.getX1() > x1)
        {
            mis.setX1(x1);
        }

        if (mis.getX2() < x1)
        {
            mis.setX2(x1);
        }

        if (mis.getY1() > y1)
        {
            mis.setY1(y1);
        }

        if (mis.getY2() < y1)
        {
            mis.setY2(y1);
        }
    }

    /**
     * Find the bounding MiniIntSector in image coordinates of a given Sector,
     * by walking the edge of the projected sector.
     *
     * @param poDataset the po dataset
     * @param sector the sector
     * @param jump the jump
     * @param aTransformFinder the a transform finder
     * @return the mini int sector
     */
    private MiniIntSector findProjectedBounds(Dataset poDataset, GeographicBoundingBox sector, double jump,
            ImageryTransform aTransformFinder)
    {
        MiniIntSector mis = new MiniIntSector();
        double curLat = 0;

        double curLon = sector.getMaxLonD();

        for (curLat = sector.getMinLatD(); curLat < sector.getMaxLatD(); curLat += jump)
        {
            findNewImageCoordinateMinsAndMaxs(curLat, curLon, mis, aTransformFinder);
            for (curLon = sector.getMinLonD(); curLon < sector.getMaxLonD(); curLon += jump)
            {
                findNewImageCoordinateMinsAndMaxs(curLat, curLon, mis, aTransformFinder);
            }
        }

        curLon = sector.getMinLonD();
        for (curLat = sector.getMinLatD(); curLat < sector.getMaxLatD(); curLat += jump)
        {
            findNewImageCoordinateMinsAndMaxs(curLat, curLon, mis, aTransformFinder);
        }

        curLat = sector.getMaxLatD();
        for (curLon = sector.getMinLonD(); curLon < sector.getMaxLonD(); curLon += jump)
        {
            findNewImageCoordinateMinsAndMaxs(curLat, curLon, mis, aTransformFinder);
        }

        curLat = sector.getMinLatD();
        for (curLon = sector.getMinLonD(); curLon < sector.getMaxLonD(); curLon += jump)
        {
            findNewImageCoordinateMinsAndMaxs(curLat, curLon, mis, aTransformFinder);
        }

        return mis;
    }

    /**
     * Find the geo transform, via GCP, or the embedded one. Favor the GCP
     * transform.
     *
     * @param e the e
     * @param dataSet the data set
     */
    private void findTransformAndFavorGCP(Enumeration<?> e, Dataset dataSet)
    {
        List<GroundControlPoint> ar = new ArrayList<>();
        while (e.hasMoreElements())
        {
            GCP gcp = (GCP)e.nextElement();

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(Thread.currentThread().getId() + " retrieveGeographicalPiece  x:" + gcp.getGCPX() + " y:"
                        + gcp.getGCPY() + " z:" + gcp.getGCPZ() + " pixel:" + gcp.getGCPPixel() + " line:" + gcp.getGCPLine()
                        + " line:" + gcp.getInfo());
            }

            GroundControlPoint gcpa = new GroundControlPoint();
            gcpa.setLat(gcp.getGCPY());
            gcpa.setLon(gcp.getGCPX());
            gcpa.setPixel(gcp.getGCPPixel());
            gcpa.setLine(gcp.getGCPLine());

            ar.add(gcpa);
        }

        if (ar.size() >= 3)
        {
            myCurrentTransformFinderLock.lock();
            try
            {
                myCurrentTransformFinder = ImageryTransformFactory.getImageryTransformBestFit(ar);
            }
            finally
            {
                myCurrentTransformFinderLock.unlock();
            }
        }
        else
        {
            ar.clear();

            int xSize = dataSet.getRasterXSize();
            int ySize = dataSet.getRasterYSize();

            double[] t = new double[6];

            dataSet.GetGeoTransform(t);

            // Upper Left:
            GroundControlPoint gcp1 = new GroundControlPoint(t[3], t[0], 0, 0);
            // Upper Right:
            GroundControlPoint gcp2 = new GroundControlPoint(t[3] + xSize * t[4], t[0] + xSize * t[1], xSize, 0);
            // Lower Left:
            GroundControlPoint gcp3 = new GroundControlPoint(t[3] + ySize * t[5], t[0] + ySize * t[2], 0, ySize);

            // Check for no transform in image
            if (myBackupImageBoundingBox != null && (gcp2.getPixel() == gcp2.getLon() || gcp3.getLine() == gcp3.getLat()))
            {
                gcp1.setLat(myBackupImageBoundingBox.getUpperLeft().getLatLonAlt().getLatD());
                gcp1.setLon(myBackupImageBoundingBox.getUpperLeft().getLatLonAlt().getLonD());
                gcp2.setLat(myBackupImageBoundingBox.getUpperRight().getLatLonAlt().getLatD());
                gcp2.setLon(myBackupImageBoundingBox.getUpperRight().getLatLonAlt().getLonD());
                gcp3.setLat(myBackupImageBoundingBox.getLowerLeft().getLatLonAlt().getLatD());
                gcp3.setLon(myBackupImageBoundingBox.getLowerLeft().getLatLonAlt().getLonD());
            }

            ar.add(gcp1);
            ar.add(gcp2);
            ar.add(gcp3);

            myCurrentTransformFinderLock.lock();
            try
            {
                myCurrentTransformFinder = ImageryTransformFactory.getImageryTransformBestFit(ar);
            }
            finally
            {
                myCurrentTransformFinderLock.unlock();
            }
        }
    }

    /**
     * Walks the edge of a tile to determine its min/max lat lon. This is only
     * important when the image is not north up. Otherwise computing min/max is
     * alot easier.
     *
     * @param aTransform the a transform
     * @param aJump - percentage to move each jump around the peremiter
     * @param aBeginX the a begin x
     * @param aEndX the a end x
     * @param aBeginY the a begin y
     * @param aEndY the a end y
     * @return the min max sector transform
     */
    private GeographicBoundingBox getMinMaxSectorTransform(ImageryTransform aTransform, double aJump, int aBeginX, int aEndX,
            int aBeginY, int aEndY)
    {
        MiniSector ms = new MiniSector();
        // checking the perimeter in divisions for the min/max lat lon,
        // important for non-north projection
        double divX = (aEndX - aBeginX) / aJump;
        double divY = (aEndY - aBeginY) / aJump;

        double xIter = aBeginX;
        double yIter = aBeginY;
        for (yIter = aBeginY; yIter <= aEndY - 1.0; yIter += divY)
        {
            // reset X:
            xIter = aBeginX;
            adjustMinMax(aTransform.getLatLonBasedOnTransform(xIter, yIter), ms);

            for (xIter = aBeginX; xIter <= aEndX - 1.0; xIter += divX)
            {
                // probe the interior
                adjustMinMax(aTransform.getLatLonBasedOnTransform(xIter, yIter), ms);
            }
        }

        xIter = aEndX - 1.0;
        for (yIter = aBeginY; yIter <= aEndY - 1.0; yIter += divY)
        {
            adjustMinMax(aTransform.getLatLonBasedOnTransform(xIter, yIter), ms);
        }

        yIter = aBeginY;
        for (xIter = aBeginX; xIter <= aEndX - 1.0; xIter += divX)
        {
            adjustMinMax(aTransform.getLatLonBasedOnTransform(xIter, yIter), ms);
        }

        yIter = aEndY - 1.0;
        for (xIter = aBeginX; xIter <= aEndY - 1.0; xIter += divX)
        {
            adjustMinMax(aTransform.getLatLonBasedOnTransform(xIter, yIter), ms);
        }

        return ms.toSector();
    }

    /**
     * Sample a region of a non-"North Up" image. "North Up" images are much
     * easier to sample. This method finds the rectangular region (NS/EW
     * paralell lines) surrounding the sample region(4 sided, but non NS/EW
     * non-parellel lines) and translates that region to the current transform,
     * which is usually correcting the image to be "North Up".
     *
     * @param gsmHelper the gsm helper
     * @param dataSet the data set
     * @param imageHisto the image histo
     * @param aTransformer the a transformer
     * @return the non north up tile
     */
    private boolean getNonNorthUpTile(GeoSampleModelHelper gsmHelper, Dataset dataSet, HistoInfo imageHisto,
            ImageryTransform aTransformer)
    {
        boolean success = true;

        MiniSector inBounds = gsmHelper.getBounds();

//        LatLonAlt ll = LatLonAlt.createFromDegrees(TWENTY_POINT_NINE, ONEHUNDRED_NINE_POINT_ONE);
        // if (inBounds.toSector().contains(ll))
        // {
        // int i = 2 + 3;
        // i++;
        // }

        Band poBand = dataSet.GetRasterBand(gsmHelper.getCurrentBand() + 1);

        int numberBytesPerPixel = gdal.GetDataTypeSize(poBand.getDataType()) / 8;
        ByteBuffer overSampleData = ByteBuffer.allocateDirect(gsmHelper.getBufSize());
        overSampleData.order(ByteOrder.nativeOrder());

        if (gsmHelper.getImageSector().intersects(gsmHelper.getSampleSector()))
        {
            // find the pixel min max of the sector in the image
            MiniIntSector strongMinMax = findProjectedBounds(dataSet, gsmHelper.getSampleSector(),
                    gsmHelper.getSampleSector().getDeltaLatD() / gsmHelper.getYsize(), aTransformer);

            // adjusting to capture more pixels for roundoff
            strongMinMax.setY1(strongMinMax.getY1() - 1);
            strongMinMax.setX1(strongMinMax.getX1() - 1);
            strongMinMax.setY2(strongMinMax.getY2() + 1);
            strongMinMax.setX2(strongMinMax.getX2() + 1);

            // int overSampleSize = pixels *
            // gdal.GetDataTypeSize(poBand.getDataType()) / 8;
            // //TODO - when sampling a tilted image, should sample higher, up
            // to double, so that the
            // 512x512 sample of a north up image would be the same
            // // maybe sample 1024 x 1024? but that is worst case scenario when
            // a sector is exactly
            // diagonal....
            //
            // ByteBuffer largeSample =
            // ByteBuffer.allocateDirect(overSampleSize);
            // data.order(ByteOrder.nativeOrder());
            strongMinMax.setX1(MathUtil.clamp(strongMinMax.getX1(), 0, dataSet.getRasterXSize()));
            strongMinMax.setX2(MathUtil.clamp(strongMinMax.getX2(), 0, dataSet.getRasterXSize()));

            strongMinMax.setY1(MathUtil.clamp(strongMinMax.getY1(), 0, dataSet.getRasterYSize()));
            strongMinMax.setY2(MathUtil.clamp(strongMinMax.getY2(), 0, dataSet.getRasterYSize()));

            long tempTime = System.currentTimeMillis();

            int returnVal = poBand.ReadRaster_Direct(strongMinMax.getX1(), strongMinMax.getY1(),
                    strongMinMax.getX2() - strongMinMax.getX1(), strongMinMax.getY2() - strongMinMax.getY1(),
                    gsmHelper.getXsize(), gsmHelper.getYsize(), poBand.getDataType(), overSampleData);

//            if (returnVal != gdalconstConstants.CE_None)
//            {
//                double i = 2 * 2.0 + SIX;
//            }

            gsmHelper.setGdalReadTime(gsmHelper.getGdalReadTime() + System.currentTimeMillis() - tempTime);

            tempTime = System.currentTimeMillis();

            double yHeightDivisor = gsmHelper.getYsize() / (double)(strongMinMax.getY2() - strongMinMax.getY1());
            double xHeightDivisor = gsmHelper.getXsize() / (double)(strongMinMax.getX2() - strongMinMax.getX1());

            if (returnVal == gdalconstConstants.CE_None)
            {
                double latDiv = (inBounds.getMaxLat() - inBounds.getMinLat()) / gsmHelper.getYsize();
                double lonDiv = (inBounds.getMaxLon() - inBounds.getMinLon()) / gsmHelper.getXsize();
                int pos = 0;
                int convertedPos = 0;
                // first byte
                int fb = 0;
                // second byte
                int sb = 0;
                // final value
                int converted = 0;
//                final double bandMax = imageHisto.getBand(gsmHelper.currentBand).getMax();
//                final double bandMin = imageHisto.getBand(gsmHelper.currentBand).getMin();
                int skip = 0;
                for (double latIter = inBounds.getMaxLat() - 0.5 * latDiv; latIter > inBounds.getMinLat(); latIter -= latDiv)
                {
                    skip++;
                    for (double lonIter = inBounds.getMinLon() + 0.5 * lonDiv; lonIter <= inBounds.getMaxLon(); lonIter += lonDiv)
                    {
                        // lat =21.955078125 lon =111.462890625 //whiten
                        boolean whiten = false;
                        skip++;
                        // if(latIter < 21.955079 && latIter > 21.955077 &&
                        // lonIter < 111.862891 && lonIter > 111.262890000 &&
                        // (skip % 2 == 0))
                        if (latIter < 22.955079 && latIter > 20.955077 && lonIter < 113.862891 && lonIter > 110.262890000
                                && skip % 2 == 0)
                        {
                            whiten = false;
                        }

                        double x1 = aTransformer.getXPixelBasedOnTransform(lonIter, latIter);
                        double y1 = aTransformer.getYPixelBasedOnTransform(lonIter, latIter);

                        // revisit this on 4th order
                        int y1adjusted = (int)Math.round((y1 - strongMinMax.getY1()) * yHeightDivisor);
                        // XXX removing this -1 for the one pix picture

                        int x1adjusted = (int)Math.round((x1 - strongMinMax.getX1()) * xHeightDivisor);

                        // TODO the next if else might be sped up, the
                        // protection might not be necessary
                        if (x1adjusted >= 0 && x1adjusted < gsmHelper.getXsize() && y1adjusted >= 0
                                && y1adjusted < gsmHelper.getYsize())
                        {
                            boolean oversample = false;
                            if (numberBytesPerPixel == 1)
                            {
                                // gsmHelper.data.put(pos,
                                // overSampleData.get((y1adjusted *
                                // gsmHelper.ysize) + (x1adjusted)));
                                // pos++;
                                if (oversample)
                                {
                                    byte r = 0;
                                    int ri = 0;
                                    double divFac = 0;

                                    for (int sampley = y1adjusted - 1; sampley < y1adjusted + 2; sampley++)
                                    {
                                        for (int samplex = x1adjusted - 1; samplex < x1adjusted + 2; samplex++)
                                        {
                                            if (sampley < 0 || samplex < 0 || samplex >= gsmHelper.getXsize()
                                                    || sampley >= gsmHelper.getYsize())
                                            {
                                                continue;
                                            }
                                            int yoffset = sampley * gsmHelper.getYsize();
                                            int xoffset = samplex;
                                            ri += 0x000000FF & overSampleData.get(yoffset + xoffset);
                                            // 0x000000FF is unsigned byte
                                            // conversion to int
                                            divFac += 1.0;
                                        }
                                    }
                                    int yoffset = y1adjusted * gsmHelper.getYsize();
                                    int xoffset = x1adjusted;
                                    ri += 0x000000FF & overSampleData.get(yoffset + xoffset);

                                    divFac += 1.0;

                                    r = (byte)(int)(ri / divFac);
                                    gsmHelper.getData().put(pos, (byte)(r & 0x000000FF));

                                    pos++;
                                    if (pos % 512 == 0)
                                    {
                                        gsmHelper.getData().put(pos - 1, (byte)255);
                                        gsmHelper.getData().put(pos - 511, (byte)255);
                                    }
                                    if (pos < 512)
                                    {
                                        gsmHelper.getData().put(pos - 1, (byte)255);
                                    }

                                    if (pos > 511 * 512)
                                    {
                                        // TODO optionally do this white tile
                                        // boundary after the fact
                                        gsmHelper.getData().put(pos - 1, (byte)255);
                                    }
                                }
                                else
                                {
                                    byte val = overSampleData.get(y1adjusted * gsmHelper.getYsize() + x1adjusted);
                                    gsmHelper.getData().put(pos, val);
                                    if (whiten)
                                    {
                                        gsmHelper.getData().put(pos, (byte)255);
                                    }

                                    pos++;

                                    if (whiten)
                                    {
                                        if (pos % 512 == 0)
                                        {
                                            gsmHelper.getData().put(pos - 1, (byte)255);
                                            gsmHelper.getData().put(pos - 511, (byte)255);
                                        }
                                        if (pos < 512)
                                        {
                                            gsmHelper.getData().put(pos - 1, (byte)255);
                                        }

                                        if (pos > 511 * 512)
                                        {
                                            // TODO optionally do this white
                                            // tile
                                            // boundary after the fact
                                            gsmHelper.getData().put(pos - 1, (byte)255);
                                        }
                                    }
                                }
                            }
                            else if (numberBytesPerPixel == 2)
                            {
                                int yoffset = y1adjusted * gsmHelper.getYsize() * 2;
                                int xoffset = x1adjusted * 2;

                                if (poBand.getDataType() == gdalconstConstants.GDT_UInt16)
                                {
                                    byte a = overSampleData.get(yoffset + xoffset);
                                    byte b = overSampleData.get(yoffset + xoffset + 1);
                                    fb = 0xFF & a;
                                    sb = 0xFF & b;
                                    converted = sb << 8 | fb;
                                }
                                else
                                {
                                    converted = overSampleData.getShort(yoffset + xoffset);
                                }

                                int[][] convertedIntegers = gsmHelper.getConvertedIntegers();
                                convertedIntegers[gsmHelper.getCurrentBand()][convertedPos] = converted;
                                convertedPos++;

                                pos += 2;
                            }
                            else if (numberBytesPerPixel == 4)
                            {
                                byte r = 0;
                                byte g = 0;
                                byte b = 0;
                                byte a = 0;

                                int ri = 0;
                                int gi = 0;
                                int bi = 0;
                                int ai = 0;

                                for (int sampley = y1adjusted - 1; sampley < y1adjusted + 2; sampley++)
                                {
                                    for (int samplex = x1adjusted - 1; sampley < x1adjusted + 2; samplex++)
                                    {
                                        int yoffset = sampley * gsmHelper.getYsize() * 4;
                                        int xoffset = samplex * 4;
                                        ri += overSampleData.get(yoffset + xoffset);
                                        gi += overSampleData.get(yoffset + xoffset + 1);
                                        bi += overSampleData.get(yoffset + xoffset + 2);
                                        ai += overSampleData.get(yoffset + xoffset + 3);
                                    }
                                }
                                r = (byte)(ri / NINE);
                                g = (byte)(gi / NINE);
                                b = (byte)(bi / NINE);
                                a = (byte)(ai / NINE);
                                gsmHelper.getData().put(pos, r);
                                gsmHelper.getData().put(pos + 1, g);
                                gsmHelper.getData().put(pos + 2, b);
                                gsmHelper.getData().put(pos + 3, a);
                                pos += 4;
                            }
                        }
                        else
                        {
                            for (int i = 0; i < numberBytesPerPixel; i++)
                            {
                                if (pos < gsmHelper.getData().capacity())
                                {
                                    gsmHelper.getData().put(pos, (byte)0);
                                }
                                pos++;
                            }

                            convertedPos++;

                            // if (pos % 512 == 0)
                            // {
                            // data.put(pos - 1, (byte) 255);
                            // data.put(pos - 511, (byte) 255);
                            // }
                            // if (pos < 512)
                            // data.put(pos-1, (byte) 255);
                            // if (pos < data.capacity())
                            // if (pos > 511 * 512)
                            // data.put(pos-1, (byte) 255);
                            // this white tile boundary
                            // after the fact
                        }
                    }
                }
            }
            else
            {
                LOGGER.error("GDAL returned an error.");
                success = false;
            }

            gsmHelper.setTransformTime(gsmHelper.getTransformTime() + System.currentTimeMillis() - tempTime);
        }
        else
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(Thread.currentThread().getId() + " Off north, no intersection "
                        + gsmHelper.getSampleSector().toString() + " prep time " + gsmHelper.getPrepTime() + " total "
                        + (gsmHelper.getBeginTime() - System.currentTimeMillis()));
            }
            success = false;
        }
        return success;
    }

    /**
     * Get the tile from a "North Up" image. Sample the sub region that is NS/EW
     * parallel.
     *
     * @param tm the tm
     * @param dataSet the data set
     * @param imageHisto the image histo
     * @param aTransformer the a transformer
     * @return the north up tile
     */
    private boolean getNorthUpTile(GeoSampleModelHelper tm, Dataset dataSet, HistoInfo imageHisto, ImageryTransform aTransformer)
    {
        double[] gt;
        if (aTransformer.getOrder() == 1)
        {
            gt = aTransformer.getADFTransform();
        }
        else
        {
            return false;
        }

        int returnVal = 0;
        // Bands are not index 0-based, so we must add 1
        Band poBand = dataSet.GetRasterBand(tm.getCurrentBand() + 1);

        if (tm.getImageSector().contains(tm.getSampleSector()))
        {
            int x1 = (int)Math.round((tm.getBounds().getMinLon() - tm.getImageSector().getMinLonD()) / gt[1]);
            int y1 = (int)Math.round((tm.getBounds().getMaxLat() - tm.getImageSector().getMaxLatD()) / gt[5]);
            int x2 = (int)Math.round((tm.getBounds().getMaxLon() - tm.getImageSector().getMinLonD()) / gt[1]);
            int y2 = (int)Math.round((tm.getBounds().getMinLat() - tm.getImageSector().getMaxLatD()) / gt[5]);

            // if(x2> rasterXSize ) x2 = rasterXSize - 1;
            // if(y2> rasterYSize ) y2 = rasterYSize - 1;

            long tempTime = System.currentTimeMillis();
            returnVal = poBand.ReadRaster_Direct(x1, y1, x2 - x1, y2 - y1, tm.getXsize(), tm.getYsize(), poBand.getDataType(),
                    tm.getData());
            tm.setGdalReadTime(tm.getGdalReadTime() + System.currentTimeMillis() - tempTime);
        }
        else if (tm.getImageSector().intersects(tm.getSampleSector()))
        {
            tm.setYsize((int)Math
                    .round(tm.getYSizeOrig() * (tm.getIntersect().getDeltaLatD() / tm.getSampleSector().getDeltaLatD())));
            tm.setXsize((int)Math
                    .round(tm.getXSizeOrig() * (tm.getIntersect().getDeltaLonD() / tm.getSampleSector().getDeltaLonD())));
            tm.setPixels(tm.getXsize() * tm.getYsize());
            tm.setBufSize(tm.getPixels() * gdal.GetDataTypeSize(poBand.getDataType()) / 8);
            tm.setData(ByteBuffer.allocateDirect(tm.getBufSize()));
            tm.getData().order(ByteOrder.nativeOrder());

            // Formulas when dealing with north up tiles:

            // geoX = gt[0] + gt[1] * x + gt[2] * y;
            // geoY = gt[3] + gt[4] * x + gt[5] * y;

            int x1 = (int)Math.round((tm.getIntersect().getMinLonD() - tm.getImageSector().getMinLonD()) / gt[1]);
            int y1 = (int)Math.round((tm.getIntersect().getMaxLatD() - tm.getImageSector().getMaxLatD()) / gt[5]);
            int x2 = (int)Math.round((tm.getIntersect().getMaxLonD() - tm.getImageSector().getMinLonD()) / gt[1]);
            int y2 = (int)Math.round((tm.getIntersect().getMinLatD() - tm.getImageSector().getMaxLatD()) / gt[5]);

            // if(x2> rasterXSize ) x2 = rasterXSize - 1;
            // if(y2> rasterYSize ) y2 = rasterYSize - 1;

            long tempTime = System.currentTimeMillis();
            returnVal = poBand.ReadRaster_Direct(x1, y1, x2 - x1, y2 - y1, tm.getXsize(), tm.getYsize(), poBand.getDataType(),
                    tm.getData());
            tm.setGdalReadTime(tm.getGdalReadTime() + System.currentTimeMillis() - tempTime);

            // TODO could use the below, byte skipping method to not do a copy over
            tm.setCopyOverRequired(true);
        }
        else
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(Thread.currentThread().getId() + " North up, no intersection " + tm.getSampleSector().toString()
                        + (System.currentTimeMillis() - tm.getBeginTime()));
            }
            return false;
        }

        if (returnVal != gdalconstConstants.CE_None)
        {
            // fail
            return false;
        }

        int numberBytesPerPixel = gdal.GetDataTypeSize(poBand.getDataType()) / 8;
        if (numberBytesPerPixel == 2)
        {
            int convertedPos = 0;
            int fb = 0;
            int sb = 0;
            int converted = 0;
//            final double bandMax = imageHisto.getBand(tm.currentBand).getMax();
//            final double bandMin = imageHisto.getBand(tm.currentBand).getMin();
            for (int pos = 0; pos < tm.getData().limit(); pos += 2)
            {
                if (poBand.getDataType() == gdalconstConstants.GDT_UInt16)
                {
                    fb = 0xFF & tm.getData().get(pos);
                    sb = 0xFF & tm.getData().get(pos + 1);
                    converted = sb << 8 | fb;
                }
                else
                {
                    converted = tm.getData().getShort(pos);
                }

                int[][] convertedIntegers = tm.getConvertedIntegers();
                convertedIntegers[tm.getCurrentBand()][convertedPos] = converted;
                convertedPos++;
            }
            tm.setTransformTime(tm.getTransformTime() + System.currentTimeMillis());
        }

        return true;
        // if (numberBytesPerPixel == 4) //TODO
        // {
        // int yoffset = y1adjusted * tm.ysize * 4;
        // int xoffset = x1adjusted * 4;
        // data.put(pos, overSampleData.get(yoffset + xoffset));
        // data.put(pos + 1, overSampleData.get(yoffset + xoffset + 1));
        // data.put(pos + 2, overSampleData.get(yoffset + xoffset + 2));
        // data.put(pos + 3, overSampleData.get(yoffset + xoffset + 3));
        // pos += 4;
        // }
    }

    /**
     * Up front preparation, like finding the adf transform based on GCPs.
     */
    private void prePrepare()
    {
        Dataset dataSet = getDataSet(myFile);

        myRasterXSize = dataSet.getRasterXSize();
        myRasterYSize = dataSet.getRasterYSize();

        Vector<?> groundControlPoints = new Vector<>();
        dataSet.GetGCPs(groundControlPoints);

        Enumeration<?> e = groundControlPoints.elements();

        findTransformAndFavorGCP(e, dataSet);
    }

    /**
     * Retrieve geographical piece.
     *
     * @param inBounds the in bounds
     * @param inXSize the in x size
     * @param inYSize the in y size
     * @param imageHisto the image histo
     * @param aTransformer the a transformer
     * @return the buffered image
     */
    private BufferedImage retrieveGeographicalPiece(MiniSector inBounds, int inXSize, int inYSize, HistoInfo imageHisto,
            ImageryTransform aTransformer)
    {
        Dataset dataSet = getDataSet(myFile);
        GeographicBoundingBox imageSector = getMinMaxSectorTransform(aTransformer, TEN, 0, myRasterXSize, 0, myRasterYSize);
        GeoSampleModelHelper gsmHelper = new GeoSampleModelHelper(imageSector, Math.min(dataSet.getRasterCount(), 4), inXSize,
                inYSize, inBounds, dataSet);

        BufferedImage img = null;

        try
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(Thread.currentThread().getId() + " retrieveGeographicalPiece sample sector "
                        + gsmHelper.getSampleSector().toString());
            }

            Band poBand = null;
            gsmHelper.setPrepTime(System.currentTimeMillis());

            for (int band = 0; band < gsmHelper.getBandCount(); band++)
            {
                // Bands are not index 0-based, so we must add 1
                poBand = dataSet.GetRasterBand(band + 1);

                int numberBytesPerPixel = gdal.GetDataTypeSize(poBand.getDataType()) / 8;

                if (numberBytesPerPixel == 2)
                {
                    if (gsmHelper.getConvertedIntegers() == null)
                    {
                        gsmHelper.setConvertedIntegers(new int[gsmHelper.getBandCount()][]);
                    }
                    int[][] convertedIntegers = gsmHelper.getConvertedIntegers();
                    convertedIntegers[band] = new int[gsmHelper.getPixels()];
                }
                else if (numberBytesPerPixel == 4)
                {
                    if (gsmHelper.getConvertedLongs() == null)
                    {
                        gsmHelper.setConvertedLongs(new long[gsmHelper.getBandCount()][]);
                    }
                    long[][] convertedLongs = gsmHelper.getConvertedLongs();
                    convertedLongs[band] = new long[gsmHelper.getPixels()];
                }

                gsmHelper.setBufSize(gsmHelper.getPixels() * gdal.GetDataTypeSize(poBand.getDataType()) / 8);

                gsmHelper.setData(ByteBuffer.allocateDirect(gsmHelper.getBufSize()));
                gsmHelper.getData().order(ByteOrder.nativeOrder());
                gsmHelper.setCurrentBand(band);

                // int returnVal = 0;
                boolean northup;
                if (aTransformer.getOrder() == 1)
                {
                    double[] gt = aTransformer.getADFTransform();

                    northup = gt[2] == 0.0 && gt[4] == 0.0;
                }
                else
                {
                    northup = false;
                }

                if (northup ? !getNorthUpTile(gsmHelper, dataSet, imageHisto, aTransformer)
                        : !getNonNorthUpTile(gsmHelper, dataSet, imageHisto, aTransformer))
                {
                    return null;
                }

                ByteBuffer[] bands = gsmHelper.getBands();
                bands[gsmHelper.getCurrentBand()] = gsmHelper.getData();
                int[] banks = gsmHelper.getBanks();
                banks[gsmHelper.getCurrentBand()] = gsmHelper.getCurrentBand();
                int[] offsets = gsmHelper.getOffsets();
                offsets[gsmHelper.getCurrentBand()] = 0;
            }

            if (poBand == null)
            {
                return null;
            }

            @SuppressWarnings("PMD.PrematureDeclaration")
            long beginPostProcess = System.currentTimeMillis();

            DataBuffer imgBuffer = null;
            SampleModel sampleModel = null;
            int dataType = 0;
            int bufferType = 0;

            // These are the possible types:
            // (TODO implement all types)
            // public static final int GDT_Byte = gdalconstJNI.GDT_Byte_get();
            // public static final int GDT_UInt16 =
            // gdalconstJNI.GDT_UInt16_get();
            // public static final int GDT_Int16 = gdalconstJNI.GDT_Int16_get();
            // public static final int GDT_UInt32 =
            // gdalconstJNI.GDT_UInt32_get();
            // public static final int GDT_Int32 = gdalconstJNI.GDT_Int32_get();
            // public static final int GDT_Float32 =
            // gdalconstJNI.GDT_Float32_get();
            // public static final int GDT_Float64 =
            // gdalconstJNI.GDT_Float64_get();
            // public static final int GDT_CInt16 =
            // gdalconstJNI.GDT_CInt16_get();
            // public static final int GDT_CInt32 =
            // gdalconstJNI.GDT_CInt32_get();
            // public static final int GDT_CFloat32 =
            // gdalconstJNI.GDT_CFloat32_get();
            // public static final int GDT_CFloat64 =
            // gdalconstJNI.GDT_CFloat64_get();

            // TODO poBand is referenced in a manner not in line with what band
            // we are on at the moment
            if (poBand.getDataType() == gdalconstConstants.GDT_Byte)
            {
                byte[][] bytes = new byte[gsmHelper.getBandCount()][];
                for (int i = 0; i < gsmHelper.getBandCount(); i++)
                {
                    bytes[i] = new byte[gsmHelper.getPixels()];
                    gsmHelper.getBands()[i].get(bytes[i]);
                }
                imgBuffer = new DataBufferByte(bytes, gsmHelper.getPixels());
                bufferType = DataBuffer.TYPE_BYTE;
                sampleModel = new BandedSampleModel(bufferType, gsmHelper.getXsize(), gsmHelper.getYsize(), gsmHelper.getXsize(),
                        gsmHelper.getBanks(), gsmHelper.getOffsets());
                dataType = poBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex
                        ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_GRAY;
            }
            else if (poBand.getDataType() == gdalconstConstants.GDT_Int16)
            {
                byte[][] bytes = new byte[gsmHelper.getBandCount()][];

                // short[][] shorts = new short[bandCount][];

                for (int i = 0; i < gsmHelper.getBandCount(); i++)
                {
                    final double bandMax = imageHisto.getBand(i).getMax();
                    final double bandMin = imageHisto.getBand(i).getMin();
                    double range = bandMax - bandMin;
                    // double range = ourImageMaximumsPerBand[i] -
                    // ourImageMinimumsPerBand[i];
                    bytes[i] = new byte[gsmHelper.getPixels()];
                    for (int t = 0; t < gsmHelper.getPixels(); t++)
                    {
                        int converted = gsmHelper.getConvertedIntegers()[i][t];
                        if (converted < bandMin)
                        {
                            converted = (int)bandMin;
                        }
                        int ranged = (int)((converted - bandMin) / range * TWO_FIFTY_FIVE);

                        if (myBrightening != 1.0)
                        {
                            ranged = MathUtil.clamp((int)(ranged * myBrightening), 0, 255);
                        }

                        if (ranged > 255)
                        {
                            ranged = 255;
                        }
                        bytes[i][t] = (byte)ranged;
                    }
                }
                imgBuffer = new DataBufferByte(bytes, gsmHelper.getPixels());
                bufferType = DataBuffer.TYPE_BYTE;
                sampleModel = new BandedSampleModel(bufferType, gsmHelper.getXsize(), gsmHelper.getYsize(), gsmHelper.getXsize(),
                        gsmHelper.getBanks(), gsmHelper.getOffsets());
                dataType = poBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex
                        ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_GRAY;
            }
            else if (poBand.getDataType() == gdalconstConstants.GDT_UInt16)
            {
                byte[][] bytes = new byte[gsmHelper.getBandCount()][];

                // short[][] shorts = new short[bandCount][];

                for (int i = 0; i < gsmHelper.getBandCount(); i++)
                {
                    final double bandMax = imageHisto.getBand(i).getMax();
                    final double bandMin = imageHisto.getBand(i).getMin();
                    // Don't want to map the actual minimums to 0 below, since
                    // it will make sections of
                    // real data transparent if "ignore zeros" has been set.
                    final double actualBottomOfRange = Math.max(0, bandMin - 1);
                    double range = bandMax - bandMin;
                    bytes[i] = new byte[gsmHelper.getPixels()];
                    for (int t = 0; t < gsmHelper.getPixels(); t++)
                    {
                        int converted = gsmHelper.getConvertedIntegers()[i][t];
                        if (converted < bandMin)
                        {
                            converted = (int)bandMin;
                        }

                        int ranged = (int)((converted - actualBottomOfRange) / range * TWO_FIFTY_FIVE);

                        if (myBrightening != 1.0)
                        {
                            ranged = MathUtil.clamp(0, 255, (int)(ranged * myBrightening));
                        }

                        if (ranged > 255)
                        {
                            ranged = 255;
                        }
                        bytes[i][t] = (byte)ranged;
                    }
                }
                imgBuffer = new DataBufferByte(bytes, gsmHelper.getPixels());
                bufferType = DataBuffer.TYPE_BYTE;
                sampleModel = new BandedSampleModel(bufferType, gsmHelper.getXsize(), gsmHelper.getYsize(), gsmHelper.getXsize(),
                        gsmHelper.getBanks(), gsmHelper.getOffsets());
                dataType = poBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex
                        ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_GRAY;
            }
            else if (poBand.getDataType() == gdalconstConstants.GDT_Int32)
            {
                int[][] ints = new int[gsmHelper.getBandCount()][];
                for (int i = 0; i < gsmHelper.getBandCount(); i++)
                {
                    ints[i] = new int[gsmHelper.getPixels()];
                    gsmHelper.getBands()[i].asIntBuffer().get(ints[i]);
                }
                imgBuffer = new DataBufferInt(ints, gsmHelper.getPixels());
                bufferType = DataBuffer.TYPE_INT;
                sampleModel = new BandedSampleModel(bufferType, gsmHelper.getXsize(), gsmHelper.getYsize(), gsmHelper.getXsize(),
                        gsmHelper.getBanks(), gsmHelper.getOffsets());
                dataType = BufferedImage.TYPE_CUSTOM;
            }
            else if (poBand.getDataType() == gdalconstConstants.GDT_UInt32)
            {
                int[][] ints = new int[gsmHelper.getBandCount()][];
                for (int i = 0; i < gsmHelper.getBandCount(); i++)
                {
                    ints[i] = new int[gsmHelper.getPixels()];
                    gsmHelper.getBands()[i].asIntBuffer().get(ints[i]);
                }
                imgBuffer = new DataBufferInt(ints, gsmHelper.getPixels());
                bufferType = DataBuffer.TYPE_INT;
                sampleModel = new BandedSampleModel(bufferType, gsmHelper.getXsize(), gsmHelper.getYsize(), gsmHelper.getXsize(),
                        gsmHelper.getBanks(), gsmHelper.getOffsets());
                dataType = BufferedImage.TYPE_CUSTOM;
            }
            else if (poBand.getDataType() == gdalconstConstants.GDT_Float32)
            {
                byte[][] bytes = new byte[gsmHelper.getBandCount()][];
                float[][] floats = new float[gsmHelper.getBandCount()][];
                for (int i = 0; i < gsmHelper.getBandCount(); i++)
                {
                    bytes[i] = new byte[gsmHelper.getPixels()];
                    floats[i] = new float[gsmHelper.getPixels()];
                    gsmHelper.getBands()[i].asFloatBuffer().get(floats[i]);

                    final double bandMax = imageHisto.getBand(i).getMax();
                    final double bandMin = imageHisto.getBand(i).getMin();
                    double range = bandMax - bandMin;

                    bytes[i] = new byte[gsmHelper.getPixels()];
                    for (int j = 0; j < gsmHelper.getPixels(); j++)
                    {
                        int ranged = (int)((floats[i][j] - bandMin) / range * TWO_FIFTY_FIVE);

                        if (myBrightening != 1.0)
                        {
                            ranged = MathUtil.clamp(0, 255, (int)(ranged * myBrightening));
                        }

                        if (ranged > 255)
                        {
                            ranged = 255;
                        }
                        bytes[i][j] = (byte)ranged;
                    }
                }

                imgBuffer = new DataBufferByte(bytes, gsmHelper.getPixels());
                bufferType = DataBuffer.TYPE_BYTE;
                sampleModel = new BandedSampleModel(bufferType, gsmHelper.getXsize(), gsmHelper.getYsize(), gsmHelper.getXsize(),
                        gsmHelper.getBanks(), gsmHelper.getOffsets());
                dataType = poBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex
                        ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_GRAY;
            }
            else if (poBand.getDataType() == gdalconstConstants.GDT_Float64)
            {
                byte[][] bytes = new byte[gsmHelper.getBandCount()][];
                double[][] doubles = new double[gsmHelper.getBandCount()][];
                for (int i = 0; i < gsmHelper.getBandCount(); i++)
                {
                    bytes[i] = new byte[gsmHelper.getPixels()];
                    doubles[i] = new double[gsmHelper.getPixels()];
                    gsmHelper.getBands()[i].asDoubleBuffer().get(doubles[i]);

                    final double bandMax = imageHisto.getBand(i).getMax();
                    final double bandMin = imageHisto.getBand(i).getMin();
                    double range = bandMax - bandMin;

                    bytes[i] = new byte[gsmHelper.getPixels()];
                    for (int j = 0; j < gsmHelper.getPixels(); j++)
                    {
                        int ranged = (int)((doubles[i][j] - bandMin) / range * TWO_FIFTY_FIVE);

                        if (myBrightening != 1.0)
                        {
                            ranged = MathUtil.clamp(0, 255, (int)(ranged * myBrightening));
                        }

                        if (ranged > 255)
                        {
                            ranged = 255;
                        }
                        bytes[i][j] = (byte)ranged;
                    }
                }

                imgBuffer = new DataBufferByte(bytes, gsmHelper.getPixels());
                bufferType = DataBuffer.TYPE_BYTE;
                sampleModel = new BandedSampleModel(bufferType, gsmHelper.getXsize(), gsmHelper.getYsize(), gsmHelper.getXsize(),
                        gsmHelper.getBanks(), gsmHelper.getOffsets());
                dataType = poBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex
                        ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_GRAY;
            }
            else
            {
                LOGGER.error(Thread.currentThread().getId() + " Unsupported poBand.getDataType(): " + poBand.getDataType());
                return null;
            }

            img = createImageFromRaster(gsmHelper, poBand, imgBuffer, sampleModel, dataType, bufferType);

            gsmHelper.setPostProcess(System.currentTimeMillis());

            img = transparentCopyOverIfNecessary(inBounds, gsmHelper, img);

            long finalTime = System.currentTimeMillis();
            finalTime = finalTime - gsmHelper.getBeginTime();

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(Thread.currentThread().getId() + " Just copyOver " + (finalTime - gsmHelper.getPostProcess()));

                gsmHelper.setPrepTime(gsmHelper.getPrepTime() - gsmHelper.getBeginTime());

                gsmHelper.setPostProcess(gsmHelper.getPostProcess() - beginPostProcess);

                LOGGER.trace(Thread.currentThread().getId() + " (from beginning) prepTime " + gsmHelper.getPrepTime()
                        + " gdalReadTime " + gsmHelper.getGdalReadTime() + " transformTime " + gsmHelper.getTransformTime()
                        + " postProcess " + gsmHelper.getPostProcess() + " final " + finalTime);

                gsmHelper.setPostProcess(gsmHelper.getPostProcess() - gsmHelper.getTransformTime() - gsmHelper.getGdalReadTime()
                        - gsmHelper.getPrepTime());

                LOGGER.trace(Thread.currentThread().getId() + " (step) prepTime " + gsmHelper.getPrepTime() + " gdalReadTime "
                        + gsmHelper.getGdalReadTime() + " transformTime " + gsmHelper.getTransformTime() + " postProcess "
                        + gsmHelper.getPostProcess());

                LOGGER.trace(Thread.currentThread().getId() + " Time To Load GDAL image (millis)"
                        + (System.currentTimeMillis() - gsmHelper.getBeginTime()));
            }
        }
        catch (RuntimeException ex)
        {
            LOGGER.warn(Thread.currentThread().getId() + " GDALTools error", ex);
            return null;
        }
        finally
        {
            gsmHelper.destroy();
        }

        return img;
    }

    /**
     * Helper method to make an image with an alpha layer and copy over the
     * portion of the tile needed into the new image.
     *
     * @param inBounds the in bounds
     * @param gsmHelper the gsm helper
     * @param pImg the img
     * @return the buffered image
     */
    private BufferedImage transparentCopyOverIfNecessary(MiniSector inBounds, GeoSampleModelHelper gsmHelper, BufferedImage pImg)
    {
        BufferedImage img = pImg;
        if (!gsmHelper.isCopyOverRequired())
        {
            return img;
        }

        BufferedImage sampleTarget = createTransparentImage(gsmHelper.getxSizeOrig(), gsmHelper.getySizeOrig());
        final int startX = (int)Math.round(gsmHelper.getxSizeOrig()
                * ((gsmHelper.getIntersect().getMinLonD() - inBounds.getMinLon()) / gsmHelper.getSampleSector().getDeltaLonD()));
        final int startY = (int)Math.round(gsmHelper.getySizeOrig()
                * ((inBounds.getMaxLat() - gsmHelper.getIntersect().getMaxLatD()) / gsmHelper.getSampleSector().getDeltaLatD()));

        int subY = 0;
        int subX = 0;
        for (int y = startY; y < startY + gsmHelper.getYsize(); y++)
        {
            subX = 0;
            for (int x = startX; x < startX + gsmHelper.getXsize(); x++)
            {
                try
                {
                    sampleTarget.setRGB(x, y, img.getRGB(subX, subY));
                }
                catch (RuntimeException e)
                {
                    LOGGER.warn(e);
                }

                subX++;
            }
            subY++;
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(Thread.currentThread().getId() + " With copy over/transparency time To Load GDAL image (millis)"
                    + (System.currentTimeMillis() - gsmHelper.getBeginTime()));
        }
        img = sampleTarget;
        return img;
    }
}
