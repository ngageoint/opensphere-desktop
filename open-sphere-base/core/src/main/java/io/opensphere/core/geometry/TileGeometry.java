package io.opensphere.core.geometry;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.ProxyTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Utilities;

/**
 * A {@link Geometry} that models an image that covers a rectangular area. If
 * this <tt>TileGeometry</tt> can be split into sub-tiles, it has a
 * {@link io.opensphere.core.geometry.AbstractTileGeometry.Divider} that knows
 * how to do that.
 */
public class TileGeometry extends AbstractTileGeometry<TileGeometry> implements ConstrainableGeometry, ColorGeometry
{
    /** Constraints on when the geometry is visible. */
    private Constraints myConstraints;

    /**
     * Properties which affect how this single geometry is rendered no matter
     * how its tile relatives are rendered within a tile tree.
     */
    private TileRenderProperties myIndividualProperties;

    /** Properties which affect how the geometry is rendered. */
    private TileRenderProperties myRenderProperties;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public TileGeometry(TileGeometry.Builder<?> builder, TileRenderProperties renderProperties, Constraints constraints)
    {
        this(builder, renderProperties, constraints, null);
    }

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     * @param layerId The id of the layer this geometry belongs to.
     */
    public TileGeometry(TileGeometry.Builder<?> builder, TileRenderProperties renderProperties, Constraints constraints,
            String layerId)
    {
        super(builder, renderProperties, layerId);
        Utilities.checkNull(renderProperties, "renderProperties");
        // If the tile is not drawable, we do not need an image manager
        // TODO eventually we should be able to have a drawable colored tile
        // which does not have a texture.
        if (renderProperties.isDrawable())
        {
            Utilities.checkNull(builder.getImageManager(), "builder.getImageManager()");
        }
        myRenderProperties = renderProperties;
        myConstraints = constraints;
    }

    /**
     * Removes the render properties unique to this tile so that it renders like
     * all of its tile relatives.
     */
    public synchronized void clearIndividualRenderProperties()
    {
        if (myIndividualProperties != null)
        {
            // Set the individual render properties to original values in case
            // something has reference to it and is using it.
            myIndividualProperties.setBlending(myRenderProperties.getBlending());
            myIndividualProperties.setColor(myRenderProperties.getColor());
            myIndividualProperties.setHidden(myRenderProperties.isHidden());
            myIndividualProperties.setHighlightColor(myRenderProperties.getHighlightColor());
            myIndividualProperties.setLighting(myRenderProperties.getLighting());
            myIndividualProperties.setObscurant(myRenderProperties.isObscurant());
            myIndividualProperties.setOpacity(myRenderProperties.getOpacity() * ColorUtilities.COLOR_COMPONENT_MAX_VALUE);
            myIndividualProperties.setRenderingOrder(myRenderProperties.getRenderingOrder());

            myIndividualProperties = null;
        }
    }

    @Override
    public TileGeometry clone()
    {
        TileGeometry clone = (TileGeometry)super.clone();
        clone.myConstraints = myConstraints == null ? null : myConstraints.clone();
        clone.myRenderProperties = myRenderProperties.clone();
        return clone;
    }

    @Override
    public Builder<Position> createBuilder()
    {
        return (Builder<Position>)super.doCreateBuilder();
    }

    @Override
    @SuppressWarnings("unchecked")
    public TileGeometry createSubTile(BoundingBox<? extends Position> bbox, Object imageKey, Divider<? extends Position> divider)
    {
        TileGeometry.Builder<Position> builder = createBuilder();
        builder.setParent(this);
        builder.setBounds(bbox);
        builder.setDivider((Divider<Position>)divider);
        builder.setImageManager(new ImageManager(imageKey, (ImageProvider<Object>)getImageManager().getImageProvider()));
        return new TileGeometry(builder, myRenderProperties, getConstraints(), getLayerId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractTileGeometry<?> createSubTile(BoundingBox<? extends Position> bbox, Object imageKey,
            io.opensphere.core.geometry.AbstractTileGeometry.Divider<? extends Position> divider, ImageManager imageManager)
    {
        TileGeometry.Builder<Position> builder = createBuilder();
        builder.setParent(this);
        builder.setBounds(bbox);
        builder.setDivider((Divider<Position>)divider);
        builder.setImageManager(imageManager);
        return new TileGeometry(builder, myRenderProperties, getConstraints(), getLayerId());
    }

    @Override
    public Constraints getConstraints()
    {
        return myConstraints;
    }

    @Override
    public synchronized TileRenderProperties getRenderProperties()
    {
        TileRenderProperties propertiesToReturn = myRenderProperties;
        if (myIndividualProperties != null)
        {
            propertiesToReturn = myIndividualProperties;
        }
        return propertiesToReturn;
    }

    /**
     * Gets the render properties that are unique to this tile as opposed to its
     * related tiles within the tile tree. When this tile needs to render like
     * the rest of its tiles in the tree call to clearIndividualRenderProperties
     * is required.
     *
     * @return The {@link TileRenderProperties} unique to this tile.
     */
    public synchronized TileRenderProperties getRenderPropertiesIndividual()
    {
        if (myIndividualProperties == null)
        {
            if (myRenderProperties instanceof DefaultTileRenderProperties)
            {
                myIndividualProperties = new ProxyTileRenderProperties((DefaultTileRenderProperties)myRenderProperties);
            }
            else
            {
                myIndividualProperties = myRenderProperties;
            }
        }

        return myIndividualProperties;
    }

    /**
     * Determine if this tile is translucent.
     *
     * @return <code>true</code> if translucent
     */
    public boolean isTranslucent()
    {
        return getRenderProperties().getOpacity() < 1.;
    }

    @Override
    protected Builder<Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    @Override
    protected TileGeometry getObservable()
    {
        return this;
    }

    /**
     * Builder for the geometry.
     *
     * @param <S> the position type used by this geometry.
     */
    public static class Builder<S extends Position> extends AbstractTileGeometry.Builder<S>
    {
    }
}
