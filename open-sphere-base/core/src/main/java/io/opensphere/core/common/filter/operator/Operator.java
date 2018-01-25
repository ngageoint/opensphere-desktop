package io.opensphere.core.common.filter.operator;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This abstract class represents a basic filter operator.
 */
public abstract class Operator implements Cloneable
{
    /**
     * Evaluates the operator against the given <code>FilterDTO</code>.
     *
     * @param dto
     * @return
     */
    public abstract boolean evaluate(FilterDTO dto);

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public abstract Operator clone() throws CloneNotSupportedException;
}
