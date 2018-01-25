package io.opensphere.core;

/** Manager for the splash screen. */
@FunctionalInterface
public interface SplashScreenManager
{
    /**
     * Add a message to the splash image.
     *
     * @param mesg The message to be displayed on the splash image.
     */
    void setInitMessage(String mesg);
}
