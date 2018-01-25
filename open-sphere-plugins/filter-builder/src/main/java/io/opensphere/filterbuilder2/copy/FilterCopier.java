package io.opensphere.filterbuilder2.copy;

import org.apache.log4j.Logger;

import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.filterbuilder.filter.v1.CommonFieldGroup;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.filterbuilder.filter.v1.Source;

/**
 * Copies filters from one data type to another.
 */
public final class FilterCopier
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(FilterCopier.class);

    /** The column mapping controller. */
    private final ColumnMappingController myColumnMappingController;

    /**
     * Constructor.
     *
     * @param columnMappingController the column mapping controller
     */
    public FilterCopier(ColumnMappingController columnMappingController)
    {
        myColumnMappingController = columnMappingController;
    }

    /**
     * Copies the filter for use with the given data type and links the copy to the original as a "virtual" copy.
     *
     * @param f the filter to copy from
     * @param typeKey the data type to which the copy applies
     * @return the new virtual copy of the original filter
     */
    public Filter virtualFilter(Filter f, String typeKey)
    {
        Filter vf = copyFilter(f, typeKey);
        vf.setVirtual(true);
        vf.setParent(f);
        return vf;
    }

    /**
     * Copies the filter for use as a prototype. No link to the original is maintained.
     *
     * @param orig the filter to copy from
     * @param typeKey the data type native to the copy
     * @return the new copy of the original filter
     */
    public Filter copyFilter(Filter orig, String typeKey)
    {
        Source s = orig.srcForType(typeKey);

        Group g = orig.getFilterGroup().clone();
        if (!orig.getTypeKey().equals(typeKey))
        {
            updateFields(g, orig.getTypeKey(), typeKey);
        }

        Filter f = new Filter(orig.getName(), s, g, s.isActive());
        f.setFilterDescription(orig.getFilterDescription());
        f.getOtherSources().addAll(orig.getOtherSources());
        return f;
    }

    /**
     * Creates a new copy of the original filter, using the same native type. There is no residual link between the copy and the
     * original.
     *
     * @param orig the filter to copy.
     * @return a deep copy of the supplied filter.
     */
    public Filter copyFilter(Filter orig)
    {
        Source s = orig.getSource();
        Group g = orig.getFilterGroup().clone();
        Filter f = new Filter(orig.getName(), s, g, orig.isActive());
        f.setFilterDescription(orig.getFilterDescription());
        f.getOtherSources().addAll(orig.getOtherSources());
        return f;
    }

    /**
     * Updates the fields of the group to be compatible with the given data type by applying column mappings.
     *
     * @param group the filter group
     * @param sourceTypeKey the data type key to copy from
     * @param targetTypeKey the data type key to copy to
     */
    private void updateFields(Group group, String sourceTypeKey, String targetTypeKey)
    {
        for (Criteria criterion : group.getCriteria())
        {
            String targetColumn = getMappedColumn(criterion.getField(), sourceTypeKey, targetTypeKey);
            if (targetColumn != null)
            {
                criterion.setField(targetColumn);
            }
        }
        for (CommonFieldGroup fieldGroup : group.getCommonFieldGroups())
        {
            String targetColumn = getMappedColumn(fieldGroup.getField(), sourceTypeKey, targetTypeKey);
            if (targetColumn != null)
            {
                fieldGroup.setField(targetColumn);
            }
        }

        for (Group childGroup : group.getStdGroups())
        {
            updateFields(childGroup, sourceTypeKey, targetTypeKey);
        }
    }

    /**
     * Gets the mapped column for the given source column and data type.
     *
     * @param sourceColumn the source column
     * @param sourceTypeKey the data type key to copy from
     * @param targetTypeKey the data type key to copy to
     * @return the mapped column
     */
    private String getMappedColumn(String sourceColumn, String sourceTypeKey, String targetTypeKey)
    {
        String targetColumn = myColumnMappingController.getMappedColumn(sourceColumn, sourceTypeKey, targetTypeKey);
        if (targetColumn == null)
        {
            LOGGER.error("Unable to map " + sourceColumn + " from " + sourceTypeKey + " to " + targetTypeKey);
        }
        return targetColumn;
    }
}
