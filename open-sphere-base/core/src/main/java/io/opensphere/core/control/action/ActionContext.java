package io.opensphere.core.control.action;

import java.awt.Component;
import java.util.List;

import io.opensphere.core.control.ContextMenuSelectionListener;

/**
 * The context provides the opportunity for {@link ContextMenuProvider}'s to
 * register {@link MenuOption}'s with the context and for a
 * {@link ContextSingleActionProvider} to perform an action for the given
 * context. Whenever a {@link ContextSingleActionProvider} is registered that
 * action will be performed and menus will not be shown even if menu providers
 * are registered. {@link MenuOptionListener}'s may subscribe for notifications
 * when menu options in this context are selected. Any plugin or component may
 * retrieve a JMenuItem that can be used to display a popup menu for the
 * context.
 *
 * @param <T> The context menu key type.
 */
public interface ActionContext<T>
{
    /**
     * Do the action for this context.
     *
     * @param key The key for the context menu.
     * @param comp The component in whose space the popup menu is to appear if a
     *            menu is appropriate..
     * @param x The x coordinate in the component's space at which the popup
     *            menu should be displayed if a menu is appropriate.
     * @param y The y coordinate in the component's space at which the popup
     *            menu should be displayed if a menu is appropriate.
     * @param listener The listener for selection on the created menu.
     * @return true when the menu is shown, false when no providers can provided
     *         menu items for the context.
     */
    boolean doAction(T key, Component comp, int x, int y, ContextMenuSelectionListener listener);

    /**
     * Gets the context identifier.
     *
     * @return the context identifier
     */
    String getContextIdentifier();

    /**
     * Retrieve menu items for the context.
     *
     * @param key The key for which menu items are desired.
     * @return A list of menu items as provided by each menu item provider.
     */
    List<Component> getMenuItems(T key);

    /**
     * Tell whether there are providers which provide for this context.
     *
     * @return true when there are providers which provide for this context.
     */
    boolean isUsed();
}
