package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconRegular;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Command to clear all loaded features and load new features in region for
 * active data types.
 */
public class LoadFeaturesCommand extends AbstractSelectionCommand
{
    /**
     * Creates a new command.
     */
    public LoadFeaturesCommand()
    {
        super("LOAD_FEATURES", "Load", "Clear all loaded features and load new features in region for active data types",
                SelectionCommandGroup.FEATURES, new GenericFontIcon(AwesomeIconRegular.CIRCLE, Color.WHITE));
    }
}
