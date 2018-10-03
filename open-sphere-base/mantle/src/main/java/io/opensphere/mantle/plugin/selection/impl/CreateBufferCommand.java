package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to create a polygon that buffers an item which can then be used for
 * queries, selections, etc.
 */
public class CreateBufferCommand extends AbstractSelectionCommand
{
    /**
     * Create a new command.
     */
    public CreateBufferCommand()
    {
        super("CREATE_BUFFER_REGION", "Create Buffer Region",
                "Create a polygon that buffers an item which can then be used for queries, selections, etc.",
                SelectionCommandGroup.TOOLS, new GenericFontIcon(AwesomeIconSolid.BULLSEYE, Color.WHITE));
    }
}
