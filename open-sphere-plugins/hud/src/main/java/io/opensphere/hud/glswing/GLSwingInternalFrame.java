package io.opensphere.hud.glswing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.apache.log4j.Logger;

import com.jidesoft.icons.JideIconsFactory.DockableFrame;

import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.hud.awt.HUDFrameBoundsHandler;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.util.FrameBoundsHelper;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreeTuple;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.PositionConverter;

/**
 * This is an implementation of a JInternalFrame which should be displayed
 * behind the content layer. The image from the frame will be painted to a
 * BufferedImage which will in turn be rendered as a tile through the pipeline.
 */
@SuppressWarnings("PMD.GodClass")
public class GLSwingInternalFrame
{
    /** An empty pane for when the frame is rolled up. */
    private static final JComponent EMPTY_PANE;

    /** An executor to use for scheduling tasks. */
    private static final ScheduledExecutorService EXECUTOR = ProcrastinatingExecutor.protect(new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("GLSwingInternalFrame"), SuppressableRejectedExecutionHandler.getInstance()));

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GLSwingInternalFrame.class);

    /** Image type for the frame to render to. */
    private static int ourImgType = BufferedImage.TYPE_4BYTE_ABGR;

    /**
     * Handler for changes to the bounds of the frame. For moves, the tile will
     * be moved, but the frame will not be moved until dragging is completed.
     * For resizing, the frame size will only be updated periodically to enhance
     * performance.
     */
    private final HUDFrameBoundsHandler myBoundsHandler = new HUDFrameBoundsHandler()
    {
        @Override
        public void boundsSet(final int x, final int y, final int width, final int height)
        {
            EventQueueUtilities.runOnEDT(() -> handleBoundsSet(new Rectangle(x, y, width, height)));
        }
    };

    /**
     * Listener for when the docking frame is closed. There only needs to be a
     * listener for this when the frame has been popped out.
     */
    private WindowListener myDockCloseListener;

    /**
     * The pointer to the current image buff we are drawing to.
     */
    private BufferedImage myDrawingImage;

    /**
     * The pointer to the current image graphics buff we are drawing to.
     */
    private Graphics2D myDrawingImageGraphics;

    /** Helper class for handling AWTEvent processing. */
    private GLSwingEventListenerHelper myEventHelper;

    /** Helper for validating frame position when changes occur. */
    private final FrameBoundsHelper myFrameBoundsHelper;

    /** True when this frame is already closed. */
    private boolean myFrameClosed;

    /** Executor to handle resizing the JInternalFrame while the . */
    private final ProcrastinatingExecutor myFrameResizeExecutor;

    /** The swing frame which is hidden behind the GLCanvas. */
    private final HUDJInternalFrame myHUDFrame;

    /** Image of the JInternalFrame. */
    private BufferedImage myImageBuff1;

    /** Image of the JInternalFrame. */
    private BufferedImage myImageBuff2;

    /** Graphics which comes from my image. */
    private Graphics2D myImageGraphicsBuff1;

    /** Graphics which comes from my image. */
    private Graphics2D myImageGraphicsBuff2;

    /** The manager for the image which represents this internal frame. */
    private ImageManager myImageManager;

    /** The image provider for the tile. */
    private SingletonImageProvider myImageProvider = new SingletonImageProvider((BufferedImage)null);

    /**
     * Keep track of when we have taken the frame off of the HUD so that we do
     * not attempt image manipulation when we are not rendering the painted
     * frame.
     */
    private boolean myPoppedOut;

    /**
     * Helper for converting positions between geographic, model, and window
     * coordinates.
     */
    private final PositionConverter myPositionConverter;

    /**
     * The first item is the contents of the frame to restore when un-rolling
     * the window, the second item is the size of the frame before rolling it
     * up. If the frame is popped out after rolling it up, the contents pane may
     * be resized, this is used to restore the original size. The third item is
     * whether the frame was resizable at the time it was rolled up.
     */
    private ThreeTuple<JComponent, Rectangle, Boolean> myRolledContents;

    /** Last published tile geometry if one is published. */
    private TileGeometry myTile;

    /**
     * The size of the tile being rendered may be different from the the frame's
     * size during resizing. This size is the size of the tile being rendered.
     * No locking is done for access to the tiles bounds, it should only be
     * referenced while on the EDT.
     */
    private Rectangle myTileBounds;

    /** The transformer I will use to publish geometries. */
    private final TransformerHelper myTransformer;

    /** Lock for tile replacement through the transformer. */
    private final ReentrantLock myTransformerLock = new ReentrantLock();

    static
    {
        EMPTY_PANE = new JPanel();
        EMPTY_PANE.setSize(0, 0);
        EMPTY_PANE.getInsets().set(0, 0, 0, 0);
    }

    /**
     * Constructor.
     *
     * @param transformer transformer for publishing geometries.
     * @param frame Internal frame which will be mirrored as a tile.
     */
    public GLSwingInternalFrame(TransformerHelper transformer, HUDJInternalFrame frame)
    {
        myTransformer = transformer;
        myHUDFrame = frame;

        Supplier<? extends JFrame> frameProvider = transformer.getToolbox().getUIRegistry().getMainFrameProvider();
        myFrameBoundsHelper = new FrameBoundsHelper(myTransformer.getToolbox().getPreferencesRegistry(), frameProvider,
                myHUDFrame.getInternalFrame());

        // set the image manager before the bounds listener to make sure we do
        // not receive bounds events before we have the ability to produce an
        // image.
        myHUDFrame.getInternalFrame().setFrameBoundsListener(myBoundsHandler);
        myHUDFrame.getInternalFrame().setFrameBoundsHelper(myFrameBoundsHelper);
        myFrameResizeExecutor = new ProcrastinatingExecutor(EXECUTOR, 100, 250);

        myPositionConverter = new PositionConverter(transformer.getToolbox().getMapManager());

        handleRegistration();

        if (myHUDFrame.getInternalFrame().isVisible())
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    paintImage((Collection<Rectangle>)null);
                    publishTile();
                }
            });
        }

        Boolean rolledProp = (Boolean)((JComponent)myHUDFrame.getInternalFrame())
                .getClientProperty(AbstractInternalFrame.ROLL_PROPERTY);
        if (rolledProp != null && rolledProp.booleanValue())
        {
            windowShadeUp();
        }
    }

    /**
     * Tell whether the current state of the frame and the current thread allow
     * for image manipulation.
     *
     * @return true when image manipulation is permitted.
     */
    public synchronized boolean allowImageManipulation()
    {
        // Make sure that the frame has not been popped out and that it is
        // visible.
        if (myFrameClosed || myHUDFrame.getInternalFrame().getParent() == null || !myHUDFrame.getInternalFrame().isVisible()
                || myPoppedOut)
        {
            return false;
        }

        // Do not allow processing off of the EDT.
        if (!SwingUtilities.isEventDispatchThread())
        {
            LOGGER.warn("Attempting image handling off of EDT.");
            return false;
        }

        return true;
    }

    /**
     * Get the internalFrame.
     *
     * @return the internalFrame
     */
    public HUDJInternalFrame getHUDFrame()
    {
        return myHUDFrame;
    }

    /**
     * Perform any necessary actions when the frame is hidden.
     *
     * @param event The event associated with the frame being hidden.
     */
    public void handleComponentHidden(ComponentEvent event)
    {
        myTransformerLock.lock();
        try
        {
            if (myTile != null)
            {
                myTransformer.replaceGeometry(myTile, null);
                myTile = null;
            }
        }
        finally
        {
            myTransformerLock.unlock();
        }
    }

    /**
     * Perform any necessary actions when the frame is shown.
     *
     * @param event The event associated with the frame being shown.
     */
    public void handleComponentShown(ComponentEvent event)
    {
        assert EventQueue.isDispatchThread();

        // Do not check glue here because of a race condition where the internal
        // frame is shown and the main frame is resized, but this method is not
        // called until after the main frame is resized. If the glue is checked,
        // the internal frame may become unglued because the main frame edge
        // moved but the internal frame has not been adjusted yet.
        myDrawingImage = null;
        myImageBuff1 = null;
        myImageGraphicsBuff1 = null;
        myImageBuff2 = null;
        myImageGraphicsBuff2 = null;
        myImageManager = null;
        myImageProvider = new SingletonImageProvider((BufferedImage)null);
        paintImage((Collection<Rectangle>)null);
        publishTile();
    }

    /** When the frame is closed, perform any necessary cleanup. */
    public void handleFrameClosed()
    {
        synchronized (this)
        {
            if (myFrameClosed)
            {
                return;
            }
            myFrameClosed = true;
        }
        EventQueueUtilities.runOnEDTAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                myHUDFrame.getInternalFrame().setFrameBoundsListener(null);
                myHUDFrame.getInternalFrame().setFrameBoundsHelper(null);
            }
        });
        handleDeregistration();
    }

    /**
     * Handle when the image is dirty, but has not been resized.
     *
     * @param dirtyRegions The regions which require repainting.
     */
    public void handleImageDirty(Collection<Rectangle> dirtyRegions)
    {
        assert EventQueue.isDispatchThread();
        if (!allowImageManipulation())
        {
            return;
        }

        if (myDrawingImage == null)
        {
            paintImage(null);
            return;
        }

        if (myHUDFrame.getInternalFrame().getBounds().width != myDrawingImage.getWidth()
                || myHUDFrame.getInternalFrame().getBounds().height != myDrawingImage.getHeight())
        {
            return;
        }

        paintImage(null);
        if (dirtyRegions != null && myTile != null)
        {
            Collection<ImageManager.DirtyRegion> regions = New.collection(dirtyRegions.size());
            for (Rectangle rect : dirtyRegions)
            {
                regions.add(new ImageManager.DirtyRegion(rect.x, rect.x + rect.width, rect.y, rect.y + rect.height));
            }
            myTile.getImageManager().addDirtyRegions(regions);
        }
    }

    /**
     * Handle viewer changes..
     *
     * @param viewer The new viewer.
     * @param type The type of view update.
     */
    public synchronized void handleViewChanged(final Viewer viewer, final ViewChangeType type)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                if (type == ViewChangeSupport.ViewChangeType.WINDOW_RESIZE)
                {
                    myHUDFrame.getInternalFrame().setBoundsForMainFrameChange(myHUDFrame.getInternalFrame().getBounds());
                }
                else if (myHUDFrame.getGeographicAnchor() != null)
                {
                    GeographicBoxAnchor anchor = myHUDFrame.getGeographicAnchor();
                    Vector3d windowOffset = myPositionConverter.convertPositionToWindow(anchor.getGeographicAnchor(), null);

                    final int width = myHUDFrame.getInternalFrame().getWidth();
                    final int height = myHUDFrame.getInternalFrame().getHeight();
                    double offsetX = windowOffset.getX() - width * anchor.getHorizontalAlignment();
                    double offsetY = viewer.getViewportHeight() - height - windowOffset.getY()
                            - height * anchor.getVerticalAlignment();
                    int anchorOffsetX = anchor.getAnchorOffset() == null ? 0 : anchor.getAnchorOffset().getX();
                    int anchorOffsetY = anchor.getAnchorOffset() == null ? 0 : anchor.getAnchorOffset().getY();
                    final int upperLeftX = (int)(Math.round(offsetX) + anchorOffsetX);
                    final int upperLeftY = (int)(Math.round(offsetY) + anchorOffsetY);

                    myHUDFrame.getInternalFrame().setBounds(new Rectangle(upperLeftX, upperLeftY, width, height));
                }
            }
        });
    }

    /**
     * Pop the internal frame out into a {@link DockableFrame}.
     */
    public void popFrame()
    {
        if (myHUDFrame.getInternalFrame().isPopable())
        {
            EventQueueUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    handleDeregistration();
                    sendToDialog();
                }
            });
        }
    }

    /** Update my tile's rendering order if necessary. */
    public void validateRenderOrder()
    {
        if (myTile != null && myTile.getRenderProperties().getRenderingOrder() != getTileRenderOrder())
        {
            myTile.getRenderProperties().setRenderingOrder(getTileRenderOrder());
        }
    }

    /**
     * Roll down the frame if necessary.
     */
    public void windowShadeDown()
    {
        // if the rolled content pane is null, this frame is not rolled up.
        if (myRolledContents != null)
        {
            myHUDFrame.getInternalFrame().setResizable(myRolledContents.getThirdObject().booleanValue());
            myHUDFrame.getInternalFrame().setContentPane(myRolledContents.getFirstObject());
            // Use the current frame bounds for the position and the saved
            // bounds for the size.
            Rectangle currentBounds = myHUDFrame.getInternalFrame().getBounds();
            currentBounds.width = myRolledContents.getSecondObject().width;
            currentBounds.height = myRolledContents.getSecondObject().height;
            myHUDFrame.getInternalFrame().setBounds(currentBounds);
            myRolledContents = null;
        }
    }

    /**
     * Roll up the frame if necessary.
     */
    public final void windowShadeUp()
    {
        // if the rolled content pane is not null, this frame is already rolled
        // up.
        if (myRolledContents == null)
        {
            // @formatter:off
            myRolledContents = new ThreeTuple<>((JComponent)myHUDFrame.getInternalFrame()
                    .getContentPane(), myHUDFrame.getInternalFrame().getBounds(), Boolean.valueOf(myHUDFrame.getInternalFrame()
                        .isResizable()));
            // @formatter:on
            myHUDFrame.getInternalFrame().setContentPane(EMPTY_PANE);

            int width = myHUDFrame.getInternalFrame().getWidth();
            int height = myHUDFrame.getInternalFrame().getHeight() - myRolledContents.getFirstObject().getHeight();
            myHUDFrame.getInternalFrame().setSize(width, height);
            myHUDFrame.getInternalFrame().setResizable(false);
        }
    }

    /**
     * Creates the popped out dialog.
     *
     * @param contentPane The content of the frame.
     * @param frameTitle The frame title.
     * @param bounds The bounds of the dialog.
     * @return The created dialog.
     */
    private JDialog createDialog(JComponent contentPane, String frameTitle, Rectangle bounds)
    {
        JDialog dialog = new JDialog(myTransformer.getToolbox().getUIRegistry().getMainFrameProvider().get());
        dialog.setTitle(frameTitle);
        dialog.setBounds(bounds);
        dialog.setContentPane(contentPane);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        return dialog;
    }

    /**
     * Get the z-order of the tile which is generated based on the relative
     * z-orders of the internal frames.
     *
     * @return z-order The generated z-order.
     */
    private int getTileRenderOrder()
    {
        if (myHUDFrame.getInternalFrame().getParent() != null)
        {
            return 1000 - myHUDFrame.getInternalFrame().getParent().getComponentZOrder(myHUDFrame.getInternalFrame());
        }
        else
        {
            return 1001;
        }
    }

    /**
     * Perform an necessary image painting or tile publishing when the internal
     * frame has been moved or resized.
     *
     * @param bounds The new bounds of the internal frame.
     */
    @SuppressWarnings("unchecked")
    private void handleBoundsSet(Rectangle bounds)
    {
        assert EventQueue.isDispatchThread();
        if (!allowImageManipulation())
        {
            return;
        }

        if (myTile == null || bounds.width == ((BoundingBox<ScreenPosition>)myTile.getBounds()).getWidth()
                && bounds.height == ((BoundingBox<ScreenPosition>)myTile.getBounds()).getHeight())
        {
            // the tile has been moved, but we can use the same image if we have
            // one.
            if (myImageManager == null)
            {
                paintImage((Collection<Rectangle>)null);
            }
            publishTile();
        }
        else
        {
            myFrameResizeExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    EventQueueUtilities.runOnEDT(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            paintImage((Collection<Rectangle>)null);
                            publishTile();
                        }
                    });
                }
            });
        }
    }

    /** Cleanup listeners AWT related event handling. */
    private void handleDeregistration()
    {
        if (myEventHelper != null)
        {
            myEventHelper.close();
            myEventHelper = null;
        }

        GLSwingEventManager.getInstance().deregisterFrame(this);
        ((GLSwingRepaintManager)RepaintManager.currentManager(null)).removeFrame(this);

        myTransformerLock.lock();
        try
        {
            if (myTile != null)
            {
                myTransformer.replaceGeometry(myTile, null);
                myTile = null;
            }
        }
        finally
        {
            myTransformerLock.unlock();
        }
    }

    /** Set up listeners and AWT related event handling. */
    private void handleRegistration()
    {
        RepaintManager man = RepaintManager.currentManager(myHUDFrame.getInternalFrame());
        GLSwingRepaintManager hudMan = null;
        if (man instanceof GLSwingRepaintManager)
        {
            hudMan = (GLSwingRepaintManager)man;
        }
        else
        {
            hudMan = new GLSwingRepaintManager();
            RepaintManager.setCurrentManager(hudMan);
        }
        hudMan.addFrame(this);

        GLSwingEventManager.getInstance().registerFrame(this);

        if (myEventHelper != null)
        {
            myEventHelper.close();
        }
        myEventHelper = new GLSwingEventListenerHelper(this, myTransformer.getToolbox().getMapManager().getViewChangeSupport());
    }

    /**
     * Do extra processing necessary to handle a frame being popped back into
     * the HUD.
     *
     * @param contentPane The content pane being popped back in.
     */
    private synchronized void handleUnpopFrame(final JComponent contentPane)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myPoppedOut = false;
                handleRegistration();

                myHUDFrame.getInternalFrame().setContentPane(contentPane);
                myHUDFrame.setVisible(true);

                // When frames which were rolled up are popped
                // out, then resized, the sometimes appear smeared when
                // un-popped. This causes the tile to be republished to
                // avoid this affect.
                myHUDFrame.getInternalFrame().setBounds(myHUDFrame.getInternalFrame().getBounds());
            }
        });
    }

    /**
     * Paint the Frame image to the buffer and update. This should only happen
     * on the EDT.
     *
     * @param dirtyRegions The regions to be painted, or {@code null} to paint
     *            the whole image.
     */
    private void paintImage(Collection<Rectangle> dirtyRegions)
    {
        assert EventQueue.isDispatchThread();
        // Since we are painting the real frame, get the real bounds.
        Rectangle loc = myHUDFrame.getInternalFrame().getBounds();
        boolean newImage = myDrawingImage == null || loc.width != myDrawingImage.getWidth()
                || loc.height != myDrawingImage.getHeight();
        if (newImage)
        {
            if (loc.width > 0 && loc.height > 0)
            {
                myImageBuff1 = new BufferedImage(loc.width, loc.height, ourImgType);
                myImageBuff2 = new BufferedImage(loc.width, loc.height, ourImgType);
                myImageGraphicsBuff1 = myImageBuff1.createGraphics();
                myImageGraphicsBuff1.setBackground(Colors.TRANSPARENT_BLACK);
                myImageGraphicsBuff2 = myImageBuff2.createGraphics();
                myImageGraphicsBuff2.setBackground(Colors.TRANSPARENT_BLACK);

                myDrawingImage = myImageBuff1;
                myDrawingImageGraphics = myImageGraphicsBuff1;

                myDrawingImageGraphics.clearRect(0, 0, loc.width, loc.height);
                myHUDFrame.getInternalFrame().paint(myDrawingImageGraphics);

                myImageProvider.setImage(myDrawingImage);
                myImageManager = new ImageManager((Void)null, myImageProvider);
            }
            else
            {
                LOGGER.warn(StringUtilities.concat("Frame, ", myHUDFrame.getTitle(), ", has invalid dimensions: ",
                        String.valueOf(loc.width), " x ", String.valueOf(loc.height)));
            }
        }
        else
        {
            if (dirtyRegions == null)
            {
                myDrawingImageGraphics.clearRect(0, 0, loc.width, loc.height);
                myDrawingImageGraphics.setClip(null);
                myHUDFrame.getInternalFrame().paint(myDrawingImageGraphics);
            }
            else
            {
                for (Rectangle dirty : dirtyRegions)
                {
                    myDrawingImageGraphics.clearRect(dirty.x, dirty.y, dirty.width, dirty.height);
                    myDrawingImageGraphics.setClip(dirty);
                    myHUDFrame.getInternalFrame().paint(myDrawingImageGraphics);
                    myDrawingImageGraphics.setColor(Color.YELLOW);
                    myDrawingImageGraphics.drawPolygon(
                            new int[] { dirty.x, dirty.x, dirty.x + dirty.width - 1, dirty.x + dirty.width - 1 },
                            new int[] { dirty.y, dirty.y + dirty.height - 1, dirty.y + dirty.height - 1, dirty.y }, 4);
                    myDrawingImageGraphics.setColor(Color.MAGENTA);
                    myDrawingImageGraphics.drawPolygon(
                            new int[] { dirty.x + 1, dirty.x + 1, dirty.x + dirty.width - 2, dirty.x + dirty.width - 2 },
                            new int[] { dirty.y + 1, dirty.y + dirty.height - 2, dirty.y + dirty.height - 2, dirty.y + 1 }, 4);
                }
            }

            myImageProvider.setImage(myDrawingImage);
        }

        if (myDrawingImage.equals(myImageBuff1))
        {
            myDrawingImage = myImageBuff2;
            myDrawingImageGraphics = myImageGraphicsBuff2;
        }
        else
        {
            myDrawingImage = myImageBuff1;
            myDrawingImageGraphics = myImageGraphicsBuff1;
        }
    }

    /**
     * Generate a new tile representing the underlying internal frame. The tile
     * size may not be the same size as the internal frame during resizing. This
     * should only happen on the EDT to ensure that the image is not changing
     * while the tile is being published.
     */
    private void publishTile()
    {
        assert EventQueue.isDispatchThread();
        if (myImageManager == null)
        {
            return;
        }

        myTileBounds = getHUDFrame().getInternalFrame().getBounds();
        ScreenPosition upperLeft = new ScreenPosition(myTileBounds.x, myTileBounds.y);
        ScreenPosition lowerRight = new ScreenPosition(myTileBounds.x + myTileBounds.width, myTileBounds.y + myTileBounds.height);
        ScreenBoundingBox tileBox = new ScreenBoundingBox(upperLeft, lowerRight);

        TileGeometry.Builder<ScreenPosition> tileBuilder = new TileGeometry.Builder<>();
        tileBuilder.setImageManager(myImageManager);
        tileBuilder.setDivider(null);
        tileBuilder.setParent(null);
        tileBuilder.setRapidUpdate(true);
        tileBuilder.setBounds(tileBox);

        TileRenderProperties props = new DefaultTileRenderProperties(ZOrderRenderProperties.TOP_Z, true, true);
        props.setHighlightColor(Color.BLACK);
        props.setRenderingOrder(getTileRenderOrder());
        props.setOpacity(1.0f);

        TileGeometry tile = new TileGeometry(tileBuilder, props, null);

        myTransformerLock.lock();
        try
        {
            myTransformer.replaceGeometry(myTile, tile);
            myTile = tile;
        }
        finally
        {
            myTransformerLock.unlock();
        }
    }

    /**
     * Creates a Dialog and takes the content pane from my internal frame and
     * put it into a dialog.
     */
    private synchronized void sendToDialog()
    {
        assert EventQueue.isDispatchThread();
        if (myHUDFrame.getGeographicAnchor() != null || !myHUDFrame.isVisible())
        {
            return;
        }

        myPoppedOut = true;

        // Do not allow rolled frames to be popped, un-roll then first.
        windowShadeDown();
        myHUDFrame.getInternalFrame().putClientProperty(AbstractInternalFrame.ROLL_PROPERTY, Boolean.FALSE);

        // Use the bounds of the internal frame for the dockable frame.
        Rectangle bounds = myHUDFrame.getInternalFrame().getBounds();
        Point locationOnScreen = myHUDFrame.getInternalFrame().getLocationOnScreen();
        bounds.x = locationOnScreen.x;
        bounds.y = locationOnScreen.y;
        // Add to the bounds height since a dialogs title bar is a little bit
        // bigger than a huds title bar.
        bounds.height += 9;

        final JComponent contentPane = (JComponent)myHUDFrame.getInternalFrame().getContentPane();
        myHUDFrame.getInternalFrame().setContentPane(new JPanel());

        myHUDFrame.setVisible(false);

        String frameTitle = myHUDFrame.getInternalFrame().getTitle();

        JDialog dialog = createDialog(contentPane, frameTitle, bounds);

        myDockCloseListener = new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                dialog.dispose();
                if (!myHUDFrame.getInternalFrame().isClosed())
                {
                    handleUnpopFrame(contentPane);
                }
            }
        };
        dialog.addWindowListener(myDockCloseListener);

        InternalFrameListener internalListener = new InternalFrameAdapter()
        {
            @Override
            public void internalFrameClosed(InternalFrameEvent e)
            {
                dialog.dispose();
            }
        };
        myHUDFrame.getInternalFrame().addInternalFrameListener(internalListener);
        dialog.setVisible(true);

        myTileBounds = null;
        myDrawingImage = null;
        myImageBuff1 = null;
        myImageGraphicsBuff1 = null;
        myImageBuff2 = null;
        myImageGraphicsBuff2 = null;
        myImageManager = null;
        if (myTile != null)
        {
            myTransformer.replaceGeometry(myTile, null);
            myTile = null;
        }
    }
}
