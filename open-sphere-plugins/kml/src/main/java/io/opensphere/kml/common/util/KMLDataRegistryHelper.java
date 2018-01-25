package io.opensphere.kml.common.util;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.InputStreamAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.StringPropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.QueryTrackerListenerAdapter;
import io.opensphere.core.data.util.SimpleIdQuery;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;

/**
 * Helper class for interacting with the data registry.
 */
public final class KMLDataRegistryHelper
{
    /** Property descriptor for the active flag. */
    public static final PropertyDescriptor<Boolean> ACTIVE_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("Active",
            Boolean.class);

    /** The KMLDataEvent property descriptor. */
    public static final PropertyDescriptor<KMLDataEvent> DATA_EVENT_PROPERTY_DESCRIPTOR = PropertyDescriptor
            .create("KMLDataEvent", KMLDataEvent.class);

    /** Property descriptor for the data source name in the data registry. */
    public static final PropertyDescriptor<String> DATA_SOURCE_NAME_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("name",
            String.class);

    /** Property descriptor for the data source in the data registry. */
    public static final PropertyDescriptor<KMLDataSource> DATA_SOURCE_PROPERTY_DESCRIPTOR = PropertyDescriptor
            .create("datasource", KMLDataSource.class);

    /** Property descriptor for display names. */
    public static final PropertyDescriptor<String> DISPLAY_NAME_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("DISPLAY_NAME",
            String.class);

    /** Property descriptor for icons. */
    public static final PropertyDescriptor<InputStream> ICON_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("Icon",
            InputStream.class);

    /** The data model category family for KML. */
    public static final String KML_CATEGORY_FAMILY = "KML";

    /** The data model category family for KML icons. */
    public static final String KML_ICON_CATEGORY_FAMILY = "KML_ICON";

    /** The data model category family for old KML data. */
    public static final String KML_OLD_CATEGORY_FAMILY = "KML_OLD";

    /** Property descriptor for URLs. */
    public static final PropertyDescriptor<String> URL_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("URL", String.class);

    /**
     * Add a listener for data source changes in the data registry.
     *
     * @param dataRegistry The data registry.
     * @param dataRegistryListener The listener.
     */
    public static void addDataSourceChangeListener(DataRegistry dataRegistry,
            DataRegistryListener<KMLDataSource> dataRegistryListener)
    {
        dataRegistry.addChangeListener(dataRegistryListener, getDataSourceDataModelCategory(Nulls.STRING, Nulls.STRING),
                DATA_SOURCE_PROPERTY_DESCRIPTOR);
    }

    /**
     * Clear some data in the data registry.
     *
     * @param dataRegistry The data registry.
     * @param dataSource The data source.
     */
    public static void clearData(DataRegistry dataRegistry, KMLDataSource dataSource)
    {
        DataModelCategory dataModelCategory = getKmlCategory(dataSource, Nulls.STRING);

        SimpleIdQuery query = new SimpleIdQuery(dataModelCategory, dataSource.getPath(), URL_PROPERTY_DESCRIPTOR);
        long[] ids = dataRegistry.performLocalQuery(query);
        if (ids.length > 0)
        {
            dataRegistry.removeModels(ids);
        }
    }

    /**
     * Clear some data in the data registry.
     *
     * @param dataRegistry The data registry.
     * @param dataSource The data source.
     */
    public static void clearIconData(DataRegistry dataRegistry, KMLDataSource dataSource)
    {
        DataModelCategory dataModelCategory = getIconCategory(dataSource, Nulls.STRING);
        dataRegistry.removeModels(dataModelCategory, false);
    }

    /**
     * Create a data registry deposit for an icon input stream.
     *
     * @param categorySource The source for the data model category.
     * @param dataSource The data source.
     * @param url The url used to retrieve the data.
     * @param iconInputStream The input stream.
     * @return The cache deposit.
     */
    public static CacheDeposit<InputStream> createCacheDeposit(String categorySource, KMLDataSource dataSource, String url,
            InputStream iconInputStream)
    {
        PropertyAccessor<InputStream, String> urlAccessor = SerializableAccessor
                .<InputStream, String>getSingletonAccessor(URL_PROPERTY_DESCRIPTOR, url);
        PropertyAccessor<InputStream, InputStream> inputStreamAccessor = InputStreamAccessor
                .getHomogeneousAccessor(ICON_PROPERTY_DESCRIPTOR);
        Collection<? extends PropertyAccessor<InputStream, ?>> accessors = Arrays.asList(urlAccessor, inputStreamAccessor);
        DataModelCategory category = getIconCategory(dataSource, categorySource);
        return new DefaultCacheDeposit<InputStream>(category, accessors, Collections.singleton(iconInputStream), true,
                CacheDeposit.SESSION_END, true);
    }

