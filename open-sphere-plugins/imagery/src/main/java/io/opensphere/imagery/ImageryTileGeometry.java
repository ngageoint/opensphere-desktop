package io.opensphere.imagery;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.Position;

/**
 * Marker class for the ImageryTileGeometry so we can differentiate it for a
 * right click menu.
 */
public class ImageryTileGeometry extends TileGeometry
{
    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public ImageryTileGeometry(TileGeometry.Builder<?> builder, TileRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
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
        return new ImageryTileGeometry(builder, getRenderProperties(), getConstraints());
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
        return new ImageryTileGeometry(builder, getRenderProperties(), getConstraints());
    }
}
