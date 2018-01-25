package io.opensphere.core.datafilter.impl;

import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterRegistryListener;

/**
 * The Class DataFilterRegistryAdapter.
 */
public class DataFilterRegistryAdapter implements DataFilterRegistryListener
{
    @Override
    public void loadFilterAdded(String typeKey, DataFilter filter, Object source)
    {
    }

    @Override
    public void loadFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
    {
    }

    @Override
    public void showEditor(String typeKey, DataFilterGroup filter)
    {
    }

    @Override
    public void spatialFilterAdded(String typeKey, Geometry filter)
    {
    }

    @Override
    public void spatialFilterRemoved(String typeKey, Geometry filter)
    {
    }

    @Override
    public void viewFilterAdded(String typeKey, DataFilter filter, Object source)
    {
    }

    @Override
    public void viewFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
    {
    }
}
