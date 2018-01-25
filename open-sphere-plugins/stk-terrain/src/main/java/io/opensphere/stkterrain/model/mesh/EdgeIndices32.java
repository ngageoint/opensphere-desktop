package io.opensphere.stkterrain.model.mesh;

import java.nio.ByteBuffer;

import javax.annotation.concurrent.Immutable;

/** 32 bit Edge Indices. */
@Immutable
public class EdgeIndices32 extends EdgeIndices
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param buffer the byte buffer
     */
    public EdgeIndices32(ByteBuffer buffer)
    {
        super(new Indices32(getIndices(buffer)), new Indices32(getIndices(buffer)), new Indices32(getIndices(buffer)),
                new Indices32(getIndices(buffer)));
    }

    /**
     * Reads the indices from the buffer.
     *
     * @param buffer the byte buffer
     * @return the indices
     */
    private static int[] getIndices(ByteBuffer buffer)
    {
        int indicesCount = buffer.getInt();
        int[] indices = new int[indicesCount];
        for (int i = 0; i < indicesCount; i++)
        {
            indices[i] = buffer.getInt();
        }
        return indices;
    }
}
