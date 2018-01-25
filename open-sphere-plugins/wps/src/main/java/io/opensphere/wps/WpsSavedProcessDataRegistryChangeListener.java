package io.opensphere.wps;

import java.util.function.BiFunction;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDeletableDataGroupInfo;
import io.opensphere.mantle.data.impl.DeletableDataGroupInfoAssistant;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.layer.WFSLayerColumnManager;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wps.layer.LayerConfigurer;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import io.opensphere.wps.layer.WpsDataTypeInfoBuilder;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.util.WPSConstants;
import net.opengis.wps._100.ProcessBriefType;

/**
 * A change listener designed to listen to the Data Registry for new WPS
 * GetCapabilities objects.
 */
public class WpsSavedProcessDataRegistryChangeListener extends DataRegistryListenerAdapter<WpsProcessConfiguration>
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(WpsSavedProcessDataRegistryChangeListener.class);

    /**
     * the toolbox through which application interaction is performed.
     */
    private final Toolbox myToolbox;

    /**
     * the function through which the server's data group is retrieved.
     */
    private final BiFunction<String, String, DataGroupInfo> myDataGroupRetriever;

    /**
     * Creates a new listener, accepting the supplied consumer.
     *
     * @param pToolbox the toolbox through which application interaction is
     *            performed.
     * @param pDataGroupRetriever the function through which the server's data
     *            group is retrieved.
     */
    public WpsSavedProcessDataRegistryChangeListener(Toolbox pToolbox,
            BiFunction<String, String, DataGroupInfo> pDataGroupRetriever)
    {
        myToolbox = pToolbox;
        myDataGroupRetriever = pDataGroupRetriever;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryListener#isIdArrayNeeded()
     */
    @Override
    public boolean isIdArrayNeeded()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryListenerAdapter#valuesRemoved(io.opensphere.core.data.util.DataModelCategory,
     *      long[], java.lang.Iterable, java.lang.Object)
     */
    @Override
    public void valuesRemoved(DataModelCategory pDataModelCategory, long[] pIds,
            Iterable<? extends WpsProcessConfiguration> pRemovedValues, Object pSource)
    {
        LOG.info("Should remove " + pIds.length + " process items, with items supplied as values.");
        int itemsRemoved = 0;
        for (WpsProcessConfiguration removedConfiguration : pRemovedValues)
        {
            processConfigurationRemoved(removedConfiguration.getServerId(), removedConfiguration.getProcessIdentifier(),
                    removedConfiguration);
            itemsRemoved++;
        }

        LOG.info("Removed " + itemsRemoved + " process configurations.");
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryListenerAdapter#valuesAdded(io.opensphere.core.data.util.DataModelCategory,
     *      long[], java.lang.Iterable, java.lang.Object)
     */
    @Override
    public void valuesAdded(DataModelCategory pDataModelCategory, long[] pIds,
            Iterable<? extends WpsProcessConfiguration> pNewValues, Object pSource)
    {
        int itemsAdded = 0;
        for (WpsProcessConfiguration addedConfiguration : pNewValues)
        {
            processConfigurationSaved(addedConfiguration.getServerId(), addedConfiguration.getProcessIdentifier(),
                    addedConfiguration, pIds[itemsAdded]);
            itemsAdded++;
        }

        LOG.info("Added " + itemsAdded + " process configurations.");
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
     * @param pConfiguration the process configuration to add to the
     *            application.
     */
    protected synchronized void processConfigurationRemoved(String pRootWpsUrl, String pServerTitle,
            WpsProcessConfiguration pConfiguration)
    {
        // TODO
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
     * @param pConfiguration the process configuration to add to the
     *            application.
     * @param modelId The model id of the wps process within the data registry.
     */
    protected synchronized void processConfigurationSaved(String pRootWpsUrl, String pServerTitle,
            WpsProcessConfiguration pConfiguration, long modelId)
    {
        LOG.info("Adding process configuration for process '" + pConfiguration.getProcessIdentifier() + "' on server: '"
                + pRootWpsUrl + "', '" + pServerTitle + "'");

        DataGroupInfo serverDataGroup = myDataGroupRetriever.apply(pRootWpsUrl, pServerTitle);
        DataGroupInfo savedAnalyticsGroup = getSavedAnalyticsGroup(serverDataGroup);
        String processTitle = pConfiguration.getInputs().get(WPSConstants.PROCESS_INSTANCE_NAME);

        boolean isNew = pConfiguration.getResultType() != null;
        // the data type and data group generated here represent the
        // configuration created by the user, and are to be shown
        // in both the layer manager and the saved analytics folder.
        WFSDataType processInstanceDataType = getLayer(pConfiguration);
        MantleToolbox mantle = MantleToolboxUtils.getMantleToolbox(myToolbox);
        processInstanceDataType.registerInUse(mantle.getDataGroupController(), false);

        if (processInstanceDataType instanceof WpsDataTypeInfo)
        {
            ((WpsDataTypeInfo)processInstanceDataType).setProcessConfiguration(pConfiguration);
            new LayerConfigurer(myToolbox).setLayerColor(pConfiguration, processInstanceDataType);
        }

        DefaultDeletableDataGroupInfo processInstanceDataGroup = new DefaultDeletableDataGroupInfo(false, myToolbox,
                WpsDataTypeInfo.SOURCE_PREFIX, processInstanceDataType.getTypeKey(), processTitle);
        processInstanceDataGroup.setAssistant(new DeletableDataGroupInfoAssistant(MantleToolboxUtils.getMantleToolbox(myToolbox),
                null, null, g -> deleteWpsProcess(modelId)));
        processInstanceDataGroup.setActivationSupported(true);
        processInstanceDataGroup.setTriggeringSupported(false);
        processInstanceDataGroup.addMember(processInstanceDataType, this);
        savedAnalyticsGroup.addChild(processInstanceDataGroup, this);

        if (isNew || !processInstanceDataGroup.activationProperty().isActive())
        {
            processInstanceDataGroup.activationProperty().setActive(false);
            processInstanceDataGroup.activationProperty().setActive(true);
        }

        LOG.info("Finished adding process configuration for process '" + pConfiguration.getProcessIdentifier() + "' on server: '"
                + pRootWpsUrl + "', '" + pServerTitle + "'");
    }

    /**
     * Deletes a saved wps process when the user clicks the delete button on the
     * layer.
     *
     * @param modelId The model id of the wps process.
     */
    private void deleteWpsProcess(long modelId)
    {
        myToolbox.getDataRegistry().removeModels(new long[] { modelId });
    }

    /**
     * Gets the layer that represents the specified wps configuration.
     *
     * @param configuration The wps configuration to get the layer for.
     * @return The layer representing the wps configuration.
     */
    private WFSDataType getLayer(WpsProcessConfiguration configuration)
    {
        WFSDataType layer = configuration.getResultType();

        if (layer == null)
        {
            WpsDataTypeInfoBuilder builder = new WpsDataTypeInfoBuilder();
            builder.setDisplayName(configuration.getInputs().get(WPSConstants.PROCESS_INSTANCE_NAME));
            builder.setToolbox(myToolbox);
            builder.setServerTitle(configuration.getServerId());
            builder.setProcessKey(configuration.getServerId() + "!!" + configuration.getProcessIdentifier());
            builder.setTypeName(configuration.getProcessIdentifier());
            builder.setMetadataInfo(new WFSMetaDataInfo(myToolbox, new WFSLayerColumnManager(myToolbox)));
            builder.setProcessId(configuration.getProcessIdentifier());
            builder.setVisualizationType(MapVisualizationType.POINT_ELEMENTS);
            builder.setLoadsTo(LoadsTo.TIMELINE);
            builder.setGeometryColumn("GEOM");

            WpsDataTypeInfo wpsLayer = builder.build(configuration.getInstanceId());
            configuration.setResultType(wpsLayer);
            layer = wpsLayer;
        }

        return layer;
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
    protected DataGroupInfo getSavedAnalyticsGroup(DataGroupInfo pServerDataGroup)
    {
        DataGroupInfo returnValue = null;
        for (DataGroupInfo child : pServerDataGroup.getChildren())
        {
            if (child.getId().endsWith(WPSConstants.SAVED_ANALYTICS_KEY))
            {
                returnValue = child;
                break;
            }
        }

        if (returnValue == null)
        {
            returnValue = new DefaultDataGroupInfo(false, myToolbox, WpsDataTypeInfo.SOURCE_PREFIX,
                    pServerDataGroup.getDisplayName() + WPSConstants.SAVED_ANALYTICS_KEY, "Saved Analytics");
            pServerDataGroup.addChild(returnValue, this);
        }

        return returnValue;
    }
}
