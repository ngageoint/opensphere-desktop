package io.opensphere.core.model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;

/**
 * Model for geographic coordinates. This provides geodetic latitude, longitude,
 * and altitude, along with a reference level for the altitude. Objects of this
 * class are immutable.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class LatLonAlt implements Serializable, SizeProvider
{
    /** The degree symbol. */
    public static final char DEGREE_SYMBOL = '\u00b0';

    /** The minute symbol. */
    public static final char MINUTE_SYMBOL = '\'';

    /** The seconds symbol. */
    public static final char SECOND_SYMBOL = '"';

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The geodetic latitude in degrees. */
    private final double myLatitudeDegrees;

    /** The longitude in degrees. */
    private final double myLongitudeDegrees;

    /**
     * Create using a latitude and longitude, with 0 altitude above local
     * terrain.
     *
     * @param latD The geodetic latitude in degrees.
     * @param lonD The longitude in degrees.
     * @return The new object.
     */
    public static LatLonAlt createFromDegrees(double latD, double lonD)
    {
        return createFromDegreesMeters(latD, lonD, 0., Altitude.ReferenceLevel.TERRAIN);
    }

    /**
     * Create using a latitude and longitude, with 0 altitude above the
     * specified reference level.
     *
     * @param latD The geodetic latitude in degrees.
     * @param lonD The longitude in degrees.
     * @param altRef The reference level for the altitude.
     * @return The new object.
     */
    public static LatLonAlt createFromDegrees(double latD, double lonD, Altitude.ReferenceLevel altRef)
    {
        return createFromDegreesMeters(latD, lonD, 0., altRef);
    }

    /**
     * Create using latitude, longitude, and altitude.
     *
     * @param latD The geodetic latitude in degrees.
     * @param lonD The longitude in degrees.
     * @param altKm The altitude in kilometers.
     * @param altRef The reference level for the altitude.
     * @return The new object.
     */
    public static LatLonAlt createFromDegreesKm(double latD, double lonD, double altKm, Altitude.ReferenceLevel altRef)
    {
        return createFromDegreesMeters(latD, lonD, altKm * Constants.UNIT_PER_KILO, altRef);
    }

    /**
     * Create using latitude, longitude, and altitude.
     *
     * @param latD The geodetic latitude in degrees.
     * @param lonD The longitude in degrees.
     * @param alt The altitude.
     * @return The new object.
     */
    public static LatLonAlt createFromDegreesMeters(double latD, double lonD, Altitude alt)
    {
        return createFromDegreesMeters(latD, lonD, alt.getMeters(), alt.getReferenceLevel());
    }

    /**
     * Create using latitude, longitude, and altitude.
     *
     * @param latD The geodetic latitude in degrees.
     * @param lonD The longitude in degrees.
     * @param altM The altitude in meters.
     * @param altRef The reference level for the altitude.
     * @return The new object.
     */
    public static LatLonAlt createFromDegreesMeters(double latD, double lonD, double altM, Altitude.ReferenceLevel altRef)
    {
        if (altRef == Altitude.ReferenceLevel.TERRAIN)
        {
            if (altM == 0. || Double.isNaN(altM))
            {
                return new LatLonZeroTerrainAlt(latD, lonD);
            }
            else
            {
                return new LatLonNonZeroTerrainAlt(latD, lonD, altM);
            }
        }
        else if (altRef == Altitude.ReferenceLevel.ELLIPSOID)
        {
            if (altM == 0. || Double.isNaN(altM))
            {
                return new LatLonZeroEllipsoidAlt(latD, lonD);
            }
            else
            {
                return new LatLonNonZeroEllipsoidAlt(latD, lonD, altM);
            }
        }
        else
        {
            if (altM == 0. || Double.isNaN(altM))
            {
                return new LatLonZeroCustomAlt(latD, lonD, altRef);
            }
            else
            {
                return new LatLonNonZeroCustomAlt(latD, lonD, altM, altRef);
            }
        }
    }

    /**
     * Create from a 2d vector containing geodetic latitude and longitude in
     * degrees.
     *
     * @param vec The vector.
     * @return The new object.
     */
    public static LatLonAlt createFromVec2d(Vector2d vec)
    {
        return createFromDegrees(vec.getY(), vec.getX());
    }

    /**
     * Tell whether connecting these locations with a line the short way will
     * create a line that crosses the antimeridian.
     *
     * @param lla1 The first location
     * @param lla2 The second location
     * @return true when the line between the locations crosses the
     *         antimeridian.
     */
    public static boolean crossesAntimeridian(LatLonAlt lla1, LatLonAlt lla2)
    {
        return Math.abs(lla1.getLonD()) != 180. && Math.abs(lla2.getLonD()) != 180.
                && Math.abs(normalizeLongitude(lla1.getLonD()) - normalizeLongitude(lla2.getLonD())) > 180.;
    }

    /**
     * Check that the decimal degrees latitude value is valid.
     *
     * @param latString The latitude string value (decimal degrees).
     * @param allowedLastChar If the last character is non-numeric and is not a
     *            decimal point, it must belong to this set for the string to be
     *            valid.
     * @return True if the lat is valid, false otherwise.
     */
    public static boolean isValidDecimalLat(String latString, char... allowedLastChar)
    {
        return isValidDecimalDegrees(latString, 90., allowedLastChar);
    }

    /**
     * Check that the decimal degrees latitude value is valid.
     *
     * @param lonString The latitude string value (decimal degrees).
     * @param allowedLastChar If the last character is non-numeric and is not a
     *            decimal point, it must belong to this set for the string to be
     *            valid.
     * @return True if the lat is valid, false otherwise.
     */
    public static boolean isValidDecimalLon(String lonString, char... allowedLastChar)
    {
        return isValidDecimalDegrees(lonString, 180., allowedLastChar);
    }

    /**
     * Create a degree minutes seconds string representation of a latitude from
     * the decimal value.
     *
     * @param degrees The decimal value.
     * @param roundOff The round off number to use (number of digits past
     *            decimal).
     * @return A string representation of degree in degree minutes seconds
     *         format.
     */
    public static String latToDMSString(double degrees, int roundOff)
    {
        return latToDMSString(degrees, roundOff, DEGREE_SYMBOL, MINUTE_SYMBOL, SECOND_SYMBOL);
    }

    /**
     * Create a degree minutes seconds string representation of a latitude from
     * the decimal value.
     *
     * @param degrees The decimal value.
     * @param roundOff The round off number to use (number of digits past
     *            decimal).
     * @param degreeSymbol the degree symbol
     * @param minuteSymbol the minute symbol
     * @param secondSymbol the second symbol
     * @return A string representation of degree in degree minutes seconds
     *         format.
     */
    public static String latToDMSString(double degrees, int roundOff, char degreeSymbol, char minuteSymbol, char secondSymbol)
    {
        return decimalDegreeToDMSString(degrees, roundOff, degreeSymbol, minuteSymbol, secondSymbol, 'N', 'S');
    }

    /**
     * Find the difference between two longitudes the short way.
     *
     * @param lonD1 The first longitude in degrees.
     * @param lonD2 The second longitude in degrees.
     * @return The difference in degrees.
     */
    public static double longitudeDifference(double lonD1, double lonD2)
    {
        double delta = lonD1 - lonD2;
        if (delta < 0.)
        {
            delta *= -1.;
        }
        if (delta > 360.)
        {
            delta -= (int)(delta / 360.) * 360.;
        }
        if (delta > 180.)
        {
            delta = 360. - delta;
        }

        return delta;
    }

    /**
     * Create a degree minutes seconds string representation of a longitude from
     * the decimal value.
     *
     * @param degrees The decimal value.
     * @param roundOff The round off number to use (number of digits past
     *            decimal).
     * @return A string representation of degree in degree minutes seconds
     *         format.
     */
    public static String lonToDMSString(double degrees, int roundOff)
    {
        return lonToDMSString(degrees, roundOff, DEGREE_SYMBOL, MINUTE_SYMBOL, SECOND_SYMBOL);
    }

    /**
     * Create a degree minutes seconds string representation of a longitude from
     * the decimal value.
     *
     * @param degrees The decimal value.
     * @param roundOff The round off number to use (number of digits past
     *            decimal).
     * @param degreeSymbol the degree symbol
     * @param minuteSymbol the minute symbol
     * @param secondSymbol the second symbol
     * @return A string representation of degree in degree minutes seconds
     *         format.
     */
    public static String lonToDMSString(double degrees, int roundOff, char degreeSymbol, char minuteSymbol, char secondSymbol)
    {
        return decimalDegreeToDMSString(degrees, roundOff, degreeSymbol, minuteSymbol, secondSymbol, 'E', 'W');
    }

    /**
     * Normalize a latitude to be between -90 and 90.  This is terrible.
     *
     * @param in The input latitude.
     * @return The normalized latitude.
     */
    public static double normalizeLatitude(double in)
    {
        final double limit1 = 270.;
        final double limit2 = 450.;
        final double limit3 = 540.;
        double out = in;
        if (out > 90.)
        {
            if (out > limit3)
            {
                out %= 360.;
                if (out > limit1)
                {
                    out = out - 360;
                }
                else if (out > 90.)
                {
                    out = 180. - out;
                }
            }
            else if (out > limit1)
            {
                if (out > limit2)
                {
                    out = limit3 - out;
                }
                else
                {
                    out = out - 360;
                }
            }
            else if (out > 90.)
            {
                out = 180. - out;
            }
        }
        else if (out < -90)
        {
            if (out < -limit3)
            {
                out %= 360.;
                if (out < -limit1)
                {
                    out = out + 360;
                }
                else if (out < -90)
                {
                    out = -180 - out;
                }
            }
            else if (out < -limit1)
            {
                if (out < -limit2)
                {
                    out = -limit3 - out;
                }
                else
                {
                    out = out + 360;
                }
            }
            else if (out < -90)
            {
                out = -180 - out;
            }
        }
        return out;
    }

    /**
     * Normalize a longitude to be between -180 and 180 inclusive.
     *
     * @param in The input latitude.
     * @return The normalized latitude.
     */
    public static double normalizeLongitude(double in)
    {
        double out = in;
        if (out > 180.)
        {
            out -= (int)((out + 180.) / 360.) * 360.;
        }
        else if (out < -180.)
        {
            out -= (int)((out - 180.) / 360.) * 360.;
        }
        // Allow both 180 and -180.

        return out;
    }

    /**
     * Parse a location from a given string.
     *
     * For decimal values, the two values (lon/lat) can be separated by
     * whitespace, a comma, or any combination of the two.
     *
     * For DMS values, the six values (3 for each lon/lat) can be separated by
     * whitespace, comma, single quote and double quote.
     *
     * The latitude value must contain 'N' or 'S'.
     *
     * If these are not present then (lat, lon) coordinate order will be
     * assumed.
     *
     * @param str The string to parse.
     * @return The location parsed from the string, or null if a location could
     *         not be parsed.
     */
    public static LatLonAlt parse(String str)
    {
        return LatLonAltParser.parseLatLon(str);
    }

    /**
     * Check for alphabetic characters in the string.
     *
     * @param str The string.
     * @param end One plus the index of the last character in the string to
     *            check. (Pass {@code str.length()} to check the whole string.)
     * @return If there are alphabetic characters in the checked portion of the
     *         string, {@code true}.
     */
    private static boolean checkForAlphabeticCharacters(String str, int end)
    {
        Matcher alphaMatcher = Pattern.compile("\\p{Alpha}").matcher(str);
        return alphaMatcher.find() && alphaMatcher.start() < end;
    }

    /**
     * Convert the decimal latitude to degrees plus decimal minutes.
     * @param deg decimal degrees latitude
     * @param roundOff digits after the decimal point for minutes
     * @return the requested latitude String
     */
    public static String latToDdmString(double deg, int roundOff)
    {
        return latToDdmString(deg, roundOff, DEGREE_SYMBOL, MINUTE_SYMBOL);
    }

    /**
     * Convert the decimal latitude to degrees plus decimal minutes, using the
     * provided symbols for degrees and minutes.
     * @param deg decimal degrees latitude
     * @param roundOff digits after the decimal point for minutes
     * @param degSym symbol for degrees
     * @param minSym symbol for minutes
     * @return the requested latitude String
     */
    public static String latToDdmString(double deg, int roundOff, char degSym, char minSym)
    {
        return degToDdm(deg, roundOff, degSym, minSym, 'N', 'S');
    }

    /**
     * Convert the decimal longitude to degrees plus decimal minutes.
     * @param deg decimal degrees longitude
     * @param roundOff digits after the decimal point for minutes
     * @return the requested longitude String
     */
    public static String lonToDdmString(double deg, int roundOff)
    {
        return lonToDdmString(deg, roundOff, DEGREE_SYMBOL, MINUTE_SYMBOL);
    }

    /**
     * Convert the decimal longitude to degrees plus decimal minutes, using the
     * provided symbols for degrees and minutes.
     * @param deg decimal degrees longitude
     * @param roundOff digits after the decimal point for minutes
     * @param degSym symbol for degrees
     * @param minSym symbol for minutes
     * @return the requested longitude String
     */
    public static String lonToDdmString(double deg, int roundOff, char degSym, char minSym)
    {
        return degToDdm(deg, roundOff, degSym, minSym, 'E', 'W');
    }

    /**
     * Construct a DecimalFormat with the specified number of digits before and
     * after the decimal point.  Regardless of input, at least one digit is
     * always allowed.  If no digits after the decimal point are requested,
     * then the decimal point is omitted.
     * @param whole digits to the left of the decimal point
     * @param frac digits after the decimal point
     * @return the generated DecimalFormat
     */
    private static DecimalFormat createDecFormat(int whole, int frac)
    {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < Math.max(1, whole); i++)
            buf.append('#');
        if (frac > 0)
            buf.append('.');
        for (int i = 0; i < frac; i++)
            buf.append('#');
        return new DecimalFormat(buf.toString());
    }

    /**
     * Worker method for formatting degrees latitude or longitude as integer
     * degrees plus decimal minutes.
     * @param decDeg decimal degrees value
     * @param roundOff number of digits for decimal minutes
     * @param degSym symbol for degrees
     * @param minSym symbol for minutes
     * @param posSuffix 'N' for latitude or 'E' for longitude
     * @param negSuffix 'S' for latitude or 'W' for longitude
     * @return see above
     */
    private static String degToDdm(double decDeg, int roundOff, char degSym,
            char minSym, char posSuffix, char negSuffix)
    {
        StringBuilder buf = degToDdmBuf(decDeg, roundOff, degSym, minSym);
        if (0.0 <= decDeg)
            buf.append(posSuffix);
        else
            buf.append(negSuffix);
        return buf.toString();
    }

    /**
     * Calculate the formatted DDM value without sign indicator.
     * @param decDeg decimal degrees value
     * @param roundOff number of digits for decimal minutes
     * @return see above
     */
    public static String degToDdm(double decDeg, int roundOff)
    {
        return degToDdmBuf(decDeg, roundOff, DEGREE_SYMBOL, MINUTE_SYMBOL).toString();
    }

    /**
     * Calculate the formatted DDM value without sign indicator.
     * @param decDeg decimal degrees value
     * @param roundOff number of digits for decimal minutes
     * @param degSym symbol for degrees
     * @param minSym symbol for minutes
     * @return a StringBuilder containing the formatted DDM
     */
    private static StringBuilder degToDdmBuf(double decDeg, int roundOff, char degSym, char minSym)
    {
        double absDeg = Math.abs(decDeg);
        int deg = (int)absDeg;
        double decMin = (absDeg - deg) * 60.0;

        DecimalFormat decimalFormat = createDecFormat(3, roundOff);
        String minStr = decimalFormat.format(decMin);

        // If the seconds get rounded up to 60, increment the minutes.
        if (minStr.startsWith("60"))
        {
            minStr = "0";
            deg++;
        }

        StringBuilder buf = new StringBuilder();
        buf.append(deg);
        buf.append(degSym);
        buf.append(minStr);
        buf.append(minSym);
        return buf;
    }

    /**
     * Create a degree minutes seconds string representation of degree from the
     * decimal value.
     *
     * @param decDeg decimal degrees
     * @param roundOff The round off number to use (number of digits past
     *            decimal).
     * @param degreeSymbol the degree symbol
     * @param minuteSymbol the minute symbol
     * @param secondSymbol the second symbol
     * @param positiveSuffix The character to put at the end of the string if
     *            the value is positive.
     * @param negativeSuffix The character to put at the end of the string if
     *            the value is negative.
     * @return A string representation of degree in degree minutes seconds
     *         format.
     */
    private static String decimalDegreeToDMSString(double decDeg, int roundOff, char degreeSymbol, char minuteSymbol,
            char secondSymbol, char positiveSuffix, char negativeSuffix)
    {
        double absDeg = Math.abs(decDeg);
        int deg = (int)absDeg;
        double decMin = (absDeg - deg) * 60.0;
        int min = (int)decMin;
        double sec = (decMin - min) * 60.0;

        DecimalFormat decimalFormat = createDecFormat(3, roundOff);
        String secStr = decimalFormat.format(sec);

        // If the seconds get rounded up to 60, increment the minutes.
        if (secStr.startsWith("60"))
        {
            secStr = "0";
            min++;
            if (min >= Constants.MINUTES_PER_DEGREE)
            {
                min = 0;
                deg++;
            }
        }

        StringBuilder buf = new StringBuilder();
        buf.append(deg);
        buf.append(degreeSymbol);
        buf.append(min);
        buf.append(minuteSymbol);
        buf.append(secStr);
        buf.append(secondSymbol);
        if (0.0 <= decDeg)
            buf.append(positiveSuffix);
        else
            buf.append(negativeSuffix);
        return buf.toString();
    }

    /**
     * Check that the decimal degrees value is valid.
     *
     * @param str The string value (decimal degrees).
     * @param boundary The maximum allowed absolute value.
     * @param allowedLastChar If the last character is non-numeric and is not a
     *            decimal point, it must belong to this set for the string to be
     *            valid.
     * @return True if the string is valid, false otherwise.
     */
    private static boolean isValidDecimalDegrees(String str, double boundary, char[] allowedLastChar)
    {
        try
        {
            String trimmed = str.trim();

            if (trimmed.isEmpty())
            {
                return false;
            }
            if (checkForAlphabeticCharacters(trimmed, trimmed.length() - 1))
            {
                return false;
            }

            char lastChar = trimmed.charAt(trimmed.length() - 1);

            double degrees;
            if (Character.isDigit(lastChar) || lastChar == '.')
            {
                degrees = Double.parseDouble(trimmed);
            }
            else
            {
                boolean okay = false;
                for (char allowedChar : allowedLastChar)
                {
                    if (lastChar == allowedChar)
                    {
                        okay = true;
                        break;
                    }
                }

                if (okay)
                {
                    degrees = Double.parseDouble(trimmed.substring(0, trimmed.length() - 1));
                }
                else
                {
                    return false;
                }
            }

            return degrees <= boundary && degrees >= -boundary;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Private constructor to enforce use of factory methods.
     *
     * @param latitudeDegrees The geodetic latitude in degrees.
     * @param longitudeDegrees The longitude in degrees.
     */
    private LatLonAlt(double latitudeDegrees, double longitudeDegrees)
    {
        myLatitudeDegrees = latitudeDegrees;
        myLongitudeDegrees = longitudeDegrees;
    }

    /**
     * Add to this point and return a new object.
     *
     * @param latitudeDegrees The amount of latitude to add.
     * @param longitudeDegrees The amount of longitude to add.
     * @param altitudeMeters The amount of altitude to add.
     * @return The new coordinates.
     */
    public LatLonAlt addDegreesMeters(double latitudeDegrees, double longitudeDegrees, double altitudeMeters)
    {
        double latD = getLatD() + latitudeDegrees;
        double lonD = getLonD() + longitudeDegrees;
        double altM = getAltM() + altitudeMeters;
        return createFromDegreesMeters(latD, lonD, altM, getAltitudeReference());
    }

    /**
     * Construct a 2-D vector containing the longitude and latitude of this
     * object in double-precision degrees.
     *
     * @return The new object.
     */
    public Vector2d asVec2d()
    {
        return new Vector2d(myLongitudeDegrees, myLatitudeDegrees);
    }

    /**
     * Construct a 3-D vector containing the longitude, latitude and altitude of
     * this object in double-precision degrees.
     *
     * @return The new object.
     */
    public Vector3d asVec3d()
    {
        return new Vector3d(myLongitudeDegrees, myLatitudeDegrees, getAltM());
    }

    /**
     * Return a location with the same latitude, longitude and altitude, but
     * with the provided reference level. If this location has the provided
     * reference level, this will be returned otherwise a new position will be
     * created.
     *
     * @param reference The desired reference level.
     * @return The geographic position with the provided reference level.
     */
    public LatLonAlt convertReference(ReferenceLevel reference)
    {
        if (getAltitudeReference() == reference)
        {
            return this;
        }
        else if (this instanceof LatLonNonZeroAlt)
        {
            return LatLonAlt.createFromDegreesMeters(myLatitudeDegrees, myLongitudeDegrees, getAltM(), reference);
        }
        else
        {
            return LatLonAlt.createFromDegrees(myLatitudeDegrees, myLongitudeDegrees, reference);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj instanceof LatLonAlt && getAltitudeReference() == ((LatLonAlt)obj).getAltitudeReference()
                && MathUtil.isZero(getAltM() - ((LatLonAlt)obj).getAltM())
                && MathUtil.isZero(myLatitudeDegrees - ((LatLonAlt)obj).myLatitudeDegrees)
                && MathUtil.isZero(myLongitudeDegrees - ((LatLonAlt)obj).myLongitudeDegrees);
    }

    /**
     * Get the altitude as an {@link Altitude}.
     *
     * @return The altitude.
     */
    public Altitude getAltitude()
    {
        return Altitude.createFromMeters(getAltM(), getAltitudeReference());
    }

    /**
     * Get the reference level for the altitude.
     *
     * @return The reference level for the altitude.
     */
    public abstract Altitude.ReferenceLevel getAltitudeReference();

    /**
     * Get the altitude in meters, relative to the level given by
     * {@link #getAltitudeReference()}.
     *
     * @return The altitude in meters.
     */
    public double getAltM()
    {
        return 0.;
    }

    /**
     * Get the geodetic latitude in degrees.
     *
     * @return The latitude.
     */
    public double getLatD()
    {
        return myLatitudeDegrees;
    }

    /**
     * Get the longitude in degrees.
     *
     * @return The longitude.
     */
    public double getLonD()
    {
        return myLongitudeDegrees;
    }

    /**
     * Return a normalized version of myself, which means the returned
     * coordinates will have a latitude between -90 and 90 inclusive, and a
     * longitude between -180 and 180 inclusive.
     *
     * @return A normalized version of myself.
     */
    public LatLonAlt getNormalized()
    {
        double latDegrees = myLatitudeDegrees;
        if (Math.abs(myLatitudeDegrees) > 90.)
        {
            latDegrees = normalizeLatitude(myLatitudeDegrees);
        }
        double lonDegrees = myLongitudeDegrees;
        if (Math.abs(myLongitudeDegrees) > 180.)
        {
            lonDegrees = normalizeLongitude(myLongitudeDegrees);
        }

        return LatLonAlt.createFromDegreesMeters(latDegrees, lonDegrees, getAltM(), getAltitudeReference());
    }

    /**
     * Get the latitude, normalized to be between -90 and 90 degrees inclusive.
     *
     * @return The normalized latitude.
     */
    public double getNormalizedLatD()
    {
        return normalizeLatitude(myLatitudeDegrees);
    }

    /**
     * Get the longitude, normalized to be between -180 and 180 degrees
     * inclusive.
     *
     * @return The normalized longitude.
     */
    public double getNormalizedLonD()
    {
        return normalizeLongitude(myLongitudeDegrees);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(getAltM());
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + getAltitudeReference().hashCode();
        temp = Double.doubleToLongBits(getLatD());
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(getLonD());
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    /**
     * Interpolate between this point and a given point, following the shortest
     * path.
     *
     * @param lla The other point.
     * @param fraction The fraction of the distance to the other point.
     * @return The interpolated point.
     */
    public LatLonAlt interpolate(LatLonAlt lla, double fraction)
    {
        return interpolate(lla, fraction, false);
    }

    /**
     * Interpolate between this point and a given point.
     *
     * @param lla The other point.
     * @param fraction The fraction of the distance to the other point.
     * @param longway Flag indicating if the interpolation should be done the
     *            long way around the globe.
     * @return The interpolated point.
     */
    public LatLonAlt interpolate(LatLonAlt lla, double fraction, boolean longway)
    {
        Altitude.ReferenceLevel altRef = getAltitudeReference();
        if (altRef != lla.getAltitudeReference())
        {
            throw new IllegalArgumentException(
                    "Incompatible altitudes references: " + altRef + " and " + lla.getAltitudeReference());
        }

        double fraction2 = 1. - fraction;
        double latD = lla.getLatD() * fraction + getLatD() * fraction2;
        double lonD;
        double lon1 = getLonD();
        double lon2 = lla.getLonD();
        if (longway ^ Math.abs(lon2 - lon1) > 180.)
        {
            if (lon2 < lon1)
            {
                lonD = normalizeLongitude((lon2 + 360.) * fraction + lon1 * fraction2);
            }
            else
            {
                lonD = normalizeLongitude(lon2 * fraction + (lon1 + 360.) * fraction2);
            }
        }
        else
        {
            lonD = lon2 * fraction + lon1 * fraction2;
        }
        double altM = lla.getAltM() * fraction + getAltM() * fraction2;
        return createFromDegreesMeters(latD, lonD, altM, altRef);
    }

    /**
     * Checks if this {@link LatLonAlt} is within a tolerance (inclusive) of
     * another LatLonAlt in terms of pure numeric offset between lat and lon
     * independently.
     *
     * Where within tolerance is defined as:
     *
     * Math.abs(this.lat - other.lat) &lt;= tolerance &amp;&amp;
     * Math.abs(this.lon - other.lon) &lt;= tolerance
     *
     *
     * @param other the other LatLonAlt to test against
     * @param tolerance the tolerance the numeric tolerance
     * @return true, if the difference between the latitudes
     */
    public boolean isWithinLatLonTolerance(LatLonAlt other, double tolerance)
    {
        return equals(other) || Math.abs(myLatitudeDegrees - other.myLatitudeDegrees) <= tolerance
                && Math.abs(myLongitudeDegrees - other.myLongitudeDegrees) <= tolerance;
    }

    /**
     * This method determines if myself and another point are on opposite sides
     * of the longitude boundary. This assumes that me and the point passed in
     * are within the range -180 to 180 (a value of -185 should be 175).
     *
     * @param p2 The second point to compare.
     * @return true if the points cross the boundary, false otherwise
     */
    public boolean positionsCrossLongitudeBoundary(LatLonAlt p2)
    {
        if (p2 == null)
        {
            throw new IllegalArgumentException("Null Value for position");
        }

        // A segment cross the line if end pos have different longitude signs
        // and are more than 180 degress longitude apart
        if (Math.signum(getLonD()) != Math.signum(p2.getLonD()))
        {
            double delta = Math.abs(getLonD() - p2.getLonD());
            if (delta > 180 && delta < 360)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Create a simple string representation of my data.
     *
     * @return A simple string.
     */
    public String toSimpleString()
    {
        return getLatD() + "/" + getLonD() + "/" + getAltM();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(30);
        sb.append(LatLonAlt.class.getSimpleName()).append(" [").append(getLatD()).append('/').append(getLonD()).append('/')
                .append(getAltM()).append(']');
        return sb.toString();
    }

    /**
     * The CoordFormat enum identifies a generic data format type.
     */
    public enum CoordFormat
    {
        /** The latitude/longitude is in decimal degrees format. */
        DECIMAL("Decimal"),

        /** Degrees plus decimal minutes. */
        DDM("DDM"),

        /** The latitude/longitude is in degree/minute/second format. */
        DMS("DMS"),

        /** The location is in the MGRS format. */
        MGRS("MGRS"),

        /** The location format is a WKT geometry. */
        WKT_GEOMETRY("WKT"),

        /** The UNKNOWN. */
        UNKNOWN("Unknown");

        /** The display text. */
        private final String myDisplayText;

        /** Index by display text. */
        private static Map<String, CoordFormat> strMap = new TreeMap<>();
        static
        {
            for (CoordFormat f :  values())
                strMap.put(f.toString(), f);
        }

        /**
         * Get the matching CoordFormat.
         * @param str display text thereof
         * @return it
         */
        public static CoordFormat fromString(String str)
        {
            return strMap.get(str);
        }

        /**
         * Instantiates a new coordinate format.
         *
         * @param displayText the display text
         */
        CoordFormat(String displayText)
        {
            myDisplayText = displayText;
        }

        @Override
        public String toString()
        {
            return myDisplayText;
        }
    }

    /** A LatLonAlt with non-zero altitude. */
    private abstract static class LatLonNonZeroAlt extends LatLonAlt
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * The altitude in meters.
         */
        private final double myAltitudeMeters;

        /**
         * Constructor.
         *
         * @param latitudeDegrees The geodetic latitude in degrees.
         * @param longitudeDegrees The longitude in degrees.
         * @param altitudeMeters The altitude in meters.
         */
        public LatLonNonZeroAlt(double latitudeDegrees, double longitudeDegrees, double altitudeMeters)
        {
            super(latitudeDegrees, longitudeDegrees);
            myAltitudeMeters = altitudeMeters;
        }

        @Override
        public Altitude getAltitude()
        {
            return Altitude.createFromMeters(myAltitudeMeters, getAltitudeReference());
        }

        @Override
        public double getAltM()
        {
            return myAltitudeMeters;
        }
    }

    /** A LatLonAlt with ellipsoid-referenced altitude. */
    private static class LatLonNonZeroCustomAlt extends LatLonNonZeroAlt
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The size of an instance. */
        private static final int SIZE_BYTES = MathUtil.roundUpTo(
                Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 3 + Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES);

        /** The altitude reference. */
        private final Altitude.ReferenceLevel myAltitudeReference;

        /**
         * Constructor.
         *
         * @param latitudeDegrees The geodetic latitude in degrees.
         * @param longitudeDegrees The longitude in degrees.
         * @param altitudeMeters The altitude in meters.
         * @param altitudeReference The altitude reference.
         */
        public LatLonNonZeroCustomAlt(double latitudeDegrees, double longitudeDegrees, double altitudeMeters,
                Altitude.ReferenceLevel altitudeReference)
        {
            super(latitudeDegrees, longitudeDegrees, altitudeMeters);
            myAltitudeReference = altitudeReference;
        }

        @Override
        public Altitude.ReferenceLevel getAltitudeReference()
        {
            return myAltitudeReference;
        }

        @Override
        public long getSizeBytes()
        {
            return SIZE_BYTES;
        }
    }

    /** A LatLonAlt with ellipsoid-referenced altitude. */
    private static class LatLonNonZeroEllipsoidAlt extends LatLonNonZeroAlt
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The size of a LatLonAlt instance. */
        private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 3,
                Constants.MEMORY_BLOCK_SIZE_BYTES);

        /**
         * Constructor.
         *
         * @param latitudeDegrees The geodetic latitude in degrees.
         * @param longitudeDegrees The longitude in degrees.
         * @param altitudeMeters The altitude in meters.
         */
        public LatLonNonZeroEllipsoidAlt(double latitudeDegrees, double longitudeDegrees, double altitudeMeters)
        {
            super(latitudeDegrees, longitudeDegrees, altitudeMeters);
        }

        @Override
        public Altitude.ReferenceLevel getAltitudeReference()
        {
            return Altitude.ReferenceLevel.ELLIPSOID;
        }

        @Override
        public long getSizeBytes()
        {
            return SIZE_BYTES;
        }
    }

    /** A LatLonAlt with terrain-referenced altitude. */
    private static class LatLonNonZeroTerrainAlt extends LatLonNonZeroAlt
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The size of a LatLonAlt instance. */
        private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 3,
                Constants.MEMORY_BLOCK_SIZE_BYTES);

        /**
         * Constructor.
         *
         * @param latitudeDegrees The geodetic latitude in degrees.
         * @param longitudeDegrees The longitude in degrees.
         * @param altitudeMeters The altitude in meters.
         */
        public LatLonNonZeroTerrainAlt(double latitudeDegrees, double longitudeDegrees, double altitudeMeters)
        {
            super(latitudeDegrees, longitudeDegrees, altitudeMeters);
        }

        @Override
        public Altitude.ReferenceLevel getAltitudeReference()
        {
            return Altitude.ReferenceLevel.TERRAIN;
        }

        @Override
        public long getSizeBytes()
        {
            return SIZE_BYTES;
        }
    }

    /** A LatLonAlt with ellipsoid-referenced altitude. */
    private static class LatLonZeroCustomAlt extends LatLonAlt
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The size of a LatLonAlt instance. */
        private static final int SIZE_BYTES = MathUtil.roundUpTo(
                Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 2 + Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES);

        /** The altitude reference. */
        private final Altitude.ReferenceLevel myAltitudeReference;

        /**
         * Constructor.
         *
         * @param latitudeDegrees The geodetic latitude in degrees.
         * @param longitudeDegrees The longitude in degrees.
         * @param altitudeReference The altitude reference.
         */
        public LatLonZeroCustomAlt(double latitudeDegrees, double longitudeDegrees, Altitude.ReferenceLevel altitudeReference)
        {
            super(latitudeDegrees, longitudeDegrees);
            myAltitudeReference = altitudeReference;
        }

        @Override
        public Altitude.ReferenceLevel getAltitudeReference()
        {
            return myAltitudeReference;
        }

        @Override
        public long getSizeBytes()
        {
            return SIZE_BYTES;
        }
    }

    /** A LatLonAlt with ellipsoid-referenced altitude. */
    private static class LatLonZeroEllipsoidAlt extends LatLonAlt
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The size of a LatLonAlt instance. */
        private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 2,
                Constants.MEMORY_BLOCK_SIZE_BYTES);

        /**
         * Constructor.
         *
         * @param latitudeDegrees The geodetic latitude in degrees.
         * @param longitudeDegrees The longitude in degrees.
         */
        public LatLonZeroEllipsoidAlt(double latitudeDegrees, double longitudeDegrees)
        {
            super(latitudeDegrees, longitudeDegrees);
        }

        @Override
        public Altitude getAltitude()
        {
            return Altitude.ZERO_ELLIPSOID;
        }

        @Override
        public Altitude.ReferenceLevel getAltitudeReference()
        {
            return Altitude.ReferenceLevel.ELLIPSOID;
        }

        @Override
        public long getSizeBytes()
        {
            return SIZE_BYTES;
        }
    }

    /** A LatLonAlt with terrain-referenced altitude. */
    private static class LatLonZeroTerrainAlt extends LatLonAlt
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The size of a LatLonAlt instance. */
        private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 2,
                Constants.MEMORY_BLOCK_SIZE_BYTES);

        /**
         * Constructor.
         *
         * @param latitudeDegrees The geodetic latitude in degrees.
         * @param longitudeDegrees The longitude in degrees.
         */
        public LatLonZeroTerrainAlt(double latitudeDegrees, double longitudeDegrees)
        {
            super(latitudeDegrees, longitudeDegrees);
        }

        @Override
        public Altitude getAltitude()
        {
            return Altitude.ZERO_TERRAIN;
        }

        @Override
        public Altitude.ReferenceLevel getAltitudeReference()
        {
            return Altitude.ReferenceLevel.TERRAIN;
        }

        @Override
        public long getSizeBytes()
        {
            return SIZE_BYTES;
        }
    }
}
