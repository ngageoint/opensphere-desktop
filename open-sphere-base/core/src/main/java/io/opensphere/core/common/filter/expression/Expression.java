package io.opensphere.core.common.filter.expression;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This interface represents an expression. An expression can be as simple as a
 * literal constant, a binary operation or a function.
 */
public interface Expression extends Cloneable
{
    /**
     * Fetches the value from the given DTO.
     *
     * @param dto the DTO from which a value is fetched.
     * @return the <code>Expression</code>-specific value.
     */
    public Object getValueFrom(FilterDTO dto);

    /**
     * Creates a copy of this object.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if this instance cannot be cloned.
     */
    public Expression clone() throws CloneNotSupportedException;
}
