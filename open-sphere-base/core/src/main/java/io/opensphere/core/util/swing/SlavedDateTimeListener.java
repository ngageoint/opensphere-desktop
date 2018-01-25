package io.opensphere.core.util.swing;

/**
 * The listener interface for receiving slavedDateTime events. The class that is
 * interested in processing a slavedDateTime event implements this interface,
 * and the object created with that class is registered with a component using
 * the component's <code>addSlavedDateTimeListener</code> method. When the
 * slavedDateTime event occurs, that object's appropriate method is invoked.
 *
 * @see SlavedDateTimeEvent
 */
@FunctionalInterface
public interface SlavedDateTimeListener
{
    /**
     * Callback for when the date has changed.
     *
     * @param evt the date change event.
     */
    void slavedDateChanged(SlavedDateTimeEvent evt);
}
