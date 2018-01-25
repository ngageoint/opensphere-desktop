package io.opensphere.core.common.geospatial;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * <code>SimpleLatLonFormat</code> is a concrete class for formatting and
 * parsing longitudes and latitudes either together or separately. It allows for
 * formatting (date -> text), parsing (text -> date)
 *
 * <p>
 * <code>SimpleLatLonFormat</code> allows you to start by choosing any
 * user-defined patterns for latitude/longitude formatting.
 *
 * <h2>Latitude and Longitude Patterns</h2>
 * <p>
 * Latitude and Longitude formats are specified by <em>Latitude and Longitude
 * pattern</em> strings. Within lat and lon pattern strings, unbracketed letters
 * from <code>'A'</code> to <code>'Z'</code> and from <code>'a'</code> to
 * <code>'z'</code> are interpreted as pattern letters representing the
 * components of a latitude or longitude string.
 * <p>
 * Text can be bracketed using bracket pairs (<code>[]</code>) to avoid
 * interpretation. <code>"[some string]"</code> represents a single bracketed
 * segment.
 * <p>
 * <p>
 * Regular expressions can be included using parentheses pairs (<code>()</code>)
 * to avoid interpretation. <code>"(some regular expression)"</code> represents
 * a single expression segment. Note: that no grouping or other internal
 * parentheses pars may be embedded in one of these locations. So ([a-z]{1,2})
 * is okay, but ((ab)[a-z]{1,2}) is not allowed. The values captured by these
 * groups can be retrieved through the API during a value parse and provided
 * during a value format.
 * <p>
 * <li><strong>Command Directives:</strong> Specific command directives can be
 * included in the format line to indicate that the data is in a particular
 * representation even if no indicator in the data is provided. All directives
 * are inside a set of "{}" braces only one indicator is allowed per {} set of
 * braces. Directives are not included in output formatting or expected to be
 * present in input text strings. They are stripped out at the beginning of
 * pattern parsing when the initial pattern is set. Directives may appear
 * anywhere in the pattern as they are found and removed before any other text
 * is interpreted.</li> <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Chart shows command
 * directives">
 * <tr bgcolor="#ccccff">
 * <th align=left>Directive
 * <th align=left>Description
 * <tr>
 * <td><code>{f}</code>
 * <td>Indicates that decimal specifiers are to be interpreted strictly. So that
 * DDD.DDD means exactly 3 digits after the decimal point, rather than any
 * number which is the default.
 * <tr bgcolor="#eeeeff">
 * <td><code>{w}</code>
 * <td>Indicates that all longitudes are in degrees west
 * <tr>
 * <td><code>{s}</code>
 * <td>Indicates all latitudes are in degrees south
 * <tr bgcolor="#eeeeff">
 * <td><code>{d180}</code>
 * <td>Indicates that all latitudes are in 0 to 180 range
 * <tr>
 * <td><code>{D360}</code>
 * <td>Indicates that all longitudes are in 0 to 360 range
 * <tr bgcolor="#eeeeff">
 * <td><code>{D1}</code>
 * <td>Indicates that the first longitude indicator in the set is optional so
 * that "DDD" means 2 to 3 digits.
 * <p>
 * Note: This should only be used when DDD is separated from the rest of the
 * longitude encoding by a spacer character such as ":","space" etc. EX:
 * {D1}DDD:MM:SS.SSS would allow 123:45:54.34 to work as well as 123:45:54.34 or
 * {D1}DDDMMSSS.SSS can also be used provided that the DDD is immediately
 * followed by MM, MM.MM, MMSS, or MMSS.SS
 * <tr>
 * <td><code>{D2}</code>
 * <td>Indicates that the first two longitude indicator in the set are optional
 * so that "DDD" means 1 to 3 digits.
 * <p>
 * Note: This should only be used when DDD is separated from the rest of the
 * longitude encoding by a spacer character such as ":","space" etc. EX:
 * {D2}DDD:MM:SS.SSS would allow 123:45:54.34 to work as well as 23:45:54.34 and
 * 3:45:54.34. or {D2}DDDMMSSS.SSS can also be used provided that the DDD is
 * immediately followed by MM, MM.MM, MMSS, or MMSS.SS
 * <tr bgcolor="#eeeeff">
 * <td><code>{d1}</code>
 * <td>Indicates that the first latitude indicator in the set is optional so
 * that "dd" means 1 to 2 digits.
 * <p>
 * Note: This should only be used when dd is separated from the rest of the
 * longitude encoding by a spacer character such as ":","space" etc. EX:
 * {d1}dd:mm:ss.ss would allow 23:45:54.34 to work as well as 3:45:54.34 or
 * {d1}ddmmsss.sss can also be used provided that the dd is immediately followed
 * by mm, mm.mm, mmss, or mmss.ss
 *
 * </table>
 * </blockquote>
 * <p>
 * All other characters are not interpreted; they're simply copied into the
 * output string during formatting or matched against the input string during
 * parsing.
 * <p>
 * The following pattern letters are defined (all other characters from
 * <code>'A'</code> to <code>'Z'</code> and from <code>'a'</code> to
 * <code>'z'</code> are reserved): <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Chart shows pattern
 * letters, lat/lon component, presentation, and examples.">
 * <tr bgcolor="#ccccff">
 * <th align=left>Letter
 * <th align=left>Lat or Lon Component *
 * <th align=left>Examples
 * <tr>
 * <td><code>D</code>
 * <td>Longitude Degrees ( Whole or Decimal )
 * <td><code>123; 123.456</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>M</code>
 * <td>Longitude Minutes ( Whole or Decimal )
 * <td><code>34; 34.232</code>
 * <tr>
 * <td><code>S</code>
 * <td>Longitude Seconds ( Whole or Decimal )
 * <td><code>45, 43.2343</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>F</code>
 * <td>Longitude Fractional Seconds ( Whole only)
 * <td><code>12</code>
 * <tr>
 * <td><code>H</code>
 * <td>Longitude Hemisphere Indicator
 * <td><code>E,e,W,w</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>d</code>
 * <td>Latitude Degrees ( Whole or Decimal )
 * <td><code>80,-89, 32.234</code>
 * <tr>
 * <td><code>m</code>
 * <td>Latitude Minutes ( Whole or Decimal )
 * <td><code>45.23, 45</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>s</code>
 * <td>Latitude Seconds ( Whole or Decimal )
 * <td><code>10.234, 23</code>
 * <tr>
 * <td><code>f</code>
 * <td>Latitude Fractional Seconds ( Whole only)
 * <td><code>12</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>h</code>
 * <td>Latitude Hemisphere Indicator
 * <td><code>N,n,W,w</code>
 * </table>
 * </blockquote> Pattern letters are usually repeated, as their number
 * determines the exact presentation:
 * <ul>
 * <li><strong>Decimal Notations:</a></strong> Each type D,d,M,m,S,s all allow
 * for decimals to be specified by using the following type of notation.
 * "DDD.DD" where the presence of a decimal point and number of indicators after
 * the point indicates that floating point precision is desired up to the number
 * of indicators in precision. <br>
 *
 * <blockquote>
 * <li>Note however that unless the <code>{f}</code> directive is used the
 * number of decimal places indicated by the number of indicators after the
 * decimal point is interpreted loosely so that any number of decimals is
 * allowed. So that DD.D will match 12.2, 12.21, 12.223 etc. If the
 * <code>{f}</code> code is used it will be interpreted literally to mean
 * exactly the number of digits specified, no more no less</li> </blockquote>
 *
 * <blockquote>
 * <li>Note also that only one indicator type in a latitude or longitude
 * indicator set may have decimal point precision specified. And no lower
 * precision type may be specified after a decimal precision is indicated, so
 * that DDD may have decimals but if it does MM, SS, and FF may no longer be
 * used, and if MM has decimals, SS and FF may not be used, and if SS has
 * decimals, FF may not be used</li> </blockquote>
 *
 * <blockquote>
 * <li>Note that when using integer fractional seconds (FF or ff) no other
 * decimal precision may be used.</li> </blockquote>
 *
 * <blockquote>
 * <li>Example: DDDMMSSS.SSS is allowed but DDD.DDMMSSS.SSS is not as it makes
 * no since to allow fractional portions for multiple types for either latitude
 * or longitude sets.</li> </blockquote>
 *
 * <blockquote>
 * <li>Example: DDDMM.MMM is allowed but not DDDMM.MMSS</li> </blockquote>
 *
 * <blockquote>
 * <li>Example: DDDMMSS.SS is allowed but not DDDMMSS.SSFF as it would be
 * redundant</li> </blockquote></li>
 * <li><strong>Longitude Hemispheres:</strong> The "H" indicates the longitude
 * hemisphere. It must be of the values "E", "e", "W", "w" to indicate the
 * direction of the longitude value if not present in the pattern it will assume
 * degrees east.</li>
 * <p>
 * <li><strong>Longitude Values:</strong> Longitude values are preferred in
 * degrees east in the -180 to 180 range however if a hemisphere value is
 * provided the values will be adjusted to the degrees east orientation. If the
 * value exceeds 180 or is less than -180 it will be adjusted ( unwrapped ) into
 * the appropriate range.</li>
 * <p>
 * <li><strong>Latitude Hemispheres:</strong> The "h" indicates the latitude
 * hemisphere. It must be of the values "S", "s", "N", "n" to indicate the
 * direction of the latitude value if not present in the pattern it will assume
 * degrees east.</li>
 * <p>
 * <li><strong>Latitude Values:</strong> Latitude values are preferred in
 * degrees north in the -90 to 90 range however if a hemisphere value is
 * provided the values will be adjusted to the degrees north orientation.</li>
 * <p>
 * <li><strong>Escaped Characters</strong> Escaped characters ( characters with
 * no information value but present in the format ) may be included so long as
 * they are escaped with bracket pairs. Example: [dt]DDMMSS.SS , the [dt]
 * specifies that values may look like "dt923311.11" where the "dt" carries no
 * information but is always present. Other values such as <code>,</code>
 * <code>"</code> <code>'</code> <code>:</code> <code>/</code> and
 * <code>space</code> may be used without escape brackets</li>
 * <p>
 * <li><strong>Individual Latitude and Longitude Patterns</strong> A format need
 * not specify patterns for both latitude and longitude, it may provide one or
 * the other, or both in the same pattern. Parts should not be mixed between
 * types or unexpected results may ensue.</li>
 * <h4>Examples</h4>
 *
 * The following examples show how latitude and longitude patterns are
 * interpreted <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Examples of date and
 * time patterns interpreted in the U.S. locale">
 * <tr bgcolor="#ccccff">
 * <th align=left>Latitude and Longitude Pattern
 * <th align=left>Result
 * <tr>
 * <td><code>"DDD:MM"SS.SSS'H [by] dd:mm"ss.sss'h"</code>
 * <td><code>123:32"12.212'E by 33:12"43.233'N</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>"DDDMMSS"</code>
 * <td><code>0121032</code>
 * <tr>
 * <td><code>"DDD.DDD dd.ddd"</code>
 * <td><code>123.231 32.231</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>"p=[Lat: ]DDDMMSSS.SSSH [Lon: ]ddmmss.sssh"</code>
 * <td><code>Lat: 0822112.212E Lon: 451221.231N</code>
 * <tr>
 * <td><code>"{w}DDD:MM"SS.SSS'H [by] dd:mm"ss.sss'h"</code>
 * <td><code>082:21"12.212'W by 45:12"21.231'N</code>
 * <tr bgcolor="#eeeeff">
 * <td><code>"HDDDMMSSFF"</code>
 * <td><code>W128563512</code>
 * </table>
 * </blockquote> <br>
 *
 * <h3><a name="synchronization">Synchronization</a></h3>
 *
 * <p>
 * LatLon formats are not synchronized. It is recommended to create separate
 * format instances for each thread. If multiple threads access a format
 * concurrently, it must be synchronized externally.
 *
 */
