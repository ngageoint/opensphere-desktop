package io.opensphere.csvcommon.detect;

import io.opensphere.csvcommon.common.CellSampler;

/**
 * Interface for detectors of CSV file format parameters that use cells for
 * input.
 *
 * @param <T> The type of the return value
 */
@FunctionalInterface
public interface CellDetector<T>
{
    /**
     * Detect the parameter.
     *
     * @param sampler The line sampler.
     * @return The detected parameter.
     */
    ValuesWithConfidence<T> detect(CellSampler sampler);
}
