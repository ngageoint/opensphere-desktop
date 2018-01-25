package io.opensphere.merge.controller;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.columns.gui.ColumnMappingOptionsProvider;

/**
 * Launches the Column Associations editor.
 */
public class ColumnAssociationsLauncher
{
    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Launches the column associations editor.
     *
     * @param toolbox The system toolbox.
     */
    public ColumnAssociationsLauncher(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Launches the column associations editor.
     */
    public void launchColumnAssociations()
    {
        go(myToolbox);
    }

    /**
     * Same as launchColumnAssociations (q.v.), but statically accessible.
     *
     * @param tb main Toolbox
     */
    public static void go(Toolbox tb)
    {
        tb.getUIRegistry().getOptionsRegistry().requestShowTopic(ColumnMappingOptionsProvider.TOPIC);
    }
}
