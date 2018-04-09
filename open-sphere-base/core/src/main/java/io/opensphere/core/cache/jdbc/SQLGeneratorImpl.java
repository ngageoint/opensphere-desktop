package io.opensphere.core.cache.jdbc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.MultiPropertyMatcher;
import io.opensphere.core.cache.matcher.NumberPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.StringPropertyMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.cache.util.PropertyArrayDescriptor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Implementation of {@link SQLGenerator}.
 */
@SuppressWarnings("PMD.GodClass")
public class SQLGeneratorImpl implements SQLGenerator
{
    /** A comma and a space. */
    protected static final String COMMA = ", ";

    /** Where expression builder for general parameters. */
    protected static final WhereExpressionBuilder GENERAL_WHERE_BUILDER = new WhereExpressionBuilder()
    {
        @Override
        protected void addOperator(PropertyMatcher<?> param, StringBuilder where)
        {
            GeneralPropertyMatcher.OperatorType oper = ((GeneralPropertyMatcher<?>)param).getOperator();
            switch (oper)
            {
                case EQ:
                    where.append(SQL.EQUALS);
                    break;
                case NE:
                    where.append(SQL.NOT_EQUALS);
                    break;
                default:
                    throw new UnexpectedEnumException(oper);
            }
        }
    };

    /** Where expression builder for geometry parameters. */
    protected static final WhereExpressionBuilder GEOMETRY_WHERE_BUILDER = new WhereExpressionBuilder()
    {
        @Override
        protected void addOperator(PropertyMatcher<?> param, StringBuilder where)
        {
            throw new UnsupportedOperationException();
        }
    };

    /** <i>In</i> followed by a parenthesis. */
    protected static final String IN_PAREN = SQL.IN + " (";

    /** Where expression builder for number parameters. */
    protected static final WhereExpressionBuilder NUMBER_WHERE_BUILDER = new WhereExpressionBuilder()
    {
        @Override
        protected void addOperator(io.opensphere.core.cache.matcher.PropertyMatcher<?> param, StringBuilder where)
        {
            NumberPropertyMatcher.OperatorType oper = ((NumberPropertyMatcher<?>)param).getOperator();
            switch (oper)
            {
                case EQ:
                    where.append(SQL.EQUALS);
                    break;
                case GT:
                    where.append(" > ");
                    break;
                case GTE:
                    where.append(" >= ");
                    break;
                case LT:
                    where.append(" < ");
                    break;
                case LTE:
                    where.append(" <= ");
                    break;
                case NE:
                    where.append(SQL.NOT_EQUALS);
                    break;
                default:
                    throw new UnexpectedEnumException(oper);
            }
        }
    };

    /** A parenthesis followed by a double quote. */
    protected static final String PAREN_QUOTE = " (\"";

    /** A double quote followed by a comma and another double quote. */
    protected static final String QUOTE_COMMA_QUOTE = "\", \"";

    /** A quote followed by a parenthesis. */
    protected static final String QUOTE_PAREN = "\")";

    /** Where expression builder for string parameters. */
    protected static final WhereExpressionBuilder STRING_WHERE_BUILDER = new WhereExpressionBuilder()
    {
        @Override
        protected void addOperator(io.opensphere.core.cache.matcher.PropertyMatcher<?> param, StringBuilder where)
        {
            StringPropertyMatcher.OperatorType oper = ((StringPropertyMatcher)param).getOperator();
            switch (oper)
            {
                case EQ:
                    where.append(SQL.EQUALS);
                    break;
                case LIKE:
                    where.append(SQL.LIKE);
                    break;
                case NE:
                    where.append(SQL.NOT_EQUALS);
                    break;
                default:
                    throw new UnexpectedEnumException(oper);
            }
        }
    };

    /** Where expression builder for time span parameters. */
    protected static final WhereExpressionBuilder TIME_SPAN_WHERE_BUILDER = new WhereExpressionBuilder()
    {
        @Override
        public void buildExpression(PropertyMatcher<?> param, List<String> columnNames, boolean quote, StringBuilder join,
                StringBuilder where)
        {
            if (columnNames.size() != 2)
            {
                throw new IllegalArgumentException("Expected 2 column names; got " + columnNames.size());
            }

            TimeSpan span = (TimeSpan)param.getOperand();
            if (!span.isUnboundedStart())
            {
                appendWhereOrAnd(where);
                if (quote)
                {
                    where.append('"');
                }
                where.append(columnNames.get(1));
                if (quote)
                {
                    where.append('"');
                }
                where.append(" > ?");
            }
            if (!span.isUnboundedEnd())
            {
                appendWhereOrAnd(where);
                if (quote)
                {
                    where.append('"');
                }
                where.append(columnNames.get(0));
                if (quote)
                {
                    where.append('"');
                }
                where.append(" < ?");
            }
        }

        @Override
        protected void addOperator(PropertyMatcher<?> param, StringBuilder where)
        {
        }
    };

    /** Map of property matcher types to where-expression builders. */
    protected static final Map<Class<?>, WhereExpressionBuilder> WHERE_EXPRESSION_BUILDER_MAP;

