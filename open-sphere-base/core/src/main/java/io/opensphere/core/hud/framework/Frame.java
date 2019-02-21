package io.opensphere.core.hud.framework;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.RenderToTextureGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.util.RenderToTextureImageProvider;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.core.viewer.impl.SimpleMapContext;
import io.opensphere.core.viewer.impl.Viewer3D;

/**
 * Frames are rendered to a texture. All positions for the sub-geometries of a
 * frame start at (0,0).
 *
 * @param <S> Layout constraint type
 * @param <T> Layout type
 */
public abstract class Frame<S extends LayoutConstraints, T extends AbstractLayout<S>> extends Panel<S, T>
{
    /**
     * Background color which will be used to clear the buffer before rendering.
     */
    private Color myBackgroundColor = new Color(0f, 0f, 0f, 0f);

    /**
     * The Box which defines this frame. This should be from (0, 0) to (size x,
     * size y).
     */
    private ScreenBoundingBox myFrameBounds;

    /** Geometry which will be rendered to the offline frame buffer. */
    private RenderToTextureGeometry myRenderToTextureGeometry;

    /**
     * The frame z-order.
     */
    private int myFrameZOrder;

    /**
     * Construct me.
     *
     * @param parent parent component.
     */
    public Frame(Component parent)
    {
        super(parent);
        myFrameZOrder = 0;
    }

    /**
     * Construct a HUDFrame. The upper left corner is always (0, 0) for the
     * frame, even if its position in an owning frame is not.
     *
     * @param parent parent component.
     * @param location location of the window on the screen.
     */
    public Frame(Component parent, ScreenBoundingBox location)
    {
        this(parent, location, 0);
    }

    /**
     * Construct a HUDFrame. The upper left corner is always (0, 0) for the
     * frame, even if its position in an owning frame is not.
     *
     * @param parent parent component.
     * @param location location of the window on the screen.
     * @param zOrder z-order for the frame.
     */
    public Frame(Component parent, ScreenBoundingBox location, int zOrder)
    {
        super(parent, location);
        myFrameBounds = new ScreenBoundingBox(new ScreenPosition(0, 0),
                new ScreenPosition((int)location.getWidth(), (int)location.getHeight()));
        myFrameZOrder = zOrder;
    }

    /**
     * Create a map manager for the frame. The default behavior is to create one
     * with a 2d viewer.
     *
     * @return newly created map manager.
     */
    public MapContext<Viewer3D> createMapContext()
    {
        MapContext<Viewer3D> mapContext = new SimpleMapContext<>(new ScreenViewer(false), new Viewer3D(new Viewer3D.Builder(), false));
        mapContext.reshape((int)myFrameBounds.getLowerRight().getX(), (int)myFrameBounds.getLowerRight().getY());

        return mapContext;
    }

