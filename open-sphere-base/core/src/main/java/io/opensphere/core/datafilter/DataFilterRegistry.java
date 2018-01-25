package io.opensphere.core.datafilter;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.columns.ColumnMappingController;

/**
 * The Interface DataFilterRegistry.
 */
public interface DataFilterRegistry
{
    /**
     * Gets the column mapping controller.
     *
     * @return the column mapping controller
     */
    ColumnMappingController getColumnMappingController();

    /**
     * Adds the {@link DataFilterRegistryListener}.
     *
     * Note that listeners are held as weak references.
     *
     * @param listener the listener
     */
    void addListener(DataFilterRegistryListener listener);

    /**
     * Activates a load filter. Returns a unique id for the filter. Creates a
     * deep copy of the provided filter to give to listeners and to provide via
     * lookup type interfaces.
     *
     * If another load filter for the same data type is already in the registry
     * that filter will be removed from the registry.
     *
     * @param filter the filter to activate.
     * @param source the source that is adding the filter
     * @return the unique id for the filter.
     */
    long addLoadFilter(DataFilter filter, Object source);

    /**
     * Adds a spatial load filter for the given type key.
     *
     * @param typeKey the type key
     * @param filter the spatial load filter
     * @return the unique id for the filter.
     */
    long addSpatialLoadFilter(String typeKey, Geometry filter);

    /**
     * De-register a filter.
     *
     * @param id The id of the filter.
     * @return The filter if one was registered.
     */
    DataFilter deregisterFilter(String id);

    /**
     * Gets the current load {@link DataFilter} that belongs to the specified
     * type key.
     *
     * @param typeKey the type key
     * @return the {@link DataFilter} or null if there is none.
     */
    DataFilter getLoadFilter(String typeKey);

    /**
     * Gets complete list of active load {@link DataFilter}s as a {@link Set}.
     *
     * @return the filters
     */
    Set<DataFilter> getLoadFilters();

    /**
     * Retrieve a filter that has been registered.
     *
     * @param id The id of the filter.
     * @return The filter, or {@code null} if not found.
     */
    @Nullable
    DataFilter getRegisteredFilter(String id);

    /**
     * Gets the current spatial load {@link Geometry} filter that belongs to the
     * specified type key.
     *
     * @param typeKey the type key
     * @return the {@link Geometry} filter or null if there is none.
     */
    Geometry getSpatialLoadFilter(String typeKey);

    /**
     * Gets the current spatial load {@link Geometry} filter type keys.
     *
     * @return the Set of {@link Geometry} filter type keys.
     */
    Set<String> getSpatialLoadFilterKeys();

    /**
     * Returns true if there is an established load filter for the specified
     * type key.
     *
     * @param typeKey the type key
     * @return true if there is an established load filter.
     */
    boolean hasLoadFilter(String typeKey);

    /**
     * Returns true if there is an established spatial load filter for the
     * specified type key.
     *
     * @param typeKey the type key
     * @return true if there is an established spatial load filter.
     */
    boolean hasSpatialLoadFilter(String typeKey);

    /**
     * Register a filter with the registry for later looking up. This does not
     * activate the filter or have any other effect aside from making it
     * possible to look up the filter.
     *
     * @param id The id of the filter.
     * @param filter The filter.
     */
    void registerFilter(String id, DataFilter filter);

    /**
     * Removes the {@link DataFilterRegistryListener}.
     *
     * @param listener the listener
     */
    void removeListener(DataFilterRegistryListener listener);

    /**
     * Removes/deactivate the load filter currently active for the specified
     * type. Returns true if removed, false if no current filter for the type
     * key.
     *
     * @param typeKey for which to remove the filter.
     * @param source the source that is removing the filters.
     * @return true if removed, false if not
     */
    boolean removeLoadFilter(String typeKey, Object source);

    /**
     * Removes/deactivate the spatial load filter currently active for the
     * specified type. Returns true if removed, false if no current filter for
     * the type key.
     *
     * @param typeKey for which to remove the filter.
     * @return true if removed, false if not
     */
    boolean removeSpatialLoadFilter(String typeKey);

    /**
     * Search the registered filters and return any whose ids match the given
     * regular expression.
     *
     * @param regex The regex.
     * @return The matching filters.
     */
    Collection<? extends DataFilter> searchRegisteredFilters(String regex);

    /**
     * Show the editor for a filter, if one is registered.
     *
     * @param typeKey The type key.
     * @param filter The filter.
     */
    void showEditor(String typeKey, DataFilterGroup filter);
}
