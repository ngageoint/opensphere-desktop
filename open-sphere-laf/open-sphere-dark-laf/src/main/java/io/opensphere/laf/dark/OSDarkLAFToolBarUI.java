package io.opensphere.laf.dark;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolBarUI;

public class OSDarkLAFToolBarUI extends MetalToolBarUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFToolBarUI();
    }

    public OSDarkLAFToolBarUI()
    {
        super();
    }

    @Override
    protected Border createRolloverBorder()
    {
        return OSDarkLAFBorders.getGenBorder();
    }
}
