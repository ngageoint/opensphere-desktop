package io.opensphere.filterbuilder.filter;

import java.util.EventListener;

/**
 * FilterComponentListener.
 */
@FunctionalInterface
public interface FilterComponentListener extends EventListener
{
    /**
     * Filter component changed.
     *
     * @param pEvent the event
     */
    void filterComponentChanged(FilterComponentEvent pEvent);
}
