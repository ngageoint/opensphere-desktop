package io.opensphere.server.state.activate;

import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.StateServerSourceController;

/**
 * Interface used for testing that simulates a class implementing both the
 * ServerSourceController and the StateServerSourceController.
 */
public interface ServerSourceStateController extends ServerSourceController, StateServerSourceController
{
}
