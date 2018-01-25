package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableRenderProperties;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/** A filled ellipse. TODO What is scalable about this? */
public class EllipseScalableGeometry extends AbstractScalableGeometry
{
    /** The angle for ellipse orientation. */
    private final float myAngle;

    /** The number of points used to describe ellipse. */
    private final int myNumPoints;

    /** The projection used for creating the vertices of the ellipse. */
    private final Projection myProjection;

    /** The semi major axis. */
    private final float mySemiMajorAxis;

    /** The semi minor axis. */
    private final float mySemiMinorAxis;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public EllipseScalableGeometry(EllipseScalableGeometry.Builder builder, ScalableRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        myAngle = builder.getAngleDegrees();
        myNumPoints = builder.getPointsNumber();
        mySemiMajorAxis = builder.getSemiMajorAxis();
        mySemiMinorAxis = builder.getSemiMinorAxis();
        myProjection = builder.getProjection();
        initEllipse();
    }

    @Override
    public EllipseScalableGeometry clone()
    {
        return (EllipseScalableGeometry)super.clone();
    }

    @Override
    public Builder createBuilder()
    {
        Builder builder = (Builder)super.createBuilder();
        builder.setAngleDegrees(getAngle());
        builder.setPointsNumber(getPointsNumber());
        builder.setSemiMajorAxis(getSemiMajorAxis());
        builder.setSemiMinorAxis(getSemiMinorAxis());
        builder.setProjection(getProjection());

        // This sub-geometry finds and sets the normals, colors, indices,
        // positions, and vertex count itself in the constructor. We don't want
        // to set it from the existing builder so reset these values.
        builder.setPolygonVertexCount(0);
        builder.setNormals(null);
        builder.setColors(null);
        builder.setIndices(null);
        builder.setPositions(null);
        return builder;
    }

    @Override
    public EllipseScalableGeometry derive(BaseRenderProperties renderProperties, Constraints constraints)
        throws ClassCastException
    {
        return new EllipseScalableGeometry(createBuilder(), (ScalableRenderProperties)renderProperties, constraints);
    }

    /**
     * Accessor for the angle.
     *
     * @return The angle of ellipse.
     */
    public float getAngle()
    {
        return myAngle;
    }

