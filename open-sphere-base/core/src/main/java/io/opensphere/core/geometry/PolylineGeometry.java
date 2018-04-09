package io.opensphere.core.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.BoundingBoxes;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;

/**
 * A {@link Geometry} that models a series of connected line segments.
 */
@SuppressWarnings("PMD.GodClass")
public class PolylineGeometry extends AbstractColorGeometry
{
    /** When true, line smoothing will be used. */
    // TODO this should be a mutable render property
    private final boolean myLineSmoothing;

    /** The type of line. */
    // TODO this might be render property
    private final LineType myLineType;

    /** The vertices between the line segments. */
    private List<? extends Position> myVertices;

    /**
     * Construct the geometry.
     *
     * @param builder the builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     * @throws IllegalArgumentException when the vertices cannot be used to
     *             build a valid polyline.
     */
    public PolylineGeometry(PolylineGeometry.Builder<?> builder, PolylineRenderProperties renderProperties,
            Constraints constraints)
        throws IllegalArgumentException
    {
        super(builder, renderProperties, constraints);
        if (renderProperties.getWidth() < 0.)
        {
            throw new IllegalArgumentException("Width cannot be < 0.");
        }

        setVertices(builder.getVertices());
        myLineSmoothing = builder.isLineSmoothing();
        myLineType = builder.getLineType();
    }

    /**
     * Special constructor for subclasses.
     *
     * @param builder The abstract geometry builder.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     * @param smooth When true, line smoothing is used.
     * @param type Type type of line.
     */
    protected PolylineGeometry(AbstractGeometry.Builder builder, PolylineRenderProperties renderProperties,
            Constraints constraints, boolean smooth, LineType type)
    {
        super(builder, renderProperties, constraints);

        if (renderProperties.getWidth() <= 0.)
        {
            throw new IllegalArgumentException("Width cannot be <= 0.");
        }

        myLineSmoothing = smooth;
        myLineType = type;
    }

    /**
     * Are any coordinates overlapping or intersecting.
     *
     * @param polygons the polygons
     * @param geomFactory the geom factory
     * @param coords the coords
     * @return true, if successful
     */
    public boolean areAnyCoordinatesOverlappingOrIntersecting(List<Polygon> polygons, GeometryFactory geomFactory,
            Coordinate[] coords)
    {
        boolean intersects = false;
        // Last ditch effort, just check the points in the polyline to
        // see if they individually
        // are contained or intersect the selection polygons.
        for (Coordinate c : coords)
        {
            Point aPoint = geomFactory.createPoint(c);
            for (Polygon aPolygon : polygons)
            {
                intersects = aPolygon.contains(aPoint) || aPolygon.intersects(aPoint);
                if (intersects)
                {
                    break;
                }
            }
            if (intersects)
            {
                break;
            }
        }
        return intersects;
    }

    @Override
    public PolylineGeometry clone()
    {
        return (PolylineGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.doCreateBuilder();
        builder.setVertices(getVertices());
        builder.setLineSmoothing(isLineSmoothing());
        builder.setLineType(getLineType());
        return builder;
    }

    @Override
    public PolylineGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new PolylineGeometry(createBuilder(), (PolylineRenderProperties)renderProperties, constraints);
    }

    /**
     * Get the lineType.
     *
     * @return the lineType
     */
    public LineType getLineType()
    {
        return myLineType;
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return myVertices.get(0).getClass();
    }

    @Override
    public Position getReferencePoint()
    {
        return myVertices.get(0);
    }

    @Override
    public PolylineRenderProperties getRenderProperties()
    {
        return (PolylineRenderProperties)super.getRenderProperties();
    }

    /**
     * Get the vertices between the line segments.
     *
     * @return the vertices
     */
    public List<? extends Position> getVertices()
    {
        return myVertices;
    }

    /**
     * Get the lineSmoothing.
     *
     * @return the lineSmoothing
     */
    public boolean isLineSmoothing()
    {
        return myLineSmoothing;
    }

