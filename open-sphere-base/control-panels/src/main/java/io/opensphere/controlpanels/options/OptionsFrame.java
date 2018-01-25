package io.opensphere.controlpanels.options;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.util.swing.FramePreferencesMonitor;

/**
 * The Class OptionsFrame.
 */
class OptionsFrame extends AbstractInternalFrame
{
    /** The title of the window. */
    public static final String TITLE = "Settings";

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The manager for changes to the frame position and size preferences. */
    @SuppressWarnings("unused")
    private final transient FramePreferencesMonitor myFramePrefsMonitor;

    /** The options panel. */
    private final OptionsPanel myOptionsPanel;

    /**
     * Instantiates a new layer manager.
     *
     * @param toolbox The toolbox.
     */
    public OptionsFrame(Toolbox toolbox)
    {
        super();
        setTitle(TITLE);
        setOpaque(false);
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        myOptionsPanel = new OptionsPanel(toolbox);
        myOptionsPanel.getClosed().addObserver((o, arg) -> setVisible(false));

        /* Since this JInternalFrame can be 'torn off' and uses the
         * JInternalFrame's content pane, set the content pane to a JPanel
         * created in this class. */
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setOpaque(false);
        containerPanel.add(myOptionsPanel);
        setContentPane(containerPanel);

        myFramePrefsMonitor = new FramePreferencesMonitor(toolbox.getPreferencesRegistry(), TITLE, this, new Rectangle(700, 500));
    }

    /**
     * Show provider.
     *
     * @param provider the provider
     */
    public void showProvider(OptionsProvider provider)
    {
        myOptionsPanel.showProvider(provider);
    }

    /**
     * Refresh topic tree.
     */
    public void refreshTopicTree()
    {
        myOptionsPanel.refreshTopicTree();
    }
}
