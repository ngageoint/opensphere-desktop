package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * This class will build a {@link JButton} with a text label or an icon that
 * will spawn a {@link JPopupMenu} when clicked. Use the addItem method to add
 * items to the {@link JPopupMenu} and implement the item actions in the parent
 * class.
 */
public class OptionsMenu extends JButton
{
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The search options popup menu. */
    private final JPopupMenu mySearchOptionsPopupMenu;

    /** The orientation. */
    private int myOrientation = -1;

    /** The X display position. */
    private int myXDisplayPos;

    /** The X offset. */
    private int myXOffset;

    /** The Y display position. */
    private int myYDisplayPos;

    /** The default press action. */
    private final Action myPressAction;

    /**
     * CTOR.
     *
     * @param icon - the icon to apply to this button
     * @param pOrientation - 0:popupmenu is aligned with left edge of the button
     *            1:right edge of popupmenu is aligned with left edge of the
     *            button
     */
    public OptionsMenu(Icon icon, int pOrientation)
    {
        this("", icon);
        myOrientation = pOrientation;
        setMargin(new Insets(2, 2, 2, 2));
    }

    /**
     * Instantiates a new options menu.
     *
     * @param icon the icon
     * @param pOrientation the orientation
     * @param xOffset the x offset
     */
    public OptionsMenu(Icon icon, int pOrientation, int xOffset)
    {
        this("", icon);
        myOrientation = pOrientation;
        myXOffset = xOffset;
        setMargin(new Insets(2, 2, 2, 2));
    }

    /**
     * Instantiates a new options menu.
     *
     * @param text the text
     * @param icon the icon
     * @param pOrientation the orientation
     * @param xOffset the x offset
     */
    public OptionsMenu(String text, Icon icon, int pOrientation, int xOffset)
    {
        this(text, icon);
        myOrientation = pOrientation;
        myXOffset = xOffset;
        setMargin(new Insets(2, 2, 2, 2));
    }

    /**
     * CTOR.
     *
     * @param text - the label for this button
     * @param pOrientation - 0:popupmenu is aligned with left edge of the button
     *            1:right edge of popupmenu is aligned with left edge of the
     *            button
     */
    public OptionsMenu(String text, int pOrientation)
    {
        this(text, null);
        myOrientation = pOrientation;
        setMargin(new Insets(2, 4, 2, 4));
    }

    /**
     * Instantiates a new options menu.
     *
     * @param text the text
     * @param icon the icon
     */
    @SuppressWarnings("rawtypes")
    private OptionsMenu(String text, Icon icon)
    {
        super(text, icon);
        myPressAction = new AbstractAction(text)
        {
            /**
             * Serial version UID.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt)
            {
                toggleVisibility();
            }
        };
        myPressAction.putValue(Action.SMALL_ICON, icon);
        setAction(myPressAction);
        setFocusable(false);
        // this is a trick to get hold of the client prop which
        // prevents closing of the popup
        JComboBox box = new JComboBox();
        Object preventHide = box.getClientProperty("doNotCancelPopup");
        putClientProperty("doNotCancelPopup", preventHide);
        mySearchOptionsPopupMenu = new JPopupMenu();
    }

    @Override
    public Component add(Component pComp)
    {
        return mySearchOptionsPopupMenu.add(pComp);
    }

    /**
     * Adds the item.
     *
     * @param pItem the menuItem to add to the popup.
     */
    public void addItem(JMenuItem pItem)
    {
        pItem.setFocusable(false);
        mySearchOptionsPopupMenu.add(pItem);
    }

    @Override
    public Component getComponent(int pIndex)
    {
        return mySearchOptionsPopupMenu.getComponent(pIndex);
    }

    @Override
    public Component[] getComponents()
    {
        return mySearchOptionsPopupMenu.getComponents();
    }

    /**
     * Insert item.
     *
     * @param pItem the item
     * @param pIndex the index
     */
    public void insertItem(JMenuItem pItem, int pIndex)
    {
        pItem.setFocusable(false);
        mySearchOptionsPopupMenu.insert(pItem, pIndex);
    }

    @Override
    public void remove(Component pComponent)
    {
        if (pComponent != null)
        {
            mySearchOptionsPopupMenu.remove(pComponent);
        }
    }

    @Override
    public void removeAll()
    {
        mySearchOptionsPopupMenu.removeAll();
    }

    /**
     * Resets the action to the default action.
     */
    public void resetAction()
    {
        setAction(myPressAction);
    }

    /**
     * Toggle visibility.
     */
    public void toggleVisibility()
    {
        if (mySearchOptionsPopupMenu.isVisible())
        {
            mySearchOptionsPopupMenu.setVisible(false);
        }
        else if (mySearchOptionsPopupMenu.getComponentCount() > 0)
        {
            Dimension buttonSize = getSize();

            if (myOrientation == 0)
            {
                myXDisplayPos = 0 + myXOffset;
                myYDisplayPos = buttonSize.height + 4;
            }
            else
            {
                myXDisplayPos = buttonSize.width - mySearchOptionsPopupMenu.getPreferredSize().width + myXOffset;
                myYDisplayPos = buttonSize.height;
            }
            mySearchOptionsPopupMenu.show(this, myXDisplayPos, myYDisplayPos);
        }
    }
}
