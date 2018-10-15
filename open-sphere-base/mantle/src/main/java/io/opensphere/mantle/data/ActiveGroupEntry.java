package io.opensphere.mantle.data;

import java.util.Comparator;

/**
 * The Interface ActiveLayerEntry.
 */
public interface ActiveGroupEntry
{
    /** The Comparator by name. */
    Comparator<ActiveGroupEntry> ComparatorByName = (o1, o2) -> o1.getName().compareTo(o2.getName());

    /**
     * Gets the id.
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();
}
