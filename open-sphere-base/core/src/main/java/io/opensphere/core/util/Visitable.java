package io.opensphere.core.util;

/**
 * Defines the contract for the Visitor Pattern (see "Design Patterns: Elements
 * of Resuable Object Oriented Software" and "Patterns in Java" Volume 1 for an
 * outline of the Visitor pattern) in which the configured information from the
 * user interface is collected by the supplied {@link Visitor}.
 */
public interface Visitable
{
    /**
     * Defines the contract for the Visitor Pattern (see "Design Patterns:
     * Elements of Resuable Object Oriented Software" and "Patterns in Java"
     * Volume 1 for an outline of the Visitor pattern) in which the configured
     * information from the user interface is collected by the supplied
     * {@link Visitor}.
     *
     * @param pVisitor the visitor used to collect information.
     */
    void visit(Visitor<?> pVisitor);
}
