package io.opensphere.core.geometry;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/**
 * A geometry representing a polygon mesh which is circular.
 */
public class CircularMeshGeometry extends PolygonMeshGeometry
{
    /** Center of the circle. */
    private final Position myCenter;

    /** Number of vertices on the circle border. */
    private final int myNumVertices;

    /** Radius of the circle. */
    private final double myRadius;

    /** Adjustment factor for lighting normals. */
    private final double myWarpFactor;

    /**
     * Constructor.
     *
     * @param builder Builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public CircularMeshGeometry(Builder<?> builder, PolygonMeshRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        myCenter = builder.getCenter();
        myNumVertices = builder.getNumVertices();
        myWarpFactor = builder.getWarpFactor();
        myRadius = builder.getRadius();
        initCircle();
        validate();
    }

    @Override
    public CircularMeshGeometry clone()
    {
        return (CircularMeshGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.createBuilder();
        builder.setCenter(myCenter);
        builder.setRadius(myRadius);
        builder.setNumVertices(myNumVertices);
        builder.setWarpFactor(myWarpFactor);
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
    public CircularMeshGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new CircularMeshGeometry(createBuilder(), (PolygonMeshRenderProperties)renderProperties, constraints);
    }

    /**
     * Get the center of the mesh.
     *
     * @return the center
     */
    public Position getCenter()
    {
        return myCenter;
    }

    /**
     * Get the radius of the circle.
     *
     * @return the radius
     */
    public double getRadius()
    {
        return myRadius;
    }

    /**
     * Get the number of vertices in the circle border.
     *
     * @return the number of vertices
     */
    public int getVertexCount()
    {
        return myNumVertices;
    }

    /**
     * Get the warpFactor (a multiplier for lighting normals).
     *
     * @return the warpFactor
     */
    public double getWarpFactor()
    {
        return myWarpFactor;
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<>();
    }

    /** Generate the required fields for the mesh. */
    private void initCircle()
    {
        List<Position> positions = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();
        PetrifyableTIntList indices = new PetrifyableTIntArrayList();

        positions.add(myCenter);
        normals.add(new Vector3d(0d, 0d, 1d));
        int currentIndex = 0;
        for (int i = 0; i <= myNumVertices; i++)
        {
            double angle = i * MathUtil.TWO_PI / myNumVertices;

            double sin = Math.sin(angle);
            double cos = Math.cos(angle);
            Position pos = myCenter.add(new Vector3d(myRadius * cos, -myRadius * sin, 0d));
            Vector3d normal = new Vector3d(-cos * myWarpFactor, -sin * myWarpFactor, 1d);
            positions.add(pos);
            normals.add(normal);
            ++currentIndex;

            if (i != 0)
            {
                indices.add(0);
                indices.add(currentIndex - 1);
                indices.add(currentIndex);
            }
        }

        setPolygonVertexCount(3);
        setPositions(positions);
        setIndices(indices);
        setNormals(normals);
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

        /** Number of vertices on the circle border. */
        private int myNumVertices;

        /** Radius of the circle. */
        private double myRadius;

        /** Adjustment factor for lighting normals. */
        private double myWarpFactor;

        /**
         * Get the center.
         *
         * @return the center
         */
        public T getCenter()
        {
            return myCenter;
        }

        /**
         * Get the numVertices.
         *
         * @return the numVertices
         */
        public int getNumVertices()
        {
            return myNumVertices;
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
         * Get the warpFactor.
         *
         * @return the warpFactor
         */
        public double getWarpFactor()
        {
            return myWarpFactor;
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
         * Set the numVertices.
         *
         * @param numVertices the numVertices to set
         */
        public void setNumVertices(int numVertices)
        {
            myNumVertices = numVertices;
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
         * Set the warpFactor.
         *
         * @param warpFactor the warpFactor to set
         */
        public void setWarpFactor(double warpFactor)
        {
            myWarpFactor = warpFactor;
        }
    }
}
