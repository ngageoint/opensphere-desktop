package io.opensphere.core.terrain;

import net.jcip.annotations.NotThreadSafe;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.projection.AbstractGeographicProjection.GeographicTesseraVertex;

/** A vertex within the terrain. */
@NotThreadSafe
public class TerrainVertex extends GeographicTesseraVertex
{
    /** True when the elevation matches the most recently available value. */
    private boolean myElevationCurrent;

    /**
     * Geographic position as a 2D vector of latitude and longitude. This is
     * cached because it is called many times when generating tesserae for tiles
     * on the globe.
     */
    private Vector2d myGeographicPositionAsVector;

    /** The current index within the full list of vertices for the terrain. */
    private int myIndex = -1;

    /**
     * Constructor.
     *
     * @param coord Geographic coordinates.
     * @param model Model coordinates.
     */
    public TerrainVertex(GeographicPosition coord, Vector3d model)
    {
        super(coord, model);
    }

    @Override
    public TerrainVertex adjustToModelCenter(Vector3d modelCenter)
    {
        return new TerrainVertex(getCoordinates(), getModelCoordinates().subtract(modelCenter));
    }

    /**
     * Get the geographic position (coordinates) as a 2D vector of latitude and
     * longitude.
     *
     * @return The geographic position (coordinates) as a 2D vector of latitude
     *         and longitude.
     */
    public Vector2d getGeographicPositionAsVector()
    {
        if (myGeographicPositionAsVector == null)
        {
            myGeographicPositionAsVector = getCoordinates().getLatLonAlt().asVec2d();
        }
        return myGeographicPositionAsVector;
    }

    /**
     * Get the index.
     *
     * @return the index
     */
    public int getIndex()
    {
        return myIndex;
    }

    /**
     * Get the elevationCurrent.
     *
     * @return the elevationCurrent
     */
    public boolean isElevationCurrent()
    {
        return myElevationCurrent;
    }

    /**
     * Set the elevationCurrent.
     *
     * @param elevationCurrent the elevationCurrent to set
     */
    public void setElevationCurrent(boolean elevationCurrent)
    {
        myElevationCurrent = elevationCurrent;
    }

    /**
     * Set the index.
     *
     * @param index the index to set
     */
    public void setIndex(int index)
    {
        myIndex = index;
    }

    /**
     * Create a snapshot of the state of this terrain vertex.
     *
     * @return A snapshot of the state of this terrain vertex.
     */
    public TerrainVertex snapshot()
    {
        TerrainVertex copy = new TerrainVertex(getCoordinates(), getModelCoordinates());
        copy.setIndex(myIndex);
        copy.setElevationCurrent(true);
        return copy;
    }
}
