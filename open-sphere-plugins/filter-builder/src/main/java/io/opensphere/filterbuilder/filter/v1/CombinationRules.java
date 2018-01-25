package io.opensphere.filterbuilder.filter.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.datafilter.DataFilterOperators.Logical;

/**
 * A collection of combination rules.
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.NONE)
public class CombinationRules
{
    /** The list of {@link CombinationRule}s. */
    @XmlElement(name = "combinationRule")
    private final List<CombinationRule> myCombinationRules;

    /**
     * Instantiates a new CombinationRule list.
     */
    public CombinationRules()
    {
        myCombinationRules = new ArrayList<>();
    }

    /**
     * Adds a combination rule.
     *
     * @param rule the rule
     */
    public void add(CombinationRule rule)
    {
        myCombinationRules.add(rule);
    }

    /**
     * Gets a combination rule.
     *
     * @param typeKey the data type key
     * @return the operator
     */
    public Logical get(String typeKey)
    {
        Logical operator = Logical.AND;
        CombinationRule rule = getRule(typeKey);
        if (rule != null)
        {
            operator = rule.getOperator();
        }
        return operator;
    }

    /**
     * Gets all the rules.
     *
     * @return the rules
     */
    public List<CombinationRule> getRules()
    {
        return myCombinationRules;
    }

    /**
     * Gets the rule for the given type key.
     *
     * @param typeKey the data type key
     * @return the matching rule
     */
    private CombinationRule getRule(String typeKey)
    {
        CombinationRule matchingRule = null;
        for (CombinationRule rule : myCombinationRules)
        {
            if (rule.getTypeKey() != null && rule.getTypeKey().equals(typeKey))
            {
                matchingRule = rule;
                break;
            }
        }
        return matchingRule;
    }
}
