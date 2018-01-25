package io.opensphere.core.data.util;

import java.util.List;

/**
 * Extension to {@link PropertyValueReceiver} that adds a receive method that
 * takes ids. Callers may call {@link #receive(long[], int, List)} if ids are
 * available, or {@link #receive(List)} if they aren't, but not both.
 *
 * @param <T> The type of the property values expected by this receiver.
 */
public interface PropertyValueIdReceiver<T> extends PropertyValueReceiver<T>
{
    /**
     * Method to be called with the properties.
     *
     * @param ids The ids for the property records.
     * @param startIndex The index of the first property value.
     * @param values The property values.
     */
    void receive(long[] ids, int startIndex, List<? extends T> values);
}
