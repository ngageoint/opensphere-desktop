package io.opensphere.analysis.export.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * A {@link MetaDataProvider} that provides metadata for an existing data
 * element but there are more in memory columns decorating that element.
 */
public class ExtraColumnsMetaDataProvider implements MetaDataProvider
{
    /**
     * The extra values related to the data element.
     */
    private final Map<String, Object> myExtraColumns;

    /**
     * The {@link MetaDataProvider} containing the existing data element
     * metadata.
     */
    private final MetaDataProvider myOriginal;

    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    protected ExtraColumnsMetaDataProvider(ExtraColumnsMetaDataProvider source)
    {
        myExtraColumns = source.myExtraColumns.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        myOriginal = source.myOriginal.createCopy(null);
    }

    /**
     * Constructs a new metadata provider.
     *
     * @param original The {@link MetaDataProvider} containing the existing data
     *            element metadata.
     * @param extraColumns The extra values related to the data element.
     */
    public ExtraColumnsMetaDataProvider(MetaDataProvider original, Map<String, Object> extraColumns)
    {
        myOriginal = original;
        myExtraColumns = Collections.synchronizedMap(New.map(extraColumns));
    }

    @Override
    public List<String> getKeys()
    {
        List<String> keys = New.list(myOriginal.getKeys());
        Set<String> originalKeys = New.set(keys);

        for (String key : myExtraColumns.keySet())
        {
            if (!originalKeys.contains(key))
            {
                keys.add(key);
            }
        }

        return keys;
    }

    @Override
    public Object getValue(String key)
    {
        Object value = null;
        if (myExtraColumns.containsKey(key))
        {
            value = myExtraColumns.get(key);
        }
        else
        {
            value = myOriginal.getValue(key);
        }

        return value;
    }

    @Override
    public List<Object> getValues()
    {
        List<String> keys = getKeys();
        List<Object> values = New.list();

        for (String key : keys)
        {
            values.add(getValue(key));
        }

        return New.unmodifiableList(values);
    }

    @Override
    public boolean hasKey(String key)
    {
        return myExtraColumns.containsKey(key) || myOriginal.hasKey(key);
    }

    @Override
    public boolean keysMutable()
    {
        return myOriginal.keysMutable();
    }

    @Override
    public void removeKey(String key)
    {
        if (myExtraColumns.containsKey(key))
        {
            myExtraColumns.remove(key);
        }

        if (myOriginal.hasKey(key))
        {
            myOriginal.removeKey(key);
        }
    }

    @Override
    public boolean setValue(String key, Serializable value)
    {
        boolean valueSet;
        if (myExtraColumns.containsKey(key))
        {
            valueSet = myExtraColumns.put(key, value) == value;
        }
        else
        {
            valueSet = myOriginal.setValue(key, value);
        }

        return valueSet;
    }

    @Override
    public boolean valuesMutable()
    {
        return myOriginal.valuesMutable();
    }

    /**
     * Gets the extra columns.
     *
     * @return the extra columns
     */
    public Map<String, Object> getExtraColumns()
    {
        return myExtraColumns;
    }

    /**
     * The original meta data provider.
     *
     * @return the meta data provider
     */
    public MetaDataProvider getOriginal()
    {
        return myOriginal;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.MetaDataProvider#createCopy(DataTypeInfo)
     */
    @Override
    public MetaDataProvider createCopy(DataTypeInfo newDatatype)
    {
        return new ExtraColumnsMetaDataProvider(this);
    }
}
