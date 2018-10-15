package io.opensphere.mantle.mappoint.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import io.opensphere.core.callout.Callout;
import io.opensphere.core.geometry.GeoScreenBubbleGeometry;
import io.opensphere.core.geometry.GeoScreenPolygonGeometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.PolylineGeometry;
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
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.swing.SwingImageHelper;

/** Helper class to reduce the complexity of ArcLengthTransformer. */
public final class MapPointTransformerHelper
{
    /** Background text of the label. */
    static final Color ourBackgroundColor = new Color(84, 84, 107, 190);

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(MapPointTransformerHelper.class);

    /**
     * This value is an offset from the highest Z value used when creating
     * geometries.
     */
    private static final int ourZDelta = 900;

    /**
     * Create the tile and line which represent the given call out.
     *
     * @param callOut The call out to use.
     * @param time The time of the geometry or null if no time.
     * @return The pair of tile and group geometry.
     */
    public static Pair<TileGeometry, PolylineGeometry> createGeometryPair(Callout callOut, TimeSpan time)
    {
        TileGeometry tile = createTile(callOut, time);

        GeoScreenBoundingBox gsbb = (GeoScreenBoundingBox)tile.getBounds();
        PolylineGeometry anchoredBorder = createScreenBubble(gsbb, callOut, time);

        return Pair.create(tile, anchoredBorder);
    }

    /**
     * Create a tile which is a copy of the original, but repositioned to the
     * new bounding box.
     *
     * @param origTile The original tile.
     * @param gsbb The new bounding box.
     * @return The newly created tile.
     */
    static TileGeometry createRepositionedTile(TileGeometry origTile, GeoScreenBoundingBox gsbb)
    {
        Builder<Position> tileBuilder = origTile.createBuilder();
        tileBuilder.setBounds(gsbb);
        return new AnnotationPointTileGeometry(tileBuilder, origTile.getRenderProperties(), origTile.getConstraints());
    }

    /**
     * Create a polyline which from the attachment point to the offset.
     *
     * @param gsbb The bounding box.
     * @param callOut The call out to use.
     * @param date The time of the geometry or null if no time.
     * @return The newly created polyline.
     */
    static GeoScreenPolygonGeometry createScreenBubble(GeoScreenBoundingBox gsbb, Callout callOut, TimeSpan date)
    {
        if (gsbb != null)
        {
            GeoScreenBubbleGeometry.Builder builder = new GeoScreenBubbleGeometry.Builder();
            builder.setBoundingBox(gsbb);
            builder.setCornerRadius(callOut.getCornerRadius());

            ColorRenderProperties fillColor = new DefaultColorRenderProperties(ZOrderRenderProperties.TOP_Z - ourZDelta - 1, true,
                    false, false);
            PolygonRenderProperties props = new DefaultPolygonRenderProperties(ZOrderRenderProperties.TOP_Z - ourZDelta - 1, true,
                    false, fillColor);
            Color color = callOut.isBorderHighlighted() ? Color.WHITE
                    : callOut.getBorderColor() == null ? ourBackgroundColor : callOut.getBorderColor();
            props.setColor(color);

            props.getFillColorRenderProperties().setColor(callOut.getBackgroundColor());
            props.setWidth(1);

            Constraints constraints = null;
            if (date != null)
            {
                TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(date);
                constraints = new Constraints(timeConstraint);
            }

            return new GeoScreenBubbleGeometry(builder, props, constraints);
        }
        return null;
    }

    /**
     * Updates the TileGeometry in the group geometry with values from the call
     * out and previous tile.
     *
     * @param callOut The call out
     * @param existingTile The previous tile geometry.
     * @return A newly updated pair of tile geometry and group geometry.
     */
    static Pair<TileGeometry, PolylineGeometry> updateGroupGeometry(Callout callOut, TileGeometry existingTile)
    {
        if (existingTile == null)
        {
            LOGGER.warn("Unable to update geometries for call out.");
            return null;
        }

        TileGeometry tile = updateTile(callOut, existingTile);

        GeoScreenBoundingBox gsbb = (GeoScreenBoundingBox)tile.getBounds();
        TimeSpan date = null;
        if (existingTile.getConstraints() != null)
        {
            date = existingTile.getConstraints().getTimeConstraint().getTimeSpan();
        }
        PolylineGeometry anchoredBorder = createScreenBubble(gsbb, callOut, date);

        return Pair.create(tile, anchoredBorder);
    }

