package io.opensphere.core.control.action.context;

import io.opensphere.core.geometry.Geometry;

/** The context key for actions against a geometry. */
public class GeometryContextKey
{
    /** The geometry which this key represents. */
    private final Geometry myGeometry;

    /**
     * Constructor.
     *
     * @param geom The geometry which this key represents.
     */
    public GeometryContextKey(Geometry geom)
    {
        myGeometry = geom;
    }

    /**
     * Get the geometry.
     *
     * @return the geometry
     */
    public Geometry getGeometry()
    {
        return myGeometry;
    }
}
