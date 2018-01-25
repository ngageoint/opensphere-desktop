package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/**
 * The frustum geometry. It is possible to create a pyramid with number of
 * points set to 3 and likewise a spike with number of points set to 4 (and top
 * radius of zero). The base radius can be set to zero as well. More complex
 * shapes can be formed by combining multiple frustum geometries.
 */
public class FrustumGeometry extends AbstractScalableMeshGeometry
{
    /** Radius at base of geometry. */
    private final float myBaseRadius;

    /** Number of points along each circular section. */
    private final int myNumPoints;

    /** Radius at top of geometry. */
    private final float myTopRadius;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public FrustumGeometry(FrustumGeometry.Builder<?> builder, ScalableMeshRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        myNumPoints = builder.getCircularPoints();
        myBaseRadius = builder.getBaseRadius();
        myTopRadius = builder.getTopRadius();
        initFrustum();
    }

    @Override
    public FrustumGeometry clone()
    {
        return (FrustumGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        Builder<? extends Position> builder = (Builder<? extends Position>)super.createBuilder();
        builder.setBaseRadius(getBaseRadius());
        builder.setTopRadius(getTopRadius());
        builder.setCircularPoints(getCircularPoints());

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
    public FrustumGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new FrustumGeometry(createBuilder(), (ScalableMeshRenderProperties)renderProperties, constraints);
    }

    /**
     * Standard getter.
     *
     * @return The radius at base.
     */
    public final float getBaseRadius()
    {
        return myBaseRadius;
    }

    /**
     * Get the number of circular points.
     *
     * @return the circularPoints
     */
    public final int getCircularPoints()
    {
        return myNumPoints;
    }

    /**
     * Standard getter.
     *
     * @return The radius at top.
     */
    public final float getTopRadius()
    {
        return myTopRadius;
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    /**
     * Create the frustum geometry.
     */
    private void createFrustum()
    {
        List<Position> positions = New.list();
        List<Color> colors = New.list();
        List<List<? extends Position>> rings = New.list();

        Position centerPos = getPosition().add(new Vector3d(0, 0, getRenderProperties().getBaseAltitude()));
        Position topCenter = centerPos.add(new Vector3d(0, 0, getRenderProperties().getHeight()));

        // Base Ring
        List<? extends Position> baseRing = createRing(centerPos, getBaseRadius());
        Color baseColor = getRenderProperties().getBaseColor();
        for (Position pos : baseRing)
        {
            positions.add(pos);
            colors.add(baseColor);
        }
        rings.add(baseRing);

        // Top Ring
        List<? extends Position> topRing = createRing(topCenter, getTopRadius());
        Color color = getRenderProperties().getColor();
        for (Position pos : topRing)
        {
            positions.add(pos);
            colors.add(color);
        }
        rings.add(topRing);

        // Generate indices for positions so far.
        PetrifyableTIntList indices = generateIndices(rings);

        // Cap the top
        if (getTopRadius() > 0)
        {
            positions.add(topCenter);
            colors.add(color);

            // Find indices for the top
            final int topCenterIndex = positions.size() - 1;
            for (int currentIndex = topCenterIndex - myNumPoints; currentIndex < positions.size() - 1; currentIndex++)
            {
                // Check to see if first point (if so wrap around to last point)
                if (currentIndex != topCenterIndex - myNumPoints)
                {
                    indices.add(currentIndex - 1);
                }
                else
                {
                    indices.add(currentIndex + myNumPoints - 1);
                }
                indices.add(currentIndex);
                indices.add(topCenterIndex);
                indices.add(topCenterIndex);
            }
        }
        setPositions(positions);
        setIndices(indices);
        setColors(colors);
    }

    /**
     * Create a ring of positions around a center position.
     *
     * @param centerPos The center position.
     * @param radius The radius around the center position.
     * @return The ring of positions.
     */
    private List<? extends Position> createRing(Position centerPos, float radius)
    {
        List<Position> ring = New.list(myNumPoints);

        double angleIncrement = MathUtil.TWO_PI / myNumPoints;
        for (int i = 0; i < myNumPoints; ++i)
        {
            double angle = i * angleIncrement;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;

            ring.add(centerPos.add(new Vector3d(x, y, 0.)));
        }
        return ring;
    }

    /**
     * Go back through and find the indices for the positions.
     *
     * @param rings The positions to calculate indices for.
     * @return A list of the indices.
     */
    private PetrifyableTIntList generateIndices(List<List<? extends Position>> rings)
    {
        PetrifyableTIntList indices = new PetrifyableTIntArrayList();
        List<? extends Position> previousRing = null;
        for (int ringNum = 1; ringNum <= rings.size(); ++ringNum)
        {
            int curRingNumber = ringNum % rings.size();
            List<? extends Position> currentRing = rings.get(curRingNumber);
            if (previousRing == null)
            {
                previousRing = currentRing;
                continue;
            }
            int ringSize = currentRing.size();
            int prevRingOffset = (ringNum - 1) * ringSize;
            int curRingOffset = curRingNumber * ringSize;

            for (int i = 1; i <= ringSize; ++i)
            {
                int curPos = i % ringSize;
                int prevPos = i - 1;
                indices.add(prevRingOffset + curPos);
                indices.add(prevRingOffset + prevPos);
                indices.add(curRingOffset + prevPos);
                indices.add(curRingOffset + curPos);
            }
        }
        return indices;
    }

    /**
     * Initialize the frustum.
     */
    private void initFrustum()
    {
        setPolygonVertexCount(4);
        createFrustum();
        validate();
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends AbstractScalableMeshGeometry.Builder<T>
    {
        /** Radius at base of geometry. */
        private float myBaseRadius;

        /** Number points along each circular section. */
        private int myNumPoints;

        /** Radius at top of geometry. */
        private float myTopRadius;

        /**
         * Standard getter.
         *
         * @return The radius at base.
         */
        public float getBaseRadius()
        {
            return myBaseRadius;
        }

        /**
         * Get the number of circular points.
         *
         * @return the circularPoints
         */
        public int getCircularPoints()
        {
            return myNumPoints;
        }

        /**
         * Standard getter.
         *
         * @return The radius at top.
         */
        public float getTopRadius()
        {
            return myTopRadius;
        }

        /**
         * Standard setter.
         *
         * @param radius The base radius.
         */
        public void setBaseRadius(float radius)
        {
            myBaseRadius = radius;
        }

        /**
         * Set the circularPoints.
         *
         * @param circularPoints the circularPoints to set
         */
        public void setCircularPoints(int circularPoints)
        {
            myNumPoints = circularPoints;
        }

        /**
         * Standard setter.
         *
         * @param radius The top radius.
         */
        public void setTopRadius(float radius)
        {
            myTopRadius = radius;
        }
    }
}
