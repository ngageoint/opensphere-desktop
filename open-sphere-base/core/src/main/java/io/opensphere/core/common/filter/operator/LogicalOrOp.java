package io.opensphere.core.common.filter.operator;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class represents the logical <code>OR</code> operator.
 */
public class LogicalOrOp extends BinaryLogicalOp
{
    /**
     * Constructor.
     */
    public LogicalOrOp()
    {
        super(BinaryLogicType.OR);
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        boolean result = false;
        for (Operator op : getOperators())
        {
            if (op.evaluate(dto))
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public LogicalOrOp clone() throws CloneNotSupportedException
    {
        LogicalOrOp copy = new LogicalOrOp();
        for (Operator op : getOperators())
        {
            copy.getOperators().add(op.clone());
        }
        return copy;
    }
}
