package io.opensphere.overlay.controls;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import io.opensphere.core.function.Procedure;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.ColorUtilities;

/**
 * A generic image button used in HUD overlays, in which an image is drawn, and
 * the supplied {@link Procedure} is called when the button is clicked.
 */
public class BufferedImageButton extends Renderable implements ControlComponent
{
    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /** The procedure called when the button is clicked. */
    private final Procedure myListener;

    /** The image to render on the button. */
    private final BufferedImage myImage;

    /** The bottom margin used in drawing the image. */
    private int myBottomMargin = 0;

    private final int mySize;

    /**
     * Creates a new button using the supplied image.
     * 
     * @param parent the parent in which the button is drawn.
     * @param listener The listener called when the button is clicked.
     * @param image the graphic drawn on the button.
     */
    public BufferedImageButton(Component parent, Procedure listener, BufferedImage image)
    {
        super(parent);
        myListener = listener;
        myImage = image;
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
        mySize = 30;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.controls.ControlComponent#getWidth()
     */
    @Override
    public int getWidth()
    {
        return mySize;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.controls.ControlComponent#getHeight()
     */
    @Override
    public int getHeight()
    {
        return mySize;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.Component#mouseClicked(io.opensphere.core.geometry.Geometry,
     *      java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(Geometry geom, MouseEvent event)
    {
        myListener.invoke();
    }

    /**
     * Sets the value of the {@link #myBottomMargin} field.
     *
     * @param bottomMargin the value to store in the {@link #myBottomMargin}
     *            field.
     */
    public void setBottomMargin(int bottomMargin)
    {
        myBottomMargin = bottomMargin;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.Renderable#init()
     */
    @Override
    public void init()
    {
        super.init();

        ScreenBoundingBox drawBounds = getDrawBounds();
        ScreenPosition topLeft = drawBounds.getUpperLeft().add(new Vector3d(1, 1, 0));
        ScreenPosition bottomRight = drawBounds.getLowerRight().add(new Vector3d(2, myBottomMargin, 0));

        TileGeometry.Builder<Position> builder = new TileGeometry.Builder<>();
        builder.setBounds(new ScreenBoundingBox(topLeft, bottomRight));
        builder.setImageManager(new ImageManager((Void)null, new SingletonImageProvider(myImage)));

        TileRenderProperties properties = new DefaultTileRenderProperties(getBaseZOrder() + 4, true, true);
        properties.setColor(ColorUtilities.opacitizeColor(Color.WHITE, 0.8f));
        properties.setObscurant(false);
        properties.setOpacity(0.8F);
        properties.setHighlightColor(ColorUtilities.opacitizeColor(Color.BLUE, 0.8f));

        TileGeometry geometry = new TileGeometry(builder, properties, null);
        getGeometries().add(geometry);

        myMouseSupport.setActionGeometry(geometry);
    }
}
