package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.List;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.ColorArrayList;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * A model for a set of points drawn on the screen that have positions and
 * colors. All points in the set must have the same position type and the same
 * size. Constraints are not supported for individual points within the set,
 * only constraints which apply to the entire set may be used.
 */
public class PointSetGeometry extends AbstractColorGeometry implements PointRenderPropertiesGeometry
{
    /**
     * The colors for the points in the set. This may either be one color for
     * each point or null.
     */
    private final List<? extends Color> myColors;

    /** The positions in the point set. */
    private final List<? extends Position> myPositions;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public PointSetGeometry(PointSetGeometry.Builder<?> builder, PointRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        if (!CollectionUtilities.hasContent(builder.getPositions()))
        {
            throw new IllegalArgumentException("A PointSetGeometry cannot be constructed using a builder with no positions.");
        }
        myPositions = builder.getPositions();
        myColors = ColorArrayList.getColorArrayList(builder.getColors());
    }

    @Override
    public PointSetGeometry clone()
    {
        return (PointSetGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.doCreateBuilder();
        builder.setPositions(getPositions());
        builder.setColors(getColors());
        return builder;
    }

    @Override
    public PointSetGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new PointSetGeometry(createBuilder(), (PointRenderProperties)renderProperties, constraints);
    }

    /**
     * Get the colors.
     *
     * @return the colors
     */
    public List<? extends Color> getColors()
    {
        return myColors;
    }

    /**
     * Accessor for the position.
     *
     * @return the position
     */
    public final List<? extends Position> getPositions()
    {
        return myPositions;
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return myPositions.get(0).getClass();
    }

    @Override
    public Position getReferencePoint()
    {
        return CollectionUtilities.getItem(getPositions(), 0);
    }

    @Override
    public PointRenderProperties getRenderProperties()
    {
        return (PointRenderProperties)super.getRenderProperties();
    }

    @Override
    public boolean jtsIntersectionTests(JTSIntersectionTests test, List<Polygon> polygons, GeometryFactory geomFactory)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Position> boolean overlaps(BoundingBox<T> boundingBox, double tolerance)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append(" [").append(getPositions().size()).append(" positions]");
        return sb.toString();
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends AbstractGeometry.Builder
    {
        /**
         * The colors for the points in the set. This may either be one color
         * for each point or null.
         */
        private List<? extends Color> myColors;

        /** The positions in the point set. */
        private List<? extends T> myPositions;

        /**
         * Get the colors.
         *
         * @return the colors
         */
        public List<? extends Color> getColors()
        {
            return myColors;
        }

        /**
         * Accessor for the positions.
         *
         * @return The positions.
         */
        public List<? extends T> getPositions()
        {
            return myPositions;
        }

        /**
         * Set the colors.
         *
         * @param colors the colors to set
         */
        public void setColors(List<? extends Color> colors)
        {
            myColors = colors;
        }

        /**
         * Set the positions of the points.
         *
         * @param positions The positions to set.
         */
        public void setPositions(List<? extends T> positions)
        {
            myPositions = New.unmodifiableList(positions);
        }
    }
}
