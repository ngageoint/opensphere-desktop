package io.opensphere.filterbuilder.filter.v1;

/**
 * FilterChangeListener: interface describing the listener.
 */
@FunctionalInterface
public interface FilterChangeListener
{
    /**
     * The method that gets called when the event happens.
     *
     * @param e the event
     */
    void filterChanged(FilterChangeEvent e);
}
