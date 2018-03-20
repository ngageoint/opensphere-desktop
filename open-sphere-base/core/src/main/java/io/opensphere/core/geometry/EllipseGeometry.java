package io.opensphere.core.geometry;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;

/**
 * A {@link Geometry} that models a series of connected line segments that
 * approximate an ellipse.
 */
public class EllipseGeometry extends PolygonGeometry
{
    /**
     * The ellipse's center point.
     */
    private Position myCenter;

    /**
     * Construct the geometry.
     *
     * @param builder The angle ellipse geometry builder.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     * @throws IllegalArgumentException when the vertices cannot be used to
     *             build a valid polyline.
     */
    public EllipseGeometry(EllipseGeometry.AngleBuilder<?> builder, PolygonRenderProperties renderProperties,
            Constraints constraints)
        throws IllegalArgumentException
    {
        this((PolygonGeometry.Builder<?>)builder, renderProperties, constraints);

        Position center = builder.getCenter().add(new Vector3d(0, 0, getRenderProperties().getBaseAltitude()));
        myCenter = center;
        double angle = builder.getAngle() * MathUtil.DEG_TO_RAD;
        Vector3d axis = new Vector3d(Math.cos(angle), Math.sin(angle), 0.);
        int vertexCount = builder.getVertexCount();
        double semiMajorAxis = builder.getSemiMajorAxis();
        double semiMinorAxis = builder.getSemiMinorAxis();

        setVertices(createVertices(center, axis, vertexCount, semiMajorAxis, semiMinorAxis));
    }

    /**
     * Construct the geometry. This is provided for use with the builder
     * returned from {@link AbstractGeometry#createBuilder()}.
     *
     * @param builder The basic ellipse geometry builder.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public EllipseGeometry(EllipseGeometry.Builder<?> builder, PolygonRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
    }

    /**
     * Construct the geometry using foci and semi-major and semi-minor axes.
     *
     * @param builder The ellipse geometry builder.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     * @throws IllegalArgumentException when the vertices cannot be used to
     *             build a valid polyline.
     */
    public EllipseGeometry(EllipseGeometry.FocusBuilder<?> builder, PolygonRenderProperties renderProperties,
            Constraints constraints)
        throws IllegalArgumentException
    {
        super(builder, renderProperties, constraints);

        Position focus1 = builder.getFocus1();
        Position focus2 = builder.getFocus2();
        Position center = focus1.interpolate(focus2, .5).add(new Vector3d(0, 0, getRenderProperties().getBaseAltitude()));
        myCenter = center;

        Vector3d axis = focus1.subtract(focus2);

        int vertexCount = builder.getVertexCount();
        double semiMajorAxis = builder.getSemiMajorAxis();
        double semiMinorAxis = builder.getSemiMinorAxis();

        double length = axis.getLength();
        if (length <= MathUtil.DBL_EPSILON)
        {
            axis = Vector3d.UNIT_X;
        }
        else if (length != 1.)
        {
            axis = axis.getNormalized();
        }

        setVertices(createVertices(center, axis, vertexCount, semiMajorAxis, semiMinorAxis));
    }

    /**
     * Construct the geometry.
     *
     * @param builder The angle ellipse geometry builder.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     * @throws IllegalArgumentException when the vertices cannot be used to
     *             build a valid polyline.
     */
    public EllipseGeometry(EllipseGeometry.ProjectedBuilder builder, PolygonRenderProperties renderProperties,
            Constraints constraints)
        throws IllegalArgumentException
    {
        this((PolygonGeometry.Builder<?>)builder, renderProperties, constraints);
        myCenter = builder.getCenter();
        Utilities.checkNull(builder.getProjection(), "builder.getProjection()");
        Utilities.checkNull(builder.getCenter(), "builder.getCenter()");
        setVertices(EllipseGeometryUtilities.createProjectedVertices(builder));
    }

