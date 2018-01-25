package io.opensphere.mantle.datasources;

/**
 * A data source controller event.
 */
public class DataSourceControllerEvent
{
    /** The my controller. */
    private final DataSourceController myController;

    /** The my data source. */
    private final IDataSource myDataSource;

    /**
     * Instantiates a new load start event.
     *
     * @param source the source
     * @param controller the controller
     */
    public DataSourceControllerEvent(IDataSource source, DataSourceController controller)
    {
        myDataSource = source;
        myController = controller;
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
     * Gets the event data source.
     *
     * @return the data source
     */
    public IDataSource getDataSource()
    {
        return myDataSource;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + ": " + (myDataSource == null ? "NULL" : myDataSource.getName());
    }
}
