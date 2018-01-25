package io.opensphere.controlpanels.layers.availabledata.detail;

import io.opensphere.controlpanels.DetailPane;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * A blank pane in which nothing is rendered.
 */
public class BlankPane extends DetailPane
{
    /**
     * Creates a new blank pane.
     *
     * @param pToolbox The toolbox through which system interactions occur.
     */
    public BlankPane(Toolbox pToolbox)
    {
        super(pToolbox);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPane#populate(io.opensphere.mantle.data.DataGroupInfo)
     */
    @Override
    public void populate(DataGroupInfo pDataGroupInfo)
    {
        /* intentionally blank */
    }
}
