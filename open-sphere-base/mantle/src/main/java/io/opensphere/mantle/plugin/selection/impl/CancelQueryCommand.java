package io.opensphere.mantle.plugin.selection.impl;

import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to cancel query from retrieving any more feature data.
 */
public class CancelQueryCommand extends AbstractSelectionCommand
{
    /**
     * Creates a new command.
     */
    public CancelQueryCommand()
    {
        super("CANCEL_QUERY", "Cancel Query", "Cancel query from retrieving any more feature data", SelectionCommandGroup.QUERY);
    }
}
