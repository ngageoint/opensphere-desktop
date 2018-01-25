package io.opensphere.mantle.datasources;

/**
 * The Class LoadEndDispositionEvent.
 */
public class LoadEndDispositionEvent
{
    /** The my controller. */
    private final DataSourceController myController;

    /** The my data source. */
    private final IDataSource myDataSource;

    /** The my exception. */
    private Throwable myException;

    /** The my impacts data layers. */
    private boolean myImpactsDataLayers;

    /** The my message. */
    private String myMessage;

    /** The my was loading. */
    private final boolean myWasLoading;

    /** The my was successful. */
    private final boolean myWasSuccessful;

    /**
     * Instantiates a new load end disposition event.
     *
     * @param wasSuccessful the was successful
     * @param wasLoading the was loading
     * @param source the source
     * @param controller the controller
     * @param impactsDataLayers the impacts data layers
     */
    public LoadEndDispositionEvent(boolean wasSuccessful, boolean wasLoading, IDataSource source, DataSourceController controller,
            boolean impactsDataLayers)
    {
        myWasLoading = wasLoading;
        myWasSuccessful = wasSuccessful;
        myDataSource = source;
        myController = controller;
        myImpactsDataLayers = impactsDataLayers;
    }

    /**
     * Instantiates a new load end disposition event.
     *
     * @param wasSuccessful the was successful
     * @param wasLoading the was loading
     * @param source the source
     * @param controller the controller
     * @param message the message
     * @param impactsTimeline the impacts timeline
     */
    public LoadEndDispositionEvent(boolean wasSuccessful, boolean wasLoading, IDataSource source, DataSourceController controller,
            String message, boolean impactsTimeline)
    {
        myWasLoading = wasLoading;
        myWasSuccessful = wasSuccessful;
        myDataSource = source;
        myController = controller;
        myMessage = message;
        myImpactsDataLayers = impactsTimeline;
    }

    /**
     * Instantiates a new load end disposition event.
     *
     * @param wasLoading the was loading
     * @param source the source
     * @param controller the controller
     * @param e the e
     * @param impactsTimeline the impacts timeline
     */
    public LoadEndDispositionEvent(boolean wasLoading, IDataSource source, DataSourceController controller, Throwable e,
            boolean impactsTimeline)
    {
        this(false, wasLoading, source, controller, impactsTimeline);
        myException = e;
    }

    /**
     * Gets the controller.
     *
     * @return the controller
     */
    public DataSourceController getController()
    {
        return myController;
    }

    /**
     * Gets the data source.
     *
     * @return the data source
     */
    public IDataSource getDataSource()
    {
        return myDataSource;
    }

    /**
     * Gets the exception.
     *
     * @return the exception
     */
    public Throwable getException()
    {
        return myException;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage()
    {
        return myMessage;
    }

    /**
     * Impacts data layers.
     *
     * @return true, if successful
     */
    public boolean impactsDataLayers()
    {
        return myImpactsDataLayers;
    }

    /**
     * Setter for impactsDataLayers.
     *
     * @param impactsDataLayers the impactsDataLayers
     */
    public void setImpactsDataLayers(boolean impactsDataLayers)
    {
        myImpactsDataLayers = impactsDataLayers;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("LoadEndDispositionEvent: Success: ").append(myWasSuccessful);
        sb.append(" Loading: ").append(myWasLoading);
        sb.append(" Source: ").append(myDataSource == null ? "NULL" : myDataSource.getName());
        sb.append(" Exception: ").append(myException == null ? "NULL" : myException.toString());
        return sb.toString();
    }

    /**
     * Was loading.
     *
     * @return true, if successful
     */
    public boolean wasLoading()
    {
        return myWasLoading;
    }

    /**
     * Was successful.
     *
     * @return true, if successful
     */
    public boolean wasSuccessful()
    {
        return myWasSuccessful;
    }
}
