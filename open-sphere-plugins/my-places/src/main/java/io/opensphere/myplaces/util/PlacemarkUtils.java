package io.opensphere.myplaces.util;

import java.awt.Color;
import java.awt.Font;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesEditListener;

/**
 * Utilities class for placemarks.
 *
 */
public final class PlacemarkUtils
{
    /** The default color. */
    public static final Color DEFAULT_COLOR = new Color(84, 84, 107, 190);

    /** The default font. */
    public static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    /**
     * Copies one placemark to another.
     *
     * @param from The placemark to copy from.
     * @param to The placemark to copy to.
     */
    public static void copyPlacemark(Placemark from, Placemark to)
    {
        to.setName(from.getName());
        to.setId(from.getId());
        to.setGeometry(from.getGeometry());
        to.setDescription(from.getDescription());
        to.setExtendedData(from.getExtendedData());
        to.setStyleSelector(from.getStyleSelector());
        to.setTimePrimitive(from.getTimePrimitive());
    }

    /**
     * Creates the data type for the placemark.
     *
     * @param placemark The placemark to create the data type for.
     * @param toolbox The toolbox.
     * @param source The source.
     * @param controller The place type controller.
     * @return The data type representing the placemark.
     */
    public static MyPlacesDataTypeInfo createDataType(Placemark placemark, Toolbox toolbox, Object source,
            MyPlacesEditListener controller)
    {
        MapVisualizationType visType = ExtendedDataUtils.getVisualizationType(placemark.getExtendedData());
        boolean theStyle = false;

        if (placemark.getId() == null)
        {
            placemark.setId(UUID.randomUUID().toString());
        }

        MyPlacesDataTypeInfo dataType = new MyPlacesDataTypeInfo(toolbox, placemark, controller);

        LoadsTo loadsTo = LoadsTo.STATIC;
        if (visType == MapVisualizationType.USER_TRACK_ELEMENTS)
        {
            loadsTo = LoadsTo.TIMELINE;
            theStyle = true;
        }

        DefaultMapFeatureVisualizationInfo visualInfo = new DefaultMapFeatureVisualizationInfo(visType, theStyle);

        dataType.setMapVisualizationInfo(visualInfo);

        Color color = PlacemarkUtils.getPlacemarkColor(placemark);

        dataType.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(loadsTo, color, false));
        dataType.getBasicVisualizationInfo().setTypeColor(color, source);

        Boolean visibility = placemark.isVisibility();
        if (!Boolean.TRUE.equals(visibility))
        {
            dataType.setVisible(false, source);
        }

        DataGroupController dataGroupController = MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController();
        dataType.registerInUse(dataGroupController, false);

