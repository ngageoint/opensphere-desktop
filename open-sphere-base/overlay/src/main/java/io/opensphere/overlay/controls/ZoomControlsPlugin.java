package io.opensphere.overlay.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.api.adapter.AbstractHUDWindowMenuItemPlugin;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.Window.ResizeOption;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.viewer.impl.ViewControlTranslator;

/**
 * 
 */
public class ZoomControlsPlugin extends AbstractHUDWindowMenuItemPlugin
{
    /** The size of the buttons, in pixels. */
    private static final int BUTTON_SIZE = 30;

    /** The distance from the right of the screen to draw the controls. */
    private static final int DEFAULT_RIGHT_MARGIN = 25;

    /** The distance from the top of the screen to draw the controls. */
    private static final int DEFAULT_TOP_MARGIN = 25;

    /** The width of the container to draw. */
    private static final int WIDTH = 35;

    /** The height of the container to draw. */
    private static final int HEIGHT = 65;

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

        ButtonContainer window = new ButtonContainer(helper, size, ToolLocation.NORTHEAST, ResizeOption.RESIZE_KEEP_FIXED_SIZE,
                this::createZoomInButton, this::createZoomOutButton, this::createSpacer, this::createChangeProjectionButton);

        return window;
    }

    /**
     * Creates a new zoom-in button, bound to the supplied parent.
     * 
     * @param parent the parent to which the button will be bound.
     * @return a button used to zoom in.
     */
    private BufferedImageButton createZoomInButton(Component parent)
    {
        return new BufferedImageButton(parent, this::zoomIn, drawIcon(AwesomeIconSolid.PLUS));
    }

    /**
     * Creates a new zoom-out button, bound to the supplied parent.
     * 
     * @param parent the parent to which the button will be bound.
     * @return a button used to zoom out.
     */
    private BufferedImageButton createZoomOutButton(Component parent)
    {
        return new BufferedImageButton(parent, this::zoomOut, drawIcon(AwesomeIconSolid.MINUS));
    }

    private BufferedImageButton createChangeProjectionButton(Component parent)
    {
        BufferedImageButton button = new BufferedImageButton(parent, this::changeProjection, drawIcon("2D"));
        button.setBottomMargin(2);
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

        graphics.setColor(ColorUtilities.opacitizeColor(Color.DARK_GRAY, 0.8f));
        graphics.fillRect(0, 0, BUTTON_SIZE, BUTTON_SIZE);

        graphics.setFont(icon.getFont().deriveFont(Font.PLAIN, 18));
        graphics.setColor(Color.WHITE);

        graphics.drawString(icon.getFontCode(), 7, 22);
        graphics.dispose();
        return image;
    }

    private void zoomIn()
    {
        ViewControlTranslator translator = getToolbox().getMapManager().getCurrentControlTranslator();
        translator.zoomView(-translator.getZoomRate());
    }

    private void zoomOut()
    {
        ViewControlTranslator translator = getToolbox().getMapManager().getCurrentControlTranslator();
        translator.zoomView(translator.getZoomRate());
    }

    private void changeProjection()
    {

    }

    private BufferedImage drawIcon(String text)
    {
        BufferedImage image = new BufferedImage(BUTTON_SIZE, BUTTON_SIZE, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(ColorUtilities.opacitizeColor(Color.DARK_GRAY, 0.8f));
        graphics.fillRect(0, 0, BUTTON_SIZE, BUTTON_SIZE);

        graphics.setFont(graphics.getFont().deriveFont(Font.PLAIN, 18));
        graphics.setColor(Color.WHITE);

        graphics.drawString(text, 3, 22);
        graphics.dispose();
        return image;
    }

}
