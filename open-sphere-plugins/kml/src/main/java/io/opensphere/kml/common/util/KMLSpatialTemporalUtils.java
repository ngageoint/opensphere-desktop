package io.opensphere.kml.common.util;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.LatLonAltBox;
import de.micromata.opengis.kml.v_2_2_0.LatLonBox;
import de.micromata.opengis.kml.v_2_2_0.Location;
import de.micromata.opengis.kml.v_2_2_0.LookAt;
import de.micromata.opengis.kml.v_2_2_0.ScreenOverlay;
import de.micromata.opengis.kml.v_2_2_0.TimePrimitive;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;
import io.opensphere.core.control.ui.ToolbarManager;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.LongSupplier;
import io.opensphere.core.util.SystemTimeMillisSupplier;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;

/**
 * KML Spatial/Temporal Utilities.
 */
@SuppressWarnings("PMD.GodClass")
public final class KMLSpatialTemporalUtils
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLSpatialTemporalUtils.class);

    /** The KML time pattern. */
    private static final Pattern KML_TIME_PATTERN = Pattern
            .compile("(\\d{4})(-\\d{2})?(-\\d{2})?(T\\d{2})?(:\\d{2})?(:\\d{2})?(\\.\\d{3})?\\d*(.+)?");

    /** RFC 1123 date format (used in HTTP headers). */
    private static final SimpleDateFormat RFC_1123_DATE_FORMAT1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    /** RFC 1123 date format (used in HTTP headers). */
    private static final SimpleDateFormat RFC_1123_DATE_FORMAT2 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

    /** Approximate milliseconds per year. */
    private static final long MILLIS_PER_YEAR = 31536000000L;

    static
    {
        RFC_1123_DATE_FORMAT1.setTimeZone(TimeZone.getTimeZone("GMT"));
        RFC_1123_DATE_FORMAT2.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Convert an JAK LatLonAltBox to Core GeographicBoundingBox.
     *
     * @param latLonAltBox The LatLonAltBox
     * @return The equivalent GeographicBoundingBox
     */
    public static GeographicBoundingBox caclulateGeographicBoundingBox(LatLonAltBox latLonAltBox)
    {
        Altitude.ReferenceLevel referenceLevel = convertAltitudeMode(latLonAltBox.getAltitudeMode());
        boolean isZeroAltitude = isZeroAltitude(latLonAltBox.getAltitudeMode());
        double minAltitude = isZeroAltitude ? 0.0 : latLonAltBox.getMinAltitude();
        double maxAltitude = isZeroAltitude ? 0.0 : latLonAltBox.getMaxAltitude();
        LatLonAlt lowerLeftCorner = LatLonAlt.createFromDegreesMeters(latLonAltBox.getSouth(), latLonAltBox.getWest(),
                minAltitude, referenceLevel);
        LatLonAlt upperRightCorner = LatLonAlt.createFromDegreesMeters(latLonAltBox.getNorth(), latLonAltBox.getEast(),
                maxAltitude, referenceLevel);
        return new GeographicBoundingBox(lowerLeftCorner, upperRightCorner);
    }

    /**
     * Convert an JAK LatLonBox to Core GeographicBoundingBox.
     *
     * @param latLonBox The LatLonBox
     * @return The equivalent GeographicBoundingBox
     */
    public static GeographicBoundingBox calculateGeographicBoundingBox(LatLonBox latLonBox)
    {
        LatLonAlt lowerLeftCorner = LatLonAlt.createFromDegrees(latLonBox.getSouth(), latLonBox.getWest());
        LatLonAlt upperRightCorner = LatLonAlt.createFromDegrees(latLonBox.getNorth(), latLonBox.getEast());
        return new GeographicBoundingBox(lowerLeftCorner, upperRightCorner);
    }

    /**
     * Calculates a ScreenBoundingBox.
     *
     * @param screenOverlay The screen overlay
     * @param image The image
     * @param viewer The viewer
     * @param toolbarManager The toolbar manager
     * @return The equivalent ScreenBoundingBox
     */
    public static ScreenBoundingBox calculateScreenBoundingBox(ScreenOverlay screenOverlay, Image image, Viewer viewer,
            ToolbarManager toolbarManager)
    {
        // Calculate image size
        Vec2 size = screenOverlay.getSize();
        if (size == null)
        {
            size = new Vec2();
            size.setX(image.getWidth());
            size.setXunits(Units.PIXELS);
            size.setY(image.getHeight());
            size.setYunits(Units.PIXELS);
        }
        int viewportWidth = viewer.getViewportWidth();
        // HACK: Subtracting the south toolbar height is not ideal. The viewer
        // should provide a way to get the usable height.
        int viewportHeight = viewer.getViewportHeight() - toolbarManager.getSouthToolbar().getHeight();
        double maxWidth = size.getX() != -1 && size.getXunits() == Units.FRACTION ? viewportWidth : image.getWidth();
        double maxHeight = size.getY() != -1 && size.getYunits() == Units.FRACTION ? viewportHeight : image.getHeight();
        double imageWidth = calculateX(size, maxWidth);
        double imageHeight = calculateY(size, maxHeight);

        // Adjust image size for special values
        if (screenOverlay.getSize() != null)
        {
            if (screenOverlay.getSize().getX() == 0 && screenOverlay.getSize().getY() == 0)
            {
                imageWidth = image.getWidth();
                imageHeight = image.getHeight();
            }
            else if (screenOverlay.getSize().getX() == 0)
            {
                imageWidth = image.getWidth() * imageHeight / image.getHeight();
            }
            else if (screenOverlay.getSize().getY() == 0)
            {
                imageHeight = image.getHeight() * imageWidth / image.getWidth();
            }
        }

        // Calculate image location
        Vec2 screenLocation = screenOverlay.getScreenXY();
        if (screenLocation == null)
        {
            screenLocation = new Vec2();
            screenLocation.setX(0.5);
            screenLocation.setY(0.5);
        }
        Vec2 overlayLocation = screenOverlay.getOverlayXY();
        if (overlayLocation == null)
        {
            overlayLocation = new Vec2();
            overlayLocation.setX(0.5);
            overlayLocation.setY(0.5);
        }
        double screenX = calculateX(screenLocation, viewportWidth);
        double screenY = calculateY(screenLocation, viewportHeight);
        double overlayX = calculateX(overlayLocation, imageWidth);
        double overlayY = calculateY(overlayLocation, imageHeight);

        // Calculate the screen bounding box coordinates
        double upperLeftX = screenX - overlayX;
        double upperLeftY = viewportHeight - (screenY - overlayY + imageHeight);
        double lowerRightX = upperLeftX + imageWidth;
        double lowerRightY = upperLeftY + imageHeight;

        // Create the bounding box
        ScreenPosition upperLeft = new ScreenPosition(upperLeftX, upperLeftY);
        ScreenPosition lowerRight = new ScreenPosition(lowerRightX, lowerRightY);
        return new ScreenBoundingBox(upperLeft, lowerRight);
    }

    /**
     * Calculates an x value.
     *
     * @param vec The vector
     * @param width The width
     * @return The x value
     */
    public static double calculateX(Vec2 vec, double width)
    {
        return calculate(vec.getX(), vec.getXunits(), width);
    }

    /**
     * Calculates a y value.
     *
     * @param vec The vector
     * @param height The height
     * @return The y value
     */
    public static double calculateY(Vec2 vec, double height)
    {
        return calculate(vec.getY(), vec.getYunits(), height);
    }

    /**
     * Converts a KML color string into a Color object.
     *
     * @param kmlColor The KML color string
     * @return The Color object, or null
     */
    public static Color convertColor(String kmlColor)
    {
        Color color = null;
        if (kmlColor != null)
        {
            String trimmedColor = kmlColor.trim();
            if (trimmedColor.length() >= 8)
            {
                try
                {
                    color = ColorUtilities.convertFromHexString(trimmedColor.substring(trimmedColor.length() - 8), 3, 2, 1, 0);
                }
                catch (NumberFormatException e)
                {
                    LOGGER.warn("Unable to convert KML color. " + e.getMessage());
                }
            }
            else
            {
                LOGGER.warn("Unable to convert KML color: " + trimmedColor);
            }
        }
        return color;
    }

    /**
     * Converts a list of Coordinates and an AltitudeMode to a list of
     * LatLonAlts.
     *
     * @param coordinates The coordinates
     * @param altitudeMode The altitude mode
     * @return The equivalent list of LatLonAlts
     */
    public static List<LatLonAlt> convertCoordinates(List<Coordinate> coordinates, AltitudeMode altitudeMode)
    {
        List<LatLonAlt> locations = new ArrayList<>(coordinates.size());
        Altitude.ReferenceLevel referenceLevel = convertAltitudeMode(altitudeMode);
        boolean isZeroAltitude = isZeroAltitude(altitudeMode);
        for (Coordinate coordinate : coordinates)
        {
            locations.add(convertCoordinate(coordinate, referenceLevel, isZeroAltitude));
        }
        return locations;
    }

    /**
     * Convert an JAK LookAt to a Core LatLonAlt.
     *
     * @param lookAt The LookAt
     * @return The equivalent LatLonAlt
     */
    public static LatLonAlt convertLookAt(LookAt lookAt)
    {
        Altitude.ReferenceLevel referenceLevel = convertAltitudeMode(lookAt.getAltitudeMode());
        boolean isZeroAltitude = isZeroAltitude(lookAt.getAltitudeMode());
        double altitude = isZeroAltitude ? 0.0 : lookAt.getAltitude();
        double distance = altitude + lookAt.getRange();
        return LatLonAlt.createFromDegreesMeters(lookAt.getLatitude(), lookAt.getLongitude(), distance, referenceLevel);
    }

    /**
     * Gets the expire time to use when Link.refreshMode = onExpire.
     *
     * @param networkLinkControlExpires The expire time of the
     *            NetworkLinkControl
     * @param cacheControlHeader The Cache-Control HTTP header value
     * @param expiresHeader The Expires HTTP header value
     * @return The expire time, or null
     */
    public static Date getExpireTime(String networkLinkControlExpires, String cacheControlHeader, String expiresHeader)
    {
        Date expireTime = parseDateString(networkLinkControlExpires);
        if (!isExpireTimeValid(expireTime))
        {
            expireTime = getMaxAgeHeaderTime(cacheControlHeader, SystemTimeMillisSupplier.INSTANCE);
            if (!isExpireTimeValid(expireTime))
            {
                expireTime = getExpiresHeaderTime(expiresHeader);
                if (!isExpireTimeValid(expireTime))
                {
                    expireTime = null;
                }
            }
        }
        return expireTime;
    }

    /**
     * Gets the time constraint for the given feature.
     *
     * @param feature the feature
     * @return the time constraint
     */
    public static TimeConstraint getTimeConstraint(KMLFeature feature)
    {
        TimeConstraint timeConstraint = null;
        if (feature.getLoadsTo() == LoadsTo.TIMELINE)
        {
            TimeExtents timeExtents = getTimeExtents(Collections.singletonList(feature));
            if (!timeExtents.getTimespans().isEmpty())
            {
                io.opensphere.core.model.time.TimeSpan timeSpan = timeExtents.getTimespans().get(0);
                String key = feature.getCreatingDataSource().getRootDataSource().getDataGroupInfo().getId();
                timeConstraint = TimeConstraint.getTimeConstraint(key, timeSpan);
            }
        }
        return timeConstraint;
    }

    /**
     * Returns the time extents of the given features.
     *
     * @param features The features
     * @return The TimeExtents
     */
    public static TimeExtents getTimeExtents(final Collection<KMLFeature> features)
    {
        DefaultTimeExtents timeExtents = new DefaultTimeExtents();

        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        for (KMLFeature feature : features)
        {
            if (feature.getTimePrimitive() instanceof TimeStamp)
            {
                Date time = parseDateString(((TimeStamp)feature.getTimePrimitive()).getWhen());
                if (time != null)
                {
                    long timeLong = time.getTime();
                    if (timeLong < minTime)
                    {
                        minTime = timeLong;
                    }
                    if (timeLong > maxTime)
                    {
                        maxTime = timeLong;
                    }
                }
            }
            else if (feature.getTimePrimitive() instanceof TimeSpan)
            {
                Date startTime = parseDateString(((TimeSpan)feature.getTimePrimitive()).getBegin());
                Date endTime = parseDateString(((TimeSpan)feature.getTimePrimitive()).getEnd());
                if (startTime != null)
                {
                    long timeLong = startTime.getTime();
                    if (timeLong < minTime)
                    {
                        minTime = timeLong;
                    }
                    if (timeLong > maxTime)
                    {
                        maxTime = timeLong;
                    }
                }
                if (endTime != null)
                {
                    long timeLong = endTime.getTime();
                    if (timeLong < minTime)
                    {
                        minTime = timeLong;
                    }
                    if (timeLong > maxTime)
                    {
                        maxTime = timeLong;
                    }
                }
            }
        }

        if (minTime != Long.MAX_VALUE && maxTime != Long.MIN_VALUE)
        {
            timeExtents.addTimeSpan(io.opensphere.core.model.time.TimeSpan.get(minTime, maxTime));
        }

        return timeExtents;
    }

    /**
     * Converts a JAK TimePrimitive to a Core TimeSpan.
     *
     * @param timePrimitive The TimePrimitive
     * @return The TimeSpan TimeSpan
     */
    public static io.opensphere.core.model.time.TimeSpan timeSpanFromTimePrimitive(TimePrimitive timePrimitive)
    {
        Date startTime;
        Date endTime;
        if (timePrimitive instanceof TimeStamp)
        {
            TimeStamp timeStamp = (TimeStamp)timePrimitive;
            startTime = parseDateString(timeStamp.getWhen());
            endTime = startTime;
        }
        else if (timePrimitive instanceof TimeSpan)
        {
            TimeSpan timeSpan = (TimeSpan)timePrimitive;
            startTime = parseDateString(timeSpan.getBegin());
            endTime = parseDateString(timeSpan.getEnd());
            if (startTime != null && endTime != null && startTime.after(endTime))
            {
                Date tmp = startTime;
                startTime = endTime;
                endTime = tmp;
            }
        }
        else
        {
            startTime = null;
            endTime = null;
        }
        return io.opensphere.core.model.time.TimeSpan.get(startTime, endTime);
    }

    /**
     * Converts a {@link Date} to a JAK TimePrimitive.
     *
     * @param time The date to convert.
     * @return The converted time.
     */
    public static TimePrimitive timeSpanToTimePrimitive(io.opensphere.core.model.time.TimeSpan time)
    {
        TimePrimitive primitive = null;
        if (time != null && !time.isTimeless())
        {
            if (time.isInstantaneous())
            {
                TimeStamp timeStamp = new TimeStamp();
                timeStamp.setWhen(DateTimeUtilities.generateISO8601DateString(time.getStartDate()));
                primitive = timeStamp;
            }
            else
            {
                TimeSpan span = new TimeSpan();
                span.setBegin(DateTimeUtilities.generateISO8601DateString(time.getStartDate()));
                span.setEnd(DateTimeUtilities.generateISO8601DateString(time.getEndDate()));
                primitive = span;
            }
        }
        return primitive;
    }

    /**
     * Gets the LatLonAlt for the given Location and AltitudeMode.
     *
     * @param loc the location
     * @param altitudeMode the altitude mode
     * @return the LatLonAlt
     */
    public static LatLonAlt getLatLonAlt(Location loc, AltitudeMode altitudeMode)
    {
        Altitude.ReferenceLevel referenceLevel = convertAltitudeMode(altitudeMode);
        boolean isZeroAltitude = isZeroAltitude(altitudeMode);
        double altitude = isZeroAltitude || Double.isNaN(loc.getAltitude()) ? 0.0 : loc.getAltitude();
        // Perform a simple normalization for performance reasons
        double normalizedLon = loc.getLongitude() > 180. ? loc.getLongitude() - 360. : loc.getLongitude();
        return LatLonAlt.createFromDegreesMeters(loc.getLatitude(), normalizedLon, altitude, referenceLevel);
    }

    /**
     * Gets the expires header time.
     *
     * @param expiresHeader The Expires HTTP header value
     * @return the expires header time
     */
    static synchronized Date getExpiresHeaderTime(String expiresHeader)
    {
        Date expires = null;
        if (!StringUtils.isBlank(expiresHeader))
        {
            expires = getExpiresHeaderTime(expiresHeader, RFC_1123_DATE_FORMAT1);
            if (expires == null)
            {
                expires = getExpiresHeaderTime(expiresHeader, RFC_1123_DATE_FORMAT2);
            }
        }
        return expires;
    }

    /**
     * Gets the max age header time.
     *
     * @param cacheControlHeader The Cache-Control HTTP header value
     * @param currentTimeSupplier The supplier for the current time in
     *            milliseconds since Java epoch.
     * @return the max age header time
     */
    static Date getMaxAgeHeaderTime(String cacheControlHeader, LongSupplier currentTimeSupplier)
    {
        Date maxAge = null;
        if (!StringUtils.isBlank(cacheControlHeader))
        {
            String[] pairs = cacheControlHeader.split(",");
            for (String pair : pairs)
            {
                String[] toks = pair.split("=");
                String key = toks[0].trim();
                String value = toks.length >= 2 ? toks[1].trim() : null;
                if ("max-age".equals(key))
                {
                    int maxAgeSeconds;
                    try
                    {
                        maxAgeSeconds = Integer.parseInt(value);
                    }
                    catch (NumberFormatException e)
                    {
                        maxAgeSeconds = 0;
                    }
                    if (maxAgeSeconds > 0)
                    {
                        maxAge = new Date(currentTimeSupplier.get() + (long)maxAgeSeconds * 1000);
                    }
                }
            }
        }
        return maxAge;
    }

    /**
     * Determines if the given expire time is valid.
     *
     * @param date The expire time
     * @return Whether it's valid
     */
    static boolean isExpireTimeValid(Date date)
    {
        boolean isValid = false;
        if (date != null)
        {
            Date now = new Date();
            isValid = date.after(now) && date.before(new Date(now.getTime() + MILLIS_PER_YEAR));
        }
        return isValid;
    }

    /**
     * Parse the String into a Date.
     *
     * @param dateString The date string
     * @return The parsed Date
     */
    static synchronized Date parseDateString(final String dateString)
    {
        Date date = null;

        if (!StringUtils.isBlank(dateString))
        {
            Matcher m = KML_TIME_PATTERN.matcher(dateString.trim());
            if (m.matches())
            {
                Calendar calendar = getCalendarFromMatcher(m);
                date = calendar.getTime();
            }
        }

        return date;
    }

    /**
     * Calculates a value.
     *
     * @param value The initial value
     * @param units The units
     * @param max The maximum value
     * @return The value
     */
    private static double calculate(double value, Units units, double max)
    {
        double result = value;
        // Adjust other units to be "pixels" units
        if (value == -1)
        {
            result = max;
        }
        else if (units == Units.FRACTION)
        {
            result = value * max;
        }
        else if (units == Units.INSET_PIXELS)
        {
            result = max - value;
        }
        return result;
    }

    /**
     * Converts a AltitudeMode to a Altitude.ReferenceLevel.
     *
     * @param altitudeMode The AltitudeMode
     * @return The equivalent Altitude.ReferenceLevel
     */
    private static Altitude.ReferenceLevel convertAltitudeMode(AltitudeMode altitudeMode)
    {
        return altitudeMode == AltitudeMode.ABSOLUTE ? Altitude.ReferenceLevel.ELLIPSOID : Altitude.ReferenceLevel.TERRAIN;
    }

    /**
     * Convert an JAK Coordinate to a Core LatLonAlt.
     *
     * @param coordinate The Coordinate
     * @param referenceLevel The altitude reference level
     * @param isZeroAltitude Whether to use zero altitude
     * @return The equivalent LatLonAlt
     */
    private static LatLonAlt convertCoordinate(Coordinate coordinate, Altitude.ReferenceLevel referenceLevel,
            boolean isZeroAltitude)
    {
        double altitude = isZeroAltitude || Double.isNaN(coordinate.getAltitude()) ? 0.0 : coordinate.getAltitude();
        // Perform a simple normalization for performance reasons
        double normalizedLon = coordinate.getLongitude() > 180. ? coordinate.getLongitude() - 360. : coordinate.getLongitude();
        return LatLonAlt.createFromDegreesMeters(coordinate.getLatitude(), normalizedLon, altitude, referenceLevel);
    }

    /**
     * Produces a Calendar from the given matcher.
     *
     * @param m The matcher
     * @return The calendar
     */
    private static Calendar getCalendarFromMatcher(Matcher m)
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(0);

        String group;
        if ((group = getGroup(m, 1)) != null)
        {
            calendar.set(Calendar.YEAR, Integer.parseInt(group));

            if ((group = getGroup(m, 2)) != null)
            {
                calendar.set(Calendar.MONTH, Integer.parseInt(group.substring(1)) - 1);

                if ((group = getGroup(m, 3)) != null)
                {
                    calendar.set(Calendar.DATE, Integer.parseInt(group.substring(1)));

                    if ((group = getGroup(m, 4)) != null)
                    {
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(group.substring(1)));

                        if ((group = getGroup(m, 5)) != null)
                        {
                            calendar.set(Calendar.MINUTE, Integer.parseInt(group.substring(1)));

                            if ((group = getGroup(m, 6)) != null)
                            {
                                calendar.set(Calendar.SECOND, Integer.parseInt(group.substring(1)));

                                if ((group = getGroup(m, 7)) != null)
                                {
                                    calendar.set(Calendar.MILLISECOND, Integer.parseInt(group.substring(1)));
                                }
                                if ((group = getGroup(m, 8)) != null
                                        && (StringUtilities.startsWith(group, '+') || StringUtilities.startsWith(group, '-')))
                                {
                                    calendar.setTimeZone(TimeZone.getTimeZone("GMT" + group));
                                }
                            }
                        }
                    }
                }
            }
        }

        return calendar;
    }

    /**
     * Gets the expires header time.
     *
     * @param expiresHeader The Expires HTTP header value
     * @param format the date format
     * @return the expires header time
     */
    private static Date getExpiresHeaderTime(String expiresHeader, SimpleDateFormat format)
    {
        Date expires;
        try
        {
            expires = format.parse(expiresHeader);
        }
        catch (ParseException e1)
        {
            expires = null;
        }
        return expires;
    }

    /**
     * Utility method to get a group.
     *
     * @param m The matcher
     * @param index The group index
     * @return The group string
     */
    private static String getGroup(Matcher m, int index)
    {
        String group = null;
        if (m.groupCount() >= index)
        {
            group = m.group(index);
        }
        return group;
    }

    /**
     * Determines if the given AltitudeMode uses an altitude value of zero.
     *
     * @param altitudeMode The AltitudeMode
     * @return Whether to use 0 altitude
     */
    private static boolean isZeroAltitude(AltitudeMode altitudeMode)
    {
        // A null altitudeMode equals clampToGround according to the KML spec
        return altitudeMode == null || altitudeMode == AltitudeMode.CLAMP_TO_GROUND
                || altitudeMode == AltitudeMode.CLAMP_TO_SEA_FLOOR;
    }

    /**
     * Private constructor.
     */
    private KMLSpatialTemporalUtils()
    {
    }
}
