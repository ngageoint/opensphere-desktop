package io.opensphere.core.geometry.renderproperties;

/** Standard implementation of {@link LOBRenderProperties}. */
public class DefaultLOBRenderProperties extends DefaultPolylineRenderProperties implements LOBRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The line length (meters). */
    private float myLineLength;

    /** The directional arrow line length (meters). */
    private float myDirectionalArrowLength;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     */
    public DefaultLOBRenderProperties(int zOrder, boolean drawable, boolean pickable)
    {
        super(zOrder, drawable, pickable);
        myLineLength = 5;
        myDirectionalArrowLength = 1;
    }

    @Override
    public DefaultLOBRenderProperties clone()
    {
        return (DefaultLOBRenderProperties)super.clone();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultLOBRenderProperties other = (DefaultLOBRenderProperties)obj;
        return myLineLength == other.myLineLength && myDirectionalArrowLength == other.myDirectionalArrowLength;
    }

    @Override
    public float getDirectionalArrowLength()
    {
        return myDirectionalArrowLength;
    }

    @Override
    public float getLineLength()
    {
        return myLineLength;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myLineLength);
        result = prime * result + Float.floatToIntBits(myDirectionalArrowLength);
        return result;
    }

    @Override
    public void setDirectionalArrowLength(float length)
    {
        myDirectionalArrowLength = length;
    }

    @Override
    public void setLineLength(float length)
    {
        myLineLength = length;
    }
}
