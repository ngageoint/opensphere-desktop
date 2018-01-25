package io.opensphere.core.control;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link ControlRegistry}.
 */
public final class ControlRegistryImpl implements ControlRegistry
{
    /** The default context names. */
    private static final String[] DEFAULT_CONTROL_CONTEXT_NAMES = { ControlRegistry.GLOBE_CONTROL_CONTEXT,
        ControlRegistry.GLUI_CONTROL_CONTEXT, };

    /** A map of control context names to contexts. */
    private final Map<String, ControlContextImpl> myControlContexts = new HashMap<>();

    /**
     * Construct the control registry implementation.
     */
    public ControlRegistryImpl()
    {
        for (String entry : DEFAULT_CONTROL_CONTEXT_NAMES)
        {
            myControlContexts.put(entry, new ControlContextImpl(entry));
        }
    }

    @Override
    public ControlContextImpl getControlContext(String name)
    {
        return myControlContexts.get(name);
    }
}
