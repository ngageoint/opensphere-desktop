package io.opensphere.featureactions.editor.controller;

import java.util.List;

import javafx.beans.value.ChangeListener;

import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterOperators;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;

/**
 * Adapts the {@link SimpleFeatureAction} filter to and from an actual
 * {@link Filter}. Keeps the two models in sync.
 */
public class FilterActionAdapter
{
    /** Listens for when the filter is changed and updates the model. */
    private final ChangeListener<Filter> myFilterListener;

    /**
     * Listens for the selected {@link CriteriaOptions} changes, and applies the
     * changes to an actual filter.
     */
    private final ChangeListener<CriteriaOptions> myOptionListener = (o, v0, v1) -> ignoreInternalEvents(() -> toFilter());

    /** The action to adapt. */
    private final SimpleFeatureAction mySimpleAction;

    /**
     * Just a generic string listener to update simple filter to an actual
     * filter.
     */
    private final ChangeListener<String> myStringListener = (o, v0, v1) -> ignoreInternalEvents(() -> toFilter());

    /** Gives indication that the model changes were caused by this class. */
    private boolean myWasMe;

    /**
     * Constructs a new adapter.
     *
     * @param simpleAction The action to adapt.
     */
    public FilterActionAdapter(SimpleFeatureAction simpleAction)
    {
        mySimpleAction = simpleAction;
        myFilterListener = (o, v0, v1) -> ignoreInternalEvents(() -> filterToModel(mySimpleAction));
        ignoreInternalEvents(() -> filterToModel(mySimpleAction));
        mySimpleAction.getColumn().addListener(myStringListener);
        mySimpleAction.getValue().addListener(myStringListener);
        mySimpleAction.getMinimumValue().addListener(myStringListener);
        mySimpleAction.getMaximumValue().addListener(myStringListener);
        mySimpleAction.getOption().addListener(myOptionListener);
        mySimpleAction.getFeatureAction().filterProperty().addListener(myFilterListener);
    }

    /** Stops listening to changes. */
    public void close()
    {
        mySimpleAction.getColumn().removeListener(myStringListener);
        mySimpleAction.getValue().removeListener(myStringListener);
        mySimpleAction.getMinimumValue().removeListener(myStringListener);
        mySimpleAction.getMaximumValue().removeListener(myStringListener);
        mySimpleAction.getOption().removeListener(myOptionListener);
        mySimpleAction.getFeatureAction().filterProperty().removeListener(myFilterListener);
    }

    /**
     * Determine whether the filter can be edited using the simple in-line GUI.
     *
     * @param model the model
     * @return true if and only if the filter is simple
     */
    public static boolean isSimpleModel(SimpleFeatureAction model)
    {
        Filter filter = model.getFeatureAction().getFilter();
        if (filter == null)
        {
            return true;
        }
        Group group = filter.getFilterGroup();
        if (group == null)
        {
            return true;
        }
        // main operator must be "and"
        if (group.getLogicOperator() != DataFilterOperators.Logical.AND)
        {
            return false;
        }
        // there must be no subgroups
        List<DataFilterGroup> subgroups = group.getGroups();
        if (subgroups != null && !subgroups.isEmpty())
        {
            return false;
        }
        List<Criteria> critList = group.getCriteria();
        if (critList == null || critList.isEmpty())
        {
            return true;
        }

        if (critList.size() == 1)
        {
            Conditional op = critList.get(0).getComparisonOperator();
            return op == Conditional.CONTAINS || op == Conditional.EQ || op == Conditional.LIKE;
        }
        else if (critList.size() == 2)
        {
            Criteria min = null;
            Criteria max = null;
            for (Criteria c : critList)
            {
                Conditional op = c.getComparisonOperator();
                if (op == Conditional.LT || op == Conditional.LTE)
                {
                    max = c;
                }
                else if (op == Conditional.GT || op == Conditional.GTE)
                {
                    min = c;
                }
            }
            // max and min must both be present and both apply to the same field
            return min != null && max != null && min.getField().equals(max.getField());
        }
        // too many criteria => not simple
        return false;
    }

    /**
     * Takes the actual filter values and applies them to the model.
     *
     * @param model the model to be updated
     */
    public static void filterToModel(SimpleFeatureAction model)
    {
        Filter filter = model.getFeatureAction().getFilter();
        if (filter == null)
        {
            return;
        }
        Group group = filter.getFilterGroup();
        if (group == null)
        {
            return;
        }
        List<Criteria> critList = group.getCriteria();
        if (critList == null)
        {
            return;
        }

        if (critList.size() == 1)
        {
            Criteria criteria = critList.get(0);
            Conditional conditional = criteria.getComparisonOperator();
            String field = criteria.getField();
            String value = criteria.getValue();
            boolean goodToSetSimple = false;
            if (conditional == Conditional.CONTAINS)
            {
                StringBuilder builder = new StringBuilder();
                builder.append('*');
                builder.append(value);
                builder.append('*');
                value = builder.toString();
                goodToSetSimple = true;
            }
            else if (conditional == Conditional.EQ || conditional == Conditional.LIKE)
            {
                goodToSetSimple = true;
            }

            if (goodToSetSimple)
            {
                model.getColumn().set(field);
                model.getValue().set(value);
                model.getOption().set(CriteriaOptions.VALUE);
            }
        }
        else if (critList.size() == 2)
        {
            Criteria minCriteria = null;
            Criteria maxCriteria = null;

            for (Criteria c : critList)
            {
                Conditional op = c.getComparisonOperator();
                if (op == Conditional.LT || op == Conditional.LTE)
                {
                    maxCriteria = c;
                }
                else if (op == Conditional.GT || op == Conditional.GTE)
                {
                    minCriteria = c;
                }
            }

            if (minCriteria != null && maxCriteria != null
                    && minCriteria.getField().equals(maxCriteria.getField()))
            {
                model.getColumn().set(minCriteria.getField());
                model.getMinimumValue().set(minCriteria.getValue());
                model.getMaximumValue().set(maxCriteria.getValue());
                model.getOption().set(CriteriaOptions.RANGE);
            }
        }
    }

    /** Copies the simple filter values into the actual filter. */
    private void toFilter()
    {
        Filter filter = mySimpleAction.getFeatureAction().getFilter();
        if (filter == null)
        {
            filter = new Filter();
            mySimpleAction.getFeatureAction().setFilter(filter);
        }

        Group group = new Group();

        if (mySimpleAction.getOption().get() == CriteriaOptions.VALUE)
        {
            Criteria criteria = new Criteria(mySimpleAction.getColumn().get(), Conditional.LIKE,
                    mySimpleAction.getValue().get());
            group.getCriteria().add(criteria);
        }
        else
        {
            Criteria criteria = new Criteria(mySimpleAction.getColumn().get(), Conditional.GTE,
                    mySimpleAction.getMinimumValue().get());
            group.getCriteria().add(criteria);

            criteria = new Criteria(mySimpleAction.getColumn().get(), Conditional.LT,
                    mySimpleAction.getMaximumValue().get());
            group.getCriteria().add(criteria);
        }

        filter.setFilterGroup(group);
    }

    /**
     * Take the specified action as long as the "was me" flag is false (i.e.,
     * the event was not internally generated).  It is used to prevent adverse
     * event feedback loops.
     *
     * @param r the action to be performed
     */
    private void ignoreInternalEvents(Runnable r)
    {
        if (myWasMe)
        {
            return;
        }
        myWasMe = true;
        try
        {
            r.run();
        }
        finally
        {
            myWasMe = false;
        }
    }
}
