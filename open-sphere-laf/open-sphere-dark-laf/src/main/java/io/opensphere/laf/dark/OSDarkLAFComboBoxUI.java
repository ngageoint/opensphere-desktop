package io.opensphere.laf.dark;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.ButtonModel;
import javax.swing.CellRendererPane;
import javax.swing.ComboBoxEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalComboBoxButton;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import javax.swing.plaf.metal.MetalComboBoxUI;

/**
 * OpenSphere Dark UI for Combo Boxes.
 */
public class OSDarkLAFComboBoxUI extends MetalComboBoxUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFComboBoxUI();
    }

    /**
     * A flag used to track the rolled-over state of the combo box.
     */
    private boolean isRollover;

    /**
     * A flag used to track the focused state of the combo box.
     */
    private boolean isFocus;

    /**
     * A flag used to track the opaqueness of the original paint job.
     */
    protected boolean wasOriginalOpaque;

    /**
     * A listener used to react to mouse events when the component is rolled
     * over or focused upon.
     */
    private MouseRolloverEffectListener myEffectListener;

    @Override
    protected JButton createArrowButton()
    {
        return new OSDarkLAFComboBoxButton(comboBox, UIManager.getIcon("ComboBox.buttonDownIcon"),
                comboBox.isEditable() ? true : false, currentValuePane, listBox);
    }

    @Override
    protected ComboBoxEditor createEditor()
    {
        return new OSDarkLAFComboBoxEditor();
    }

    @Override
    public Dimension getMinimumSize(JComponent jComp)
    {
        final Dimension dim = super.getMinimumSize(jComp);

        if (comboBox.isEditable())
        {
            dim.height = editor.getPreferredSize().height - 2;
        }

        dim.width -= 20;

        return dim;
    }

    @Override
    protected void installDefaults()
    {
        super.installDefaults();
        wasOriginalOpaque = comboBox.isOpaque();
        comboBox.setOpaque(false);
    }

    @Override
    protected void installListeners()
    {
        super.installListeners();
        myEffectListener = new MouseRolloverEffectListener();
        comboBox.addFocusListener(myEffectListener);
        comboBox.addMouseListener(myEffectListener);
    }

    @Override
    protected void uninstallDefaults()
    {
        super.uninstallDefaults();
        comboBox.setOpaque(wasOriginalOpaque);
    }

    @Override
    protected void uninstallListeners()
    {
        super.uninstallListeners();
        comboBox.removeFocusListener(myEffectListener);
        comboBox.removeMouseListener(myEffectListener);
    }

    /**
     * The OpenSphere Dark Look and Feel for the combo box button.
     */
    private final class OSDarkLAFComboBoxButton extends MetalComboBoxButton
    {
        /**
         * The unique identifier used for serialization operations.
         */
        private static final long serialVersionUID = -7378877987964223611L;

        /**
         * Creates a new button bound to the supplied combo box, initialized
         * with the supplied icon.
         *
         * @param comboBox the combo box to which the button is bound.
         * @param ic the icon to display in the button.
         * @param editable a flag used to control the editable state of the
         *            combo box.
         * @param renderPane the pane in which the button is rendered.
         * @param list the list in which choices are rendered.
         */
        public OSDarkLAFComboBoxButton(JComboBox<Object> comboBox, Icon ic, boolean editable, CellRendererPane renderPane, JList<Object> list)
        {
            super(comboBox, ic, editable, renderPane, list);

            myEffectListener = new MouseRolloverEffectListener();
            addMouseListener(myEffectListener);
            addFocusListener(myEffectListener);
        }

        @Override
        public boolean contains(int x, int y)
        {
            boolean result = super.contains(x, y);
            if (result && !iconOnly)
            {
                if (x < getWidth() - comboIcon.getIconWidth() - getInsets().right - 5)
                {
                    result = false;
                }
            }
            return result;
        }

        @Override
        public void paintComponent(Graphics graph)
        {
            boolean thinBorder = false;

            if (iconOnly)
            {
                Border buttonBorder = OSDarkLAFBorders.getComboButtonBorder();
                final Insets buttonInsets = buttonBorder.getBorderInsets(comboBox);

                if (getSize().height < comboIcon.getIconHeight() + buttonInsets.top + buttonInsets.bottom
                        || getSize().width < comboIcon.getIconWidth() + buttonInsets.left + buttonInsets.right)
                {
                    thinBorder = true;
                    buttonBorder = OSDarkLAFBorders.getThinGenBorder();
                }

                setBorder(buttonBorder);
                setMargin(new Insets(0, 5, 0, 7));
            }
            else
            {
                Border buttonBorder = OSDarkLAFBorders.getComboEditorBorder();
                final Insets buttonInsets = buttonBorder.getBorderInsets(comboBox);

                if (getSize().height < getFont().getSize() + buttonInsets.top + buttonInsets.bottom)
                {
                    thinBorder = true;
                    buttonBorder = OSDarkLAFBorders.getThinGenBorder();
                }
                setBorder(buttonBorder);
                setOpaque(false);
            }

            if (comboBox != null && !iconOnly)
            {
                try
                {
                    graph.setColor(getBackground());
                    if (!thinBorder)
                    {
                        graph.fillRect(2, 3, getWidth() - 4, getHeight() - 6);
                    }
                    else
                    {
                        graph.fillRect(0, 0, getWidth(), getHeight());
                    }

                    graph.drawLine(3, 2, getWidth() - 4, 2);
                    graph.drawLine(3, getHeight() - 3, getWidth() - 4, getHeight() - 3);

                    paintWithIcons(graph);
                }
                catch (final Exception e)
                {
                    // Intentionally grounded
                }
            }

            if (iconOnly)
            {
                final RoundRectangle2D.Float buttonArea = new RoundRectangle2D.Float();
                if (thinBorder)
                {
                    buttonArea.width = getWidth();
                    buttonArea.height = getHeight();
                    buttonArea.x = 0;
                    buttonArea.y = 0;
                    buttonArea.arcwidth = 1;
                    buttonArea.archeight = 1;
                }
                else
                {
                    buttonArea.width = getWidth() - 4;
                    buttonArea.height = getHeight() - 4;
                    buttonArea.x = 2;
                    buttonArea.y = 2;
                    buttonArea.arcwidth = 8;
                    buttonArea.archeight = 8;
                }

                setOpaque(false);
                paintWithIcons(graph);

                final ButtonModel buttonModel = getModel();
                final Graphics2D graph2D = (Graphics2D)graph;
                graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = null;

                if (buttonModel.isSelected() || buttonModel.isPressed())
                {
                    gp = new GradientPaint(0, 0, OSDarkLAFUtils.getShadowColor(), 0, getHeight(),
                            OSDarkLAFUtils.getActiveColor());
                    graph2D.setPaint(gp);
                    graph2D.fill(buttonArea);
                }
                else
                {
                    gp = new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), 0, getHeight(),
                            OSDarkLAFUtils.getShadowColor());
                    graph2D.setPaint(gp);
                    graph2D.fill(buttonArea);
                }

                graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            if (isEnabled() && !thinBorder)
            {
                if (isFocus)
                {
                    OSDarkLAFUtils.focusPaint(graph, 1, 1, getWidth() - 2, getHeight() - 2, 4, 4, 3,
                            OpenSphereDarkLookAndFeel.getFocusColor());
                }
                else if (isRollover)
                {
                    OSDarkLAFUtils.focusPaint(graph, 1, 1, getWidth() - 2, getHeight() - 2, 4, 4, 3,
                            OSDarkLAFUtils.opacitizeColor(OpenSphereDarkLookAndFeel.getFocusColor(), 150));
                }
            }
        }

        /**
         * Paints the button using icons.
         *
         * @param graph the graphics component with which painting is performed.
         */
        protected void paintWithIcons(Graphics graph)
        {
            final boolean isLeftToRight = comboBox.getComponentOrientation().isLeftToRight();

            if (ui != null)
            {
                final Graphics g = graph == null ? null : graph.create();
                try
                {
                    ui.update(g, this);
                }
                finally
                {
                    g.dispose();
                }
            }

            final Insets cbInsets = getInsets();

            final int w = getWidth() - (cbInsets.left + cbInsets.right);
            final int h = getHeight() - (cbInsets.top + cbInsets.bottom);

            if (w <= 0 || h <= 0)
            {
                return;
            }

            final int left = cbInsets.left;
            final int right = left + w - 1;
            final int top = cbInsets.top;
            final int bottom = top + h - 1;

            int icWidth = 0;
            int icLeft = isLeftToRight ? right : left;

            if (comboIcon != null)
            {
                icWidth = comboIcon.getIconWidth();
                final int icHeight = comboIcon.getIconHeight();
                int icTop = 0;

                if (iconOnly)
                {
                    icLeft = getWidth() / 2 - icWidth / 2;
                    icTop = getHeight() / 2 - icHeight / 2;
                }
                else
                {
                    icLeft = isLeftToRight ? left + w - 1 - icWidth : left;
                    icTop = top + (bottom - top) / 2 - icHeight / 2;
                }

                comboIcon.paintIcon(this, graph, icLeft, icTop);

                if (!iconOnly)
                {
                    graph.setColor(OSDarkLAFUtils.getShadowColor());
                    graph.drawLine(icLeft - 5, 6, icLeft - 5, getHeight() - 6);
                    graph.setColor(OSDarkLAFUtils.getActiveColor());
                    graph.drawLine(icLeft - 4, 6, icLeft - 4, getHeight() - 6);
                }
            }

            // Let the renderer paint
            if (!iconOnly && comboBox != null)
            {
                final ListCellRenderer<Object> cellRenderer = comboBox.getRenderer();
                Component comp;
                final boolean isRenderPressed = getModel().isPressed();
                comp = cellRenderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, isRenderPressed, false);
                comp.setFont(rendererPane.getFont());

                if (model.isPressed() && model.isArmed())
                {
                    comp.setForeground(comboBox.getForeground());
                    comp.setBackground(getBackground());
                }
                else if (!comboBox.isEnabled())
                {
                    if (isOpaque())
                    {
                        comp.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
                    }

                    comp.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
                }
                else
                {
                    comp.setForeground(comboBox.getForeground());
                    comp.setBackground(comboBox.getBackground());
                }

                final int compWidth = w - (cbInsets.right + icWidth);

                boolean needsValidate = false;
                if (comp instanceof JPanel)
                {
                    needsValidate = true;
                }

                if (isLeftToRight)
                {
                    rendererPane.paintComponent(graph, comp, this, left, top, compWidth, h, needsValidate);
                }
                else
                {
                    rendererPane.paintComponent(graph, comp, this, left + icWidth, top, compWidth, h, needsValidate);
                }
            }
        }
    }

    /**
     * The OpenSphere Dark Look and Feel for the combo box editor.
     */
    public class OSDarkLAFComboBoxEditor extends MetalComboBoxEditor
    {
        /**
         * Creates a new combo box editor.
         */
        public OSDarkLAFComboBoxEditor()
        {
            super();
            editor.setBorder(OSDarkLAFBorders.getComboEditorBorder());
        }
    }

    /**
     * The roll over listener used for combo boxes.
     */
    public class MouseRolloverEffectListener extends MouseAdapter implements FocusListener
    {
        @Override
        public void focusGained(FocusEvent e)
        {
            isFocus = true;
            repaint();
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            isFocus = false;
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            isRollover = true;
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            isRollover = false;
            repaint();
        }

        /**
         * Repaints the parent component.
         */
        protected void repaint()
        {
            if (null != comboBox && null != comboBox.getParent())
            {
                comboBox.getParent().repaint(comboBox.getX() - 5, comboBox.getY() - 5, comboBox.getWidth() + 10,
                        comboBox.getHeight() + 10);
            }
        }
    }
}
