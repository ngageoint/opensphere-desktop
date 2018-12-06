package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to create a polygon that buffers the selected line segment, which can
 * be used for queries, selections, etc.
 */
public class CreateBufferSelectedCommand extends AbstractSelectionCommand
{
    /**
     * Creates a new command.
     */
    public CreateBufferSelectedCommand()
    {
        super("CREATE_BUFFER_REGION_FOR_SELECTED_SEGMENT", "Create Buffer Region For Selected",
                "Create a polygon that buffers the selected item, which can be used for queries, selections, etc.",
                SelectionCommandGroup.TOOLS, new GenericFontIcon(AwesomeIconSolid.BULLSEYE, Color.WHITE));
    }
}
