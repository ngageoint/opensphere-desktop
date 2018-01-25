package io.opensphere.auxiliary.cache.jdbc;

import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.io.WKTWriter;

import io.opensphere.core.cache.jdbc.ColumnNames;
import io.opensphere.core.cache.jdbc.SQL;
import io.opensphere.core.cache.jdbc.TableNames;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.GeometryMatcher.OperatorType;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;

/**
 * SQL generator with Hatbox extensions.
 */
public class HatboxSQLGeneratorImpl extends H2SQLGeneratorImpl
{
    /** Flag indicating if INTERSECT can be used. */
    private boolean myUseIntersect;

    @Override
    protected void processIntervalParameter(int index, IntervalPropertyMatcher<?> param, boolean joinGroupTable,
            StringBuilder join, StringBuilder where, StringBuilder intersect)
    {
        if (param instanceof GeometryMatcher)
        {
            String groupTableName = TableNames.getGroupTableName(param.getPropertyDescriptor().getType());
            String wkt = new WKTWriter().write(((GeometryMatcher)param).getOperand());

            // Assume that the enum constants in OperatorType match those in
            // Hatbox (except for INTERSECTS_NO_TOUCH).
            OperatorType op = ((GeometryMatcher)param).getOperator();

            String proc;
            String procArg;
            if (op == OperatorType.INTERSECTS_NO_TOUCH)
            {
                proc = "HATBOX_QUERY_WITH_MATRIX_WKT('";
                procArg = "T********";
            }
            else
            {
                proc = "HATBOX_QUERY_WITH_PREDICATE_WKT('";
                procArg = op.toString();
            }
            if (joinGroupTable)
            {
                join.append(SQL.INNER_JOIN).append(proc).append(HatboxUtilities.SCHEMA_NAME).append(SQL.TICK_COMMA_TICK)
                        .append(groupTableName).append(SQL.TICK_COMMA_TICK).append(procArg).append(SQL.TICK_COMMA_TICK)
                        .append(wkt).append("')").append(SQL.ON).append("HATBOX_JOIN_ID = ").append(SQL.DATA_GROUP_GROUP_ID);

                String tableAlias = "t" + index;
                join.append(SQL.INNER_JOIN).append(groupTableName).append(SQL.AS).append(tableAlias).append(SQL.ON)
                        .append(tableAlias).append(SQL.DOT_GROUP_ID).append(SQL.EQUALS).append(SQL.DATA_GROUP_GROUP_ID);
            }
            else
            {
                intersect.append(SQL.INTERSECT_SELECT).append("HATBOX_JOIN_ID").append(SQL.FROM).append(proc)
                        .append(HatboxUtilities.SCHEMA_NAME).append(SQL.TICK_COMMA_TICK).append(groupTableName)
                        .append(SQL.TICK_COMMA_TICK).append(procArg).append(SQL.TICK_COMMA_TICK).append(wkt).append("')");
            }
        }
        else
        {
            super.processIntervalParameter(index, param, joinGroupTable, join, where, intersect);
        }
    }

    @Override
    protected void processParameter(int groupId, PropertyMatcher<?> parameter, List<String> columnNames,
            Iterator<String> joinTableNameIterator, StringBuilder join, StringBuilder where, StringBuilder intersect)
    {
        if (parameter instanceof GeometryMatcher)
        {
            // Assume that the enum constants in OperatorType match those in
            // Hatbox (except for INTERSECTS_NO_TOUCH).
            OperatorType op = ((GeometryMatcher)parameter).getOperator();

            String proc;
            String procArg;
            if (op == OperatorType.INTERSECTS_NO_TOUCH)
            {
                proc = "HATBOX_QUERY_WITH_MATRIX_WKT('";
                procArg = "T********";
            }
            else
            {
                proc = "HATBOX_QUERY_WITH_PREDICATE_WKT('";
                procArg = op.toString();
            }

            String tableName = TableNames.getDataTableName(groupId);
            String wkt = new WKTWriter().write(((GeometryMatcher)parameter).getOperand());
            String intersectFunction = new StringBuilder(128).append(proc).append(HatboxUtilities.SCHEMA_NAME)
                    .append(SQL.TICK_COMMA_TICK).append(tableName).append(SQL.TICK_COMMA_TICK).append(procArg)
                    .append(SQL.TICK_COMMA_TICK).append(wkt).append("')").toString();

            if (myUseIntersect)
            {
                intersect.append(SQL.INTERSECT_SELECT).append(groupId).append(" as GROUP_ID, HATBOX_JOIN_ID").append(SQL.FROM)
                        .append(intersectFunction);
            }
            else
            {
                join.append(SQL.INNER_JOIN).append(intersectFunction).append(SQL.ON).append("HATBOX_JOIN_ID = ").append(tableName)
                        .append('.').append(ColumnNames.DATA_ID);
            }
        }
        else
        {
            super.processParameter(groupId, parameter, columnNames, joinTableNameIterator, join, where, intersect);
        }
    }
}
