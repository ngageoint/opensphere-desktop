package io.opensphere.mantle.data;

import java.util.function.Predicate;

import io.opensphere.core.util.ObservableValue;

/**
 * Interface for streaming support.
 */
public interface StreamingSupport
{
    /** A predicate that determines if streaming is enabled for a data type. */
    Predicate<DataTypeInfo> IS_STREAMING_ENABLED = dataType -> dataType.getStreamingSupport().isStreamingEnabled();

    /**
     * Gets the observable value for the play state of this data type.
     *
     * @return The observable play state value. Note that any listeners will be
     *         stored as a weak reference.
     */
    ObservableValue<PlayState> getPlayState();

    /**
     * Gets whether streaming is enabled.
     *
     * @return whether streaming is enabled
     */
    boolean isStreamingEnabled();

    /**
     * Sets whether streaming is enabled.
     *
     * @param isEnabled whether streaming is enabled
     */
    void setStreamingEnabled(boolean isEnabled);

    /**
     * Gets whether streaming is synchronized for this data type.
     *
     * @return whether streaming is synchronized for this data type.
     */
    Boolean getSynchronized();

    /**
     * Sets whether streaming is synchronized for this data type.
     *
     * @param isSynchronized whether streaming is synchronized for this data
     *            type.
     */
    void setSynchronized(Boolean isSynchronized);

    /**
     * Gets the observable value for whether streaming is synchronized for this
     * data type.
     *
     * @return The observable boolean value. Note that any listeners will be
     *         stored as a weak reference.
     */
    ObservableValue<Boolean> synchronizedProperty();

    /**
     * Gets the streaming mechanism for the data type.
     *
     * @return the streaming mechanism for the data type.
     */
    StreamingMechanism getStreamingMechanism();

    /**
     * Sets the streaming mechanism for the data type.
     *
     * @param streamingMechanism the streaming mechanism for the data type.
     */
    void setStreamingMechanism(StreamingMechanism streamingMechanism);

    /**
     * Gets the observable value for the streaming mechanism field.
     *
     * @return the observable value for the streaming mechanism field.
     */
    ObservableValue<StreamingMechanism> streamingMechanismProperty();

    /**
     * Gets the name of the host through which streaming occurs.
     *
     * @return the name of the host through which streaming occurs.
     */
    String getStreamingHost();

    /**
     * Sets the name of the host through which streaming occurs.
     *
     * @param streamingHost the name of the host through which streaming occurs.
     */
    void setStreamingHost(String streamingHost);

    /**
     * Gets the observable value for the streaming host field.
     *
     * @return the observable value for the streaming host field.
     */
    ObservableValue<String> streamingHostProperty();
}
