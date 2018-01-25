package io.opensphere.arcgis2.envoy.tile;

import java.util.Set;

import io.opensphere.arcgis2.model.ArcGISDataGroupInfo;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * This class knows how to get an {@link ArcGISDataGroupInfo} for a given
 * {@link DataModelCategory}.
 */
public class ArcGISDataGroupProvider
{
    /**
     * The data group controller.
     */
    private final DataGroupController myDataGroupController;

    /**
     * Constructs a new {@link ArcGISDataGroupProvider}.
     *
     * @param dataGroupController The data group controller.
     */
    public ArcGISDataGroupProvider(DataGroupController dataGroupController)
    {
        myDataGroupController = dataGroupController;
    }

    /**
     * Gets the {@link ArcGISDataGroupInfo} for the given
     * {@link DataModelCategory}.
     *
     * @param category The tile image {@link DataModelCategory}.
     * @return The {@link ArcGISDataGroupInfo} who we are trying to get tile
     *         images for.
     */
    public ArcGISDataGroupInfo getDataGroup(DataModelCategory category)
    {
        String startsWithId = category.getCategory().replaceAll("/MapServer.*", "");
        Set<DataGroupInfo> found = myDataGroupController.findActiveDataGroupInfo((t) -> test(t, startsWithId), true);
        ArcGISDataGroupInfo dataGroup = null;
        if (!found.isEmpty())
        {
            dataGroup = (ArcGISDataGroupInfo)found.iterator().next();
        }

        return dataGroup;
    }

    /**
     * Tests the {@link DataGroupInfo} to see if its the right one.
     *
     * @param t The group to test.
     * @param startsWithId The start of the group id we are looking for.
     * @return True if t is the group we are looking for, false otherwise.
     */
    private boolean test(DataGroupInfo t, String startsWithId)
    {
        boolean isIt = false;

        if (t instanceof ArcGISDataGroupInfo && t.getId().startsWith(startsWithId))
        {
            isIt = true;
        }

        return isIt;
    }
}
