package io.opensphere.core.appl;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.DiscreteEventAdapter;

/**
 * Initializer for default controls.
 */
class ControlInit
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Construct the initializer.
     *
     * @param toolbox The toolbox.
     */
    public ControlInit(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Add the garbage collection control.
     *
     * @param r A runnable to run when the control is activated.
     * @return The listener.
     */
    public BoundEventListener addGarbageCollectionControl(final Runnable r)
    {
        DiscreteEventAdapter gcRequestListener = new DiscreteEventAdapter("System", "Request Garbage Collect",
                "Requests that the JVM run the garbage collector")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                r.run();
            }
        };

        ControlContext context = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        context.addListener(gcRequestListener, new DefaultKeyPressedBinding(KeyEvent.VK_G));

        return gcRequestListener;
    }
}
