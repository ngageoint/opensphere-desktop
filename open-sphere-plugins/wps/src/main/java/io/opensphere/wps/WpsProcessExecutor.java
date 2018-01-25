package io.opensphere.wps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import io.opensphere.core.Notify;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wfs.consumer.FeatureConsumer;
import io.opensphere.wfs.consumer.FeatureConsumerManager;
import io.opensphere.wfs.gml311.CapturingHandler;
import io.opensphere.wfs.gml311.CompoundXmlHandler;
import io.opensphere.wfs.gml311.GmlExceptionReport;
import io.opensphere.wfs.gml311.GmlSaxFeatureResponseHandler;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wps.envoy.WpsUrlHelper;
import io.opensphere.wps.layer.LayerConfigurer;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.util.ServerException;
import io.opensphere.wps.util.WpsServerConnectionHelper;

/**
 * A driver class, in which a WPS process instance is executed against a server,
 * and the results processed. Instances of this class are associated with a
 * single server, but may execute many processes. Every process executed by an
 * instance of this class will be executed against the server configured upon
 * instantiation.
 */
public class WpsProcessExecutor
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(WpsProcessExecutor.class);

    /**
     * The connection parameters describing the remote server.
     */
    private final ServerConnectionParams myConnectionParams;

    /**
     * The toolbox through which application interaction occurs.
     */
    private final Toolbox myToolbox;

    /**
     * Manager used to retrieve feature consumers.
     */
    private final FeatureConsumerManager myConsumerManager;

    /**
     * The factory with which SAX Parser instances are created.
     */
    private final SAXParserFactory myParserFactory;

    /**
     * Configures the layer so data can be shown properly for it.
     */
    private final LayerConfigurer myLayerConfigurer;

    /**
     * Creates a execute process request stream.
     */
    private final PayloadCreator myPayloadCreator = new PayloadCreator();

    /**
     * Creates a new process executor, configured to submit the process to the
     * supplied server.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     * @param pConnectionParams the connection parameters describing the remote
     *            server.
     */
    public WpsProcessExecutor(Toolbox pToolbox, ServerConnectionParams pConnectionParams)
    {
        if (pConnectionParams == null)
        {
            throw new IllegalArgumentException("Unable to configure process executor: connection descriptor is null.");
        }
        myToolbox = pToolbox;
        myConnectionParams = pConnectionParams;
        myLayerConfigurer = new LayerConfigurer(myToolbox);
        myConsumerManager = new FeatureConsumerManager(MantleToolboxUtils.getMantleToolbox(pToolbox), pToolbox.getTimeManager());

        myParserFactory = SAXParserFactory.newInstance();
        myParserFactory.setNamespaceAware(true);
    }

    /**
     * Executes the supplied process against the configured server, and
     * associates the results with the {@link WFSDataType} supplied with the
     * supplied configuration.
     *
     * @param pConfiguration the configuration of the process to execute.
     */
    public void execute(WpsProcessConfiguration pConfiguration)
    {
        try (WpsQueryTracker queryTracker = new WpsQueryTracker())
        {
            queryTracker.setLabelValue("Executing " + pConfiguration.getProcessIdentifier());
            queryTracker.setActive(true);
            queryTracker.setProgress(0);
            myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(queryTracker);

            WFSDataType resultType = pConfiguration.getResultType();
            URL url = WpsUrlHelper.buildExecuteProcessUrl(pConfiguration.getServerId(), pConfiguration.getProcessIdentifier());
            WpsServerConnectionHelper wpsServerConnectionHelper = new WpsServerConnectionHelper(url, myToolbox);

            try (InputStream response = executeStreamQuery(pConfiguration, wpsServerConnectionHelper, resultType))
            {
                processResponse(pConfiguration, resultType, response);
            }
            catch (QueryException e)
            {
                String message = "Unable to execute WPS Process on remote server.";
                Notify.error(message);
                LOG.error(message, e);
            }
            catch (ParserConfigurationException | SAXException | IOException e)
            {
                Notify.error("Unable to process response from server for " + pConfiguration.getProcessTitle());
                LOG.error("Unable to parse WPS Process response.", e);
            }
            queryTracker.setComplete(true);
            myToolbox.getUIRegistry().getMenuBarRegistry().removeTaskActivity(queryTracker);
        }
    }

    /**
     * Processes the supplied configuration response.
     *
     * @param pConfiguration the configuration used to determine how to process
     *            the response.
     * @param pResultType the data type to apply to the values in the response.
     * @param pResponse the input stream from which the response is read.
     * @throws SAXException if the XML data from the response cannot be
     *             processed.
     * @throws IOException if the response cannot be read.
     * @throws ParserConfigurationException if the XML parser cannot be
     *             configured.
     */
    protected void processResponse(WpsProcessConfiguration pConfiguration, WFSDataType pResultType, InputStream pResponse)
        throws SAXException, IOException, ParserConfigurationException
    {
        DataGroupInfo group = myLayerConfigurer.configureLayer(pConfiguration, pResultType);
        // parse the WFS response
        FeatureConsumer consumer = myConsumerManager.requestConsumer(pResultType, false);
        SAXParser saxParser = myParserFactory.newSAXParser();

        pResultType.registerInUse(MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController(), true);

        GmlSaxFeatureResponseHandler handler = new GmlSaxFeatureResponseHandler(pResultType, consumer);
        CapturingHandler logger = new CapturingHandler();
        CompoundXmlHandler compoundHandler = new CompoundXmlHandler(New.list(handler, logger));
        saxParser.parse(pResponse, compoundHandler);

        if (handler.isInError())
        {
            processErrorResponse(handler);
        }
        else
        {
            processSuccessResponse(handler, pResultType, group);
        }
    }

    /**
     * Examines the supplied response handler, updating the supplied data group
     * with information extracted from the response. The supplied
     * {@link WFSDataType} (to which results should be bound) is then associated
     * with the supplied data group, and made active.
     *
     * @param pHandler the handler to examine, with which results were
     *            processed.
     * @param pResultType the {@link WFSDataType} to which results are bound.
     * @param pGroup the data group with which the result data type will be
     *            associated.
     */
    protected void processSuccessResponse(GmlSaxFeatureResponseHandler pHandler, WFSDataType pResultType, DataGroupInfo pGroup)
    {
        int processedCount = pHandler.getProcessedCount();
        LOG.info("Found " + processedCount + " features from WPS process.");
        if (pGroup != null)
        {
            pGroup.addMember(pResultType, this);
            pGroup.activationProperty().setActive(true);
            pResultType.setVisible(true, this);
            MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController().addDataType("WPS", "WPS", pResultType, this);
        }
    }

    /**
     * Examines the supplied response handler, searching for an error response.
     * If the handler has one or more errors returned from the
     * {@link GmlSaxFeatureResponseHandler#getErrors()} method, each error is
     * logged, and an alert is generated to indicate the failure.
     *
     * @param pHandler the handler to examine, in which errors are sought.
     */
    protected void processErrorResponse(GmlSaxFeatureResponseHandler pHandler)
    {
        LOG.error("Encountered errors while processing document.");
        Collection<GmlExceptionReport> errors = pHandler.getErrors();
        LOG.error("Found " + errors.size() + " errors in WPS response.");
        for (GmlExceptionReport errorReport : errors)
        {
            LOG.error(errorReport.toString());
            UserMessageEvent.message(myToolbox.getEventManager(), Type.ERROR, errorReport.toString(), false, this, null, true);
        }
    }

    /**
     * Executes a stream query using the supplied parameters.
     *
     * @param pConfiguration the configuration used to generate a payload.
     * @param pHelper a connection helper used to execute the query.
     * @param pResultType the result type expected in the response.
     * @return an input stream containing a reference to the response.
     * @throws QueryException if the query cannot be constructed / submitted.
     */
    protected InputStream executeStreamQuery(WpsProcessConfiguration pConfiguration, WpsServerConnectionHelper pHelper,
            WFSDataType pResultType)
        throws QueryException
    {
        try
        {
            return pHelper.requestStreamAsPost(myPayloadCreator.createPayload(pConfiguration, pResultType));
        }
        catch (ServerException e)
        {
            throw new QueryException("Unable to execute process on remote server '" + myConnectionParams.getWpsUrl() + "'.", e);
        }
        catch (IOException e)
        {
            throw new QueryException("Unable to marshal payload while submitting process to remote server.", e);
        }
    }
}
