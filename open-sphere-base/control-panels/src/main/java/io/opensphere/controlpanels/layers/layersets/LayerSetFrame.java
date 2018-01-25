package io.opensphere.controlpanels.layers.layersets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.Collections;

import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.util.swing.AbstractHUDPanel;

/**
 * The frame for layer sets.
 */
public class LayerSetFrame extends AbstractInternalFrame implements PreferenceChangeListener
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Frame. */
    private final transient HUDJInternalFrame myFrame;

    /** True when the frame has been registered with the component registry. */
    private boolean myFrameRegistered;

    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /**
     * Instantiates a new layer detail frame.
     *
     * @param tb the tb
     */
    public LayerSetFrame(Toolbox tb)
    {
        super();
        myToolbox = tb;
        setTitle("Layer Set Manager");
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        myToolbox.getPreferencesRegistry().getPreferences(AbstractHUDPanel.class)
                .addPreferenceChangeListener(AbstractHUDPanel.ourHUDBackgroundColorKey, this);
        setBackground(new Color(myToolbox.getPreferencesRegistry().getPreferences(AbstractHUDPanel.class)
                .getInt(AbstractHUDPanel.ourHUDBackgroundColorKey, new JPanel().getBackground().getRGB()), true));

        HUDJInternalFrame.Builder builder = new HUDJInternalFrame.Builder();
        builder.setInternalFrame(this);
        myFrame = new HUDJInternalFrame(builder);
        setSize(450, 350);
        setMinimumSize(new Dimension(450, 350));
        setPreferredSize(new Dimension(450, 350));

        Frame mainFrame = myToolbox.getUIRegistry().getMainFrameProvider().get();
        int mainFrameWidth = mainFrame.getWidth();
        int mainFrameHeight = mainFrame.getHeight();

        int xLoc = mainFrameWidth / 2 - getWidth() / 2;
        int yLoc = mainFrameHeight - 800;
        setLocation(xLoc < 0 ? 0 : xLoc, yLoc < 0 ? 0 : yLoc);
        initialize();
        pack();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        setBackground(new Color(myToolbox.getPreferencesRegistry().getPreferences(AbstractHUDPanel.class)
                .getInt(AbstractHUDPanel.ourHUDBackgroundColorKey, new JPanel().getBackground().getRGB()), true));
    }

    /**
     * Sets the micro frame visibility.
     *
     * @param visible the micro frame visible
     */
    public void setFrameVisible(boolean visible)
    {
        if (visible)
        {
            if (!myFrameRegistered)
            {
                myToolbox.getUIRegistry().getComponentRegistry().addObjectsForSource(this, Collections.singleton(myFrame));
                myFrameRegistered = true;
            }
        }
        else
        {
            if (myFrameRegistered)
            {
                myToolbox.getUIRegistry().getComponentRegistry().removeObjectsForSource(this);
                myFrameRegistered = false;
            }
        }
        myFrame.setVisible(visible);
    }

    /**
     * Sets the data group info.
     */
    private void initialize()
    {
        LayerSetPanel panel = new LayerSetPanel(myToolbox, this);
        setContentPane(panel);
        invalidate();
        revalidate();
        repaint();
    }
}
