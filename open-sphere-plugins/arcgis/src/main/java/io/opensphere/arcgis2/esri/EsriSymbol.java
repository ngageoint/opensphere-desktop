package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * The Class EsriSymbol.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = EsriSimpleMarkerSymbol.class, name = "esriSMS"),
    @Type(value = EsriSimpleLineSymbol.class, name = "esriSLS"), @Type(value = EsriSimpleFillSymbol.class, name = "esriSFS"),
    @Type(value = EsriPictureMarkerSymbol.class, name = "esriPMS"), @Type(value = EsriPictureFillSymbol.class, name = "esriPFS"),
    @Type(value = EsriTextSymbol.class, name = "esriTS") })

public abstract class EsriSymbol implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My color. */
    @JsonProperty("color")
    private EsriColor myColor;

    /**
     * Gets the color.
     *
     * @return the color
     */
    public EsriColor getColor()
    {
        return myColor;
    }

    /**
     * Sets the color.
     *
     * @param color the new color
     */
    public void setColor(EsriColor color)
    {
        myColor = color;
    }
}
