package io.opensphere.overlay.worldmap;

import java.awt.Color;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.function.Procedure;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.overlay.controls.HUDGraphicUtilities;
import io.opensphere.overlay.util.AbstractOverlayWindow;
import javafx.beans.property.BooleanProperty;

/** Draws the HUD world-map tool. */
public class HUDWorldMap extends AbstractOverlayWindow
{
    /** Executor shared by HUD components. */
    private final ScheduledExecutorService myExecutor;

    /** The background color with which to draw controls. */
    private static final Color DEFAULT_CONTROL_BACKGROUND = ColorUtilities.convertFromHexString("71333333", 1, 2, 3, 0);

    /** Responsible for drawing foot print on the map. */
    private WorldMapFootPrint myMapFootPrint;

    /** The button used to minimize the world map window. */
    private WorldMapButton myMinimizeButton;

    /** A property in which the minimized state is maintained. */
    private BooleanProperty myMinimizedState;

    /**
     * Constructor.
     *
     * @param hudTransformer The transformer.
     * @param executor Executor shared by HUD components.
     * @param size The bounding box we will use for size (but not location on
     *            screen).
     * @param location The predetermined location.
     * @param resize The resize behavior.
     * @param minimizeProcedure the procedure to be called when the minimize
     *            button is clicked.
     */
    public HUDWorldMap(TransformerHelper hudTransformer, ScheduledExecutorService executor, ScreenBoundingBox size,
            ToolLocation location, ResizeOption resize, Procedure minimizeProcedure)
    {
        super(hudTransformer, size, location, resize, ZOrderRenderProperties.TOP_Z - 30);
        myMinimizedState = new ConcurrentBooleanProperty(false);
        myMinimizedState.addListener((obs, ov, nv) -> minimizeProcedure.invoke());
        myExecutor = executor;
    }

    @Override
    public void init()
    {
        super.init();
        setLayout(new GridLayout(300, 150, this));

        addBackground();
        addFootprint();
        addMinimizeButton();

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

    /** Add the world map background. */
    private synchronized void addBackground()
    {
        WorldMapBackground mapBackground = new WorldMapBackground(this);
        GridLayoutConstraints constraints = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(mapBackground, constraints);
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

    /** Adds the minimize button to the map display. */
    private synchronized void addMinimizeButton()
    {
        if (myMinimizeButton == null)
        {
            myMinimizeButton = new WorldMapButton(this, this::changeMinimizationState, HUDGraphicUtilities
                    .drawIcon(AwesomeIconSolid.ANGLE_DOUBLE_RIGHT, 22, 14, DEFAULT_CONTROL_BACKGROUND, Color.WHITE));
        }

        GridLayoutConstraints constraints = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));

        add(myMinimizeButton, constraints);
    }

    /** Changes the state of the minimized property. */
    private void changeMinimizationState()
    {
        myMinimizedState.set(!myMinimizedState.get());
    }
}
