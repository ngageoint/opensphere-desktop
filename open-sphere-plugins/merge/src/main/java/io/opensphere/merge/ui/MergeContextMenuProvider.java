package io.opensphere.merge.ui;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.merge.controller.MergeController;

/**
 * Adds the merge option when the user right clicks on a layer and at least 2 or
 * more feature layers are selected.
 */
public class MergeContextMenuProvider extends AbstractMergeMergeContextMenuProvider<MultiDataGroupContextKey>
{
    /**
     * Constructs a new merge context menu provider.
     *
     * @param toolbox The system toolbox.
     * @param mergeController The merge controller.
     */
    public MergeContextMenuProvider(Toolbox toolbox, MergeController mergeController)
    {
        super(toolbox, mergeController);
    }
}
