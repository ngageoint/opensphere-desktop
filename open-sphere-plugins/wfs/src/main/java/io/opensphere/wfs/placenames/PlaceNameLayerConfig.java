package io.opensphere.wfs.placenames;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A set of place names as determined by an associated filter for the WFS query.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PlaceNameLayerConfig implements Cloneable
{
    /** Name which uniquely identifies this data set. */
    @XmlElement(name = "DataSetName")
    private String myDataSetName;

    /** True when the layer should be displayed. */
    @XmlElement(name = "Enabled")
    private boolean myEnabled;

    /** The filter used to request features from the server. */
    @XmlElement(name = "Filter")
    private String myFilter;

    /** Color of the font. */
    @XmlElement(name = "FontColor")
    private String myFontColor;

    /** Font name to use for name display. */
    @XmlElement(name = "FontName")
    private String myFontName;

    /** The maximum altitude above which the layer will not be displayed. */
    @XmlElement(name = "MaxDisplayDistance")
    private double myMaxDisplayDistance;

    /** The minimum altitude below which the layer will not be displayed. */
    @XmlElement(name = "MinDisplayDistance")
    private double myMinDisplayDistance;

    /** Latitude and longitude span of the tile in degrees. */
    @XmlElement(name = "TileSize")
    private double myTileSize;

    @Override
    public PlaceNameLayerConfig clone() throws CloneNotSupportedException
    {
        return (PlaceNameLayerConfig)super.clone();
    }

    /**
     * Get the dataSetName.
     *
     * @return the dataSetName
     */
    public String getDataSetName()
    {
        return myDataSetName;
    }

    /**
     * Get the filter.
     *
     * @return the filter
     */
    public String getFilter()
    {
        return myFilter;
    }

    /**
     * Get the fontColor.
     *
     * @return the fontColor
     */
    public String getFontColor()
    {
        return myFontColor;
    }

    /**
     * Get the fontName.
     *
     * @return the fontName
     */
    public String getFontName()
    {
        return myFontName;
    }

    /**
     * Get the maxDisplayDistance.
     *
     * @return the maxDisplayDistance
     */
    public double getMaxDisplayDistance()
    {
        return myMaxDisplayDistance;
    }

    /**
     * Get the minDisplayDistance.
     *
     * @return the minDisplayDistance
     */
    public double getMinDisplayDistance()
    {
        return myMinDisplayDistance;
    }

    /**
     * Get the tileSize.
     *
     * @return the tileSize
     */
    public double getTileSize()
    {
        return myTileSize;
    }

    /**
     * Get the enabled.
     *
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    /**
     * Set the dataSetName.
     *
     * @param dataSetName the dataSetName to set
     */
    public void setDataSetName(String dataSetName)
    {
        myDataSetName = dataSetName;
    }

    /**
     * Set the enabled.
     *
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }

    /**
     * Set the filter.
     *
     * @param filter the filter to set
     */
    public void setFilter(String filter)
    {
        myFilter = filter;
    }

    /**
     * Set the fontColor.
     *
     * @param fontColor the fontColor to set
     */
    public void setFontColor(String fontColor)
    {
        myFontColor = fontColor;
    }

    /**
     * Set the fontName.
     *
     * @param fontName the fontName to set
     */
    public void setFontName(String fontName)
    {
        myFontName = fontName;
    }

    /**
     * Set the maxDisplayDistance.
     *
     * @param maxDisplayDistance the maxDisplayDistance to set
     */
    public void setMaxDisplayDistance(double maxDisplayDistance)
    {
        myMaxDisplayDistance = maxDisplayDistance;
    }

    /**
     * Set the minDisplayDistance.
     *
     * @param minDisplayDistance the minDisplayDistance to set
     */
    public void setMinDisplayDistance(double minDisplayDistance)
    {
        myMinDisplayDistance = minDisplayDistance;
    }

    /**
     * Set the tileSize.
     *
     * @param tileSize the tileSize to set
     */
    public void setTileSize(double tileSize)
    {
        myTileSize = tileSize;
    }
}
