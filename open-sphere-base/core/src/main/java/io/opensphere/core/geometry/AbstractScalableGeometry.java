package io.opensphere.core.geometry;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.ScalableRenderProperties;
import io.opensphere.core.model.Position;

/**
 * Abstract class that holds common properties for visualization geometries.
 * TODO This needs better documentation. What is the reference position used for
 * and in what way is this geometry scalable?
 */
public abstract class AbstractScalableGeometry extends PolygonMeshGeometry
{
    /** The reference position of this geometry. */
    private final Position myPosition;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    protected AbstractScalableGeometry(AbstractScalableGeometry.Builder<?> builder, ScalableRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        myPosition = builder.getPosition();
    }

    @Override
    public AbstractScalableGeometry clone()
    {
        return (AbstractScalableGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.createBuilder();
        builder.setPosition(getPosition());
        return builder;
    }

    /**
     * Accessor for the position.
     *
     * @return the position
     */
    public final Position getPosition()
    {
        return myPosition;
    }

    @Override
    public ScalableRenderProperties getRenderProperties()
    {
        return (ScalableRenderProperties)super.getRenderProperties();
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends PolygonMeshGeometry.Builder<T>
    {
        /** The reference position for this geometry. */
        private T myPosition;

        /**
         * Get the reference position of the scalable geometry.
         *
         * @return The position.
         */
        public T getPosition()
        {
            return myPosition;
        }

        /**
         * Set the reference position of the scalable geometry.
         *
         * @param position The position to set.
         */
        public void setPosition(T position)
        {
            myPosition = position;
        }
    }
}
