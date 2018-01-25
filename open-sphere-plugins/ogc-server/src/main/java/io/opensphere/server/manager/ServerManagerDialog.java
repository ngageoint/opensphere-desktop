package io.opensphere.server.manager;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.OptionDialog;

/**
 * The Server Manager Dialog.
 */
public class ServerManagerDialog extends OptionDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new server configuration GUI.
     *
     * @param toolbox the Core toolbox
     */
    public ServerManagerDialog(Toolbox toolbox)
    {
        super(toolbox.getUIRegistry().getMainFrameProvider().get());
        setModal(false);
        setComponent(new ServerManagerPanel(toolbox, this));
        build(400, 300);
        setLocationRelativeTo(toolbox.getUIRegistry().getMainFrameProvider().get());
    }
}
