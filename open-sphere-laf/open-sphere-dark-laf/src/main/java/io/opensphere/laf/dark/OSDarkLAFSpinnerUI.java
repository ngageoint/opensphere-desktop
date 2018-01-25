package io.opensphere.laf.dark;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

public class OSDarkLAFSpinnerUI extends BasicSpinnerUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFSpinnerUI();
    }

    /**
     * A flag used to track the opaqueness of the original paint job.
     */
    protected boolean wasOriginalOpaque;

    @Override
    protected Component createPreviousButton()
    {
        final Component comp = createArrowButton(SwingConstants.SOUTH);
        comp.setName("Spinner.previousButton");
        installPreviousButtonListeners(comp);
        return comp;
    }

    @Override
    protected Component createNextButton()
    {
        final Component comp = createArrowButton(SwingConstants.NORTH);
        comp.setName("Spinner.nextButton");
        installNextButtonListeners(comp);
        return comp;
    }

    private Component createArrowButton(int dir)
    {
        final JButton button = new OSDarkLAFArrowButton(dir);
        button.setInheritsPopupMenu(true);
        return button;
    }

    @Override
    public void paint(Graphics graph, JComponent jComp)
    {
        super.paint(graph, jComp);

        graph.setColor(jComp.getBackground());
        graph.fillRect(2, 3, jComp.getWidth() - 4, jComp.getHeight() - 6);
        graph.drawLine(3, 2, jComp.getWidth() - 4, 2);
        graph.drawLine(3, jComp.getHeight() - 3, jComp.getWidth() - 4, jComp.getHeight() - 3);

        final JComponent compEditor = spinner.getEditor();
        if (compEditor instanceof JSpinner.DefaultEditor)
        {
            final JTextField aTextField = ((JSpinner.DefaultEditor)compEditor).getTextField();
            aTextField.setBackground(jComp.getBackground());
        }
    }

    @Override
    protected void installDefaults()
    {
        super.installDefaults();
        wasOriginalOpaque = spinner.isOpaque();
        spinner.setOpaque(false);
    }

    @Override
    protected void uninstallDefaults()
    {
        super.uninstallDefaults();
        spinner.setOpaque(wasOriginalOpaque);
    }

    @SuppressWarnings("serial")
    class OSDarkLAFArrowButton extends JButton
    {
        private final int direction;

        public OSDarkLAFArrowButton(int pDirection)
        {
            super();
            setRequestFocusEnabled(false);
            direction = pDirection;

            if (pDirection == SwingConstants.NORTH)
            {
                setIcon(UIManager.getIcon("Spinner.nextIcon"));
            }
            else
            {
                setIcon(UIManager.getIcon("Spinner.previousIcon"));
            }

            setOpaque(false);
        }

        @Override
        public void paint(Graphics graph)
        {
            final Icon ic = getIcon();

            int width = getWidth() - 1;
            final int height = getHeight() - 1;
            final int x = (width - ic.getIconWidth()) / 2;
            int y = (height - ic.getIconHeight()) / 2;

            int yf = y;

            final Border parentBorder = ((JSpinner)getParent()).getBorder();
            if (null != parentBorder)
            {
                if (direction != SwingConstants.NORTH)
                {
                    yf = getHeight() - parentBorder.getBorderInsets(this).bottom;
                }
                else
                {
                    y += parentBorder.getBorderInsets(this).top / 2;
                    yf = parentBorder.getBorderInsets(this).top - 2;
                }

                width -= 3;
            }

            ic.paintIcon(this, graph, x, y);

            if (direction != SwingConstants.NORTH)
            {
                graph.setColor(OSDarkLAFUtils.getActiveColor());
                graph.drawLine(1, 0, width, 0);
                graph.drawLine(1, 0, 1, yf);
                graph.setColor(OSDarkLAFUtils.getShadowColor());
                graph.drawLine(0, 0, 0, yf);
            }
            else
            {
                graph.setColor(OSDarkLAFUtils.getActiveColor());
                graph.drawLine(1, yf, 1, height);
                graph.setColor(OSDarkLAFUtils.getShadowColor());
                graph.drawLine(1, height, width, height);
                graph.drawLine(0, yf, 0, height);
            }
        }

        @Override
        public boolean isFocusTraversable()
        {
            return false;
        }

        @Override
        public Dimension getPreferredSize()
        {
            return new Dimension(15, getIcon().getIconHeight());
        }

        @Override
        public Dimension getMinimumSize()
        {
            return getPreferredSize();
        }
    }
}
