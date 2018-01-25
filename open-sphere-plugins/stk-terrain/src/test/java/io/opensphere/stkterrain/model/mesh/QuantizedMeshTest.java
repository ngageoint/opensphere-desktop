package io.opensphere.stkterrain.model.mesh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link QuantizedMesh}.
 */
public class QuantizedMeshTest
{
    /**
     * Tests java serializing the class.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testSerialize() throws IOException, ClassNotFoundException
    {
        QuantizedMesh input = new QuantizedMesh(ByteBuffer.wrap(createMeshByes()));

        QuantizedMesh actual = (QuantizedMesh)deserialize(serialize(input));

        final double delta = 0.000001;
//        Assert.assertEquals(1, actual.getHeader().getCenterX(), delta);
//        Assert.assertEquals(2, actual.getHeader().getCenterY(), delta);
//        Assert.assertEquals(3, actual.getHeader().getCenterZ(), delta);
        Assert.assertEquals(4, actual.getHeader().getMinHeight(), delta);
        Assert.assertEquals(5, actual.getHeader().getMaxHeight(), delta);
//        Assert.assertEquals(6, actual.getHeader().getBoundingSphereCenterX(), delta);
//        Assert.assertEquals(7, actual.getHeader().getBoundingSphereCenterY(), delta);
//        Assert.assertEquals(8, actual.getHeader().getBoundingSphereCenterZ(), delta);
//        Assert.assertEquals(9, actual.getHeader().getBoundingSphereRadius(), delta);
//        Assert.assertEquals(10, actual.getHeader().getHorizonOcclusionPointX(), delta);
//        Assert.assertEquals(11, actual.getHeader().getHorizonOcclusionPointY(), delta);
//        Assert.assertEquals(12, actual.getHeader().getHorizonOcclusionPointZ(), delta);

        Assert.assertEquals(2, actual.getVertexData().getVertexCount());
        Assert.assertEquals(50, actual.getVertexData().getU(0));
        Assert.assertEquals(20, actual.getVertexData().getU(1));
        Assert.assertEquals(0, actual.getVertexData().getV(0));
        Assert.assertEquals(32767, actual.getVertexData().getV(1));
        Assert.assertEquals(32767, actual.getVertexData().getHeight(0));
        Assert.assertEquals(0, actual.getVertexData().getHeight(1));

        Assert.assertEquals(6, actual.getIndexData().getIndexCount());
        Assert.assertEquals(0, actual.getIndexData().getIndex(0));
        Assert.assertEquals(1, actual.getIndexData().getIndex(1));
        Assert.assertEquals(2, actual.getIndexData().getIndex(2));
        Assert.assertEquals(3, actual.getIndexData().getIndex(3));
        Assert.assertEquals(2, actual.getIndexData().getIndex(4));
        Assert.assertEquals(1, actual.getIndexData().getIndex(5));

        Assert.assertNotNull(actual.getByteBuffer());
        Assert.assertEquals(input.getByteBuffer(), actual.getByteBuffer());

//        Assert.assertEquals(2, actual.getEdgeIndices().getWestIndices().getIndexCount());
//        Assert.assertEquals(0, actual.getEdgeIndices().getWestIndices().getIndex(0));
//        Assert.assertEquals(1, actual.getEdgeIndices().getWestIndices().getIndex(1));
//        Assert.assertEquals(2, actual.getEdgeIndices().getSouthIndices().getIndexCount());
//        Assert.assertEquals(2, actual.getEdgeIndices().getSouthIndices().getIndex(0));
//        Assert.assertEquals(3, actual.getEdgeIndices().getSouthIndices().getIndex(1));
//        Assert.assertEquals(2, actual.getEdgeIndices().getEastIndices().getIndexCount());
//        Assert.assertEquals(4, actual.getEdgeIndices().getEastIndices().getIndex(0));
//        Assert.assertEquals(5, actual.getEdgeIndices().getEastIndices().getIndex(1));
//        Assert.assertEquals(2, actual.getEdgeIndices().getNorthIndices().getIndexCount());
//        Assert.assertEquals(6, actual.getEdgeIndices().getNorthIndices().getIndex(0));
//        Assert.assertEquals(7, actual.getEdgeIndices().getNorthIndices().getIndex(1));
    }

    /**
     * Creates sample quantized mesh bytes.
     *
     * @return the bytes
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static byte[] createMeshByes()
    {
        byte[] bytes = new byte[152];
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        // Header
        buffer.putDouble(1);
        buffer.putDouble(2);
        buffer.putDouble(3);
        buffer.putFloat(4);
        buffer.putFloat(5);
        buffer.putDouble(6);
        buffer.putDouble(7);
        buffer.putDouble(8);
        buffer.putDouble(9);
        buffer.putDouble(10);
        buffer.putDouble(11);
        buffer.putDouble(12);

        // Vertex Data
        buffer.putInt(2);
        // u (100, 100-30)
        buffer.putShort((short)100).putShort((short)59);
        // v (0, 0+32767)
        buffer.putShort((short)0).putShort((short)65534);
        // height (32767, 32767-32767)
        buffer.putShort((short)65534).putShort((short)65533);

        // Index Data
        buffer.putInt(2);
        buffer.putShort((short)0).putShort((short)0).putShort((short)0);
        buffer.putShort((short)0).putShort((short)2).putShort((short)3);

        // Edge Indices
        // west
        buffer.putInt(2);
        buffer.putShort((short)0).putShort((short)1);
        // south
        buffer.putInt(2);
        buffer.putShort((short)2).putShort((short)3);
        // east
        buffer.putInt(2);
        buffer.putShort((short)4).putShort((short)5);
        // north
        buffer.putInt(2);
        buffer.putShort((short)6).putShort((short)7);

        return bytes;
    }

    /**
     * Serializes the object out to a ByteArrayOutputStream.
     *
     * @param o the object to serialize
     * @return the ByteArrayOutputStream
     * @throws IOException if an error occurs
     */
    private static ByteArrayOutputStream serialize(Object o) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(o);
        return out;
    }

    /**
     * De-serializes the object from the ByteArrayOutputStream.
     *
     * @param stream the ByteArrayOutputStream
     * @return the deserialized object
     * @throws IOException if an error occurs
     * @throws ClassNotFoundException if an error occurs
     */
    private static Object deserialize(ByteArrayOutputStream stream) throws IOException, ClassNotFoundException
    {
        return new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray())).readObject();
    }
}
