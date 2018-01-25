package io.opensphere.controlpanels.layers.base;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Layer selection utilities.
 */
public final class LayerSelectionUtilities
{
    /**
     * Filters a collection of data groups and data types to the ones that
     * should be acted upon.
     *
     * @param layers a collection of data groups and data types
     * @return the data groups and data types that should be acted upon
     */
    public static List<Object> filter(Collection<?> layers)
    {
        List<Object> filtered = New.list(layers);

        for (Object layer : layers)
        {
            if (layer instanceof DataGroupInfo)
            {
                DataGroupInfo group = (DataGroupInfo)layer;
                filtered.removeAll(group.getMembers(true));
                filtered.removeAll(getAllChildren(group));
            }
        }

        return filtered;
    }

    /**
     * Gets all the children for the given group.
     *
     * @param group the group
     * @return children the children
     */
    public static Collection<DataGroupInfo> getAllChildren(DataGroupInfo group)
    {
        Collection<DataGroupInfo> children = New.list();
        getAllChildren(group, children);
        return children;
    }

    /**
     * Gets all the children for the given group.
     *
     * @param group the group
     * @param children the children
     */
    private static void getAllChildren(DataGroupInfo group, Collection<DataGroupInfo> children)
    {
        for (DataGroupInfo child : group.getChildren())
        {
            children.add(child);
            getAllChildren(child, children);
        }
    }

    /** Private constructor. */
    private LayerSelectionUtilities()
    {
    }
}
