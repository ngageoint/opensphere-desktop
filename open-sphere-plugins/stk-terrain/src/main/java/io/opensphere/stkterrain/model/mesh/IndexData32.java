package io.opensphere.stkterrain.model.mesh;

import java.nio.ByteBuffer;

import net.jcip.annotations.Immutable;

/** 32 bit Index Data. */
@Immutable
public class IndexData32 extends Indices32
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param buffer the byte buffer
     */
    public IndexData32(ByteBuffer buffer)
    {
        super(getIndices(buffer));
    }

    /**
     * Reads the indices from the buffer.
     *
     * @param buffer the byte buffer
     * @return the indices
     */
    private static int[] getIndices(ByteBuffer buffer)
    {
        int triangleCount = buffer.getInt();
        int indicesCount = triangleCount * 3;
        int[] indices = new int[indicesCount];
        for (int i = 0; i < indicesCount; i++)
        {
            indices[i] = buffer.getInt();
        }

        decodeIndices(indices);

        return indices;
    }

    /**
     * Decodes the indices.
     *
     * @param indices the indices
     */
    private static void decodeIndices(int[] indices)
    {
        int highest = 0;
        for (int i = 0; i < indices.length; i++)
        {
            int code = indices[i];
            indices[i] = highest - code;
            if (code == 0)
            {
                highest++;
            }
        }
    }
}
