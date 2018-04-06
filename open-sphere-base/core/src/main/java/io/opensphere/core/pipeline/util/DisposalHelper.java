package io.opensphere.core.pipeline.util;

import com.jogamp.opengl.GL;

/**
 * Helper for on-card memory disposal handling.
 */
public interface DisposalHelper
{
    /**
     * Cleanup on card memory which is no longer needed. This will check the
     * reference queue for what to clean up. This relies on objects to be weakly
     * reachable, which may not happen until the garbage collector runs.
     *
     * @param gl The GL context.
     */
    void cleanOncardMemory(GL gl);

    /** Perform any necessary cleanup. */
    void close();

    /**
     * Force disposal of all on-card memory, referenced or not. This is used
     * when the GL context is disposed.
     *
     * @param gl The GL context.
     */
    void forceDispose(GL gl);

    /** Open the helper. */
    void open();
}
