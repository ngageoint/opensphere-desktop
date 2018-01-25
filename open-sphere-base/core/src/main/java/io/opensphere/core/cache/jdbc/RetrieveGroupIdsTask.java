package io.opensphere.core.cache.jdbc;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator.ConnectionUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.PreparedStatementUser;
import io.opensphere.core.cache.jdbc.type.ValueTranslator;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Database task for querying group ids from the database.
 */
public class RetrieveGroupIdsTask extends DatabaseTask implements ConnectionUser<int[]>
{
    /**
     * The data model category. Any <code>null</code> values are treated as
     * wildcards.
     */
    private final DataModelCategory myCategory;

    /** If not {@code null}, the required criticality of the groups. */
    private final Boolean myCritical;

    /**
     * If not {@code null}, the range that the groups' expiration times must lie
     * within. If {@code null}, the groups' expiration times must be
     * {@code null}.
     */
    private final TimeSpan myExpirationRange;

    /**
     * Interval parameters for the query.
     */
    private final Collection<? extends IntervalPropertyMatcher<?>> myParameters;

    /** The result count. */
    private transient int myResultCount;

    /**
     * Constructor.
     *
     * @param category The data model category. Any <code>null</code> values are
     *            treated as wildcards.
     * @param parameters Optional interval parameters for the query.
     * @param databaseTaskFactory The database task factory.
     * @param expirationRange If not {@code null}, the range that the groups'
     *            expiration times must lie within. If {@code null}, the groups'
     *            expiration times must be {@code null}.
     * @param critical If not {@code null}, the required criticality of the
     *            groups.
     */
    public RetrieveGroupIdsTask(DataModelCategory category, Collection<? extends IntervalPropertyMatcher<?>> parameters,
            DatabaseTaskFactory databaseTaskFactory, TimeSpan expirationRange, Boolean critical)
    {
        super(databaseTaskFactory);

        myCategory = Utilities.checkNull(category, "category");
        myParameters = parameters;
        myExpirationRange = expirationRange;
        myCritical = critical;
    }

    @Override
    public String getTimingMessage()
    {
        return getResultCount() + " group ids retrieved from cache in ";
    }

    @Override
    public int[] run(Connection conn) throws CacheException
    {
        final Collection<? extends IntervalPropertyMatcher<?>> resultFilterParameters = getResultFilterParameters();
        Collection<PropertyDescriptor<?>> filterProperties = getCacheUtilities()
                .extractPropertyDescriptorsFromMatchers(resultFilterParameters);
        final String sql = getSQLGenerator().generateRetrieveGroupValuesSql((int[])null, (JoinTableColumn)null, getCategory(),
                getParameters(), filterProperties, getExpirationRange(), isCritical());
        return new StatementAppropriator(conn).appropriateStatement(new PreparedStatementUser<int[]>()
        {
            @Override
            public int[] run(Connection conn1, PreparedStatement pstmt) throws CacheException
            {
                try
                {
                    prepareGroupQueryStatement(conn1, pstmt, sql);
                    ResultSet rs = getCacheUtilities().executeQuery(pstmt, sql);
                    try
                    {
                        int[] ids = getCacheUtilities().convertResultSetToIntArray(rs, resultFilterParameters, getTypeMapper());
                        setResultCount(ids.length);
                        return ids;
                    }
                    finally
                    {
                        rs.close();
                    }
                }
                catch (SQLException e1)
                {
                    throw new CacheException("Failed to read group ids from cache: " + e1, e1);
                }
                catch (NotSerializableException e)
                {
                    throw new CacheException(e);
                }
            }
        }, sql);
    }

    /**
     * Accessor for the category.
     *
     * @return The category.
     */
    protected DataModelCategory getCategory()
    {
        return myCategory;
    }

    /**
     * If not {@code null}, the range that the groups' expiration times must lie
     * within. If {@code null}, the groups' expiration times must be
     * {@code null}.
     *
     * @return The expiration range.
     */
    protected TimeSpan getExpirationRange()
    {
        return myExpirationRange;
    }

    /**
     * Accessor for the parameters.
     *
     * @return The parameters.
     */
    protected Collection<? extends IntervalPropertyMatcher<?>> getParameters()
    {
        return myParameters;
    }

    /**
     * Accessor for the resultCount.
     *
     * @return The resultCount.
     */
    protected int getResultCount()
    {
        return myResultCount;
    }

