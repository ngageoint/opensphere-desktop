package io.opensphere.mantle.data;

import io.opensphere.core.util.lang.CancellableThreePhaseProperty;

/** Listener for changes to an activation state property. */
public interface ActivationListener
        extends CancellableThreePhaseProperty.CancellableThreePhasePropertyListener<ActivationState, DataGroupActivationProperty>
{
}
