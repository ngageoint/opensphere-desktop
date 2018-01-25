package io.opensphere.wfs.state.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;

/**
 * State model class that contains basic feature style details for a WFS layer.
 */
@XmlRootElement(name = "basicFeatureStyle")
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicFeatureStyle
{
    /** The Altitude column. */
    @XmlElement(name = "altitudeColumn")
    private String myAltitudeColumn;

    /** The Altitude column units. */
    @XmlElement(name = "altColUnits")
    private String myAltitudeColUnits = "meters";

    /** The Altitude reference. */
    @XmlElement(name = "altitudeRef")
    private String myAltitudeRef = StyleAltitudeReference.AUTOMATIC.toString();

    /** The Label color. */
    @XmlElement(name = "labelColor")
    private String myLabelColor;

    /** The Label column. */
    @XmlElement(name = "labelColumn")
    private String myLabelColumn = "NONE";

    /** support for multiple labels. */
    @XmlElement(name = "labelColumns")
    private LblConfig multiLabel;

    /** The Label size. */
    @XmlElement(name = "labelSize")
    private int myLabelSize;

    /** The Lift. */
    @XmlElement(name = "lift")
    private double myLift;

    /** The point color. */
    @XmlElement(name = "pointColor")
    private String myPointColor = "ffffff";

    /** The Point opacity. */
    @XmlElement(name = "pointOpacity")
    private int myPointOpacity = 255;

    /** The Point size. */
    @XmlElement(name = "pointSize")
    private float myPointSize = 4;

    /** The Use altitude. */
    @XmlElement(name = "useAltitude")
    private boolean myUseAltitude;

    /** The Use labels. */
    @XmlTransient
    private boolean myUseLabels;

    /** JAXB. */
    @XmlRootElement(name = "labelColumns")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LblConfig
    {
        /** JAXB. */
        @XmlElement(name = "label")
        public List<LblCfgRec> entries = new LinkedList<>();
    }

    /** JAXB. */
    @XmlRootElement(name = "label")
    public static class LblCfgRec
    {
        /** JAXB. */
        @XmlAttribute(name = "showColumn")
        public boolean showColumn;

        /** JAXB. */
        @XmlAttribute(name = "column")
        public String column;
    }

    /**
     * Gets the altitude column.
     *
     * @return the altitude column
     */
    public String getAltitudeColumn()
    {
        return myAltitudeColumn;
    }

    /**
     * Gets the altitude col units.
     *
     * @return the altitude col units
     */
    public String getAltitudeColUnits()
    {
        return myAltitudeColUnits;
    }

    /**
     * Gets the altitude ref.
     *
     * @return the altitude ref
     */
    public String getAltitudeRef()
    {
        return myAltitudeRef;
    }

    /**
     * Gets the label color.
     *
     * @return the label color
     */
    public String getLabelColor()
    {
        return myLabelColor;
    }

    /**
     * Gets the label column.
     *
     * @return the label column
     */
    public String getLabelColumn()
    {
        return myLabelColumn;
    }

    /**
     * Get while the gettin's good.
     * @return it
     */
    public List<LblCfgRec> getLabelRecs()
    {
        if (multiLabel == null)
        {
            return Collections.emptyList();
        }
        return multiLabel.entries;
    }

    /**
     * Reset to a state where no label information is present.
     */
    public void clearLabelRecs()
    {
        multiLabel = null;
    }

    /**
     * Not that you'd guess it from the name, but this adds a record to the
     * label stuff.
     * @param show a String that is parsed as a boolean
     * @param col a String column name
     */
    public void addLabelRec(String show, String col)
    {
        if (multiLabel == null)
        {
            multiLabel = new LblConfig();
        }
        LblCfgRec rec = new LblCfgRec();
        rec.showColumn = Boolean.parseBoolean(show);
        rec.column = col;
        multiLabel.entries.add(rec);
    }

    /**
     * Gets the label size.
     *
     * @return the label size
     */
    public int getLabelSize()
    {
        return myLabelSize;
    }

    /**
     * Gets the lift.
     *
     * @return the lift
     */
    public double getLift()
    {
        return myLift;
    }

    /**
     * Gets the point color.
     *
     * @return the point color
     */
    public String getPointColor()
    {
        return myPointColor;
    }

    /**
     * Gets the point opacity.
     *
     * @return the point opacity
     */
    public int getPointOpacity()
    {
        return myPointOpacity;
    }

    /**
     * Gets the point size.
     *
     * @return the point size
     */
    public float getPointSize()
    {
        return myPointSize;
    }

    /**
     * Checks if is use altitude.
     *
     * @return true, if is use altitude
     */
    public boolean isUseAltitude()
    {
        return myUseAltitude;
    }

    /**
     * Sets the altitude column.
     *
     * @param altitudeColumn the new altitude column
     */
    public void setAltitudeColumn(String altitudeColumn)
    {
        myAltitudeColumn = altitudeColumn;
    }

    /**
     * Sets the altitude col units.
     *
     * @param altitudeColUnits the new altitude col units
     */
    public void setAltitudeColUnits(String altitudeColUnits)
    {
        myAltitudeColUnits = altitudeColUnits;
    }

    /**
     * Sets the altitude ref.
     *
     * @param altitudeRef the new altitude ref
     */
    public void setAltitudeRef(String altitudeRef)
    {
        myAltitudeRef = altitudeRef;
    }

    /**
     * Sets the label color.
     *
     * @param labelColor the new label color
     */
    public void setLabelColor(String labelColor)
    {
        myLabelColor = labelColor;
    }

    /**
     * Sets the label column.
     *
     * @param labelColumn the new label column
     */
    public void setLabelColumn(String labelColumn)
    {
        myLabelColumn = labelColumn;
    }

    /**
     * Sets the label size.
     *
     * @param labelSize the new label size
     */
    public void setLabelSize(int labelSize)
    {
        myLabelSize = labelSize;
    }

    /**
     * Sets the lift.
     *
     * @param lift the new lift
     */
    public void setLift(double lift)
    {
        myLift = lift;
    }

    /**
     * Sets the point color.
     *
     * @param pointColor the new point color
     */
    public void setPointColor(String pointColor)
    {
        myPointColor = pointColor;
    }

    /**
     * Sets the point opacity.
     *
     * @param pointOpacity the new point opacity
     */
    public void setPointOpacity(int pointOpacity)
    {
        myPointOpacity = pointOpacity;
    }

    /**
     * Sets the point size.
     *
     * @param pointSize the new point size
     */
    public void setPointSize(float pointSize)
    {
        myPointSize = pointSize;
    }

    /**
     * Sets the use altitude.
     *
     * @param useAltitude the new use altitude
     */
    public void setUseAltitude(boolean useAltitude)
    {
        myUseAltitude = useAltitude;
    }

    /**
     * Gets the useLabels.
     *
     * @return the useLabels
     */
    public boolean isUseLabels()
    {
        return myUseLabels;
    }

    /**
     * Sets the useLabels.
     *
     * @param useLabels the useLabels
     */
    public void setUseLabels(boolean useLabels)
    {
        myUseLabels = useLabels;
    }
}