    static
    {
        Map<Class<?>, WhereExpressionBuilder> map = New.map();

        map.put(TimeSpanMatcher.class, TIME_SPAN_WHERE_BUILDER);
        map.put(StringPropertyMatcher.class, STRING_WHERE_BUILDER);
        map.put(NumberPropertyMatcher.class, NUMBER_WHERE_BUILDER);
        map.put(GeneralPropertyMatcher.class, GENERAL_WHERE_BUILDER);
        map.put(GeometryMatcher.class, GEOMETRY_WHERE_BUILDER);

        WHERE_EXPRESSION_BUILDER_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Add where clause conditions for the data model category and the
     * expiration time.
     *
     * @param category The category.
     * @param expirationRange If not {@code null}, the range that the groups'
     *            expiration times must lie within. If {@code null}, the groups'
     *            expiration times must be {@code null}.
     * @param critical If not {@code null}, the required criticality of the
     *            groups.
     * @param where The string builder for the where clause.
     */
    protected static void addStandardWhereConditions(DataModelCategory category, TimeSpan expirationRange, Boolean critical,
            StringBuilder where)
    {
        if (category != null)
        {
            if (category.getFamily() != null)
            {
                appendWhereOrAnd(where).append(ColumnNames.FAMILY).append("=?");
            }
            if (category.getCategory() != null)
            {
                appendWhereOrAnd(where).append(ColumnNames.CATEGORY).append("=?");
            }
            if (category.getSource() != null)
            {
                appendWhereOrAnd(where).append(ColumnNames.SOURCE).append("=?");
            }
        }
        if (critical != null)
        {
            appendWhereOrAnd(where).append(ColumnNames.CRITICAL).append("=?");
        }
        if (expirationRange == null)
        {
            appendWhereOrAnd(where).append(ColumnNames.EXPIRATION_TIME).append(SQL.IS_NULL);
        }
        else
        {
            if (!expirationRange.isUnboundedStart())
            {
                appendWhereOrAnd(where).append(SQL.EXPIRATION_TIME_QUERY).append(" >= ?");
            }
            if (!expirationRange.isUnboundedEnd())
            {
                appendWhereOrAnd(where).append(SQL.EXPIRATION_TIME_QUERY).append(" <= ?");
            }
        }
    }

    /**
     * If the string is empty, append WHERE, otherwise append AND.
     *
     * @param where The where clause.
     * @return The where clause.
     */
    private static StringBuilder appendWhereOrAnd(StringBuilder where)
    {
        return where.append(where.length() > 0 ? SQL.AND : SQL.WHERE);
    }

    @Override
    public String generateAddColumn(String tableName, String columnName, String columnType)
    {
        return new StringBuilder(64).append(SQL.ALTER_TABLE).append(tableName).append(SQL.ADD_IF_NOT_EXISTS).append('"')
                .append(columnName).append("\" ").append(columnType).toString();
    }

    @Override
    public String generateCreateIndex(String indexName, String tableName, boolean unique, String... columnNames)
    {
        StringBuilder sb = new StringBuilder(128).append(unique ? SQL.CREATE_INDEX_UNIQUE : SQL.CREATE_INDEX).append(indexName)
                .append(SQL.ON).append(tableName).append(PAREN_QUOTE);
        StringUtilities.join(sb, QUOTE_COMMA_QUOTE, columnNames).append(QUOTE_PAREN);
        return sb.toString();
    }

    @Override
    public String generateCreateSequence(String sequenceName, long startWith, long incrementBy)
    {
        return new StringBuilder(32).append(SQL.CREATE_SEQUENCE).append(sequenceName).append(SQL.START_WITH).append(startWith)
                .append(SQL.INCREMENT_BY).append(incrementBy).toString();
    }

    @Override
    public String generateCreateTable(String tableName, Map<String, String> columnNamesToTypes)
    {
        return generateCreateTable(tableName, columnNamesToTypes, null);
    }

    @Override
    public String generateCreateTable(String tableName, Map<String, String> columnNamesToTypes, PrimaryKeyConstraint primaryKey,
            ForeignKeyConstraint... foreignKeyConstraints)
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append(SQL.CREATE_TABLE).append(tableName).append(" (");

        for (Entry<String, String> entry : columnNamesToTypes.entrySet())
        {
            String columnName = entry.getKey();
            String colSqlType = entry.getValue();
            sb.append('"').append(columnName).append("\" ").append(colSqlType).append(COMMA);
        }
        if (primaryKey != null)
        {
            sb.append(SQL.PRIMARY_KEY).append(PAREN_QUOTE);
            StringUtilities.join(sb, QUOTE_COMMA_QUOTE, primaryKey.getColumnNames()).append(QUOTE_PAREN).append(COMMA);
        }
        if (foreignKeyConstraints != null)
        {
            for (ForeignKeyConstraint constraint : foreignKeyConstraints)
            {
                generateForeignKeyClause(constraint, sb).append(COMMA);
            }
        }
        sb.setLength(sb.length() - COMMA.length());
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String generateCreateTemporaryTable(String tempTableName, Map<String, String> columnNamesToTypes,
            PrimaryKeyConstraint primaryKeyConstraint)
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append(SQL.CREATE_TEMP_TABLE).append(tempTableName).append(" (");

        for (Entry<String, String> entry : columnNamesToTypes.entrySet())
        {
            String columnName = entry.getKey();
            String colSqlType = entry.getValue();
            sb.append('"').append(columnName).append("\" ").append(colSqlType).append(COMMA);
        }
        if (primaryKeyConstraint != null)
        {
            sb.append(SQL.PRIMARY_KEY).append(PAREN_QUOTE);
            StringUtilities.join(sb, QUOTE_COMMA_QUOTE, primaryKeyConstraint.getColumnNames()).append(QUOTE_PAREN).append(COMMA);
        }
        sb.setLength(sb.length() - COMMA.length());
        sb.append(')').append(SQL.NOT_PERSISTENT).append(SQL.TRANSACTIONAL);
        return sb.toString();
    }

