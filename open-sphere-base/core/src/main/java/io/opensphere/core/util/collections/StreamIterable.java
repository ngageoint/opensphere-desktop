package io.opensphere.core.util.collections;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * An {@link Iterable} wrapper for a {@link Stream}.
 *
 * @param <T> The type of the stream.
 */
public class StreamIterable<T> implements Iterable<T>
{
    /** The wrapped stream. */
    private final Stream<T> myStream;

    /**
     * Factory method.
     *
     * @param <T> The type of the stream.
     * @param stream The stream to be iterated.
     * @return The iterable.
     */
    public static <T> StreamIterable<T> get(Stream<T> stream)
    {
        return new StreamIterable<>(stream);
    }

    /**
     * Constructor.
     *
     * @param stream The stream.
     */
    public StreamIterable(Stream<T> stream)
    {
        myStream = stream;
    }

    @Override
    public Iterator<T> iterator()
    {
        return myStream.iterator();
    }
}
