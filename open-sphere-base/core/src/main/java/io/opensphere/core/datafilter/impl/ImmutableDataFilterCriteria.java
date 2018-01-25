package io.opensphere.core.datafilter.impl;

import java.util.Objects;
import java.util.function.Function;

import io.opensphere.core.datafilter.DataFilterCriteria;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * The Class ImmutableDataFilterCriteria.
 *
 * Wraps a {@link DataFilterCriteria} and creates a deep immutable copy of the
 * original.
 */
public class ImmutableDataFilterCriteria implements DataFilterCriteria
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The Comparison operator. */
    private final Conditional myComparisonOperator;

    /** The Field. */
    private final String myField;

    /** The SQL like string. */
    private final String mySQLLikeString;

    /** The Value. */
    private final String myValue;

    /**
     * Instantiates a new immutable data filter criteria.
     *
     * @param dfc the dfc
     */
    public ImmutableDataFilterCriteria(DataFilterCriteria dfc)
    {
        Utilities.checkNull(dfc, "dfc");
        mySQLLikeString = dfc.getSqlLikeString();
        myField = dfc.getField();
        myComparisonOperator = dfc.getComparisonOperator();
        myValue = dfc.getValue();
    }

    /**
     * Instantiates a new immutable data filter criteria.
     *
     * @param field the field
     * @param value the value
     * @param conditional the conditional
     * @param sqlLikeString the sql like string
     */
    public ImmutableDataFilterCriteria(String field, String value, Conditional conditional, String sqlLikeString)
    {
        mySQLLikeString = sqlLikeString;
        myField = field;
        myComparisonOperator = conditional;
        myValue = value;
    }

    /**
     * Clone me, applying a function to my field name.
     *
     * @param transform The field name transform.
     * @return The new criteria with the transform applied.
     */
    public ImmutableDataFilterCriteria applyFieldNameTransform(Function<String, String> transform)
    {
        return new ImmutableDataFilterCriteria(transform.apply(myField), myValue, myComparisonOperator, mySQLLikeString);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ImmutableDataFilterCriteria other = (ImmutableDataFilterCriteria)obj;
        // @formatter:off
        return Objects.equals(myComparisonOperator, other.myComparisonOperator)
                && Objects.equals(myField, other.myField)
                && Objects.equals(mySQLLikeString, other.mySQLLikeString)
                && Objects.equals(myValue, other.myValue);
        // @formatter:on
    }

    @Override
    public Conditional getComparisonOperator()
    {
        return myComparisonOperator;
    }

    @Override
    public String getField()
    {
        return myField;
    }

    @Override
    public String getSqlLikeString()
    {
        return mySQLLikeString;
    }

    @Override
    public String getValue()
    {
        return myValue;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myComparisonOperator);
        result = prime * result + HashCodeHelper.getHashCode(myField);
        result = prime * result + HashCodeHelper.getHashCode(mySQLLikeString);
        result = prime * result + HashCodeHelper.getHashCode(myValue);
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(this.getClass().getSimpleName());
        sb.append("\n  Field   : ").append(myField);
        sb.append("\n  Value   : ").append(myValue);
        sb.append("\n  Operator: ").append(myComparisonOperator == null ? "NULL" : myComparisonOperator.toString());
        sb.append("\n  SQLStr  : ").append(mySQLLikeString).append('\n');
        return sb.toString();
    }
}
