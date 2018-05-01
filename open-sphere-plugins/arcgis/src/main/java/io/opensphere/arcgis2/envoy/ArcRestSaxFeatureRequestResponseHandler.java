package io.opensphere.arcgis2.envoy;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import io.opensphere.arcgis2.envoy.ArcConstants.ArcSaxState;
import io.opensphere.arcgis2.esri.EsriFullLayer.EsriGeometryType;
import io.opensphere.core.common.json.JSONSaxHandler;
import io.opensphere.core.common.json.JSONSaxParseException;
import io.opensphere.core.common.json.JSONSaxParser;
import io.opensphere.core.common.json.obj.JSONSaxPrimitiveValue;
import io.opensphere.core.common.json.obj.JSONSaxTextValue;
import io.opensphere.core.common.util.SimpleDateFormatHelper;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Aggregator;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.model.GeographicUtilities;
import io.opensphere.core.util.model.GeographicUtilities.PolygonWinding;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * The Class ArcRestSaxFeatureRequestResponseHandler uses a sax parser to
 * translate results from a JSON-formatted Arc REST data query.
 */
@SuppressWarnings("PMD.GodClass")
public class ArcRestSaxFeatureRequestResponseHandler implements JSONSaxHandler
{
    /** The Constant ourDSTOffset. */
    public static final long ourDSTOffset;

    /** The Constant ourGMTOffset. */
    public static final long ourGMTOffset;

    /** The Constant ourDefaultColor. */
    protected static final Color ourDefaultColor = Color.WHITE;

    /** The Constant time zone for local times. */
    private static final TimeZone LOCAL_TIME_ZONE = Calendar.getInstance().getTimeZone();

    /** The static LOGGER reference. */
    private static final Logger LOGGER = Logger.getLogger(ArcRestSaxFeatureRequestResponseHandler.class);

    /** Consumer of translated points, tracks, and polygons. */
    private final Aggregator<MapDataElement> myConsumer;

    /** My current attribute name. */
    private String myCurrentAttributeName;

    /** My current attributes. */
    private final List<Pair<String, JSONSaxPrimitiveValue>> myCurrentAttributes = New.list();

    /** My current geometry type. */
    private EsriGeometryType myCurrentGeometryType;

    /** My current geometry type string. */
    private String myCurrentGeometryTypeStr;

    /** Current "has attributes" flag. */
    private boolean myCurrentHasAttributes;

    /** Current "has geometry" flag. */
    private boolean myCurrentHasGeometry;

    /** My current latitude. */
    private double myCurrentLat;

    /** My current longitude. */
    private double myCurrentLon;

    /** My current list of points. */
    private final List<LatLonAlt> myCurrentPoints = New.list();

    /** Name of the down-time field. */
    private final String myDownTimeFieldName;

    /** Accumulated list of errors. */
    private final List<JSONSaxParseException> myErrors = New.list();

    /** Accumulated list of fatal errors. */
    private final List<JSONSaxParseException> myFatalErrors = New.list();

    /** Count of processed features. */
    private int myFeatureCount;

    /** List of lists of points. */
    private final List<List<LatLonAlt>> myPointListList = New.list();

    /** My relative array depth. */
    private int myRelativeArrayDepth;

    /** My current parse state. */
    private ArcSaxState myState = ArcSaxState.SEEK_GEOMETRY_TYPE_TAG;

    /** Name of the time or up-time field. */
    private final String myTimeOrUpTimeFieldName;

    /** The current DataTypeInfo. */
    private final DataTypeInfo myTypeInfo;

    /** Accumulated list of warnings. */
    private final List<JSONSaxParseException> myWarnings = New.list();

    static
    {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        ourGMTOffset = cal.get(Calendar.ZONE_OFFSET);
        ourDSTOffset = LOCAL_TIME_ZONE.getDSTSavings();
    }

    /**
     * CTOR with a properties model.
     *
     * @param info the DataTypeInfo for this layer
     * @param consumer the feature consumer
     */
    public ArcRestSaxFeatureRequestResponseHandler(DataTypeInfo info, Aggregator<MapDataElement> consumer)
    {
        myTypeInfo = info;
        myConsumer = consumer;

        myTimeOrUpTimeFieldName = info.getMetaDataInfo() == null ? null : info.getMetaDataInfo().getTimeKey();
        // TODO
//        myDownTimeFieldName = info.getMetaDataInfo() == null ? null
//                : info.getMetaDataInfo().getKeyForSpecialType(EndTimeKey.DEFAULT);
        myDownTimeFieldName = null;
    }

