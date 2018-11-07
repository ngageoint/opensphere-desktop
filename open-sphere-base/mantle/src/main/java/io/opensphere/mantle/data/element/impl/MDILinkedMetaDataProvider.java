package io.opensphere.mantle.data.element.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * A {@link MetaDataProvider} where the key set is provided by a
 * {@link MetaDataInfo} and the values are stored in a list.
 */
public class MDILinkedMetaDataProvider extends AbstractMDILinkedMetaDataProvider
{
    /** The value list. */
    private final List<Object> myValues;

    /** The values mutable. */
    private boolean myValuesMutable = true;

    /**
     * Creates the backed meta data provider.
     *
     * @param mdi the mdi
     * @param values the values
     * @return the meta data provider
     */
    public static MetaDataProvider createImmutableBackedMetaDataProvider(MetaDataInfo mdi, List<Object> values)
    {
        return new MDILinkedMetaDataProvider(mdi, values, false);
    }

    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    protected MDILinkedMetaDataProvider(MDILinkedMetaDataProvider source)
    {
        super(source);
        myValues = New.list(source.myValues);
        myValuesMutable = source.myValuesMutable;
    }

    /**
     * Primary constructor.
     *
     * @param mdi the MetaDataInfo to be linked to.
     */
    public MDILinkedMetaDataProvider(MetaDataInfo mdi)
    {
        super(mdi);
        int numKeys = mdi.getKeyCount();
        myValues = new ArrayList<>(mdi.getKeyCount());
        for (int i = 0; i < numKeys; i++)
        {
            myValues.add(null);
        }
    }

    /**
     * Constructor that takes the meta data info and a collection of values. The
     * iteration order of the values must be the same as the iteration order of
     * the keys in the meta data info. It's okay for the number of values to be
     * less than the number of keys.
     *
     * @param mdi the MetaDataInfo to be linked to.
     * @param values The meta data.
     */
    public MDILinkedMetaDataProvider(MetaDataInfo mdi, Collection<? extends Object> values)
    {
        super(mdi);
        myValues = New.list(values);
    }

    /**
     * Instantiates a MDI linked MetaDataProvider backed by the provided lists
     * (not copies).
     *
     * @param mdi the mdi
     * @param values the values
     * @param valuesMutable the values mutable
     */
    private MDILinkedMetaDataProvider(MetaDataInfo mdi, List<Object> values, boolean valuesMutable)
    {
        super(mdi);
        myValuesMutable = valuesMutable;
        myValues = values;
    }

    @Override
    public Object getValue(String key)
    {
        int index = getMetaDataInfo().getKeyIndex(key);
        Object result = null;
        if (myValues != null && index >= 0 && index < myValues.size())
        {
            result = myValues.get(index);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> getValues()
    {
        if (myValues == null)
        {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(myValues);
    }

    @Override
    public boolean setValue(String key, Serializable value)
    {
        if (myValuesMutable)
        {
            int index = getMetaDataInfo().getKeyIndex(key);
            if (index >= 0)
            {
                while (myValues.size() <= index)
                {
                    myValues.add(null);
                }
                myValues.set(index, value);
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(this.getClass().getSimpleName()).append(" : Mutable[").append(myValuesMutable).append("]\n" + "{\n");
        for (String key : getKeys())
        {
            Class<?> keyClass = getMetaDataInfo().getKeyClassType(key);
            String keyClassStr = keyClass == null ? "?" : keyClass.getSimpleName();
            Object value = getValue(key);
            String valueClass = value == null ? "?" : value.getClass().getSimpleName();
            sb.append("  ").append(key).append('[').append(keyClassStr).append("]=").append(value).append('[').append(valueClass)
                    .append("]\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public boolean valuesMutable()
    {
        return myValuesMutable;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.MetaDataProvider#createCopy()
     */
    @Override
    public MetaDataProvider createCopy()
    {
        return new MDILinkedMetaDataProvider(this);
    }
}
