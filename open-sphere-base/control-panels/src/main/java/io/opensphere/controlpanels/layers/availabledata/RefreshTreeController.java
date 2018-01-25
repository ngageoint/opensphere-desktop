package io.opensphere.controlpanels.layers.availabledata;

import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.RefreshableDataGroupInfo;

/**
 * Refreshes the tree structure under a specified {@link DataGroupInfo}.
 */
public class RefreshTreeController
{
    /**
     * Refreshes the tree structure under the specified dataGroup.
     *
     * @param dataGroup The data group to refresh.
     */
    public void refresh(final DataGroupInfo dataGroup)
    {
        if (dataGroup instanceof RefreshableDataGroupInfo)
        {
            ThreadUtilities.runBackground(new Runnable()
            {
                @Override
                public void run()
                {
                    ((RefreshableDataGroupInfo)dataGroup).refresh();
                }
            });
        }
    }
}
