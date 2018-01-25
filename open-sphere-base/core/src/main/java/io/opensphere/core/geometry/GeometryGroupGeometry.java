package io.opensphere.core.geometry;

import java.util.Collection;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Position;

/**
 * Render a set of geometries together as a single unit.
 */
public class GeometryGroupGeometry extends AbstractGeometryGroup
{
    /** The Position type to be used for the geometries in this group. */
    private final Class<? extends Position> mySubGeometryType;

    /**
     * Construct me.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     */
    public GeometryGroupGeometry(Builder builder, ZOrderRenderProperties renderProperties)
    {
        super(builder, renderProperties);
        mySubGeometryType = builder.getSubGeometryType();
        for (Geometry geom : builder.getInitialGeometries())
        {
            if (!mySubGeometryType.isAssignableFrom(geom.getPositionType()))
            {
                throw new IllegalArgumentException("Wrong position type for group: " + geom);
            }
        }
        addInitialGeometries(builder.getInitialGeometries());
    }

    @Override
    public GeometryGroupGeometry clone()
    {
        return (GeometryGroupGeometry)super.clone();
    }

    @Override
    public Builder createBuilder()
    {
        return (Builder)super.doCreateBuilder();
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return mySubGeometryType;
    }

    @Override
    public synchronized void receiveObjects(Object source, Collection<? extends Geometry> adds,
            Collection<? extends Geometry> removes)
    {
        for (Geometry add : adds)
        {
            if (!mySubGeometryType.isAssignableFrom(add.getPositionType()))
            {
                throw new IllegalArgumentException("Wrong position type for group: " + add);
            }
        }
        super.receiveObjects(source, adds, removes);
    }

    @Override
    protected Builder createRawBuilder()
    {
        return new Builder(mySubGeometryType);
    }

    /**
     * Builder for the geometry.
     */
    public static class Builder extends AbstractGeometryGroup.Builder
    {
        /** The Position type to be used for the geometries in this group. */
        private final Class<? extends Position> mySubGeometryType;

        /**
         * Constructor.
         *
         * @param subGeomType The Position type to be used for the geometries in
         *            this group.
         */
        public Builder(Class<? extends Position> subGeomType)
        {
            mySubGeometryType = subGeomType;
        }

        /**
         * Get the subGeometryType.
         *
         * @return the subGeometryType
         */
        public Class<? extends Position> getSubGeometryType()
        {
            return mySubGeometryType;
        }
    }
}
