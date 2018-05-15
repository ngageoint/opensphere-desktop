package io.opensphere.mantle.data.cache.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import gnu.trove.list.TLongList;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.rangeset.DefaultRangedLongSet;
import io.opensphere.core.util.rangeset.ImmutableRangedLongSet;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DataTypeToIdMap.
 */
public class DataTypeToIdMap
{
    /** The Constant TYPE_PARAMETER_NAME. */
    private static final String TYPE_PARAMETER_NAME = "type";

    /** The my data type to type id set map. */
    private final Map<String, RangedLongSet> myDataTypeKeyToTypeIdSetMap;

    /** The DTI key to dti map. */
    private final Map<String, DataTypeInfo> myDTIKeyToDTIMap;

    /**
     * Instantiates a new data type to id map.
     */
    public DataTypeToIdMap()
    {
        myDataTypeKeyToTypeIdSetMap = new ConcurrentHashMap<>();
        myDTIKeyToDTIMap = new ConcurrentHashMap<>();
    }

    /**
     * Adds the data type.
     *
     * @param dti the dti
     */
    public void addDataType(DataTypeInfo dti)
    {
        Utilities.checkNull(dti, "dti");
        if (!myDataTypeKeyToTypeIdSetMap.containsKey(dti.getTypeKey()))
        {
            myDataTypeKeyToTypeIdSetMap.put(dti.getTypeKey(), new DefaultRangedLongSet());
            myDTIKeyToDTIMap.put(dti.getTypeKey(), dti);
        }
    }

    /**
     * Adds the ids for type to ranged long set.
     *
     * @param dti the dti
     * @param rls the rls
     */
    public void addIdsForTypeToRangedLongSet(DataTypeInfo dti, RangedLongSet rls)
    {
        Utilities.checkNull(rls, "rls");
        Utilities.checkNull(dti, "dti");
        RangedLongSet result = myDataTypeKeyToTypeIdSetMap.get(dti.getTypeKey());
        if (result != null)
        {
            rls.add(result);
        }
    }

    /**
     * Adds the ids to type set.
     *
     * @param type the type
     * @param idList the id list
     */
    public void addIdsToTypeSet(DataTypeInfo type, TLongList idList)
    {
        Utilities.checkNull(type, TYPE_PARAMETER_NAME);
        RangedLongSet idSet = myDataTypeKeyToTypeIdSetMap.get(type.getTypeKey());
        if (idSet == null)
        {
            idSet = new DefaultRangedLongSet();
            myDataTypeKeyToTypeIdSetMap.put(type.getTypeKey(), idSet);
            myDTIKeyToDTIMap.put(type.getTypeKey(), type);
        }
        idSet.addAll(idList.toArray());
    }

    /**
     * Adds the id to type set.
     *
     * @param type the type
     * @param id the id
     */
    public void addIdToTypeSet(DataTypeInfo type, long id)
    {
        Utilities.checkNull(type, TYPE_PARAMETER_NAME);
        RangedLongSet idSet = myDataTypeKeyToTypeIdSetMap.get(type.getTypeKey());
        if (idSet == null)
        {
            idSet = new DefaultRangedLongSet(id);
            myDataTypeKeyToTypeIdSetMap.put(type.getTypeKey(), idSet);
            myDTIKeyToDTIMap.put(type.getTypeKey(), type);
        }
        else
        {
            idSet.add(id);
        }
    }

    /**
     * Gets the element count for type.
     *
     * @param type the type
     * @return the element count for type
     */
    public int getElementCountForType(DataTypeInfo type)
    {
        int count = 0;
        if (type != null)
        {
            RangedLongSet rls = myDataTypeKeyToTypeIdSetMap.get(type.getTypeKey());
            if (rls != null)
            {
                count = rls.size();
            }
        }
        return count;
    }

    /**
     * Gets the ids for type as array.
     *
     * @param type the type
     * @return the ids for type as array
     */
    public long[] getIdsForTypeAsArray(DataTypeInfo type)
    {
        Utilities.checkNull(type, TYPE_PARAMETER_NAME);
        RangedLongSet result = myDataTypeKeyToTypeIdSetMap.get(type.getTypeKey());
        if (result != null)
        {
            return result.getValues();
        }
        else
        {
            return new long[0];
        }
    }

