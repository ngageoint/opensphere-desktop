package io.opensphere.core.geometry;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.math.Matrix3d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/**
 * A geometry representing a polygon mesh which is a torus. The parameterized
 * equations for a torus are : x(u,v) = (R + r * cos(v)) * cos(u) y(u,v) = (R +
 * r * cos(v)) * sin(u) z(u,v) = r * sin(v) where u is the tube radius. v is the
 * radius from the center to the center of the tube. u and v are in [0, 2*pi). R
 * is the distance from the center of the tube to the center of the torus. r is
 * the radius of the tube.
 */
public class TorusMeshGeometry extends PolygonMeshGeometry
{
    /** Center of the circle. */
    private final Position myCenter;

    /** Number points along each tube sections. */
    private final int myCircPoints;

    /** Transform positions on the torus using this transform. */
    private Matrix3d myPositionTransform;

    /** Distance from the center of the torus to the center of the tube. */
    private final double myRadius;

    /** Distance from the center of the tube to the outer edge of the tube. */
    private final double myTubeRadius;

    /** Number of sections which make up the tube of the torus. */
    private final int myTubeSections;

    /**
     * Constructor.
     *
     * @param builder Builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public TorusMeshGeometry(Builder<?> builder, PolygonMeshRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);

        myCenter = builder.getCenter();
        myCircPoints = builder.getCircularPoints();
        myRadius = builder.getRadius();
        myTubeRadius = builder.getTubeRadius();
        myTubeSections = builder.getTubeSections();
        myPositionTransform = builder.getPositionTransform() == null ? null : builder.getPositionTransform().clone();

        initTorus();
        validate();
    }

    @Override
    public TorusMeshGeometry clone()
    {
        TorusMeshGeometry clone = (TorusMeshGeometry)super.clone();
        clone.myPositionTransform = myPositionTransform == null ? null : myPositionTransform.clone();
        return clone;
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.createBuilder();
        builder.setCenter(myCenter);
        builder.setCircularPoints(myCircPoints);
        builder.setPositionTransform(myPositionTransform.clone());
        builder.setRadius(myRadius);
        builder.setTubeRadius(myTubeRadius);
        builder.setTubeSections(myTubeSections);
        // This sub-geometry finds and sets the normals, colors, indices,
        // positions,
        // and vertex count itself in the constructor. We don't want to set it
        // from
        // the existing builder so reset these values.
        builder.setPolygonVertexCount(0);
        builder.setNormals(null);
        builder.setColors(null);
        builder.setIndices(null);
        builder.setPositions(null);
        return builder;
    }

    @Override
    public TorusMeshGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new TorusMeshGeometry(createBuilder(), (PolygonMeshRenderProperties)renderProperties, constraints);
    }

    /**
     * Get the center.
     *
     * @return the center
     */
    public Position getCenter()
    {
        return myCenter;
    }

    /**
     * Get the circPoints.
     *
     * @return the circPoints
     */
    public int getCirPoints()
    {
        return myCircPoints;
    }

    /**
     * Get the positionTransform.
     *
     * @return the positionTransform
     */
    public Matrix3d getPositionTransform()
    {
        return myPositionTransform.clone();
    }

    /**
     * Get the radius.
     *
     * @return the radius
     */
    public double getRadius()
    {
        return myRadius;
    }

    /**
     * Get the tubeRadius.
     *
     * @return the tubeRadius
     */
    public double getTubeRadius()
    {
        return myTubeRadius;
    }

    /**
     * Get the tubeSections.
     *
     * @return the tubeSections
     */
    public int getTubeSections()
    {
        return myTubeSections;
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        Builder<Position> bldr = new Builder<>();
        return bldr;
    }

    /**
     * Generate the indices for the polygon mesh given the vertices on the
     * torus.
     *
     * @param rings Vertices organized by ring.
     */
    private void generateIndices(List<List<Position>> rings)
    {
        PetrifyableTIntList indices = new PetrifyableTIntArrayList();

        List<Position> previousRing = null;
        for (int ringNum = 0; ringNum <= rings.size(); ++ringNum)
        {
            int curRingNumber = ringNum % rings.size();
            List<Position> currentRing = rings.get(curRingNumber);
            if (previousRing == null)
            {
                previousRing = currentRing;
                continue;
            }
            int ringSize = currentRing.size();
            int prevRingOffset = (ringNum - 1) * ringSize;
            int curRingOffset = curRingNumber * ringSize;

            for (int i = 0; i <= ringSize; ++i)
            {
                if (i == 0)
                {
                    continue;
                }
                int curPos = i % ringSize;
                int prevPos = i - 1;

                indices.add(prevRingOffset + curPos);
                indices.add(prevRingOffset + prevPos);
                indices.add(curRingOffset + prevPos);
                indices.add(curRingOffset + curPos);
            }
        }

        setIndices(indices);
    }