    @Override
    public boolean jtsIntersectionTests(JTSIntersectionTests test, List<Polygon> polygons, GeometryFactory geomFactory)
    {
        JTSIntersectionTests tests = test == null ? ALL_INTERSECTION_TESTS : test;
        boolean intersects = false;
        if (!getRenderProperties().isHidden() && polygons != null && !polygons.isEmpty() && !myVertices.isEmpty())
        {
            Coordinate[] coords = new Coordinate[myVertices.size()];
            Position p = null;
            Vector3d vec = null;
            for (int i = 0; i < myVertices.size(); i++)
            {
                p = myVertices.get(i);
                vec = p.asVector3d();
                coords[i] = new Coordinate(vec.getX(), vec.getY());
            }

            LineString ls = null;
            try
            {
                ls = geomFactory.createLineString(coords);
            }
            catch (RuntimeException e)
            {
                // Bail if we failed to create the line string.
                ls = null;
            }
            if (ls != null)
            {
                for (Polygon aPolygon : polygons)
                {
                    intersects = tests.isContains() && aPolygon.contains(ls) || tests.isIntersects() && aPolygon.intersects(ls)
                            || tests.isOverlaps() && aPolygon.overlaps(ls);
                    if (intersects)
                    {
                        break;
                    }
                }
            }
            else
            {
                intersects = areAnyCoordinatesOverlappingOrIntersecting(polygons, geomFactory, coords);
            }
        }
        return intersects;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Position> boolean overlaps(BoundingBox<T> boundingBox, double tolerance)
    {
        if (getPositionType().isAssignableFrom(boundingBox.getPositionType()))
        {
            return boundingBox.overlaps(BoundingBoxes.getMinimumBoundingBox((Collection<? extends T>)getVertices()), tolerance);
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(150);
        sb.append(getClass().getSimpleName()).append(' ').append(hashCode()).append(" [").append(getVertices()).append(']');
        return sb.toString();
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    /**
     * Set the vertices between the line segments. This is intended for use only
     * during construction.
     *
     * @param vertices the vertices
     * @throws IllegalArgumentException when the vertices cannot be used to
     *             build a valid polyline.
     */
    protected final void setVertices(List<? extends Position> vertices) throws IllegalArgumentException
    {
        if (vertices.size() < 2)
        {
            throw new IllegalArgumentException(getClass().getSimpleName() + " must have at least two vertices.");
        }
        if (vertices.contains(null))
        {
            throw new IllegalArgumentException(getClass().getSimpleName() + " may not have null vertices.");
        }

        final Position firstPosition = vertices.get(0);
        final Class<? extends Position> type = firstPosition.getClass();
        for (int i = 1; i < vertices.size(); i++)
        {
            if (type != vertices.get(i).getClass())
            {
                throw new IllegalArgumentException("Position type must be consistent across " + getClass().getSimpleName());
            }
        }
        if (GeographicPosition.class.isAssignableFrom(type))
        {
            LatLonAlt firstLla = ((GeographicPosition)firstPosition).getLatLonAlt();
            final Altitude.ReferenceLevel altRef = firstLla.getAltitudeReference();
            for (int i = 1; i < vertices.size(); i++)
            {
                LatLonAlt lla = ((GeographicPosition)vertices.get(i)).getLatLonAlt();
                if (altRef != lla.getAltitudeReference())
                {
                    throw new IllegalArgumentException(
                            "Altitude reference system must be consistent across " + getClass().getSimpleName());
                }
            }
        }

        if (myVertices != null)
        {
            throw new IllegalStateException("Vertices cannot be set more than once.");
        }

        myVertices = Collections.unmodifiableList(new ArrayList<Position>(vertices));
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends AbstractGeometry.Builder
    {
        /** When true, line smoothing will be used. */
        private boolean myLineSmoothing;

        /** The type of line. */
        private LineType myLineType = LineType.STRAIGHT_LINE;

        /** The vertices between the line segments. */
        private List<? extends T> myVertices;

        /**
         * Get the lineType.
         *
         * @return the lineType
         */
        public LineType getLineType()
        {
            return myLineType;
        }

        /**
         * Accessor for the vertices.
         *
         * @return The vertices.
         */
        public List<? extends T> getVertices()
        {
            return myVertices;
        }

        /**
         * Get the lineSmoothing.
         *
         * @return the lineSmoothing
         */
        public boolean isLineSmoothing()
        {
            return myLineSmoothing;
        }

        /**
         * Set the lineSmoothing.
         *
         * @param lineSmoothing the lineSmoothing to set
         */
        public void setLineSmoothing(boolean lineSmoothing)
        {
            myLineSmoothing = lineSmoothing;
        }

        /**
         * Set the lineType.
         *
         * @param lineType the lineType to set
         */
        public void setLineType(LineType lineType)
        {
            myLineType = lineType;
        }

        /**
         * Set the vertices between the line segments.
         *
         * @param vertices The vertices to set.
         */
        public void setVertices(List<? extends T> vertices)
        {
            myVertices = vertices;
        }
    }
}
