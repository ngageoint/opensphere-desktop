package io.opensphere.core.math;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import io.opensphere.core.util.JAXBWrapper;
import io.opensphere.core.util.JAXBableXMLAdapter;

/** Mutable Vector3d for JAXB. */
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableVector3d implements JAXBWrapper<Vector3d>
{
    /** The x. */
    private double myX;

    /** The y. */
    private double myY;

    /** The z. */
    private double myZ;

    /**
     * Constructor that takes a vector.
     *
     * @param v The vector.
     */
    public MutableVector3d(Vector3d v)
    {
        myX = v.getX();
        myY = v.getY();
        myZ = v.getZ();
    }

    /** Constructor for JAXB. */
    protected MutableVector3d()
    {
    }

    @Override
    public Vector3d getWrappedObject()
    {
        return new Vector3d(myX, myY, myZ);
    }

    /**
     * Mutator for the x.
     *
     * @param x The x to set.
     */
    protected void setX(double x)
    {
        myX = x;
    }

    /**
     * Mutator for the y.
     *
     * @param y The y to set.
     */
    protected void setY(double y)
    {
        myY = y;
    }

    /**
     * Mutator for the z.
     *
     * @param z The z to set.
     */
    protected void setZ(double z)
    {
        myZ = z;
    }

    /**
     * XML adapter that converts {@link Vector3d} to and from
     * {@link MutableVector3d}.
     */
    public static class Vector3dAdapter extends JAXBableXMLAdapter<MutableVector3d, Vector3d>
    {
    }
}
