package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to deselect all features in region.
 */
public class DeselectCommand extends AbstractSelectionCommand
{
    /**
     * Create a new command.
     */
    public DeselectCommand()
    {
        super("DESELECT", "Deselect All", "Deselect all features in region", SelectionCommandGroup.FEATURES,
                new GenericFontIcon(AwesomeIconSolid.TIMES_CIRCLE, Color.WHITE, 12));
    }
}
