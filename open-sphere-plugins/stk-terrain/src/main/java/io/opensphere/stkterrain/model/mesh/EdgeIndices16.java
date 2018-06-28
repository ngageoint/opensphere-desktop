package io.opensphere.stkterrain.model.mesh;

import java.nio.ByteBuffer;

import net.jcip.annotations.Immutable;

/** 16 bit Edge Indices. */
@Immutable
public class EdgeIndices16 extends EdgeIndices
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param buffer the byte buffer
     */
    public EdgeIndices16(ByteBuffer buffer)
    {
        super(new Indices16(getIndices(buffer)), new Indices16(getIndices(buffer)), new Indices16(getIndices(buffer)),
                new Indices16(getIndices(buffer)));
    }

    /**
     * Reads the indices from the buffer.
     *
     * @param buffer the byte buffer
     * @return the indices
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    private static short[] getIndices(ByteBuffer buffer)
    {
        int indicesCount = buffer.getInt();
        short[] indices = new short[indicesCount];
        for (int i = 0; i < indicesCount; i++)
        {
            indices[i] = buffer.getShort();
        }
        return indices;
    }
}
