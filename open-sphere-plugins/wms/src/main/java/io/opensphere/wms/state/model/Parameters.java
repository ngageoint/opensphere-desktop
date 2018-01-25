package io.opensphere.wms.state.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A State model class that contains specific url parameters used by the wms
 * layer to add to the wms url.
 *
 */
@XmlRootElement(name = "params")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameters
{
    /**
     * Image height in pixels.
     */
    @XmlElement(name = "HEIGHT")
    private Integer myHeight;

    /**
     * Image width in pixels.
     */
    @XmlElement(name = "WIDTH")
    private Integer myWidth;

    /**
     * If this layer has transparency.
     */
    @XmlElement(name = "TRANSPARENT")
    private Boolean myTransparent;

    /**
     * The image format.
     */
    @XmlElement(name = "FORMAT")
    private String myFormat;

    /**
     * The background color.
     */
    @XmlElement(name = "BGCOLOR")
    private String myBgColor;

    /**
     * The Spatial Reference System.
     */
    @XmlElement(name = "SRS")
    private String mySrs;

    /**
     * The style parameter for the WMS url.
     */
    @XmlElement(name = "STYLE")
    private String myStyle;

    /**
     * Parameters that are useful for this server, but not part of the WMS
     * specification.
     */
    @XmlElement(name = "CUSTOM")
    private String myCustom;

    /**
     * The name of the layer to retrieve from the server.
     */
    @XmlElement(name = "LAYERS")
    private String myLayerName;

    /**
     * Gets the background color.
     *
     * @return The background color in hex string format.
     */
    public String getBgColor()
    {
        return myBgColor;
    }

    /**
     * Gets the Parameters that are useful for this server, but not part of the
     * WMS specification.
     *
     * @return The custom parameters or null if none.
     */
    public String getCustom()
    {
        return myCustom;
    }

    /**
     * Gets the image format.
     *
     * @return The image format.
     */
    public String getFormat()
    {
        return myFormat;
    }

    /**
     * Gets the image height in pixels.
     *
     * @return The image height in pixels.
     */
    public Integer getHeight()
    {
        return myHeight;
    }

    /**
     * Gets the layer name.
     *
     * @return The layer name.
     */
    public String getLayerName()
    {
        return myLayerName;
    }

    /**
     * Gets The Spatial Reference System.
     *
     * @return The Spatial Reference System.
     */
    public String getSrs()
    {
        return mySrs;
    }

    /**
     * Gets the style parameter for the WMS url.
     *
     * @return The style parameter for the WMS url.
     */
    public String getStyle()
    {
        return myStyle;
    }

    /**
     * Gets the image width in pixels.
     *
     * @return The image width in pixels.
     */
    public Integer getWidth()
    {
        return myWidth;
    }

    /**
     * Gets if this layer has transparency.
     *
     * @return The layer transparency.
     */
    public Boolean isTransparent()
    {
        return myTransparent;
    }

    /**
     * Sets the background color.
     *
     * @param bgColor The background color in hex string format.
     */
    public void setBgColor(String bgColor)
    {
        myBgColor = bgColor;
    }

    /**
     * Sets the Parameters that are useful for this server, but not part of the
     * WMS specification.
     *
     * @param custom The custom parameters or null if none.
     */
    public void setCustom(String custom)
    {
        myCustom = custom;
    }

    /**
     * Sets the image format.
     *
     * @param format The image format.
     */
    public void setFormat(String format)
    {
        myFormat = format;
    }

    /**
     * Sets the image height in pixels.
     *
     * @param height The image height in pixels.
     */
    public void setHeight(Integer height)
    {
        myHeight = height;
    }

    /**
     * Sets the layer name.
     *
     * @param layerName The layer name to set.
     */
    public void setLayerName(String layerName)
    {
        myLayerName = layerName;
    }

    /**
     * Sets the Spatial Reference System.
     *
     * @param srs The Spatial Reference System.
     */
    public void setSrs(String srs)
    {
        mySrs = srs;
    }

    /**
     * Sets the style parameter for the WMS url.
     *
     * @param style The style parameter for the WMS url.
     */
    public void setStyle(String style)
    {
        myStyle = style;
    }

    /**
     * Sets if this layer has transparency.
     *
     * @param transparent True if it has transparency, false otherwise.
     */
    public void setTransparent(Boolean transparent)
    {
        myTransparent = transparent;
    }

    /**
     * Sets the image width in pixels.
     *
     * @param width The image width in pixels.
     */
    public void setWidth(Integer width)
    {
        myWidth = width;
    }
}