public class SimpleLatLonFormat
{

    protected enum LatitudeHemisphere
    {
        NORTH, SOUTH
    }

    protected enum LongitudeHemisphere
    {
        EAST, WEST
    }

    protected enum ComponentType
    {
        LONGITUDE_DEGREES, LONGITUDE_MINUTES, LONGITUDE_SECONDS, LONGITUDE_FRACTION_SECONDS, LONGITUDE_HEMISPHERE, LATITUDE_DEGREES, LATITUDE_MINUTES, LATITUDE_SECONDS, LATITUDE_FRACTION_SECONDS, LATITUDE_HEMISPHERE, LITERAL, REGEX
    }

    protected static final String LONGITUDE_DEGREE_PATTERN_STR = "[-]?([D]+\\.[D]+|[D]+)";

    protected static final String LONGITUDE_MINUTE_PATTERN_STR = "[-]?([M]+\\.[M]+|[M]+)";

    protected static final String LONGITUDE_SECOND_PATTERN_STR = "[-]?([S]+\\.[S]+|[S]+)";

    protected static final String LONGITUDE_FRACTION_SECOND_PATTERN_STR = "[-]?([F]+\\.[F]+|[F]+)";

    protected static final String LONGITUDE_HEMISPHERE_PATTERN_STR = "[H]";

    protected static final String LATITUDE_DEGREE_PATTERN_STR = "[-]?([d]+\\.[d]+|[d]+)";

    protected static final String LATITUDE_MINUTE_PATTERN_STR = "[-]?([m]+\\.[m]+|[m]+)";

    protected static final String LATITUDE_SECOND_PATTERN_STR = "[-]?([s]+\\.[s]+|[s]+)";

    protected static final String LATITUDE_FRACTION_SECOND_PATTERN_STR = "[-]?([f]+\\.[f]+|[f]+)";

    protected static final String LATITUDE_HEMISPHERE_PATTERN_STR = "[h]";

    protected static final String VARIABLE_EXTRA_CHARS_PATTERN_STR = "\\((.*?)\\)";

    protected static final String WHITESPACE_LITERALESCAPES_SPECIALCHARS_PATTERN_STR = "\\((.*?)\\)|\\[(.*?)\\]|\\s+|,|[\\\"\\:\\']";

    protected static final String DIRECTIVES_PATTERN_STR = "\\{(.*?)\\}";

    public static final String DIRECTIVE_STRICT_FRACTION_INDICATORS = "{f}";

    public static final String DIRECTIVE_LATITUDES_IN_DEG_SOUTH = "{s}";

    public static final String DIRECTIVE_LONGITUDES_IN_DEG_WEST = "{w}";

    public static final String DIRECTIVE_LATITUDES_IN_0_TO_180 = "{d180}";

    public static final String DIRECTIVE_LONGITUDES_IN_0_TO_360 = "{D360}";

    public static final String DIRECTIVE_FIRST_LONGITUDE_CHAR_IS_OPTIONAL = "{D1}";

    public static final String DIRECTIVE_FIRSTTWO_LONGITUDE_CHARS_ARE_OPTIONAL = "{D2}";

    public static final String DIRECTIVE_FIRST_LATITUDE_CHAR_IS_OPTIONAL = "{d1}";

    protected static final Pattern LONGITUDE_DEGREE_PATTERN = Pattern.compile(LONGITUDE_DEGREE_PATTERN_STR);

    protected static final Pattern LONGITUDE_MINUTE_PATTERN = Pattern.compile(LONGITUDE_MINUTE_PATTERN_STR);

    protected static final Pattern LONGITUDE_SECOND_PATTERN = Pattern.compile(LONGITUDE_SECOND_PATTERN_STR);

    protected static final Pattern LONGITUDE_FRACTION_SECOND_PATTERN = Pattern.compile(LONGITUDE_FRACTION_SECOND_PATTERN_STR);

    protected static final Pattern LONGITUDE_HEMISPHERE_PATTERN = Pattern.compile(LONGITUDE_HEMISPHERE_PATTERN_STR);

    protected static final Pattern LATITUDE_DEGREE_PATTERN = Pattern.compile(LATITUDE_DEGREE_PATTERN_STR);

    protected static final Pattern LATITUDE_MINUTE_PATTERN = Pattern.compile(LATITUDE_MINUTE_PATTERN_STR);

    protected static final Pattern LATITUDE_SECOND_PATTERN = Pattern.compile(LATITUDE_SECOND_PATTERN_STR);

    protected static final Pattern LATITUDE_FRACTION_SECOND_PATTERN = Pattern.compile(LATITUDE_FRACTION_SECOND_PATTERN_STR);

    protected static final Pattern LATITUDE_HEMISPHERE_PATTERN = Pattern.compile(LATITUDE_HEMISPHERE_PATTERN_STR);

    protected static final Pattern WHITESPACE_LITERALESCAPES_SPECIALCHARS_PATTERN = Pattern
            .compile(WHITESPACE_LITERALESCAPES_SPECIALCHARS_PATTERN_STR);

    protected static final Pattern DIRECTIVES_PATTERN = Pattern.compile(DIRECTIVES_PATTERN_STR);

    protected static final Pattern VARIABLE_EXTRA_CHARS_PATTERN = Pattern.compile(VARIABLE_EXTRA_CHARS_PATTERN_STR);

    protected static final String DEFAULT_PATTERN = "{D2}{d1}DDD:MM'SS.SSS\"H ddd:mm'ss.sss\"h";

    protected ArrayList<PatternComponent> myPatternComponentList;

    protected boolean myHasLatHemisphereIndicatorInPattern = false;

    protected boolean myHasLonHemisphereIndicatorInPattern = false;

    protected ArrayList<ComponentType> myComponentOrder;

    protected Pattern myPattern;

    protected boolean latitudesInDegSouth = false;

    protected boolean latitudesIn0to180 = false;

    protected boolean longitudesInDegWest = false;

    protected boolean longitudesIn0to360 = false;

    protected boolean firstLongitudeCharIsOptional = false;

    protected boolean first2LongitudeCharsAreOptional = false;

    protected boolean firstLatitudeCharIsOptional = false;

    protected boolean strictFractionIndicators = false;

    protected String myInputPatternString;

    public SimpleLatLonFormat()
    {
        applyPattern(DEFAULT_PATTERN);
    }

    public SimpleLatLonFormat(String pattern) throws IllegalArgumentException, NullPointerException
    {
        if (pattern == null)
        {
            throw new NullPointerException();
        }
        applyPattern(pattern);
    }

    /**
     * Gets the directive that latitudes are all in degrees south
     *
     * @return true if set, false if not
     */
    public boolean getLatitudesInDegSouth()
    {
        return latitudesInDegSouth;
    }

    /**
     * Sets the directive that latitudes are all in degrees south
     *
     * @param latitudesInDegSouth
     */
    public void setLatitudesInDegSouth(boolean latitudesInDegSouth)
    {
        this.latitudesInDegSouth = latitudesInDegSouth;
    }

    /**
     * Gets the directive that latitudes are in zero to 180 range
     *
     * @return true if set, false if not
     */
    public boolean getLatitudesIn0to180()
    {
        return latitudesIn0to180;
    }

