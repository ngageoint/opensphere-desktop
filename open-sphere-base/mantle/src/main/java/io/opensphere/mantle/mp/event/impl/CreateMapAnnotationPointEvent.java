package io.opensphere.mantle.mp.event.impl;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.model.GeographicPosition;

/**
 * The Class CreateMapAnnotationPointEvent. Event for notifying that a map point
 * should be created.
 */
public class CreateMapAnnotationPointEvent extends AbstractSingleStateEvent
{
    /** The Source. */
    private final Object mySource;

    /** The Geometry. */
    private final GeographicPosition myGeographicPosition;

    /**
     * Instantiates a new creates the map annotation point event.
     *
     * @param source the source
     * @param geom the geom
     */
    public CreateMapAnnotationPointEvent(Object source, GeographicPosition geom)
    {
        mySource = source;
        myGeographicPosition = geom;
    }

    @Override
    public String getDescription()
    {
        return "CreateMapAnnotationPointEvent";
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public GeographicPosition getPosition()
    {
        return myGeographicPosition;
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
