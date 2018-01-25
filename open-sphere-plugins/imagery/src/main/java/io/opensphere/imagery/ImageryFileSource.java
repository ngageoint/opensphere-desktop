package io.opensphere.imagery;

import java.io.File;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.SingleFileDataSource;
import io.opensphere.mantle.datasources.impl.AbstractDataSource;

/**
 * Configuration of a single image file source.
 */
@XmlRootElement(name = "ImageryFileSource")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.GodClass")
public class ImageryFileSource extends AbstractDataSource implements SingleFileDataSource
{
    /** The Constant DEFAULT_TILE_SIZE. */
    public static final int DEFAULT_TILE_SIZE = 512;

    /** The Constant IMAGETYPE. */
    public static final String IMAGE = "ImageryFileSource";

    /** The Constant SOURCE_OFFSET_CHANGED. */
    public static final String SOURCE_OFFSET_CHANGED = "SOURCE_OFFSET_CHANGED";

    /** Logger. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(ImageryFileSource.class);

    /** The bands. */
    @XmlAttribute(name = "bands", required = false)
    private int myBands;

    /** The create overviews. */
    @XmlTransient
    private boolean myCreateOverviews;

    /** The my data type info. */
    @XmlTransient
    private DataTypeInfo myDataTypeInfo;

    /** The datum. */
    @XmlAttribute(name = "datum", required = false)
    private String myDatum = "";

    /** The description. */
    @XmlElement(name = "description", required = true)
    private String myDescription = "";

    /** The enabled. */
    @XmlAttribute(name = "enabled", required = true)
    private boolean myEnabled = true;

    /** The file path. */
    @XmlElement(name = "filePath", required = true)
    private String myFilePath = "";

    /** The group ref. */
    @XmlTransient
    private ImagerySourceGroup myGroup;

    /** The group name. */
    @XmlAttribute(name = "groupName")
    private String myGroupName = "";

    /** The has overviews. */
    @XmlAttribute(name = "hasOverviews", required = false)
    private boolean myHasOverviews;

    /** The ignore zeros. */
    @XmlAttribute(name = "ignoreZeros", required = false)
    private boolean myIgnoreZeros;

    /** The lat offset. */
    @XmlElement(name = "latOffset", required = false)
    private double myLatOffset;

    /** The load error. */
    @XmlAttribute(name = "loadError", required = false)
    private boolean myLoadError;

    /** The lon offset. */
    @XmlElement(name = "lonOffset", required = false)
    private double myLonOffset;

    /** The lower right lat. */
    @XmlElement(name = "lrLat", required = true)
    private double myLowerRightLat;

    /** The lower right lon. */
    @XmlElement(name = "lrLon", required = true)
    private double myLowerRightLon;

    /** The name. */
    @XmlAttribute(name = "name")
    private String myName = "";

    /** The pixel size lat. */
    @XmlElement(name = "pixelSizeLat", required = true)
    private double myPixelSizeLat;

    /** The pixel size lon. */
    @XmlElement(name = "pixelSizeLon", required = true)
    private double myPixelSizeLon;

    /** The projection. */
    @XmlAttribute(name = "projection", required = false)
    private String myProjection = "";

    /** The upper left lat. */
    @XmlElement(name = "ulLat", required = true)
    private double myUpperLeftLat;

    /** The upper left lon. */
    @XmlElement(name = "ulLon", required = true)
    private double myUpperLeftLon;

    /** The X resolution. */
    @XmlElement(name = "xResolution", required = true)
    private int myXResolution;

    /** The Y resolution. */
    @XmlElement(name = "yResolution", required = true)
    private int myYResolution;

    /**
     * Default constructor.
     */
    public ImageryFileSource()
    {
        super();
    }

    /**
     * Instantiates a new advanced image file source.
     *
     * @param aFile the a file
     */
    public ImageryFileSource(File aFile)
    {
        this();
        myFilePath = aFile.getAbsolutePath();
    }