    /**
     * Sets the directive that latitudes are in zero to 180 range
     *
     * @param latitudesIn0to180
     */
    public void setLatitudesIn0to180(boolean latitudesIn0to180)
    {
        this.latitudesIn0to180 = latitudesIn0to180;
    }

    /**
     * Gets the directive that longitudes are in degrees west.
     *
     * @return true if set, false if not
     */
    public boolean getLongitudesInDegWest()
    {
        return longitudesInDegWest;
    }

    /**
     * Sets the directive that longitudes are in degrees west
     *
     * @param longitudesInDegWest
     */
    public void setLongitudesInDegWest(boolean longitudesInDegWest)
    {
        this.longitudesInDegWest = longitudesInDegWest;
    }

    /**
     * Gets the directive that longitudes are in the 0 to 360 range
     *
     * @return true if set, false if not
     */
    public boolean getLongitudesIn0to360()
    {
        return longitudesIn0to360;
    }

    /**
     * Sets the directive that longitudes are in the 0 to 360 range
     *
     * @param longitudesIn0to360
     */
    public void setLongitudesIn0to360(boolean longitudesIn0to360)
    {
        this.longitudesIn0to360 = longitudesIn0to360;
    }

    /**
     * Gets the directive that indicates that the first longitude specifier is
     * optional
     *
     * @return true if set, false if not
     */
    public boolean getFirstLongitudeCharIsOptional()
    {
        return firstLongitudeCharIsOptional;
    }

    /**
     * Sets the directive that indicates that the first longitude specifier is
     * optional: Note must be set BEFORE setting the pattern it has no effect
     * afterward.
     *
     * @param firstLongitudeCharIsOptional
     */
    public void setFirstLongitudeCharIsOptional(boolean firstLongitudeCharIsOptional)
    {
        this.firstLongitudeCharIsOptional = firstLongitudeCharIsOptional;
    }

    /**
     * Gets the directive that indicates that the first longitude specifier is
     * optional
     *
     * @return true if set, false if not
     */
    public boolean isFirst2LongitudeCharsAreOptional()
    {
        return first2LongitudeCharsAreOptional;
    }

    /**
     * Sets the directive that indicates that the first two longitude specifiers
     * are optional: Note must be set BEFORE setting the pattern it has no
     * effect afterward. ( Overrides first char optional )
     *
     * @param first2LongitudeCharsAreOptional
     */
    public void setFirst2LongitudeCharsAreOptional(boolean first2LongitudeCharsAreOptional)
    {
        this.first2LongitudeCharsAreOptional = first2LongitudeCharsAreOptional;
    }

    /**
     * Gets the directive that indicates that the first latitude specifier is
     * optional
     *
     * @return true if set, false if not
     */
    public boolean getFirstLatitudeCharIsOptional()
    {
        return firstLatitudeCharIsOptional;
    }

    /**
     * Sets the directive that indicates that the first latitude specifier is
     * optional: Note must be set BEFORE setting the pattern it has no effect
     * afterward.
     *
     * @param firstLatitudeCharIsOptional
     */
    public void setFirstLatitudeCharIsOptional(boolean firstLatitudeCharIsOptional)
    {
        this.firstLatitudeCharIsOptional = firstLatitudeCharIsOptional;
    }

    /**
     * Gets the directive that indicates that the number of decimap places
     * specified in the pattern are to be interpreted strictly ( exact match )
     * rather than flexible.
     *
     * @return true if set, false if not
     */
    public boolean getStrictFractionIndicators()
    {
        return strictFractionIndicators;
    }

    /**
     * Sets the directive that indicates that the number of decimap places
     * specified in the pattern are to be interpreted strictly ( exact match )
     * rather than flexible.
     *
     * @param strictFractionIndicators, true to set on, false to turn off
     */
    public void setStrictFractionIndicators(boolean strictFractionIndicators)
    {
        this.strictFractionIndicators = strictFractionIndicators;
    }

