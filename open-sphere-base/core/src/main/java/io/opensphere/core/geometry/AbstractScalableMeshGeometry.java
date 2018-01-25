package io.opensphere.core.geometry;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;

/**
 * Abstract class that holds common properties for visualization geometries and
 * uses mesh specific render properties.
 */
public abstract class AbstractScalableMeshGeometry extends AbstractScalableGeometry
{
    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    protected AbstractScalableMeshGeometry(AbstractScalableGeometry.Builder<?> builder,
            ScalableMeshRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
    }

    @Override
    public ScalableMeshRenderProperties getRenderProperties()
    {
        return (ScalableMeshRenderProperties)super.getRenderProperties();
    }
}
