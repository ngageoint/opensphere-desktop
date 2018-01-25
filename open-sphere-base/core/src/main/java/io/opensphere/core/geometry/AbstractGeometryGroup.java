package io.opensphere.core.geometry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Render a set of geometries together as a single unit.
 */
public abstract class AbstractGeometryGroup extends AbstractGeometry
{
    /** Geometries to render together as a set. */
    private GeometryRegistry myGeometryRegistry;

    /**
     * Geometries to draw with the initial publishing of the group. This set is
     * only used until the geometry registry has been created.
     */
    private Collection<Geometry> myInitialGeometries = new HashSet<>();

    /**
     * Constructor.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     */
    public AbstractGeometryGroup(Builder builder, ZOrderRenderProperties renderProperties)
    {
        super(builder, renderProperties);
    }

    @Override
    public AbstractGeometryGroup clone()
    {
        AbstractGeometryGroup clone = (AbstractGeometryGroup)super.clone();

        clone.myGeometryRegistry = null;
        Collection<Geometry> geometries = getGeometries();
        clone.myInitialGeometries = New.collection(geometries.size());

        for (Geometry geom : geometries)
        {
            clone.myInitialGeometries.add(geom.clone());
        }

        return clone;
    }

    /**
     * Get the geometries in the processor.
     *
     * @return The geometries.
     */
    public synchronized Collection<Geometry> getGeometries()
    {
        return myGeometryRegistry == null ? myInitialGeometries : myGeometryRegistry.getGeometries();
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
    public synchronized void receiveObjects(Object source, Collection<? extends Geometry> adds,
            Collection<? extends Geometry> removes)
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
    public synchronized void setGeometryRegistry(GeometryRegistry registry)
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
            myInitialGeometries = myGeometryRegistry.getGeometries();
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
    protected final synchronized void addInitialGeometries(Collection<? extends Geometry> geometries)
    {
        if (myInitialGeometries == null)
        {
            throw new IllegalStateException("Cannot add initial geometries after the geometry distributor has been set.");
        }
        myInitialGeometries.addAll(geometries);
    }

    @Override
    protected Builder createRawBuilder()
    {
        return new Builder();
    }

    /**
     * Builder for the geometry.
     */
    public static class Builder extends AbstractGeometry.Builder
    {
        /** Geometries which will initially be rendered for the group. */
        private final Set<Geometry> myInitialGeometries = new HashSet<>();

        /**
         * Get the initialGeometries.
         *
         * @return the initialGeometries
         */
        public Set<Geometry> getInitialGeometries()
        {
            return myInitialGeometries;
        }

        /**
         * Set the initialGeometries.
         *
         * @param initialGeometries the initialGeometries to set
         */
        public void setInitialGeometries(Collection<Geometry> initialGeometries)
        {
            myInitialGeometries.clear();
            if (initialGeometries != null)
            {
                myInitialGeometries.addAll(initialGeometries);
            }
        }
    }
}
