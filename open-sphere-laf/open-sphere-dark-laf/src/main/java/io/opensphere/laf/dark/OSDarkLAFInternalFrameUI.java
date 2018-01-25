package io.opensphere.laf.dark;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalInternalFrameUI;

public class OSDarkLAFInternalFrameUI extends MetalInternalFrameUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFInternalFrameUI((JInternalFrame)pComponent);
    }

    OSDarkLAFInternalFrameTitlePane titlePane;

    public OSDarkLAFInternalFrameUI(JInternalFrame jIntFr)
    {
        super(jIntFr);
    }

    @Override
    protected JComponent createNorthPane(JInternalFrame jIntFr)
    {
        super.createNorthPane(jIntFr);
        titlePane = new OSDarkLAFInternalFrameTitlePane(jIntFr);
        return titlePane;
    }

    @Override
    public void update(Graphics graph, JComponent jComp)
    {
        paint(graph, jComp);
    }
}
