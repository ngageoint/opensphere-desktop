package io.opensphere.wfs.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.factory.AvroTimeHelper;
import io.opensphere.mantle.data.element.mdfilter.OGCFilterParameters;
import io.opensphere.server.control.DefaultServerDataGroupInfo;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.util.OGCOutputFormat;
import io.opensphere.server.util.OGCServerConnector;
import io.opensphere.server.util.OGCServerException;
import io.opensphere.wfs.config.WFSServerConfig;
import io.opensphere.wfs.config.WFSServerConfig.WFSServerState;
import io.opensphere.wfs.consumer.FeatureConsumer;
import io.opensphere.wfs.filter.WFSTimeFieldGetter;
import io.opensphere.wfs.gml311.GmlSaxFeatureResponseHandler;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.placenames.PlaceNameLayerManager;
import io.opensphere.wfs.placenames.PlaceNameTile;
import io.opensphere.wfs.placenames.PlaceNamesRequestEvent;
import io.opensphere.wfs.util.WFSPreferenceUtilities;
import net.opengis.ows._100.DomainType;
import net.opengis.ows._100.Operation;
import net.opengis.ows._100.OperationsMetadata;
import net.opengis.ows._100.RequestMethodType;
import net.opengis.wfs._110.WFSCapabilitiesType;

/**
 * Envoy that queries and retrieves data from a WFS server.
 */
