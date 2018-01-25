package io.opensphere.hud.dashboard;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.swing.FramePreferencesMonitor;

/**
 * The Class Dashboard.
 */
public class Dashboard extends AbstractInternalFrame
{
    /** The title of the window. */
    public static final String TITLE = "Dashboard";

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The manager for changes to the frame position and size preferences. */
    @SuppressWarnings("unused")
    private final transient FramePreferencesMonitor myFramePrefsMonitor;

    /**
     * Instantiates a new layer manager.
     *
     * @param toolbox The toolbox.
     */
    public Dashboard(Toolbox toolbox)
    {
        super();
        setTitle(TITLE);
        setOpaque(false);
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new DashboardPanel(toolbox), BorderLayout.CENTER);
        setContentPane(panel);

        myFramePrefsMonitor = new FramePreferencesMonitor(toolbox.getPreferencesRegistry(), TITLE, this, new Rectangle(300, 250));
    }
}
