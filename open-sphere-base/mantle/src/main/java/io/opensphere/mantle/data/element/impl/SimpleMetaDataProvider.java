package io.opensphere.mantle.data.element.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * A very simple implementation of a MetaDataProvider.
 */
public class SimpleMetaDataProvider implements MetaDataProvider, Serializable
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The key to value map. */
    private final Map<String, Serializable> myKeyToValueMap;

    /** Default constructor */
    public SimpleMetaDataProvider()
    {
        myKeyToValueMap = Collections.synchronizedMap(new LinkedHashMap<String, Serializable>());
    }

    /**
     * Copy constructor.
     *
     * @param source the source object from which to get data.
     */
    protected SimpleMetaDataProvider(SimpleMetaDataProvider source)
    {
        myKeyToValueMap = source.myKeyToValueMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    /**
     * CTOR with initial map.
     *
     * @param initialMap - the initial map.
     */
    public SimpleMetaDataProvider(Map<String, Serializable> initialMap)
    {
        myKeyToValueMap = Collections.synchronizedMap(new LinkedHashMap<>(initialMap));
    }

    /**
     * Constructor from another meta data provider.
     *
     * @param otherProvider the provider from which to copy.
     */
    public SimpleMetaDataProvider(MetaDataProvider otherProvider)
    {
        Map<String, Serializable> map = new LinkedHashMap<>();
        for (String key : otherProvider.getKeys())
        {
            Object value = otherProvider.getValue(key);
            if (value instanceof Serializable)
            {
                map.put(key, (Serializable)value);
            }
        }
        myKeyToValueMap = Collections.synchronizedMap(map);
    }

    @Override
    public List<String> getKeys()
    {
        return new ArrayList<>(myKeyToValueMap.keySet());
    }

    @Override
    public Object getValue(String key)
    {
        return myKeyToValueMap.get(key);
    }

    @Override
    public List<Object> getValues()
    {
        return Collections.unmodifiableList(new ArrayList<Object>(myKeyToValueMap.values()));
    }

    @Override
    public boolean hasKey(String key)
    {
        return myKeyToValueMap.containsKey(key);
    }

    @Override
    public boolean keysMutable()
    {
        return true;
    }

    @Override
    public void removeKey(String key)
    {
        myKeyToValueMap.remove(key);
    }

    @Override
    public boolean setValue(String key, Serializable value)
    {
        myKeyToValueMap.put(key, value);
        return true;
    }

    @Override
    public boolean valuesMutable()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.MetaDataProvider#createCopy()
     */
    @Override
    public MetaDataProvider createCopy()
    {
        return new SimpleMetaDataProvider(this);
    }
}