    /**
     * Create a data registry deposit for KML data.
     *
     * @param categorySource The source for the data model category.
     * @param dataSource The data source.
     * @param url The url used to retrieve the data.
     * @param displayName The display name for this KML data.
     * @param event The data event.
     * @return The cache deposit.
     */
    public static CacheDeposit<KMLDataEvent> createCacheDeposit(String categorySource, KMLDataSource dataSource, String url,
            String displayName, KMLDataEvent event)
    {
        PropertyAccessor<KMLDataEvent, String> urlAccessor = SerializableAccessor
                .<KMLDataEvent, String>getSingletonAccessor(URL_PROPERTY_DESCRIPTOR, url);
        PropertyAccessor<KMLDataEvent, String> displayNameAccessor = SerializableAccessor
                .<KMLDataEvent, String>getSingletonAccessor(DISPLAY_NAME_PROPERTY_DESCRIPTOR, displayName);
        PropertyAccessor<KMLDataEvent, KMLDataEvent> dataEventAccessor = UnserializableAccessor
                .getHomogeneousAccessor(DATA_EVENT_PROPERTY_DESCRIPTOR);
        PropertyAccessor<KMLDataEvent, Boolean> activeAccessor = SerializableAccessor
                .<KMLDataEvent, Boolean>getSingletonAccessor(ACTIVE_PROPERTY_DESCRIPTOR, Boolean.FALSE);
        Collection<? extends PropertyAccessor<KMLDataEvent, ?>> accessors = Arrays.asList(urlAccessor, dataEventAccessor,
                activeAccessor, displayNameAccessor);
        DataModelCategory category = getKmlCategory(dataSource, categorySource);
        return new DefaultCacheDeposit<KMLDataEvent>(category, accessors, Collections.singleton(event), true,
                CacheDeposit.SESSION_END, true);
    }

    /**
     * Deposit KML data sources into the data registry.
     *
     * @param dataRegistry The data registry.
     * @param source The source of the data sources.
     * @param serverId The server id.
     * @param kmlDataSources The data sources.
     */
    public static void depositDataSources(DataRegistry dataRegistry, String source, String serverId,
            Collection<? extends KMLDataSource> kmlDataSources)
    {
        Date expiration = CacheDeposit.SESSION_END;

        Collection<PropertyAccessor<KMLDataSource, ?>> accessors = New.collection();
        accessors.add(UnserializableAccessor.getHomogeneousAccessor(DATA_SOURCE_PROPERTY_DESCRIPTOR));
        accessors.add(new SerializableAccessor<KMLDataSource, String>(DATA_SOURCE_NAME_PROPERTY_DESCRIPTOR)
        {
            @Override
            public String access(KMLDataSource input)
            {
                return input.getName();
            }
        });
        CacheDeposit<KMLDataSource> deposit = new DefaultCacheDeposit<KMLDataSource>(
                getDataSourceDataModelCategory(source, serverId), accessors, kmlDataSources, true, expiration, true);
        dataRegistry.addModels(deposit);
        for (KMLDataSource ds : kmlDataSources)
        {
            ds.waitForHandler(Constants.MILLI_PER_UNIT);
        }
    }

    /**
     * Query the data registry for some KML data and mark the data active.
     *
     * @param dataRegistry The data registry.
     * @param dataSource The data source.
     * @param url The url.
     * @param displayName Optional display name to use for the results.
     * @return The tracker for the query.
     */
    public static QueryTracker queryAndActivate(final DataRegistry dataRegistry, KMLDataSource dataSource, String url,
            String displayName)
    {
        DataModelCategory dataModelCategory = getKmlCategory(dataSource, Nulls.STRING);

        // Listener that will be called once the query is complete.
        QueryTrackerListenerAdapter listener = new QueryTrackerListenerAdapter()
        {
            /** Flag indicating if the listener has already been called. */
            private final AtomicBoolean myDone = new AtomicBoolean();

            @Override
            public void statusChanged(QueryTracker tracker, QueryStatus status)
            {
                tracker.logException();
                if (myDone.compareAndSet(false, true) && status == QueryStatus.SUCCESS)
                {
                    activate(dataRegistry, tracker.getIds());
                }
            }
        };

        // Set the URL and display name parameters on the query.
        List<PropertyMatcher<?>> params = New.list(2);
        params.add(new StringPropertyMatcher(URL_PROPERTY_DESCRIPTOR, url));
        if (displayName != null)
        {
            params.add(new StringPropertyMatcher(DISPLAY_NAME_PROPERTY_DESCRIPTOR, displayName));
        }

        // Run the query.
        SimpleIdQuery query = new SimpleIdQuery(dataModelCategory, params);
        QueryTracker tracker = dataRegistry.submitQuery(query);
        tracker.addListener(listener);

        // Just in case the query finished before the tracker was added.
        if (tracker.isDone())
        {
            listener.statusChanged(tracker, tracker.getQueryStatus());
        }

        return tracker;
    }

