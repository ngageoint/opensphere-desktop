package io.opensphere.core.cache.jdbc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * Container for table name constants.
 */
public final class TableNames
{
    /** Table for data groups. */
    public static final String DATA_GROUP = "DATA_GROUP";

    /** Table for the version of the database schema. */
    public static final String SCHEMA_VERSION = "SCHEMA_VERSION";

    /** Map of property value types to table names. */
    public static final Map<Class<?>, String> TABLE_NAME_MAP;

    /** Prefix for data tables. */
    private static final String DATA_TABLE_PREFIX = "DATA";

    static
    {
        Map<Class<?>, String> tableNameMap = New.map();
        tableNameMap.put(String.class, "VARCHAR_DATA");
        tableNameMap.put(Integer.class, "INT_DATA");
        tableNameMap.put(Boolean.class, "BOOL_DATA");
        tableNameMap.put(Byte.class, "TINYINT_DATA");
        tableNameMap.put(Short.class, "SMALLINT_DATA");
        tableNameMap.put(Long.class, "BIGINT_DATA");
        tableNameMap.put(BigDecimal.class, "DECIMAL_DATA");
        tableNameMap.put(Double.class, "DOUBLE_DATA");
        tableNameMap.put(Float.class, "REAL_DATA");
        tableNameMap.put(Geometry.class, "GEOMETRY_DATA");
        tableNameMap.put(byte[].class, "BINARY_DATA");
        tableNameMap.put(Serializable.class, "SERIALIZABLE_DATA");
        tableNameMap.put(TimeSpan.class, "TIME_DATA");
        tableNameMap.put(Object[].class, "ARRAY_DATA");
        TABLE_NAME_MAP = Collections.unmodifiableMap(tableNameMap);
    }

    /**
     * Get the data table name for a given group id.
     *
     * @param groupId The group id.
     * @return The data table name.
     */
    public static String getDataTableName(int groupId)
    {
        return DATA_TABLE_PREFIX + groupId;
    }

    /**
     * Get the group table name for a particular property type.
     *
     * @param type The property type.
     * @return The group table name.
     */
    public static String getGroupTableName(Class<?> type)
    {
        String tableName = getTableName(type);
        if (tableName == null)
        {
            throw new UnsupportedOperationException("The property type [" + type.getName() + "] is not supported");
        }
        return new StringBuilder(32).append(tableName).append("_GROUP").toString();
    }

    /**
     * Get the table name for a particular property type.
     *
     * @param type The property type.
     * @return The table name.
     */
    public static String getTableName(Class<?> type)
    {
        String tableName = TABLE_NAME_MAP.get(type);
        if (tableName == null)
        {
            if (Serializable.class.isAssignableFrom(type))
            {
                tableName = TABLE_NAME_MAP.get(Serializable.class);
            }
            else if (Geometry.class.isAssignableFrom(type))
            {
                tableName = TABLE_NAME_MAP.get(Geometry.class);
            }
        }

        return tableName;
    }

    /** Disallow instantiation. */
    private TableNames()
    {
    }
}
