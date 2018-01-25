package io.opensphere.core.common.filter.expression;

import java.util.List;

import io.opensphere.core.common.filter.operator.Operator;

/**
 * This class is a special type {@link FunctionEx} that can be evaluated by the
 * database server and does not have to be further processed.
 */
public class FlatFunctionEx extends FunctionEx
{

    /**
     * Constructor.
     *
     * @param name the function name.
     * @param arguments the function arguments if applicable.
     */
    public FlatFunctionEx(String name, Expression... arguments)
    {
        super(name, arguments);
    }

    /**
     * Will return an {@link String} expression to be evaluated by a
     * FilterProcessor as most other {@link Operator}s are processed.
     *
     * @param parameters The set of parameters used by the processed filter.
     *            Additions to this {@link List} should be complimented with
     *            additions of ? to the returned {@link String}.
     *
     * @param exs The optional {@link LiteralEx} arguments that the
     *            {@link FlatFunctionEx} was provided.
     * @return {@link String} expression to be evaluated by a FilterProcessor
     */
    public String getExpression(List<Object> parameters, LiteralEx... exs)
    {
        throw new UnsupportedOperationException("This method has not been implemented");
    }

}
