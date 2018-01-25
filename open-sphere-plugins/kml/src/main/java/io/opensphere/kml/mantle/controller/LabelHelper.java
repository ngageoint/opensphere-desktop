package io.opensphere.kml.mantle.controller;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import io.opensphere.core.geometry.ColorGeometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.kml.common.util.KMLStyleCache;

/** Label helper. */
public final class LabelHelper
{
    /**
     * Creates a label geometry from the feature. Not all features will have a
     * label, so it could be null.
     *
     * @param feature The KML feature.
     * @param styleCache The KML style "cache"
     * @return the label, or null
     */
    public static LabelGeometry createLabel(KMLFeature feature, KMLStyleCache styleCache)
    {
        LabelGeometry geom = null;

        if (feature.getLabel() == null && !StringUtils.isBlank(feature.getName())
                && feature.getCreatingDataSource().getRootDataSource().isShowLabels() && bigEnough(feature, styleCache))
        {
            LatLonAlt location = getLabelLocation(feature);
            if (location != null)
            {
                LabelGeometry.Builder<GeographicPosition> builder = new LabelGeometry.Builder<>();
                builder.setPosition(new GeographicPosition(location));
                builder.setText(" " + feature.getName());
                builder.setOutlined(true);

                LabelRenderProperties renderProperties = new DefaultLabelRenderProperties(0, true, false);
                renderProperties.setScaleFunction(KMLMantleUtilities.getScaleFunction(feature.getDataSource()));
                Style style = styleCache.getStyle(feature, StyleState.NORMAL);
                if (style != null && style.getLabelStyle() != null)
                {
                    Color color = KMLSpatialTemporalUtils.convertColor(style.getLabelStyle().getColor());
                    if (color != null)
                    {
                        renderProperties.setColor(color);
                    }
                }
                setHighlighted(feature, styleCache, false, renderProperties);

                Constraints constraints = new Constraints(KMLSpatialTemporalUtils.getTimeConstraint(feature));

                geom = new LabelGeometry(builder, renderProperties, constraints);
            }
        }

        return geom;
    }

    /**
     * Sets whether the label for the feature is highlighted.
     *
     * @param feature the feature
     * @param styleCache the style cache
     * @param isHighlighted whether it's highlighted
     */
    public static void setHighlighted(KMLFeature feature, KMLStyleCache styleCache, boolean isHighlighted)
    {
        if (feature != null && feature.getLabel() != null)
        {
            setHighlighted(feature, styleCache, isHighlighted, feature.getLabel().getRenderProperties());
        }
    }

    /**
     * Sets the visibility of the given feature's label.
     *
     * @param feature The feature
     * @param isVisible Whether the label is to be visible
     */
    public static void setVisibility(KMLFeature feature, boolean isVisible)
    {
        feature.getLabel().getRenderProperties().setHidden(!isVisible);
    }

    /**
     * Sets the opacity of the label geometry.
     *
     * @param geometry The label geometry
     * @param opacity the opacity to set
     */
    public static void setOpacity(ColorGeometry geometry, int opacity)
    {
        ColorRenderProperties renderProperties = geometry.getRenderProperties();
        Color color = renderProperties.getColor();
        renderProperties.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity));
    }

    /**
     * Sets whether the label render properties for the feature is highlighted.
     *
     * @param feature the feature
     * @param styleCache the style cache
     * @param isHighlighted whether it's highlighted
     * @param renderProperties the label render properties
     */
    private static void setHighlighted(KMLFeature feature, KMLStyleCache styleCache, boolean isHighlighted,
            LabelRenderProperties renderProperties)
    {
        int fontSize = getFontSize(feature, styleCache, isHighlighted ? StyleState.HIGHLIGHT : StyleState.NORMAL);
        renderProperties.setFont(StringUtilities.concat(Font.SANS_SERIF, " ", Integer.toString(fontSize)));
    }

    /**
     * Determines if the label font has sufficient size.
     *
     * @param feature the feature
     * @param styleCache the style cache
     * @return whether the font is big enough
     */
    private static boolean bigEnough(KMLFeature feature, KMLStyleCache styleCache)
    {
        return getFontSize(feature, styleCache, StyleState.NORMAL) >= 8
                || getFontSize(feature, styleCache, StyleState.HIGHLIGHT) >= 8;
    }

    /**
     * Gets the font size of the label.
     *
     * @param feature the feature
     * @param styleCache the style cache
     * @param styleState the style state
     * @return the font size
     */
    private static int getFontSize(KMLFeature feature, KMLStyleCache styleCache, StyleState styleState)
    {
        Style style = styleCache.getStyle(feature, styleState);
        float scale = style != null && style.getLabelStyle() != null ? (float)style.getLabelStyle().getScale() : 1f;
        int fontSize = Math.round(scale * 18);
        return fontSize;
    }

    /**
     * Get the label location of the given feature.
     *
     * @param feature The feature
     * @return The label location
     */
    private static LatLonAlt getLabelLocation(KMLFeature feature)
    {
        LatLonAlt location = null;

        // Get the first point from the geometry
        Point point = null;
        Placemark placemark = (Placemark)feature.getFeature();
        if (placemark.getGeometry() instanceof Point)
        {
            point = (Point)placemark.getGeometry();
        }
        else if (placemark.getGeometry() instanceof MultiGeometry)
        {
            MultiGeometry multiGeom = (MultiGeometry)placemark.getGeometry();
            for (de.micromata.opengis.kml.v_2_2_0.Geometry childGeom : multiGeom.getGeometry())
            {
                if (childGeom instanceof Point)
                {
                    point = (Point)childGeom;
                    break;
                }
            }
        }

        // Create a location from the point
        if (point != null)
        {
            AltitudeMode altitudeMode = feature.getDataSource().isClampToTerrain() ? AltitudeMode.CLAMP_TO_GROUND
                    : point.getAltitudeMode();
            List<LatLonAlt> locations = KMLSpatialTemporalUtils.convertCoordinates(point.getCoordinates(), altitudeMode);
            if (!locations.isEmpty())
            {
                location = locations.get(0);
            }
        }

        return location;
    }

    /** Private constructor. */
    private LabelHelper()
    {
    }
}
