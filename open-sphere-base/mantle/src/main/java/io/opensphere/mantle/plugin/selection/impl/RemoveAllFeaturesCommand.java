package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to remove all features in a region.
 */
public class RemoveAllFeaturesCommand extends AbstractSelectionCommand
{
    /**
     * Creates a new command.
     */
    public RemoveAllFeaturesCommand()
    {
        super("REMOVE_ALL", "Remove Features in Region", "Remove all features in region", SelectionCommandGroup.FEATURES,
                new GenericFontIcon(AwesomeIconSolid.TIMES, Color.WHITE, 12));
    }
}
