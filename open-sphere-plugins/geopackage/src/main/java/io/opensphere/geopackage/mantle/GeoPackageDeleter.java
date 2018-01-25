package io.opensphere.geopackage.mantle;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DeletableDataGroupInfoAssistant;

/**
 * Deletes the data from the registry when the user has requested a delete.
 */
public class GeoPackageDeleter extends DeletableDataGroupInfoAssistant
{
    /**
     * The listener wanting notification when the group has been deleted.
     */
    private final PackageDeleteListener myDeleteListener;

    /**
     * Contains the geo package data.
     */
    private final DataRegistry myRegistry;

    /**
     * Constructs a new deleter.
     *
     * @param mantleToolbox The mantle toolbox.
     * @param dataRegistry Contains the geo package data to delete.
     * @param deleteListener The listener wanting notification when the group
     *            has been deleted.
     */
    public GeoPackageDeleter(MantleToolbox mantleToolbox, DataRegistry dataRegistry, PackageDeleteListener deleteListener)
    {
        super(mantleToolbox, null, null);
        myRegistry = dataRegistry;
        myDeleteListener = deleteListener;
    }

    @Override
    public void deleteGroup(DataGroupInfo dgi, Object source)
    {
        myRegistry.removeModels(new DataModelCategory(dgi.getId(), null, null), false);
        if (getDeleteListener() != null)
        {
            getDeleteListener().packageDeleted();
        }
        super.deleteGroup(dgi, source);
    }

    /**
     * Gets the delete listener.
     *
     * @return The delete listener.
     */
    protected PackageDeleteListener getDeleteListener()
    {
        return myDeleteListener;
    }
}
