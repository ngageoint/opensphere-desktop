package io.opensphere.mantle.icon;

import java.util.List;

/**
 * Listener interface to the icon registry.
 *
 */
public interface IconRegistryListener
{
    /**
     * Icon assigned.
     *
     * Called when an icon has been assigned/reassigned to one or more data
     * elements.
     *
     * @param iconId the icon id of the icon assigned.
     * @param deIds the data element ids that have been assigned the icon.
     * @param source the source of the assignment.
     */
    void iconAssigned(long iconId, List<Long> deIds, Object source);

    /**
     * Icons added.
     *
     * @param added the added
     * @param source the source
     */
    void iconsAdded(List<IconRecord> added, Object source);

    /**
     * Icons removed.
     *
     * @param removed the removed
     * @param source the source
     */
    void iconsRemoved(List<IconRecord> removed, Object source);

    /**
     * Icons unassigned.
     *
     * Called when an icon or icons have been unassigned to a set of data
     * element ids.
     *
     * @param deIds the data element ids that have had their icon assignment
     *            removed.
     * @param source the source of the de-assignment.
     */
    void iconsUnassigned(List<Long> deIds, Object source);
}