    /**
     * Protected constructor for use by other public constructors.
     *
     * @param builder A polygon geometry builder for the superclass.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    protected EllipseGeometry(PolygonGeometry.Builder<?> builder, PolygonRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints, builder.isLineSmoothing(), builder.getLineType());
    }

    @Override
    public EllipseGeometry clone()
    {
        return (EllipseGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        return (Builder<? extends Position>)super.createBuilder();
    }

    @Override
    public EllipseGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new EllipseGeometry(createBuilder(), (PolygonRenderProperties)renderProperties, constraints);
    }

    @Override
    public Position getReferencePoint()
    {
        Position refPoint = myCenter;
        if (refPoint == null)
        {
            refPoint = super.getReferencePoint();
        }
        return refPoint;
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    /**
     * Create the vertices for the ellipse.
     *
     * @param center The center of the ellipse.
     * @param axis The normalized axis of the ellipse.
     * @param vertexCount The number of vertices to create.
     * @param semiMajorAxis The semi-major axis of the ellipse.
     * @param semiMinorAxis The semi-minor axis of the ellipse.
     * @return The vertices.
     */
    protected final List<Position> createVertices(Position center, Vector3d axis, int vertexCount, double semiMajorAxis,
            double semiMinorAxis)
    {
        List<Position> vertices = new ArrayList<>(vertexCount);
        double angleStep = MathUtil.TWO_PI / vertexCount;

        for (int i = 0; i < vertexCount; i++)
        {
            double theta = i * angleStep;

            double sin = Math.sin(theta);
            double cos = Math.cos(theta);
            double x = cos * semiMajorAxis * axis.getX() - sin * semiMinorAxis * axis.getY();
            double y = cos * semiMajorAxis * axis.getY() + sin * semiMinorAxis * axis.getX();
            Position pt = center.add(new Vector3d(x, y, 0.));
            vertices.add(pt);
        }
        return vertices;
    }

    /**
     * Builder for the geometry that takes an angle.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class AngleBuilder<T extends Position> extends EllipseGeometry.AbstractBuilder<T>
    {
        /** Builder property. */
        private double myAngle;

        /** Builder property. */
        private T myCenter;

        /**
         * Accessor for the angle in degrees.
         *
         * @return The angle.
         */
        public double getAngle()
        {
            return myAngle;
        }

        /**
         * Accessor for the center.
         *
         * @return The center.
         */
        public T getCenter()
        {
            return myCenter;
        }

        /**
         * Mutator for the angle.
         *
         * @param angle The angle to set in degrees.
         */
        public void setAngle(double angle)
        {
            myAngle = angle;
        }

