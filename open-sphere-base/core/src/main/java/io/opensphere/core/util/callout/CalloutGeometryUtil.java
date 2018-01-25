package io.opensphere.core.util.callout;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.border.EmptyBorder;

import io.opensphere.core.callout.Callout;
import io.opensphere.core.callout.CalloutImpl;
import io.opensphere.core.geometry.GeoScreenBubbleGeometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.TileGeometry.Builder;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.Image;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.swing.SwingImageHelper;

/** Facility for generating callout geometries. */
public final class CalloutGeometryUtil
{
    /**
     * Create The tile for the callout which contains the text.
     *
     * @param callout The callout.
     * @return The newly created tile.
     */
    public static TileGeometry createCalloutTile(Callout callout)
    {
        int borderInsets = 3;
        EmptyBorder border = new EmptyBorder(borderInsets, borderInsets, borderInsets, borderInsets);

        Color backgroundColor = callout.getBackgroundColor();

        boolean isFilled = backgroundColor.getAlpha() > 0;

        Color textColor;
        if (isFilled)
        {
            textColor = callout.getTextColor();
        }
        else
        {
            // Use black for the text color in the image since the geometry will
            // get the actual text color.
            textColor = Color.BLACK;
        }

        BufferedImage image = SwingImageHelper.textToImage(true, callout.getTextLines(), null, textColor, border,
                callout.getFont(), 0);

        ScreenPosition upperLeft = ScreenPosition.ZERO;
        ScreenPosition lowerRight = new ScreenPosition(image.getWidth(), image.getHeight());
        GeographicPosition attachment = new GeographicPosition(callout.getLocation());
        GeoScreenBoundingBox gsbb = new GeoScreenBoundingBox(upperLeft, lowerRight,
                new GeographicBoxAnchor(attachment, callout.getAnchorOffset(), 0f, 0f));

        TileGeometry.Builder<ScreenPosition> tileBuilder = new TileGeometry.Builder<ScreenPosition>();
        tileBuilder.setBounds(gsbb);
        SingletonImageProvider imageProvider = new SingletonImageProvider(image, Image.CompressionType.D3DFMT_A8R8G8B8);
        tileBuilder.setImageManager(new ImageManager(null, imageProvider));
        tileBuilder.setDataModelId(callout.getId());
        TileRenderProperties props = new DefaultTileRenderProperties(10, true, callout.isPickable());
        if (!isFilled)
        {
            props.setHighlightColorARGB(callout.getTextColor().getRGB());
            props.setColorARGB(callout.getTextColor().getRGB());
        }

        Constraints constraints = null;
        if (callout.getTime() != null)
        {
            TimeConstraint contraint = TimeConstraint.getTimeConstraint(callout.getTime());
            constraints = new Constraints(contraint);
        }

        return new TileGeometry(tileBuilder, props, constraints);
    }

    /**
     * Create The tile for the callout which contains the text.
     *
     * @param calloutText The text for the callout; each item will occupy one
     *            line.
     * @param attachment The geographic attachment for the tile position.
     * @param offset The offset from the attachment for the tile position.
     * @param labelColor The color of the text.
     * @param labelFont The font of the text.
     * @param pickable True when the tile should be pickable.
     * @return The newly created tile.
     */
    public static TileGeometry createCalloutTile(List<? extends String> calloutText, GeographicPosition attachment,
            Vector2i offset, Color labelColor, Font labelFont, boolean pickable)
    {
        Callout callout = new CalloutImpl(0, calloutText, attachment.getLatLonAlt(), labelFont);
        callout.setAnchorOffset(offset);
        callout.setTextColor(labelColor);
        callout.setCornerRadius(10);
        callout.setBackgroundColor(Color.GRAY);
        callout.setPickable(pickable);

        return createCalloutTile(callout);
    }

    /**
     * Create a tile which is a copy of the original, but repositioned to the
     * new bounding box.
     *
     * @param origTile The original tile.
     * @param gsbb The new bounding box.
     * @return The newly created tile.
     */
    public static TileGeometry createRepositionedTile(TileGeometry origTile, GeoScreenBoundingBox gsbb)
    {
        Builder<Position> tileBuilder = origTile.createBuilder();
        tileBuilder.setBounds(gsbb);
        return new TileGeometry(tileBuilder, origTile.getRenderProperties(), origTile.getConstraints());
    }

    /**
     * Create a polyline which forms a bubble around the box.
     *
     * @param gsbb The box which will be surrounded by the bubble.
     * @param callout The callout.
     * @return The newly created polyline.
     */
    public static GeoScreenBubbleGeometry createTextBubble(GeoScreenBoundingBox gsbb, Callout callout)
    {
        GeoScreenBubbleGeometry.Builder builder = new GeoScreenBubbleGeometry.Builder();
        builder.setBoundingBox(gsbb);
        builder.setCornerRadius(callout.getCornerRadius());

        ColorRenderProperties fillColor = new DefaultColorRenderProperties(9, true, false, false);
        fillColor.setColor(callout.getBackgroundColor());

        PolygonRenderProperties props = new DefaultPolygonRenderProperties(9, true, false, fillColor);
        props.setColor(callout.getBorderColor());
        props.setWidth(callout.getBorderWidth());

        Constraints constraints = null;
        if (callout.getTime() != null)
        {
            TimeConstraint contraint = TimeConstraint.getTimeConstraint(callout.getTime());
            constraints = new Constraints(contraint);
        }

        return new GeoScreenBubbleGeometry(builder, props, constraints);
    }

    /** Disallow instantiation. */
    private CalloutGeometryUtil()
    {
    }
}
