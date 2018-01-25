package io.opensphere.core.control.action;

/**
 * The listener interface for receiving menuOption events. The class that is
 * interested in processing a menuOption event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addMenuOptionListener</code> method. When the menuOption
 * event occurs, that object's appropriate method is invoked.
 */
public interface MenuOptionListener
{
    /** Notify the listener that no menu item was selected. */
    void handleMenuCancelled();

    /**
     * Menu option selected.
     *
     * @param optionContext the option context
     * @param optionCommand the option command
     */
    void menuOptionSelected(String optionContext, String optionCommand);
}
