package io.opensphere.core.control;

/**
 * The registry for {@link ControlContext}s.
 */
@FunctionalInterface
public interface ControlRegistry
{
    /** The control context for the globe. */
    String GLOBE_CONTROL_CONTEXT = "globe";

    /** The control context for the GL user interface. */
    String GLUI_CONTROL_CONTEXT = "glui";

    /**
     * Get a named control context.
     *
     * @param name The name of the control context.
     * @return The control context, or <code>null</code>.
     */
    ControlContext getControlContext(String name);
}
