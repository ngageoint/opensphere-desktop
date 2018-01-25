package io.opensphere.wms.config.v1;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Configuration for parameters used in the GetMap request. These settings are
 * used to generate the URL used to retrieve map tiles. It should be noted that
 * the URLs may be protected which would prevent them from being written to
 * disk. In this case, they must be set when this object is created.
 */
@XmlRootElement(name = "GetMapConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class WMSLayerGetMapConfig implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Background color requested for the tile. This is only used if transparent
     * is false.
     */
    @XmlElement(name = "BGColor")
    private String myBGColor;

    /**
     * Parameters that are useful for this server, but not part of the WMS
     * specification.
     */
    @XmlElement(name = "CustomParams")
    private String myCustomParams = StringUtilities.EMPTY;

    /**
     * Elevation of requested tile.
     */
    @XmlElement(name = "Elevation")
    private Integer myElevation;

    /**
     * Requested exception format. The default is "application/vnd.ogc.se_xml".
     */
    @XmlElement(name = "Exceptions")
    private String myExceptions;

    /**
     * The URL to be used for GetMap requests Provided by the GetCapabilities
     * document.
     */
    @XmlElement(name = "GetMapURL")
    private String myGetMapURL;

    /**
     * The Overridden URL to be used for GetMap requests. This should only be
     * used when the server provides invalid information.
     */
    @XmlElement(name = "GetMapURLOverride")
    private String myGetMapURLOverride;

    /**
     * The image format for the tile.
     */
    @XmlElement(name = "ImageFormat")
    private String myImageFormat;

    /**
     * The Spatial Reference System (SRS) which the bounding box is in.
     */
    @XmlElement(name = "SRS")
    private String mySRS;

    /**
     * The Style type. The default value is for compatibility with stored SERVER
     * types where the style has not been set.
     */
    @XmlElement(name = "StyleType")
    private StyleType myStyleType = StyleType.SERVER;

    /**
     * Style selection for the tile.
     */
    @XmlElement(name = "Style")
    private String myStyle;

    /**
     * Default style for the tile.
     */
    @XmlElement(name = "DefaultStyle")
    private String myDefaultStyle;

    /**
     * Pixel height of the returned tile.
     */
    @XmlElement(name = "TextureHeight")
    private Integer myTextureHeight;

    /**
     * Pixel width of the returned tile.
     */
    @XmlElement(name = "TextureWidth")
    private Integer myTextureWidth;

    /**
     * Time for the data the returned tile represents.
     */
    @XmlElement(name = "Time")
    private String myTime;

    /**
     * Transparency of the tile. If this is true, the background color is not
     * used. The GetCapabilities document provides a hint as to whether the
     * tiles are transparent, but either can be requested. When in doubt, it is
     * better to request transparent.
     */
    @XmlElement(name = "Transparent")
    private Boolean myTransparent;

    @Override
    public WMSLayerGetMapConfig clone() throws CloneNotSupportedException
    {
        return (WMSLayerGetMapConfig)super.clone();
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
        WMSLayerGetMapConfig other = (WMSLayerGetMapConfig)obj;
        //@formatter:off
        return Objects.equals(myBGColor, other.myBGColor)
                && Objects.equals(myCustomParams, other.myCustomParams)
                && Objects.equals(myElevation, other.myElevation)
                && Objects.equals(myExceptions, other.myExceptions)
                && Objects.equals(myGetMapURL, other.myGetMapURL)
                && Objects.equals(myGetMapURLOverride, other.myGetMapURLOverride)
                && Objects.equals(myImageFormat, other.myImageFormat)
                && Objects.equals(mySRS, other.mySRS)
                && Objects.equals(myStyle, other.myStyle)
                && Objects.equals(myDefaultStyle, other.myDefaultStyle)
                && Objects.equals(myStyleType, other.myStyleType)
                && Objects.equals(myTextureHeight, other.myTextureHeight)
                && Objects.equals(myTextureWidth, other.myTextureWidth)
                && Objects.equals(myTime, other.myTime)
                && Objects.equals(myTransparent, other.myTransparent);
        //@formatter:on
    }

    /**
     * Get the Background Color.
     *
     * @return Hex string representation of the Background Color
     */
    public String getBGColor()
    {
        return myBGColor;
    }

    /**
     * Get the customParams.
     *
     * @return the customParams
     */
    public String getCustomParams()
    {
        return myCustomParams;
    }

    /**
     * Get the elevation.
     *
     * @return the elevation
     */
    public Integer getElevation()
    {
        return myElevation;
    }

    /**
     * Get the exceptions.
     *
     * @return the exceptions
     */
    public String getExceptions()
    {
        return myExceptions;
    }

    /**
     * Get the GetMap URL.
     *
     * @return the GetMap URL
     */
    public String getGetMapURL()
    {
        return myGetMapURL;
    }

    /**
     * Get the GetMap URL Override.
     *
     * @return the GetMap URL Override
     */
    public String getGetMapURLOverride()
    {
        return myGetMapURLOverride;
    }

    /**
     * Get the imageFormat.
     *
     * @return the imageFormat
     */
    public String getImageFormat()
    {
        return myImageFormat;
    }

    /**
     * Get the Spatial Reference System (SRS).
     *
     * @return the SRS
     */
    public String getSRS()
    {
        return mySRS;
    }

    /**
     * Get the style.
     *
     * @return the style
     */
    public String getStyle()
    {
        return myStyle;
    }

    /**
     * Gets the defaultStyle.
     *
     * @return the defaultStyle
     */
    public String getDefaultStyle()
    {
        return myDefaultStyle;
    }

    /**
     * Gets the style type.
     *
     * @return the style type
     */
    public StyleType getStyleType()
    {
        return myStyleType;
    }

    /**
     * Get the textureHeight.
     *
     * @return the textureHeight
     */
    public Integer getTextureHeight()
    {
        return myTextureHeight;
    }

    /**
     * Get the textureWidth.
     *
     * @return the textureWidth
     */
    public Integer getTextureWidth()
    {
        return myTextureWidth;
    }

    /**
     * Get the time.
     *
     * @return the time
     */
    public String getTime()
    {
        return myTime;
    }

    /**
     * Get the transparent.
     *
     * @return the transparent
     */
    public Boolean getTransparent()
    {
        return myTransparent;
    }

    /**
     * Get the GetMap URL to actually use in the request. This is the override
     * URL if available, otherwise the one from the GetCapabilites document.
     *
     * @return the GetMap URL to use
     */
    public String getUsableGetMapURL()
    {
        if (StringUtils.isNotEmpty(myGetMapURLOverride))
        {
            return myGetMapURLOverride.trim();
        }
        return myGetMapURL.trim();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myBGColor);
        result = prime * result + HashCodeHelper.getHashCode(myCustomParams);
        result = prime * result + HashCodeHelper.getHashCode(myElevation);
        result = prime * result + HashCodeHelper.getHashCode(myExceptions);
        result = prime * result + HashCodeHelper.getHashCode(myGetMapURL);
        result = prime * result + HashCodeHelper.getHashCode(myGetMapURLOverride);
        result = prime * result + HashCodeHelper.getHashCode(myImageFormat);
        result = prime * result + HashCodeHelper.getHashCode(mySRS);
        result = prime * result + HashCodeHelper.getHashCode(myStyle);
        result = prime * result + HashCodeHelper.getHashCode(myDefaultStyle);
        result = prime * result + HashCodeHelper.getHashCode(myStyleType);
        result = prime * result + HashCodeHelper.getHashCode(myTextureHeight);
        result = prime * result + HashCodeHelper.getHashCode(myTextureWidth);
        result = prime * result + HashCodeHelper.getHashCode(myTime);
        result = prime * result + HashCodeHelper.getHashCode(myTransparent);
        return result;
    }

    /**
     * Set the Background Color.
     *
     * @param bGColor the background color to set
     */
    public void setBGColor(String bGColor)
    {
        myBGColor = bGColor;
    }

    /**
     * Set the customParams.
     *
     * @param customParams the customParams to set
     */
    public void setCustomParams(String customParams)
    {
        myCustomParams = customParams;
    }

    /**
     * Set the elevation.
     *
     * @param elevation the elevation to set
     */
    public void setElevation(Integer elevation)
    {
        myElevation = elevation;
    }

    /**
     * Set the exceptions.
     *
     * @param exceptions the exceptions to set
     */
    public void setExceptions(String exceptions)
    {
        myExceptions = exceptions;
    }

    /**
     * Set the GetMap URL.
     *
     * @param getMapURL the GetMap URL to set
     */
    public void setGetMapURL(String getMapURL)
    {
        myGetMapURL = getMapURL;
    }

    /**
     * Set the GetMap URL Override.
     *
     * @param getMapURLOverride the GetMap URL Override to set
     */
    public void setGetMapURLOverride(String getMapURLOverride)
    {
        myGetMapURLOverride = getMapURLOverride;
    }

    /**
     * Set the imageFormat.
     *
     * @param imageFormat the imageFormat to set
     */
    public void setImageFormat(String imageFormat)
    {
        myImageFormat = imageFormat;
    }

    /**
     * Set the Spatial Reference System (SRS).
     *
     * @param sRS the SRS to set
     */
    public void setSRS(String sRS)
    {
        mySRS = sRS;
    }

    /**
     * Set the style.
     *
     * @param style the style to set
     */
    public void setStyle(String style)
    {
        myStyle = style;
    }

    /**
     * Sets the defaultStyle.
     *
     * @param defaultStyle the defaultStyle
     */
    public void setDefaultStyle(String defaultStyle)
    {
        myDefaultStyle = defaultStyle;
    }

    /**
     * Sets the style type.
     *
     * @param styleType the new style type
     */
    public void setStyleType(StyleType styleType)
    {
        myStyleType = styleType;
    }

    /**
     * Set the textureHeight.
     *
     * @param textureHeight the textureHeight to set
     */
    public void setTextureHeight(Integer textureHeight)
    {
        myTextureHeight = textureHeight;
    }

    /**
     * Set the textureWidth.
     *
     * @param textureWidth the textureWidth to set
     */
    public void setTextureWidth(Integer textureWidth)
    {
        myTextureWidth = textureWidth;
    }

    /**
     * Set the time.
     *
     * @param time the time to set
     */
    public void setTime(String time)
    {
        myTime = time;
    }

    /**
     * Set the transparent.
     *
     * @param transparent the transparent to set
     */
    public void setTransparent(Boolean transparent)
    {
        myTransparent = transparent;
    }

    /**
     * The Enum StyleType.
     */
    @XmlTransient
    public enum StyleType
    {
        /** The CLIENT. */
        CLIENT("client"),

        /** The SERVER. */
        SERVER("server");

        /** The Title. */
        private final String myTitle;

        /**
         * Instantiates a new style type.
         *
         * @param title the title
         */
        StyleType(String title)
        {
            myTitle = title;
        }

        @Override
        public String toString()
        {
            return myTitle;
        }
    }
}
