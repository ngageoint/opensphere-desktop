package io.opensphere.wps.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.filter.DataLayerFilter;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wfs.consumer.FeatureConsumer;
import io.opensphere.wfs.gml311.GmlExceptionReport;
import io.opensphere.wfs.gml311.GmlSaxFeatureResponseHandler;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.util.ServerException;
import io.opensphere.wps.util.WPSConstants;
import io.opensphere.wps.util.WpsServerConnectionHelper;
import net.opengis.ows._110.CodeType;
import net.opengis.wps._100.DataInputsType;
import net.opengis.wps._100.DataType;
import net.opengis.wps._100.Execute;
import net.opengis.wps._100.InputType;
import net.opengis.wps._100.LiteralDataType;
import net.opengis.wps._100.OutputDefinitionType;
import net.opengis.wps._100.ResponseFormType;

/**
 * An envoy implementation that executes a process on a remote WPS server.
 */
public class WpsExecuteProcessEnvoy extends AbstractWpsEnvoy<InputStream>
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(WpsExecuteProcessEnvoy.class);

    /**
     * A configuration describing the process to execute.
     */
    private final WpsProcessConfiguration myConfiguration;

    /**
     * The data type with which to associate the response data.
     */
    private final WFSDataType myWfsType;

    /**
     * Creates a new envoy, configured using the supplied parameters to perform
     * WPS execute requests.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     * @param pServer the parameters defining the server to which the request
     *            will be submitted.
     * @param pConfiguration a configuration describing the process to execute.
     * @param pWfsType the data type with which to associate the response data.
     */
    public WpsExecuteProcessEnvoy(Toolbox pToolbox, ServerConnectionParams pServer, WpsProcessConfiguration pConfiguration,
            WFSDataType pWfsType)
    {
        super(pToolbox, pServer, WpsRequestType.EXECUTE);
        myConfiguration = pConfiguration;
        myWfsType = pWfsType;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.AbstractEnvoy#open()
     */
    @Override
    public void open()
    {
        switch (myConfiguration.getRunMode())
        {
            case RUN_ONCE:
                run();
                break;
            case SAVE:
                save();
                break;
            case SAVE_AND_RUN:
                save();
                run();
                break;
            default:
                LOG.error("Unknown run mode: '" + myConfiguration.getRunMode().name() + "'");
                break;
        }
    }

    /**
     * Saves the supplied configuration to the data registry for later reuse.
     */
    protected void save()
    {
        WpsCacheDepositReceiver receiver = new WpsCacheDepositReceiver(getDataRegistry());
        DataModelCategory category = new DataModelCategory(getServer().getWpsUrl(), OGCServerSource.WPS_SERVICE,
                "Saved Processes");

        LOG.info("Depositing data to data registry from " + this.getClass().getName());
        SimpleSessionOnlyCacheDeposit<WpsProcessConfiguration> deposit = new SimpleSessionOnlyCacheDeposit<>(category,
                WpsPropertyDescriptors.WPS_SAVE_PROCESS_CONFIGURATION, Collections.singleton(myConfiguration));
        try
        {
            long[] values = receiver.receive(deposit);
            LOG.info("Recieved IDs from registry: " + Arrays.toString(values) + "(" + this.getClass().getName() + ")");
        }
        catch (CacheException e)
        {
            // TODO
            LOG.error("Unable to write to cache.", e);
        }
    }

    /**
     * Executes the configuration, generating a new data type and propagating
     * the results.
     */
    protected void run()
    {
        Map<String, String> parameters = getParameterMap();
        try (InputStream response = executeStreamQuery(parameters))
        {
            DataTypeInfo correspondingType = getDataType(myWfsType.getTypeName());
            DataGroupInfo group = null;
            if (correspondingType != null)
            {
                myWfsType.setMetaDataInfo(correspondingType.getMetaDataInfo());
                DataGroupInfo parent = correspondingType.getParent();
                while (!parent.isRootNode())
                {
                    parent = parent.getParent();
                }

                group = getDataGroup(parent);
            }

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();

            // parse the WFS response
            FeatureConsumer consumer = getConsumerManager().requestConsumer(myWfsType, false);

            GmlSaxFeatureResponseHandler handler = new GmlSaxFeatureResponseHandler(myWfsType, consumer);
            saxParser.parse(response, handler);

            if (handler.isInError())
            {
                LOG.error("Encountered errors while processing document.");
                Collection<GmlExceptionReport> errors = handler.getErrors();
                LOG.error("Found " + errors.size() + " errors in WPS response.");
                for (GmlExceptionReport errorReport : errors)
                {
                    LOG.error(errorReport.toString());
                    UserMessageEvent.message(getToolbox().getEventManager(), Type.ERROR, errorReport.toString(), false, this,
                            null, true);
                }
            }
            else
            {
                int processedCount = handler.getProcessedCount();
                LOG.info("Found " + processedCount + " features from WPS process.");
                if (group != null)
                {
                    group.addMember(myWfsType, this);

                    group.activationProperty().setActive(true);
                    MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataTypeController().addDataType("WPS", "WPS", myWfsType,
                            this);
                    myWfsType.getBasicVisualizationInfo().setLoadsTo(LoadsTo.TIMELINE, this);
                    myWfsType.setVisible(true, this);
                    myWfsType.registerInUse(MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController(), true);
                }
            }
        }
        catch (QueryException e)
        {
            LOG.error("Unable to execute WPS Process on remote server.", e);
        }
        catch (ParserConfigurationException | SAXException | IOException e)
        {
            LOG.error("Unable to parse WPS Process response.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeQuery(java.util.Map)
     */
    @Override
    protected synchronized InputStream executeQuery(Map<String, String> pParameters) throws QueryException
    {
        return executeStreamQuery(pParameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeQuery(io.opensphere.wps.util.WpsServerConnectionHelper,
     *      java.util.Map)
     */
    @Override
    protected InputStream executeQuery(WpsServerConnectionHelper pHelper, Map<String, String> pParameters) throws QueryException
    {
        return executeStreamQuery(pHelper, pParameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeStreamQuery(io.opensphere.wps.util.WpsServerConnectionHelper,
     *      java.util.Map)
     */
    @Override
    protected InputStream executeStreamQuery(WpsServerConnectionHelper pHelper, Map<String, String> pParameters)
        throws QueryException
    {
        try
        {
            return pHelper.requestStreamAsPost(createPayload(pParameters));
        }
        catch (ServerException e)
        {
            throw new QueryException("Unable to execute process on remote server '" + getServer().getWpsUrl() + "'.", e);
        }
        catch (IOException e)
        {
            throw new QueryException("Unable to marshal payload while submitting process to remote server.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeStreamQuery(java.util.Map)
     */
    @Override
    protected synchronized InputStream executeStreamQuery(Map<String, String> pParameters) throws QueryException
    {
        return executeStreamQuery(
                new WpsServerConnectionHelper(
                        WpsUrlHelper.buildExecuteProcessUrl(getServerId(), myConfiguration.getProcessIdentifier()), getToolbox()),
                pParameters);
    }

    /**
     * Gets the set of available data types currently loaded in the application.
     *
     * @param pTypeName the name of the type to fetch.
     * @return the set of available data types currently loaded in the
     *         application.
     */
    public DataTypeInfo getDataType(String pTypeName)
    {
        DataTypeInfo returnValue = null;

        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(getToolbox());
        List<DataGroupInfo> dataGroups = mantleToolbox.getDataGroupController().createGroupList(null, new DataLayerFilter());
        for (DataGroupInfo dgi : dataGroups)
        {
            for (DataTypeInfo type : dgi.getMembers(false))
            {
                if (type.getMetaDataInfo() != null && type.isInUse() && StringUtils.equals(pTypeName, type.getTypeName()))
                {
                    returnValue = type;
                    break;
                }
            }
        }
        return returnValue;
    }

    /**
     * Gets the set of available data types currently loaded in the application.
     *
     * @param pRootNode the node for which to get the data group.
     * @return the set of available data types currently loaded in the
     *         application.
     */
    public synchronized DataGroupInfo getDataGroup(DataGroupInfo pRootNode)
    {
        if (pRootNode != null)
        {
            String instanceName = myConfiguration.getInputs().get(WPSConstants.PROCESS_INSTANCE_NAME);
            if (StringUtils.isNotBlank(instanceName))
            {
                for (DataGroupInfo child : pRootNode.getChildren())
                {
                    if (StringUtils.equals(child.getId(), instanceName))
                    {
                        return child;
                    }
                }

                // if execution gets here, it means there were not found
                // children, so create one:
                DataGroupInfo returnValue = new DefaultDataGroupInfo(false, getToolbox(), "WPS", instanceName);
                pRootNode.addChild(returnValue, this);
                return returnValue;
            }
        }

        return null;
    }

    /**
     * Creates a new {@link Execute} payload {@link InputStream} that can be
     * transmitted as part of an WPS execute request. The supplied parameters
     * are used to generate a new Execute object, which is then serialized into
     * an input stream.
     *
     * @param pParameters the set of parameters to encode into the payload.
     * @return an input stream in which the request payload is serialized.
     * @throws IOException if the request cannot be serialized to a stream.
     */
    protected InputStream createPayload(Map<String, String> pParameters) throws IOException
    {
        Execute executePayload = new Execute();
        executePayload.setService("WPS");
        executePayload.setVersion("1.0.0");

        CodeType identifier = new CodeType();
        identifier.setValue(myConfiguration.getProcessIdentifier());
        executePayload.setIdentifier(identifier);

        DataInputsType inputs = new DataInputsType();
        for (Entry<String, String> entry : pParameters.entrySet())
        {
            inputs.getInput().add(generateInput(entry.getKey(), entry.getValue()));
        }
        executePayload.setDataInputs(inputs);

        ResponseFormType response = new ResponseFormType();
        OutputDefinitionType outputDefinition = new OutputDefinitionType();

        CodeType outputIdentifier = new CodeType();
        outputIdentifier.setValue("OutputData");
        outputDefinition.setIdentifier(outputIdentifier);
        response.setRawDataOutput(outputDefinition);

        executePayload.setResponseForm(response);

        return serializeToStream(executePayload);
    }

    /**
     * Serializes the supplied {@link Execute} JAXB payload to an
     * {@link InputStream} that can be processed with a request.
     *
     * @param pPayload the payload to serialize to an {@link InputStream}.
     * @return an {@link InputStream} in which the supplied JAXB object has been
     *         serialized.
     * @throws IOException if the supplied object cannot be serialized.
     */
    protected InputStream serializeToStream(Execute pPayload) throws IOException
    {
        ByteArrayInputStream bodyStream;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            XMLUtilities.writeXMLObject(pPayload, out);
            out.flush();
            bodyStream = new ByteArrayInputStream(out.toByteArray());
        }
        catch (JAXBException | IOException e)
        {
            throw new IOException("Unable to serialize execute request to stream.", e);
        }
        return bodyStream;
    }

    /**
     * Generates a new input type for the supplied key / value pair.
     *
     * @param pKey the name of the identifier to apply to the input.
     * @param pValue the value of the input.
     * @return a new {@link InputType} generated for the supplied key / value
     *         pair.
     */
    protected InputType generateInput(String pKey, String pValue)
    {
        InputType input = new InputType();
        CodeType inputIdentifier = new CodeType();
        inputIdentifier.setValue(pKey);
        input.setIdentifier(inputIdentifier);

        DataType data = new DataType();
        LiteralDataType literal = new LiteralDataType();

        String value = pValue;
        if (StringUtils.equals("TYPENAME", pKey))
        {
            value = pValue.replace(' ', '_').toUpperCase();
            myWfsType.setTypeName(value);
        }

        literal.setValue(value);
        data.setLiteralData(literal);
        input.setData(data);
        return input;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#getParameterMap()
     */
    @Override
    protected Map<String, String> getParameterMap()
    {
        return myConfiguration.getInputs();
    }
}
