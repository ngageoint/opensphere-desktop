package io.opensphere.core.common.filter.operator;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class is a special type of <code>ComparisonOp</code> that specializes in
 * comparing IDs.
 */
public class IdEqualsOp extends ComparisonOp
{
    /**
     * The identifier value.
     */
    private String value;

    /**
     * Constructor.
     *
     * @param name the identifier name or <code>null</code> if not yet known.
     * @param value the identifier value or <code>null</code> if not yet known
     */
    public IdEqualsOp(String value)
    {
        setValue(value);
    }

    /**
     * Sets the identifier value.
     *
     * @param value the identifier value.
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * Returns the identifierValue.
     *
     * @return the identifierValue or <code>null</code>.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        Object otherValue = dto.getId();
        boolean result = getValue() == null && otherValue == null;

        // If the results are both non-null, compare them.
        if (getValue() != null && otherValue != null)
        {
            result = getValue().compareTo(otherValue.toString()) == 0;
        }
        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ID == " + getValue();
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public IdEqualsOp clone() throws CloneNotSupportedException
    {
        return new IdEqualsOp(value);
    }
}
