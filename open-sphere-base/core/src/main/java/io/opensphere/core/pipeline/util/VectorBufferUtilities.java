package io.opensphere.core.pipeline.util;

import java.nio.FloatBuffer;
import java.util.List;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.Vector3f;

/**
 * Utilities for making vectors work with NIO buffers. The methods provided here
 * return buffers which are indirectly allocated. For use with JOGL, indirect
 * buffers can only be used when the JOGL function being called uses the buffer
 * immediately. If the function requires a buffer for deferred use, it must be
 * allocated directly (com.jogamp.opengl.util.BufferUtil provides some
 * convenience methods for direct allocation). The JOGL documentation mentions
 * that function which require direct buffers will typically end with the word
 * "Pointer."
 */
public final class VectorBufferUtilities
{
    /**
     * Create a float buffer from a list of vectors. The coordinates for each
     * vector are put into the buffer in order.
     *
     * @param vectors The list of vectors.
     * @return The buffer with position set to zero.
     */
    public static FloatBuffer vec2dtoFloatBuffer(List<? extends Vector2d> vectors)
    {
        FloatBuffer retVerts = FloatBuffer.allocate(vectors.size() * 2);

        for (Vector2d vec : vectors)
        {
            retVerts.put((float)vec.getX());
            retVerts.put((float)vec.getY());
        }
        return retVerts.flip();
    }

    /**
     * Create a float buffer from an array of vectors. The coordinates for each
     * vector are put into the buffer in order.
     *
     * @param vectors The array of vectors.
     * @return The buffer with position set to zero.
     */
    public static FloatBuffer vec2dtoFloatBuffer(Vector2d[] vectors)
    {
        FloatBuffer retVerts = FloatBuffer.allocate(vectors.length * 2);

        for (Vector2d vec : vectors)
        {
            retVerts.put((float)vec.getX());
            retVerts.put((float)vec.getY());
        }
        return retVerts.flip();
    }

    /**
     * Create a float buffer from a list of vectors. The coordinates for each
     * vector are put into the buffer in order.
     *
     * @param vectors The list of vectors.
     * @return The buffer with position set to zero.
     */
    public static FloatBuffer vec3dToFloatBuffer(List<? extends Vector3d> vectors)
    {
        FloatBuffer retVerts = FloatBuffer.allocate(vectors.size() * 3);

        vec3dToFloatBuffer(vectors, retVerts);

        return retVerts.flip();
    }

    /**
     * Put the coordinates for the given vectors into the provided buffer at its
     * current position.
     *
     * @param vectors The list of vectors.
     * @param result The buffer to contain the coordinates.
     */
    public static void vec3dToFloatBuffer(List<? extends Vector3d> vectors, FloatBuffer result)
    {
        for (Vector3d vec : vectors)
        {
            result.put((float)vec.getX());
            result.put((float)vec.getY());
            result.put((float)vec.getZ());
        }
    }

    /**
     * Create a float buffer from an array of vectors. The coordinates for each
     * vector are put into the buffer in order.
     *
     * @param vectors The array of vectors.
     * @return The buffer with position set to zero.
     */
    public static FloatBuffer vec3dToFloatBuffer(Vector3d[] vectors)
    {
        FloatBuffer retVerts = FloatBuffer.allocate(vectors.length * 3);

        for (Vector3d vec : vectors)
        {
            retVerts.put((float)vec.getX());
            retVerts.put((float)vec.getY());
            retVerts.put((float)vec.getZ());
        }
        return retVerts.flip();
    }

    /**
     * Create a float buffer from a list of vectors. The coordinates for each
     * vector are put into the buffer in order.
     *
     * @param vectors The list of vectors.
     * @return The buffer with position set to zero.
     */
    public static FloatBuffer vec3fToFloatBuffer(List<Vector3f> vectors)
    {
        FloatBuffer retVerts = FloatBuffer.allocate(vectors.size() * 3);

        for (Vector3f vec : vectors)
        {
            retVerts.put(vec.getX());
            retVerts.put(vec.getY());
            retVerts.put(vec.getZ());
        }
        return retVerts.flip();
    }

    /**
     * Create a float buffer from an array of vectors. The coordinates for each
     * vector are put into the buffer in order.
     *
     * @param vectors The array of vectors.
     * @return The buffer with position set to zero.
     */
    public static FloatBuffer vec3fToFloatBuffer(Vector3f[] vectors)
    {
        FloatBuffer retVerts = FloatBuffer.allocate(vectors.length * 3);

        for (Vector3f vec : vectors)
        {
            retVerts.put(vec.getX());
            retVerts.put(vec.getY());
            retVerts.put(vec.getZ());
        }
        return retVerts.flip();
    }

    /** Disallow class instantiation. */
    private VectorBufferUtilities()
    {
    }
}
