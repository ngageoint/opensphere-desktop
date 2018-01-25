package io.opensphere.core.hud.widget;

import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;

/** It is sometime convenient to have an empty component. */
public class EmptyRenderable extends Renderable
{
    /**
     * Constructor.
     *
     * @param parent Parent Component.
     */
    public EmptyRenderable(Component parent)
    {
        super(parent);
    }
}
