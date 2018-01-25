package io.opensphere.stkterrain.model.mesh;

import java.io.Serializable;

/** Indices interface. */
public interface Indices extends Serializable
{
    /**
     * Gets the index count.
     *
     * @return the index count
     */
    int getIndexCount();

    /**
     * Gets the index.
     *
     * @param i the index of the index
     * @return the index
     */
    int getIndex(int i);
}
