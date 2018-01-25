package io.opensphere.myplaces.controllers;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.event.DataGroupInfoChildRemovedEvent;
import io.opensphere.myplaces.importer.KmlTranslator;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Listens for data type info color changes.
 *
 */
public class DataGroupInfoChildRemovedListener implements EventListener<DataGroupInfoChildRemovedEvent>
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
     * Translates from data source groups to kml.
     */
    private final KmlTranslator myKmlTranslator;

    /**
     * Constructs a new color listener.
     *
     * @param toolbox The toolbox.
     * @param model The model.
     * @param kmlTranslator translates from data source groups to kml.
     */
    public DataGroupInfoChildRemovedListener(Toolbox toolbox, MyPlacesModel model, KmlTranslator kmlTranslator)
    {
        myModel = model;
        myToolbox = toolbox;
        myToolbox.getEventManager().subscribe(DataGroupInfoChildRemovedEvent.class, this);
        myKmlTranslator = kmlTranslator;
    }

    /**
     * Close open resources.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(DataGroupInfoChildRemovedEvent.class, this);
    }

    @Override
    public void notify(final DataGroupInfoChildRemovedEvent event)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (event.getRemoved() instanceof MyPlacesDataGroupInfo)
                {
                    Kml kml = myKmlTranslator.toKml(myModel.getDataGroups());
                    myModel.setMyPlaces(kml);
                }
            }
        });
    }
}
