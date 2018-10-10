package io.opensphere.core.cache.accessor;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Geometry property accessor.
 *
 * @param <S> The type of object that provides the geometries.
 */
public abstract class GeometryAccessor<S> extends AbstractIntervalPropertyAccessor<S, Geometry>
implements PersistentPropertyAccessor<S, Geometry>
{
    /** The standard geometry property name. */
    public static final String GEOMETRY_PROPERTY_NAME = "geom";

    /** The property descriptor. */
    public static final PropertyDescriptor<Geometry> PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(
            GEOMETRY_PROPERTY_NAME, Geometry.class);

    /** A geometry factory. */
    private final GeometryFactory myGeometryFactory = new GeometryFactory();

    /**
     * Construct the geometry accessor.
     *
     * @param extent A geometry that comprises all of the geometries provided by
     *            this accessor.
     */
    public GeometryAccessor(Geometry extent)
    {
        super(extent);
    }

    @Override
    public IntervalPropertyMatcher<?> createMatcher()
    {
        return new GeometryMatcher(GEOMETRY_PROPERTY_NAME, GeometryMatcher.OperatorType.EQUALS, getExtent());
    }

    @Override
    public PropertyDescriptor<Geometry> getPropertyDescriptor()
    {
        return PROPERTY_DESCRIPTOR;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(GeometryAccessor.class.getSimpleName()).append('[').append(getPropertyDescriptor())
                .append(']').toString();
    }

    /**
     * Accessor for the geometry factory to be used by concrete implementations.
     *
     * @return The geometry factory.
     */
    protected GeometryFactory getGeometryFactory()
    {
        return myGeometryFactory;
    }
}
