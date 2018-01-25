package io.opensphere.wms.config.v1;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * Layer configuration settings that affect how the layer is displayed.
 */
@XmlRootElement(name = "DisplayConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class WMSLayerDisplayConfig implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The largest size of tile to be requested.
     */
    @XmlElement(name = "LargestTileSize")
    private double myLargestTileSize = 45.0;

    /**
     * The Maximum elevation above which the layer will not be displayed.
     */
    @XmlElement(name = "MaxDisplayElevation")
    private Double myMaxDisplayElevation;

    /**
     * The Minimum elevation below which the layer will not be displayed.
     */
    @XmlElement(name = "MinDisplayElevation")
    private Double myMinDisplayElevation;

    /**
     * The amount of time before the tile layers should be re-downloaded.
     */
    @XmlElement(name = "RefreshTime")
    private long myRefreshTime;

    /**
     * The maximum number of times to split. Beyond this, the last loaded tiles
     * will continued to be displayed.
     */
    @XmlElement(name = "ResolveLevels")
    private int myResolveLevels = 18;

    @Override
    public WMSLayerDisplayConfig clone() throws CloneNotSupportedException
    {
        return (WMSLayerDisplayConfig)super.clone();
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
        WMSLayerDisplayConfig other = (WMSLayerDisplayConfig)obj;
        //@formatter:off
        return EqualsHelper.doubleEquals(myLargestTileSize, other.myLargestTileSize)
                && Objects.equals(myMaxDisplayElevation, other.myMaxDisplayElevation)
                && Objects.equals(myMinDisplayElevation, other.myMinDisplayElevation)
                && myRefreshTime == other.myRefreshTime
                && myResolveLevels == other.myResolveLevels;
        //@formatter:on
    }

    /**
     * Get the largestTileSize.
     *
     * @return the largestTileSize
     */
    public double getLargestTileSize()
    {
        return myLargestTileSize;
    }

    /**
     * Get the maxDisplayElevation.
     *
     * @return the maxDisplayElevation
     */
    public Double getMaxDisplayElevation()
    {
        return myMaxDisplayElevation;
    }

    /**
     * Get the minDisplayElevation.
     *
     * @return the minDisplayElevation
     */
    public Double getMinDisplayElevation()
    {
        return myMinDisplayElevation;
    }

    /**
     * Gets the amount of time before the tile layers should be re-downloaded.
     *
     * @return the refreshTime The amount of time before the tile layers should
     *         be re-downloaded.
     */
    public long getRefreshTime()
    {
        return myRefreshTime;
    }

    /**
     * Get the resolveLevels.
     *
     * @return the resolveLevels
     */
    public Integer getResolveLevels()
    {
        return Integer.valueOf(myResolveLevels);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myLargestTileSize);
        result = prime * result + HashCodeHelper.getHashCode(myMaxDisplayElevation);
        result = prime * result + HashCodeHelper.getHashCode(myMinDisplayElevation);
        result = prime * result + HashCodeHelper.getHashCode(myRefreshTime);
        result = prime * result + HashCodeHelper.getHashCode(myResolveLevels);
        return result;
    }

    /**
     * Set the largestTileSize.
     *
     * @param largestTileSize the largestTileSize to set
     */
    public void setLargestTileSize(double largestTileSize)
    {
        myLargestTileSize = largestTileSize;
    }

    /**
     * Set the maxDisplayElevation.
     *
     * @param maxDisplayElevation the maxDisplayElevation to set
     */
    public void setMaxDisplayElevation(Double maxDisplayElevation)
    {
        myMaxDisplayElevation = maxDisplayElevation;
    }

    /**
     * Set the minDisplayElevation.
     *
     * @param minDisplayElevation the minDisplayElevation to set
     */
    public void setMinDisplayElevation(Double minDisplayElevation)
    {
        myMinDisplayElevation = minDisplayElevation;
    }

    /**
     * Sets the amount of time before the tile layers should be re-downloaded.
     *
     * @param refreshTime the refreshTime to set.
     */
    public void setRefreshTime(long refreshTime)
    {
        myRefreshTime = refreshTime;
    }

    /**
     * Set the resolveLevels.
     *
     * @param resolveLevels the resolveLevels to set
     */
    public void setResolveLevels(Integer resolveLevels)
    {
        myResolveLevels = resolveLevels.intValue();
    }
}

