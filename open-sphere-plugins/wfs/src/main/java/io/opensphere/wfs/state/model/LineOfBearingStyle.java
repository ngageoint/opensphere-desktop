package io.opensphere.wfs.state.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * State model class that contains line of bearing style details for a WFS
 * layer.
 */
@XmlRootElement(name = "lobStyle")
@XmlAccessorType(XmlAccessType.FIELD)
public class LineOfBearingStyle
{
    /** The Arrow length. */
    @XmlElement(name = "arrowLength")
    private float myArrowLength;

    /** The Line width. */
    @XmlElement(name = "lineWidth")
    private float myLineWidth = 1;

    /** The Lob length. */
    @XmlElement(name = "logLength")
    private float myLobLength = 100000;

    /** The LOB orientation column. */
    @XmlElement(name = "lobOrientCol")
    private String myLOBOrientationColumn = "NONE";

    /** The Origin point size. */
    @XmlElement(name = "originPointSize")
    private float myOriginPointSize = 4;

    /** The Show arrow. */
    @XmlElement(name = "showArrow")
    private boolean myShowArrow;

    /**
     * Gets the arrow length.
     *
     * @return the arrow length
     */
    public float getArrowLength()
    {
        return myArrowLength;
    }

    /**
     * Gets the line width.
     *
     * @return the line width
     */
    public float getLineWidth()
    {
        return myLineWidth;
    }

    /**
     * Gets the lob length.
     *
     * @return the lob length
     */
    public float getLobLength()
    {
        return myLobLength;
    }

    /**
     * Gets the lOB orientation column.
     *
     * @return the lOB orientation column
     */
    public String getLOBOrientationColumn()
    {
        return myLOBOrientationColumn;
    }

    /**
     * Gets the origin point size.
     *
     * @return the origin point size
     */
    public float getOriginPointSize()
    {
        return myOriginPointSize;
    }

    /**
     * Checks if is show arrow.
     *
     * @return true, if is show arrow
     */
    public boolean isShowArrow()
    {
        return myShowArrow;
    }

    /**
     * Sets the arrow length.
     *
     * @param arrowLength the new arrow length
     */
    public void setArrowLength(float arrowLength)
    {
        myArrowLength = arrowLength;
    }

    /**
     * Sets the line width.
     *
     * @param lineWidth the new line width
     */
    public void setLineWidth(float lineWidth)
    {
        myLineWidth = lineWidth;
    }

    /**
     * Sets the lob length.
     *
     * @param lobLength the new lob length
     */
    public void setLobLength(float lobLength)
    {
        myLobLength = lobLength;
    }

    /**
     * Sets the lOB orientation column.
     *
     * @param lOBOrientationColumn the new lOB orientation column
     */
    public void setLOBOrientationColumn(String lOBOrientationColumn)
    {
        myLOBOrientationColumn = lOBOrientationColumn;
    }

    /**
     * Sets the origin point size.
     *
     * @param originPointSize the new origin point size
     */
    public void setOriginPointSize(float originPointSize)
    {
        myOriginPointSize = originPointSize;
    }

    /**
     * Sets the show arrow.
     *
     * @param showArrow the new show arrow
     */
    public void setShowArrow(boolean showArrow)
    {
        myShowArrow = showArrow;
    }
}
