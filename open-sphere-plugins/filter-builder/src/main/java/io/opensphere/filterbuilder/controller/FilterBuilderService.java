package io.opensphere.filterbuilder.controller;

import io.opensphere.core.datafilter.DataFilter;

/**
 * A service definition in which methods needed to get and edit filter sets are defined.
 */
public interface FilterBuilderService
{
    /**
     * Obtain a copy of the set of filters and logical operation currently
     * assigned to the data type identified by the specified typeKey. The
     * filters returned in this operation do not maintain any relation ship with
     * those from which they were copied.
     *
     * @param typeKey the type identifier for the desired FilterSet
     * @return a FilterSet for the required type
     */
    FilterSet getFilterSet(String typeKey);

    /**
     * Request that a FilterManagerDialog be loaded with the provided FilterSet
     * and presented to the user. The dialog should be initialized in such a way
     * that any changes made to the filters are not saved or persisted outside
     * of the provided FilterSet.
     *
     * @param fs the FilterSet to be edited
     * @return true if and only if the user accepted the changes
     */
    boolean editFilterSet(FilterSet fs);

    /**
     * Convert a FilterSet into a DataFilter by applying the logical operator to
     * the active members of the resident filter list. The result is always a
     * valid filter, even if no filters are active in the FilterSet.
     *
     * @param fs the FilterSet to be converted
     * @return a single DataFilter representing the FilterSet
     */
    DataFilter fuseFilters(FilterSet fs);
}
