package io.opensphere.core.common.filter.operator;

import io.opensphere.core.common.filter.dto.FilterDTO;
import io.opensphere.core.common.filter.expression.Expression;

/**
 * This class represents a comparison operation where an expression is
 * determined to be within a specific range or not.
 */
public class PropertyIsBetweenOp extends ComparisonOp
{
    /**
     * The expression to be range checked.
     */
    private Expression expression;

    /**
     * The inclusive lower boundary of the range.
     */
    private Expression lowerBound;

    /**
     * The inclusive upper boundary of the range.
     */
    private Expression upperBound;

    /**
     * Constructor.
     *
     * @param expression the expression to be checked.
     * @param lowerBound the inclusive lower boundary of the range.
     * @param upperBound the inclusive upper boundary of the range.
     */
    public PropertyIsBetweenOp(Expression expression, Expression lowerBound, Expression upperBound)
    {
        this.expression = expression;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Returns the expression to be range checked.
     *
     * @return the expression to be range checked.
     */
    public Expression getExpression()
    {
        return expression;
    }

    /**
     * Sets the expression to be range checked.
     *
     * @param expression the expression to be range checked.
     */
    public void setExpression(Expression expression)
    {
        this.expression = expression;
    }

    /**
     * Returns the inclusive lower boundary of the range.
     *
     * @return the inclusive lower boundary of the range.
     */
    public Expression getLowerBound()
    {
        return lowerBound;
    }

    /**
     * Sets the inclusive lower boundary of the range.
     *
     * @param lowerBound the inclusive lower boundary of the range.
     */
    public void setLowerBound(Expression lowerBound)
    {
        this.lowerBound = lowerBound;
    }

    /**
     * Returns the inclusive upper boundary of the range.
     *
     * @return the inclusive upper boundary of the range.
     */
    public Expression getUpperBound()
    {
        return upperBound;
    }

    /**
     * Sets the inclusive upper boundary of the range.
     *
     * @param upperBound the inclusive upper boundary of the range.
     */
    public void setUpperBound(Expression upperBound)
    {
        this.upperBound = upperBound;
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        Comparable value = (Comparable)getExpression().getValueFrom(dto);
        Comparable lower = (Comparable)getLowerBound().getValueFrom(dto);
        Comparable upper = (Comparable)getUpperBound().getValueFrom(dto);

        // If either of these are false then return false, usually
        // this means the filter contains a field that is not found
        // in the backing dto
        if (value == null || lower == null || upper == null)
        {
            return false;
        }
        return value.compareTo(lower) >= 0 && value.compareTo(upper) <= 0;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getLowerBound() + " <= " + getExpression() + " <= " + getUpperBound();
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public PropertyIsBetweenOp clone() throws CloneNotSupportedException
    {
        return new PropertyIsBetweenOp(expression.clone(), lowerBound.clone(), upperBound.clone());
    }
}
