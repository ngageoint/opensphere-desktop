package io.opensphere.myplaces.controllers;

import java.awt.Color;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Listens for data type info color changes.
 *
 */
public class DataTypeInfoColorChangeListener extends BaseDataTypeInfoChangedListener<DataTypeInfoColorChangeEvent>
        implements EventListener<DataTypeInfoColorChangeEvent>
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
    public DataTypeInfoColorChangeListener(Toolbox toolbox, MyPlacesModel model)
    {
        super(model);
        myToolbox = toolbox;
        myToolbox.getEventManager().subscribe(DataTypeInfoColorChangeEvent.class, this);
    }

    /**
     * Close open resources.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(DataTypeInfoColorChangeEvent.class, this);
    }

    @Override
    public void notify(final DataTypeInfoColorChangeEvent event)
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
    protected void makeChange(DataTypeInfoColorChangeEvent event, Placemark placemark)
    {
        Color color = event.getColor();
        PlacemarkUtils.setPlacemarkColor(placemark, color);
    }
}
