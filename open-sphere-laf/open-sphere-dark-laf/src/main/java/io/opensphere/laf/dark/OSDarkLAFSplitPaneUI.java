package io.opensphere.laf.dark;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.metal.MetalSplitPaneUI;

public class OSDarkLAFSplitPaneUI extends MetalSplitPaneUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFSplitPaneUI();
    }

    @Override
    public BasicSplitPaneDivider createDefaultDivider()
    {
        return new OSDarkLAFSplitPaneDivider(this);
    }
}