@SuppressWarnings("PMD.GodClass")
public class WFSEnvoy extends AbstractWFSEnvoy
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WFSEnvoy.class);

    /**
     * Each point takes ~50 chars so set max points to maximum URL size, minus
     * enough for a large URL without the points, divided by 50 chars.
     */
    private static final int MAX_POINTS = (8000 - 500) / 50;

    /**
     * The preferred output format at the server level. This is used by any
     * layer that does not specify its own list of output formats.
     */
    private OGCOutputFormat myDefaultOutputFormat = OGCOutputFormat.GML_311;

    /** A flag indicating whether this server supports HTTP Post. */
    private boolean myIsGetFeaturePostEnabled;

    /** Listener for events which contain place names to publish. */
    private final EventListener<PlaceNamesRequestEvent> myPlaceNamesEventListener;

    /** The Place names manager. */
    private final PlaceNameLayerManager myPlaceNamesManager;

    /** Task activity that tracks placenames requests. */
    private final PlacenamesTaskActivity myPlacenamesTaskActivity;

    /** The WFS preferences. */
    private final Preferences myPreferences;

    /** The WFS GetCapabilities object. */
    private WFSCapabilitiesType myWfsCapabilities;

    /**
     * The helper class used by the envoy to perform additional processing.
     */
    private final WFSEnvoyHelper myEnvoyHelper;

    /**
     * Construct the envoy.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     * @param preferences The WFS preferences.
     * @param wfsConn The parameters used to connect to an OGC server URL.
     * @param tools Collection of WFS tools.
     */
    public WFSEnvoy(Toolbox toolbox, Preferences preferences, ServerConnectionParams wfsConn, WFSTools tools)
    {
        super(toolbox, wfsConn, tools);
        LOGGER.debug("Creating new WFS Envoy.");
        myPreferences = preferences;
        myPlaceNamesManager = tools.getPlaceNamesManager();
        myPlacenamesTaskActivity = new PlacenamesTaskActivity();
        EventManager eventManager = getToolbox().getEventManager();
        if (eventManager != null)
        {
            myPlaceNamesEventListener = event -> retrievePlacenameData(event.getTile());
            eventManager.subscribe(PlaceNamesRequestEvent.class, myPlaceNamesEventListener);
        }
        else
        {
            myPlaceNamesEventListener = null;
        }

        myEnvoyHelper = toolbox.getPluginToolboxRegistry().getPluginToolbox(WFSToolbox.class).getEnvoyHelper();
    }

    @Override
    public synchronized void close()
    {
        EventManager eventManager = getToolbox().getEventManager();
        if (eventManager != null && myPlaceNamesEventListener != null)
        {
            eventManager.unsubscribe(PlaceNamesRequestEvent.class, myPlaceNamesEventListener);
        }

        super.close();

        myPlaceNamesManager.removeServer(getServerConfig().getServerTitle());

        myWfsCapabilities = null;
    }

    @Override
    public synchronized void open()
    {
        super.open();

        /* If a server has a layer with the place names keyword, consider it a
         * place names provider and add it to the corresponding set of
         * providers. */
        for (WFSDataType type : getDataTypes())
        {
            for (String tag : type.getTags())
            {
                if (tag.equalsIgnoreCase(PlaceNameLayerManager.SOURCE))
                {
                    myPlaceNamesManager.addServer(getServerConfig().getServerTitle());
                    break;
                }
            }
        }

        getToolbox().getUIRegistry().getMenuBarRegistry().addTaskActivity(myPlacenamesTaskActivity);
    }

    @SuppressWarnings("PMD.SimplifiedTernary")
    @Override
    protected boolean activateLayers(Collection<WFSDataType> typesToActivate) throws InterruptedException
    {
        boolean addedDescribeFeatureData = true;
        try
        {
            describeFeatures(typesToActivate);

            /* Since the data type has been altered by the describe features,
             * set the type sync (whatever that is). */
            for (WFSDataType dataType : typesToActivate)
            {
                if (dataType.getParent() instanceof DefaultServerDataGroupInfo)
                {
                    DefaultServerDataGroupInfo serverGroup = (DefaultServerDataGroupInfo)dataType.getParent();
                    dataType.setTimeExtents(null, false);
                    dataType.setTypeSync(serverGroup.getTypeSync());
                }
            }
        }
        catch (OGCServerException e)
        {
            String error = "Failed to parse layer information from server [" + getServerConfig().getWfsUrl() + "]: "
                    + e.getMessage();
            UserMessageEvent.error(getToolbox().getEventManager(), error);
            addedDescribeFeatureData = false;
        }

        return addedDescribeFeatureData ? super.activateLayers(typesToActivate) : false;
    }

    @Override
    protected int getFeatures(WFSDataType wfsType, TimeSpan timeSpan, Geometry geometry, DataFilter userFilter)
        throws OGCServerException
    {
        OGCOutputFormat format = getOutputFormat(wfsType);

        OGCFilterParameters filterParams = new OGCFilterParameters();
        filterParams.setLatBeforeLon(wfsType.isLatBeforeLon());
        filterParams.setRegion(geometry.buffer(0));
        filterParams.setTimeSpan(timeSpan);
        filterParams.setUserFilter(userFilter == null ? null
                : userFilter.applyFieldNameTransform(WFSTimeFieldGetter.getTimeFieldTransform(wfsType)));
        filterParams.setSrs(getServerConfig().getServerCustomization().getSrsName());
        filterParams.setOutputFormat(format.getFormatString());
        int maxFeatures = WFSPreferenceUtilities.getMaxFeaturesFromPreferences(myPreferences);
        filterParams.setMaxFeatures(BigInteger.valueOf(maxFeatures));
        filterParams.setGeometryTagName(wfsType.getMetaDataInfo().getGeometryColumn());
        filterParams.setTimeFieldNames(WFSTimeFieldGetter.getTimeFieldNames(wfsType));

        URL url;
        InputStream postRequest = null;
        try
        {
            if (myIsGetFeaturePostEnabled)
            {
                String urlString = getServerConfig().getWfsUrl();
                if (format.isStreaming())
                {
                    urlString = StringUtilities.concat(urlString, "?STREAMING=", Boolean.toString(true));
                }
                url = new URL(urlString);
                postRequest = myEnvoyHelper.buildPostQuery(filterParams, wfsType);
            }
            else
            {
                /* Format query as an HTTP GET request Simplify the geometry to
                 * avoid exceeding the HTTP GET character limit. */
                filterParams.setRegion(simplifyGeometry(geometry.buffer(0)));
                url = myEnvoyHelper.buildGetFeatureURL(filterParams, wfsType, getServerConfig(), myPreferences);
            }
        }
        catch (MalformedURLException e)
        {
            LOGGER.warn("Failed to build URL for feature request to server: " + e, e);
            return -1;
        }
        catch (UnsupportedEncodingException e)
        {
            LOGGER.warn("Failed to properly encode URL for feature request to server: " + e, e);
            return -1;
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Requesting features from server using URL: " + url);
        }

        OGCServerConnector connector = getConnector(url, postRequest, getToolbox().getServerProviderRegistry());
        return getFeatures(wfsType, connector, format);
    }

    /**
     * Gets the output format for the data type.
     *
     * @param wfsType the data type
     * @return the output format
     */
    protected OGCOutputFormat getOutputFormat(WFSDataType wfsType)
    {
        OGCOutputFormat format = OGCOutputFormat.getDefaultFormat();
        // Serialized objects are not supported in OpenSphere
        if (wfsType.getOutputFormat() != null && wfsType.getOutputFormat() != OGCOutputFormat.JAVA_OBJECT
                && wfsType.getOutputFormat() != OGCOutputFormat.STREAMING_FEATURE_JAVA_OBJECT)
        {
            format = wfsType.getOutputFormat();
        }
        return format;
    }

    /**
     * Gets features.
     *
     * @param wfsType the {@link WFSDataType} for the layer
     * @param connector a configured connection to the server
     * @param format the output format string for the request
     * @return a count of {@link MapDataElement}s returned from the server
     * @throws OGCServerException the server exception
     */
    protected int getFeatures(WFSDataType wfsType, OGCServerConnector connector, OGCOutputFormat format) throws OGCServerException
    {
        switch (format)
        {
            case AVRO_DEFLATE:
            case AVRO:
                return getAvroStreaming(wfsType, connector);
            case AVRO_B_DEFLATE:
            case AVRO_B:
                return getAvroFeatures(wfsType, connector);
            case GML_311:
                return getFeaturesGml(wfsType, connector, format.getFormatString());
            default:
                return getFeaturesGml(wfsType, connector, format.getFormatString());
        }
    }

    @Override
    protected void populateWFSServerConfig(WFSServerConfig wfsServer) throws OGCServerException
    {
        myWfsCapabilities = getCapabilitiesDoc(getServerConfig());

        OperationsMetadata operationsMetadata = myWfsCapabilities.getOperationsMetadata();
        List<Operation> opsList = operationsMetadata == null ? Collections.<Operation>emptyList()
                : operationsMetadata.getOperation();
        Operation getFeatureOperation = StreamUtilities.filterOne(opsList, op -> op.getName().equals("GetFeature"));

        if (getFeatureOperation != null)
        {
            for (DomainType dt : getFeatureOperation.getParameter())
            {
                if (dt.getName().equals("OutputFormat"))
                {
                    myDefaultOutputFormat = OGCOutputFormat.getPreferredFormat(dt.getValue());
                }
            }

            for (JAXBElement<RequestMethodType> getOrPost : getFeatureOperation.getDCP().get(0).getHTTP().getGetOrPost())
            {
                if (getOrPost.getName().getLocalPart().equals("Post"))
                {
                    myIsGetFeaturePostEnabled = true;
                }
            }
        }

        wfsServer.setServerId(getServerConfig().getServerId(OGCServerSource.WFS_SERVICE));
        wfsServer.setServerTitle(getServerTitle());
        wfsServer.setLayers(myEnvoyHelper.buildWFSTypes(myWfsCapabilities, getToolbox(), getServerConfig(), getColumnManager(),
                myDefaultOutputFormat));
        wfsServer.setServerState(WFSServerState.ACTIVE);
    }

    /**
     * Request the describe features service of the server for the given
     * {@link WFSDataType}s and update the data types with the data returned.
     *
     * @param dataTypes The set of WFS types.
     * @throws OGCServerException If communication with the server fails.
     * @throws InterruptedException If the thread is interrupted.
     */
    protected void describeFeatures(Collection<WFSDataType> dataTypes) throws OGCServerException, InterruptedException
    {
        long parseStart = 0L;
        if (LOGGER.isDebugEnabled())
        {
            parseStart = System.nanoTime();
        }

        for (WFSDataType dataType : dataTypes)
        {
            ThreadControl.check();
            try
            {
                URL url = myEnvoyHelper.buildDescribeFeatureTypeURL(getServerConfig(), dataType);
                Set<Document> describeDocs = getDescribeFeatureDocs(url, dataType);
                myEnvoyHelper.addDescribeFeatureDataToType(dataType, describeDocs);
            }
            catch (MalformedURLException e)
            {
                throw new OGCServerException(e.getMessage(), e);
            }
        }

        if (LOGGER.isDebugEnabled())
        {
            long parseEnd = System.nanoTime();
            LOGGER.debug(StringUtilities.formatTimingMessage("Time to request " + dataTypes.size() + " describeFeatures: ",
                    parseEnd - parseStart));
        }
    }

    /**
     * Gets the capabilities doc.
     *
     * @param serverCfg the server cfg
     * @return the capabilities doc
     * @throws OGCServerException the server exception
     */
    protected WFSCapabilitiesType getCapabilitiesDoc(ServerConnectionParams serverCfg) throws OGCServerException
    {
        URL url;
        try
        {
            url = myEnvoyHelper.buildGetCapabilitiesURL(serverCfg);
        }
        catch (MalformedURLException e)
        {
            throw new OGCServerException("Invalid URL: " + getServerConfig().getWfsUrl(), e);
        }
        OGCServerConnector connector = getConnector(url, getToolbox().getServerProviderRegistry());
        WFSCapabilitiesType wfsCapabilities = connector.requestObject(WFSCapabilitiesType.class);
        if (wfsCapabilities == null)
        {
            throw new OGCServerException("Failed to retrieve WFS Capabilities for ["
                    + getServerConfig().getServerId(OGCServerSource.WFS_SERVICE) + "]", null);
        }
        return wfsCapabilities;
    }

    /**
     * Gets the describe feature docs.
     *
     * @param serverUrl the server url
     * @param dataType the data type
     * @return the describe feature docs
     * @throws OGCServerException the server exception
     * @throws MalformedURLException the malformed url exception
     */
    protected Set<Document> getDescribeFeatureDocs(URL serverUrl, WFSDataType dataType)
        throws OGCServerException, MalformedURLException
    {
        Set<Document> docSet = New.set();
        OGCServerConnector connector = getConnector(serverUrl, getToolbox().getServerProviderRegistry());
        Document doc = connector.requestDocument();

        if (LOGGER.isTraceEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("describeFeatures response from server [").append(serverUrl).append("]: [");
            sb.append(XMLUtilities.format(doc));
            sb.append(']');
            LOGGER.trace(sb.toString());
        }

        // If there was an exception, try requesting each layer individually
        NodeList exceptions = doc.getElementsByTagNameNS("*", "Exception");
        if (exceptions != null && exceptions.getLength() > 0)
        {
            throw new OGCServerException("Describe feature failed for " + dataType.getDisplayName(), new IOException());
        }
        else
        {
            docSet.add(doc);

            // Check for nested DescribeFeatureType docs
            NodeList imports = doc.getElementsByTagNameNS("*", "import");
            for (int i = 0; i < imports.getLength(); i++)
            {
                String schemaLocation = imports.item(i).getAttributes().getNamedItem("schemaLocation").getNodeValue();
                if (schemaLocation.toLowerCase().contains("request=describefeaturetype"))
                {
                    docSet.addAll(getDescribeFeatureDocs(new URL(schemaLocation), dataType));
                }
            }
        }
        return docSet;
    }

    /**
     * Request features via the GML 3.1.1 interface.
     *
     * @param wfsType the {@link WFSDataType} for the layer
     * @param connector a configured connection to the server
     * @param format the output format string for the request
     * @return a count of {@link MapDataElement}s returned from the server
     * @throws OGCServerException the server exception
     */
    private int getFeaturesGml(WFSDataType wfsType, OGCServerConnector connector, String format) throws OGCServerException
    {
        InputStream is = connector.requestStream();
        long t0 = System.nanoTime();
        int count = -1;

        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();

            // parse the WFS response
            FeatureConsumer consumer = getConsumerManager().requestConsumer(wfsType, true);
            GmlSaxFeatureResponseHandler handler = new GmlSaxFeatureResponseHandler(wfsType, consumer);
            is = new StreamReader(is).copyStream(System.out);
            saxParser.parse(is, handler);
            // consumer.flush();
            count = handler.getProcessedCount();
        }
        catch (ParserConfigurationException e)
        {
            LOGGER.warn("Failed to create GML parser for feature type [" + format + "]: " + e, e);
        }
        catch (SAXException e)
        {
            LOGGER.warn("Failed to parse features from server for layer [" + wfsType.getTypeName() + "], server format [" + format
                    + "]: " + e, e);
        }
        catch (IOException e)
        {
            LOGGER.warn("Failure while reading features from server connection for layer [" + wfsType.getTypeName()
                    + "], server format [" + format + "]: " + e, e);
        }

        if (LOGGER.isInfoEnabled())
        {
            long t1 = System.nanoTime();
            LOGGER.info(StringUtilities.formatTimingMessage(
                    "Retrieved " + count + " objects of type [" + wfsType.getDisplayName() + "] from server in ", t1 - t0));
        }
        return count;
    }

    /**
     * Get a batch of Avro records and stuff them into the layer.
     *
     * @param type the layer
     * @param connector a connector
     * @return the number of records inserted
     */
    private int getAvroFeatures(WFSDataType type, OGCServerConnector connector)
    {
        try
        {
            DataFileStream<GenericRecord> dataFileStream = connector.requestAvro();
            if (dataFileStream == null)
            {
                return -1;
            }
            AvroTimeHelper help = new AvroTimeHelper(type);
            try
            {
                List<MapDataElement> elements = new LinkedList<>();
                while (dataFileStream.hasNext())
                {
                    elements.add(myEnvoyHelper.createDataElement(dataFileStream.next(), help));
                }
                FeatureConsumer featureConsumer = getConsumerManager().requestConsumer(type, true);
                featureConsumer.addFeatures(elements);
                featureConsumer.flush();
                return elements.size();
            }
            finally
            {
                dataFileStream.close();
            }
        }
        catch (IOException | OGCServerException e)
        {
            LOGGER.error(e);
            return -1;
        }
    }

    /**
     * Parse a stream of Avro records for inclusion in the WFSDataType.
     *
     * @param type the layer
     * @param conn connector
     * @return the number of data elements inserted
     * @throws OGCServerException if cancelled or any kind of error occurs
     */
    private int getAvroStreaming(WFSDataType type, OGCServerConnector conn) throws OGCServerException
    {
        CancellableInputStream in = conn.requestStream();
        if (in == null)
        {
            return -1;
        }

        int count = 0;
        boolean cancelled = false;
        FeatureConsumer featureConsumer = getConsumerManager().requestConsumer(type, true);
        AvroTimeHelper help = new AvroTimeHelper(type);
        try
        {
            DataFileStream<GenericRecord> data = OGCServerConnector.avroDataStream(in);
            ThreadControl.check();
            while (data.hasNext())
            {
                MapDataElement element = myEnvoyHelper.createDataElement(data.next(), help);
                element.getMapGeometrySupport().setFollowTerrain(true, type);
                featureConsumer.addFeature(element);
                count++;
                ThreadControl.check();
            }
        }
        catch (InterruptedException eek)
        {
            cancelled = true;
            throw new OGCServerException("Cancelled streaming " + type.getDisplayName(), eek);
        }
        catch (IOException eek)
        {
            if (in.isCancelled())
            {
                throw new OGCServerException("Cancelled streaming " + type.getDisplayName(), eek);
            }
            throw new OGCServerException("Failed streaming " + type.getDisplayName(), eek);
        }
        catch (RuntimeException eek)
        {
            throw new OGCServerException("Anomaly occurred while streaming " + type.getDisplayName(), eek);
        }
        finally
        {
            if (!cancelled && !in.isCancelled())
            {
                featureConsumer.flush();
            }
            IOUtils.closeQuietly(in);
        }

        return count;
    }

    /**
     * Gets the server title from the saved off WFS capabilities.
     *
     * @return the server title
     */
    private String getServerTitle()
    {
        return myWfsCapabilities.getServiceIdentification() != null ? myWfsCapabilities.getServiceIdentification().getTitle()
                : null;
    }

    /**
     * Retrieve the place name data for this tile. This method will cache the
     * data once retrieved and provide the data back to the PlaceNameTile.
     *
     * @param placeNameTile Tile for which to retrieve data.
     */
    protected void retrievePlacenameData(final PlaceNameTile placeNameTile)
    {
        if (placeNameTile.getServerName().equals(getServerConfig().getServerTitle()))
        {
            getExecutor().execute(new Runnable()
            {
                @Override
                public void run()
                {
                    DataRegistry dataRegistry = getToolbox().getDataRegistry();
                    if (dataRegistry == null)
                    {
                        return;
                    }

                    try
                    {
                        myPlacenamesTaskActivity.increment();
                        WFSEnvoyPlaceNameHelper.retrievePlaceNameData(getServerURL(), toString(), placeNameTile, dataRegistry,
                                getToolbox());
                    }
                    finally
                    {
                        myPlacenamesTaskActivity.decrement();
                    }
                }
            });
        }
    }

    /**
     * Simplify a geometry (polygon) to have a maximum number of vertices. This
     * is only required for HTTP "Get" requests, "Post" requests do not have a
     * length limitation.
     *
     * @param inputGeometry the geometry to simplify
     * @return the new, simplified geometry
     */
    private Geometry simplifyGeometry(Geometry inputGeometry)
    {
        Geometry finalGeom = inputGeometry;
        while (finalGeom.getNumPoints() > MAX_POINTS)
        {
            final double simplificationFactor = 0.001;
            finalGeom = TopologyPreservingSimplifier.simplify(finalGeom, simplificationFactor);
            LOGGER.warn("Simplified request geometry from " + inputGeometry.getNumPoints() + " to " + finalGeom.getNumPoints()
                    + " points.");
        }

        return finalGeom;
    }

    /**
     * Extension of {@link TaskActivity} that counts concurrent placenames
     * requests.
     */
    private static class PlacenamesTaskActivity extends TaskActivity
    {
        /** The number of concurrent requests. */
        private int myCount;

        /** Decrement the number of requests. */
        public synchronized void decrement()
        {
            if (--myCount == 0)
            {
                setActive(false);
            }
            setLabelValue();
        }

        /** Increment the number of requests. */
        public synchronized void increment()
        {
            if (myCount++ == 0)
            {
                setActive(true);
            }
            setLabelValue();
        }

        /** Set the label value based on the current count. */
        private void setLabelValue()
        {
            setLabelValue(new StringBuilder(32).append("Place Name Downloads: ").append(myCount).toString());
        }
    }
}
