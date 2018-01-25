package io.opensphere.mantle.data.element.mdfilter.impl;

/** Evaluator for the IS_EMPTY condition. */
public class IsEmptyEvaluator extends AbstractEvaluator
{
    /**
     * Constructor.
     *
     * @param value The value to be evaluated.
     */
    public IsEmptyEvaluator(Object value)
    {
        super(value);
    }

    @Override
    public boolean evaluate(Object value)
    {
        return value == null || value instanceof String && ((String)value).isEmpty();
    }
}
