package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * The command used to add features.
 */
public class AddFeaturesCommand extends AbstractSelectionCommand
{
    /**
     * Creates a new add features command.
     */
    public AddFeaturesCommand()
    {
        super("ADD_FEATURES", "Add", "Add features in region for active data types", SelectionCommandGroup.QUERY,
                new GenericFontIcon(AwesomeIconSolid.PLUS_CIRCLE, Color.WHITE, 12));
    }
}
