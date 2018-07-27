package io.opensphere.overlay.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.MapManager;
import io.opensphere.core.api.adapter.AbstractHUDWindowMenuItemPlugin;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.Window.ResizeOption;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangeSupport.ProjectionChangeListener;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.viewer.impl.AbstractDynamicViewer;
import io.opensphere.core.viewer.impl.ViewControlTranslator;

/**
 * The plugin in which HUD Overlay controls are added to the application. These
 * controls include perspective buttons, and zoom in / zoom out buttons.
 */
public class HUDOverlayControlsPlugin extends AbstractHUDWindowMenuItemPlugin
{
    /** The foreground color with which to draw controls. */
    private static final Color DEFAULT_CONTROL_FOREGROUND = Color.WHITE;

    /** The background color with which to draw controls. */
    private static final Color DEFAULT_CONTROL_BACKGROUND = ColorUtilities.convertFromHexString("71333333", 1, 2, 3, 0);

    /** The distance from the right of the screen to draw the controls. */
    private static final int DEFAULT_RIGHT_MARGIN = 14;

    /** The distance from the top of the screen to draw the controls. */
    private static final int DEFAULT_TOP_MARGIN = 15;

    /** The width of the container to draw. */
    private static final int WIDTH = 30;

    /** The height of the container to draw. */
    private static final int HEIGHT = 88;

    /** The listener used to react to projection changes. */
    private ProjectionChangeListener myProjectionChangeListener;

    /** Constructor. */
    public HUDOverlayControlsPlugin()
    {
        super("HUD Overlay Controls", true, true);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.AbstractHUDWindowMenuItemPlugin#createWindow(io.opensphere.core.hud.framework.TransformerHelper,
     *      java.util.concurrent.ScheduledExecutorService)
     */
    @Override
    protected Window<?, ?> createWindow(TransformerHelper helper, ScheduledExecutorService executor)
    {
        Dimension screenSize = getToolbox().getUIRegistry().getMainFrameProvider().get().getSize();

        int topLeftX = (int)screenSize.getWidth() - DEFAULT_RIGHT_MARGIN - WIDTH;
        int topLeftY = DEFAULT_TOP_MARGIN;

        int bottomRightX = topLeftX + WIDTH;
        int bottomRightY = topLeftY + HEIGHT;

        ScreenPosition topLeft = new ScreenPosition(topLeftX, topLeftY);
        ScreenPosition bottomRight = new ScreenPosition(bottomRightX, bottomRightY);
        ScreenBoundingBox size = new ScreenBoundingBox(topLeft, bottomRight);

        return new ControlComponentContainer(helper, size, ToolLocation.NORTHEAST, ResizeOption.RESIZE_KEEP_FIXED_SIZE,
                this::createZoomInButton, this::createZoomOutButton, this::createSpacer, this::createChangeProjection2DButton);
    }

