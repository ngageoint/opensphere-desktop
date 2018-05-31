package io.opensphere.mantle.crust;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * Completely generic MetaDataProvider base class; concrete subclasses must
 * override the getValue method to return field values.
 */
public abstract class MetaDataProviderAdapter implements MetaDataProvider
{
    /** The set of supported field names. */
    protected Set<String> myFieldNames;

    /**
     * Sets the value of the {@link #myFieldNames} field.
     *
     * @param fieldNames the value to store in the {@link #myFieldNames} field.
     */
    public void setFieldNames(Set<String> fieldNames)
    {
        myFieldNames = fieldNames;
    }

    @Override
    public List<String> getKeys()
    {
        return new LinkedList<>(myFieldNames);
    }

    @Override
    public List<Object> getValues()
    {
        List<Object> ret = new LinkedList<>();
        for (String k : myFieldNames)
        {
            ret.add(getValue(k));
        }
        return ret;
    }

    @Override
    public boolean hasKey(String key)
    {
        return myFieldNames.contains(key);
    }

    @Override
    public Stream<String> matchKey(Pattern key)
    {
        return myFieldNames.stream().filter(k -> key.matcher(k).matches());
    }

    @Override
    public boolean keysMutable()
    {
        return false;
    }

    @Override
    public void removeKey(String key)
    {
    }

    @Override
    public boolean setValue(String key, Serializable value)
    {
        return false;
    }

    @Override
    public boolean valuesMutable()
    {
        return false;
    }
}