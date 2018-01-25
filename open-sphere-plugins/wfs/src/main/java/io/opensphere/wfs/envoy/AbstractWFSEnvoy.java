package io.opensphere.wfs.envoy;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.IntervalPropertyAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.MultiPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.StringPropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.event.Event.State;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.server.services.OGCServiceStateEvent;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.util.OGCServerConnector;
import io.opensphere.server.util.OGCServerException;
import io.opensphere.wfs.config.DefaultWfsConnectionParams;
import io.opensphere.wfs.config.WFSServerConfig;
import io.opensphere.wfs.config.WFSServerConfig.WFSServerState;
import io.opensphere.wfs.consumer.FeatureConsumerManager;
import io.opensphere.wfs.layer.SingleLayerRequeryEvent;
import io.opensphere.wfs.layer.SingleLayerRequeryEvent.RequeryType;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.layer.WFSLayerColumnManager;

/**
 * Abstract Class for all types of WFS Envoys.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractWFSEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /** Property descriptor for keys used in the data registry. */
    public static final PropertyDescriptor<String> KEY_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("TypeKey", String.class);

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractWFSEnvoy.class);

    /** The Column manager. */
    private final WFSLayerColumnManager myColumnManager;

    /** Manager used to retrieve feature consumers. */
    private final FeatureConsumerManager myConsumerManager;

    /** My active layers. */
    private final Collection<WFSDataType> myDataTypes = New.set();

    /** The State data types. */
    private final Collection<WFSDataType> myStateDataTypes = New.set();

    /** The State data groups. */
    private final Collection<DataGroupInfo> myStateDataGroups = New.set();

    /** My WFS download monitor. */
    private final WFSDownloadMonitor myDownloadMonitor;

    /** The URL and other connection information for the server. */
    private final ServerConnectionParams myServerConfig;

    /** An optional set of consumers to call when layers are activated. */
    private final Collection<Consumer<Collection<WFSDataType>>> myActivationConsumers = New.set();

    /** The listener to be notified when a WFS data group is activated. */
    private final ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public boolean handleActivating(DataGroupInfo dgi, io.opensphere.core.util.lang.PhasedTaskCanceller canceller)
            throws io.opensphere.mantle.data.DataGroupActivationException, InterruptedException
        {
            return activateLayers(getMyMembersFromGroup(dgi));
        }

        @Override
        public void handleDeactivating(DataGroupInfo dgi)
        {
            deactivateLayers(getMyMembersFromGroup(dgi));
        }
    };

    /**
     * Fire state event.
     *
     * @param serverConf the server configuration
     * @param isValid the valid flag
     * @param error String detailing any errors that occurred.
     * @param eventMgr the event manager used to send the new state event
     */
    private static void fireStateEvent(WFSServerConfig serverConf, boolean isValid, String error, EventManager eventMgr)
    {
        if (eventMgr != null)
        {
            OGCServiceStateEvent stateEvent = new OGCServiceStateEvent(serverConf.getServerId(), serverConf.getServerTitle(),
                    OGCServerSource.WFS_SERVICE, serverConf.getLayers(), isValid);
            if (error != null)
            {
                stateEvent.setError(error);
                // Note: EventManager.setEventState publishes the event
                eventMgr.setEventState(stateEvent, State.FAILED);
                LOGGER.warn(error);
            }
            else
            {
                if (serverConf.getServerState() == WFSServerState.ACTIVE)
                {
                    // Note: EventManager.setEventState publishes the event
                    eventMgr.setEventState(stateEvent, State.COMPLETED);
                }
                else
                {
                    eventMgr.publishEvent(stateEvent);
                }
            }
        }
    }

    /**
     * Instantiates a new abstract WFS envoy.
     *
     * @param toolbox the toolbox
     * @param wfsConn The parameters used to connect to an OGC server URL.
     * @param tools Collection of WFS tools.
     */
    public AbstractWFSEnvoy(Toolbox toolbox, ServerConnectionParams wfsConn, WFSTools tools)
    {
        super(toolbox);
        // Make a local copy of the Server Connection Parameters
        myServerConfig = new DefaultWfsConnectionParams(wfsConn);
        myDownloadMonitor = tools.getDownloadMonitor();
        myColumnManager = tools.getLayerColumnManager();
        myConsumerManager = tools.getFeatureConsumerManager();
    }

    /**
     * Adds the supplied consumer to the set of registered notification
     * recipients. The consumer will be called whenever one or more WFS layers
     * are activated.
     *
     * @param activationConsumer the consumer to register.
     */
    public void addActivationConsumer(Consumer<Collection<WFSDataType>> activationConsumer)
    {
        myActivationConsumers.add(activationConsumer);
    }

    /**
     * Provides state handlers a way to register data types that are created
     * during state activation.
     *
     * @param dgi the dgi
     * @param dti the dti
     */
    public void registerState(DataGroupInfo dgi, WFSDataType dti)
    {
        myStateDataTypes.add(dti);
        myStateDataGroups.add(dgi);
        myDataTypes.add(dti);
        dti.setActivationListener(myActivationListener);
    }

    @Override
    public void close()
    {
        dumpFeatures(false, true);
        getDataRegistry().removeModels(getMyTypeCategory(), false);
    }

    /**
     * Provides state handlers a way to clean up when states are deactivated.
     */
    public void deactivateState()
    {
        deactivateLayers(myStateDataTypes);
        for (WFSDataType dti : myStateDataTypes)
        {
            myDataTypes.remove(dti);
        }
        myStateDataTypes.clear();
        myStateDataGroups.clear();
    }

    /**
     * Dump the features that this envoy queried.
     *
     * @param timedOnly Only remove features from layers with time constraints
     * @param forceAll Remove all features from all layers (overrides timedOnly
     *            parameter)
     */
    public void dumpFeatures(boolean timedOnly, boolean forceAll)
    {
        for (WFSDataType type : getMyTypesFromRegistry())
        {
            if (!type.isAnimationSensitive() && (forceAll || !timedOnly && type.isQueryable() || timedOnly && !type.isTimeless()))
            {
                myConsumerManager.removeType(type);
                getDataRegistry().removeModels(getMyQueryCategory(type.getTypeKey()), false);
            }
        }
    }

    /**
     * Gets the getCapabilities URL.
     *
     * @return the getCapabilities URL
     */
    public String getGetCapabilitiesURL()
    {
        return myServerConfig == null ? null : myServerConfig.getWfsUrl();
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets)
    {
        return SingleSatisfaction.generateSatisfactions(intervalSets);
    }

    @Override
    public String getThreadPoolName()
    {
        return "WFS:" + myServerConfig.getWfsUrl();
    }

    @Override
    public void open()
    {
        WFSServerConfig wfsServer = new WFSServerConfig();
        String errorString = null;
        try
        {
            populateWFSServerConfig(wfsServer);
        }
        catch (OGCServerException e)
        {
            errorString = e.getMessage();
        }

        Collection<WFSDataType> types = CollectionUtilities.filterDowncast(wfsServer.getLayers(), WFSDataType.class);
        types.forEach(t -> t.setActivationListener(myActivationListener));
        myDataTypes.addAll(types);

        fireStateEvent(wfsServer, wfsServer.getServerState() == WFSServerState.ACTIVE, errorString,
                getToolbox().getEventManager());
    }

    @Override
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean providesDataFor(DataModelCategory category)
    {
        DataModelCategory cat = getMyQueryCategory(null);
        if (category.getSource() != null && !cat.getSource().equals(category.getSource()))
        {
            return false;
        }
        if (category.getFamily() != null && !cat.getFamily().equals(category.getFamily()))
        {
            return false;
        }
        if (category.getCategory() != null && getWFSTypeFromRegistry(category.getCategory()) == null)
        {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
                throws InterruptedException, QueryException
    {
        WFSDataType wfsType = getWFSTypeFromRegistry(category.getCategory());
        checkParameters(category, satisfactions, wfsType);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Received query: " + satisfactions);
        }
        for (Satisfaction sat : satisfactions)
        {
            ThreadControl.check();
            Collection<? extends Geometry> geometries = sat.getIntervalPropertyValueSet()
                    .getValues(GeometryAccessor.PROPERTY_DESCRIPTOR);

            Collection<? extends TimeSpan> timeSpans = sat.getIntervalPropertyValueSet()
                    .getValues(TimeSpanAccessor.PROPERTY_DESCRIPTOR);
            UnserializableAccessor<MapDataElement, MapDataElement> mdeAccessor = UnserializableAccessor
                    .getHomogeneousAccessor(WFSDataRegistryHelper.MDE_PROPERTY_DESCRIPTOR);

            Collection<? extends DataFilter> dataFilters = sat.getIntervalPropertyValueSet()
                    .getValues(WFSDataRegistryHelper.DATA_FILTER_PROPERTY_DESCRIPTOR);
            if (dataFilters == null)
            {
                dataFilters = Collections.singleton(null);
            }

            for (DataFilter dataFilter : dataFilters)
            {
                ThreadControl.check();
                IntervalPropertyAccessor<Object, DataFilter> dataFilterAccessor = new DataFilterAccessor(dataFilter);

                for (TimeSpan timeSpan : timeSpans)
                {
                    ThreadControl.check();

                    TimeSpanAccessor<MapDataElement> timeSpanAccessor = WFSDataRegistryHelper.createTimeSpanAccessor(timeSpan);
                    for (Geometry geometry : geometries)
                    {
                        final Collection<PropertyAccessor<? super MapDataElement, ?>> accessors = New.list(3);
                        if (timeSpanAccessor != null)
                        {
                            accessors.add(timeSpanAccessor);
                        }
                        accessors.add(WFSDataRegistryHelper.createGeometryAccessor(geometry));
                        accessors.add(dataFilterAccessor);
                        accessors.add(mdeAccessor);

                        int numFeatures = -1;
                        try
                        {
                            myDownloadMonitor.addDownload(wfsType, timeSpan, geometry);
                            numFeatures = getFeatures(wfsType, timeSpan, geometry, dataFilter);
                        }
                        catch (OGCServerException e)
                        {
                            if (e.getCause() instanceof InterruptedException || e.getCause() instanceof RuntimeException)
                            {
                                if (LOGGER.isDebugEnabled())
                                {
                                    LOGGER.debug("Request was interrupted.");
                                }
                                throw new InterruptedException();
                            }
                            else
                            {
                                throw new QueryException("Failed to get features from server: " + e, e);
                            }
                        }
                        catch (OutOfMemoryError e)
                        {
                            System.gc();
                            throw new QueryException("Out of memory error during query.");
                        }
                        finally
                        {
                            myDownloadMonitor.removeDownload(wfsType, timeSpan, geometry, numFeatures);
                        }

                        CacheDeposit<MapDataElement> deposit = new DefaultCacheDeposit<>(getMyQueryCategory(wfsType.getTypeKey()),
                                accessors, Collections.<MapDataElement>emptyList(), true, CacheDeposit.SESSION_END, true);
                        try
                        {
                            queryReceiver.receive(deposit);
                        }
                        catch (CacheException e)
                        {
                            LOGGER.warn("Failed to deposit query region: " + e, e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder instance = new StringBuilder(getClass().getSimpleName());
        String serverId = myServerConfig == null ? "" : myServerConfig.getServerId(OGCServerSource.WFS_SERVICE);
        if (serverId != null && !serverId.isEmpty())
        {
            instance.append(':').append(serverId);
        }
        return instance.toString();
    }

    /**
     * Activate a set of layers. This sends the layers to the
     * {@link io.opensphere.core.data.DataRegistry} after which time they can
     * be considered "Active"
     *
     * @param typesToActivate the collection of types to activate.
     * @return true, if layers activated successfully
     * @throws InterruptedException If the request is cancelled.
     */
    protected boolean activateLayers(Collection<WFSDataType> typesToActivate) throws InterruptedException
    {
        /* Let the user choose time column(s) for servers with multiple time
         * columns. */
        for (WFSDataType dataType : typesToActivate)
        {
            if (dataType.isTimeColumnChangeable() && !dataType.isTimeColumnChosen() && !dataType.setPreferredTimeColumns(this))
            {
                dataType.chooseTimeColumns(this);
            }
        }

        notifyConsumers(typesToActivate);

        // Deposit this so that WFSControls can receive it
        DataModelCategory dataModelCategory = getMyTypeCategory();
        Collection<PropertyAccessor<WFSDataType, ?>> accessors = New.collection();
        accessors.add(UnserializableAccessor.getHomogeneousAccessor(WFSDataType.WFS_PROPERTY_DESCRIPTOR));
        accessors.add(new SerializableAccessor<WFSDataType, String>(KEY_PROPERTY_DESCRIPTOR)
        {
            @Override
            public String access(WFSDataType input)
            {
                return input.getTypeKey();
            }
        });
        CacheDeposit<WFSDataType> deposit = new DefaultCacheDeposit<>(dataModelCategory, accessors, typesToActivate, true,
                CacheDeposit.SESSION_END, true);
        long[] ids = getDataRegistry().addModels(deposit);

        // This also goes to WFSControls
        for (WFSDataType type : typesToActivate)
        {
            // If type does not support BoundingBox queries, request data now
            if (!type.isQueryable() && !type.getStreamingSupport().isStreamingEnabled())
            {
                SingleLayerRequeryEvent queryEvent = new SingleLayerRequeryEvent(type, RequeryType.FULL_REQUERY);
                getToolbox().getEventManager().publishEvent(queryEvent);
            }
        }

        return ids.length > 0;
    }

    /**
     * Notifies all registered consumers that the supplied types have been activated.
     *
     * @param activatedTypes the set of types that were activated.
     */
    protected void notifyConsumers(Collection<WFSDataType> activatedTypes)
    {
        for (Consumer<Collection<WFSDataType>> consumer : myActivationConsumers)
        {
            consumer.accept(activatedTypes);
        }
    }

    /**
     * Gets the column manager.
     *
     * @return the column manager
     */
    protected WFSLayerColumnManager getColumnManager()
    {
        return myColumnManager;
    }

    /**
     * Get a connector for HTTP POST requests from the specified URL.
     *
     * @param url The WFS URL.
     * @param postParams the InputStream to be included as the body of an HTTP
     *            POST request
     * @param serverProvider The registry of server providers.
     * @return The connector.
     */
    protected OGCServerConnector getConnector(URL url, InputStream postParams, ServerProviderRegistry serverProvider)
    {
        return new OGCServerConnector(url, postParams, serverProvider);
    }

    /**
     * Get a connector for HTTP POST requests from the specified URL.
     *
     * @param url The WFS URL.
     * @param postParams the parameters to be included as the body of an HTTP
     *            POST request
     * @param serverProvider The registry of server providers.
     * @return The connector.
     */
    protected OGCServerConnector getConnector(URL url, Map<String, String> postParams, ServerProviderRegistry serverProvider)
    {
        return new OGCServerConnector(url, postParams, serverProvider);
    }

    /**
     * Get a connector for HTTP GET requests from the specified URL.
     *
     * @param url The WFS URL.
     * @param serverProvider The registry of server providers.
     * @return The connector.
     */
    protected OGCServerConnector getConnector(URL url, ServerProviderRegistry serverProvider)
    {
        return new OGCServerConnector(url, serverProvider);
    }

    /**
     * Gets the feature consumer manager.
     *
     * @return the consumer manager
     */
    protected FeatureConsumerManager getConsumerManager()
    {
        return myConsumerManager;
    }

    /**
     * Gets the data types.
     *
     * @return the data types
     */
    protected Collection<WFSDataType> getDataTypes()
    {
        return New.set(myDataTypes);
    }

    /**
     * Request features from the server and convert them into map data elements.
     *
     * @param wfsType The WFS type.
     * @param timeSpan The query time span.
     * @param geometry The query geometry.
     * @param dataFilter The filter on the query.
     * @return The number of processed features.
     * @throws OGCServerException If features could not be retrieved from the
     *             server.
     */
    protected abstract int getFeatures(WFSDataType wfsType, TimeSpan timeSpan, Geometry geometry, DataFilter dataFilter)
        throws OGCServerException;

    /**
     * Gets a DataModelCategory that's used to store data and query regions from
     * this Envoy for a specific type.
     *
     * @param typeKey the unique type key to query
     * @return the my query category
     */
    protected DataModelCategory getMyQueryCategory(String typeKey)
    {
        return new DataModelCategory(toString(), MapDataElement.class.getName(), typeKey);
    }

    /**
     * Gets a DataModelCategory that's used to store types from this Envoy.
     *
     * @return the category
     */
    protected DataModelCategory getMyTypeCategory()
    {
        String family = WFSDataType.class.getName();
        String category = "";
        return new DataModelCategory(toString(), family, category);
    }

    /**
     * Get this Envoy's WFSTypes from the registry.
     *
     * @return the types from the registry
     */
    protected Collection<WFSDataType> getMyTypesFromRegistry()
    {
        // Get this Envoy's active WFSTypes from the registry
        SimpleQuery<WFSDataType> query = new SimpleQuery<>(getMyTypeCategory(), WFSDataType.WFS_PROPERTY_DESCRIPTOR);
        getDataRegistry().performLocalQuery(query);
        return query.getResults();
    }

    /**
     * Gets the server config.
     *
     * @return the server config
     */
    protected ServerConnectionParams getServerConfig()
    {
        return myServerConfig;
    }

    /**
     * Get the configured server URL.
     *
     * @return The server URL.
     */
    protected String getServerURL()
    {
        return getServerConfig().getWfsUrl();
    }

    /**
     * Get a WFS type from the registry.
     *
     * @param typeKey The type key.
     * @return The type, or {@code null} if it wasn't found.
     */
    protected WFSDataType getWFSTypeFromRegistry(String typeKey)
    {
        WFSDataType wfsType = null;
        for (WFSDataType type : getMyTypesFromRegistry())
        {
            if (type.getTypeKey().equals(typeKey))
            {
                wfsType = type;
                break;
            }
        }
        return wfsType;
    }

    /**
     * Populates the given WFSServerConfig.
     *
     * @param wfsServer the WFSServerConfig
     * @throws OGCServerException if an exception occurs getting the data from
     *             the server
     */
    protected abstract void populateWFSServerConfig(WFSServerConfig wfsServer) throws OGCServerException;

    /**
     * Check parameters.
     *
     * @param category The category.
     * @param satisfactions The query tracker.
     * @param wfsType The WFS type,
     */
    private void checkParameters(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            WFSDataType wfsType)
    {
        if (wfsType == null)
        {
            throw new IllegalArgumentException("Unknown WFSType: " + category.getCategory());
        }

        if (satisfactions == null)
        {
            throw new IllegalArgumentException("Received a query with null satisfactions. This envoy requires satisfactions.");
        }
    }

    /**
     * De-activate a set of layers. This removes the layers from the
     * {@link io.opensphere.core.data.DataRegistry} after which time they can
     * be considered "Not Active." This method also removes any queried data for
     * the specified layers from the feature cache.
     *
     * @param typesToDeactivate the collection of types to deactivate.
     * @return true, if types were deactivated successfully
     */
    private boolean deactivateLayers(Collection<WFSDataType> typesToDeactivate)
    {
        PropertyMatcher<String> matcher = null;

        if (typesToDeactivate == null || typesToDeactivate.isEmpty())
        {
            return false;
        }
        else if (typesToDeactivate.size() == 1)
        {
            matcher = new StringPropertyMatcher(KEY_PROPERTY_DESCRIPTOR, typesToDeactivate.iterator().next().getTypeKey());
        }
        else
        {
            Set<String> typeKeys = New.set(typesToDeactivate.size());
            for (WFSDataType type : typesToDeactivate)
            {
                typeKeys.add(type.getTypeKey());
            }
            matcher = new MultiPropertyMatcher<>(KEY_PROPERTY_DESCRIPTOR, typeKeys);
        }

        // Get the IDs for the types from the Registry
        SimpleQuery<WFSDataType> query = new SimpleQuery<>(getMyTypeCategory(), WFSDataType.WFS_PROPERTY_DESCRIPTOR, matcher);
        long[] ids = getDataRegistry().performLocalQuery(query);
        getDataRegistry().removeModels(ids);

        for (WFSDataType type : typesToDeactivate)
        {
            if (!type.isAnimationSensitive())
            {
                myConsumerManager.removeType(type);
                getDataRegistry().removeModels(getMyQueryCategory(type.getTypeKey()), false);
            }
        }

        return ids.length > 0;
    }

    /**
     * Gets the {@link DataTypeInfo}s applicable to this Envoy from a
     * {@link DataGroupInfo}.
     *
     * @param group the {@link DataGroupInfo} from which to extract my types
     * @return the the {@link DataTypeInfo}s for this Envoy from the group
     */
    private Set<WFSDataType> getMyMembersFromGroup(DataGroupInfo group)
    {
        Set<WFSDataType> returnSet = New.set();
        if (group.hasMembers(false))
        {
            for (DataTypeInfo info : group.getMembers(false))
            {
                if (info instanceof WFSDataType && CollectionUtilities.hasContent(myDataTypes) && myDataTypes.contains(info)
                        && !((WFSDataType)info).isAnimationSensitive())
                {
                    returnSet.add((WFSDataType)info);
                }
            }
        }
        return returnSet;
    }
}
