package io.opensphere.mantle.data;

import java.util.Collection;
import java.util.Map;

/**
 * The Interface DataGroupInfoLookup.
 */
public interface DataGroupInfoLookup
{
    /**
     * Gets the DataGroupInfo for key.
     *
     * @param dgiKey the data group info key to look up.
     * @return the group for key or null if not found.
     */
    DataGroupInfo getGroupForKey(String dgiKey);

    /**
     * Gets the groups for the specified keys. Returns a map of key to value.
     * Map will only contain key/value pairs that were found in the underlying
     * map.
     *
     * @param keyCollection the key collection to lookup.
     * @return the {@link Map} of keys to groups that were found.
     */
    Map<String, DataGroupInfo> getGroupsForKeys(Collection<String> keyCollection);
}
