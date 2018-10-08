package io.opensphere.core.geometry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A spiritual extension to {@link GeometryGroupGeometry} that is implemented to
 * specifically extend {@link PolygonGeometry}, allowing it to be used anywhere
 * a {@link PolygonGeometry} can be used.
 */
public class MultiPolygonGeometry extends PolygonGeometry
{
    /** Geometries to render together as a set. */
    private GeometryRegistry myGeometryRegistry;

    /**
     * Geometries to draw with the initial publishing of the group. This set is
     * only used until the geometry registry has been created.
     */
    private Collection<PolygonGeometry> myInitialGeometries = new HashSet<>();

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public MultiPolygonGeometry(MultiPolygonGeometry.Builder<?> builder, PolygonRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        myInitialGeometries.addAll(builder.getInitialGeometries());
    }

    @Override
    public MultiPolygonGeometry clone()
    {
        MultiPolygonGeometry clone = (MultiPolygonGeometry)super.clone();

        clone.myGeometryRegistry = null;
        Collection<PolygonGeometry> geometries = getGeometries();
        clone.myInitialGeometries = New.collection(geometries.size());

        for (PolygonGeometry geom : geometries)
        {
            clone.myInitialGeometries.add(geom.clone());
        }

        return clone;
    }

    /**
     * Get the geometries in the processor.
     *
     * @return The child geometries contained within the multi-geometry.
     * @throws UnsupportedOperationException if the registry contains geometries
     *             of a type not descendant from {@link PolygonGeometry}.
     */
    public synchronized Collection<PolygonGeometry> getGeometries()
    {
        if (myGeometryRegistry == null)
        {
            return myInitialGeometries;
        }
        return readAllGeometriesFromRegistry();
    }

    /**
     * Reads all geometries from the registry, and converts them to
     * {@link PolygonGeometry}s on the fly.
     *
     * @return The child geometries contained within the registry.
     * @throws UnsupportedOperationException if the registry contains geometries
     *             of a type not descendant from {@link PolygonGeometry}.
     */
    private Collection<PolygonGeometry> readAllGeometriesFromRegistry()
    {
        Collection<Geometry> geometries = myGeometryRegistry.getGeometries();
        if (!geometries.stream().allMatch(g -> g instanceof PolygonGeometry))
        {
            throw new UnsupportedOperationException("Multipolygon geometry contained one or more non-polygon-geometry children.");
        }

        return geometries.stream().map(g -> (PolygonGeometry)g).collect(Collectors.toList());
    }

    /**
     * Get the geometries. Plugins should not add geometries directly to the
     * registry.
     *
     * @return the geometries
     */
    public synchronized GeometryRegistry getGeometryRegistry()
    {
        return myGeometryRegistry;
    }

    @Override
    public Position getReferencePoint()
    {
        return new ModelPosition(0., 0., 0.);
    }

    @Override
    public <T extends Position> boolean overlaps(BoundingBox<T> boundingBox, double tolerance)
    {
        for (Geometry geom : getGeometries())
        {
            if (geom.overlaps(boundingBox, tolerance))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Receive objects.
     *
     * @param source The source of the objects.
     * @param adds The added objects.
     * @param removes The removed objects.
     */
    public synchronized void receiveObjects(Object source, Collection<? extends PolygonGeometry> adds,
            Collection<? extends PolygonGeometry> removes)
    {
        if (myGeometryRegistry == null)
        {
            myInitialGeometries.addAll(adds);
            myInitialGeometries.removeAll(removes);
        }
        else
        {
            myGeometryRegistry.receiveObjects(source, adds, removes);
        }
    }

    /**
     * Set the geometry registry.
     *
     * @param registry the geometry registry to set
     */
    protected synchronized void setGeometryRegistry(GeometryRegistry registry)
    {
        if (Utilities.sameInstance(registry, myGeometryRegistry))
        {
            return;
        }
        else if (myGeometryRegistry != null)
        {
            if (myInitialGeometries != null)
            {
                throw new IllegalStateException("An old geometry registry exists and initial geometries are not null.");
            }
            myInitialGeometries = readAllGeometriesFromRegistry();
            myGeometryRegistry.removeGeometriesForSource(this);
        }
        else if (myInitialGeometries == null)
        {
            throw new IllegalStateException("Initial geometries have already been nulled.");
        }
        myGeometryRegistry = registry;
        myGeometryRegistry.addGeometriesForSource(this, myInitialGeometries);
        myInitialGeometries = null;
    }

    /**
     * Add the initial geometries.
     *
     * @param geometries The geometries.
     */
    protected final synchronized void addInitialGeometries(Collection<? extends PolygonGeometry> geometries)
    {
        if (myInitialGeometries == null)
        {
            throw new IllegalStateException("Cannot add initial geometries after the geometry distributor has been set.");
        }
        myInitialGeometries.addAll(geometries);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.geometry.PolygonGeometry#createRawBuilder()
     */
    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<>(GeographicPosition.class);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.geometry.PolygonGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties,
     *      io.opensphere.core.geometry.constraint.Constraints)
     */
    @Override
    public PolygonGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new MultiPolygonGeometry(createBuilder(), (PolygonRenderProperties)renderProperties, constraints);
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.createBuilder();
        builder.setInitialGeometries(myInitialGeometries);
        return builder;
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends PolygonGeometry.Builder<T>
    {
        /** The Position type to be used for the geometries in this group. */
        private final Class<T> mySubGeometryType;

        /** Geometries which will initially be rendered for the group. */
        private final Set<PolygonGeometry> myInitialGeometries = new HashSet<>();

        /**
         * Constructor.
         *
         * @param subGeomType The Position type to be used for the geometries in
         *            this group.
         */
        public Builder(Class<T> subGeomType)
        {
            mySubGeometryType = subGeomType;
        }

        /**
         * Get the subGeometryType.
         *
         * @return the subGeometryType
         */
        public Class<T> getSubGeometryType()
        {
            return mySubGeometryType;
        }

        /**
         * Gets the value of the {@link #myInitialGeometries} field.
         *
         * @return the value stored in the {@link #myInitialGeometries} field.
         */
        public Set<PolygonGeometry> getInitialGeometries()
        {
            return myInitialGeometries;
        }

        /**
         * Set the initialGeometries.
         *
         * @param initialGeometries the initialGeometries to set
         */
        public void setInitialGeometries(Collection<PolygonGeometry> initialGeometries)
        {
            myInitialGeometries.clear();
            if (initialGeometries != null)
            {
                myInitialGeometries.addAll(initialGeometries);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see io.opensphere.core.geometry.PolylineGeometry.Builder#getVertices()
         */
        @SuppressWarnings("unchecked")
        @Override
        public List<? extends T> getVertices()
        {
            List<T> vertices = New.list();

            for (PolygonGeometry childGeometry : myInitialGeometries)
            {
                List<? extends Position> childVertices = childGeometry.getVertices();

                for (Position position : childVertices)
                {
                    vertices.add((T)position);
                }
            }

            return vertices;
        }
    }
}
