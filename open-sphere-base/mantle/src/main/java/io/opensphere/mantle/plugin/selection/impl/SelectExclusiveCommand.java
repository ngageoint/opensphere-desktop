package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to select features in region, deselect all other features.
 */
public class SelectExclusiveCommand extends AbstractSelectionCommand
{
    /**
     * Creates a new command.
     */
    public SelectExclusiveCommand()
    {
        super("SELECT_EXCLUSIVE", "Select Exclusive", "Select features in region, deselect all other features",
                SelectionCommandGroup.FEATURES, new GenericFontIcon(AwesomeIconSolid.CHECK_CIRCLE, Color.WHITE));
    }

}
