package io.opensphere.core.control.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.function.Supplier;

/**
 * A component supplier that simply provides the component given in its
 * constructor.
 */
public class DefaultComponentSupplier implements Supplier<Component>
{
    /** The component to be provided. */
    private final Component myComponent;

    /**
     * Constructor.
     *
     * @param component The component to be provided.
     */
    public DefaultComponentSupplier(Component component)
    {
        myComponent = component;
    }

    @Override
    public Component get()
    {
        assert EventQueue.isDispatchThread();
        return myComponent;
    }
}
