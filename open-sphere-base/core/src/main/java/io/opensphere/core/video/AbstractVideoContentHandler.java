package io.opensphere.core.video;

/**
 * Abstract implementation with empty open/close methods.
 *
 * @param <T> The type of content handled.
 */
public abstract class AbstractVideoContentHandler<T> implements VideoContentHandler<T>
{
    @Override
    public void open()
    {
    }

    @Override
    public void close()
    {
    }
}
