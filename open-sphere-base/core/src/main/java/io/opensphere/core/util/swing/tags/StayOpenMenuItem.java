package io.opensphere.core.util.swing.tags;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

/**
 * A menu item that stays open until the user clicks off (unlike standard menu
 * items that close their parent menu when the user clicks an item).
 */
public class StayOpenMenuItem extends JMenuItem
{
    /** The unique identifier used for serialization. */
    private static final long serialVersionUID = -7852138697396040802L;

    /** The path of the items within the menu. */
    private MenuElement[] path;

    /**
     * Creates a new menu item.
     *
     * @see JMenuItem#JMenuItem()
     */
    public StayOpenMenuItem()
    {
        super();
        initializeListener();
    }

    /**
     * Creates a new menu item.
     *
     * @param a the action of the <code>JMenuItem</code>
     * @see JMenuItem#JMenuItem(javax.swing.Action)
     */
    public StayOpenMenuItem(Action a)
    {
        super(a);
        initializeListener();
    }

    /**
     * Creates a new menu item.
     *
     * @param icon the icon of the <code>JMenuItem</code>
     * @see JMenuItem#JMenuItem(javax.swing.Icon)
     */
    public StayOpenMenuItem(Icon icon)
    {
        super(icon);
        initializeListener();
    }

    /**
     * Creates a new menu item.
     *
     * @param text the text of the <code>JMenuItem</code>
     * @see JMenuItem#JMenuItem(java.lang.String)
     */
    public StayOpenMenuItem(String text)
    {
        super(text);
        initializeListener();
    }

    /**
     * Creates a new menu item.
     *
     * @param text the text of the <code>JMenuItem</code>
     * @param icon the icon of the <code>JMenuItem</code>
     * @see JMenuItem#JMenuItem(java.lang.String, javax.swing.Icon)
     */
    public StayOpenMenuItem(String text, Icon icon)
    {
        super(text, icon);
        initializeListener();
    }

    /**
     * Creates a new menu item.
     *
     * @param text the text of the <code>JMenuItem</code>
     * @param mnemonic the keyboard mnemonic for the <code>JMenuItem</code>
     * @see JMenuItem#JMenuItem(java.lang.String, int)
     */
    public StayOpenMenuItem(String text, int mnemonic)
    {
        super(text, mnemonic);
        initializeListener();
    }

    /** Initializes a change listener to keep the menu showing. */
    private void initializeListener()
    {
        getModel().addChangeListener(e ->
        {
            if (getModel().isArmed() && isShowing())
            {
                path = MenuSelectionManager.defaultManager().getSelectedPath();
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.JMenuItem#menuSelectionChanged(boolean)
     */
    @Override
    public void menuSelectionChanged(boolean isIncluded)
    {
        super.menuSelectionChanged(isIncluded);
        if (!isIncluded)
        {
            path = MenuSelectionManager.defaultManager().getSelectedPath();
        }
    }

    /**
     * Overridden to reopen the menu.
     *
     * @param pressTime the time to "hold down" the button, in milliseconds
     */
    @Override
    public void doClick(int pressTime)
    {
        super.doClick(pressTime);
        JPopupMenu popup = (JPopupMenu)path[0];
        if (!popup.isShowing())
        {
            popup.setVisible(true);
        }
        MenuSelectionManager.defaultManager().setSelectedPath(path);
    }
}
