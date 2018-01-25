package io.opensphere.core.hud.framework;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Set;
import java.util.function.Consumer;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.RenderToTextureGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.hud.util.FrameBoundsHelper;
import io.opensphere.core.hud.util.PositionBoundedFrame;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.Viewer;

/**
 * A window differs from a regular frame in that it is responsible for
 * publishing components.
 *
 * @param <S> Layout constraint type
 * @param <T> Layout type
 */
public abstract class Window<S extends LayoutConstraints, T extends AbstractLayout<S>> extends Frame<S, T>
        implements PositionBoundedFrame
{
    /** The spacing between tool and the view-port border. */
    private static final int ourBorderWidth = 20;

    /** True when the frame has been published. */
    private boolean myDisplayed;

    /** The Frame bounds helper. */
    private FrameBoundsHelper myFrameBoundsHelper;

    /**
     * Location of the HUD window on the earth or null if it is displayed
     * normally.
     */
    private GeographicBoundingBox myGeographicLocation;

    /** Location (predefined locations in view port). */
    private final ToolLocation myLocationHint;

    /** Listen to events from the main viewer. */
    private final ViewChangeListener myMainViewListener = new ViewChangeListener()
    {
        @Override
        public void viewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type)
        {
            if (type == ViewChangeSupport.ViewChangeType.WINDOW_RESIZE)
            {
                final Rectangle frameBounds = getBounds();
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Rectangle adjustedBounds = myFrameBoundsHelper.getAdjustedFrameBoundsForMainFrameChange(frameBounds);
                        if (!frameBounds.equals(adjustedBounds))
                        {
                            setFrameLocation(ScreenBoundingBox.fromRectangle(adjustedBounds));
                            replaceTile();
                        }
                    }
                });
            }
        }
    };

    /** Original size. */
    private final Dimension myOriginalSize;

    /** Resize behavior. */
    private final ResizeOption myResizeBehavior;

    /** Transformer for publishing geometries. */
    private final TransformerHelper myTransformer;

    /** Listener for window movements. */
    private volatile Consumer<ScreenPosition> myMoveListener;

    /**
     * Construct me.
     *
     * @param hudTransformer Transformer for publishing geometries.
     * @param location location of the window on the screen. When used with an
     *            geographicLocation, this is used to determine the size of the
     *            off-line rendering buffer.
     * @param geographicLocation When non-null this will cause the HUD to be
     *            displayed on the earth.
     * @param zOrder the z-order for the window.
     */
    public Window(TransformerHelper hudTransformer, ScreenBoundingBox location, GeographicBoundingBox geographicLocation,
            int zOrder)
    {
        super(null, location, zOrder);
        myTransformer = hudTransformer;
        myGeographicLocation = geographicLocation;
        myOriginalSize = new Dimension((int)location.getWidth(), (int)location.getHeight());
        myResizeBehavior = null;
        myLocationHint = null;
    }

    /**
     * Construct me.
     *
     * @param hudTransformer Transformer for publishing geometries.
     * @param location location of the window on the screen.
     * @param zOrder the z-order for the window.
     */
    public Window(TransformerHelper hudTransformer, ScreenBoundingBox location, int zOrder)
    {
        super(null, location, zOrder);
        myTransformer = hudTransformer;
        myOriginalSize = new Dimension((int)location.getWidth(), (int)location.getHeight());
        myResizeBehavior = null;
        myLocationHint = null;
    }

    /**
     * Construct me.
     *
     * @param hudTransformer The transformer.
     * @param size The bounding box we will use for size (but not location on
     *            screen).
     * @param locationHint The predetermined location.
     * @param resize The resize behavior.
     * @param zOrder the z-order for the window.
     */
    public Window(TransformerHelper hudTransformer, ScreenBoundingBox size, ToolLocation locationHint, ResizeOption resize,
            int zOrder)
    {
        super(null, size, zOrder);
        myTransformer = hudTransformer;

        // We are going to use the dimensions from screen bounding box passed
        // in, but
        // set the actual position here.
        myOriginalSize = new Dimension((int)size.getWidth(), (int)size.getHeight());
        myResizeBehavior = resize;
        myLocationHint = locationHint;
        myFrameBoundsHelper = new FrameBoundsHelper(myTransformer.getToolbox().getPreferencesRegistry(),
                myTransformer.getToolbox().getUIRegistry().getMainFrameProvider(), this);

        // Register as a listener for view change events
        myTransformer.getToolbox().getMapManager().getViewChangeSupport().addViewChangeListener(myMainViewListener);
    }

    @Override
    public synchronized void closeWindow()
    {
        myDisplayed = false;
        myTransformer.closeFrame(this);
        handleCleanupListeners();
    }

    /**
     * Display or update the display for this window.
     */
    public synchronized void display()
    {
        if (myDisplayed)
        {
            return;
        }

        // Create the rttg.
        // add the map manager to the rttg
        setupRenderToTexture();

        myDisplayed = true;

        // I am done initializing, tell the transformer to publish my
        // geometries.
        myTransformer.displayFrame(this);
    }

    /**
     * Get the geographicLocation.
     *
     * @return the geographicLocation
     */
    public GeographicBoundingBox getGeographicLocation()
    {
        return myGeographicLocation;
    }

    @Override
    public BoundingBox<?> getTileLocation()
    {
        if (myGeographicLocation != null)
        {
            return myGeographicLocation;
        }

        return super.getTileLocation();
    }

    @Override
    public TransformerHelper getTransformer()
    {
        return myTransformer;
    }

    @Override
    public void handleCleanupListeners()
    {
        myTransformer.getToolbox().getMapManager().getViewChangeSupport().removeViewChangeListener(myMainViewListener);
        super.handleCleanupListeners();
    }

    /** Reposition this window to the default location. */
    public void moveToDefaultLocation()
    {
        int width = myTransformer.getToolbox().getMapManager().getStandardViewer().getViewportWidth();
        int height = myTransformer.getToolbox().getMapManager().getStandardViewer().getViewportHeight();
        setFrameLocation(computeLocation(width, height));
    }

    @Override
    public synchronized void moveWindow(ScreenPosition delta)
    {
        ScreenBoundingBox finalLoc;
        if (getRenderToTextureGeometry().getTileGeometry().getBounds() instanceof GeoScreenBoundingBox)
        {
            ScreenBoundingBox newLoc = getAdjustedFrameLocation(delta);
            GeoScreenBoundingBox origGeoScreenLoc = (GeoScreenBoundingBox)getRenderToTextureGeometry().getTileGeometry()
                    .getBounds();
            finalLoc = new GeoScreenBoundingBox(newLoc.getUpperLeft(), newLoc.getLowerRight(), origGeoScreenLoc.getAnchor());
        }
        else if (getRenderToTextureGeometry().getTileGeometry().getBounds() instanceof ScreenBoundingBox)
        {
            Rectangle moveTo = getAdjustedFrameLocation(delta).asRectangle();
            final Rectangle adjustedBounds = myFrameBoundsHelper.getAdjustedFrameBoundsForInternalFrameChange(moveTo, true);
            finalLoc = ScreenBoundingBox.fromRectangle(adjustedBounds);
        }
        else
        {
            // TODO enzio - handle geographic moves?
            return;
        }

        setFrameLocation(finalLoc);
        replaceTile();

        Consumer<ScreenPosition> moveListener = myMoveListener;
        if (moveListener != null)
        {
            moveListener.accept(finalLoc.getUpperLeft());
        }
    }

    @Override
    public synchronized void resizeWindow(ScreenPosition delta)
    {
        handleCleanupListeners();
        myTransformer.closeFrame(this);
        myDisplayed = false;

        ScreenBoundingBox frameBox = getFrameLocation();
        int width = (int)(frameBox.getLowerRight().getX() + delta.getX());
        int height = (int)(frameBox.getLowerRight().getY() + delta.getY());

        ScreenBoundingBox newFrameBox = new ScreenBoundingBox(frameBox.getUpperLeft(), new ScreenPosition(width, height));
        setFrameLocation(newFrameBox);
        init();

        display();
    }

    /**
     * Set the geographicLocation.
     *
     * @param geographicLocation the geographicLocation to set
     */
    public void setGeographicLocation(GeographicBoundingBox geographicLocation)
    {
        myGeographicLocation = geographicLocation;
    }

    /**
     * Sets the moveListener.
     *
     * @param moveListener the moveListener
     */
    public void setMoveListener(Consumer<ScreenPosition> moveListener)
    {
        myMoveListener = moveListener;
    }

    @Override
    public synchronized void updateGeometries(Set<? extends Geometry> adds, Set<? extends Geometry> removes)
    {
        if (!myDisplayed)
        {
            // If we are not displaying, then we do not need to do this. The
            // correct geometries will be published when the display occurs.
            return;
        }
        RenderToTextureGeometry rttg = getRenderToTextureGeometry();
        if (rttg != null)
        {
            rttg.receiveObjects(rttg, adds, removes);
        }
    }

    /**
     * Computes the screen position using the tool location and view port sizes.
     *
     * @param width The new width of view port.
     * @param height The new height of view port.
     * @return ScreenBoundingBox The location.
     */
    private ScreenBoundingBox computeLocation(int width, int height)
    {
        double scale = computeScale(width);

        double scaledWidth = scale * myOriginalSize.getWidth();
        double scaledHeight = scale * myOriginalSize.getHeight();

        double x;
        double y;

        switch (myLocationHint)
        {
            case SOUTHEAST:
                x = width - scaledWidth - ourBorderWidth;
                y = height - scaledHeight - ourBorderWidth;
                break;
            case NORTHEAST:
                x = width - scaledWidth - ourBorderWidth;
                y = ourBorderWidth;
                break;
            case SOUTHWEST:
                x = ourBorderWidth;
                y = height - scaledHeight - ourBorderWidth;
                break;
            case NORTHWEST:
                x = ourBorderWidth;
                y = ourBorderWidth;
                break;
            case NORTH:
                x = (double)width / 2 - scaledWidth / 2;
                y = ourBorderWidth;
                break;
            case EAST:
                x = width - scaledWidth - ourBorderWidth;
                y = (double)height / 2 - scaledHeight / 2;
                break;
            case SOUTH:
                x = (double)width / 2 - scaledWidth / 2;
                y = height - scaledHeight - ourBorderWidth;
                break;
            case WEST:
                x = ourBorderWidth;
                y = (double)height / 2 - scaledHeight / 2;
                break;
            default: // use south
                x = (double)width / 2 - scaledWidth / 2;
                y = height - scaledHeight - ourBorderWidth;
                break;
        }

        ScreenPosition upLeft = new ScreenPosition((int)x, (int)y);
        ScreenPosition lowRight = new ScreenPosition((int)(x + scaledWidth), (int)(y + scaledHeight));
        return new ScreenBoundingBox(upLeft, lowRight);
    }

    /**
     * Computes the scale value for a tool using the viewport dimensions and
     * resize behavior.
     *
     * @param viewPortWidth The width used to compute the scale
     * @return The scaled value.
     */
    private double computeScale(int viewPortWidth)
    {
        double scale = 1.;

        final double toViewportScale = 0.2;

        if (myResizeBehavior == ResizeOption.RESIZE_SHRINK_ONLY)
        {
            scale = Math.min(1d, toViewportScale * viewPortWidth / myOriginalSize.getWidth());
        }
        else if (myResizeBehavior == ResizeOption.RESIZE_STRETCH)
        {
            scale = toViewportScale * viewPortWidth / myOriginalSize.getWidth();
        }
        else if (myResizeBehavior == ResizeOption.RESIZE_KEEP_FIXED_SIZE)
        {
            scale = 1d;
        }

        return scale;
    }

    /**
     * Replace the on-screen tile.
     */
    private synchronized void replaceTile()
    {
        if (!myDisplayed)
        {
            return;
        }
        TileGeometry oldTile = getRenderToTextureGeometry().getTileGeometry();
        TileGeometry newTile = createFrameTile(oldTile.getImageManager());
        getRenderToTextureGeometry().setTileGeometry(newTile);

        handleWindowMoved();

        // Replace the on-screen tile.
        myTransformer.replaceGeometry(oldTile, newTile);
    }

    /** The resize option that will be used when view port size is changed. */
    public enum ResizeOption
    {
        /** Does not modify the item size when the view port changes size. */
        RESIZE_KEEP_FIXED_SIZE,

        /**
         * On view port resize, scales the item to occupy a constant relative
         * size of the view port, but not larger than the item's inherent size
         * scaled by the layer's item scale factor.
         */
        RESIZE_SHRINK_ONLY,

        /**
         * On view port resize, scales the item to occupy a constant relative
         * size of the view port.
         */
        RESIZE_STRETCH
    }

    /**
     * The location in relation to the view port. These are standard areas where
     * this window might be.
     */
    public enum ToolLocation
    {
        /** East. */
        EAST,

        /** North. */
        NORTH,

        /** North East corner. */
        NORTHEAST,

        /** North West corner. */
        NORTHWEST,

        /** South. */
        SOUTH,

        /** South East corner. */
        SOUTHEAST,

        /** South West corner. */
        SOUTHWEST,

        /** West. */
        WEST
    }
}
