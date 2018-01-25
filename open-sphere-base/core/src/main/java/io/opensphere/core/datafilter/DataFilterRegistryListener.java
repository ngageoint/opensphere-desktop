package io.opensphere.core.datafilter;

import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

/**
 * DataFilterRegistryListener Interface.
 */
public interface DataFilterRegistryListener
{
    /**
     * Load filter added.
     *
     * @param typeKey the type key
     * @param filter the filter
     * @param source the source of the add
     */
    void loadFilterAdded(String typeKey, DataFilter filter, Object source);

    /**
     * Load filters removed.
     *
     * @param removedFilters the removed filters
     * @param source the source of the remove.
     */
    void loadFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source);

    /**
     * Request that the listener show an editor for a filter.
     *
     * @param typeKey The type key associated with the filter.
     * @param filter The filter.
     */
    void showEditor(String typeKey, DataFilterGroup filter);

    /**
     * Spatial Filter added.
     *
     * @param typeKey the type key
     * @param filter the filter
     */
    void spatialFilterAdded(String typeKey, Geometry filter);

    /**
     * Spatial Filter removed.
     *
     * @param typeKey the type key
     * @param filter the filter
     */
    void spatialFilterRemoved(String typeKey, Geometry filter);

    /**
     * View Filter added.
     *
     * @param typeKey the type key
     * @param filter the filter
     * @param source the source of the add
     */
    void viewFilterAdded(String typeKey, DataFilter filter, Object source);

    /**
     * View filters removed.
     *
     * @param removedFilters the removed filters
     * @param source the source of the remove.
     */
    void viewFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source);
}
