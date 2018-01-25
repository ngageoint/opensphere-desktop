package io.opensphere.core.util;

import javax.xml.stream.events.StartElement;

/**
 * Inspects the start element and determines if the start element is what is
 * expected for serialization.
 *
 */
@FunctionalInterface
public interface StartElementInspector
{
    /**
     * Checks to see if the start element is what is expected.
     *
     * @param element The start element.
     * @return True if the start element is what is expected, false otherwise.
     */
    boolean isValidStartElement(StartElement element);
}
