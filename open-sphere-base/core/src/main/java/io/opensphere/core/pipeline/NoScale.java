package io.opensphere.core.pipeline;

/**
 * A scale detector that doesn't scale anything.
 */
public class NoScale extends ScaleDetector
{
    /**
     * Constructor.
     */
    public NoScale()
    {
        super(null);
    }

    @Override
    public float getScale()
    {
        return 1.0f;
    }
}
