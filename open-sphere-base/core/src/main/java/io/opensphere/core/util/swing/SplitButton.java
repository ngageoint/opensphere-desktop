package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;

/**
 * An implementation of a split button that uses a JButton under the hood.
 */
public class SplitButton extends IconButton
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * If the main part and drop-down part of the button have different
     * functionality.
     */
    private final boolean myHasMultipleFunctions;

    /** The width of the region which activates the drop menu. */
    private int myDropRegionWidth = 14;

    /** The dynamic menu items. */
    private final List<Component> myDynamicMenuItems = New.list();

    /** The static menu items. */
    private final List<Component> myStaticMenuItems = New.list();

    /** True if the button press was over the menu, false otherwise. */
    private boolean myIsMenuPress;

    /** The popup menu. */
    private final JPopupMenu myMenu = new JPopupMenu();

    /** This is true when the mouse is over the button (not the menu). */
    private boolean myMouseOver;

    /** When true, do not open the menu when the action is fired. */
    private boolean mySkipNextMenu;

    /**
     * Instantiates a new split button. In this case, pressing the main part
     * button will have a different effect than pressing the drop-down part of
     * the button.
     *
     * @param text the text
     * @param icon the icon
     */
    public SplitButton(String text, Icon icon)
    {
        this(text, icon, true);
    }

    /**
     * Instantiates a new split button with the option to have the main part of
     * the button do something different than the drop-down part of the button.
     *
     * @param text the text
     * @param icon the icon
     * @param hasMultipleFunctions if the main part and drop-down part of the
     *            button have different functionality
     */
    public SplitButton(String text, Icon icon, boolean hasMultipleFunctions)
    {
        super(text, icon);
        myHasMultipleFunctions = hasMultipleFunctions;

        addExtraSpace();

        // Listen to mouse events to determine where the user clicked
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                myMouseOver = true;
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                myMouseOver = false;
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                myIsMenuPress = myHasMultipleFunctions && e.getX() >= getWidth() - myDropRegionWidth || !myHasMultipleFunctions;
            }
        });

        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                // If escape is used to cancel the menu when the mouse is over
                // the button, do not skip the next show of the menu.
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    mySkipNextMenu = false;
                }
            }
        });

        myMenu.addPopupMenuListener(new PopupMenuListener()
        {
            @Override
            public void popupMenuCanceled(PopupMenuEvent e)
            {
                if (myMouseOver)
                {
                    mySkipNextMenu = true;
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
            }
        });
    }

    @Override
    public Component add(Component comp)
    {
        return getMenu().add(comp);
    }

    /**
     * Adds the item.
     *
     * @param pMenuItem the menuItem to add to the popup.
     */
    public void addMenuItem(JMenuItem pMenuItem)
    {
        pMenuItem.setFocusable(false);
        myMenu.add(pMenuItem);
        myStaticMenuItems.add(pMenuItem);
    }

    /**
     * Adds a separator to the menu.
     */
    public void addSeparator()
    {
        myMenu.addSeparator();
    }

    /**
     * Get the dropRegionWidth.
     *
     * @return the dropRegionWidth
     */
    public int getDropRegionWidth()
    {
        return myDropRegionWidth;
    }

    /**
     * Gets the popup menu.
     *
     * @return the popup menu
     */
    public JPopupMenu getMenu()
    {
        return myMenu;
    }

    @Override
    public void remove(Component comp)
    {
        if (comp != null)
        {
            getMenu().remove(comp);
        }
    }

    @Override
    public void removeAll()
    {
        getMenu().removeAll();
    }

    /**
     * Set the dropRegionWidth.
     *
     * @param dropRegionWidth the dropRegionWidth to set
     */
    public void setDropRegionWidth(int dropRegionWidth)
    {
        myDropRegionWidth = dropRegionWidth;
    }

    /**
     * Toggles the popup menu visibility.
     */
    public void toggleDropComponentVisibility()
    {
        List<Component> newDynamicMenuItems = getDynamicMenuItems();

        // determine if there are/were dynamic menu items
        if (!myDynamicMenuItems.isEmpty() || !newDynamicMenuItems.isEmpty())
        {
            // clear the menu completely
            myMenu.removeAll();
            myDynamicMenuItems.clear();

            // determine if there are dynamic items to add to the menu
            if (!newDynamicMenuItems.isEmpty())
            {
                myDynamicMenuItems.addAll(newDynamicMenuItems);
                myMenu.add(new JLabel(getDynamicMenuItemsLabel()));
                myDynamicMenuItems.forEach(item -> myMenu.add(item));
                myMenu.addSeparator();
            }

            // add static menu items back either below the dynamic menu items or
            // alone if there were no dynamic menu items to add
            myStaticMenuItems.forEach(item -> myMenu.add(item));
        }

        boolean empty = myMenu.getComponentCount() == 0;
        if (empty)
        {
            myMenu.add(new JMenuItem());
        }

        myMenu.show(this, 0, getHeight());
        if (empty)
        {
            myMenu.removeAll();
        }
    }

    /**
     * Since ActionEvent is fired before mouseClicked(), but we need
     * mouseClicked() to determine whether to fire the ActionEvent, save off the
     * ActionEvent to be possibly fired later.
     *
     * @param event the action event
     */
    @Override
    protected void fireActionPerformed(ActionEvent event)
    {
        if (mySkipNextMenu)
        {
            mySkipNextMenu = false;
            return;
        }
        if (myIsMenuPress)
        {
            toggleDropComponentVisibility();
        }
        else
        {
            super.fireActionPerformed(event);
        }
    }

    /**
     * Gets the dynamic menu items. Exists to be overwritten if dynamic menu
     * items are needed
     *
     * @return the dynamic menu items
     */
    protected List<Component> getDynamicMenuItems()
    {
        return Collections.emptyList();
    }

    /**
     * Gets the dynamic menu items' label. Exists to be overwritten if dynamic
     * menu items are needed
     *
     * @return the dynamic menu items' label
     */
    protected String getDynamicMenuItemsLabel()
    {
        return null;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        int margin = 3;
        int triWidth = myDropRegionWidth / 2;
        int triHeight = (triWidth + 1) / 2;

        Graphics2D g2d = (Graphics2D)g;
        int lineX = getWidth() - myDropRegionWidth - 1;

        // Draw vertical line
        g2d.setColor(Colors.LF_SECONDARY1);
        g2d.drawLine(lineX, 1, lineX, getHeight() - 1);

        // Draw triangle
        int x1 = lineX + margin + 1;
        int x2 = x1 + triWidth;
        int x3 = x1 + (triWidth - 1) / 2;
        int y1 = getHeight() / 2;
        int y2 = y1;
        int y3 = y1 + triHeight;
        g2d.setColor(isEnabled() ? Color.WHITE : Color.GRAY);
        g2d.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
    }

    @Override
    protected void sizeToFit()
    {
        super.sizeToFit();
        addExtraSpace();
    }

    /**
     * Adds extra margin and size to fit the extra space of the dropdown
     * section.
     */
    private void addExtraSpace()
    {
        Insets margin = getMargin();
        margin.right += myDropRegionWidth;
        setMargin(margin);
        Dimension size = new Dimension(getMaximumSize().width + myDropRegionWidth, getMaximumSize().height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }
}
