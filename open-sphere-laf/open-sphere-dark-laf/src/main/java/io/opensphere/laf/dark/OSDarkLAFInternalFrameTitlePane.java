package io.opensphere.laf.dark;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.accessibility.AccessibleContext;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.metal.MetalInternalFrameTitlePane;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

/**
 * The OpenSphere Dark Look and Feel component for titled internal frames area.
 */
public class OSDarkLAFInternalFrameTitlePane extends MetalInternalFrameTitlePane
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -1543371220136198733L;

    /**
     * The constant in which the setting for controlling the poppable behavior
     * is stored.
     */
    private static final String IS_POPPABLE_KEY = "AbstractInternalFrame.isPoppable";

    /**
     * The constant in which the setting for controlling the rollable behavior
     * is stored.
     */
    private static final String IS_ROLLABLE_KEY = "AbstractInternalFrame.isRollable";

    private static final String ROLL_PROPERTY = "frameRolledUp";

    private static final String POPPED_PROPERTY = "framePopped";

    private MyCustomMouseListener miml;

    private Icon resizeIcon;

    private Icon antIcon;

    private final int litWidth = UIManager.getInt("OSDarkLAFInternalFrameIconLit.width");

    private final int litHeight = UIManager.getInt("OSDarkLAFInternalFrameIconLit.height");

    public OSDarkLAFInternalFrameTitlePane(JInternalFrame fr)
    {
        super(fr);

        maxButton.setBorderPainted(false);
        maxButton.setFocusPainted(false);
        maxButton.setOpaque(false);

        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setOpaque(false);

        iconButton.setBorderPainted(false);
        iconButton.setOpaque(false);
        iconButton.setFocusPainted(false);

        myPopButton.setBorderPainted(false);
        myPopButton.setOpaque(false);
        myPopButton.setFocusPainted(false);

        myRollupButton.setBorderPainted(false);
        myRollupButton.setOpaque(false);
        myRollupButton.setFocusPainted(false);

        final CustomMouseListener2 hackML = new CustomMouseListener2();
        maxButton.addMouseListener(hackML);
        closeButton.addMouseListener(hackML);
        iconButton.addMouseListener(hackML);
        myPopButton.addMouseListener(hackML);
        myRollupButton.addMouseListener(hackML);
    }

    /**
     * Gets an Icon for the given URL.
     *
     * @param iconUrl the icon URL.
     * @return the Icon
     */
    private static Icon getIcon(String iconUrl)
    {
        Icon icon = null;
        try
        {
            icon = new ImageIcon(ImageIO.read(OSDarkLAFInternalFrameTitlePane.class.getResource(iconUrl)));
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return icon;
    }

    private class NoFocusButton extends JButton
    {
        private String uiKey;

        public NoFocusButton(String uiKey, String opacityKey)
        {
            setFocusPainted(false);
            setMargin(new Insets(0, 0, 0, 0));
            this.uiKey = uiKey;

            final Object opacity = UIManager.get(opacityKey);
            if (opacity instanceof Boolean)
            {
                setOpaque(((Boolean)opacity).booleanValue());
            }
        }

        @Override
        public boolean isFocusTraversable()
        {
            return false;
        }

        @Override
        public void requestFocus()
        {
        }

        @Override
        public AccessibleContext getAccessibleContext()
        {
            final AccessibleContext ac = super.getAccessibleContext();
            if (uiKey != null)
            {
                ac.setAccessibleName(UIManager.getString(uiKey));
                uiKey = null;
            }
            return ac;
        }
    }

    int buttonsWidth;

    private JButton myPopButton;

    private JButton myRollupButton;

    private static final Border handyEmptyBorder = new EmptyBorder(0, 0, 0, 0);

    @Override
    protected void createButtons()
    {
        super.createButtons();

        final Boolean paintActive = frame.isSelected() ? Boolean.TRUE : Boolean.FALSE;

        // button to pop off frames
        final Icon popIcon = getIcon("/icons/FrameResize.png");
        final Icon popRolloverIcon = getIcon("/icons/FrameResizeRoll.png");
        final Icon popPressIcon = getIcon("/icons/FrameResizePush.png");

        myPopButton = new NoFocusButton("InternalFrameTitlePane.popButtonAccessibleName",
                "InternalFrameTitlePane.popButtonOpacity");
        myPopButton.setIcon(popIcon);
        myPopButton.setRolloverIcon(popRolloverIcon);
        myPopButton.setPressedIcon(popPressIcon);

        myPopButton.putClientProperty("paintActive", paintActive);
        myPopButton.setBorder(handyEmptyBorder);

        myPopButton.addActionListener(new PopAction());

        myPopButton.setToolTipText("Pop frame out of HUD");

        // button to roll up frames.
        myRollupButton = new NoFocusButton("InternalFrameTitlePane.rollupButtonAccessibleName",
                "InternalFrameTitlePane.rollupButtonOpacity");
        final Icon rollupIcon = getIcon("/icons/FrameRollup.png");
        final Icon rollupRolloverIcon = getIcon("/icons/FrameRollupRoll.png");
        final Icon rollupPressIcon = getIcon("/icons/FrameRollupPush.png");
        myRollupButton.setIcon(rollupIcon);
        myRollupButton.setRolloverIcon(rollupRolloverIcon);
        myRollupButton.setPressedIcon(rollupPressIcon);

        myRollupButton.putClientProperty("paintActive", paintActive);
        myRollupButton.setBorder(handyEmptyBorder);

        myRollupButton.addActionListener(new RollupAction());

        myRollupButton.setToolTipText("Roll up frame");

        if (MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme)
        {
            myPopButton.setContentAreaFilled(false);
            myRollupButton.setContentAreaFilled(false);
        }
    }

    public class PopAction extends AbstractAction
    {
        public PopAction()
        {
            super(UIManager.getString("InternalFrameTitlePane.popButtonText"));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            frame.firePropertyChange(POPPED_PROPERTY, false, true);
        }
    }

    public class RollupAction extends AbstractAction
    {
        public RollupAction()
        {
            super(UIManager.getString("InternalFrameTitlePane.rollupButtonText"));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final Boolean prop = (Boolean)frame.getClientProperty(ROLL_PROPERTY);
            if (prop != null)
            {
                final boolean oldValue = prop.booleanValue();
                frame.putClientProperty(ROLL_PROPERTY, Boolean.valueOf(!oldValue));
            }
            else
            {
                frame.putClientProperty(ROLL_PROPERTY, Boolean.TRUE);
            }
        }
    }

    @Override
    protected void addSubComponents()
    {
        super.addSubComponents();
        if (Boolean.TRUE.equals(frame.getClientProperty(IS_ROLLABLE_KEY)))
        {
            add(myRollupButton);
        }
        if (Boolean.TRUE.equals(frame.getClientProperty(IS_POPPABLE_KEY)))
        {
            add(myPopButton);
        }
    }

    @Override
    protected LayoutManager createLayout()
    {
        return new MetalTitlePaneLayout();
    }

    class MetalTitlePaneLayout extends TitlePaneLayout
    {
        @Override
        public void addLayoutComponent(String name, Component c)
        {
        }

        @Override
        public void removeLayoutComponent(Component c)
        {
        }

        @Override
        public Dimension preferredLayoutSize(Container c)
        {
            return minimumLayoutSize(c);
        }

        @Override
        public Dimension minimumLayoutSize(Container c)
        {
            // Compute width.
            int width = 30;
            if (frame.isClosable())
            {
                width += 21;
            }
            if (frame.isMaximizable())
            {
                width += 16 + (frame.isClosable() ? 10 : 4);
            }
            if (frame.isIconifiable())
            {
                width += 16 + (frame.isMaximizable() ? 2 : frame.isClosable() ? 10 : 4);
            }

            // (16 for the rollup) + (2 for gap) + (16 for pop) + (gap next
            // button or edge)
            width += 18 + 16 + (frame.isMaximizable() || frame.isIconifiable() ? 2 : frame.isClosable() ? 10 : 4);

            final FontMetrics fm = frame.getFontMetrics(getFont());
            final String frameTitle = frame.getTitle();
            final int title_w = frameTitle != null ? fm.stringWidth(frameTitle) : 0;
            final int title_length = frameTitle != null ? frameTitle.length() : 0;

            if (title_length > 2)
            {
                final int subtitle_w = fm.stringWidth(frame.getTitle().substring(0, 2) + "...");
                width += title_w < subtitle_w ? title_w : subtitle_w;
            }
            else
            {
                width += title_w;
            }

            // Compute height.
            int height;
            if (isPalette)
            {
                height = paletteTitleHeight;
            }
            else
            {
                int fontHeight = fm.getHeight();
                fontHeight += 7;
                final Icon icon = frame.getFrameIcon();
                int iconHeight = 0;
                if (icon != null)
                {
                    // SystemMenuBar forces the icon to be 16x16 or less.
                    iconHeight = Math.min(icon.getIconHeight(), 16);
                }
                iconHeight += 5;
                height = Math.max(fontHeight, iconHeight);
            }

            return new Dimension(width, height);
        }

        @Override
        public void layoutContainer(Container c)
        {
            final boolean leftToRight = frame.getComponentOrientation().isLeftToRight();

            final int w = getWidth();
            int x = leftToRight ? w : 0;
            final int y = 2;
            int spacing;

            // assumes all buttons have the same dimensions
            // these dimensions include the borders
            final int buttonHeight = closeButton.getIcon().getIconHeight();
            final int buttonWidth = closeButton.getIcon().getIconWidth();

            if (frame.isClosable())
            {
                if (isPalette)
                {
                    spacing = 3;
                    x += leftToRight ? -spacing - (buttonWidth + 2) : spacing;
                    closeButton.setBounds(x, y, buttonWidth + 2, getHeight() - 4);
                    if (!leftToRight)
                    {
                        x += buttonWidth + 2;
                    }
                }
                else
                {
                    spacing = 4;
                    x += leftToRight ? -spacing - buttonWidth : spacing;
                    closeButton.setBounds(x, y, buttonWidth, buttonHeight);
                    if (!leftToRight)
                    {
                        x += buttonWidth;
                    }
                }
            }

            if (frame.isMaximizable() && !isPalette)
            {
                spacing = frame.isClosable() ? 10 : 4;
                x += leftToRight ? -spacing - buttonWidth : spacing;
                maxButton.setBounds(x, y, buttonWidth, buttonHeight);
                if (!leftToRight)
                {
                    x += buttonWidth;
                }
            }

            if (frame.isIconifiable() && !isPalette)
            {
                spacing = frame.isMaximizable() ? 2 : frame.isClosable() ? 10 : 4;
                x += leftToRight ? -spacing - buttonWidth : spacing;
                iconButton.setBounds(x, y, buttonWidth, buttonHeight);
                if (!leftToRight)
                {
                    x += buttonWidth;
                }
            }

            // pop button
            spacing = frame.isIconifiable() || frame.isMaximizable() ? 2 : frame.isClosable() ? 10 : 4;
            x += leftToRight ? -spacing - buttonWidth : spacing;
            myPopButton.setBounds(x, y, buttonWidth, buttonHeight);
            if (!leftToRight)
            {
                x += buttonWidth;
            }

            // rollup button
            spacing = 2;
            x += leftToRight ? -spacing - buttonWidth : spacing;
            myRollupButton.setBounds(x, y, buttonWidth, buttonHeight);
            if (!leftToRight)
            {
                x += buttonWidth;
            }

            buttonsWidth = leftToRight ? w - x : x;
        }
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener()
    {
        return new OSDarkLAFPropertyChangeHandler();
    }

    @Override
    public void paintComponent(Graphics graph)
    {
        final Graphics2D graph2D = (Graphics2D)graph.create();
        final int w = getWidth();
        final int h = getHeight();

        graph2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OSDarkLAFUtils.getFrameOpacityFloat()));

        final GradientPaint gradientPaint = frame.isSelected()
                ? new GradientPaint(0, 0, OpenSphereDarkLookAndFeel.getPrimaryControlDarkShadow(), w, 0,
                        OpenSphereDarkLookAndFeel.getPrimaryControl())
                : new GradientPaint(0, 0, OpenSphereDarkLookAndFeel.getControl(), w, 0,
                        OpenSphereDarkLookAndFeel.getControlDarkShadow());

        graph2D.setPaint(gradientPaint);
        graph2D.fillRect(0, 1, w, h);
        graph2D.dispose();

        int xOff = frame.getComponentOrientation().isLeftToRight() ? 5 : w - 5;

        if (frame.getFrameIcon() != antIcon)
        {
            final int tall = litHeight > h ? h - 2 : litHeight;
            final int wide = litHeight > h ? tall : litWidth;

            antIcon = frame.getFrameIcon();
            resizeIcon = OSDarkLAFUtils.rescaleIcon(antIcon, tall, wide);
        }

        if (null != resizeIcon)
        {
            final int iconY = h / 2 - resizeIcon.getIconHeight() / 2;
            resizeIcon.paintIcon(frame, graph, xOff, iconY);
            xOff += 5 + resizeIcon.getIconWidth();
        }

        String title = frame.getTitle();
        if (null != title)
        {
            final Font aFont = getFont();
            graph.setFont(aFont);
            final FontMetrics fMetrics = getFontMetrics(aFont);
            final int yOff = (h - fMetrics.getHeight()) / 2 + fMetrics.getAscent();

            int length = myRollupButton.getBounds().x;

            length = length - xOff - getInsets().left;
            title = getTitle(title, fMetrics, length);

            if (frame.isSelected())
            {
                OSDarkLAFUtils.paintShadowTitleWide(graph, title, xOff, yOff, Color.white, null);
            }
            else
            {
                OSDarkLAFUtils.paintShadowTitleWide(graph, title, xOff, yOff, OpenSphereDarkLookAndFeel.getControlDisabled(),
                        null);
            }
        }
    }

    @Override
    public void installListeners()
    {
        super.installListeners();

        miml = new MyCustomMouseListener();
        addMouseMotionListener(miml);
        addMouseListener(miml);
        frame.addMouseMotionListener(miml);
        frame.addMouseListener(miml);
    }

    @Override
    protected void uninstallListeners()
    {
        super.uninstallListeners();
        removeMouseMotionListener(miml);
        removeMouseListener(miml);
        frame.removeMouseMotionListener(miml);
        frame.removeMouseListener(miml);
        miml = null;
    }

    class OSDarkLAFPropertyChangeHandler extends BasicInternalFrameTitlePane.PropertyChangeHandler
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            final String propertyValue = evt.getPropertyName();

            if (propertyValue.equals(JInternalFrame.IS_SELECTED_PROPERTY))
            {
                final Boolean paintActive = (Boolean)evt.getNewValue();
                iconButton.putClientProperty("paintActive", paintActive);
                maxButton.putClientProperty("paintActive", paintActive);
                closeButton.putClientProperty("paintActive", paintActive);
            }
            else if ("JInternalFrame.messageType".equals(propertyValue))
            {
                frame.repaint();
            }
            else if ("icon".equals(propertyValue))
            {
                closeButton.getModel().setRollover(false);
                iconButton.getModel().setRollover(false);
                maxButton.getModel().setRollover(false);

                ((OSDarkLAFDesktopIconUI)frame.getDesktopIcon().getUI()).iconHasFocus = false;
            }
            else if ("frameIcon".equals(propertyValue))
            {
                if (null != frame.getDesktopPane())
                {
                    frame.getDesktopPane().updateUI();
                }
            }

            super.propertyChange(evt);
        }
    }

    private class MyCustomMouseListener extends MouseInputAdapter
    {
        Insets insets = frame.getBorder().getBorderInsets(frame);

        void procEvent(MouseEvent e)
        {
            if (e.getComponent() instanceof OSDarkLAFInternalFrameTitlePane)
            {
                if (null != frame.getDesktopPane())
                {
                    frame.getDesktopPane().updateUI();
                }

                // TODO why do I only get mouse pressed and not mouse clicked?
                if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
                {
                    final Boolean prop = (Boolean)frame.getClientProperty(ROLL_PROPERTY);
                    if (prop != null)
                    {
                        final boolean oldValue = prop.booleanValue();
                        frame.putClientProperty(ROLL_PROPERTY, Boolean.valueOf(!oldValue));
                    }
                    else
                    {
                        frame.putClientProperty(ROLL_PROPERTY, Boolean.TRUE);
                    }
                }
            }
            else
            {
                final int x = e.getX();
                final int y = e.getY();
                final int w = frame.getWidth();
                final int h = frame.getHeight();

                if (x <= 5 || x >= w - insets.right || y >= h - insets.bottom)
                {
                    if (null != frame.getDesktopPane())
                    {
                        frame.getDesktopPane().updateUI();
                    }
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            procEvent(e);
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            procEvent(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            procEvent(e);
        }
    }

    private class CustomMouseListener2 extends MouseInputAdapter
    {
        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (null != frame.getDesktopPane())
            {
                frame.getDesktopPane().updateUI();
            }
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            if (null != frame.getDesktopPane())
            {
                frame.getDesktopPane().updateUI();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            if (null != frame.getDesktopPane())
            {
                frame.getDesktopPane().updateUI();
            }
        }
    }
}
