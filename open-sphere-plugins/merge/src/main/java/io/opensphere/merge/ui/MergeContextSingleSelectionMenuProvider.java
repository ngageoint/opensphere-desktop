package io.opensphere.merge.ui;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;

/**
 * Adds the merge option when the user right clicks on a layer and at only a
 * single feature layer is selected.
 */
public class MergeContextSingleSelectionMenuProvider extends AbstractMergeMergeContextMenuProvider<DataGroupContextKey>
{
    /**
     * Constructs a new merge context menu provider.
     *
     * @param toolbox The system toolbox.
     */
    public MergeContextSingleSelectionMenuProvider(Toolbox toolbox)
    {
        super(toolbox);
    }
}
