package io.opensphere.kml.datasource.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gnu.trove.procedure.TObjectIntProcedure;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLDataSourceController;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLToolbox;
import io.opensphere.kml.datasource.model.v1.KMLDataSourceConfig;
import io.opensphere.kml.mantle.controller.KMLDataGroupInfo;
import io.opensphere.kml.mantle.controller.KMLMantleUtilities;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupActivationException;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoAssistant;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.datasources.IDataSource;

/**
 * The KML data source controller.
 */
@SuppressWarnings("PMD.GodClass")
public class KMLDataSourceControllerImpl extends EventListenerService implements KMLDataSourceController
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLDataSourceControllerImpl.class);

    /** Config preferences key. */
    private static final String PREFERENCES_KEY = "config";

    /** The KML data source configuration. */
    private final KMLDataSourceConfig myConfig;

    /** The KML data group info assistant. */
    private final KMLDataGroupInfoAssistant myDataGroupInfoAssistant;

    /** The KML importer. */
    private final KMLImporter myKMLImporter;

    /** Listener for changes to the order of KML sources. */
    private final OrderChangeListener myOrderChangeListener = new OrderChangeListener()
    {
        @Override
        public void orderChanged(ParticipantOrderChangeEvent event)
        {
            if (event.getChangeType() == ParticipantChangeType.ORDER_CHANGED)
            {
                event.getChangedParticipants().forEachEntry(new TObjectIntProcedure<OrderParticipantKey>()
                {
                    @Override
                    public boolean execute(OrderParticipantKey participant, int order)
                    {
                        DataGroupInfo dgi = myOrderKeyMap.get(participant);
                        if (dgi != null)
                        {
                            for (DataTypeInfo dti : dgi.getMembers(true))
                            {
                                dti.getMapVisualizationInfo().setZOrder(order, null);
                            }
                        }
                        return true;
                    }
                });
            }
        }
    };

    /** A map of the order key to the data type info being ordered. */
    private final Map<OrderParticipantKey, DataGroupInfo> myOrderKeyMap = New.map();

    /** A map of source to the keys which participate in order management. */
    private final Map<IDataSource, OrderParticipantKey> myOrderParticipants = New.map();

    /** The plugin preferences. */
    private final Preferences myPreferences;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The data group activation listener. */
    private final ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public void handleDeactivating(DataGroupInfo dgi)
        {
            KMLDataSource dataSource = getDataSource(dgi);
            if (dataSource != null)
            {
                dataSource.setActive(false);
                deactivateSource(dataSource);
                updateSource(dataSource);
            }
        }

        @Override
        public boolean handleActivating(DataGroupInfo dgi, io.opensphere.core.util.lang.PhasedTaskCanceller canceller)
            throws DataGroupActivationException
        {
            KMLDataSource dataSource = getDataSource(dgi);
            return handleActivateSource(dataSource);
        }
    };

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param prefs The plugin preferences
     */
    public KMLDataSourceControllerImpl(Toolbox toolbox, Preferences prefs)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;
        myPreferences = prefs;
        myConfig = prefs.getJAXBObject(KMLDataSourceConfig.class, PREFERENCES_KEY, new KMLDataSourceConfig());
        myKMLImporter = new KMLImporter(this);
        myDataGroupInfoAssistant = new KMLDataGroupInfoAssistant(this);
        OrderManager orderManager = myToolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY);

        bindEvent(ApplicationLifecycleEvent.class, this::handleApplicationLifecycleEvent);
        addService(orderManager.getParticipantChangeListenerService(myOrderChangeListener));
        addService(toolbox.getImporterRegistry().getImporterService(myKMLImporter));
    }

    @Override
    public void open()
    {
        super.open();

        for (KMLDataSource dataSource : myConfig.getKMLSourceList())
        {
            createDataGroup(dataSource, myDataGroupInfoAssistant);
        }
    }

    @Override
    public void addSource(KMLDataSource dataSource)
    {
        addSource(dataSource, myDataGroupInfoAssistant);
    }

    @Override
    public void addSource(KMLDataSource dataSource, DataGroupInfoAssistant dgiAssistant)
    {
        createDataGroup(dataSource, dgiAssistant);

        myConfig.addSource(dataSource);
        saveConfigState();

        if (dataSource.isActive())
        {
            // If the group has been activated by mantle, we need to activate the data source manually
            if (dataSource.getDataGroupInfo().activationProperty().isActiveOrActivating())
            {
                try
                {
                    handleActivateSource(dataSource);
                }
                catch (DataGroupActivationException e)
                {
                    LOGGER.error(e, e);
                }
            }
            else
            {
                dataSource.getDataGroupInfo().activationProperty().setActive(true);
            }
        }
    }

    @Override
    public void removeSource(KMLDataSource dataSource)
    {
        removeDataGroup(dataSource);
        if (dataSource.isActive())
        {
            dataSource.setActive(false);
            deactivateSource(dataSource);
        }

        myConfig.removeSource(dataSource);
        saveConfigState();
    }

    /**
     * Updates a data source in the controller.
     *
     * @param dataSource The data source
     */
    public void updateSource(KMLDataSource dataSource)
    {
        myConfig.updateSource(dataSource);
        saveConfigState();
    }

    /**
     * Gets the data source for the given data group.
     *
     * @param dataGroup The data group
     * @return The data source
     */
    public KMLDataSource getDataSource(DataGroupInfo dataGroup)
    {
        KMLDataSource matchingSource = null;
        for (KMLDataSource dataSource : myConfig.getKMLSourceList())
        {
            if (dataSource.getDataGroupInfo() == dataGroup)
            {
                matchingSource = dataSource;
                break;
            }
        }
        if (matchingSource == null)
        {
            KMLDataGroupInfo kmlGroup = (KMLDataGroupInfo)dataGroup;
            matchingSource = kmlGroup.getKMLDataSource();
        }

        return matchingSource;
    }

    /**
     * Gets the data source for the given data type key.
     *
     * @param dataTypeKey The data type key
     * @return The data source
     */
    public KMLDataSource getDataSource(String dataTypeKey)
    {
        KMLDataSource matchingSource = null;
        for (KMLDataSource dataSource : myConfig.getKMLSourceList())
        {
            if (dataSource.getDataTypeKey().equals(dataTypeKey))
            {
                matchingSource = dataSource;
                break;
            }
        }
        return matchingSource;
    }

    /**
     * Gets the importer.
     *
     * @return The importer
     */
    public KMLImporter getImporter()
    {
        return myKMLImporter;
    }

    /**
     * Gets the list of data sources.
     *
     * @return The list of data sources
     */
    public List<KMLDataSource> getSourceList()
    {
        return myConfig.getKMLSourceList();
    }

    /**
     * Gets the list of source names.
     *
     * @return The source names
     */
    public Collection<String> getSourceNames()
    {
        return myConfig.getSourceList().stream().map(s -> s.getName()).collect(Collectors.toList());
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Handle DataTypeInfoLoadsToChangeEvent.
     *
     * @param event the event
     */
    public void handleLoadsToChanged(DataTypeInfoLoadsToChangeEvent event)
    {
        KMLDataSource dataSource = getDataSource(event.getDataTypeKey());
        if (dataSource != null)
        {
            dataSource.setIncludeInTimeline(event.getLoadsTo().isTimelineEnabled());
            updateSource(dataSource);
        }
    }

    /**
     * Handles activating a data source.
     *
     * @param dataSource the data source
     * @return whether it was activated
     * @throws DataGroupActivationException if something went wrong
     */
    private boolean handleActivateSource(KMLDataSource dataSource) throws DataGroupActivationException
    {
        if (dataSource != null)
        {
            dataSource.setActive(true);
            if (activateSource(dataSource))
            {
                updateSource(dataSource);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Handles a {@link ApplicationLifecycleEvent}.
     *
     * @param event the event
     */
    private void handleApplicationLifecycleEvent(ApplicationLifecycleEvent event)
    {
        if (event.getStage() == ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED)
        {
            /* Activate sources marked as active. Ideally mantle would trigger
             * myActivationListener to do this, but Jeremy's data group
             * activation refactor changed that behavior. */
            KMLToolbox kmlToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(KMLToolbox.class);
            kmlToolbox.getPluginExecutor().execute(this::activateSources);
        }
    }

    /**
     * Activates all sources that need activating.
     */
    private void activateSources()
    {
        for (KMLDataSource dataSource : myConfig.getKMLSourceList())
        {
            if (dataSource.isActive())
            {
                dataSource.getDataGroupInfo().activationProperty().setActive(true);
                try
                {
                    activateSource(dataSource);
                }
                catch (DataGroupActivationException e)
                {
                    LOGGER.error(e, e);
                }
            }
        }
    }

    /**
     * Activates the given source.
     *
     * @param dataSource The data source
     * @return {@code true}, if successful
     * @throws DataGroupActivationException If the activation fails.
     */
    private boolean activateSource(final KMLDataSource dataSource) throws DataGroupActivationException
    {
        KMLDataRegistryHelper.depositDataSources(myToolbox.getDataRegistry(), KMLDataSource.class.getSimpleName(),
                dataSource.getName(), Collections.singleton(dataSource));
        QueryTracker tracker = KMLDataRegistryHelper.queryAndActivate(myToolbox.getDataRegistry(), dataSource,
                dataSource.getPath(), Nulls.STRING);
        tracker.awaitCompletion();

        if (tracker.getQueryStatus() == QueryStatus.SUCCESS)
        {
            return true;
        }
        else
        {
            if (tracker.getException() != null)
            {
                throw new DataGroupActivationException(tracker.getException());
            }
            return false;
        }
    }

    /**
     * Creates a data group for the given data source.
     *
     * @param dataSource The data source
     * @param dgiAssistant the dgi assistant
     */
    private void createDataGroup(KMLDataSource dataSource, DataGroupInfoAssistant dgiAssistant)
    {
        DataGroupInfo dataGroup = dataSource.getDataGroupInfo();
        if (dataGroup == null)
        {
            dataGroup = KMLMantleUtilities.createDataGroup(dataSource);

            DefaultOrderParticipantKey participant = new DefaultOrderParticipantKey(
                    DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY, dataGroup.getId());
            myOrderKeyMap.put(participant, dataGroup);
            myOrderParticipants.put(dataSource, participant);
            OrderManager manager = myToolbox.getOrderManagerRegistry().getOrderManager(participant);
            int zorder = manager.activateParticipant(participant);
            for (DataTypeInfo dti : dataGroup.getMembers(true))
            {
                // If these should be managed separately, they will need
                // their own order participant keys.
                dti.getMapVisualizationInfo().setZOrder(zorder, null);
                dti.setOrderKey(participant);
            }

            dataGroup.activationProperty().addListener(myActivationListener);
            if (dataGroup instanceof DefaultDataGroupInfo)
            {
                ((DefaultDataGroupInfo)dataGroup).setAssistant(dgiAssistant);
            }
            dataSource.setDataGroupInfo(dataGroup);
        }
    }

    /**
     * Deactivates the given source.
     *
     * @param dataSource The data source
     */
    private void deactivateSource(KMLDataSource dataSource)
    {
        KMLDataRegistryHelper.removeDataSources(myToolbox.getDataRegistry(), KMLDataSource.class.getSimpleName(),
                dataSource.getName());
        KMLDataRegistryHelper.queryAndDeactivate(myToolbox.getDataRegistry(), dataSource, dataSource.getPath());
    }

    /**
     * Removes the data group from the given data source.
     *
     * @param dataSource The data source
     */
    private void removeDataGroup(KMLDataSource dataSource)
    {
        OrderParticipantKey participant = myOrderParticipants.remove(dataSource);
        if (participant != null)
        {
            OrderManager manager = myToolbox.getOrderManagerRegistry().getOrderManager(participant);
            myOrderKeyMap.remove(participant);
            manager.deactivateParticipant(participant);
        }
        DataGroupInfo dataGroup = dataSource.getDataGroupInfo();
        if (dataGroup != null)
        {
            KMLMantleUtilities.removeDataGroup(dataSource);
            dataGroup.activationProperty().removeListener(myActivationListener);
            if (dataGroup instanceof DefaultDataGroupInfo)
            {
                ((DefaultDataGroupInfo)dataGroup).setAssistant(null);
            }
            dataSource.setDataGroupInfo(null);
        }
    }

    /**
     * Saves the config state to the file system.
     */
    private void saveConfigState()
    {
        myPreferences.putJAXBObject(PREFERENCES_KEY, myConfig, false, this);
    }
}
