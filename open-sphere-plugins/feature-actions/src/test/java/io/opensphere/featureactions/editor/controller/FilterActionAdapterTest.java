package io.opensphere.featureactions.editor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;

/**
 * Unit test for {@link FilterActionAdapter}.
 */
public class FilterActionAdapterTest
{
    /**
     * The test column.
     */
    private static final String ourColumn = "USER_NAME";

    /**
     * Tests closing the adapter.
     */
    @Test
    public void testClose()
    {
        Filter filter = new Filter();
        Group group = new Group();
        group.getCriteria().add(new Criteria(ourColumn, Conditional.LIKE, "B*"));
        filter.setFilterGroup(group);

        FeatureAction action = new FeatureAction();
        action.setFilter(filter);
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        FilterActionAdapter adapter = new FilterActionAdapter(simpleAction);

        assertEquals(ourColumn, simpleAction.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, simpleAction.getOption().get());
        assertEquals("B*", simpleAction.getValue().get());

        adapter.close();

        group = new Group();
        group.getCriteria().add(new Criteria(ourColumn, Conditional.GTE, "B"));
        group.getCriteria().add(new Criteria(ourColumn, Conditional.LT, "D"));

        filter.setFilterGroup(group);
        action.setFilter(filter);

        assertEquals(ourColumn, simpleAction.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, simpleAction.getOption().get());
        assertEquals("B*", simpleAction.getValue().get());

        simpleAction.getColumn().set("other");
        simpleAction.getOption().set(CriteriaOptions.RANGE);
        simpleAction.getValue().set("A");

        assertEquals(group, action.getFilter().getFilterGroup());
    }

    /**
     * Tests deleting criteria from the filter editor.
     */
    @Test
    public void testDeleteCriteria()
    {
        Filter filter = new Filter();
        Group group = new Group();
        group.getCriteria().add(new Criteria(ourColumn, Conditional.GTE, "B"));
        group.getCriteria().add(new Criteria(ourColumn, Conditional.LT, "D"));
        filter.setFilterGroup(group);

        FeatureAction action = new FeatureAction();
        action.setFilter(filter);
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        FilterActionAdapter adapter = new FilterActionAdapter(simpleAction);

        assertEquals(ourColumn, simpleAction.getColumn().get());
        assertEquals(CriteriaOptions.RANGE, simpleAction.getOption().get());
        assertEquals("B", simpleAction.getMinimumValue().get());
        assertEquals("D", simpleAction.getMaximumValue().get());

        filter.getFilterGroup().getCriteria().remove(1);
        action.setFilter(null);
        action.setFilter(filter);

        adapter.close();
    }

    /**
     * Tests editing the filter from the filter editor.
     */
    @Test
    public void testEditFilter()
    {
        Filter filter = new Filter();
        Group group = new Group();
        group.getCriteria().add(new Criteria(ourColumn, Conditional.LIKE, "B*"));
        filter.setFilterGroup(group);

        FeatureAction action = new FeatureAction();
        action.setFilter(filter);
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        FilterActionAdapter adapter = new FilterActionAdapter(simpleAction);

        assertEquals(ourColumn, simpleAction.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, simpleAction.getOption().get());
        assertEquals("B*", simpleAction.getValue().get());

        group = new Group();
        group.getCriteria().add(new Criteria(ourColumn, Conditional.GTE, "B"));
        group.getCriteria().add(new Criteria(ourColumn, Conditional.LT, "D"));

        filter.setFilterGroup(group);
        action.setFilter(null);
        action.setFilter(filter);

        assertEquals(ourColumn, simpleAction.getColumn().get());
        assertEquals(CriteriaOptions.RANGE, simpleAction.getOption().get());
        assertEquals("B", simpleAction.getMinimumValue().get());
        assertEquals("D", simpleAction.getMaximumValue().get());

        adapter.close();
    }

    /**
     * Tests editing the filter from the simple editor.
     */
    @Test
    public void testEditSimple()
    {
        Filter filter = new Filter();
        Group group = new Group();
        group.getCriteria().add(new Criteria(ourColumn, Conditional.LIKE, "B*"));
        filter.setFilterGroup(group);

        FeatureAction action = new FeatureAction();
        action.setFilter(filter);
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        FilterActionAdapter adapter = new FilterActionAdapter(simpleAction);

        assertEquals(ourColumn, simpleAction.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, simpleAction.getOption().get());
        assertEquals("B*", simpleAction.getValue().get());

        simpleAction.getOption().set(CriteriaOptions.RANGE);
        simpleAction.getMinimumValue().set("A");
        simpleAction.getMaximumValue().set("M");

        List<Criteria> criterias = filter.getFilterGroup().getCriteria();
        assertEquals(2, criterias.size());

        assertEquals(ourColumn, criterias.get(0).getField());
        assertEquals(Conditional.GTE, criterias.get(0).getComparisonOperator());
        assertEquals("A", criterias.get(0).getValue());

        assertEquals(ourColumn, criterias.get(1).getField());
        assertEquals(Conditional.LT, criterias.get(1).getComparisonOperator());
        assertEquals("M", criterias.get(1).getValue());

        adapter.close();
    }

    /**
     * Tests a new filter created within the filter editor.
     */
    @Test
    public void testNewFilter()
    {
        Filter filter = new Filter();
        Group group = new Group();
        group.getCriteria().add(new Criteria(ourColumn, Conditional.LIKE, "B*"));
        filter.setFilterGroup(group);

        FeatureAction action = new FeatureAction();

        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        FilterActionAdapter adapter = new FilterActionAdapter(simpleAction);

        assertNull(simpleAction.getColumn().get());
        assertNull(simpleAction.getMinimumValue().get());
        assertNull(simpleAction.getMaximumValue().get());
        assertNull(simpleAction.getValue().get());

        action.setFilter(filter);

        assertEquals(ourColumn, simpleAction.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, simpleAction.getOption().get());
        assertEquals("B*", simpleAction.getValue().get());

        adapter.close();
    }

    /**
     * Tests a new filter created via the simple editor.
     */
    @Test
    public void testNewSimple()
    {
        FeatureAction action = new FeatureAction();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        FilterActionAdapter adapter = new FilterActionAdapter(simpleAction);

        assertNull(simpleAction.getColumn().get());
        assertNull(simpleAction.getMinimumValue().get());
        assertNull(simpleAction.getMaximumValue().get());
        assertNull(simpleAction.getValue().get());

        simpleAction.getColumn().set(ourColumn);
        simpleAction.getOption().set(CriteriaOptions.RANGE);
        simpleAction.getMinimumValue().set("A");
        simpleAction.getMaximumValue().set("M");

        List<Criteria> criterias = action.getFilter().getFilterGroup().getCriteria();
        assertEquals(2, criterias.size());

        assertEquals(ourColumn, criterias.get(0).getField());
        assertEquals(Conditional.GTE, criterias.get(0).getComparisonOperator());
        assertEquals("A", criterias.get(0).getValue());

        assertEquals(ourColumn, criterias.get(1).getField());
        assertEquals(Conditional.LT, criterias.get(1).getComparisonOperator());
        assertEquals("M", criterias.get(1).getValue());

        adapter.close();
    }
}
