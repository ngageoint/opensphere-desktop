package io.opensphere.csvcommon.detect.controller;

import java.io.FileNotFoundException;

import io.opensphere.csvcommon.common.LineSampler;

/**
 * Interface to the factory that creates line samplers.
 *
 */
@FunctionalInterface
public interface LineSamplerFactory
{
    /**
     * Creates the line sampler.
     *
     * @param textDelimiters The text delimiter for the reader to be aware of.
     * @return The newly create line sampler
     * @throws FileNotFoundException If the file cannot be opened.
     */
    LineSampler createSampler(char[] textDelimiters) throws FileNotFoundException;
}
