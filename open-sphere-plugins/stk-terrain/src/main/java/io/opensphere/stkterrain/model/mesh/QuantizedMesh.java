package io.opensphere.stkterrain.model.mesh;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.jcip.annotations.Immutable;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.util.lang.ToStringHelper;

/**
 * Stores the QuantizeMesh data and any other information needed.
 */
@Immutable
public class QuantizedMesh extends Image
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

//    /** The edge indices. */
//    private final EdgeIndices myEdgeIndices;

    /**
     * The quantized mesh bytes.
     */
    private final byte[] myByteBuffer;

    /** The header. */
    private final QuantizedMeshHeader myHeader;

    /** The index data. */
    private final Indices myIndexData;

    /** The vertex data. */
    private final VertexData myVertexData;

    /**
     * Constructs a new QuantizedMesh model.
     *
     * @param buffer The uncompressed QuantizedMesh buffer.
     */
    public QuantizedMesh(ByteBuffer buffer)
    {
        myByteBuffer = buffer.array();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        myHeader = new QuantizedMeshHeader(buffer);
        myVertexData = new VertexData(buffer);
        boolean is32bit = myVertexData.getVertexCount() > 65536;
        myIndexData = is32bit ? new IndexData32(buffer) : new IndexData16(buffer);
//        myEdgeIndices = is32bit ? new EdgeIndices32(buffer) : new EdgeIndices16(buffer);
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public ByteBuffer getByteBuffer()
    {
        return ByteBuffer.wrap(myByteBuffer);
    }

    @Override
    public ByteBuffer getByteBuffer(Rectangle region)
    {
        return null;
    }

//    /**
//     * Gets the edgeIndices.
//     *
//     * @return the edgeIndices
//     */
//    public EdgeIndices getEdgeIndices()
//    {
//        return myEdgeIndices;
//    }

    /**
     * Gets the header.
     *
     * @return the header
     */
    public QuantizedMeshHeader getHeader()
    {
        return myHeader;
    }

    @Override
    public int getHeight()
    {
        return 512;
    }

    /**
     * Gets the indexData.
     *
     * @return the indexData
     */
    public Indices getIndexData()
    {
        return myIndexData;
    }

    @Override
    public long getSizeInBytes()
    {
        return 0;
    }

    /**
     * Gets the vertexData.
     *
     * @return the vertexData
     */
    public VertexData getVertexData()
    {
        return myVertexData;
    }

    @Override
    public int getWidth()
    {
        return 512;
    }

    @Override
    public boolean isBlank()
    {
        return false;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("Header", myHeader);
        helper.add("VertexData", myVertexData);
        helper.add("IndexData", myIndexData);
//        helper.add("EdgeIndices", myEdgeIndices);
        return helper.toStringMultiLine();
    }

    @Override
    protected void setByteBuffer(ByteBuffer data, boolean usePool) throws ImageFormatUnknownException, IOException
    {
    }
}
