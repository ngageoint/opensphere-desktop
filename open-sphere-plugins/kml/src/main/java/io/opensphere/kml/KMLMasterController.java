package io.opensphere.kml;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLToolbox;
import io.opensphere.kml.datasource.controller.KMLDataSourceControllerImpl;
import io.opensphere.kml.mantle.controller.KMLMantleController;
import io.opensphere.kml.mantle.controller.KMLMantleUtilities;
import io.opensphere.kml.mantle.controller.KMLTransformer;
import io.opensphere.kml.tree.controller.KMLTreeController;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;

/**
 * The master controller.
 */
class KMLMasterController extends EventListenerService
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLMasterController.class);

    /** The system data registry. */
    private final DataRegistry myDataRegistry;

    /** The KML toolbox. */
    private final KMLToolbox myKMLToolbox;

    /**
     * Listener for notifications of the active flag changing on a KML document.
     */
    private final DataRegistryListener<Boolean> myDataRegistryActiveListener = new DataRegistryActiveListener();

    /** The data source controller. */
    private final KMLDataSourceControllerImpl myDataSourceController;

    /** The tree controller. */
    private final KMLTreeController myTreeController;

    /** The mantle controller. */
    private final KMLMantleController myMantleController;

    /** The refresh controller. */
    private final KMLRefreshController myRefreshController;

    /** The region controller. */
    private final KMLRegionController myRegionController;

    /** Procrastinating executor. */
    private final ProcrastinatingExecutor myProcrastinatingExecutor = new ProcrastinatingExecutor("KMLMasterController", 500);

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param pluginPreferences The plugin preferences
     */
    public KMLMasterController(Toolbox toolbox, Preferences pluginPreferences)
    {
        super(toolbox.getEventManager());

//         myDataRegistry = toolbox.getDataRegistry();
        myDataRegistry = null;
        myKMLToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(KMLToolbox.class);

        myDataSourceController = new KMLDataSourceControllerImpl(toolbox, pluginPreferences);
        myMantleController = new KMLMantleController(toolbox);
        myTreeController = new KMLTreeController(toolbox, myMantleController);
        myRefreshController = new KMLRefreshController(toolbox);
        myRegionController = new KMLRegionController(toolbox, myMantleController);

        myKMLToolbox.setDataSourceController(myDataSourceController);

        addService(myDataSourceController);
        addService(myMantleController);
        bindEvent(DataTypeInfoLoadsToChangeEvent.class, this::handleDataTypeInfoLoadsToChange);
        addService(toolbox.getMapManager().getViewChangeSupport().getViewChangeListenerService(this::handleViewChanged));
        addService(myDataRegistry.getChangeListenerService(myDataRegistryActiveListener,
                new DataModelCategory(Nulls.STRING, "KML", Nulls.STRING), KMLDataRegistryHelper.ACTIVE_PROPERTY_DESCRIPTOR));
    }

    /**
     * Gets the data source controller.
     *
     * @return the data source controller
     */
    public KMLDataSourceControllerImpl getDataSourceController()
    {
        return myDataSourceController;
    }

    /**
     * Getter for transformer.
     *
     * @return the transformer
     */
    public KMLTransformer getTransformer()
    {
        return myMantleController.getTransformer();
    }

    /**
     * Handles a DataTypeInfoLoadsToChangeEvent.
     *
     * @param event the event
     */
    private void handleDataTypeInfoLoadsToChange(final DataTypeInfoLoadsToChangeEvent event)
    {
        if (KMLMantleUtilities.KML.equals(event.getDataTypeInfo().getTypeName()))
        {
            executeOnKMLThread(() -> myDataSourceController.handleLoadsToChanged(event));
        }
    }

    /**
     * Handle view changed.
     *
     * @param viewer the viewer
     * @param type the change type
     */
    private void handleViewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type)
    {
        myProcrastinatingExecutor.execute(() ->
        {
            myRefreshController.handleViewChanged();
            myRegionController.handleViewChanged();
        });
    }

    /**
     * Load a KMLDataEvent.
     *
     * @param dataEvent The KMLDataEvent
     * @param oldRootFeature The old root KML feature for a reload.
     */
    private void loadKMLDataEvent(final KMLDataEvent dataEvent, final KMLFeature oldRootFeature)
    {
        // Prevent inactive data sources from loading, such as network links
        // within a KML that has been deactivated
        if (dataEvent.getDataSource().isActive())
        {
            executeOnKMLThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // Update features with any existing features' data
                        updateFeatures(dataEvent, oldRootFeature);

                        // Deposit internal data sources
                        depositInternalSources(dataEvent.getData(), dataEvent.getDataSource().getRootDataSource().getName());

                        boolean reload = oldRootFeature != null;
                        myMantleController.addData(dataEvent, reload);
                        myTreeController.addData(dataEvent, reload);
                        myRefreshController.addData(dataEvent, reload);
                        myRegionController.addData(dataEvent, reload);
                    }
                    catch (RuntimeException e)
                    {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            });
        }
    }

    /**
     * Unload a KMLDataEvent. This should not be done in the case of a reload.
     *
     * @param dataEvent The KMLDataEvent
     */
    private void unloadKMLDataEvent(final KMLDataEvent dataEvent)
    {
        executeOnKMLThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    myRegionController.removeData(dataEvent.getDataSource());
                    myRefreshController.removeData(dataEvent.getDataSource());
                    myTreeController.removeData(dataEvent.getDataSource());
                    myMantleController.removeData(dataEvent.getDataSource());
                }
                catch (RuntimeException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }

                for (KMLDataSource child : dataEvent.getDataSource().getAllDataSources())
                {
                    if (child != dataEvent.getDataSource())
                    {
                        KMLDataRegistryHelper.queryAndDeactivate(myDataRegistry, child, child.getPath());
                    }
                }
            }
        });
    }

    /**
     * Updates the new features in with any existing features' data.
     *
     * @param dataEvent The KMLDataEvent
     * @param pOldRootFeature The old root feature (if this is a reload)
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    private void updateFeatures(KMLDataEvent dataEvent, KMLFeature pOldRootFeature)
    {
        KMLDataSource dataSource = dataEvent.getDataSource();
        KMLFeature newRootFeature = dataEvent.getData();
        KMLFeature oldRootFeature = pOldRootFeature;

        // Update the data source to have the new resulting root feature and
        // child data sources
        dataSource.setResultingFeature(newRootFeature);
        dataSource.getChildDataSources().clear();
        for (KMLFeature feature : newRootFeature.getAllFeatures())
        {
            if (feature.getResultingDataSource() != null)
            {
                dataSource.getChildDataSources().add(feature.getResultingDataSource());
            }
        }

        // Update existing features
        if (oldRootFeature != null && updateFeature(oldRootFeature, newRootFeature))
        {
            // If the root feature matches and it has no children, null it
            // out to make sure it doesn't get removed
            if (oldRootFeature.getChildren().isEmpty())
            {
                oldRootFeature = null;
            }
        }

        // Set the old root feature
        dataEvent.setOldData(oldRootFeature);
    }

    /**
     * Recursively updates new features with their existing features'
     * properties.
     *
     * Also determines which new features are existing, and removes all but the
     * features to be deleted from the old feature tree.
     *
     * @param oldFeature The old feature
     * @param newFeature The new feature
     * @return Whether the features are the same
     */
    private boolean updateFeature(KMLFeature oldFeature, KMLFeature newFeature)
    {
        boolean equals = newFeature.equalsNominally(oldFeature);
        if (equals)
        {
            // Update this feature from the old one and set its state
            boolean equalsSpatially = newFeature.equalsSpatially(oldFeature);
            newFeature.setEqualTo(oldFeature, equalsSpatially);
            if (equalsSpatially)
            {
                newFeature.setAdded(false);

                // Go ahead and set any features under the resulting data source
                // to EXISTING as well
                if (oldFeature.getResultingDataSource() != null)
                {
                    for (KMLFeature child : oldFeature.getResultingDataSource().getAllFeatures())
                    {
                        child.setAdded(false);
                    }
                }
            }
            else
            {
                equals = false;
            }

            // Update children
            for (KMLFeature newChild : newFeature.getChildren())
            {
                synchronized (oldFeature)
                {
                    if (oldFeature.getChildrenUnsafe() != null)
                    {
                        for (Iterator<KMLFeature> iter = oldFeature.getChildrenUnsafe().iterator(); iter.hasNext();)
                        {
                            KMLFeature oldChild = iter.next();
                            if (updateFeature(oldChild, newChild))
                            {
                                // Remove this node from the old feature tree
                                if (oldChild.getChildren().isEmpty())
                                {
                                    iter.remove();
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return equals;
    }

    /**
     * Deposits internal sources into the data registry.
     *
     * @param rootFeature The root feature
     * @param sourceName The name of the data source
     */
    private void depositInternalSources(KMLFeature rootFeature, String sourceName)
    {
        // Determine which internal data sources to load
        Collection<KMLDataSource> resultingDataSources = rootFeature.getAllFeatures().stream()
                .filter(f -> f.getResultingDataSource() != null && f.isAdded()).map(f -> f.getResultingDataSource())
                .collect(Collectors.toSet());

        if (!resultingDataSources.isEmpty())
        {
            // Create the internal data sources
            KMLDataRegistryHelper.depositDataSources(myDataRegistry, KMLDataSource.class.getSimpleName(), sourceName,
                    resultingDataSources);
        }
    }

    /**
     * Helper method to run a runnable on the KML plugin thread.
     *
     * @param r the runnable
     */
    private void executeOnKMLThread(Runnable r)
    {
        myKMLToolbox.getPluginExecutor().execute(r);
    }

    /**
     * Listener for notifications of the active flag changing on a KML document.
     */
    private class DataRegistryActiveListener extends DataRegistryListenerAdapter<Boolean>
    {
        @Override
        public boolean isIdArrayNeeded()
        {
            return true;
        }

        @Override
        public void valuesUpdated(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends Boolean> newValues,
                Object source)
        {
            TLongList activeIds = new TLongArrayList();
            TLongList inactiveIds = new TLongArrayList();
            Iterator<? extends Boolean> iter = newValues.iterator();
            for (int index = 0; index < ids.length && iter.hasNext(); ++index)
            {
                long id = ids[index];
                if (iter.next().booleanValue())
                {
                    activeIds.add(id);
                }
                else
                {
                    inactiveIds.add(id);
                }
            }

            if (!activeIds.isEmpty())
            {
                SimpleQuery<KMLDataEvent> query = new SimpleQuery<>(dataModelCategory,
                        KMLDataRegistryHelper.DATA_EVENT_PROPERTY_DESCRIPTOR);
                myDataRegistry.performLocalQuery(activeIds.toArray(), query);
                for (KMLDataEvent dataEvent : query.getResults())
                {
                    KMLDataEvent oldData = KMLDataRegistryHelper.queryAndRemoveOldData(myDataRegistry, dataEvent.getDataSource(),
                            dataEvent.getDataSource().getPath());
                    KMLFeature oldRootFeature = oldData == null ? null : oldData.getData();
                    loadKMLDataEvent(dataEvent, oldRootFeature);
                }
            }
            if (!inactiveIds.isEmpty())
            {
                SimpleQuery<KMLDataEvent> query = new SimpleQuery<>(dataModelCategory,
                        KMLDataRegistryHelper.DATA_EVENT_PROPERTY_DESCRIPTOR);
                myDataRegistry.performLocalQuery(inactiveIds.toArray(), query);
                myDataRegistry.removeModels(inactiveIds.toArray());
                for (KMLDataEvent dataEvent : query.getResults())
                {
                    unloadKMLDataEvent(dataEvent);
                }
            }
        }
    }
}
