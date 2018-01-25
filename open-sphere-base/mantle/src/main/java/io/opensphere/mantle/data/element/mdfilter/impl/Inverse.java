package io.opensphere.mantle.data.element.mdfilter.impl;

/** Simplifies construction of inverse relations. */
public class Inverse implements Evaluator
{
    /** Original relation. */
    private final Evaluator delegate;

    /**
     * Create.
     *
     * @param e relation
     */
    public Inverse(Evaluator e)
    {
        delegate = e;
    }

    /** Invoke the delegate and invert. */
    @Override
    public boolean evaluate(Object rhs)
    {
        return !delegate.evaluate(rhs);
    }
}
