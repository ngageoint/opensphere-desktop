package io.opensphere.mantle.plugin.selection.impl;

import java.awt.Color;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 *
 */
public class ZoomToCommand extends AbstractSelectionCommand
{
    /**
     *
     */
    public ZoomToCommand()
    {
        super("ZOOM", "Zoom", "Zoom the map to the feature(s)", SelectionCommandGroup.FEATURES,
                new GenericFontIcon(AwesomeIconSolid.CROP, Color.WHITE));
    }
}
