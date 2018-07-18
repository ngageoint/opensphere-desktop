package io.opensphere.overlay.worldmap;

import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.overlay.util.AbstractOverlayWindow;

/** Draws the HUD world-map tool. */
public class HUDWorldMap extends AbstractOverlayWindow
{
    /** Executor shared by HUD components. */
    private final ScheduledExecutorService myExecutor;

    /** Responsible for drawing foot print on the map. */
    private WorldMapFootPrint myMapFootPrint;

    private WorldMapMinimizeButton myMinimizeButton;

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
        myExecutor = executor;
    }

    @Override
    public void init()
    {
        super.init();

        // set the layout
        setLayout(new GridLayout(300, 150, this));

        // add world map background
        addBackground();

        // add footprint
        addFootprint();

        addMinimizeButton();

        getLayout().complete();
    }

    @Override
    public void repositionForInsets()
    {
        // intentionally blank
    }

    /** Add the world map background. */
    private void addBackground()
    {
        WorldMapBackground mapBackground = new WorldMapBackground(this);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(mapBackground, constr);
    }

    /** Add the visible footprint around the current position. */
    private void addFootprint()
    {
        myMapFootPrint = new WorldMapFootPrint(this, myExecutor);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(myMapFootPrint, constr);
    }

    private void addMinimizeButton()
    {
        myMinimizeButton = new WorldMapMinimizeButton(this);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(myMinimizeButton, constr);
    }
}
