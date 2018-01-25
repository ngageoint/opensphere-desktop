package io.opensphere.mantle.data.geom.style.impl.ui;

import java.util.Arrays;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;

/**
 * The Class VisibilityConstraint.
 */
public class ParameterVisibilityConstraint implements VisibilityConstraint
{
    /** The Multi parameter operator. */
    private MultiParameterOperator myMultiParameterOperator = MultiParameterOperator.OR;

    /** The Parameter key. */
    private final String myParameterKey;

    /** The Parameter value. */
    private final Set<Object> myParameterValues;

    /** The Visible on value match. */
    private final boolean myVisibleOnValueMatch;

    /**
     * Instantiates a new visibility constraint.
     *
     * @param parameterKey the parameter key
     * @param visibleOnMatch the visible on match
     * @param opr the Multi-parameter operator (AND or OR)
     * @param paramValue the list of paramter values
     */
    public ParameterVisibilityConstraint(String parameterKey, boolean visibleOnMatch, MultiParameterOperator opr,
            Object... paramValue)
    {
        myParameterKey = parameterKey;
        myMultiParameterOperator = opr;
        myParameterValues = New.set();
        myParameterValues.addAll(Arrays.asList(paramValue));
        myVisibleOnValueMatch = visibleOnMatch;
    }

    /**
     * Instantiates a new visibility constraint.
     *
     * @param parameterKey the parameter key
     * @param paramValue the param value
     * @param visibleOnMatch the visible on match
     */
    public ParameterVisibilityConstraint(String parameterKey, boolean visibleOnMatch, Object paramValue)
    {
        myParameterKey = parameterKey;
        myParameterValues = New.set();
        myParameterValues.add(paramValue);
        myVisibleOnValueMatch = visibleOnMatch;
    }

    /**
     * Checks if is visible.
     *
     * @param styleToEvaluate the style to evaluate
     * @return true, if is visible
     */
    @Override
    public boolean isVisible(VisualizationStyle styleToEvaluate)
    {
        boolean paramMatch = false;

        VisualizationStyleParameter param = styleToEvaluate.getStyleParameter(myParameterKey);
        if (param != null)
        {
            boolean allMatch = true;
            boolean anyMatch = false;
            for (Object val : myParameterValues)
            {
                if (EqualsHelper.equals(val, param.getValue()))
                {
                    anyMatch = true;
                }
                else
                {
                    allMatch = false;
                }
            }
            switch (myMultiParameterOperator)
            {
                case AND:
                    paramMatch = allMatch;
                    break;
                case OR:
                    paramMatch = anyMatch;
                    break;
                default:
                    break;
            }
        }

        return myVisibleOnValueMatch ? paramMatch : !paramMatch;
    }

    /**
     * The Enum MultiParameterOperator.
     */
    public enum MultiParameterOperator
    {
        /** The AND. */
        AND,

        /** The OR. */
        OR
    }
}
