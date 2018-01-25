package io.opensphere.wfs.state.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.mantle.data.geom.style.impl.AbstractEllipseFeatureVisualizationStyle.EllipseFillStyle;

/**
 * State model class that contains ellipse style details for a WFS layer.
 */
@XmlRootElement(name = "ellipseStyle")
@XmlAccessorType(XmlAccessType.FIELD)
public class EllipseStyle
{
    /** The Axis units. */
    @XmlElement(name = "axisUnits")
    private String myAxisUnits = "meters";

    /** The Center point. */
    @XmlElement(name = "centerPoint")
    private boolean myCenterPoint = true;

    /** The Center point size. */
    @XmlElement(name = "centerPointSize")
    private int myCenterPointSize = 4;

    /** The Edge line. */
    @XmlElement(name = "edgeLine")
    private boolean myEdgeLine = true;

    /** The Edge line width. */
    @XmlElement(name = "edgeLineWidth")
    private int myEdgeLineWidth = 1;

    /** The Ellipse on select. */
    @XmlElement(name = "ellipseOnSelect")
    private boolean myEllipseOnSelect;

    /** The Fill style. */
    @XmlElement(name = "fillStyle")
    private String myFillStyle = EllipseFillStyle.NO_FILL.toString();

    /** The Orientation column. */
    @XmlElement(name = "orientationColumn")
    private String myOrientationColumn;

    /** The Axis units. */
    @XmlElement(name = "rimFade")
    private int myRimFade = 50;

    /** The Semi major column. */
    @XmlElement(name = "semiMajorColumn")
    private String mySemiMajorColumn = "NONE";

    /** The Semi minor column. */
    @XmlElement(name = "semiMinorColumn")
    private String mySemiMinorColumn;

    /**
     * Gets the axis units.
     *
     * @return the axis units
     */
    public String getAxisUnits()
    {
        return myAxisUnits;
    }

    /**
     * Gets the center point size.
     *
     * @return the center point size
     */
    public int getCenterPointSize()
    {
        return myCenterPointSize;
    }

    /**
     * Gets the edge line width.
     *
     * @return the edge line width
     */
    public int getEdgeLineWidth()
    {
        return myEdgeLineWidth;
    }

    /**
     * Gets the fill style.
     *
     * @return the fill style
     */
    public String getFillStyle()
    {
        return myFillStyle;
    }

    /**
     * Gets the orientation column.
     *
     * @return the orientation column
     */
    public String getOrientationColumn()
    {
        return myOrientationColumn;
    }

    /**
     * Get the rimFade.
     *
     * @return the rimFade
     */
    public int getRimFade()
    {
        return myRimFade;
    }

    /**
     * Gets the semi major column.
     *
     * @return the semi major column
     */
    public String getSemiMajorColumn()
    {
        return mySemiMajorColumn;
    }

    /**
     * Gets the semi minor column.
     *
     * @return the semi minor column
     */
    public String getSemiMinorColumn()
    {
        return mySemiMinorColumn;
    }

    /**
     * Checks if is center point.
     *
     * @return true, if is center point
     */
    public boolean isCenterPoint()
    {
        return myCenterPoint;
    }

    /**
     * Checks if is edge line.
     *
     * @return true, if is edge line
     */
    public boolean isEdgeLine()
    {
        return myEdgeLine;
    }

    /**
     * Checks if is ellipse on select.
     *
     * @return true, if is ellipse on select
     */
    public boolean isEllipseOnSelect()
    {
        return myEllipseOnSelect;
    }

    /**
     * Sets the axis units.
     *
     * @param axisUnits the new axis units
     */
    public void setAxisUnits(String axisUnits)
    {
        myAxisUnits = axisUnits;
    }

    /**
     * Sets the center point.
     *
     * @param centerPoint the new center point
     */
    public void setCenterPoint(boolean centerPoint)
    {
        myCenterPoint = centerPoint;
    }

    /**
     * Sets the center point size.
     *
     * @param centerPointSize the new center point size
     */
    public void setCenterPointSize(int centerPointSize)
    {
        myCenterPointSize = centerPointSize;
    }

    /**
     * Sets the edge line.
     *
     * @param edgeLine the new edge line
     */
    public void setEdgeLine(boolean edgeLine)
    {
        myEdgeLine = edgeLine;
    }

    /**
     * Sets the edge line width.
     *
     * @param edgeLineWidth the new edge line width
     */
    public void setEdgeLineWidth(int edgeLineWidth)
    {
        myEdgeLineWidth = edgeLineWidth;
    }

    /**
     * Sets the ellipse on select.
     *
     * @param ellipseOnSelect the new ellipse on select
     */
    public void setEllipseOnSelect(boolean ellipseOnSelect)
    {
        myEllipseOnSelect = ellipseOnSelect;
    }

    /**
     * Sets the fill style.
     *
     * @param fillStyle the new fill style
     */
    public void setFillStyle(String fillStyle)
    {
        myFillStyle = fillStyle;
    }

    /**
     * Sets the orientation column.
     *
     * @param orientationColumn the new orientation column
     */
    public void setOrientationColumn(String orientationColumn)
    {
        myOrientationColumn = orientationColumn;
    }

    /**
     * Set the rimFade.
     *
     * @param rimFade the rimFade to set
     */
    public void setRimFade(int rimFade)
    {
        myRimFade = rimFade;
    }

    /**
     * Sets the semi major column.
     *
     * @param semiMajorColumn the new semi major column
     */
    public void setSemiMajorColumn(String semiMajorColumn)
    {
        mySemiMajorColumn = semiMajorColumn;
    }

    /**
     * Sets the semi minor column.
     *
     * @param semiMinorColumn the new semi minor column
     */
    public void setSemiMinorColumn(String semiMinorColumn)
    {
        mySemiMinorColumn = semiMinorColumn;
    }
}
