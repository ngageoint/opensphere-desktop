package io.opensphere.laf.dark;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

import io.opensphere.laf.dark.border.OSDarkLAFGenBorder;

/**
 * Basis of the OpenSphere Dark look and feel for a JTextField.
 */
public class OSDarkLAFTextFieldUI extends BasicTextFieldUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFTextFieldUI(pComponent);
    }

    private boolean isFocused;

    private boolean isRolledOver;

    private MyCustomTextMouseListener myCustomTextMouseListener;

    protected boolean isWeak;

    /**
     * A flag used to track the opaqueness of the original paint job.
     */
    protected boolean wasOriginalOpaque;

    OSDarkLAFTextFieldUI(JComponent jComp)
    {
        super();
    }

    public boolean isFocus()
    {
        return isFocused;
    }

    public boolean isRollover()
    {
        return isRolledOver;
    }

    @Override
    protected void paintSafely(Graphics graph)
    {
        paintFocus(graph);
        paintAll(graph);
        super.paintSafely(graph);
    }

    protected void paintAll(Graphics graph)
    {
        final JTextComponent comp = getComponent();

        final Border compBorder = comp.getBorder();

        if (compBorder != null && compBorder instanceof OSDarkLAFGenBorder)
        {
            final Insets ins = OSDarkLAFBorders.getTextFieldBorder().getBorderInsets(comp);

            if (comp.getSize().height + 2 < comp.getFont().getSize() + ins.top + ins.bottom)
            {
                comp.setBorder(OSDarkLAFBorders.getThinGenBorder());
                isWeak = true;
            }
            else
            {
                comp.setBorder(OSDarkLAFBorders.getTextFieldBorder());
                isWeak = false;
            }

            if (!comp.isEnabled() || !comp.isEditable())
            {
                graph.setColor(UIManager.getColor("TextField.inactiveBackground"));
            }
            else
            {
                graph.setColor(comp.getBackground());
            }

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graph.fillRoundRect(2, 2, comp.getWidth() - 4, comp.getHeight() - 4, 7, 7);

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
        else
        {
            super.paintBackground(graph);
        }
    }

    protected void paintFocus(Graphics graph)
    {
        final JTextComponent comp = getComponent();

        if (comp.isEnabled() && comp.isEditable() && !isWeak)
        {
            if (isFocused)
            {
                OSDarkLAFUtils.focusPaint(graph, 1, 1, comp.getWidth() - 2, comp.getHeight() - 2, 4, 4, 3,
                        OpenSphereDarkLookAndFeel.getFocusColor());
            }
            else if (isRolledOver)
            {
                OSDarkLAFUtils.focusPaint(graph, 1, 1, comp.getWidth() - 2, comp.getHeight() - 2, 4, 4, 3,
                        OSDarkLAFUtils.opacitizeColor(OpenSphereDarkLookAndFeel.getFocusColor(), 150));
            }
        }
    }

    @Override
    protected void paintBackground(Graphics graph)
    {
    }

    @Override
    protected void installDefaults()
    {
        super.installDefaults();
        wasOriginalOpaque = getComponent().isOpaque();
        getComponent().setOpaque(false);
    }

    @Override
    protected void uninstallDefaults()
    {
        super.uninstallDefaults();
        getComponent().setOpaque(wasOriginalOpaque);
    }

    @Override
    protected void installListeners()
    {
        super.installListeners();
        myCustomTextMouseListener = new MyCustomTextMouseListener();
        getComponent().addFocusListener(myCustomTextMouseListener);
        getComponent().addMouseListener(myCustomTextMouseListener);
    }

    @Override
    protected void uninstallListeners()
    {
        super.uninstallListeners();
        getComponent().removeFocusListener(myCustomTextMouseListener);
        getComponent().removeMouseListener(myCustomTextMouseListener);
    }

    class MyCustomTextMouseListener extends MouseAdapter implements FocusListener
    {
        protected void repaint()
        {
            if (null != getComponent().getParent())
            {
                final Component parent = getComponent();
                parent.getParent().repaint(parent.getX() - 5, parent.getY() - 5, parent.getWidth() + 10, parent.getHeight() + 10);
            }
        }

        @Override
        public void focusGained(FocusEvent e)
        {
            isFocused = true;
            repaint();
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            isFocused = false;
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            isRolledOver = false;
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            isRolledOver = true;
            repaint();
        }
    }
}
