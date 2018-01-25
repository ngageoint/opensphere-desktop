package io.opensphere.myplaces.controllers;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.myplaces.models.DataTypeInfoMyPlaceChangedEvent;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Listens for data type info changes and requests a redraw for that type.
 *
 */
public class DataTypeInfoChangedListener extends BaseDataTypeInfoChangedListener<DataTypeInfoMyPlaceChangedEvent>
        implements EventListener<DataTypeInfoMyPlaceChangedEvent>
{
    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new color listener.
     *
     * @param toolbox The toolbox.
     * @param model The model.
     */
    public DataTypeInfoChangedListener(Toolbox toolbox, MyPlacesModel model)
    {
        super(model);
        myToolbox = toolbox;
        myToolbox.getEventManager().subscribe(DataTypeInfoMyPlaceChangedEvent.class, this);
    }

    /**
     * Close open resources.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(DataTypeInfoMyPlaceChangedEvent.class, this);
    }

    @Override
    public void notify(final DataTypeInfoMyPlaceChangedEvent event)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                DataTypeInfo info = event.getDataTypeInfo();
                handleChange(event, info);
            }
        });
    }

    @Override
    protected void makeChange(DataTypeInfoMyPlaceChangedEvent changeObject, Placemark placemark)
    {
        DataTypeInfo info = changeObject.getDataTypeInfo();
        BasicVisualizationInfo basicVisualization = info.getBasicVisualizationInfo();
        if (basicVisualization != null)
        {
            basicVisualization.setTypeColor(PlacemarkUtils.getPlacemarkColor(placemark), this);
        }
    }
}
