package io.opensphere.wfs.gml311;

import java.awt.Color;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.impl.specialkey.EndTimeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.wfs.consumer.FeatureConsumer;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wfs.util.WFSConstants;

/**
 * The Class GmlSaxFeatureResponseHandler.
 */
public class GmlSaxFeatureResponseHandler extends DefaultHandler
{
    /** The default feature color. */
    private static final Color DEFAULT_FEATURE_COLOR = Color.WHITE;

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(GmlSaxFeatureResponseHandler.class);

    /** GML tag that suggests the feature's color. */
    private static final String TAG_FEATURE_COLOR = "styleVariation";

    /** Sub-handler for parsing feature geometries. */
    private AbstractGmlGeometryHandler myCurrentGeometryHandler;

    /** The handler used to capture error output. */
    private final GmlErrorHandler myErrorHandler;

    /** The name of the property currently getting parsed. */
    private String myCurrentProperty;

    /** Current state. */
    private State myCurrentState = State.SEEK_FEATURE;

    /** The value of the property currently getting parsed. */
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private StringBuilder myCurrentValue;

    /** The Feature color. */
    private Color myFeatureColor = DEFAULT_FEATURE_COLOR;

    /** The feature id. */
    private long myFeatureId;

    /** Consumer of converted WFS features. */
    private final FeatureConsumer myConsumer;

    /** Current feature count. */
    private int myFeatureCount;

    /** The layer name used to identify where Feature tags start/stop. */
    private String myLayerNameTag;

    /** Handler for MetaData (column) data. */
    private final MetaDataHandler myMetaDataHandler;

    /** Handler for GML Time data (separate from Time columns in MetaData). */
    private final TimeHandler myTimeHandler;

    /** The DataTypeInfo for the type being requested. */
    private final WFSDataType myType;

    /**
     * Instantiates a new SAX handler for reading GML-formatted WFS responses.
     *
     * @param type the WFS DataTypeInfo for the requested layer
     * @param consumer the consumer of the converted features
     */
    public GmlSaxFeatureResponseHandler(WFSDataType type, FeatureConsumer consumer)
    {
        Utilities.checkNull(type, WFSDataType.class.getSimpleName());
        Utilities.checkNull(type.getMetaDataInfo(), MetaDataInfo.class.getSimpleName());
        Utilities.checkNull(consumer, "consumer");
        myType = type;
        myMetaDataHandler = new MetaDataHandler(type.getMetaDataInfo());
        // TODO bug in the handler: must reset in order to instantiate the internal provider field:
        myMetaDataHandler.reset();
        myErrorHandler = new GmlErrorHandler();
        myTimeHandler = new TimeHandler();
        myConsumer = consumer;
    }

    /**
     * Tests to determine if the response handler parsed an error.
     *
     * @return true if the handler parsed an error, false otherwise.
     */
    public boolean isInError()
    {
        return myCurrentState.equals(State.ERROR);
    }