    /**
     * Query the data registry for some KML data and deactivate the data.
     *
     * @param dataRegistry The data registry.
     * @param dataSource The data source.
     * @param url Optional url.
     */
    public static void queryAndDeactivate(DataRegistry dataRegistry, KMLDataSource dataSource, String url)
    {
        DataModelCategory dataModelCategory = getKmlCategory(dataSource, Nulls.STRING);

        SimpleIdQuery query = url == null ? new SimpleIdQuery(dataModelCategory)
                : new SimpleIdQuery(dataModelCategory, url, URL_PROPERTY_DESCRIPTOR);
        long[] ids = dataRegistry.performLocalQuery(query);

        if (ids.length > 0)
        {
            deactivate(dataRegistry, ids);
        }
    }

    /**
     * Query the data registry for some old KML data. This also removes the old
     * data.
     *
     * @param dataRegistry The data registry.
     * @param dataSource The data source.
     * @param url The url.
     * @return The old data, or {@code null} if none was found.
     */
    public static KMLDataEvent queryAndRemoveOldData(final DataRegistry dataRegistry, KMLDataSource dataSource, String url)
    {
        DataModelCategory dataModelCategory = getKmlOldCategory(dataSource, Nulls.STRING);

        SimpleQuery<KMLDataEvent> query = new SimpleQuery<KMLDataEvent>(dataModelCategory, DATA_EVENT_PROPERTY_DESCRIPTOR,
                new StringPropertyMatcher(URL_PROPERTY_DESCRIPTOR, url));
        long[] ids = dataRegistry.performLocalQuery(query);
        if (ids.length > 0)
        {
            dataRegistry.removeModels(ids);
        }
        return ids.length > 0 ? query.getResults().get(0) : null;
    }

    /**
     * Query the data registry for an icon.
     *
     * @param dataRegistry The data registry.
     * @param dataSource The data source.
     * @param url The url for the icon.
     * @return The input stream, or {@code null}.
     */
    public static InputStream queryAndReturn(DataRegistry dataRegistry, KMLDataSource dataSource, String url)
    {
        DataModelCategory dataModelCategory = getIconCategory(dataSource, Nulls.STRING);

        List<PropertyMatcher<?>> params = New.list(2);
        params.add(new StringPropertyMatcher(URL_PROPERTY_DESCRIPTOR, url));
        SimpleQuery<InputStream> query = new SimpleQuery<>(dataModelCategory, ICON_PROPERTY_DESCRIPTOR, params);
        dataRegistry.performQuery(query).logException();
        if (!query.getResults().isEmpty())
        {
            return query.getResults().get(0);
        }

        return null;
    }

    /**
     * Reload some data in the data registry.
     *
     * @param dataRegistry The data registry.
     * @param dataSource The data source.
     */
    public static void reloadData(DataRegistry dataRegistry, KMLDataSource dataSource)
    {
        DataModelCategory dataModelCategory = getKmlCategory(dataSource, Nulls.STRING);

        SimpleQuery<KMLDataEvent> query = new SimpleQuery<KMLDataEvent>(dataModelCategory, DATA_EVENT_PROPERTY_DESCRIPTOR,
                new StringPropertyMatcher(URL_PROPERTY_DESCRIPTOR, dataSource.getPath()));
        long[] ids = dataRegistry.performLocalQuery(query);
        if (ids.length > 0)
        {
            List<KMLDataEvent> results = query.getResults();
            dataRegistry.removeModels(ids);

            // Put the old data in the data registry for use in the reload.
            if (results.size() == 1)
            {
                depositOldData(dataRegistry, dataSource, dataSource.getPath(), results.get(0));
            }
        }

        // Perform the query to load the data.
        queryAndActivate(dataRegistry, dataSource, dataSource.getPath(), Nulls.STRING);
    }

    /**
     * Remove data sources from the data registry.
     *
     * @param dataRegistry The data registry.
     * @param sourceFilter Optional filter on the sources removed.
     * @param kmlDataSources The data sources.
     */
    public static void removeDataSources(DataRegistry dataRegistry, String sourceFilter,
            Collection<? extends KMLDataSource> kmlDataSources)
    {
        SimpleQuery<KMLDataSource> query = new SimpleQuery<KMLDataSource>(
                getDataSourceDataModelCategory(sourceFilter, Nulls.STRING), DATA_SOURCE_PROPERTY_DESCRIPTOR);
        long[] ids = dataRegistry.performLocalQuery(query);
        if (ids.length > 0)
        {
            TLongList idsToRemove = new TLongArrayList(ids.length);
            for (int index = 0; index < ids.length; ++index)
            {
                if (kmlDataSources.contains(query.getResults().get(index)))
                {
                    idsToRemove.add(ids[index]);
                }
            }
            dataRegistry.removeModels(idsToRemove.toArray());
        }
    }

