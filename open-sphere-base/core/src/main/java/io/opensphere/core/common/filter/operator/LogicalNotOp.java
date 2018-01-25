package io.opensphere.core.common.filter.operator;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class represents the logical <code>NOT</code> operator.
 */
public class LogicalNotOp extends UnaryLogicalOp
{
    /**
     * Constructor.
     *
     * @param op the operator to negate.
     */
    public LogicalNotOp(Operator op)
    {
        super(op);
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        return !getOperators().get(0).evaluate(dto);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "!" + getOperators().get(0);
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public LogicalNotOp clone() throws CloneNotSupportedException
    {
        return new LogicalNotOp(getOperators().get(0).clone());
    }
}
