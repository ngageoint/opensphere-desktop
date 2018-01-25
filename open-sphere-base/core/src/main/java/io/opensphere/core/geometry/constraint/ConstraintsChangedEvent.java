package io.opensphere.core.geometry.constraint;

/** Event for when one or more render constraints have changed. */
public class ConstraintsChangedEvent
{
    /** The constraints object which has changed. */
    private final Constraints myRenderConstraints;

    /**
     * Constructor.
     *
     * @param props The constraints object which has changed.
     */
    public ConstraintsChangedEvent(Constraints props)
    {
        myRenderConstraints = props;
    }

    /**
     * Get the renderConstraints.
     *
     * @return the renderConstraints
     */
    public Constraints getRenderConstraints()
    {
        return myRenderConstraints;
    }
}
