package io.opensphere.overlay.worldmap;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;

/**
 * A button class displayed atop the world map.
 */
public class WorldMapButton extends AbstractWorldMapRenderable
{
    /** The default color with which to draw the border. */
    private static final Color DEFAULT_BORDER_COLOR = new Color(0xE7E7E7);

    /** The default component size (assuming square components), in pixels. */
    public static final int DEFAULT_COMPONENT_SIZE = 22;

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /** The image to render on the button. */
    private final BufferedImage myImage;

    /** The procedure called when the button is clicked. */
    private final Procedure myListener;

    /** The color of the border drawn around the button. */
    private Color myBorderColor = DEFAULT_BORDER_COLOR;

    /** The size of the button to draw. */
    private final int mySize;

    /** The render properties for the primary image. */
    private TileRenderProperties myPrimaryImageProperties;

    /**
     * Creates a new button.
     * 
     * @param parent the component to which the button is bound.
     * @param listener the listener called when the button is clicked.
     * @param image the image displayed on the button.
     */
    public WorldMapButton(Component parent, Procedure listener, BufferedImage image)
    {
        super(parent);
        myListener = listener;
        myImage = image;
        mySize = DEFAULT_COMPONENT_SIZE;
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.Component#handleCleanupListeners()
     */
    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
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
        PolylineGeometry.Builder<Position> borderBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties borderProperties = new DefaultPolylineRenderProperties(getBaseZOrder() + 5, true, false);
        borderProperties.setWidth(2.0f);
        borderProperties.setColor(myBorderColor);
        borderBuilder.setLineSmoothing(true);
        borderBuilder.setLineType(LineType.STRAIGHT_LINE);
        ScreenPosition topLeft = new ScreenPosition(drawBounds.getWidth() - mySize, 2);
        ScreenPosition topRight = new ScreenPosition(drawBounds.getWidth() + 4, 2);
        ScreenPosition bottomLeft = new ScreenPosition(drawBounds.getWidth() - mySize, mySize + 4);
        ScreenPosition bottomRight = new ScreenPosition(drawBounds.getWidth() + 4, mySize + 4);
        borderBuilder.setVertices(New.list(topLeft, topRight, bottomRight, bottomLeft, topLeft));

        PolylineGeometry border = new PolylineGeometry(borderBuilder, borderProperties, null);
        getGeometries().add(border);

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
    }

    /**
     * Draws a border around the exterior boundary of the button.
     * 
     * @param drawBounds the boundary of the border.
     */
    protected void drawBorder(ScreenBoundingBox drawBounds)
    {
        PolylineGeometry.Builder<Position> borderBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties borderProperties = new DefaultPolylineRenderProperties(getBaseZOrder() + 5, true, false);
        borderProperties.setWidth(2.0f);
        borderProperties.setColor(myBorderColor);
        borderBuilder.setLineSmoothing(true);
        borderBuilder.setLineType(LineType.STRAIGHT_LINE);

        ScreenPosition topLeft = new ScreenPosition(drawBounds.getWidth() - mySize + 4, 2);
        ScreenPosition topRight = new ScreenPosition(drawBounds.getWidth() + 4, 2);
        ScreenPosition bottomLeft = new ScreenPosition(drawBounds.getWidth() - mySize + 4, mySize);
        ScreenPosition bottomRight = new ScreenPosition(drawBounds.getWidth() + 4, mySize);
        borderBuilder.setVertices(New.list(topLeft, topRight, bottomRight, bottomLeft, topLeft));

        PolylineGeometry border = new PolylineGeometry(borderBuilder, borderProperties, null);
        getGeometries().add(border);
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
}
