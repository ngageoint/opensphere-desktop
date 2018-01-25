package io.opensphere.filterbuilder2.copy;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.columns.ColumnMappingControllerImpl;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.filterbuilder.filter.v1.Source;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/** Tests {@link FilterCopier}. */
public class FilterCopierTest
{
    /** Tests {@link FilterCopier#copyFilter(Filter, String)}. */
    @Test
    public void testCopyFilter()
    {
        ColumnMappingControllerImpl columnMappingController = new ColumnMappingControllerImpl(null);
        columnMappingController.initialize();

        FilterCopier copier = new FilterCopier(columnMappingController);

        Filter sourceFilter = newFilter();
        sourceFilter.setFilterDescription("filter1");
        DataTypeInfo targetDataType = new DefaultDataTypeInfo(null, null, "key2", "name2", "display2", true);

        sourceFilter.getOtherSources().add(Source.fromDataType(targetDataType));

        // Test copy with no mappings

        Filter copiedFilter = copier.copyFilter(sourceFilter, targetDataType.getTypeKey());

        Assert.assertEquals("filter1", copiedFilter.getFilterDescription());
        Criteria criteria0 = copiedFilter.getFilterGroup().getCriteria().get(0);
        Assert.assertEquals("field1", criteria0.getField());
        Assert.assertEquals(Conditional.EQ, criteria0.getComparisonOperator());
        Assert.assertEquals("123", criteria0.getValue());
        Criteria criteriaInner0 = copiedFilter.getFilterGroup().getStdGroups().get(0).getCriteria().get(0);
        Assert.assertEquals("field1", criteriaInner0.getField());
        Assert.assertEquals(Conditional.EQ, criteriaInner0.getComparisonOperator());
        Assert.assertEquals("124", criteriaInner0.getValue());

        // Test copy with mappings

        columnMappingController.addMapping("blah", "key1", "field1", false);
        columnMappingController.addMapping("blah", "key2", "field2", false);

        copiedFilter = copier.copyFilter(sourceFilter, targetDataType.getTypeKey());

        criteria0 = copiedFilter.getFilterGroup().getCriteria().get(0);
        Assert.assertEquals("field2", criteria0.getField());
        Assert.assertEquals(Conditional.EQ, criteria0.getComparisonOperator());
        Assert.assertEquals("123", criteria0.getValue());
        criteriaInner0 = copiedFilter.getFilterGroup().getStdGroups().get(0).getCriteria().get(0);
        Assert.assertEquals("field2", criteriaInner0.getField());
        Assert.assertEquals(Conditional.EQ, criteriaInner0.getComparisonOperator());
        Assert.assertEquals("124", criteriaInner0.getValue());
    }

    /**
     * Creates a new filter.
     *
     * @return the filter
     */
    private static Filter newFilter()
    {
        Filter filter = new Filter("filter1", new Source("display1", "name1", "server", "key1"));
        filter.getOtherSources().add(filter.getSource());
        Group group = new Group();
        group.addFilterCriteria(new Criteria("field1", Conditional.EQ, "123"));
        Group group2 = new Group();
        group2.addFilterCriteria(new Criteria("field1", Conditional.EQ, "124"));
        group.addFilterGroup(group2);
        filter.setFilterGroup(group);
        return filter;
    }
}