    /**
     * Gets the errors collected from the parsed document.
     *
     * @return the errors collected from the parsed document.
     */
    public Collection<GmlExceptionReport> getErrors()
    {
        return myErrorHandler.getExceptionReports();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Adding characters for " + myCurrentProperty);
        }
        myCurrentValue.append(ch, start, length);
    }

    @Override
    public void endDocument()
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Ending document");
        }
        myConsumer.flush();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Ending element " + localName);
        }

        if (myCurrentState.equals(State.ERROR))
        {
            myErrorHandler.handleClosingTag(localName, myCurrentValue.toString());
        }
        else if (myCurrentState.equals(State.COLLECT_GEOMETRY))
        {
            if (myCurrentGeometryHandler.getTagName().equals(localName))
            {
                myCurrentState = State.SEEK_FEATURE;
            }
            else
            {
                myCurrentGeometryHandler.handleClosingTag(localName, myCurrentValue.toString());
            }
        }
        else if (myCurrentState.equals(State.COLLECT_TIME))
        {
            if (myTimeHandler.handlesTag(localName))
            {
                myCurrentState = State.SEEK_FEATURE;
            }
            else
            {
                myTimeHandler.handleTimeData(localName, myCurrentValue.toString());
            }
        }
        else if (Objects.equals(myLayerNameTag, localName))
        {
            myCurrentState = State.SEEK_FEATURE;
            if (myCurrentGeometryHandler != null)
            {
                addFeature();
            }
        }
        else if (TAG_FEATURE_COLOR.equals(localName))
        {
            try
            {
                String c = myCurrentValue.toString().substring(1);
                c = StringUtilities.concat("0x", c);
                myFeatureColor = Color.decode(c.toLowerCase());
            }
            catch (NumberFormatException e)
            {
                LOGGER.warn("Failed to parse color [" + myCurrentValue + "] in GML feature.", e);
            }
        }
        else if (myType.getMetaDataInfo().hasKey(localName))
        {
            myMetaDataHandler.handleMetaData(localName, myCurrentValue.toString());
        }
    }

    /**
     * Get the count of translated features.
     *
     * @return the count of translated features
     */
    public int getProcessedCount()
    {
        return myFeatureCount;
    }

    /**
     * Reset the state between features.
     */
    public void resetFeatureState()
    {
        myMetaDataHandler.reset();
        myTimeHandler.reset();
        myFeatureId = 0;
        myFeatureColor = Color.WHITE;
    }

    @Override
    public void startDocument() throws SAXException
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Starting document");
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Starting element " + localName);
        }

        if (myCurrentState.equals(State.ERROR))
        {
            myErrorHandler.handleOpeningTag(localName);
        }
        else if (myCurrentState.equals(State.SEEK_GEOMETRY))
        {
            // if we changed features, reset everything:
            myCurrentGeometryHandler = GeometryHandlerFactory.getGeometryHandler(localName, myType.isLatBeforeLon());
            myCurrentState = myCurrentGeometryHandler != null ? State.COLLECT_GEOMETRY : State.SEEK_FEATURE;
        }
        else if (myCurrentState.equals(State.COLLECT_GEOMETRY))
        {
            myCurrentGeometryHandler.handleOpeningTag(localName);
        }
        else if (attributes.getValue("gml:id") != null)
        {
            String idString = attributes.getValue("gml:id");
            try
            {
                // TODO: The ID in the features is currently a long, but
                // long-term should be changed to a string (not all servers
                // will follow this naming convention).
                myFeatureId = Long.parseLong(idString.substring(idString.lastIndexOf('.') + 1));
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Found ID[" + myLayerNameTag + "][" + myFeatureId + "]");
                }
            }
            catch (NumberFormatException e)
            {
                myFeatureId = 0;
                LOGGER.debug("Received non numeric feature ID: " + idString);
            }
            myLayerNameTag = localName;
            resetFeatureState();
        }
        else if (myTimeHandler.handlesTag(localName))
        {
            myCurrentState = State.COLLECT_TIME;
        }
        else if (myType.getMetaDataInfo().getGeometryColumn().equals(localName))
        {
            myCurrentState = State.SEEK_GEOMETRY;
        }
        else if (myErrorHandler.handlesTag(localName))
        {
            myCurrentState = State.ERROR;
            myErrorHandler.handleOpeningTag(localName);
        }

        myCurrentProperty = localName;
        myCurrentValue = new StringBuilder();
    }

    /**
     * Creates a feature from the collected items in this class and adds it to the list of finished features.
     */
    private void addFeature()
    {
        MapGeometrySupport mgs = myCurrentGeometryHandler.getGeometry();
        MetaDataProvider mdp = myMetaDataHandler.getMetaDataProvider();
        TimeSpan span = buildTimeSpan(mdp);
        mgs.setTimeSpan(span);
        MapDataElement element = new DefaultMapDataElement(myFeatureId, span, myType, mdp, mgs);
        Color typeColor = myType.getBasicVisualizationInfo().getTypeColor().equals(DEFAULT_FEATURE_COLOR) ? myFeatureColor
                : myType.getBasicVisualizationInfo().getTypeColor();
        element.getVisualizationState().setColor(typeColor);
        myConsumer.addFeature(element);
        myFeatureCount++;
    }

    /**
     * Builds the time span.
     *
     * @param mdp The MetaDataProvider with all the column data in it.
     *
     * @return the time span
     */
    private TimeSpan buildTimeSpan(MetaDataProvider mdp)
    {
        if (!myType.getBasicVisualizationInfo().getSupportedLoadsToTypes().contains(LoadsTo.TIMELINE))
        {
            return TimeSpan.TIMELESS;
        }

        TimeSpan returnSpan = null;

        // First check for a special key that, for some layers, holds a special
        // end time with no date attached.
        String xtraSpecialKey = "TIMEDOWN";
        Object xtraSpecialObj = mdp.getValue(xtraSpecialKey);
        if (xtraSpecialObj instanceof String && StringUtils.isNotEmpty((String)xtraSpecialObj))
        {
            returnSpan = myTimeHandler.resolveTimeWithEndDay((String)xtraSpecialObj);
        }
        else
        {
            // Otherwise, pass in the start/stop from the MetaData and let the
            // TimeManager sort it out.
            String startKey = myType.getMetaDataInfo().getKeyForSpecialType(TimeKey.DEFAULT);
            String endKey = myType.getMetaDataInfo().getKeyForSpecialType(EndTimeKey.DEFAULT);
            Object startObj = mdp.getValue(startKey);
            Object endObj = mdp.getValue(endKey);
            String startString = startObj instanceof String && StringUtils.isNotEmpty((String)startObj) ? (String)startObj
                    : null;
            String endString = endObj instanceof String && StringUtils.isNotEmpty((String)endObj) ? (String)endObj : null;
            returnSpan = myTimeHandler.resolveTimes(startString, endString);
        }

        if (((WFSMetaDataInfo)myType.getMetaDataInfo()).isDynamicTime())
        {
            mdp.setValue(WFSConstants.DEFAULT_TIME_FIELD, returnSpan);
        }
        return returnSpan;
    }

    /** Enum used to track the current parse State. */
    private enum State
    {
        /** State while processing a Geometry element. */
        COLLECT_GEOMETRY,

        /** State while processing a time element. */
        COLLECT_TIME,

        /** State while waiting for a Feature element. */
        SEEK_FEATURE,

        /** State while waiting for the next Geometry Tag. */
        SEEK_GEOMETRY,

        /** State used when an error is reported as the response. */
        ERROR,
    }
}
