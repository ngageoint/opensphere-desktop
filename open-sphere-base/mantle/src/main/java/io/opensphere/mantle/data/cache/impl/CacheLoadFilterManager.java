package io.opensphere.mantle.data.cache.impl;

import java.util.Map;
import java.util.Set;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.impl.DataFilterRegistryAdapter;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.mdfilter.MetaDataFilter;
import io.opensphere.mantle.data.element.mdfilter.impl.DataFilterEvaluator;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class CacheLoadFilterManager.
 */
public class CacheLoadFilterManager extends DataFilterRegistryAdapter
{
    /** The DTI key to load filter map. */
    private final Map<String, MetaDataFilter> myDTIKeyToLoadFilterMap;

    /** The dynamic enumeration registry. */
    private final DynamicEnumerationRegistry myDynEnumReg;

    /**
     * Instantiates a new cache load filter manager.
     *
     * @param dynEnumReg the dynamic enumeration registry
     */
    public CacheLoadFilterManager(DynamicEnumerationRegistry dynEnumReg)
    {
        myDTIKeyToLoadFilterMap = New.concurrentMap();
        myDynEnumReg = Utilities.checkNull(dynEnumReg, "dynEnumReg");
    }

    /**
     * Gets the filter.
     *
     * @param dti the {@link DataTypeInfo}
     * @param onlyIfNotFilteredByProvider the only if not filtered by provider
     * @return the {@link MetaDataFilter} or null if not found.
     */
    public MetaDataFilter getFilter(DataTypeInfo dti, boolean onlyIfNotFilteredByProvider)
    {
        return dti == null || onlyIfNotFilteredByProvider && dti.providerFiltersMetaData() ? null
                : myDTIKeyToLoadFilterMap.get(dti.getTypeKey());
    }

    /**
     * Gets the filter.
     *
     * @param dtiKey the dti key
     * @return the filter
     */
    public MetaDataFilter getFilter(String dtiKey)
    {
        return myDTIKeyToLoadFilterMap.get(dtiKey);
    }

    /**
     * Checks for load filter.
     *
     * @param dti the dti
     * @return true, if successful
     */
    public boolean hasLoadFilter(DataTypeInfo dti)
    {
        return dti != null && hasLoadFilter(dti.getTypeKey());
    }

    /**
     * Checks for load filter.
     *
     * @param dtiKey the dti key
     * @return true, if successful
     */
    public boolean hasLoadFilter(String dtiKey)
    {
        return myDTIKeyToLoadFilterMap.containsKey(dtiKey);
    }

    @Override
    public void loadFilterAdded(String typeKey, DataFilter filter, Object source)
    {
        if (typeKey != null)
        {
            myDTIKeyToLoadFilterMap.put(typeKey, new DataFilterEvaluator(filter, myDynEnumReg));
        }
    }

    @Override
    public void loadFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
    {
        if (removedFilters != null && !removedFilters.isEmpty())
        {
            for (DataFilter df : removedFilters)
            {
                if (df.getTypeKey() != null)
                {
                    myDTIKeyToLoadFilterMap.remove(df.getTypeKey());
                }
            }
        }
    }
}
