package io.opensphere.mantle.iconproject.view;

import java.awt.Window;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.gui.IconBuilderDialog;
import io.opensphere.mantle.util.MantleToolboxUtils;

public class IconProjBuilderDialog
{
    public IconProjBuilderDialog(Window owner, Toolbox tb)
    {
        IconRegistry iconRegistry = MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry();
        IconBuilderDialog dialog = new IconBuilderDialog(owner, iconRegistry);
        dialog.setVisible(true);
    }
}
