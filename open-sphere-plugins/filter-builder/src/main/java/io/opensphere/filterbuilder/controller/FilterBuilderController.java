package io.opensphere.filterbuilder.controller;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.filterbuilder.config.FilterBuilderConfigurationManager;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.FilterChangeListener;
import io.opensphere.filterbuilder.filter.v1.FilterList;

// GCD:  Outside the filter builder source, this interface is only used in one
// place, which is in external tools class FilterCountByToolActivities in
// method createFilterFromBins.  There, only one method is used, which is
// addFilter(DataFilter).  At the very least, this interface is unjustifiably
// large, and in all likelihood, it is totally unnecessary.
/**
 * The Interface FilterBuilderController. Used to define the controller for all
 * filter builder UIs.
 */
public interface FilterBuilderController
{
    /**
     * Adds the filter.
     *
     * @param pFilter the filter
     * @return true, if successful
     */
    boolean addFilter(DataFilter pFilter);

    /**
     * Register updates to the specified filter.
     *
     * @param f the filter
     */
    void updateFilter(Filter f);

    /**
     * Adds a listener to the list that's notified each time a change to the
     * data model occurs.
     *
     * @param listener the <code>FilterChangeListener</code> to be added
     */
    void addFilterChangeListener(FilterChangeListener listener);

    /**
     * Exports filters to the given file.
     *
     * @param file the file
     * @param dataTypeKey the data type key (null means export all data types)
     * @param isActiveOnly whether to export only the active filters
     *
     * @throws JAXBException if a problem occurs writing the file to the file
     *             system.
     */
    void exportFilters(File file, String dataTypeKey, boolean isActiveOnly) throws JAXBException;

    /**
     * Gets all the filters.
     *
     * @return all the filters
     */
    FilterList getAllFilters();

    /**
     * Gets a combination operator.
     *
     * @param dataTypeKey the data type key
     * @return the operator
     */
    ChoiceModel<Logical> getCombinationOperator(String dataTypeKey);

    /**
     * Gets the filters for the given data type.
     *
     * @param dataTypeKey the data type key
     * @return the filters
     */
    List<Filter> getFilters(String dataTypeKey);

    /**
     * Creates virtual filters for the given type key.
     *
     * @param dataTypeKey the type key
     * @return the list of virtual filters if there are any
     */
    List<Filter> createVirtualFilters(String dataTypeKey);

    /**
     * Imports filters from the given file.
     *
     * @param file the file
     */
    void importFilters(File file);

    /**
     * Removes the filter.
     *
     * @param pFilter the filter to remove
     */
    void removeFilter(DataFilter pFilter);

    /**
     * Removes a listener from the list that's notified each time a change to
     * the data model occurs.
     *
     * @param listener the <code>FilterChangeListener</code> to be removed
     */
    void removeFilterChangeListener(FilterChangeListener listener);

    /**
     * Save filters. This will save the filters to the file that is returned by
     * {@link FilterBuilderConfigurationManager#getCurrentFile()}. If that value
     * is null, then the SaveAs dialog will pop up.
     */
    void saveFilters();

    /**
     * Sets the combination operator.
     *
     * @param dataTypeKey the data type key
     * @param logicOp the logic op
     */
    void setCombinationOperator(String dataTypeKey, Logical logicOp);
}
