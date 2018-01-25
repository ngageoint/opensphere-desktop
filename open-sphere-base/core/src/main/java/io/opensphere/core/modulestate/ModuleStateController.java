package io.opensphere.core.modulestate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.util.collections.New;

/**
 * A controller for the current state of a module.
 */
public interface ModuleStateController
{
    /** Unqualified name of the state element. */
    String STATE_NAME = "state";

    /** The namespace for the state. */
    String STATE_NAMESPACE = "http://www.bit-sys.com/state/v2";

    /** The namespace for the state. */
    String STATE_NAMESPACE_V3 = "http://www.bit-sys.com/state/v3";

    /** The namespace for the state. */
    String STATE_NAMESPACE_V4 = "http://www.bit-sys.com/mist/state/v4";

    /** The prefix for the state namespace. */
    String STATE_NAMESPACE_PREFIX = "";

    /** Qualified name for the state element. */
    String STATE_QNAME = STATE_NAMESPACE_PREFIX + ":" + STATE_NAME;

    /** The known namespaces. */
    Collection<String> KNOWN_NAMESPACES = Collections
            .unmodifiableList(New.list(STATE_NAMESPACE, STATE_NAMESPACE_V3, STATE_NAMESPACE_V4));

    /**
     * Activate the state of the module from a DOM.
     *
     * @param id The identifier for this state.
     * @param description The description of this state.
     * @param tags The tags associated with this state.
     * @param node A DOM node that contains the state of the module to be
     *            activated.
     * @throws InterruptedException If activation is interrupted.
     */
    void activateState(String id, String description, Collection<? extends String> tags, Node node) throws InterruptedException;

    /**
     * Activate the state of the module from a state object.
     *
     * @param id The identifier for this state.
     * @param description The description of this state.
     * @param tags The tags associated with this state.
     * @param state A state object that contains the state of the module to be
     *            activated.
     * @throws InterruptedException If activation is interrupted.
     */
    void activateState(String id, String description, Collection<? extends String> tags, StateType state)
        throws InterruptedException;

    /**
     * Determine if a DOM node contains valid state for this controller.
     *
     * @param node The DOM node.
     * @return {@code true} if this controller can activate state in the given
     *         node.
     */
    boolean canActivateState(Node node);

    /**
     * Determine if a state object contains valid state for this controller.
     *
     * @param state The state object.
     * @return {@code true} if this controller can activate state in the given
     *         state object.
     */
    boolean canActivateState(StateType state);

    /**
     * Determine if this controller currently has state that can be saved.
     *
     * @return {@code true} if this controller can save state.
     */
    boolean canSaveState();

    /**
     * Deactivate the state of the module from a DOM.
     *
     * @param id The identifier for this state.
     * @param node A DOM node to contain the state of the module to be
     *            deactivated.
     * @throws InterruptedException If de-activation is interrupted.
     */
    void deactivateState(String id, Node node) throws InterruptedException;

    /**
     * Deactivate the state of the module from a state object.
     *
     * @param id The identifier for this state.
     * @param state A state object to contain the state of the module to be
     *            deactivated.
     * @throws InterruptedException If de-activation is interrupted.
     */
    void deactivateState(String id, StateType state) throws InterruptedException;

    /**
     * Gets the module state dependencies.
     *
     * @return the required state dependencies
     */
    List<? extends String> getRequiredStateDependencies();

    /**
     * Determines if this controller needs its activateState to be called every
     * time a state file is activated. If this is true this controllers activate
     * call will be called at the end.
     *
     * @return True if this controller needs to activate state all the time.
     */
    boolean isAlwaysActivateState();

    /**
     * Determines if this controller needs its saveState to be called every time
     * a state file is saved. If this is true this controller's save state will
     * be called at the end.
     *
     * @return True if this controller needs to save state all the time.
     */
    boolean isAlwaysSaveState();

    /**
     * Determine if this controller should save its state by default.
     *
     * @return {@code true} if this controller should save state by default.
     */
    boolean isSaveStateByDefault();

    /**
     * Save the current state of the module to a DOM.
     *
     * @param node A DOM node to contain the state of the module.
     */
    void saveState(Node node);

    /**
     * Save the current state of the module to the state object.
     *
     * @param state The state object to contain the state of the module.
     */
    void saveState(StateType state);
}
