package io.opensphere.core.geometry;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;

/**
 * A {@link Geometry} that has color.
 *
 */
public abstract class AbstractColorGeometry extends AbstractRenderableGeometry implements ColorGeometry
{
    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    AbstractColorGeometry(AbstractGeometry.Builder builder, ColorRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
    }

    @Override
    public AbstractColorGeometry clone()
    {
        return (AbstractColorGeometry)super.clone();
    }

    @Override
    public ColorRenderProperties getRenderProperties()
    {
        return (ColorRenderProperties)super.getRenderProperties();
    }
}
