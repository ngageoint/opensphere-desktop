package io.opensphere.laf.dark;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 * A Pluggable look and feel interface for JButton.
 */
public class OSDarkLAFButtonUI extends MetalButtonUI
{
    /**
     * Utility method used to create the button UI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new button UI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFButtonUI();
    }

    /**
     * A flag used to track the opaqueness of the original paint job.
     */
    protected boolean wasOriginalOpaque;

    /**
     * The listener used to respond to mouse-over events.
     */
    protected MouseOverEffectListener myListener;

    /**
     * Creates a rounded button area using the supplied component as a template.
     *
     * @param pComponent the template from which to create the button area.
     * @return a rounded rectangle containing the bounds of the target button.
     */
    private RoundRectangle2D.Float createRoundedButtonAreaFromComponent(JComponent pComponent)
    {
        final RoundRectangle2D.Float button = new RoundRectangle2D.Float();
        button.width = pComponent.getWidth();
        button.height = pComponent.getHeight();
        button.x = 0;
        button.y = 0;
        button.arcwidth = 8;
        button.archeight = 8;
        return button;
    }

    @Override
    public void installDefaults(AbstractButton bt)
    {
        super.installDefaults(bt);
        bt.setBorder(OSDarkLAFBorders.getButtonBorder());
        selectColor = OpenSphereDarkLookAndFeel.getFocusColor();
    }

    @Override
    public void installListeners(AbstractButton bt)
    {
        super.installListeners(bt);
        myListener = new MouseOverEffectListener(bt);
        bt.addMouseListener(myListener);
        bt.addPropertyChangeListener(myListener);
        bt.addFocusListener(myListener);
    }

    @Override
    public void paint(Graphics graph, JComponent pComponent)
    {
        final ButtonModel btModel = ((AbstractButton)pComponent).getModel();

        if (wasOriginalOpaque)
        {
            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            final RoundRectangle2D.Float area = createRoundedButtonAreaFromComponent(pComponent);

            graph2D.clip(area);
            graph2D.setColor(pComponent.getBackground());
            graph2D.fill(area);

            if (!(pComponent.getParent() instanceof JToolBar))
            {
                GradientPaint gradientPaint = null;

                if (btModel.isSelected() || btModel.isPressed())
                {
                    gradientPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getShadowColor(), 0, pComponent.getHeight(),
                            OSDarkLAFUtils.getActiveColor());
                }
                else
                {
                    gradientPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), 0, pComponent.getHeight(),
                            OSDarkLAFUtils.getShadowColor());
                }

                graph2D.setPaint(gradientPaint);
                graph2D.fill(area);

                if (btModel.isRollover())
                {
                    graph2D.setColor(OSDarkLAFUtils.getRolloverColor());
                    graph2D.fill(area);
                }
            }
            else
            {
                if (btModel.isSelected() || btModel.isPressed() || btModel.isRollover())
                {
                    pComponent.setBorder(OSDarkLAFBorders.getGenBorder());
                }
                else
                {
                    pComponent.setBorder(OSDarkLAFBorders.getEmptyGenBorder());
                }

                if (btModel.isSelected() || btModel.isPressed())
                {
                    graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                    graph2D.fill(area);
                }
            }
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
        super.paint(graph, pComponent);
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
            final RoundRectangle2D.Float buttonArea = createRoundedButtonAreaFromComponent(button);
            graph2D.fill(buttonArea);
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    @Override
    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect)
    {
        if (b.getParent() instanceof JToolBar || !b.isFocusPainted() || !wasOriginalOpaque)
        {
            return;
        }

        OSDarkLAFUtils.focusPaint(g, 3, 3, b.getWidth() - 6, b.getHeight() - 6, 2, 2, OpenSphereDarkLookAndFeel.getFocusColor());
    }

    @Override
    protected void uninstallListeners(AbstractButton bt)
    {
        bt.removeMouseListener(myListener);
        bt.removePropertyChangeListener(myListener);
        bt.removeFocusListener(myListener);
    }

    @Override
    public void uninstallDefaults(AbstractButton bt)
    {
        super.uninstallDefaults(bt);
        bt.setBorder(MetalBorders.getButtonBorder());
    }

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
}
