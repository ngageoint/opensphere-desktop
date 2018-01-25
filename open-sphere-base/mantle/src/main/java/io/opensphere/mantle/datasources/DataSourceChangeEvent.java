package io.opensphere.mantle.datasources;

/**
 * The Class DataSourceChangeEvent.
 */
public class DataSourceChangeEvent
{
    /** The my change type. */
    private final String myChangeType;

    /** The my data source. */
    private final IDataSource myDataSource;

    /** The my source. */
    private final Object mySource;

    /**
     * Instantiates a new data source change event.
     *
     * @param dataSource the data source
     * @param changeType the change type
     * @param source the source
     */
    public DataSourceChangeEvent(IDataSource dataSource, String changeType, Object source)
    {
        myDataSource = dataSource;
        mySource = source;
        myChangeType = changeType;
    }

    /**
     * Gets the change type.
     *
     * @return the change type
     */
    public String getChangeType()
    {
        return myChangeType;
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
     * Gets the source.
     *
     * @return the source
     */
    public Object getSource()
    {
        return mySource;
    }
}
