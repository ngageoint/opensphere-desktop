package io.opensphere.core.common.filter.operator;

/**
 * This abstract class represents a unary logical operator (e.g. logical
 * <code>NOT</code>).
 */
public abstract class UnaryLogicalOp extends LogicalOp
{
    /**
     * Constructor.
     *
     * @param op the single operator.
     */
    public UnaryLogicalOp(Operator op)
    {
        getOperators().add(op);
        makeFixedSize();
    }
}