    /**
     * Remove a data source from the data registry.
     *
     * @param dataRegistry The data registry.
     * @param sourceFilter Optional filter on the sources removed.
     * @param dataSourceNameFilter Optional filter on the data source name.
     */
    public static void removeDataSources(DataRegistry dataRegistry, String sourceFilter, String dataSourceNameFilter)
    {
        dataRegistry.removeModels(getDataSourceDataModelCategory(sourceFilter, dataSourceNameFilter), false);
    }

    /**
     * Creates an KML DataModelCategory.
     *
     * @param dataSource the data source
     * @param source the DataModelCategory source
     * @return the DataModelCategory
     */
    public static DataModelCategory getKmlCategory(KMLDataSource dataSource, String source)
    {
        return new DataModelCategory(source, KML_CATEGORY_FAMILY, dataSource.getPath());
    }

    /**
     * Creates an KML old DataModelCategory.
     *
     * @param dataSource the data source
     * @param source the DataModelCategory source
     * @return the DataModelCategory
     */
    public static DataModelCategory getKmlOldCategory(KMLDataSource dataSource, String source)
    {
        return new DataModelCategory(source, KML_OLD_CATEGORY_FAMILY, dataSource.getPath());
    }

    /**
     * Creates an icon DataModelCategory.
     *
     * @param dataSource the data source
     * @param source the DataModelCategory source
     * @return the DataModelCategory
     */
    public static DataModelCategory getIconCategory(KMLDataSource dataSource, String source)
    {
        return new DataModelCategory(source, KML_ICON_CATEGORY_FAMILY, dataSource.getPath());
    }

    /**
     * Activate some KML data.
     *
     * @param dataRegistry The data registry.
     * @param ids The data registry ids for the data.
     */
    private static void activate(DataRegistry dataRegistry, long[] ids)
    {
        if (ids.length > 0)
        {
            Collection<Boolean> input = Collections.singleton(Boolean.TRUE);
            Collection<? extends PropertyAccessor<Boolean, Boolean>> accessors = Collections
                    .singleton(SerializableAccessor.getHomogeneousAccessor(ACTIVE_PROPERTY_DESCRIPTOR));
            dataRegistry.updateModels(ids, input, accessors, (CacheModificationListener)null);
        }
    }

    /**
     * Deactivate some KML data.
     *
     * @param dataRegistry The data registry.
     * @param ids The data registry ids for the data.
     */
    private static void deactivate(DataRegistry dataRegistry, long[] ids)
    {
        Collection<Boolean> input = Collections.singleton(Boolean.FALSE);
        Collection<? extends PropertyAccessor<Boolean, Boolean>> accessors = Collections
                .singleton(SerializableAccessor.getHomogeneousAccessor(ACTIVE_PROPERTY_DESCRIPTOR));
        dataRegistry.updateModels(ids, input, accessors, (CacheModificationListener)null);
    }

    /**
     * Deposit old KML data for use in a reload.
     *
     * @param dataRegistry The data registry.
     * @param dataSource The data source.
     * @param url The url used to retrieve the data.
     * @param event The data event.
     */
    private static void depositOldData(DataRegistry dataRegistry, KMLDataSource dataSource, String url, KMLDataEvent event)
    {
        PropertyAccessor<KMLDataEvent, String> urlAccessor = SerializableAccessor
                .<KMLDataEvent, String>getSingletonAccessor(URL_PROPERTY_DESCRIPTOR, url);
        PropertyAccessor<KMLDataEvent, KMLDataEvent> dataEventAccessor = UnserializableAccessor
                .getHomogeneousAccessor(DATA_EVENT_PROPERTY_DESCRIPTOR);
        Collection<? extends PropertyAccessor<KMLDataEvent, ?>> accessors = Arrays.asList(urlAccessor, dataEventAccessor);
        DataModelCategory category = getKmlOldCategory(dataSource, KMLDataRegistryHelper.class.getSimpleName());
        DefaultCacheDeposit<KMLDataEvent> deposit = new DefaultCacheDeposit<KMLDataEvent>(category, accessors,
                Collections.singleton(event), true, CacheDeposit.SESSION_END, true);
        dataRegistry.addModels(deposit);
    }

    /**
     * Get the data model category for a data source.
     *
     * @param source The source of the data source.
     * @param dataSourceName The name of the data source.
     * @return The data model category for use in the data registry.
     */
    private static DataModelCategory getDataSourceDataModelCategory(String source, String dataSourceName)
    {
        return new DataModelCategory(source, KMLDataSource.class.getName(), dataSourceName);
    }

    /** Disallow instantiation. */
    private KMLDataRegistryHelper()
    {
    }
}
