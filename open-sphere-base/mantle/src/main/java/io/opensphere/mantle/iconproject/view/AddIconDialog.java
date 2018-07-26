package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.AddIconPane;

public class AddIconDialog extends JFXDialog
{
    /**
     *  Wraps the Add Icon into a java FX pannel.
     */
    private static final long serialVersionUID = -4136694415228468073L;

    public AddIconDialog(Window owner,PanelModel myPanelModel)
    {
        super(owner, "Add Icon From File",false);
        setMinimumSize(new Dimension(500,300));
        setLocationRelativeTo(owner);
        setFxNode(new AddIconPane(myPanelModel));
    }

}