    /**
     * Get the frame location adjusted by the delta.
     *
     * @param delta Amount of position change.
     * @return adjusted frame location.
     */
    public ScreenBoundingBox getAdjustedFrameLocation(ScreenPosition delta)
    {
        ScreenBoundingBox loc = getFrameLocation();
        ScreenPosition upperLeft = loc.getUpperLeft().add(delta);
        ScreenPosition lowerRight = loc.getLowerRight().add(delta);
        return new ScreenBoundingBox(upperLeft, lowerRight);
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

    @Override
    public ScreenBoundingBox getDrawBounds()
    {
        return myFrameBounds;
    }

    @Override
    public Set<Geometry> getGeometries()
    {
        Set<Geometry> geoms = new HashSet<>();
        geoms.add(getRenderToTextureGeometry());
        geoms.add(getRenderToTextureGeometry().getTileGeometry());

        return geoms;
    }

    /**
     * Get the renderToTextureGeometry.
     *
     * @return the renderToTextureGeometry
     */
    public RenderToTextureGeometry getRenderToTextureGeometry()
    {
        return myRenderToTextureGeometry;
    }

    /**
     * Aggregate all of the geometries which make up this component.
     *
     * @return Geometries of mine and my children.
     */
    public Set<Geometry> getSubGeometries()
    {
        return super.getGeometries();
    }

    /**
     * Get the bounding box location of the tile into which the frame is
     * rendered.
     *
     * @return on screen location of the tile.
     */
    public BoundingBox<?> getTileLocation()
    {
        return getFrameLocation();
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

    @Override
    public void setFrameLocation(ScreenBoundingBox frameLocation)
    {
        super.setFrameLocation(frameLocation);
        myFrameBounds = new ScreenBoundingBox(new ScreenPosition(0, 0),
                new ScreenPosition((int)frameLocation.getWidth(), (int)frameLocation.getHeight()));
    }

    /**
     * Set the renderToTextureGeometry.
     *
     * @param renderToTextureGeometry the renderToTextureGeometry to set
     */
    public void setRenderToTextureGeometry(RenderToTextureGeometry renderToTextureGeometry)
    {
        myRenderToTextureGeometry = renderToTextureGeometry;
    }

    /** set up the map manager. */
    public void setupRenderToTexture()
    {
        MapContext<?> mapContext = createMapContext();

        TileGeometry tileGeometry = createFrameTile(null);

        RenderToTextureGeometry.Builder rttgBuilder = new RenderToTextureGeometry.Builder();
        rttgBuilder.setRenderBox(myFrameBounds);
        rttgBuilder.setTileGeometry(tileGeometry);
        rttgBuilder.setMapContext(mapContext);
        rttgBuilder.setBackgroundColor(myBackgroundColor);
        rttgBuilder.setInitialGeometries(getSubGeometries());
        RenderToTextureGeometry rttg = new RenderToTextureGeometry(rttgBuilder);

        setRenderToTextureGeometry(rttg);
    }

    /**
     * Adjust the existing geometry to accommodate the changes.
     */
    protected void adjustRenderToTexture()
    {
        RenderToTextureGeometry rttg = getRenderToTextureGeometry();

        TileGeometry newTile = createFrameTile(rttg.getTileGeometry().getImageManager());
        rttg.setTileGeometry(newTile);

        ScreenBoundingBox frame = getFrameLocation();
        getRenderToTextureGeometry().getMapContext().reshape((int)frame.getWidth(), (int)frame.getHeight());
    }

    /**
     * Create the tile used to display this frame.
     *
     * @param imageManager manager for the texture.
     * @return the newly create frame tile.
     */
    protected TileGeometry createFrameTile(ImageManager imageManager)
    {
        ImageManager manager = imageManager;
        TileGeometry.Builder<? extends Position> builder;
        if (getTileLocation() instanceof GeographicBoundingBox)
        {
            GeographicBoundingBox tileLoc = (GeographicBoundingBox)getTileLocation();

            TileGeometry.Builder<GeographicPosition> tileBuilder = getGenericTileBuilder();
            tileBuilder.setBounds(tileLoc);
            builder = tileBuilder;
        }
        else
        {
            ScreenBoundingBox tileLoc = (ScreenBoundingBox)getTileLocation();

            TileGeometry.Builder<ScreenPosition> tileBuilder = getGenericTileBuilder();
            tileBuilder.setBounds(tileLoc);
            builder = tileBuilder;
        }
        if (manager == null)
        {
            RenderToTextureImageProvider imageProvider = new RenderToTextureImageProvider();
            manager = new ImageManager(null, imageProvider);
        }
        builder.setImageManager(manager);
        builder.setRapidUpdate(true);

        return new TileGeometry(builder, new DefaultTileRenderProperties(getFrameZOrder(), true, true), null);
    }

    /**
     * Get the z-order for the frame.
     *
     * @return The z-order.
     */
    protected int getFrameZOrder()
    {
        return myFrameZOrder;
    }
}
