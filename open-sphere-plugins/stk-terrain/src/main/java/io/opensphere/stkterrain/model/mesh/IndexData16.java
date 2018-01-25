package io.opensphere.stkterrain.model.mesh;

import java.nio.ByteBuffer;

import javax.annotation.concurrent.Immutable;

/** 16 bit Index Data. */
@SuppressWarnings("PMD.AvoidUsingShortType")
@Immutable
public class IndexData16 extends Indices16
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param buffer the byte buffer
     */
    public IndexData16(ByteBuffer buffer)
    {
        super(getIndices(buffer));
    }

    /**
     * Reads the indices from the buffer.
     *
     * @param buffer the byte buffer
     * @return the indices
     */
    private static short[] getIndices(ByteBuffer buffer)
    {
        int triangleCount = buffer.getInt();
        int indicesCount = triangleCount * 3;
        short[] indices = new short[indicesCount];
        for (int i = 0; i < indicesCount; i++)
        {
            indices[i] = buffer.getShort();
        }

        decodeIndices(indices);

        return indices;
    }

    /**
     * Decodes the indices.
     *
     * @param indices the indices
     */
    private static void decodeIndices(short[] indices)
    {
        int highest = 0;
        for (int i = 0; i < indices.length; i++)
        {
            int code = indices[i];
            indices[i] = (short)(highest - code);
            if (code == 0)
            {
                highest++;
            }
        }
    }
}
