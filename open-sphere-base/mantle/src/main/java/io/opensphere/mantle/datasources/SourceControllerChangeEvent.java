package io.opensphere.mantle.datasources;

/**
 * The Class SourceControllerChangeEvent.
 */
public class SourceControllerChangeEvent
{
    /** The my controller. */
    private final DataSourceController myController;

    /** The my source. */
    private final Object mySource;

    /**
     * Instantiates a new source controller change event.
     *
     * @param controller the controller
     * @param source the source
     */
    public SourceControllerChangeEvent(DataSourceController controller, Object source)
    {
        myController = controller;
        mySource = source;
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
     * Gets the source.
     *
     * @return the source
     */
    public Object getSource()
    {
        return mySource;
    }
}
