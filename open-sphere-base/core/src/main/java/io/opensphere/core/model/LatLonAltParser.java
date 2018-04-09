package io.opensphere.core.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Class to hold utilities for parsing various forms of latitude and longitude.
 */
@SuppressWarnings("PMD.GodClass")
public class LatLonAltParser
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LatLonAltParser.class);

    /** Regex for the start of the string. */
    private static final String START = "\\s*";

    /** Regex for the middle of a pair of coordinates. */
    private static final String MIDDLE = "[,\\s/]*";

    /** Regex for the end of the string. */
    private static final String END = "\\s*";

    /** Separator characters including minutes/seconds. */
    private static final String SEP = "\\s'\",:";

    /** Decimal point. */
    private static final String PERIOD = ".";

    /** Degree symbol. */
    private static final String DEG = "\u00b0";

    /** Optional separator group, not including the degree symbol. */
    private static final String G_SEP = "[" + SEP + PERIOD + "]*";

    /** Forced separator group, not including the degree symbol. */
    private static final String F_SEP = "[" + SEP + PERIOD + "]+";

    /** Optional separator group, including the degree symbol. */
    private static final String G_DEG = "[" + SEP + PERIOD + DEG + "]*";

    /** Forced separator group, including the degree symbol. */
    private static final String F_DEG = "[" + SEP + PERIOD + DEG + "]+";

    /** Latitude direction indicator group. */
    private static final String NS = "([NS])?";

    /** Longitude direction indicator group. */
    private static final String EW = "([EW])?";

    /** Forced two-digit minutes or seconds group. */
    private static final String F_60 = "([0-5][0-9])";

    /** One- or two-digit minutes or seconds group. */
    private static final String G_60 = "([0-5]?[0-9])";

    /** Optional decimal fraction group. */
    private static final String G_FRAC = "(?:[.]([0-9]*))?";

    /** One- or two-digit latitude with optional zero padding. */
    private static final String G_LAT = "([-+]?0*?0?[0-9]?[0-9])";

    /** Forced two-digit latitude. */
    private static final String F_LAT = "([-+]?[0-9][0-9])";

    /** One- to three-digit longitude with optional zero padding. */
    private static final String G_LON = "([-+]?(?:0?(?:[0-2]?[0-9]?[0-9]|3[0-5][0-9]|360)))";

    /** Forced three-digit longitude. */
    private static final String F_LON = "([-+]?(?:[0-2][0-9][0-9]|3[0-5][0-9]|360))";

    /** Minutes and seconds fields with forced digits and optional spacing. */
    private static final String G_MM_SS = "(?:" + F_60 + "(?:" + G_SEP + F_60 + G_FRAC + ")?)?";

    /** Minutes and seconds fields with optional digits and forced spacing. */
    private static final String F_MM_SS = "(?:" + G_60 + "(?:" + F_SEP + "(?:" + G_60 + G_FRAC + ")?)?)?";

    /** Decimal minutes field with extra digits optional. */
    private static final String G_DMIN = "([0-5]?[0-9](?:[.][0-9]+)?)";

    /** Decimal minutes field with all digits forced. */
    private static final String F_DMIN = "([0-5][0-9](?:[.]?[0-9]+)?)?";

    /** The pattern for latitude decimal degrees. */
    private static final Coord LAT_DECIMAL = new Coord(CoordType.LAT, CoordFormat.DECIMAL,
            NS + "([-+]?\\d{0,2}(?:[.]\\d*)?)[\\s" + DEG + "]*" + NS, 2, -1, -1, -1, 3, 1, 3);

    /** The pattern for longitude decimal degrees. */
    private static final Coord LON_DECIMAL = new Coord(CoordType.LON, CoordFormat.DECIMAL,
            EW + "([-+]?(?:[0-3]?\\d{0,2}(?:[.]\\d*)?))[\\s" + DEG + "]*" + EW, 2, -1, -1, -1, 3, 1, 3);

    /** The pattern for latitude DDMMSS. */
    private static final Coord LAT_DDMMSS = new Coord(CoordType.LAT, LatLonAlt.CoordFormat.DMS,
            NS + G_LAT + G_DEG + G_MM_SS + G_SEP + NS, 2, 3, 4, 5, 6, 1, 6);

    /** The pattern for longitude DDDMMSS. */
    private static final Coord LON_DDDMMSS = new Coord(CoordType.LON, CoordFormat.DMS, EW + G_LON + G_DEG + G_MM_SS + G_SEP + EW,
            2, 3, 4, 5, 6, 1, 6);

    /** The pattern for latitude DDMMSS with symbols. */
    private static final Coord LAT_DDMMSS_SYM = new Coord(CoordType.LAT, CoordFormat.DMS,
            NS + G_LAT + F_DEG + F_MM_SS + G_SEP + NS, 2, 3, 4, 5, 6, 1, 6);

    /** The pattern for longitude DDDMMSS with symbols. */
    private static final Coord LON_DDDMMSS_SYM = new Coord(CoordType.LON, CoordFormat.DMS,
            EW + G_LON + F_DEG + F_MM_SS + G_SEP + EW, 2, 3, 4, 5, 6, 1, 6);

    /** Latitude pattern DDMMSSSSS with no separators or decimal point. */
    private static final Coord LAT_DDMMSSSSS = new Coord(CoordType.LAT, CoordFormat.DMS,
            NS + F_LAT + F_60 + "(?:" + F_60 + "([0-9]*)?)?" + NS, 2, 3, 4, 5, 6, 1, 6);

    /** Longitude pattern DDDMMSSSSS with no separators or decimal point. */
    private static final Coord LON_DDDMMSSSSS = new Coord(CoordType.LON, CoordFormat.DMS,
            EW + F_LON + F_60 + "(?:" + F_60 + "([0-9]*)?)?" + EW, 2, 3, 4, 5, 6, 1, 6);

    /** Latitude as degrees + decimal minutes, no unnecessary punctuation. */
    private static final Coord LAT_DEG_DMIN = new Coord(CoordType.LAT, CoordFormat.DDM,
            NS + F_LAT + "[" + SEP + DEG + "]*" + F_DMIN + G_SEP + NS, 2, 3, -1, -1, 4, 1, 4);

    /** Longitude as degrees + decimal minutes, no unnecessary punctuation. */
    private static final Coord LON_DEG_DMIN = new Coord(CoordType.LON, CoordFormat.DDM,
            EW + F_LON + "[" + SEP + DEG + "]*" + F_DMIN + G_SEP + EW, 2, 3, -1, -1, 4, 1, 4);

    /** Latitude as degrees + decimal minutes, field separator required. */
    private static final Coord LAT_DEG_DMIN_SYM = new Coord(CoordType.LAT, CoordFormat.DDM,
            NS + G_LAT + "[" + SEP + DEG + "]+" + G_DMIN + G_SEP + NS, 2, 3, -1, -1, 4, 1, 4);

    /** Longitude as degrees + decimal minutes, field separator required. */
    private static final Coord LON_DEG_DMIN_SYM = new Coord(CoordType.LON, CoordFormat.DDM,
            EW + G_LON + "[" + SEP + DEG + "]+" + G_DMIN + G_SEP + EW, 2, 3, -1, -1, 4, 1, 4);

    /** Map of latitude parsers by CoordFormat key. */
    private static Map<CoordFormat, Coord[]> latByFormat = new TreeMap<>();

    /** Map of latitude parsers by CoordFormat key. */
    private static Map<CoordFormat, Coord[]> lonByFormat = new TreeMap<>();
    static
    {
        latByFormat.put(CoordFormat.DECIMAL, new Coord[] { LAT_DECIMAL });
        latByFormat.put(CoordFormat.DMS, new Coord[] { LAT_DDMMSS, LAT_DDMMSS_SYM, LAT_DDMMSSSSS });
        latByFormat.put(CoordFormat.DDM, new Coord[] { LAT_DEG_DMIN, LAT_DEG_DMIN_SYM });

        lonByFormat.put(CoordFormat.DECIMAL, new Coord[] { LON_DECIMAL });
        lonByFormat.put(CoordFormat.DMS, new Coord[] { LON_DDDMMSS, LON_DDDMMSS_SYM, LON_DDDMMSSSSS });
        lonByFormat.put(CoordFormat.DDM, new Coord[] { LON_DEG_DMIN, LON_DEG_DMIN_SYM });
    }

    /** The possible latitude coordinates, in priority order. */
    private static final Coord[] POSSIBLE_LAT_COORDS = new Coord[] { LAT_DECIMAL, LAT_DEG_DMIN_SYM, LAT_DDMMSS_SYM, LAT_DDMMSS,
        LAT_DDMMSSSSS, LAT_DEG_DMIN };

    /** The possible longitude coordinates, in priority order. */
    private static final Coord[] POSSIBLE_LON_COORDS = new Coord[] { LON_DECIMAL, LON_DEG_DMIN_SYM, LON_DDDMMSS_SYM, LON_DDDMMSS,
        LON_DDDMMSSSSS, LON_DEG_DMIN };

    /** List of possible lat/lon patterns. */
    private static Collection<Coords> latLonOptions = New.collection();
    static
    {
        latLonOptions.add(new Coords(LAT_DECIMAL, LON_DECIMAL));
        latLonOptions.add(new Coords(LON_DECIMAL, LAT_DECIMAL));

        latLonOptions.add(new Coords(LAT_DEG_DMIN_SYM, LON_DEG_DMIN_SYM));
        latLonOptions.add(new Coords(LON_DEG_DMIN_SYM, LAT_DEG_DMIN_SYM));

        latLonOptions.add(new Coords(LAT_DDMMSS_SYM, LON_DDDMMSS_SYM));
        latLonOptions.add(new Coords(LON_DDDMMSS_SYM, LAT_DDMMSS_SYM));

        latLonOptions.add(new Coords(LAT_DDMMSS, LON_DDDMMSS));
        latLonOptions.add(new Coords(LON_DDDMMSS, LAT_DDMMSS));

        latLonOptions.add(new Coords(LAT_DDMMSSSSS, LON_DDDMMSSSSS));
        latLonOptions.add(new Coords(LON_DDDMMSSSSS, LAT_DDMMSSSSS));

        latLonOptions.add(new Coords(LAT_DDMMSS_SYM, LON_DDDMMSS));
        latLonOptions.add(new Coords(LON_DDDMMSS_SYM, LAT_DDMMSS));
        latLonOptions.add(new Coords(LAT_DDMMSS, LON_DDDMMSS_SYM));
        latLonOptions.add(new Coords(LON_DDDMMSS, LAT_DDMMSS_SYM));
        latLonOptions.add(new Coords(LAT_DDMMSS_SYM, LON_DECIMAL));
        latLonOptions.add(new Coords(LON_DDDMMSS_SYM, LAT_DECIMAL));
        latLonOptions.add(new Coords(LAT_DDMMSS, LON_DECIMAL));
        latLonOptions.add(new Coords(LON_DDDMMSS, LAT_DECIMAL));
        latLonOptions.add(new Coords(LAT_DECIMAL, LON_DDDMMSS_SYM));
        latLonOptions.add(new Coords(LON_DECIMAL, LAT_DDMMSS_SYM));
        latLonOptions.add(new Coords(LAT_DECIMAL, LON_DDDMMSS));
        latLonOptions.add(new Coords(LON_DECIMAL, LAT_DDMMSS));

        latLonOptions.add(new Coords(LAT_DEG_DMIN, LON_DEG_DMIN));
        latLonOptions.add(new Coords(LON_DEG_DMIN, LAT_DEG_DMIN));
    }

    /**
     * Builds a string that contains the string representation of the latitude
     * and longitude coords.
     *
     * @param input the lat lon string
     * @return the matched coords for the input string
     */
    public static Pair<CoordFormat, CoordFormat> getLatLonFormat(String input)
    {
        if (input == null)
        {
            return null;
        }
        String str = input.trim().toUpperCase();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Lat/Lon string to parse = " + input);
        }

        for (Coords c : latLonOptions)
        {
            Matcher matcher = c.getPattern().matcher(str);
            if (matcher.matches())
            {
                return Pair.create(c.getLat().getCoordFormat(), c.getLon().getCoordFormat());
            }
        }
        return null;
    }

    /**
     * Parse a latitude from a given string.
     *
     * @param latString The string to parse.
     * @return The latitude parsed from the string, or {@link Double#NaN} if it
     *         could not be parsed.
     */
    public static double parseLat(String latString)
    {
        return parseAngle(latString, POSSIBLE_LAT_COORDS);
    }

    /**
     * Test whether there is any latitude format that can handle the input.
     * 
     * @param latString the input
     * @return see above
     */
    public static boolean validLat(String latString)
    {
        return !Double.isNaN(parseLat(latString, CoordFormat.DECIMAL)) || !Double.isNaN(parseLat(latString, CoordFormat.DMS))
                || !Double.isNaN(parseLat(latString, CoordFormat.DDM));
    }

    /**
     * Test whether there is any longitude format that can handle the input.
     * 
     * @param lonString the input
     * @return see above
     */
    public static boolean validLon(String lonString)
    {
        return !Double.isNaN(parseLon(lonString, CoordFormat.DECIMAL)) || !Double.isNaN(parseLon(lonString, CoordFormat.DMS))
                || !Double.isNaN(parseLon(lonString, CoordFormat.DDM));
    }

    /**
     * Parse a latitude from a given string.
     *
     * @param latString The string to parse.
     * @param format The required format.
     * @return The latitude parsed from the string, or {@link Double#NaN} if it
     *         could not be parsed.
     */
    public static double parseLat(String latString, CoordFormat format)
    {
        double lat = Double.NaN;
        // Try more efficient parsing for decimal values
        if (format == CoordFormat.DECIMAL)
        {
            lat = parseDecimalAngle(latString, CoordType.LAT);
        }
        if (Double.isNaN(lat))
        {
            lat = parseAngle(latString, latByFormat.get(format));
        }
        return lat;
    }

    /**
     * Parse a longitude from a given string.
     *
     * @param lonString The string to parse.
     * @return The latitude parsed from the string, or {@link Double#NaN} if it
     *         could not be parsed.
     */
    public static double parseLon(String lonString)
    {
        return parseAngle(lonString, POSSIBLE_LON_COORDS);
    }

    /**
     * Parse a longitude from a given string.
     *
     * @param lonString The string to parse.
     * @param format The required format.
     * @return The latitude parsed from the string, or {@link Double#NaN} if it
     *         could not be parsed.
     */
    public static double parseLon(String lonString, CoordFormat format)
    {
        double lon = Double.NaN;
        // Try more efficient parsing for decimal values
        if (format == CoordFormat.DECIMAL)
        {
            lon = parseDecimalAngle(lonString, CoordType.LON);
        }
        if (Double.isNaN(lon))
        {
            lon = parseAngle(lonString, lonByFormat.get(format));
        }
        return lon;
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
     * @param input The string to parse.
     * @return The location parsed from the string, or null if a location could
     *         not be parsed.
     */
    public static LatLonAlt parseLatLon(String input)
    {
        if (input == null)
        {
            return null;
        }
        String str = input.trim().toUpperCase();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Lat/Lon string to parse = " + input);
        }

        for (Coords c : latLonOptions)
        {
            Matcher matcher = c.getPattern().matcher(str);
            if (matcher.matches())
            {
                // lat is first, lon second
                double lat = parse(c.getLat(), matcher);
                double lon = parse(c.getLon(), matcher);
                if (Double.isNaN(lat) || Double.isNaN(lon))
                {
                    return null;
                }

                // If our latitude is greater than 90, we know
                // that the latitude and longitude need to be switched
                if (Math.abs(lat) > 90)
                {
                    return LatLonAlt.createFromDegrees(lon, lat);
                }
                return LatLonAlt.createFromDegrees(lat, lon);
            }
        }
        return null;
    }

    /**
     * Method to parse and calculate decimal degree from other formats such as
     * degree, minutes, seconds.
     *
     * @param coord The coords.
     * @param matcher The matcher.
     *
     * @return The decimal degree equivalent from the inputs.
     */
    private static double parse(Coord coord, Matcher matcher)
    {
        String deg = coord.getDeg(matcher);
        String min = coord.getMin(matcher);
        String sec = coord.getSec(matcher);
        String fracSec = coord.getFracSec(matcher);

        try
        {
            return parse(deg, min, sec, fracSec, coord.getDir(matcher));
        }
        catch (NumberFormatException nfe)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Number format exception trying to parse deg = " + coord.getDeg(matcher) + " min = "
                        + coord.getMin(matcher) + " sec = " + coord.getSec(matcher) + " fracSec = " + coord.getFracSec(matcher));
            }
            return Double.NaN;
        }
    }

    /**
     * Convert separated DMS strings into a floating point value.
     *
     * @param deg The degrees.
     * @param min The minutes.
     * @param sec The seconds.
     * @param fracSec The fractional seconds.
     * @param dir The direction.
     * @return The value.
     * @throws NumberFormatException If there is an error parsing one of the
     *             strings.
     */
    private static double parse(String deg, String min, String sec, String fracSec, String dir) throws NumberFormatException
    {
        double d = deg == null ? Double.NaN : Double.parseDouble(deg);
        double val = Math.abs(d);

        if (min != null)
        {
            val += parseMinutes(min) / Constants.MINUTES_PER_DEGREE;
        }
        if (sec != null)
        {
            val += Double.parseDouble(sec) / Constants.SECONDS_PER_DEGREE;
        }
        if (fracSec != null)
        {
            val += Double.parseDouble("." + fracSec) / Constants.SECONDS_PER_DEGREE;
        }

        if (Double.compare(d, 0.) < 0)
        {
            val = -val;
        }

        // possibly, this is the second time inverting the value
        if ("S".equals(dir) || "W".equals(dir))
        {
            val = -val;
        }
        return val;
    }

    /**
     * Parse the minutes field, correctly handling the case where a decimal
     * point is omitted but implied.
     * 
     * @param min the minutes field String
     * @return the number of minutes represented
     */
    private static double parseMinutes(String min)
    {
        if (min.length() <= 2 || min.indexOf(".") != -1)
        {
            return Double.parseDouble(min);
        }
        return Double.parseDouble(min.substring(0, 2) + "." + min.substring(2));
    }

    /**
     * Parse a latitude or a longitude from a given string.
     *
     * @param input The string to parse.
     * @param coords The patterns to try.
     * @return The latitude parsed from the string, or {@link Double#NaN} if it
     *         could not be parsed.
     */
    private static double parseAngle(String input, Coord[] coords)
    {
        if (input == null || coords == null)
        {
            return Double.NaN;
        }

        String str = input.trim().toUpperCase();
        for (Coord c : coords)
        {
            Matcher matcher = c.getPattern().matcher(str);
            if (matcher.matches())
            {
                double result = parse(c, matcher);
                if (c.getCoordType() == CoordType.LAT && Math.abs(result) > 90.)
                {
                    result = Double.NaN;
                }
                return result;
            }
        }
        return Double.NaN;
    }

    /**
     * Parse a latitude or a longitude from a given string, expecting that the
     * string is a decimal number.
     *
     * @param input The string to parse.
     * @param coordType The coordinate type.
     * @return The latitude parsed from the string, or {@link Double#NaN} if it
     *         could not be parsed.
     */
    private static double parseDecimalAngle(String input, CoordType coordType)
    {
        double result = Double.NaN;
        if (input != null && !StringUtilities.startsWith(input, '0'))
        {
            try
            {
                result = Double.parseDouble(input.trim());
                if (coordType == CoordType.LAT && Math.abs(result) > 90.)
                {
                    result = Double.NaN;
                }
            }
            catch (NumberFormatException e)
            {
                LOGGER.debug(e);
            }
        }
        return result;
    }

    /**
     * Private constructor.
     */
    private LatLonAltParser()
    {
    }

    /**
     * Class to hold degrees, minutes, seconds, and direction indices for
     * parsing group results.
     */
    private static class Coord
    {
        /** The Coord format. */
        private final CoordFormat myCoordFormat;

        /** The type of coordinate. */
        private final CoordType myCoordType;

        /**
         * The index for the regex group that matches the degrees, or -1 if
         * there isn't one.
         */
        private final int myDegreesIndex;

        /**
         * The index for the first regex group that matches the direction, or -1
         * if there isn't one.
         */
        private final int myDirectionIndex1;

        /**
         * The index for the second regex group that matches the direction, or
         * -1 if there isn't one.
         */
        private final int myDirectionIndex2;

        /**
         * The index for the regex group that matches the fractional seconds, or
         * -1 if there isn't one.
         */
        private final int myFractionalSecondsIndex;

        /** The index for the last group. */
        private final int myLastIndex;

        /**
         * The index for the regex group that matches the minutes, or -1 if
         * there isn't one.
         */
        private final int myMinutesIndex;

        /** The compiled pattern. */
        private final Pattern myPattern;

        /** The regular expression. */
        private final String myRegex;

        /**
         * The index for the regex group that matches the seconds, or -1 if
         * there isn't one.
         */
        private final int mySecondsIndex;

        /**
         * Constructor.
         *
         * @param type The type of coordinate.
         * @param format The coordinate format.
         * @param regex The regular expression.
         * @param degrees The degrees index, or -1.
         * @param minutes The minutes index, or -1.
         * @param seconds The seconds index, or -1.
         * @param secondsFraction The fractional seconds index, or -1.
         * @param direction1 The first direction index, or -1.
         * @param direction2 The second direction index, or -1.
         * @param last The last group.
         */
        public Coord(CoordType type, CoordFormat format, String regex, int degrees, int minutes, int seconds, int secondsFraction,
                int direction1, int direction2, int last)
        {
            myCoordType = type;
            myCoordFormat = format;
            myRegex = Utilities.checkNull(regex, "regex");
            myDegreesIndex = degrees;
            myMinutesIndex = minutes;
            mySecondsIndex = seconds;
            myFractionalSecondsIndex = secondsFraction;
            myDirectionIndex1 = direction1;
            myDirectionIndex2 = direction2;
            myLastIndex = last;
            myPattern = Pattern.compile(myRegex);
        }

        /**
         * Get the coordinate format.
         *
         * @return The format.
         */
        public CoordFormat getCoordFormat()
        {
            return myCoordFormat;
        }

        /**
         * Get the coordinate type.
         *
         * @return The type.
         */
        public CoordType getCoordType()
        {
            return myCoordType;
        }

        /**
         * Get the degrees group from the matcher.
         *
         * @param matcher The matcher.
         * @return The degrees group, or {@code null} if none.
         */
        public String getDeg(Matcher matcher)
        {
            return getGroup(matcher, myDegreesIndex);
        }

        /**
         * Get the direction group from the matcher.
         *
         * @param matcher The matcher.
         * @return The direction group, or {@code null} if none.
         */
        public String getDir(Matcher matcher)
        {
            String dir = getGroup(matcher, myDirectionIndex1);
            if (dir != null && !dir.isEmpty())
            {
                return dir;
            }
            return getGroup(matcher, myDirectionIndex2);
        }

        /**
         * Get the fractional seconds group from the matcher.
         *
         * @param matcher The matcher.
         * @return The fractional seconds group, or {@code null} if none.
         */
        public String getFracSec(Matcher matcher)
        {
            return getGroup(matcher, myFractionalSecondsIndex);
        }

        /**
         * Get the index for the last group.
         *
         * @return The index.
         */
        public int getLastIndex()
        {
            return myLastIndex;
        }

        /**
         * Get the minutes group from the matcher.
         *
         * @param matcher The matcher.
         * @return The minutes group, or {@code null} if none.
         */
        public String getMin(Matcher matcher)
        {
            return getGroup(matcher, myMinutesIndex);
        }

        /**
         * Get the {@link Pattern} for this coordinate.
         *
         * @return The pattern.
         */
        public Pattern getPattern()
        {
            return myPattern;
        }

        /**
         * Get the regular expression for this coordinate.
         *
         * @return The regular expression.
         */
        public String getRegex()
        {
            return myRegex;
        }

        /**
         * Get the seconds group from the matcher.
         *
         * @param matcher The matcher.
         * @return The seconds group, or {@code null} if none.
         */
        public String getSec(Matcher matcher)
        {
            return getGroup(matcher, mySecondsIndex);
        }

        /**
         * Generate a new object the same as this one but with the non-negative
         * indices incremented by the given amount.
         *
         * @param addend The amount to add to the indices.
         * @return The new object.
         */
        public Coord incrementIndices(int addend)
        {
            return new Coord(myCoordType, myCoordFormat, myRegex, inc(myDegreesIndex, addend), inc(myMinutesIndex, addend),
                    inc(mySecondsIndex, addend), inc(myFractionalSecondsIndex, addend), inc(myDirectionIndex1, addend),
                    inc(myDirectionIndex2, addend), inc(myLastIndex, addend));
        }

        /**
         * Get a group from the given matcher, or null of the index is null.
         *
         * @param matcher The matcher.
         * @param index The group index.
         * @return The group
         */
        private String getGroup(Matcher matcher, int index)
        {
            return index == -1 ? null : matcher.group(index);
        }

        /**
         * Increment an index if it is non-negative.
         *
         * @param index The index.
         * @param addend The amount to add.
         * @return The result.
         */
        private int inc(int index, int addend)
        {
            return index == -1 ? -1 : index + addend;
        }
    }

    /**
     * A pair of two coordinates.
     */
    private static class Coords
    {
        /** The first coordinate. */
        private final Coord myFirst;

        /** The compiled pattern. */
        private final Pattern myLatLonPattern;

        /** The second coordinate. */
        private final Coord mySecond;

        /**
         * Constructor.
         *
         * @param first the description for the first coordinate
         * @param second the description for the second coordinate
         */
        public Coords(Coord first, Coord second)
        {
            myFirst = Utilities.checkNull(first, "first");

            mySecond = Utilities.checkNull(second, "second").incrementIndices(myFirst.getLastIndex());

            if (myFirst.getCoordType() == mySecond.getCoordType())
            {
                throw new IllegalArgumentException("Coordinate types must be different.");
            }

            myLatLonPattern = Pattern.compile(START + myFirst.getRegex() + MIDDLE + mySecond.getRegex() + END);
        }

        /**
         * Get the latitude coordinate.
         *
         * @return The latitude.
         */
        public Coord getLat()
        {
            return myFirst.getCoordType() == CoordType.LAT ? myFirst : mySecond;
        }

        /**
         * Get the longitude coordinate.
         *
         * @return The longitude.
         */
        public Coord getLon()
        {
            return myFirst.getCoordType() == CoordType.LON ? myFirst : mySecond;
        }

        /**
         * Get the {@link Pattern} that comprises both coordinates.
         *
         * @return The pattern.
         */
        public Pattern getPattern()
        {
            return myLatLonPattern;
        }
    }

    /** Enumeration of coordinate types. */
    private enum CoordType
    {
        /** Latitude type. */
        LAT,

        /** Longitude type. */
        LON,
    }
}