    /**
     * Accessor for the number of points.
     *
     * @return The number of points.
     */
    public int getPointsNumber()
    {
        return myNumPoints;
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
     * Accessor for the semi major axis.
     *
     * @return The semi major axis value.
     */
    public float getSemiMajorAxis()
    {
        return mySemiMajorAxis;
    }

    /**
     * Accessor for the semi minor axis.
     *
     * @return The semi minor axis value.
     */
    public float getSemiMinorAxis()
    {
        return mySemiMinorAxis;
    }

    @Override
    protected Builder createRawBuilder()
    {
        return new Builder();
    }

    /**
     * Create the ellipse geometry.
     */
    private void createEllipse()
    {
        Projection proj = myProjection;
        if (proj == null)
        {
            createOrdinaryEllipse();
        }
        else
        {
            createProjectedEllipse(proj);
        }
    }

    /**
     * Create an un-projected ellipse.
     */
    private void createOrdinaryEllipse()
    {
        List<Position> positions = New.list();
        List<Color> colors = New.list();
        PetrifyableTIntList indices = new PetrifyableTIntArrayList();

        Color centerColor = getRenderProperties().getColor();
        Color edgeColor = getRenderProperties().getBaseColor();

        GeographicPosition center = (GeographicPosition)getPosition();
        double angleInRads = myAngle * MathUtil.DEG_TO_RAD;
        int vertexCount = myNumPoints;
        float semiMajorAxis = mySemiMajorAxis;
        float semiMinorAxis = mySemiMinorAxis;

        Vector3d axis = new Vector3d(Math.cos(angleInRads), Math.sin(angleInRads), 0.);
        Position centerPos = getPosition().add(new Vector3d(0, 0, getRenderProperties().getBaseAltitude()));

        double angleStep = MathUtil.TWO_PI / vertexCount;
        int currentIndex = 0;
        positions.add(center);
        colors.add(centerColor);

        for (int i = 0; i <= vertexCount; i++)
        {
            double theta = i * angleStep;
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            double x = cos * semiMajorAxis * axis.getX() - sin * semiMinorAxis * axis.getY();
            double y = cos * semiMajorAxis * axis.getY() + sin * semiMinorAxis * axis.getX();

            Vector3d unadjustedLoc = new Vector3d(x, y, center.getAlt().getMeters());
            positions.add(centerPos.add(unadjustedLoc));
            colors.add(edgeColor);

            ++currentIndex;
            if (i != 0)
            {
                indices.add(0);
                indices.add(currentIndex - 1);
                indices.add(currentIndex);
            }
        }
        setPositions(positions);
        setIndices(indices);
        setColors(colors);
    }

    /**
     * Create a projected ellipse. For ellipses whose centers have a non-zero
     * altitude, the ellipse will enlarge as the altitude increases.
     *
     * @param projection The projection to use.
     */
    private void createProjectedEllipse(Projection projection)
    {
        Utilities.checkNull(projection, "projection");
        List<Position> positions = New.list();
        List<Color> colors = New.list();
        PetrifyableTIntList indices = new PetrifyableTIntArrayList();

        Color centerColor = getRenderProperties().getColor();
        Color edgeColor = getRenderProperties().getBaseColor();

        GeographicPosition center = (GeographicPosition)getPosition();
        double angleInRads = myAngle * MathUtil.DEG_TO_RAD;
        int vertexCount = myNumPoints;
        float semiMajorAxis = mySemiMajorAxis;
        float semiMinorAxis = mySemiMinorAxis;

        // Create an ellipsoid to use for projecting the points into model
        // coordinates.
        Ellipsoid ellipsoid = EllipseGeometryUtilities.createEllipsoid(projection, center, angleInRads, semiMajorAxis,
                semiMinorAxis);

        double angleStep = MathUtil.TWO_PI / vertexCount;
        int currentIndex = 0;
        positions.add(center);
        colors.add(centerColor);

        for (int i = 0; i <= vertexCount; i++)
        {
            double theta = i * angleStep;
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            Vector3d modelPt = ellipsoid.localToModel(new Vector3d(cos, sin, 0.));
            GeographicPosition geoWithAlt = projection.convertToPosition(modelPt, ReferenceLevel.TERRAIN);
            // Remove the altitude from the position so that the ellipsoid
            // renders flat on the model. This contracts the radius
            // slightly. If we want to project it flat, the model position
            // should be moved in the opposite direction of the ellipsoid's
            // z-axis.
            LatLonAlt location = LatLonAlt.createFromDegreesMeters(geoWithAlt.getLatLonAlt().getLatD(),
                    geoWithAlt.getLatLonAlt().getLonD(), center.getAlt().getMeters(), center.getAlt().getReferenceLevel());
            positions.add(new GeographicPosition(location));
            colors.add(edgeColor);

            ++currentIndex;
            if (i != 0)
            {
                indices.add(0);
                indices.add(currentIndex - 1);
                indices.add(currentIndex);
            }
        }
        setPositions(positions);
        setIndices(indices);
        setColors(colors);
    }

    /**
     * Initialize the ellipse.
     */
    private void initEllipse()
    {
        setPolygonVertexCount(3);
        createEllipse();
        validate();
    }

    /**
     * Builder for the geometry.
     */
    public static class Builder extends AbstractScalableGeometry.Builder<GeographicPosition>
    {
        /** The angle for ellipse orientation. */
        private float myAngle;

        /** The number of points to use to describe ellipse. */
        private int myNumPoints = 37;

        /** The projection used for creating the vertices of the ellipse. */
        private Projection myProjection;

        /** The semi major axis. */
        private float mySemiMajorAxis;

        /** The semi minor axis. */
        private float mySemiMinorAxis;

        /**
         * Accessor for the angle.
         *
         * @return The angle of ellipse (degrees).
         */
        public float getAngleDegrees()
        {
            return myAngle;
        }

        /**
         * Accessor for number of points.
         *
         * @return The number of points used to describe ellipse.
         */
        public int getPointsNumber()
        {
            return myNumPoints;
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
         * Accessor for the semi major axis.
         *
         * @return The semi major axis.
         */
        public float getSemiMajorAxis()
        {
            return mySemiMajorAxis;
        }

        /**
         * Accessor for the semi minor axis.
         *
         * @return The semi minor axis.
         */
        public float getSemiMinorAxis()
        {
            return mySemiMinorAxis;
        }

        /**
         * Mutator for the angle.
         *
         * @param angle The angle of ellipse (degrees).
         */
        public void setAngleDegrees(float angle)
        {
            myAngle = angle;
        }

        /**
         * Mutator for the number of points.
         *
         * @param numPoints The number of points to use to describe ellipse.
         */
        public void setPointsNumber(int numPoints)
        {
            myNumPoints = numPoints;
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
         * Mutator for the semi major axis.
         *
         * @param semiMajorAxis The semi major axis to set.
         */
        public void setSemiMajorAxis(float semiMajorAxis)
        {
            mySemiMajorAxis = semiMajorAxis;
        }

        /**
         * Mutator for the semi minor axis.
         *
         * @param semiMinorAxis The semi minor axis to set.
         */
        public void setSemiMinorAxis(float semiMinorAxis)
        {
            mySemiMinorAxis = semiMinorAxis;
        }
    }
}
