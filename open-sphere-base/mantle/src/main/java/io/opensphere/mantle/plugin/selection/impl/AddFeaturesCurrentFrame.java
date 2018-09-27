package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command for adding features in region for active data types current time
 * frame only
 */
public class AddFeaturesCurrentFrame extends AbstractSelectionCommand
{
    /**
     * Creates a new command.
     */
    public AddFeaturesCurrentFrame()
    {
        super("ADD_FEATURES_CURRENT_FRAME", "Add in Current Frame", "Add features in region for active data types current time frame only",
                SelectionCommandGroup.QUERY, new GenericFontIcon(AwesomeIconSolid.PLUS_CIRCLE, Color.WHITE, 12));
    }
}
