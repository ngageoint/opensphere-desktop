package io.opensphere.laf.dark;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.JTextComponent;

import io.opensphere.laf.dark.border.OSDarkLAFGenBorder;

public class OSDarkLAFTextAreaUI extends BasicTextAreaUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFTextAreaUI(pComponent);
    }

    private boolean isRolledOver;

    private boolean isFocused;

    private MyCustomTextMouseListener myCustomListener;

    public OSDarkLAFTextAreaUI(JComponent jComp)
    {
        super();
    }

    @Override
    protected void paintBackground(Graphics graph)
    {
        final JTextComponent comp = getComponent();

        final Border compBorder = comp.getBorder();

        if (null != compBorder && compBorder instanceof OSDarkLAFGenBorder)
        {
            graph.setColor(getComponent().getBackground());
            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graph.fillRoundRect(2, 2, comp.getWidth() - 4, comp.getHeight() - 4, 7, 7);
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            if (comp.isEditable() && comp.isEnabled())
            {
                if (isFocused)
                {
                    OSDarkLAFUtils.focusPaint(graph, 1, 1, comp.getWidth() - 2, comp.getHeight() - 2, 2, 2,
                            OpenSphereDarkLookAndFeel.getFocusColor());
                }
                else if (isRolledOver)
                {
                    OSDarkLAFUtils.focusPaint(graph, 1, 1, comp.getWidth() - 2, comp.getHeight() - 2, 2, 2,
                            OSDarkLAFUtils.opacitizeColor(OpenSphereDarkLookAndFeel.getFocusColor(), 150));
                }
            }
        }
        else
        {
            super.paintBackground(graph);
        }
    }

    @Override
    protected void installListeners()
    {
        super.installListeners();
        myCustomListener = new MyCustomTextMouseListener();
        getComponent().addFocusListener(myCustomListener);
        getComponent().addMouseListener(myCustomListener);
    }

    @Override
    protected void uninstallListeners()
    {
        super.uninstallListeners();
        getComponent().removeFocusListener(myCustomListener);
        getComponent().removeMouseListener(myCustomListener);
    }

    public boolean isFocus()
    {
        return isFocused;
    }

    public boolean isRollover()
    {
        return isRolledOver;
    }

    class MyCustomTextMouseListener extends MouseAdapter implements FocusListener
    {
        protected void repaintBorder()
        {
            if (getComponent().getParent() != null)
            {
                final Component parentComp = getComponent();
                parentComp.getParent().repaint(parentComp.getX() - 5, parentComp.getY() - 5, parentComp.getWidth() + 10,
                        parentComp.getHeight() + 10);
            }
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            isRolledOver = false;
            repaintBorder();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            isRolledOver = true;
            repaintBorder();
        }

        @Override
        public void focusGained(FocusEvent e)
        {
            isFocused = true;
            repaintBorder();
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            isFocused = false;
            repaintBorder();
        }
    }
}
