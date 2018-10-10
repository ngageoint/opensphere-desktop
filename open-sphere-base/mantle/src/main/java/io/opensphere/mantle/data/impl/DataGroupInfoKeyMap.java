package io.opensphere.mantle.data.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.ref.WeakReference;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoLookup;

/**
 * The Class DataGroupInfoKeyMap. A map, that maps data group info keys to weak
 * references of the data group info.
 */
public final class DataGroupInfoKeyMap implements DataGroupInfoLookup
{
    /** The key to DataGroupInfo map. */
    private final Map<String, WeakReference<DataGroupInfo>> myDGIKeyToDGIMap = New.map();

    /** The map lock. */
    private final ReentrantLock myDGIKeyToDGIMapLock = new ReentrantLock();

    /**
     * Instantiates a new data group info key map.
     */
    public DataGroupInfoKeyMap()
    {
    }

    /**
     * Clean up the map by pruning out any garbage collected weak references.
     */
    public void cleanUp()
    {
        myDGIKeyToDGIMapLock.lock();
        try
        {
            Set<String> keysToRemove = New.set();
            for (Map.Entry<String, WeakReference<DataGroupInfo>> entry : myDGIKeyToDGIMap.entrySet())
            {
                WeakReference<DataGroupInfo> value = entry.getValue();
                if (value == null || value.get() == null)
                {
                    keysToRemove.add(entry.getKey());
                }
            }
            for (String key : keysToRemove)
            {
                myDGIKeyToDGIMap.remove(key);
            }
        }
        finally
        {
            myDGIKeyToDGIMapLock.unlock();
        }
    }

    @Override
    public DataGroupInfo getGroupForKey(String key)
    {
        DataGroupInfo result = null;
        if (key != null)
        {
            myDGIKeyToDGIMapLock.lock();
            try
            {
                WeakReference<DataGroupInfo> value = myDGIKeyToDGIMap.get(key);
                if (value != null)
                {
                    result = value.get();
                    if (result == null)
                    {
                        myDGIKeyToDGIMap.remove(key);
                    }
                }
            }
            finally
            {
                myDGIKeyToDGIMapLock.unlock();
            }
        }
        return result;
    }

    @Override
    public Map<String, DataGroupInfo> getGroupsForKeys(Collection<String> keyCollection)
    {
        Utilities.checkNull(keyCollection, "keyCollection");
        Map<String, DataGroupInfo> result = null;
        if (!keyCollection.isEmpty())
        {
            result = New.map(keyCollection.size());
            myDGIKeyToDGIMapLock.lock();
            try
            {
                for (String key : keyCollection)
                {
                    WeakReference<DataGroupInfo> value = myDGIKeyToDGIMap.get(key);
                    if (value != null)
                    {
                        DataGroupInfo dgi = value.get();
                        if (dgi == null)
                        {
                            myDGIKeyToDGIMap.remove(key);
                        }
                        else
                        {
                            result.put(key, dgi);
                        }
                    }
                }
            }
            finally
            {
                myDGIKeyToDGIMapLock.unlock();
            }
        }
        return result == null || result.isEmpty() ? Collections.<String, DataGroupInfo>emptyMap() : result;
    }

    /**
     * Re-key an entry.
     *
     * @param oldKey the old key
     * @param newKey the new key
     */
    public void rekey(String oldKey, String newKey)
    {
        myDGIKeyToDGIMapLock.lock();
        try
        {
            WeakReference<DataGroupInfo> ref = myDGIKeyToDGIMap.remove(oldKey);
            myDGIKeyToDGIMap.put(newKey, ref);
        }
        finally
        {
            myDGIKeyToDGIMapLock.unlock();
        }
    }

    /**
     * Removes the key.
     *
     * @param key the key
     */
    public void removeKey(String key)
    {
        myDGIKeyToDGIMapLock.lock();
        try
        {
            myDGIKeyToDGIMap.remove(key);
        }
        finally
        {
            myDGIKeyToDGIMapLock.unlock();
        }
    }

    /**
     * Sets the key to group map entry.
     *
     * @param key the key
     * @param dgi the {@link DataGroupInfo}
     */
    public void setKeyToGroupMapEntry(String key, DataGroupInfo dgi)
    {
        myDGIKeyToDGIMapLock.lock();
        try
        {
            myDGIKeyToDGIMap.put(key, new WeakReference<>(dgi));
        }
        finally
        {
            myDGIKeyToDGIMapLock.unlock();
        }
    }
}
