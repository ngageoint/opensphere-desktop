package io.opensphere.core.model;

/**
 * A simple implementation of {code Tessera.TesseraVertex} that comprises a set
 * of geographic coordinates and a set of model coordinates.
 *
 * @param <S> Position Type of tessera vertex used by this tessera.
 */
public abstract class SimpleTesseraVertex<S extends Position> implements Tessera.TesseraVertex<S>
{
    /** The geographic coordinates. */
    private final S myCoordinates;

    /**
     * Construct a vertex.
     *
     * @param coord The coordinates.
     */
    public SimpleTesseraVertex(S coord)
    {
        myCoordinates = coord;
    }

    @Override
    public S getCoordinates()
    {
        return myCoordinates;
    }
}
