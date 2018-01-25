package io.opensphere.controlpanels.about;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;

/**
 * The Class Options.
 */
public class About extends AbstractInternalFrame
{
    /** The title of the window. */
    public static final String TITLE = "About";

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Container panel. Since this JInternalFrame can be 'torn off' and uses
     * the JInternalFrame's content pane, set the content pane to a JPanel
     * created in this class.
     */
    private JPanel myCPanel;

    /**
     * Instantiates a new layer manager.
     *
     * @param toolbox The toolbox.
     */
    public About(Toolbox toolbox)
    {
        super();
        int height = 420;
        if (StringUtils.isNotEmpty(System.getProperty("opensphere.about")))
        {
            height = 570;
        }
        setSize(600, height);
        setPreferredSize(getSize());
        setMinimumSize(getSize());
        setTitle(TITLE);
        setOpaque(false);
        // TODO It is not clear how minimizing can be done for the HUD. Might
        // need to look into this later
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        getContainerPanel().add(new AboutPanel(toolbox, this));
        setContentPane(getContainerPanel());
    }

    /**
     * Gets the container panel.
     *
     * @return the container panel
     */
    private JPanel getContainerPanel()
    {
        if (myCPanel == null)
        {
            myCPanel = new JPanel(new BorderLayout());
            myCPanel.setOpaque(false);
        }
        return myCPanel;
    }
}