    @Override
    public String generateDelete(String tableName, int[] ids)
    {
        StringBuilder sb = new StringBuilder(128).append(SQL.DELETE_FROM).append(tableName).append(SQL.WHERE)
                .append(ColumnNames.DATA_ID).append(IN_PAREN);
        return StringUtilities.join(sb, ", ", ids).append(')').toString();
    }

    @Override
    public String generateDelete(String tableName, JoinTableColumn joinTableColumn)
    {
        return new StringBuilder(128).append(SQL.DELETE_FROM).append(tableName).append(SQL.WHERE).append(SQL.EXISTS_SELECT)
                .append(joinTableColumn.getColumnName()).append(SQL.FROM).append(joinTableColumn.getTableName()).append(SQL.WHERE)
                .append(joinTableColumn.getColumnName()).append(SQL.EQUALS).append(ColumnNames.DATA_ID).append(')').toString();
    }

    @Override
    public String generateDeleteGroup(int groupId)
    {
        return new StringBuilder(64).append(SQL.DELETE_FROM).append(TableNames.DATA_GROUP).append(SQL.WHERE)
                .append(ColumnNames.GROUP_ID).append(SQL.EQUALS).append(groupId).toString();
    }

    @Override
    public String generateDropAllObjects()
    {
        return SQL.DROP_ALL_OBJECTS;
    }

    @Override
    public String generateDropTable(String tableName)
    {
        return new StringBuilder(32).append(SQL.DROP_TABLE).append(tableName).toString();
    }

    @Override
    public String generateDropTrigger(String trigger)
    {
        return SQL.DROP_TRIGGER + trigger;
    }

    @Override
    public String generateExpireGroups(int[] groupIds)
    {
        StringBuilder sb = new StringBuilder(128).append(SQL.UPDATE).append(TableNames.DATA_GROUP).append(SQL.SET)
                .append(ColumnNames.EXPIRATION_TIME).append(SQL.EQUALS).append('0').append(SQL.WHERE).append(ColumnNames.GROUP_ID)
                .append(SQL.IN).append(" (");
        return StringUtilities.join(sb, ", ", groupIds).append(')').toString();
    }

    @Override
    public String generateGetExpiredGroups(long thresholdMilliseconds)
    {
        return new StringBuilder(128).append(SQL.SELECT).append(ColumnNames.GROUP_ID).append(SQL.FROM)
                .append(TableNames.DATA_GROUP).append(SQL.WHERE).append(SQL.EXPIRATION_TIME_QUERY).append(" < ")
                .append(thresholdMilliseconds).toString();
    }

    @Override
    public String generateGetNextSequenceValue(String sequenceName)
    {
        return new StringBuilder().append(SQL.SELECT).append(SQL.NEXT_VALUE_FOR).append(sequenceName).toString();
    }

    @Override
    public String generateInsert(String tableName, String... columnNames)
    {
        StringBuilder sb = new StringBuilder(64).append(SQL.INSERT_INTO).append(tableName).append(PAREN_QUOTE);
        StringBuilder values = new StringBuilder();
        if (columnNames.length > 0)
        {
            StringUtilities.join(sb, QUOTE_COMMA_QUOTE, columnNames).append('"');
            StringUtilities.repeat("?, ", columnNames.length, values);
            values.setLength(values.length() - 2);
        }
        else
        {
            sb.setLength(sb.length() - 1);
        }
        sb.append(") values (").append(values).append(')');

        return sb.toString();
    }

    @Override
    public String generateMerge(String tableName, String[] keyColumnNames, String[] columnNames)
    {
        StringBuilder part1 = new StringBuilder(128).append(SQL.MERGE_INTO).append(tableName).append(PAREN_QUOTE);
        StringBuilder part2 = new StringBuilder(32);

        if (keyColumnNames.length > 0)
        {
            part2.append(SQL.KEY);
            StringUtilities.join(part2, QUOTE_COMMA_QUOTE, keyColumnNames).append(QUOTE_PAREN);
        }
        part2.append(SQL.VALUES);

        if (columnNames.length == 0)
        {
            part1.setCharAt(part1.length() - 1, ')');
            part2.append(')');
        }
        else
        {
            StringUtilities.join(part1, QUOTE_COMMA_QUOTE, columnNames).append(QUOTE_PAREN);
            StringUtilities.repeat("?, ", columnNames.length, part2).setLength(part2.length() - 2);
            part2.append(')');
        }

        return part1.append(part2).toString();
    }

