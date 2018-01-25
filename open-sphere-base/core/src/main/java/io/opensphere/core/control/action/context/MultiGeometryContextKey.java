package io.opensphere.core.control.action.context;

import java.util.Collection;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.New;

/** The context key for actions against multiple geometries. */
public class MultiGeometryContextKey
{
    /** The geometries which this key represents. */
    private final Collection<? extends Geometry> myGeometries;

    /**
     * Constructor.
     *
     * @param geoms The geometries which this key represents.
     */
    public MultiGeometryContextKey(Collection<? extends Geometry> geoms)
    {
        myGeometries = New.unmodifiableCollection(geoms);
    }

    /**
     * Get the geometries.
     *
     * @return the geometries
     */
    public Collection<? extends Geometry> getGeometries()
    {
        return myGeometries;
    }
}
