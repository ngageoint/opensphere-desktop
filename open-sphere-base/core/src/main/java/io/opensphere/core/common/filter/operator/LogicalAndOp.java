package io.opensphere.core.common.filter.operator;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class represents the logical <code>AND</code> operator.
 */
public class LogicalAndOp extends BinaryLogicalOp
{
    /**
     * Constructor.
     */
    public LogicalAndOp()
    {
        super(BinaryLogicType.AND);
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        boolean result = true;
        for (Operator op : getOperators())
        {
            if (!op.evaluate(dto))
            {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public LogicalAndOp clone() throws CloneNotSupportedException
    {
        LogicalAndOp copy = new LogicalAndOp();
        for (Operator op : getOperators())
        {
            copy.getOperators().add(op.clone());
        }
        return copy;
    }
}
