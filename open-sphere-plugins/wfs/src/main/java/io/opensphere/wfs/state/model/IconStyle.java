package io.opensphere.wfs.state.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * State model class that contains icon style details for a WFS layer.
 */
@XmlRootElement(name = "iconStyle")
@XmlAccessorType(XmlAccessType.FIELD)
public class IconStyle
{
    /** The Default point size. */
    @XmlElement(name = "iconDefaultPointSize")
    private float myDefaultPointSize;

    /**
     * What to draw, in case the user wants to have the icon style, but draw
     * something other than icons.
     */
    @XmlElement(name = "iconDefaultTo")
    private String myDefaultTo = "Icon";

    /** The Icon size. */
    @XmlElement(name = "iconScale")
    private float myIconScale = 10;

    /** The Icon url. */
    @XmlElement(name = "defaultIconURL")
    private String myIconURL;

    /** The Icon x offset. */
    @XmlElement(name = "iconXOffset")
    private int myIconXOffset;

    /** The Icon y offset. */
    @XmlElement(name = "iconYOffset")
    private int myIconYOffset;

    /** The Mix icon element color. */
    @XmlElement(name = "mixIconElementColor")
    private boolean myMixIconElementColor;

    /**
     * Gets the default point size.
     *
     * @return the default point size
     */
    public float getDefaultPointSize()
    {
        return myDefaultPointSize;
    }

    /**
     * Get the defaultTo.
     *
     * @return the defaultTo
     */
    public String getDefaultTo()
    {
        return myDefaultTo;
    }

    /**
     * Gets the icon scale.
     *
     * @return the icon scale
     */
    public float getIconScale()
    {
        return myIconScale;
    }

    /**
     * Gets the icon url.
     *
     * @return the icon url
     */
    public String getIconURL()
    {
        return myIconURL;
    }

    /**
     * Gets the icon x offset.
     *
     * @return the icon x offset
     */
    public int getIconXOffset()
    {
        return myIconXOffset;
    }

    /**
     * Gets the icon y offset.
     *
     * @return the icon y offset
     */
    public int getIconYOffset()
    {
        return myIconYOffset;
    }

    /**
     * Checks if is mix icon element color.
     *
     * @return true, if is mix icon element color
     */
    public boolean isMixIconElementColor()
    {
        return myMixIconElementColor;
    }

    /**
     * Set the defaultPointSize.
     *
     * @param defaultPointSize the defaultPointSize to set
     */
    public void setDefaultPointSize(float defaultPointSize)
    {
        myDefaultPointSize = defaultPointSize;
    }

    /**
     * Set the defaultTo.
     *
     * @param defaultTo the defaultTo to set
     */
    public void setDefaultTo(String defaultTo)
    {
        myDefaultTo = defaultTo;
    }

    /**
     * Sets the icon scale.
     *
     * @param iconScale the new icon scale
     */
    public void setIconScale(float iconScale)
    {
        myIconScale = iconScale;
    }

    /**
     * Sets the icon url.
     *
     * @param iconURL the new icon url
     */
    public void setIconURL(String iconURL)
    {
        myIconURL = iconURL;
    }

    /**
     * Sets the icon x offset.
     *
     * @param iconXOffset the new icon x offset
     */
    public void setIconXOffset(int iconXOffset)
    {
        myIconXOffset = iconXOffset;
    }

    /**
     * Sets the icon y offset.
     *
     * @param iconYOffset the new icon y offset
     */
    public void setIconYOffset(int iconYOffset)
    {
        myIconYOffset = iconYOffset;
    }

    /**
     * Sets the mix icon element color.
     *
     * @param mixIconElementColor the new mix icon element color
     */
    public void setMixIconElementColor(boolean mixIconElementColor)
    {
        myMixIconElementColor = mixIconElementColor;
    }
}
