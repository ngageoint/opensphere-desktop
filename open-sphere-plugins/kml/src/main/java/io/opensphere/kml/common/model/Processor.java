package io.opensphere.kml.common.model;

import javax.xml.bind.JAXBException;

/**
 * Processor interface.
 *
 * @param <I> The input
 * @param <O> The output
 */
@FunctionalInterface
public interface Processor<I, O>
{
    /**
     * Process the input into output.
     *
     * @param input The input
     * @return The output
     * @throws JAXBException If a JAXBException occurs
     */
    O process(I input) throws JAXBException;
}
