package io.opensphere.wfs.envoy;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.common.geospatial.model.DataObject;
import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * DataObjectMetaDataProvider - a class wrapper for a MetaDataProvider that uses
 * a {@link DataObject}s member keys and values for its backing store.
 */
public class DataObjectMetaDataProvider implements MetaDataProvider
{
    /** List of column names taken from the DataObject. */
    private final List<String> myKeys;

    /** The column values from the DataObject. */
    private final List<Object> myValues;

    /**
     * Instantiates a new {@link MetaDataProvider} for {@link DataObject}s.
     *
     * @param dataObject the data object
     */
    @SuppressWarnings("unchecked")
    public DataObjectMetaDataProvider(DataObject dataObject)
    {
        myKeys = dataObject.getPropertyKeys();
        myValues = (List<Object>)dataObject.getProperties();
    }

    @Override
    public List<String> getKeys()
    {
        return Collections.unmodifiableList(myKeys);
    }

    @Override
    public Object getValue(String key)
    {
        int index = myKeys.indexOf(key);
        Object result = null;
        if (index >= 0 && index < myValues.size())
        {
            result = myValues.get(index);
        }
        return result;
    }

    @Override
    public List<Object> getValues()
    {
        return Collections.unmodifiableList(myValues);
    }

    @Override
    public boolean hasKey(String key)
    {
        return myKeys.contains(key);
    }

    @Override
    public boolean keysMutable()
    {
        return false;
    }

    @Override
    public void removeKey(String key)
    {
        throw new UnsupportedOperationException("Cannot remove key from a DataObjectMetaDataProvider, remove via MetaDataInfo");
    }

    @Override
    public boolean setValue(String key, Serializable value)
    {
        if (keysMutable())
        {
            int index = myKeys.indexOf(key);
            if (index >= 0)
            {
                myValues.set(index, value);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean valuesMutable()
    {
        return false;
    }
}