    /**
     * Sets or replaces the existing format pattern
     *
     * @param pattern the pattern string to apply to this formatter
     * @throws IllegalArgumentException if pattern is not understood
     * @throws NullPointerException if pattern is null
     */
    public void applyPattern(String pattern) throws IllegalArgumentException, NullPointerException
    {
        if (pattern == null)
        {
            throw new NullPointerException();
        }

        // System.out.println(pattern);

        // Try to build our regular expression, if we have an error
        // then throw the Illegal Argument Exception.

        String origPattern = pattern;
        // System.out.println("Pattern1["+pattern+"]");

        // Extract regex to avoid confusion with directives
        ArrayList<String> literalList = new ArrayList<>();
        pattern = extractRegex(pattern, literalList);
        // System.out.println("Pattern2["+pattern+"]");

        // First look for, interpret, and remove directives
        Matcher m = DIRECTIVES_PATTERN.matcher(pattern);
        while (m.find())
        {
            String group = m.group();
//            System.out.println("Directive[" + group + "]");

            if (group.equals(DIRECTIVE_LATITUDES_IN_DEG_SOUTH))
            {
                latitudesInDegSouth = true;
            }
            else if (group.equals(DIRECTIVE_LONGITUDES_IN_DEG_WEST))
            {
                longitudesInDegWest = true;
            }
            else if (group.equals(DIRECTIVE_LATITUDES_IN_0_TO_180))
            {
                latitudesIn0to180 = true;
            }
            else if (group.equals(DIRECTIVE_LONGITUDES_IN_0_TO_360))
            {
                longitudesIn0to360 = true;
            }
            else if (group.equals(DIRECTIVE_FIRST_LONGITUDE_CHAR_IS_OPTIONAL))
            {
                firstLongitudeCharIsOptional = true;
            }
            else if (group.equals(DIRECTIVE_FIRSTTWO_LONGITUDE_CHARS_ARE_OPTIONAL))
            {
                first2LongitudeCharsAreOptional = true;
            }
            else if (group.equals(DIRECTIVE_FIRST_LATITUDE_CHAR_IS_OPTIONAL))
            {
                firstLatitudeCharIsOptional = true;
            }
            else if (group.equals(DIRECTIVE_STRICT_FRACTION_INDICATORS))
            {
                strictFractionIndicators = true;
            }
            else
            {
                throw new IllegalArgumentException("Unrecognized directive \"" + group + "\"found in pattern.");
            }
        }
        pattern = pattern.replaceAll(DIRECTIVES_PATTERN_STR, "");
        // System.out.println("Pattern3["+pattern+"]");

        // Start, length
        myPatternComponentList = new ArrayList<>();
        m = WHITESPACE_LITERALESCAPES_SPECIALCHARS_PATTERN.matcher(pattern);
        int litIdx = 0;
        while (m.find())
        {
            int start = m.start();
            int end = m.end();
            String group = m.group();

            // Check for and handle literals
            PatternComponent pc = null;
            if (group.equals("()"))
            {
                Pattern.compile(literalList.get(litIdx));
                String litStr = "(" + literalList.get(litIdx) + ")";
                pc = new PatternComponent(ComponentType.REGEX, start, end - start, litStr);
                litIdx++;
            }
            else
            {
                pc = new PatternComponent(ComponentType.LITERAL, start, end - start, group);
            }
            myPatternComponentList.add(pc);

            // System.out.println(pc.toString());
        }

        proccessPatternForType(ComponentType.LONGITUDE_DEGREES, LONGITUDE_DEGREE_PATTERN.matcher(pattern), myPatternComponentList,
                "degrees longitude");
        proccessPatternForType(ComponentType.LONGITUDE_MINUTES, LONGITUDE_MINUTE_PATTERN.matcher(pattern), myPatternComponentList,
                "minutes longitude");
        proccessPatternForType(ComponentType.LONGITUDE_SECONDS, LONGITUDE_SECOND_PATTERN.matcher(pattern), myPatternComponentList,
                "seconds longitude");
        proccessPatternForType(ComponentType.LONGITUDE_FRACTION_SECONDS, LONGITUDE_FRACTION_SECOND_PATTERN.matcher(pattern),
                myPatternComponentList, "fractional seconds longitude");
        proccessPatternForType(ComponentType.LONGITUDE_HEMISPHERE, LONGITUDE_HEMISPHERE_PATTERN.matcher(pattern),
                myPatternComponentList, "longitude hemisphere");
        proccessPatternForType(ComponentType.LATITUDE_DEGREES, LATITUDE_DEGREE_PATTERN.matcher(pattern), myPatternComponentList,
                "degrees latitude");
        proccessPatternForType(ComponentType.LATITUDE_MINUTES, LATITUDE_MINUTE_PATTERN.matcher(pattern), myPatternComponentList,
                "minutes latitude");
        proccessPatternForType(ComponentType.LATITUDE_SECONDS, LATITUDE_SECOND_PATTERN.matcher(pattern), myPatternComponentList,
                "seconds latitude");
        proccessPatternForType(ComponentType.LATITUDE_FRACTION_SECONDS, LATITUDE_FRACTION_SECOND_PATTERN.matcher(pattern),
                myPatternComponentList, "fractional seconds latitude");
        proccessPatternForType(ComponentType.LATITUDE_HEMISPHERE, LATITUDE_HEMISPHERE_PATTERN.matcher(pattern),
                myPatternComponentList, "latitude hemisphere");

        // Now sort the PC list into order

        Collections.sort(myPatternComponentList, new Comparator<PatternComponent>()
        {
            @Override
            public int compare(PatternComponent o1, PatternComponent o2)
            {
                Integer i1 = new Integer(o1.startIndex);
                Integer i2 = new Integer(o2.startIndex);
                return i1.compareTo(i2);
            }
        });

        // Rebuild the pattern with the regex included for comparison.
        StringBuilder rebuiltPattern = new StringBuilder();
        // System.out.println("Origina["+pattern+"]");
        String[] parts = pattern.split("\\(\\)");
        for (int prtIdx = 0; prtIdx < Math.max(literalList.size(), parts.length); prtIdx++)
        {
            if (prtIdx < parts.length)
            {
                rebuiltPattern.append(parts[prtIdx]);
                // System.out.println("Part[" + prtIdx + "][" + parts[prtIdx] +
                // "]");
            }
            if (prtIdx < literalList.size())
            {
                rebuiltPattern.append("(").append(literalList.get(prtIdx)).append(")");
            }
        }
        // System.out.println("Rebuilt["+rebuiltPattern.toString()+"]");
        pattern = rebuiltPattern.toString();

        StringBuilder sb = new StringBuilder();
        PatternComponent pc = null;
        for (int i = 0; i < myPatternComponentList.size(); i++)
        {
            pc = myPatternComponentList.get(i);
            if (pc.type == ComponentType.LATITUDE_HEMISPHERE)
            {
                myHasLatHemisphereIndicatorInPattern = true;
            }

            if (pc.type == ComponentType.LONGITUDE_HEMISPHERE)
            {
                myHasLonHemisphereIndicatorInPattern = true;
            }

            // System.out.println("["+i+"]"+myPatternComponentList.get(i).toString()
            // );
            sb.append(pc.value);
        }
        // System.out.println("Pattern1["+pattern+"]");
        // System.out.println("Pattern2["+sb.toString()+"]");

        // if we an match our reconstructed pattern with the original pattern we
        // have succeeded
        // in parsing, if not throw the exception and exit
        if (!sb.toString().equals(pattern))
        {
            throw new IllegalArgumentException(
                    "Error parsing pattern, could not validate Old[" + pattern + "] New[" + sb.toString() + "]");
        }

        // Determine After Lon Degrees Composite Length
        // If the Lon pattern looks like DDDMMSS, the composite length is 4,
        // which is the four
        // expected digits after the longitude degrees specifiers. Basically it
        // is the number of
        // numeric digits expected after the DDD specifier, but only when DDD
        // does not
        // have a decimal component like DDD.DD. We will use this later in REGEX
        // generation.
        // In the case of DDDMMSS.SS, composite length is still 4 because we
        // don't care about
        // the decimal point or anything after.
        int afterLonDegCompositeLength = 0;

        boolean foundLonDegrees = false;
        pc = null;
        for (int i = 0; i < myPatternComponentList.size(); i++)
        {
            pc = myPatternComponentList.get(i);
            if (!foundLonDegrees)
            {
                if (pc.type == ComponentType.LONGITUDE_DEGREES)
                {
                    foundLonDegrees = true;

                    if (pc.indexOfDot != -1)
                    {
                        // There are decimals break out not needed
                        break;
                    }
                }
            }
            else
            {
                if (pc.type == ComponentType.LONGITUDE_MINUTES || pc.type == ComponentType.LONGITUDE_SECONDS)
                {
                    if (pc.indexOfDot != -1)
                    {
                        afterLonDegCompositeLength += pc.wDigits;
                        // This component specified decimals, we only need to
                        // know up to the decimal point
                        break;
                    }
                    else
                    {
                        afterLonDegCompositeLength += pc.value.length();
                    }
                }
                else
                {
                    break;
                }
            }
        }
        // System.out.println("After Lon Composite Length: " +
        // afterLonDegCompositeLength );

        // Lat Composite Length
        // Same as Lon above but for latitude using dddmmss.
        int afterLatDegCompositeLength = 0;

        boolean foundLatDegrees = false;
        for (int i = 0; i < myPatternComponentList.size(); i++)
        {
            pc = myPatternComponentList.get(i);
            if (!foundLatDegrees)
            {
                if (pc.type == ComponentType.LATITUDE_DEGREES)
                {
                    foundLatDegrees = true;

                    if (pc.indexOfDot != -1)
                    {
                        // There are decimals break out not needed
                        break;
                    }
                }
            }
            else
            {
                if (pc.type == ComponentType.LATITUDE_MINUTES || pc.type == ComponentType.LATITUDE_SECONDS)
                {
                    if (pc.indexOfDot != -1)
                    {
                        afterLatDegCompositeLength += pc.wDigits;
                        // This component specified decimals, we only need to
                        // know up to the decimal point
                        break;
                    }
                    else
                    {
                        afterLatDegCompositeLength += pc.value.length();
                    }
                }
                else
                {
                    break;
                }
            }
        }
        // System.out.println("After Lat Composite Length: " +
        // afterLatDegCompositeLength );

        // Now use the PatternComponents to build up our matching regular
        // expression pattern
        // and formatter settings

        myComponentOrder = new ArrayList<>();
        StringBuilder regexSB = new StringBuilder();
        HashMap<ComponentType, Boolean> componentIsDecimalMap = new HashMap<>();
        StringBuilder formatSB = new StringBuilder();
        for (int i = 0; i < myPatternComponentList.size(); i++)
        {
            pc = myPatternComponentList.get(i);
            switch (pc.type)
            {
                case LATITUDE_DEGREES:
                case LATITUDE_MINUTES:
                case LATITUDE_SECONDS:
                case LATITUDE_FRACTION_SECONDS:
                case LONGITUDE_DEGREES:
                case LONGITUDE_MINUTES:
                case LONGITUDE_SECONDS:
                case LONGITUDE_FRACTION_SECONDS:
                {
                    myComponentOrder.add(pc.type);
                    int length = pc.value.length();
                    pc.decimal = pc.indexOfDot != -1;
                    componentIsDecimalMap.put(pc.type, pc.decimal);

                    // System.out.println("[" + pc.value + "] L: " + length + "
                    // Dot Index: " + pc.decimal );

                    switch (pc.type)
                    {
                        case LATITUDE_DEGREES:
                        case LONGITUDE_DEGREES:
                        {
                            boolean firstIsOptional = pc.type == ComponentType.LATITUDE_DEGREES && firstLatitudeCharIsOptional
                                    || pc.type == ComponentType.LONGITUDE_DEGREES && firstLongitudeCharIsOptional;

                            boolean first2AreOptional = pc.type == ComponentType.LONGITUDE_DEGREES
                                    && first2LongitudeCharsAreOptional;

                            if (first2AreOptional)
                            {
                                firstIsOptional = false;
                            }

                            if (pc.indexOfDot != -1)
                            {
                                // Build formatter string
                                if (firstIsOptional || first2AreOptional)
                                {
                                    pc.format = "%" + length + "." + pc.fDigits + "f";
                                }
                                else
                                {
                                    pc.format = "%0" + length + "." + pc.fDigits + "f";
                                }

                                // Build regex string
                                StringBuilder sbc = new StringBuilder();
                                if (strictFractionIndicators)
                                {
                                    sbc.append("([-]?\\d{");
                                }
                                else
                                {
                                    sbc.append("(?:([-]?\\d{");
                                }

                                StringBuilder sb2 = new StringBuilder();
                                if (first2AreOptional)
                                {
                                    sb2.append(pc.wDigits - 2).append(",").append(pc.wDigits);
                                }
                                else if (firstIsOptional)
                                {
                                    sb2.append(pc.wDigits - 1).append(",").append(pc.wDigits);
                                }
                                else
                                {
                                    sb2.append(pc.wDigits);
                                }

                                sbc.append(sb2.toString());
                                sbc.append("}");

                                if (strictFractionIndicators)
                                {
                                    sbc.append("\\.\\d{").append(pc.fDigits).append("})");
                                }
                                else
                                {
                                    sbc.append("\\.\\d+|[-]?\\d{").append(sb2.toString()).append("}))");
                                }

                                regexSB.append(sbc.toString());
                            }
                            else
                            {
                                // Build formatter string
                                if (firstIsOptional || first2AreOptional)
                                {
                                    pc.format = "%" + length + "d";
                                }
                                else
                                {
                                    pc.format = "%0" + length + "d";
                                }

                                // Only set the after val composite length for
                                // degrees types.
                                int afterValCompositeLength = 0;
                                if (pc.type == ComponentType.LATITUDE_DEGREES)
                                {
                                    afterValCompositeLength = afterLatDegCompositeLength;
                                }
                                else if (pc.type == ComponentType.LONGITUDE_DEGREES)
                                {
                                    afterValCompositeLength = afterLonDegCompositeLength;
                                }

                                // Build regex string
                                StringBuilder sbc = new StringBuilder();
                                sbc.append("([-]?\\d{");

                                if (first2AreOptional)
                                {
                                    sbc.append(length - 2).append(",").append(length);
                                }
                                else if (firstIsOptional)
                                {
                                    sbc.append(length - 1).append(",").append(length);
                                }
                                else
                                {
                                    sbc.append(length);
                                }

                                sbc.append("}");

                                // Add the positive look ahead regex that
                                // specifies that the degrees portion must
                                // be followed by exactly the composite length
                                // number of digits to work properly.
                                if (afterValCompositeLength > 0)
                                {
                                    sbc.append("(?=");
                                    sbc.append("\\d{");
                                    sbc.append(afterValCompositeLength);
                                    sbc.append("})");
                                }

                                sbc.append(")");
                                regexSB.append(sbc.toString());
                            }
                        }
                            break;
                        default:
                        {
                            if (pc.indexOfDot != -1)
                            {
                                // Build formatter string
                                pc.format = "%0" + length + "." + pc.fDigits + "f";
                                // Build regex string
                                StringBuilder sbc = new StringBuilder();

                                if (strictFractionIndicators)
                                {
                                    sbc.append("(\\d{").append(pc.wDigits).append("}\\.\\d{").append(pc.fDigits).append("})");
                                }
                                else
                                {
                                    sbc.append("((?:\\d{").append(pc.wDigits).append("}\\.\\d+|\\d{").append(pc.wDigits)
                                            .append("}))");
                                }

                                regexSB.append(sbc.toString());
                            }
                            else
                            {
                                // Build formatter string
                                pc.format = "%0" + length + "d";
                                // Build regex string
                                regexSB.append("(\\d{").append(length).append("})");
                            }
                        }
                    }
                    break;
                }
                case LATITUDE_HEMISPHERE:
                    myComponentOrder.add(pc.type);
                    pc.format = "%1s";
                    regexSB.append("([n|N|s|S])");
                    break;
                case LONGITUDE_HEMISPHERE:
                    myComponentOrder.add(pc.type);
                    pc.format = "%1s";
                    regexSB.append("([e|E|w|W])");
                    break;
                case LITERAL:
                {
                    String value = pc.value;
                    if (value.startsWith("[") && value.endsWith("]"))
                    {
                        value = value.substring(1, value.length() - 1);
                    }

                    pc.format = value;

                    StringBuilder sbc = new StringBuilder();
                    for (int c = 0; c < value.length(); c++)
                    {
                        sbc.append(value.charAt(c));
                    }
                    regexSB.append(Pattern.quote(sbc.toString()));
                }
                    break;
                case REGEX:
                {
                    String value = pc.value;
                    myComponentOrder.add(pc.type);
                    pc.format = "%s";

                    StringBuilder sbc = new StringBuilder();
                    for (int c = 0; c < value.length(); c++)
                    {
                        sbc.append(value.charAt(c));
                    }
                    regexSB.append(sbc.toString());
                }
                    break;
            }

            formatSB.append(pc.format);
        }

        // Check for the multiple decimal types specified errors for latitude
        // there is only allowed to be one type
        int latDecCount = 0;
        if (componentIsDecimalMap.get(ComponentType.LATITUDE_DEGREES) != null
                && componentIsDecimalMap.get(ComponentType.LATITUDE_DEGREES).booleanValue())
        {
            latDecCount++;
        }

        if (componentIsDecimalMap.get(ComponentType.LATITUDE_MINUTES) != null
                && componentIsDecimalMap.get(ComponentType.LATITUDE_MINUTES).booleanValue())
        {
            latDecCount++;
        }

        if (componentIsDecimalMap.get(ComponentType.LATITUDE_SECONDS) != null
                && componentIsDecimalMap.get(ComponentType.LATITUDE_SECONDS).booleanValue())
        {
            latDecCount++;
        }

        if (latDecCount > 1)
        {
            throw new IllegalArgumentException("Cannot specify more than one latitude component with decimal precision");
        }

        // Check for the multiple decimal types specified errors for longitude
        // there is only allowed to be one type
        int lonDecCount = 0;
        if (componentIsDecimalMap.get(ComponentType.LONGITUDE_DEGREES) != null
                && componentIsDecimalMap.get(ComponentType.LONGITUDE_DEGREES).booleanValue())
        {
            lonDecCount++;
        }

        if (componentIsDecimalMap.get(ComponentType.LONGITUDE_MINUTES) != null
                && componentIsDecimalMap.get(ComponentType.LONGITUDE_MINUTES).booleanValue())
        {
            lonDecCount++;
        }

        if (componentIsDecimalMap.get(ComponentType.LONGITUDE_SECONDS) != null
                && componentIsDecimalMap.get(ComponentType.LONGITUDE_SECONDS).booleanValue())
        {
            lonDecCount++;
        }

        if (lonDecCount > 1)
        {
            throw new IllegalArgumentException("Cannot specify more than one longitude component with decimal precision");
        }

        // Check to make sure that if degrees has decimals, no other type is
        // specified, if minutes with decimals
        // no seconds are specified. Longitude first
        if (componentIsDecimalMap.get(ComponentType.LONGITUDE_DEGREES) != null
                && componentIsDecimalMap.get(ComponentType.LONGITUDE_DEGREES).booleanValue())
        {
            if (componentIsDecimalMap.get(ComponentType.LONGITUDE_MINUTES) != null
                    || componentIsDecimalMap.get(ComponentType.LONGITUDE_SECONDS) != null)
            {
                throw new IllegalArgumentException(
                        "Cannot have longitude minutes or seconds if degrees are specified with decimal precision");
            }
        }

        if (componentIsDecimalMap.get(ComponentType.LONGITUDE_MINUTES) != null
                && componentIsDecimalMap.get(ComponentType.LONGITUDE_MINUTES).booleanValue()
                && componentIsDecimalMap.get(ComponentType.LONGITUDE_SECONDS) != null)
        {
            throw new IllegalArgumentException("Cannot have longitude seconds if minutes are specified with decimal precision");
        }

        // Check to make sure that if degrees has decimals, no other type is
        // specified, if minutes with decimals
        // no seconds are specified. Now Latitudes
        if (componentIsDecimalMap.get(ComponentType.LATITUDE_DEGREES) != null
                && componentIsDecimalMap.get(ComponentType.LATITUDE_DEGREES).booleanValue())
        {
            if (componentIsDecimalMap.get(ComponentType.LATITUDE_MINUTES) != null
                    || componentIsDecimalMap.get(ComponentType.LATITUDE_SECONDS) != null)
            {
                throw new IllegalArgumentException(
                        "Cannot have latitude minutes or seconds if degrees are specified with decimal precision");
            }
        }

        if (componentIsDecimalMap.get(ComponentType.LATITUDE_MINUTES) != null
                && componentIsDecimalMap.get(ComponentType.LATITUDE_MINUTES).booleanValue()
                && componentIsDecimalMap.get(ComponentType.LATITUDE_SECONDS) != null)
        {
            throw new IllegalArgumentException("Cannot have latitude seconds if minutes are specified with decimal precision");
        }

        // Check to make sure that if fractional seconds is being used that we
        // didn't have any decimals
        boolean hadLatitudeFractionalSeconds = false;
        boolean hadLongitudeFractionalSeconds = false;

        for (int i = 0; i < myPatternComponentList.size(); i++)
        {
            pc = myPatternComponentList.get(i);
            if (pc.type == ComponentType.LATITUDE_FRACTION_SECONDS)
            {
                hadLatitudeFractionalSeconds = true;
            }

            if (pc.type == ComponentType.LONGITUDE_FRACTION_SECONDS)
            {
                hadLongitudeFractionalSeconds = true;
            }
        }

        if (hadLatitudeFractionalSeconds && latDecCount > 0)
        {
            throw new IllegalArgumentException(
                    "Cannot specify a latitude component with decimal precision when using integer fractional seconds");
        }

        if (hadLongitudeFractionalSeconds && lonDecCount > 0)
        {
            throw new IllegalArgumentException(
                    "Cannot specify a longitude component with decimal precision when using integer fractional seconds");
        }

        // If we made it this far we can go ahead and try to compile our pattern

//        System.out.println("Regex [" + regexSB.toString() + "]" );
//        System.out.println("Format[" + formatSB.toString() + "]");

        myInputPatternString = origPattern;
        myPattern = Pattern.compile(regexSB.toString());

    }

