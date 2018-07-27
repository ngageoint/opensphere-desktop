package io.opensphere.overlay.controls;

/**
 * A marker interface used to identify an item used in control rendering.
 */
public interface ControlComponent
{
    /**
     * Gets the declared width of the component, in pixels.
     * 
     * @return the declared width of the component, in pixels.
     */
    int getWidth();

    /**
     * Gets the declared height of the component, in pixels.
     * 
     * @return the declared height of the component, in pixels.
     */
    int getHeight();
}
