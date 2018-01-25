package io.opensphere.core.model;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Abstract base class for quadrilateral implementations.
 *
 * @param <T> The position type.
 */
public abstract class AbstractQuadrilateral<T extends Position> implements Quadrilateral<T>, Serializable
{
    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /** The vertices of the quadrilateral. */
    private final List<? extends T> myVertices;

    /**
     * Constructor.
     *
     * @param vertices The vertices, which must number 4.
     */
    public AbstractQuadrilateral(List<? extends T> vertices)
    {
        Utilities.checkNull(vertices, "vertices");

        if (vertices.size() != 4)
        {
            throw new IllegalArgumentException("Vertex count is " + vertices.size() + " instead of 4.");
        }
        myVertices = New.unmodifiableList(vertices);
    }

    /**
     * Constructor.
     *
     * @param pos1 The first vertex.
     * @param pos2 The second vertex.
     * @param pos3 The third vertex.
     * @param pos4 The fourth vertex.
     */
    public AbstractQuadrilateral(T pos1, T pos2, T pos3, T pos4)
    {
        List<T> vertices = New.list(4);
        vertices.add(Utilities.checkNull(pos1, "pos1"));
        vertices.add(Utilities.checkNull(pos2, "pos2"));
        vertices.add(Utilities.checkNull(pos3, "pos3"));
        vertices.add(Utilities.checkNull(pos4, "pos4"));
        myVertices = New.unmodifiableList(vertices);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getPositionType()
    {
        return (Class<T>)myVertices.get(0).getClass();
    }

    @Override
    public List<? extends T> getVertices()
    {
        return myVertices;
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append("DefaultQuadrilateral [vertices=").append(myVertices).append(']').toString();
    }
}
