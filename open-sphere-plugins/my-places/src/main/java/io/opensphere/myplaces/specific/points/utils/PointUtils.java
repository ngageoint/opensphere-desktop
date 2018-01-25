package io.opensphere.myplaces.specific.points.utils;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointSettings;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPoint;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPointSettings;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Utility class for points.
 */
public final class PointUtils
{
    /**
     * Edits the specified placemark with the values from point.
     *
     * @param existing The placemark to edit.
     * @param point The point to convert to kml.
     */
    public static void editExistingKml(Placemark existing, MapAnnotationPoint point)
    {
        existing.setName(point.getTitle());
        existing.setDescription(point.getDescription());

        Geometry geometry = existing.getGeometry();
        if (geometry instanceof Point)
        {
            Point kmlPoint = (Point)geometry;
            kmlPoint.getCoordinates().clear();
            kmlPoint.addToCoordinates(point.getLon(), point.getLat());

            Style style = PlacemarkUtils.setPlacemarkColor(existing, point.getColor());

            BalloonStyle balloonStyle = style.getBalloonStyle();
            balloonStyle.setTextColor(ColorUtilities.convertToHexString(point.getFontColor(), 3, 2, 1, 0));

            ExtendedData newExtended = new ExtendedData();
            existing.setExtendedData(newExtended);

            migrateExtendedData(point, newExtended);
        }
    }

    /**
     * Converts the placemark into a point.
     *
     * @param placemark The placemark containing all point data.
     * @param source The object creating the point.
     * @return The point.
     */
    public static MutableMapAnnotationPoint fromKml(Placemark placemark, Object source)
    {
        DefaultMapAnnotationPoint point = null;

        Geometry geometry = placemark.getGeometry();

        if (geometry instanceof Point)
        {
            Point kmlPoint = (Point)geometry;
            point = new DefaultMapAnnotationPoint();
            ExtendedData extendedData = placemark.getExtendedData();

            point.setTitle(placemark.getName(), source);
            point.setDescription(placemark.getDescription(), source);

            List<Coordinate> coords = kmlPoint.getCoordinates();

            double lat = coords.get(0).getLatitude();
            double lon = coords.get(0).getLongitude();
            double altitude = coords.get(0).getAltitude();
            if (!coords.isEmpty())
            {
                String decPrecision = ExtendedDataUtils.getString(extendedData, Constants.DECIMAL_PRECISION);

                if (decPrecision != null)
                {
                    int precision = Integer.parseInt(decPrecision);

                    StringBuilder sb = new StringBuilder();
                    sb.append("##0.");
                    for (int i = 0; i < precision; i++)
                    {
                        sb.append('#');
                    }
                    DecimalFormat df = new DecimalFormat(sb.toString());
                    lat = Double.parseDouble(df.format(lat));
                    lon = Double.parseDouble(df.format(lon));
                    altitude = Double.parseDouble(df.format(altitude));
                }
                point.setLat(lat, source);
                point.setLon(lon, source);

                if (kmlPoint.getAltitudeMode() != AltitudeMode.CLAMP_TO_GROUND)
                {
                    point.setAltitude(altitude, source);
                }
            }

            for (StyleSelector selector : placemark.getStyleSelector())
            {
                if (selector instanceof Style)
                {
                    Style style = (Style)selector;
                    IconStyle iconStyle = style.getIconStyle();
                    Color color = ColorUtilities.convertFromHexString(iconStyle.getColor(), 3, 2, 1, 0);
                    point.setColor(color, source);
                    point.setBackgroundColor(color, source);

                    BalloonStyle balloonStyle = style.getBalloonStyle();
                    String textColor = balloonStyle.getTextColor();

                    if (StringUtils.isNotEmpty(textColor))
                    {
                        color = ColorUtilities.convertFromHexString(balloonStyle.getTextColor(), 3, 2, 1, 0);
                        point.setFontColor(color, source);
                    }

                    break;
                }
            }

            if (placemark.getTimePrimitive() != null)
            {
                point.setTime(KMLSpatialTemporalUtils.timeSpanFromTimePrimitive(placemark.getTimePrimitive()));
            }

            getExtendedData(point, extendedData, source);
        }

        return point;
    }

    /**
     * Adds a point Placemark to the specified Folder.  If no Folder is
     * specified, then the Placemark is created but is not added to a Folder.
     *
     * @param folder the Folder to add to, if any
     * @param point the point to convert to KML
     * @return a Placemark representing the point
     */
    public static Placemark toKml(Folder folder, MapAnnotationPoint point)
    {
        Placemark placemark = null;

        if (folder != null)
        {
            placemark = folder.createAndAddPlacemark();
        }
        else
        {
            placemark = new Placemark();
        }

        placemark.setId(UUID.randomUUID().toString());
        placemark.setName(point.getTitle());
        placemark.setDescription(point.getDescription());
        placemark.setVisibility(Boolean.valueOf(point.isVisible()));

        Point kmlPoint = placemark.createAndSetPoint();
        kmlPoint.addToCoordinates(point.getLon(), point.getLat(), point.getAltitude());

        if (point.hasAltitude())
        {
            kmlPoint.setAltitudeMode(AltitudeMode.RELATIVE_TO_SEA_FLOOR);
        }
        else
        {
            kmlPoint.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        }

        if (point.isTimeEnabled() && point.getTime() != null)
        {
            TimeSpan pointTime = point.getTime();
            if (pointTime.isInstantaneous())
            {
                TimeStamp primitive = new TimeStamp();
                primitive.setWhen(pointTime.toISO8601String());
                placemark.setTimePrimitive(primitive);
            }
            else
            {
                de.micromata.opengis.kml.v_2_2_0.TimeSpan primitive = new de.micromata.opengis.kml.v_2_2_0.TimeSpan();
                primitive.setBegin(pointTime.getStartInstant().toISO8601String());
                primitive.setEnd(pointTime.getEndInstant().toISO8601String());
                placemark.setTimePrimitive(primitive);
            }
        }

        Style style = PlacemarkUtils.setPlacemarkColor(placemark, point.getColor());

        BalloonStyle balloonStyle = new BalloonStyle();
        balloonStyle.setColor(style.getIconStyle().getColor());
        balloonStyle.setTextColor(ColorUtilities.convertToHexString(point.getFontColor(), 3, 2, 1, 0));

        style.setBalloonStyle(balloonStyle);

        ExtendedData extendedData = new ExtendedData();
        placemark.setExtendedData(extendedData);

        migrateExtendedData(point, extendedData);

        placemark.setTimePrimitive(KMLSpatialTemporalUtils.timeSpanToTimePrimitive(point.getTime()));

        if (point.getAltitude() != Double.NaN && point.getAltitude() > 0)
        {
            ExtendedDataUtils.putBoolean(extendedData, Constants.IS_ALTITUDE_ID, true);
        }

        return placemark;
    }

