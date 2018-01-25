package io.opensphere.kml.common.util;

import java.util.Collection;

import io.opensphere.kml.common.model.KMLFeature;

/**
 * Abstract KML feature accumulator.
 *
 * @param <T> The source of the features.
 */
public abstract class KMLFeatureAccumulator<T>
{
    /**
     * Accumulates a list of T objects.
     *
     * @param feature The feature
     * @param values The list of values to accumulate
     */
    public void accumulate(KMLFeature feature, Collection<? super T> values)
    {
        // Process this node
        if (process(feature, values))
        {
            // Accumulate the children
            for (KMLFeature childFeature : feature.getChildren())
            {
                accumulate(childFeature, values);
            }
        }
    }

    /**
     * Processes the node, possibly accumulating it.
     *
     * @param feature The feature
     * @param values The list of values to accumulate
     * @return Whether to continue processing the children
     */
    protected abstract boolean process(KMLFeature feature, Collection<? super T> values);
}