    /**
     * Instantiates a new advanced image file source.
     *
     * @param other the other
     */
    public ImageryFileSource(ImageryFileSource other)
    {
        this();
        setEqualTo(other);
    }

    /**
     * Center on image.
     *
     * @param tb the tb
     * @param fly the fly
     */
    public void centerOnImage(Toolbox tb, boolean fly)
    {
        GeographicBoundingBox gbb = getBoundingBox();

        DynamicViewer view = tb.getMapManager().getStandardViewer();
        ViewerAnimator animator = new ViewerAnimator(view, gbb.getCenter());

        if (fly)
        {
            animator.start();
        }
        else
        {
            animator.snapToPosition();
        }
    }

    /**
     * Clean cache.
     *
     * @param dataRegistry The data registry.
     */
    public void cleanCache(DataRegistry dataRegistry)
    {
        String key = generateTypeKey();
        DataModelCategory dmc = new DataModelCategory(null, Image.class.getName(), key);
        long[] modelsRemoved = dataRegistry.removeModels(dmc, false);
        if (modelsRemoved != null)
        {
            LOGGER.info("Cleaned " + modelsRemoved.length + " items for layer " + key);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ImageryFileSource other = (ImageryFileSource)obj;
        return myBands == other.myBands && myEnabled == other.myEnabled && myHasOverviews == other.myHasOverviews
                && myIgnoreZeros == other.myIgnoreZeros && myLoadError == other.myLoadError
                && myXResolution == other.myXResolution && myYResolution == other.myYResolution
                && Utilities.equalsOrBothNaN(myLowerRightLat, other.myLowerRightLat)
                && Utilities.equalsOrBothNaN(myLowerRightLon, other.myLowerRightLon)
                && Utilities.equalsOrBothNaN(myLatOffset, other.myLatOffset)
                && Utilities.equalsOrBothNaN(myLonOffset, other.myLonOffset)
                && Utilities.equalsOrBothNaN(myPixelSizeLat, other.myPixelSizeLat)
                && Utilities.equalsOrBothNaN(myPixelSizeLon, other.myPixelSizeLon)
                && Utilities.equalsOrBothNaN(myUpperLeftLat, other.myUpperLeftLat)
                && Utilities.equalsOrBothNaN(myUpperLeftLon, other.myUpperLeftLon)
                && EqualsHelper.equals(myDatum, other.myDatum, myDescription, other.myDescription, myFilePath, other.myFilePath,
                        myGroupName, other.myGroupName, myName, other.myName, myProjection, other.myProjection);
    }

    /**
     * Generate type key.
     *
     * @return the string
     */
    public String generateTypeKey()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(myGroupName);
        sb.append("!!");
        sb.append(IMAGE);
        sb.append("::");
        sb.append(myName);
        sb.append("::");
        sb.append(myFilePath);
        return sb.toString();
    }

    /**
     * Gets the bands.
     *
     * @return the bands
     */
    public int getBands()
    {
        return myBands;
    }

