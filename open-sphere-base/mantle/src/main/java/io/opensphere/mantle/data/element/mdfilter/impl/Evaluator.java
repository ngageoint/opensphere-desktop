package io.opensphere.mantle.data.element.mdfilter.impl;

/**
 * Interface for evaluating data values against filter values.
 */
@FunctionalInterface
public interface Evaluator
{
    /**
     * Evaluate.
     *
     * @param valueToEvaluate the value to evaluate
     * @return true, if successful
     */
    boolean evaluate(Object valueToEvaluate);
}
