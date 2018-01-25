package io.opensphere.osh.model;

import java.util.List;
import java.util.Map;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ToStringHelper;

/** Binary encoding. */
public class BinaryEncoding implements Encoding
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Map of field to data type. */
    private final Map<String, String> myFieldToTypeMap = New.insertionOrderMap();

    /**
     * Adds a field/type.
     *
     * @param field the field
     * @param type the type
     */
    public void addField(String field, String type)
    {
        myFieldToTypeMap.put(field, type);
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public List<String> getFields()
    {
        return New.list(myFieldToTypeMap.keySet());
    }

    /**
     * Gets the data types.
     *
     * @return the data types
     */
    public List<String> getDataTypes()
    {
        return New.list(myFieldToTypeMap.values());
    }

    /**
     * Gets the type for the given field.
     *
     * @param field the field
     * @return the type
     */
    public String getType(String field)
    {
        return myFieldToTypeMap.get(field);
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        for (Map.Entry<String, String> entry : myFieldToTypeMap.entrySet())
        {
            helper.add(entry.getKey(), entry.getValue());
        }
        return helper.toString();
    }
}
