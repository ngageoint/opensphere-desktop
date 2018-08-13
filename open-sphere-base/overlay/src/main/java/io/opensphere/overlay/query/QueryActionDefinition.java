package io.opensphere.overlay.query;

import java.util.function.Consumer;

import javax.swing.Icon;

/**
 * An action definition used to add additional behavior to the query pulldown
 * menu.
 */
public class QueryActionDefinition extends AbstractQueryActionDefinition
{
    /** The listener called when the action is performed. */
    private final Consumer<QueryEvent> myEventListener;

    /**
     * @param icon The icon used for the query action.
     * @param label The label displayed alongside the icon.
     * @param eventListener The listener called when the action is performed.
     */
    public QueryActionDefinition(Icon icon, String label, Consumer<QueryEvent> eventListener)
    {
        super(icon, label);
        myEventListener = eventListener;
    }

    /**
     * Gets the value of the {@link #myEventListener} field.
     *
     * @return the value stored in the {@link #myEventListener} field.
     */
    public Consumer<QueryEvent> getEventListener()
    {
        return myEventListener;
    }
}
