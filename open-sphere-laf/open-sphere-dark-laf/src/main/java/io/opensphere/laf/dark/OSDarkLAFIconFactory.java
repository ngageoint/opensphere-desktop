package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

/**
 * A factory implementation used to create icons for the OpenSphere Dark Look
 * and Feel.
 */
public class OSDarkLAFIconFactory implements Serializable
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -8198423379438718459L;

    private static Icon checkBoxIcon;

    private static Icon checkBoxMenuItemIcon;

    private static Icon comboArrowIcon;

    private static Icon frameAltMaximizeIcon;

    private static Icon frameCloseIcon;

    private static Icon frameMaxIcon;

    private static Icon frameMinIcon;

    private static Icon groupMenuItemIcon;

    private static Icon radioButtonIcon;

    private static Icon radioButtonMenuItemIcon;

    private static Icon scrollEastIcon;

    private static Icon scrollNorthIcon;

    private static Icon scrollSouthIcon;

    private static Icon scrollWestIcon;

    private static Icon sliderHorizIcon;

    private static Icon sliderVertIcon;

    private static Icon spinnerNextIcon;

    private static Icon spinnerPreviousIcon;

    private static Icon treeCollapsedIcon;

    private static Icon treeExpandedIcon;

    /**
     * Private constructor to prevent instantiation of utility classes.
     */
    private OSDarkLAFIconFactory()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    private static class GroupMenuItemIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public GroupMenuItemIcon()
        {
            width = 21;
            height = 0;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final JMenuItem jMuIt = (JMenuItem)comp;

            graph.setColor(OSDarkLAFUtils.getRolloverColor());
            graph.fillRect(0, 0, 22, jMuIt.getHeight());
            graph.setColor(OSDarkLAFUtils.getShadowColor());
            graph.drawLine(22, 0, 22, jMuIt.getHeight());
            graph.setColor(OSDarkLAFUtils.getActiveColor());
            graph.drawLine(23, 0, 23, jMuIt.getHeight());
        }
    }

    private static class CheckBoxIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public CheckBoxIcon()
        {
            width = 21;
            height = 21;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final JCheckBox jCheckBox = (JCheckBox)comp;
            final ButtonModel btModel = jCheckBox.getModel();

            final boolean isEnabled = btModel.isEnabled();
            final boolean isSelectedOrPressed = btModel.isSelected() || btModel.isPressed();

            graph.setColor(OpenSphereDarkLookAndFeel.getControl());
            graph.fillRect(x + 4, y + 3, 13, 15);
            graph.drawLine(x + 3, y + 4, x + 3, y + 16);
            graph.drawLine(x + 17, y + 4, x + 17, y + 16);

            Icon icono = UIManager.getIcon("CheckBox.iconBase");
            icono.paintIcon(comp, graph, x, y);

            if (isSelectedOrPressed)
            {
                graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                graph.fillRect(x + 4, y + 3, 13, 15);
                graph.drawLine(x + 3, y + 4, x + 3, y + 16);
                graph.drawLine(x + 17, y + 4, x + 17, y + 16);
            }

            if (btModel.isArmed() && isEnabled)
            {
                graph.setColor(new Color(255, 255, 155, 127));
                graph.fillRect(x + 5, y + 5, 11, 11);
            }

            if (isSelectedOrPressed)
            {
                icono = UIManager.getIcon("CheckBox.iconTick");
                icono.paintIcon(comp, graph, x, y);
            }
        }
    }

    private static class CheckBoxMenuItemIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public CheckBoxMenuItemIcon()
        {
            width = 21;
            height = 0;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final JMenuItem jMenuIt = (JMenuItem)comp;
            final ButtonModel btModel = jMenuIt.getModel();

            x = 1;
            y = 0;
            final boolean isEnabled = btModel.isEnabled();
            final boolean isSelectedOrPressed = btModel.isSelected() || btModel.isPressed();

            Icon ic = UIManager.getIcon("MenuCheckBox.iconBase");
            ic.paintIcon(comp, graph, x, y);

            if (!isEnabled)
            {
                graph.setColor(new Color(0, 0, 0, 63));
                graph.fillRect(x + 4, y + 3, 13, 15);
                graph.drawLine(x + 3, y + 4, x + 3, y + 16);
                graph.drawLine(x + 17, y + 4, x + 17, y + 16);
            }
            else if (isSelectedOrPressed)
            {
                graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                graph.fillRect(x + 4, y + 3, 13, 15);
                graph.drawLine(x + 3, y + 4, x + 3, y + 16);
                graph.drawLine(x + 17, y + 4, x + 17, y + 16);
            }

            if (btModel.isArmed() && isEnabled)
            {
                graph.setColor(new Color(255, 255, 155, 127));
                graph.fillRect(x + 5, y + 5, 11, 11);
            }

            if (isSelectedOrPressed)
            {
                ic = UIManager.getIcon("MenuCheckBox.iconTick");
                ic.paintIcon(comp, graph, x, y);
            }

            graph.setColor(OSDarkLAFUtils.getRolloverColor());
            graph.fillRect(0, 0, 22, jMenuIt.getHeight());
            graph.setColor(OSDarkLAFUtils.getShadowColor());
            graph.drawLine(22, 0, 22, jMenuIt.getHeight());
            graph.setColor(OSDarkLAFUtils.getActiveColor());
            graph.drawLine(23, 0, 23, jMenuIt.getHeight());
        }
    }

    private static class ComboArrowIcon implements Icon, UIResource, Serializable
    {
        private int height;

        private int width;

        public ComboArrowIcon()
        {
            width = 15;
            height = 15;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final Icon ic = UIManager.getIcon("ComboBox.arrowIcon");
            width = ic.getIconWidth();
            height = ic.getIconHeight();

            ic.paintIcon(comp, graph, x, y);
            graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
            graph.drawLine(x + 2, y + 5, x + 7, y + 10);
            graph.drawLine(x + 7, y + 10, x + 12, y + 5);
            graph.drawLine(x + 2, y + 4, x + 7, y + 9);
            graph.drawLine(x + 7, y + 9, x + 12, y + 4);
        }
    }

    private static class FrameGenericIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final String sIcono;

        private final String sIconoR;

        private final String sIconoP;

        private final int width;

        public FrameGenericIcon(String icon, String iconR, String iconP)
        {
            width = 20;
            height = 20;
            sIcono = icon;
            sIconoR = iconR;
            sIconoP = iconP;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final ButtonModel model = ((JButton)comp).getModel();

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Icon ic = null;
            if (model.isPressed())
            {
                graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                graph2D.fillRoundRect(x, y, width, height, 4, 4);
                ic = UIManager.getIcon(sIconoP);
            }
            else if (model.isRollover())
            {
                ic = UIManager.getIcon(sIconoR);
            }
            else
            {
                ic = UIManager.getIcon(sIcono);
            }

            ic.paintIcon(comp, graph, x, y);
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    private static class RadioButtonIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public RadioButtonIcon()
        {
            width = 21;
            height = 21;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final JRadioButton jRadioBt = (JRadioButton)comp;
            final ButtonModel btModel = jRadioBt.getModel();

            final boolean isEnabled = btModel.isEnabled();
            final boolean isSelectedOrPressed = btModel.isSelected() || btModel.isPressed();

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graph2D.setColor(OpenSphereDarkLookAndFeel.getControl());
            graph2D.fillOval(x + 3, y + 3, 15, 15);

            Icon ic = UIManager.getIcon("RadioButton.iconBase");
            ic.paintIcon(comp, graph, x, y);

            if (isSelectedOrPressed)
            {
                graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                graph2D.fillOval(x + 3, y + 3, 15, 15);
            }

            if (btModel.isArmed() && isEnabled)
            {
                graph2D.setColor(new Color(255, 255, 155, 127));
                graph2D.fillOval(x + 5, y + 5, 11, 11);
            }

            if (isSelectedOrPressed)
            {
                ic = UIManager.getIcon("RadioButton.iconTick");
                ic.paintIcon(comp, graph, x, y);
            }

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    private static class RadioButtonMenuItemIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public RadioButtonMenuItemIcon()
        {
            width = 21;
            height = 0;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final JMenuItem jMuIt = (JMenuItem)comp;
            final ButtonModel btModel = jMuIt.getModel();

            x = 1;
            y = 0;
            final boolean isEnabled = btModel.isEnabled();
            final boolean isSelectedOrPressed = btModel.isSelected() || btModel.isPressed();

            Icon ic = UIManager.getIcon("MenuRadioButton.iconBase");
            ic.paintIcon(comp, graph, x, y);

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isEnabled)
            {
                graph2D.setColor(new Color(0, 0, 0, 63));
                graph2D.fillOval(x + 3, y + 3, 15, 15);
            }
            else if (isSelectedOrPressed)
            {
                graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                graph2D.fillOval(x + 3, y + 3, 15, 15);
            }

            if (isEnabled && btModel.isArmed())
            {
                graph2D.setColor(new Color(255, 255, 155, 127));
                graph2D.fillOval(x + 5, y + 5, 11, 11);
            }

            if (isSelectedOrPressed)
            {
                ic = UIManager.getIcon("MenuRadioButton.iconTick");
                ic.paintIcon(comp, graph, x, y);
            }

            graph.setColor(OSDarkLAFUtils.getRolloverColor());
            graph.fillRect(0, 0, 22, jMuIt.getHeight());
            graph.setColor(OSDarkLAFUtils.getShadowColor());
            graph.drawLine(22, 0, 22, jMuIt.getHeight());
            graph.setColor(OSDarkLAFUtils.getActiveColor());
            graph.drawLine(23, 0, 23, jMuIt.getHeight());

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    private static class ScrollBarEastButtonIcon implements Icon, UIResource, Serializable
    {
        private int height;

        private int width;

        public ScrollBarEastButtonIcon()
        {
            width = 15;
            height = 15;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final Icon icon = UIManager.getIcon("ScrollBar.eastButtonIconImage");
            height = icon.getIconHeight();
            width = icon.getIconWidth();

            icon.paintIcon(comp, graph, x, y);
            graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
            graph.drawLine(x + 5, y + 2, x + 10, y + 7);
            graph.drawLine(x + 10, y + 7, x + 5, y + 12);
            graph.drawLine(x + 6, y + 2, x + 11, y + 7);
            graph.drawLine(x + 11, y + 7, x + 6, y + 12);
        }
    }

    private static class ScrollBarNorthButtonIcon implements Icon, UIResource, Serializable
    {
        private int height;

        private int width;

        public ScrollBarNorthButtonIcon()
        {
            width = 15;
            height = 15;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final Icon ic = UIManager.getIcon("ScrollBar.northButtonIconImage");
            height = ic.getIconHeight();
            width = ic.getIconWidth();

            ic.paintIcon(comp, graph, x, y);

            graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
            graph.drawLine(x + 2, y + 8, x + 7, y + 3);
            graph.drawLine(x + 7, y + 3, x + 12, y + 8);
            graph.drawLine(x + 2, y + 9, x + 7, y + 4);
            graph.drawLine(x + 7, y + 4, x + 12, y + 9);
        }
    }

    private static class ScrollBarSouthButtonIcon implements Icon, UIResource, Serializable
    {
        private int height;

        private int width;

        public ScrollBarSouthButtonIcon()
        {
            width = 15;
            height = 15;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final Icon ic = UIManager.getIcon("ScrollBar.southButtonIconImage");
            height = ic.getIconHeight();
            width = ic.getIconWidth();
            ic.paintIcon(comp, graph, x, y);
            graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
            graph.drawLine(x + 2, y + 5, x + 7, y + 10);
            graph.drawLine(x + 7, y + 10, x + 12, y + 5);
            graph.drawLine(x + 2, y + 6, x + 7, y + 11);
            graph.drawLine(x + 7, y + 11, x + 12, y + 6);
        }
    }

    private static class ScrollBarWestButtonIcon implements Icon, UIResource, Serializable
    {
        private int height;

        private int width;

        public ScrollBarWestButtonIcon()
        {
            width = 15;
            height = 15;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final Icon ic = UIManager.getIcon("ScrollBar.westButtonIconImage");
            height = ic.getIconHeight();
            width = ic.getIconWidth();
            ic.paintIcon(comp, graph, x, y);
            graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
            graph.drawLine(x + 9, y + 2, x + 4, y + 7);
            graph.drawLine(x + 4, y + 7, x + 9, y + 12);
            graph.drawLine(x + 10, y + 2, x + 5, y + 7);
            graph.drawLine(x + 5, y + 7, x + 10, y + 12);
        }
    }

    private static class SliderHorizontalIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public SliderHorizontalIcon()
        {
            width = 19;
            height = 21;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (comp.hasFocus())
            {
                graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                graph2D.fillOval(x + 3, y + 2, 11, 17);
            }
            else if (!comp.isEnabled())
            {
                graph2D.setColor(Color.gray);
                graph2D.fillOval(x + 3, y + 2, 11, 17);
            }

            final Icon icono = UIManager.getIcon("Slider.horizontalThumbIconImage");
            icono.paintIcon(comp, graph, x, y);

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    private static class SliderVerticalIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public SliderVerticalIcon()
        {
            width = 21;
            height = 19;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (comp.hasFocus())
            {
                graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                graph2D.fillOval(x + 1, y + 4, 17, 11);
            }
            else if (!comp.isEnabled())
            {
                graph2D.setColor(Color.gray);
                graph2D.fillOval(x + 1, y + 4, 17, 11);
            }

            final Icon icono = UIManager.getIcon("Slider.verticalThumbIconImage");
            icono.paintIcon(comp, graph, x, y);

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }

    private static class SpinnerNextIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public SpinnerNextIcon()
        {
            width = 7;
            height = 5;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            graph.translate(x, y);

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!comp.isEnabled())
            {
                graph2D.setColor(OpenSphereDarkLookAndFeel.getInactiveControlTextColor());
            }
            else
            {
                final ButtonModel btModel = ((JButton)comp).getModel();
                if (btModel.isPressed())
                {
                    graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                }
                else
                {
                    graph2D.setColor(OpenSphereDarkLookAndFeel.getControlTextColor());
                }
            }

            graph2D.drawLine(1, 3, 3, 1);
            graph2D.drawLine(3, 1, 5, 3);
            graph2D.drawLine(1, 4, 3, 2);
            graph2D.drawLine(3, 2, 5, 4);
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            graph.translate(-x, -y);
        }
    }

    private static class SpinnerPreviousIcon implements Icon, UIResource, Serializable
    {
        private final int height;

        private final int width;

        public SpinnerPreviousIcon()
        {
            width = 7;
            height = 5;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            graph.translate(x, y);

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!comp.isEnabled())
            {
                graph2D.setColor(OpenSphereDarkLookAndFeel.getInactiveControlTextColor());
            }
            else
            {
                final ButtonModel btModel = ((JButton)comp).getModel();
                if (btModel.isPressed())
                {
                    graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
                }
                else
                {
                    graph2D.setColor(OpenSphereDarkLookAndFeel.getControlTextColor());
                }
            }

            graph2D.drawLine(1, 1, 3, 3);
            graph2D.drawLine(3, 3, 5, 1);
            graph2D.drawLine(1, 2, 3, 4);
            graph2D.drawLine(3, 4, 5, 2);

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            graph.translate(-x, -y);
        }
    }

    private static class TreeCollapsedIcon implements Icon, UIResource, Serializable
    {
        private final int width;

        private final int height;

        public TreeCollapsedIcon()
        {
            width = 18;
            height = 18;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            graph.translate(x, y);

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
            graph2D.fillOval(2, 2, 14, 14);

            graph2D.setColor(OpenSphereDarkLookAndFeel.getBlack());
            graph2D.drawLine(11, 11, 7, 7);
            graph2D.drawLine(11, 11, 7, 11);
            graph2D.drawLine(11, 11, 11, 7);

            final Icon icono = UIManager.getIcon("Tree.PickIcon");
            icono.paintIcon(comp, graph, 0, 0);

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            graph.translate(-x, -y);
        }
    }

    private static class TreeExpandedIcon implements Icon, UIResource, Serializable
    {
        private final int width;

        private final int height;

        public TreeExpandedIcon()
        {
            width = 18;
            height = 18;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public void paintIcon(Component comp, Graphics graph, int x, int y)
        {
            graph.translate(x, y);

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graph2D.setColor(OpenSphereDarkLookAndFeel.getFocusColor());
            graph2D.fillOval(2, 2, 14, 14);
            graph2D.setColor(OpenSphereDarkLookAndFeel.getBlack());
            graph2D.drawLine(10, 10, 6, 6);
            graph2D.drawLine(6, 6, 6, 10);
            graph2D.drawLine(6, 6, 10, 6);

            final Icon icono = UIManager.getIcon("Tree.PickIcon");
            icono.paintIcon(comp, graph, 0, 0);

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            graph.translate(-x, -y);
        }
    }

    public static Icon getBandaMenuItemIcon()
    {
        if (null == groupMenuItemIcon)
        {
            groupMenuItemIcon = new GroupMenuItemIcon();
        }
        return groupMenuItemIcon;
    }

    public static Icon getCheckBoxIcon()
    {
        if (null == checkBoxIcon)
        {
            checkBoxIcon = new CheckBoxIcon();
        }
        return checkBoxIcon;
    }

    public static Icon getCheckBoxMenuItemIcon()
    {
        if (null == checkBoxMenuItemIcon)
        {
            checkBoxMenuItemIcon = new CheckBoxMenuItemIcon();
        }
        return checkBoxMenuItemIcon;
    }

    public static Icon getComboArrowIcon()
    {
        if (null == comboArrowIcon)
        {
            comboArrowIcon = new ComboArrowIcon();
        }
        return comboArrowIcon;
    }

    public static Icon getFrameAltMaximizeIcon()
    {
        if (null == frameAltMaximizeIcon)
        {
            frameAltMaximizeIcon = new FrameGenericIcon("InternalFrame.OSDarkLAFResizeIcon",
                    "InternalFrame.OSDarkLAFResizeIconRoll", "InternalFrame.OSDarkLAFResizeIconPush");
        }
        return frameAltMaximizeIcon;
    }

    public static Icon getFrameCloseIcon()
    {
        if (null == frameCloseIcon)
        {
            frameCloseIcon = new FrameGenericIcon("InternalFrame.OSDarkLAFCloseIcon", "InternalFrame.OSDarkLAFCloseIconRoll",
                    "InternalFrame.OSDarkLAFCloseIconPush");
        }
        return frameCloseIcon;
    }

    public static Icon getFrameMaxIcon()
    {
        if (null == frameMaxIcon)
        {
            frameMaxIcon = new FrameGenericIcon("InternalFrame.OSDarkLAFMaxIcon", "InternalFrame.OSDarkLAFMaxIconRoll",
                    "InternalFrame.OSDarkLAFMaxIconPush");
        }
        return frameMaxIcon;
    }

    public static Icon getFrameMinIcon()
    {
        if (null == frameMinIcon)
        {
            frameMinIcon = new FrameGenericIcon("InternalFrame.OSDarkLAFMinIcon", "InternalFrame.OSDarkLAFMinIconRoll",
                    "InternalFrame.OSDarkLAFMinIconPush");
        }
        return frameMinIcon;
    }

    public static Icon getRadioButtonIcon()
    {
        if (null == radioButtonIcon)
        {
            radioButtonIcon = new RadioButtonIcon();
        }
        return radioButtonIcon;
    }

    public static Icon getRadioButtonMenuItemIcon()
    {
        if (null == radioButtonMenuItemIcon)
        {
            radioButtonMenuItemIcon = new RadioButtonMenuItemIcon();
        }
        return radioButtonMenuItemIcon;
    }

    public static Icon getScrollBarEastButtonIcon()
    {
        if (null == scrollEastIcon)
        {
            scrollEastIcon = new ScrollBarEastButtonIcon();
        }
        return scrollEastIcon;
    }

    public static Icon getScrollBarNorthButtonIcon()
    {
        if (null == scrollNorthIcon)
        {
            scrollNorthIcon = new ScrollBarNorthButtonIcon();
        }
        return scrollNorthIcon;
    }

    public static Icon getScrollBarSouthButtonIcon()
    {
        if (null == scrollSouthIcon)
        {
            scrollSouthIcon = new ScrollBarSouthButtonIcon();
        }
        return scrollSouthIcon;
    }

    public static Icon getScrollBarWestButtonIcon()
    {
        if (null == scrollWestIcon)
        {
            scrollWestIcon = new ScrollBarWestButtonIcon();
        }
        return scrollWestIcon;
    }

    public static Icon getSliderHorizontalIcon()
    {
        if (null == sliderHorizIcon)
        {
            sliderHorizIcon = new SliderHorizontalIcon();
        }
        return sliderHorizIcon;
    }

    public static Icon getSliderVerticalIcon()
    {
        if (null == sliderVertIcon)
        {
            sliderVertIcon = new SliderVerticalIcon();
        }
        return sliderVertIcon;
    }

    public static Icon getSpinnerNextIcon()
    {
        if (null == spinnerNextIcon)
        {
            spinnerNextIcon = new SpinnerNextIcon();
        }
        return spinnerNextIcon;
    }

    public static Icon getSpinnerPreviousIcon()
    {
        if (null == spinnerPreviousIcon)
        {
            spinnerPreviousIcon = new SpinnerPreviousIcon();
        }
        return spinnerPreviousIcon;
    }

    public static Icon getTreeCollapsedIcon()
    {
        if (null == treeCollapsedIcon)
        {
            treeCollapsedIcon = new TreeCollapsedIcon();
        }
        return treeCollapsedIcon;
    }

    public static Icon getTreeExpandedIcon()
    {
        if (null == treeExpandedIcon)
        {
            treeExpandedIcon = new TreeExpandedIcon();
        }
        return treeExpandedIcon;
    }
}
