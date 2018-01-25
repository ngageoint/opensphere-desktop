package io.opensphere.imagery.gdal;

import org.apache.log4j.Logger;

import io.opensphere.core.model.GeographicBoundingBox;

/**
 * A class to pass data derived by the GDALTools class to summarize a data set
 * into world wind terminology. numLevels and zeroLevelTileSize being the most
 * important.
 */
public class GDALImageLayerData
{
    /** The Constant DEFAULT_LARGE_ADVANCED_IMAGE_FILE_SIZE_NERF. */
    private static final int DEFAULT_LARGE_ADVANCED_IMAGE_FILE_SIZE_NERF = 1200000000;

    /** The Constant LARGE_ADVANCED_IMAGE_FILE_SIZE_ALTITUDE. */
    private static final int LARGE_ADVANCED_IMAGE_FILE_SIZE_ALTITUDE = 15000;

    /** The Constant ourLogger. */
    private static final Logger LOGGER = Logger.getLogger(GDALImageLayerData.class);

    /** The Constant ONE_HUNDREDTH. */
    private static final double ONE_HUNDREDTH = 0.01;

    /** The Constant ONE_POINT_FOUR. */
    private static final double ONE_POINT_FOUR = 1.4;

    /** The Constant ONE_POINT_SEVEN. */
    private static final double ONE_POINT_SEVEN = 1.7;

    /** The Constant ONE_QUARTER. */
    private static final double ONE_QUARTER = 0.25;

    /** The num levels. */
    private int myNumLevels = 5;

    /** The sector surrounding the image. */
    private GeographicBoundingBox mySector;

    /** The split scale. */
    private double mySplitScale = ONE_POINT_SEVEN;

    /** The start altitude. */
    private double myStartAltitude = 100000;

    /** The x width. */
    private int myXWidth;

    /** The y width. */
    private int myYWidth;

    /** The zero level tile size. */
    private double myZeroLevelTileSize = ONE_HUNDREDTH;

    /**
     * Helps determine the settings for how many levels in the tiling structure,
     * start altitude, zero level tile size, etc.
     *
     * @param imageSize the image size
     * @param fileSize the file size
     */
    public void determineSettings(int imageSize, long fileSize)
    {
        // TODO: with large images, start with many more tiles at the initial
        // level
        // the current algorithm is fine for < 400 megabyte files. For 2.7 gig
        // images, the 0th layer
        // should have more than 30 tiles across the top level
        // x tiles across the image at lowest level
        int xTiles = (int)Math.ceil((double)myXWidth / (double)imageSize);
        // y tiles across the image at lowest level
        int yTiles = (int)Math.ceil((double)myYWidth / (double)imageSize);
        int max = xTiles > yTiles ? xTiles : yTiles;

        int levels = 1;

        double tempMax = max;

        double latWidth = mySector.getDeltaLatD();
        double lonWidth = mySector.getDeltaLonD();

        double divisionFactor = ONE_QUARTER;
        while (tempMax > divisionFactor)
        {
            levels++;
            tempMax = tempMax / 2.0;
        }

        latWidth = latWidth / divisionFactor;
        lonWidth = lonWidth / divisionFactor;

        if (latWidth > lonWidth)
        {
            myStartAltitude += myStartAltitude * latWidth * latWidth * 2.0;
        }
        else
        {
            myStartAltitude += myStartAltitude * lonWidth * lonWidth * 2.0;
        }

        if (fileSize > DEFAULT_LARGE_ADVANCED_IMAGE_FILE_SIZE_NERF)
        {
            myStartAltitude = LARGE_ADVANCED_IMAGE_FILE_SIZE_ALTITUDE;
        }

        myNumLevels = levels + 1;

        if (myXWidth + myYWidth < 2048)
        {
            myNumLevels = levels + 2;
            mySplitScale = ONE_POINT_FOUR;
        }

        myZeroLevelTileSize = latWidth < lonWidth ? latWidth : lonWidth;
    }

    /**
     * Gets the num levels.
     *
     * @return the num levels
     */
    public int getNumLevels()
    {
        return myNumLevels;
    }

    /**
     * Gets the sector.
     *
     * @return the sector
     */
    public GeographicBoundingBox getSector()
    {
        return mySector;
    }

    /**
     * Gets the split scale.
     *
     * @return the split scale
     */
    public double getSplitScale()
    {
        return mySplitScale;
    }

    /**
     * Gets the start altitude.
     *
     * @return the start altitude
     */
    public double getStartAltitude()
    {
        return myStartAltitude;
    }

    /**
     * Gets the x width.
     *
     * @return the x width
     */
    public int getXWidth()
    {
        return myXWidth;
    }

    /**
     * Gets the y width.
     *
     * @return the y width
     */
    public int getYWidth()
    {
        return myYWidth;
    }

    /**
     * Gets the zero level tile size.
     *
     * @return the zero level tile size
     */
    public double getZeroLevelTileSize()
    {
        return myZeroLevelTileSize;
    }

    /**
     * Prints the.
     */
    public void print()
    {
        {
            LOGGER.warn("Sector " + mySector.toString());
            LOGGER.warn("Levels " + myNumLevels);
            LOGGER.warn("X Width " + myXWidth);
            LOGGER.warn("Y Width " + myYWidth);
            LOGGER.warn("SplitScale " + mySplitScale);
            LOGGER.warn("ZeroLevelTileSize " + myZeroLevelTileSize);
        }
    }

    /**
     * Sets the num levels.
     *
     * @param numLevels the new num levels
     */
    public void setNumLevels(int numLevels)
    {
        myNumLevels = numLevels;
    }

    /**
     * Sets the sector.
     *
     * @param sector the new sector
     */
    public void setSector(GeographicBoundingBox sector)
    {
        mySector = sector;
    }

    /**
     * Sets the split scale.
     *
     * @param splitScale the new split scale
     */
    public void setSplitScale(double splitScale)
    {
        mySplitScale = splitScale;
    }

    /**
     * Sets the start altitude.
     *
     * @param startAltitude the new start altitude
     */
    public void setStartAltitude(double startAltitude)
    {
        myStartAltitude = startAltitude;
    }

    /**
     * Sets the x width.
     *
     * @param width the new x width
     */
    public void setXWidth(int width)
    {
        myXWidth = width;
    }

    /**
     * Sets the y width.
     *
     * @param width the new y width
     */
    public void setYWidth(int width)
    {
        myYWidth = width;
    }

    /**
     * Sets the zero level tile size.
     *
     * @param zeroLevelTileSize the new zero level tile size
     */
    public void setZeroLevelTileSize(double zeroLevelTileSize)
    {
        myZeroLevelTileSize = zeroLevelTileSize;
    }
}
