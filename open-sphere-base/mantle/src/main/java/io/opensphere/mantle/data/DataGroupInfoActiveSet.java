package io.opensphere.mantle.data;

import java.util.List;

/**
 * The Interface DataGroupInfoActiveSet.
 */
public interface DataGroupInfoActiveSet
{
    /**
     * Gets the group ids.
     *
     * @return the group ids
     */
    List<? extends ActiveGroupEntry> getGroupEntries();

    /**
     * Gets the group ids.
     *
     * @return the group ids
     */
    List<String> getGroupIds();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    void setName(String name);
}
