package io.opensphere.wps;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.filter.DataLayerFilter;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.control.DefaultServerDataGroupInfo;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.layer.WFSLayerColumnManager;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wps.config.v2.ProcessConfig;
import io.opensphere.wps.config.v2.WpsProcessConfig;
import io.opensphere.wps.envoy.WpsPropertyDescriptors;
import io.opensphere.wps.envoy.WpsRequestType;
import io.opensphere.wps.envoy.WpsUrlHelper;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import io.opensphere.wps.layer.WpsDataTypeInfoBuilder;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.ui.WpsProcessInstanceCreationHelper;
import io.opensphere.wps.util.WPSConstants;
import net.opengis.ows._110.DCP;
import net.opengis.ows._110.HTTP;
import net.opengis.ows._110.LanguageStringType;
import net.opengis.ows._110.Operation;
import net.opengis.ows._110.RequestMethodType;
import net.opengis.wps._100.ProcessBriefType;
import net.opengis.wps._100.ProcessOfferings;
import net.opengis.wps._100.WPSCapabilitiesType;

/**
 * A mantle controller used to react to WPS Server events.
 */
@SuppressWarnings("PMD.GodClass")
public class WpsMantleController extends EventListenerService
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(WpsMantleController.class);

    /**
     * The mantle toolbox through which data group registration is handled.
     */
    private final MantleToolbox myMantleToolbox;

    /**
     * The toolbox through which application interaction occurs.
     */
    private final Toolbox myToolbox;

    /** The plugin preferences. */
    private final Preferences myPreferences;

    /**
     * A dictionary of non-root data groups, using the server identifier as the
     * key.
     */
    private final Map<String, DataGroupInfo> myServerDataGroups = New.map();

    /**
     * A dictionary of connection information, using the server identifier as
     * the key.
     */
    private final Map<String, ServerConnectionParams> myServerDetails = New.map();

    /**
     * A dictionary of envoys used to connect to the WPS Server for streaming
     * operations.
     */
    private final Map<String, LegacyWpsExecuteEnvoy> myStreamingServerEnvoys = New.map();

    /**
     * A cache of helper classes, using the WPS Server ID as the key, and the
     * helper as the value.
     */
    private final Map<String, WpsProcessInstanceCreationHelper> myHelperCache;

    /**
     * The listener configured to listen to changes in the data registry for
     * {@link WPSCapabilitiesType} objects.
     */
    private final WpsGetCapabilitiesDataRegistryChangeListener myDataRegistryListener;

    /**
     * The listener configured to listen to changes in the data registry for
     * {@link WpsProcessConfiguration} objects.
     */
    private final WpsSavedProcessDataRegistryChangeListener mySavedProcessRegistryListener;

    /**
     * The listener through which data type activations occur.
     */
    private final WpsActivationListener myActivationListener;

    /**
     * The controller with which WPS processes are executed.
     */
    private final WpsProcessExecutionController myExecutionController;

    /**
     * The controller with which WPS process configuration persistence is
     * handled.
     */
    private final WpsProcessDepositController myDepositController;

    /** The WPS process config. */
    private final WpsProcessConfig myProcessConfig;

    /**
     * Creates a new WPS Mantle controller, populated with the supplied toolbox.
     *
     * @param pToolbox the toolbox through which application interactions occur.
     * @param preferences the plugin preferences
     */
    public WpsMantleController(Toolbox pToolbox, Preferences preferences)
    {
        super(pToolbox.getEventManager(), 1);
        myToolbox = pToolbox;
        myPreferences = preferences;
        myMantleToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);

        myHelperCache = New.map();

        myDataRegistryListener = new WpsGetCapabilitiesDataRegistryChangeListener(this::processCapabilitiesDocumentAdded,
                this::processCapabilitiesDocumentRemoved);
        mySavedProcessRegistryListener = new WpsSavedProcessDataRegistryChangeListener(myToolbox, this::getDataGroup);

        DataModelCategory category = new DataModelCategory(null, WpsDataTypeInfo.SOURCE_PREFIX,
                WpsRequestType.GET_CAPABLITIES.getValue());
        myToolbox.getDataRegistry().addChangeListener(myDataRegistryListener, category,
                WpsPropertyDescriptors.WPS_GET_CAPABILITIES);

        DataModelCategory savedProcessCategory = new DataModelCategory(null, OGCServerSource.WPS_SERVICE, "Saved Processes");
        myToolbox.getDataRegistry().addChangeListener(mySavedProcessRegistryListener, savedProcessCategory,
                WpsPropertyDescriptors.WPS_SAVE_PROCESS_CONFIGURATION);
        myActivationListener = new WpsActivationListener(myToolbox);

        myExecutionController = new WpsProcessExecutionController(myToolbox);
        myExecutionController.open();
        myDepositController = new WpsProcessDepositController(myToolbox);

        myProcessConfig = preferences.getJAXBObject(WpsProcessConfig.class, "processConfig", null);
    }

    /**
     * Removes the data associated with the identified server.
     *
     * @param pServerId the unique ID of the server for which the data should be
     *            removed.
     */
    public void serverRemoved(String pServerId)
    {
        if (myServerDataGroups.containsKey(pServerId))
        {
            List<DataGroupInfo> itemsToRemove = New.list();

            DataGroupInfo serverDataGroup = myServerDataGroups.get(pServerId);
            for (DataGroupInfo child : serverDataGroup.getChildren())
            {
                if (child.getId().contains(WPSConstants.AVAILABLE_ANALYTICS_KEY)
                        || child.getId().contains(WPSConstants.SAVED_ANALYTICS_KEY))
                {
                    itemsToRemove.add(child);
                }
            }
            for (DataGroupInfo dataGroupInfo : itemsToRemove)
            {
                serverDataGroup.removeChild(dataGroupInfo, this);
                for (DataTypeInfo dataType : dataGroupInfo.getMembers(true))
                {
                    myMantleToolbox.getDataTypeController().removeDataType(dataType, this);
                }
            }

            myServerDataGroups.remove(pServerId);
        }
        if (myMantleToolbox.getDataGroupController().hasDataGroupInfo(pServerId))
        {
            myMantleToolbox.getDataGroupController().removeDataGroupInfo(pServerId, this);
        }
    }

    /**
     * Processes the supplied capabilities document as a new server.
     *
     * @param pCapabilities the document describing the server.
     */
    protected synchronized void processCapabilitiesDocumentAdded(WPSCapabilitiesType pCapabilities)
    {
        int itemsAdded = 0;
        String serverTitle = "";
        for (LanguageStringType title : pCapabilities.getServiceIdentification().getTitle())
        {
            serverTitle = StringUtilities.concat(serverTitle, title.getValue());
        }

        Map<WpsRequestType, Operation> operationDefinitions = New.map();
        for (Operation operation : pCapabilities.getOperationsMetadata().getOperation())
        {
            operationDefinitions.put(WpsRequestType.fromValue(operation.getName()), operation);
        }

        String serverUrl = null;

        if (operationDefinitions.containsKey(WpsRequestType.DESCRIBE_PROCESS_TYPE))
        {
            Collection<String> supportedProcessNames = myProcessConfig != null ? New.set(myProcessConfig.getSupportedProcesses())
                    : Collections.emptySet();
            for (DCP dcp : operationDefinitions.get(WpsRequestType.DESCRIBE_PROCESS_TYPE).getDCP())
            {
                HTTP httpConfiguration = dcp.getHTTP();
                for (JAXBElement<RequestMethodType> requestMethodType : httpConfiguration.getGetOrPost())
                {
                    serverUrl = requestMethodType.getValue().getHref();

                    ProcessOfferings processOfferings = pCapabilities.getProcessOfferings();
                    List<ProcessBriefType> processes = processOfferings.getProcess();

                    List<ProcessBriefType> supportedProcesses = processes.stream()
                            .filter(p -> supportedProcessNames.contains(p.getIdentifier().getValue()))
                            .collect(Collectors.toList());

                    processTypesAdded(serverUrl, serverUrl, supportedProcesses);
                    itemsAdded += supportedProcesses.size();
                }
            }
        }
        LOG.info("Added " + itemsAdded + " datatypes as processes for " + serverUrl);

        if (myServerDetails.get(serverUrl) != null)
        {
            LegacyWpsExecuteEnvoy envoy = new LegacyWpsExecuteEnvoy(myToolbox, myServerDetails.get(serverUrl),
                    myServerDataGroups.get(serverUrl), pCapabilities);
            envoy.open();

            myStreamingServerEnvoys.put(serverUrl, envoy);
        }
        else
        {
            LOG.warn("Unable to activate WPS portion of server due to missing server details ('" + serverUrl + "')");
        }
    }

    /**
     * Processes the supplied capabilities document as a removed server.
     *
     * @param pCapabilities the document describing the server.
     */
    protected synchronized void processCapabilitiesDocumentRemoved(WPSCapabilitiesType pCapabilities)
    {
        String serverTitle = "";
        for (LanguageStringType title : pCapabilities.getServiceIdentification().getTitle())
        {
            serverTitle = StringUtilities.concat(serverTitle, title.getValue());
        }

        Map<WpsRequestType, Operation> operationDefinitions = New.map();
        for (Operation operation : pCapabilities.getOperationsMetadata().getOperation())
        {
            operationDefinitions.put(WpsRequestType.fromValue(operation.getName()), operation);
        }

        String serverUrl = null;
        if (operationDefinitions.containsKey(WpsRequestType.DESCRIBE_PROCESS_TYPE))
        {
            for (DCP dcp : operationDefinitions.get(WpsRequestType.DESCRIBE_PROCESS_TYPE).getDCP())
            {
                HTTP httpConfiguration = dcp.getHTTP();
                for (JAXBElement<RequestMethodType> requestMethodType : httpConfiguration.getGetOrPost())
                {
                    serverUrl = requestMethodType.getValue().getHref();

                    ProcessOfferings processOfferings = pCapabilities.getProcessOfferings();
                    List<ProcessBriefType> processes = processOfferings.getProcess();

                    for (ProcessBriefType process : processes)
                    {
                        processTypeRemoved(serverUrl, serverTitle, process);
                    }
                }
            }
        }

        LegacyWpsExecuteEnvoy legacyWpsExecuteEnvoy = myStreamingServerEnvoys.get(serverUrl);

        if (legacyWpsExecuteEnvoy != null)
        {
            legacyWpsExecuteEnvoy.close();
            myStreamingServerEnvoys.remove(serverUrl);
        }
    }

    /**
     *
     * A processor method used to react to an existing {@link ProcessBriefType}
     * being removed to the data registry.
     *
     * @param pRootWpsUrl the root URL of the WPS server to which the process
     *            type belongs.
     * @param pServerTitle the title of the server to which the process type
     *            belongs.
     * @param pProcessType the type removed from the data registry.
     */
    protected synchronized void processTypeRemoved(String pRootWpsUrl, String pServerTitle, ProcessBriefType pProcessType)
    {
        DataGroupInfo serverDataGroup = getDataGroup(pRootWpsUrl, pServerTitle);
        DataGroupInfo availableAnalyticsGroup = getAvailableAnalyticsGroup(serverDataGroup);

        String dataTypeKey = pRootWpsUrl + "!!" + pProcessType.getIdentifier().getValue();
        String dataTypeName = pProcessType.getIdentifier().getValue();
        DataTypeInfo dataType = getDataType(dataTypeKey, dataTypeName);
        if (dataType != null)
        {
            dataType.getParent().removeMember(dataType, false, this);
        }

        String targetTypeKey = pRootWpsUrl + "!!" + pProcessType.getIdentifier().getValue();
        String targetTypeName = pProcessType.getIdentifier().getValue();

        String targetProcessDataGroupId = pRootWpsUrl + "!!" + pProcessType.getIdentifier().getValue();
        for (DataGroupInfo child : availableAnalyticsGroup.getChildren())
        {
            if (StringUtils.equals(child.getId(), targetProcessDataGroupId))
            {
                removeTypeFromGroup(targetTypeKey, targetTypeName, child);
                availableAnalyticsGroup.removeChild(child, this);
            }
        }
    }

    /**
     * Removes the identified data type from the supplied group.
     *
     * @param pKey the key of the type to remove.
     * @param pName the name of the type to remove.
     * @param pGroup the group from which to remove entries.
     */
    protected void removeTypeFromGroup(String pKey, String pName, DataGroupInfo pGroup)
    {
        for (DataTypeInfo dataTypeInfo : pGroup.getMembers(false))
        {
            if (StringUtils.equals(pKey, dataTypeInfo.getTypeKey()) && StringUtils.equals(pName, dataTypeInfo.getTypeName()))
            {
                pGroup.removeMember(dataTypeInfo, false, this);
            }
        }
    }

    /**
     * A processor method used to react to a new {@link ProcessBriefType} being
     * added to the data registry. A {@link ProcessBriefType} represents a
     * "stub" of a process type, which will be displayed to the user for further
     * action. Before a user can configure a process instance for a given
     * {@link ProcessBriefType}, the type's description must be resolved from
     * the server. For each process brief type, a new data group and data type
     * will be created and added to the parent type.
     *
     * @param pRootWpsUrl the root URL of the WPS server to which the process
     *            type belongs.
     * @param pServerTitle the title of the server to which the process type
     *            belongs.
     * @param pProcessTypes the types added to the data registry.
     */
    protected synchronized void processTypesAdded(String pRootWpsUrl, String pServerTitle, List<ProcessBriefType> pProcessTypes)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Adding " + pProcessTypes.size() + " process types: '" + pRootWpsUrl + "', '" + pServerTitle + "'");
        }

        DataGroupInfo serverDataGroup = getDataGroup(pRootWpsUrl, pServerTitle);
        DataGroupInfo availableAnalyticsGroup = getAvailableAnalyticsGroup(serverDataGroup);

        WpsProcessInstanceCreationHelper helper = getWpsProcessInstanceCreationHelper(pRootWpsUrl);

        for (ProcessBriefType processType : pProcessTypes)
        {
            // the data type and data group generated here represent the
            // available analytic, NOT the configuration saved by
            // the user, and should only be shown in the available analytics
            // folder.
            WpsDataTypeInfo processDefinitionDataType = createAvailableDataTypeInfo(pRootWpsUrl, pServerTitle, processType);

            WpsDataTypeInfoBuilder resultDataTypeBuilder = createResultDataTypeBuilder(pRootWpsUrl, pServerTitle, processType);
            ProcessConfig config = myProcessConfig.getProcessConfig(processType.getIdentifier().getValue());
            DefaultDataGroupInfo processDefinitionDataGroup = new DefaultDataGroupInfo(false, myToolbox,
                    WpsDataTypeInfo.SOURCE_PREFIX, pRootWpsUrl + "!!" + processType.getIdentifier().getValue(),
                    processType.getTitle().getValue());
            processDefinitionDataGroup.setActivationSupported(false);
            processDefinitionDataGroup.setTriggeringSupported(true);
            processDefinitionDataGroup.setTriggerHandler(pEvent -> helper.createNewWpsInstance(pEvent, pRootWpsUrl, processType,
                    resultDataTypeBuilder, config, this::wpsInstanceCreated));
            processDefinitionDataGroup.addMember(processDefinitionDataType, this);
            availableAnalyticsGroup.addChild(processDefinitionDataGroup, this);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug(
                    "Finished adding " + pProcessTypes.size() + " process types: '" + pRootWpsUrl + "', '" + pServerTitle + "'");
        }
    }

    /**
     * Creates a new datatype to represent the available process configuration.
     * The datatype generated here represents the available analytic, NOT the
     * configuration saved by the user, and should only be shown in the
     * available analytics folder.
     *
     * @param pRootWpsUrl the root URL of the server's WPS endpoint.
     * @param pServerTitle the title of the server.
     * @param pProcessType the definition of the process configuration, from
     *            which the data type will be created.
     * @return a new data type representing the available analytic's
     *         configuration.
     */
    protected WpsDataTypeInfo createAvailableDataTypeInfo(String pRootWpsUrl, String pServerTitle, ProcessBriefType pProcessType)
    {
        DefaultMetaDataInfo metadata = new DefaultMetaDataInfo();
        metadata.setSpecialKeyDetector(MantleToolboxUtils.getMantleToolbox(myToolbox).getColumnTypeDetector());
        WpsDataTypeInfo processDefinitionDataType = new WpsDataTypeInfo(myToolbox, pServerTitle,
                pRootWpsUrl + "!!" + pProcessType.getIdentifier().getValue(), pProcessType.getIdentifier().getValue(),
                pProcessType.getTitle().getValue(), metadata);
        processDefinitionDataType.getMapVisualizationInfo().setVisualizationType(MapVisualizationType.PROCESS_RESULT_ELEMENTS);
        processDefinitionDataType.getBasicVisualizationInfo().setLoadsTo(LoadsTo.TIMELINE, this);
        processDefinitionDataType.setProcessId(pProcessType.getIdentifier().getValue());
        processDefinitionDataType
                .setUrl(WpsUrlHelper.buildDescribeProcessUrl(pRootWpsUrl, pProcessType.getIdentifier().getValue()).toString());
        processDefinitionDataType.getMetaDataInfo().setGeometryColumn("GEOM");
        processDefinitionDataType.setDescription(pProcessType.getAbstract().getValue());
        return processDefinitionDataType;
    }

    /**
     * Creates a new datatype to represent the available process configuration.
     * The datatype generated here represents the available analytic, NOT the
     * configuration saved by the user, and should only be shown in the
     * available analytics folder.
     *
     * @param pRootWpsUrl the root URL of the server's WPS endpoint.
     * @param pServerTitle the title of the server.
     * @param pProcessType the definition of the process configuration, from
     *            which the data type will be created.
     * @return a new data type representing the available analytic's
     *         configuration.
     */
    protected WpsDataTypeInfoBuilder createResultDataTypeBuilder(String pRootWpsUrl, String pServerTitle,
            ProcessBriefType pProcessType)
    {
        WpsDataTypeInfoBuilder builder = new WpsDataTypeInfoBuilder();
        builder.setToolbox(myToolbox);
        builder.setServerTitle(pServerTitle);
        builder.setProcessKey(pRootWpsUrl + "!!" + pProcessType.getIdentifier().getValue());
        builder.setTypeName(pProcessType.getIdentifier().getValue());
        builder.setMetadataInfo(new WFSMetaDataInfo(myToolbox, new WFSLayerColumnManager(myToolbox)));
        builder.setProcessId(pProcessType.getIdentifier().getValue());
        builder.setUrl(WpsUrlHelper.buildDescribeProcessUrl(pRootWpsUrl, pProcessType.getIdentifier().getValue()).toString());
        builder.setDescription(pProcessType.getAbstract().getValue());
        builder.setVisualizationType(MapVisualizationType.POINT_ELEMENTS);
        builder.setLoadsTo(LoadsTo.STATIC);
        builder.setGeometryColumn("GEOM");
        return builder;
    }

    /**
     * A {@link BiConsumer} method used to react to new WPS instance creation
     * requests from the dialog.
     *
     * @param pInstance the configuration instance generated by the dialog.
     * @param pDataType the WFS data type returned by the process execution.
     */
    protected void wpsInstanceCreated(WpsProcessConfiguration pInstance, WFSDataType pDataType)
    {
        switch (pInstance.getRunMode())
        {
            case RUN_ONCE:
                myExecutionController.execute(pInstance);
                break;
            case SAVE:
            case SAVE_AND_RUN:
                // the change listener will receive notification that the
                // configuration has been saved, and based on the
                // run mode, the listener will determine if the mode was SAVE,
                // and just display the new item in the saved
                // analytics folder, or if it was SAVE_AND_RUN, and should both
                // be displayed in the analytics folder and
                // executed.
                myDepositController.deposit(pInstance.getServerId(), pInstance);
                break;
            default:
                LOG.error("Unknown run mode: '" + pInstance.getRunMode().name() + "'");
                break;
        }
    }

    /**
     * Gets the set of available data types currently loaded in the application.
     *
     * @param pTypeKey the key of the type to fetch.
     * @param pTypeName the name of the type to fetch.
     * @return the set of available data types currently loaded in the
     *         application.
     */
    public DataTypeInfo getDataType(String pTypeKey, String pTypeName)
    {
        DataTypeInfo returnValue = null;

        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(myToolbox);
        List<DataGroupInfo> dataGroups = mantleToolbox.getDataGroupController().createGroupList(null, new DataLayerFilter());
        for (DataGroupInfo dgi : dataGroups)
        {
            for (DataTypeInfo type : dgi.getMembers(false))
            {
                if (type.getMetaDataInfo() != null && type.isInUse() && StringUtils.equals(pTypeKey, type.getTypeKey())
                        && StringUtils.equals(pTypeName, type.getTypeName()))
                {
                    returnValue = type;
                    break;
                }
            }
        }
        return returnValue;
    }

    /**
     * Gets the child data group with which available WPS processes are
     * associated. If the supplied data group does not contain a child named
     * "Available Analytics", a new instance is created, associated to the
     * supplied data group, and returned.
     *
     * @param pServerDataGroup the server group in which to search.
     * @return the data group with which available analytics are associated for
     *         the server identified in the supplied group.
     */
    protected DataGroupInfo getAvailableAnalyticsGroup(DataGroupInfo pServerDataGroup)
    {
        DataGroupInfo returnValue = null;
        for (DataGroupInfo child : pServerDataGroup.getChildren())
        {
            if (StringUtils.equals(pServerDataGroup.getDisplayName() + WPSConstants.AVAILABLE_ANALYTICS_KEY, child.getId()))
            {
                returnValue = child;
                break;
            }
        }

        if (returnValue == null)
        {
            returnValue = new DefaultDataGroupInfo(false, myToolbox, WpsDataTypeInfo.SOURCE_PREFIX,
                    pServerDataGroup.getDisplayName() + WPSConstants.AVAILABLE_ANALYTICS_KEY, "Available Analytics");
            pServerDataGroup.addChild(returnValue, this);
        }

        return returnValue;
    }

    /**
     * Gets the data group for the supplied WPS server ID. If no server group is
     * found in the {@link DataGroupController}, then a new instance is created.
     *
     * @param pServerId the unique identifier of the server for which to get the
     *            root data group.
     * @param pServerTitle the display name of the server, used when a new
     *            instance must be created.
     * @return the data group corresponding to the supplied server identifier.
     */
    protected synchronized DataGroupInfo getDataGroup(String pServerId, String pServerTitle)
    {
        if (!myServerDataGroups.containsKey(pServerId))
        {
            DataGroupInfo serverDataGroup = new DefaultServerDataGroupInfo(false, myToolbox, pServerId, pServerTitle);
            serverDataGroup.addChild(new DefaultDataGroupInfo(false, myToolbox, WpsDataTypeInfo.SOURCE_PREFIX,
                    pServerTitle + WPSConstants.AVAILABLE_ANALYTICS_KEY, "Available Analytics"), this);
            serverDataGroup.addChild(new DefaultDataGroupInfo(false, myToolbox, WpsDataTypeInfo.SOURCE_PREFIX,
                    pServerTitle + WPSConstants.SAVED_ANALYTICS_KEY, "Saved Analytics"), this);

            myServerDataGroups.put(pServerId, serverDataGroup);
        }
        return myServerDataGroups.get(pServerId);
    }

    /**
     * Gets the WPS process instance creation helper, used to generate forms for
     * configuring WPS processes. This method has side-effects, namely lazily
     * instantiating new helpers and populating the cache when the
     * {@link #myHelperCache} does not contain an instance associated with the
     * supplied server identifier.
     *
     * @param pServerId the unique identifier for which to get the WPS helper.
     * @return the WPS process instance creation helper associated with the
     *         identified server.
     */
    protected WpsProcessInstanceCreationHelper getWpsProcessInstanceCreationHelper(String pServerId)
    {
        if (!myHelperCache.containsKey(pServerId))
        {
            WpsProcessInstanceCreationHelper helper = new WpsProcessInstanceCreationHelper(myToolbox, pServerId, myPreferences);
            myHelperCache.put(pServerId, helper);
        }

        return myHelperCache.get(pServerId);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.CompositeService#close()
     */
    @Override
    public void close()
    {
        DataGroupController dataGroupController = myMantleToolbox.getDataGroupController();
        for (String serverId : myHelperCache.keySet())
        {
            if (dataGroupController.hasDataGroupInfo(serverId))
            {
                dataGroupController.removeDataGroupInfo(serverId, this);
            }
        }

        myActivationListener.close();
        myExecutionController.close();

        myHelperCache.clear();

        super.close();
    }

    /**
     * The server details to add to the controller.
     *
     * @param pServerId the unique identifier of the server to add information.
     * @param pServer the information describing the server to add.
     */
    public void addServerDetails(String pServerId, ServerConnectionParams pServer)
    {
        LOG.info("Adding server details for server with ID '" + pServerId + "', and parameters are not null: ["
                + (pServer != null) + "]");

        myServerDetails.put(pServerId, pServer);
        DataModelCategory savedProcessCategory = new DataModelCategory(pServerId, OGCServerSource.WPS_SERVICE, "Saved Processes");
        loadSaved(savedProcessCategory);
    }

    /**
     * Loads the saved wps processes.
     *
     * @param savedProcessCategory The saved process category.
     */
    private void loadSaved(DataModelCategory savedProcessCategory)
    {
        SimpleQuery<WpsProcessConfiguration> query = new SimpleQuery<>(savedProcessCategory,
                WpsPropertyDescriptors.WPS_SAVE_PROCESS_CONFIGURATION);
        long[] ids = myToolbox.getDataRegistry().performLocalQuery(query);
        if (query.getResults() != null)
        {
            mySavedProcessRegistryListener.valuesAdded(savedProcessCategory, ids, query.getResults(), this);
        }
    }
}
