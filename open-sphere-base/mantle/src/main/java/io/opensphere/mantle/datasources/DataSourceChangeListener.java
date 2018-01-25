package io.opensphere.mantle.datasources;

/**
 * The listener interface for receiving dataSourceChange events. The class that
 * is interested in processing a dataSourceChange event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's <code>addDataSourceChangeListener</code>
 * method. When the dataSourceChange event occurs, that object's appropriate
 * method is invoked.
 *
 * @see DataSourceChangeEvent
 */
@FunctionalInterface
public interface DataSourceChangeListener
{
    /**
     * Data source changed.
     *
     * @param evt the evt
     */
    void dataSourceChanged(DataSourceChangeEvent evt);
}
