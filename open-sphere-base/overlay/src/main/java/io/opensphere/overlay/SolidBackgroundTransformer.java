package io.opensphere.overlay;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.PluginToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/** Class that creates a simple solid background to display over the globe. */
public class SolidBackgroundTransformer extends DefaultTransformer implements PluginToolbox
{
    /** The current color. */
    private Color myCurrentColor;

    /**
     * Divide the background tiles to allow matching the size of other tiles
     * which are being rendered. This increases efficiency because the tile data
     * is re-used for tiles whose bounds match.
     */
    private final AbstractDivider<GeographicPosition> myDivider = new AbstractDivider<GeographicPosition>("background")
    {
        @Override
        public Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent)
        {
            Collection<AbstractTileGeometry<?>> result = New.list(4);
            GeographicBoundingBox bbox = (GeographicBoundingBox)parent.getBounds();
            for (GeographicBoundingBox aBox : bbox.quadSplit())
            {
                SingletonImageProvider provider = new SingletonImageProvider(myImage);
                myImageProviders.add(provider);
                ImageManager imageManager = new ImageManager((Void)null, provider);
                result.add(parent.createSubTile(aBox, (Void)null, parent.getGeneration() < 12 ? this : null, imageManager));
            }

            return result;
        }
    };

    /** The image which is used as the texture for all of my tiles. */
    private BufferedImage myImage;

    /**
     * The providers for the image which is the globe's background. Use weak
     * references so that when tiles are joined, the image managers for the
     * removed tiles can be cleaned up.
     */
    // TODO this should be a single image provider with a single image manager
    // which is shared by all of the tiles. This should be fixed with
    // VORTEX-1491
    private final Collection<SingletonImageProvider> myImageProviders = New.weakSet(32);

    /**
     * The geometries which are a solid colored globe under all other layers.
     */
    private Collection<TileGeometry> myTiles;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public SolidBackgroundTransformer(Toolbox toolbox)
    {
        super(toolbox.getDataRegistry());
    }

    @Override
    public String getDescription()
    {
        return "SolidBackgroundTrandformer";
    }

    @Override
    public synchronized void open()
    {
        super.open();
        // If publish was called before the transformer was opened, the tile may
        // have already been created. In this case, just publish it.
        if (myTiles != null)
        {
            Collection<TileGeometry> tiles = New.collection(myTiles.size());
            tiles.addAll(myTiles);
            publishGeometries(tiles, Collections.<TileGeometry>emptySet());
        }
    }

    /**
     * Create the background tile and publish if this transformer is open. If
     * this transformer is not open, the publish will occur when opened.
     */
    public synchronized void publishBackground()
    {
        Collection<TileGeometry> oldGeometries = myTiles;

        TileGeometry.Builder<GeographicPosition> tileBuilder = new TileGeometry.Builder<GeographicPosition>();

        myImage = createImage();

        // TODO this matches what is used by WMSGetCapabilitiesEnvoy, it should
        // be defined somewhere more generic.
        tileBuilder.setMinimumDisplaySize(384);
        tileBuilder.setMaximumDisplaySize(1280);
        tileBuilder.setDivider(myDivider);
        tileBuilder.setParent(null);
        tileBuilder.setRapidUpdate(true);

        // Make these tiles pickable to ensure that there geometries on the back
        // side of the earth cannot be picked.
        TileRenderProperties props = new DefaultTileRenderProperties(1, true, true);
        props.setHighlightColorARGB(TileRenderProperties.DEFAULT_COLOR);

        myTiles = createTiles(tileBuilder, props, myImage);

        Collection<TileGeometry> removes = oldGeometries == null ? Collections.<TileGeometry>emptySet() : oldGeometries;

        if (isOpen())
        {
            Collection<TileGeometry> tiles = New.collection(myTiles.size());
            tiles.addAll(myTiles);
            // There is no need to make a copy of the removes since that
            // collection will not be changed again.
            publishGeometries(tiles, removes);
        }
    }

    /**
     * Change the color of the background geometry.
     *
     * @param color The new color.
     */
    public synchronized void setColor(Color color)
    {
        myCurrentColor = color;
        if (myImageProviders != null)
        {
            myImage = createImage();
            for (SingletonImageProvider provider : myImageProviders)
            {
                provider.setImage(myImage);
            }
        }
    }

    /** Remove the background image. */
    public synchronized void unpublishBackground()
    {
        if (myTiles != null)
        {
            Collection<TileGeometry> tiles = New.collection(myTiles.size());
            tiles.addAll(myTiles);
            publishGeometries(Collections.<TileGeometry>emptySet(), tiles);
            myTiles = null;
            myImageProviders.clear();
        }
    }

    /**
     * Create a single pixel image which is colored as my current color.
     *
     * @return A newly created buffered image.
     */
    private BufferedImage createImage()
    {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        image.setRGB(0, 0, myCurrentColor.getRGB());
        return image;
    }

    /**
     * Create a set of tiles colored with the background color.
     *
     * @param builder The builder to use for constructing the tiles.
     * @param props The properties for the tiles.
     * @param image The image which is the texture for each tile.
     * @return The newly constructed tiles.
     */
    private Collection<TileGeometry> createTiles(TileGeometry.Builder<GeographicPosition> builder, TileRenderProperties props,
            BufferedImage image)
    {
        Collection<TileGeometry> tiles = New.collection(32);

        // the geographic size must divide 180
        double geographicSize = 45.;
        int steps = (int)(180. / geographicSize);

        for (int i = 0; i < steps * 2; ++i)
        {
            for (int j = 0; j < steps; ++j)
            {
                double lowerLeftLon = -180 + i * geographicSize;
                double lowerLeftLat = -90 + j * geographicSize;

                double upperRightLon = lowerLeftLon + geographicSize;
                double upperRightLat = lowerLeftLat + geographicSize;

                GeographicPosition lowerLeft = new GeographicPosition(LatLonAlt.createFromDegrees(lowerLeftLat, lowerLeftLon));
                GeographicPosition upperRight = new GeographicPosition(LatLonAlt.createFromDegrees(upperRightLat, upperRightLon));
                GeographicBoundingBox backgroundBox = new GeographicBoundingBox(lowerLeft, upperRight);

                builder.setBounds(backgroundBox);
                SingletonImageProvider provider = new SingletonImageProvider(image);
                myImageProviders.add(provider);
                builder.setImageManager(new ImageManager((Void)null, provider));

                tiles.add(new TileGeometry(builder, props, null));
            }
        }

        return tiles;
    }
}
