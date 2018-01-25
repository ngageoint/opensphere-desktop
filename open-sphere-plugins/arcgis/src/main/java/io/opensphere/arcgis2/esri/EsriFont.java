package io.opensphere.arcgis2.esri;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class EsriFont.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriFont
{
    /** My font decoration. */
    @JsonProperty("decoration")
    private EsriFontDecoration myDecoration;

    /** My font family. */
    @JsonProperty("family")
    private String myFamily;

    /** My size. This is a string that may contain the units (e.g. "12px") */
    @JsonProperty("size")
    private String mySize;

    /** My style. */
    @JsonProperty("style")
    private EsriFontStyle myStyle;

    /** My font weight. */
    @JsonProperty("weight")
    private EsriFontWeight myWeight;

    /**
     * Gets the font decoration.
     *
     * @return the font decoration
     */
    public EsriFontDecoration getDecoration()
    {
        return myDecoration;
    }

    /**
     * Gets the font family.
     *
     * @return the font family
     */
    public String getFamily()
    {
        return myFamily;
    }

    /**
     * Gets the font size.
     *
     * @return the font size
     */
    public String getSize()
    {
        return mySize;
    }

    /**
     * Gets the style.
     *
     * @return the style
     */
    public EsriFontStyle getStyle()
    {
        return myStyle;
    }

    /**
     * Gets the font weight.
     *
     * @return the font weight
     */
    public EsriFontWeight getWeight()
    {
        return myWeight;
    }

    /**
     * Sets the font decoration.
     *
     * @param decoration the new font decoration
     */
    public void setDecoration(EsriFontDecoration decoration)
    {
        myDecoration = decoration;
    }

    /**
     * Sets the font family.
     *
     * @param family the new family
     */
    public void setFamily(String family)
    {
        myFamily = family;
    }

    /**
     * Sets the font size.
     *
     * @param size the new font size
     */
    public void setSize(String size)
    {
        mySize = size;
    }

    /**
     * Sets the style.
     *
     * @param style the new style
     */
    public void setStyle(EsriFontStyle style)
    {
        myStyle = style;
    }

    /**
     * Sets the font weight.
     *
     * @param weight the new font weight
     */
    public void setWeight(EsriFontWeight weight)
    {
        myWeight = weight;
    }

    /**
     * The Enum EsriFontDecoration.
     */
    public enum EsriFontDecoration
    {
        /** Bold font weight. */
        LINE_THROUGH("line-through"),

        /** Lighter font weight. */
        NONE("none"),

        /** Bolder font weight. */
        UNDERLINE("underline");

        /** The enum value. */
        private final String myValue;

        /**
         * Instantiates a new ESRI font decoration enum.
         *
         * @param value the enum value
         */
        EsriFontDecoration(String value)
        {
            myValue = value;
        }

        @Override
        public String toString()
        {
            return myValue;
        }
    }

    /**
     * The Enum EsriFontStyle.
     */
    public enum EsriFontStyle
    {
        /** Italic font style. */
        italic,

        /** Normal font style. */
        normal,

        /** Oblique font style. */
        oblique
    }

    /**
     * The Enum EsriFontWeight.
     */
    public enum EsriFontWeight
    {
        /** Bold font weight. */
        bold,

        /** Bolder font weight. */
        bolder,

        /** Lighter font weight. */
        lighter,

        /** Normal font weight. */
        normal
    }
}
