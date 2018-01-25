package io.opensphere.core.viewbookmark.config.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.util.Utilities;

/**
 * The Class JAXBVector3d.
 */
@XmlRootElement(name = "Vector3D")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBVector3d
{
    /** The X. */
    @XmlAttribute(name = "x")
    private double myX;

    /** The Y. */
    @XmlAttribute(name = "y")
    private double myY;

    /** The Z. */
    @XmlAttribute(name = "z")
    private double myZ;

    /**
     * Instantiates a new jAXB vector3d.
     */
    public JAXBVector3d()
    {
    }

    /**
     * Instantiates a new jAXB vector3d.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public JAXBVector3d(double x, double y, double z)
    {
        myX = x;
        myY = y;
        myZ = z;
    }

    /**
     * Instantiates a new jAXB vector3d.
     *
     * @param vec the {@link Vector3d}
     */
    public JAXBVector3d(Vector3d vec)
    {
        myX = vec.getX();
        myY = vec.getY();
        myZ = vec.getZ();
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
        JAXBVector3d other = (JAXBVector3d)obj;
        return Utilities.equalsOrBothNaN(myX, other.myX) && Utilities.equalsOrBothNaN(myY, other.myY)
                && Utilities.equalsOrBothNaN(myZ, other.myZ);
    }

    /**
     * Gets the vector3d.
     *
     * @return the vector3d
     */
    public Vector3d getVector3d()
    {
        return new Vector3d(myX, myY, myZ);
    }

    /**
     * Gets the x.
     *
     * @return the x
     */
    public final double getX()
    {
        return myX;
    }

    /**
     * Gets the y.
     *
     * @return the y
     */
    public final double getY()
    {
        return myY;
    }

    /**
     * Gets the z.
     *
     * @return the z
     */
    public final double getZ()
    {
        return myZ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myX);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myY);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myZ);
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    /**
     * Sets the x.
     *
     * @param x the new x
     */
    public final void setX(double x)
    {
        myX = x;
    }

    /**
     * Sets the y.
     *
     * @param y the new y
     */
    public final void setY(double y)
    {
        myY = y;
    }

    /**
     * Sets the z.
     *
     * @param z the new z
     */
    public final void setZ(double z)
    {
        myZ = z;
    }
}
