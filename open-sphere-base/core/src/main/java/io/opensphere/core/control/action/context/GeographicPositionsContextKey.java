package io.opensphere.core.control.action.context;

import java.util.List;

import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/** The context key for actions against a list of geographic positions. */
public class GeographicPositionsContextKey
{
    /**
     * The positions which are associated with this key and the geometry
     * associated with that position (or null if there is no associated
     * geometry).
     */
    private final List<Pair<GeographicPosition, AbstractRenderableGeometry>> myPositions;

    /**
     * Constructor.
     *
     * @param positions The positions which are associated with this key.
     */
    public GeographicPositionsContextKey(List<Pair<GeographicPosition, AbstractRenderableGeometry>> positions)
    {
        myPositions = positions;
    }

    /**
     * Get the positions which are associated with this key.
     *
     * @return The positions which are associated with this key.
     */
    public List<Pair<GeographicPosition, AbstractRenderableGeometry>> getPositions()
    {
        return New.list(myPositions);
    }
}