    /**
     * Parses the given latLonText string against the current pattern and
     * returns a {@link Point2D} that contains Latitude in the X portion and
     * Longitude in the Y portion. Any REGEX matches are discarded
     *
     * Latitude is in decimal degrees north ( -90 to 90 range ) Longitude is in
     * decimal degrees east ( -180 to 180 range )
     *
     * @param latLonText
     * @return the {@link Point2D} with the lat/lon info
     * @throws ParseException if an error is encountered parsing the given
     *             string
     * @throws NullPointerException if the latLonText is null
     */
    public Point2D parse(String latLonText) throws ParseException, NullPointerException
    {
        return parse(latLonText, null);
    }

    /**
     * Parses the given latLonText string against the current pattern and
     * returns a {@link Point2D} that contains Latitude in the X portion and
     * Longitude in the Y portion.
     *
     * Additionally any regex matches from specified regex portions are returned
     * in regexMatches ( if not null ) in the order they occur in the pattern.
     *
     * Latitude is in decimal degrees north ( -90 to 90 range ) Longitude is in
     * decimal degrees east ( -180 to 180 range )
     *
     * @param latLonText
     * @param regexMatches a List<String> of the regex matches specified with
     *            using parens in the pattern, in the order of occurrence, list
     *            is immediately cleared at function begin.
     * @return the {@link Point2D} with the lat/lon info
     * @throws ParseException if an error is encountered parsing the given
     *             string
     * @throws NullPointerException if the latLonText is null
     */
    public Point2D parse(String latLonText, List<String> regexMatches) throws ParseException, NullPointerException
    {
        if (latLonText == null)
        {
            throw new NullPointerException();
        }

        if (regexMatches == null)
        {
            regexMatches = new ArrayList<>();
        }
        regexMatches.clear();

//        System.out.println("CHECKING[" + latLonText + "]");
        Matcher m = myPattern.matcher(latLonText);

        if (m.matches())
        {
            LatitudeTransformer latXform = new LatitudeTransformer();
            LongitudeTransfomer lonXform = new LongitudeTransfomer();
//            System.out.println("Matches!!!");
//            for ( int i = 0; i <= m.groupCount(); i++ )
//            {
//                System.out.println("[" + i + "][" + m.group(i) + "]");
//            }

            if (myComponentOrder.size() != m.groupCount())
            {
                throw new ParseException(
                        "Insufficient matching componentnts! Expected " + myComponentOrder.size() + " Found " + m.groupCount(),
                        -1);
            }

            ComponentType ct = null;

            for (int i = 1; i <= m.groupCount(); i++)
            {
                ct = myComponentOrder.get(i - 1);
                switch (ct)
                {
                    case LATITUDE_DEGREES:
                        latXform.degStr = m.group(i);
                        break;
                    case LATITUDE_MINUTES:
                        latXform.minStr = m.group(i);
                        break;
                    case LATITUDE_SECONDS:
                        latXform.secStr = m.group(i);
                        break;
                    case LATITUDE_FRACTION_SECONDS:
                        latXform.fracSecStr = m.group(i);
                        break;
                    case LATITUDE_HEMISPHERE:
                        latXform.hemStr = m.group(i);
                        break;
                    case LONGITUDE_DEGREES:
                        lonXform.degStr = m.group(i);
                        break;
                    case LONGITUDE_MINUTES:
                        lonXform.minStr = m.group(i);
                        break;
                    case LONGITUDE_SECONDS:
                        lonXform.secStr = m.group(i);
                        break;
                    case LONGITUDE_FRACTION_SECONDS:
                        lonXform.fracSecStr = m.group(i);
                        break;
                    case LONGITUDE_HEMISPHERE:
                        lonXform.hemStr = m.group(i);
                        break;
                    case REGEX:
                        regexMatches.add(m.group(i));
                        break;
                }
            }

            latXform.transformStrToDouble();
            lonXform.transformStrToDouble();

            return new Point2D.Double(latXform.latitudeDecDegNorth, lonXform.longitudeDecDegEast);
        }
        else
        {
            // System.out.println("DOES NOT MATCH!!!");
            throw new ParseException("Provided String did not match current pattern", 0);
        }
    }

