package io.opensphere.overlay.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.viewer.impl.AbstractDynamicViewer;
import io.opensphere.core.viewer.impl.ViewControlTranslator;

/**
 * 
 */
public class ZoomControlsPlugin extends AbstractHUDWindowMenuItemPlugin
{
    /** The size of the buttons, in pixels. */
    private static final int BUTTON_SIZE = 22;

    /** The distance from the right of the screen to draw the controls. */
    private static final int DEFAULT_RIGHT_MARGIN = 14;

    /** The distance from the top of the screen to draw the controls. */
    private static final int DEFAULT_TOP_MARGIN = 15;

    /** The width of the container to draw. */
    private static final int WIDTH = 30;

    /** The height of the container to draw. */
    private static final int HEIGHT = 88;

    private ButtonContainer myWindow;

    /** Constructor. */
    public ZoomControlsPlugin()
    {
        super("Overlay Controls", true, true);
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

        // TODO integrate with ControlsLayoutManager to do the offset stuff.

        myWindow = new ButtonContainer(helper, size, ToolLocation.NORTHEAST, ResizeOption.RESIZE_KEEP_FIXED_SIZE,
                this::createZoomInButton, this::createZoomOutButton, this::createSpacer, this::createChangeProjection2DButton);

        return myWindow;
    }

    /**
     * Creates a new zoom-in button, bound to the supplied parent.
     * 
     * @param parent the parent to which the button will be bound.
     * @return a button used to zoom in.
     */
    private BufferedImageButton createZoomInButton(Component parent)
    {
        BufferedImageButton button = new BufferedImageButton(parent, this::zoomIn, drawIcon(AwesomeIconSolid.PLUS));
        button.setFrameLocation(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(22, 22)));
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
        BufferedImageButton button = new BufferedImageButton(parent, this::zoomOut, drawIcon(AwesomeIconSolid.MINUS));
        button.setFrameLocation(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(22, 22)));
        return button;
    }

    private BufferedImageButton createChangeProjection2DButton(Component parent)
    {
        BufferedImageButton button = new BufferedImageButton(parent, this::changeProjection, drawIcon("3D"));
        button.setFrameLocation(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(22, 22)));
        button.setBottomMargin(-3);
        button.setAlternateImage(drawIcon("2D"));

        return button;
    }

    private ControlSpacer createSpacer(Component parent)
    {
        return new ControlSpacer(BUTTON_SIZE, 5);
    }

    private BufferedImage drawIcon(AwesomeIconSolid icon)
    {
        BufferedImage image = new BufferedImage(BUTTON_SIZE, BUTTON_SIZE, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(ColorUtilities.opacitizeColor(ColorUtilities.convertFromHexString("FF333333", 1, 2, 3, 0), 0.445f));
        graphics.fillRect(0, 0, BUTTON_SIZE, BUTTON_SIZE);

        graphics.setFont(icon.getFont().deriveFont(Font.PLAIN, 14));
        graphics.setColor(Color.WHITE);

        graphics.drawString(icon.getFontCode(), 5, 16);
        graphics.dispose();
        return image;
    }

    private BufferedImage drawIcon(String text)
    {
        BufferedImage image = new BufferedImage(BUTTON_SIZE, BUTTON_SIZE, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(ColorUtilities.opacitizeColor(ColorUtilities.convertFromHexString("FF333333", 1, 2, 3, 0), 0.445f));
        graphics.fillRect(0, 0, BUTTON_SIZE, BUTTON_SIZE);

        graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 13));
        graphics.setColor(Color.WHITE);

        graphics.drawString(text, 1, 16);
        graphics.dispose();
        return image;
    }

    private void zoomIn(BufferedImageButton sourceButton)
    {
        ViewControlTranslator translator = getToolbox().getMapManager().getCurrentControlTranslator();
        translator.zoomView(-translator.getZoomRate());
    }

    private void zoomOut(BufferedImageButton sourceButton)
    {
        ViewControlTranslator translator = getToolbox().getMapManager().getCurrentControlTranslator();
        translator.zoomView(translator.getZoomRate());
    }

    private void changeProjection(BufferedImageButton sourceButton)
    {
        sourceButton.reverseImages();

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
