package io.opensphere.core.datafilter.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.datafilter.DataFilterRegistryListener;
import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.datafilter.columns.ColumnMappingControllerImpl;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.FixedThreadPoolExecutor;
import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * The Class DataFilterRegistryImpl.
 */
public class DataFilterRegistryImpl implements DataFilterRegistry
{
    /** The column mapping controller. */
    private final ColumnMappingControllerImpl myColumnMappingController;

    /** Map of ids to registered filters. */
    private final Map<String, ImmutableDataFilter> myRegisteredFilters = new ConcurrentHashMap<>();

    /** The type key to load filter map. */
    private final Map<String, DataFilter> myTypeKeyToLoadFilterMap = new ConcurrentHashMap<>();

    /** The type key to spatial filter map. */
    private final Map<String, Geometry> myTypeKeyToSpatialFilterMap = new ConcurrentHashMap<>();

    /** The change support. */
    private final ChangeSupport<DataFilterRegistryListener> myChangeSupport = new WeakChangeSupport<>();

    /** The executor service. */
    private final Executor myEventExecutor = new FixedThreadPoolExecutor(1, new NamedThreadFactory("DataFilterRegistry"));

    /**
     * Constructor.
     *
     * @param preferencesRegistry The preferences registry
     */
    public DataFilterRegistryImpl(PreferencesRegistry preferencesRegistry)
    {
        myColumnMappingController = new ColumnMappingControllerImpl(preferencesRegistry);
        myColumnMappingController.initialize();
    }

    @Override
    public ColumnMappingController getColumnMappingController()
    {
        return myColumnMappingController;
    }

    @Override
    public void registerFilter(String id, DataFilter filter)
    {
        myRegisteredFilters.put(id,
                filter instanceof ImmutableDataFilter ? (ImmutableDataFilter)filter : new ImmutableDataFilter(filter, this));
    }

    @Override
    public ImmutableDataFilter deregisterFilter(String id)
    {
        return myRegisteredFilters.remove(id);
    }

    @Override
    public DataFilter getRegisteredFilter(String id)
    {
        return myRegisteredFilters.get(id);
    }

    @Override
    public Collection<? extends DataFilter> searchRegisteredFilters(String regex)
    {
        Pattern pattern = Pattern.compile(regex);
        return myRegisteredFilters.entrySet().stream().filter(e -> pattern.matcher(e.getKey()).matches()).map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    @Override
    public long addLoadFilter(DataFilter filter, final Object source)
    {
        final ImmutableDataFilter iFilter = new ImmutableDataFilter(filter, source);
        DataFilter oldFilter = myTypeKeyToLoadFilterMap.put(iFilter.getTypeKey(), iFilter);
        if (oldFilter != null)
        {
            notifyListeners(l -> l.loadFiltersRemoved(Collections.singleton(oldFilter), source));
        }
        notifyListeners(l -> l.loadFilterAdded(iFilter.getTypeKey(), iFilter, source));
        return 0;
    }

    @Override
    public long addSpatialLoadFilter(final String typeKey, final Geometry filter)
    {
        myTypeKeyToSpatialFilterMap.put(typeKey, filter);
        notifyListeners(l -> l.spatialFilterAdded(typeKey, filter));
        return 0;
    }

    @Override
    public DataFilter getLoadFilter(String typeKey)
    {
        return myTypeKeyToLoadFilterMap.get(typeKey);
    }

    @Override
    public Set<DataFilter> getLoadFilters()
    {
        return Collections.unmodifiableSet(New.set(myTypeKeyToLoadFilterMap.values()));
    }

    @Override
    public Geometry getSpatialLoadFilter(String typeKey)
    {
        return myTypeKeyToSpatialFilterMap.get(typeKey);
    }

    @Override
    public Set<String> getSpatialLoadFilterKeys()
    {
        return New.set(myTypeKeyToSpatialFilterMap.keySet());
    }

    @Override
    public boolean hasLoadFilter(String typeKey)
    {
        return myTypeKeyToLoadFilterMap.containsKey(typeKey);
    }

    @Override
    public boolean hasSpatialLoadFilter(String typeKey)
    {
        return myTypeKeyToSpatialFilterMap.containsKey(typeKey);
    }

    @Override
    public boolean removeLoadFilter(final String typeKey, final Object source)
    {
        final DataFilter filter = myTypeKeyToLoadFilterMap.remove(typeKey);
        if (filter != null)
        {
            notifyListeners(l -> l.loadFiltersRemoved(Collections.singleton(filter), source));
        }
        return filter != null;
    }

    @Override
    public boolean removeSpatialLoadFilter(final String typeKey)
    {
        final Geometry filter = myTypeKeyToSpatialFilterMap.remove(typeKey);
        if (filter != null)
        {
            notifyListeners(l -> l.spatialFilterRemoved(typeKey, filter));
        }
        return filter != null;
    }

    @Override
    public void showEditor(String typeKey, DataFilterGroup filter)
    {
        myChangeSupport.notifyListeners(l -> l.showEditor(typeKey, filter));
    }

    @Override
    public void addListener(DataFilterRegistryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void removeListener(DataFilterRegistryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Notifies listeners.
     *
     * @param callback the callback
     */
    private void notifyListeners(ChangeSupport.Callback<DataFilterRegistryListener> callback)
    {
        myChangeSupport.notifyListeners(callback, myEventExecutor);
    }
}
