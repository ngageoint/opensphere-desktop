package io.opensphere.mantle.data.element.mdfilter.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.mdfilter.MetaDataFilter;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class DataFilterEvaluator.
 */
public class DataFilterEvaluator implements MetaDataFilter
{
    /** The Columns. */
    private final List<String> myColumns;

    /** The DTI key. */
    private final String myDTIKey;

    /** The Evaluator. */
    private final DataFilterGroupEvaluator myEvaluator;

    /** The Name. */
    private final String myName;

    /**
     * Instantiates a new data filter evaluator.
     *
     * @param aFilter the a filter
     * @param dynEnumReg the dynamic enumeration registry
     */
    public DataFilterEvaluator(DataFilter aFilter, DynamicEnumerationRegistry dynEnumReg)
    {
        Utilities.checkNull(aFilter, "aFilter");
        myDTIKey = aFilter.getTypeKey();
        myName = aFilter.getName();
        myColumns = aFilter.getColumns() == null ? Collections.<String>emptyList() : New.list(aFilter.getColumns());
        myEvaluator = new DataFilterGroupEvaluator(aFilter.getFilterGroup(), dynEnumReg);
    }

    @Override
    public boolean accepts(DataElement element)
    {
        boolean accepts = false;
        if (element != null && element.getDataTypeInfo() != null
                && Objects.equals(myDTIKey, element.getDataTypeInfo().getTypeKey()))
        {
            accepts = myEvaluator.accepts(element);
        }
        return accepts;
    }

    /**
     * Gets the columns.
     *
     * @return the columns
     */
    public List<String> getColumns()
    {
        return Collections.unmodifiableList(myColumns);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the type key.
     *
     * @return the type key
     */
    public String getTypeKey()
    {
        return myDTIKey;
    }
}
