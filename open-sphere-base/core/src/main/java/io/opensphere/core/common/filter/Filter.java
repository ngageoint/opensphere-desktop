package io.opensphere.core.common.filter;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.filter.expression.Expression;
import io.opensphere.core.common.filter.expression.LiteralEx;
import io.opensphere.core.common.filter.expression.PropertyNameEx;
import io.opensphere.core.common.filter.operator.BinaryComparisonOp;
import io.opensphere.core.common.filter.operator.LogicalOp;
import io.opensphere.core.common.filter.operator.Operator;
import io.opensphere.core.common.filter.operator.PropertyIsBetweenOp;

public class Filter
{
    private static final Log LOGGER = LogFactory.getLog(Filter.class);

    private Operator operator;

    /**
     * Sets the operator.
     *
     * @param expression the operator.
     */
    public void setOperator(Operator operator)
    {
        this.operator = operator;
    }

    /**
     * Returns the operator.
     *
     * @return the operator.
     */
    public Operator getOperator()
    {
        return operator;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Filter [operator=");
        builder.append(operator);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Apply the given type map to this filter. This traverse through the
     * operators of this filter and attempt to make sure the literal values are
     * the correct type as given by the typesToApply map
     *
     * @param typesToApply Key: String name of column/value Value: Desired Java
     *            class of the literal
     */
    public void applyTypesToOperator(Map<String, Class<?>> typesToApply)
    {
        if (typesToApply == null)
        {
            return;
        }
        Map<String, Class<?>> upperCaseMap = new HashMap<String, Class<?>>();
        // Make sure all the keys are upper case
        for (Entry<String, Class<?>> entry : typesToApply.entrySet())
        {
            upperCaseMap.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        applyTypesToOperator(getOperator(), upperCaseMap);
    }

    private void applyTypesToOperator(Operator operator, Map<String, Class<?>> typesToApply)
    {
        if (operator instanceof LogicalOp)
        {
            for (Operator op : ((LogicalOp)operator).getOperators())
            {
                applyTypesToOperator(op, typesToApply);
            }
        }
        else if (operator instanceof PropertyIsBetweenOp)
        {
            PropertyIsBetweenOp op = ((PropertyIsBetweenOp)operator);
            // TODO: Make this a bit more robust for other types
            if (op.getExpression() instanceof PropertyNameEx)
            {
                PropertyNameEx nameEx = (PropertyNameEx)op.getExpression();
                applyTypesToExpression(op.getLowerBound(), typesToApply.get(nameEx.getName().toUpperCase()));
                applyTypesToExpression(op.getUpperBound(), typesToApply.get(nameEx.getName().toUpperCase()));
            }
        }
        else if (operator instanceof BinaryComparisonOp)
        {
            BinaryComparisonOp op = ((BinaryComparisonOp)operator);
            // TODO: Make this a bit more robust for other types
            if (op.getLeftExpression() instanceof PropertyNameEx)
            {
                PropertyNameEx nameEx = (PropertyNameEx)op.getLeftExpression();
                applyTypesToExpression(op.getRightExpression(), typesToApply.get(nameEx.getName().toUpperCase()));
            }
        }

    }

    private void applyTypesToExpression(Expression expression, Class<?> clazz)
    {
        if (expression instanceof LiteralEx && clazz != null)
        {
            LiteralEx literalEx = (LiteralEx)expression;

            if (literalEx.getClass().isAssignableFrom(clazz))
            {
                // Already the correct type
                return;
            }
            else if (Number.class.isAssignableFrom(clazz))
            {
                assignLiteralValue(clazz, literalEx);
            }
            // TODO: Do other types of checking that might be useful
            // TODO: Do some date checking of common format types
        }

    }

    @SuppressWarnings("unchecked")
    private boolean assignLiteralValue(Class<?> targetClass, LiteralEx literalEx)
    {
        // See if there are any constructors that take
        // the type of literalEx's value
        Constructor<?> constructor = null;
        try
        {
            constructor = targetClass.getConstructor(literalEx.getValue().getClass());
        }
        catch (Exception e1)
        {
            // Just return. Nothing we can do
            LOGGER.warn(
                    "Could not assign value of :" + literalEx.getValue() + " to class type of :" + targetClass.getSimpleName());
            return false;
        }

        // Try and construct now
        try
        {
            literalEx.setValue(constructor.newInstance(literalEx.getValue()));
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Could not construct an instance of " + targetClass.getSimpleName() + " from " + "literal value of :"
                    + literalEx.getValue());
        }
        return false;
    }

}
