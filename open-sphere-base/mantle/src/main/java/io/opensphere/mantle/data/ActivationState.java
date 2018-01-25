package io.opensphere.mantle.data;

import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * The state of activation for a data group.
 */
public enum ActivationState
{
    /** The data group is activating. */
    ACTIVATING,

    /** The data group has been activated. */
    ACTIVE,

    /** The data is deactivating. */
    DEACTIVATING,

    /** The data group failed to activate. */
    ERROR,

    /** The data group is not active. */
    INACTIVE,

    ;

    /**
     * Determines if a transition from the given state to this state is legal.
     *
     * @param fromState The from state.
     * @return {@code true} if the transition is legal.
     */
    public boolean isLegalTransitionFrom(ActivationState fromState)
    {
        switch (this)
        {
            case ACTIVATING:
                return INACTIVE.equals(fromState) || ERROR.equals(fromState);
            case ACTIVE:
                return ACTIVATING.equals(fromState);
            case DEACTIVATING:
                return ACTIVE.equals(fromState) || ACTIVATING.equals(fromState);
            case ERROR:
                return ACTIVATING.equals(fromState);
            case INACTIVE:
                return DEACTIVATING.equals(fromState) || ACTIVATING.equals(fromState);
            default:
                throw new UnexpectedEnumException(this);
        }
    }
}
