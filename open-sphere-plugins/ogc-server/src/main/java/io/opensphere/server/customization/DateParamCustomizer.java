package io.opensphere.server.customization;

import io.opensphere.core.model.time.TimeSpan;

/**
 * Interface for server customizations that format their own date parameters.
 */
@FunctionalInterface
public interface DateParamCustomizer
{
    /**
     * Get the date parameter for a time span.
     *
     * @param timeSpan The time span.
     * @return The date parameter.
     */
    String getDateParam(TimeSpan timeSpan);
}
