package io.opensphere.myplaces.controllers;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.event.DataGroupInfoChildAddedEvent;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Listens for data type info color changes.
 *
 */
public class DataGroupInfoChildAddedListener implements EventListener<DataGroupInfoChildAddedEvent>
{
    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The my places model.
     */
    private final MyPlacesModel myModel;

    /**
     * Constructs a new color listener.
     *
     * @param toolbox The toolbox.
     * @param model The model.
     */
    public DataGroupInfoChildAddedListener(Toolbox toolbox, MyPlacesModel model)
    {
        myModel = model;
        myToolbox = toolbox;
        myToolbox.getEventManager().subscribe(DataGroupInfoChildAddedEvent.class, this);
    }

    /**
     * Close open resources.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(DataGroupInfoChildAddedEvent.class, this);
    }

    @Override
    public void notify(final DataGroupInfoChildAddedEvent event)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (event.getAdded() instanceof MyPlacesDataGroupInfo)
                {
                    myModel.notifyObservers();
                }
            }
        });
    }
}
