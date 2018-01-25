package io.opensphere.wps.response;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.common.time.DateUtils;
import io.opensphere.core.common.util.SimpleDateFormatHelper;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.wps.source.WPSFeature;
import io.opensphere.wps.source.WPSFeature.GEOMTYPE;
import io.opensphere.wps.util.WPSConstants;

/** Parser for the GML place names. */
@SuppressWarnings("PMD.GodClass")
public class GmlWpsSaxHandler311 extends org.xml.sax.helpers.DefaultHandler
{
    /** The class logger. */
    private static final Logger LOGGER = Logger.getLogger(GmlWpsSaxHandler311.class);

    /** Boolean to determine if begin date has already been set. */
    private boolean myBeginDateSet;

    /** The current beginning date. */
    private Date myCurrentBeginDate;

    /** The current color. */
    private Color myCurrentDotColor = Color.WHITE;

    /** The current ending date. */
    private Date myCurrentEndDate;

    /** The latitude of the current name. */
    private double myCurrentLat;

    /** The longitude of the current name. */
    private double myCurrentLon;

    /**
     * The current positions (used when multiple lat/lon values are present).
     */
    private final List<LatLonAlt> myCurrentPositions = new ArrayList<>();

    /** The current name of featureMember being populated. */
    private String myCurrentName;

    /** The way to determine if track. */
    // private boolean myIsTrack;
    /** The way to determine if polygon. */
    // private boolean myIsPolygon;

    /** The current time instant. */
    private Date myCurrentTimeInstant = new Date();

    /** Boolean to determine if end date has already been set. */
    private boolean myEndDateSet;

    /** The list of geometries created from request. */
    private final List<Geometry> myGeometries = new ArrayList<>();

    /** The lower timespan time value. */
    private Date myLowerTimespanDate = new Date();

    /** The wps process result. */
    private final WPSProcessResult myProcessResult;

    /** The properties for a feature. */
    private final Map<String, String> myProperties = new HashMap<>();

    /** Interned names. */
    private final Stack<String> myTags = new Stack<>();

    /** The upper timespan time value. */
    private Date myUpperTimespanDate = new Date();

    /**
     * Constructor.
     *
     * @param name The name of request we are parsing.
     */
    public GmlWpsSaxHandler311(String name)
    {
        this(new WPSProcessResult(name));
    }

    /**
     * Constructor.
     *
     * @param result The process result to fill in.
     */
    public GmlWpsSaxHandler311(WPSProcessResult result)
    {
        myProcessResult = result;
        myUpperTimespanDate.setTime(0);
    }

