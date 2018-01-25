package io.opensphere.mantle.data.element.mdfilter.impl;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.datafilter.DataFilterCriteria;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class DataFilterGroupEvaluator.
 */
public class DataFilterGroupEvaluator
{
    /** The Criteria evaluators. */
    private List<DataFilterCriteriaEvaluator> myCriteriaEvaluators;

    /** The Group evaluators. */
    private List<DataFilterGroupEvaluator> myGroupEvaluators;

    /** The Name. */
    private final String myName;

    /** The Operator. */
    private final Logical myOperator;

    /**
     * Instantiates a new data filter group evaluator.
     *
     * @param group the group
     * @param dynEnumReg the dynamic enumeration registry
     */
    public DataFilterGroupEvaluator(DataFilterGroup group, DynamicEnumerationRegistry dynEnumReg)
    {
        Utilities.checkNull(group, "group");
        myName = group.getName();
        myOperator = group.getLogicOperator();
        if (group.getCriteria() != null && !group.getCriteria().isEmpty())
        {
            myCriteriaEvaluators = New.list(group.getCriteria().size());
            for (DataFilterCriteria criteria : group.getCriteria())
            {
                myCriteriaEvaluators.add(new DataFilterCriteriaEvaluator(criteria, dynEnumReg));
            }
        }
        else
        {
            myCriteriaEvaluators = Collections.<DataFilterCriteriaEvaluator>emptyList();
        }

        if (group.getGroups() != null && !group.getGroups().isEmpty())
        {
            myGroupEvaluators = New.list(group.getGroups().size());
            for (DataFilterGroup g : group.getGroups())
            {
                myGroupEvaluators.add(new DataFilterGroupEvaluator(g, dynEnumReg));
            }
        }
        else
        {
            myGroupEvaluators = Collections.<DataFilterGroupEvaluator>emptyList();
        }
    }

    /**
     * Accepts the provider.
     *
     * @param element the data element
     * @return true, if successful
     */
    public boolean accepts(DataElement element)
    {
        boolean accepts = false;

        if (myOperator == Logical.AND)
        {
            accepts = acceptsANDOperator(element);
        }
        else if (myOperator == Logical.OR)
        {
            accepts = acceptsOROperator(element);
        }
        else
        {
            accepts = acceptsNOTOperator(element);
        }
        return accepts;
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
     * Accepts and operator.
     *
     * @param element the data element
     * @return true, if successful
     */
    private boolean acceptsANDOperator(DataElement element)
    {
        boolean allCriteriaAccept = true;
        for (DataFilterCriteriaEvaluator ev : myCriteriaEvaluators)
        {
            if (!ev.accepts(element))
            {
                allCriteriaAccept = false;
                break;
            }
        }
        if (allCriteriaAccept)
        {
            for (DataFilterGroupEvaluator ev : myGroupEvaluators)
            {
                if (!ev.accepts(element))
                {
                    allCriteriaAccept = false;
                    break;
                }
            }
        }
        return allCriteriaAccept;
    }

    /**
     * Accepts not operator.
     *
     * @param element the data element
     * @return true, if successful
     */
    private boolean acceptsNOTOperator(DataElement element)
    {
        boolean accepts = false;
        if (myOperator == Logical.NOT)
        {
            if (!myCriteriaEvaluators.isEmpty())
            {
                accepts = myCriteriaEvaluators.get(0).accepts(element);
            }
            else if (!myGroupEvaluators.isEmpty())
            {
                accepts = myGroupEvaluators.get(0).accepts(element);
            }
        }
        return !accepts;
    }

    /**
     * Accepts or operator.
     *
     * @param element the data element
     * @return true, if successful
     */
    private boolean acceptsOROperator(DataElement element)
    {
        boolean oneAccepts = false;
        for (DataFilterCriteriaEvaluator ev : myCriteriaEvaluators)
        {
            if (ev.accepts(element))
            {
                oneAccepts = true;
                break;
            }
        }
        if (!oneAccepts)
        {
            for (DataFilterGroupEvaluator ev : myGroupEvaluators)
            {
                if (ev.accepts(element))
                {
                    oneAccepts = true;
                    break;
                }
            }
        }
        return oneAccepts;
    }
}
