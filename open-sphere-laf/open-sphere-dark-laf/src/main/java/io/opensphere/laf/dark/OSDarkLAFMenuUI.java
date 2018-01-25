package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuUI;

public class OSDarkLAFMenuUI extends BasicMenuUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFMenuUI();
    }

    @Override
    public void update(Graphics graph, JComponent jComp)
    {
        final JMenu mu = (JMenu)jComp;
        if (mu.isTopLevelMenu())
        {
            mu.setOpaque(false);

            final ButtonModel buttonModel = mu.getModel();
            if (buttonModel.isSelected() || buttonModel.isArmed())
            {
                graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                graph.fillRoundRect(1, 1, jComp.getWidth() - 2, jComp.getHeight() - 3, 2, 2);
            }
        }
        else
        {
            menuItem.setOpaque(false);
            menuItem.setBorderPainted(false);
        }
        super.update(graph, jComp);
    }

    @Override
    protected void paintBackground(Graphics graph, JMenuItem muItem, Color backgroundColor)
    {
        OSDarkLAFUtils.paintMenuBar(graph, muItem, backgroundColor);
    }
}
