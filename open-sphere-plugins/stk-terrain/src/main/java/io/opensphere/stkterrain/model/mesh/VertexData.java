package io.opensphere.stkterrain.model.mesh;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.util.lang.ToStringHelper;

/** Vertex Data. */
@SuppressWarnings("PMD.AvoidUsingShortType")
@Immutable
public class VertexData implements Serializable
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /** The vertex count. */
    private final int myVertexCount;

    /** The horizontal coordinate array. */
    private final short[] myU;

    /** The vertical coordinate array. */
    private final short[] myV;

    /** The height coordinate array. */
    private final short[] myHeight;

    /**
     * Constructor.
     *
     * @param buffer the byte buffer
     */
    public VertexData(ByteBuffer buffer)
    {
        myVertexCount = buffer.getInt();
        myU = decodeArray(buffer, myVertexCount);
        myV = decodeArray(buffer, myVertexCount);
        myHeight = decodeArray(buffer, myVertexCount);
    }

    /**
     * Gets the vertexCount.
     *
     * @return the vertexCount
     */
    public int getVertexCount()
    {
        return myVertexCount;
    }

    /**
     * Gets the U value.
     *
     * @param i the index
     * @return the U value
     */
    public short getU(int i)
    {
        return myU[i];
    }

    /**
     * Gets the V value.
     *
     * @param i the index
     * @return the V value
     */
    public short getV(int i)
    {
        return myV[i];
    }

    /**
     * Gets the height value.
     *
     * @param i the index
     * @return the height value
     */
    public short getHeight(int i)
    {
        return myHeight[i];
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("Count", myVertexCount);
        helper.add("U", Arrays.toString(myU));
        helper.add("V", Arrays.toString(myV));
        helper.add("H", Arrays.toString(myHeight));
        return helper.toStringMultiLine(1);
    }

    /**
     * Decodes a vertex data array.
     *
     * @param buffer the buffer
     * @param vertexCount the vertex count
     * @return the vertex data
     */
    private static short[] decodeArray(ByteBuffer buffer, int vertexCount)
    {
        short[] values = new short[vertexCount];
        int value = 0;
        for (int i = 0; i < vertexCount; i++)
        {
            value += decodeZigZag(buffer.getShort());
            values[i] = (short)value;
        }
        return values;
    }

    /**
     * Decodes a zig-zag encoded number.
     *
     * @param encoded the encoded number
     * @return the decoded number
     */
    private static int decodeZigZag(short encoded)
    {
        int unsignedEncoded = Short.toUnsignedInt(encoded);
        return unsignedEncoded >> 1 ^ -(unsignedEncoded & 1);
    }
}