    /**
     * Parses the given text string against the known pattern and returns the
     * latitude portion if found
     *
     * Note: Do not call this function if you are going to retrieve both
     * latitude and longitude from a string, use parse( String ) as if you call
     * both parseLatitude and parseLongitude you will double parse your string
     * and waste CPU resources.
     *
     * @param text
     * @param regexMatches a List<String> of the regex matches specified with
     *            using parens in the pattern, in the order of occurrance, list
     *            is immediately cleared at function begin.
     * @return double, the latitude in decimal degrees north ( -90 to 90 ) *
     * @throws ParseException if an error is encountered parsing the given
     *             string
     * @throws NullPointerException if the latLonText is null
     */
    public double parseLatitude(String text, List<String> regexMatches) throws ParseException, NullPointerException
    {
        Point2D pt = parse(text, regexMatches);
        return pt.getX();
    }

    /**
     * Parses the given text string against the known pattern and returns the
     * latitude portion if found
     *
     * Additionally any regex matches from specified regex portions are returned
     * in regexMatches ( if not null ) in the order they occur in the pattern.
     *
     * Note: Do not call this function if you are going to retrieve both
     * latitude and longitude from a string, use parse( String ) as if you call
     * both parseLatitude and parseLongitude you will double parse your string
     * and waste CPU resources.
     *
     * @param text
     * @return double, the latitude in decimal degrees north ( -90 to 90 )
     * @throws ParseException if an error is encountered parsing the given
     *             string
     * @throws NullPointerException if the latLonText is null
     */
    public double parseLatitude(String text) throws ParseException, NullPointerException
    {
        Point2D pt = parse(text);
        return pt.getX();
    }

    /**
     * Parses the given text string against the known pattern and returns the
     * longitude portion if found
     *
     * Additionally any regex matches from specified regex portions are returned
     * in regexMatches ( if not null ) in the order they occur in the pattern.
     *
     * Note: Do not call this function if you are going to retrieve both
     * latitude and longitude from a string, use parse( String ) as if you call
     * both parseLatitude and parseLongitude you will double parse your string
     * and waste CPU resources.
     *
     * @param text
     * @param regexMatches a List<String> of the regex matches specified with
     *            using parens in the pattern, in the order of occurrance, list
     *            is immediately cleared at function begin.
     * @return double, the longitude in decimal degrees east ( -180 to 180 )
     * @throws ParseException
     * @throws NullPointerException
     */
    public double parseLongitude(String text, List<String> regexMatches) throws ParseException, NullPointerException
    {
        Point2D pt = parse(text);
        return pt.getY();
    }

    /**
     * Parses the given text string against the known pattern and returns the
     * longitude portion if found
     *
     * Note: Do not call this function if you are going to retrieve both
     * latitude and longitude from a string, use parse( String ) as if you call
     * both parseLatitude and parseLongitude you will double parse your string
     * and waste CPU resources.
     *
     * @param text
     * @return double, the longitude in decimal degrees east ( -180 to 180 )
     * @throws ParseException
     * @throws NullPointerException
     */
    public double parseLongitude(String text) throws ParseException, NullPointerException
    {
        Point2D pt = parse(text);
        return pt.getY();
    }

    /**
     * Formats the provided latitude into the current pattern for output assumes
     * that there is no longitude specified in the current pattern.
     *
     * @param lat - the latitude in decimal degrees north ( -90 to 90 range )
     * @return the formatted string according to the pattern.
     */
    public String formatLatitude(double lat)
    {
        return format(lat, 0.0);
    }

    /**
     * Formats the provided latitude into the current pattern for output assumes
     * that there is no longitude specified in the current pattern.
     *
     * @param lat - the latitude in decimal degrees north ( -90 to 90 range )
     * @param regexValues, the list of string values to be inserted in place of
     *            regular expressions, inserted in the order they occur in the
     *            pattern string. If not available or if regexValues is null,
     *            "null" is printed in their place which may not satisfy round
     *            trip formatting.
     * @return the formatted string according to the pattern.
     */
    public String formatLatitude(double lat, List<String> regexValues)
    {
        return format(lat, 0.0, regexValues);
    }

    /**
     * Formats the provided longitude into the current pattern for output
     * assumes that there is no latitude specified in the current pattern.
     *
     * @param lon - the longitude in decimal degrees east ( -180 to 180 range )
     * @return the formatted string according to the pattern.
     */
    public String formatLongitude(double lon)
    {
        return format(0.0, lon);
    }

    /**
     * Formats the provided longitude into the current pattern for output
     * assumes that there is no latitude specified in the current pattern.
     *
     * @param lon - the longitude in decimal degrees east ( -180 to 180 range )
     * @param regexValues, the list of string values to be inserted in place of
     *            regular expressions, inserted in the order they occur in the
     *            pattern string. If not available or if regexValues is null,
     *            "null" is printed in their place which may not satisfy round
     *            trip formatting.
     * @return the formatted string according to the pattern.
     */
    public String formatLongitude(double lon, List<String> regexValues)
    {
        return format(0.0, lon, regexValues);
    }

    /**
     * Formats the provided latitude and longitude into the current pattern for
     * output
     *
     * @param lat - the latitude in decimal degrees north ( -90 to 90 range )
     * @param lon - the longitude in decimal degrees east ( -180 to 180 range )
     * @return the formatted string according to the pattern.
     */
    public String format(double lat, double lon)
    {
        return format(lat, lon, null);
    }

