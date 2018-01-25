package io.opensphere.controlpanels.recording;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.api.Transformer;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;

/** Play a video on the GL canvas. */
public class CanvasVideoPlayer extends AbstractVideoPlayer
{
    /** Image type for the frame to render to. */
    private static int ourImgType = BufferedImage.TYPE_4BYTE_ABGR;

    /** A dirty region which covers the entire image. */
    private final Collection<ImageManager.DirtyRegion> myDirtyRegions = New.collection(1);

    /** The image provider for the tile. */
    private SingletonImageProvider myImageProvider;

    /** The bounds for the tile into which the video is played. */
    private final BoundingBox<? extends Position> myLocation;

    /** The tile into which the video is played. */
    private TileGeometry myTileGeometry;

    /** The transformer used to publish the tile. */
    private final Transformer myTransformer;

    /**
     * Constructor.
     *
     * @param transfomer The transformer used to publish the tile.
     * @param location The bounds for the tile into which the video is played.
     */
    public CanvasVideoPlayer(Transformer transfomer, BoundingBox<? extends Position> location)
    {
        myTransformer = transfomer;
        myLocation = location;
    }

    @Override
    void closeMedium()
    {
        if (myTileGeometry != null)
        {
            myTransformer.publishGeometries(Collections.<TileGeometry>emptyList(), Collections.singleton(myTileGeometry));
            myTileGeometry = null;
        }
    }

    @Override
    void initializeMedium(int width, int height)
    {
        if (myTileGeometry == null)
        {
            myTileGeometry = generateTile(width, height);
            myTransformer.publishGeometries(Collections.singleton(myTileGeometry), Collections.<TileGeometry>emptyList());
            myDirtyRegions.add(new ImageManager.DirtyRegion(0, width, 0, height));
        }
    }

    @Override
    void updateMedium(BufferedImage image)
    {
        myTileGeometry.getImageManager().addDirtyRegions(myDirtyRegions);
        myImageProvider.setImage(image);
    }

    /**
     * Create the tile into which the video is played.
     *
     * @param videoWidth The pixel width of the video.
     * @param videoHeight The pixel height of the video.
     * @return The newly generated tile.
     */
    private TileGeometry generateTile(int videoWidth, int videoHeight)
    {
        TileGeometry.Builder<Position> tileBuilder = new TileGeometry.Builder<Position>();
        BufferedImage initialImage = new BufferedImage(videoWidth, videoHeight, ourImgType);
        Graphics2D graphics = initialImage.createGraphics();
        graphics.setBackground(new Color(0f, 0f, 0f, 0f));
        graphics.clearRect(0, 0, videoWidth, videoHeight);

        myImageProvider = new SingletonImageProvider(initialImage);
        tileBuilder.setImageManager(new ImageManager((Void)null, myImageProvider));
        tileBuilder.setDivider(null);
        tileBuilder.setParent(null);
        tileBuilder.setRapidUpdate(true);
        tileBuilder.setBounds(myLocation);

        TileRenderProperties props = new DefaultTileRenderProperties(10100, true, false);
        props.setRenderingOrder(5);
        props.setOpacity(1.0f);
        props.setHidden(false);

        return new TileGeometry(tileBuilder, props, null);
    }
}
