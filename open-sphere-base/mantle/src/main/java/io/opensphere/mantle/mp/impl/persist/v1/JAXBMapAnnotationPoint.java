package io.opensphere.mantle.mp.impl.persist.v1;

import java.awt.Color;
import java.awt.Font;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointSettings;

/**
 * The Class JAXBMapAnnotationPoint.
 */
@XmlRootElement(name = "MapAnnotationPoint")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBMapAnnotationPoint implements MapAnnotationPoint
{
    /** The annotation settings. */
    @XmlElement(name = "annoSettings", required = true)
    private final JAXBMapAnnotationPointSettings myAnnoSettings;

    /** The color. */
    @XmlAttribute(name = "color")
    private String myColorValStr;

    /** The description. */
    @XmlAttribute(name = "description")
    private String myDescriptionVal = "";

    /** Whether the bubble is filled. */
    @XmlAttribute(name = "filled")
    private boolean myFilled;

    /** The font color. */
    @XmlElement(name = "font", required = false)
    private final JAXBFont myFont;

    /** The font color. */
    @XmlAttribute(name = "fontColor")
    private String myFontColorValStr;

    /** The font position. */
    @XmlAttribute(name = "fontSize")
    private String myFontSizeVal = "";

    /** The latitude. */
    @XmlAttribute(name = "lat")
    private double myLatVal;

    /** The longitude. */
    @XmlAttribute(name = "lon")
    private double myLonVal;

    /** The altitude value. */
    @XmlAttribute(name = "altitude")
    private double myAltitude;

    /** The MGRS coords. */
    @XmlAttribute(name = "mgrs")
    private String myMGRSVal = "";

    /** The title. */
    @XmlAttribute(name = "title")
    private String myTitleVal = "";

    /** The visible. */
    @XmlAttribute(name = "viewName", required = false)
    private String myViewName;

    /** The visible. */
    @XmlAttribute(name = "visible")
    private boolean myVisibleVal;

    /**
     * The x coordinate offset of the call out for the map point with respect to
     * the anchor position.
     */
    @XmlAttribute(name = "xOffset")
    private int myXOffsetVal;

    /**
     * The y coordinate offset of the call out for the map point with respect to
     * the anchor position.
     */
    @XmlAttribute(name = "yOffset")
    private int myYOffsetVal;

    /**
     * Constructor.
     */
    public JAXBMapAnnotationPoint()
    {
        myAnnoSettings = new JAXBMapAnnotationPointSettings();
        myFont = new JAXBFont();
    }

    /**
     * COPY CTOR.
     *
     * @param other the other to copy.
     */
    public JAXBMapAnnotationPoint(MapAnnotationPoint other)
    {
        myAnnoSettings = new JAXBMapAnnotationPointSettings(other.getAnnoSettings());
        myColorValStr = ColorUtilities.convertToRGBAColorString(other.getColor() == null ? Color.white : other.getColor());
        myFilled = other.isFilled();
        myFontColorValStr = ColorUtilities
                .convertToRGBAColorString(other.getFontColor() == null ? Color.white : other.getFontColor());
        myDescriptionVal = other.getDescription();
        myFontSizeVal = other.getFontSize();
        myFont = new JAXBFont();
        try
        {
            float size = Float.parseFloat(myFontSizeVal);
            myFont.setFont(other.getFont().deriveFont(size));
        }
        catch (NumberFormatException e)
        {
            myFont.setFont(other.getFont());
        }
        myLatVal = other.getLat();
        myLonVal = other.getLon();
        myMGRSVal = other.getMGRS();
        myTitleVal = other.getTitle();
        myViewName = other.getAssociatedViewName();
        myVisibleVal = other.isVisible();
        myXOffsetVal = other.getxOffset();
        myYOffsetVal = other.getyOffset();
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
        MapAnnotationPoint other = (MapAnnotationPoint)obj;
        if (!EqualsHelper.equals(myTitleVal, other.getTitle()) || !EqualsHelper.equals(myViewName, other.getAssociatedViewName()))
        {
            return false;
        }
        if (!EqualsHelper.equals(Double.valueOf(myLatVal), Double.valueOf(other.getLat()), Double.valueOf(myLonVal),
                Double.valueOf(other.getLon()), Double.valueOf(myAltitude), Double.valueOf(other.getAltitude())))
        {
            return false;
        }
        if (!EqualsHelper.equals(Boolean.valueOf(myVisibleVal), Boolean.valueOf(other.isVisible()), Double.valueOf(myXOffsetVal),
                Double.valueOf(other.getxOffset()), Double.valueOf(myYOffsetVal), Double.valueOf(other.getyOffset())))
        {
            return false;
        }
        if (!EqualsHelper.equals(myAnnoSettings, other.getAnnoSettings(), myColorValStr, other.getColor(), myDescriptionVal,
                other.getDescription()))
        {
            return false;
        }
        return EqualsHelper.equals(getFont(), other.getFont(), myFontColorValStr, other.getFontColor(), myFontSizeVal,
                other.getFontSize(), myMGRSVal, other.getMGRS(), Boolean.valueOf(myFilled), Boolean.valueOf(other.isFilled()));
    }

    @Override
    public MapAnnotationPointSettings getAnnoSettings()
    {
        return myAnnoSettings;
    }

    @Override
    public String getAssociatedViewName()
    {
        return myViewName;
    }

    @Override
    public Color getBackgroundColor()
    {
        // Not implemented
        return null;
    }

    @Override
    public Color getColor()
    {
        return ColorUtilities.convertFromColorString(myColorValStr);
    }

    @Override
    public String getDescription()
    {
        return myDescriptionVal;
    }

    @Override
    public Font getFont()
    {
        // TODO fix this
        Font result = null;
        try
        {
            result = myFont == null ? null : myFont.getFont().deriveFont(Float.parseFloat(myFontSizeVal));
        }
        catch (NumberFormatException e)
        {
            result = myFont.getFont().deriveFont(myFont.getFont().getSize());
        }
        return result;
    }

    @Override
    public Color getFontColor()
    {
        return ColorUtilities.convertFromColorString(myFontColorValStr);
    }

    @Override
    public String getFontSize()
    {
        return myFontSizeVal;
    }

    @Override
    public double getLat()
    {
        return myLatVal;
    }

    @Override
    public double getLon()
    {
        return myLonVal;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MapAnnotationPoint#getAltitude()
     */
    @Override
    public double getAltitude()
    {
        return myAltitude;
    }

    @Override
    public String getMGRS()
    {
        return myMGRSVal;
    }

    @Override
    public String getTitle()
    {
        return myTitleVal;
    }

    @Override
    public int getxOffset()
    {
        return myXOffsetVal;
    }

    @Override
    public int getyOffset()
    {
        return myYOffsetVal;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myLatVal);
        result = prime * result + HashCodeHelper.getHashCode(myLonVal);
        result = prime * result + HashCodeHelper.getHashCode(myAnnoSettings);
        result = prime * result + HashCodeHelper.getHashCode(myColorValStr);
        result = prime * result + HashCodeHelper.getHashCode(myFilled);
        result = prime * result + HashCodeHelper.getHashCode(myDescriptionVal);
        result = prime * result + HashCodeHelper.getHashCode(getFont());
        result = prime * result + HashCodeHelper.getHashCode(myFontColorValStr);
        result = prime * result + HashCodeHelper.getHashCode(myFontSizeVal);
        result = prime * result + HashCodeHelper.getHashCode(myMGRSVal);
        result = prime * result + HashCodeHelper.getHashCode(myTitleVal);
        result = prime * result + HashCodeHelper.getHashCode(myViewName);
        result = prime * result + HashCodeHelper.getHashCode(myVisibleVal);
        result = prime * result + HashCodeHelper.getHashCode(myXOffsetVal);
        result = prime * result + HashCodeHelper.getHashCode(myYOffsetVal);
        return result;
    }

    @Override
    public boolean isFilled()
    {
        return myFilled;
    }

    @Override
    public boolean isVisible()
    {
        return myVisibleVal;
    }

    /**
     * JAXB Font.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class JAXBFont
    {
        /** The logical name. */
        @XmlAttribute(name = "name")
        private String myName;

        /** The size. */
        @XmlAttribute(name = "size")
        private int mySize;

        /** The style. */
        @XmlAttribute(name = "style")
        private int myStyle;

        /**
         * Constructor.
         */
        public JAXBFont()
        {
            myName = DEFAULT_FONT.getName();
            myStyle = DEFAULT_FONT.getStyle();
            mySize = DEFAULT_FONT.getSize();
        }

        /**
         * Getter for font.
         *
         * @return the font
         */
        public Font getFont()
        {
            return new Font(myName, myStyle, mySize);
        }

        /**
         * Setter for font.
         *
         * @param font The font
         */
        public void setFont(Font font)
        {
            myName = font.getName();
            myStyle = font.getStyle();
            mySize = font.getSize();
        }
    }

    @Override
    public TimeSpan getTime()
    {
        return null;
    }

    @Override
    public boolean isTimeEnabled()
    {
        return false;
    }

    @Override
    public boolean hasAltitude()
    {
        return false;
    }
}
