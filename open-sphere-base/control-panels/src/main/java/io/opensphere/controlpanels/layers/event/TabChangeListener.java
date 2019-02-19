package io.opensphere.controlpanels.layers.event;

import java.awt.Color;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jidesoft.swing.JideTabbedPane;

import io.opensphere.core.util.Colors;

public class TabChangeListener implements ChangeListener
{
    int lastTab;

    @Override
    public void stateChanged(ChangeEvent e)
    {
        JideTabbedPane pane = (JideTabbedPane)e.getSource();
        pane.setBackgroundAt(lastTab, new Color(0, 0, 0, 0));
        lastTab = pane.getSelectedIndex();
        pane.setBackgroundAt(lastTab, Colors.LF_PRIMARY2);
    }
}
