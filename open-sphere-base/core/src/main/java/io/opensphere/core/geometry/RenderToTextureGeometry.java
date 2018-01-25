package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.Set;

import io.opensphere.core.geometry.renderproperties.DefaultZOrderRenderProperties;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Render a set of geometries together as a set to a texture. Provide a tile
 * geometry with the texture as its image.
 */
public class RenderToTextureGeometry extends AbstractGeometryGroup
{
    /** Color which will be used to clear the buffer before rendering. */
    private final Color myBackgroundColor;

    /** Viewers used for this geometry group. */
    private final MapContext<?> myMapContext;

    /** Box which defines the frame buffer size for offline rendering. */
    private final ScreenBoundingBox myRenderBox;

    /** Tile geometry to render as the aggregated texture. */
    private volatile TileGeometry myTileGeometry;

    /**
     * Construct me.
     *
     * @param builder The builder for the geometry.
     */
    public RenderToTextureGeometry(Builder builder)
    {
        super(builder, new DefaultZOrderRenderProperties(0, true));
        myRenderBox = builder.getRenderBox();
        myTileGeometry = builder.getTileGeometry();
        myBackgroundColor = builder.getBackgroundColor();
        myMapContext = builder.getMapContext();
        Utilities.checkNull(myMapContext, "builder.getMapContext()");
        Set<Geometry> initialGeometries = builder.getInitialGeometries();
        addInitialGeometries(initialGeometries);
    }

    @Override
    public RenderToTextureGeometry clone()
    {
        return (RenderToTextureGeometry)super.clone();
    }

    @Override
    public Builder createBuilder()
    {
        Builder builder = (Builder)super.doCreateBuilder();
        builder.setRenderBox(getRenderBox());
        builder.setTileGeometry(getTileGeometry());
        builder.setMapContext(getMapContext());
        return builder;
    }

    /**
     * Get the backgroundColor.
     *
     * @return the backgroundColor
     */
    public Color getBackgroundColor()
    {
        return myBackgroundColor;
    }

    /**
     * Get the mapContext.
     *
     * @return the mapContext
     */
    public MapContext<?> getMapContext()
    {
        return myMapContext;
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        if (myTileGeometry != null)
        {
            return myTileGeometry.getPositionType();
        }

        return null;
    }

    /**
     * Get renderBox.
     *
     * @return renderBox
     */
    public ScreenBoundingBox getRenderBox()
    {
        return myRenderBox;
    }

    /**
     * Get the tile geometry.
     *
     * @return the tileGeometry
     */
    public TileGeometry getTileGeometry()
    {
        return myTileGeometry;
    }

    /**
     * Set the tileGeometry.
     *
     * @param tileGeometry the tileGeometry to set
     */
    public void setTileGeometry(TileGeometry tileGeometry)
    {
        myTileGeometry = tileGeometry;
    }

    @Override
    protected Builder createRawBuilder()
    {
        return new Builder();
    }

    /** Builder for the geometry. */
    public static class Builder extends AbstractGeometryGroup.Builder
    {
        /** Color which will be used to clear the buffer before rendering. */
        private Color myBackgroundColor;

        /** Viewers for the group. */
        private MapContext<?> myMapContext;

        /** BoundingBox for the render buffer dimensions. */
        private ScreenBoundingBox myRenderBox;

        /** Tile to render as the aggregated geometry. */
        private TileGeometry myTileGeometry;

        /**
         * Get the backgroundColor.
         *
         * @return the backgroundColor
         */
        public Color getBackgroundColor()
        {
            return myBackgroundColor;
        }

        /**
         * Accessor for the mapContext.
         *
         * @return The mapContext.
         */
        public MapContext<?> getMapContext()
        {
            return myMapContext;
        }

        /**
         * Get the renderBox.
         *
         * @return the renderBox
         */
        public ScreenBoundingBox getRenderBox()
        {
            return myRenderBox;
        }

        /**
         * Get the tileGeometry.
         *
         * @return the tileGeometry
         */
        public TileGeometry getTileGeometry()
        {
            return myTileGeometry;
        }

        /**
         * Set the backgroundColor.
         *
         * @param backgroundColor the backgroundColor to set
         */
        public void setBackgroundColor(Color backgroundColor)
        {
            myBackgroundColor = backgroundColor;
        }

        /**
         * Mutator for the mapContext.
         *
         * @param mapContext The mapContext to set.
         */
        public void setMapContext(MapContext<?> mapContext)
        {
            myMapContext = mapContext;
        }

        /**
         * Set the renderBox.
         *
         * @param renderBox the renderBox to set
         */
        public void setRenderBox(ScreenBoundingBox renderBox)
        {
            myRenderBox = renderBox;
        }

        /**
         * Set the tileGeometry.
         *
         * @param tileGeometry the tileGeometry to set
         */
        public void setTileGeometry(TileGeometry tileGeometry)
        {
            myTileGeometry = tileGeometry;
        }
    }
}
