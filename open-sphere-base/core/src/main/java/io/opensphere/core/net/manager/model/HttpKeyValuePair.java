package io.opensphere.core.net.manager.model;

import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A simple POJO in which an HTTP key / value pair is described. Used to
 * describe both parameters and headers.
 */
public class HttpKeyValuePair
{
    /** The property in which the name of the pair is described. */
    private final StringProperty myNameProperty = new ConcurrentStringProperty();

    /** The property in which the value of the pair is described. */
    private final StringProperty myValueProperty = new ConcurrentStringProperty();

    /**
     * Creates an empty pair.
     */
    public HttpKeyValuePair()
    {
        /* intentionally blank. */
    }

    /**
     * Creates a new pair using the supplied key and value.
     *
     * @param key the name of the pair.
     * @param value the value of the pair.
     */
    public HttpKeyValuePair(String key, String value)
    {
        myNameProperty.set(key);
        myValueProperty.set(value);
    }

    /**
     * Gets the value of the {@link #myNameProperty} field.
     *
     * @return the value of the myNameProperty field.
     */
    public StringProperty nameProperty()
    {
        return myNameProperty;
    }

    /**
     * Gets the value of the {@link #myValueProperty} field.
     *
     * @return the value of the myValueProperty field.
     */
    public StringProperty valueProperty()
    {
        return myValueProperty;
    }
}
