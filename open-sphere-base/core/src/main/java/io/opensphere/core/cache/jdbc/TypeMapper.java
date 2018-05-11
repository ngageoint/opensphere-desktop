package io.opensphere.core.cache.jdbc;

import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.jdbc.type.GeometryTranslator;
import io.opensphere.core.cache.jdbc.type.InputStreamTranslator;
import io.opensphere.core.cache.jdbc.type.ObjectTranslator;
import io.opensphere.core.cache.jdbc.type.SerializableTranslator;
import io.opensphere.core.cache.jdbc.type.StringTranslator;
import io.opensphere.core.cache.jdbc.type.TimespanTranslator;
import io.opensphere.core.cache.jdbc.type.ValueTranslator;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyArrayDescriptor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Class that handles translations between Java objects and database records.
 */
@SuppressWarnings("PMD.GodClass")
public class TypeMapper
{
    /** The maximum size allowed for cached values. */
    public static final int MAX_VALUE_SIZE_BYTES = Integer.getInteger("opensphere.db.maxValueSizeBytes", 52428800).intValue();

    /** The maximum size allowed for cached geometry values. */
    public static final int MAX_GEOM_SIZE_BYTES = Integer.getInteger("opensphere.db.maxGeomSizeBytes", 524288).intValue();

    /** Map of property value types to SQL types. */
    public static final Map<Class<?>, String> TYPE_MAP;

    /** Map of Java types to column name prefixes. */
    protected static final Map<Class<?>, String> COLUMN_NAME_MAP;

    /** A retriever for geometries. */
    protected static final ValueTranslator<Geometry> GEOMETRY_TRANSLATOR = new GeometryTranslator();

    /** A translator for input streams. */
    protected static final ValueTranslator<InputStream> INPUT_STREAM_TRANSLATOR = new InputStreamTranslator();

    /** A prepared statement setter for object values. */
    protected static final ValueTranslator<Object> OBJECT_TRANSLATOR = new ObjectTranslator();

    /** A translator for serializables. */
    protected static final ValueTranslator<Serializable> SERIALIZABLE_TRANSLATOR = new SerializableTranslator();

    /** A prepared statement setter for String values. */
    protected static final ValueTranslator<String> STRING_TRANSLATOR = new StringTranslator();

    /** A retriever for timespans. */
    protected static final ValueTranslator<TimeSpan> TIMESPAN_TRANSLATOR = new TimespanTranslator();

    /** Map of Java types to prepared statement setters. */
    protected static final Map<Class<?>, ValueTranslator<?>> TYPES_TO_TRANSLATORS;

    static
    {
        final Map<Class<?>, ValueTranslator<?>> map = New.map();
        map.put(String.class, STRING_TRANSLATOR);
        map.put(Integer.class, OBJECT_TRANSLATOR);
        map.put(Boolean.class, OBJECT_TRANSLATOR);
        map.put(Byte.class, OBJECT_TRANSLATOR);
        map.put(Short.class, OBJECT_TRANSLATOR);
        map.put(Long.class, OBJECT_TRANSLATOR);
        map.put(BigDecimal.class, OBJECT_TRANSLATOR);
        map.put(Double.class, OBJECT_TRANSLATOR);
        map.put(Float.class, OBJECT_TRANSLATOR);
        map.put(TimeSpan.class, TIMESPAN_TRANSLATOR);
        map.put(Geometry.class, GEOMETRY_TRANSLATOR);
        map.put(byte[].class, OBJECT_TRANSLATOR);
        map.put(Serializable.class, SERIALIZABLE_TRANSLATOR);
        map.put(InputStream.class, INPUT_STREAM_TRANSLATOR);
        TYPES_TO_TRANSLATORS = Collections.unmodifiableMap(map);
    }

    static
    {
        final Map<Class<?>, String> sqlTypeMap = New.map();
        sqlTypeMap.put(String.class, "varchar(2147483647)");
        sqlTypeMap.put(Integer.class, "int");
        sqlTypeMap.put(Boolean.class, "boolean");
        sqlTypeMap.put(Byte.class, "tinyint");
        sqlTypeMap.put(Short.class, "smallint");
        sqlTypeMap.put(Long.class, "bigint");
        sqlTypeMap.put(BigDecimal.class, "decimal");
        sqlTypeMap.put(Double.class, "double");
        sqlTypeMap.put(Float.class, "real");
        sqlTypeMap.put(Geometry.class, "binary(" + MAX_GEOM_SIZE_BYTES + ")");
        sqlTypeMap.put(byte[].class, "binary");
        sqlTypeMap.put(Serializable.class, "binary(" + MAX_VALUE_SIZE_BYTES + ")");
        sqlTypeMap.put(InputStream.class, "blob");
        TYPE_MAP = Collections.unmodifiableMap(sqlTypeMap);
    }

