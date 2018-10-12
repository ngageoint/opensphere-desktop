package io.opensphere.core.geometry;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.Position;

/**
 * A model of a point drawn on the screen that has position, color and size.
 */
public class PointGeometry extends AbstractColorGeometry implements PointRenderPropertiesGeometry
{
    /** The position of this point. */
    private final Position myPosition;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public PointGeometry(PointGeometry.Builder<?> builder, PointRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        // Attempt to reuse the Position to save memory
        float baseAltitude = getRenderProperties().getBaseAltitude();
        myPosition = baseAltitude == 0 ? builder.getPosition() : builder.getPosition().add(new Vector3d(0, 0, baseAltitude));
    }

    @Override
    public PointGeometry clone()
    {
        return (PointGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.doCreateBuilder();
        builder.setPosition(getPosition());
        return builder;
    }

    @Override
    public PointGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new PointGeometry(createBuilder(), (PointRenderProperties)renderProperties, constraints);
    }

    /**
     * Accessor for the position.
     *
     * @return the position
     */
    public final Position getPosition()
    {
        return myPosition;
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return myPosition.getClass();
    }

    @Override
    public Position getReferencePoint()
    {
        return myPosition;
    }

    @Override
    public PointRenderProperties getRenderProperties()
    {
        return (PointRenderProperties)super.getRenderProperties();
    }

    @Override
    public boolean jtsIntersectionTests(JTSIntersectionTests test, List<Polygon> polygons, GeometryFactory geomFactory)
    {
        JTSIntersectionTests tests = test == null ? ALL_INTERSECTION_TESTS : test;
        boolean containedByOrIntersects = false;
        if (polygons != null && !polygons.isEmpty() && !getRenderProperties().isHidden())
        {
            Vector3d vec = myPosition.asVector3d();
            Point aPoint = geomFactory.createPoint(new Coordinate(vec.getX(), vec.getY()));
            for (Polygon aPolygon : polygons)
            {
                containedByOrIntersects = tests.isContains() && aPolygon.contains(aPoint)
                        || tests.isIntersects() && aPolygon.intersects(aPoint) || tests.isOverlaps() && aPolygon.overlaps(aPoint);
                if (containedByOrIntersects)
                {
                    break;
                }
            }
        }
        return containedByOrIntersects;
    }

    @Override
    public <T extends Position> boolean overlaps(BoundingBox<T> boundingBox, double tolerance)
    {
        return boundingBox.contains(getPosition(), tolerance);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append(" [").append(getPosition()).append(']');
        return sb.toString();
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<>();
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends AbstractGeometry.Builder
    {
        /** The position of this point. */
        private T myPosition;

        /**
         * Accessor for the position.
         *
         * @return The position.
         */
        public T getPosition()
        {
            return myPosition;
        }

        /**
         * Set the position of the point.
         *
         * @param position The position to set.
         */
        public void setPosition(T position)
        {
            myPosition = position;
        }
    }
}
