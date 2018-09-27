package io.opensphere.myplaces.controllers;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.event.DataGroupInfoMemberRemovedEvent;
import io.opensphere.myplaces.importer.KmlTranslator;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Listens for data type info color changes.
 */
public class DataGroupInfoMemberRemovedListener implements EventListener<DataGroupInfoMemberRemovedEvent>
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
    public DataGroupInfoMemberRemovedListener(Toolbox toolbox, MyPlacesModel model, KmlTranslator kmlTranslator)
    {
        myModel = model;
        myToolbox = toolbox;
        myToolbox.getEventManager().subscribe(DataGroupInfoMemberRemovedEvent.class, this);
        myKmlTranslator = kmlTranslator;
    }

    /**
     * Close open resources.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(DataGroupInfoMemberRemovedEvent.class, this);
    }

    @Override
    public void notify(final DataGroupInfoMemberRemovedEvent event)
    {
        EventQueueUtilities.invokeLater(() ->
        {
            if (event.getRemoved() instanceof MyPlacesDataTypeInfo)
            {
                Kml kml = myKmlTranslator.toKml(myModel.getDataGroups());
                myModel.setMyPlaces(kml);
            }
        });
    }
}