    /**
     * Adds the extended data to the point.
     *
     * @param point The point to add data to.
     * @param extendedData The extended data to add.
     * @param source The object creating the extended data.
     */
    private static void getExtendedData(DefaultMapAnnotationPoint point, ExtendedData extendedData, Object source)
    {
        String fontName = ExtendedDataUtils.getString(extendedData, Constants.FONT_NAME_ID);
        int fontSize = ExtendedDataUtils.getInt(extendedData, Constants.FONT_SIZE_ID, 0);
        int fontStyle = ExtendedDataUtils.getInt(extendedData, Constants.FONT_STYLE_ID, 0);

        Font font = new Font(fontName, fontStyle, fontSize);

        point.setFont(font, source);
        point.setMGRS(ExtendedDataUtils.getString(extendedData, Constants.MGRS_ID), source);
        point.setxOffset(ExtendedDataUtils.getInt(extendedData, Constants.X_OFFSET_ID, 0), source);
        point.setyOffset(ExtendedDataUtils.getInt(extendedData, Constants.Y_OFFSET_ID, 0), source);
        point.setFilled(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_BUBBLE_FILLED_ID, false), source);
        point.setAssociatedViewName(ExtendedDataUtils.getString(extendedData, Constants.ASSOCIATED_VIEW_ID), source);

        if (point.getAnnoSettings() instanceof DefaultMapAnnotationPointSettings)
        {
            DefaultMapAnnotationPointSettings settings = (DefaultMapAnnotationPointSettings)point.getAnnoSettings();

            settings.setAnnohide(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_ANNOHIDE_ID, false), source);
            settings.setDesc(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DESC_ID, false), source);
            settings.setDms(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DMS_ID, false), source);
            settings.setDotOn(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_FEATURE_ON_ID, false), source);
            settings.setLatLon(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_LAT_LON_ID, false), source);
            settings.setMgrs(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_MGRS, false), source);
            settings.setAltitude(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_SHOW_ALTITUDE, false), source);
            settings.setTitle(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_TITLE, false), source);
            settings.setFieldTitle(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_FIELD_TITLE, true), source);

            settings.setDistance(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DISTANCE_ID, true), source);
            settings.setDuration(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DURATION_ID, true), source);
            settings.setHeading(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_HEADING_ID, true), source);
            settings.setVelocity(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_VELOCITY_ID, true), source);
        }
    }

    /**
     * Migrates the extended data.
     *
     * @param point The point to migrate.
     * @param extendedData The extended data.
     */
    private static void migrateExtendedData(MapAnnotationPoint point, ExtendedData extendedData)
    {
        ExtendedDataUtils.putString(extendedData, Constants.FONT_NAME_ID, point.getFont().getName());
        ExtendedDataUtils.putInt(extendedData, Constants.FONT_SIZE_ID, point.getFont().getSize());
        ExtendedDataUtils.putInt(extendedData, Constants.FONT_STYLE_ID, point.getFont().getStyle());
        ExtendedDataUtils.putVisualizationType(extendedData, MapVisualizationType.ANNOTATION_POINTS);
        ExtendedDataUtils.putString(extendedData, Constants.MGRS_ID, point.getMGRS());
        ExtendedDataUtils.putInt(extendedData, Constants.X_OFFSET_ID, point.getxOffset());
        ExtendedDataUtils.putInt(extendedData, Constants.Y_OFFSET_ID, point.getyOffset());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_BUBBLE_FILLED_ID, point.isFilled());
        ExtendedDataUtils.putString(extendedData, Constants.ASSOCIATED_VIEW_ID, point.getAssociatedViewName());

        MapAnnotationPointSettings settings = point.getAnnoSettings();

        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_ANNOHIDE_ID, settings.isAnnohide());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DESC_ID, settings.isDesc());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DMS_ID, settings.isDms());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_FEATURE_ON_ID, settings.isDotOn());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_LAT_LON_ID, settings.isLatLon());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_MGRS, settings.isMgrs());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_SHOW_ALTITUDE, settings.isAltitude());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_TITLE, settings.isTitle());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_FIELD_TITLE, settings.isFieldTitle());

        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DISTANCE_ID, settings.isDistance());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DURATION_ID, settings.isDuration());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_HEADING_ID, settings.isHeading());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_VELOCITY_ID, settings.isVelocity());
    }

    /**
     * not constructible.
     */
    private PointUtils()
    {
    }
}
