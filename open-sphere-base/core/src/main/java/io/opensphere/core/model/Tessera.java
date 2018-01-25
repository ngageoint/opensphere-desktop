package io.opensphere.core.model;

import java.util.List;

import io.opensphere.core.math.Vector3d;

/**
 * A tessera is a component in a tessellation. It could be a triangle or a quad
 * or any other shape that can be used to form a surface.
 *
 * @param <S> Position Type of tessera vertex used by this tessera.
 */
public interface Tessera<S extends Position>
{
    /**
     * Get the vertices of the tessera.
     *
     * @return The list of vertices.
     */
    List<? extends TesseraVertex<S>> getTesseraVertices();

    /**
     * A vertex of the tessera.
     *
     * @param <S> Position Type of tessera vertex.
     */
    public interface TesseraVertex<S extends Position>
    {
        /**
         * Create a new tessera vertex with the same position as this vertex,
         * but adjusted to put it in the model coordinates with the given model
         * center. It is assumed that the vertex is centered at the origin
         * before conversion.
         *
         * @param modelCenter The center of the model coordinates.
         * @return The converted tessera vertex.
         */
        TesseraVertex<S> adjustToModelCenter(Vector3d modelCenter);

        /**
         * Get the coordinates of the vertex.
         *
         * @return The coordinates.
         */
        S getCoordinates();
    }
}
