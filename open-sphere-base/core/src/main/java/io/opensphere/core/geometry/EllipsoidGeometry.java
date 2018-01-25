package io.opensphere.core.geometry;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.model.Position;

/**
 * Geometry representing an Ellipsoid shape that can be rendered on the globe.
 */
public class EllipsoidGeometry extends PolygonMeshGeometry
{
    /**
     * The builder that calculates the ellipsoids transform.
     */
    private final EllipsoidGeometryBuilder<? extends Position> myBuilder;

    /**
     * Constructs a new ellipsoid geometry.
     *
     * @param builder Contains parameters about the geometry, such as color
     *            position, shape and size.
     * @param renderProperties Contains properties used for rendering.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public EllipsoidGeometry(EllipsoidGeometryBuilder<? extends Position> builder, PolygonMeshRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder.generatePolygonMeshBuilder(), renderProperties, constraints);
        renderProperties.setTransform(builder.generateTransform());
        myBuilder = builder;
    }

    @Override
    public EllipsoidGeometry clone()
    {
        return (EllipsoidGeometry)super.clone();
    }

    @Override
    public EllipsoidGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new EllipsoidGeometry(myBuilder, (PolygonMeshRenderProperties)renderProperties, constraints);
    }

    /**
     * Handles a projection change.
     */
    public void handleProjectionChanged()
    {
        getRenderProperties().setTransform(myBuilder.generateTransform());
    }
}
