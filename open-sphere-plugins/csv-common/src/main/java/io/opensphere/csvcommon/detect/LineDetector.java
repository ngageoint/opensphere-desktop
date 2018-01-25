package io.opensphere.csvcommon.detect;

import io.opensphere.csvcommon.common.LineSampler;

/**
 * Interface for detectors of CSV file format parameters that take lines for
 * input.
 *
 * @param <T> The type of the return value
 */
@FunctionalInterface
public interface LineDetector<T>
{
    /**
     * Detect the parameter.
     *
     * @param sampler The line sampler.
     * @return The detected parameter.
     */
    ValuesWithConfidence<? extends T> detect(LineSampler sampler);
}
