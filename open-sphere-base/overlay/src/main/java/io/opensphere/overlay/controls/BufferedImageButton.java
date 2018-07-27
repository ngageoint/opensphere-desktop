package io.opensphere.overlay.controls;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import org.apache.commons.lang.math.RandomUtils;

import io.opensphere.core.function.Procedure;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import javafx.beans.property.BooleanProperty;

/**
 * A generic image button used in HUD overlays, in which an image is drawn, and
 * the supplied {@link Procedure} is called when the button is clicked.
 */
public class BufferedImageButton extends Renderable implements ControlComponent
{
    /** The default color with which to draw the border. */
    private static final Color DEFAULT_BORDER_COLOR = new Color(0xE7E7E7);

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /** The procedure called when the button is clicked. */
    private final Consumer<BufferedImageButton> myListener;

    /** The property used to indicate which image should be used. */
    private final BooleanProperty myUsePrimaryImageProperty = new ConcurrentBooleanProperty(true);

    /** The image to render on the button. */
    private final BufferedImage myImage;

    /** The image to render on the button. */
    private BufferedImage myAlternateImage;

    /** The color of the border drawn around the button. */
    private Color myBorderColor = DEFAULT_BORDER_COLOR;

    /** The bottom margin used in drawing the image. */
    private int myBottomMargin = -2;

    /** The size of the button to draw. */
    private final int mySize;

    /** The render properties for the primary image. */
    private TileRenderProperties myPrimaryImageProperties;

    /** The render properties for the alternate image (if present). */
    private TileRenderProperties myAlternateImageProperties;

    /**
     * Creates a new button using the supplied image.
     * 
     * @param parent the parent in which the button is drawn.
     * @param listener The listener called when the button is clicked.
     * @param image the graphic drawn on the button.
     */
    public BufferedImageButton(Component parent, Consumer<BufferedImageButton> listener, BufferedImage image)
    {
        super(parent);
        myListener = listener;
        myImage = image;
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
        mySize = ControlComponentContainer.DEFAULT_COMPONENT_SIZE;
        myUsePrimaryImageProperty.addListener((obs, ov, usePrimary) ->
        {
            if (usePrimary)
            {
                usePrimaryImage();
            }
            else
            {
                useAlternateImage();
            }
        });
    }

    /**
     * Sets the value of the borderColor field.
     *
     * @param borderColor the value to store in the borderColor field.
     */
    public void setBorderColor(Color borderColor)
    {
        myBorderColor = borderColor;
    }

    /**
     * Sets the value of the alternateImage field.
     *
     * @param alternateImage the value to store in the alternateImage field.
     */
    public void setAlternateImage(BufferedImage alternateImage)
    {
        myAlternateImage = alternateImage;
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
        myListener.accept(this);
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
        drawBorder(drawBounds);

        ScreenPosition topLeft = drawBounds.getUpperLeft().add(new Vector3d(2, 2, 0));
        ScreenPosition bottomRight = drawBounds.getLowerRight().add(new Vector3d(-2, myBottomMargin, 0));
        ScreenBoundingBox drawingLocation = new ScreenBoundingBox(topLeft, bottomRight);

        TileGeometry.Builder<Position> builder = new TileGeometry.Builder<>();
        builder.setBounds(drawingLocation);
        builder.setDataModelId(RandomUtils.nextLong());
        builder.setImageManager(new ImageManager((Void)null, new SingletonImageProvider(myImage)));

        myPrimaryImageProperties = new DefaultTileRenderProperties(getBaseZOrder() + 4, true, true);
        myPrimaryImageProperties.setColor(ColorUtilities.opacitizeColor(Color.WHITE, 0.8f));
        myPrimaryImageProperties.setObscurant(false);
        myPrimaryImageProperties.setOpacity(0.8F);
        myPrimaryImageProperties.setHidden(false);
        myPrimaryImageProperties.setHighlightColor(ColorUtilities.opacitizeColor(Color.BLUE, 0.8f));

        TileGeometry imageGeometry = new TileGeometry(builder, myPrimaryImageProperties, null);
        getGeometries().add(imageGeometry);

        myMouseSupport.setActionGeometry(imageGeometry);

        if (myAlternateImage != null)
        {
            TileGeometry.Builder<Position> alternateBuilder = new TileGeometry.Builder<>();
            alternateBuilder.setBounds(drawingLocation);
            alternateBuilder.setDataModelId(RandomUtils.nextLong());
            alternateBuilder.setImageManager(new ImageManager((Void)null, new SingletonImageProvider(myAlternateImage)));

            myAlternateImageProperties = new DefaultTileRenderProperties(getBaseZOrder() + 3, true, true);
            myAlternateImageProperties.setColor(ColorUtilities.opacitizeColor(Color.WHITE, 0.8f));
            myAlternateImageProperties.setObscurant(false);
            myAlternateImageProperties.setOpacity(0.8F);
            myAlternateImageProperties.setHidden(false);
            myAlternateImageProperties.setHighlightColor(ColorUtilities.opacitizeColor(Color.BLUE, 0.8f));

            TileGeometry alternateImageGeometry = new TileGeometry(alternateBuilder, myAlternateImageProperties, null);
            getGeometries().add(alternateImageGeometry);

            myMouseSupport.setActionGeometry(alternateImageGeometry);

            if (myUsePrimaryImageProperty.get())
            {
                myPrimaryImageProperties.setHidden(false);
                myAlternateImageProperties.setHidden(true);
            }
            else
            {
                myPrimaryImageProperties.setHidden(true);
                myAlternateImageProperties.setHidden(false);
            }
        }
    }

