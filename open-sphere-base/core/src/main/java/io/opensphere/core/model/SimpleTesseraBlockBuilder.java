package io.opensphere.core.model;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Tessera.TesseraVertex;
import io.opensphere.core.model.TesseraList.TesseraBlockBuilder;
import io.opensphere.core.util.Utilities;

/**
 * A tessera block builder which handles mapping terrain vertices.
 *
 * @param <T> the type of vertex used by this builder.
 */
public class SimpleTesseraBlockBuilder<T extends TesseraVertex<?>> extends TesseraBlockBuilder<T>
{
    /**
     * A map of vertices to the index within the block. This map uses -1 to mean
     * "no entry."
     */
    private final TObjectIntMap<T> myIndexMap = new TObjectIntHashMap<>(10, .5f, -1);

    /**
     * The origin of the model coordinate space for the results. If no
     * adjustment is required {@code null} may be used.
     */
    private final Vector3d myModelCenter;

    /**
     * Constructor.
     *
     * @param tesseraVertexCount The number of vertices per tessera.
     * @param modelCenter The origin of the model coordinate space for the
     *            results. If no adjustment is required {@code null} may be
     *            given.
     */
    public SimpleTesseraBlockBuilder(int tesseraVertexCount, Vector3d modelCenter)
    {
        super(tesseraVertexCount);
        myModelCenter = modelCenter;
    }

    /**
     * Add a tessera to the block of tesserae.
     *
     * @param vertices The vertices which make up the tessera.
     */
    @SuppressWarnings("unchecked")
    public void add(T[] vertices)
    {
        if (getBlockTesseraVertexCount() != vertices.length)
        {
            throw new IllegalArgumentException(
                    "Wrong number of verticies (" + vertices.length + "). Exepected " + getBlockTesseraVertexCount());
        }

        for (T vert : vertices)
        {
            int index = myIndexMap.get(vert);
            // check to see if the index is the "no entry" value
            if (index == -1)
            {
                index = getNextIndex();

                T vertex = vert;
                if (myModelCenter != null && !Utilities.sameInstance(myModelCenter, Vector3d.ORIGIN))
                {
                    vertex = (T)vert.adjustToModelCenter(myModelCenter);
                }

                getBlockVertices().add(vertex);
                myIndexMap.put(vertex, index);
            }
            getBlockIndices().add(index);
        }
    }
}
