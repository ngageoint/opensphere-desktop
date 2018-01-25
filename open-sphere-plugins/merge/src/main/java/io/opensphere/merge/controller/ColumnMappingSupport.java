package io.opensphere.merge.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.merge.algorithm.Col;
import io.opensphere.merge.algorithm.EnvSupport;

/**
 * Provides the data elements and column mappings, used by the merge and join
 * algorithms.
 */
public class ColumnMappingSupport implements EnvSupport
{
    /**
     * Gets the data elements for layers.
     */
    private final DataElementLookupUtils myElementProvider;

    /**
     * Contains the column mapping configurations.
     */
    private final ColumnMappingController myMapper;

    /**
     * Constructs a new column mapping support.
     *
     * @param elementProvider Gets the data elements for layers.
     * @param mappingController Contains the column mapping configurations.
     */
    public ColumnMappingSupport(DataElementLookupUtils elementProvider, ColumnMappingController mappingController)
    {
        myElementProvider = elementProvider;
        myMapper = mappingController;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.merge.algorithm.EnvSupport#columnMatch(io.opensphere.merge.algorithm.Col,
     *      io.opensphere.merge.algorithm.Col)
     */
    @Override
    public String columnMatch(Col column1, Col column2)
    {
        // fail fast in the comparison. Columns cannot match, by definition, if
        // they're in the same layer.
        if (StringUtils.equals(column1.owner.getTypeKey(), column2.owner.getTypeKey()))
        {
            return null;
        }

        if (StringUtils.equals(column1.name, column2.name))
        {
            return column1.name;
        }

        String mappedColumn1 = myMapper.getDefinedColumn(column1.owner.getTypeKey(), column1.name);
        String mappedColumn2 = myMapper.getDefinedColumn(column2.owner.getTypeKey(), column2.name);

        if (mappedColumn1 != null && mappedColumn1.equals(mappedColumn2))
        {
            return mappedColumn1;
        }

        // if nothing has matched yet, check the special keys:
        return specialKeyMatch(column1, column2);
    }

    /**
     * Test the {@link SpecialKey} value from each of the supplied {@link Col}
     * instances. If the name of the special key is the same, then treat the
     * columns as the same. The value returned from this method reflects the
     * "merged" column name, or null if no match is found.
     *
     * @param column1 the first column to compare.
     * @param column2 the second column to compare.
     * @return the name of the matched column or null if no match is found.
     */
    protected String specialKeyMatch(Col column1, Col column2)
    {
        if (column1.special != null && column2.special != null
                && StringUtils.equals(column1.special.getKeyName(), column2.special.getKeyName()))
        {
            // take the longer name between the two columns as the match name,
            // as it is probably more descriptive:
            return column1.name.length() > column2.name.length() ? column1.name : column2.name;
        }
        // default return:
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.merge.algorithm.EnvSupport#getRecords(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public List<DataElement> getRecords(DataTypeInfo type)
    {
        return myElementProvider.getDataElements(type);
    }
}