    @Override
    public void characters(char[] ch, int start, int length)
    {
        String qName = myTags.peek();
        String currentValue = new String(ch, start, length);
        if (currentValue.isEmpty())
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Unable to parse value for " + qName + ", empty string value.");
            }
        }
        else if (WPSConstants.LAT.equals(qName))
        {
            handleLat(currentValue);
        }
        else if (WPSConstants.LON.equals(qName))
        {
            handleLon(currentValue);
        }
        else if (WPSConstants.DATA_POINT_COLOR.equals(qName))
        {
            handleDataPointColor(currentValue);
        }
        else if (!myBeginDateSet
                && (WPSConstants.BEGIN_TIME_POSITION.equals(qName) || WPSConstants.GML_BEGIN_TIME_POSITION.equals(qName)))
        {
            handleBeginPosition(currentValue);
        }
        else if (!myEndDateSet
                && (WPSConstants.END_TIME_POSITION.equals(qName) || WPSConstants.GML_END_TIME_POSITION.equals(qName)))
        {
            handleEndPosition(currentValue);
        }
        else if (WPSConstants.TIME_POSITION.equals(qName))
        {
            handleTimePosition(currentValue);
        }
        else if (!myBeginDateSet && WPSConstants.UP_TIME_FIELD.equals(qName))
        {
            handleUpDateTime(currentValue);
        }
        else if (!myEndDateSet && (WPSConstants.DOWN_TIME_FIELD.equals(qName) || WPSConstants.DATA_POINT_DOWN_TIME.equals(qName)
                || WPSConstants.DATA_POINT_INT_TIME_DOWN.equals(qName)))
        {
            handleEndPosition(currentValue);
        }
        else if (myEndDateSet && WPSConstants.DATA_POINT_TIME_DOWN.equals(qName))
        {
            setTimeDown(currentValue);
        }
        else if ("gml:pos".equals(qName))
        {
            parseLatLon(currentValue);
        }
        else if ("gml:posList".equals(qName))
        {
            parseLatLon(currentValue);
        }
        else if ("gml:id".equals(qName))
        {
            myCurrentName = currentValue;
        }
        else
        {
            // Add everything else to the properties.
            myProperties.put(qName, currentValue);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        // End tag of feature
        if (WPSConstants.GML_FEATURE_MEMBER.equals(qName))
        {
            myProcessResult.addFeature(createWPSFeature());

            if (myCurrentBeginDate != null && myCurrentBeginDate.getTime() < myLowerTimespanDate.getTime())
            {
                myLowerTimespanDate = myCurrentBeginDate;
            }
            if (myCurrentTimeInstant != null && myCurrentTimeInstant.getTime() < myLowerTimespanDate.getTime())
            {
                myLowerTimespanDate = myCurrentTimeInstant;
            }

            if (myCurrentEndDate != null && myCurrentEndDate.getTime() > myUpperTimespanDate.getTime())
            {
                myUpperTimespanDate = myCurrentEndDate;
            }
            if (myCurrentTimeInstant != null && myCurrentTimeInstant.getTime() > myUpperTimespanDate.getTime())
            {
                myUpperTimespanDate = myCurrentTimeInstant;
            }

            myCurrentName = null;
            myCurrentLat = 0;
            myCurrentLon = 0;
            myCurrentPositions.clear();
            myCurrentDotColor = Color.white;
            myCurrentBeginDate = null;
            myCurrentEndDate = null;
            myCurrentTimeInstant = null;
            myBeginDateSet = false;
            myEndDateSet = false;
            myProperties.clear();
        }

        // End tag of all feature data.
        if (WPSConstants.DATA_COLLECTION_ROOT.equals(qName))
        {
            // Check if times have not been set (no features returned).
            if (myUpperTimespanDate.getTime() < myLowerTimespanDate.getTime())
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(" The times have not been set.  uppertimespandate now equal to lowertimespandate");
                }
                myUpperTimespanDate.setTime(myLowerTimespanDate.getTime());
            }
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(" Timespan " + myLowerTimespanDate.toString() + "  " + myUpperTimespanDate.toString());
            }
            myProcessResult.setTimespan(TimeSpan.get(myLowerTimespanDate.getTime(), myUpperTimespanDate.getTime()));
        }

        myTags.pop();
    }

    /**
     * Standard getter.
     *
     * @return The color.
     */
    public Color getColor()
    {
        return myCurrentDotColor;
    }

    /**
     * Standard getter.
     *
     * @return The geometries that have been parsed.
     */
    public List<Geometry> getGeometries()
    {
        return myGeometries;
    }

    /**
     * Standard getter.
     *
     * @return The wps process result.
     */
    public WPSProcessResult getProcessResult()
    {
        return myProcessResult;
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes)
    {
        /* if ("LinearRing".equals(localName)) { myIsPolygon = true; } else if
         * ("LineString".equals(localName)) { myIsTrack = true; } */

        myTags.push(qName);
    }

    /**
     * Gets the tags.
     *
     * @return the tags
     */
    protected Stack<String> getTags()
    {
        return myTags;
    }

    /**
     * Create a wps feature from my current values.
     *
     * @return The wps feature.
     */
    private WPSFeature createWPSFeature()
    {
        // Check if we have lat/lon values for a single point.
        if (myCurrentPositions.isEmpty())
        {
            myCurrentPositions.add(LatLonAlt.createFromDegrees(myCurrentLat, myCurrentLon));
        }

        WPSFeature feature = new WPSFeature(myCurrentPositions);

        setGeometryType(feature);

        feature.setColor(myCurrentDotColor);

        if (!myProperties.isEmpty())
        {
            feature.addProperties(myProperties);
        }
        if (myCurrentName != null)
        {
            feature.setName(myCurrentName);
        }
        if (myCurrentBeginDate != null)
        {
            feature.setUpDate(myCurrentBeginDate);
        }
        if (myCurrentEndDate != null)
        {
            feature.setDownDate(myCurrentEndDate);
        }
        if (myCurrentTimeInstant != null)
        {
            feature.setTimeInstant(myCurrentTimeInstant);
        }
        return feature;
    }

    /**
     * Handle begin position tag.
     *
     * @param currentValue The value.
     */
    private void handleBeginPosition(String currentValue)
    {
        Date date = DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(currentValue));
        if (date == null)
        {
            printParsingWarning(currentValue);
        }
        else
        {
            myCurrentBeginDate = date;
            myCurrentTimeInstant = date;
        }
    }

    /**
     * Handle data point color tag.
     *
     * @param currentValue The value.
     */
    private void handleDataPointColor(String currentValue)
    {
        setColor(currentValue);
    }

    /**
     * Handle end position tag.
     *
     * @param currentValue The value.
     */
    private void handleEndPosition(String currentValue)
    {
        Date date = DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(currentValue));
        if (date == null)
        {
            printParsingWarning(currentValue);
        }
        else
        {
            myCurrentEndDate = date;
            myEndDateSet = true;
        }
    }

    /**
     * Handle LAT tag.
     *
     * @param currentValue The value.
     */
    private void handleLat(String currentValue)
    {
        myCurrentLat = Double.parseDouble(currentValue);
    }

    /**
     * Handle LON tag.
     *
     * @param currentValue The value.
     */
    private void handleLon(String currentValue)
    {
        myCurrentLon = Double.parseDouble(currentValue);
    }

    /**
     * Handle time position tag.
     *
     * @param currentValue The value.
     */
    private void handleTimePosition(String currentValue)
    {
        Date date = DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(currentValue));
        if (date == null)
        {
            printParsingWarning(currentValue);
        }
        else
        {
            myCurrentTimeInstant = date;
            myCurrentBeginDate = date;
            myCurrentEndDate = date;
            myEndDateSet = true;
        }
    }

    /**
     * Handle up date time tag.
     *
     * @param currentValue The value.
     */
    private void handleUpDateTime(String currentValue)
    {
        Date date = DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(currentValue));
        if (date == null)
        {
            printParsingWarning(currentValue);
        }
        else
        {
            myCurrentBeginDate = date;
            myBeginDateSet = true;
        }
    }

    /**
     * Helper class to parse a string containing longitude and latitude (in that
     * order) separated by a space and set appropriate values.
     *
     * @param value The string of lat/lon values.
     */
    private void parseLatLon(String value)
    {
        try
        {
            String[] arr = value.split(" ");

            if (arr.length >= 2 && arr.length % 2 == 0)
            {
                for (int i = 0; i < arr.length; i = i + 2)
                {
                    if (!StringUtils.isBlank(arr[i + 1]) && !StringUtils.isBlank(arr[i]))
                    {
                        myCurrentPositions
                                .add(LatLonAlt.createFromDegrees(Double.parseDouble(arr[i + 1]), Double.parseDouble(arr[i])));
                    }
                }
            }
        }
        catch (NumberFormatException e)
        {
            LOGGER.warn("Parse error parsing a pos", e);
        }
    }

    /**
     * Helper method to print common warning.
     *
     * @param value The value that failed to parse.
     */
    private void printParsingWarning(String value)
    {
        LOGGER.warn("Failed parsing " + value);
    }

    /**
     * Helper method to set the color.
     *
     * @param value The string value of current tag.
     */
    private void setColor(String value)
    {
        try
        {
            StringBuilder strBuf = new StringBuilder("0x");
            strBuf.append(value.replace("#", ""));

            myCurrentDotColor = Color.decode(strBuf.toString().toLowerCase());
        }
        catch (NumberFormatException e)
        {
            LOGGER.warn("Color parse error", e);
        }
    }

    /**
     * Helper method to find and set the geometry type of the given feature.
     *
     * @param feature The feature to set geometry type for.
     */
    private void setGeometryType(WPSFeature feature)
    {
        if (myCurrentPositions.size() == 1)
        {
            feature.setGeometryType(GEOMTYPE.POINT);
        }
        else if (myCurrentPositions.size() == 2)
        {
            feature.setGeometryType(GEOMTYPE.LINE);
        }
        else if (myCurrentPositions.size() > 2)
        {
            feature.setGeometryType(GEOMTYPE.POLYGON);
        }
    }

    /**
     * Helper method to set the down date.
     *
     * @param value The string value of current tag.
     */
    private void setTimeDown(String value)
    {
        try
        {
            SimpleDateFormat dayFormatter = SimpleDateFormatHelper.getSimpleDateFormat("yyyy-MM-dd 'z'HHmmss'.00'");
            SimpleDateFormat day = SimpleDateFormatHelper.getSimpleDateFormat("yyyy-MM-dd");

            StringBuilder time = new StringBuilder(day.format(myCurrentEndDate));
            time.append(' ').append(value.replace(" ", ""));

            myCurrentEndDate = DateTimeUtilities.parse(dayFormatter, time.toString());
            myEndDateSet = true;
        }
        catch (ParseException e)
        {
            LOGGER.warn("Parsing optional downtimes " + e.getMessage());
            myEndDateSet = false;
        }
    }
}
