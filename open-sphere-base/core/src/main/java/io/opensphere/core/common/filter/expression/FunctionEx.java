package io.opensphere.core.common.filter.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class is a special type of <code>Expression</code> that creates its
 * result from a custom algorithm.
 */
public class FunctionEx implements Expression
{
    /**
     * The function name.
     */
    private String name;

    /**
     * The function arguments.
     */
    private List<Expression> arguments = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param name the function name.
     * @param arguments the function arguments.
     */
    public FunctionEx(String name, Expression... arguments)
    {
        this.name = name;
        this.arguments.addAll(Arrays.asList(arguments));
    }

    /**
     * Constructor.
     *
     * @param name the function name.
     * @param arguments the function arguments.
     */
    public FunctionEx(String name, List<Expression> arguments)
    {
        this.name = name;
        this.arguments = arguments;
    }

    /**
     * Returns the name.
     *
     * @return the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the arguments.
     *
     * @return the arguments.
     */
    public List<Expression> getArguments()
    {
        return arguments;
    }

    /**
     * Indicates if the full list of DTOs is required by the
     * <code>getValueFrom</code> methods. If <code>true</code>, use the
     * {@link #getValueFrom(FilterDTO...)} method. Otherwise, use
     * {@link #getValueFrom(FilterDTO)}.
     *
     * @return <code>true</code> if this function needs to see all DTOs before
     *         producing a valid answer.
     */
    public boolean isFullListRequired()
    {
        return false;
    }

    /**
     * This method invokes the {@link #getValueFrom(FilterDTO...)} version to
     * handle variable argument lists.
     *
     * @see io.opensphere.core.common.filter.expression.Expression#getValueFrom(FilterDTO)
     */
    @Override
    public Object getValueFrom(FilterDTO dto)
    {
        return getValueFrom(new FilterDTO[] { dto });
    }

    public Object getValueFrom(Expression e, FilterDTO dto)
    {
        return getValueFrom(dto);
    }

    /**
     * Use this method if a <code>FunctionEx</code> requires the full set of
     * DTOs in order to perform its function. For example, functions like min,
     * max and average require all DTOs in order to compute the appropriate
     * value.
     *
     * @param dtos the list of DTOs.
     * @return the computed result.
     */
    public Comparable<? extends Object> getValueFrom(FilterDTO... dtos)
    {
        throw new UnsupportedOperationException("This method has not been implemented");
    }

    public Comparable<? extends Object> getValueFrom(Expression e, FilterDTO... dtos)
    {
        return getValueFrom(dtos);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName() + "(" + StringUtils.join(getArguments(), ", ") + ")";
    }

    /**
     * @see io.opensphere.core.common.filter.expression.Expression#clone()
     */
    @Override
    public FunctionEx clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("The function " + name + " does not support cloning.");
    }
}
