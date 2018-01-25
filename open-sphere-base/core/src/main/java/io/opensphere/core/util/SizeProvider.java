package io.opensphere.core.util;

/**
 * An interface for resources that are being managed on a per byte level.
 */
@FunctionalInterface
public interface SizeProvider
{
    /**
     * Get the estimated size of the object in bytes.
     *
     * @return The size.
     */
    long getSizeBytes();
}
