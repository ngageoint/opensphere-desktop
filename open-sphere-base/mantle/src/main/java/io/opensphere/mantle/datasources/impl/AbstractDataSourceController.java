package io.opensphere.mantle.datasources.impl;

import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.datasources.DataSourceController;

/**
 * The Class AbstractDataSourceController.
 */
public abstract class AbstractDataSourceController implements DataSourceController
{
    /** The Loads to change listener. */
    @XmlTransient
    private final transient EventListener<DataTypeInfoLoadsToChangeEvent> myLoadsToChangeListener = this::handleLoadsToChanged;

    /** The my toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new abstract data source controller.
     *
     * @param tb the {@link Toolbox}
     */
    public AbstractDataSourceController(Toolbox tb)
    {
        myToolbox = tb;
        myToolbox.getEventManager().subscribe(DataTypeInfoLoadsToChangeEvent.class, myLoadsToChangeListener);
    }

    @Override
    public int compareTo(DataSourceController other)
    {
        int returnValue = 0;
        if (getSourceType() == DataSourceType.SERVER && other.getSourceType() != DataSourceType.SERVER)
        {
            returnValue = -10000;
        }
        else if (getSourceType() != DataSourceType.SERVER && other.getSourceType() == DataSourceType.SERVER)
        {
            returnValue = 10000;
        }
        else
        {
            returnValue = getTypeName().compareTo(other.getTypeName());
        }
        return returnValue;
    }

    @Override
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Handle loads to changed.
     *
     * @param event the event
     */
    protected void handleLoadsToChanged(DataTypeInfoLoadsToChangeEvent event)
    {
    }
}
