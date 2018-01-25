package io.opensphere.mantle.data.impl;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueService;
import io.opensphere.core.util.WeakObservableValue;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.PlayState;
import io.opensphere.mantle.data.StreamingMechanism;
import io.opensphere.mantle.data.StreamingSupport;

/**
 * The streaming support implementation.
 */
public class StreamingSupportImpl extends ObservableValueService implements StreamingSupport
{
    /** The play state. */
    private final ObservableValue<PlayState> myPlayState = new WeakObservableValue<>();

    /** Whether streaming is enabled. */
    private final ObservableValue<Boolean> myStreamingEnabled = new WeakObservableValue<>();

    /** Whether sync is enabled. */
    private final ObservableValue<Boolean> mySynchronized = new WeakObservableValue<>();

    /** The mechanism through which streaming occurs. */
    private final ObservableValue<StreamingMechanism> myStreamingMechanism = new WeakObservableValue<>();

    /** The host through which streaming occurs. */
    private final ObservableValue<String> myStreamingHost = new WeakObservableValue<>();

    /**
     * Constructor.
     *
     * @param dataTypeInfoPreferenceAssistant the data type preference assistant
     * @param dtiKey the {@link DataTypeInfo} key for the data type.
     */
    public StreamingSupportImpl(final DataTypeInfoPreferenceAssistant dataTypeInfoPreferenceAssistant, final String dtiKey)
    {
        // number of services
        super(2);

        myStreamingEnabled.set(Boolean.FALSE);
        myStreamingMechanism.set(StreamingMechanism.CLIENT_POLL);

        // Set the initial play state when streaming is enabled
        bindModel(myStreamingEnabled, new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue.booleanValue() && myPlayState.get() == null)
                {
                    myPlayState.set(PlayState.STOP);
                }
            }
        });

        // Save the play state to the preferences when it changes
        bindModel(myPlayState, new ChangeListener<PlayState>()
        {
            @Override
            public void changed(ObservableValue<? extends PlayState> observable, PlayState oldValue, PlayState newValue)
            {
                dataTypeInfoPreferenceAssistant.setPlayStatePreference(dtiKey, newValue);
            }
        });

        open();
    }

    @Override
    public ObservableValue<PlayState> getPlayState()
    {
        return myPlayState;
    }

    @Override
    public boolean isStreamingEnabled()
    {
        return myStreamingEnabled.get().booleanValue();
    }

    @Override
    public void setStreamingEnabled(boolean isEnabled)
    {
        myStreamingEnabled.set(Boolean.valueOf(isEnabled));
    }

    /**
     * Gets the observable value for whether streaming is enabled for this data
     * type.
     *
     * @return The observable boolean value. Note that any listeners will be
     *         stored as a weak reference.
     */
    ObservableValue<Boolean> streamingEnabledProperty()
    {
        return myStreamingEnabled;
    }

    @Override
    public Boolean getSynchronized()
    {
        return mySynchronized.get();
    }

    @Override
    public void setSynchronized(Boolean isSynchronized)
    {
        mySynchronized.set(isSynchronized);
    }

    @Override
    public ObservableValue<Boolean> synchronizedProperty()
    {
        return mySynchronized;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.StreamingSupport#getStreamingMechanism()
     */
    @Override
    public StreamingMechanism getStreamingMechanism()
    {
        return myStreamingMechanism.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.StreamingSupport#setStreamingMechanism(io.opensphere.mantle.data.StreamingMechanism)
     */
    @Override
    public void setStreamingMechanism(StreamingMechanism streamingMechanism)
    {
        myStreamingMechanism.set(streamingMechanism);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.StreamingSupport#streamingMechanismProperty()
     */
    @Override
    public ObservableValue<StreamingMechanism> streamingMechanismProperty()
    {
        return myStreamingMechanism;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.StreamingSupport#getStreamingHost()
     */
    @Override
    public String getStreamingHost()
    {
        return myStreamingHost.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.StreamingSupport#setStreamingHost(java.lang.String)
     */
    @Override
    public void setStreamingHost(String streamingHost)
    {
        myStreamingHost.set(streamingHost);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.StreamingSupport#streamingHostProperty()
     */
    @Override
    public ObservableValue<String> streamingHostProperty()
    {
        return myStreamingHost;
    }
}
