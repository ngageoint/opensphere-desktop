package io.opensphere.core.pipeline.renderer.buffered;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL;

import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.Immutable;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A list of {@link AbstractBufferObject}s that behaves like a
 * {@link AbstractBufferObject} by delegating calls to its subordinate buffer
 * objects.
 *
 * @param <E> The type of the buffer objects in the list.
 */
@Immutable
public class BufferObjectList<E extends BufferObject> extends AbstractList<E> implements BufferObject
{
    /** The buffer objects. */
    private final List<? extends E> myBufferObjects;

    /**
     * Convenience factory for creating a list with a single object.
     *
     * @param bufferObject The buffer object.
     * @return The list.
     *
     * @param <E> The type of the buffer objects.
     */
    public static <E extends BufferObject> BufferObjectList<E> create(E bufferObject)
    {
        return new BufferObjectList<E>(Collections.singletonList(bufferObject));
    }

    /**
     * Constructor that takes a list of buffer objects.
     *
     * @param bufferObjects The buffer objects.
     */
    public BufferObjectList(List<? extends E> bufferObjects)
    {
        myBufferObjects = New.unmodifiableList(bufferObjects);
    }

    @Override
    public void dispose(GL gl)
    {
        for (int index = 0; index < size();)
        {
            get(index++).dispose(gl);
        }
    }

    @Override
    public boolean draw(RenderContext rc, int drawMode)
    {
        boolean result = false;
        for (int index = 0; index < size();)
        {
            result |= get(index++).draw(rc, drawMode);
        }
        return result;
    }

    @Override
    public final boolean equals(Object obj)
    {
        // Use instance equals since these objects are used in the LRU cache.
        return Utilities.sameInstance(this, obj);
    }

    @Override
    public E get(int index)
    {
        return myBufferObjects.get(index);
    }

    /**
     * Get an unmodifiable view of my buffer objects.
     *
     * @return The list.
     */
    public List<? extends E> getBufferObjects()
    {
        return myBufferObjects;
    }

    @Override
    public long getSizeGPU()
    {
        long size = 0L;
        for (int index = 0; index < size();)
        {
            size += get(index++).getSizeGPU();
        }
        return size;
    }

    @Override
    public final int hashCode()
    {
        // Use system hashCode since these objects are used in the LRU cache.
        return System.identityHashCode(this);
    }

    @Override
    public int size()
    {
        return myBufferObjects.size();
    }
}
