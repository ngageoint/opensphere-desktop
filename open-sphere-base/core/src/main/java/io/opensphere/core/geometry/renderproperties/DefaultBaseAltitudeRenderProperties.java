package io.opensphere.core.geometry.renderproperties;

/** Default implementation of {@link BaseAltitudeRenderProperties}. */
public class DefaultBaseAltitudeRenderProperties extends DefaultPolygonMeshRenderProperties
implements BaseAltitudeRenderProperties
{
    /** The serial id. */
    private static final long serialVersionUID = 1L;

    /** Base altitude of the geometry. */
    private volatile float myBaseAltitude;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     */
    public DefaultBaseAltitudeRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant)
    {
        super(zOrder, drawable, pickable, obscurant);
    }

    @Override
    public DefaultBaseAltitudeRenderProperties clone()
    {
        return (DefaultBaseAltitudeRenderProperties)super.clone();
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
        DefaultBaseAltitudeRenderProperties other = (DefaultBaseAltitudeRenderProperties)obj;
        return myBaseAltitude == other.myBaseAltitude;
    }

    @Override
    public float getBaseAltitude()
    {
        return myBaseAltitude;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myBaseAltitude);
        return result;
    }

    @Override
    public void setBaseAltitude(float baseAlt)
    {
        myBaseAltitude = baseAlt;
        notifyChanged();
    }
}
