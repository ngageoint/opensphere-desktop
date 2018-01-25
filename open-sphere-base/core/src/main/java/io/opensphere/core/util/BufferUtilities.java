package io.opensphere.core.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import gnu.trove.list.TIntList;

/**
 * Utilities for working with NIO buffers. The methods provided here return
 * buffers which are indirectly allocated. For use with JOGL, indirect buffers
 * can only be used when the JOGL function being called uses the buffer
 * immediately. If the function requires a buffer for deferred use, it must be
 * allocated directly (com.jogamp.opengl.util.BufferUtil provides some
 * convenience methods for direct allocation). The JOGL documentation mentions
 * that function which require direct buffers will typically end with the word
 * "Pointer."
 */
public final class BufferUtilities
{
    /** Constant indicating if direct buffers are to be used. */
    private static final boolean DIRECT_BUFFERS = Boolean.getBoolean("opensphere.directBuffers");

    /**
     * Add a sequence of integers to a buffer.
     *
     * @param begin The begin.
     * @param count The count.
     * @param buf The buffer.
     */
    public static void addSequenceToBuffer(int begin, int count, IntBuffer buf)
    {
        for (int i = begin; i < count;)
        {
            buf.put(i++);
        }
    }

    /**
     * Create a new byte buffer that clones the contents of a given byte buffer.
     * The new byte buffer has independent storage. The position and limit are
     * copied, but the mark (if any) is not copied.
     *
     * @param buf The buffer to be cloned.
     * @return The new buffer.
     */
    public static ByteBuffer clone(ByteBuffer buf)
    {
        final ByteBuffer result = buf.isDirect() ? ByteBuffer.allocateDirect(buf.capacity()) : ByteBuffer.allocate(buf.capacity());
        final ByteBuffer dup = buf.duplicate();
        dup.rewind().limit(dup.capacity());
        result.put(dup);
        result.position(buf.position());
        result.limit(buf.limit());
        result.order(buf.order());
        return result;
    }

    /**
     * Allocate a new int buffer.
     *
     * @param capacity The size of the buffer.
     * @return The buffer.
     */
    public static ByteBuffer newByteBuffer(int capacity)
    {
        return newByteBuffer(capacity, DIRECT_BUFFERS);
    }

    /**
     * Allocate a new int buffer.
     *
     * @param capacity The size of the buffer.
     * @param direct If the result should be a direct buffer.
     * @return The buffer.
     */
    public static ByteBuffer newByteBuffer(int capacity, boolean direct)
    {
        final ByteBuffer buffer = direct ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }

    /**
     * Allocate a new float buffer.
     *
     * @param capacity The size of the buffer.
     * @return The buffer.
     */
    public static FloatBuffer newFloatBuffer(int capacity)
    {
        return DIRECT_BUFFERS ? newByteBuffer(Constants.FLOAT_SIZE_BYTES * capacity).asFloatBuffer()
                : FloatBuffer.allocate(capacity);
    }

    /**
     * Allocate a new int buffer.
     *
     * @param capacity The size of the buffer.
     * @return The buffer.
     */
    public static IntBuffer newIntBuffer(int capacity)
    {
        return DIRECT_BUFFERS ? newByteBuffer(Constants.INT_SIZE_BYTES * capacity).asIntBuffer() : IntBuffer.allocate(capacity);
    }

    /**
     * Create a byte buffer and fill it with a sequence of bytes.
     *
     * @param numberOfRepeats The number of times to repeat the sequence.
     * @param sequence The values in the sequence.
     * @return The buffer with position set to zero.
     */
    public static ByteBuffer newRepeatingByteBuffer(int numberOfRepeats, ByteBuffer sequence)
    {
        final ByteBuffer buf = newByteBuffer(numberOfRepeats * sequence.capacity());
        for (int i = 0; i < numberOfRepeats; i++)
        {
            buf.put((ByteBuffer)sequence.rewind());
        }
        return (ByteBuffer)buf.flip();
    }

    /**
     * Create an int buffer and fill it with sequential numbers.
     *
     * @param begin The first number to put in the buffer.
     * @param count How many numbers to put in.
     * @return The buffer with position set to zero.
     */
    public static IntBuffer newSequentialIntBuffer(int begin, int count)
    {
        final IntBuffer buf = newIntBuffer(count);
        addSequenceToBuffer(begin, count, buf);
        return (IntBuffer)buf.flip();
    }

    /**
     * Get a byte array containing the values in a {@code ByteBuffer} from
     * position zero to the limit.
     *
     * @param data The byte buffer.
     * @return The array.
     */
    public static byte[] toByteArray(ByteBuffer data)
    {
        final ByteBuffer dup = data.duplicate();
        dup.rewind();
        final byte[] arr = new byte[dup.limit()];
        dup.get(arr);
        return arr;
    }

    /**
     * Create an int buffer from an array of ints.
     *
     * @param array The array of integers.
     * @return The buffer with position set to zero.
     */
    public static IntBuffer toIntBuffer(int[] array)
    {
        final IntBuffer retIndices = newIntBuffer(array.length);
        for (final int index : array)
        {
            retIndices.put(index);
        }
        return (IntBuffer)retIndices.flip();
    }

    /**
     * Create an IntBuffer from a TIntList.
     *
     * @param list the list to convert.
     * @return The buffer with position set to zero.
     */
    public static IntBuffer toIntBuffer(TIntList list)
    {
        final IntBuffer retIndices = newIntBuffer(list.size());
        retIndices.put(list.toArray());
        return (IntBuffer)retIndices.flip();
    }

    /** Disallow class instantiation. */
    private BufferUtilities()
    {
    }
}