    /**
     * Formats the provided latitude and longitude into the current pattern for
     * output
     *
     * @param lat - the latitude in decimal degrees north ( -90 to 90 range )
     * @param lon - the longitude in decimal degrees east ( -180 to 180 range )
     * @param regexValues, the list of string values to be inserted in place of
     *            regular expressions, inserted in the order they occur in the
     *            pattern string. If not available or if regexValues is null,
     *            "null" is printed in their place which may not satisfy round
     *            trip formatting.
     * @return the formatted string according to the pattern.
     */
    public String format(double lat, double lon, List<String> regexValues)
    {
        LongitudeTransfomer lonXF = new LongitudeTransfomer();
        LatitudeTransformer latXF = new LatitudeTransformer();
        lonXF.longitudeDecDegEast = lon;
        latXF.latitudeDecDegNorth = lat;

        lonXF.decomposeDecDegComponents(myPatternComponentList);
        latXF.decomposeDecDegComponents(myPatternComponentList);

        StringBuilder sb = new StringBuilder();

        int regexCount = 0;
        for (PatternComponent pc : myPatternComponentList)
        {
            switch (pc.type)
            {
                case LATITUDE_DEGREES:
                    if (pc.decimal)
                    {
                        sb.append(String.format(pc.format, latXF.latitudeDecDegNorth));
                    }
                    else
                    {
                        sb.append(latXF.wasNegative ? "-" : "").append(String.format(pc.format, latXF.wholeDeg));
                    }
                    break;
                case LATITUDE_MINUTES:
                    if (pc.decimal)
                    {
                        sb.append(String.format(pc.format, latXF.decMin));
                    }
                    else
                    {
                        sb.append(String.format(pc.format, latXF.wholeMin));
                    }
                    break;
                case LATITUDE_SECONDS:
                    if (pc.decimal)
                    {
                        sb.append(String.format(pc.format, latXF.decSec));
                    }
                    else
                    {
                        sb.append(String.format(pc.format, latXF.wholeSec));
                    }
                    break;
                case LATITUDE_FRACTION_SECONDS:
                    sb.append(String.format(pc.format, latXF.wholeFracSec));
                    break;
                case LATITUDE_HEMISPHERE:
                    sb.append(String.format(pc.format, latXF.hemStr));
                    break;

                case LONGITUDE_DEGREES:
                    if (pc.decimal)
                    {
                        sb.append(String.format(pc.format, lonXF.longitudeDecDegEast));
                    }
                    else
                    {
                        sb.append(lonXF.wasNegative ? "-" : "").append(String.format(pc.format, lonXF.wholeDeg));
                    }
                    break;
                case LONGITUDE_MINUTES:
                    if (pc.decimal)
                    {
                        sb.append(String.format(pc.format, lonXF.decMin));
                    }
                    else
                    {
                        sb.append(String.format(pc.format, lonXF.wholeMin));
                    }
                    break;
                case LONGITUDE_SECONDS:
                    if (pc.decimal)
                    {
                        sb.append(String.format(pc.format, lonXF.decSec));
                    }
                    else
                    {
                        sb.append(String.format(pc.format, lonXF.wholeSec));
                    }
                    break;
                case LONGITUDE_FRACTION_SECONDS:
                    sb.append(String.format(pc.format, lonXF.wholeFracSec));
                    break;
                case LONGITUDE_HEMISPHERE:
                    sb.append(String.format(pc.format, lonXF.hemStr));
                    break;
                case LITERAL:
                    sb.append(pc.format);
                    break;
                case REGEX:
                {
                    String value = null;
                    if (regexValues != null && regexCount < regexValues.size())
                    {
                        value = regexValues.get(regexCount);
                    }
                    sb.append(String.format(pc.format, value));
                    regexCount++;
                }
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * Formats the provided point into the current pattern for output the point
     * must have latitude in the "X" position, and longitude in the "Y"
     * position.
     *
     * The latitude must be in decimal degrees north ( -90 to 90 range ) The
     * longitude must be in in decimal degrees east ( -180 to 180 range )
     *
     * @param latLonPoint - the point to format
     * @param regexValues, the list of string values to be inserted in place of
     *            regular expressions, inserted in the order they occur in the
     *            pattern string. If not available or if regexValues is null,
     *            "null" is printed in their place which may not satisfy round
     *            trip formatting.
     * @return the formatted string according to the pattern.
     */
    public String format(Point2D latLonPoint, List<String> regexValues)
    {
        if (latLonPoint == null)
        {
            throw new NullPointerException();
        }

        return format(latLonPoint.getX(), latLonPoint.getY(), regexValues);
    }

    /**
     * Formats the provided point into the current pattern for output the point
     * must have latitude in the "X" position, and longitude in the "Y"
     * position.
     *
     * The latitude must be in decimal degrees north ( -90 to 90 range ) The
     * longitude must be in in decimal degrees east ( -180 to 180 range )
     *
     * @param latLonPoint - the point to format
     * @return the formatted string according to the pattern.
     */
    public String format(Point2D latLonPoint)
    {
        return format(latLonPoint, null);
    }

    /**
     * Returns the currently set pattern as it was set this could be used to
     * "reset" this formatter.
     *
     * @return String the pattern
     */
    public String getPattern()
    {
        return myInputPatternString;
    }

    /**
     * Returns a regular expression string that would match a value conforming
     * to the current pattern
     *
     * @return the regular expression string.
     */
    public String toRegex()
    {
        return myPattern.pattern();
    }

    /**
     * Gets a {@link Pattern} for the regular expression that would match a
     * value of this foramt
     *
     * @return the Pattern
     */
    public Pattern getRegexPattern()
    {
        return Pattern.compile(toRegex());
    }

    public static void main(String[] args)
    {
        String aString = "{d1}{D2}[p(]([a-zA-Z1-9]{1,3}) DDD:MM\"SS.SSS'H ([a-z]{1}) dd:mm\"ss.ss'h[l l][jj]";
        String vString = "p(afd 23:45\"44.890'W p 6:45\"23.21'Sl ljj";

//        String aString = "DDD:MM\"SS.SSS'H ddd:mm\"ss.ss'h";
//        String vString = "123:45\"67.890'E 987:65\"43.21'N";

//        String aString = "DDDMMSS.SSSH ddmmss.ssh";
//        String vString = "1234522.890E 453412.21N";

//        String aString = "{d0}ddd mm ss.sss";
//        String vString = "-23 45 37.890";
//
        // String aString = "DDD:MM\"SS.SSS'H ddd:mm\"ss.ss'h";

//        SimpleLatLonFormat test = new SimpleLatLonFormat(aString);

//        try
//        {
//            ArrayList<String> regexParts = new ArrayList<String>();
//            Point2D p2d = test.parse( vString, regexParts );
//            System.out.println(p2d.toString());
//
//            String value = test.format( p2d, regexParts );
//            System.out.println("Formatted1[" + value + "]");
//
//            value = test.format( p2d );
//            System.out.println("Formatted2[" + value + "]");
//
//        }
//        catch (ParseException e)
//        {
//            e.printStackTrace();
//        }

//        try
//        {
//            //            String format = "{D2}{d1}DDD MMSSH dd mmssh";
//            //            String value = "5 3231E 2 4132N";
//
//            String format = "{D2}DDD.DDDDDDD";
//            String value = "123.567345345";
//
//            //            String format = "{d1}dd.dddddd";
//            //            String value = "74";
//
//            System.out.println("Start Format[" + format + "]");
//            System.out.println("Start Value[" + value + "]");
//
//            SimpleLatLonFormat sllf = new SimpleLatLonFormat(format);
//
//            Point2D point = sllf.parse(value);
//
//            System.out.println("Lon: " + point.getY() );
//            System.out.println("Lat: " + point.getX() );
//            System.out.println("End Value[" + sllf.format(point) + "]");
//        }
//        catch (ParseException e)
//        {
//            e.printStackTrace();
//        }
    }

    /**
     * helper method to make building up our list of PatternComponents easier
     *
     * @param ct - the component type
     * @param m - a matcher for searching
     * @param pcList - the list to add the found parts to
     * @param exceptionText - extension of exception text so error messages make
     *            more sense.
     * @throws IllegalArgumentException
     */
    protected void proccessPatternForType(ComponentType ct, Matcher m, ArrayList<PatternComponent> pcList, String exceptionText)
        throws IllegalArgumentException
    {
        ArrayList<PatternComponent> tempList = new ArrayList<>();
        while (m.find())
        {
            int start = m.start();
            int end = m.end();
            String group = m.group();
            PatternComponent pc = new PatternComponent(ct, start, end - start, group);

            if (!SimpleLatLonFormat.isOverlapping(pc, pcList))
            {
                tempList.add(pc);
                // System.out.println(pc.toString());
            }
//            else
//            {
//                System.out.println("REJECT-OVERLAP: " + pc.toString());
//            }

            if (tempList.size() > 1)
            {
                throw new IllegalArgumentException("Pattern may not contain more than one specifier for " + exceptionText);
            }
        }
        pcList.addAll(tempList);
    }

    /**
     * Determines if the given pattern component overlaps any other pattern
     * component in the provided list
     *
     * @param pc
     * @param list
     * @return true if overlaps, false if not
     */
    protected static boolean isOverlapping(PatternComponent pc, List<PatternComponent> list)
    {
        boolean overlapping = false;
        for (PatternComponent lpc : list)
        {
            if (pc.overlaps(lpc))
            {
                overlapping = true;
                break;
            }
        }
        return overlapping;
    }

    /**
     * Helper class for storing lat/lon pattern information
     */
    protected class PatternComponent
    {
        public int startIndex = -1;

        public int length = -1;

        public String value;

        public ComponentType type;

        public String format;

        public boolean decimal = false;

        public int indexOfDot = -1;

        public int wDigits = 0;

        public int fDigits = 0;

        public PatternComponent(ComponentType type, int startIndex, int length, String value)
        {
            this.type = type;
            this.startIndex = startIndex;
            this.length = length;
            this.value = value;

            indexOfDot = value.indexOf(".");
            if (indexOfDot != -1)
            {
                wDigits = indexOfDot;
                fDigits = length - indexOfDot - 1;
            }
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("PC[").append(type).append("]: Start: ").append(startIndex);
            sb.append(" End: ").append(getInclusiveEndIndex());
            sb.append(" Length: ").append(length);
            sb.append(" Value[").append(value).append("]");
            return sb.toString();
        }

        public int getInclusiveEndIndex()
        {
            return startIndex + length - 1;
        }

        public boolean overlaps(PatternComponent other)
        {
            int thisEndInclusiveIndex = getInclusiveEndIndex();
            int otherEndInclusiveIndex = other.getInclusiveEndIndex();
            return other.startIndex >= startIndex && other.startIndex <= thisEndInclusiveIndex
                    || otherEndInclusiveIndex >= startIndex && otherEndInclusiveIndex <= thisEndInclusiveIndex
                    || other.startIndex <= startIndex && otherEndInclusiveIndex >= thisEndInclusiveIndex;
        }
    }

    /**
     * A helper class for transforming latitude
     */
    protected class LatitudeTransformer
    {
        String degStr;

        String minStr;

        String secStr;

        String fracSecStr;

        String hemStr;

        double latitudeDecDegNorth;

        int wholeDeg;

        double decMin;

        int wholeMin;

        double decSec;

        int wholeSec;

        long wholeFracSec;

        boolean wasNegative = false;

        public LatitudeTransformer()
        {
        }

        public void transformStrToDouble()
        {
            double deg = 0.0;
            double min = 0.0;
            double sec = 0.0;
            double fsec = 0.0;

            LatitudeHemisphere lh = LatitudeHemisphere.NORTH;

            if (latitudesInDegSouth || hemStr != null && hemStr.toLowerCase().equals("s"))
            {
                lh = LatitudeHemisphere.SOUTH;
            }

            latitudeDecDegNorth = 0.0;

            if (degStr != null)
            {
                try
                {
                    deg = Double.parseDouble(degStr);
                }
                catch (NumberFormatException e)
                {
                    deg = 0.0;
                }
            }

            if (minStr != null)
            {
                try
                {
                    min = Double.parseDouble(minStr);
                }
                catch (NumberFormatException e)
                {
                    deg = 0.0;
                }
            }

            if (secStr != null)
            {
                try
                {
                    sec = Double.parseDouble(secStr);
                }
                catch (NumberFormatException e)
                {
                    deg = 0.0;
                }
            }

            if (fracSecStr != null)
            {
                try
                {
                    fsec = Double.parseDouble(fracSecStr);
                }
                catch (NumberFormatException e)
                {
                    deg = 0.0;
                }
                fsec /= Math.pow(10.0, fracSecStr.length());
            }

            latitudeDecDegNorth = deg + (min + (sec + fsec) / 60.0) / 60.0;

            if (lh == LatitudeHemisphere.SOUTH)
            {
                latitudeDecDegNorth = -1.0 * latitudeDecDegNorth;
            }

            // If our latitudesIn0to180 directive is set, modify the latitude
            // into the proper range.
            if (latitudesIn0to180)
            {
                latitudeDecDegNorth -= 90.0;
            }

        }

        public void decomposeDecDegComponents(List<PatternComponent> patternComponents)
        {
            double workingLat = latitudeDecDegNorth;
            hemStr = "N";

            if (latitudesInDegSouth)
            {
                workingLat = -1.0 * workingLat;
                hemStr = "S";
            }
            else
            {
                // If we're not being directed to deg south, then
                // then determine our hemisphere marker by our
                // value, if negative switch markers

                if (myHasLatHemisphereIndicatorInPattern && workingLat < 0.0)
                {
                    hemStr = "S";
                    workingLat = -1.0 * workingLat;
                }
            }

            if (latitudesIn0to180)
            {
                workingLat += 90.0;
            }

            latitudeDecDegNorth = workingLat;
            // This conversion works better on positive numbers
            // so remember if it was negative then fix it later
            wasNegative = workingLat < 0;
            if (wasNegative)
            {
                workingLat = -1.0 * workingLat;
            }

            wholeDeg = (int)Math.floor(workingLat);
            decMin = (workingLat - wholeDeg) * 60.0;

            wholeMin = (int)Math.floor(decMin);
            decSec = (decMin - wholeMin) * 60.0;

            wholeSec = (int)Math.round(decSec);

            double fSecMultiplier = 0.0;
            for (PatternComponent pc : patternComponents)
            {
                if (pc.type == ComponentType.LATITUDE_FRACTION_SECONDS)
                {
                    // If fractional seconds is being used then we don't want to
                    // round whole seconds
                    wholeSec = (int)Math.floor(decSec);
                    fSecMultiplier = Math.pow(10.0, pc.length);
                    break;
                }
            }

            double decFracSec = (decSec - wholeSec) * fSecMultiplier;
            wholeFracSec = Math.round(decFracSec);
        }
    }

    /**
     * A helper class for transforming longitude
     */
    protected class LongitudeTransfomer
    {
        String degStr;

        String minStr;

        String secStr;

        String fracSecStr;

        String hemStr;

        // Must be -180 to 180
        double longitudeDecDegEast;

        int wholeDeg;

        double decMin;

        int wholeMin;

        double decSec;

        int wholeSec;

        long wholeFracSec;

        boolean wasNegative = false;

        public LongitudeTransfomer()
        {
        }

        public void transformStrToDouble()
        {
            double deg = 0.0;
            double min = 0.0;
            double sec = 0.0;
            double fsec = 0.0;

            LongitudeHemisphere lh = LongitudeHemisphere.EAST;

            if (longitudesInDegWest || hemStr != null && hemStr.toLowerCase().equals("w"))
            {
                lh = LongitudeHemisphere.WEST;
            }

            longitudeDecDegEast = 0.0;

            if (degStr != null)
            {
                try
                {
                    deg = Double.parseDouble(degStr);
                }
                catch (NumberFormatException e)
                {
                    deg = 0.0;
                }
            }

            if (minStr != null)
            {
                try
                {
                    min = Double.parseDouble(minStr);
                }
                catch (NumberFormatException e)
                {
                    deg = 0.0;
                }
            }

            if (secStr != null)
            {
                try
                {
                    sec = Double.parseDouble(secStr);
                }
                catch (NumberFormatException e)
                {
                    deg = 0.0;
                }
            }

            if (fracSecStr != null)
            {
                try
                {
                    fsec = Double.parseDouble(fracSecStr);
                }
                catch (NumberFormatException e)
                {
                    deg = 0.0;
                }
                fsec /= Math.pow(10.0, fracSecStr.length());
            }

            longitudeDecDegEast = deg + (min + (sec + fsec) / 60.0) / 60.0;
            if (lh == LongitudeHemisphere.WEST)
            {
                longitudeDecDegEast = -1.0 * longitudeDecDegEast;
            }

            // Unwrap and ensure we're in the -180 to 180 range.
            while (longitudeDecDegEast > 180.0)
            {
                longitudeDecDegEast -= 360.0;
            }

            while (longitudeDecDegEast < -180.0)
            {
                longitudeDecDegEast += 360.0;
            }
        }

        public void decomposeDecDegComponents(List<PatternComponent> patternComponents)
        {
            double workingLon = longitudeDecDegEast;

            hemStr = "E";
            if (longitudesInDegWest)
            {
                workingLon = -1.0 * workingLon;
                hemStr = "W";
            }
            else
            {
                // If we're not being directed to deg west, then
                // then determine our hemisphere marker by our
                // value, if negative switch markers

                if (myHasLonHemisphereIndicatorInPattern && workingLon < 0)
                {
                    hemStr = "W";
                    workingLon = -1.0 * workingLon;
                }
            }

            if (longitudesIn0to360)
            {
                if (workingLon < 0.0)
                {
                    workingLon = 360.0 + workingLon;
                }
            }

            longitudeDecDegEast = workingLon;

            // This conversion works better on positive numbers
            // so remember if it was negative then fix it later
            wasNegative = workingLon < 0;
            if (wasNegative)
            {
                workingLon = -1.0 * workingLon;
            }

            wholeDeg = (int)Math.floor(workingLon);

            decMin = (workingLon - wholeDeg) * 60.0;

            wholeMin = (int)Math.floor(decMin);
            decSec = (decMin - wholeMin) * 60.0;

            wholeSec = (int)Math.round(decSec);

            double fSecMultiplier = 0.0;
            for (PatternComponent pc : patternComponents)
            {
                if (pc.type == ComponentType.LONGITUDE_FRACTION_SECONDS)
                {
                    // If fractional seconds is being used then we don't want to
                    // round whole seconds
                    wholeSec = (int)Math.floor(decSec);
                    fSecMultiplier = Math.pow(10.0, pc.length);
                    break;
                }
            }

            double decFracSec = (decSec - wholeSec) * fSecMultiplier;
            wholeFracSec = Math.round(decFracSec);
        }
    }

    /**
     * Extracts the contents of liters from the given pattern so that all
     * content without outer most paren pairs are preserved and stored in
     * occurrence order in the literalsList. Returned string contains pattern
     * with no content within outermost bracket pairs.
     *
     * EX: pattern 123(12323)kji returns the string "123()kji" and the value
     * "12323" as the first entry EX: pattern 123([2]a)kji returns the string
     * "123()kji" and the value "[2]a" as the first entry
     *
     * @param pattern the pattern to process.
     * @param literalsList the list of literals extracted in order of
     *            occurrance.
     * @return the pattern string
     */
    protected String extractRegex(String pattern, ArrayList<String> literalsList)
    {
        if (pattern == null || literalsList == null)
        {
            throw new NullPointerException();
        }

        literalsList.clear();

        int parenStartIdx = -1;
        int parenCount = 0;
        int bracketCount = 0;
        StringBuilder newPattern = new StringBuilder();
        StringBuilder curLiteral = null;

        char lastChar = ' ';

        for (int i = 0; i < pattern.length(); i++)
        {
            char curChar = pattern.charAt(i);
            if (parenStartIdx != -1)
            {
                if (curChar == '(')
                {
                    if (lastChar != '\\')
                    {
                        throw new RegexGroupUnallowedException(
                                "Regular Expressions for SimpleLatLonFormat may not contain groups ( parenthesis ) unless they are escaped: "
                                        + pattern);
                    }
                    parenCount++;
                    curLiteral.append(curChar);
                }
                else if (curChar == ')')
                {
                    if (parenCount == 0)
                    {
                        newPattern.append(curChar);
                        literalsList.add(curLiteral.toString());
                        curLiteral = null;
                        parenStartIdx = -1;
                    }
                    else
                    {
                        curLiteral.append(curChar);
                        parenCount--;
                    }
                }
                else
                {
                    curLiteral.append(curChar);
                }
            }
            else
            {
                if (curChar == '[')
                {
                    bracketCount++;
                }

                if (bracketCount > 0 && curChar == ']')
                {
                    bracketCount--;
                }

                if (bracketCount == 0 && curChar == '(')
                {
                    curLiteral = new StringBuilder();
                    parenStartIdx = i;
                }
                newPattern.append(curChar);
            }

            lastChar = curChar;
        }

//        System.out.println("NEW PATTERN[" + newPattern.toString() + "]");
//        for ( int i = 0; i < literalsList.size(); i++ )
//            System.out.println("Literal[" + i + "][" + literalsList.get(i) + "]");

        return newPattern.toString();
    }

    public class RegexGroupUnallowedException extends RuntimeException
    {
        public RegexGroupUnallowedException()
        {
            super();
        }

        public RegexGroupUnallowedException(String message)
        {
            super(message);
        }

        public RegexGroupUnallowedException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public RegexGroupUnallowedException(Throwable cause)
        {
            super(cause);
        }
    }
}