        /**
         * Mutator for the center.
         *
         * @param center The center to set.
         */
        public void setCenter(T center)
        {
            myCenter = center;
        }
    }

    /**
     * Builder for the geometry that takes ellipse foci.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class FocusBuilder<T extends Position> extends EllipseGeometry.AbstractBuilder<T>
    {
        /** Builder property. */
        private T myFocus1;

        /** Builder property. */
        private T myFocus2;

        /**
         * Accessor for the focus1.
         *
         * @return The focus1.
         */
        public T getFocus1()
        {
            return myFocus1;
        }

        /**
         * Accessor for the focus2.
         *
         * @return The focus2.
         */
        public T getFocus2()
        {
            return myFocus2;
        }

        /**
         * Set the position of the first focus of the ellipse.
         *
         * @param focus1 The focus1 to set.
         */
        public void setFocus1(T focus1)
        {
            myFocus1 = focus1;
        }

        /**
         * Set the position of the second focus of the ellipse.
         *
         * @param focus2 The focus2 to set.
         */
        public void setFocus2(T focus2)
        {
            myFocus2 = focus2;
        }
    }

    /**
     * A builder for creating the ellipse when the vertices must be projected.
     */
    public static class ProjectedBuilder extends EllipseGeometry.Builder<GeographicPosition>
    {
        /** Builder property. */
        private double myAngle;

        /** Builder property. */
        private GeographicPosition myCenter;

        /** The projection used for creating the vertices of the ellipse. */
        private Projection myProjection;

        /** Builder property. */
        private double mySemiMajorAxis;

        /** Builder property. */
        private double mySemiMinorAxis;

        /**
         * Accessor for the angle in degrees.
         *
         * @return The angle.
         */
        public double getAngle()
        {
            return myAngle;
        }

        /**
         * Accessor for the center.
         *
         * @return The center.
         */
        public GeographicPosition getCenter()
        {
            return myCenter;
        }

        /**
         * Get the projection.
         *
         * @return the projection
         */
        public Projection getProjection()
        {
            return myProjection;
        }

        /**
         * Get the length of the semi-major axis of the ellipse, in meters.
         *
         * @return The length of the semi-major axis.
         */
        public double getSemiMajorAxis()
        {
            return mySemiMajorAxis;
        }

        /**
         * Get the length of the semi-minor axis of the ellipse, in meters.
         *
         * @return The length of the semi-minor axis.
         */
        public double getSemiMinorAxis()
        {
            return mySemiMinorAxis;
        }

        /**
         * Mutator for the angle.
         *
         * @param angle The angle to set in degrees.
         */
        public void setAngle(double angle)
        {
            myAngle = angle;
        }

        /**
         * Mutator for the center.
         *
         * @param center The center to set.
         */
        public void setCenter(GeographicPosition center)
        {
            myCenter = center;
        }

        /**
         * Set the projection.
         *
         * @param projection the projection to set
         */
        public void setProjection(Projection projection)
        {
            myProjection = projection;
        }

        /**
         * Set the length of the semi-major axis of the ellipse, in meters.
         *
         * @param semiMajorAxis The length of the semi-major axis.
         */
        public void setSemiMajorAxis(double semiMajorAxis)
        {
            mySemiMajorAxis = semiMajorAxis;
        }

        /**
         * Set the length of the semi-minor axis of the ellipse, in meters.
         *
         * @param semiMinorAxis The length of the semi-minor axis.
         */
        public void setSemiMinorAxis(double semiMinorAxis)
        {
            mySemiMinorAxis = semiMinorAxis;
        }
    }

    /**
     * Abstract base class for ellipse builders that have axes in the same units
     * as their coordinates.
     *
     * @param <T> The position type associated with the geometry.
     */
    protected abstract static class AbstractBuilder<T extends Position> extends EllipseGeometry.Builder<T>
    {
        /** Builder property. */
        private double mySemiMajorAxis;

        /** Builder property. */
        private double mySemiMinorAxis;

        /**
         * Get the length of the semi-major axis of the ellipse, in the same
         * units as its center/foci.
         *
         * @return The length of the semi-major axis.
         */
        public double getSemiMajorAxis()
        {
            return mySemiMajorAxis;
        }

        /**
         * Get the length of the semi-minor axis of the ellipse, in the same
         * units as its center/foci.
         *
         * @return The length of the semi-minor axis.
         */
        public double getSemiMinorAxis()
        {
            return mySemiMinorAxis;
        }

        /**
         * Set the length of the semi-major axis of the ellipse, in the same
         * units as its center/foci.
         *
         * @param semiMajorAxis The length of the semi-major axis.
         */
        public void setSemiMajorAxis(double semiMajorAxis)
        {
            mySemiMajorAxis = semiMajorAxis;
        }

        /**
         * Set the length of the semi-minor axis of the ellipse, in the same
         * units as its center/foci.
         *
         * @param semiMinorAxis The length of the semi-minor axis.
         */
        public void setSemiMinorAxis(double semiMinorAxis)
        {
            mySemiMinorAxis = semiMinorAxis;
        }
    }

    /**
     * Abstract base class for the ellipse builder classes.
     *
     * @param <T> The position type associated with the geometry.
     */
    protected static class Builder<T extends Position> extends PolygonGeometry.Builder<T>
    {
        /** Builder property. */
        private int myVertexCount = 37;

        /**
         * Get the number of vertices in the ellipse.
         *
         * @return The vertexCount.
         */
        public int getVertexCount()
        {
            return myVertexCount;
        }

        /**
         * Set the number of vertices in the ellipse.
         *
         * @param vertexCount The vertexCount to set.
         */
        public void setVertexCount(int vertexCount)
        {
            myVertexCount = vertexCount;
        }
    }
}
