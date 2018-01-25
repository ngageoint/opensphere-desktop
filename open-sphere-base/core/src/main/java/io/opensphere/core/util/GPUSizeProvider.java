package io.opensphere.core.util;

/**
 * An interface for resources that are being managed on a per byte level for
 * video card memory.
 */
@FunctionalInterface
public interface GPUSizeProvider
{
    /**
     * Get the video memory usage in bytes.
     *
     * @return The video memory usage in bytes.
     */
    long getSizeGPU();
}
