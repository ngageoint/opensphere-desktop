package io.opensphere.imagery.gdal;

import java.nio.ByteBuffer;

import org.gdal.gdal.Dataset;

import io.opensphere.core.model.GeographicBoundingBox;

/**
 * The Class GeoSampleModelHelper.
 */
public class GeoSampleModelHelper
{
    /** The band count. */
    private final int myBandCount;

    /** The bands. */
    private ByteBuffer[] myBands;

    /** The banks. */
    private int[] myBanks;

    /** The begin time. */
    private final long myBeginTime = System.currentTimeMillis();

    /** The bounds. */
    private MiniSector myBounds;

    /** The buf_size. */
    private int myBufSize;

    /** The converted integers. */
    private int[][] myConvertedIntegers;

    /** The converted longs. */
    private long[][] myConvertedLongs;

    /**
     * partial intersection with sampled images result in an additional copy.
     */
    private boolean myCopyOverRequired;

    /** The current band. */
    private int myCurrentBand;

    /** The data. */
    private ByteBuffer myData;

    /** The Data set. */
    private Dataset myDataSet;

    /** The gdal read time. */
    private long myGdalReadTime;

    /** The image sector. */
    private GeographicBoundingBox myImageSector;

    /** The intersect. */
    private GeographicBoundingBox myIntersect;

    /** The offsets. */
    private int[] myOffsets;

    /** The pixels. */
    private int myPixels;

    /** The post process. */
    private long myPostProcess;

    /** The prep time. */
    private long myPrepTime;

    /** The sample sector. */
    private GeographicBoundingBox mySmpleSector;

    /** The transform time. */
    private long myTransformTime;

    /** The xsize. */
    private int myXsize;

    /** The x size orig. */
    private final int myXSizeOrig;

    /** The ysize. */
    private int myYsize;

    /** The y size orig. */
    private final int myYSizeOrig;

    /**
     * Instantiates a new geo sample model helper.
     *
     * @param sector the sector
     * @param numBands the num bands
     * @param xLength the x length
     * @param yLength the y length
     * @param inBounds the in bounds
     * @param aDataset the a dataset
     */
    GeoSampleModelHelper(GeographicBoundingBox sector, int numBands, int xLength, int yLength, MiniSector inBounds,
            Dataset aDataset)
    {
        myBounds = inBounds;
        mySmpleSector = myBounds.toSector();
        myDataSet = aDataset;
        myImageSector = sector;
        myBandCount = numBands;
        myBands = new ByteBuffer[myBandCount];
        myBanks = new int[myBandCount];
        myOffsets = new int[myBandCount];
        myXsize = xLength;
        myYsize = yLength;
        myXSizeOrig = myXsize;
        myYSizeOrig = myYsize;
        myPixels = myXsize * myYsize;
        myIntersect = (GeographicBoundingBox)myImageSector.intersection(mySmpleSector);
    }

    /**
     * Destroy.
     */
    public void destroy()
    {
        myBands = null;
        myBanks = null;
        myOffsets = null;
        myConvertedIntegers = null;
        myConvertedLongs = null;
        myDataSet = null;
        myBounds = null;
        mySmpleSector = null;
        myImageSector = null;
        myIntersect = null;
        myData = null;
    }

    /**
     * Gets the band count.
     *
     * @return the band count
     */
    public final int getBandCount()
    {
        return myBandCount;
    }

    /**
     * Gets the begin time.
     *
     * @return the begin time
     */
    public final long getBeginTime()
    {
        return myBeginTime;
    }

    /**
     * Gets the bounds.
     *
     * @return the bounds
     */
    public final MiniSector getBounds()
    {
        return myBounds;
    }

    /**
     * Gets the buf_size.
     *
     * @return the buf_size
     */
    public final int getBufSize()
    {
        return myBufSize;
    }