    @Override
    public String generateNullSelectForUpdate(String tableName)
    {
        return new StringBuilder().append(SQL.SELECT).append(SQL.NULL).append(SQL.FROM).append(tableName).append(SQL.WHERE)
                .append("1=0").append(SQL.FOR_UPDATE).toString();
    }

    @Override
    public String generateRetrieveDataModelCategories(boolean selectSource, boolean selectFamily, boolean selectCategory,
            boolean distinct, JoinTableColumn joinTableColumn, String whereExpression)
    {
        StringBuilder sb = new StringBuilder(128).append(distinct ? SQL.SELECT_DISTINCT : SQL.SELECT);
        if (selectSource)
        {
            sb.append(ColumnNames.SOURCE).append(", ");
        }
        if (selectFamily)
        {
            sb.append(ColumnNames.FAMILY).append(", ");
        }
        if (selectCategory)
        {
            sb.append(ColumnNames.CATEGORY).append(", ");
        }

        sb.setLength(sb.length() - 2);

        sb.append(SQL.FROM).append(TableNames.DATA_GROUP);
        if (joinTableColumn != null)
        {
            sb.append(SQL.RIGHT_JOIN).append(joinTableColumn.getTableName()).append(SQL.ON).append(joinTableColumn.getTableName())
                    .append('.').append(joinTableColumn.getColumnName()).append(SQL.EQUALS).append(SQL.DATA_GROUP_GROUP_ID);
        }
        if (whereExpression != null && whereExpression.length() > 0)
        {
            sb.append(SQL.WHERE).append(whereExpression);
        }
        if (!distinct && joinTableColumn != null)
        {
            sb.append(SQL.ORDER_BY).append(joinTableColumn.getTableName()).append(SQL.DOT_SEQUENCE);
        }
        return sb.toString();
    }

    @Override
    public String generateRetrieveGroupValuesSql(int[] groupIds, JoinTableColumn joinTableColumn, DataModelCategory category,
            Collection<? extends PropertyMatcher<?>> parameters, Collection<? extends PropertyDescriptor<?>> selectProperties,
            TimeSpan expirationRange, Boolean critical)
        throws CacheException
    {
        StringBuilder selectExpression = new StringBuilder(512).append(SQL.SELECT);
        StringBuilder tableExpression = new StringBuilder(256).append(SQL.FROM).append(TableNames.DATA_GROUP);
        StringBuilder joinExpression = new StringBuilder();
        StringBuilder whereExpression = new StringBuilder(256);
        StringBuilder intersectExpression = new StringBuilder();

        addGroupIdConditions(groupIds, joinTableColumn, tableExpression, whereExpression);

        addStandardWhereConditions(category, expirationRange, critical, whereExpression);

        selectExpression.append(SQL.DATA_GROUP_GROUP_ID).append(", ");

        Map<PropertyDescriptor<?>, PropertyMatcher<?>> propMap;
        if (CollectionUtilities.hasContent(parameters))
        {
            propMap = New.map(parameters.size());
        }
        else
        {
            propMap = Collections.emptyMap();
        }
        Map<PropertyDescriptor<?>, String> aliasMap;
        if (CollectionUtilities.hasContent(selectProperties))
        {
            aliasMap = New.map(selectProperties.size());
        }
        else
        {
            aliasMap = Collections.emptyMap();
        }

        int index = 0;
        if (parameters != null)
        {
            for (PropertyMatcher<?> parameter : parameters)
            {
                if (selectProperties.contains(parameter.getPropertyDescriptor()))
                {
                    aliasMap.put(parameter.getPropertyDescriptor(), "t" + index);
                    propMap.put(parameter.getPropertyDescriptor(), parameter);
                }

                processIntervalParameter(index++, (IntervalPropertyMatcher<?>)parameter, true, joinExpression, whereExpression,
                        intersectExpression);
            }
        }
        if (CollectionUtilities.hasContent(selectProperties))
        {
            processSelectProperties(selectProperties, selectExpression, joinExpression, propMap, aliasMap, index);
        }
        selectExpression.setLength(selectExpression.length() - 2);

        return selectExpression.append(tableExpression).append(joinExpression).append(whereExpression).append(intersectExpression)
                .toString();
    }

