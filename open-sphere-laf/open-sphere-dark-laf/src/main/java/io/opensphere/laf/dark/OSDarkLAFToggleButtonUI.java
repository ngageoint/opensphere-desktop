package io.opensphere.laf.dark;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalToggleButtonUI;

public class OSDarkLAFToggleButtonUI extends MetalToggleButtonUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFToggleButtonUI();
    }

    protected MyCustomListener myCustomListener;

    /**
     * A flag used to track the opaqueness of the original paint job.
     */
    protected boolean wasOriginalOpaque;

    @Override
    public void update(Graphics graph, JComponent jComp)
    {
        wasOriginalOpaque = jComp.isOpaque();

        if (!(jComp.getParent() instanceof JToolBar))
        {
            jComp.setOpaque(false);
            super.update(graph, jComp);
            jComp.setOpaque(wasOriginalOpaque);
        }
        else
        {
            super.update(graph, jComp);
        }
    }

    @Override
    protected void paintButtonPressed(Graphics graph, AbstractButton button)
    {
        if (!wasOriginalOpaque)
        {
            return;
        }

        if (button.isContentAreaFilled())
        {
            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graph2D.setColor(OSDarkLAFUtils.opacitizeColor(selectColor, 100));
            final RoundRectangle2D.Float buttonRect = createButtonRectangle(button);
            graph2D.fill(buttonRect);
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    @Override
    protected void paintFocus(Graphics graph, AbstractButton button, Rectangle viewRect, Rectangle textRect, Rectangle iconRect)
    {
        if (!wasOriginalOpaque || !button.isFocusPainted())
        {
            return;
        }

        if (button.getParent() instanceof JToolBar)
        {
            return;
        }

        OSDarkLAFUtils.focusPaint(graph, 3, 3, button.getWidth() - 6, button.getHeight() - 6, 2, 2,
                OpenSphereDarkLookAndFeel.getFocusColor());
    }

    @Override
    public void paint(Graphics graph, JComponent jComp)
    {
        final ButtonModel btModel = ((AbstractButton)jComp).getModel();

        if (wasOriginalOpaque)
        {
            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            final RoundRectangle2D.Float buttonArea = createButtonRectangle(jComp);

            graph2D.clip(buttonArea);
            graph2D.setColor(jComp.getBackground());
            graph2D.fill(buttonArea);

            if (jComp.getParent() instanceof JToolBar)
            {
                if (btModel.isSelected() || btModel.isPressed() || btModel.isRollover())
                {
                    jComp.setBorder(OSDarkLAFBorders.getGenBorder());
                }
                else
                {
                    jComp.setBorder(OSDarkLAFBorders.getEmptyGenBorder());
                }

                if (btModel.isSelected() || btModel.isPressed())
                {
                    graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                    graph2D.fill(buttonArea);
                }
            }
            else
            {
                GradientPaint gradientPaint = null;

                if (btModel.isPressed() || btModel.isSelected())
                {
                    gradientPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getShadowColor(), 0, jComp.getHeight(),
                            OSDarkLAFUtils.getActiveColor());
                }
                else
                {
                    gradientPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), 0, jComp.getHeight(),
                            OSDarkLAFUtils.getShadowColor());
                }

                graph2D.setPaint(gradientPaint);
                graph2D.fill(buttonArea);

                if (btModel.isRollover())
                {
                    graph2D.setColor(OSDarkLAFUtils.getRolloverColor());
                    graph2D.fill(buttonArea);
                }
            }
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
        super.paint(graph, jComp);
    }

    private RoundRectangle2D.Float createButtonRectangle(JComponent jComp)
    {
        final RoundRectangle2D.Float buttonArea = new RoundRectangle2D.Float();
        buttonArea.width = jComp.getWidth();
        buttonArea.height = jComp.getHeight();
        buttonArea.x = 0;
        buttonArea.y = 0;
        buttonArea.arcwidth = 8;
        buttonArea.archeight = 8;
        return buttonArea;
    }

    @Override
    public void installDefaults(AbstractButton bt)
    {
        super.installDefaults(bt);
        bt.setBorder(OSDarkLAFBorders.getButtonBorder());
        selectColor = UIManager.getColor("ScrollBar.thumb");
    }

    @Override
    public void uninstallDefaults(AbstractButton bt)
    {
        super.uninstallDefaults(bt);
        bt.setBorder(MetalBorders.getButtonBorder());
    }

    @Override
    public void installListeners(AbstractButton button)
    {
        super.installListeners(button);
        myCustomListener = new MyCustomListener(button);
        button.addFocusListener(myCustomListener);
        button.addPropertyChangeListener(myCustomListener);
        button.addMouseListener(myCustomListener);
    }

    @Override
    protected void uninstallListeners(AbstractButton button)
    {
        button.removeFocusListener(myCustomListener);
        button.removePropertyChangeListener(myCustomListener);
        button.removeMouseListener(myCustomListener);
    }

    public class MyCustomListener extends MouseInputAdapter implements PropertyChangeListener, FocusListener
    {
        private final AbstractButton parentButton;

        MyCustomListener(AbstractButton button)
        {
            parentButton = button;
        }

        public void redraw()
        {
            if (null != parentButton && null != parentButton.getParent())
            {
                parentButton.getParent().repaint(parentButton.getX() - 5, parentButton.getY() - 5, parentButton.getWidth() + 10,
                        parentButton.getHeight() + 10);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals("enabled"))
            {
                redraw();
            }
        }

        @Override
        public void focusGained(FocusEvent e)
        {
            redraw();
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            redraw();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            parentButton.getModel().setRollover(true);
            redraw();
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            parentButton.getModel().setRollover(false);
            redraw();
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            parentButton.getModel().setRollover(false);
            redraw();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            parentButton.getModel().setRollover(false);
            redraw();
        }
    }
}
