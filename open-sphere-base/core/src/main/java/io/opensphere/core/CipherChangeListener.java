package io.opensphere.core;

/**
 * Listener for cipher change events.
 */
@FunctionalInterface
public interface CipherChangeListener
{
    /** Method called when the cipher is changed. */
    void cipherChanged();
}
