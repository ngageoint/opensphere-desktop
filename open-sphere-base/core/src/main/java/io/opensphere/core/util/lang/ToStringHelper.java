package io.opensphere.core.util.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

/** Helper for implementing {@link Object#toString()}. */
public class ToStringHelper
{
    /** Size of the indentation. */
    private static final int INDENT_SIZE = 2;

    /** The class for which the string is being generated. */
    private final Class<?> myClass;

    /** The expected size of the generated string. */
    private final int mySize;

    /** The map of field name to value. */
    private final Map<String, Object> myMap = new LinkedHashMap<>();

    /**
     * Constructor.
     *
     * @param object The object for which the string is being generated
     */
    public ToStringHelper(Object object)
    {
        this(object.getClass(), 32);
    }

    /**
     * Constructor.
     *
     * @param object The object for which the string is being generated
     * @param size The expected size of the generated string
     */
    public ToStringHelper(Object object, int size)
    {
        this(object.getClass(), size);
    }

    /**
     * Constructor.
     *
     * @param clazz The class for which the string is being generated
     */
    public ToStringHelper(Class<?> clazz)
    {
        this(clazz, 32);
    }

    /**
     * Constructor.
     *
     * @param clazz The class for which the string is being generated
     * @param size The expected size of the generated string
     */
    public ToStringHelper(Class<?> clazz, int size)
    {
        myClass = clazz;
        mySize = size;
    }

    /**
     * Adds a field/value combination to the helper.
     *
     * @param field The field
     * @param value The value
     * @return this
     */
    public ToStringHelper add(String field, Object value)
    {
        myMap.put(field, value);
        return this;
    }

    /**
     * Adds a field/value combination to the helper if the value is not null.
     *
     * @param field The field
     * @param value The value
     * @return this
     */
    public ToStringHelper addIfNotNull(String field, Object value)
    {
        if (value != null)
        {
            myMap.put(field, value);
        }
        return this;
    }

    /**
     * Adds a field/value combination to the helper.
     *
     * @param field The field
     * @param value The value
     * @return this
     */
    public ToStringHelper add(String field, boolean value)
    {
        return add(field, Boolean.valueOf(value));
    }

    /**
     * Adds a field/value combination to the helper.
     *
     * @param field The field
     * @param value The value
     * @return this
     */
    public ToStringHelper add(String field, int value)
    {
        return add(field, Integer.valueOf(value));
    }

    /**
     * Adds a field/value combination to the helper.
     *
     * @param field The field
     * @param value The value
     * @return this
     */
    public ToStringHelper add(String field, long value)
    {
        return add(field, Long.valueOf(value));
    }

    /**
     * Adds a field/value combination to the helper.
     *
     * @param field The field
     * @param value The value
     * @return this
     */
    public ToStringHelper add(String field, float value)
    {
        return add(field, Float.valueOf(value));
    }

    /**
     * Adds a field/value combination to the helper.
     *
     * @param field The field
     * @param value The value
     * @return this
     */
    public ToStringHelper add(String field, double value)
    {
        return add(field, Double.valueOf(value));
    }

    /**
     * Adds a line to the helper.
     *
     * @param line The line
     * @return this
     */
    public ToStringHelper add(String line)
    {
        myMap.put(line, null);
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(mySize);
        if (myClass != null)
        {
            builder.append(myClass.getSimpleName()).append(' ');
        }
        builder.append('[').append(toStringGeneric(k -> k, v -> v, "=", ", ")).append(']');
        return builder.toString();
    }

    /**
     * Returns a multi-line string representation of the helper.
     *
     * @return the multi-line string
     */
    public String toStringMultiLine()
    {
        return toStringMultiLine(0);
    }

    /**
     * Returns a multi-line string representation of the helper.
     *
     * @param indentLevel the indent level (0-based)
     * @return the multi-line string
     */
    public String toStringMultiLine(int indentLevel)
    {
        StringBuilder builder = new StringBuilder(mySize);
        if (myClass != null)
        {
            builder.append(myClass.getSimpleName()).append(':').append(System.lineSeparator());
        }
        int maxKeyLen = myMap.entrySet().stream().filter(e -> e.getValue() != null).mapToInt(e -> e.getKey().length()).max()
                .orElse(0);
        String propertyPad = StringUtilities.repeat(" ", (indentLevel + 1) * INDENT_SIZE);
        builder.append(
                toStringGeneric(k -> propertyPad + StringUtilities.pad(k, maxKeyLen), v -> v, " = ", System.lineSeparator()));
        return builder.toString();
    }

    /**
     * Formats the helper for a preference dump.
     *
     * @return the string
     */
    public String toStringPreferenceDump()
    {
        int maxKeyLen = myMap.entrySet().stream().filter(e -> e.getValue() != null).mapToInt(e -> e.getKey().length()).max()
                .orElse(0);
        String lineDelimiter = System.lineSeparator() + "\t";
        return lineDelimiter + toStringGeneric(k -> StringUtilities.pad(k, maxKeyLen), v -> v, " ", lineDelimiter);
    }

    /**
     * A generic way of creating a string from the current state of the helper.
     *
     * @param keyMapper maps the key to a new value
     * @param valueMapper maps the value to a new value
     * @param valueDelimiter the delimiter between key and value
     * @param lineDelimiter the delimiter between lines
     * @return the string
     */
    private String toStringGeneric(Function<String, String> keyMapper, Function<String, String> valueMapper,
            String valueDelimiter, String lineDelimiter)
    {
        Collection<String> lines = new ArrayList<>(myMap.size());
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : myMap.entrySet())
        {
            builder.setLength(0);
            builder.append(keyMapper.apply(entry.getKey()));
            if (entry.getValue() != null)
            {
                builder.append(valueDelimiter).append(valueMapper.apply(entry.getValue().toString()));
            }
            lines.add(builder.toString());
        }

        StringJoiner joiner = new StringJoiner(lineDelimiter);
        for (String line : lines)
        {
            joiner.add(line);
        }
        return joiner.toString();
    }
}
