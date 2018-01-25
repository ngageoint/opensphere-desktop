package io.opensphere.core.pipeline.util;

/**
 * Interface for a class that receives repaint requests.
 */
@FunctionalInterface
public interface RepaintListener
{
    /**
     * Repaint the display.
     */
    void repaint();
}