    /**
     * Creates a new zoom-in button, bound to the supplied parent.
     * 
     * @param parent the parent to which the button will be bound.
     * @return a button used to zoom in.
     */
    private BufferedImageButton createZoomInButton(Component parent)
    {
        BufferedImageButton button = new BufferedImageButton(parent, this::zoomIn,
                HUDGraphicUtilities.drawIcon(AwesomeIconSolid.PLUS, ControlComponentContainer.DEFAULT_COMPONENT_SIZE, 14,
                        DEFAULT_CONTROL_BACKGROUND, DEFAULT_CONTROL_FOREGROUND));
        button.setFrameLocation(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(
                ControlComponentContainer.DEFAULT_COMPONENT_SIZE, ControlComponentContainer.DEFAULT_COMPONENT_SIZE)));
        return button;
    }

    /**
     * Creates a new zoom-out button, bound to the supplied parent.
     * 
     * @param parent the parent to which the button will be bound.
     * @return a button used to zoom out.
     */
    private BufferedImageButton createZoomOutButton(Component parent)
    {
        BufferedImageButton button = new BufferedImageButton(parent, this::zoomOut,
                HUDGraphicUtilities.drawIcon(AwesomeIconSolid.MINUS, ControlComponentContainer.DEFAULT_COMPONENT_SIZE, 14,
                        DEFAULT_CONTROL_BACKGROUND, DEFAULT_CONTROL_FOREGROUND));
        button.setFrameLocation(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(
                ControlComponentContainer.DEFAULT_COMPONENT_SIZE, ControlComponentContainer.DEFAULT_COMPONENT_SIZE)));
        return button;
    }

    /**
     * Creates a new projection-change button, bound to the supplied parent.
     * 
     * @param parent the parent to which the button will be bound.
     * @return a button used to change between 2D and 3D projections.
     */
    private BufferedImageButton createChangeProjection2DButton(Component parent)
    {
        BufferedImageButton button = new BufferedImageButton(parent, this::changeProjection, HUDGraphicUtilities.drawIcon("3D",
                ControlComponentContainer.DEFAULT_COMPONENT_SIZE, 13, DEFAULT_CONTROL_BACKGROUND, DEFAULT_CONTROL_FOREGROUND));
        button.setFrameLocation(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(
                ControlComponentContainer.DEFAULT_COMPONENT_SIZE, ControlComponentContainer.DEFAULT_COMPONENT_SIZE)));
        button.setBottomMargin(-3);
        button.setAlternateImage(HUDGraphicUtilities.drawIcon("2D", ControlComponentContainer.DEFAULT_COMPONENT_SIZE, 13,
                DEFAULT_CONTROL_BACKGROUND, DEFAULT_CONTROL_FOREGROUND));

        myProjectionChangeListener = (e) -> button.usePrimaryImageProperty()
                .set(getToolbox().getMapManager().getProjection().getName().equals("3-D"));

        getToolbox().getMapManager().getProjectionChangeSupport().addProjectionChangeListener(myProjectionChangeListener);
        button.usePrimaryImageProperty().set(getToolbox().getMapManager().getProjection().getName().equals("3-D"));

        return button;
    }

    /**
     * Creates a spacer to place between component groups.
     * 
     * @param parent the parent to which the spacer will be bound.
     * @return a spacer used to layout controls.
     */
    private ControlSpacer createSpacer(Component parent)
    {
        return new ControlSpacer(ControlComponentContainer.DEFAULT_COMPONENT_SIZE, 5);
    }

    /**
     * Action method to zoom the map in.
     * 
     * @param sourceButton the button that fired the action.
     */
    private void zoomIn(BufferedImageButton sourceButton)
    {
        ViewControlTranslator translator = getToolbox().getMapManager().getCurrentControlTranslator();
        translator.zoomView(-translator.getZoomRate());
    }

    /**
     * Action method to zoom the map out.
     * 
     * @param sourceButton the button that fired the action.
     */
    private void zoomOut(BufferedImageButton sourceButton)
    {
        ViewControlTranslator translator = getToolbox().getMapManager().getCurrentControlTranslator();
        translator.zoomView(translator.getZoomRate());
    }

    /**
     * Action method to switch between 2D and 3D projections. Also reverses the
     * image shown on the button.
     * 
     * @param sourceButton the button that fired the action.
     */
    private void changeProjection(BufferedImageButton sourceButton)
    {
        MapManager mapManager = getToolbox().getMapManager();
        Projection currentProjection = mapManager.getProjection();

        Map<Projection, Class<? extends AbstractDynamicViewer>> projections = mapManager.getProjections();

        Projection newProjection;
        if (currentProjection.getName().equals("3-D"))
        {
            newProjection = projections.keySet().stream().filter(p -> p.getName().equals("Equirectangular")).findFirst()
                    .orElse(null);
        }
        else
        {
            newProjection = projections.keySet().stream().filter(p -> p.getName().equals("3-D")).findFirst().orElse(null);
        }
        mapManager.setProjection(projections.get(newProjection));
    }

}
