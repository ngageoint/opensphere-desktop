package io.opensphere.filterbuilder.impl;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.filterbuilder.controller.FilterBuilderService;
import io.opensphere.filterbuilder.controller.FilterSet;

/**
 * A default implementation of the {@link FilterBuilderService}.
 */
public class FbServiceImpl implements FilterBuilderService
{
    /**
     * The controller used to manage the service.
     */
    private final FilterBuilderControllerImpl myBuilderController;

    /**
     * Creates a new implementation of the filter service, populated with the supplied value.
     *
     * @param fbc The controller used to manage the service.
     */
    public FbServiceImpl(FilterBuilderControllerImpl fbc)
    {
        myBuilderController = fbc;
    }

    /**
     * {@inheritDoc}
     *
     * @see FilterBuilderService#getFilterSet(java.lang.String)
     */
    @Override
    public FilterSet getFilterSet(String typeKey)
    {
        return myBuilderController.getFilterSet(typeKey, true);
    }

    /**
     * {@inheritDoc}
     *
     * @see FilterBuilderService#editFilterSet(FilterSet)
     */
    @Override
    public boolean editFilterSet(FilterSet fs)
    {
        return myBuilderController.editFilterSet(fs);
    }

    /**
     * {@inheritDoc}
     *
     * @see FilterBuilderService#fuseFilters(FilterSet)
     */
    @Override
    public DataFilter fuseFilters(FilterSet fs)
    {
        return myBuilderController.fuseFilters(fs);
    }
}
