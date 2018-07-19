package io.opensphere.overlay.worldmap;

import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.overlay.util.AbstractOverlayWindow;
import javafx.beans.property.BooleanProperty;

/** Draws the HUD world-map tool. */
public class HUDWorldMap extends AbstractOverlayWindow
{
    /** Executor shared by HUD components. */
    private final ScheduledExecutorService myExecutor;

    /** Responsible for drawing foot print on the map. */
    private WorldMapFootPrint myMapFootPrint;

    private WorldMapButton myMinimizeButton;

    private WorldMapBackground myMapBackground;

    /** A property in which the minimized state is maintained. */
    private BooleanProperty myMinimizedState;

    private WorldMapButton myRestoreButton;

    private boolean myInitialized;

    /**
     * Constructor.
     *
     * @param hudTransformer The transformer.
     * @param executor Executor shared by HUD components.
     * @param size The bounding box we will use for size (but not location on
     *            screen).
     * @param location The predetermined location.
     * @param resize The resize behavior.
     */
    public HUDWorldMap(TransformerHelper hudTransformer, ScheduledExecutorService executor, ScreenBoundingBox size,
            ToolLocation location, ResizeOption resize)
    {
        super(hudTransformer, size, location, resize, ZOrderRenderProperties.TOP_Z - 30);
        myMinimizedState = new ConcurrentBooleanProperty(false);
        myMinimizedState.addListener((obs, ov, nv) -> redisplay(nv));
        myExecutor = executor;
        myInitialized = false;
    }

    @Override
    public void init()
    {
        if (!myInitialized)
        {
            super.init();
            setLayout(new GridLayout(300, 150, this));
            myInitialized = true;
        }

        if (isMinimized())
        {
            remove(myMinimizeButton);
            remove(myMapFootPrint);
            remove(myMapBackground);
            addRestoreButton();
        }
        else
        {
            remove(myRestoreButton);
            addBackground();
            addFootprint();
            addMinimizeButton();
        }

        getLayout().complete();
    }

    @Override
    public void repositionForInsets()
    {
        // intentionally blank
    }

    /**
     * Gets the {@link #myMinimizedState} property.
     *
     * @return the {@link #myMinimizedState} property.
     */
    public BooleanProperty minimizedStateProperty()
    {
        return myMinimizedState;
    }

    /**
     * Tests to determine if the map is minimized.
     * 
     * @return true if the map is minimized, false otherwise.
     */
    public boolean isMinimized()
    {
        return myMinimizedState.get();
    }

    private void redisplay(boolean minimize)
    {
        ScreenPosition delta;
        if (minimize)
        {
            delta = new ScreenPosition(-275, -125);
        }
        else
        {
            delta = new ScreenPosition(275, 125);
        }
        resizeWindow(delta);
    }

    /** Add the world map background. */
    private synchronized void addBackground()
    {
        if (myMapBackground == null)
        {
            myMapBackground = new WorldMapBackground(this);
        }
        GridLayoutConstraints constraints = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(myMapBackground, constraints);
    }

    /** Add the visible footprint around the current position. */
    private synchronized void addFootprint()
    {
        if (myMapFootPrint == null)
        {
            myMapFootPrint = new WorldMapFootPrint(this, myExecutor);
        }
        GridLayoutConstraints constraints = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(myMapFootPrint, constraints);
    }

    private synchronized void addRestoreButton()
    {
        if (myRestoreButton == null)
        {
            myRestoreButton = new WorldMapButton(this, this::changeMinimizationState);
        }

        GridLayoutConstraints constraints = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(25, 25)));

        add(myRestoreButton, constraints);
    }

    private synchronized void addMinimizeButton()
    {
        if (myMinimizeButton == null)
        {
            myMinimizeButton = new WorldMapButton(this, this::changeMinimizationState);
        }

        GridLayoutConstraints constraints = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));

        add(myMinimizeButton, constraints);
    }

    private void changeMinimizationState()
    {
        myMinimizedState.set(!myMinimizedState.get());
    }
}