    /**
     * Creates a new tile geometry from the given call out values.
     *
     * @param callOut The call out to use.
     * @param date The time of the geometry or null if no time.
     * @return A tile geometry.
     */
    private static TileGeometry createTile(Callout callOut, TimeSpan date)
    {
        int borderInsets = 3;
        EmptyBorder border = new EmptyBorder(borderInsets, borderInsets, borderInsets, borderInsets);

        boolean isFilled = callOut.getBackgroundColor().getAlpha() > 0;
        Color textColor = isFilled ? callOut.getTextColor() : Color.BLACK;

        BufferedImage image = SwingImageHelper.textToImage(true, callOut.getTextLines(), Colors.ALL_BUT_TRANSPARENT_BLACK,
                textColor, border, callOut.getFont(), 0);

        ScreenPosition upperLeft = new ScreenPosition(0., 0.);
        ScreenPosition lowerRight = new ScreenPosition(image.getWidth(), image.getHeight());
        GeographicPosition anchor = new GeographicPosition(callOut.getLocation());
        GeoScreenBoundingBox gsbb = new GeoScreenBoundingBox(upperLeft, lowerRight,
                new GeographicBoxAnchor(anchor, callOut.getAnchorOffset(), 0f, 0f));

        TileGeometry.Builder<ScreenPosition> tileBuilder = new TileGeometry.Builder<>();
        tileBuilder.setBounds(gsbb);
        tileBuilder.setDataModelId(callOut.getId());
        SingletonImageProvider imageProvider = new SingletonImageProvider(image, Image.CompressionType.D3DFMT_A8R8G8B8);
        tileBuilder.setImageManager(new ImageManager((Void)null, imageProvider));
        TileRenderProperties props = new DefaultTileRenderProperties(ZOrderRenderProperties.TOP_Z - ourZDelta, true,
                callOut.isPickable());
        if (!isFilled)
        {
            props.setHighlightColorARGB(callOut.getTextColor().getRGB() ^ 0x00ffffff);
            props.setColorARGB(callOut.getTextColor().getRGB());
        }

        Constraints constraints = null;
        if (date != null)
        {
            TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(date);
            constraints = new Constraints(timeConstraint);
        }

        return new AnnotationPointTileGeometry(tileBuilder, props, constraints);
    }

    /**
     * Creates an updated tile geometry using the new call out and previous tile
     * geometry.
     *
     * @param callOut The new call out.
     * @param previousTile The previous tile geometry.
     * @return The updated tile geometry.
     */
    private static TileGeometry updateTile(Callout callOut, TileGeometry previousTile)
    {
        BufferedImage image = SwingImageHelper.textToImage(true, callOut.getTextLines(), new Color(0, 0, 0, 1), Color.BLACK, null,
                callOut.getFont());

        // Need to use the same anchor but change bounding box size in case the
        // call out text has changed.
        ScreenPosition upperLeft = new ScreenPosition(0., 0.);
        ScreenPosition lowerRight = new ScreenPosition(image.getWidth(), image.getHeight());

        GeoScreenBoundingBox newBoundingBox = new GeoScreenBoundingBox(upperLeft, lowerRight,
                ((GeoScreenBoundingBox)previousTile.getBounds()).getAnchor());

        TileGeometry.Builder<ScreenPosition> tileBuilder = new TileGeometry.Builder<>();
        tileBuilder.setBounds(newBoundingBox);
        tileBuilder.setDataModelId(callOut.getId());
        SingletonImageProvider imageProvider = new SingletonImageProvider(image, Image.CompressionType.D3DFMT_A8R8G8B8);
        tileBuilder.setImageManager(new ImageManager((Void)null, imageProvider));

        return new AnnotationPointTileGeometry(tileBuilder, previousTile.getRenderProperties(), previousTile.getConstraints());
    }

    /** Disallow instantiation. */
    private MapPointTransformerHelper()
    {
    }
}