    /**
     * Gets the ids for type as list.
     *
     * @param type the type
     * @return the ids for type as list
     */
    public List<Long> getIdsForTypeAsList(DataTypeInfo type)
    {
        Utilities.checkNull(type, TYPE_PARAMETER_NAME);
        RangedLongSet set = myDataTypeKeyToTypeIdSetMap.get(type.getTypeKey());
        List<Long> result = null;
        if (set == null)
        {
            result = Collections.<Long>emptyList();
        }
        else
        {
            result = CollectionUtilities.listView(set.getValues());
        }
        return result;
    }

    /**
     * Gets the ids for type as ranged long set.
     *
     * @param type the type
     * @return the ids for type as ranged long set
     */
    public RangedLongSet getIdsForTypeAsRangedLongSet(DataTypeInfo type)
    {
        Utilities.checkNull(type, TYPE_PARAMETER_NAME);
        RangedLongSet result = myDataTypeKeyToTypeIdSetMap.get(type.getTypeKey());
        if (result == null)
        {
            result = new DefaultRangedLongSet();
        }
        else
        {
            result = new DefaultRangedLongSet(result);
        }
        return new ImmutableRangedLongSet(result);
    }

    /**
     * Gets the {@link DataTypeInfo} from the map that have at least one entry
     * in the element set.
     *
     * @return the types with elements
     */
    public Set<DataTypeInfo> getTypesWithElements()
    {
        Set<DataTypeInfo> result = new HashSet<>();
        for (Map.Entry<String, RangedLongSet> entry : myDataTypeKeyToTypeIdSetMap.entrySet())
        {
            if (entry.getValue() != null && !entry.getValue().isEmpty())
            {
                result.add(myDTIKeyToDTIMap.get(entry.getKey()));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Removes the data type.
     *
     * @param dti the dti
     */
    public void removeDataType(DataTypeInfo dti)
    {
        Utilities.checkNull(dti, "dti");
        myDataTypeKeyToTypeIdSetMap.remove(dti.getTypeKey());
        myDTIKeyToDTIMap.remove(dti.getTypeKey());
    }

    /**
     * Removes the id from type set.
     *
     * @param type the type
     * @param id the id
     */
    public void removeIdFromTypeSet(DataTypeInfo type, long id)
    {
        Utilities.checkNull(type, TYPE_PARAMETER_NAME);
        RangedLongSet idSet = myDataTypeKeyToTypeIdSetMap.get(type.getTypeKey());
        if (idSet != null)
        {
            idSet.remove(id);
        }
    }

    /**
     * Removes the ids from type set.
     *
     * @param type the type
     * @param idList the id list
     */
    public void removeIdsFromTypeSet(DataTypeInfo type, List<Long> idList)
    {
        Utilities.checkNull(type, TYPE_PARAMETER_NAME);
        RangedLongSet idSet = myDataTypeKeyToTypeIdSetMap.get(type.getTypeKey());
        if (idSet != null)
        {
            idSet.remove(idList);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Total Data Types: ").append(myDataTypeKeyToTypeIdSetMap.size()).append("\n" + "Data Type List:\n" + "  ")
                .append(String.format("%-12s%-40s", "Count", "Type Name")).append(" Type Key\n" + "  ")
                .append(String.format("%-12s%-40s", "----------", "------------")).append(" ------------\n");
        for (Map.Entry<String, RangedLongSet> entry : myDataTypeKeyToTypeIdSetMap.entrySet())
        {
            String dtiKey = entry.getKey();
            DataTypeInfo dti = myDTIKeyToDTIMap.get(dtiKey);
            sb.append("  ").append(String.format("%-12d", Integer.valueOf(entry.getValue().size())))
                    .append(String.format("%-40s", dti.getDisplayName())).append(" [").append(dtiKey).append("]\n");
        }
        return sb.toString();
    }

    /**
     * Type count.
     *
     * @return the int
     */
    public int typeCount()
    {
        return myDTIKeyToDTIMap.size();
    }
}
