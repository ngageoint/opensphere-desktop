package io.opensphere.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A simple implementation of {@code Tessera} that is just a list of
 * {@link Tessera.TesseraVertex}s.
 *
 * @param <S> Position Type of tessera vertex used by this tessera.
 */
public class SimpleTessera<S extends Position> implements Tessera<S>
{
    /** The vertices. */
    private final List<TesseraVertex<S>> myVertices;

    /**
     * Constructor.
     *
     * @param vertices The vertices.
     */
    public SimpleTessera(Collection<? extends TesseraVertex<S>> vertices)
    {
        if (vertices == null)
        {
            myVertices = Collections.emptyList();
        }
        else
        {
            myVertices = Collections.unmodifiableList(new ArrayList<TesseraVertex<S>>(vertices));
        }
    }

    @Override
    public List<TesseraVertex<S>> getTesseraVertices()
    {
        return myVertices;
    }
}