    /**
     * Array element separator.
     */
    @Override
    public void arrayElementSeparator()
    {
    }

    /**
     * Array end.
     */
    @Override
    public void arrayEnd()
    {
        // Look for triggers for the second ring/polyline in the array so we can
        // start a second list.
        if (myRelativeArrayDepth == 2
                && (myState == ArcSaxState.COLLECT_TRACK_PATH_X || myState == ArcSaxState.COLLECT_POLYGON_RING_X))
        {
            myPointListList.add(New.list(myCurrentPoints));
            myCurrentPoints.clear();
        }
        myRelativeArrayDepth--;
    }

    /**
     * Array start.
     */
    @Override
    public void arrayStart()
    {
        myRelativeArrayDepth++;
    }

    /**
     * Clear all errors and warnings.
     */
    public void clearAllErrorsAndWarnings()
    {
        myErrors.clear();
        myWarnings.clear();
        myFatalErrors.clear();
    }

    /**
     * Clear the current feature state.
     */
    public void clearFeatureState()
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("->CLEAR FEATURE STATE");
        }

        myCurrentHasAttributes = false;
        myCurrentHasGeometry = false;
        myCurrentAttributes.clear();
        myCurrentPoints.clear();
        myPointListList.clear();
        myCurrentLat = 0.;
        myCurrentLon = 0.;
    }

    /**
     * Document end.
     */
    @Override
    public void documentEnd()
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("->DOCUMENT END");
        }

        if (myCurrentHasAttributes || myCurrentHasGeometry)
        {
            // We may be in an incomplete state from our last feature. If we
            // have both try to create the feature, otherwise abandon feature.
            if (myCurrentHasAttributes && myCurrentHasGeometry)
            {
                createGeometryFromCurrent();
            }
            else
            {
                LOGGER.error("Incomplete feature from document, abandoning");
                clearFeatureState();
            }
        }
    }

    /**
     * Document start.
     */
    @Override
    public void documentStart()
    {
    }

    /**
     * Error.
     *
     * @param e The e.
     */
    @Override
    public void error(JSONSaxParseException e)
    {
        myErrors.add(e);
    }

    /**
     * Fatal error.
     *
     * @param e The e.
     */
    @Override
    public void fatalError(JSONSaxParseException e)
    {
        myFatalErrors.add(e);
    }

    /**
     * Gets the errors.
     *
     * @return the list of errors
     */
    public List<JSONSaxParseException> getErrors()
    {
        if (myErrors == null)
        {
            return null;
        }
        return Collections.unmodifiableList(myErrors);
    }

    /**
     * Gets the fatal errors.
     *
     * @return the list of fatal errors
     */
    public List<JSONSaxParseException> getFatalErrors()
    {
        if (myFatalErrors == null)
        {
            return null;
        }
        return Collections.unmodifiableList(myFatalErrors);
    }

    /**
     * Gets the number of processed features.
     *
     * @return the number of features processed
     */
    public int getProcessedCount()
    {
        return myFeatureCount;
    }

    /**
     * Gets the warnings.
     *
     * @return the list of warnings
     */
    public List<JSONSaxParseException> getWarnings()
    {
        if (myWarnings == null)
        {
            return null;
        }
        return Collections.unmodifiableList(myWarnings);
    }

    /**
     * Handle a file response.
     *
     * @param file the file
     * @throws JSONSaxParseException the jSON sax parse exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void handleResponse(File file) throws JSONSaxParseException, IOException
    {
        JSONSaxParser aParser = new JSONSaxParser(this);
        aParser.parse(file);
        myConsumer.processAll();
    }

    /**
     * Parses the JSON format text.
     *
     * @param inputStream the input stream
     *
     * @throws JSONSaxParseException if a problem is encountered while
     *             processing the input text.
     * @throws IOException if an error occurs connecting to or reading from
     *             inputStream.
     */
    public void handleResponse(InputStream inputStream) throws JSONSaxParseException, IOException
    {
        JSONSaxParser aParser = new JSONSaxParser(this);
        aParser.parse(inputStream);
        myConsumer.processAll();
    }

    /**
     * Ignorable white space.
     *
     * @param whiteSpaceChars The white space chars.
     */
    @Override
    public void ignorableWhiteSpace(String whiteSpaceChars)
    {
    }

    /**
     * Key.
     *
     * @param keyValue The key value.
     */
    @Override
    public void key(String keyValue)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Key[" + keyValue + "] State[" + myState + "]");
        }

        switch (myState)
        {
            case SEEK_GEOMETRY_TYPE_TAG:
                checkForGeometryTypeKey(keyValue);
                break;
            case SEEK_FEATURES_TAG:
                checkForFeaturesKey(keyValue);
                break;
            case SEEK_ATTRIBUTES_TAG:
                checkForAttributesKey(keyValue);
                break;
            case COLLECT_ATTRIBUTES:
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("->CREATING NEW PAIR");
                }
                myCurrentAttributeName = keyValue;
                break;
            case SEEK_GEOMETRY_TAG:
                checkForGeometryKey(keyValue);
                break;
            case SEEK_POINT_X_TAG:
                checkForGeometryLonKey(keyValue);
                break;
            case SEEK_POINT_Y_TAG:
                checkForGeometryLatKey(keyValue);
                break;
            case SEEK_TRACK_PATH_TAG:
                checkForPaths(keyValue);
                break;
            case SEEK_POLYGON_RING_TAG:
                checkForRings(keyValue);
                break;
            default:
                break;
        }
    }

    /**
     * Key value pair separator.
     */
    @Override
    public void keyValuePairSeparator()
    {
    }

    /**
     * Key value separator.
     */
    @Override
    public void keyValueSeparator()
    {
    }

    /**
     * Object end.
     */
    @Override
    public void objectEnd()
    {
        switch (myState)
        {
            case COLLECT_ATTRIBUTES:
                myCurrentHasAttributes = true;
                myState = ArcSaxState.SEEK_GEOMETRY_TAG;
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("->DETECTED END OF GEOMETRY OBJECT");
                }
                break;
            case COLLECT_TRACK_PATH_X:
            case COLLECT_TRACK_PATH_Y:
            case COLLECT_POLYGON_RING_X:
            case COLLECT_POLYGON_RING_Y:
                myCurrentHasGeometry = true;
                createGeometryFromCurrent();
                myState = ArcSaxState.SEEK_ATTRIBUTES_TAG;
                break;
            default:
                break;
        }
    }

    /**
     * Object start.
     */
    @Override
    public void objectStart()
    {
    }

    /**
     * Value.
     *
     * @param value The value.
     */
    @Override
    public void value(JSONSaxPrimitiveValue value)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Value[" + value.getValue() + "] State[" + myState + "]");
        }

        switch (myState)
        {
            case COLLECT_GEOMETRY_TYPE:
                collectGeometry(value);
                break;
            case COLLECT_ATTRIBUTES:
                collectAttributes(value);
                break;
            case COLLECT_POINT_X_VALUE:
                collectPointX(value);
                break;
            case COLLECT_POINT_Y_VALUE:
                collectPointY(value);
                break;
            case COLLECT_TRACK_PATH_X:
                collectTrackPathX(value);
                break;
            case COLLECT_TRACK_PATH_Y:
                collectTrackPathY(value);
                break;
            case COLLECT_POLYGON_RING_X:
                collectPolygonRingX(value);
                break;
            case COLLECT_POLYGON_RING_Y:
                collectPolygonRingY(value);
                break;
            default:
                break;
        }
    }

    /**
     * Warning.
     *
     * @param e The e.
     */
    @Override
    public void warning(JSONSaxParseException e)
    {
        myWarnings.add(e);
    }

    /**
     * Check for "attributes" key value.
     *
     * @param keyValue The key value.
     */
    private void checkForAttributesKey(String keyValue)
    {
        if (ArcConstants.ATTRIBUTES_KEY.equals(keyValue))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->Found ATTRIBUTES_KEY");
            }
            if (myCurrentHasAttributes || myCurrentHasGeometry)
            {
                // We may be in an incomplete state from our last feature. If we
                // have both try to create the feature, otherwise abandon
                // feature.
                if (myCurrentHasAttributes && myCurrentHasGeometry)
                {
                    createGeometryFromCurrent();
                }
                else
                {
                    LOGGER.error("Incomplete feature from document, abandoning");
                    clearFeatureState();
                }
            }
            myState = ArcSaxState.COLLECT_ATTRIBUTES;
        }
    }

    /**
     * Check for "features" key value.
     *
     * @param keyValue The key value.
     */
    private void checkForFeaturesKey(String keyValue)
    {
        if (ArcConstants.FEATURES_KEY.equals(keyValue))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->Found SEEK_ATTRIBUTES_TAG");
            }
            myState = ArcSaxState.SEEK_ATTRIBUTES_TAG;
            clearFeatureState();
        }
    }

    /**
     * Check for "geometry" key value.
     *
     * @param keyValue The key value.
     */
    private void checkForGeometryKey(String keyValue)
    {
        if (ArcConstants.GEOMETRY_KEY.equals(keyValue))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->FOUND GEOMETRY_KEY");
            }
            switch (myCurrentGeometryType)
            {
                case esriGeometryPoint:
                    myState = ArcSaxState.SEEK_POINT_X_TAG;
                    break;
                case esriGeometryPolygon:
                    myState = ArcSaxState.SEEK_POLYGON_RING_TAG;
                    break;
                case esriGeometryPolyline:
                    myState = ArcSaxState.SEEK_TRACK_PATH_TAG;
                    break;
                default:
                    myState = ArcSaxState.SEEK_ATTRIBUTES_TAG;
                    break;
            }
        }
    }

    /**
     * Check for "y" key value.
     *
     * @param keyValue The key value.
     */
    private void checkForGeometryLatKey(String keyValue)
    {
        if (ArcConstants.GEOMETRY_LAT_KEY.equals(keyValue))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->FOUND GEOMETRY_LAT_KEY");
            }
            myState = ArcSaxState.COLLECT_POINT_Y_VALUE;
        }
    }

    /**
     * Check for "x" key value.
     *
     * @param keyValue The key value.
     */
    private void checkForGeometryLonKey(String keyValue)
    {
        if (ArcConstants.GEOMETRY_LON_KEY.equals(keyValue))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->FOUND GEOMETRY_LON_KEY");
            }
            myState = ArcSaxState.COLLECT_POINT_X_VALUE;
        }
    }

    /**
     * Check for "geometryType" key value.
     *
     * @param keyValue The key value.
     */
    private void checkForGeometryTypeKey(String keyValue)
    {
        if (ArcConstants.GEOMETRY_TYPE_KEY.equals(keyValue))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->Found COLLECT_GEOMETRY_TYPE");
            }
            myState = ArcSaxState.COLLECT_GEOMETRY_TYPE;
        }
    }

    /**
     * Check for "paths" key value.
     *
     * @param keyValue The key value.
     */
    private void checkForPaths(String keyValue)
    {
        if ("paths".equals(keyValue))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->FOUND TRACK PATH KEY");
            }
            myRelativeArrayDepth = 0;
            myState = ArcSaxState.COLLECT_TRACK_PATH_X;
        }
    }

    /**
     * Check for "rings" key value.
     *
     * @param keyValue The key value.
     */
    private void checkForRings(String keyValue)
    {
        if ("rings".equals(keyValue))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->FOUND POLYGON RING KEY");
            }
            myRelativeArrayDepth = 0;
            myState = ArcSaxState.COLLECT_POLYGON_RING_X;
        }
    }

    /**
     * Checks JSON value for string and decodes escape characters.
     *
     * @param value the {@link JSONSaxPrimitiveValue} to check
     * @return the corrected string value, if necessary, original value
     *         otherwise.
     */
    private JSONSaxPrimitiveValue checkStringValue(JSONSaxPrimitiveValue value)
    {
        if (value instanceof JSONSaxTextValue)
        {
            String decodedString = StringEscapeUtils.unescapeJson(value.getValue());
            if (StringUtils.isNotEmpty(decodedString))
            {
                return new JSONSaxTextValue(decodedString);
            }
        }
        return value;
    }

    /**
     * Collect attributes.
     *
     * @param value The value.
     */
    private void collectAttributes(JSONSaxPrimitiveValue value)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("->COLLECTED ATTRIBUTE VALUE: " + value.getValue());
        }
        JSONSaxPrimitiveValue newValue = checkStringValue(value);
        myCurrentAttributes.add(Pair.create(myCurrentAttributeName, newValue));
    }

    /**
     * Collect geometry.
     *
     * @param value The value.
     */
    private void collectGeometry(JSONSaxPrimitiveValue value)
    {
        myCurrentGeometryTypeStr = value.getValue().trim();
        myCurrentGeometryType = determineGeometryType(myCurrentGeometryTypeStr);
        if (myCurrentGeometryType == null)
        {
            LOGGER.error("Unknown geometry type: " + myCurrentGeometryTypeStr + ". Not Supported.");
        }
        else
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("->COLLECTED GEOMETRY TYPE: " + myCurrentGeometryTypeStr);
            }
            myState = ArcSaxState.SEEK_FEATURES_TAG;
        }
    }

    /**
     * Collect point x.
     *
     * @param value The value.
     */
    private void collectPointX(JSONSaxPrimitiveValue value)
    {
        myCurrentLon = value.getDouble();
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("->COLLECTED POINT X VALUE(LON): " + myCurrentLon);
        }
        myState = ArcSaxState.SEEK_POINT_Y_TAG;
    }

    /**
     * Collect point y.
     *
     * @param value The value.
     */
    private void collectPointY(JSONSaxPrimitiveValue value)
    {
        myCurrentLat = value.getDouble();
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("->COLLECTED POINT Y VALUE(LAT): " + myCurrentLon);
        }
        myCurrentHasGeometry = true;
        createGeometryFromCurrent();
        myState = ArcSaxState.SEEK_ATTRIBUTES_TAG;
    }

    /**
     * Collect polygon ring x.
     *
     * @param value The value.
     */
    private void collectPolygonRingX(JSONSaxPrimitiveValue value)
    {
        myCurrentLon = value.getDouble();
        myState = ArcSaxState.COLLECT_POLYGON_RING_Y;
    }

    /**
     * Collect polygon ring y.
     *
     * @param value The value.
     */
    private void collectPolygonRingY(JSONSaxPrimitiveValue value)
    {
        myCurrentLat = value.getDouble();
        myCurrentPoints.add(LatLonAlt.createFromDegrees(myCurrentLat, myCurrentLon));
        myState = ArcSaxState.COLLECT_POLYGON_RING_X;
    }

    /**
     * Collect track path x.
     *
     * @param value The value.
     */
    private void collectTrackPathX(JSONSaxPrimitiveValue value)
    {
        myCurrentLon = value.getDouble();
        myState = ArcSaxState.COLLECT_TRACK_PATH_Y;
    }

    /**
     * Collect track path y.
     *
     * @param value The value.
     */
    private void collectTrackPathY(JSONSaxPrimitiveValue value)
    {
        myCurrentLat = value.getDouble();
        myCurrentPoints.add(LatLonAlt.createFromDegrees(myCurrentLat, myCurrentLon));
        myState = ArcSaxState.COLLECT_TRACK_PATH_X;
    }

    /**
     * Reformat a JSON primitive date into a java Date object.
     *
     * @param jsonDate the JSON-formatted date
     * @return the java Date representation or null if format is unrecognized.
     */
    private Date convertDate(JSONSaxPrimitiveValue jsonDate)
    {
        Date javaTime = null;
        try
        {
            long lTime = jsonDate.getLong() - ourGMTOffset;
            javaTime = new Date(lTime);
            if (LOCAL_TIME_ZONE.inDaylightTime(javaTime))
            {
                lTime = lTime - ourDSTOffset;
                javaTime = new Date(lTime);
            }
        }
        catch (NumberFormatException e)
        {
            javaTime = parseDate(jsonDate.getValue());
        }
        return javaTime;
    }

    /**
     * Converts the passed-in parameters representing a point into a.
     *
     * @param type the {@link DataTypeInfo}
     * @param attributes the list of attributes
     * @param lon the longitude
     * @param lat the latitude
     * @return the {@link MapDataElement} or null if there was a problem
     *         converting, an error will be logged. {@link MapDataElement}.
     */
    private MapDataElement createDataPoint(DataTypeInfo type, List<Pair<String, JSONSaxPrimitiveValue>> attributes, double lon,
            double lat)
    {
        MapGeometrySupport mgs = new DefaultMapPointGeometrySupport(
                LatLonAlt.createFromDegrees(lat, lon, Altitude.ReferenceLevel.TERRAIN));
        MetaDataProvider mdp = createMetaDataProvider(type.getMetaDataInfo(), attributes);
        MapDataElement point = createMapDataElement(type, attributes, mdp, mgs);
        return point;
    }

    /**
     * Creates a geometry from the current translated fields.
     */
    private void createGeometryFromCurrent()
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(">>>>>>>>>>>>>CREATE GEOMETRY " + myCurrentGeometryType);
        }

        switch (myCurrentGeometryType)
        {
            case esriGeometryPoint:
                MapDataElement dp = createDataPoint(myTypeInfo, myCurrentAttributes, myCurrentLon, myCurrentLat);
                if (dp != null)
                {
                    myConsumer.addItem(dp);
                    myFeatureCount++;
                }
                break;
            case esriGeometryPolygon:
                List<MapDataElement> polygons = createPolygons(myTypeInfo, myCurrentAttributes, myPointListList);
                myConsumer.addItems(polygons);
                myFeatureCount += polygons.size();
                break;
            case esriGeometryPolyline:
                List<MapDataElement> tracks = createTracks(myTypeInfo, myCurrentAttributes, myPointListList);
                myConsumer.addItems(tracks);
                myFeatureCount += tracks.size();
                break;
            case esriGeometryEnvelope:
            case esriGeometryMultipoint:
            case esriGeometryUnknown:
            default:
                LOGGER.error("Geometry type: " + myCurrentGeometryTypeStr + ". Not Supported.");
                break;
        }
        clearFeatureState();
    }

    /**
     * Creates a new map data element.
     *
     * @param info the info
     * @param attributes the attributes
     * @param mdp the mdp
     * @param mgs the mgs
     * @return the map data element
     */
    private MapDataElement createMapDataElement(DataTypeInfo info, List<Pair<String, JSONSaxPrimitiveValue>> attributes,
            MetaDataProvider mdp, MapGeometrySupport mgs)
    {
        Date startTime = null;
        Date endTime = null;
        String colorString = null;
        long id = 0;

        // First search for the UP/DOWN TIME keys
        String startTimePropertyName = myTimeOrUpTimeFieldName != null && !myTimeOrUpTimeFieldName.isEmpty()
                ? myTimeOrUpTimeFieldName : ArcConstants.ATTRIBUTE_DATA_DATE_KEY;

        for (Pair<String, JSONSaxPrimitiveValue> pair : attributes)
        {
            String key = pair.getFirstObject();
            String uKey = key.toUpperCase();
            JSONSaxPrimitiveValue value = pair.getSecondObject();

            if (key.equals(startTimePropertyName) && !value.isNull())
            {
                startTime = convertDate(value);
            }
            else if (startTime == null && uKey.equals(ArcConstants.ATTRIBUTE_DATE_STRING_KEY) && value != null)
            {
                startTime = parseDate(value.getValue());
            }
            else if (key.equals(myDownTimeFieldName) && value != null && !value.isNull())
            {
                endTime = convertDate(value);
            }
            else if (endTime == null && ("END_TIME_POSITION".equals(uKey) || "DOWN_TIME".equals(uKey)
                    || "INT_TIME_DOWN".equals(uKey) || "TIMEDOWN".equals(uKey)) && value != null)
            {
                endTime = parseDate(value.getValue());
            }
            else if (uKey.equals(ArcConstants.ATTRIBUTE_OBJECTID_KEY) && value != null)
            {
                try
                {
                    id = value.getLong();
                }
                catch (NumberFormatException e)
                {
                    id = 1;
                }
            }
            else if (id == 0 && uKey.equals(ArcConstants.ATTRIBUTE_ID_KEY) && value != null)
            {
                id = value.getLong();
            }
            else if (("DATA_POINT_COLOR".equals(uKey) || "COLOR".equals(uKey)) && value != null)
            {
                colorString = value.getValue().substring(1);
            }
        }

        TimeSpan elementTime = TimeSpan.spanOrPt(startTime, endTime);
        if (startTime != null)
        {
            if (info.getMetaDataInfo().getTimeKey() == null)
            {
                final String defaultTimeField = "TIME";
                info.getMetaDataInfo().addKey(defaultTimeField, String.class, null);
                info.getMetaDataInfo().setSpecialKey(defaultTimeField, TimeKey.DEFAULT, this);
                mdp.setValue(defaultTimeField, SimpleDateFormatHelper
                        .getSimpleDateFormat(ArcConstants.DATE_FORMAT_3, LOCAL_TIME_ZONE).format(startTime));
            }
            mgs.setTimeSpan(elementTime);
        }

        MapDataElement dataElement = new DefaultMapDataElement(id, elementTime, info, mdp, mgs);
        Color elementColor = determineElementColor(info, colorString);
        dataElement.getVisualizationState().setColor(elementColor);
        dataElement.getMapGeometrySupport().setColor(elementColor, null);
        return dataElement;
    }

    /**
     * Create a {@link MetaDataProvider} from a {@link MetaDataInfo} and list of
     * attributes.
     *
     * @param model - the {@link MetaDataInfo}
     * @param attributes - the attribute pair list.
     * @return {@link MetaDataProvider} imprinted with the properties.
     */
    private MetaDataProvider createMetaDataProvider(MetaDataInfo model, List<Pair<String, JSONSaxPrimitiveValue>> attributes)
    {
        MetaDataProvider mdp = new MDILinkedMetaDataProvider(model);

        // Process the attribute list
        for (Pair<String, JSONSaxPrimitiveValue> pair : attributes)
        {
            String key = pair.getFirstObject();
            JSONSaxPrimitiveValue value = pair.getSecondObject();
            if (value != null && value.isNull())
            {
                value = null;
            }

            if (model.hasKey(key))
            {
                mdp.setValue(key, value == null ? null : value.getValue());
            }
        }

        return mdp;
    }

    /**
     * Converts a esriGeometryPolygon into {@link MapDataElement} because the
     * polygon can have multiple rings ( i.e. multiple polygons ) each polygon
     * is build separately and put into the return list.
     *
     * @param type the {@link DataTypeInfo}
     * @param attributes the list of attributes to add to the converted polygon
     * @param pointListList the list of lists of lat/lon pairs for the
     *            individual polylines
     * @return a list of the polygons from the conversion.
     */
    private List<MapDataElement> createPolygons(DataTypeInfo type, List<Pair<String, JSONSaxPrimitiveValue>> attributes,
            List<List<LatLonAlt>> pointListList)
    {
        List<MapDataElement> polyList = New.list();
        try
        {
            MetaDataProvider mdp = createMetaDataProvider(type.getMetaDataInfo(), attributes);
            Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> polygons = groupInnerRings(pointListList);

            for (Entry<List<LatLonAlt>, Collection<List<LatLonAlt>>> entry : polygons.entrySet())
            {
                MapGeometrySupport mgs = new DefaultMapPolygonGeometrySupport(entry.getKey(), entry.getValue());
                MapDataElement poly = createMapDataElement(type, attributes, mdp, mgs);
                poly.getMapGeometrySupport().setFollowTerrain(true, null);
                polyList.add(poly);
            }
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Error converting feature to LivePolygon. Attributes: \n" + attributes, e);
        }
        return polyList;
    }

    /**
     * Converts a esriGeometryPolyline into {@link MapDataElement} because the
     * polyline can have multiple lines ( i.e. multiple polylines ) each
     * polyline is build separately and put into the return list.
     *
     * @param type - the {@link DataTypeInfo}
     * @param attributes - the feature attributes list
     * @param pointListList - the list of lists of lat/lon pairs for the
     *            individual polylines
     * @return a list of the tracks from the conversion.
     */
    private List<MapDataElement> createTracks(DataTypeInfo type, List<Pair<String, JSONSaxPrimitiveValue>> attributes,
            List<List<LatLonAlt>> pointListList)
    {
        List<MapDataElement> trackList = New.list();
        try
        {
            MetaDataProvider mdp = createMetaDataProvider(type.getMetaDataInfo(), attributes);
            // Make each path a separate track
            for (List<LatLonAlt> ptList : pointListList)
            {
                DefaultMapPolylineGeometrySupport mgs = new DefaultMapPolylineGeometrySupport(ptList);
                MapDataElement track = createMapDataElement(type, attributes, mdp, mgs);
                for (LatLonAlt ll : ptList)
                {
                    MapGeometrySupport pt = new DefaultMapPointGeometrySupport(ll);
                    pt.setColor(track.getMapGeometrySupport().getColor(), null);
                    mgs.addChild(pt);
                }
                track.getMapGeometrySupport().setFollowTerrain(true, null);
                trackList.add(track);
            }
        }
        catch (RuntimeException e)
        {
            trackList.clear();
            LOGGER.error("Error converting feature to LiveTrack(s). Attributes: \n" + attributes, e);
        }

        return trackList;
    }

    /**
     * Determine element color.
     *
     * @param info the info
     * @param jsonColor the json color
     * @return the color
     */
    private Color determineElementColor(DataTypeInfo info, String jsonColor)
    {
        Color aColor = ourDefaultColor;
        if (info.getBasicVisualizationInfo() != null && info.getBasicVisualizationInfo().getTypeColor() != null)
        {
            aColor = info.getBasicVisualizationInfo().getTypeColor();
        }
        else
        {
            try
            {
                String c = "0x" + jsonColor;
                aColor = Color.decode(c.toLowerCase());
            }
            catch (NumberFormatException e)
            {
                LOGGER.warn("Color parse error", e);
            }
        }
        return aColor;
    }

    /**
     * Determine geometry type.
     *
     * @param type the type
     * @return the geometry type
     */
    private EsriGeometryType determineGeometryType(String type)
    {
        if (type != null && !type.isEmpty())
        {
            return EsriGeometryType.valueOf(type);
        }
        return null;
    }

    /**
     * Decompose polygon based on closing positions.
     *
     * @param rings the input list of LatLonAlt.
     * @return the decomposed parts list of lists of lat lon alt.
     */
    private Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> groupInnerRings(List<List<LatLonAlt>> rings)
    {
        Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> polygons = New.map();

        Collection<List<LatLonAlt>> holes = null;
        for (List<LatLonAlt> polygon : rings)
        {
            // Exterior rings are defined in clockwise order and
            // inner rings are defined in counter-clockwise order.
            PolygonWinding winding = GeographicUtilities.getNaturalWinding(polygon);
            if (winding == PolygonWinding.CLOCKWISE)
            {
                holes = New.collection();
                polygons.put(polygon, holes);
            }
            else
            {
                if (holes != null)
                {
                    holes.add(polygon);
                }
                else
                {
                    LOGGER.error("Polygon hole listed before polygon or incorrect winding for polygon.");
                }
            }
        }

        return polygons;
    }

    /**
     * Utility function for extracting and converting dates as they are found in
     * the JSON response. Several formats are tried.
     *
     * @param val - the date string to parse
     * @return the Date or null if there is a problem.
     */
    private Date parseDate(String val)
    {
        Date aDate = null;
        try
        {
            aDate = DateTimeUtilities.parse(
                    SimpleDateFormatHelper.getSimpleDateFormat(ArcConstants.DATE_FORMAT_1, ArcConstants.TIME_ZONE_GMT00), val);
        }
        catch (ParseException e)
        {
            String time = val;
            try
            {
                aDate = DateTimeUtilities.parseISO8601Date(DateTimeUtilities.fixMillis(time));
            }
            catch (ParseException e2)
            {
                time = time.replace(" ", "");
                try
                {
                    aDate = DateTimeUtilities.parse(
                            SimpleDateFormatHelper.getSimpleDateFormat(ArcConstants.DATE_FORMAT_2, ArcConstants.TIME_ZONE_GMT00),
                            time);
                }
                catch (ParseException pe)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(pe, pe);
                    }
                }
            }

            LOGGER.warn("Failed parsing date " + val);
        }
        return aDate;
    }
}