        return dataType;
    }

    /**
     * Gets the placemark's color.
     *
     * @param placemark The placemarks color.
     * @return The color of the placemark, or null if there isn't one.f
     */
    public static Color getPlacemarkColor(Placemark placemark)
    {
        return getPlacemarkColor(placemark, DEFAULT_COLOR);
    }

    /**
     * Gets the placemark's color.
     *
     * @param placemark The placemarks color.
     * @param defaultColor The default color to return if the placemark does not
     *            have a color set, or null if a default color is not desired.
     * @return The color of the placemark, or null if there isn't one.
     */
    public static Color getPlacemarkColor(Placemark placemark, Color defaultColor)
    {
        Color color = null;

        for (StyleSelector selector : placemark.getStyleSelector())
        {
            if (selector instanceof Style)
            {
                Style style = (Style)selector;
                IconStyle iconStyle = style.getIconStyle();
                if (iconStyle != null)
                {
                    color = ColorUtilities.convertFromHexString(iconStyle.getColor(), 3, 2, 1, 0);
                    break;
                }
            }
        }

        if (color == null)
        {
            color = defaultColor;
        }

        return color;
    }

    /**
     * Gets the placemark's polygon fill color.
     *
     * @param placemark The placemark for which to get the fill color.
     * @return The color of the polygon fill, or null if there isn't one.
     */
    public static Color getPolygonFillColor(Placemark placemark)
    {
        return getPolygonFillColor(placemark, DEFAULT_COLOR);
    }

    /**
     * Gets the placemark's polygon fill color.
     *
     * @param placemark The placemark for which to get the fill color.
     * @param defaultColor The default color to return if the placemark does not
     *            have a color set, or null if a default color is not desired.
     * @return The color of the placemark, or null if there isn't one.
     */
    public static Color getPolygonFillColor(Placemark placemark, Color defaultColor)
    {
        Color color = null;

        for (StyleSelector selector : placemark.getStyleSelector())
        {
            if (selector instanceof Style)
            {
                Style style = (Style)selector;
                PolyStyle polygonStyle = style.getPolyStyle();
                if (polygonStyle != null)
                {
                    color = ColorUtilities.convertFromHexString(polygonStyle.getColor(), 3, 2, 1, 0);
                    break;
                }
            }
        }

        if (color == null)
        {
            color = defaultColor;
        }

        return color;
    }

    /**
     * Gets the placemark's line color.
     *
     * @param placemark The placemark for which to get the line color.
     * @return The color of the polygon fill, or null if there isn't one.
     */
    public static Color getLineColor(Placemark placemark)
    {
        return getLineColor(placemark, DEFAULT_COLOR);
    }

    /**
     * Gets the placemark's line color.
     *
     * @param placemark The placemark for which to get the line color.
     * @param defaultColor The default color to return if the placemark does not
     *            have a color set, or null if a default color is not desired.
     * @return The color of the placemark, or null if there isn't one.
     */
    public static Color getLineColor(Placemark placemark, Color defaultColor)
    {
        Color color = null;

        for (StyleSelector selector : placemark.getStyleSelector())
        {
            if (selector instanceof Style)
            {
                Style style = (Style)selector;
                LineStyle lineStyle = style.getLineStyle();
                if (lineStyle != null)
                {
                    color = ColorUtilities.convertFromHexString(lineStyle.getColor(), 3, 2, 1, 0);
                    break;
                }
            }
        }

        if (color == null)
        {
            color = defaultColor;
        }

        return color;
    }

    /**
     * Gets the font set for the placemark.
     *
     * @param placemark The placemark to get the font for.
     * @return The placemarks font.
     */
    public static Font getPlacemarkFont(Placemark placemark)
    {
        if (placemark == null)
        {
            return DEFAULT_FONT;
        }

        ExtendedData extendedData = placemark.getExtendedData();
        if (extendedData == null)
        {
            return DEFAULT_FONT;
        }

        String fontName = ExtendedDataUtils.getString(extendedData, Constants.FONT_NAME_ID);
        if (fontName == null)
        {
            return DEFAULT_FONT;
        }

        int fontSize = ExtendedDataUtils.getInt(extendedData, Constants.FONT_SIZE_ID, 0);
        int fontStyle = ExtendedDataUtils.getInt(extendedData, Constants.FONT_STYLE_ID, 0);

        Font font = new Font(fontName, fontStyle, fontSize);

        return font;
    }

    /**
     * Gets the placemark's color.
     *
     * @param placemark The placemarks color.
     * @return The color of the placemark, or null if there isn't one.f
     */
    public static Color getPlacemarkTextColor(Placemark placemark)
    {
        Color color = null;

        for (StyleSelector selector : placemark.getStyleSelector())
        {
            if (selector instanceof Style)
            {
                Style style = (Style)selector;
                BalloonStyle balloonStyle = style.getBalloonStyle();
                String textColor = balloonStyle.getTextColor();

                if (StringUtils.isNotEmpty(textColor))
                {
                    color = ColorUtilities.convertFromHexString(textColor, 3, 2, 1, 0);
                }
                else
                {
                    color = Color.white;
                }

                break;
            }
        }

        return color;
    }

    /**
     * Sets the placemark color.
     *
     * @param placemark The placemark to set the color for.
     * @param color The color to set.
     * @return The style containing the text.
     */
    public static Style setPlacemarkColor(Placemark placemark, Color color)
    {
        Style style = getOrCreateStyle(placemark);

        IconStyle iconStyle = style.getIconStyle();
        if (iconStyle == null)
        {
            iconStyle = new IconStyle();
            style.setIconStyle(iconStyle);
        }

        iconStyle.setScale(1d);
        iconStyle.setColor(ColorUtilities.convertToHexString(color, 3, 2, 1, 0));

        return style;
    }

    /**
     * Sets the placemark color.
     *
     * @param placemark The placemark to set the color for.
     * @param color The color to set.
     * @return The style containing the text.
     */
    public static Style setPolygonFillColor(Placemark placemark, Color color)
    {
        Style style = getOrCreateStyle(placemark);

        PolyStyle polygonStyle = style.getPolyStyle();
        if (polygonStyle == null)
        {
            polygonStyle = new PolyStyle();
            style.setPolyStyle(polygonStyle);
        }

        polygonStyle.setColor(ColorUtilities.convertToHexString(color, 3, 2, 1, 0));
        polygonStyle.setOutline(true);

        return style;
    }

    /**
     * Sets the line color.
     *
     * @param placemark The placemark to set the line color for.
     * @param color The color to set.
     * @return The style containing the text.
     */
    public static Style setLineColor(Placemark placemark, Color color)
    {
        Style style = getOrCreateStyle(placemark);

        LineStyle lineStyle = style.getLineStyle();
        if (lineStyle == null)
        {
            lineStyle = new LineStyle();
            style.setLineStyle(lineStyle);
        }

        lineStyle.setColor(ColorUtilities.convertToHexString(color, 3, 2, 1, 0));

        return style;
    }

    /**
     * Sets the font for the placemark.
     *
     * @param placemark The placemark to set the font for.
     * @param font The font to set.
     */
    public static void setPlacemarkFont(Placemark placemark, Font font)
    {
        ExtendedData extendedData = placemark.getExtendedData();

        ExtendedDataUtils.putString(extendedData, Constants.FONT_NAME_ID, font.getName());
        ExtendedDataUtils.putInt(extendedData, Constants.FONT_SIZE_ID, font.getSize());
        ExtendedDataUtils.putInt(extendedData, Constants.FONT_STYLE_ID, font.getStyle());
    }

    /**
     * Sets the text color for the placemark.
     *
     * @param placemark The placemark to set the color for.
     * @param color the color to set.
     */
    public static void setPlacemarkTextColor(Placemark placemark, Color color)
    {
        Style style = getOrCreateStyle(placemark);

        BalloonStyle balloonStyle = style.getBalloonStyle();
        if (balloonStyle == null)
        {
            balloonStyle = new BalloonStyle();
            style.setBalloonStyle(balloonStyle);
        }

        balloonStyle.setTextColor(ColorUtilities.convertToHexString(color, 3, 2, 1, 0));
    }

    /**
     * Gets the style from the placemark or creates it if necessary.
     *
     * @param placemark the placemark
     * @return the style
     */
    private static Style getOrCreateStyle(Placemark placemark)
    {
        Style style = null;
        for (StyleSelector selector : placemark.getStyleSelector())
        {
            if (selector instanceof Style)
            {
                style = (Style)selector;
                break;
            }
        }
        if (style == null)
        {
            style = placemark.createAndAddStyle();
        }
        return style;
    }

    /**
     * Non constructible.
     */
    private PlacemarkUtils()
    {
    }
}
