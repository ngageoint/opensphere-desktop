package io.opensphere.core.event;

/**
 * An interface that provides the object which was the source of something.
 */
public interface SourceableEvent extends Event
{
    /**
     * Gets the source.
     *
     * @return the source
     */
    Object getSource();
}
