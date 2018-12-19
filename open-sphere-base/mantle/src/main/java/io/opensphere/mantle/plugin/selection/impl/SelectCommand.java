package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to select features in region
 */
public class SelectCommand extends AbstractSelectionCommand
{
    /**
     * Creates a new command.
     */
    public SelectCommand()
    {
        super("SELECT", "Select", "Select features in region", SelectionCommandGroup.FEATURES,
                new GenericFontIcon(AwesomeIconSolid.CHECK_CIRCLE, Color.WHITE));
    }
}
