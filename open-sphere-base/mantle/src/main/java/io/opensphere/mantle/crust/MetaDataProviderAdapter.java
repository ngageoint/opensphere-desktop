package io.opensphere.mantle.crust;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * Completely generic MetaDataProvider base class; concrete subclasses must
 * override the getValue method to return field values.
 */
public abstract class MetaDataProviderAdapter implements MetaDataProvider
{
    /** The set of supported field names. */
    private Set<String> myFieldNames;

    /**
     * Copy constructor.
     *
     * @param source the object from which data is read.
     */
    protected MetaDataProviderAdapter(MetaDataProviderAdapter source)
    {
        myFieldNames = New.set(source.myFieldNames);
    }

    /** Creates a new adapter. */
    public MetaDataProviderAdapter()
    {
        /* intentionally blank */
    }

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
        myFieldNames.stream().map(k -> getValue(k)).forEach(ret::add);
        return ret;
    }

    @Override
    public boolean hasKey(String key)
    {
        return myFieldNames.contains(key);
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