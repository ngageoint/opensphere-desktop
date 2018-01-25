package io.opensphere.laf.dark;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalRadioButtonUI;

public class OSDarkLAFRadioButtonUI extends MetalRadioButtonUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFRadioButtonUI();
    }

    boolean wasOpaque;

    @Override
    protected void paintFocus(Graphics graph, Rectangle area, Dimension dim)
    {
        OSDarkLAFUtils.focusPaint(graph, 1, 1, dim.width - 2, dim.height - 2, 8, 8, OpenSphereDarkLookAndFeel.getFocusColor());
    }

    @Override
    public void installDefaults(AbstractButton button)
    {
        super.installDefaults(button);
        wasOpaque = button.isOpaque();
        button.setOpaque(false);
        icon = OSDarkLAFIconFactory.getRadioButtonIcon();
    }

    @Override
    public void uninstallDefaults(AbstractButton button)
    {
        super.uninstallDefaults(button);
        button.setOpaque(wasOpaque);
        icon = MetalIconFactory.getRadioButtonIcon();
    }
}
