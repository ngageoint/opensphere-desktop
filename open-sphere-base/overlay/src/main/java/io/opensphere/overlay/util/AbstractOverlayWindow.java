package io.opensphere.overlay.util;

import java.awt.Rectangle;

import io.opensphere.core.hud.border.SimpleLineBorder;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;

/**
 * An abstract class for common functionality in overlay windows.
 */
public abstract class AbstractOverlayWindow extends Window<GridLayoutConstraints, GridLayout>
{
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
    public AbstractOverlayWindow(TransformerHelper hudTransformer, ScreenBoundingBox size, ToolLocation locationHint,
            ResizeOption resize, int zOrder)
    {
        super(hudTransformer, size, locationHint, resize, zOrder);
    }

    @Override
    public Rectangle getBounds()
    {
        return getFrameLocation().asRectangle();
    }

    @Override
    public void init()
    {
        clearGeometries();

        // set the border
        SimpleLineBorder.Builder borderBuilder = new SimpleLineBorder.Builder();
        borderBuilder.setHeight(3);
        borderBuilder.setWidth(3);
        borderBuilder.setLineWidth(1);
        setBorder(new SimpleLineBorder(this, borderBuilder));
        initBorder();
    }
}