    @Override
    public String generateRetrieveIds(int[] groupIds, Collection<? extends PropertyMatcher<?>> parameters,
            List<String> joinTableNames, List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit,
            TypeMapper typeMapper)
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append(buildGetIdQuery(groupIds, parameters, orderSpecifiers, joinTableNames, typeMapper));
        if (limit >= 0 && limit < Integer.MAX_VALUE || startIndex > 0)
        {
            sb.append(SQL.LIMIT).append(limit).append(SQL.OFFSET).append(startIndex);
        }
        return sb.toString();
    }

    @Override
    public String generateRetrieveSchemaVersion()
    {
        return new StringBuilder(32).append(SQL.SELECT).append(ColumnNames.VERSION).append(SQL.FROM)
                .append(TableNames.SCHEMA_VERSION).toString();
    }

    @Override
    public String generateRetrieveValues(int dataId, String tableName, Collection<String> columnNames)
    {
        StringBuilder sb = new StringBuilder(256).append(SQL.SELECT).append('"');

        StringUtilities.join(sb, QUOTE_COMMA_QUOTE, columnNames).append('"');

        sb.append(SQL.FROM).append(tableName);

        sb.append(SQL.WHERE).append(ColumnNames.DATA_ID).append(SQL.EQUALS).append(dataId);

        return sb.toString();
    }

    @Override
    public String generateRetrieveValues(String joinTableName, String tableName, Collection<String> columnNames)
    {
        StringBuilder sb = new StringBuilder(256).append(SQL.SELECT).append('"');

        StringUtilities.join(sb, QUOTE_COMMA_QUOTE, columnNames).append('"');

        sb.append(SQL.FROM).append(tableName);

        sb.append(SQL.INNER_JOIN).append(joinTableName).append(SQL.ON).append(tableName).append('.').append(ColumnNames.DATA_ID)
                .append(SQL.EQUALS).append(joinTableName).append('.').append(ColumnNames.JOIN_ID);
        sb.append(SQL.ORDER_BY).append(joinTableName).append(SQL.DOT_SEQUENCE);

        return sb.toString();
    }

    @Override
    public String generateRetrieveValueSizes(int dataId, String tableName, Collection<String> columnNames)
    {
        StringBuilder sb = new StringBuilder(256).append(SQL.SELECT).append(SQL.LENGTH);

        StringUtilities.join(sb, "\") + " + SQL.LENGTH, columnNames).append("\")");

        sb.append(SQL.FROM).append(tableName);

        sb.append(SQL.WHERE).append(ColumnNames.DATA_ID).append(SQL.EQUALS).append(dataId);

        return sb.toString();
    }

    @Override
    public String generateRetrieveValueSizes(String joinTableName, String tableName, Collection<String> columnNames)
    {
        StringBuilder sb = new StringBuilder(256).append(SQL.SELECT).append(SQL.LENGTH);

        StringUtilities.join(sb, "\") + " + SQL.LENGTH, columnNames).append("\")");

        sb.append(SQL.FROM).append(tableName);

        sb.append(SQL.INNER_JOIN).append(joinTableName).append(SQL.ON).append(tableName).append('.').append(ColumnNames.DATA_ID)
                .append(SQL.EQUALS).append(joinTableName).append('.').append(ColumnNames.JOIN_ID);
        sb.append(SQL.ORDER_BY).append(joinTableName).append(SQL.DOT_SEQUENCE);

        return sb.toString();
    }

    @Override
    public String generateUpdate(String tableName, Map<String, String> columnNamesToValues, String whereExpression)
    {
        StringBuilder sb = new StringBuilder(128).append(SQL.UPDATE).append(tableName).append(SQL.SET);
        Set<Entry<String, String>> entrySet = columnNamesToValues.entrySet();
        for (Entry<String, String> entry : entrySet)
        {
            String columnName = entry.getKey();
            String value = entry.getValue();
            sb.append(columnName).append(SQL.EQUALS).append(value).append(", ");
        }
        sb.setLength(sb.length() - 2);
        if (whereExpression != null && whereExpression.length() > 0)
        {
            sb.append(SQL.WHERE).append(whereExpression);
        }
        return sb.toString();
    }

    @Override
    public String generateWhereExpression(String columnName, int[] values)
    {
        StringBuilder sb = new StringBuilder(64).append(columnName).append(IN_PAREN);
        StringUtilities.join(sb, ",", values);
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String generateWhereExpression(String columnName, JoinTableColumn joinTableColumn)
    {
        return new StringBuilder(64).append(columnName).append(IN_PAREN).append(SQL.SELECT)
                .append(joinTableColumn.getColumnName()).append(SQL.FROM).append(joinTableColumn.getTableName()).append(')')
                .toString();
    }

    /**
     * Add clauses to filter results based on some group ids.
     *
     * @param groupIds The optional group ids.
     * @param joinTableColumn An optional temporary table that contains the
     *            group ids.
     * @param tableExpression The table expression to append to.
     * @param whereExpression The where expression to append to.
     */
    protected void addGroupIdConditions(int[] groupIds, JoinTableColumn joinTableColumn, StringBuilder tableExpression,
            StringBuilder whereExpression)
    {
        if (joinTableColumn != null)
        {
            tableExpression.append(SQL.INNER_JOIN).append(joinTableColumn.getTableName()).append(SQL.ON)
                    .append(joinTableColumn.getTableName()).append('.').append(joinTableColumn.getColumnName()).append(SQL.EQUALS)
                    .append(SQL.DATA_GROUP_GROUP_ID);
        }
        else if (groupIds != null)
        {
            if (groupIds.length == 1)
            {
                whereExpression.append(SQL.WHERE).append(SQL.DATA_GROUP_GROUP_ID).append(SQL.EQUALS).append(groupIds[0]);
            }
            else
            {
                whereExpression.append(SQL.WHERE).append(SQL.DATA_GROUP_GROUP_ID).append(IN_PAREN);
                StringUtilities.join(whereExpression, ", ", groupIds).append(')');
            }
        }
    }

    /**
     * Get the select expression for some property descriptors. This appends to
     * the given string builder and leaves a dangling comma and space.
     *
     * @param descs The property descriptions.
     * @param sb The string builder to which the expression is to be added.
     * @param typeMapper The type mapper.
     */
    protected void buildColumnsSql(Collection<? extends PropertyDescriptor<?>> descs, StringBuilder sb, TypeMapper typeMapper)
    {
        Collection<String> columnNames = typeMapper.getColumnNames(descs);

        for (String columnName : columnNames)
        {
            sb.append('\"').append(columnName).append("\", ");
        }
    }

    /**
     * Build the columns portion of the id query.
     *
     * @param orderSpecifiers The order specifiers.
     * @param typeMapper The type mapper.
     * @return The required columns.
     */
    protected String buildColumnsSqlForIdQuery(List<? extends OrderSpecifier> orderSpecifiers, TypeMapper typeMapper)
    {
        StringBuilder columns = new StringBuilder(128).append(ColumnNames.DATA_ID);
        if (CollectionUtilities.hasContent(orderSpecifiers))
        {
            columns.append(", ");

            Collection<PropertyDescriptor<?>> descs = new LinkedHashSet<>(orderSpecifiers.size());
            for (OrderSpecifier orderSpecifier : new LinkedHashSet<OrderSpecifier>(orderSpecifiers))
            {
                descs.add(orderSpecifier.getPropertyDescriptor());
            }

            buildColumnsSql(descs, columns, typeMapper);
            columns.setLength(columns.length() - 2);
        }
        return columns.toString();
    }

    /**
     * Build the SQL string for a get id query.
     *
     * @param groupIds The group ids.
     * @param parameters The optional parameters on the query.
     * @param orderSpecifiers The order specifiers for the query.
     * @param joinTableNames Any temporary tables created by this call will be
     *            added to this collection.
     * @param typeMapper A type mapper.
     * @return The query.
     */
    protected String buildGetIdQuery(int[] groupIds, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, List<String> joinTableNames, TypeMapper typeMapper)
    {
        StringBuilder sql = new StringBuilder(256);

        String columns = buildColumnsSqlForIdQuery(orderSpecifiers, typeMapper);

        String orderBy = buildOrderByClause(orderSpecifiers, typeMapper);

        StringBuilder join;
        StringBuilder where;
        StringBuilder intersect;
        if (CollectionUtilities.hasContent(parameters))
        {
            join = new StringBuilder(128);
            where = new StringBuilder(128);
            intersect = new StringBuilder();
        }
        else
        {
            join = where = intersect = new StringBuilder(0);
        }

        String part1 = new StringBuilder().append('(').append(SQL.SELECT).toString();
        String part2 = new StringBuilder(32).append(SQL.AS).append(ColumnNames.GROUP_ID).append(", ").append(columns)
                .append(SQL.FROM).toString();

        for (int groupId : groupIds)
        {
            String tableName = TableNames.getDataTableName(groupId);
            sql.append(part1).append(groupId).append(part2).append(tableName);

            if (CollectionUtilities.hasContent(parameters))
            {
                join.setLength(0);
                where.setLength(0);
                intersect.setLength(0);
                Iterator<String> joinTableNameIterator = joinTableNames == null ? null : joinTableNames.iterator();
                for (PropertyMatcher<?> parameter : parameters)
                {
                    List<String> columnNames = typeMapper.getColumnNames(parameter.getPropertyDescriptor());
                    processParameter(groupId, parameter, columnNames, joinTableNameIterator, join, where, intersect);
                }
                sql.append(join).append(where).append(intersect);
            }
            sql.append(')').append(SQL.UNION);
        }

        sql.setLength(sql.length() - SQL.UNION.length());

        if (orderBy != null && orderBy.length() > 0)
        {
            sql.append(orderBy);
        }
        else if (joinTableNames != null && !joinTableNames.isEmpty())
        {
            sql.append(SQL.ORDER_BY).append(joinTableNames.get(0)).append(SQL.DOT_SEQUENCE);
        }

        return sql.toString();
    }

    /**
     * Build the order-by clause for a query.
     *
     * @param orderSpecifiers The order specifiers.
     * @param typeMapper A type mapper.
     * @return The order-by clause.
     */
    protected String buildOrderByClause(final List<? extends OrderSpecifier> orderSpecifiers, TypeMapper typeMapper)
    {
        if (CollectionUtilities.hasContent(orderSpecifiers))
        {
            StringBuilder sb = new StringBuilder(128);
            sb.append(SQL.ORDER_BY);
            for (OrderSpecifier orderSpecifier : orderSpecifiers)
            {
                PropertyDescriptor<?> desc = orderSpecifier.getPropertyDescriptor();
                sb.append('"').append(typeMapper.getOrderByColumnName(desc)).append('"');

                switch (orderSpecifier.getOrder())
                {
                    case ASCENDING:
                        sb.append(" ASC, ");
                        break;
                    case DESCENDING:
                        sb.append(" DESC, ");
                        break;
                    default:
                        throw new UnexpectedEnumException(orderSpecifier.getOrder());
                }
            }
            sb.setLength(sb.length() - 2);
            return sb.toString();
        }
        else
        {
            return StringUtilities.EMPTY;
        }
    }

    /**
     * Get the where expression for an individual parameter, specifying the
     * column names.
     *
     * @param parameter The query parameter.
     * @param columnNames The column names to use.
     * @param quote If the column names need to be quoted.
     * @param join The string builder to which any join expressions may be
     *            added.
     * @param where The string builder to which the where expression is to be
     *            added.
     * @param intersect The string builder to which the intersect expression is
     *            to be added.
     */
    protected void buildWhereExpression(PropertyMatcher<?> parameter, List<String> columnNames, boolean quote, StringBuilder join,
            StringBuilder where, StringBuilder intersect)
    {
        if (parameter.getPropertyDescriptor() instanceof PropertyArrayDescriptor)
        {
            throw new UnsupportedOperationException("Property matchers for property arrays are not currently supported.");
        }

        WhereExpressionBuilder expressionBuilder = null;
        Class<?> cl = parameter.getClass();
        do
        {
            expressionBuilder = WHERE_EXPRESSION_BUILDER_MAP.get(cl);
        }
        while (expressionBuilder == null && (cl = cl.getSuperclass()) != null);

        if (expressionBuilder == null)
        {
            throw new UnsupportedOperationException("Property matcher type is not supported: " + parameter.getClass());
        }
        else
        {
            expressionBuilder.buildExpression(parameter, columnNames, quote, join, where);
        }
    }

    /**
     * Generate the SQL for a foreign key clause.
     *
     * @param constraint The foreign key constraint.
     * @param sb A string builder.
     * @return The string builder.
     */
    protected StringBuilder generateForeignKeyClause(ForeignKeyConstraint constraint, StringBuilder sb)
    {
        sb.append(SQL.FOREIGN_KEY);
        StringUtilities.join(sb, COMMA, constraint.getNativeColumnNames());
        sb.append(SQL.REFERENCES).append(constraint.getForeignTableName()).append(" (");
        StringUtilities.join(sb, COMMA, constraint.getForeignColumnNames());
        sb.append(')').append(SQL.ON_DELETE_CASCADE);
        return sb;
    }

    /**
     * Process an interval query parameter and add SQL to the string builders.
     *
     * @param index The index for this parameter.
     * @param parameter The parameter.
     * @param joinGroupTable Indicates if a column is being selected on the
     *            group data table.
     * @param join The string builder for the join clause.
     * @param where The string builder for the where clause.
     * @param intersect The string builder for the union/minus/intersect clause.
     */
    protected void processIntervalParameter(int index, IntervalPropertyMatcher<?> parameter, boolean joinGroupTable,
            StringBuilder join, StringBuilder where, StringBuilder intersect)
    {
        PropertyDescriptor<?> desc = parameter.getPropertyDescriptor();

        String tableAlias = "T" + index;
        String groupTableName = TableNames.getGroupTableName(desc.getType());

        // Special case for timeless time span parameter. Remove groups that
        // have any time span interval.
        if (parameter instanceof TimeSpanMatcher && ((TimeSpanMatcher)parameter).getOperand().isTimeless())
        {
            appendWhereOrAnd(where);
            where.append(SQL.NOT).append(SQL.EXISTS_SELECT).append('*').append(SQL.FROM).append(groupTableName).append(SQL.AS)
                    .append(tableAlias).append(SQL.WHERE).append(tableAlias).append(SQL.DOT_GROUP_ID).append(SQL.EQUALS)
                    .append(SQL.DATA_GROUP_GROUP_ID);
        }
        else
        {
            join.append(SQL.INNER_JOIN).append(groupTableName).append(SQL.AS).append(tableAlias).append(SQL.ON).append(tableAlias)
                    .append(SQL.DOT_GROUP_ID).append(SQL.EQUALS).append(SQL.DATA_GROUP_GROUP_ID);
        }

        if (!(parameter instanceof GeometryMatcher))
        {
            where.append(SQL.AND).append(tableAlias).append('.').append(ColumnNames.PROPERTY).append(" = ?");
        }

        List<String> columnNames;
        if (TimeSpan.class.equals(desc.getType()))
        {
            if (parameter instanceof TimeSpanMatcher && ((TimeSpanMatcher)parameter).getOperand().isTimeless())
            {
                // Complete the special timeless syntax.
                where.append(')');
                columnNames = null;
            }
            else
            {
                columnNames = Arrays.asList(tableAlias + '.' + ColumnNames.VALUE_START, tableAlias + '.' + ColumnNames.VALUE_END);
            }
        }
        else if (parameter instanceof GeometryMatcher)
        {
            // Spatial operations cannot be done using SQL.
            columnNames = null;
        }
        else
        {
            columnNames = Collections.singletonList(tableAlias + '.' + ColumnNames.VALUE);
        }
        if (columnNames != null)
        {
            buildWhereExpression(parameter, columnNames, false, join, where, intersect);
        }
    }

    /**
     * Process a parameter on a query. Add the appropriate SQL to the string
     * builders.
     *
     * @param groupId The id for the group being processed.
     * @param parameter The parameter.
     * @param columnNames The column names for the parameter.
     * @param joinTableNameIterator An iterator over the tables containing
     *            values for {@link MultiPropertyMatcher}s.
     * @param join The string builder to which any join expressions may be
     *            added.
     * @param where The string builder to which the where expression is to be
     *            added.
     * @param intersect The string builder to which the intersect expression is
     *            to be added.
     */
    protected void processParameter(int groupId, PropertyMatcher<?> parameter, List<String> columnNames,
            Iterator<String> joinTableNameIterator, StringBuilder join, StringBuilder where, StringBuilder intersect)
    {
        if (parameter instanceof MultiPropertyMatcher)
        {
            String joinTableName = joinTableNameIterator.next();
            String columnName = columnNames.get(0);
            join.append(SQL.INNER_JOIN).append(joinTableName).append(SQL.ON).append(joinTableName).append('.')
                    .append(ColumnNames.VALUE).append(SQL.EQUALS).append('"').append(columnName).append('"');
        }
        else
        {
            buildWhereExpression(parameter, columnNames, true, join, where, intersect);
        }
    }

    /**
     * Process the select properties for a group value query. The select
     * properties are the properties whose values are being selected.
     *
     * @param selectProperties The select properties.
     * @param selectExpression A string builder for the select expression, which
     *            already contains SELECT and possibly other columns.
     * @param joinExpression A string builder for the join expression.
     * @param propMap A map of property descriptors to property matchers for the
     *            properties in the parameters.
     * @param aliasMap A map of property descriptors to aliases that have been
     *            defined already.
     * @param startIndex The next index for a join table.
     * @throws CacheException If there's a database error.
     */
    private void processSelectProperties(Collection<? extends PropertyDescriptor<?>> selectProperties,
            StringBuilder selectExpression, StringBuilder joinExpression, Map<PropertyDescriptor<?>, PropertyMatcher<?>> propMap,
            Map<PropertyDescriptor<?>, String> aliasMap, int startIndex)
        throws CacheException
    {
        int index = startIndex;
        for (PropertyDescriptor<?> desc : selectProperties)
        {
            String alias = aliasMap.get(desc);
            if (alias == null)
            {
                alias = "t" + index++;
                String groupTableName = TableNames.getGroupTableName(desc.getType());
                joinExpression.append(SQL.INNER_JOIN).append(groupTableName).append(SQL.AS).append(alias).append(SQL.ON)
                        .append(alias).append(SQL.DOT_GROUP_ID).append(SQL.EQUALS).append(SQL.DATA_GROUP_GROUP_ID);
            }
            if (TimeSpan.class.isAssignableFrom(desc.getType()))
            {
                PropertyMatcher<?> propertyMatcher = propMap.get(desc);
                if (propertyMatcher == null || !((TimeSpan)propertyMatcher.getOperand()).isTimeless())
                {
                    selectExpression.append(alias).append('.').append(ColumnNames.VALUE_START).append(", ");
                    selectExpression.append(alias).append('.').append(ColumnNames.VALUE_END).append(", ");
                }
                else
                {
                    selectExpression.append(SQL.NULL + ", " + SQL.NULL + ", ");
                }
            }
            else if (Serializable.class.isAssignableFrom(desc.getType()))
            {
                selectExpression.append(alias).append('.').append(ColumnNames.VALUE).append(", ");
            }
            else
            {
                throw new CacheException("Unsupported parameter type for group query: " + desc);
            }
        }
    }

    /**
     * Class for objects that help build where expressions based on property
     * matchers.
     */
    protected abstract static class WhereExpressionBuilder
    {
        /**
         * Build a clause in a where expression based on a property matcher.
         *
         * @param param The property matcher.
         * @param columnNames The column names.
         * @param quote If the columns need to be quoted in the SQL.
         * @param join The string builder to which to add any join clauses.
         * @param where The string builder to which to add the where clause.
         */
        public void buildExpression(PropertyMatcher<?> param, List<String> columnNames, boolean quote, StringBuilder join,
                StringBuilder where)
        {
            if (columnNames.size() != 1)
            {
                throw new IllegalArgumentException("Expected 1 column name; got " + columnNames.size());
            }

            where.append(where.length() > 0 ? SQL.AND : SQL.WHERE);
            if (quote)
            {
                where.append('"');
            }
            where.append(columnNames.get(0));
            if (quote)
            {
                where.append('"');
            }
            addOperator(param, where);
            where.append('?');
        }

        /**
         * Add the operator to the where expression.
         *
         * @param param The property matcher.
         * @param where The string builder to which to add the where clause.
         */
        protected abstract void addOperator(PropertyMatcher<?> param, StringBuilder where);
    }
}
