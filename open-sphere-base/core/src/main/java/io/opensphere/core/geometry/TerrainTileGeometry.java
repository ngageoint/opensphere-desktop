package io.opensphere.core.geometry;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.Position;
import io.opensphere.core.terrain.util.ElevationImageReader;
import io.opensphere.core.util.Utilities;

/**
 * A {@link Geometry} that models an image that covers a rectangular area. If
 * this can be split into sub-tiles, it has a
 * {@link io.opensphere.core.geometry.AbstractTileGeometry.Divider} that knows
 * how to do that.
 */
public class TerrainTileGeometry extends AbstractTileGeometry<TerrainTileGeometry>
{
    /** The reader for extracting elevation data associated with this tile. */
    private final ElevationImageReader myReader;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param layerId The id of the layer this geometry belongs to.
     */
    public TerrainTileGeometry(Builder<?> builder, ZOrderRenderProperties renderProperties, String layerId)
    {
        super(builder, renderProperties, layerId);
        Utilities.checkNull(builder.getImageManager(), "builder.getImageManager()");
        myReader = builder.getElevationReader();
    }

    @Override
    public TerrainTileGeometry clone()
    {
        return (TerrainTileGeometry)super.clone();
    }

    @Override
    public Builder<Position> createBuilder()
    {
        Builder<Position> builder = (Builder<Position>)super.doCreateBuilder();
        builder.setElevationReader(myReader);
        return builder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractTileGeometry<?> createSubTile(BoundingBox<? extends Position> bbox, Object imageKey,
            io.opensphere.core.geometry.AbstractTileGeometry.Divider<? extends Position> divider, ImageManager imageManager)
    {
        Builder<Position> builder = createBuilder();
        builder.setParent(this);
        builder.setBounds(bbox);
        builder.setDivider((Divider<Position>)divider);
        builder.setImageManager(imageManager);
        return new TerrainTileGeometry(builder, getRenderProperties(), getLayerId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public TerrainTileGeometry createSubTile(BoundingBox<? extends Position> bbox, Object imageKey,
            Divider<? extends Position> divider)
    {
        Builder<Position> builder = createBuilder();
        builder.setParent(this);
        builder.setBounds(bbox);
        builder.setDivider((Divider<Position>)divider);
        builder.setImageManager(new ImageManager(imageKey, (ImageProvider<Object>)getImageManager().getImageProvider()));
        return new TerrainTileGeometry(builder, getRenderProperties(), getLayerId());
    }

    /**
     * Get the reader.
     *
     * @return the reader
     */
    public ElevationImageReader getReader()
    {
        return myReader;
    }

    @Override
    protected Builder<Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    @Override
    protected TerrainTileGeometry getObservable()
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
        /**
         * The reader for extracting elevation data associated with the tile.
         */
        private ElevationImageReader myElevationReader;

        /**
         * Get the reader.
         *
         * @return the reader
         */
        public ElevationImageReader getElevationReader()
        {
            return myElevationReader;
        }

        /**
         * Set the reader.
         *
         * @param reader the reader to set
         */
        public void setElevationReader(ElevationImageReader reader)
        {
            myElevationReader = reader;
        }
    }
}