    /**
     * Get the parameters that require post-query filtering.
     *
     * @return Any of the input parameters that require post-query filtering.
     */
    protected Collection<? extends IntervalPropertyMatcher<?>> getResultFilterParameters()
    {
        if (getParameters().isEmpty())
        {
            return Collections.emptySet();
        }
        else
        {
            Collection<IntervalPropertyMatcher<?>> result = New.collection();
            for (IntervalPropertyMatcher<?> intervalPropertyMatcher : getParameters())
            {
                if (intervalPropertyMatcher instanceof GeometryMatcher)
                {
                    result.add(intervalPropertyMatcher);
                }
            }
            return result;
        }
    }

    /**
     * If not {@code null}, the required criticality of the groups.
     *
     * @return The critical flag.
     */
    protected Boolean isCritical()
    {
        return myCritical;
    }

    /**
     * Prepare a database statement for a group query.
     *
     * @param conn The database connection.
     * @param pstmt The statement.
     * @param sql The SQL string.
     * @throws CacheException If there's a problem with the SQL.
     * @throws SQLException If an argument cannot be set in the statement.
     * @throws NotSerializableException If the operand of the parameter is not
     *             serializable.
     */
    protected void prepareGroupQueryStatement(Connection conn, PreparedStatement pstmt, String sql)
        throws CacheException, SQLException, NotSerializableException
    {
        int index = setStandardWhereParameters(pstmt, getCategory(), getExpirationRange(), isCritical());

        if (CollectionUtilities.hasContent(getParameters()))
        {
            for (PropertyMatcher<? extends Serializable> param : getParameters())
            {
                index = prepareGroupQueryStatement(pstmt, index, param);
            }
        }
    }

    /**
     * Set a parameter in the prepared statement.
     *
     * @param <T> The type of the property value.
     * @param pstmt The prepared statement.
     * @param startIndex The index of the parameter.
     * @param param The parameter.
     * @return The next parameter index.
     * @throws CacheException If there's a problem with the SQL.
     * @throws SQLException If an argument cannot be set in the statement.
     * @throws NotSerializableException If the operand of the parameter is not
     *             serializable.
     */
    protected <T extends Serializable> int prepareGroupQueryStatement(PreparedStatement pstmt, int startIndex,
            PropertyMatcher<T> param) throws SQLException, CacheException, NotSerializableException
    {
        int index = startIndex;

        // The geometry table does not have a property name.
        if (!(param instanceof GeometryMatcher))
        {
            pstmt.setString(index++, param.getPropertyDescriptor().getPropertyName());
        }

        if (!(param instanceof GeometryMatcher))
        {
            PropertyDescriptor<T> desc = param.getPropertyDescriptor();
            ValueTranslator<? super T> valueTranslator = getTypeMapper().getValueTranslator(desc);

            index = valueTranslator.setValue(pstmt, index, param.getOperand(), false);
        }
        return index;
    }

    /**
     * Mutator for the resultCount.
     *
     * @param resultCount The resultCount to set.
     */
    protected void setResultCount(int resultCount)
    {
        myResultCount = resultCount;
    }

    /**
     * Set the parameters for the standard where conditions.
     *
     * @param pstmt The already-prepared statement.
     * @param category The data model category.
     * @param expirationRange If not {@code null}, the range that the groups'
     *            expiration times must lie within. If {@code null}, the groups'
     *            expiration times must be {@code null}.
     * @param critical If not {@code null}, the required criticality of the
     *            groups.
     * @return The index for the next parameter in the statement.
     * @throws SQLException If an argument cannot be set in the statement.
     */
    protected int setStandardWhereParameters(PreparedStatement pstmt, DataModelCategory category, TimeSpan expirationRange,
            Boolean critical) throws SQLException
    {
        int index = 1;
        if (category.getFamily() != null)
        {
            pstmt.setString(index++, category.getFamily());
        }
        if (category.getCategory() != null)
        {
            pstmt.setString(index++, category.getCategory());
        }
        if (category.getSource() != null)
        {
            pstmt.setString(index++, category.getSource());
        }
        if (critical != null)
        {
            pstmt.setBoolean(index++, critical.booleanValue());
        }
        if (expirationRange != null)
        {
            if (!expirationRange.isUnboundedStart())
            {
                pstmt.setLong(index++, expirationRange.getStart());
            }
            if (!expirationRange.isUnboundedEnd())
            {
                pstmt.setLong(index++, expirationRange.getEnd());
            }
        }
        return index;
    }
}
