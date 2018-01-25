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

    /** Responsible for drawing cross hairs on the map. */
    private WorldMapCrosshair myMapCrosshair;

    /** Responsible for drawing foot print on the map. */
    private WorldMapFootPrint myMapFootPrint;

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

        // add world map crosshair
        addCrosshair();

        // add footprint
        addFootprint();

        getLayout().complete();
    }

    @Override
    public void repositionForInsets()
    {
    }

    /** Add the world map background. */
    private void addBackground()
    {
        WorldMapBackground mapBackground = new WorldMapBackground(this);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(mapBackground, constr);
    }

    /** Add the cross hair over current position. */
    private void addCrosshair()
    {
        myMapCrosshair = new WorldMapCrosshair(this, myExecutor);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(myMapCrosshair, constr);
    }

    /** Add the visible footprint around the current position. */
    private void addFootprint()
    {
        myMapFootPrint = new WorldMapFootPrint(this, myExecutor);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        add(myMapFootPrint, constr);
    }
}
