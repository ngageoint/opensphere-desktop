package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/**
 * Transformer for the background.
 */
class BackgroundTransformer extends DefaultTransformer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(BackgroundTransformer.class);

    /** The tile. */
    private TileGeometry myTile;

    /**
     * Constructor.
     */
    public BackgroundTransformer()
    {
        super((DataRegistry)null);
    }

    @Override
    public void open()
    {
        super.open();
        publishTile();
    }

    /**
     * Method that publishes the tile.
     */
    protected void publishTile()
    {
        if (isOpen())
        {
            Set<TileGeometry> adds;
            Set<TileGeometry> removes;
            synchronized (this)
            {
                TileGeometry oldTile = myTile;

                TileGeometry.Builder<ScreenPosition> builder = new TileGeometry.Builder<ScreenPosition>();
                int height = 40;
                int width = 500;
                ScreenPosition upperLeft = new ScreenPosition(0, -height);
                ScreenPosition lowerRight = new ScreenPosition(-1, -1);
                builder.setBounds(new ScreenBoundingBox(upperLeft, lowerRight));
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D graphics = image.createGraphics();
                graphics.setColor(Color.DARK_GRAY);
                graphics.draw(new Rectangle2D.Double(0, 0, width, height));
                ImageManager imageManager = new ImageManager(null, new SingletonImageProvider(image));
                builder.setImageManager(imageManager);
                TileRenderProperties props = new DefaultTileRenderProperties(ZOrderRenderProperties.TOP_Z - 100, true, false);
                final float opacity = .7f;
                props.setOpacity(opacity);
                myTile = new TileGeometry(builder, props, null);
                adds = Collections.singleton(myTile);

                removes = oldTile == null ? Collections.<TileGeometry>emptySet() : Collections.singleton(oldTile);
            }
            if (LOGGER.isTraceEnabled() && (!adds.isEmpty() || !removes.isEmpty()))
            {
                LOGGER.trace("publishing " + adds.size() + " and " + removes.size() + " removes");
                LOGGER.trace("adds: " + adds);
                LOGGER.trace("removes: " + removes);
            }
            if (!adds.isEmpty() || !removes.isEmpty())
            {
                publishGeometries(adds, removes);
            }
        }
    }
}