    /**
     * Gets the current band.
     *
     * @return the current band
     */
    public final int getCurrentBand()
    {
        return myCurrentBand;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public final ByteBuffer getData()
    {
        return myData;
    }

    /**
     * Gets the data set.
     *
     * @return the data set
     */
    public final Dataset getDataSet()
    {
        return myDataSet;
    }

    /**
     * Gets the gdal read time.
     *
     * @return the gdal read time
     */
    public final long getGdalReadTime()
    {
        return myGdalReadTime;
    }

    /**
     * Gets the image sector.
     *
     * @return the image sector
     */
    public final GeographicBoundingBox getImageSector()
    {
        return myImageSector;
    }

    /**
     * Gets the intersect.
     *
     * @return the intersect
     */
    public final GeographicBoundingBox getIntersect()
    {
        return myIntersect;
    }

    /**
     * Gets the pixels.
     *
     * @return the pixels
     */
    public final int getPixels()
    {
        return myPixels;
    }

    /**
     * Gets the post process.
     *
     * @return the post process
     */
    public final long getPostProcess()
    {
        return myPostProcess;
    }

    /**
     * Gets the prep time.
     *
     * @return the prep time
     */
    public final long getPrepTime()
    {
        return myPrepTime;
    }

    /**
     * Gets the sample sector.
     *
     * @return the sample sector
     */
    public final GeographicBoundingBox getSampleSector()
    {
        return mySmpleSector;
    }

    /**
     * Gets the smple sector.
     *
     * @return the smple sector
     */
    public final GeographicBoundingBox getSmpleSector()
    {
        return mySmpleSector;
    }

    /**
     * Gets the transform time.
     *
     * @return the transform time
     */
    public final long getTransformTime()
    {
        return myTransformTime;
    }

    /**
     * Gets the xsize.
     *
     * @return the xsize
     */
    public final int getXsize()
    {
        return myXsize;
    }

    /**
     * Gets the x size orig.
     *
     * @return the x size orig
     */
    public final int getxSizeOrig()
    {
        return myXSizeOrig;
    }

    /**
     * Gets the x size orig.
     *
     * @return the x size orig
     */
    public final int getXSizeOrig()
    {
        return myXSizeOrig;
    }

    /**
     * Gets the ysize.
     *
     * @return the ysize
     */
    public final int getYsize()
    {
        return myYsize;
    }

    /**
     * Gets the y size orig.
     *
     * @return the y size orig
     */
    public final int getySizeOrig()
    {
        return myYSizeOrig;
    }

    /**
     * Gets the y size orig.
     *
     * @return the y size orig
     */
    public final int getYSizeOrig()
    {
        return myYSizeOrig;
    }

    /**
     * Checks if is copy over required.
     *
     * @return true, if is copy over required
     */
    public final boolean isCopyOverRequired()
    {
        return myCopyOverRequired;
    }

    /**
     * Sets the bounds.
     *
     * @param bounds the new bounds
     */
    public final void setBounds(MiniSector bounds)
    {
        myBounds = bounds;
    }

    /**
     * Sets the buf size.
     *
     * @param bufSize the new buf size
     */
    public final void setBufSize(int bufSize)
    {
        myBufSize = bufSize;
    }

    /**
     * Sets the copy over required.
     *
     * @param copyOverRequired the new copy over required
     */
    public final void setCopyOverRequired(boolean copyOverRequired)
    {
        myCopyOverRequired = copyOverRequired;
    }

    /**
     * Sets the current band.
     *
     * @param currentBand the new current band
     */
    public final void setCurrentBand(int currentBand)
    {
        myCurrentBand = currentBand;
    }

    /**
     * Sets the data.
     *
     * @param data the new data
     */
    public final void setData(ByteBuffer data)
    {
        myData = data;
    }

    /**
     * Sets the data set.
     *
     * @param dataSet the new data set
     */
    public final void setDataSet(Dataset dataSet)
    {
        myDataSet = dataSet;
    }

    /**
     * Sets the gdal read time.
     *
     * @param gdalReadTime the new gdal read time
     */
    public final void setGdalReadTime(long gdalReadTime)
    {
        myGdalReadTime = gdalReadTime;
    }

    /**
     * Sets the image sector.
     *
     * @param imageSector the new image sector
     */
    public final void setImageSector(GeographicBoundingBox imageSector)
    {
        myImageSector = imageSector;
    }

    /**
     * Sets the intersect.
     *
     * @param intersect the new intersect
     */
    public final void setIntersect(GeographicBoundingBox intersect)
    {
        myIntersect = intersect;
    }

    /**
     * Sets the pixels.
     *
     * @param i the new pixels
     */
    public void setPixels(int i)
    {
        myPixels = i;
    }

    /**
     * Sets the post process.
     *
     * @param postProcess the new post process
     */
    public final void setPostProcess(long postProcess)
    {
        myPostProcess = postProcess;
    }

    /**
     * Sets the prep time.
     *
     * @param prepTime the new prep time
     */
    public final void setPrepTime(long prepTime)
    {
        myPrepTime = prepTime;
    }

    /**
     * Sets the smple sector.
     *
     * @param smpleSector the new smple sector
     */
    public final void setSmpleSector(GeographicBoundingBox smpleSector)
    {
        mySmpleSector = smpleSector;
    }

    /**
     * Sets the transform time.
     *
     * @param transformTime the new transform time
     */
    public final void setTransformTime(long transformTime)
    {
        myTransformTime = transformTime;
    }

    /**
     * Sets the xsize.
     *
     * @param xsize the new xsize
     */
    public final void setXsize(int xsize)
    {
        myXsize = xsize;
    }

    /**
     * Sets the ysize.
     *
     * @param ysize the new ysize
     */
    public final void setYsize(int ysize)
    {
        myYsize = ysize;
    }

    /**
     * Gets the bands.
     *
     * @return the bands
     */
    protected final ByteBuffer[] getBands()
    {
        return myBands;
    }

    /**
     * Gets the banks.
     *
     * @return the banks
     */
    protected final int[] getBanks()
    {
        return myBanks;
    }

    /**
     * Gets the converted integers.
     *
     * @return the converted integers
     */
    protected final int[][] getConvertedIntegers()
    {
        return myConvertedIntegers;
    }

    /**
     * Gets the converted longs.
     *
     * @return the converted longs
     */
    protected final long[][] getConvertedLongs()
    {
        return myConvertedLongs;
    }

    /**
     * Gets the offsets.
     *
     * @return the offsets
     */
    protected final int[] getOffsets()
    {
        return myOffsets;
    }

    /**
     * Sets the bands.
     *
     * @param bands the new bands
     */
    protected final void setBands(ByteBuffer[] bands)
    {
        myBands = bands;
    }

    /**
     * Sets the banks.
     *
     * @param banks the new banks
     */
    protected final void setBanks(int[] banks)
    {
        myBanks = banks;
    }

    /**
     * Sets the converted integers.
     *
     * @param convertedIntegers the new converted integers
     */
    protected final void setConvertedIntegers(int[][] convertedIntegers)
    {
        myConvertedIntegers = convertedIntegers;
    }

    /**
     * Sets the converted longs.
     *
     * @param convertedLongs the new converted longs
     */
    protected final void setConvertedLongs(long[][] convertedLongs)
    {
        myConvertedLongs = convertedLongs;
    }

    /**
     * Sets the offsets.
     *
     * @param offsets the new offsets
     */
    protected final void setOffsets(int[] offsets)
    {
        myOffsets = offsets;
    }
}