    /**
     * Gets the bounding box.
     *
     * @return the bounding box
     */
    @Nonnull
    public GeographicBoundingBox getBoundingBox()
    {
        return new GeographicBoundingBox(LatLonAlt.createFromDegrees(myLowerRightLat, myUpperLeftLon),
                LatLonAlt.createFromDegrees(myUpperLeftLat, myLowerRightLon));
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    /**
     * Gets the datum.
     *
     * @return the datum
     */
    public String getDatum()
    {
        return myDatum;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the file path.
     *
     * @return the file path
     */
    public String getFilePath()
    {
        return myFilePath;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public ImagerySourceGroup getGroup()
    {
        return myGroup;
    }

    /**
     * Gets the group name.
     *
     * @return the group name
     */
    public String getGroupName()
    {
        return myGroupName;
    }

    /**
     * Gets the image size.
     *
     * @return the image size
     */
    public long getImageSize()
    {
        return new File(myFilePath).length();
    }

    /**
     * Gets the lat offset.
     *
     * @return the lat offset
     */
    public double getLatOffset()
    {
        return myLatOffset;
    }

    /**
     * Computes the number of divisions of the image at native resolution are
     * required to get just under the full resolution 512x512 tile chips
     * assuming the first level is the width of the full image.
     *
     * @return the number of levels.
     */
    public int getLevels()
    {
        int countToMaxResX = 1;
        double widthDivision = getXResolution();
        while (widthDivision > DEFAULT_TILE_SIZE)
        {
            countToMaxResX++;
            widthDivision = widthDivision / 2;
        }
        countToMaxResX = countToMaxResX > 10 ? 10 : countToMaxResX;

        int countToMaxResY = 1;
        double heightDivision = getYResolution();
        while (heightDivision > DEFAULT_TILE_SIZE)
        {
            countToMaxResY++;
            heightDivision = heightDivision / 2;
        }
        countToMaxResY = countToMaxResY > 10 ? 10 : countToMaxResY;

        return countToMaxResX > countToMaxResY ? countToMaxResX : countToMaxResY;
    }

    /**
     * Gets the lon offset.
     *
     * @return the lon offset
     */
    public double getLonOffset()
    {
        return myLonOffset;
    }

    /**
     * Gets the lower right lat.
     *
     * @return the lower right lat
     */
    public double getLowerRightLat()
    {
        return myLowerRightLat;
    }

    /**
     * Gets the lower right lon.
     *
     * @return the lower right lon
     */
    public double getLowerRightLon()
    {
        return myLowerRightLon;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public String getPath()
    {
        return getFilePath();
    }

    /**
     * Gets the pixel size lat.
     *
     * @return the pixel size lat
     */
    public double getPixelSizeLat()
    {
        return myPixelSizeLat;
    }

    /**
     * Gets the pixel size lon.
     *
     * @return the pixel size lon
     */
    public double getPixelSizeLon()
    {
        return myPixelSizeLon;
    }

    /**
     * Gets the projection.
     *
     * @return the projection
     */
    public String getProjection()
    {
        return myProjection;
    }

    /**
     * Gets the upper left lat.
     *
     * @return the upper left lat
     */
    public double getUpperLeftLat()
    {
        return myUpperLeftLat;
    }

    /**
     * Gets the upper left lon.
     *
     * @return the upper left lon
     */
    public double getUpperLeftLon()
    {
        return myUpperLeftLon;
    }

    /**
     * Gets the x resolution.
     *
     * @return the x resolution
     */
    public final int getXResolution()
    {
        return myXResolution;
    }

    /**
     * Gets the y resolution.
     *
     * @return the y resolution
     */
    public final int getYResolution()
    {
        return myYResolution;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myBands);
        result = prime * result + HashCodeHelper.getHashCode(myDatum);
        result = prime * result + HashCodeHelper.getHashCode(myDescription);
        result = prime * result + HashCodeHelper.getHashCode(myEnabled);
        result = prime * result + HashCodeHelper.getHashCode(myFilePath);
        result = prime * result + HashCodeHelper.getHashCode(myGroupName);
        result = prime * result + HashCodeHelper.getHashCode(myHasOverviews);
        result = prime * result + HashCodeHelper.getHashCode(myIgnoreZeros);
        result = prime * result + HashCodeHelper.getHashCode(myLoadError);
        result = prime * result + HashCodeHelper.getHashCode(myLowerRightLat);
        result = prime * result + HashCodeHelper.getHashCode(myLowerRightLon);
        result = prime * result + HashCodeHelper.getHashCode(myLatOffset);
        result = prime * result + HashCodeHelper.getHashCode(myLonOffset);
        result = prime * result + HashCodeHelper.getHashCode(myName);
        result = prime * result + HashCodeHelper.getHashCode(myPixelSizeLat);
        result = prime * result + HashCodeHelper.getHashCode(myPixelSizeLon);
        result = prime * result + HashCodeHelper.getHashCode(myProjection);
        result = prime * result + HashCodeHelper.getHashCode(myUpperLeftLat);
        result = prime * result + HashCodeHelper.getHashCode(myUpperLeftLon);
        result = prime * result + HashCodeHelper.getHashCode(myXResolution);
        result = prime * result + HashCodeHelper.getHashCode(myYResolution);
        return result;
    }

    /**
     * Checks for overviews.
     *
     * @return true, if successful
     */
    public boolean hasOverviews()
    {
        return myHasOverviews;
    }

    /**
     * Ignore zeros.
     *
     * @return true, if successful
     */
    public boolean ignoreZeros()
    {
        return myIgnoreZeros;
    }

    @Override
    public boolean isActive()
    {
        return isEnabled();
    }

    /**
     * Checks if is creates the overviews.
     *
     * @return true, if is creates the overviews
     */
    public boolean isCreateOverviews()
    {
        return myCreateOverviews;
    }

    /**
     * Checks if is enabled.
     *
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    @Override
    public boolean loadError()
    {
        return myLoadError;
    }

    @Override
    public void setActive(boolean active)
    {
        setEnabled(active);
    }

    /**
     * Sets the bands.
     *
     * @param bands the new bands
     */
    public void setBands(int bands)
    {
        myBands = bands;
    }

    /**
     * Sets the creates the overviews.
     *
     * @param createOverviews the new creates the overviews
     */
    public void setCreateOverviews(boolean createOverviews)
    {
        myCreateOverviews = createOverviews;
    }

    /**
     * Sets the data type info.
     *
     * @param dti the new data type info
     */
    public void setDataTypeInfo(DataTypeInfo dti)
    {
        myDataTypeInfo = dti;
    }

    /**
     * Sets the datum.
     *
     * @param datum the new datum
     */
    public void setDatum(String datum)
    {
        myDatum = datum;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }

    /**
     * Sets this {@link ImageryFileSource} equal to another ImageFileSource.
     *
     * @param other the other shape file source
     */
    public final void setEqualTo(ImageryFileSource other)
    {
        Utilities.checkNull(other, "other");
        myName = other.myName;
        myGroupName = other.myGroupName;
        myLoadError = other.myLoadError;
        myIgnoreZeros = other.myIgnoreZeros;
        myHasOverviews = other.myHasOverviews;
        myEnabled = other.myEnabled;
        myFilePath = other.myFilePath;
        myBands = other.myBands;
        myProjection = other.myProjection;
        myDatum = other.myDatum;
        myDescription = other.myDescription;
        myUpperLeftLat = other.myUpperLeftLat;
        myUpperLeftLon = other.myUpperLeftLon;
        myLowerRightLat = other.myLowerRightLat;
        myLowerRightLon = other.myLowerRightLon;
        myLatOffset = other.myLatOffset;
        myLonOffset = other.myLonOffset;
        myPixelSizeLat = other.myPixelSizeLat;
        myPixelSizeLon = other.myPixelSizeLon;
        myXResolution = other.myXResolution;
        myYResolution = other.myYResolution;
    }

    /**
     * Sets the file path.
     *
     * @param path the new file path
     */
    public void setFilePath(String path)
    {
        myFilePath = path;
    }

    /**
     * Sets the group.
     *
     * @param group the new group
     */
    public void setGroup(ImagerySourceGroup group)
    {
        myGroup = group;
    }

    /**
     * Sets the group name.
     *
     * @param groupName the new group name
     */
    public void setGroupName(String groupName)
    {
        myGroupName = groupName;
    }

    /**
     * Sets the checks for overviews.
     *
     * @param hasOverviews the new checks for overviews
     */
    public void setHasOverviews(boolean hasOverviews)
    {
        myHasOverviews = hasOverviews;
    }

    /**
     * Sets the ignore zeros.
     *
     * @param ignoreZeros the new ignore zeros
     */
    public void setIgnoreZeros(boolean ignoreZeros)
    {
        myIgnoreZeros = ignoreZeros;
    }

    /**
     * Sets the lat offset.
     *
     * @param latOffset the lat offset
     * @param source the source
     */
    public void setLatOffset(double latOffset, Object source)
    {
        myLatOffset = latOffset;
        fireDataSourceChanged(new DataSourceChangeEvent(this, ImageryFileSource.SOURCE_OFFSET_CHANGED, source));
    }

    @Override
    public void setLoadError(boolean error, Object source)
    {
        myLoadError = error;
        fireDataSourceChanged(new DataSourceChangeEvent(this, IDataSource.SOURCE_LOAD_ERROR_CHANGED, source));
    }

    /**
     * Sets the lon offset.
     *
     * @param lonOffset the lon offset
     * @param source the source
     */
    public void setLonOffset(double lonOffset, Object source)
    {
        myLonOffset = lonOffset;
        fireDataSourceChanged(new DataSourceChangeEvent(this, ImageryFileSource.SOURCE_OFFSET_CHANGED, source));
    }

    /**
     * Sets the lower right lat.
     *
     * @param lowerRightLat the new lower right lat
     */
    public void setLowerRightLat(double lowerRightLat)
    {
        myLowerRightLat = lowerRightLat;
    }

    /**
     * Sets the lower right lon.
     *
     * @param lowerRIghtLon the new lower right lon
     */
    public void setLowerRightLon(double lowerRIghtLon)
    {
        myLowerRightLon = lowerRIghtLon;
    }

    @Override
    public void setName(String name)
    {
        myName = name;
    }

    @Override
    public void setPath(String path)
    {
        setFilePath(path);
    }

    /**
     * Sets the pixel size lat.
     *
     * @param pixelSizeLat the new pixel size lat
     */
    public void setPixelSizeLat(double pixelSizeLat)
    {
        myPixelSizeLat = pixelSizeLat;
    }

    /**
     * Sets the pixel size lon.
     *
     * @param pixelSizeLon the new pixel size lon
     */
    public void setPixelSizeLon(double pixelSizeLon)
    {
        myPixelSizeLon = pixelSizeLon;
    }

    /**
     * Sets the projection.
     *
     * @param projection the new projection
     */
    public void setProjection(String projection)
    {
        myProjection = projection;
    }

    /**
     * Sets the upper left lat.
     *
     * @param upperLeftLat the new upper left lat
     */
    public void setUpperLeftLat(double upperLeftLat)
    {
        myUpperLeftLat = upperLeftLat;
    }

    /**
     * Sets the upper left lon.
     *
     * @param upperLeftLon the new upper left lon
     */
    public void setUpperLeftLon(double upperLeftLon)
    {
        myUpperLeftLon = upperLeftLon;
    }

    /**
     * Sets the x resolution.
     *
     * @param xResolution the new x resolution
     */
    public final void setXResolution(int xResolution)
    {
        myXResolution = xResolution;
    }

    /**
     * Sets the y resolution.
     *
     * @param yResolution the new y resolution
     */
    public final void setYResolution(int yResolution)
    {
        myYResolution = yResolution;
    }

    /**
     * Zoom1 to1.
     */
    public void zoom1To1()
    {
        // TODO
    }

    /**
     * Zoom to image.
     *
     * @param tb the tb
     */
    public void zoomToImage(Toolbox tb)
    {
        GeographicBoundingBox gbb = getBoundingBox();
        DynamicViewer view = tb.getMapManager().getStandardViewer();
        ViewerAnimator animator;
        if (gbb.getWidth() > 0.0 || gbb.getHeight() > 0.0)
        {
            animator = new ViewerAnimator(view, gbb.getVertices(), true);
            animator.snapToPosition();
        }
    }
}