    /**
     * Generate all of the positions which will be used for the torus. These are
     * organizes into rings along the torus surface.
     *
     * @return a list of rings of positions.
     */
    private List<List<Position>> generateRings()
    {
        List<Position> positions = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();
        List<List<Position>> rings = new ArrayList<>();
        for (int i = 0; i < myTubeSections; ++i)
        {
            List<Position> currentRing = new ArrayList<>();
            rings.add(currentRing);
            // the torus angle is angle "u"
            double angleU = i * MathUtil.TWO_PI / myTubeSections;
            double cosu = Math.cos(angleU);
            double sinu = Math.sin(angleU);
            Vector3d tubeCenterLoc = new Vector3d(myRadius * cosu, myRadius * sinu, 0d);
            for (int j = 0; j < myCircPoints; ++j)
            {
                // the tube angle is angle "v"
                double angleV = j * MathUtil.TWO_PI / myCircPoints;

                double tubeCenterPart = myRadius + myTubeRadius * Math.cos(angleV);

                double x = tubeCenterPart * cosu;
                double y = tubeCenterPart * sinu;
                double z = myTubeRadius * Math.sin(angleV);
                Vector3d unadjustedLoc = new Vector3d(x, y, z);
                Position pos = null;
                if (myPositionTransform != null)
                {
                    pos = myCenter.add(myPositionTransform.mult(unadjustedLoc));
                    normals.add(myPositionTransform.mult(unadjustedLoc.subtract(tubeCenterLoc)).getNormalized());
                }
                else
                {
                    pos = myCenter.add(unadjustedLoc);
                    normals.add(unadjustedLoc.subtract(tubeCenterLoc).getNormalized());
                }

                currentRing.add(pos);
                positions.add(pos);
            }
        }

        setNormals(normals);
        setPositions(positions);
        return rings;
    }

    /** Generate the required fields for the mesh. */
    private void initTorus()
    {
        setPolygonVertexCount(4);

        List<List<Position>> rings = generateRings();
        generateIndices(rings);
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> position type for the geometry.
     */
    public static class Builder<T extends Position> extends PolygonMeshGeometry.Builder<T>
    {
        /** Center of the circle. */
        private T myCenter;

        /** Number points along each tube sections. */
        private int myCircularPoints;

        /** Transform positions on the torus using this transform. */
        private Matrix3d myPositionTransform;

        /** Distance from the center of the torus to the center of the tube. */
        private double myRadius;

        /**
         * Distance from the center of the tube to the outer edge of the tube.
         */
        private double myTubeRadius;

        /** Number of sections which make up the tube of the torus. */
        private int myTubeSections;

        /**
         * Get the center position.
         *
         * @return the center
         */
        public T getCenter()
        {
            return myCenter;
        }

        /**
         * Get the number of circular points.
         *
         * @return the circularPoints
         */
        public int getCircularPoints()
        {
            return myCircularPoints;
        }

        /**
         * Get the positionTransform.
         *
         * @return the positionTransform
         */
        public Matrix3d getPositionTransform()
        {
            return myPositionTransform;
        }

        /**
         * Get the distance from the center of the torus to the center of the
         * tube.
         *
         * @return the radius
         */
        public double getRadius()
        {
            return myRadius;
        }

        /**
         * Get the tubeRadius.
         *
         * @return the tubeRadius
         */
        public double getTubeRadius()
        {
            return myTubeRadius;
        }

        /**
         * Get the number of tube sections.
         *
         * @return the tubeSections
         */
        public int getTubeSections()
        {
            return myTubeSections;
        }

        /**
         * Set the center.
         *
         * @param center the center to set
         */
        public void setCenter(T center)
        {
            myCenter = center;
        }

        /**
         * Set the circularPoints.
         *
         * @param circularPoints the circularPoints to set
         */
        public void setCircularPoints(int circularPoints)
        {
            myCircularPoints = circularPoints;
        }

        /**
         * Set the positionTransform.
         *
         * @param positionTransform the positionTransform to set
         */
        public void setPositionTransform(Matrix3d positionTransform)
        {
            myPositionTransform = positionTransform;
        }

        /**
         * Set the radius.
         *
         * @param radius the radius to set
         */
        public void setRadius(double radius)
        {
            myRadius = radius;
        }

        /**
         * Set the tubeRadius.
         *
         * @param tubeRadius the tubeRadius to set
         */
        public void setTubeRadius(double tubeRadius)
        {
            myTubeRadius = tubeRadius;
        }

        /**
         * Set the tubeSections.
         *
         * @param tubeSections the tubeSections to set
         */
        public void setTubeSections(int tubeSections)
        {
            myTubeSections = tubeSections;
        }
    }
}
