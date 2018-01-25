package io.opensphere.mantle.data.element.event.consolidators;

import java.awt.Color;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.DataElementColorChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementColorChangeEvent;

/**
 * The Class DataElementColorChangeConsolidator.
 */
public class DataElementColorChangeConsolidator extends AbstractDataElementEventConsolidator<DataElementColorChangeEvent>
{
    /** The colors. */
    private final TLongObjectHashMap<Color> myIdToColorMap;

    /** The Opacity change only. */
    private boolean myOpacityChangeOnly;

    /**
     * Instantiates a new data element color change consolidator.
     */
    public DataElementColorChangeConsolidator()
    {
        super();
        myIdToColorMap = new TLongObjectHashMap<>();
    }

    @Override
    public void addEvent(DataElementColorChangeEvent event)
    {
        super.addEvent(event);
        if (event != null)
        {
            myIdToColorMap.put(event.getRegistryId(), event.getColor());
        }
    }

    @Override
    public Event createConsolidatedEvent()
    {
        return new ConsolidatedDataElementColorChangeEvent(getIdSet(), getDataTypeSet(), myIdToColorMap, myOpacityChangeOnly,
                getSource());
    }

    @Override
    public EventConsolidator<DataElementColorChangeEvent> newInstance()
    {
        return new DataElementColorChangeConsolidator();
    }

    @Override
    public void reset()
    {
        super.reset();
        myIdToColorMap.clear();
    }

    /**
     * Sets the opacity change only flag.
     *
     * @param opacityChangeOnly the new opacity change only
     */
    public void setOpacityChangeOnly(boolean opacityChangeOnly)
    {
        myOpacityChangeOnly = opacityChangeOnly;
    }
}
