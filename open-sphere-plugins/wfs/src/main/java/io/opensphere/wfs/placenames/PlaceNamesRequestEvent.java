package io.opensphere.wfs.placenames;

import io.opensphere.core.event.AbstractMultiStateEvent;

/**
 * An event describing a place name request.
 */
public class PlaceNamesRequestEvent extends AbstractMultiStateEvent
{
    /** Place names to inject into the pipeline. */
    private final PlaceNameTile myTile;

    /**
     * Constructor.
     *
     * @param tile Tile which will receive the place names.
     */
    public PlaceNamesRequestEvent(PlaceNameTile tile)
    {
        myTile = tile;
    }

    @Override
    public String getDescription()
    {
        return "Event indicating that a place name request has been issued.";
    }

    /**
     * Get the tile.
     *
     * @return the tile
     */
    public PlaceNameTile getTile()
    {
        return myTile;
    }
}
