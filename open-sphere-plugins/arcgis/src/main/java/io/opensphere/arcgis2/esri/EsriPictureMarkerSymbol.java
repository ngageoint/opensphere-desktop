package io.opensphere.arcgis2.esri;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * <p>
 * Picture marker symbols can be used to symbolize point geometries. The
 * <code>type</code> property for simple marker symbols is <code>esriPMS</code>.
 * </p>
 * These symbols include the base64 encoded <code>imageData</code> as well as a
 * <code>url</code> that could be used to retrieve the image from the server.
 * Note that this is a relative URL. It can be dereferenced by accessing the
 * "map layer image resource" or the "feature layer image resource."
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriPictureMarkerSymbol extends EsriSymbolWithOffset
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** MIME type for the content type. */
    @JsonProperty("contentType")
    private String myContentType;

    /** My height. */
    @JsonProperty("height")
    private double myHeight;

    /** Image Data - a base64-encoded image. */
    @JsonProperty("imageData")
    private String myImageData;

    /** My relative URL. */
    @JsonProperty("url")
    private String myUrl;

    /** My width. */
    @JsonProperty("width")
    private double myWidth;

    /**
     * Gets the content type of the encoded image data.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return myContentType;
    }

    /**
     * Gets the image height.
     *
     * @return the height
     */
    public double getHeight()
    {
        return myHeight;
    }

    /**
     * Returns an image built from the received "imageData".
     *
     * @return the Java.awt.Image
     */
    public BufferedImage getImage()
    {
        BufferedImage image = null;
        byte[] imgBuffer = Base64.decodeBase64(myImageData.getBytes(StringUtilities.DEFAULT_CHARSET));
        try
        {
            image = ImageIO.read(new ByteArrayInputStream(imgBuffer));
        }
        catch (IOException e)
        {
            image = null;
        }
        return image;
    }

    /**
     * Gets the base-64 encoded image data.
     *
     * @return the image data
     */
    public String getImageData()
    {
        return myImageData;
    }

    /**
     * Gets the relative URL.
     *
     * @return the relative URL
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Gets the image width.
     *
     * @return the width
     */
    public double getWidth()
    {
        return myWidth;
    }

    /**
     * Sets the content type of the image.
     *
     * @param contentType the new contentType
     */
    public void setContentType(String contentType)
    {
        myContentType = contentType;
    }

    /**
     * Sets the image height.
     *
     * @param height the new height
     */
    public void setHeight(double height)
    {
        myHeight = height;
    }

    /**
     * Sets the base-64 encoded image data.
     *
     * @param imageData the new imageData
     */
    public void setImageData(String imageData)
    {
        myImageData = imageData;
    }

    /**
     * Sets the relative URL to the image on the server.
     *
     * @param url the new relative url
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }

    /**
     * Sets the image width.
     *
     * @param width the new width
     */
    public void setWidth(double width)
    {
        myWidth = width;
    }
}
