package io.opensphere.filterbuilder.impl;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.filterbuilder.filter.v1.CommonFieldGroup;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.mantle.data.element.mdfilter.FilterException;
import io.opensphere.mantle.data.element.mdfilter.FilterToWFS110Converter;

/**
 * Tests converting between Filter Builder and WFS filters.
 */
public class WFS110FilterToDataFilterConverterTest
{
    /**
     * Tests converting and reconverting.
     */
    @Test
    public void testConvert()
    {
        Filter filter = createTestFilter();
        try
        {
            // Convert to WFS format and back
            Filter convertedFilter = new Filter();
            new WFS110FilterToDataFilterConverter().convert(FilterToWFS110Converter.convert(filter), convertedFilter, null, null);

            // Compare original to converted
            Assert.assertEquals(filter.getSqlLikeString(), convertedFilter.getSqlLikeString());
        }
        catch (FilterException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Creates a filter for testing.
     *
     * @return the filter
     */
    private static Filter createTestFilter()
    {
        final String beer = "beer";

        Group mainGroup = new Group(Logical.OR);

        Group andGroup = new Group(Logical.AND);
        andGroup.addFilterCriteria(new Criteria(beer, Conditional.GT, "wheat"));
        andGroup.addFilterCriteria(new Criteria(beer, Conditional.LT, "stout"));
        mainGroup.addFilterGroup(andGroup);

        Group notCriteraGroup = new Group(Logical.NOT);
        notCriteraGroup.addFilterCriteria(new Criteria(beer, Conditional.EQ, "fruity"));
        mainGroup.addFilterGroup(notCriteraGroup);

        Group notGroupGroup = new Group(Logical.NOT);
        Group tmpGroup1 = new Group(Logical.OR);
        tmpGroup1.addFilterCriteria(new Criteria(beer, Conditional.EQ, "tastes-great"));
        tmpGroup1.addFilterCriteria(new Criteria(beer, Conditional.EQ, "less-filling"));
        notGroupGroup.addFilterGroup(tmpGroup1);
        mainGroup.addFilterGroup(notGroupGroup);

        mainGroup.addFilterCriteria(new Criteria(beer, Conditional.LIKE, "heaven"));

        CommonFieldGroup commonFieldGroup = new CommonFieldGroup(Logical.OR, beer);
        commonFieldGroup.addFilterCriteria(new Criteria(beer, Conditional.EQ, "test1"));
        commonFieldGroup.addFilterCriteria(new Criteria(beer, Conditional.EQ, "test2"));
        mainGroup.addCommonFieldFilterGroup(commonFieldGroup);

        return new Filter("Test", null, mainGroup, false);
    }
}
