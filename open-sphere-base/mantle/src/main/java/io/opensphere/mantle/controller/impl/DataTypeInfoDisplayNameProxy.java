package io.opensphere.mantle.controller.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.ToStringProxy;
import io.opensphere.mantle.data.DataTypeInfo;

/** The Class DataTypeInfoDisplayNameProxy. */
public class DataTypeInfoDisplayNameProxy extends ToStringProxy<DataTypeInfo>
{
    /** The Include prefix. */
    private final boolean myIncludePrefix;

    /**
     * Converts a list of items to a list of proxy. Sorts the proxy list using
     * the provided comparator or uses the {@link ToStringComparator} if null.
     *
     * @param itemList the item list to be wrapped in proxy objects.
     * @param comp the {@link Comparator} to use to sort the list (sort by to
     *            sting if null)
     * @return the result list of the proxy objects.
     */
    public static List<DataTypeInfoDisplayNameProxy> toProxyList(Collection<DataTypeInfo> itemList,
            Comparator<DataTypeInfoDisplayNameProxy> comp)
    {
        Utilities.checkNull(itemList, "itemList");
        if (!itemList.isEmpty())
        {
            List<DataTypeInfoDisplayNameProxy> resultList = New.list(itemList.size());
            for (DataTypeInfo item : itemList)
            {
                resultList.add(new DataTypeInfoDisplayNameProxy(item, true));
            }
            Collections.sort(resultList, comp == null ? (x, y) -> x.toString().compareTo(y.toString()) : comp);
            return resultList;
        }
        else
        {
            return Collections.<DataTypeInfoDisplayNameProxy>emptyList();
        }
    }

    /**
     * Instantiates a new data type info display name proxy.
     *
     * @param type the type
     */
    public DataTypeInfoDisplayNameProxy(DataTypeInfo type)
    {
        this(type, false);
    }

    /**
     * Instantiates a new data type info proxy.
     *
     * @param type the type
     * @param includePrefix the include prefix
     */
    public DataTypeInfoDisplayNameProxy(DataTypeInfo type, boolean includePrefix)
    {
        super(type);
        myIncludePrefix = includePrefix;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        return myIncludePrefix == ((DataTypeInfoDisplayNameProxy)obj).myIncludePrefix;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myIncludePrefix ? 1231 : 1237);
        return result;
    }

    @Override
    public String toString()
    {
        return myIncludePrefix ? getItem().getSourcePrefixAndDisplayNameCombo() : getItem().getDisplayName();
    }
}
