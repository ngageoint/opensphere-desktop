package io.opensphere.merge.algorithm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * Packages the information related to a join operation for a single type.
 * Included are several methods that assist in performing the join.
 */
public class JoinInfo
{
    /** Specific matches for values in case of inexact matching. */
    private Map<Object, DataElement> found;

    /** An index of records by value of the join column. */
    private Map<Object, DataElement> index;

    /** The name of the column being joined for this type. */
    private final String joinKey;

    /** The columns in this type that will be represented in the join. */
    private final List<String> keepKeys = new LinkedList<>();

    /** A type participating in the join. */
    private final DataTypeInfo type;

    /**
     * Empty value.
     *
     * @param value The value.
     * @return True or false.
     */
    private static boolean emptyValue(Object value)
    {
        return value == null || value instanceof String && ((String)value).isEmpty();
    }

    /**
     * Index by.
     *
     * @param data The data.
     * @param joinKey The join key.
     * @return The data.
     */
    private static Map<Object, DataElement> indexBy(List<DataElement> data, String joinKey)
    {
        HashMap<Object, DataElement> index = new HashMap<>();
        for (DataElement elt : data)
        {
            Object val = elt.getMetaData().getValue(joinKey);
            if (!emptyValue(val) && !index.containsKey(val))
            {
                index.put(val, elt);
            }
        }
        return index;
    }

    /**
     * same as indexBy except that all index keys are converted to Strings.
     *
     * @param data The data.
     * @param joinKey The key.
     * @return The map.
     */
    private static Map<Object, DataElement> indexByString(List<DataElement> data, String joinKey)
    {
        HashMap<Object, DataElement> index = new HashMap<>();
        for (DataElement elt : data)
        {
            Object val = stringize(elt.getMetaData().getValue(joinKey));
            if (val != null && !index.containsKey(val))
            {
                index.put(val, elt);
            }
        }
        return index;
    }

    /**
     * Stringize.
     *
     * @param val The value.
     * @return The String.
     */
    private static String stringize(Object val)
    {
        if (val == null)
        {
            return null;
        }
        String str = val.toString();
        if (str != null && !str.isEmpty())
        {
            return str;
        }
        return null;
    }

    /**
     * Substring match.
     *
     * @param s1 String one.
     * @param s2 String two.
     * @return True if substring match, false otherwise.
     */
    private static boolean substringMatch(String s1, String s2)
    {
        if (s1 == null || s2 == null)
        {
            return false;
        }
        return s1.contains(s2) || s2.contains(s1);
    }

    /**
     * Bla.
     *
     * @param t bla
     * @param k bla
     */
    public JoinInfo(DataTypeInfo t, String k)
    {
        type = t;
        joinKey = k;
    }

    /**
     * Gets the join key.
     *
     * @return the joinKey
     */
    public String getJoinKey()
    {
        return joinKey;
    }

    /**
     * Gets the keep key.
     *
     * @return the keepKeys
     */
    public List<String> getKeepKeys()
    {
        return keepKeys;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public DataTypeInfo getType()
    {
        return type;
    }

    /**
     * Indexes data.
     *
     * @param data The data.
     * @param useExact True if use exact, false otherwise.
     */
    public void indexData(List<DataElement> data, boolean useExact)
    {
        if (useExact)
        {
            index = indexBy(data, joinKey);
        }
        else
        {
            index = indexByString(data, joinKey);
        }
    }

    /**
     * Merge in match.
     *
     * @param valMap The value map.
     * @param val The value.
     */
    public void mergeInMatch(Map<String, Serializable> valMap, Object val)
    {
        mergeRecordWithValueMap(valMap, index.get(val));
    }

    /**
     * Merge in sub.
     *
     * @param valMap The value map.
     * @param val The value.
     */
    public void mergeInSub(Map<String, Serializable> valMap, Object val)
    {
        if (found == null)
        {
            found = new HashMap<>();
        }
        String valStr = stringize(val);
        if (valStr == null)
        {
            return;
        }
        // see if a match has already been found, use it
        DataElement rec = found.get(valStr);
        if (rec == null)
        {
            // not found => search the index for a matching key
            for (Map.Entry<Object, DataElement> ent : index.entrySet())
            {
                String keyStr = ent.getKey().toString();
                if (substringMatch(keyStr, valStr))
                {
                    rec = ent.getValue();
                    found.put(keyStr, rec);
                    break;
                }
            }
        }
        if (rec != null)
        {
            mergeRecordWithValueMap(valMap, rec);
        }
    }

    /**
     * Merges the values from the supplied value map into the supplied
     * {@link DataElement}.
     *
     * @param valueMap the map from which to extract values.
     * @param record the record into which values will be placed.
     */
    public void mergeRecordWithValueMap(Map<String, Serializable> valueMap, DataElement record)
    {
        if (record == null)
        {
            return;
        }
        MetaDataProvider meta = record.getMetaData();
        for (String keepKey : keepKeys)
        {
            valueMap.put(keepKey, (Serializable)meta.getValue(keepKey));
        }
    }
}
