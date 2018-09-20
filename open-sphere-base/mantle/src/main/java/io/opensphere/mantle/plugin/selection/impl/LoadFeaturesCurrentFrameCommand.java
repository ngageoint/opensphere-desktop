package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconRegular;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to clear all loaded features and load new features in region for
 * active data types current time frame only.
 */
public class LoadFeaturesCurrentFrameCommand extends AbstractSelectionCommand
{
    /**
     * Creates a new command.
     */
    public LoadFeaturesCurrentFrameCommand()
    {
        super("LOAD_FEATURES_CURRENT_FRAME", "Load in Current Frame",
                "Clear all loaded features and load new features in region for active data types current time frame only",
                SelectionCommandGroup.FEATURES, new GenericFontIcon(AwesomeIconRegular.CIRCLE, Color.WHITE, 12));
    }
}
