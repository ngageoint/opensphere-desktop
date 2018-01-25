package io.opensphere.mantle.data;

import java.util.Comparator;

/**
 * The Interface ActiveLayerEntry.
 */
public interface ActiveGroupEntry
{
    /** The Comparator by name. */
    Comparator<ActiveGroupEntry> ComparatorByName = new Comparator<ActiveGroupEntry>()
    {
        @Override
        public int compare(ActiveGroupEntry o1, ActiveGroupEntry o2)
        {
            return o1.getName().compareTo(o2.getName());
        }
    };

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