    /**
     * Draws a border around the exterior boundary of the button.
     * 
     * @param drawBounds the boundary of the border.
     */
    protected void drawBorder(ScreenBoundingBox drawBounds)
    {
        PolylineGeometry.Builder<Position> borderBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties borderProperties = new DefaultPolylineRenderProperties(getBaseZOrder(), true, false);
        borderProperties.setWidth(3.0f);
        borderProperties.setColor(myBorderColor);
        borderBuilder.setLineSmoothing(true);
        borderBuilder.setLineType(LineType.STRAIGHT_LINE);

        ScreenPosition upperLeft = drawBounds.getUpperLeft().add(new Vector3d(1, 1, 0));
        ScreenPosition upperRight = drawBounds.getUpperRight().add(new Vector3d(-1, 1, 0));
        ScreenPosition lowerRight = drawBounds.getLowerRight().add(new Vector3d(-1, myBottomMargin + 1, 0));
        ScreenPosition lowerLeft = drawBounds.getLowerLeft().add(new Vector3d(1, myBottomMargin + 1, 0));
        borderBuilder.setVertices(New.list(upperLeft, upperRight, lowerRight, lowerLeft, upperLeft));

        PolylineGeometry border = new PolylineGeometry(borderBuilder, borderProperties, null);
        getGeometries().add(border);
    }

    /**
     * Gets the value of the {@link #myMouseSupport} field.
     *
     * @return the value stored in the {@link #myMouseSupport} field.
     */
    public ControlEventSupport getMouseSupport()
    {
        return myMouseSupport;
    }

    /**
     * Gets the value of the {@link #myBottomMargin} field.
     *
     * @return the value stored in the {@link #myBottomMargin} field.
     */
    public int getBottomMargin()
    {
        return myBottomMargin;
    }

    /**
     * If an alternate image is provided, reverses the primary and alternate
     * images. If no alternate image is present, then no reversal will occur.
     */
    public void reverseImages()
    {
        if (myAlternateImage != null)
        {
            if (myPrimaryImageProperties.isHidden())
            {
                myPrimaryImageProperties.setHidden(false);
                myAlternateImageProperties.setHidden(true);
            }
            else
            {
                myAlternateImageProperties.setHidden(false);
                myPrimaryImageProperties.setHidden(true);
            }
        }
    }

    /**
     * If an alternate image is provided, reverses the primary and alternate
     * images. If no alternate image is present, then no reversal will occur.
     */
    public void usePrimaryImage()
    {
        myPrimaryImageProperties.setHidden(false);
        myAlternateImageProperties.setHidden(true);
    }

    /**
     * If an alternate image is provided, reverses the primary and alternate
     * images. If no alternate image is present, then no reversal will occur.
     */
    public void useAlternateImage()
    {
        if (myAlternateImage != null && myAlternateImageProperties != null)
        {
            myAlternateImageProperties.setHidden(false);
            myPrimaryImageProperties.setHidden(true);
        }
    }

    /**
     * Gets the property for tracking the UsePrimaryImage functionality.
     *
     * @return the property for tracking the UsePrimaryImage functionality.
     */
    public BooleanProperty usePrimaryImageProperty()
    {
        return myUsePrimaryImageProperty;
    }
}
