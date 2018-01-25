package io.opensphere.core.appl;

/**
 * Entry point for the OpenSphere application.
 */
public final class OpenSphere
{
    /**
     * A static reference to the Kernel to prevent it from being
     * garbage-collected.
     */
    @SuppressWarnings("unused")
    private static final Kernel INSTANCE = new Kernel();

    /**
     * The main main method for the OpenSphere application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args)
    {
    }

    /** Disallow instantiation. */
    private OpenSphere()
    {
    }
}
