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
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;

/**
 * A generic image button used in HUD overlays, in which an image is drawn, and
 * the supplied {@link Procedure} is called when the button is clicked.
 */
public class BufferedImageButton extends Renderable implements ControlComponent
{
    private static final Color BORDER_COLOR = new Color(0xE7E7E7);

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /** The procedure called when the button is clicked. */
    private final Consumer<BufferedImageButton> myListener;

    /** The image to render on the button. */
    private final BufferedImage myImage;

    /** The image to render on the button. */
    private BufferedImage myAlternateImage;

    /** The bottom margin used in drawing the image. */
    private int myBottomMargin = -2;

    private final int mySize;

    private TileGeometry myImageGeometry;

    private TileGeometry myAlternateImageGeometry;

    private ScreenBoundingBox myDrawingLocation;

    private TransformerHelper myTransformer;

    private TileRenderProperties myPrimaryImageProperties;

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
        mySize = 22;

        if (parent instanceof Window<?, ?>)
        {
            myTransformer = parent.getTransformer();
        }
    }

    /**
     * Sets the value of the {@link #alternateImage} field.
     *
     * @param alternateImage the value to store in the {@link #alternateImage}
     *            field.
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
        myDrawingLocation = new ScreenBoundingBox(topLeft, bottomRight);

        TileGeometry.Builder<Position> builder = new TileGeometry.Builder<>();
        builder.setBounds(myDrawingLocation);
        builder.setDataModelId(RandomUtils.nextLong());
        builder.setImageManager(new ImageManager((Void)null, new SingletonImageProvider(myImage)));

        myPrimaryImageProperties = new DefaultTileRenderProperties(getBaseZOrder() + 4, true, true);
        myPrimaryImageProperties.setColor(ColorUtilities.opacitizeColor(Color.WHITE, 0.8f));
        myPrimaryImageProperties.setObscurant(false);
        myPrimaryImageProperties.setOpacity(0.8F);
        myPrimaryImageProperties.setHidden(false);
        myPrimaryImageProperties.setHighlightColor(ColorUtilities.opacitizeColor(Color.BLUE, 0.8f));

        myImageGeometry = new TileGeometry(builder, myPrimaryImageProperties, null);
        getGeometries().add(myImageGeometry);

        myMouseSupport.setActionGeometry(myImageGeometry);

        if (myAlternateImage != null)
        {
            TileGeometry.Builder<Position> alternateBuilder = new TileGeometry.Builder<>();
            alternateBuilder.setBounds(myDrawingLocation);
            alternateBuilder.setDataModelId(RandomUtils.nextLong());
            alternateBuilder.setImageManager(new ImageManager((Void)null, new SingletonImageProvider(myAlternateImage)));

            myAlternateImageProperties = new DefaultTileRenderProperties(getBaseZOrder() + 3, true, true);
            myAlternateImageProperties.setColor(ColorUtilities.opacitizeColor(Color.WHITE, 0.8f));
            myAlternateImageProperties.setObscurant(false);
            myAlternateImageProperties.setOpacity(0.8F);
            myAlternateImageProperties.setHidden(false);
            myAlternateImageProperties.setHighlightColor(ColorUtilities.opacitizeColor(Color.BLUE, 0.8f));

            myAlternateImageGeometry = new TileGeometry(alternateBuilder, myAlternateImageProperties, null);
            getGeometries().add(myAlternateImageGeometry);

            myMouseSupport.setActionGeometry(myAlternateImageGeometry);
        }
    }

    /**
     * @return
     */
    protected ScreenBoundingBox drawBorder(ScreenBoundingBox drawBounds)
    {
        PolylineGeometry.Builder<Position> borderBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties borderProperties = new DefaultPolylineRenderProperties(getBaseZOrder(), true, false);
        borderProperties.setWidth(3.0f);
        borderProperties.setColor(BORDER_COLOR);
        borderBuilder.setLineSmoothing(true);
        borderBuilder.setLineType(LineType.STRAIGHT_LINE);

        ScreenPosition upperLeft = drawBounds.getUpperLeft().add(new Vector3d(1, 1, 0));
        ScreenPosition upperRight = drawBounds.getUpperRight().add(new Vector3d(-1, 1, 0));
        ScreenPosition lowerRight = drawBounds.getLowerRight().add(new Vector3d(-1, myBottomMargin + 1, 0));
        ScreenPosition lowerLeft = drawBounds.getLowerLeft().add(new Vector3d(1, myBottomMargin + 1, 0));
        borderBuilder.setVertices(New.list(upperLeft, upperRight, lowerRight, lowerLeft, upperLeft));

        PolylineGeometry border = new PolylineGeometry(borderBuilder, borderProperties, null);
        getGeometries().add(border);
        return drawBounds;
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

    public void reverseImages()
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
