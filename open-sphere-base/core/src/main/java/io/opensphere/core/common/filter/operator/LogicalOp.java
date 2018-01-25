package io.opensphere.core.common.filter.operator;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.common.collection.FixedSizeList;

/**
 * This abstract class represents a logical operator.
 */
public abstract class LogicalOp extends Operator
{
    private List<Operator> operators = new ArrayList<>();

    /**
     * Changes the list of operators to a fixed-size list. Any attempt to add or
     * remove from the list will cause an exception. Operators can be changed in
     * place, however.
     */
    protected void makeFixedSize()
    {
        operators = new FixedSizeList<>(operators);
    }

    public List<Operator> getOperators()
    {
        return operators;
    }
}
