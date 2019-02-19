package io.opensphere.controlpanels.layers.event;

import java.awt.Color;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jidesoft.swing.JideTabbedPane;

import io.opensphere.core.util.Colors;

/**
 * Listener for tab selection events.
 * Changes background colors.
 */
public class TabChangeListener implements ChangeListener
{
	/** Previously-selected tab. */
    private int lastTab;

    @Override
    public void stateChanged(ChangeEvent e)
    {
        JideTabbedPane pane = (JideTabbedPane)e.getSource();
        pane.setBackgroundAt(lastTab, new Color(0, 0, 0, 0));
        lastTab = pane.getSelectedIndex();
        pane.setBackgroundAt(lastTab, Colors.LF_PRIMARY2);
    }
}
