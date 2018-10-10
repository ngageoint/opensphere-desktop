package io.opensphere.core.geometry;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;

/**
 * An abstract class that provides constraints and render properties for
 * geometries that are renderable.
 */
public abstract class AbstractRenderableGeometry extends AbstractGeometry implements ConstrainableGeometry, RenderableGeometry
{
    /** Constraints on when the geometry is visible. */
    private Constraints myConstraints;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            {@code null}).
     */
    protected AbstractRenderableGeometry(AbstractGeometry.Builder builder, BaseRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties);
        myConstraints = constraints;
    }

    @Override
    public AbstractRenderableGeometry clone()
    {
        AbstractRenderableGeometry clone = (AbstractRenderableGeometry)super.clone();
        clone.myConstraints = myConstraints == null ? null : myConstraints.clone();
        return clone;
    }

    /**
     * Create a new geometry that is a copy of this one, except with new render
     * properties and constraints. This is intended for situations where the
     * concrete geometry type is unknown, but render properties need to be
     * reorganized. If the concrete geometry type is known,
     * {@link #createBuilder()} should be used in conjunction with the
     * constructor instead of this method to allow for more flexibility and type
     * safety.
     *
     * @param renderProperties The render properties for the new geometry.
     * @param constraints The constraints for the new geometry.
     * @return The new geometry.
     * @throws ClassCastException If the render properties or constraints cannot
     *             be cast to the correct types for this geometry.
     */
    public abstract AbstractRenderableGeometry derive(BaseRenderProperties renderProperties, Constraints constraints)
            throws ClassCastException;

    @Override
    public Constraints getConstraints()
    {
        return myConstraints;
    }

    @Override
    public BaseRenderProperties getRenderProperties()
    {
        return (BaseRenderProperties)super.getRenderProperties();
    }
}
