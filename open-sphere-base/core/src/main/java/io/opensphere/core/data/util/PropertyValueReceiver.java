package io.opensphere.core.data.util;

import java.util.List;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Interface for the client objects that receive the results of the query. This
 * callback mechanism allows for asynchronous receipt of the values.
 *
 * @param <T> The type of the property values expected by this receiver.
 */
public interface PropertyValueReceiver<T>
{
    /**
     * Get the description of the property expected by this receiver.
     *
     * @return The property descriptor.
     */
    PropertyDescriptor<T> getPropertyDescriptor();

    /**
     * Get the optional processor to be called before values are received.
     *
     * @return The processor.
     */
    ValueProcessor<T> getValueProcessor();

    /**
     * Method to be called with the properties.
     *
     * @param values The property values.
     */
    void receive(List<? extends T> values);

    /**
     * Interface for a processor to be called as values are received.
     *
     * @param <T> The type of the object handled by this processor.
     */
    @FunctionalInterface
    public interface ValueProcessor<T>
    {
        /**
         * Method called prior to {@link PropertyValueReceiver#receive(List)}.
         *
         * @param values The values.
         */
        void preReceive(List<? extends T> values);
    }
}
