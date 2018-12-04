package io.opensphere.csvcommon.parse;

import java.awt.Color;
import java.util.Date;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.util.Utilities;

/**
 * Bean for extracting data from each CSV Line.
 */
public class PointExtract
{
    /** alt. */
    private Double myAlt;

    /** date. */
    private Date myDate;

    /** The Down date. */
    private Date myDownDate;

    /** lat. */
    private Double myLat;

    /** lob. */
    private Double myLob;

    /** lon. */
    private Double myLon;

    /** ornt. */
    private Double myOrientation;

    /** The radius. */
    private Double myRadius;

    /** sma. */
    private Double mySma;

    /** smi. */
    private Double mySmi;

    /** The WKT geometry. */
    private Geometry myWKTGeometry;

    /** The color assigned to the point. */
    private Color myColor;

    /**
     * Gets the alt.
     *
     * @return the alt
     */
    public Double getAlt()
    {
        return myAlt;
    }

    /**
     * Sets the alt.
     *
     * @param alt the alt
     */
    public void setAlt(Double alt)
    {
        myAlt = alt;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate()
    {
        return Utilities.clone(myDate);
    }

    /**
     * Gets the down date.
     *
     * @return the down date
     */
    public Date getDownDate()
    {
        return Utilities.clone(myDownDate);
    }

    /**
     * Gets the lat.
     *
     * @return the lat
     */
    public Double getLat()
    {
        return myLat;
    }

    /**
     * Gets the lob.
     *
     * @return the lob
     */
    public Double getLob()
    {
        return myLob;
    }

    /**
     * Gets the lon.
     *
     * @return the lon
     */
    public Double getLon()
    {
        return myLon;
    }

    /**
     * Gets the orientation.
     *
     * @return the orientation
     */
    public Double getOrientation()
    {
        return myOrientation;
    }

    /**
     * Get the radius.
     *
     * @return the radius
     */
    public Double getRadius()
    {
        return myRadius;
    }

    /**
     * Gets the sma.
     *
     * @return the sma
     */
    public Double getSma()
    {
        return mySma;
    }

    /**
     * Gets the smi.
     *
     * @return the smi
     */
    public Double getSmi()
    {
        return mySmi;
    }

    /**
     * Gets the WKT geometry.
     *
     * @return the WKT geometry
     */
    public Geometry getWKTGeometry()
    {
        return myWKTGeometry;
    }

    /**
     * Sets the date.
     *
     * @param date the new date
     */
    public void setDate(Date date)
    {
        myDate = Utilities.clone(date);
    }

    /**
     * Sets the down date.
     *
     * @param downDate the new down date
     */
    public void setDownDate(Date downDate)
    {
        myDownDate = Utilities.clone(downDate);
    }

    /**
     * Sets the lat.
     *
     * @param lat the new lat
     */
    public void setLat(Double lat)
    {
        myLat = lat;
    }

    /**
     * Sets the lob.
     *
     * @param lob the new lob
     */
    public void setLob(Double lob)
    {
        myLob = lob;
    }

    /**
     * Sets the lon.
     *
     * @param lon the new lon
     */
    public void setLon(Double lon)
    {
        myLon = lon;
    }

    /**
     * Sets the orientation.
     *
     * @param orientation the new orientation
     */
    public void setOrientation(Double orientation)
    {
        myOrientation = orientation;
    }

    /**
     * Set the radius.
     *
     * @param rad the radius to set
     */
    public void setRadius(Double rad)
    {
        myRadius = rad;
    }

    /**
     * Sets the sma.
     *
     * @param sma the new sma
     */
    public void setSma(Double sma)
    {
        mySma = sma;
    }

    /**
     * Sets the smi.
     *
     * @param smi the new smi
     */
    public void setSmi(Double smi)
    {
        mySmi = smi;
    }

    /**
     * Sets the wKT geometry.
     *
     * @param wKTGeometry the new wKT geometry
     */
    public void setWKTGeometry(Geometry wKTGeometry)
    {
        myWKTGeometry = wKTGeometry;
    }

    /**
     * Sets the value of the {@link #myColor} field.
     *
     * @param color the value to store in the {@link #myColor} field.
     */
    public void setColor(Color color)
    {
        myColor = color;
    }

    /**
     * Gets the value of the {@link #myColor} field.
     *
     * @return the value stored in the {@link #myColor} field.
     */
    public Color getColor()
    {
        return myColor;
    }
}
