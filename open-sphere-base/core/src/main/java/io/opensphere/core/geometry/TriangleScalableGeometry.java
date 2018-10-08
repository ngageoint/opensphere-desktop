package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/** The triangle geometry for visualization. */
public class TriangleScalableGeometry extends AbstractScalableMeshGeometry
{
    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public TriangleScalableGeometry(AbstractScalableGeometry.Builder<?> builder, ScalableMeshRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        initTriangle();
    }

    @Override
    public TriangleScalableGeometry clone()
    {
        return (TriangleScalableGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        Builder<? extends Position> builder = (Builder<? extends Position>)super.createBuilder();

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
    public TriangleScalableGeometry derive(BaseRenderProperties renderProperties, Constraints constraints)
        throws ClassCastException
    {
        return new TriangleScalableGeometry(createBuilder(), (ScalableMeshRenderProperties)renderProperties, constraints);
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<>();
    }

    /**
     * Create the triangle geometry.
     */
    private void createTriangle()
    {
        List<Position> positions = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();
        PetrifyableTIntList indices = new PetrifyableTIntArrayList();
        List<Color> colors = new ArrayList<>();

        Position centerPos = getPosition().add(new Vector3d(0, 0, getRenderProperties().getBaseAltitude()));

        double width = getRenderProperties().getWidth();
        double length = getRenderProperties().getLength();
        double height = getRenderProperties().getHeight();

        // The vertices of triangle
        Position tip = centerPos.add(new Vector3d(0, 0, height));
        Position southWest = centerPos.add(new Vector3d(-width, -length, 0));
        Position southEast = centerPos.add(new Vector3d(width, -length, 0));
        Position north = centerPos.add(new Vector3d(0, length, 0));

        // South
        positions.add(southWest);
        positions.add(southEast);
        positions.add(tip);
        Vector3d normal = southWest.subtract(tip).cross(southWest.subtract(southEast)).getNormalized();
        normals.add(normal);
        normals.add(normal);
        normals.add(normal);
        indices.add(0);
        indices.add(1);
        indices.add(2);
        colors.add(getRenderProperties().getBaseColor());
        colors.add(getRenderProperties().getBaseColor());
        colors.add(getRenderProperties().getColor());

        // West
        positions.add(north);
        normal = north.subtract(tip).cross(north.subtract(southWest)).getNormalized();
        normals.add(normal);
        indices.add(3);
        indices.add(0);
        indices.add(2);
        colors.add(getRenderProperties().getBaseColor());

        // East
        indices.add(1);
        indices.add(3);
        indices.add(2);

        setNormals(normals);
        setPositions(positions);
        setIndices(indices);
        setColors(colors);
    }

    /**
     * Initialize the triangle.
     */
    private void initTriangle()
    {
        setPolygonVertexCount(3);
        createTriangle();
        validate();
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends AbstractScalableMeshGeometry.Builder<T>
    {
    }
}