    static
    {
        final Map<Class<?>, String> map = New.map();
        map.put(String.class, "STR");
        map.put(Integer.class, "INT");
        map.put(Boolean.class, "BOOL");
        map.put(Byte.class, "BYTE");
        map.put(Short.class, "SHORT");
        map.put(Long.class, "LONG");
        map.put(BigDecimal.class, "BIGD");
        map.put(Double.class, "DOUB");
        map.put(Float.class, "FLOAT");
        map.put(Geometry.class, "GEOM");
        map.put(byte[].class, "BYTEARR");
        map.put(Serializable.class, "SERIAL");
        map.put(TimeSpan.class, "TIME");
        map.put(Object[].class, "ARR");
        map.put(InputStream.class, "STREAM");
        COLUMN_NAME_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Get a column name prefix for a data type.
     *
     * @param valueType The data type.
     * @return The column name prefix.
     */
    public String getColumnNamePrefix(Class<?> valueType)
    {
        String name = COLUMN_NAME_MAP.get(valueType);
        if (name == null)
        {
            if (Geometry.class.isAssignableFrom(valueType))
            {
                name = getColumnNamePrefix(Geometry.class);
            }
            else if (Serializable.class.isAssignableFrom(valueType))
            {
                name = getColumnNamePrefix(Serializable.class);
            }
        }
        if (name == null)
        {
            throw new IllegalArgumentException("Unsupported property type: " + valueType);
        }
        return name;
    }

    /**
     * Get the column names for some property descriptors.
     *
     * @param propertyDescriptors The property descriptors.
     * @return The map.
     */
    public List<String> getColumnNames(Collection<? extends PropertyDescriptor<?>> propertyDescriptors)
    {
        final List<String> columnNames = New.list();
        for (final PropertyDescriptor<?> desc : propertyDescriptors)
        {
            columnNames.addAll(getColumnNames(desc));
        }
        return columnNames;
    }

    /**
     * Get the column names for a property descriptor.
     *
     * @param desc The property descriptor.
     * @return The column names
     */
    public List<String> getColumnNames(PropertyDescriptor<?> desc)
    {
        final String base = getColumnNameBase(desc);

        List<String> columnNames;
        if (desc instanceof PropertyArrayDescriptor)
        {
            columnNames = getColumnNames(base, (PropertyArrayDescriptor)desc);
        }
        else if (desc.getType().equals(TimeSpan.class))
        {
            columnNames = Arrays.asList(base + "_START", base + "_END");
        }
        else
        {
            columnNames = Arrays.asList(base);
        }

        return columnNames;
    }

    /**
     * Get the column names for the active columns in a property array
     * descriptor.
     *
     * @param columnPrefix The column prefix.
     * @param desc The property array descriptor.
     * @return The map.
     */
    public List<String> getColumnNames(String columnPrefix, PropertyArrayDescriptor desc)
    {
        final int[] activeColumns = desc.getActiveColumns();
        final List<String> result = New.list(activeColumns.length);
        final Class<?>[] columnTypes = desc.getColumnTypes();
        final StringBuilder sb = new StringBuilder(32);
        for (final int arrayColumnIndex : activeColumns)
        {
            final Class<?> colType = columnTypes[arrayColumnIndex];
            if (colType == null)
            {
                throw new IllegalArgumentException("Column type is null for active column index " + arrayColumnIndex
                        + " in property descriptor [" + desc + "]");
            }
            final String arrayColumnPrefix = getColumnNamePrefix(colType);
            sb.append(columnPrefix).append('_').append(arrayColumnPrefix).append(arrayColumnIndex);
            result.add(sb.toString());
            sb.setLength(0);
        }
        return result;
    }

    /**
     * Get an ordered map of column names to column types for some property
     * descriptors. The iteration order of the map matches the iteration order
     * of the input property descriptors.
     *
     * @param propertyDescriptors The property descriptors.
     * @return The map.
     */
    public Map<String, String> getColumnNamesToTypes(Collection<? extends PropertyDescriptor<?>> propertyDescriptors)
    {
        final Map<String, String> columnNamesToTypes = New.insertionOrderMap();
        for (final PropertyDescriptor<?> desc : propertyDescriptors)
        {
            final String base = getColumnNamePrefix(desc.getType()) + "_" + desc.getPropertyName();

            if (desc instanceof PropertyArrayDescriptor)
            {
                columnNamesToTypes.putAll(getColumnsForPropertyArrayDescriptor(base, (PropertyArrayDescriptor)desc));
            }
            else if (TimeSpan.class.isAssignableFrom(desc.getType()))
            {
                final String type = getSqlType(Long.class);
                columnNamesToTypes.put(base + "_START", type);
                columnNamesToTypes.put(base + "_END", type);
            }
            else
            {
                final String type = getSqlType(desc.getType());
                columnNamesToTypes.put(base, type);
            }
        }
        return columnNamesToTypes;
    }

    /**
     * Get a map of column names to SQL column types required for the active
     * columns in a property array descriptor.
     *
     * @param columnPrefix The column prefix.
     * @param desc The property array descriptor.
     * @return The map.
     */
    public Map<String, String> getColumnsForPropertyArrayDescriptor(String columnPrefix, PropertyArrayDescriptor desc)
    {
        final int[] activeColumns = desc.getActiveColumns();
        final Map<String, String> result = new LinkedHashMap<>(activeColumns.length);
        final Class<?>[] columnTypes = desc.getColumnTypes();
        for (final int arrayColumnIndex : activeColumns)
        {
            final Class<?> colType = columnTypes[arrayColumnIndex];
            if (colType == null)
            {
                throw new IllegalArgumentException("Column type is null for active column index " + arrayColumnIndex
                        + " in property descriptor [" + desc + "]");
            }

            if (TimeSpan.class.isAssignableFrom(colType))
            {
                final String type = getSqlType(Long.class);
                final StringBuilder sb = new StringBuilder().append(columnPrefix).append('_')
                        .append(getColumnNamePrefix(Long.class)).append(arrayColumnIndex);
                result.put(sb.toString() + "_START", type);
                result.put(sb.toString() + "_END", type);
            }
            else
            {
                final StringBuilder sb = new StringBuilder().append(columnPrefix).append('_').append(getColumnNamePrefix(colType))
                        .append(arrayColumnIndex);
                result.put(sb.toString(), getSqlType(colType));
            }
        }
        return result;
    }

    /**
     * Get the name of the order-by column for a property array descriptor.
     *
     * @param desc The property array descriptor.
     * @return The column name.
     */
    public String getOrderByColumnName(PropertyArrayDescriptor desc)
    {
        final Class<?>[] columnTypes = desc.getColumnTypes();
        final int arrayColumnIndex = desc.getOrderByColumn();
        final Class<?> colType = columnTypes[arrayColumnIndex];
        if (colType == null)
        {
            throw new IllegalArgumentException("Column type is null for order-by column index " + arrayColumnIndex
                    + " in property descriptor [" + desc + "]");
        }
        final String arrayColumnPrefix = getColumnNamePrefix(colType);
        final String base = getColumnNameBase(desc);
        final StringBuilder sb = new StringBuilder().append(base).append('_').append(arrayColumnPrefix).append(arrayColumnIndex);
        return sb.toString();
    }

    /**
     * Get the column name to be used for ordering from a property descriptor.
     *
     * @param desc The property descriptor.
     * @return The order-by column.
     */
    public String getOrderByColumnName(PropertyDescriptor<?> desc)
    {
        if (desc instanceof PropertyArrayDescriptor)
        {
            return getOrderByColumnName((PropertyArrayDescriptor)desc);
        }
        return getColumnNames(desc).get(0);
    }

    /**
     * Get a column definition appropriate for a data type.
     *
     * @param valueType The data type.
     * @param nullable If the column can hold {@code null} values.
     * @return The SQL column definition, for a create table statement.
     */
    public String getSqlColumnDefinition(Class<?> valueType, boolean nullable)
    {
        return getSqlColumnDefinition(valueType, null, nullable, 0);
    }

    /**
     * Get a column definition appropriate for a data type.
     *
     * @param valueType The data type.
     * @param defaultValue The optional default value for the column.
     * @param nullable If the column can hold {@code null} values.
     * @return The SQL column definition, for a create table statement.
     */
    public String getSqlColumnDefinition(Class<?> valueType, String defaultValue, boolean nullable)
    {
        return getSqlColumnDefinition(valueType, defaultValue, nullable, 0);
    }

    /**
     * Get a column definition appropriate for a data type.
     *
     * @param valueType The data type.
     * @param defaultValue The optional default value for the column.
     * @param nullable If the column can hold {@code null} values.
     * @param increment If non-zero, each new row in this column will be
     *            automatically incremented by the specified amount.
     * @return The SQL column definition, for a create table statement.
     */
    public String getSqlColumnDefinition(Class<?> valueType, String defaultValue, boolean nullable, int increment)
    {
        final String sqlType = getSqlType(valueType);
        if (defaultValue == null && nullable && increment == 0)
        {
            return sqlType;
        }
        final StringBuilder sb = new StringBuilder().append(sqlType);
        if (defaultValue != null)
        {
            sb.append(" default ").append(defaultValue);
        }
        if (!nullable)
        {
            sb.append(" not null");
        }
        if (increment != 0)
        {
            sb.append(" auto_increment");
            if (increment != 1)
            {
                sb.append("(1, ").append(increment).append(')');
            }
        }
        return sb.toString();
    }

    /**
     * Get an SQL type for a data type.
     *
     * @param valueType The data type.
     * @return The SQL type.
     * @throws IllegalArgumentException If the valueType is unsupported.
     */
    public String getSqlType(Class<?> valueType)
    {
        String sqlType = TYPE_MAP.get(valueType);
        if (sqlType == null)
        {
            if (Geometry.class.isAssignableFrom(valueType))
            {
                sqlType = getSqlType(Geometry.class);
            }
            else if (Serializable.class.isAssignableFrom(valueType))
            {
                sqlType = getSqlType(Serializable.class);
            }
        }
        if (sqlType == null)
        {
            throw new IllegalArgumentException("Unsupported property type: " + valueType);
        }
        return sqlType;
    }

    /**
     * Get an appropriate property value translator.
     *
     * @param <T> The property value type.
     *
     * @param desc The property descriptor.
     * @return The value translator.
     */
    @SuppressWarnings("unchecked")
    public <T> ValueTranslator<? super T> getValueTranslator(PropertyDescriptor<T> desc)
    {
        ValueTranslator<? super T> valueTranslator;
        if (desc instanceof PropertyArrayDescriptor)
        {
            valueTranslator = (ValueTranslator<? super T>)getObjectArrayTranslator((PropertyArrayDescriptor)desc);
        }
        else
        {
            valueTranslator = getValueTranslator(desc.getType());
        }
        return valueTranslator;
    }

    /**
     * Get a list of value translators, one for each property accessor.
     *
     * @param propertyAccessors The property accessors.
     * @return The value translators.
     */
    public ValueTranslator<?>[] getValueTranslators(Collection<? extends PropertyAccessor<?, ?>> propertyAccessors)
    {
        final ValueTranslator<?>[] valueTranslators = new ValueTranslator<?>[propertyAccessors.size()];
        int index = 0;
        for (final PropertyAccessor<?, ?> propertyAccessor : propertyAccessors)
        {
            valueTranslators[index++] = getValueTranslator(propertyAccessor.getPropertyDescriptor());
        }
        return valueTranslators;
    }

    /**
     * Get a list of value translators, one for each property descriptor.
     *
     * @param props The property descriptors.
     * @return The value translators.
     */
    public ValueTranslator<?>[] getValueTranslators(PropertyDescriptor<?>[] props)
    {
        final ValueTranslator<?>[] valueTranslators = new ValueTranslator<?>[props.length];
        for (int i = 0; i < props.length;)
        {
            valueTranslators[i] = getValueTranslator(props[i++]);
        }
        return valueTranslators;
    }

    /**
     * Get if there's a value translator for a property descriptor.
     *
     * @param desc The property descriptor.
     * @return {@code true} if there's a value translator.
     */
    public boolean hasValueTranslator(PropertyDescriptor<?> desc)
    {
        return desc instanceof PropertyArrayDescriptor || TYPES_TO_TRANSLATORS.containsKey(desc.getType())
                || Geometry.class.isAssignableFrom(desc.getType()) || Serializable.class.isAssignableFrom(desc.getType());
    }

    /**
     * Get the base portion of a column name for a property.
     *
     * @param desc The property descriptor.
     * @return The column name.
     */
    protected String getColumnNameBase(PropertyDescriptor<?> desc)
    {
        return new StringBuilder(32).append(getColumnNamePrefix(desc.getType())).append('_').append(desc.getPropertyName())
                .toString();
    }

    /**
     * Get a translator for object arrays.
     *
     * @param propertyDescriptor The property descriptor.
     * @return The translator.
     */
    protected ValueTranslator<Object[]> getObjectArrayTranslator(PropertyArrayDescriptor propertyDescriptor)
    {
        final Class<? extends Object>[] columnTypes = propertyDescriptor.getColumnTypes();
        final int[] activeColumns = propertyDescriptor.getActiveColumns();
        final ValueTranslator<? extends Object>[] translators = new ValueTranslator<?>[activeColumns.length];
        for (int index = 0; index < activeColumns.length; index++)
        {
            final ValueTranslator<?> valueTranslator = getValueTranslator(columnTypes[activeColumns[index]]);
            translators[index] = valueTranslator;
        }
        return new ValueTranslator<Object[]>()
        {
            @Override
            public Class<Object[]> getType()
            {
                return Object[].class;
            }

            @Override
            public int getValue(Class<? extends Object[]> type, long sizeBytes, int column, ResultSet rs,
                    PropertyMatcher<? extends Object[]> filter, Collection<? super Object[]> results)
                throws CacheException
            {
                throw new UnsupportedOperationException("Object[] value translator requires property descriptor.");
            }

            @Override
            public int getValue(PropertyDescriptor<? extends Object[]> unused, int startColumn, ResultSet rs,
                    PropertyMatcher<? extends Object[]> filter, Collection<? super Object[]> results)
                throws CacheException
            {
                int column = startColumn;
                final Collection<Object> rowResults = new ArrayList<>(translators.length);
                for (int index = 0; index < translators.length; ++index)
                {
                    column = getColumnValue(translators[index], columnTypes[activeColumns[index]], -1L, column, rs, filter,
                            rowResults);
                }
                results.add(rowResults.toArray(new Object[translators.length]));
                return column;
            }

            @Override
            public int setValue(PreparedStatement pstmt, int startColumn, Object[] value, boolean forInsert)
                throws CacheException, SQLException
            {
                Utilities.checkNull(value, "value");

                if (value.length != activeColumns.length)
                {
                    throw new IllegalArgumentException(
                            "Length of array provided by accessor does not match number of active columns defined by "
                                    + "property descriptor. Active column count is " + activeColumns.length
                                    + ",  but array length is " + value.length);
                }
                int column = startColumn;
                for (int index = 0; index < translators.length; ++index)
                {
                    column = setColumnValue(translators[index], pstmt, column, value[index], forInsert);
                }

                return column;
            }

            /**
             * Helper method to get around problems with generics.
             */
            @SuppressWarnings({ "rawtypes", "unchecked" })
            private int getColumnValue(ValueTranslator columnTranslator, Class type, long sizeBytes, int column, ResultSet rs,
                    PropertyMatcher<? extends Object[]> filter, Collection results)
                throws CacheException
            {
                return columnTranslator.getValue(type, sizeBytes, column, rs, filter, results);
            }

            /**
             * Helper method to get around problems with generics.
             */
            @SuppressWarnings({ "unchecked" })
            private <T> int setColumnValue(ValueTranslator<T> columnTranslator, PreparedStatement pstmt, int column, Object value,
                    boolean forInsert)
                throws CacheException, SQLException
            {
                return columnTranslator.setValue(pstmt, column, (T)value, forInsert);
            }
        };
    }

    /**
     * Get a property value translator for a Java type.
     *
     * @param <T> The Java type.
     * @param type The Java type.
     * @return The translator.
     */
    @SuppressWarnings("unchecked")
    protected <T> ValueTranslator<? super T> getValueTranslator(Class<T> type)
    {
        final ValueTranslator<? super T> valueTranslator = (ValueTranslator<? super T>)TYPES_TO_TRANSLATORS.get(type);
        if (valueTranslator == null)
        {
            if (Geometry.class.isAssignableFrom(type))
            {
                return (ValueTranslator<? super T>)getValueTranslator(Geometry.class);
            }
            else if (Serializable.class.isAssignableFrom(type))
            {
                return (ValueTranslator<? super T>)getValueTranslator(Serializable.class);
            }
            else
            {
                throw new UnsupportedOperationException("The property type " + type.getName() + " is unsupported.");
            }
        }
        return valueTranslator;
    }
}
