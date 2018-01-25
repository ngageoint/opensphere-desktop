package io.opensphere.myplaces.controllers;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.event.DataGroupInfoMemberAddedEvent;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Listens for data type info color changes.
 *
 */
public class DataGroupInfoMemberAddedListener implements EventListener<DataGroupInfoMemberAddedEvent>
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
    public DataGroupInfoMemberAddedListener(Toolbox toolbox, MyPlacesModel model)
    {
        myModel = model;
        myToolbox = toolbox;
        myToolbox.getEventManager().subscribe(DataGroupInfoMemberAddedEvent.class, this);
    }

    /**
     * Close open resources.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(DataGroupInfoMemberAddedEvent.class, this);
    }

    @Override
    public void notify(final DataGroupInfoMemberAddedEvent event)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (event.getAdded() instanceof MyPlacesDataTypeInfo)
                {
                    myModel.notifyObservers();
                }
            }
        });
    }
}
